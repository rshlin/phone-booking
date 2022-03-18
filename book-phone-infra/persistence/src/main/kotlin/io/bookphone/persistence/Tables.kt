package io.bookphone.persistence

import io.bookphone.persistence.common.OptimisticLockingAware
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PhoneTable : UUIDTable("phone"), OptimisticLockingAware {
  val name = varchar("name", 50)
  val spec = text("spec").nullable()
  val updatedAt = timestamp("updated_at")
  override val optimisticLockingVersion: Column<Instant> = updatedAt
}

object InventoryTable : UUIDTable("inventory"), OptimisticLockingAware {
  val phoneId = uuid("phone_id").references(PhoneTable.id)
  val updatedAt = timestamp("updated_at")
  override val optimisticLockingVersion: Column<Instant> = updatedAt
}

object RentalTable : UUIDTable("rental"), OptimisticLockingAware {
  val inventoryId = uuid("inventory_id").references(InventoryTable.id)
  val userEmail = varchar("user_email", 50)
  val rentAt = timestamp("rent_at").defaultExpression(CurrentTimestamp())
  val returnAt = timestamp("return_at").nullable()
  val updatedAt = timestamp("updated_at")
  override val optimisticLockingVersion: Column<Instant> = updatedAt
}
