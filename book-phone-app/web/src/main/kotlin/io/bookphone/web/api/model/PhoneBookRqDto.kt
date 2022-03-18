package io.bookphone.web.api.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @param phoneId
 * @param phoneVersion used for optimistic locking
 */
data class PhoneBookRqDto(

    @field:JsonProperty("phoneId", required = true) val phoneId: java.util.UUID,

    @field:JsonProperty("phoneVersion", required = true) val phoneVersion: java.time.OffsetDateTime? = null
)
