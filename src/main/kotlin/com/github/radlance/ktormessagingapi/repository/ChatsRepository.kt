package com.github.radlance.ktormessagingapi.repository

import com.github.radlance.ktormessagingapi.database.entity.ChatEntity
import com.github.radlance.ktormessagingapi.database.entity.UserEntity
import com.github.radlance.ktormessagingapi.database.table.ChatMemberTable
import com.github.radlance.ktormessagingapi.database.table.ChatTable
import com.github.radlance.ktormessagingapi.database.table.MessageTable
import com.github.radlance.ktormessagingapi.database.table.UserTable
import com.github.radlance.ktormessagingapi.domain.chats.Chat
import com.github.radlance.ktormessagingapi.domain.chats.ChatRole
import com.github.radlance.ktormessagingapi.domain.chats.ChatWithLastMessage
import com.github.radlance.ktormessagingapi.domain.chats.Message
import com.github.radlance.ktormessagingapi.domain.chats.MessageType
import com.github.radlance.ktormessagingapi.domain.chats.NewChat
import com.github.radlance.ktormessagingapi.exception.MissingCredentialException
import com.github.radlance.ktormessagingapi.util.loggedTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert

class ChatsRepository {

    suspend fun chats(currentUserEmail: String): List<ChatWithLastMessage> = loggedTransaction {

        val chats = mutableListOf<ChatWithLastMessage>()

        exec(
            """
            SELECT c.id                         AS chat_id,
                   c.title                      AS chat_name,
                   lm.text                      AS last_message_text,
                   COALESCE(u.display_name, lm.type)   AS last_message_display_name,
                   lm.created_at                AS last_message_timestamp,
                   COALESCE(um.unread_count, 0) AS unread_count
            FROM chat c
                     JOIN chat_member cm
                          ON cm.chat_id = c.id
                     JOIN users cu
                          ON cm.user_id = cu.id
                     LEFT JOIN LATERAL (
                SELECT m.id, m.text, m.sender_id, m.created_at, m.type
                FROM message m
                WHERE m.chat_id = c.id
                ORDER BY m.created_at DESC
                LIMIT 1
                ) lm ON TRUE
                     LEFT JOIN users u
                               ON lm.sender_id = u.id
                     CROSS JOIN LATERAL (
                SELECT COUNT(*) AS unread_count
                FROM message m
                         LEFT JOIN message_read_status mrs
                                   ON m.id = mrs.message_id
                                       AND mrs.user_id = cm.user_id
                WHERE m.chat_id = c.id
                  AND mrs.message_id IS NULL
                  AND m.sender_id != cm.user_id
                ) um
            WHERE cu.email = '$currentUserEmail'
            ORDER BY lm.created_at DESC NULLS LAST;
        """.trimIndent()
        ) { rs ->
            while (rs.next()) {
                chats.add(
                    ChatWithLastMessage(
                        id = rs.getInt("chat_id"),
                        name = rs.getString("chat_name"),
                        lastMessage = rs.getString("last_message_text")?.let {
                            Message(
                                text = rs.getString("last_message_text"),
                                displayName = rs.getString("last_message_display_name"),
                                sendDate = rs.getTimestamp("last_message_timestamp").toString()
                            )
                        },
                        unreadCount = rs.getInt("unread_count")
                    )
                )
            }
        }

        return@loggedTransaction chats
    }

    suspend fun createChat(email: String, chat: NewChat): Chat = loggedTransaction {
        val newChat = ChatEntity.new {
            type = "group"
            title = chat.title
        }.toChat()

        val user = UserTable.select(UserTable.id, UserTable.displayName, UserTable.id)
            .where { UserTable.email eq email }
            .single()

        ChatMemberTable.insert {
            it[this.user] = user[UserTable.id]
            it[this.chat] = EntityID(id = newChat.id, table = ChatTable)
            it[role] = ChatRole.ADMIN.displayName
        }

        MessageTable.insert {
            it[this.chat] = newChat.id
            it[text] = "${user[UserTable.displayName]} created a chat"
            it[type] = MessageType.SYSTEM.displayName
        }
        newChat
    }

    suspend fun addMember(currentUserEmail: String, email: String, chatId: Int) = loggedTransaction {

        val currentUser = UserEntity.find { UserTable.email eq currentUserEmail }.first()

        val user = UserEntity.find { UserTable.email eq email }.firstOrNull() ?: throw MissingCredentialException(
            message = "user with email $email not found"
        )

        val existsChatMember = ChatMemberTable.select(ChatMemberTable.user).where {
            (ChatMemberTable.user eq user.id) and (ChatMemberTable.chat eq chatId)
        }.singleOrNull()

        if (existsChatMember?.get(ChatMemberTable.user) == user.id) {
            throw MissingCredentialException(message = "user with email $email already in chat")
        }

        ChatMemberTable.insert {
            it[this.user] = EntityID(id = user.id.value, table = UserTable)
            it[chat] = EntityID(id = chatId, table = ChatTable)
        }

        MessageTable.insert {
            it[this.chat] = chatId
            it[text] = "${currentUser.displayName} added ${user.displayName}"
            it[type] = MessageType.SYSTEM.displayName
        }
    }
}