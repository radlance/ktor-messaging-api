package com.github.radlance.ktormessagingapi.database.entity

import com.github.radlance.ktormessagingapi.database.table.ChatTable
import com.github.radlance.ktormessagingapi.domain.chats.Chat
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ChatEntity(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<ChatEntity>(ChatTable)

    var type by ChatTable.type
    var title by ChatTable.title
    private val createdAt by ChatTable.createdAt

    fun toChat(): Chat {
        return Chat(
            id = id.value,
            type = type,
            title = title,
            createdAt = createdAt.toString()
        )
    }
}