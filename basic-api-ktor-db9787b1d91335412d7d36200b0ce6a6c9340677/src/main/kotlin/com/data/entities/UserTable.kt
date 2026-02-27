package com.data.entities

import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.dao.id.IdTable

object UserTable : IdTable<Int>("users") {
    override val id = integer("id").autoIncrement().entityId()
    val username = varchar("username", 50)
    val email = varchar("email", 128)
    val passwordHash = varchar("password_hash", 256)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}