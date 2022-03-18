package io.bookphone.web.api.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @param network
 */
data class PhoneSpecDto(

    @field:JsonProperty("network") val network: PhoneSpecNetworkDto? = null
)
