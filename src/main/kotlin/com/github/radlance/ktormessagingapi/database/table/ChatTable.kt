package com.github.radlance.ktormessagingapi.database.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object ChatTable : IntIdTable(name = "chat") {
    val type = varchar(name = "type", length = 7)
    val title = varchar(name = "title", length = 64)
    val createdAt = timestamp(name = "created_at").nullable()
}