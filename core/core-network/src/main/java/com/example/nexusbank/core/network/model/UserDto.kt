package com.example.nexusbank.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val avatarUrl: String? = null,
    val kycStatus: String,
    val createdAt: Long
)

@Serializable
data class UserResponse(
    val user: UserDto
)

@Serializable
data class UpdateProfileRequest(
    val name: String,
    val email: String,
    val address: String? = null
)
