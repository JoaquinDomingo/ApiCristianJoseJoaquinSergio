package com.data.repository

import com.data.database.DatabaseFactory.dbQuery
import com.data.entities.MessagesTable
import com.domain.models.ChatMessage
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object MessageRepository {

    suspend fun saveMessage(msg: ChatMessage) = dbQuery {
        MessagesTable.insert {
            it[sender] = msg.sender
            it[receiver] = msg.receiver
            it[text] = msg.message
            it[timestamp] = msg.timestamp
        }
    }

    suspend fun getChatHistory(userA: String, userB: String): List<ChatMessage> = dbQuery {
        MessagesTable.selectAll()
            .where {
                (MessagesTable.sender eq userA and (MessagesTable.receiver eq userB)) or
                (MessagesTable.sender eq userB and (MessagesTable.receiver eq userA))
            }
            .orderBy(MessagesTable.timestamp to SortOrder.ASC)
            .map {
                ChatMessage(
                    sender = it[MessagesTable.sender],
                    receiver = it[MessagesTable.receiver],
                    message = it[MessagesTable.text],
                    timestamp = it[MessagesTable.timestamp]
                )
            }
    }
}