package com.github.radlance.ktormessagingapi.route

import com.github.radlance.ktormessagingapi.domain.chats.NewChat
import com.github.radlance.ktormessagingapi.domain.chats.NewMember
import com.github.radlance.ktormessagingapi.domain.chats.NewMessage
import com.github.radlance.ktormessagingapi.service.ChatsService
import com.github.radlance.ktormessagingapi.util.chatIdParameterOrThrow
import com.github.radlance.ktormessagingapi.util.claimByNameOrElse
import com.github.radlance.ktormessagingapi.util.claimByNameOrUnauthorized
import com.github.radlance.ktormessagingapi.util.receiveOrThrow
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

fun Route.chats(chatsService: ChatsService) {

    authenticate {
        route("/chats") {
            get {
                val userEmail = call.claimByNameOrUnauthorized<String>(name = "email")

                val chats = chatsService.loadAndEmitChats(userEmail)
                call.respond(HttpStatusCode.OK, chats)
            }

            webSocket {
                val userEmail = call.claimByNameOrElse<String>(name = "email") {
                    return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No JWT principal"))
                }

                val job = launch {
                    chatsService.subscribe(userEmail).collect { chats ->
                        send(Json.encodeToString(chats))
                    }
                }

                runCatching {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            println("WS from $userEmail: ${frame.readText()}")
                        }
                    }
                }.onFailure { exception ->
                    println("WebSocket exception: ${exception.localizedMessage}")
                }.also { job.cancel() }
            }

            post {
                val request = call.receiveOrThrow<NewChat>()
                val userEmail = call.claimByNameOrUnauthorized<String>(name = "email")

                val newChat = chatsService.createChat(email = userEmail, chat = request)
                call.respond(HttpStatusCode.OK, newChat)
            }

            post("/{chatId}/members") {
                val chatId = call.chatIdParameterOrThrow()
                val userEmail = call.claimByNameOrUnauthorized<String>(name = "email")

                val request = call.receiveOrThrow<NewMember>()

                chatsService.addMember(currentUserEmail = userEmail, email = request.email, chatId = chatId)
                call.respond(HttpStatusCode.NoContent)
            }

            get("/{chatId}/leave") {
                val chatId = call.chatIdParameterOrThrow()
                val userEmail = call.claimByNameOrUnauthorized<String>(name = "email")

                chatsService.leaveChat(email = userEmail, chatId = chatId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}