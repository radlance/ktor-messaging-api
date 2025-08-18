package com.github.radlance.ktormessagingapi.repository

import com.github.radlance.ktormessagingapi.database.entity.UserEntity
import com.github.radlance.ktormessagingapi.database.table.ChatMemberTable
import com.github.radlance.ktormessagingapi.database.table.ChatTable
import com.github.radlance.ktormessagingapi.database.table.MessageTable
import com.github.radlance.ktormessagingapi.database.table.UserTable
import com.github.radlance.ktormessagingapi.domain.chats.Message
import com.github.radlance.ktormessagingapi.domain.chats.MessageType
import com.github.radlance.ktormessagingapi.domain.chats.NewMessage
import com.github.radlance.ktormessagingapi.util.loggedTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class ChatRepository {
    suspend fun leaveChat(email: String, chatId: Int) = loggedTransaction {
        val currentUser = UserEntity.find { UserTable.email eq email }.first()
        ChatMemberTable.deleteWhere { (this.chatId eq chatId) and (userId eq currentUser.id) }
        MessageTable.insert {
            it[this.chatId] = chatId
            it[text] = "${currentUser.displayName} left the chat"
            it[type] = MessageType.SYSTEM.displayName
        }
    }

    suspend fun sendMessage(email: String, chatId: Int, message: NewMessage) = loggedTransaction {
        val currentUser = UserEntity.find { UserTable.email eq email }.first()
        MessageTable.insert {
            it[text] = message.message
            it[this.chatId] = EntityID(id = chatId, table = ChatTable)
            it[senderId] = EntityID(id = currentUser.id.value, table = UserTable)
        }
    }

    suspend fun messages(chatId: Int): List<Message> = loggedTransaction {
        MessageTable
            .innerJoin(UserTable)
            .select(
                MessageTable.id,
                UserTable.displayName,
                MessageTable.text,
                MessageTable.createdAt,
                MessageTable.updatedAt,
                MessageTable.type,
            ).where {
                MessageTable.chatId eq chatId
            }.orderBy(MessageTable.createdAt, SortOrder.DESC)
            .map {
                Message(
                    id = it[MessageTable.id].value,
                    displayName = it[UserTable.displayName],
                    text = it[MessageTable.text],
                    createdAt = it[MessageTable.createdAt]!!.toString(),
                    updatedAt = it[MessageTable.updatedAt]?.toString(),
                    type = it[MessageTable.type]!!
                )
            }
    }
}