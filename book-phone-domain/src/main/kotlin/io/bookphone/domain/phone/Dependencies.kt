package io.bookphone.domain.phone

import arrow.core.Either
import arrow.core.Option
import io.bookphone.domain.common.QueryFilter
import io.bookphone.domain.user.UserEmail
import kotlinx.coroutines.flow.Flow

typealias GetPhones = suspend (QueryFilter, UserEmail) -> Flow<PhoneDeviceDetails>
typealias GetPhone = suspend (DeviceId, UserEmail) -> Option<PhoneDeviceDetails>
typealias PhoneExistsByName = suspend (PhoneName) -> Boolean
typealias PhoneExistsById = suspend (PhoneId) -> Boolean
typealias FetchPhoneSpec = suspend (PhoneName) -> List<Pair<PhoneSpec, PhoneName>>
typealias SavePhoneDevice = suspend (PhoneId, UserEmail) -> Either<AddPhoneToStockError, PhoneDeviceDetails>
typealias SavePhone = suspend (PhoneName, PhoneSpec) -> Either<AddPhoneError, Pair<PhoneDevice, PhoneId>>

typealias LevenshteinDistance = (String, String) -> Int
