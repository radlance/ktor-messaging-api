package com.github.radlance.ktormessagingapi.domain.chats

import kotlinx.serialization.Serializable

@Serializable
data class NewMember(val email: String)