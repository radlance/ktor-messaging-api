package com.github.radlance.ktormessagingapi.route

import com.github.radlance.ktormessagingapi.domain.chat.NewMessage
import com.github.radlance.ktormessagingapi.domain.chats.NewMember
import com.github.radlance.ktormessagingapi.service.ChatService
import com.github.radlance.ktormessagingapi.util.allowChatIdOrElse
import com.github.radlance.ktormessagingapi.util.allowedChatId
import com.github.radlance.ktormessagingapi.util.emailAndAllowedChatId
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
                val chatId = allowChatIdOrElse(chatService) {
                    return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, it))
                }

                handleFlowSubscription(flow = chatService.subscribeChat(chatId))
            }

            webSocket("/members") {
                val chatId = allowChatIdOrElse(chatService) {
                    return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, it))
                }

                handleFlowSubscription(flow = chatService.subscribeMembers(chatId))
            }

            get {
                val chatId = call.allowedChatId(chatService)
                val messages = chatService.loadAndEmitMessages(chatId)
                call.respond(HttpStatusCode.OK, messages)
            }

            post("/members") {
                val (chatId, userEmail) = call.emailAndAllowedChatId(chatService)
                val request = call.receiveOrThrow<NewMember>()

                chatService.addMember(currentUserEmail = userEmail, email = request.email, chatId = chatId)
                call.respond(HttpStatusCode.Created)
            }

            get("/members") {
                val chatId = call.allowedChatId(chatService)
                val members = chatService.loadAndEmitMembers(chatId)
                call.respond(HttpStatusCode.OK, members)
            }

            delete("/leave") {
                val (chatId, userEmail) = call.emailAndAllowedChatId(chatService)
                chatService.leaveChat(email = userEmail, chatId = chatId)
                call.respond(HttpStatusCode.NoContent)
            }

            post("/messages") {
                val (chatId, userEmail) = call.emailAndAllowedChatId(chatService)
                val message = call.receiveOrThrow<NewMessage>()

                chatService.sendMessage(email = userEmail, chatId = chatId, message = message)
                call.respond(HttpStatusCode.Created)
            }
        }
    }
}