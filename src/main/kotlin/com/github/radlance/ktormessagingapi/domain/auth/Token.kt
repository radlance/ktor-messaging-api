package com.github.radlance.ktormessagingapi.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class Token(val accessToken: String)