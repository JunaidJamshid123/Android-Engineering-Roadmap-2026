package com.example.nexusbank.core.network.interceptor

import com.example.nexusbank.core.network.api.AuthApiService
import com.example.nexusbank.core.network.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Automatically refreshes expired JWT tokens on 401 responses.
 * If refresh fails, clears tokens to force re-login.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val authApiServiceProvider: dagger.Lazy<AuthApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite loop: if we already tried refreshing, give up
        if (response.request.header("X-Retry") != null) {
            return null
        }

        val refreshToken = tokenProvider.getRefreshToken() ?: return null

        // Synchronize to avoid multiple simultaneous refresh calls
        synchronized(this) {
            // Check if another thread already refreshed the token
            val currentToken = tokenProvider.getAccessToken()
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            if (currentToken != null && currentToken != requestToken) {
                // Token was already refreshed by another thread, retry with new token
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .header("X-Retry", "true")
                    .build()
            }

            // Attempt to refresh (runBlocking is acceptable here — OkHttp calls authenticate() on a background thread)
            return try {
                val refreshResponse = runBlocking {
                    authApiServiceProvider.get()
                        .refreshToken(RefreshTokenRequest(refreshToken))
                }

                if (refreshResponse.isSuccessful) {
                    val apiResponse = refreshResponse.body()
                    val data = apiResponse?.data
                    if (data != null) {
                        tokenProvider.saveTokens(data.accessToken, data.refreshToken)

                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${data.accessToken}")
                            .header("X-Retry", "true")
                            .build()
                    } else {
                        tokenProvider.clearTokens()
                        null
                    }
                } else {
                    tokenProvider.clearTokens()
                    null
                }
            } catch (e: Exception) {
                tokenProvider.clearTokens()
                null
            }
        }
    }
}
