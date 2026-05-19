package com.example.nexusbank.core.security.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of a biometric authentication attempt.
 */
sealed interface BiometricResult {
    data object Success : BiometricResult
    data object Cancelled : BiometricResult
    data class Failed(val reason: String) : BiometricResult
    data class HardwareUnavailable(val reason: String) : BiometricResult
}

/**
 * Thin wrapper over [BiometricPrompt] for gating sensitive actions
 * (transfers, statement download, profile edit). Always calls back on
 * the main thread.
 *
 * Usage in a ViewModel/Activity:
 * ```
 * biometricAuthenticator.authenticate(
 *     activity = this,
 *     title = "Confirm transfer",
 *     subtitle = "Authenticate to send ₹5,000",
 * ) { result ->
 *     if (result is BiometricResult.Success) viewModel.submitTransfer()
 * }
 * ```
 */
@Singleton
class BiometricAuthenticator @Inject constructor() {

    /**
     * @return true when the device has enrolled biometrics that can be used.
     */
    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(ALLOWED_AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String = "",
        negativeButtonText: String = "Cancel",
        onResult: (BiometricResult) -> Unit,
    ) {
        val manager = BiometricManager.from(activity)
        when (manager.canAuthenticate(ALLOWED_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Unit
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                return onResult(BiometricResult.HardwareUnavailable("No biometric hardware"))
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                return onResult(BiometricResult.HardwareUnavailable("Hardware unavailable"))
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                return onResult(BiometricResult.HardwareUnavailable("No biometrics enrolled"))
            else ->
                return onResult(BiometricResult.HardwareUnavailable("Biometrics unavailable"))
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(BiometricResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> onResult(BiometricResult.Cancelled)
                        else -> onResult(BiometricResult.Failed(errString.toString()))
                    }
                }

                override fun onAuthenticationFailed() {
                    // User attempted but biometric didn't match. Prompt
                    // stays open — don't dispatch a terminal result here.
                }
            },
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            // Negative button is forbidden when DEVICE_CREDENTIAL is allowed,
            // so only set it for strong-only configurations.
            .apply {
                if (ALLOWED_AUTHENTICATORS == BiometricManager.Authenticators.BIOMETRIC_STRONG) {
                    setNegativeButtonText(negativeButtonText)
                }
            }
            .setConfirmationRequired(true)
            .build()

        prompt.authenticate(info)
    }

    private companion object {
        // STRONG biometric + device credential fallback. Acceptable for
        // banking-tier auth on Android 11+. For Android 10 and below,
        // device credential is the PIN/pattern/password.
        const val ALLOWED_AUTHENTICATORS: Int =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
}
