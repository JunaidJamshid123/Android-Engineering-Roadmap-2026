package com.example.nexusbank.core.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val avatarUrl: String?,
    val kycStatus: KycStatus,
    val createdAt: Long
)

enum class KycStatus {
    NOT_STARTED, PENDING, VERIFIED, REJECTED
}
