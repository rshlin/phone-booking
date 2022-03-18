package io.bookphone.web.security

import io.bookphone.domain.user.User
import io.bookphone.domain.user.userEmail
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder

suspend fun getAuthentication(): Authentication = ReactiveSecurityContextHolder.getContext().awaitSingle().authentication

fun Authentication.toUser(): User = User(name.userEmail())
