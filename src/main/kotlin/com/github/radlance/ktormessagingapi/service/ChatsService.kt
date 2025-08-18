package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.domain.chats.Chat
import com.github.radlance.ktormessagingapi.domain.chats.ChatWithLastMessage
import com.github.radlance.ktormessagingapi.domain.chats.NewChat
import com.github.radlance.ktormessagingapi.repository.ChatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class ChatsService(
    private val socketService: SocketService,
    private val chatsRepository: ChatsRepository
) {
    private fun getUserFlow(email: String): MutableSharedFlow<List<ChatWithLastMessage>> =
        socketService.userFlows.computeIfAbsent(email) {
            MutableSharedFlow(replay = 1)
        }

    suspend fun loadAndEmitChats(email: String): List<ChatWithLastMessage> {
        val chats = chatsRepository.chats(email)
        getUserFlow(email).emit(chats)
        return chats
    }

    fun subscribe(email: String): Flow<List<ChatWithLastMessage>> = getUserFlow(email)

    suspend fun createChat(email: String, chat: NewChat): Chat {
        val newChat = chatsRepository.createChat(email, chat)
        socketService.notifyChatsChanged(email = email)
        return newChat
    }

    suspend fun addMember(currentUserEmail: String, email: String, chatId: Int) {
        chatsRepository.addMember(currentUserEmail = currentUserEmail, email = email, chatId = chatId)
        socketService.notifyChatMembers(chatId)
    }
}