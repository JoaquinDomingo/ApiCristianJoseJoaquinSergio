package com.data.repository

import com.data.database.DatabaseFactory
import com.data.entities.ConsoleTable
import com.data.entities.GameTable
import com.domain.models.Console
import com.domain.models.Game
import com.domain.models.UpdateConsole
import com.domain.repository.ConsoleRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ConsoleRepositoryImpl : ConsoleRepository {

    private fun ResultRow.toConsoleWithGames(): Console {
        val consoleName = this[ConsoleTable.name]
        
        // CORRECCIÓN: Usar selectAll().where para evitar ambigüedades en versiones nuevas
        val allGamesRows = GameTable.selectAll().where { GameTable.consoleName eq consoleName }.toList()

        return Console(
            name = consoleName,
            releasedate = this[ConsoleTable.releaseDate],
            company = this[ConsoleTable.company],
            description = this[ConsoleTable.description],
            image = this[ConsoleTable.image],
            price = this[ConsoleTable.price],
            favorite = this[ConsoleTable.favorite],
            userEmail = this[ConsoleTable.userEmail] 
        ).apply {
            this.nativeGames = allGamesRows
                .filter { it[GameTable.isNative] }
                .map { rowToGameModel(it) }

            this.adaptedGames = allGamesRows
                .filter { !it[GameTable.isNative] }
                .map { rowToGameModel(it) }
        }
    }

    // CORRECCIÓN CRÍTICA: Había un row[row[...]] que causaba el fallo de tipos
    private fun rowToGameModel(row: ResultRow) = Game(
        title = row[GameTable.title],
        releaseDate = row[GameTable.releaseDate],
        description = row[GameTable.description], // Corregido: antes tenías row[row[...]]
        image = row[GameTable.image]
    )

    override suspend fun getConsoles(): List<Console> = DatabaseFactory.dbQuery {
        ConsoleTable.selectAll().map { it.toConsoleWithGames() }
    }

    override suspend fun getConsoleByName(name: String): Console? = DatabaseFactory.dbQuery {
        ConsoleTable.selectAll().where { ConsoleTable.name eq name }
            .singleOrNull()
            ?.toConsoleWithGames()
    }

    override suspend fun addConsole(console: Console) {
        DatabaseFactory.dbQuery {
            ConsoleTable.insert {
                it[name] = console.name
                it[releaseDate] = console.releasedate
                it[company] = console.company
                it[description] = console.description
                it[image] = console.image
                it[price] = console.price
                it[favorite] = console.favorite
                it[userEmail] = console.userEmail 
            }
        }
    }

    override suspend fun deleteConsoleByName(name: String): Boolean = DatabaseFactory.dbQuery {
        // En versiones modernas de Exposed, deleteWhere requiere el scope explícito
        GameTable.deleteWhere { consoleName eq name }
        ConsoleTable.deleteWhere { ConsoleTable.name eq name } > 0
    }

    override suspend fun updateConsole(name: String, update: UpdateConsole): Console? = DatabaseFactory.dbQuery {
        ConsoleTable.update({ ConsoleTable.name eq name }) {
            update.name?.let { n -> it[ConsoleTable.name] = n }
            update.releasedate?.let { r -> it[releaseDate] = r }
            update.company?.let { c -> it[company] = c }
            update.description?.let { d -> it[description] = d }
            update.image?.let { i -> it[image] = i }
            update.price?.let { p -> it[price] = p }
            update.favorite?.let { f -> it[favorite] = f }
        }

        val finalName = update.name ?: name
        ConsoleTable.selectAll().where { ConsoleTable.name eq finalName }
            .singleOrNull()
            ?.toConsoleWithGames()
    }

    override suspend fun addGameToConsole(consoleName: String, game: Game, isNative: Boolean): Boolean = DatabaseFactory.dbQuery {
        try {
            val consoleExists = ConsoleTable.selectAll().where { ConsoleTable.name eq consoleName }.any()
            if (consoleExists) {
                GameTable.insert {
                    it[title] = game.title
                    it[releaseDate] = game.releaseDate
                    it[description] = game.description
                    it[image] = game.image
                    it[GameTable.consoleName] = consoleName
                    it[GameTable.isNative] = isNative
                }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }
}