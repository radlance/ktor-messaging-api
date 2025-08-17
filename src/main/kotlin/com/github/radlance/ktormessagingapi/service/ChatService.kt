package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.domain.chats.NewMessage
import com.github.radlance.ktormessagingapi.repository.ChatRepository

class ChatService(private val chatRepository: ChatRepository, private val socketService: SocketService) {

    suspend fun leaveChat(email: String, chatId: Int) {
        chatRepository.leaveChat(email = email, chatId = chatId)
        socketService.notifyChatMembers(chatId)
    }

    suspend fun sendMessage(email: String, chatId: Int, message: NewMessage) {
        chatRepository.sendMessage(email, chatId, message)
        socketService.notifyChatMembers(chatId)
    }
}