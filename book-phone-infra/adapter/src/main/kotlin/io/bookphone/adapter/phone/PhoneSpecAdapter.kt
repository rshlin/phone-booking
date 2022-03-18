package io.bookphone.adapter.phone

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import io.bookphone.domain.phone.PhoneName
import io.bookphone.domain.phone.PhoneSpec
import io.bookphone.domain.phone.phoneName
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc

class PhoneSpecAdapter(
  private val configureHttpClient: ConfigureHttpClient,
  private val configureUrl: ConfigurePhoneSpecUrl
) {

  suspend fun fetchSpecByName(phoneName: PhoneName): List<Pair<PhoneSpec, PhoneName>> {
    val specRefs = scrapeSpecRefs(phoneName)

    val result = specRefs.mapNotNull { (slug, name) ->
      val spec = fetchSpec(slug)
      if (spec is Some) spec.value to name.phoneName() else null
    }

    return result
  }

  private suspend fun scrapeSpecRefs(phoneName: PhoneName) = skrape(AsyncFetcher) {
    request {
      configureHttpClient()
      url {
        configureUrl()
        path = "res.php3?sSearch=${phoneName.value}"
      }
    }
    response {
      when (status { code }) {
        200 -> {
          kotlin.runCatching {
            htmlDocument {
              findFirst("div#review-body") {
                findAll("ul>li>a") {
                  map { it.attribute("href") to it.children[1].text }
                }
              }
            }
          }.getOrElse { listOf() }
        }
        else -> listOf()
      }
    }
  }

  suspend fun fetchSpec(extId: String): Option<PhoneSpec> =
    scrapePhoneSpecByExtId(extId).map { PhoneSpec.fromDto(it) }

  suspend fun scrapePhoneSpecByExtId(extId: String): Option<PhoneSpecDto> =
    skrape(AsyncFetcher) {
      request {
        configureHttpClient()
        url {
          configureUrl()
          path = "$extId.php"
        }
      }
      response {
        when (status { code }) {
          200 -> kotlin.runCatching {
            htmlDocument(html = responseBody, baseUri = baseUri).extractSpec()
          }.getOrDefault(None)
          else -> None
        }
      }
    }
}

private fun Doc.extractSpec(): Option<PhoneSpecDto> {
  val technology = "a[data-spec='nettech']" {
    findFirst { text }
  }
  val net2g = "td[data-spec='net2g']" {
    findFirst { text }
  }
  val net3g = "td[data-spec='net3g']" {
    findFirst { text }
  }
  val net4g = "td[data-spec='net4g']" {
    findFirst { text }
  }
  return if (technology.isEmpty()) None
  else PhoneSpecDto(
    networkSpec = NetworkSpecDto(
      technology = technology,
      net2g = net2g,
      net3g = net3g,
      net4g = net4g
    )
  ).toOption()
}
