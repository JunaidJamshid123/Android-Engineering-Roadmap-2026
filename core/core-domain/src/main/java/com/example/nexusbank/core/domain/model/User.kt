package com.example.nexusbank.core.domain.model

data class User(
    val id: String,
    val userId: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val dateOfBirth: String?,
    val gender: String?,
    val kycStatus: KycStatus,
    val fatherName: String?,
    val cnic: String?,
    val maritalStatus: String?,
    val nationality: String?,
    val occupation: String?,
    val monthlyIncome: String?,
    val addressLine: String?,
    val city: String?,
    val country: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val profilePictureUrl: String?,
    val createdAt: String?,
    val updatedAt: String?
)

enum class KycStatus {
    NOT_STARTED, PENDING, VERIFIED, REJECTED
}
