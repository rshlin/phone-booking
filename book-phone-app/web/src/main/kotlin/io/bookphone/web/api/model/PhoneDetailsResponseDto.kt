package io.bookphone.web.api.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @param id
 * @param name
 * @param version
 * @param availability
 * @param spec
 */
data class PhoneDetailsResponseDto(

    @field:JsonProperty("id", required = true) val id: java.util.UUID? = null,

    @field:JsonProperty("name", required = true) val name: kotlin.String? = null,

    @field:JsonProperty("version", required = true) val version: java.time.OffsetDateTime? = null,

    @field:JsonProperty("availability", required = true) val availability: PhoneAvailabilityDto,

    @field:JsonProperty("spec") val spec: PhoneSpecDto? = null
)
