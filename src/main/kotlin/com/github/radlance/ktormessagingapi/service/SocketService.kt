package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.domain.chats.ChatWithLastMessage
import com.github.radlance.ktormessagingapi.repository.ChatsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap

class SocketService(private val chatsRepository: ChatsRepository) {
    val userFlows = ConcurrentHashMap<String, MutableSharedFlow<List<ChatWithLastMessage>>>()

    private fun getUserFlow(email: String): MutableSharedFlow<List<ChatWithLastMessage>> =
        userFlows.computeIfAbsent(email) {
            MutableSharedFlow(replay = 1)
        }

    suspend fun notifyChatsChanged(email: String) {
        val chats = chatsRepository.chats(email)
        getUserFlow(email).emit(chats)
    }

    suspend fun notifyChatMembers(chatId: Int) {
        chatsRepository.chatMembersEmails(chatId).forEach { memberEmail ->
            val chats = chatsRepository.chats(memberEmail)
            getUserFlow(memberEmail).emit(chats)
        }
    }
}