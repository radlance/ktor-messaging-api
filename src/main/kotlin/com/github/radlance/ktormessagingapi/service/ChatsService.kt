package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.domain.chats.Chat
import com.github.radlance.ktormessagingapi.repository.ChatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap

class ChatsService(private val chatsRepository: ChatsRepository) {

    private val userFlows = ConcurrentHashMap<String, MutableSharedFlow<List<Chat>>>()

    private fun getUserFlow(email: String): MutableSharedFlow<List<Chat>> = userFlows.computeIfAbsent(email) {
        MutableSharedFlow(replay = 1)
    }

    suspend fun loadAndEmitChats(email: String): List<Chat> {
        val chats = chatsRepository.chats(email)
        getUserFlow(email).emit(chats)
        return chats
    }

    fun subscribe(email: String): Flow<List<Chat>> = getUserFlow(email)

    suspend fun notifyChatsChanged(email: String) {
        val chats = chatsRepository.chats(email)
        getUserFlow(email).emit(chats)
    }
}