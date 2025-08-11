package com.github.radlance.ktormessagingapi.database.entity

import com.github.radlance.ktormessagingapi.database.table.MessageReadStatusTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class MessageReadStatusEntity(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<MessageReadStatusEntity>(MessageReadStatusTable)

    val message by MessageEntity referencedOn MessageReadStatusTable.message
    val user by UserEntity referencedOn MessageReadStatusTable.user
    val readAt by MessageReadStatusTable.readAt
}