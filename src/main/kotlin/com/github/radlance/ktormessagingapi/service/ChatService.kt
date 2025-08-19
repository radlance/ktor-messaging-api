package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.domain.chat.Message
import com.github.radlance.ktormessagingapi.domain.chat.NewMessage
import com.github.radlance.ktormessagingapi.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap

class ChatService(private val chatRepository: ChatRepository, private val socketService: SocketService) {

    private val chatFlows = ConcurrentHashMap<Int, MutableSharedFlow<List<Message>>>()

    suspend fun loadAndEmitMessages(chatId: Int): List<Message> {
        val messages = chatRepository.messages(chatId)
        getChatFlow(chatId).emit(messages)
        return messages
    }

    suspend fun leaveChat(email: String, chatId: Int) {
        chatRepository.leaveChat(email = email, chatId = chatId)
        socketService.notifyChatMembers(chatId)
    }

    suspend fun sendMessage(email: String, chatId: Int, message: NewMessage) {
        chatRepository.sendMessage(email, chatId, message)
        notifyChatChanged(chatId)
        socketService.notifyChatMembers(chatId)
    }

    fun subscribeChat(chatId: Int): Flow<List<Message>> = getChatFlow(chatId)

    private fun getChatFlow(chatId: Int): MutableSharedFlow<List<Message>> =
        chatFlows.computeIfAbsent(chatId) {
            MutableSharedFlow(replay = 1)
        }

    private suspend fun notifyChatChanged(chatId: Int) {
        val messages = chatRepository.messages(chatId)
        getChatFlow(chatId).emit(messages)
    }
}