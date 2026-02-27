package com.domain.models

data class User(
    val id: Int? = null,
    val username: String,
    val email: String,
    val password: String,
    val token: String? = null
)