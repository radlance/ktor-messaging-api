package com.github.radlance.ktormessagingapi.database.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object ChatMemberTable : Table(name = "chat_member") {
    val chat = reference(name = "chat_id", foreign = ChatTable, onDelete = ReferenceOption.CASCADE)
    val user = reference(name = "user_id", foreign = UserTable, onDelete = ReferenceOption.CASCADE)
    val role = varchar(name = "role", length = 6).nullable()
    val joinedAt = timestamp(name = "joined_at").nullable()
    override val primaryKey = PrimaryKey(chat, user)
}