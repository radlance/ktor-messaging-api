package com.github.radlance.ktormessagingapi.security.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)