package com.github.radlance.ktormessagingapi.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class TokenService(tokenConfig: TokenConfig) {

    private val audience = tokenConfig.audience
    private val issuer = tokenConfig.issuer
    private val secret = tokenConfig.secret

    fun generateToken(userEmail: String, tokenType: String, expirationDate: Long): String {
        return JWT.create().apply {
            withAudience(audience)
            withIssuer(issuer)
            withClaim("email", userEmail)
            withClaim("tokenType", tokenType)
            withExpiresAt(Date(System.currentTimeMillis() + expirationDate))
        }.sign(Algorithm.HMAC256(secret))
    }

    fun verifyToken(tokenType: String): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(secret)).apply {
            withAudience(audience)
            withIssuer(issuer)
            withClaim("tokenType", tokenType)
        }.build()
    }
}