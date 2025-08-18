package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.domain.chats.Message
import com.github.radlance.ktormessagingapi.domain.chats.NewMessage
import com.github.radlance.ktormessagingapi.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ChatService(private val chatRepository: ChatRepository, private val socketService: SocketService) {

    suspend fun leaveChat(email: String, chatId: Int) {
        chatRepository.leaveChat(email = email, chatId = chatId)
        socketService.notifyChatMembers(chatId)
    }

    suspend fun sendMessage(email: String, chatId: Int, message: NewMessage) {
        chatRepository.sendMessage(email, chatId, message)
        notifyChatChanged(chatId)
        socketService.notifyChatMembers(chatId)
    }

    fun subscribe(chatId: Int): Flow<List<Message>> = socketService.getChatFlow(chatId)

    private suspend fun notifyChatChanged(chatId: Int) {
        val messages = chatRepository.messages(chatId)
        socketService.getChatFlow(chatId).emit(messages)
    }
}