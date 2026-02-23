package com.ktor

import com.data.database.DatabaseFactory
import io.ktor.server.application.*

fun Application.configureDatabase() {
    DatabaseFactory.init(environment.config)
}
