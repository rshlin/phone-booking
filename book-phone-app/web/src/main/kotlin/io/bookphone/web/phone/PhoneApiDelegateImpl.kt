package io.bookphone.web.phone

import io.bookphone.domain.common.QueryFilter
import io.bookphone.domain.phone.*
import io.bookphone.web.api.PhoneApiDelegate
import io.bookphone.web.api.model.PhoneDetailsResponseDto
import io.bookphone.web.api.model.toResponse
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
class PhoneApiDelegateImpl(
  private val getPhoneUseCase: GetPhoneUseCase,
  private val getPhonesUseCase: GetPhonesUseCase,
) : PhoneApiDelegate {

  override suspend fun getPhoneDetails(phoneId: String): ResponseEntity<PhoneDetailsResponseDto> {
    val cmd = PhoneCommand.GetPhoneCommand(UUID.fromString(phoneId).deviceId(), getAuthentication().toUser())

    val result = newSuspendedTransaction(Dispatchers.IO) {
      with(getPhoneUseCase) { cmd.runUseCase() }
    }
    return result.fold(
      { ResponseEntity.notFound().build() },
      { phoneDetails -> ResponseEntity.ok(phoneDetails.toResponse()) }
    )
  }

  override suspend fun getPhones(limit: Int?, offset: Int?): ResponseEntity<Flow<PhoneDetailsResponseDto>> {
    val cmd = PhoneCommand.GetPhonesCommand(
      QueryFilter(limit ?: 10, offset ?: 0),
      getAuthentication().toUser()
    )
    val result = newSuspendedTransaction(Dispatchers.IO) {
      with(getPhonesUseCase) { cmd.runUseCase() }
    }
    return ResponseEntity.ok(result.map { it.toResponse() })
  }
}
