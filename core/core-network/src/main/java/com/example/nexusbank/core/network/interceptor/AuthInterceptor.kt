package com.example.nexusbank.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adds the Authorization header with the Bearer token to all API requests.
 * Token is sourced from [TokenProvider] which abstracts the storage layer.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for login/refresh endpoints
        val path = originalRequest.url.encodedPath
        if (path.contains("auth/login") ||
            path.contains("auth/send-otp") ||
            path.contains("auth/verify-otp") ||
            path.contains("auth/refresh-token")
        ) {
            return chain.proceed(originalRequest)
        }

        val token = tokenProvider.getAccessToken()
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
