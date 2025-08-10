package com.github.radlance.ktormessagingapi.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class Tokens(
    val accessToken: String,
    val refreshToken: String
)