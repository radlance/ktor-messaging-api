package com.github.radlance.ktormessagingapi.plugins

import com.github.radlance.ktormessagingapi.route.auth
import com.github.radlance.ktormessagingapi.route.chat
import com.github.radlance.ktormessagingapi.route.chats
import com.github.radlance.ktormessagingapi.service.AuthService
import com.github.radlance.ktormessagingapi.service.ChatService
import com.github.radlance.ktormessagingapi.service.ChatsService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService by inject<AuthService>()
    val chatsService by inject<ChatsService>()
    val chatService by inject<ChatService>()

    routing {
        route("/api") {
            auth(authService)
            chats(chatsService)
            chat(chatService)
        }
    }
}