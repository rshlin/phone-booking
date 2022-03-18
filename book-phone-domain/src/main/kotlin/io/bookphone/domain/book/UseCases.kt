package io.bookphone.domain.book

import arrow.core.*
import arrow.core.computations.either
import io.bookphone.domain.book.BookCommand.*
import io.bookphone.domain.common.QueryFilter
import io.bookphone.domain.common.toEither
import io.bookphone.domain.phone.DeviceId
import io.bookphone.domain.phone.isAlreadyBookedBy
import io.bookphone.domain.phone.isBookedByAnother
import io.bookphone.domain.user.User
import kotlinx.coroutines.flow.Flow
import java.time.Instant

sealed class PhoneBookError {
  object NotFound : PhoneBookError()
  object AlreadyBooked : PhoneBookError()
}

sealed class PhoneReturnError {
  object NotFound : PhoneReturnError()
  object RentedByAnotherUser : PhoneReturnError()
  object Changed : PhoneReturnError()
}

sealed class BookCommand {
  data class BookPhoneCommand(val deviceId: DeviceId, val deviceVersion: Instant, val user: User) : BookCommand()
  data class ListBookingsCommand(val filter: QueryFilter, val user: User) : BookCommand()
  data class GetBookingDetailsCommand(val rentId: RentId, val user: User) : BookCommand()
  data class ReturnPhoneCommand(val rentId: RentId, val rentVersion: Instant, val user: User) : BookCommand()
}

interface BookPhoneUseCase {
  val getPhoneDetails: GetPhoneDetails
  val bookPhone: BookPhone

  suspend fun BookPhoneCommand.runUseCase(): Either<PhoneBookError, Rent> {
    val cmd = this

    return either<PhoneBookError, Unit> {
      val phone = getPhoneDetails(cmd.deviceId, cmd.user.email).toEither { PhoneBookError.NotFound }.bind()
      if (phone.availability.isBookedByAnother(cmd.user.email) || phone.availability.isAlreadyBookedBy(cmd.user.email))
        PhoneBookError.AlreadyBooked
    }
      .flatMap { bookPhone(cmd.deviceId, cmd.deviceVersion, cmd.user.email) }
  }
}

interface GetBookingDetailsUseCase {
  val getRentInfo: GetRentInfo

  suspend fun GetBookingDetailsCommand.runUseCase(): Option<Rent> =
    getRentInfo(rentId, user.email)
}

interface ListBookingsUseCase {
  val listBookings: ListBookings

  suspend fun ListBookingsCommand.runUseCase(): Flow<Rent> =
    listBookings(filter, user.email)
}

interface ReturnPhoneUseCase {
  val getRentInfo: GetRentInfo
  val returnPhone: ReturnPhone

  suspend fun ReturnPhoneCommand.runUseCase(): Either<PhoneReturnError, Rent> {
    val cmd = this
    return either {
      val rent = getRentInfo(cmd.rentId, cmd.user.email).toEither { PhoneReturnError.NotFound }.bind()
      rent.isAssignedToAnother(cmd.user.email).toEither { PhoneReturnError.RentedByAnotherUser }.bind()

      when (rent.returnDate) {
        is Some<Instant> -> rent
        is None -> returnPhone(rentId, rentVersion).bind()
      }
    }
  }
}
