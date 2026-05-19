package com.example.nexusbank.core.security

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Debug
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
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
    @ApplicationContext private val context: Context,
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

    // ── Root / Emulator / Debugger Detection ──────────────────────

    /**
     * Heuristic root detection. Combines several common signals:
     * - `test-keys` build tag (rooted/dev ROMs).
     * - Presence of `su` binary in common locations.
     * - Presence of well-known root manager apps.
     *
     * For production-grade attestation, also call [PlayIntegrityClient]
     * which is backed by Google's hardware-attested signal.
     */
    fun isDeviceRooted(): Boolean {
        return hasTestKeysBuild() || hasSuBinary() || hasRootManagerApps()
    }

    private fun hasTestKeysBuild(): Boolean =
        Build.TAGS?.contains("test-keys") == true

    private fun hasSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/su", "/system/bin/.ext/su",
            "/system/usr/we-need-root/su", "/system/sd/xbin/su",
            "/data/local/su", "/data/local/bin/su", "/data/local/xbin/su",
            "/su/bin/su", "/cache/su", "/dev/su",
        )
        return paths.any { runCatching { File(it).exists() }.getOrDefault(false) }
    }

    private fun hasRootManagerApps(): Boolean {
        val packages = arrayOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.noshufou.android.su",
            "com.kingoapp.apk",
            "com.zhiqupk.root.global",
            "com.alephzain.framaroot",
        )
        val pm = context.packageManager
        return packages.any {
            runCatching { pm.getPackageInfo(it, 0); true }.getOrDefault(false)
        }
    }

    /**
     * True when a debugger (adb or JDWP) is currently attached, OR the
     * build is debuggable. Use to short-circuit sensitive operations
     * (e.g. transfers, biometric prompts) on release builds.
     */
    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() ||
            Debug.waitingForDebugger() ||
            (context.applicationInfo.flags and
                android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic")
                || Build.PRODUCT.contains("sdk")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }

    fun isDeviceSecure(): Boolean {
        return !isDeviceRooted() && !isDebuggerAttached()
    }

    companion object {
        /** 5-minute inactivity timeout for banking apps */
        const val SESSION_TIMEOUT_MS = 5 * 60 * 1000L
    }
}
