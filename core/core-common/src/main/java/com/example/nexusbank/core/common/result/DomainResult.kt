package com.example.nexusbank.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A discriminated wrapper for results returned from repositories and use cases.
 *
 * Use this everywhere a call can succeed, fail, or be in progress. It removes
 * the need to throw across layer boundaries and keeps error handling explicit
 * in the UI / ViewModel layer.
 */
sealed interface DomainResult<out T> {

    /** Operation completed successfully and produced [data]. */
    data class Success<T>(val data: T) : DomainResult<T>

    /**
     * Operation failed. [throwable] is the underlying cause (may be null when
     * the failure was synthesised, e.g. a validation error). [message] is a
     * human-readable description; prefer using a [DomainError] when possible.
     */
    data class Error(
        val throwable: Throwable? = null,
        val message: String? = throwable?.message,
    ) : DomainResult<Nothing>

    /** Operation is in progress. Useful when emitting from a [Flow]. */
    data object Loading : DomainResult<Nothing>
}

// ---------------------------------------------------------------------------
// Construction helpers
// ---------------------------------------------------------------------------

/** Wraps [value] in [DomainResult.Success]. */
fun <T> T.asSuccess(): DomainResult<T> = DomainResult.Success(this)

/** Wraps [throwable] in [DomainResult.Error]. */
fun Throwable.asError(): DomainResult<Nothing> = DomainResult.Error(this)

/**
 * Executes [block] and wraps the outcome in a [DomainResult].
 * Cancellation is rethrown so coroutines remain cooperative.
 */
inline fun <T> runCatchingDomain(block: () -> T): DomainResult<T> = try {
    DomainResult.Success(block())
} catch (cancellation: kotlinx.coroutines.CancellationException) {
    throw cancellation
} catch (t: Throwable) {
    DomainResult.Error(t)
}

// ---------------------------------------------------------------------------
// Transformations
// ---------------------------------------------------------------------------

/** Maps [DomainResult.Success] values; leaves Error / Loading untouched. */
inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> =
    when (this) {
        is DomainResult.Success -> DomainResult.Success(transform(data))
        is DomainResult.Error -> this
        DomainResult.Loading -> DomainResult.Loading
    }

/** Chains another [DomainResult]-producing operation on success. */
inline fun <T, R> DomainResult<T>.flatMap(transform: (T) -> DomainResult<R>): DomainResult<R> =
    when (this) {
        is DomainResult.Success -> transform(data)
        is DomainResult.Error -> this
        DomainResult.Loading -> DomainResult.Loading
    }

/** Returns the success value or null. */
fun <T> DomainResult<T>.getOrNull(): T? = (this as? DomainResult.Success)?.data

/** Returns the success value or [default] for non-success results. */
fun <T> DomainResult<T>.getOrDefault(default: T): T = getOrNull() ?: default

inline fun <T> DomainResult<T>.onSuccess(action: (T) -> Unit): DomainResult<T> = apply {
    if (this is DomainResult.Success) action(data)
}

inline fun <T> DomainResult<T>.onError(action: (DomainResult.Error) -> Unit): DomainResult<T> = apply {
    if (this is DomainResult.Error) action(this)
}

inline fun <T> DomainResult<T>.onLoading(action: () -> Unit): DomainResult<T> = apply {
    if (this is DomainResult.Loading) action()
}

val DomainResult<*>.isSuccess: Boolean get() = this is DomainResult.Success
val DomainResult<*>.isError: Boolean get() = this is DomainResult.Error
val DomainResult<*>.isLoading: Boolean get() = this is DomainResult.Loading

// ---------------------------------------------------------------------------
// Flow helpers
// ---------------------------------------------------------------------------

/**
 * Wraps a cold [Flow] producing raw values into a Flow of [DomainResult],
 * emitting [DomainResult.Loading] first and catching exceptions as
 * [DomainResult.Error].
 */
fun <T> Flow<T>.asDomainResult(
    context: CoroutineContext = EmptyCoroutineContext,
): Flow<DomainResult<T>> = this
    .map<T, DomainResult<T>> { DomainResult.Success(it) }
    .onStart { emit(DomainResult.Loading) }
    .catch { emit(DomainResult.Error(it)) }
    .let { if (context == EmptyCoroutineContext) it else it.flowOn(context) }

/**
 * Builds a [Flow] of [DomainResult] from a suspending [block]. Emits
 * [DomainResult.Loading], then either [DomainResult.Success] or
 * [DomainResult.Error].
 */
fun <T> domainResultFlow(block: suspend () -> T): Flow<DomainResult<T>> = flow {
    emit(DomainResult.Loading)
    emit(runCatchingDomain { block() })
}
