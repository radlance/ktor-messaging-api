package com.github.radlance.ktormessagingapi.repository.impl

import com.github.radlance.ktormessagingapi.database.entity.UserEntity
import com.github.radlance.ktormessagingapi.database.table.UserTable
import com.github.radlance.ktormessagingapi.domain.RegisterUser
import com.github.radlance.ktormessagingapi.domain.User
import com.github.radlance.ktormessagingapi.domain.UserWithPassword
import com.github.radlance.ktormessagingapi.repository.api.AuthRepository
import com.github.radlance.ktormessagingapi.util.loggedTransaction

class AuthRepositoryImpl : AuthRepository {

    override suspend fun create(user: RegisterUser, salt: String): User = loggedTransaction {
        UserEntity.new {
            email = user.email
            passwordHash = user.password
            this.salt = salt
            displayName = user.displayName
        }.toUser()
    }

    override suspend fun getUserByEmail(email: String): UserWithPassword? = loggedTransaction {
        UserEntity.find { UserTable.email eq email }.limit(1).firstOrNull()?.toUserWithPassword()
    }
}