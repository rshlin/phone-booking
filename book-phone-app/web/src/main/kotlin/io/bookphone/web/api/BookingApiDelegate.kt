package io.bookphone.web.api

import io.bookphone.web.api.model.PhoneBookRqDto
import io.bookphone.web.api.model.RentInfoResponseDto
import io.bookphone.web.api.model.ReturnPhoneRqDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.NativeWebRequest
import kotlinx.coroutines.flow.Flow

import java.util.Optional

/**
 * A delegate to be called by the {@link BookingApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@javax.annotation.Generated(value = ["org.openapitools.codegen.languages.KotlinSpringServerCodegen"])
interface BookingApiDelegate {

    fun getRequest(): Optional<NativeWebRequest> = Optional.empty()

    /**
     * @see BookingApi#getBooking
     */
    suspend fun getBooking(limit: kotlin.Int?,
        offset: kotlin.Int?): ResponseEntity<Flow<RentInfoResponseDto>> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    /**
     * @see BookingApi#getBookingDetails
     */
    suspend fun getBookingDetails(bookingId: java.util.UUID): ResponseEntity<RentInfoResponseDto> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    /**
     * @see BookingApi#postBooking
     */
    suspend fun postBooking(phoneBookRqDto: PhoneBookRqDto?): ResponseEntity<RentInfoResponseDto> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    /**
     * @see BookingApi#returnPhone
     */
    suspend fun returnPhone(bookingId: kotlin.String,
        returnPhoneRqDto: ReturnPhoneRqDto?): ResponseEntity<RentInfoResponseDto> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }
}
