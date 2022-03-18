package io.bookphone.web.api.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @param rentId
 * @param rentVersion used for optimistic locking
 */
data class ReturnPhoneRqDto(

    @field:JsonProperty("rentId", required = true) val rentId: java.util.UUID,

    @field:JsonProperty("rentVersion", required = true) val rentVersion: java.time.OffsetDateTime? = null
)
