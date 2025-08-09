package com.github.radlance.ktormessagingapi.domain

import kotlinx.serialization.Serializable

@Serializable
data class LoginUser(
    val email: String,
    val password: String
)