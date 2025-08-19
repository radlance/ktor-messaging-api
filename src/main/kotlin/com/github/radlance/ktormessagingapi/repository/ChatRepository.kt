package com.github.radlance.ktormessagingapi.repository

import com.github.radlance.ktormessagingapi.database.entity.UserEntity
import com.github.radlance.ktormessagingapi.database.table.ChatMemberTable
import com.github.radlance.ktormessagingapi.database.table.ChatTable
import com.github.radlance.ktormessagingapi.database.table.MessageTable
import com.github.radlance.ktormessagingapi.database.table.UserTable
import com.github.radlance.ktormessagingapi.domain.chat.ChatMember
import com.github.radlance.ktormessagingapi.domain.chat.Message
import com.github.radlance.ktormessagingapi.domain.chat.NewMessage
import com.github.radlance.ktormessagingapi.domain.chats.ChatRole
import com.github.radlance.ktormessagingapi.domain.chats.MessageType
import com.github.radlance.ktormessagingapi.exception.MissingCredentialException
import com.github.radlance.ktormessagingapi.util.loggedTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class ChatRepository {
    suspend fun isMember(email: String, chatId: Int): Boolean = loggedTransaction {
        val user = UserEntity.find { UserTable.email eq email }.firstOrNull() ?: return@loggedTransaction false
        ChatMemberTable.selectAll().where {
            (ChatMemberTable.chatId eq chatId) and (ChatMemberTable.userId eq user.id)
        }.count() > 0
    }

    suspend fun addMember(currentUserEmail: String, email: String, chatId: Int) = loggedTransaction {

        val currentUser = UserEntity.find { UserTable.email eq currentUserEmail }.first()

        val user = UserEntity.find { UserTable.email eq email }.firstOrNull() ?: throw MissingCredentialException(
            message = "user with email $email not found"
        )

        val existsChatMember = ChatMemberTable.select(ChatMemberTable.userId).where {
            (ChatMemberTable.userId eq user.id) and (ChatMemberTable.chatId eq chatId)
        }.singleOrNull()

        if (existsChatMember?.get(ChatMemberTable.userId) == user.id) {
            throw MissingCredentialException(message = "user with email $email already in chat")
        }

        ChatMemberTable.insert {
            it[this.userId] = EntityID(id = user.id.value, table = UserTable)
            it[this.chatId] = EntityID(id = chatId, table = ChatTable)
        }

        MessageTable.insert {
            it[this.chatId] = chatId
            it[text] = "${currentUser.displayName} added ${user.displayName}"
            it[type] = MessageType.SYSTEM.displayName
        }
    }

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
                    type = MessageType.entries.first { entry -> entry.displayName == it[MessageTable.type]!! }
                )
            }
    }

    suspend fun members(chatId: Int): List<ChatMember> = loggedTransaction {
        ChatMemberTable
            .innerJoin(UserTable)
            .select(ChatMemberTable.userId, UserTable.displayName, ChatMemberTable.role)
            .where {
                ChatMemberTable.chatId eq chatId
            }.orderBy(UserTable.displayName, SortOrder.ASC)
            .map {
                ChatMember(
                    userId = it[ChatMemberTable.userId].value,
                    displayName = it[UserTable.displayName],
                    role = ChatRole.entries.first { entry -> entry.displayName == it[ChatMemberTable.role] }
                )
            }
    }
}