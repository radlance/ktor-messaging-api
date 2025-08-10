package com.github.radlance.ktormessagingapi.repository.api

import com.github.radlance.ktormessagingapi.domain.auth.RegisterUser
import com.github.radlance.ktormessagingapi.domain.auth.User
import com.github.radlance.ktormessagingapi.domain.auth.UserWithPassword

interface AuthRepository {

    suspend fun create(user: RegisterUser, salt: String): User

    suspend fun getUserByEmail(email: String): UserWithPassword?
}