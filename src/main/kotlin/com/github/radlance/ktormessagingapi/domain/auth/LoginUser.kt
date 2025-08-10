package com.github.radlance.ktormessagingapi.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginUser(
    val email: String,
    val password: String
)