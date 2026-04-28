package com.example.nexusbank.core.security

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central security manager handling:
 * - Session timeout / inactivity tracking
 * - Root & emulator detection
 * - Screenshot prevention (FLAG_SECURE)
 * - Auth state management (login, logout, re-auth)
 *
 * Expanded in Phase 8 (Security Hardening) with Play Integrity,
 * SQLCipher, debugger detection, and tamper checks.
 */
@Singleton
class SecurityManager @Inject constructor(
    private val encryptedPrefs: EncryptedPrefs
) {

    // ── Session / Inactivity Timeout ──────────────────────────────

    fun recordActivity() {
        encryptedPrefs.lastActiveTimestamp = System.currentTimeMillis()
    }

    fun isSessionExpired(): Boolean {
        val lastActive = encryptedPrefs.lastActiveTimestamp
        if (lastActive == 0L) return true
        return (System.currentTimeMillis() - lastActive) > SESSION_TIMEOUT_MS
    }

    fun requireReAuth(): Boolean = isSessionExpired()

    // ── Auth State ────────────────────────────────────────────────

    fun isLoggedIn(): Boolean = encryptedPrefs.isLoggedIn()

    fun isMpinSetup(): Boolean = encryptedPrefs.mpinHash != null

    fun isBiometricEnabled(): Boolean = encryptedPrefs.isBiometricEnabled

    fun logout() {
        encryptedPrefs.clearSession()
    }

    fun fullReset() {
        encryptedPrefs.clearAll()
    }

    // ── Screenshot Prevention ─────────────────────────────────────

    fun enableScreenshotPrevention(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun disableScreenshotPrevention(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    // ── Root / Emulator Detection (Stubs) ─────────────────────────

    fun isDeviceRooted(): Boolean {
        // TODO: Integrate Play Integrity API in Phase 8
        return false
    }

    fun isRunningOnEmulator(): Boolean {
        // Basic heuristic — replaced with Play Integrity in Phase 8
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic"))
    }

    fun isDeviceSecure(): Boolean {
        return !isDeviceRooted() && !isRunningOnEmulator()
    }

    companion object {
        /** 5-minute inactivity timeout for banking apps */
        const val SESSION_TIMEOUT_MS = 5 * 60 * 1000L
    }
}
