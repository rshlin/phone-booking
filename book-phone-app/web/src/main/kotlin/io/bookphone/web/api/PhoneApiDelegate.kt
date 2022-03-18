package io.bookphone.web.api

import io.bookphone.web.api.model.PhoneDetailsResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.NativeWebRequest
import kotlinx.coroutines.flow.Flow

import java.util.Optional

/**
 * A delegate to be called by the {@link PhoneApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@javax.annotation.Generated(value = ["org.openapitools.codegen.languages.KotlinSpringServerCodegen"])
interface PhoneApiDelegate {

    fun getRequest(): Optional<NativeWebRequest> = Optional.empty()

    /**
     * @see PhoneApi#getPhoneDetails
     */
    suspend fun getPhoneDetails(phoneId: kotlin.String): ResponseEntity<PhoneDetailsResponseDto> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    /**
     * @see PhoneApi#getPhones
     */
    suspend fun getPhones(limit: kotlin.Int?,
        offset: kotlin.Int?): ResponseEntity<Flow<PhoneDetailsResponseDto>> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }
}
