package com.example.nexusbank.feature.auth.domain.repository

import com.example.nexusbank.core.domain.util.Resource

interface AuthRepository {
    suspend fun sendOtp(phone: String): Resource<String>
    suspend fun verifyOtp(phone: String, otp: String, otpRef: String): Resource<String>
    suspend fun loginWithMpin(phone: String, mpin: String): Resource<Unit>
    suspend fun logout(): Resource<Unit>
    fun isLoggedIn(): Boolean
}
