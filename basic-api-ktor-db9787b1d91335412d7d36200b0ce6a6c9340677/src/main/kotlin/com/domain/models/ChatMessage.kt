package com.domain.models

import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val sender: String,
    val receiver: String,
    val message: String,
    val timestamp: Long
)
