package io.bookphone.domain.book

import arrow.core.Option
import io.bookphone.domain.phone.DeviceId
import io.bookphone.domain.user.UserEmail
import java.time.Instant
import java.util.*

@JvmInline
value class RentId(val value: UUID)

fun UUID.rentId() = RentId(this)

data class Rent(
    val id: RentId,
    val phoneId: DeviceId,
    val userEmail: UserEmail,
    val rentDate: Instant,
    val returnDate: Option<Instant>
) { companion object }

fun Rent.isAssignedToAnother(userEmail: UserEmail) = this.userEmail != userEmail && returnDate.isEmpty()
