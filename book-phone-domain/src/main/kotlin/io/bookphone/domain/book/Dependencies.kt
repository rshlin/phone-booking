package io.bookphone.domain.book

import arrow.core.Either
import arrow.core.Option
import io.bookphone.domain.common.QueryFilter
import io.bookphone.domain.phone.PhoneDeviceDetails
import io.bookphone.domain.phone.DeviceId
import io.bookphone.domain.user.UserEmail
import kotlinx.coroutines.flow.Flow
import java.time.Instant

typealias GetPhoneDetails = suspend (phoneId: DeviceId, userEmail: UserEmail) -> Option<PhoneDeviceDetails>
typealias BookPhone = suspend (phoneId: DeviceId, phoneVersion: Instant, userEmail: UserEmail) -> Either<PhoneBookError, Rent>
typealias ReturnPhone = suspend (rentId: RentId, rentVersion: Instant) -> Either<PhoneReturnError, Rent>
typealias GetRentInfo = suspend (rentId: RentId, userEmail: UserEmail) -> Option<Rent>
typealias ListBookings = suspend (filter: QueryFilter, userEmail: UserEmail) -> Flow<Rent>
