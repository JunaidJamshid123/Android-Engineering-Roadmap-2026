package com.example.nexusbank.core.network.model

import kotlinx.serialization.Serializable

/**
 * Profile DTO returned by `GET /api/profile`.
 *
 * The backend wraps the payload in the generic [ApiResponse] envelope
 * (`{ "success": true, "data": { ... } }`), so the API method should
 * request `Response<ApiResponse<UserDto>>`.
 */
@Serializable
data class UserDto(
    val id: String,
    val userId: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val kycStatus: String,
    val fatherName: String? = null,
    val cnic: String? = null,
    val maritalStatus: String? = null,
    val nationality: String? = null,
    val occupation: String? = null,
    val monthlyIncome: String? = null,
    val addressLine: String? = null,
    val city: String? = null,
    val country: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val profilePictureUrl: String? = null,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class UpdateProfileRequest(
    val fullName: String? = null,
    val email: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val fatherName: String? = null,
    val cnic: String? = null,
    val maritalStatus: String? = null,
    val nationality: String? = null,
    val occupation: String? = null,
    val monthlyIncome: Long? = null,
    val addressLine: String? = null,
    val city: String? = null,
    val country: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null
)

@Serializable
data class UpdateProfilePictureRequest(
    val profilePictureUrl: String
)
