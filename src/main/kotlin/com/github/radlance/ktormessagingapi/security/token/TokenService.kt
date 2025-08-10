package com.github.radlance.ktormessagingapi.security.token

import com.auth0.jwt.JWTVerifier

interface TokenService {

    fun generateToken(userEmail: String, tokenType: String, expirationDate: Long): String

    fun verifyToken(tokenType: String): JWTVerifier
}