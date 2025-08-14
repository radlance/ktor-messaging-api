package com.github.radlance.ktormessagingapi.route

import com.github.radlance.ktormessagingapi.domain.auth.LoginUser
import com.github.radlance.ktormessagingapi.domain.auth.RegisterUser
import com.github.radlance.ktormessagingapi.domain.auth.Token
import com.github.radlance.ktormessagingapi.domain.auth.User
import com.github.radlance.ktormessagingapi.service.AuthService
import com.github.radlance.ktormessagingapi.util.receiveOrThrow
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.auth(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val request = call.receiveOrThrow<RegisterUser>()
            val createdUser: User = authService.register(user = request)
            call.respond(createdUser)
        }

        post("/login") {
            val request = call.receiveOrThrow<LoginUser>()
            val tokens = authService.login(user = request)
            call.respond(HttpStatusCode.OK, tokens)
        }

        post("/refresh-token") {
            val request = call.receiveOrThrow<Token>()
            val tokens = authService.refreshToken(token = request)
            call.respond(HttpStatusCode.OK, tokens)
        }
    }
}