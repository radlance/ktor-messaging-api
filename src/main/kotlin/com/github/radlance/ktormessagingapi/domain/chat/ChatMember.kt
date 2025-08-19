package com.github.radlance.ktormessagingapi.domain.chat

import com.github.radlance.ktormessagingapi.domain.chats.ChatRole
import kotlinx.serialization.Serializable

@Serializable
data class ChatMember(
    val userId: Int,
    val displayName: String,
    val role: ChatRole
)