package com.example.nexusbank.feature.auth.data.repository

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.api.AuthApiService
import com.example.nexusbank.core.network.model.LoginRequest
import com.example.nexusbank.core.network.model.SendOtpRequest
import com.example.nexusbank.core.network.model.VerifyOtpRequest
import com.example.nexusbank.core.network.util.NetworkResult
import com.example.nexusbank.core.network.util.safeApiCall
import com.example.nexusbank.core.security.EncryptedPrefs
import com.example.nexusbank.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val encryptedPrefs: EncryptedPrefs
) : AuthRepository {

    override suspend fun sendOtp(phone: String): Resource<String> {
        return when (val result = safeApiCall { authApiService.sendOtp(SendOtpRequest(phone)) }) {
            is NetworkResult.Success -> Resource.Success(result.data.otpRef)
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun verifyOtp(phone: String, otp: String, otpRef: String): Resource<String> {
        return when (val result = safeApiCall { authApiService.verifyOtp(VerifyOtpRequest(phone, otp, otpRef)) }) {
            is NetworkResult.Success -> Resource.Success(result.data.tempToken)
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun loginWithMpin(phone: String, mpin: String): Resource<Unit> {
        val deviceId = encryptedPrefs.deviceId ?: ""
        return when (val result = safeApiCall { authApiService.loginWithMpin(LoginRequest(phone, mpin, deviceId)) }) {
            is NetworkResult.Success -> {
                encryptedPrefs.accessToken = result.data.accessToken
                encryptedPrefs.refreshToken = result.data.refreshToken
                Resource.Success(Unit)
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            safeApiCall { authApiService.logout() }
            encryptedPrefs.clearSession()
            Resource.Success(Unit)
        } catch (e: Exception) {
            encryptedPrefs.clearSession()
            Resource.Success(Unit)
        }
    }

    override fun isLoggedIn(): Boolean = encryptedPrefs.isLoggedIn()
}
