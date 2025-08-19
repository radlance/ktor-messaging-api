package com.github.radlance.ktormessagingapi.route

import com.github.radlance.ktormessagingapi.domain.chat.NewMessage
import com.github.radlance.ktormessagingapi.service.ChatService
import com.github.radlance.ktormessagingapi.util.chatIdParameterOrThrow
import com.github.radlance.ktormessagingapi.util.claimByNameOrUnauthorized
import com.github.radlance.ktormessagingapi.util.handleFlowSubscription
import com.github.radlance.ktormessagingapi.util.receiveOrThrow
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.chat(chatService: ChatService) {
    authenticate {
        route("/chat/{chatId}") {
            webSocket {
                val chatId = call.parameters["chatId"]?.toIntOrNull()
                    ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid chatId"))

                handleFlowSubscription(flow = chatService.subscribeChat(chatId))
            }


            get {
                val chatId = call.chatIdParameterOrThrow()
                val messages = chatService.loadAndEmitMessages(chatId)
                call.respond(HttpStatusCode.OK, messages)
            }

            get("/leave") {
                val chatId = call.chatIdParameterOrThrow()
                val userEmail = call.claimByNameOrUnauthorized<String>(name = "email")

                chatService.leaveChat(email = userEmail, chatId = chatId)
                call.respond(HttpStatusCode.NoContent)
            }

            post("/messages") {
                val chatId = call.chatIdParameterOrThrow()
                val userEmail = call.claimByNameOrUnauthorized<String>(name = "email")
                val message = call.receiveOrThrow<NewMessage>()

                chatService.sendMessage(email = userEmail, chatId = chatId, message = message)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}