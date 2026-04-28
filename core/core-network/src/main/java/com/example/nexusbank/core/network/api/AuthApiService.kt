package com.example.nexusbank.core.network.api

import com.example.nexusbank.core.network.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<OtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("auth/login-mpin")
    suspend fun loginWithMpin(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}
