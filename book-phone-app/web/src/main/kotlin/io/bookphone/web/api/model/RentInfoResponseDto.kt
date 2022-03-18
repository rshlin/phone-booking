package io.bookphone.web.api.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @param id
 * @param phoneId
 * @param userEmail
 * @param rentDate
 * @param returnDate
 */
data class RentInfoResponseDto(

    @field:JsonProperty("id", required = true) val id: kotlin.String,

    @field:JsonProperty("phoneId", required = true) val phoneId: kotlin.String,

    @field:JsonProperty("userEmail", required = true) val userEmail: kotlin.String,

    @field:JsonProperty("rentDate", required = true) val rentDate: java.time.OffsetDateTime,

    @field:JsonProperty("returnDate") val returnDate: java.time.OffsetDateTime? = null
)
