package com.github.radlance.ktormessagingapi.domain.chats

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val text: String,
    val senderEmail: String,
    val sendDate: String
)