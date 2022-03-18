package io.bookphone.web.api.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @param technology
 * @param net2g
 * @param net3g
 * @param net4g
 */
data class PhoneSpecNetworkDto(

    @field:JsonProperty("technology", required = true) val technology: kotlin.String? = null,

    @field:JsonProperty("net2g") val net2g: kotlin.String? = null,

    @field:JsonProperty("net3g") val net3g: kotlin.String? = null,

    @field:JsonProperty("net4g") val net4g: kotlin.String? = null
)
