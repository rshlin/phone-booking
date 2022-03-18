package io.bookphone.web.book

import io.bookphone.domain.book.*
import io.bookphone.persistence.PhoneRepository
import io.bookphone.persistence.RentalRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BookingUseCaseConfig {
  @Bean
  fun bookPhone(phoneRepository: PhoneRepository, rentalRepository: RentalRepository) =
    object : BookPhoneUseCase {
      override val getPhoneDetails: GetPhoneDetails = phoneRepository::getPhoneAndAvailabilityById
      override val bookPhone: BookPhone = rentalRepository::bookPhone
    }

  @Bean
  fun returnPhone(rentalRepository: RentalRepository) =
    object : ReturnPhoneUseCase {
      override val getRentInfo: GetRentInfo = rentalRepository::getRentInfo
      override val returnPhone: ReturnPhone = rentalRepository::returnPhone
    }

  @Bean
  fun listBookings(rentalRepository: RentalRepository) =
    object : ListBookingsUseCase {
      override val listBookings: ListBookings = rentalRepository::listBookings
    }

  @Bean
  fun getBookingDetails(rentalRepository: RentalRepository) =
    object : GetBookingDetailsUseCase {
      override val getRentInfo: GetRentInfo = rentalRepository::getRentInfo
    }
}
