package com.example.nexusbank.core.network.util

import com.example.nexusbank.core.network.model.ApiError
import kotlinx.serialization.json.Json
import retrofit2.Response

/**
 * Safely executes a Retrofit API call and wraps the result.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error("Empty response body", response.code())
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val message = try {
                errorBody?.let { Json.decodeFromString<ApiError>(it).message }
            } catch (_: Exception) {
                null
            } ?: "Unknown error"
            NetworkResult.Error(message, response.code())
        }
    } catch (e: Exception) {
        NetworkResult.Error(e.localizedMessage ?: "Network error")
    }
}

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
}
