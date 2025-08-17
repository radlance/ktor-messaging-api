package com.github.radlance.ktormessagingapi.route

import com.github.radlance.ktormessagingapi.service.ChatService
import com.github.radlance.ktormessagingapi.util.chatIdParameterOrThrow
import com.github.radlance.ktormessagingapi.util.claimByNameOrUnauthorized
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chat(chatService: ChatService) {
    authenticate {
        route("/chat") {
            get("/{chatId}/leave") {
                val chatId = call.chatIdParameterOrThrow()
                val userEmail = call.claimByNameOrUnauthorized<String>(name = "email")

                chatService.leaveChat(email = userEmail, chatId = chatId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}