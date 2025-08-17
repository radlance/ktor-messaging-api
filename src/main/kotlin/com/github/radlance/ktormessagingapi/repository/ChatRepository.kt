package com.github.radlance.ktormessagingapi.repository

import com.github.radlance.ktormessagingapi.database.entity.UserEntity
import com.github.radlance.ktormessagingapi.database.table.ChatMemberTable
import com.github.radlance.ktormessagingapi.database.table.MessageTable
import com.github.radlance.ktormessagingapi.database.table.UserTable
import com.github.radlance.ktormessagingapi.domain.chats.MessageType
import com.github.radlance.ktormessagingapi.util.loggedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class ChatRepository {
    suspend fun leaveChat(email: String, chatId: Int) = loggedTransaction {
        val currentUser = UserEntity.find { UserTable.email eq email }.first()
        ChatMemberTable.deleteWhere { (chat eq chatId) and (user eq currentUser.id) }
        MessageTable.insert {
            it[chat] = chatId
            it[text] = "${currentUser.displayName} left the chat"
            it[type] = MessageType.SYSTEM.displayName
        }
    }
}