package com.github.radlance.ktormessagingapi.domain.chats

import kotlinx.serialization.Serializable

@Serializable
data class NewMessage(val message: String)