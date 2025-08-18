package com.github.radlance.ktormessagingapi.domain.chats

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int,
    val displayName: String?,
    val text: String,
    val createdAt: String,
    val updatedAt: String?,
    val type: String
)