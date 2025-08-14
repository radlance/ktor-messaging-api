package com.github.radlance.ktormessagingapi.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.github.radlance.ktormessagingapi.domain.auth.Token
import java.util.*

class TokenService(private val tokenConfig: TokenConfig) {

    private val audience = tokenConfig.audience
    private val issuer = tokenConfig.issuer
    private val secret = tokenConfig.secret

    fun generateToken(userEmail: String): String {
        return JWT.create().apply {
            withAudience(audience)
            withIssuer(issuer)
            withClaim("email", userEmail)
            withExpiresAt(Date(System.currentTimeMillis() + tokenConfig.expiresIn))
        }.sign(Algorithm.HMAC256(secret))
    }

    fun verifyToken(): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(secret)).apply {
            withAudience(audience)
            withIssuer(issuer)
        }.build()
    }

    fun refreshToken(token: Token): Token {
        val decodedJWT = JWT.decode(token.accessToken)

        try {
            verifyToken().verify(decodedJWT)
        } catch (e: TokenExpiredException) {
            val expirationDate = decodedJWT.expiresAt
            val timeDifference = Date().time - expirationDate.time

            if (timeDifference > tokenConfig.totalExpiresIn) {
                throw JWTVerificationException("Invalid or expired token")
            }
        }

        val userEmail = decodedJWT.getClaim("email").asString()

        val newAccessToken = generateToken(userEmail = userEmail)

        return Token(accessToken = newAccessToken)
    }
}