package io.bookphone.domain.user

@JvmInline value class UserEmail(val value: String)

fun String.userEmail() = UserEmail(this)

data class User(
  val email: UserEmail
)
