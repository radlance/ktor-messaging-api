package com.github.radlance.ktormessagingapi.plugins

import com.auth0.jwt.exceptions.JWTVerificationException
import com.github.radlance.ktormessagingapi.domain.auth.LoginUser
import com.github.radlance.ktormessagingapi.domain.auth.RegisterUser
import com.github.radlance.ktormessagingapi.exception.MissingCredentialException
import com.github.radlance.ktormessagingapi.exception.NoAccessException
import com.github.radlance.ktormessagingapi.exception.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureValidation() {
    install(RequestValidation) {
        validate<RegisterUser> { user ->
            when {
                !user.email.matchesEmailRegex() -> {
                    ValidationResult.Invalid("Invalid email format")
                }

                user.displayName.length !in (4..30) -> {
                    ValidationResult.Invalid("Username should be of min 4 and max 30 character in length")
                }

                user.password.length !in (8..50) -> {
                    ValidationResult.Invalid("Password should be of min 8 and max 50 character in length")
                }

                else -> ValidationResult.Valid
            }
        }

        validate<LoginUser> { user ->
            when {
                !user.email.matchesEmailRegex() -> {
                    ValidationResult.Invalid("Invalid email format")
                }

                user.password.length !in (8..50) -> {
                    ValidationResult.Invalid("Password should be of min 8 and max 50 character in length")
                }

                else -> ValidationResult.Valid
            }
        }
    }

    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respondText(status = HttpStatusCode.BadRequest, text = cause.message ?: "Bad Credentials")
        }

        exception<BadRequestException> { call, _ ->
            call.respondText(status = HttpStatusCode.BadRequest, text = "Bad Request")
        }

        exception<MissingCredentialException> { call, cause ->
            call.respondText(status = HttpStatusCode.BadRequest, text = cause.message)
        }

        exception<JWTVerificationException> { call, cause ->
            call.respondText(status = HttpStatusCode.Unauthorized, text = cause.message!!)
        }

        exception<UnauthorizedException> { call, cause ->
            cause.message?.let { call.respondText(status = HttpStatusCode.Unauthorized, text = it) } ?: run {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        exception<NoAccessException> { call, cause ->
            cause.message?.let { call.respondText(status = HttpStatusCode.Forbidden, text = it) } ?: run {
                call.respond(HttpStatusCode.Forbidden)
            }
        }

        exception<Throwable> { call, cause ->
            call.respondText(status = HttpStatusCode.InternalServerError, text = "Internal Server Error: $cause")
        }
    }
}

private fun String.matchesEmailRegex(): Boolean {
    return "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$".toRegex().matches(this)
}