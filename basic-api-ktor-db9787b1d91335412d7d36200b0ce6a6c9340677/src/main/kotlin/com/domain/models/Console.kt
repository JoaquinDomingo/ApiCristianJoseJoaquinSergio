package com.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Console(
    var name: String,
    var releasedate: String,
    var company: String,
    var description: String,
    var image: String,
    val price: Double,
    val favorite: Boolean,
    @SerialName("user_email") val userEmail: String,
    var nativeGames: List<Game> = emptyList(),
    var adaptedGames: List<Game> = emptyList()
    
)
