package com.github.radlance.ktormessagingapi.database.entity

import com.github.radlance.ktormessagingapi.database.table.UserTable
import com.github.radlance.ktormessagingapi.domain.auth.User
import com.github.radlance.ktormessagingapi.domain.auth.UserWithPassword
import io.ktor.server.http.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserEntity(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<UserEntity>(UserTable)

    var email by UserTable.email
    var passwordHash by UserTable.passwordHash
    var salt by UserTable.salt
    var displayName by UserTable.displayName
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt

    fun toUser(): User = User(
        id = id.value,
        email = email,
        displayName = displayName,
        createdAt = createdAt?.toHttpDateString(),
        updatedAt = updatedAt?.toHttpDateString()
    )

    fun toUserWithPassword(): UserWithPassword = UserWithPassword(
        id = id.value,
        email = email,
        passwordHash = passwordHash,
        salt = salt,
        displayName = displayName,
        createdAt = createdAt?.toHttpDateString(),
        updatedAt = updatedAt?.toHttpDateString()
    )
}