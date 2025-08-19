package com.github.radlance.ktormessagingapi.domain.chat

import kotlinx.serialization.Serializable

@Serializable
data class NewMessage(val message: String)