package com.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Console(
    var name: String,
    var releasedate: String,
    var company: String,
    var description: String,
    var image: String
)
