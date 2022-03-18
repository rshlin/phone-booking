package io.bookphone.web.infra

import io.bookphone.persistence.PhoneRepository
import io.bookphone.persistence.RentalRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConstructorBinding
@ConfigurationProperties(prefix = "db")
data class DbProps(
  val url: String,
  val user: String,
  val password: String
)

@Configuration
class PersistenceConfig(
  private val props: DbProps
) {

  @Bean
  fun db(): Database =
    Database.connect(
      url = props.url,
      user = props.user,
      password = props.password,
    ).also {
      TransactionManager.defaultDatabase = it
    }

  @Bean
  fun phoneRepo(db: Database): PhoneRepository = PhoneRepository()

  @Bean
  fun bookRepo(db: Database): RentalRepository = RentalRepository()
}
