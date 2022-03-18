package io.bookphone.web.api.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @param available
 * @param details
 */
data class PhoneAvailabilityDto(

    @field:JsonProperty("available", required = true) val available: kotlin.Boolean? = null,

    @field:JsonProperty("details") val details: PhoneAvailabilityDetailsDto? = null
)
