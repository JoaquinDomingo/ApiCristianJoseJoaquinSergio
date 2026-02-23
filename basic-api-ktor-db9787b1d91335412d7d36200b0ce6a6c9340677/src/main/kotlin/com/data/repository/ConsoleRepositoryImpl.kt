package com.data.repository

import com.data.database.DatabaseFactory
import com.data.entities.ConsoleTable
import com.domain.models.Console
import com.domain.models.UpdateConsole
import com.domain.repository.ConsoleRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ConsoleRepositoryImpl : ConsoleRepository {

    override suspend fun getConsoles(): List<Console> = DatabaseFactory.dbQuery {
        ConsoleTable.selectAll().map { it.toConsole() }
    }

    override suspend fun addConsole(console: Console) {
        DatabaseFactory.dbQuery {
            ConsoleTable.insert {
                it[name] = console.name
                it[releaseDate] = console.releasedate
                it[company] = console.company
                it[description] = console.description
                it[image] = console.image
            }
        }
    }

    override suspend fun getConsoleByName(name: String): Console? = DatabaseFactory.dbQuery {
        ConsoleTable.select { ConsoleTable.name eq name }.singleOrNull()?.toConsole()
    }

    override suspend fun deleteConsoleByName(name: String): Boolean {
        return DatabaseFactory.dbQuery {
            ConsoleTable.deleteWhere { ConsoleTable.name eq name } > 0
        }
    }

    override suspend fun updateConsole(name: String, update: UpdateConsole): Console? {
        return DatabaseFactory.dbQuery {
            val target = ConsoleTable.select { ConsoleTable.name eq name }.singleOrNull() ?: return@dbQuery null

            ConsoleTable.update({ ConsoleTable.name eq name }) {
                update.name?.let { newName -> it[ConsoleTable.name] = newName }
                update.releasedate?.let { rd -> it[releaseDate] = rd }
                update.company?.let { comp -> it[company] = comp }
                update.description?.let { desc -> it[description] = desc }
                update.image?.let { img -> it[image] = img }
            }

            ConsoleTable.select { ConsoleTable.name eq (update.name ?: name) }
                .singleOrNull()
                ?.toConsole() ?: target.toConsole()
        }
    }

    private fun ResultRow.toConsole() = Console(
        name = this[ConsoleTable.name],
        releasedate = this[ConsoleTable.releaseDate],
        company = this[ConsoleTable.company],
        description = this[ConsoleTable.description],
        image = this[ConsoleTable.image]
    )
}
