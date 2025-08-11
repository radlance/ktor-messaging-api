package com.github.radlance.ktormessagingapi.util

import com.github.radlance.ktormessagingapi.exception.MissingCredentialException
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*

suspend inline fun <reified T : Any> ApplicationCall.receiveOrThrow(): T {
    return kotlin.runCatching { receive<T>() }.getOrElse { throw MissingCredentialException() }
}

inline fun <reified T : Any> JWTPrincipal.getClaimOrThrow(name: String): T {
    return getClaim(name, T::class) ?: throw MissingCredentialException()
}