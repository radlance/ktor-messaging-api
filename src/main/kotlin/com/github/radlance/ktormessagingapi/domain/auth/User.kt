package com.github.radlance.ktormessagingapi.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val email: String,
    val displayName: String,
    val createdAt: String?,
    val updatedAt: String?
)