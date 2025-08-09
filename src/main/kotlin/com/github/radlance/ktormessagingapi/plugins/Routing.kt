package com.github.radlance.ktormessagingapi.plugins

import com.github.radlance.ktormessagingapi.repository.api.AuthRepository
import com.github.radlance.ktormessagingapi.routes.register
import com.github.radlance.ktormessagingapi.security.hashing.HashingService
import com.github.radlance.ktormessagingapi.security.token.TokenConfig
import com.github.radlance.ktormessagingapi.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authRepository by inject<AuthRepository>()
    val hashingService by inject<HashingService>()
    val tokenService by inject<TokenService>()
    val tokenConfig by inject<TokenConfig>()

    routing {
        route("api/") {
            get {
                call.respondText("Hello World!")
            }

            register(
                authRepository = authRepository,
                hashingService = hashingService,
                tokenService = tokenService,
                tokenConfig = tokenConfig
            )
        }
    }
}