package com.ktor

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.websocket.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Duration
import java.util.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // 1. Inicializar Base de Datos (MariaDB)
    configureDatabase()

    // 2. Configurar WebSockets para el Chat en tiempo real
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    // 3. Configuración de JSON
    install(ContentNegotiation) {
        json()
    }

    // 4. Seguridad JWT
    install(Authentication) {
        val jwtSecret = "mi_super_secreto"
        val jwtIssuer = "ktor.io"
        val jwtAudience = "ktor-audience"

        jwt("auth-jwt") {
            realm = "ktor sample app"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("email").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    // 5. Rutas (donde incluiremos el WebSocket)
    configureRouting()
}