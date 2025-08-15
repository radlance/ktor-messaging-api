package com.github.radlance.ktormessagingapi.domain.chats

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val text: String,
    val displayName: String?,
    val sendDate: String
)