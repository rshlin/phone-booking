package io.bookphone.adapter.phone

import it.skrape.fetcher.Request
import it.skrape.fetcher.request.UrlBuilder

typealias ConfigureHttpClient = Request.() -> Unit
typealias ConfigurePhoneSpecUrl = UrlBuilder.() -> Unit
