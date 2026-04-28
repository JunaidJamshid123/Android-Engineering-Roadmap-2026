package com.example.nexusbank.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper around EncryptedSharedPreferences for secure storage of
 * sensitive data like auth tokens, MPIN hash, and session info.
 *
 * Uses AES-256 encryption via Android Keystore — keys never leave
 * the secure hardware (TEE/StrongBox).
 */
@Singleton
class EncryptedPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ── Token Management ──────────────────────────────────────────

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    // ── MPIN ──────────────────────────────────────────────────────

    var mpinHash: String?
        get() = prefs.getString(KEY_MPIN_HASH, null)
        set(value) = prefs.edit().putString(KEY_MPIN_HASH, value).apply()

    // ── Biometric ─────────────────────────────────────────────────

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    // ── Session ───────────────────────────────────────────────────

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var deviceId: String?
        get() = prefs.getString(KEY_DEVICE_ID, null)
        set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()

    var lastActiveTimestamp: Long
        get() = prefs.getLong(KEY_LAST_ACTIVE, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_ACTIVE, value).apply()

    // ── Onboarding ────────────────────────────────────────────────

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    // ── Operations ────────────────────────────────────────────────

    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_LAST_ACTIVE)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return accessToken != null && refreshToken != null
    }

    companion object {
        private const val PREFS_FILE_NAME = "nexus_secure_prefs"

        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_MPIN_HASH = "mpin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_LAST_ACTIVE = "last_active_timestamp"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
