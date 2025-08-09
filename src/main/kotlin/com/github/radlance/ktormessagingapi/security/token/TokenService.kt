package com.github.radlance.ktormessagingapi.security.token

interface TokenService {

    fun generate(config: TokenConfig, vararg claims: TokenClaim): String
}