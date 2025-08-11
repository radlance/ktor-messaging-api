package com.github.radlance.ktormessagingapi.database.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UserTable : IntIdTable(name = "users") {
    val email = varchar(name = "email", length = 255)
    val passwordHash = varchar(name = "password_hash", length = 255)
    val salt = varchar(name = "salt", length = 255)
    val displayName = varchar(name = "display_name", length = 255)
    val createdAt = timestamp(name = "created_at").nullable()
    val updatedAt = timestamp(name = "updated_at").nullable()
}