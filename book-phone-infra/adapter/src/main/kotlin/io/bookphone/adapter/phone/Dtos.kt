package io.bookphone.adapter.phone

import arrow.core.None
import arrow.core.Option
import arrow.core.toOption
import io.bookphone.domain.phone.LTEBands
import io.bookphone.domain.phone.NetworkSpec
import io.bookphone.domain.phone.PhoneSpec
import kotlinx.serialization.Serializable

@Serializable
data class PhoneSpecDto(val networkSpec: NetworkSpecDto) { companion object }

@Serializable
data class NetworkSpecDto(
  val technology: String,
  val net2g: String?,
  val net3g: String?,
  val net4g: String?
)
private fun String?.safeToLTEBands(): Option<LTEBands> {
  return this?.toLTEBands() ?: None
}

private fun String.toLTEBands(): Option<LTEBands> {
  return if (isEmpty()) None
  else LTEBands(this).toOption()
}

fun PhoneSpec.Companion.fromDto(dto: PhoneSpecDto) = PhoneSpec(
  network = NetworkSpec(
    technology = dto.networkSpec.technology,
    net2g = dto.networkSpec.net2g.safeToLTEBands(),
    net3g = dto.networkSpec.net3g.safeToLTEBands(),
    net4g = dto.networkSpec.net4g.safeToLTEBands()
  )
)

fun PhoneSpecDto.Companion.fromDomain(spec: PhoneSpec) = PhoneSpecDto(
  networkSpec = NetworkSpecDto(
    technology = spec.network.technology,
    net2g = spec.network.net2g.map { it.value }.orNull(),
    net3g = spec.network.net3g.map { it.value }.orNull(),
    net4g = spec.network.net4g.map { it.value }.orNull()
  )
)
