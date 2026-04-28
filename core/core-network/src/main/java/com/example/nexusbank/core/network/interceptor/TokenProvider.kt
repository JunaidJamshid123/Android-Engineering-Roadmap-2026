package com.example.nexusbank.core.network.interceptor

/**
 * Abstraction for providing auth tokens. Implemented in core-security or core-data
 * to avoid circular dependencies.
 */
interface TokenProvider {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(accessToken: String, refreshToken: String)
    fun clearTokens()
}
