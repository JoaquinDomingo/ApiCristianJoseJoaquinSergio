package com.data.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.data.database.DatabaseFactory
import com.data.entities.UserTable
import com.domain.models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UserRepository {

    suspend fun registerUser(user: User): User? = DatabaseFactory.dbQuery {
        val exists = UserTable.select { UserTable.email eq user.email }.any()
        if (exists) return@dbQuery null

        val passwordHash = BCrypt.withDefaults().hashToString(12, user.password.toCharArray())

        val id = UserTable.insertAndGetId {
            it[UserTable.email] = user.email
            it[UserTable.passwordHash] = passwordHash
            it[UserTable.username] = user.username
        }

        user.copy(id = id.value, password = passwordHash)
    }

    suspend fun login(email: String, password: String): User? = DatabaseFactory.dbQuery {
        UserTable.select { UserTable.email eq email }
            .map { row ->
                val hash = row[UserTable.passwordHash]
                val result = BCrypt.verifyer().verify(password.toCharArray(), hash)
                if (result.verified) rowToUser(row) else null
            }
            .singleOrNull()
    }

    private fun rowToUser(row: ResultRow) = User(
        id = row[UserTable.id].value,
        username = row[UserTable.username],
        email = row[UserTable.email],
        password = row[UserTable.passwordHash],
        token = null
    )
}