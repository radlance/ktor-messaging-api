package com.github.radlance.ktormessagingapi.exception

data class NoAccessException(override val message: String? = null) : RuntimeException()