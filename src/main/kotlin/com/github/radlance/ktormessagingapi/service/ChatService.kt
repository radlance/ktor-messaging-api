package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.domain.chat.ChatMember
import com.github.radlance.ktormessagingapi.domain.chat.Message
import com.github.radlance.ktormessagingapi.domain.chat.NewMessage
import com.github.radlance.ktormessagingapi.exception.NoAccessException
import com.github.radlance.ktormessagingapi.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap

class ChatService(private val chatRepository: ChatRepository, private val socketService: SocketService) {

    private val chatFlows = ConcurrentHashMap<Int, MutableSharedFlow<List<Message>>>()
    private val membersFlows = ConcurrentHashMap<Int, MutableSharedFlow<List<ChatMember>>>()

    suspend fun requireMembership(email: String, chatId: Int) {
        if (!chatRepository.isMember(email, chatId)) {
            throw NoAccessException("User $email is not a member of chat with id $chatId")
        }
    }

    suspend fun loadAndEmitMessages(chatId: Int): List<Message> {
        val messages = chatRepository.messages(chatId)
        getChatFlow(chatId).emit(messages)
        return messages
    }

    suspend fun loadAndEmitMembers(chatId: Int): List<ChatMember> {
        val members = chatRepository.members(chatId)
        getMembersFlow(chatId).emit(members)
        return members
    }

    suspend fun addMember(currentUserEmail: String, email: String, chatId: Int) {
        chatRepository.addMember(currentUserEmail = currentUserEmail, email = email, chatId = chatId)
        socketService.notifyChatMembers(chatId)
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

    fun subscribeMembers(chatId: Int) = getMembersFlow(chatId)

    private fun getMembersFlow(chatId: Int): MutableSharedFlow<List<ChatMember>> =
        membersFlows.computeIfAbsent(chatId) {
            MutableSharedFlow(replay = 1)
        }

    private fun getChatFlow(chatId: Int): MutableSharedFlow<List<Message>> =
        chatFlows.computeIfAbsent(chatId) {
            MutableSharedFlow(replay = 1)
        }

    private suspend fun notifyChatChanged(chatId: Int) {
        val messages = chatRepository.messages(chatId)
        getChatFlow(chatId).emit(messages)
    }
}