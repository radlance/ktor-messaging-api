package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.domain.chats.Chat
import com.github.radlance.ktormessagingapi.repository.ChatsRepository

class ChatsService(
    private val chatsRepository: ChatsRepository
) {

    suspend fun chats(currentUserEmail: String): List<Chat> = chatsRepository.chats(currentUserEmail)
}