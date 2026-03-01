package com.ktor

import com.data.repository.ConsoleProviderUseCase
import com.data.repository.UserRepository
import com.data.repository.MessageRepository // Necesitarás crear este repositorio
import com.domain.models.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import io.ktor.server.auth.jwt.JWTPrincipal

// Mapa global para rastrear usuarios conectados (Email -> Sesión)
val userSessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Servidor de Consolas Activo")
        }

        // --- AUTH PÚBLICA ---
        post("/register") {
            val request = call.receive<User>()
            val repo = UserRepository()
            val result = repo.registerUser(request)
            if (result != null) {
                call.respond(HttpStatusCode.Created, result)
            } else {
                call.respond(HttpStatusCode.Conflict, "Email ya existe")
            }
        }

        post("/login") {
            val request = call.receive<User>()
            val repo = UserRepository()
            val user = repo.login(request.email, request.password)
            if (user != null) {
                val token = repo.generateToken(user.email)
                call.respond(mapOf("token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Email o contraseña incorrectos")
            }
        }

        // --- RUTAS PROTEGIDAS (JWT) ---
        authenticate("auth-jwt") {
            
            // 1. CHAT EN TIEMPO REAL (WebSocket)
            // Se conecta en: ws://dominio/chat?email=usuario@gmail.com
            webSocket("/chat") {
                val email = call.parameters["email"] ?: return@webSocket close(
                    CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Email no proporcionado")
                )

                userSessions[email] = this
                println("INFO: Usuario $email conectado al socket")

                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            val msg = Json.decodeFromString<ChatMessage>(text)

                            // Guardar en MariaDB
                            MessageRepository.saveMessage(msg)

                            // Reenviar al receptor si está conectado
                            userSessions[msg.receiver]?.let { recipientSession ->
                                recipientSession.send(Frame.Text(Json.encodeToString(msg)))
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("ERROR: Chat de $email interrumpido: ${e.localizedMessage}")
                } finally {
                    userSessions.remove(email)
                    println("INFO: Usuario $email desconectado")
                }
            }

            // 2. OBTENER HISTORIAL DE MENSAJES
            // GET /messages?with=amigo@gmail.com
            get("/messages") {
                val myEmail = call.principal<JWTPrincipal>()?.payload?.getClaim("email")?.asString() ?: ""
                val otherEmail = call.request.queryParameters["with"] ?: ""
                
                if (otherEmail.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Falta el parámetro 'with'")
                } else {
                    val history = MessageRepository.getChatHistory(myEmail, otherEmail)
                    call.respond(history)
                }
            }

            // --- RUTAS DE CONSOLAS ---
            get("/console") {
                val consoles = ConsoleProviderUseCase.getAllConsoles()
                call.respond(consoles)
            }

            get("/console/{name}") {
                val name = call.parameters["name"] ?: ""
                val console = ConsoleProviderUseCase.getConsoleByName(name)
                if (console == null) {
                    call.respond(HttpStatusCode.NotFound, "No se ha encontrado la consola")
                } else {
                    call.respond(console)
                }
            }

            post("/console") {
                try {
                    val console = call.receive<Console>()
                    val res = ConsoleProviderUseCase.insertConsole(console)
                    if (res) {
                        call.respond(HttpStatusCode.Created, "Consola creada: ${console.name}")
                    } else {
                        call.respond(HttpStatusCode.Conflict, "No se ha podido crear la consola")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "JSON de consola inválido")
                }
            }

            post("/console/{name}/game") {
                val name = call.parameters["name"] ?: ""
                val isNative = call.request.queryParameters["isNative"]?.toBoolean() ?: true
                try {
                    val game = call.receive<Game>()
                    val success = ConsoleProviderUseCase.addGameToConsole(name, game, isNative)
                    if (success) {
                        call.respond(HttpStatusCode.Created, "Juego '${game.title}' añadido correctamente")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "La consola '$name' no existe")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "JSON de juego inválido")
                }
            }

            patch("/console/{name}") {
                val name = call.parameters["name"] ?: ""
                try {
                    val update = call.receive<UpdateConsole>()
                    val updated = ConsoleProviderUseCase.updateConsole(name, update)
                    if (updated == null) {
                        call.respond(HttpStatusCode.NotFound, "No se ha encontrado la consola para actualizar")
                    } else {
                        call.respond(HttpStatusCode.OK, updated)
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error al procesar la actualización")
                }
            }

            delete("/console/{name}") {
                val name = call.parameters["name"] ?: ""
                val deleted = ConsoleProviderUseCase.deleteConsoleByName(name)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Consola no encontrada")
                }
            }
        }

        staticResources("/static", "static")
    }
}