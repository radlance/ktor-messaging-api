package com.github.radlance.ktormessagingapi.route

import com.github.radlance.ktormessagingapi.service.ChatsService
import com.github.radlance.ktormessagingapi.util.getClaimOrThrow
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chats(chatsService: ChatsService) {

    authenticate {
        get("/chats") {
            val principal = call.principal<JWTPrincipal>()
            val userEmail = principal?.getClaimOrThrow<String>("email")

            if (userEmail == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            call.respond(HttpStatusCode.OK, chatsService.chats(currentUserEmail = userEmail))
        }
    }
}