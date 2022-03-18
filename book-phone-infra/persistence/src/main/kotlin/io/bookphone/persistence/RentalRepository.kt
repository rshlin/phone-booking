package io.bookphone.persistence

import arrow.core.*
import io.bookphone.domain.book.*
import io.bookphone.domain.common.QueryFilter
import io.bookphone.domain.phone.DeviceId
import io.bookphone.domain.phone.deviceId
import io.bookphone.domain.user.UserEmail
import io.bookphone.domain.user.userEmail
import io.bookphone.persistence.common.optimisticLockingAwareUpdate
import io.bookphone.persistence.common.optimisticLockingAwareUpdateReturning
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import java.time.Instant
import java.util.*

class RentalRepository {
  suspend fun getRentInfo(rentId: RentId, userEmail: UserEmail): Option<Rent> =
    with(TransactionManager.current()) {
      RentalTable
        .select { (RentalTable.id eq rentId.value) and (RentalTable.userEmail eq userEmail.value) }
        .singleOrNone()
        .map { Rent.fromRow(it) }
    }

  suspend fun bookPhone(deviceId: DeviceId, phoneVersion: Instant, userEmail: UserEmail): Either<PhoneBookError, Rent> =
    with(TransactionManager.current()) {

      val result = InventoryTable.optimisticLockingAwareUpdate(
        { InventoryTable.id eq deviceId.value },
        version = phoneVersion.toKotlinInstant()
      ) {}
      when (result) {
        0 -> {
          rollback()
          PhoneBookError.AlreadyBooked.left()
        }
        else -> createRent(deviceId, userEmail).right()
      }
    }

  private fun createRent(phoneId: DeviceId, userEmail: UserEmail): Rent {
    val result = RentalTable.insert {
      it[id] = UUID.randomUUID()
      it[inventoryId] = phoneId.value
      it[this.userEmail] = userEmail.value
    }
    return result.resultedValues!!
      .first()
      .let { Rent.fromRow(it) }
  }

  suspend fun listBookings(filter: QueryFilter, userEmail: UserEmail): Flow<Rent> =
    with(TransactionManager.currentOrNew(Connection.TRANSACTION_READ_COMMITTED)) {
      RentalTable.select { RentalTable.userEmail eq userEmail.value }
        .orderBy(RentalTable.returnAt to SortOrder.DESC_NULLS_FIRST, RentalTable.rentAt to SortOrder.DESC)
        .limit(filter.limit, filter.offset.toLong())
        .map { Rent.fromRow(it) }
        .asFlow()
    }

  suspend fun returnPhone(rentId: RentId, rentVersion: Instant): Either<PhoneReturnError, Rent> =
    with(TransactionManager.currentOrNew(Connection.TRANSACTION_READ_COMMITTED)) {
      val updatedRows =
        RentalTable.optimisticLockingAwareUpdateReturning({ RentalTable.id eq rentId.value }, version = rentVersion.toKotlinInstant()) {
          it[returnAt] = CurrentTimestamp()
        }
      when (updatedRows.size) {
        0 -> {
          rollback()
          PhoneReturnError.Changed.left()
        }
        else -> updatedRows.first().let { Rent.fromRow(it).right() }
      }
    }
}

private fun Rent.Companion.fromRow(row: ResultRow) = Rent(
  id = row[RentalTable.id].value.rentId(),
  phoneId = row[RentalTable.inventoryId].deviceId(),
  userEmail = row[RentalTable.userEmail].userEmail(),
  rentDate = row[RentalTable.rentAt].toJavaInstant(),
  returnDate = row[RentalTable.returnAt].toOption()
)

private fun kotlinx.datetime.Instant?.toOption(): Option<Instant> {
  return this?.toJavaInstant().toOption()
}
