package com.github.radlance.ktormessagingapi.exception

data class MissingCredentialException(override val message: String = "Missing credentials") : RuntimeException()