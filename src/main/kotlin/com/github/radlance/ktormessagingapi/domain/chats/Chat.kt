package com.github.radlance.ktormessagingapi.domain.chats

import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: Int,
    val name: String,
    val lastMessage: Message,
    val unreadCount: Int
)