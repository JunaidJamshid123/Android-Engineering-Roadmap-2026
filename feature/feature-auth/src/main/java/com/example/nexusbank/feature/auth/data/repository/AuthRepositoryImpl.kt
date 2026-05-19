package com.example.nexusbank.feature.auth.data.repository

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.api.AuthApiService
import com.example.nexusbank.core.network.api.UserApiService
import com.example.nexusbank.core.network.model.*
import com.example.nexusbank.core.network.util.NetworkResult
import com.example.nexusbank.core.network.util.safeApiCall
import com.example.nexusbank.core.security.EncryptedPrefs
import com.example.nexusbank.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val userApiService: UserApiService,
    private val encryptedPrefs: EncryptedPrefs
) : AuthRepository {

    override suspend fun register(
        phone: String,
        fullName: String,
        email: String,
        dateOfBirth: String,
        gender: String,
        password: String,
        mpin: String
    ): Resource<RegisterResponseData> {
        val request = RegisterRequest(phone, fullName, email, dateOfBirth, gender, password, mpin)
        val registerResult = safeApiCall { authApiService.register(request) }
        return when (registerResult) {
            is NetworkResult.Success -> {
                val apiResponse = registerResult.data
                if (apiResponse.success && apiResponse.data != null) {
                    // Auto-login so the auth token is persisted; otherwise every
                    // subsequent API call would fail with "Access token is required".
                    when (val loginRes = login(phone, password, mpin)) {
                        is Resource.Success -> Resource.Success(apiResponse.data!!)
                        is Resource.Error -> Resource.Error(
                            loginRes.message ?: "Registered but auto-login failed"
                        )
                        is Resource.Loading -> Resource.Success(apiResponse.data!!)
                    }
                } else {
                    Resource.Error(apiResponse.message ?: "Registration failed")
                }
            }

            is NetworkResult.Error -> Resource.Error(registerResult.message, registerResult.code)
        }
    }

    override suspend fun login(
        phone: String,
        password: String,
        mpin: String
    ): Resource<LoginResponseData> {
        val deviceId = encryptedPrefs.deviceId ?: "android-device"
        val request = LoginRequest(
            phone = phone,
            password = password,
            mpin = mpin,
            deviceId = deviceId,
            deviceName = android.os.Build.MODEL,
            osVersion = "Android ${android.os.Build.VERSION.RELEASE}",
            appVersion = "1.0.0"
        )
        return when (val result = safeApiCall { authApiService.login(request) }) {
            is NetworkResult.Success -> {
                val apiResponse = result.data
                val data = apiResponse.data
                if (apiResponse.success && data != null) {
                    encryptedPrefs.accessToken = data.accessToken
                    encryptedPrefs.refreshToken = data.refreshToken
                    encryptedPrefs.userId = data.user.id
                    Resource.Success(data)
                } else {
                    Resource.Error(apiResponse.message ?: "Login failed")
                }
            }

            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun checkPhone(phone: String): Resource<Boolean> {
        return when (val result = safeApiCall { authApiService.checkPhone(phone) }) {
            is NetworkResult.Success -> {
                val apiResponse = result.data
                if (apiResponse.success && apiResponse.data != null) {
                    Resource.Success(apiResponse.data!!.exists)
                } else {
                    Resource.Error(apiResponse.message ?: "Check failed")
                }
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun getMe(): Resource<MeResponseData> {
        return when (val result = safeApiCall { userApiService.getMe() }) {
            is NetworkResult.Success -> {
                val apiResponse = result.data
                if (apiResponse.success && apiResponse.data != null) {
                    Resource.Success(apiResponse.data!!)
                } else {
                    Resource.Error(apiResponse.message ?: "Failed to fetch user")
                }
            }

            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            safeApiCall { userApiService.logout() }
            encryptedPrefs.clearSession()
            Resource.Success(Unit)
        } catch (e: Exception) {
            encryptedPrefs.clearSession()
            Resource.Success(Unit)
        }
    }

    override fun isLoggedIn(): Boolean = encryptedPrefs.isLoggedIn()
}
