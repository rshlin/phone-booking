package io.bookphone.web.api.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @param message
 */
data class ErrorResponseDto(

    @field:JsonProperty("message", required = true) val message: kotlin.String? = null
)
