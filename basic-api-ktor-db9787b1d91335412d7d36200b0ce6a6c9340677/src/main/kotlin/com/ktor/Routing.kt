package com.ktor

import com.data.repository.ConsoleProviderUseCase
import com.data.repository.UserRepository
import com.domain.models.Console
import com.domain.models.Game
import com.domain.models.UpdateConsole
import com.domain.models.User
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Servidor de Consolas Activo")
        }

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

        authenticate("auth-jwt") {
            // 1. Obtener todas las consolas
            get("/console") {
                val consoles = ConsoleProviderUseCase.getAllConsoles()
                call.respond(consoles)
            }

            // 2. Obtener una consola por nombre
            get("/console/{name}") {
                val name = call.parameters["name"] ?: ""
                val console = ConsoleProviderUseCase.getConsoleByName(name)
                if (console == null) {
                    call.respond(HttpStatusCode.NotFound, "No se ha encontrado la consola")
                } else {
                    call.respond(console)
                }
            }

            // 3. Crear una nueva consola
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

            // 4. Añadir juego
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

            // 5. Actualizar consola
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

            // 6. Eliminar consola
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

        // Recursos estáticos (imágenes, etc.)
        staticResources("/static", "static")
    }
}