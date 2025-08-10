package com.github.radlance.ktormessagingapi.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserWithPassword(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val displayName: String,
    val createdAt: String?,
    val updatedAt: String?
)