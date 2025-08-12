package com.github.radlance.ktormessagingapi.database.entity

import com.github.radlance.ktormessagingapi.database.table.ChatMemberTable
import com.github.radlance.ktormessagingapi.database.table.ChatTable
import com.github.radlance.ktormessagingapi.database.table.MessageTable
import com.github.radlance.ktormessagingapi.domain.chats.Chat
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ChatEntity(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<ChatEntity>(ChatTable)

    var type by ChatTable.type
    var title by ChatTable.title
    val createdAt by ChatTable.createdAt

    val members by UserEntity via ChatMemberTable
    val messages by MessageEntity referrersOn MessageTable.chat

    fun toChat(): Chat {
        return Chat(
            id = id.value,
            type = type,
            title = title,
            createdAt = createdAt.toString()
        )
    }
}