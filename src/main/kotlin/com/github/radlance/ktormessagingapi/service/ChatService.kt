package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.repository.ChatRepository

class ChatService(private val chatRepository: ChatRepository, private val socketService: SocketService) {

    suspend fun leaveChat(email: String, chatId: Int) {
        chatRepository.leaveChat(email = email, chatId = chatId)
        socketService.notifyChatMembers(chatId)
    }
}