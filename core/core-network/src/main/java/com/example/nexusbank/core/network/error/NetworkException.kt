package com.example.nexusbank.core.network.error

import okhttp3.ResponseBody
import java.io.IOException

/**
 * Typed transport-level exceptions thrown by [ErrorHandlingInterceptor] and
 * the network layer. They extend [IOException] so OkHttp/Retrofit treat them
 * as standard network failures.
 *
 * Data-layer code should not surface these directly to features — map them to
 * `DomainError` via `NetworkErrorMapper` in :core:core-data.
 */
sealed class NetworkException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause) {

    /** No connectivity, DNS failure, host unreachable. */
    class NoConnectivity(cause: Throwable? = null) :
        NetworkException("No network connectivity", cause)

    /** Socket / connect / read timeout. */
    class Timeout(cause: Throwable? = null) :
        NetworkException("Network request timed out", cause)

    /** SSL handshake or certificate pinning failure. */
    class Ssl(cause: Throwable? = null) :
        NetworkException("Secure connection failed", cause)

    /**
     * Non-2xx HTTP response. [serverMessage] is parsed from the response body
     * when it matches the standard `ApiError` shape.
     */
    class Http(
        val code: Int,
        val serverMessage: String? = null,
        val errorBody: ResponseBody? = null,
        cause: Throwable? = null,
    ) : NetworkException(serverMessage ?: "HTTP $code", cause)

    /** Response body could not be deserialised. */
    class Serialization(cause: Throwable? = null) :
        NetworkException("Failed to parse server response", cause)

    /** Any other IOException not covered above. */
    class Unknown(cause: Throwable? = null) :
        NetworkException(cause?.message ?: "Unknown network error", cause)
}
