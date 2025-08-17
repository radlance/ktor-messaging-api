package com.github.radlance.ktormessagingapi.route

import com.github.radlance.ktormessagingapi.domain.chats.NewMessage
import com.github.radlance.ktormessagingapi.service.ChatService
import com.github.radlance.ktormessagingapi.util.chatIdParameterOrThrow
import com.github.radlance.ktormessagingapi.util.claimByNameOrUnauthorized
import com.github.radlance.ktormessagingapi.util.receiveOrThrow
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

            post("/{chatId}/messages") {
                val chatId = call.chatIdParameterOrThrow()
                val userEmail = call.claimByNameOrUnauthorized<String>(name = "email")
                val message = call.receiveOrThrow<NewMessage>()

                chatService.sendMessage(email = userEmail, chatId = chatId, message = message)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}