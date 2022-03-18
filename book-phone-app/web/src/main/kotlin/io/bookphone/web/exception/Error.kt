package io.bookphone.web.exception

import com.fasterxml.jackson.databind.ObjectMapper
import io.bookphone.web.api.model.ErrorResponseDto
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.HttpStatus

object ResourceNotFoundException : RuntimeException("not found")
object ResourceChangedException : RuntimeException("resource changed")
object UnauthorizedToDiscoverResourceException : RuntimeException("not found")

fun Throwable.serialize(mapper: ObjectMapper, bufferFactory: DataBufferFactory): Pair<DataBuffer, HttpStatus> {
  val dataBuffer = bufferFactory.wrap(mapper.writeValueAsBytes(ErrorResponseDto(message)))
  val status = when (this) {
    is IllegalArgumentException -> HttpStatus.BAD_REQUEST
    is ResourceNotFoundException -> HttpStatus.NOT_FOUND
    is ResourceChangedException -> HttpStatus.CONFLICT
    is UnauthorizedToDiscoverResourceException -> HttpStatus.NOT_FOUND
    else -> HttpStatus.INTERNAL_SERVER_ERROR
  }
  return dataBuffer to status
}
