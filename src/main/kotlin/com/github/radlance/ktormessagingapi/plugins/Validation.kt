package com.github.radlance.ktormessagingapi.plugins

import com.github.radlance.ktormessagingapi.domain.auth.LoginUser
import com.github.radlance.ktormessagingapi.domain.auth.RegisterUser
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
            call.respondText(text = cause.message ?: "Bad Credentials", status = HttpStatusCode.BadRequest)
        }

        exception<BadRequestException> { call, _ ->
            call.respondText(text = "Bad Request", status = HttpStatusCode.BadRequest)
        }

        exception<Throwable> { call, cause ->
            call.respondText(text = "Internal Server Error: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
}

private fun String.matchesEmailRegex(): Boolean {
    return "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$".toRegex().matches(this)
}