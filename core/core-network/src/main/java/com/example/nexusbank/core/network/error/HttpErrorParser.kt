package com.example.nexusbank.core.network.error

import com.example.nexusbank.core.network.model.ApiError
import kotlinx.serialization.json.Json
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses `ApiError` bodies returned by the backend on non-2xx responses.
 * Falls back to `null` when the body is missing or unparseable so the
 * interceptor can still produce a typed [NetworkException.Http] with the
 * status code only.
 */
@Singleton
class HttpErrorParser @Inject constructor(
    private val json: Json,
) {
    fun parseMessage(response: Response): String? {
        val body = response.peekBody(MAX_PEEK_BYTES).string()
        if (body.isBlank()) return null
        return runCatching { json.decodeFromString(ApiError.serializer(), body).message }
            .getOrNull()
    }

    private companion object {
        const val MAX_PEEK_BYTES = 1L * 1024 * 1024 // 1 MiB
    }
}
