package com.data.entities

import org.jetbrains.exposed.dao.id.IntIdTable

object ConsoleTable : IntIdTable(name = "consoles") {
    val name = varchar("name", 128).uniqueIndex()
    val releaseDate = varchar("release_date", 32)
    val company = varchar("company", 64)
    val description = varchar("description", 512)
    val image = varchar("image", 512)
}
