package com.domain.repository

import com.domain.models.Console
import com.domain.models.UpdateConsole

interface ConsoleRepository {
    suspend fun getConsoles(): List<Console>
    // suspend fun getConsoleByName(name: String): Console? 
    suspend fun addConsole(console: Console)
    suspend fun deleteConsoleByName(name: String): Boolean
    suspend fun updateConsole(name: String, update: UpdateConsole): Console?
}