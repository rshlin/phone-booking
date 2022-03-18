package io.bookphone.web.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.util.Optional
@javax.annotation.Generated(value = ["org.openapitools.codegen.languages.KotlinSpringServerCodegen"])

@Controller
@RequestMapping("\${openapi.bookPhone.base-path:/api/v1}")
class BookingApiController(
        @org.springframework.beans.factory.annotation.Autowired(required = false) delegate: BookingApiDelegate?
) : BookingApi {
    private val delegate: BookingApiDelegate

    init {
        this.delegate = Optional.ofNullable(delegate).orElse(object : BookingApiDelegate {})
    }

    override fun getDelegate(): BookingApiDelegate = delegate
}
