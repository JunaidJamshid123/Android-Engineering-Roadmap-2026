package com.example.nexusbank.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val message: String,
    val code: Int? = null
)
