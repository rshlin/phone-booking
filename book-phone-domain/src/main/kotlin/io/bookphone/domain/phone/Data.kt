package io.bookphone.domain.phone

import arrow.core.Option
import io.bookphone.domain.book.RentId
import io.bookphone.domain.user.UserEmail
import java.time.Instant
import java.util.*

@JvmInline
value class PhoneId(val value: UUID)

public fun UUID.phoneId() = PhoneId(this)

@JvmInline
value class DeviceId(val value: UUID)

public fun UUID.deviceId() = DeviceId(this)

@JvmInline
value class PhoneName(val value: String)

fun String.phoneName() = PhoneName(this)

interface PhoneDevice {
  val id: DeviceId
  val phoneVersion: Instant
  val name: PhoneName
}

data class PhoneDeviceWithAvailabilityInfo(
  val availability: AvailabilityInfo,
  private val phoneDevice: PhoneDevice
) : PhoneDevice by phoneDevice { companion object }

data class PhoneDeviceDetails(
  val spec: Option<PhoneSpec>,
  val availability: AvailabilityInfo,
  private val phoneDevice: PhoneDevice
) : PhoneDevice by phoneDevice {
  companion object
}

sealed class AvailabilityInfo {
  object Available : AvailabilityInfo()
  data class Unavailable(
    val bookId: Option<RentId>,
    val bookedAt: Instant,
    val bookedBy: UserEmail
  ) : AvailabilityInfo()
  companion object
}

fun AvailabilityInfo.isBookedByAnother(userEmail: UserEmail) = this is AvailabilityInfo.Unavailable && this.bookedBy != userEmail
fun AvailabilityInfo.isAlreadyBookedBy(userEmail: UserEmail) = this is AvailabilityInfo.Unavailable && this.bookedBy == userEmail

@JvmInline
value class LTEBands(val value: String)

data class PhoneSpec(
  val network: NetworkSpec
) { companion object }

data class NetworkSpec(
  val technology: String,
  val net4g: Option<LTEBands>,
  val net3g: Option<LTEBands>,
  val net2g: Option<LTEBands>
)
