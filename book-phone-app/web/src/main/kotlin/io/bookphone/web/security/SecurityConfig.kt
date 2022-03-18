package io.bookphone.web.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import java.nio.file.Files
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

@EnableWebFluxSecurity
class SecurityConfig {
  @Value("classpath:jwt.public.pem")
  lateinit var publicKey: Resource

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    http
      .authorizeExchange()
        .anyExchange().authenticated()
        .and()
      .oauth2ResourceServer()
        .jwt()

    return http.build()
  }

  @Bean
  fun jwtDecoder(): ReactiveJwtDecoder {
    val pubKeyFileContent = Files.readAllBytes(publicKey.file.toPath()).decodeToString()

    val publicKey = KeyFactory.getInstance("RSA")
      .generatePublic(X509EncodedKeySpec(getKeySpec(pubKeyFileContent))) as RSAPublicKey
    return NimbusReactiveJwtDecoder.withPublicKey(publicKey)
      .signatureAlgorithm(SignatureAlgorithm.RS256).build() as ReactiveJwtDecoder
  }

  private fun getKeySpec(keyValue: String): ByteArray? {
    val plainKv = keyValue.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
    return Base64.getMimeDecoder().decode(plainKv)
  }
}
