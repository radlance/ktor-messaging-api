package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.domain.chats.Chat
import com.github.radlance.ktormessagingapi.domain.chats.ChatWithLastMessage
import com.github.radlance.ktormessagingapi.domain.chats.NewChat
import com.github.radlance.ktormessagingapi.repository.ChatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap

class ChatsService(private val chatsRepository: ChatsRepository) {

    private val userFlows = ConcurrentHashMap<String, MutableSharedFlow<List<ChatWithLastMessage>>>()

    private fun getUserFlow(email: String): MutableSharedFlow<List<ChatWithLastMessage>> =
        userFlows.computeIfAbsent(email) {
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
        notifyChatsChanged(email = email)
        return newChat
    }

    suspend fun addMember(currentUserEmail: String, email: String, chatId: Int) {
        chatsRepository.addMember(currentUserEmail = currentUserEmail, email = email, chatId = chatId)
        notifyChatMembers(chatId)
    }

    private suspend fun notifyChatsChanged(email: String) {
        val chats = chatsRepository.chats(email)
        getUserFlow(email).emit(chats)
    }

    private suspend fun notifyChatMembers(chatId: Int) {
        chatsRepository.chatMembersEmails(chatId).forEach { memberEmail ->
            val chats = chatsRepository.chats(memberEmail)
            getUserFlow(memberEmail).emit(chats)
        }
    }
}