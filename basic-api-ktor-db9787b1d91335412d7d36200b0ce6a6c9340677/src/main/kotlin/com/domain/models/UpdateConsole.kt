package com.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateConsole(
    var name: String? = null,
    var releasedate: String? = null,
    var company: String? = null,
    var description: String? = null,
    var image: String? = null,
)
