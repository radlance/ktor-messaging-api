package com.github.radlance.ktormessagingapi.plugins

import com.github.radlance.ktormessagingapi.domain.LoginUser
import com.github.radlance.ktormessagingapi.domain.RegisterUser
import io.ktor.http.*
import io.ktor.server.application.*
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

                else -> ValidationResult.Valid
            }
        }

        validate<LoginUser> { user ->
            when {
                !user.email.matchesEmailRegex() -> {
                    ValidationResult.Invalid("Invalid email format")
                }

                else -> ValidationResult.Valid
            }
        }
    }

    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respondText(text = "Bad Request: $cause", status = HttpStatusCode.BadRequest)
        }

        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
}

private fun String.matchesEmailRegex(): Boolean {
    return "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$".toRegex().matches(this)
}