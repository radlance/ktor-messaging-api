package com.github.radlance.ktormessagingapi.database.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object MessageReadStatusTable : IntIdTable(name = "message_read_status") {
    val message = reference(name = "message_id", foreign = MessageTable, onDelete = ReferenceOption.CASCADE)
    val user = reference(name = "user_id", foreign = UserTable, onDelete = ReferenceOption.CASCADE)
    val readAt = timestamp(name = "read_at").nullable()

    init {
        uniqueIndex(message, user)
    }
}