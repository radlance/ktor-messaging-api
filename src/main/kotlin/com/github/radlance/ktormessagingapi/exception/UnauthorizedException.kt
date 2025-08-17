package com.github.radlance.ktormessagingapi.exception

data class UnauthorizedException(override val message: String? = null) : RuntimeException()