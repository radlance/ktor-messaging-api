package com.github.radlance.ktormessagingapi.plugins

import com.github.radlance.ktormessagingapi.route.auth
import com.github.radlance.ktormessagingapi.route.chats
import com.github.radlance.ktormessagingapi.service.AuthService
import com.github.radlance.ktormessagingapi.service.ChatsService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService by inject<AuthService>()
    val chatsService by inject<ChatsService>()

    routing {
        route("api") {
            get("/") {
                call.respondText("Hello World!")
            }

            auth(authService)
            chats(chatsService)
        }
    }
}