package com.example.nexusbank.core.data.error

import com.example.nexusbank.core.common.result.DomainError
import com.example.nexusbank.core.network.error.NetworkException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single point of conversion from transport-level [Throwable]s (mostly
 * [NetworkException] subclasses produced by `ErrorHandlingInterceptor`) into
 * the feature-facing [DomainError] hierarchy.
 *
 * Inject this into repositories instead of catching exceptions inline.
 */
@Singleton
class NetworkErrorMapper @Inject constructor() {

    fun map(throwable: Throwable): DomainError = when (throwable) {
        is DomainError -> throwable

        is NetworkException.NoConnectivity -> DomainError.Network(throwable)
        is NetworkException.Timeout -> DomainError.Network(throwable)
        is NetworkException.Ssl -> DomainError.Network(throwable)
        is NetworkException.Serialization -> DomainError.Unknown(throwable)
        is NetworkException.Unknown -> DomainError.Unknown(throwable)
        is NetworkException.Http -> throwable.toDomainError()

        // Retrofit/OkHttp types in case the interceptor was bypassed.
        is HttpException -> mapHttpCode(throwable.code(), throwable.message(), throwable)
        is SerializationException -> DomainError.Unknown(throwable)

        else -> DomainError.Unknown(throwable)
    }

    private fun NetworkException.Http.toDomainError(): DomainError =
        mapHttpCode(code, serverMessage, this)

    private fun mapHttpCode(code: Int, message: String?, cause: Throwable): DomainError =
        when (code) {
            401 -> DomainError.Unauthorized(message, cause)
            403 -> DomainError.Forbidden(message, cause)
            404 -> DomainError.NotFound(message, cause)
            in 400..499 -> DomainError.Validation(field = null, message = message)
            in 500..599 -> DomainError.Server(code, message, cause)
            else -> DomainError.Server(code, message, cause)
        }
}
