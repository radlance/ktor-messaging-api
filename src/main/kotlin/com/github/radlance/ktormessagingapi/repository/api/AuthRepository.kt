package com.github.radlance.ktormessagingapi.repository.api

import com.github.radlance.ktormessagingapi.domain.RegisterUser
import com.github.radlance.ktormessagingapi.domain.User
import com.github.radlance.ktormessagingapi.domain.UserWithPassword

interface AuthRepository {

    suspend fun create(user: RegisterUser, salt: String): User

    suspend fun getUserByEmail(email: String): UserWithPassword?
}