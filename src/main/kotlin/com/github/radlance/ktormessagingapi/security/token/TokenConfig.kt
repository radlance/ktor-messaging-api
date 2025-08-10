package com.github.radlance.ktormessagingapi.security.token

data class TokenConfig(
    val issuer: String,
    val audience: String,
    val secret: String
)