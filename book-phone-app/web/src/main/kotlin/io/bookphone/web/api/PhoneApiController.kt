package io.bookphone.web.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.util.Optional
@javax.annotation.Generated(value = ["org.openapitools.codegen.languages.KotlinSpringServerCodegen"])

@Controller
@RequestMapping("\${openapi.bookPhone.base-path:/api/v1}")
class PhoneApiController(
        @org.springframework.beans.factory.annotation.Autowired(required = false) delegate: PhoneApiDelegate?
) : PhoneApi {
    private val delegate: PhoneApiDelegate

    init {
        this.delegate = Optional.ofNullable(delegate).orElse(object : PhoneApiDelegate {})
    }

    override fun getDelegate(): PhoneApiDelegate = delegate
}
