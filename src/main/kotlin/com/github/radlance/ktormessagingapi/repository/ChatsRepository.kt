package com.github.radlance.ktormessagingapi.repository

import com.github.radlance.ktormessagingapi.domain.chats.Chat
import com.github.radlance.ktormessagingapi.domain.chats.Message
import com.github.radlance.ktormessagingapi.util.loggedTransaction

class ChatsRepository {

    suspend fun chats(currentUserEmail: String): List<Chat> = loggedTransaction {

        val chats = mutableListOf<Chat>()

        exec(
            """
            SELECT c.id                         AS chat_id,
                   c.title                      AS chat_name,
                   lm.text                      AS last_message_text,
                   u.email                      AS last_message_sender_email,
                   lm.created_at                AS last_message_timestamp,
                   COALESCE(um.unread_count, 0) AS unread_count
            FROM chat c
                     JOIN chat_member cm
                          ON cm.chat_id = c.id
                     JOIN users cu
                          ON cm.user_id = cu.id
                     CROSS JOIN LATERAL (
                SELECT m.id, m.text, m.sender_id, m.created_at
                FROM message m
                WHERE m.chat_id = c.id
                ORDER BY m.created_at DESC
                LIMIT 1
                ) lm
                     JOIN users u
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
                    Chat(
                        id = rs.getInt("chat_id"),
                        name = rs.getString("chat_name"),
                        lastMessage = Message(
                            text = rs.getString("last_message_text"),
                            senderEmail = rs.getString("last_message_sender_email"),
                            sendDate = rs.getTimestamp("last_message_timestamp").toString()
                        ),
                        unreadCount = rs.getInt("unread_count")
                    )
                )
            }
        }

        return@loggedTransaction chats
    }
}