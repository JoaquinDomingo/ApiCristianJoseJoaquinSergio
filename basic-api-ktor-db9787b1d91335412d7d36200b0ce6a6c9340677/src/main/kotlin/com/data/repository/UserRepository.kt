package com.data.repository

import com.data.database.DatabaseFactory
import com.data.entities.UserTable
import com.domain.models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.mindrot.jbcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class UserRepository {
    suspend fun registerUser(user: User): User? = DatabaseFactory.dbQuery {
        val exists = UserTable
            .select { UserTable.email eq user.email }
            .any()
        if (exists) return@dbQuery null
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        val id = UserTable.insertAndGetId {
            it[UserTable.email] = user.email
            it[passwordHash] = hashedPassword
        }
        user.copy(id = id.value, password = "")
    }

    suspend fun login(email: String, password: String): User? = DatabaseFactory.dbQuery {
        val userRow = UserTable
            .select { UserTable.email eq email }
            .singleOrNull()
        if (userRow != null) {
            val storedHash = userRow[UserTable.passwordHash]
            if (BCrypt.checkpw(password, storedHash)) {
                return@dbQuery User(
                    id = userRow[UserTable.id].value,
                    email = userRow[UserTable.email],
                    password = ""
                )
            }
        }
        null
    }

    fun generateToken(userEmail: String): String {
        val jwtSecret = "mi_super_secreto"
        val jwtIssuer = "ktor.io"
        val jwtAudience = "ktor-audience"
        val expiration = System.currentTimeMillis() + 600000
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("email", userEmail)
            .withExpiresAt(Date(expiration))
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}