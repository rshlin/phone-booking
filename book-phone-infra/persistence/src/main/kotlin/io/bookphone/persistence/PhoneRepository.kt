package io.bookphone.persistence

import arrow.core.*
import arrow.core.computations.ResultEffect.bind
import io.bookphone.adapter.phone.PhoneSpecDto
import io.bookphone.adapter.phone.fromDomain
import io.bookphone.adapter.phone.fromDto
import io.bookphone.domain.book.rentId
import io.bookphone.domain.common.QueryFilter
import io.bookphone.domain.phone.*
import io.bookphone.domain.user.UserEmail
import io.bookphone.domain.user.userEmail
import io.bookphone.persistence.common.execCTE
import kotlinx.coroutines.flow.*
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

private data class PhoneDeviceData(
  override val id: DeviceId,
  override val phoneVersion: Instant,
  override val name: PhoneName
) : PhoneDevice {
  companion object {
    fun fromRow(row: ResultRow): PhoneDeviceData =
      PhoneDeviceData(
        id = row[PhoneTable.id].value.deviceId(),
        name = row[PhoneTable.name].phoneName(),
        phoneVersion = row[PhoneTable.updatedAt].toJavaInstant(),
      )
  }
}

class PhoneRepository {
  suspend fun getPhoneAndAvailabilityById(
    deviceId: DeviceId,
    userEmail: UserEmail
  ): Option<PhoneDeviceDetails> =
    with(TransactionManager.current()) {
      //language=POSTGRES-SQL
      execCTE(
        """
WITH open_rent AS 
(
    SELECT *
    FROM rental
    WHERE 
        rent_at=(
            SELECT MAX(rent_at) 
            FROM rental 
            WHERE inventory_id = '${deviceId.value}'
        )
    AND return_at IS NULL
)
SELECT 
    i.id as device_id, 
    i.updated_at as updated_at,
    p.id as phone_id, 
    p.name as phone_name, 
    p.spec as phone_spec,
    open_rent.user_email as user_email,
    open_rent.rent_at as rent_at,
    open_rent.return_at as return_at,
    case when open_rent.user_email = '${userEmail.value}' then open_rent.id
        else null end as rent_id
FROM inventory i
LEFT JOIN open_rent ON i.id = open_rent.inventory_id
INNER JOIN phone p ON p.id = i.phone_id
WHERE i.id = '${deviceId.value}'
      """.trimIndent()
      ) { PhoneDeviceDetails.fromRow(it) }
        .toOption()
    }

  suspend fun fetchPhoneAndAvailability(filter: QueryFilter, userEmail: UserEmail): Flow<PhoneDeviceDetails> =
    with(TransactionManager.current()) {
      //language=POSTGRES-SQL
      execCTE(
        """
WITH last_rent AS 
(
    SELECT *, RANK() OVER(PARTITION BY inventory_id ORDER BY rent_at DESC) AS r
    FROM rental
)
SELECT 
    i.id as device_id, 
    i.updated_at as updated_at,
    p.name as phone_name, 
    p.spec as phone_spec,
    last_rent.user_email as user_email,
    last_rent.rent_at as rent_at,
    last_rent.return_at as return_at,
    case when last_rent.user_email = '${userEmail.value}' then last_rent.id
        else null end as rent_id
FROM inventory i 
LEFT JOIN last_rent ON i.id = last_rent.inventory_id AND last_rent.return_at is null AND last_rent.r = 1
INNER JOIN phone p ON p.id = i.phone_id
LIMIT ${filter.limit} OFFSET ${filter.offset}
""".trimIndent()
      ) { PhoneDeviceDetails.fromRows(it) }!!
    }

  suspend fun phoneExistsByName(phoneName: PhoneName): Boolean =
    with(TransactionManager.currentOrNew(Connection.TRANSACTION_READ_COMMITTED)) {
      PhoneTable.select { PhoneTable.name eq phoneName.value }
        .singleOrNone()
        .isNotEmpty()
    }

  suspend fun phoneExistsById(phoneId: PhoneId): Boolean =
    with(TransactionManager.currentOrNew(Connection.TRANSACTION_READ_COMMITTED)) {
      PhoneTable.select { PhoneTable.id eq phoneId.value }
        .singleOrNone()
        .isNotEmpty()
    }

  suspend fun savePhone(phoneName: PhoneName, phoneSpec: PhoneSpec): Either<AddPhoneError, Pair<PhoneDevice, PhoneId>> =
    with(TransactionManager.currentOrNew(Connection.TRANSACTION_READ_COMMITTED)) {
      val phoneResult = PhoneTable.insert {
        it[id] = UUID.randomUUID()
        it[this.spec] = kotlin.runCatching { Json.encodeToString(PhoneSpecDto.fromDomain(phoneSpec)) }.getOrNull()
        it[this.name] = phoneName.value
      }
      val phoneId = phoneResult.resultedValues!!.first()[PhoneTable.id].value
      PhoneTable.select { PhoneTable.id eq phoneId }
        .singleOrNone()
        .map { PhoneDeviceData.fromRow(it) to phoneId.phoneId() }
        .toEither { AddPhoneError.NotFound }
    }

  suspend fun saveDevice(phoneId: PhoneId, userEmail: UserEmail): Either<AddPhoneToStockError, PhoneDeviceDetails> =
    with(TransactionManager.current()) {
      val id = InventoryTable.insert { it[this.phoneId] = phoneId.value }
        .resultedValues.toOption()
        .flatMap { it.singleOrNone() }
        .map { it[InventoryTable.id].value.deviceId() }

      id.flatMap { deviceId -> getPhoneAndAvailabilityById(deviceId, userEmail) }
        .toEither { AddPhoneToStockError.NotFound }
    }
}

private fun PhoneDeviceDetails.Companion.fromRows(rs: ResultSet): Flow<PhoneDeviceDetails> =
  generateSequence {
    if (rs.next()) PhoneDeviceDetails.fromRow(rs) else null
  }.toList()
    .asFlow()

private fun PhoneDeviceDetails.Companion.fromRow(rs: ResultSet): PhoneDeviceDetails {
  if (rs.row == 0) rs.next()
  val userEmail = rs.getString("user_email")
  val rentId =
    kotlin.runCatching { rs.getObject("rent_id", UUID::class.java) }.getOrNull().toOption().map { it.rentId() }
  val availabilityInfo = when (userEmail) {
      null -> AvailabilityInfo.Available
      else -> AvailabilityInfo.Unavailable(
        bookId = rentId,
        bookedAt = rs.getTimestamp("rent_at").toInstant(),
        bookedBy = userEmail.userEmail()
      )
  }
  return PhoneDeviceDetails(
    availability = availabilityInfo,
    phoneDevice = PhoneDeviceData(
      id = rs.getObject("device_id", UUID::class.java).deviceId(),
      name = rs.getString("phone_name").phoneName(),
      phoneVersion = rs.getTimestamp("updated_at").toInstant()
    ),
    spec = rs.getString("phone_spec").toOption().map {
      val dto = kotlin.runCatching { Json.decodeFromString<PhoneSpecDto>(it) }.bind()
      PhoneSpec.fromDto(dto)
    }
  )
}
