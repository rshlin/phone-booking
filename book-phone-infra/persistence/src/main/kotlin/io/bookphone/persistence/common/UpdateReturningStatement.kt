package io.bookphone.persistence.common

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import java.sql.ResultSet

/**
  currently RETURNING clause for INSERT/UPDATE/DELETE statements is not applicable out-of-the-box
  see https://github.com/JetBrains/Exposed/issues/1271
 */
class UpdateReturningStatement(
  table: Table,
  where: Op<Boolean>? = null,
) : UpdateStatement(table, null, where) {

  var resultRows: List<ResultRow> = listOf()
    private set

  override fun PreparedStatementApi.executeInternal(transaction: Transaction): Int {
    if (values.isEmpty()) return 0
    // executeUpdate is return only number of affected so it can't be used
    val updatedReturning = executeQuery()
    resultRows = ResultIterator(updatedReturning, targetsSet).iterator().asSequence().toList()

    return resultRows.size
  }

  override fun prepareSQL(transaction: Transaction): String {
    val sql = super.prepareSQL(transaction)
    return QueryBuilder(prepared = true).apply {
      append(sql)
      targetsSet.realFields.appendTo(prefix = " RETURNING ") {
        it.toQueryBuilder(this)
      }
    }.toString()
  }

  // copied from AbstractQuery
  private class ResultIterator(
    private val rs: ResultSet,
    fieldSet: FieldSet
  ) : Iterator<ResultRow> {
    private var hasNext: Boolean? = null
    private val fieldsIndex = fieldSet.realFields.toSet().mapIndexed { index, expression -> expression to index }.toMap()

    override operator fun next(): ResultRow {
      if (hasNext == null) hasNext()
      if (hasNext == false) throw NoSuchElementException()
      hasNext = null
      return ResultRow.create(rs, fieldsIndex)
    }

    override fun hasNext(): Boolean {
      if (hasNext == null) hasNext = rs.next()
      if (hasNext == false) rs.close()
      return hasNext!!
    }
  }
}
