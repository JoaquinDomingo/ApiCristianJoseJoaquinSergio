package com.data.repository

import com.domain.models.Console
import com.domain.models.UpdateConsole
import com.domain.repository.ConsoleRepository

class ConsoleRepositoryImpl : ConsoleRepository {

    private val consoles = mutableListOf(
        Console(
            "Nintendo Entertainment System (NES)",
            "1983",
            "Nintendo",
            "Una consola de 8 bits que revitalizó la industria del videojuego y se volvió un ícono mundial.",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/NES-Console-Set.png/2560px-NES-Console-Set.png"
        ),
        Console(
            "Super NES (SNES)",
            "1990",
            "Nintendo",
            "Consola de 16 bits famosa por sus títulos legendarios y avances en sonido y gráficos.",
            "https://upload.wikimedia.org/wikipedia/commons/1/18/Wikipedia_SNES_PAL.jpg"
        ),
        Console(
            "Nintendo 64",
            "1996",
            "Nintendo",
            "Primera consola de Nintendo con gráficos en 3D reales y soporte para 4 jugadores.",
            "https://images.cashconverters.es/productslive/consola-nintendo-64/nintendo-nintendo-64_CC076_E464115-0_0.jpg"
        ),
        Console(
            "PlayStation 1",
            "1994",
            "Sony",
            "Consola de 32 bits que lanzó a Sony al mercado con gran éxito mundial.",
            "https://noseestropea.com/wp-content/uploads/2022/01/PlayStation-scaled.jpg"
        ),
        Console(
            "PlayStation 2",
            "2000",
            "Sony",
            "La consola más vendida de la historia, con un enorme catálogo de juegos.",
            "https://s3.abcstatics.com/media/tecnologia/2020/03/04/playstation-2-kRQG--1248x698@abc.jpg"
        ),
        Console(
            "PlayStation 3",
            "2006",
            "Sony",
            "Primera consola de Sony con soporte HD y Blu-ray, revolucionando el multimedia.",
            "https://w7.pngwing.com/pngs/686/533/png-transparent-playstation-2-sony-playstation-3-super-slim-black-others-gadget-electronics-video-game.png"
        ),
        Console(
            "Xbox",
            "2001",
            "Microsoft",
            "La primera consola de Microsoft, pionera con Xbox Live y hardware potente para su época.",
            "https://cdn.mos.cms.futurecdn.net/8b9bca79a376631e1b09e821e951ea90.jpg"
        ),
        Console(
            "Xbox 360",
            "2005",
            "Microsoft",
            "Consola muy popular que consolidó el juego online y trajo grandes franquicias.",
            "https://t4.ftcdn.net/jpg/03/53/12/41/360_F_353124125_QxCu2PKncCs9GIppQLmvyu2U9xJ0c7Gt.jpg"
        ),
        Console(
            "Sega Mega Drive",
            "1988",
            "Sega",
            "Consola de 16 bits conocida por Sonic y por la guerra de consolas con Nintendo.",
            "https://static.wikia.nocookie.net/segaenciclopedia/images/f/f3/SegaMegaDrive.png/revision/latest?cb=20171103010533&path-prefix=es"
        ),
        Console(
            "GameCube",
            "2001",
            "Nintendo",
            "Consola compacta con discos mini-DVD y un catálogo muy querido por fans.",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Nintendo-GameCube-Console-FL.jpg/250px-Nintendo-GameCube-Console-FL.jpg"
        )
    )

    override suspend fun getConsoles(): List<Console> = consoles

    override suspend fun addConsole(console: Console) {
        consoles.add(console)
    }

    override suspend fun deleteConsoleByName(name: String): Boolean {
        return consoles.removeIf { it.name == name }
    }

    override suspend fun updateConsole(name: String, update: UpdateConsole): Console? {
        val console = consoles.find { it.name == name } ?: return null

        update.name?.let { console.name = it }
        update.releasedate?.let { console.releasedate = it }
        update.company?.let { console.company = it }
        update.description?.let { console.description = it }
        update.image?.let { console.image = it }

        return console
    }
}