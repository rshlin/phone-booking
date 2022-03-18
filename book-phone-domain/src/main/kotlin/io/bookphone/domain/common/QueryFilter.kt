package io.bookphone.domain.common

data class QueryFilter(
  val limit: Int = 10,
  val offset: Int = 0
)
