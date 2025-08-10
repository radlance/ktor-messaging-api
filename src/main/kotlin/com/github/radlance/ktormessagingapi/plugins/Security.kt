package com.github.radlance.ktormessagingapi.plugins

import com.github.radlance.ktormessagingapi.security.token.TokenService
import com.github.radlance.ktormessagingapi.security.token.TokenType
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val tokenService by inject<TokenService>()
    val audience = environment.config.property("jwt.audience").getString()

    authentication {
        jwt {
            verifier(tokenService.verifyToken(tokenType = TokenType.ACCESS_TOKEN.name))
            validate { credential ->
                if (credential.payload.audience.contains(audience) && credential.payload.claims.contains("email")) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
