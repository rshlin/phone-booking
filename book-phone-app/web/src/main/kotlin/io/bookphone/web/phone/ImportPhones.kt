package io.bookphone.web.phone

import arrow.core.getOrElse
import io.bookphone.domain.phone.AddPhoneToStockUseCase
import io.bookphone.domain.phone.AddPhoneUseCase
import io.bookphone.domain.phone.PhoneCommand
import io.bookphone.domain.phone.phoneName
import io.bookphone.domain.user.User
import io.bookphone.domain.user.userEmail
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Profile("import")
@ConstructorBinding
@ConfigurationProperties(prefix = "import")
data class PhoneImportProps(val phones: List<ImportPhone>)
data class ImportPhone(val name: String, val count: Int)

@Profile("import")
@Component
class ImportPhones(
  private val addPhoneUseCase: AddPhoneUseCase,
  private val addPhoneToStockUseCase: AddPhoneToStockUseCase,
  private val props: PhoneImportProps
) {

  @PostConstruct
  private fun import() {
    val user = User("abc@qaz.com".userEmail())
    runBlocking {
      newSuspendedTransaction {
        props.phones
          .map { cfg ->
            val (_, phoneId) = with(addPhoneUseCase) { PhoneCommand.AddPhoneCommand(cfg.name.phoneName()).runUseCase() }.getOrElse { throw java.lang.RuntimeException() }
            for (i in 1..cfg.count) {
              with(addPhoneToStockUseCase) { PhoneCommand.AddPhoneToStockCommand(phoneId, user).runUseCase() }.getOrElse { throw java.lang.RuntimeException() }
            }
          }
      }
    }
  }
}
