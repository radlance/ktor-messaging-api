package com.github.radlance.ktormessagingapi.route

import com.github.radlance.ktormessagingapi.domain.chats.NewChat
import com.github.radlance.ktormessagingapi.service.ChatsService
import com.github.radlance.ktormessagingapi.util.claimByNameOrElse
import com.github.radlance.ktormessagingapi.util.claimByNameOrUnauthorized
import com.github.radlance.ktormessagingapi.util.handleFlowSubscription
import com.github.radlance.ktormessagingapi.util.receiveOrThrow
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.chats(chatsService: ChatsService) {

    authenticate {
        route("/chats") {
            webSocket {
                val userEmail = call.claimByNameOrElse<String>("email") {
                    return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No JWT principal"))
                }
                handleFlowSubscription(flow = chatsService.subscribe(userEmail))
            }

            get {
                val userEmail = call.claimByNameOrUnauthorized<String>(name = "email")

                val chats = chatsService.loadAndEmitChats(userEmail)
                call.respond(HttpStatusCode.OK, chats)
            }

            post {
                val request = call.receiveOrThrow<NewChat>()
                val userEmail = call.claimByNameOrUnauthorized<String>(name = "email")

                val newChat = chatsService.createChat(email = userEmail, chat = request)
                call.respond(HttpStatusCode.Created, newChat)
            }
        }
    }
}