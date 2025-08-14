package com.github.radlance.ktormessagingapi.service

import com.github.radlance.ktormessagingapi.domain.auth.LoginUser
import com.github.radlance.ktormessagingapi.domain.auth.RegisterUser
import com.github.radlance.ktormessagingapi.domain.auth.Token
import com.github.radlance.ktormessagingapi.domain.auth.User
import com.github.radlance.ktormessagingapi.exception.MissingCredentialException
import com.github.radlance.ktormessagingapi.repository.AuthRepository
import com.github.radlance.ktormessagingapi.security.hashing.HashingService
import com.github.radlance.ktormessagingapi.security.hashing.SaltedHash
import com.github.radlance.ktormessagingapi.security.token.TokenService

class AuthService(
    private val authRepository: AuthRepository,
    private val hashingService: HashingService,
    private val tokenService: TokenService
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

    suspend fun login(user: LoginUser): Token {
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

        val accessToken = tokenService.generateToken(userEmail = user.email)
        return Token(accessToken = accessToken)
    }

    fun refreshToken(token: Token): Token = tokenService.refreshToken(token)
}