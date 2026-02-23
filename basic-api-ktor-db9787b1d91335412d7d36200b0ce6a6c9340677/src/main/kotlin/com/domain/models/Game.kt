package com.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Game (
    val title: String,
    val releaseDate: String,
    val description: String,
    val image: String
)