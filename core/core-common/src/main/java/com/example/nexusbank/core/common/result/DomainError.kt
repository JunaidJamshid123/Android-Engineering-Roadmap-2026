package com.example.nexusbank.core.common.result

/**
 * Strongly-typed domain errors. Repositories should map low-level exceptions
 * (IOException, HttpException, SQLiteException, etc.) to one of these values
 * before returning [DomainResult.Error], so the UI never has to know about
 * transport-level types.
 *
 * Extend the hierarchy as new error categories emerge.
 */
sealed class DomainError(
    message: String? = null,
    cause: Throwable? = null,
) : Throwable(message, cause) {

    /** No network connectivity, DNS failure, or socket timeout. */
    class Network(cause: Throwable? = null) : DomainError("Network unavailable", cause)

    /** Server returned a non-2xx response. */
    class Server(val code: Int, message: String? = null, cause: Throwable? = null) :
        DomainError(message ?: "Server error ($code)", cause)

    /** Authentication required or token expired. */
    class Unauthorized(message: String? = "Unauthorized", cause: Throwable? = null) :
        DomainError(message, cause)

    /** Caller is authenticated but lacks permission. */
    class Forbidden(message: String? = "Forbidden", cause: Throwable? = null) :
        DomainError(message, cause)

    /** Requested resource does not exist. */
    class NotFound(message: String? = "Not found", cause: Throwable? = null) :
        DomainError(message, cause)

    /** Input failed validation. [field] identifies which input. */
    class Validation(val field: String? = null, message: String? = null) :
        DomainError(message ?: "Invalid input")

    /** Local persistence failure (Room / DataStore / file IO). */
    class Storage(cause: Throwable? = null) : DomainError("Storage failure", cause)

    /** Unexpected condition that doesn't fit the above categories. */
    class Unknown(cause: Throwable? = null, message: String? = cause?.message) :
        DomainError(message ?: "Unknown error", cause)
}
