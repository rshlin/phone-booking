package io.bookphone.web.api.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * can be null if phone is available to book
 * @param bookedAt
 * @param bookedBy
 * @param bookId can be null if requester does not own the booking
 */
data class PhoneAvailabilityDetailsDto(

    @field:JsonProperty("bookedAt", required = true) val bookedAt: java.time.OffsetDateTime? = null,

    @field:JsonProperty("bookedBy", required = true) val bookedBy: kotlin.String? = null,

    @field:JsonProperty("bookId") val bookId: java.util.UUID? = null
)
