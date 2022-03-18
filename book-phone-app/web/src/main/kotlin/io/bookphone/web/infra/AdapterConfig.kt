package io.bookphone.web.infra

import io.bookphone.adapter.phone.ConfigureHttpClient
import io.bookphone.adapter.phone.ConfigurePhoneSpecUrl
import io.bookphone.adapter.phone.PhoneSpecAdapter
import it.skrape.fetcher.request.UrlBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConstructorBinding
@ConfigurationProperties(prefix = "phone-spec")
data class PhoneSpecAdapterProps(val host: String, val protocol: String, val port: Int)

@Configuration
class AdapterConfig(
  private val adapterProps: PhoneSpecAdapterProps
) {

  @Bean
  fun specUrl(): ConfigurePhoneSpecUrl = {
    protocol = UrlBuilder.Protocol.valueOf(adapterProps.protocol)
    host = adapterProps.host
    port = adapterProps.port
  }

  @Bean
  fun httpClient(): ConfigureHttpClient = {}

  @Bean
  fun phoneSpec(): PhoneSpecAdapter = PhoneSpecAdapter(httpClient(), specUrl())
}
