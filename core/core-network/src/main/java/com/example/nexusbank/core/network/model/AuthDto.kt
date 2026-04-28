package com.example.nexusbank.core.network.model

import kotlinx.serialization.Serializable

// ── Auth DTOs ──

@Serializable
data class LoginRequest(
    val phone: String,
    val mpin: String,
    val deviceId: String
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
