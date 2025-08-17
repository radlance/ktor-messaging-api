package com.github.radlance.ktormessagingapi.domain.chats

import kotlinx.serialization.Serializable

@Serializable
data class ChatWithLastMessage(
    val id: Int,
    val name: String,
    val lastMessage: ChatsMessage?,
    val unreadCount: Int?
)