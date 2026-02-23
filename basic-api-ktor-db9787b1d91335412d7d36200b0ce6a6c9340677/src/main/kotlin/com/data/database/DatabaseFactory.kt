package com.data.database

import com.data.entities.ConsoleTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Properties
import javax.sql.DataSource

object DatabaseFactory {

    fun init(config: ApplicationConfig) {
        val dataSource = hikari(config)
        Database.connect(dataSource)
        transaction {
            SchemaUtils.create(ConsoleTable)
            seedConsoles()
        }
    }

    private fun hikari(config: ApplicationConfig): DataSource {
        val dbConfig = config.config("database")
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = dbConfig.propertyOrNull("url")?.getString()
                ?: "jdbc:h2:./build/db/consoles;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;"
            driverClassName = dbConfig.propertyOrNull("driver")?.getString()
                ?: "org.h2.Driver"
            username = dbConfig.propertyOrNull("user")?.getString() ?: "sa"
            password = dbConfig.propertyOrNull("password")?.getString() ?: ""
            maximumPoolSize = dbConfig.propertyOrNull("maxPoolSize")?.getString()?.toIntOrNull() ?: 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        hikariConfig.validate()
        return HikariDataSource(hikariConfig)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }

    private fun seedConsoles() {
        val exists = ConsoleTable.selectAll().limit(1).empty().not()
        if (exists) return

        val seedData = listOf(
            listOf(
                "Nintendo Entertainment System (NES)",
                "1983",
                "Nintendo",
                "Una consola de 8 bits que revitalizó la industria del videojuego y se volvió un ícono mundial.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/NES-Console-Set.png/2560px-NES-Console-Set.png"
            ),
            listOf(
                "Super NES (SNES)",
                "1990",
                "Nintendo",
                "Consola de 16 bits famosa por sus títulos legendarios y avances en sonido y gráficos.",
                "https://upload.wikimedia.org/wikipedia/commons/1/18/Wikipedia_SNES_PAL.jpg"
            ),
            listOf(
                "Nintendo 64",
                "1996",
                "Nintendo",
                "Primera consola de Nintendo con gráficos en 3D reales y soporte para 4 jugadores.",
                "https://images.cashconverters.es/productslive/consola-nintendo-64/nintendo-nintendo-64_CC076_E464115-0_0.jpg"
            ),
            listOf(
                "PlayStation 1",
                "1994",
                "Sony",
                "Consola de 32 bits que lanzó a Sony al mercado con gran éxito mundial.",
                "https://noseestropea.com/wp-content/uploads/2022/01/PlayStation-scaled.jpg"
            ),
            listOf(
                "PlayStation 2",
                "2000",
                "Sony",
                "La consola más vendida de la historia, con un enorme catálogo de juegos.",
                "https://s3.abcstatics.com/media/tecnologia/2020/03/04/playstation-2-kRQG--1248x698@abc.jpg"
            )
        )

        ConsoleTable.batchInsert(seedData) { console ->
            this[ConsoleTable.name] = console[0]
            this[ConsoleTable.releaseDate] = console[1]
            this[ConsoleTable.company] = console[2]
            this[ConsoleTable.description] = console[3]
            this[ConsoleTable.image] = console[4]
        }
    }
}
