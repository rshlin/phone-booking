package io.bookphone.domain.phone

import arrow.core.*
import io.bookphone.domain.common.QueryFilter
import io.bookphone.domain.phone.PhoneCommand.GetPhoneCommand
import io.bookphone.domain.phone.PhoneCommand.GetPhonesCommand
import io.bookphone.domain.user.User
import kotlinx.coroutines.flow.Flow

sealed class PhoneCommand {
  data class GetPhonesCommand(val filter: QueryFilter, val user: User) : PhoneCommand()
  data class GetPhoneCommand(val phoneId: DeviceId, val user: User) : PhoneCommand()
  data class AddPhoneCommand(val phoneName: PhoneName) : PhoneCommand()
  data class AddPhoneToStockCommand(val phoneId: PhoneId, val user: User) : PhoneCommand()
}

sealed class AddPhoneToStockError {
  object NotFound : AddPhoneToStockError()
}

sealed class AddPhoneError {
  object NotFound : AddPhoneError()
  object AlreadyExists : AddPhoneError()
}

interface GetPhonesUseCase {
  val getPhones: GetPhones

  suspend fun GetPhonesCommand.runUseCase(): Flow<PhoneDeviceDetails> = getPhones(filter, user.email)
}

interface GetPhoneUseCase {
  val getPhone: GetPhone

  suspend fun GetPhoneCommand.runUseCase(): Option<PhoneDeviceDetails> = getPhone(phoneId, user.email)
}

interface AddPhoneToStockUseCase {
  val phoneExistsById: PhoneExistsById
  val savePhoneDevice: SavePhoneDevice

  suspend fun PhoneCommand.AddPhoneToStockCommand.runUseCase(): Either<AddPhoneToStockError, PhoneDeviceDetails> {
    val cmd = this

    if (!phoneExistsById(cmd.phoneId)) return AddPhoneToStockError.NotFound.left()
    return savePhoneDevice(cmd.phoneId, user.email)
  }
}

interface AddPhoneUseCase {
  val phoneExists: PhoneExistsByName
  val fetchPhoneSpec: FetchPhoneSpec
  val phoneSpecService: PhoneSpecService
  val savePhone: SavePhone

  suspend fun PhoneCommand.AddPhoneCommand.runUseCase(): Either<AddPhoneError, Pair<PhoneDevice, PhoneId>> {
    val cmd = this
    if (phoneExists(cmd.phoneName)) return AddPhoneError.AlreadyExists.left()

    val spec = with(phoneSpecService) { fetchPhoneSpec(cmd.phoneName).getBestSpec(cmd.phoneName) }
      .toEither<AddPhoneError> { AddPhoneError.NotFound }

    return spec.flatMap { savePhone(cmd.phoneName, it) }
  }
}
