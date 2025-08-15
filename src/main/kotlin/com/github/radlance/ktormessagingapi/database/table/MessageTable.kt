package com.github.radlance.ktormessagingapi.database.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object MessageTable : IntIdTable(name = "message") {
    val chat = reference(name = "chat_id", foreign = ChatTable, onDelete = ReferenceOption.CASCADE)
    val text = text(name = "text")
    val type = varchar(name = "type", length = 10).nullable()
    val sender = reference(name = "sender_id", foreign = UserTable, onDelete = ReferenceOption.CASCADE).nullable()
    val createdAt = timestamp(name = "created_at").nullable()
    val updatedAt = timestamp(name = "updated_at").nullable()
}