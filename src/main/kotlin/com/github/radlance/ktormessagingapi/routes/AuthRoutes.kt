package com.github.radlance.ktormessagingapi.routes

import com.github.radlance.ktormessagingapi.domain.LoginUser
import com.github.radlance.ktormessagingapi.domain.RegisterUser
import com.github.radlance.ktormessagingapi.domain.User
import com.github.radlance.ktormessagingapi.repository.api.AuthRepository
import com.github.radlance.ktormessagingapi.security.hashing.HashingService
import com.github.radlance.ktormessagingapi.security.hashing.SaltedHash
import com.github.radlance.ktormessagingapi.security.token.TokenClaim
import com.github.radlance.ktormessagingapi.security.token.TokenConfig
import com.github.radlance.ktormessagingapi.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.register(
    authRepository: AuthRepository,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    route("auth/") {
        post("register") {
            val request = call.receive<RegisterUser>()
            val saltedHash = hashingService.generateSaltedHash(request.password)

            val user = RegisterUser(
                email = request.email,
                password = saltedHash.hash,
                displayName = request.displayName
            )

            val createdUser: User = authRepository.create(user, saltedHash.salt)
            call.respond(createdUser)
        }

        post("login") {
            val request = call.receive<LoginUser>()
            val user = authRepository.getUserByEmail(request.email)

            if (user == null) {
                call.respond(HttpStatusCode.Conflict, "Incorrect email or password")
                return@post
            }
            val isValidPassword = hashingService.verify(
                value = request.password,
                saltedHash = SaltedHash(
                    hash = user.passwordHash,
                    salt = user.salt
                )
            )
            if (!isValidPassword) {
                call.respond(HttpStatusCode.Conflict, "Incorrect email or password")
                return@post
            }

            val token = tokenService.generate(
                config = tokenConfig,
                TokenClaim(name = "userId", value = user.id.toString()),
                TokenClaim(name = "userId", value = user.email)
            )

            call.respond(message = token)
        }

        authenticate {
            get("authenticate") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}