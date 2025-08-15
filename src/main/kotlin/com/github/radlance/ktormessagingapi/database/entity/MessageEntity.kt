package com.github.radlance.ktormessagingapi.database.entity

import com.github.radlance.ktormessagingapi.database.table.MessageReadStatusTable
import com.github.radlance.ktormessagingapi.database.table.MessageTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class MessageEntity(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<MessageEntity>(MessageTable)

    val chat by ChatEntity referencedOn MessageTable.chat
    val sender by UserEntity optionalReferrersOn MessageTable.sender
    val text by MessageTable.text
    val createdAt by MessageTable.createdAt
    val updatedAt by MessageTable.updatedAt

    val readStatuses by MessageReadStatusEntity referrersOn MessageReadStatusTable.message
}