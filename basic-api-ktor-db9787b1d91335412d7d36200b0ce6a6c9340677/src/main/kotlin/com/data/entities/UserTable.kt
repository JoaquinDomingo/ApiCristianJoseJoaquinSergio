package com.data.entities

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("users") {

    val email = varchar("email", 128).uniqueIndex()
    val passwordHash = varchar("password_hash", 256)
}