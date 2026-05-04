package com.example.nexusbank.core.network.model

import kotlinx.serialization.Serializable

// ── Generic API wrapper ──

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

// ── Auth DTOs ──

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String,
    val mpin: String,
    val deviceId: String,
    val deviceName: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = null
)

@Serializable
data class LoginResponseData(
    val accessToken: String,
    val refreshToken: String,
    val user: LoginUserData
)

@Serializable
data class LoginUserData(
    val id: String,
    val phone: String,
    val fullName: String,
    val email: String? = null,
    val kycStatus: String? = null
)

@Serializable
data class RegisterRequest(
    val phone: String,
    val fullName: String,
    val email: String,
    val dateOfBirth: String,
    val gender: String,
    val password: String,
    val mpin: String
)

@Serializable
data class RegisterResponseData(
    val id: String,
    val phone: String,
    val fullName: String,
    val email: String? = null
)

@Serializable
data class CheckPhoneData(
    val exists: Boolean
)

@Serializable
data class MeResponseData(
    val id: String,
    val phone: String,
    val email: String? = null,
    val fullName: String,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val avatarUrl: String? = null,
    val kycStatus: String? = null,
    val riskCategory: String? = null,
    val isActive: Boolean = true,
    val isBlocked: Boolean = false,
    val blockedReason: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val bankAccounts: List<BankAccountDto> = emptyList()
)

@Serializable
data class BankAccountDto(
    val id: String,
    val accountNumber: String,
    val accountType: String,
    val balance: String,
    val currency: String,
    val status: String,
    val createdAt: String? = null
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class SendOtpRequest(
    val phone: String
)

@Serializable
data class OtpResponse(
    val otpRef: String
)

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val otp: String,
    val otpRef: String
)

@Serializable
data class VerifyOtpResponse(
    val tempToken: String
)
