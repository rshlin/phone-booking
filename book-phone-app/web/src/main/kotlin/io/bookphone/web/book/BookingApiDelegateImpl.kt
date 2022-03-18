package io.bookphone.web.book

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import io.bookphone.domain.book.*
import io.bookphone.domain.common.QueryFilter
import io.bookphone.domain.phone.deviceId
import io.bookphone.web.api.BookingApiDelegate
import io.bookphone.web.api.model.PhoneBookRqDto
import io.bookphone.web.api.model.RentInfoResponseDto
import io.bookphone.web.api.model.ReturnPhoneRqDto
import io.bookphone.web.api.model.toResponse
import io.bookphone.web.exception.ResourceChangedException
import io.bookphone.web.exception.ResourceNotFoundException
import io.bookphone.web.exception.UnauthorizedToDiscoverResourceException
import io.bookphone.web.security.getAuthentication
import io.bookphone.web.security.toUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookingApiDelegateImpl(
  private val returnPhoneUseCase: ReturnPhoneUseCase,
  private val listBookingsUseCase: ListBookingsUseCase,
  private val getBookingDetailsUseCase: GetBookingDetailsUseCase,
  private val bookPhoneUseCase: BookPhoneUseCase
) : BookingApiDelegate {
  override suspend fun postBooking(phoneBookRqDto: PhoneBookRqDto?): ResponseEntity<RentInfoResponseDto> {
    val user = getAuthentication().toUser()
    require(phoneBookRqDto != null)
    require(phoneBookRqDto.phoneVersion != null)

    val cmd = BookCommand.BookPhoneCommand(
      phoneBookRqDto.phoneId.deviceId(),
      phoneBookRqDto.phoneVersion.toInstant(),
      user
    )

    val result = newSuspendedTransaction(Dispatchers.IO) {
      with(bookPhoneUseCase) { cmd.runUseCase() }
    }

    return when (result) {
      is Either.Left<PhoneBookError> -> when (result.value) {
        is PhoneBookError.NotFound -> throw ResourceNotFoundException
        is PhoneBookError.AlreadyBooked -> throw ResourceChangedException
      }
      is Either.Right<Rent> -> ResponseEntity.ok(result.value.toResponse())
    }
  }

  override suspend fun getBooking(limit: Int?, offset: Int?): ResponseEntity<Flow<RentInfoResponseDto>> {
    val user = getAuthentication().toUser()
    val cmd = BookCommand.ListBookingsCommand(QueryFilter(limit ?: 10, offset ?: 0), user)

    val result = newSuspendedTransaction(Dispatchers.IO) {
      with(listBookingsUseCase) { cmd.runUseCase() }
    }
    return ResponseEntity.ok().body(result.map { it.toResponse() })
  }

  override suspend fun getBookingDetails(bookingId: UUID): ResponseEntity<RentInfoResponseDto> {
    val user = getAuthentication().toUser()
    val cmd = BookCommand.GetBookingDetailsCommand(bookingId.rentId(), user)

    val result = newSuspendedTransaction(Dispatchers.IO) {
      with(getBookingDetailsUseCase) { cmd.runUseCase() }
    }
    return when (result) {
      is None -> throw ResourceNotFoundException
      is Some -> ResponseEntity.ok(result.value.toResponse())
    }
  }

  override suspend fun returnPhone(
    bookingId: String,
    returnPhoneRqDto: ReturnPhoneRqDto?
  ): ResponseEntity<RentInfoResponseDto> {
    require(returnPhoneRqDto != null)
    require(returnPhoneRqDto.rentVersion != null)
    val user = getAuthentication().toUser()
    val cmd = BookCommand.ReturnPhoneCommand(
      returnPhoneRqDto.rentId.rentId(),
      returnPhoneRqDto.rentVersion.toInstant(),
      user
    )
    val result = newSuspendedTransaction(Dispatchers.IO) {
      with(returnPhoneUseCase) { cmd.runUseCase() }
    }

    return when (result) {
      is Either.Left<PhoneReturnError> -> when (result.value) {
        is PhoneReturnError.NotFound -> throw ResourceNotFoundException
        is PhoneReturnError.RentedByAnotherUser -> throw UnauthorizedToDiscoverResourceException
        is PhoneReturnError.Changed -> throw ResourceChangedException
      }
      is Either.Right<Rent> -> ResponseEntity.ok(result.value.toResponse())
    }
  }
}
