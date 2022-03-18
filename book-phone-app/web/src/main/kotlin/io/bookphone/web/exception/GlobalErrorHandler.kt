package io.bookphone.web.exception

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class GlobalErrorHandler(
  private val mapper: ObjectMapper
) : ErrorWebExceptionHandler {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
    val (body, status) = ex.serialize(mapper, exchange.response.bufferFactory())
    if (status == HttpStatus.INTERNAL_SERVER_ERROR) log.error("Server failure", ex)

    exchange.response.statusCode = status
    return exchange.response.writeWith(Mono.just(body))
  }
}
