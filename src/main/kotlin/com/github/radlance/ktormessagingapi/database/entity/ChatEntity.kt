package com.github.radlance.ktormessagingapi.database.entity

import com.github.radlance.ktormessagingapi.database.table.ChatMemberTable
import com.github.radlance.ktormessagingapi.database.table.ChatTable
import com.github.radlance.ktormessagingapi.database.table.MessageTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ChatEntity(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<ChatEntity>(ChatTable)

    val type by ChatTable.type
    val title by ChatTable.title
    val createdAt by ChatTable.createdAt

    val members by UserEntity via ChatMemberTable
    val messages by MessageEntity referrersOn MessageTable.chat
}