package com.data.entities

import org.jetbrains.exposed.dao.id.IntIdTable

object MessagesTable : IntIdTable(name = "messages") {
    val sender = varchar("sender", 255)    // Email del emisor
    val receiver = varchar("receiver", 255) // Email del receptor
    val text = text("text")                 // El contenido del mensaje (usamos text para que no tenga límite de 255)
    val timestamp = long("timestamp")       // Fecha en milisegundos
}