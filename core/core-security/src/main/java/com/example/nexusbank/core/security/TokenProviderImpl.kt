package com.example.nexusbank.core.security

import com.example.nexusbank.core.network.interceptor.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProviderImpl @Inject constructor(
    private val encryptedPrefs: EncryptedPrefs
) : TokenProvider {

    override fun getAccessToken(): String? = encryptedPrefs.accessToken

    override fun getRefreshToken(): String? = encryptedPrefs.refreshToken

    override fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs.accessToken = accessToken
        encryptedPrefs.refreshToken = refreshToken
    }

    override fun clearTokens() {
        encryptedPrefs.clearSession()
    }
}
