package com.example.nexusbank.core.domain.model

data class Beneficiary(
    val id: String,
    val userId: String,
    val name: String,
    val accountNumber: String,
    val ifscCode: String,
    val bankName: String,
    val nickname: String?,
    val transferLimit: Double?,
    val isVerified: Boolean,
    val createdAt: Long
)
