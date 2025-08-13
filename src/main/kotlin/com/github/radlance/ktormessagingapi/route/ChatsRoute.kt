package com.github.radlance.ktormessagingapi.route

import com.github.radlance.ktormessagingapi.domain.chats.NewChat
import com.github.radlance.ktormessagingapi.service.ChatsService
import com.github.radlance.ktormessagingapi.util.claimByNameOrElse
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
                val userEmail = call.claimByNameOrElse<String>(name = "email") {
                    return@get call.respond(HttpStatusCode.Unauthorized)
                }

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
        }

        post("/chat") {
            val request = call.receiveOrThrow<NewChat>()
            val userEmail = call.claimByNameOrElse<String>(name = "email") {
                return@post call.respond(HttpStatusCode.Unauthorized)
            }

            val newChat = chatsService.createChat(email = userEmail, chat = request)
            chatsService.notifyChatsChanged(email = userEmail)
            call.respond(HttpStatusCode.OK, newChat)
        }
    }
}