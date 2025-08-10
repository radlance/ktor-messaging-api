package com.github.radlance.ktormessagingapi.service

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.github.radlance.ktormessagingapi.domain.auth.LoginUser
import com.github.radlance.ktormessagingapi.domain.auth.RefreshToken
import com.github.radlance.ktormessagingapi.domain.auth.RegisterUser
import com.github.radlance.ktormessagingapi.domain.auth.Tokens
import com.github.radlance.ktormessagingapi.domain.auth.User
import com.github.radlance.ktormessagingapi.exception.MissingCredentialException
import com.github.radlance.ktormessagingapi.repository.AuthRepository
import com.github.radlance.ktormessagingapi.security.hashing.HashingService
import com.github.radlance.ktormessagingapi.security.hashing.SaltedHash
import com.github.radlance.ktormessagingapi.security.token.TokenService
import com.github.radlance.ktormessagingapi.security.token.TokenType

class AuthService(
    private val authRepository: AuthRepository,
    private val hashingService: HashingService,
    private val tokenService: TokenService,
    private val jwtExpiration: Long,
    private val refreshExpiration: Long
) {

    suspend fun register(user: RegisterUser): User {
        if (authRepository.getUserByEmail(user.email) != null) {
            throw MissingCredentialException("Email already taken! Input another email")
        }

        val saltedHash = hashingService.generateSaltedHash(user.password)

        val registerUser = RegisterUser(
            email = user.email,
            password = saltedHash.hash,
            displayName = user.displayName
        )

        return authRepository.create(registerUser, saltedHash.salt)
    }

    suspend fun login(user: LoginUser): Tokens {
        val existingUser = authRepository.getUserByEmail(user.email)
            ?: throw MissingCredentialException("Incorrect email or password")

        val isValidPassword = hashingService.verify(
            value = user.password,
            saltedHash = SaltedHash(
                hash = existingUser.passwordHash,
                salt = existingUser.salt
            )
        )
        if (!isValidPassword) {
            throw MissingCredentialException("Incorrect email or password")
        }

        val accessToken = tokenService.generateToken(
            userEmail = user.email,
            tokenType = TokenType.ACCESS_TOKEN.name,
            expirationDate = jwtExpiration
        )

        val refreshToken = tokenService.generateToken(
            userEmail = user.email,
            tokenType = TokenType.REFRESH_TOKEN.name,
            expirationDate = refreshExpiration
        )


        return Tokens(accessToken = accessToken, refreshToken = refreshToken)
    }

    fun refreshToken(refreshToken: RefreshToken): Tokens {
        val decodedJWT = JWT.decode(refreshToken.refreshToken)

        val principal = try {
            val tokenType = decodedJWT.getClaim("tokenType").asString()
            if (tokenType != TokenType.REFRESH_TOKEN.name) {
                throw JWTVerificationException("Invalid token type")
            }

            tokenService.verifyToken(TokenType.REFRESH_TOKEN.name).verify(decodedJWT)
            decodedJWT
        } catch (e: Exception) {
            throw JWTVerificationException("Invalid or expired refresh token")
        }

        val userEmail = principal.getClaim("email").asString()

        val newAccessToken = tokenService.generateToken(
            userEmail = userEmail,
            tokenType = TokenType.ACCESS_TOKEN.name,
            expirationDate = jwtExpiration
        )

        val newRefreshToken = tokenService.generateToken(
            userEmail = userEmail,
            tokenType = TokenType.REFRESH_TOKEN.name,
            expirationDate = refreshExpiration
        )

        return Tokens(accessToken = newAccessToken, refreshToken = newRefreshToken)
    }
}