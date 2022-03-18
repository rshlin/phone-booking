package io.bookphone.persistence.common

import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

interface OptimisticLockingAware {
  val optimisticLockingVersion: Column<Instant>
}

fun <T> T.optimisticLockingAwareUpdate(
  where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null,
  version: Instant,
  limit: Int? = null,
  body: T.(UpdateStatement) -> Unit
): Int where T : Table, T : OptimisticLockingAware {
  val optimisticLockingAwareWhereClause: (SqlExpressionBuilder.() -> Op<Boolean>) = {
    val versionEquals = this@optimisticLockingAwareUpdate.optimisticLockingVersion eq version
    when (where != null) {
      true -> where(this).and(versionEquals)
      else -> versionEquals
    }
  }

  val updateVersionAwareStatement: T.(UpdateStatement) -> Unit = {
    body(it)
    it[this.optimisticLockingVersion] = CurrentTimestamp()
  }

  return this.update(optimisticLockingAwareWhereClause, limit, updateVersionAwareStatement)
}

fun <T> T.optimisticLockingAwareUpdateReturning(
  where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null,
  version: Instant,
  @Suppress("UNUSED_PARAMETER") limit: Int? = null,
  body: T.(UpdateReturningStatement) -> Unit
): List<ResultRow> where T : Table, T : OptimisticLockingAware {
  val optimisticLockingAwareWhereClause: (SqlExpressionBuilder.() -> Op<Boolean>) = {
    val versionEquals = this@optimisticLockingAwareUpdateReturning.optimisticLockingVersion eq version
    when (where != null) {
      true -> where(this).and(versionEquals)
      else -> versionEquals
    }
  }
  val statement = UpdateReturningStatement(this, SqlExpressionBuilder.run(optimisticLockingAwareWhereClause))

  val updateVersionAwareStatement: T.(UpdateReturningStatement) -> Unit = {
    it[this.optimisticLockingVersion] = CurrentTimestamp()
    body(it)
  }
  updateVersionAwareStatement(statement)

  statement.execute(TransactionManager.current())!!
  return statement.resultRows
}
