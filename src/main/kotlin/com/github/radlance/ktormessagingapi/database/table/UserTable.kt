package com.github.radlance.ktormessagingapi.database.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UserTable : IntIdTable(name = "users") {
    val email = varchar("email", length = 255)
    val passwordHash = varchar("password_hash", length = 255)
    val salt = varchar("salt", length = 255)
    val displayName = varchar("display_name", length = 255)
    val createdAt = timestamp("created_at").nullable()
    val updatedAt = timestamp("updated_at").nullable()
}