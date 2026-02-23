package com.data.entities

import org.jetbrains.exposed.dao.id.IntIdTable

object GameTable : IntIdTable(name = "games") {
    val title = varchar("title", 128)
    val releaseDate = varchar("release_date", 32)
    val description = varchar("description", 512)
    val image = varchar("image", 512)
    
    // Clave foránea que vincula el juego con el nombre de la consola
    val consoleName = varchar("console_name", 128).references(ConsoleTable.name)
    val isNative = bool("is_native").default(true)
}