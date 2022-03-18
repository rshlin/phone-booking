package io.bookphone.persistence.common

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import java.sql.ResultSet

/**
 * See https://github.com/JetBrains/Exposed/issues/423
 */
fun <T : Any> Transaction.execCTE(stmt: String, transform: (ResultSet) -> T): T? {
  if (stmt.isEmpty()) return null

  val type = StatementType.SELECT

  return exec(object : Statement<T>(type, emptyList()) {
    override fun PreparedStatementApi.executeInternal(transaction: Transaction): T? {
      executeQuery()
      return resultSet?.let { rs ->
        rs.use { transform(it) }
      }
    }

    override fun prepareSQL(transaction: Transaction): String = stmt

    override fun arguments(): Iterable<Iterable<Pair<ColumnType, Any?>>> = emptyList()
  })
}
