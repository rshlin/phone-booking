package io.bookphone.web.phone

import io.bookphone.adapter.phone.PhoneSpecAdapter
import io.bookphone.domain.phone.*
import io.bookphone.persistence.PhoneRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class PhoneUseCaseConfig {
  @Bean
  fun getPhones(repo: PhoneRepository) =
    object : GetPhonesUseCase {
      override val getPhones: GetPhones = repo::fetchPhoneAndAvailability
    }

  @Bean
  fun getPhone(repo: PhoneRepository) =
    object : GetPhoneUseCase {
      override val getPhone: GetPhone = repo::getPhoneAndAvailabilityById
    }
}

@Profile("import")
@Configuration
class ImportConfiguration {
  @Bean
  fun specService() =
    object : PhoneSpecService {
      override val levenshteinDistance: LevenshteinDistance = { s1, s2 ->
        org.apache.commons.text.similarity.LevenshteinDistance.getDefaultInstance().apply(s1, s2)
      }
    }

  @Bean
  fun createPhone(
    repo: PhoneRepository,
    specAdapter: PhoneSpecAdapter
  ) = object : AddPhoneUseCase {
    override val phoneExists: PhoneExistsByName = repo::phoneExistsByName
    override val fetchPhoneSpec: FetchPhoneSpec = specAdapter::fetchSpecByName
    override val phoneSpecService: PhoneSpecService = specService()
    override val savePhone: SavePhone = repo::savePhone
  }

  @Bean
  fun createDevice(
    repo: PhoneRepository
  ) = object : AddPhoneToStockUseCase {
    override val phoneExistsById: PhoneExistsById = repo::phoneExistsById
    override val savePhoneDevice: SavePhoneDevice = repo::saveDevice
  }
}
