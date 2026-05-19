package com.example.nexusbank.core.security.integrity

import android.content.Context
import com.example.nexusbank.core.common.result.DomainError
import com.example.nexusbank.core.common.result.DomainResult
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Wrapper over Google Play Integrity API.
 *
 * Flow:
 *  1. Client asks server for a fresh nonce (server stores it, single-use).
 *  2. Client calls [requestIntegrityToken] with that nonce.
 *  3. Client POSTs the returned token back to the server.
 *  4. Server decodes it via Google's Play Integrity API and verifies
 *     `deviceIntegrity`, `appIntegrity`, and the embedded nonce match.
 *
 * If the token is missing or verdict is weak, the server rejects the
 * sensitive operation (login, transfer, statement download).
 *
 * Setup required (outside code):
 *  - Register the app in Google Play Console → Release → App integrity.
 *  - Add Cloud Project number; enable the Play Integrity API in GCP.
 *
 * Until that's done, calls will return [DomainError.UnknownError]; the
 * call site should treat absence-of-token as a fail-open in debug and
 * fail-closed in release builds.
 */
@Singleton
class PlayIntegrityClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Request an integrity token for [nonce]. The nonce MUST come from
     * the server and be at least 16 bytes (URL-safe base64 recommended).
     */
    suspend fun requestIntegrityToken(nonce: String): DomainResult<String> {
        if (nonce.length < MIN_NONCE_LENGTH) {
            return DomainResult.Error(
                DomainError.Validation(message = "Integrity nonce too short (min $MIN_NONCE_LENGTH chars)")
            )
        }

        return runCatching {
            val manager = IntegrityManagerFactory.create(context)
            val request = IntegrityTokenRequest.builder()
                .setNonce(nonce)
                .build()

            suspendCancellableCoroutine<String> { cont ->
                manager.requestIntegrityToken(request)
                    .addOnSuccessListener { response ->
                        cont.resume(response.token())
                    }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(e)
                    }
            }
        }.fold(
            onSuccess = { DomainResult.Success(it) },
            onFailure = { DomainResult.Error(DomainError.Unknown(cause = it)) },
        )
    }

    private companion object {
        const val MIN_NONCE_LENGTH = 16
    }
}
