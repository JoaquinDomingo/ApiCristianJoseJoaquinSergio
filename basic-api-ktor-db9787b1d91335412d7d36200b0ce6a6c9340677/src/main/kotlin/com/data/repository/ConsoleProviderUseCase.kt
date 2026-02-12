package com.data.repository

import com.domain.models.Console
import com.domain.models.UpdateConsole
import com.domain.repository.ConsoleRepository

object ConsoleProviderUseCase {
    private val repository = ConsoleRepositoryImpl()

    suspend fun getAllConsoles() = repository.getConsoles()

    suspend fun insertConsole(console: Console): Boolean {
        repository.addConsole(console)
        return true
    }

    suspend fun updateConsole(name: String, update: UpdateConsole): Console? {
        return repository.updateConsole(name, update)
    }

    suspend fun deleteConsoleByName(name: String): Boolean {
        return repository.deleteConsoleByName(name)
    }

    suspend fun getConsoleByName(name: String): Console? {
        return repository.getConsoles().find { it.name == name }
    }
}