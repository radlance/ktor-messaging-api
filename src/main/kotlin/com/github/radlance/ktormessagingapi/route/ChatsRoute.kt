package com.github.radlance.ktormessagingapi.route

import com.github.radlance.ktormessagingapi.service.ChatsService
import com.github.radlance.ktormessagingapi.util.getClaimOrThrow
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

fun Route.chats(chatsService: ChatsService) {

    authenticate {
        get("/chats") {
            val principal = call.principal<JWTPrincipal>()
            val userEmail = principal?.getClaimOrThrow<String>("email") ?: run {
                return@get call.respond(HttpStatusCode.Unauthorized)
            }

            val chats = chatsService.loadAndEmitChats(userEmail)
            call.respond(HttpStatusCode.OK, chats)
        }

        webSocket("/chats") {
            val principal = call.principal<JWTPrincipal>()
            val userEmail = principal?.getClaimOrThrow<String>("email") ?: run {
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

        post("/send-message") {
            val principal = call.principal<JWTPrincipal>()
            val userEmail = principal?.getClaimOrThrow<String>("email") ?: run {
                return@post call.respond(HttpStatusCode.Unauthorized)
            }

            chatsService.notifyChatsChanged(email = userEmail)
            call.respond(HttpStatusCode.OK)
        }
    }
}