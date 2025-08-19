package com.github.radlance.ktormessagingapi.util

import com.github.radlance.ktormessagingapi.exception.NoAccessException
import com.github.radlance.ktormessagingapi.service.ChatService
import io.ktor.server.websocket.*

suspend inline fun DefaultWebSocketServerSession.allowChatIdOrElse(
    chatService: ChatService,
    action: (String) -> Nothing
): Int {
    val chatId = call.parameters["chatId"]?.toIntOrNull()
        ?: action("Invalid chatId")
    val userEmail = call.claimByNameOrUnauthorized<String>("email")

    try {
        chatService.requireMembership(userEmail, chatId)
    } catch (e: NoAccessException) {
        action(e.message ?: "No access")
    }

    return chatId
}