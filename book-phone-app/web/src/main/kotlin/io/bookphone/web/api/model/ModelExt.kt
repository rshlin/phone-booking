package io.bookphone.web.api.model

import io.bookphone.domain.book.Rent
import io.bookphone.domain.phone.AvailabilityInfo
import io.bookphone.domain.phone.NetworkSpec
import io.bookphone.domain.phone.PhoneDeviceDetails
import io.bookphone.domain.phone.PhoneSpec
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun Instant.toDateTime(): OffsetDateTime = OffsetDateTime.ofInstant(this, ZoneOffset.systemDefault())

fun Rent.toResponse() = RentInfoResponseDto(
  id = id.value.toString(),
  phoneId = phoneId.value.toString(),
  userEmail = userEmail.value,
  rentDate = rentDate.toDateTime(),
  returnDate = returnDate.map { it.toDateTime() }.orNull()
)

fun PhoneDeviceDetails.toResponse() = PhoneDetailsResponseDto(
  id = id.value,
  name = name.value,
  version = phoneVersion.toDateTime(),
  availability = availability.toResponse(),
  spec = spec.map { it.toResponse() }.orNull()
)

fun AvailabilityInfo.toResponse() =
  when (this) {
    is AvailabilityInfo.Available -> PhoneAvailabilityDto(true, null)
    is AvailabilityInfo.Unavailable -> PhoneAvailabilityDto(
      false,
      PhoneAvailabilityDetailsDto(
        this.bookedAt.toDateTime(),
        this.bookedBy.value,
        this.bookId.map { it.value }.orNull()
      )
    )
  }

fun PhoneSpec.toResponse() = PhoneSpecDto(
  network.toResponse()
)

fun NetworkSpec.toResponse() = PhoneSpecNetworkDto(
  this.technology,
  this.net2g.map { it.value }.orNull(),
  this.net3g.map { it.value }.orNull(),
  this.net4g.map { it.value }.orNull(),
)
