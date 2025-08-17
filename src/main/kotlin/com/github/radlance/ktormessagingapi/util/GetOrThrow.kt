package com.github.radlance.ktormessagingapi.util

import com.github.radlance.ktormessagingapi.exception.MissingCredentialException
import com.github.radlance.ktormessagingapi.exception.UnauthorizedException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*

suspend inline fun <reified T : Any> ApplicationCall.receiveOrThrow(): T {
    return runCatching { receive<T>() }.getOrElse { throw MissingCredentialException() }
}

inline fun <reified T : Any> ApplicationCall.claimByNameOrElse(name: String, action: () -> Nothing): T {
    val principal = principal<JWTPrincipal>()
    return principal?.getClaim(name, T::class) ?: action()
}

fun ApplicationCall.chatIdParameterOrThrow(): Int {
    return parameters["chatId"]?.toIntOrNull() ?: run {
        throw MissingCredentialException("Missing chat id parameter")
    }
}

inline fun <reified T : Any> ApplicationCall.claimByNameOrUnauthorized(name: String): T {
    return claimByNameOrElse<T>(name = name) {
        throw UnauthorizedException()
    }
}