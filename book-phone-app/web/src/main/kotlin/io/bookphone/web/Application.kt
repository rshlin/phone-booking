package io.bookphone.web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import javax.annotation.PostConstruct

@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = ["io.bookphone.web"])
class Application {
  @Autowired
  lateinit var mapper: ObjectMapper

  @PostConstruct
  fun configureMapper() {
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
