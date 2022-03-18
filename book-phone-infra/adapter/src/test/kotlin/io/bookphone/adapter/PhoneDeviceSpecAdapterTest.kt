package io.bookphone.adapter

import arrow.core.getOrElse
import io.bookphone.adapter.phone.PhoneSpecAdapter
import io.bookphone.domain.phone.phoneName
import it.skrape.fetcher.request.UrlBuilder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

internal class PhoneDeviceSpecAdapterTest {

  private fun getPhoneSpecAdapter() =
    PhoneSpecAdapter(
      {},
      {
        host = "gsmarena.com"
        protocol = UrlBuilder.Protocol.HTTPS
        port = 443
      }
    )

  @Test
  fun `scrape existing phone spec`() {
    val phoneSpecAdapter = getPhoneSpecAdapter()
    val spec = runBlocking {
      phoneSpecAdapter.scrapePhoneSpecByExtId("xiaomi_12_pro-11287")
    }
    assertTrue { spec.isNotEmpty() }
    val phoneSpec = spec.getOrElse { fail() }
    assertTrue { phoneSpec.networkSpec.technology.equals("GSM / CDMA / HSPA / EVDO / LTE / 5G") }
    assertTrue { phoneSpec.networkSpec.net2g.equals("GSM 850 / 900 / 1800 / 1900 - SIM 1 & SIM 2") }
    assertTrue { phoneSpec.networkSpec.net3g.equals("HSDPA 800 / 850 / 900 / 1700(AWS) / 1900 / 2100") }
    assertTrue { phoneSpec.networkSpec.net4g.equals("1, 2, 3, 4, 5, 7, 8, 12, 13, 17, 18, 19, 20, 25, 26, 28, 32, 38, 39, 40, 41, 42, 48, 66 - International") }
  }

  @Test
  fun `scrape nonexistent phone spec`() {
    val phoneSpecAdapter = getPhoneSpecAdapter()
    val malformedSpec = runBlocking {
      phoneSpecAdapter.scrapePhoneSpecByExtId("xyz")
    }
    assertTrue { malformedSpec.isEmpty() }
  }

  @Test
  fun `scrape spec by name`() {
    val phoneSpecAdapter = getPhoneSpecAdapter()
    val result = runBlocking {
      phoneSpecAdapter.fetchSpecByName("iphone 13".phoneName())
    }
    assertEquals(4, result.size)
  }
}
