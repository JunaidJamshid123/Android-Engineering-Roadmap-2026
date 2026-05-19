package com.example.nexusbank.core.data.error

import com.example.nexusbank.core.common.result.DomainResult
import kotlinx.coroutines.CancellationException

/**
 * Executes a suspending API call and wraps the outcome in a [DomainResult],
 * routing any thrown exception through [NetworkErrorMapper].
 *
 * Cancellation is rethrown to keep coroutines cooperative.
 *
 * Usage:
 * ```
 * override suspend fun login(req: LoginRequest): DomainResult<UserSession> =
 *     errorMapper.safeApiCall { api.login(req).toDomain() }
 * ```
 */
suspend inline fun <T> NetworkErrorMapper.safeApiCall(
    block: () -> T,
): DomainResult<T> = try {
    DomainResult.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (t: Throwable) {
    val domainError = map(t)
    DomainResult.Error(throwable = domainError, message = domainError.message)
}
