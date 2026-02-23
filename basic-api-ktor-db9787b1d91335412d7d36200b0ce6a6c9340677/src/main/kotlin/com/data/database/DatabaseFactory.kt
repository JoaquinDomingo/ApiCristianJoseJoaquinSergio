package com.data.database

import com.data.entities.ConsoleTable
import com.data.entities.GameTable
import com.domain.models.Console
import com.domain.models.Game
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import javax.sql.DataSource

object DatabaseFactory {

    fun init(config: ApplicationConfig) {
        val dataSource = hikari(config)
        Database.connect(dataSource)
        transaction {
            SchemaUtils.create(ConsoleTable, GameTable)
            seedDatabase()
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

    private fun seedDatabase() {
        if (!ConsoleTable.selectAll().empty()) return

        val seedData = listOf(
            Console("Nintendo Entertainment System (NES)", "1983", "Nintendo", "Una consola de 8 bits que revitalizó la industria del videojuego.", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/NES-Console-Set.png/2560px-NES-Console-Set.png").apply {
                nativeGames = listOf(
                    Game("Super Mario Bros.", "1985", "Plataformas icónico.", "https://m.media-amazon.com/images/I/51kWyydz-QL._AC_UF894,1000_QL80_.jpg"),
                    Game("The Legend of Zelda", "1986", "Aventura en Hyrule.", "https://static.wikia.nocookie.net/zelda/images/d/d7/Zelda_box.jpg/revision/latest?cb=20160714142215&path-prefix=es"),
                    Game("Metroid", "1986", "Acción y exploración.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRc_8bPC8xeObIKpwifKIgCSw_ENi2obeSLSA&s")
                )
            },
            Console("Super NES (SNES)", "1990", "Nintendo", "Consola de 16 bits famosa por sus títulos legendarios.", "https://upload.wikimedia.org/wikipedia/commons/1/18/Wikipedia_SNES_PAL.jpg").apply {
                nativeGames = listOf(
                    Game("Super Mario World", "1990", "Aventura en Dinosaur Land.", "https://www.retroplace.com/pics/snes/packshots/48002--super-mario-world.png"),
                    Game("F-Zero", "1990", "Carreras futuristas.", "https://m.media-amazon.com/images/M/MV5BNjg3ZmNhMDUtMzcwMC00NWJkLWE1MzUtZTNlNjBhM2IyYmQwXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg"),
                    Game("Star Fox", "1993", "Combate espacial en 3D.", "https://m.media-amazon.com/images/M/MV5BZWRlNTk4NTUtNzJiYS00MmZlLTk5NDEtM2U2OTk5ZmE0NDBkXkEyXkFqcGc@._V1_.jpg")
                )
                adaptedGames = listOf(
                    Game("Pac-Man", "1984", "Clásico arcade.", "https://www.retroplace.com/pics/nes/packshots/2721--pac-man.png"),
                    Game("Donkey Kong", "1983", "Port del arcade original.", "https://images.wikidexcdn.net/mwuploads/esssbwiki/thumb/e/e8/latest/20121029150116/Car%C3%A1tula_Donkey_Kong_%28NES%29.jpg/800px-Car%C3%A1tula_Donkey_Kong_%28NES%29.jpg")
                )
            },
            Console("Nintendo 64", "1996", "Nintendo", "Primera consola de Nintendo con gráficos en 3D reales.", "https://images.cashconverters.es/productslive/consola-nintendo-64/nintendo-nintendo-64_CC076_E464115-0_0.jpg").apply {
                nativeGames = listOf(
                    Game("Super Mario 64", "1996", "Plataformas 3D revolucionario.", "https://upload.wikimedia.org/wikipedia/en/thumb/e/e9/Super_Mario_64.png/250px-Super_Mario_64.png"),
                    Game("The Legend of Zelda: Ocarina of Time", "1998", "Obra maestra de aventura.", "https://storage.googleapis.com/images.pricecharting.com/bidbex33joqxpepk/1600.jpg"),
                    Game("GoldenEye 007", "1997", "FPS multijugador legendario.", "https://images.wikidexcdn.net/mwuploads/esssbwiki/thumb/f/fb/latest/20160807231757/Caratula_americana_de_GoldenEye_007.jpg/1200px-Caratula_americana_de_GoldenEye_007.jpg")
                )
                adaptedGames = listOf(
                    Game("Street Fighter II", "1992", "Lucha competitiva.", "https://i.ebayimg.com/images/g/fmYAAOSwA0tkXruS/s-l1200.jpg"),
                    Game("Mortal Kombat", "1992", "Lucha visceral.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSkbnBeUD5_r8s1_3q_Yz2Xm_5SMHKBQGb2ew&s")
                )
            },
            Console("PlayStation 1", "1994", "Sony", "Consola de 32 bits que lanzó a Sony al éxito mundial.", "https://noseestropea.com/wp-content/uploads/2022/01/PlayStation-scaled.jpg").apply {
                nativeGames = listOf(
                    Game("Gran Turismo", "1997", "Simulador de conducción real.", "https://upload.wikimedia.org/wikipedia/en/c/c8/Gran_Turismo_-_Cover_-_JP.jpg"),
                    Game("Tekken 3", "1998", "Lucha 3D de alto nivel.", "https://static.wikia.nocookie.net/tekkenlatino/images/4/4e/300px-Tekken3.jpg/revision/latest?cb=20091206010553&path-prefix=es"),
                    Game("Crash Bandicoot", "1996", "Plataformas carismático.", "https://upload.wikimedia.org/wikipedia/en/4/44/Crash_Bandicoot_Cover.png")
                )
                adaptedGames = listOf(
                    Game("Doom 64", "1997", "FPS de terror demoníaco.", "https://static.wikia.nocookie.net/wadguia/images/2/27/Doom_64_Box.jpg/revision/latest?cb=20141005041621"),
                    Game("Resident Evil 2", "1999", "Survival horror adaptado.", "https://media.vandal.net/m/7-2018/20187613111_1.jpg.webp")
                )
            },
            Console("PlayStation 2", "2000", "Sony", "La consola más vendida de la historia.", "https://s3.abcstatics.com/media/tecnologia/2020/03/04/playstation-2-kRQG--1248x698@abc.jpg").apply {
                nativeGames = listOf(
                    Game("God of War", "2005", "Acción hack and slash.", "https://i.ebayimg.com/images/g/Lk8AAOSwLa9UV3Wh/s-l400.jpg"),
                    Game("Shadow of the Colossus", "2005", "Batallas contra colosos.", "https://static.wikia.nocookie.net/shadowofthecolossusespaol/images/b/b9/Shadow-colossus.jpg/revision/latest?cb=20111111215606&path-prefix=es"),
                    Game("Jak and Daxter", "2001", "Plataformas y aventura.", "https://m.media-amazon.com/images/I/717SiKCGF2L.jpg")
                )
                adaptedGames = listOf(
                    Game("Final Fantasy VII", "1997", "JRPG cinematográfico.", "https://upload.wikimedia.org/wikipedia/en/thumb/c/c2/Final_Fantasy_VII_Box_Art.jpg/250px-Final_Fantasy_VII_Box_Art.jpg"),
                    Game("Metal Gear Solid", "1998", "Sigilo y espionaje.", "https://static.wikia.nocookie.net/videojuego/images/3/36/MetalGearSolidcover.png/revision/latest?cb=20090311195014")
                )
            },
            Console("PlayStation 3", "2006", "Sony", "Soporte HD y Blu-ray, revolucionando el multimedia.", "https://i.blogs.es/746491/ps3/840_560.jpg").apply {
                nativeGames = listOf(
                    Game("Uncharted 2", "2009", "Aventura de acción.", "https://m.media-amazon.com/images/I/916oecP4GUL.jpg"),
                    Game("The Last of Us", "2013", "Drama post-apocalíptico.", "https://m.media-amazon.com/images/I/71zVE5lPRVL.jpg"),
                    Game("Infamous", "2009", "Superpoderes en mundo abierto.", "https://m.media-amazon.com/images/I/71E3B4ZB3WL.jpg")
                )
                adaptedGames = listOf(
                    Game("GTA: San Andreas", "2004", "Crimen en mundo abierto.", "https://static.wikia.nocookie.net/esgta/images/c/c2/Grand_Theft_Auto_San_Andreas.JPG/revision/latest/thumbnail/width/360/height/360?cb=20161127211015"),
                    Game("Resident Evil 4", "2005", "Terror y acción.", "https://static.wikia.nocookie.net/residentevil/images/e/e2/Resident_evil4_ps2.jpg/revision/latest/scale-to-width/360?cb=20250709164434&path-prefix=es")
                )
            },
            Console("Xbox", "2001", "Microsoft", "Pionera con Xbox Live y hardware potente.", "https://cdn.mos.cms.futurecdn.net/8b9bca79a376631e1b09e821e951ea90.jpg").apply {
                nativeGames = listOf(
                    Game("Halo: Combat Evolved", "2001", "FPS de ciencia ficción.", "https://storage.googleapis.com/retrobroker/products/rtr_img_8038_1715802448128/1x.webp"),
                    Game("Fable", "2004", "RPG de acción.", "https://media.vandal.net/m/688/2004102223363_1.jpg"),
                    Game("Forza Motorsport", "2005", "Simulación de carreras.", "https://store-images.s-microsoft.com/image/apps.57449.14311721322459352.08838dcd-1049-42ef-a004-b13ad1d41946.4eeb1f6f-f279-4d5b-83b0-8fb4cc7f9aea")
                )
                adaptedGames = listOf(
                    Game("Splinter Cell", "2002", "Sigilo táctico.", "https://static.wikia.nocookie.net/splintercell/images/a/ab/SplinterCellCover.jpg/revision/latest/thumbnail/width/360/height/360?cb=20131020102635"),
                    Game("Star Wars: KOTOR", "2003", "RPG estelar.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQgz9Dz1EyNmS5hvioYEGq3GxFdUPYbAdR9qg&s")
                )
            },
            Console("Xbox 360", "2005", "Microsoft", "Consolidó el juego online y las grandes franquicias.", "https://t4.ftcdn.net/jpg/03/53/12/41/360_F_353124125_QxCu2PKncCs9GIppQLmvyu2U9xJ0c7Gt.jpg").apply {
                nativeGames = listOf(
                    Game("Gears of War", "2006", "Shooter táctico.", "https://static.wikia.nocookie.net/gearsofwar/images/f/f5/Gears_of_War_Portada.png/revision/latest?cb=20160708053912&path-prefix=es"),
                    Game("Halo 3", "2007", "Conclusión épica.", "https://static.wikia.nocookie.net/halo/images/3/32/Halo_3.png/revision/latest/scale-to-width-down/230?cb=20111014172256&path-prefix=es"),
                    Game("Forza Horizon", "2012", "Carreras en mundo abierto.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS2c5m-vFQC6ZXD0CXjQsOEeesbeI8rVwZmLg&s")
                )
                adaptedGames = listOf(
                    Game("Mass Effect", "2007", "Ópera espacial RPG.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQBqPGIAWxeVqvz-rb29VxF34QJKLHed5PV0w&s"),
                    Game("Fallout 3", "2008", "RPG post-nuclear.", "https://media.vandal.net/m/7296/20081015172418_1.jpg")
                )
            },
            Console("Sega Mega Drive", "1988", "Sega", "Consola de 16 bits conocida por Sonic y la guerra con Nintendo.", "https://static.wikia.nocookie.net/segaenciclopedia/images/f/f3/SegaMegaDrive.png/revision/latest?cb=20171103010533&path-prefix=es").apply {
                nativeGames = listOf(
                    Game("Sonic the Hedgehog", "1991", "Velocidad pura.", "https://upload.wikimedia.org/wikipedia/en/b/ba/Sonic_the_Hedgehog_1_Genesis_box_art.jpg"),
                    Game("Streets of Rage", "1991", "Beat 'em up.", "https://static.wikia.nocookie.net/streetsofrage/images/0/0a/SoRCover.jpg/revision/latest?cb=20190819175212"),
                    Game("Golden Axe", "1989", "Fantasía épica.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRwHAv15bdYteJpfjPAOrM4vTM2Hdg1iy4izw&s")
                )
                adaptedGames = listOf(
                    Game("Castlevania: Bloodlines", "1994", "Vampiros en 16 bits.", "https://upload.wikimedia.org/wikipedia/en/a/af/Castlevania_Bloodlines.jpg"),
                    Game("Mega Man: The Wily Wars", "1994", "Recopilación.", "https://static.wikia.nocookie.net/esmegaman/images/7/7f/WilyWars-Portada-USA.jpg/revision/latest?cb=20130127135251")
                )
            },
            Console("GameCube", "2001", "Nintendo", "Consola compacta con discos mini-DVD y catálogo querido.", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Nintendo-GameCube-Console-FL.jpg/250px-Nintendo-GameCube-Console-FL.jpg").apply {
                nativeGames = listOf(
                    Game("Super Smash Bros. Melee", "2001", "Lucha frenética.", "https://m.media-amazon.com/images/I/71hDoTzdOXS.jpg"),
                    Game("Luigi's Mansion", "2001", "Aventura de fantasmas.", "https://static.wikia.nocookie.net/mario/images/3/34/Luigi%27s_Mansion.jpg/revision/latest?cb=20090413154011&path-prefix=es"),
                    Game("Pikmin", "2001", "Estrategia.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ_cpax_uTS2p3c7KSZM5EHYQFOWJIh3zlYIQ&s")
                )
                adaptedGames = listOf(
                    Game("Resident Evil Remake", "2002", "Terror rediseñado.", "https://static.wikia.nocookie.net/residentevil/images/a/a3/Resident-evil-cover.jpg/revision/latest?cb=20210704170743&path-prefix=es"),
                    Game("Metal Gear Solid: The Twin Snakes", "2004", "Remake clásico.", "https://static.wikia.nocookie.net/metalgear/images/b/b2/256px-Ttsbox.jpg/revision/latest?cb=20121228155657&path-prefix=es")
                )
            }
        )

        seedData.forEach { console ->
            ConsoleTable.insert {
                it[name] = console.name
                it[releaseDate] = console.releasedate
                it[company] = console.company
                it[description] = console.description
                it[image] = console.image
            }

            // INSERTAMOS LOS JUEGOS DIFERENCIANDO NATIVOS DE ADAPTADOS
            console.nativeGames.forEach { game ->
                insertGameRecord(game, console.name, isNative = true)
            }
            console.adaptedGames.forEach { game ->
                insertGameRecord(game, console.name, isNative = false)
            }
        }
    }

    private fun insertGameRecord(game: Game, consoleName: String, isNative: Boolean) {
        GameTable.insert {
            it[title] = game.title
            it[releaseDate] = game.releaseDate
            it[description] = game.description
            it[image] = game.image
            it[GameTable.consoleName] = consoleName
            it[GameTable.isNative] = isNative
        }
    }
}