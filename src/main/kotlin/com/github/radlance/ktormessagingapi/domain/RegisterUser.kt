package com.github.radlance.ktormessagingapi.domain

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUser(
    val email: String,
    val password: String,
    val displayName: String
)