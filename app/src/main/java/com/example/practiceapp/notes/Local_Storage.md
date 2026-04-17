# Android Local Storage - Complete Guide

## 7. Local Storage

Android provides multiple options for local data persistence, each suited for different use cases.

### Storage Options Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     ANDROID LOCAL STORAGE OPTIONS                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐          │
│  │ SharedPreferences│  │    DataStore     │  │      Room        │          │
│  │   (Key-Value)    │  │   (Key-Value)    │  │   (SQLite ORM)   │          │
│  │                  │  │                  │  │                  │          │
│  │  • Simple data   │  │  • Async/Flow    │  │  • Complex data  │          │
│  │  • Synchronous   │  │  • Type-safe     │  │  • Relations     │          │
│  │  • XML storage   │  │  • Coroutines    │  │  • Queries       │          │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘          │
│                                                                             │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐          │
│  │  Internal Files  │  │  External Files  │  │    MediaStore    │          │
│  │                  │  │                  │  │                  │          │
│  │  • Private       │  │  • Shareable     │  │  • Media files   │          │
│  │  • No permission │  │  • App-specific  │  │  • System gallery│          │
│  │  • App sandbox   │  │  • or shared     │  │  • Content URI   │          │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Android Storage Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           ANDROID DEVICE                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    INTERNAL STORAGE                                  │   │
│  │                   /data/data/<package>/                              │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │   │
│  │  │shared_prefs/│ │  databases/ │ │   files/    │ │   cache/    │   │   │
│  │  │   *.xml     │ │   *.db      │ │ app files   │ │ temp files  │   │   │
│  │  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    EXTERNAL STORAGE                                  │   │
│  │                   /storage/emulated/0/                               │   │
│  │  ┌───────────────────────────┐ ┌───────────────────────────┐       │   │
│  │  │    App-Specific           │ │      Shared Storage       │       │   │
│  │  │ Android/data/<package>/   │ │  Pictures/, Downloads/    │       │   │
│  │  │  (No permission needed)   │ │  (MediaStore/SAF needed)  │       │   │
│  │  └───────────────────────────┘ └───────────────────────────┘       │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Key Concepts

**Data Persistence** refers to storing data so it survives:
- App restarts
- Device reboots
- System kills (low memory)

**App Sandbox** - Each Android app runs in its own security sandbox:
- Apps cannot access each other's data directly
- Internal storage is completely private
- External storage requires permissions or special APIs

**Storage Locations:**
| Location | Path | Deleted on Uninstall | Permission |
|----------|------|---------------------|------------|
| Internal Files | `/data/data/<pkg>/files/` | Yes | None |
| Internal Cache | `/data/data/<pkg>/cache/` | Yes | None |
| External App Files | `/storage/.../Android/data/<pkg>/` | Yes | None (API 19+) |
| External Shared | `/storage/.../Pictures/` etc. | No | MediaStore/SAF |

### Data Lifecycle Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DATA LIFECYCLE IN ANDROID                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────┐                                                          │
│   │  App Start  │                                                          │
│   └──────┬──────┘                                                          │
│          │                                                                  │
│          ▼                                                                  │
│   ┌─────────────────────────────────────────────────────────────────┐      │
│   │                    Data Loading Phase                            │      │
│   │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │      │
│   │  │ SharedPrefs     │  │  DataStore      │  │    Room         │  │      │
│   │  │ (Sync load)     │  │  (Async Flow)   │  │  (Async Query)  │  │      │
│   │  │                 │  │                 │  │                 │  │      │
│   │  │ ⚠️ Blocks on    │  │ ✅ Non-blocking │  │ ✅ Non-blocking │  │      │
│   │  │  first access   │  │   with defaults │  │   with Flow     │  │      │
│   │  └─────────────────┘  └─────────────────┘  └─────────────────┘  │      │
│   └─────────────────────────────────────────────────────────────────┘      │
│          │                                                                  │
│          ▼                                                                  │
│   ┌─────────────────────────────────────────────────────────────────┐      │
│   │                    Active App Usage                              │      │
│   │                                                                  │      │
│   │  Read/Write Operations  ←──→  Memory Cache  ←──→  Disk Storage  │      │
│   │                                                                  │      │
│   └─────────────────────────────────────────────────────────────────┘      │
│          │                                                                  │
│          ▼                                                                  │
│   ┌─────────────────────────────────────────────────────────────────┐      │
│   │                    App Background/Kill                           │      │
│   │                                                                 │      │
│   │  SharedPrefs: Pending writes may be lost if killed              │      │
│   │  DataStore: Atomic writes, safe on kill                         │      │
│   │  Room: Transaction-safe, ACID compliant                         │      │
│   │                                                                  │      │
│   └─────────────────────────────────────────────────────────────────┘      │
│          │                                                                  │
│          ▼                                                                  │
│   ┌─────────────────────────────────────────────────────────────────┐      │
│   │                    App Uninstall                                 │      │
│   │                                                                  │      │
│   │  Internal Storage: DELETED                                       │      │
│   │  External App-Specific: DELETED                                  │      │
│   │  MediaStore Files: PRESERVED (user's media)                      │      │
│   │  SAF Documents: PRESERVED (user chose location)                  │      │
│   │                                                                  │      │
│   └─────────────────────────────────────────────────────────────────┘      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 7.1 SharedPreferences

### Overview
SharedPreferences provides a simple key-value storage mechanism for primitive data types.

### How SharedPreferences Works Internally

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SharedPreferences Architecture                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌────────────┐         ┌────────────────┐         ┌────────────────┐   │
│    │   Your     │ ──────► │ SharedPrefs    │ ──────► │   XML File     │   │
│    │   Code     │         │   Object       │         │  (Disk)        │   │
│    └────────────┘         └────────────────┘         └────────────────┘   │
│          │                       │                          │             │
│          │                       ▼                          │             │
│          │               ┌────────────────┐                 │             │
│          │               │  In-Memory     │ ◄───────────────┘             │
│          │               │  HashMap       │    (Loaded on first access)   │
│          │               └────────────────┘                               │
│          │                       │                                        │
│          ▼                       ▼                                        │
│    ┌─────────────────────────────────────────────────────────┐           │
│    │                    Operations                            │           │
│    │  • get*() → reads from memory (fast)                    │           │
│    │  • edit().apply() → writes async to disk                │           │
│    │  • edit().commit() → writes sync to disk (blocks)       │           │
│    └─────────────────────────────────────────────────────────┘           │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Theory: SharedPreferences Internals

**Storage Mechanism:**
- Data stored as XML files in `/data/data/<package>/shared_prefs/`
- Entire file loaded into memory on first access
- All reads happen from in-memory HashMap (O(1) lookup)
- Writes serialize entire HashMap back to XML

**Threading Model:**
- `apply()` - Asynchronous, schedules write to disk, returns immediately
- `commit()` - Synchronous, blocks until write completes, returns success/failure
- **Always prefer `apply()`** on main thread to avoid ANRs

**Supported Data Types:**
- `String`, `Int`, `Long`, `Float`, `Boolean`
- `Set<String>` (no List support)
- No support for custom objects (use JSON serialization)

**Limitations:**
- Not type-safe (runtime errors possible)
- No migrations support
- Synchronous read can block on first access
- Not suitable for large data (entire file loaded into memory)

### Key-Value Storage

```kotlin
// Getting SharedPreferences instance
val sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

// Alternative: Activity's default SharedPreferences
val sharedPref = getPreferences(Context.MODE_PRIVATE)

// Writing data
sharedPref.edit().apply {
    putString("username", "john_doe")
    putInt("user_age", 25)
    putBoolean("is_logged_in", true)
    putFloat("rating", 4.5f)
    putLong("timestamp", System.currentTimeMillis())
    putStringSet("tags", setOf("android", "kotlin"))
    apply() // Asynchronous write
    // OR commit() // Synchronous write, returns boolean
}

// Reading data
val username = sharedPref.getString("username", "default_value")
val age = sharedPref.getInt("user_age", 0)
val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)

// Removing data
sharedPref.edit().remove("username").apply()

// Clearing all data
sharedPref.edit().clear().apply()
```

### SharedPreferences Mode Comparison

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SHAREDPREFERENCES MODES                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   MODE_PRIVATE (Default - Use This!)                                        │
│   ─────────────────────────────────                                         │
│   • Only your app can read/write                                            │
│   • Most secure option                                                      │
│   • File permissions: 0660 (rw-rw----)                                     │
│                                                                             │
│   MODE_WORLD_READABLE (Deprecated API 17, Removed API 24)                  │
│   ─────────────────────────────────────────────────────                     │
│   • ❌ DO NOT USE - Security vulnerability                                 │
│   • Other apps could read your data                                         │
│                                                                             │
│   MODE_WORLD_WRITEABLE (Deprecated API 17, Removed API 24)                 │
│   ─────────────────────────────────────────────────────                     │
│   • ❌ DO NOT USE - Security vulnerability                                 │
│   • Other apps could modify your data                                       │
│                                                                             │
│   MODE_MULTI_PROCESS (Deprecated API 23)                                   │
│   ──────────────────────────────────────                                    │
│   • ⚠️ AVOID - Unreliable cross-process synchronization                   │
│   • Use ContentProvider for multi-process data sharing                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### apply() vs commit() Deep Dive

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    apply() vs commit() Comparison                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   apply()                                                                   │
│   ─────────────────────────────────────────────────────                     │
│                                                                             │
│   Main Thread ──┬── edit() ──┬── putString() ──┬── apply() ──► Returns     │
│                 │            │                 │        │     Immediately  │
│                 │            │                 │        │                  │
│                 │            │                 │        ▼                  │
│                 │            │                 │   ┌──────────┐           │
│                 │            │                 │   │Background│           │
│                 │            │                 │   │  Thread  │           │
│                 │            │                 │   │  writes  │           │
│                 │            │                 │   │ to disk  │           │
│                 │            │                 │   └──────────┘           │
│                                                                             │
│   ✅ Non-blocking - Use for UI thread                                      │
│   ⚠️ No return value - Can't verify success                               │
│   ⚠️ Multiple apply() calls are batched                                   │
│                                                                             │
│   commit()                                                                  │
│   ─────────────────────────────────────────────────────                     │
│                                                                             │
│   Main Thread ──┬── edit() ──┬── putString() ──┬── commit() ──┬── BLOCKED │
│                 │            │                 │              │            │
│                 │            │                 │    Writes    │            │
│                 │            │                 │   to disk    │            │
│                 │            │                 │    (sync)    │            │
│                 │            │                 │              │            │
│                 │            │                 │              ▼            │
│                 │            │                 │         true/false        │
│                                                                             │
│   ⚠️ BLOCKING - Avoid on UI thread (causes ANR)                          │
│   ✅ Returns boolean - Know if write succeeded                             │
│   ✅ Use in background thread when result matters                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Complete SharedPreferences Helper Class

```kotlin
/**
 * Production-ready SharedPreferences wrapper with:
 * - Type-safe property access
 * - Default value handling
 * - Null safety
 * - Change listeners
 */
class AppPreferences(context: Context) {
    
    private val prefs = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )
    
    // ═══════════════════════════════════════════════════════════════════════
    // PROPERTY DELEGATES - Type-safe preference access
    // ═══════════════════════════════════════════════════════════════════════
    
    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
    
    var username: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()
    
    var userAge: Int
        get() = prefs.getInt(KEY_USER_AGE, 0)
        set(value) = prefs.edit().putInt(KEY_USER_AGE, value).apply()
    
    var notificationVolume: Float
        get() = prefs.getFloat(KEY_NOTIFICATION_VOLUME, 0.5f)
        set(value) = prefs.edit().putFloat(KEY_NOTIFICATION_VOLUME, value).apply()
    
    var lastSyncTime: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()
    
    var selectedTags: Set<String>
        get() = prefs.getStringSet(KEY_SELECTED_TAGS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_SELECTED_TAGS, value).apply()
    
    // ═══════════════════════════════════════════════════════════════════════
    // THEME HANDLING - Enum stored as String
    // ═══════════════════════════════════════════════════════════════════════
    
    enum class Theme { LIGHT, DARK, SYSTEM }
    
    var theme: Theme
        get() {
            val value = prefs.getString(KEY_THEME, Theme.SYSTEM.name)
            return try {
                Theme.valueOf(value ?: Theme.SYSTEM.name)
            } catch (e: IllegalArgumentException) {
                Theme.SYSTEM
            }
        }
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()
    
    // ═══════════════════════════════════════════════════════════════════════
    // JSON SERIALIZATION - Complex objects
    // ═══════════════════════════════════════════════════════════════════════
    
    private val gson = Gson()
    
    var userProfile: UserProfile?
        get() {
            val json = prefs.getString(KEY_USER_PROFILE, null) ?: return null
            return try {
                gson.fromJson(json, UserProfile::class.java)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            val json = value?.let { gson.toJson(it) }
            prefs.edit().putString(KEY_USER_PROFILE, json).apply()
        }
    
    // ═══════════════════════════════════════════════════════════════════════
    // BATCH OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════
    
    fun saveLoginData(username: String, token: String, expiresAt: Long) {
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_AUTH_TOKEN, token)
            putLong(KEY_TOKEN_EXPIRES, expiresAt)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply() // Single write operation
        }
    }
    
    fun clearLoginData() {
        prefs.edit().apply {
            remove(KEY_USERNAME)
            remove(KEY_AUTH_TOKEN)
            remove(KEY_TOKEN_EXPIRES)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // CHANGE LISTENERS
    // ═══════════════════════════════════════════════════════════════════════
    
    private val listeners = mutableListOf<OnPreferenceChangeListener>()
    
    interface OnPreferenceChangeListener {
        fun onPreferenceChanged(key: String, value: Any?)
    }
    
    private val internalListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        val value = when (key) {
            KEY_THEME -> theme
            KEY_USERNAME -> username
            KEY_IS_LOGGED_IN -> prefs.getBoolean(key, false)
            else -> prefs.all[key]
        }
        listeners.forEach { it.onPreferenceChanged(key ?: "", value) }
    }
    
    init {
        prefs.registerOnSharedPreferenceChangeListener(internalListener)
    }
    
    fun addListener(listener: OnPreferenceChangeListener) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: OnPreferenceChangeListener) {
        listeners.remove(listener)
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // KEYS
    // ═══════════════════════════════════════════════════════════════════════
    
    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_AGE = "user_age"
        private const val KEY_NOTIFICATION_VOLUME = "notification_volume"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_SELECTED_TAGS = "selected_tags"
        private const val KEY_THEME = "theme"
        private const val KEY_USER_PROFILE = "user_profile"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_TOKEN_EXPIRES = "token_expires"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?
)
```

### Listening for Changes

```kotlin
val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
    when (key) {
        "theme" -> updateTheme(prefs.getString(key, "light"))
        "notifications" -> updateNotificationSettings(prefs.getBoolean(key, true))
    }
}

// Register listener
sharedPref.registerOnSharedPreferenceChangeListener(listener)

// Don't forget to unregister to prevent memory leaks
sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
```

### Security Concerns

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              SharedPreferences Security Vulnerabilities                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   REGULAR SharedPreferences                ENCRYPTED SharedPreferences      │
│   ─────────────────────────                ───────────────────────────      │
│                                                                             │
│   ┌─────────────────────┐                 ┌─────────────────────┐          │
│   │  my_prefs.xml       │                 │  secure_prefs.xml   │          │
│   │                     │                 │                     │          │
│   │  <string name=      │                 │  <string name=      │          │
│   │    "password">      │                 │    "ATx9f2k...">   │          │
│   │    secret123        │   ──────►       │    Ek3mF9x2...     │          │
│   │  </string>          │   ENCRYPT       │  </string>          │          │
│   │                     │                 │                     │          │
│   │  ⚠️ PLAIN TEXT!     │                 │  ✅ AES-256 GCM     │          │
│   └─────────────────────┘                 └─────────────────────┘          │
│                                                                             │
│   ATTACK VECTORS:                         PROTECTION:                       │
│   • Rooted device access                  • Keys stored in Android Keystore │
│   • ADB backup extraction                 • Keys encrypted by hardware      │
│   • Malware with root                     • Per-file encryption             │
│   • Physical device access                • Key rotation support            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Problems with regular SharedPreferences:**
1. **Stored in plain XML** - Data is readable on rooted devices
2. **World-readable modes deprecated** - `MODE_WORLD_READABLE` removed in API 24
3. **No encryption** - Sensitive data like tokens/passwords are vulnerable
4. **Backup exposure** - Data can be extracted from backups

```xml
<!-- SharedPreferences stored at: /data/data/<package>/shared_prefs/my_prefs.xml -->
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="auth_token">eyJhbGciOiJIUzI1NiIs...</string>
    <string name="password">mysecretpassword</string> <!-- BAD! -->
</map>
```

### EncryptedSharedPreferences

**Setup:**
```kotlin
// Add dependency
// implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

**Implementation:**
```kotlin
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferences(context: Context) {
    
    // Create or retrieve the master key
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    // Create EncryptedSharedPreferences
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // Use same API as regular SharedPreferences
    fun saveToken(token: String) {
        encryptedPrefs.edit().putString("auth_token", token).apply()
    }
    
    fun getToken(): String? {
        return encryptedPrefs.getString("auth_token", null)
    }
    
    fun saveCredentials(username: String, password: String) {
        encryptedPrefs.edit().apply {
            putString("username", username)
            putString("password", password)
            apply()
        }
    }
    
    fun clearAllSecureData() {
        encryptedPrefs.edit().clear().apply()
    }
}
```

**Best Practices:**
```kotlin
// Wrapper class for secure preference management
class PreferenceManager(private val context: Context) {
    
    // Regular prefs for non-sensitive data
    private val regularPrefs by lazy {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
    
    // Encrypted prefs for sensitive data
    private val securePrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    // Non-sensitive settings
    var isDarkMode: Boolean
        get() = regularPrefs.getBoolean("dark_mode", false)
        set(value) = regularPrefs.edit().putBoolean("dark_mode", value).apply()
    
    // Sensitive data - use encrypted
    var authToken: String?
        get() = securePrefs.getString("auth_token", null)
        set(value) {
            if (value != null) {
                securePrefs.edit().putString("auth_token", value).apply()
            } else {
                securePrefs.edit().remove("auth_token").apply()
            }
        }
}
```

---

## 7.2 DataStore (Modern Replacement)

DataStore is the modern replacement for SharedPreferences with:
- **Asynchronous API** with Kotlin coroutines and Flow
- **Data consistency** with transactional operations
- **Error handling** built-in
- **Type safety** (Proto DataStore)

### DataStore Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        DataStore Architecture                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌──────────────────────────────────────────────────────────────────┐   │
│    │                         UI Layer                                  │   │
│    │    ┌─────────────┐              ┌─────────────┐                  │   │
│    │    │  Compose    │              │  ViewModel  │                  │   │
│    │    │    UI       │◄─────────────│             │                  │   │
│    │    └─────────────┘   StateFlow  └─────────────┘                  │   │
│    └──────────────────────────────────────│────────────────────────────┘   │
│                                           │                                 │
│                                           │ collect()                       │
│                                           ▼                                 │
│    ┌──────────────────────────────────────────────────────────────────┐   │
│    │                      Repository Layer                             │   │
│    │                                                                   │   │
│    │    dataStore.data ──► Flow<Preferences> ──► map() ──► Flow<T>   │   │
│    │                                                                   │   │
│    │    dataStore.edit { prefs -> prefs[KEY] = value }                │   │
│    │                           │                                       │   │
│    └───────────────────────────│───────────────────────────────────────┘   │
│                                │                                           │
│                                ▼                                           │
│    ┌──────────────────────────────────────────────────────────────────┐   │
│    │                      DataStore Core                               │   │
│    │  ┌────────────────┐    ┌────────────────┐    ┌────────────────┐ │   │
│    │  │  Preferences   │    │   Proto        │    │  Serializer    │ │   │
│    │  │  DataStore     │    │   DataStore    │    │                │ │   │
│    │  │  (Key-Value)   │    │  (Type-Safe)   │    │  (Custom)      │ │   │
│    │  └────────────────┘    └────────────────┘    └────────────────┘ │   │
│    └──────────────────────────────────────────────────────────────────┘   │
│                                │                                           │
│                                ▼                                           │
│    ┌──────────────────────────────────────────────────────────────────┐   │
│    │                         File System                               │   │
│    │            /data/data/<package>/files/datastore/                  │   │
│    │                    settings.preferences_pb                        │   │
│    └──────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Why DataStore over SharedPreferences?

```
┌───────────────────────────────────────────────────────────────────────────┐
│              SharedPreferences vs DataStore Comparison                    │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  Feature              SharedPreferences        DataStore                  │
│  ───────────────────────────────────────────────────────────────────────  │
│  API                  Synchronous              Asynchronous (Flow)        │
│  Thread Safety        Manual handling          Built-in                   │
│  Error Handling       Exceptions thrown        Caught via Flow            │
│  Data Corruption      Possible                 Protected                  │
│  Type Safety          Runtime                  Compile-time (Proto)       │
│  Transactions         Not atomic               Atomic transactions        │
│  Migration            Manual                   Built-in support           │
│  Coroutines           Not supported            Native support             │
│                                                                           │
│  Recommendation: ✅ Use DataStore for all new projects                   │
│                                                                           │
└───────────────────────────────────────────────────────────────────────────┘
```

### Theory: DataStore Internals

**Preferences DataStore:**
- Stores data in Protocol Buffers format (not XML)
- Single-writer guarantee (no race conditions)
- Reads never block (returns cached value or default)
- Writes are atomic (all-or-nothing)

**Flow-based Reactive Updates:**
```
Write → File Update → Flow Emission → UI Update
                          │
                          └─► All collectors receive new value
```

**Error Handling:**
- `IOException` on read failures → emit default value
- `CorruptionException` on data corruption → can clear and restart
- All errors flow through the `catch` operator

### DataStore Migration from SharedPreferences

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    MIGRATING SHAREDPREFS TO DATASTORE                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   MIGRATION FLOW                                                            │
│   ──────────────                                                            │
│                                                                             │
│   ┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐ │
│   │  SharedPrefs     │      │   DataStore      │      │   DataStore      │ │
│   │  (old_prefs.xml) │ ──►  │   Migration      │ ──►  │  (settings.pb)   │ │
│   │                  │      │                  │      │                  │ │
│   │  theme: "dark"   │      │  Read old prefs  │      │  theme: "dark"   │ │
│   │  fontSize: 14    │      │  Write to new    │      │  fontSize: 14    │ │
│   │                  │      │  Delete old file │      │                  │ │
│   └──────────────────┘      └──────────────────┘      └──────────────────┘ │
│                                                                             │
│   ✅ Migration happens automatically on first DataStore access             │
│   ✅ Old SharedPrefs file is deleted after successful migration            │
│   ✅ Migration is atomic - all or nothing                                  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// Migration Code Example
val Context.dataStore by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "old_prefs", // Old SharedPrefs file name
                keysToMigrate = setOf("theme", "fontSize", "username") // Optional: specific keys
            )
        )
    }
)

// Custom migration with transformation
val customMigration = SharedPreferencesMigration(
    context = context,
    sharedPreferencesName = "old_prefs"
) { sharedPrefs, currentData ->
    // Transform data during migration
    val mutablePrefs = currentData.toMutablePreferences()
    
    // Migrate with transformation
    if (sharedPrefs.contains("theme")) {
        val oldTheme = sharedPrefs.getString("theme", "light")
        // Transform old value to new format
        mutablePrefs[THEME_KEY] = oldTheme?.uppercase() ?: "LIGHT"
    }
    
    // Handle renamed keys
    if (sharedPrefs.contains("user_name")) {
        mutablePrefs[USERNAME_KEY] = sharedPrefs.getString("user_name", "") ?: ""
    }
    
    mutablePrefs.toPreferences()
}
```

### DataStore Error Handling Patterns

```kotlin
/**
 * Production-ready DataStore with comprehensive error handling
 */
class RobustUserPreferencesRepository(private val context: Context) {
    
    companion object {
        val USERNAME = stringPreferencesKey("username")
        val THEME = stringPreferencesKey("theme")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // READING WITH ERROR HANDLING
    // ═══════════════════════════════════════════════════════════════════════
    
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            when (exception) {
                is IOException -> {
                    // File read error - emit defaults
                    Log.e("DataStore", "Error reading preferences", exception)
                    emit(emptyPreferences())
                }
                is CorruptionException -> {
                    // Data corrupted - try to recover
                    Log.e("DataStore", "Data corrupted, clearing", exception)
                    // Could also emit defaults and schedule a clear
                    emit(emptyPreferences())
                }
                else -> throw exception // Re-throw unexpected errors
            }
        }
        .map { prefs -> mapToUserPreferences(prefs) }
    
    private fun mapToUserPreferences(prefs: Preferences): UserPreferences {
        return UserPreferences(
            username = prefs[USERNAME] ?: "",
            theme = prefs[THEME] ?: "system",
            onboardingComplete = prefs[ONBOARDING_COMPLETE] ?: false
        )
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // WRITING WITH ERROR HANDLING
    // ═══════════════════════════════════════════════════════════════════════
    
    sealed class WriteResult {
        object Success : WriteResult()
        data class Error(val exception: Exception) : WriteResult()
    }
    
    suspend fun updateUsername(username: String): WriteResult {
        return try {
            context.dataStore.edit { prefs ->
                prefs[USERNAME] = username
            }
            WriteResult.Success
        } catch (e: IOException) {
            Log.e("DataStore", "Failed to update username", e)
            WriteResult.Error(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // TRANSACTIONAL UPDATES
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Update multiple values atomically
     * If any part fails, nothing is written
     */
    suspend fun completeOnboarding(username: String, theme: String): WriteResult {
        return try {
            context.dataStore.edit { prefs ->
                prefs[USERNAME] = username
                prefs[THEME] = theme
                prefs[ONBOARDING_COMPLETE] = true
            }
            WriteResult.Success
        } catch (e: IOException) {
            WriteResult.Error(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // CONDITIONAL UPDATES
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Update only if current value matches expected
     * Useful for optimistic concurrency
     */
    suspend fun updateUsernameIfEmpty(newUsername: String): Boolean {
        var updated = false
        context.dataStore.edit { prefs ->
            if (prefs[USERNAME].isNullOrEmpty()) {
                prefs[USERNAME] = newUsername
                updated = true
            }
        }
        return updated
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // CLEAR DATA
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun clearAll(): WriteResult {
        return try {
            context.dataStore.edit { it.clear() }
            WriteResult.Success
        } catch (e: IOException) {
            WriteResult.Error(e)
        }
    }
    
    /**
     * Clear and recreate - useful when data is corrupted
     */
    suspend fun resetToDefaults() {
        try {
            context.dataStore.edit { prefs ->
                prefs.clear()
                prefs[THEME] = "system"
                prefs[ONBOARDING_COMPLETE] = false
            }
        } catch (e: IOException) {
            Log.e("DataStore", "Failed to reset", e)
        }
    }
}

data class UserPreferences(
    val username: String,
    val theme: String,
    val onboardingComplete: Boolean
)
```

### Setup

```kotlin
// build.gradle.kts
dependencies {
    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Proto DataStore
    implementation("androidx.datastore:datastore:1.0.0")
    
    // Optional: for Proto DataStore serialization
    implementation("com.google.protobuf:protobuf-javalite:3.21.7")
}
```

### Preferences DataStore

```kotlin
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Extension property on Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {
    
    // Define keys
    companion object {
        val USERNAME = stringPreferencesKey("username")
        val USER_AGE = intPreferencesKey("user_age")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FAVORITE_IDS = stringSetPreferencesKey("favorite_ids")
    }
    
    // Read data as Flow
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                username = preferences[USERNAME] ?: "",
                age = preferences[USER_AGE] ?: 0,
                isLoggedIn = preferences[IS_LOGGED_IN] ?: false,
                notificationEnabled = preferences[NOTIFICATION_ENABLED] ?: true,
                themeMode = preferences[THEME_MODE] ?: "system"
            )
        }
    
    // Read single value
    val themeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: "system"
        }
    
    // Write data
    suspend fun updateUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME] = username
        }
    }
    
    // Write multiple values atomically
    suspend fun saveUserSettings(username: String, age: Int, theme: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME] = username
            preferences[USER_AGE] = age
            preferences[THEME_MODE] = theme
        }
    }
    
    // Toggle boolean
    suspend fun toggleNotifications() {
        context.dataStore.edit { preferences ->
            val current = preferences[NOTIFICATION_ENABLED] ?: true
            preferences[NOTIFICATION_ENABLED] = !current
        }
    }
    
    // Clear all data
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

// Data class to hold preferences
data class UserPreferences(
    val username: String,
    val age: Int,
    val isLoggedIn: Boolean,
    val notificationEnabled: Boolean,
    val themeMode: String
)
```

**Using in ViewModel:**
```kotlin
class SettingsViewModel(
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {
    
    val userPreferences: StateFlow<UserPreferences> = prefsRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences("", 0, false, true, "system")
        )
    
    fun updateTheme(theme: String) {
        viewModelScope.launch {
            prefsRepository.saveUserSettings(
                username = userPreferences.value.username,
                age = userPreferences.value.age,
                theme = theme
            )
        }
    }
}
```

**Using in Compose:**
```kotlin
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val preferences by viewModel.userPreferences.collectAsState()
    
    Column {
        Text("Username: ${preferences.username}")
        
        Switch(
            checked = preferences.notificationEnabled,
            onCheckedChange = { viewModel.toggleNotifications() }
        )
        
        // Theme selector
        RadioGroup(
            selected = preferences.themeMode,
            options = listOf("light", "dark", "system"),
            onSelect = { viewModel.updateTheme(it) }
        )
    }
}
```

### Proto DataStore for Typed Storage

Proto DataStore provides **type safety** and **schema evolution** using Protocol Buffers.

**Step 1: Define Proto Schema**
```protobuf
// app/src/main/proto/user_settings.proto
syntax = "proto3";

option java_package = "com.example.practiceapp";
option java_multiple_files = true;

message UserSettings {
    string username = 1;
    int32 age = 2;
    bool is_logged_in = 3;
    Theme theme = 4;
    repeated string favorite_ids = 5;
    
    enum Theme {
        THEME_UNSPECIFIED = 0;
        LIGHT = 1;
        DARK = 2;
        SYSTEM = 3;
    }
}
```

**Step 2: Configure Protobuf Plugin**
```kotlin
// build.gradle.kts (app)
plugins {
    id("com.google.protobuf") version "0.9.4"
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.7"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
```

**Step 3: Create Serializer**
```kotlin
import androidx.datastore.core.Serializer
import com.example.practiceapp.UserSettings
import java.io.InputStream
import java.io.OutputStream

object UserSettingsSerializer : Serializer<UserSettings> {
    override val defaultValue: UserSettings = UserSettings.getDefaultInstance()
    
    override suspend fun readFrom(input: InputStream): UserSettings {
        try {
            return UserSettings.parseFrom(input)
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }
    
    override suspend fun writeTo(t: UserSettings, output: OutputStream) {
        t.writeTo(output)
    }
}
```

**Step 4: Create DataStore**
```kotlin
val Context.userSettingsDataStore: DataStore<UserSettings> by dataStore(
    fileName = "user_settings.pb",
    serializer = UserSettingsSerializer
)

class UserSettingsRepository(private val context: Context) {
    
    val userSettingsFlow: Flow<UserSettings> = context.userSettingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }
    
    suspend fun updateUsername(username: String) {
        context.userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setUsername(username)
                .build()
        }
    }
    
    suspend fun updateTheme(theme: UserSettings.Theme) {
        context.userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setTheme(theme)
                .build()
        }
    }
    
    suspend fun addFavorite(id: String) {
        context.userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .addFavoriteIds(id)
                .build()
        }
    }
    
    suspend fun logout() {
        context.userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setIsLoggedIn(false)
                .clearUsername()
                .build()
        }
    }
}
```

### Preferences DataStore vs Proto DataStore

| Feature | Preferences DataStore | Proto DataStore |
|---------|----------------------|-----------------|
| Schema | Key-value (like SharedPrefs) | Typed schema (Protocol Buffers) |
| Type Safety | Runtime keys | Compile-time type safety |
| Complex Types | Limited (primitives, sets) | Full support (nested, enums, lists) |
| Migration | Simple | Built-in schema evolution |
| Setup | Easy | Requires protobuf setup |
| Use Case | Simple settings | Complex, structured data |

### Proto DataStore Type System

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PROTO DATASTORE TYPE SYSTEM                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   PREFERENCES DATASTORE              PROTO DATASTORE                        │
│   ─────────────────────              ──────────────                         │
│                                                                             │
│   ┌─────────────────────────┐        ┌─────────────────────────┐           │
│   │ // Runtime type checking │        │ // Compile-time type    │           │
│   │                         │        │                         │           │
│   │ val KEY = stringPrefKey │        │ message UserSettings {  │           │
│   │ prefs[KEY] = "value"    │        │   string username = 1;  │           │
│   │                         │        │   int32 age = 2;        │           │
│   │ // Can accidentally:    │        │   Theme theme = 3;      │           │
│   │ prefs[KEY] = 123 // ❌  │        │ }                       │           │
│   │ // Wrong type at runtime│        │                         │           │
│   └─────────────────────────┘        │ // IDE autocomplete:    │           │
│                                       │ settings.username ✅    │           │
│                                       │ settings.unknownField ❌│           │
│                                       └─────────────────────────┘           │
│                                                                             │
│   PROTO ADVANTAGES:                                                         │
│   ✅ Schema evolution (add fields without breaking)                        │
│   ✅ Default values defined in schema                                      │
│   ✅ Efficient binary serialization                                        │
│   ✅ Nested messages and enums                                             │
│   ✅ Repeated fields (lists)                                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 7.3 Room Database (Complete Guide)

Room is Android's recommended SQLite abstraction library providing:
- Compile-time SQL verification
- Convenient annotations to reduce boilerplate
- Integration with Kotlin coroutines and Flow

### Room Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ROOM ARCHITECTURE                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌─────────────────────────────────────────────────────────────────┐    │
│    │                        APPLICATION                               │    │
│    │   ┌─────────────┐                        ┌─────────────┐        │    │
│    │   │  ViewModel  │ ◄───── Flow<List<T>> ──│ Repository  │        │    │
│    │   └─────────────┘                        └─────────────┘        │    │
│    └───────────────────────────────────────────────│─────────────────┘    │
│                                                    │                       │
│    ┌───────────────────────────────────────────────▼─────────────────┐    │
│    │                           ROOM                                   │    │
│    │                                                                  │    │
│    │    ┌──────────────────────────────────────────────────────┐    │    │
│    │    │                    @Database                          │    │    │
│    │    │   AppDatabase (RoomDatabase)                         │    │    │
│    │    │   • Singleton instance                               │    │    │
│    │    │   • Defines entities & version                       │    │    │
│    │    │   • Provides DAO access                              │    │    │
│    │    └──────────────────────────────────────────────────────┘    │    │
│    │                           │                                     │    │
│    │              ┌────────────┼────────────┐                       │    │
│    │              ▼            ▼            ▼                       │    │
│    │    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐         │    │
│    │    │  @Dao        │ │  @Dao        │ │  @Dao        │         │    │
│    │    │  UserDao     │ │  PostDao     │ │  TagDao      │         │    │
│    │    │              │ │              │ │              │         │    │
│    │    │ @Query       │ │ @Insert      │ │ @Delete      │         │    │
│    │    │ @Insert      │ │ @Update      │ │ @Query       │         │    │
│    │    └──────────────┘ └──────────────┘ └──────────────┘         │    │
│    │                           │                                     │    │
│    │              ┌────────────┼────────────┐                       │    │
│    │              ▼            ▼            ▼                       │    │
│    │    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐         │    │
│    │    │  @Entity     │ │  @Entity     │ │  @Entity     │         │    │
│    │    │  User        │ │  Post        │ │  Tag         │         │    │
│    │    │              │ │              │ │              │         │    │
│    │    │ id: Long     │ │ id: Long     │ │ id: Long     │         │    │
│    │    │ name: String │ │ title: String│ │ name: String │         │    │
│    │    └──────────────┘ └──────────────┘ └──────────────┘         │    │
│    │                                                                  │    │
│    └──────────────────────────────────────────────────────────────────┘    │
│                                    │                                        │
│    ┌───────────────────────────────▼────────────────────────────────┐     │
│    │                         SQLite                                   │     │
│    │              /data/data/<package>/databases/                     │     │
│    │                      app_database.db                             │     │
│    └──────────────────────────────────────────────────────────────────┘     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Room Components Explained

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       ROOM COMPONENTS                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │ @Entity - Represents a table in the database                        │  │
│  │                                                                      │  │
│  │   @Entity(tableName = "users")                                      │  │
│  │   data class User(                                                  │  │
│  │       @PrimaryKey val id: Long,   ◄── Primary key column           │  │
│  │       @ColumnInfo val name: String ◄── Column mapping              │  │
│  │   )                                                                 │  │
│  │                                     ▼                               │  │
│  │   Generates: CREATE TABLE users (id INTEGER PRIMARY KEY, ...)      │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │ @Dao - Data Access Object (interface for database operations)       │  │
│  │                                                                      │  │
│  │   @Dao                                                              │  │
│  │   interface UserDao {                                               │  │
│  │       @Query("SELECT * FROM users")  ◄── SQL at compile time       │  │
│  │       fun getAll(): Flow<List<User>>                                │  │
│  │                                                                      │  │
│  │       @Insert ◄── Auto-generates INSERT statement                  │  │
│  │       suspend fun insert(user: User)                                │  │
│  │   }                                                                 │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │ @Database - Main access point, contains DAOs                        │  │
│  │                                                                      │  │
│  │   @Database(entities = [User::class], version = 1)                  │  │
│  │   abstract class AppDatabase : RoomDatabase() {                     │  │
│  │       abstract fun userDao(): UserDao  ◄── DAO accessor             │  │
│  │   }                                                                 │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Room Compile-Time Verification

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ROOM COMPILE-TIME SAFETY                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   YOUR CODE                          COMPILE TIME                            │
│   ─────────                          ────────────                            │
│                                                                             │
│   @Query("SELECT * FROM userss")     ❌ Error: No table 'userss'           │
│                     ▲                                                       │
│                     │ Typo detected!                                        │
│                                                                             │
│   @Query("SELECT nam FROM users")    ❌ Error: No column 'nam'             │
│                  ▲                                                          │
│                  │ Column name verified                                     │
│                                                                             │
│   @Query("SELECT * FROM users")      ✅ Valid SQL, matches Entity          │
│   fun getAll(): List<Post>           ❌ Error: Return type mismatch        │
│                       ▲                                                     │
│                       │ Type checking!                                      │
│                                                                             │
│   Benefits:                                                                 │
│   • Catch SQL errors at compile time, not runtime                          │
│   • IDE autocomplete for table/column names                                │
│   • Refactoring safety - rename column updates all queries                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Room Query Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ROOM QUERY EXECUTION FLOW                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   SUSPEND FUNCTION (One-shot)                                               │
│   ───────────────────────────                                               │
│                                                                             │
│   @Query("SELECT * FROM users WHERE id = :userId")                         │
│   suspend fun getUserById(userId: Long): User?                              │
│                                                                             │
│   ┌─────────┐  suspends   ┌─────────┐  executes   ┌─────────┐  returns    │
│   │Coroutine│ ──────────► │ Room    │ ──────────► │ SQLite  │ ──────────► │
│   │         │             │Dispatcher│            │ Query   │    User?    │
│   └─────────┘             └─────────┘             └─────────┘             │
│                                │                                           │
│                         Dispatchers.IO                                      │
│                         (background thread)                                 │
│                                                                             │
│   FLOW FUNCTION (Reactive/Observable)                                       │
│   ───────────────────────────────────                                       │
│                                                                             │
│   @Query("SELECT * FROM users")                                            │
│   fun getAllUsers(): Flow<List<User>>                                       │
│                                                                             │
│   ┌─────────┐            ┌─────────┐            ┌─────────┐               │
│   │   UI    │◄─ emits ───│ Flow    │◄─ triggers─│ SQLite  │               │
│   │Collector│            │         │            │ Change  │               │
│   └─────────┘            └─────────┘            │Listener │               │
│        ▲                      ▲                 └─────────┘               │
│        │                      │                      │                     │
│        │                      │                      │                     │
│        └──────────────────────┴──────────────────────┘                     │
│                    Auto-updates on any table change!                        │
│                                                                             │
│   DATA CHANGE PROPAGATION:                                                  │
│   ┌─────────────────────────────────────────────────────────────┐          │
│   │  INSERT/UPDATE/DELETE  ──►  Room detects  ──►  Flow emits   │          │
│   │          users                 change             new data  │          │
│   └─────────────────────────────────────────────────────────────┘          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Room Transaction Guarantees

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ROOM TRANSACTION (ACID)                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   @Transaction                                                              │
│   suspend fun transferMoney(from: Account, to: Account, amount: Double) {   │
│       val fromBalance = getBalance(from.id)                                 │
│       if (fromBalance < amount) throw InsufficientFundsException()         │
│       updateBalance(from.id, fromBalance - amount)  // Step 1               │
│       updateBalance(to.id, to.balance + amount)     // Step 2               │
│   }                                                                         │
│                                                                             │
│   WITHOUT @Transaction:                 WITH @Transaction:                  │
│   ─────────────────────                 ──────────────────                  │
│                                                                             │
│   Step 1: ✅ Success                   ┌─────────────────────┐            │
│   Step 2: ❌ Crash/Error              │ BEGIN TRANSACTION    │            │
│   Result: Inconsistent!                │                     │            │
│           Money disappeared!           │ Step 1: Executed    │            │
│                                        │ Step 2: Executed    │            │
│                                        │                     │            │
│   ┌───────────────────┐                │ COMMIT (all success)│            │
│   │ Account A: -$100  │                │   OR                │            │
│   │ Account B: +$0    │                │ ROLLBACK (any fail) │            │
│   │ Total: -$100 ❌   │                └─────────────────────┘            │
│   └───────────────────┘                                                    │
│                                        ┌───────────────────┐               │
│                                        │ Either both or     │               │
│                                        │ neither happen! ✅ │               │
│                                        └───────────────────┘               │
│                                                                             │
│   ACID Properties:                                                          │
│   A - Atomicity:   All operations succeed or all fail                      │
│   C - Consistency: Database always valid state                             │
│   I - Isolation:   Concurrent transactions don't interfere                 │
│   D - Durability:  Committed data survives crashes                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Setup

```kotlin
// build.gradle.kts
plugins {
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

dependencies {
    val roomVersion = "2.6.1"
    
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Kotlin Extensions and Coroutines
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // Optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$roomVersion")
    
    // Optional - Testing
    testImplementation("androidx.room:room-testing:$roomVersion")
}
```

### Entity (Table)

```kotlin
import androidx.room.*

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"])
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "profile_image")
    val profileImage: String? = null,
    
    @Ignore // Won't be stored in database
    val isOnline: Boolean = false
)

// Entity with Embedded object
data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String
)

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    
    @Embedded(prefix = "address_")
    val address: Address
)

// Entity with foreign key relationship
@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id")]
)
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: Long,
    
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

### Type Converters

```kotlin
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    
    private val gson = Gson()
    
    // Date conversion
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    // List<String> conversion
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
    
    // Enum conversion
    @TypeConverter
    fun fromStatus(status: Status): String {
        return status.name
    }
    
    @TypeConverter
    fun toStatus(value: String): Status {
        return Status.valueOf(value)
    }
}

enum class Status {
    PENDING,
    ACTIVE,
    COMPLETED,
    CANCELLED
}
```

### DAO (Data Access Object)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    DAO OPERATIONS CHEAT SHEET                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ANNOTATION        USAGE                    RETURN TYPE                    │
│   ──────────────────────────────────────────────────────────────────────    │
│                                                                             │
│   @Insert           Insert new rows         Long (row ID) or List<Long>    │
│                     OnConflict strategies:                                  │
│                     - ABORT (default)                                       │
│                     - REPLACE (update if exists)                            │
│                     - IGNORE (skip if exists)                               │
│                                                                             │
│   @Update           Update existing rows    Int (rows updated)             │
│                                                                             │
│   @Delete           Delete rows             Int (rows deleted)             │
│                                                                             │
│   @Query            Custom SQL queries      Any type or Flow<T>            │
│                     SELECT, UPDATE, DELETE                                  │
│                     Use :paramName for args                                 │
│                                                                             │
│   @Transaction      Multiple operations     Wrap multiple DAO calls        │
│                     as atomic unit                                          │
│                                                                             │
│   @RawQuery         Dynamic SQL             Use when query unknown         │
│                     at compile time                                         │
│                                                                             │
│   @Upsert (2.5+)    Insert or update        Long or List<Long>            │
│                     in one operation                                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(users: List<User>): List<Long>
    
    // Update operations
    @Update
    suspend fun update(user: User): Int // Returns number of rows updated
    
    @Query("UPDATE users SET username = :newUsername WHERE id = :userId")
    suspend fun updateUsername(userId: Long, newUsername: String)
    
    // Delete operations
    @Delete
    suspend fun delete(user: User)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: Long)
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
    
    // Query operations - Flow (reactive)
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Long): Flow<User?>
    
    // Query operations - Suspend (one-shot)
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdOnce(userId: Long): User?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    // Search with LIKE
    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<User>>
    
    // Pagination
    @Query("SELECT * FROM users ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getUsersPaginated(limit: Int, offset: Int): List<User>
    
    // Count
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Query("SELECT COUNT(*) FROM users")
    fun observeUserCount(): Flow<Int>
    
    // Exists check
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun emailExists(email: String): Boolean
    
    // Transaction
    @Transaction
    suspend fun replaceAllUsers(users: List<User>) {
        deleteAll()
        insertAll(users)
    }
}
```

### Room Advanced Queries

```kotlin
@Dao
interface AdvancedUserDao {
    
    // ═══════════════════════════════════════════════════════════════════════
    // DYNAMIC QUERIES with @RawQuery
    // ═══════════════════════════════════════════════════════════════════════
    
    @RawQuery(observedEntities = [User::class])
    fun getUsersRaw(query: SupportSQLiteQuery): Flow<List<User>>
    
    // Usage:
    // val query = SimpleSQLiteQuery(
    //     "SELECT * FROM users WHERE age > ? ORDER BY ? DESC",
    //     arrayOf(18, "name")
    // )
    // dao.getUsersRaw(query)
    
    // ═══════════════════════════════════════════════════════════════════════
    // AGGREGATION QUERIES
    // ═══════════════════════════════════════════════════════════════════════
    
    @Query("SELECT AVG(age) FROM users")
    suspend fun getAverageAge(): Double
    
    @Query("SELECT MIN(age), MAX(age), AVG(age), COUNT(*) FROM users")
    suspend fun getUserStats(): UserStats
    
    @Query("""
        SELECT 
            CASE 
                WHEN age < 18 THEN 'minor'
                WHEN age < 30 THEN 'young'
                WHEN age < 50 THEN 'middle'
                ELSE 'senior'
            END as ageGroup,
            COUNT(*) as count
        FROM users
        GROUP BY ageGroup
    """)
    fun getAgeDistribution(): Flow<List<AgeGroupCount>>
    
    // ═══════════════════════════════════════════════════════════════════════
    // COMPLEX JOINS
    // ═══════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT u.*, COUNT(p.id) as postCount
        FROM users u
        LEFT JOIN posts p ON u.id = p.user_id
        GROUP BY u.id
        ORDER BY postCount DESC
    """)
    fun getUsersWithPostCount(): Flow<List<UserWithPostCount>>
    
    @Query("""
        SELECT u.*
        FROM users u
        INNER JOIN (
            SELECT user_id, COUNT(*) as cnt
            FROM posts
            GROUP BY user_id
            HAVING cnt > :minPosts
        ) p ON u.id = p.user_id
    """)
    fun getActiveUsers(minPosts: Int): Flow<List<User>>
    
    // ═══════════════════════════════════════════════════════════════════════
    // FULL TEXT SEARCH (FTS)
    // ═══════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT * FROM users 
        WHERE username LIKE '%' || :query || '%' 
           OR email LIKE '%' || :query || '%'
        LIMIT :limit
    """)
    fun searchUsers(query: String, limit: Int = 20): Flow<List<User>>
    
    // With FTS table (faster for large datasets)
    // @Query("SELECT * FROM users_fts WHERE users_fts MATCH :query")
    // fun searchUsersFts(query: String): Flow<List<User>>
    
    // ═══════════════════════════════════════════════════════════════════════
    // PAGINATION
    // ═══════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT * FROM users 
        ORDER BY created_at DESC 
        LIMIT :pageSize OFFSET :offset
    """)
    suspend fun getUsersPage(pageSize: Int, offset: Int): List<User>
    
    // For Paging 3 library
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun getUsersPagingSource(): PagingSource<Int, User>
    
    // ═══════════════════════════════════════════════════════════════════════
    // CONDITIONAL UPDATES
    // ═══════════════════════════════════════════════════════════════════════
    
    @Query("UPDATE users SET last_login = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: Long, timestamp: Long)
    
    @Query("""
        UPDATE users 
        SET status = 'inactive' 
        WHERE last_login < :cutoffTime AND status = 'active'
    """)
    suspend fun deactivateInactiveUsers(cutoffTime: Long): Int
    
    @Query("""
        UPDATE users 
        SET email_verified = 1 
        WHERE id IN (:userIds)
    """)
    suspend fun verifyEmails(userIds: List<Long>): Int
}

data class UserStats(
    @ColumnInfo(name = "MIN(age)") val minAge: Int,
    @ColumnInfo(name = "MAX(age)") val maxAge: Int,
    @ColumnInfo(name = "AVG(age)") val avgAge: Double,
    @ColumnInfo(name = "COUNT(*)") val totalCount: Int
)

data class AgeGroupCount(
    val ageGroup: String,
    val count: Int
)

data class UserWithPostCount(
    @Embedded val user: User,
    val postCount: Int
)
```

### Room Database Migrations

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ROOM DATABASE MIGRATIONS                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   VERSION 1              VERSION 2              VERSION 3                   │
│   ─────────              ─────────              ─────────                   │
│                                                                             │
│   users                  users                  users                       │
│   ┌──────────┐           ┌──────────┐           ┌──────────┐               │
│   │ id       │           │ id       │           │ id       │               │
│   │ name     │  ──────►  │ name     │  ──────►  │ name     │               │
│   │ email    │ Migration │ email    │ Migration │ email    │               │
│   └──────────┘   1 → 2   │ avatar   │   2 → 3   │ avatar   │               │
│                          └──────────┘           │ phone    │               │
│                           (add col)             │ verified │               │
│                                                 └──────────┘               │
│                                                  (add cols)                 │
│                                                                             │
│   MIGRATION STRATEGIES:                                                     │
│   ─────────────────────                                                     │
│                                                                             │
│   1. MIGRATION OBJECTS (Recommended for production)                        │
│      - Write SQL to transform schema                                        │
│      - Data is preserved                                                    │
│      - Fine-grained control                                                 │
│                                                                             │
│   2. AUTO-MIGRATION (Room 2.4+)                                            │
│      - Automatic for simple changes                                         │
│      - Add/remove columns                                                   │
│      - Rename with @RenameColumn                                            │
│                                                                             │
│   3. DESTRUCTIVE MIGRATION (Dev only!)                                     │
│      - .fallbackToDestructiveMigration()                                   │
│      - Drops and recreates tables                                           │
│      - ALL DATA LOST ⚠️                                                    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// MANUAL MIGRATIONS
// ═══════════════════════════════════════════════════════════════════════════

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new column with default value
        database.execSQL(
            "ALTER TABLE users ADD COLUMN avatar TEXT DEFAULT NULL"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add multiple columns
        database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT")
        database.execSQL("ALTER TABLE users ADD COLUMN verified INTEGER NOT NULL DEFAULT 0")
        
        // Create index for new column
        database.execSQL("CREATE INDEX index_users_phone ON users(phone)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Complex migration: rename column (SQLite doesn't support ALTER COLUMN)
        // 1. Create new table with desired schema
        database.execSQL("""
            CREATE TABLE users_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                full_name TEXT NOT NULL,
                email TEXT NOT NULL,
                avatar TEXT,
                phone TEXT,
                verified INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // 2. Copy data, transforming as needed
        database.execSQL("""
            INSERT INTO users_new (id, full_name, email, avatar, phone, verified)
            SELECT id, name, email, avatar, phone, verified FROM users
        """)
        
        // 3. Drop old table
        database.execSQL("DROP TABLE users")
        
        // 4. Rename new table
        database.execSQL("ALTER TABLE users_new RENAME TO users")
        
        // 5. Recreate indexes
        database.execSQL("CREATE UNIQUE INDEX index_users_email ON users(email)")
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// AUTO-MIGRATION (Room 2.4+)
// ═══════════════════════════════════════════════════════════════════════════

@Database(
    entities = [User::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = Migration2To3::class)
    ]
)
abstract class AppDatabase : RoomDatabase()

@RenameColumn(tableName = "users", fromColumnName = "name", toColumnName = "full_name")
class Migration2To3 : AutoMigrationSpec

// ═══════════════════════════════════════════════════════════════════════════
// DATABASE BUILDER WITH MIGRATIONS
// ═══════════════════════════════════════════════════════════════════════════

fun buildDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "app_database"
    )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        // For development only - clears DB if migration missing
        .fallbackToDestructiveMigrationOnDowngrade()
        // Callback for post-migration actions
        .addCallback(object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Run after every database open
            }
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Run only on first creation
            }
        })
        .build()
}
```

### Relations (One-to-Many, Many-to-Many)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        ROOM RELATIONSHIPS                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ONE-TO-MANY (User → Posts)                                               │
│   ──────────────────────────                                               │
│                                                                             │
│   ┌─────────┐           ┌─────────┐                                        │
│   │  User   │ 1 ─────── │  Post   │ *                                      │
│   │─────────│           │─────────│                                        │
│   │ id (PK) │◄──────────│ userId  │ (FK)                                   │
│   │ name    │           │ title   │                                        │
│   └─────────┘           │ content │                                        │
│                         └─────────┘                                        │
│                                                                             │
│   MANY-TO-MANY (Posts ↔ Tags) via Junction Table                           │
│   ──────────────────────────────────────────────                           │
│                                                                             │
│   ┌─────────┐         ┌─────────────┐         ┌─────────┐                 │
│   │  Post   │ * ───── │ PostTagRef  │ ─────── │   Tag   │ *               │
│   │─────────│         │─────────────│         │─────────│                 │
│   │ id (PK) │◄────────│ postId (PK) │         │ id (PK) │                 │
│   │ title   │         │ tagId  (PK) │────────►│ name    │                 │
│   └─────────┘         └─────────────┘         └─────────┘                 │
│                                                                             │
│   @Embedded - Flatten object into parent table                             │
│   ─────────────────────────────────────────────                            │
│                                                                             │
│   ┌───────────────────────────────────────┐                                │
│   │           Company Table               │                                │
│   │───────────────────────────────────────│                                │
│   │ id | name | address_street | address_ │ ◄── Address embedded          │
│   │    |      |                | city     │     as prefixed columns        │
│   └───────────────────────────────────────┘                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// One-to-Many: User has many Posts
data class UserWithPosts(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "user_id"
    )
    val posts: List<Post>
)

@Dao
interface UserWithPostsDao {
    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserWithPosts(userId: Long): Flow<UserWithPosts?>
    
    @Transaction
    @Query("SELECT * FROM users")
    fun getAllUsersWithPosts(): Flow<List<UserWithPosts>>
}

// Many-to-Many: Posts and Tags
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    val name: String
)

@Entity(
    tableName = "post_tag_cross_ref",
    primaryKeys = ["postId", "tagId"]
)
data class PostTagCrossRef(
    val postId: Long,
    val tagId: Long
)

data class PostWithTags(
    @Embedded val post: Post,
    @Relation(
        parentColumn = "id",
        entityColumn = "tagId",
        associateBy = Junction(PostTagCrossRef::class)
    )
    val tags: List<Tag>
)

data class TagWithPosts(
    @Embedded val tag: Tag,
    @Relation(
        parentColumn = "tagId",
        entityColumn = "id",
        associateBy = Junction(PostTagCrossRef::class)
    )
    val posts: List<Post>
)
```

### Database

```kotlin
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [User::class, Post::class, Tag::class, PostTagCrossRef::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun userWithPostsDao(): UserWithPostsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // Use carefully!
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        // Migration example
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN profile_image TEXT"
                )
            }
        }
        
        // Callback for pre-populating database
        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate with initial data
                db.execSQL(
                    "INSERT INTO users (username, email) VALUES ('admin', 'admin@example.com')"
                )
            }
        }
    }
}
```

### Using Room with Repository Pattern

```kotlin
class UserRepository(
    private val userDao: UserDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    
    fun getUserById(id: Long): Flow<User?> = userDao.getUserById(id)
    
    suspend fun insert(user: User): Long = withContext(ioDispatcher) {
        userDao.insert(user)
    }
    
    suspend fun update(user: User) = withContext(ioDispatcher) {
        userDao.update(user)
    }
    
    suspend fun delete(user: User) = withContext(ioDispatcher) {
        userDao.delete(user)
    }
    
    fun searchUsers(query: String): Flow<List<User>> = userDao.searchUsers(query)
}
```

### Using with ViewModel

```kotlin
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()
    
    fun selectUser(userId: Long) {
        viewModelScope.launch {
            repository.getUserById(userId).collect {
                _selectedUser.value = it
            }
        }
    }
    
    fun addUser(username: String, email: String) {
        viewModelScope.launch {
            repository.insert(User(username = username, email = email))
        }
    }
    
    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.delete(user)
        }
    }
}
```

### Room Testing

```kotlin
/**
 * Room Database Testing Patterns
 */
@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    
    @Before
    fun setup() {
        // Use in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // Only for testing!
            .build()
        
        userDao = database.userDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveUser() = runTest {
        // Given
        val user = User(username = "test", email = "test@example.com")
        
        // When
        val id = userDao.insert(user)
        val retrieved = userDao.getUserByIdOnce(id)
        
        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.username).isEqualTo("test")
    }
    
    @Test
    fun flowEmitsOnInsert() = runTest {
        // Given
        val users = mutableListOf<List<User>>()
        val job = launch(UnconfinedTestDispatcher()) {
            userDao.getAllUsers().toList(users)
        }
        
        // When
        userDao.insert(User(username = "user1", email = "u1@test.com"))
        userDao.insert(User(username = "user2", email = "u2@test.com"))
        
        // Then
        assertThat(users).hasSize(3) // empty + 2 inserts
        assertThat(users.last()).hasSize(2)
        
        job.cancel()
    }
    
    @Test
    fun deleteRemovesUser() = runTest {
        // Given
        val user = User(username = "test", email = "test@example.com")
        val id = userDao.insert(user)
        
        // When
        userDao.deleteById(id)
        val retrieved = userDao.getUserByIdOnce(id)
        
        // Then
        assertThat(retrieved).isNull()
    }
    
    @Test
    fun transactionRollsBackOnError() = runTest {
        // Given
        userDao.insert(User(username = "existing", email = "test@example.com"))
        
        // When/Then
        assertThrows<SQLiteConstraintException> {
            userDao.insert(User(username = "new", email = "test@example.com")) // Duplicate email
        }
        
        // Verify original user still exists
        val count = userDao.getUserCount()
        assertThat(count).isEqualTo(1)
    }
}
```

### Room with Dependency Injection (Hilt)

```kotlin
// ═══════════════════════════════════════════════════════════════════════════
// DATABASE MODULE
// ═══════════════════════════════════════════════════════════════════════════

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// REPOSITORY
// ═══════════════════════════════════════════════════════════════════════════

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
        .flowOn(ioDispatcher)
    
    suspend fun insert(user: User) = withContext(ioDispatcher) {
        userDao.insert(user)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ═══════════════════════════════════════════════════════════════════════════

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    val users: StateFlow<List<User>> = userRepository.allUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun addUser(name: String, email: String) {
        viewModelScope.launch {
            userRepository.insert(User(username = name, email = email))
        }
    }
}
```

---

## 7.4 File Storage

### File Storage Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     ANDROID FILE STORAGE HIERARCHY                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│                          ┌──────────────────┐                              │
│                          │   Your App       │                              │
│                          └────────┬─────────┘                              │
│                                   │                                        │
│            ┌──────────────────────┼──────────────────────┐                │
│            ▼                      ▼                      ▼                │
│   ┌────────────────┐    ┌────────────────┐    ┌────────────────┐         │
│   │   INTERNAL     │    │   EXTERNAL     │    │    SHARED      │         │
│   │   STORAGE      │    │  APP-SPECIFIC  │    │   STORAGE      │         │
│   └───────┬────────┘    └───────┬────────┘    └───────┬────────┘         │
│           │                     │                     │                   │
│           ▼                     ▼                     ▼                   │
│   /data/data/<pkg>/     /storage/.../        /storage/.../               │
│   ├── files/            Android/data/<pkg>/  ├── Pictures/               │
│   ├── cache/            ├── files/           ├── Downloads/              │
│   ├── databases/        └── cache/           ├── Music/                  │
│   └── shared_prefs/                          └── Documents/              │
│                                                                           │
│   ┌─────────────────────────────────────────────────────────────────────┐│
│   │ ACCESS REQUIREMENTS                                                  ││
│   │                                                                      ││
│   │  Internal:        No permission needed                               ││
│   │  External App:    No permission needed (API 19+)                     ││
│   │  Shared Storage:  MediaStore API or SAF (Android 10+)               ││
│   │                   Legacy: READ/WRITE_EXTERNAL_STORAGE               ││
│   └─────────────────────────────────────────────────────────────────────┘│
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Scoped Storage Evolution (Android 10+)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    STORAGE ACCESS EVOLUTION                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   BEFORE ANDROID 10 (API 29)                                               │
│   ──────────────────────────                                               │
│                                                                             │
│   ┌─────────────┐                     ┌─────────────────────┐             │
│   │   App A     │ ──── PERMISSION ──► │  Entire External    │             │
│   └─────────────┘   (broad access)    │     Storage         │             │
│   ┌─────────────┐                     │  /storage/emulated/ │             │
│   │   App B     │ ──── PERMISSION ──► │  ├── Pictures/      │             │
│   └─────────────┘   (read anything)   │  ├── App A files    │  ⚠️        │
│                                       │  └── Any file!      │  Privacy    │
│                                       └─────────────────────┘  Risk!      │
│                                                                             │
│   ANDROID 10+ (SCOPED STORAGE)                                             │
│   ────────────────────────────                                             │
│                                                                             │
│   ┌─────────────┐     ┌─────────────────────────────────────┐             │
│   │   App A     │ ──► │  App-Specific Directory             │  ✅ Always  │
│   └─────────────┘     │  Android/data/com.appA/             │  Available  │
│                       └─────────────────────────────────────┘             │
│                                                                             │
│   ┌─────────────┐     ┌─────────────────────────────────────┐             │
│   │   App A     │ ──► │  MediaStore (Images/Videos/Audio)   │  ✅ Via API │
│   └─────────────┘     │  Can only see own media by default  │             │
│                       └─────────────────────────────────────┘             │
│                                                                             │
│   ┌─────────────┐     ┌─────────────────────────────────────┐             │
│   │   App A     │ ──► │  User-Selected Files (SAF)          │  ✅ User    │
│   └─────────────┘     │  Explicit user consent required     │  Consent    │
│                       └─────────────────────────────────────┘             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Internal vs External Storage

| Feature | Internal Storage | External Storage |
|---------|-----------------|------------------|
| Location | `/data/data/<package>/` | `/storage/emulated/0/` or SD card |
| Privacy | Private to app | Accessible to other apps (with permission) |
| Persistence | Deleted with app uninstall | Survives uninstall (unless app-specific) |
| Permission | No permission needed | Requires permission (before Android 10) |
| Availability | Always available | May not be available |

### Internal Storage

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    INTERNAL STORAGE DIRECTORIES                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   /data/data/com.yourapp/                                                   │
│   │                                                                         │
│   ├── files/              ◄── context.filesDir                             │
│   │   └── user_data.json      Permanent files, backed up by default        │
│   │                                                                         │
│   ├── cache/              ◄── context.cacheDir                             │
│   │   └── temp_image.jpg      System may delete when low on space          │
│   │                                                                         │
│   ├── no_backup/          ◄── context.noBackupFilesDir                     │
│   │   └── device_token.txt    Never backed up (for device-specific data)   │
│   │                                                                         │
│   ├── code_cache/         ◄── context.codeCacheDir                          │
│   │   └── dex_files           System-managed, for compiled code            │
│   │                                                                         │
│   ├── databases/          ◄── (Room/SQLite databases)                      │
│   │   └── app.db                                                            │
│   │                                                                         │
│   └── shared_prefs/       ◄── (SharedPreferences XML files)                │
│       └── settings.xml                                                      │
│                                                                             │
│   ACCESS METHODS:                                                           │
│   ────────────────                                                          │
│   context.filesDir          → /data/data/<pkg>/files/                      │
│   context.cacheDir          → /data/data/<pkg>/cache/                      │
│   context.noBackupFilesDir  → /data/data/<pkg>/no_backup/                  │
│   context.codeCacheDir      → /data/data/<pkg>/code_cache/                 │
│   context.getDir("custom", MODE_PRIVATE) → /data/data/<pkg>/app_custom/    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
class InternalStorageManager(private val context: Context) {
    
    // Write to internal storage
    fun writeFile(fileName: String, content: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { fos ->
            fos.write(content.toByteArray())
        }
    }
    
    // Read from internal storage
    fun readFile(fileName: String): String {
        return context.openFileInput(fileName).bufferedReader().use { it.readText() }
    }
    
    // Write binary data
    fun writeBinaryFile(fileName: String, data: ByteArray) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { fos ->
            fos.write(data)
        }
    }
    
    // List files
    fun listFiles(): Array<String> {
        return context.fileList()
    }
    
    // Delete file
    fun deleteFile(fileName: String): Boolean {
        return context.deleteFile(fileName)
    }
    
    // Get file path
    fun getFilePath(fileName: String): File {
        return File(context.filesDir, fileName)
    }
    
    // Cache directory
    fun writeCacheFile(fileName: String, content: String) {
        val cacheFile = File(context.cacheDir, fileName)
        cacheFile.writeText(content)
    }
    
    fun readCacheFile(fileName: String): String? {
        val cacheFile = File(context.cacheDir, fileName)
        return if (cacheFile.exists()) cacheFile.readText() else null
    }
    
    // Clear cache
    fun clearCache() {
        context.cacheDir.deleteRecursively()
    }
}
```

### Complete File Storage Manager

```kotlin
/**
 * Production-ready file storage manager
 * Handles internal, cache, and no-backup storage with error handling
 */
class FileStorageManager(private val context: Context) {
    
    // ═══════════════════════════════════════════════════════════════════════
    // DIRECTORIES
    // ═══════════════════════════════════════════════════════════════════════
    
    enum class StorageType {
        FILES,          // Permanent, backed up
        CACHE,          // Temporary, system can delete
        NO_BACKUP       // Permanent, never backed up
    }
    
    private fun getDirectory(type: StorageType): File {
        return when (type) {
            StorageType.FILES -> context.filesDir
            StorageType.CACHE -> context.cacheDir
            StorageType.NO_BACKUP -> context.noBackupFilesDir
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════
    
    sealed class FileResult<T> {
        data class Success<T>(val data: T) : FileResult<T>()
        data class Error<T>(val exception: Exception) : FileResult<T>()
    }
    
    /**
     * Write text to file with atomic write (write to temp, then rename)
     */
    suspend fun writeText(
        fileName: String,
        content: String,
        storage: StorageType = StorageType.FILES
    ): FileResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getDirectory(storage)
            val file = File(dir, fileName)
            val tempFile = File(dir, "$fileName.tmp")
            
            // Atomic write: write to temp, then rename
            tempFile.writeText(content)
            tempFile.renameTo(file)
            
            FileResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("FileStorage", "Failed to write $fileName", e)
            FileResult.Error(e)
        }
    }
    
    /**
     * Write binary data
     */
    suspend fun writeBytes(
        fileName: String,
        data: ByteArray,
        storage: StorageType = StorageType.FILES
    ): FileResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(getDirectory(storage), fileName)
            file.writeBytes(data)
            FileResult.Success(Unit)
        } catch (e: Exception) {
            FileResult.Error(e)
        }
    }
    
    /**
     * Write object as JSON
     */
    suspend inline fun <reified T> writeJson(
        fileName: String,
        data: T,
        storage: StorageType = StorageType.FILES
    ): FileResult<Unit> {
        return try {
            val json = Gson().toJson(data)
            writeText(fileName, json, storage)
        } catch (e: Exception) {
            FileResult.Error(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // READ OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun readText(
        fileName: String,
        storage: StorageType = StorageType.FILES
    ): FileResult<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(getDirectory(storage), fileName)
            if (!file.exists()) {
                return@withContext FileResult.Error(FileNotFoundException("File not found: $fileName"))
            }
            FileResult.Success(file.readText())
        } catch (e: Exception) {
            FileResult.Error(e)
        }
    }
    
    suspend fun readBytes(
        fileName: String,
        storage: StorageType = StorageType.FILES
    ): FileResult<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val file = File(getDirectory(storage), fileName)
            FileResult.Success(file.readBytes())
        } catch (e: Exception) {
            FileResult.Error(e)
        }
    }
    
    suspend inline fun <reified T> readJson(
        fileName: String,
        storage: StorageType = StorageType.FILES
    ): FileResult<T> {
        return when (val result = readText(fileName, storage)) {
            is FileResult.Success -> {
                try {
                    val data = Gson().fromJson(result.data, T::class.java)
                    FileResult.Success(data)
                } catch (e: Exception) {
                    FileResult.Error(e)
                }
            }
            is FileResult.Error -> FileResult.Error(result.exception)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // FILE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════
    
    fun exists(fileName: String, storage: StorageType = StorageType.FILES): Boolean {
        return File(getDirectory(storage), fileName).exists()
    }
    
    fun delete(fileName: String, storage: StorageType = StorageType.FILES): Boolean {
        return File(getDirectory(storage), fileName).delete()
    }
    
    fun listFiles(storage: StorageType = StorageType.FILES): List<String> {
        return getDirectory(storage).listFiles()?.map { it.name } ?: emptyList()
    }
    
    /**
     * Get file size in bytes
     */
    fun getFileSize(fileName: String, storage: StorageType = StorageType.FILES): Long {
        return File(getDirectory(storage), fileName).length()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // CACHE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Get total cache size
     */
    fun getCacheSize(): Long {
        return calculateDirSize(context.cacheDir) + 
               (context.externalCacheDir?.let { calculateDirSize(it) } ?: 0L)
    }
    
    private fun calculateDirSize(dir: File): Long {
        var size = 0L
        dir.walkTopDown().forEach { file ->
            if (file.isFile) {
                size += file.length()
            }
        }
        return size
    }
    
    /**
     * Clear all cache files
     */
    fun clearCache() {
        context.cacheDir.deleteRecursively()
        context.externalCacheDir?.deleteRecursively()
    }
    
    /**
     * Clear cache files older than specified duration
     */
    fun clearOldCache(maxAgeMillis: Long) {
        val cutoff = System.currentTimeMillis() - maxAgeMillis
        context.cacheDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) {
                file.deleteRecursively()
            }
        }
    }
    
    /**
     * Trim cache to specified size limit
     */
    fun trimCacheToSize(maxSizeBytes: Long) {
        val files = context.cacheDir.listFiles()?.toMutableList() ?: return
        files.sortBy { it.lastModified() } // Oldest first
        
        var currentSize = getCacheSize()
        var index = 0
        
        while (currentSize > maxSizeBytes && index < files.size) {
            val file = files[index]
            val fileSize = file.length()
            if (file.deleteRecursively()) {
                currentSize -= fileSize
            }
            index++
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // SHARING FILES
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Get content URI for sharing via FileProvider
     */
    fun getShareableUri(fileName: String, storage: StorageType = StorageType.FILES): Uri {
        val file = File(getDirectory(storage), fileName)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
```

### External Storage (Legacy - Pre Android 10)

```kotlin
// AndroidManifest.xml
// <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
//     android:maxSdkVersion="28" />
// <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
//     android:maxSdkVersion="32" />

class ExternalStorageManager(private val context: Context) {
    
    // Check if external storage is available
    fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
    
    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY
    }
    
    // App-specific external storage (no permission needed)
    fun getAppSpecificExternalDir(): File? {
        return context.getExternalFilesDir(null)
    }
    
    fun getAppSpecificExternalDir(type: String): File? {
        // Types: Environment.DIRECTORY_PICTURES, DIRECTORY_DOCUMENTS, etc.
        return context.getExternalFilesDir(type)
    }
    
    // Write to app-specific external storage
    fun writeToAppExternal(fileName: String, content: String) {
        val file = File(context.getExternalFilesDir(null), fileName)
        file.writeText(content)
    }
    
    // App-specific external cache
    fun getAppExternalCacheDir(): File? {
        return context.externalCacheDir
    }
}
```

### Scoped Storage (Android 10+)

Android 10 introduced **Scoped Storage** to improve user privacy:
- Apps can access their own files without permission
- Access to other apps' files requires the Storage Access Framework or MediaStore
- No more broad file system access

```kotlin
// AndroidManifest.xml for compatibility
// <application
//     android:requestLegacyExternalStorage="true" <!-- Only for Android 10 -->
//     ... >

class ScopedStorageManager(private val context: Context) {
    
    // App-specific storage (always available)
    private val appFilesDir: File = context.filesDir
    private val appCacheDir: File = context.cacheDir
    private val appExternalFilesDir: File? = context.getExternalFilesDir(null)
    
    // Save file to app-specific directory
    fun saveFile(fileName: String, content: ByteArray): Uri {
        val file = File(appFilesDir, fileName)
        file.writeBytes(content)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    // For sharing files with other apps, use FileProvider
    fun getShareableUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
```

### MediaStore API

MediaStore provides access to shared media files (images, videos, audio).

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         MEDIASTORE ARCHITECTURE                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌─────────────┐                                                         │
│    │   Your App  │                                                         │
│    └──────┬──────┘                                                         │
│           │                                                                │
│           │  ContentResolver                                               │
│           ▼                                                                │
│    ┌─────────────────────────────────────────────────────────────────┐    │
│    │                     MediaStore API                               │    │
│    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │    │
│    │  │   Images    │  │   Video     │  │   Audio     │              │    │
│    │  │   .Media    │  │   .Media    │  │   .Media    │              │    │
│    │  └─────────────┘  └─────────────┘  └─────────────┘              │    │
│    │  ┌─────────────┐  ┌─────────────┐                               │    │
│    │  │  Downloads  │  │   Files     │                               │    │
│    │  │  (API 29+)  │  │             │                               │    │
│    │  └─────────────┘  └─────────────┘                               │    │
│    └─────────────────────────────────────────────────────────────────┘    │
│                          │                                                 │
│                          ▼                                                 │
│    ┌─────────────────────────────────────────────────────────────────┐    │
│    │                   Shared Storage                                 │    │
│    │   /storage/emulated/0/                                          │    │
│    │   ├── Pictures/       ◄── Images.Media                          │    │
│    │   ├── DCIM/           ◄── Images.Media, Video.Media            │    │
│    │   ├── Movies/         ◄── Video.Media                          │    │
│    │   ├── Music/          ◄── Audio.Media                          │    │
│    │   ├── Downloads/      ◄── Downloads (API 29+)                   │    │
│    │   └── Documents/      ◄── Files                                 │    │
│    └─────────────────────────────────────────────────────────────────┘    │
│                                                                             │
│   KEY CONCEPTS:                                                             │
│   • Content URIs: content://media/external/images/media/123                │
│   • IS_PENDING: Mark file as incomplete during write (API 29+)            │
│   • RELATIVE_PATH: Specify subdirectory (e.g., Pictures/MyApp)            │
│   • Query returns Cursor with file metadata                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
class MediaStoreManager(private val context: Context) {
    
    // Save image to MediaStore (visible in gallery)
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun saveImageToGallery(
        bitmap: Bitmap,
        displayName: String
    ): Uri? = withContext(Dispatchers.IO) {
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.IS_PENDING, 1) // Mark as pending
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            
            // Mark as complete
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(it, contentValues, null, null)
        }
        
        uri
    }
    
    // Save image compatible with all Android versions
    fun saveImage(bitmap: Bitmap, displayName: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runBlocking { saveImageToGallery(bitmap, displayName) }
        } else {
            // Legacy approach
            saveLegacyImage(bitmap, displayName)
        }
    }
    
    @Suppress("DEPRECATION")
    private fun saveLegacyImage(bitmap: Bitmap, displayName: String): Uri? {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, "$displayName.jpg")
        
        FileOutputStream(image).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }
        
        // Notify media scanner
        MediaScannerConnection.scanFile(context, arrayOf(image.absolutePath), null, null)
        
        return Uri.fromFile(image)
    }
    
    // Query images from MediaStore
    fun queryImages(): List<MediaItem> {
        val images = mutableListOf<MediaItem>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED
        )
        
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val size = cursor.getLong(sizeColumn)
                
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                images.add(MediaItem(id, name, size, contentUri))
            }
        }
        
        return images
    }
    
    // Delete image
    fun deleteImage(uri: Uri): Boolean {
        return try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (e: SecurityException) {
            // On Android 10+, might need user confirmation
            false
        }
    }
    
    // Save video to MediaStore
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun saveVideoToGallery(
        videoFile: File,
        displayName: String,
        mimeType: String = "video/mp4"
    ): Uri? = withContext(Dispatchers.IO) {
        
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                videoFile.inputStream().copyTo(outputStream)
            }
            
            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(it, contentValues, null, null)
        }
        
        uri
    }
    
    // Save file to Downloads
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun saveToDownloads(
        fileName: String,
        content: ByteArray,
        mimeType: String
    ): Uri? = withContext(Dispatchers.IO) {
        
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(content)
            }
        }
        
        uri
    }
}

data class MediaItem(
    val id: Long,
    val name: String,
    val size: Long,
    val uri: Uri
)
```

### Storage Access Framework (SAF)

SAF allows users to browse and pick files/folders from any document provider.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                   STORAGE ACCESS FRAMEWORK (SAF)                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────┐                                                          │
│   │   Your App  │                                                          │
│   └──────┬──────┘                                                          │
│          │                                                                 │
│          │ Intent: ACTION_OPEN_DOCUMENT                                    │
│          │         ACTION_CREATE_DOCUMENT                                  │
│          │         ACTION_OPEN_DOCUMENT_TREE                               │
│          ▼                                                                 │
│   ┌─────────────────────────────────────────────────────────────────┐     │
│   │                    System Document Picker                        │     │
│   │   ┌──────────────────────────────────────────────────────────┐  │     │
│   │   │  📁 Recent    📁 Drive    📁 Downloads    📁 Images     │  │     │
│   │   │                                                          │  │     │
│   │   │  User browses and selects file(s)                       │  │     │
│   │   └──────────────────────────────────────────────────────────┘  │     │
│   └────────────────────────────┬────────────────────────────────────┘     │
│                                │                                           │
│                                │ Returns content:// URI                    │
│                                ▼                                           │
│   ┌─────────────────────────────────────────────────────────────────┐     │
│   │                    Document Providers                            │     │
│   │                                                                  │     │
│   │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐            │     │
│   │  │Local Storage │ │Google Drive  │ │  Dropbox     │            │     │
│   │  │  Provider    │ │  Provider    │ │  Provider    │            │     │
│   │  └──────────────┘ └──────────────┘ └──────────────┘            │     │
│   │                                                                  │     │
│   │  Any app can implement DocumentsProvider to expose files        │     │
│   └─────────────────────────────────────────────────────────────────┘     │
│                                                                             │
│   FEATURES:                                                                 │
│   ✅ User explicitly chooses files (privacy-respecting)                    │
│   ✅ Works with any document provider (local, cloud)                       │
│   ✅ Persistent URI permissions (survives reboots)                         │
│   ✅ Full file CRUD operations                                             │
│   ✅ Directory access with OPEN_DOCUMENT_TREE                              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
class StorageAccessFrameworkManager(private val activity: ComponentActivity) {
    
    // Request codes
    companion object {
        const val CREATE_FILE_REQUEST = 1001
        const val OPEN_FILE_REQUEST = 1002
        const val OPEN_FOLDER_REQUEST = 1003
    }
    
    // Modern approach using Activity Result API
    private val createFileLauncher = activity.registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { saveToUri(it) }
    }
    
    private val openFileLauncher = activity.registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { readFromUri(it) }
    }
    
    private val openMultipleFilesLauncher = activity.registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { readFromUri(it) }
    }
    
    private val openFolderLauncher = activity.registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { handleFolderSelection(it) }
    }
    
    // Create a new file
    fun createFile(fileName: String) {
        createFileLauncher.launch(fileName)
    }
    
    // Open a file
    fun openFile() {
        openFileLauncher.launch(arrayOf("*/*")) // All file types
    }
    
    fun openImageFile() {
        openFileLauncher.launch(arrayOf("image/*"))
    }
    
    fun openTextFile() {
        openFileLauncher.launch(arrayOf("text/*", "application/json"))
    }
    
    // Open multiple files
    fun openMultipleFiles() {
        openMultipleFilesLauncher.launch(arrayOf("*/*"))
    }
    
    // Open folder/directory
    fun openFolder() {
        openFolderLauncher.launch(null)
    }
    
    // Read content from URI
    private fun readFromUri(uri: Uri) {
        activity.contentResolver.openInputStream(uri)?.use { inputStream ->
            val content = inputStream.bufferedReader().use { it.readText() }
            // Process content
        }
    }
    
    // Write content to URI
    private fun saveToUri(uri: Uri) {
        activity.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write("File content here".toByteArray())
        }
    }
    
    // Handle folder selection - get persistent access
    private fun handleFolderSelection(uri: Uri) {
        // Take persistent permission
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        activity.contentResolver.takePersistableUriPermission(uri, takeFlags)
        
        // List files in the folder
        listFilesInFolder(uri)
    }
    
    // List files in a folder
    private fun listFilesInFolder(folderUri: Uri) {
        val documentFile = DocumentFile.fromTreeUri(activity, folderUri)
        documentFile?.listFiles()?.forEach { file ->
            Log.d("SAF", "File: ${file.name}, isDirectory: ${file.isDirectory}")
        }
    }
    
    // Create file in selected folder
    fun createFileInFolder(folderUri: Uri, fileName: String, mimeType: String): Uri? {
        val documentFile = DocumentFile.fromTreeUri(activity, folderUri)
        val newFile = documentFile?.createFile(mimeType, fileName)
        return newFile?.uri
    }
    
    // Delete file
    fun deleteFile(uri: Uri): Boolean {
        val documentFile = DocumentFile.fromSingleUri(activity, uri)
        return documentFile?.delete() ?: false
    }
    
    // Check if file exists
    fun fileExists(uri: Uri): Boolean {
        val documentFile = DocumentFile.fromSingleUri(activity, uri)
        return documentFile?.exists() ?: false
    }
    
    // Get file metadata
    fun getFileInfo(uri: Uri): FileInfo? {
        val documentFile = DocumentFile.fromSingleUri(activity, uri) ?: return null
        return FileInfo(
            name = documentFile.name ?: "",
            size = documentFile.length(),
            mimeType = documentFile.type ?: "",
            lastModified = documentFile.lastModified(),
            isDirectory = documentFile.isDirectory
        )
    }
}

data class FileInfo(
    val name: String,
    val size: Long,
    val mimeType: String,
    val lastModified: Long,
    val isDirectory: Boolean
)
```

### FileProvider Setup

```xml
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

```xml
<!-- res/xml/file_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path name="internal_files" path="." />
    <cache-path name="cache" path="." />
    <external-files-path name="external_files" path="." />
    <external-cache-path name="external_cache" path="." />
    <external-path name="external" path="." />
</paths>
```

---

## 7.5 SQLite (Raw)

### SQLite Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SQLite Architecture                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌─────────────────────────────────────────────────────────────────┐    │
│    │                      Your Application                            │    │
│    │                                                                  │    │
│    │    ┌──────────────────┐    ┌──────────────────┐                │    │
│    │    │ SQLiteOpenHelper │    │  Repository      │                │    │
│    │    │ (DB lifecycle)   │    │  (Data access)   │                │    │
│    │    └────────┬─────────┘    └────────┬─────────┘                │    │
│    │             │                       │                           │    │
│    │             ▼                       ▼                           │    │
│    │    ┌────────────────────────────────────────────┐              │    │
│    │    │           SQLiteDatabase                    │              │    │
│    │    │                                             │              │    │
│    │    │  • getWritableDatabase()                   │              │    │
│    │    │  • getReadableDatabase()                   │              │    │
│    │    │  • execSQL(), rawQuery()                   │              │    │
│    │    │  • insert(), update(), delete()            │              │    │
│    │    │  • beginTransaction(), endTransaction()    │              │    │
│    │    └──────────────────────┬─────────────────────┘              │    │
│    └───────────────────────────│─────────────────────────────────────┘    │
│                                │                                           │
│    ┌───────────────────────────▼─────────────────────────────────────┐    │
│    │                      SQLite Engine                               │    │
│    │   (Embedded in Android - no separate server process)             │    │
│    │                                                                  │    │
│    │   Features:                                                      │    │
│    │   • ACID compliant (Atomicity, Consistency, Isolation, Durable) │    │
│    │   • Single-file database                                         │    │
│    │   • Zero configuration                                           │    │
│    │   • Full SQL support                                             │    │
│    │   • Supports up to 281 TB database size                         │    │
│    └──────────────────────────────────────────────────────────────────┘    │
│                                │                                           │
│    ┌───────────────────────────▼─────────────────────────────────────┐    │
│    │               /data/data/<package>/databases/                    │    │
│    │                        app.db                                    │    │
│    │                                                                  │    │
│    │   Single file contains:                                          │    │
│    │   • Schema (tables, indexes, triggers)                          │    │
│    │   • Data (rows)                                                  │    │
│    │   • Metadata                                                     │    │
│    └──────────────────────────────────────────────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### When to Use Raw SQLite

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ROOM vs RAW SQLite Decision                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   USE ROOM (Recommended)              USE RAW SQLite                        │
│   ──────────────────────              ──────────────                        │
│                                                                             │
│   ✅ New Android projects             ✅ Maximum performance needed         │
│   ✅ Type-safe queries                ✅ Legacy codebase migration          │
│   ✅ Compile-time verification        ✅ Dynamic schema changes             │
│   ✅ LiveData/Flow integration        ✅ Cross-platform (KMM)               │
│   ✅ Automatic migrations             ✅ Complex custom queries             │
│   ✅ Less boilerplate                 ✅ Direct SQLite features             │
│                                                                             │
│   PERFORMANCE COMPARISON:                                                   │
│   ┌─────────────────────────────────────────────────────────────────────┐ │
│   │ Operation          │ Room      │ Raw SQLite │ Difference            │ │
│   │────────────────────│───────────│────────────│───────────────────────│ │
│   │ Simple Insert      │ ~1.2ms    │ ~1.0ms     │ ~20% overhead         │ │
│   │ Batch Insert (1000)│ ~150ms    │ ~120ms     │ ~25% overhead         │ │
│   │ Simple Query       │ ~0.5ms    │ ~0.4ms     │ ~25% overhead         │ │
│   │                                                                     │ │
│   │ Note: Room overhead is usually negligible for most apps             │ │
│   └─────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

Use raw SQLite instead of Room when:
- Maximum performance is critical (avoid Room overhead)
- You need features not supported by Room
- Working with legacy codebase
- Dynamic table creation/modification
- Complex migrations that Room can't handle
- Cross-platform code sharing (KMM)

### SQLiteOpenHelper Lifecycle

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SQLiteOpenHelper Lifecycle                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   getWritableDatabase() / getReadableDatabase()                            │
│                    │                                                        │
│                    ▼                                                        │
│            ┌──────────────┐                                                │
│            │ DB Exists?   │                                                │
│            └──────┬───────┘                                                │
│                   │                                                         │
│         ┌────────┴────────┐                                                │
│         │ NO              │ YES                                            │
│         ▼                 ▼                                                │
│   ┌───────────┐    ┌───────────────────┐                                  │
│   │ onCreate()│    │ Check DB Version  │                                  │
│   │           │    └─────────┬─────────┘                                  │
│   │ Create    │              │                                            │
│   │ tables,   │    ┌─────────┴─────────┐                                  │
│   │ indexes   │    │                   │                                  │
│   └───────────┘    │ DB_VER < APP_VER  │ DB_VER > APP_VER                 │
│         │          ▼                   ▼                                   │
│         │    ┌───────────┐      ┌────────────┐                            │
│         │    │onUpgrade()│      │onDowngrade()│                           │
│         │    │           │      │             │                           │
│         │    │ Migrate   │      │ Handle     │                            │
│         │    │ schema    │      │ downgrade  │                            │
│         │    └───────────┘      └────────────┘                            │
│         │          │                   │                                   │
│         └──────────┴───────────────────┘                                  │
│                    │                                                       │
│                    ▼                                                       │
│            ┌───────────────┐                                              │
│            │  onOpen()      │ (called every time DB opens)                │
│            └───────────────┘                                              │
│                    │                                                       │
│                    ▼                                                       │
│            ┌───────────────┐                                              │
│            │  DB Ready!    │                                              │
│            └───────────────┘                                              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### SQLiteOpenHelper

```kotlin
class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "app_database.db"
        private const val DATABASE_VERSION = 2
        
        // Table names
        const val TABLE_USERS = "users"
        const val TABLE_POSTS = "posts"
        
        // User columns
        const val COLUMN_ID = "_id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_CREATED_AT = "created_at"
        
        // Post columns
        const val COLUMN_POST_ID = "_id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        
        // Create table SQL
        private const val CREATE_TABLE_USERS = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL UNIQUE,
                $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_CREATED_AT INTEGER DEFAULT (strftime('%s', 'now'))
            )
        """
        
        private const val CREATE_TABLE_POSTS = """
            CREATE TABLE $TABLE_POSTS (
                $COLUMN_POST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_CONTENT TEXT,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
                    ON DELETE CASCADE ON UPDATE CASCADE
            )
        """
        
        // Index creation
        private const val CREATE_INDEX_USER_EMAIL = """
            CREATE INDEX idx_user_email ON $TABLE_USERS($COLUMN_EMAIL)
        """
        
        private const val CREATE_INDEX_POST_USER = """
            CREATE INDEX idx_post_user ON $TABLE_POSTS($COLUMN_USER_ID)
        """
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USERS)
        db.execSQL(CREATE_TABLE_POSTS)
        db.execSQL(CREATE_INDEX_USER_EMAIL)
        db.execSQL(CREATE_INDEX_POST_USER)
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Migration from version 1 to 2
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN profile_image TEXT")
        }
        // Add more migrations as needed
    }
    
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Enable foreign key support
        db.setForeignKeyConstraintsEnabled(true)
    }
    
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle downgrade if needed, or throw exception
        super.onDowngrade(db, oldVersion, newVersion)
    }
}
```

### CRUD Operations

```kotlin
class UserRepository(private val dbHelper: DatabaseHelper) {
    
    // CREATE
    fun insertUser(username: String, email: String): Long {
        val db = dbHelper.writableDatabase
        
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USERNAME, username)
            put(DatabaseHelper.COLUMN_EMAIL, email)
        }
        
        return db.insertWithOnConflict(
            DatabaseHelper.TABLE_USERS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }
    
    // Using raw SQL
    fun insertUserRaw(username: String, email: String) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "INSERT INTO ${DatabaseHelper.TABLE_USERS} " +
            "(${DatabaseHelper.COLUMN_USERNAME}, ${DatabaseHelper.COLUMN_EMAIL}) " +
            "VALUES (?, ?)",
            arrayOf(username, email)
        )
    }
    
    // READ - Single user
    fun getUserById(id: Long): User? {
        val db = dbHelper.readableDatabase
        
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null, // All columns
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null, // groupBy
            null, // having
            null  // orderBy
        )
        
        return cursor.use {
            if (it.moveToFirst()) {
                cursorToUser(it)
            } else {
                null
            }
        }
    }
    
    // READ - All users
    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = dbHelper.readableDatabase
        
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_CREATED_AT} DESC"
        )
        
        cursor.use {
            while (it.moveToNext()) {
                users.add(cursorToUser(it))
            }
        }
        
        return users
    }
    
    // READ - Using raw query
    fun searchUsers(query: String): List<User> {
        val users = mutableListOf<User>()
        val db = dbHelper.readableDatabase
        
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} " +
            "WHERE ${DatabaseHelper.COLUMN_USERNAME} LIKE ? " +
            "OR ${DatabaseHelper.COLUMN_EMAIL} LIKE ?",
            arrayOf("%$query%", "%$query%")
        )
        
        cursor.use {
            while (it.moveToNext()) {
                users.add(cursorToUser(it))
            }
        }
        
        return users
    }
    
    // UPDATE
    fun updateUser(id: Long, username: String, email: String): Int {
        val db = dbHelper.writableDatabase
        
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USERNAME, username)
            put(DatabaseHelper.COLUMN_EMAIL, email)
        }
        
        return db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }
    
    // DELETE
    fun deleteUser(id: Long): Int {
        val db = dbHelper.writableDatabase
        
        return db.delete(
            DatabaseHelper.TABLE_USERS,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }
    
    // DELETE - All users
    fun deleteAllUsers(): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_USERS, null, null)
    }
    
    // Transaction
    fun insertUsersInTransaction(users: List<Pair<String, String>>) {
        val db = dbHelper.writableDatabase
        
        db.beginTransaction()
        try {
            users.forEach { (username, email) ->
                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_USERNAME, username)
                    put(DatabaseHelper.COLUMN_EMAIL, email)
                }
                db.insertOrThrow(DatabaseHelper.TABLE_USERS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    
    // Count
    fun getUserCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_USERS}", null)
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }
    
    // Helper function to convert cursor to User object
    private fun cursorToUser(cursor: Cursor): User {
        return User(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
            username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT))
        )
    }
}

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val createdAt: Long
)
```

### Advanced SQLite Operations

```kotlin
class AdvancedSQLiteOperations(private val dbHelper: DatabaseHelper) {
    
    // Join query
    fun getUsersWithPostCount(): List<UserWithPostCount> {
        val results = mutableListOf<UserWithPostCount>()
        val db = dbHelper.readableDatabase
        
        val query = """
            SELECT u.${DatabaseHelper.COLUMN_ID}, 
                   u.${DatabaseHelper.COLUMN_USERNAME}, 
                   COUNT(p.${DatabaseHelper.COLUMN_POST_ID}) as post_count
            FROM ${DatabaseHelper.TABLE_USERS} u
            LEFT JOIN ${DatabaseHelper.TABLE_POSTS} p 
                ON u.${DatabaseHelper.COLUMN_ID} = p.${DatabaseHelper.COLUMN_USER_ID}
            GROUP BY u.${DatabaseHelper.COLUMN_ID}
        """.trimIndent()
        
        val cursor = db.rawQuery(query, null)
        
        cursor.use {
            while (it.moveToNext()) {
                results.add(
                    UserWithPostCount(
                        id = it.getLong(0),
                        username = it.getString(1),
                        postCount = it.getInt(2)
                    )
                )
            }
        }
        
        return results
    }
    
    // Compiled statement for repeated operations
    fun insertMultipleUsersOptimized(users: List<Pair<String, String>>) {
        val db = dbHelper.writableDatabase
        val sql = "INSERT INTO ${DatabaseHelper.TABLE_USERS} " +
                "(${DatabaseHelper.COLUMN_USERNAME}, ${DatabaseHelper.COLUMN_EMAIL}) " +
                "VALUES (?, ?)"
        
        val statement = db.compileStatement(sql)
        
        db.beginTransaction()
        try {
            users.forEach { (username, email) ->
                statement.clearBindings()
                statement.bindString(1, username)
                statement.bindString(2, email)
                statement.executeInsert()
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    
    // Pagination
    fun getUsersPaginated(page: Int, pageSize: Int): List<User> {
        val users = mutableListOf<User>()
        val db = dbHelper.readableDatabase
        val offset = page * pageSize
        
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} " +
            "ORDER BY ${DatabaseHelper.COLUMN_CREATED_AT} DESC " +
            "LIMIT ? OFFSET ?",
            arrayOf(pageSize.toString(), offset.toString())
        )
        
        cursor.use {
            while (it.moveToNext()) {
                users.add(cursorToUser(it))
            }
        }
        
        return users
    }
    
    // Full text search (FTS)
    fun setupFullTextSearch() {
        val db = dbHelper.writableDatabase
        
        // Create FTS virtual table
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS users_fts 
            USING fts4(content="${DatabaseHelper.TABLE_USERS}", 
                      username, email)
        """)
        
        // Populate FTS table
        db.execSQL("INSERT INTO users_fts(users_fts) VALUES('rebuild')")
    }
    
    fun fullTextSearch(query: String): List<Long> {
        val ids = mutableListOf<Long>()
        val db = dbHelper.readableDatabase
        
        val cursor = db.rawQuery(
            "SELECT docid FROM users_fts WHERE users_fts MATCH ?",
            arrayOf(query)
        )
        
        cursor.use {
            while (it.moveToNext()) {
                ids.add(it.getLong(0))
            }
        }
        
        return ids
    }
    
    // Check if table exists
    fun tableExists(tableName: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tableName)
        )
        
        return cursor.use { it.count > 0 }
    }
    
    // Get table info
    fun getTableInfo(tableName: String): List<ColumnInfo> {
        val columns = mutableListOf<ColumnInfo>()
        val db = dbHelper.readableDatabase
        
        val cursor = db.rawQuery("PRAGMA table_info($tableName)", null)
        
        cursor.use {
            while (it.moveToNext()) {
                columns.add(
                    ColumnInfo(
                        cid = it.getInt(0),
                        name = it.getString(1),
                        type = it.getString(2),
                        notNull = it.getInt(3) == 1,
                        defaultValue = it.getString(4),
                        primaryKey = it.getInt(5) == 1
                    )
                )
            }
        }
        
        return columns
    }
    
    // Backup database
    fun backupDatabase(context: Context, backupName: String): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DatabaseHelper.DATABASE_NAME)
            val backupFile = File(context.getExternalFilesDir(null), backupName)
            dbFile.copyTo(backupFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun cursorToUser(cursor: Cursor): User {
        return User(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
            username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT))
        )
    }
}

data class UserWithPostCount(
    val id: Long,
    val username: String,
    val postCount: Int
)

data class ColumnInfo(
    val cid: Int,
    val name: String,
    val type: String,
    val notNull: Boolean,
    val defaultValue: String?,
    val primaryKey: Boolean
)
```

---

## Summary: Choosing the Right Storage Option

### Complete Storage Comparison

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    STORAGE OPTIONS COMPARISON                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│              DATA SIZE                                                      │
│                                                                             │
│   Small ◄────────────────────────────────────────────────────────► Large   │
│                                                                             │
│   ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐         │
│   │SharedPreferences│   │    DataStore    │   │      Room       │         │
│   │                 │   │                 │   │                 │         │
│   │ • Settings      │   │ • Settings      │   │ • Users         │         │
│   │ • Flags         │   │ • User prefs    │   │ • Messages      │         │
│   │ • Simple cache  │   │ • Session data  │   │ • Products      │         │
│   │ • Auth tokens   │   │ • Proto objects │   │ • Transactions  │         │
│   │                 │   │                 │   │ • Any structured│         │
│   │ Max: ~1MB       │   │ Max: ~1MB       │   │ Max: Unlimited  │         │
│   └─────────────────┘   └─────────────────┘   └─────────────────┘         │
│                                                                             │
│   ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐         │
│   │ Internal Files  │   │ External Files  │   │   MediaStore    │         │
│   │                 │   │                 │   │                 │         │
│   │ • Config files  │   │ • Downloads     │   │ • Photos        │         │
│   │ • Temp data     │   │ • Exports       │   │ • Videos        │         │
│   │ • Private docs  │   │ • Shared docs   │   │ • Audio files   │         │
│   │                 │   │                 │   │ • Large media   │         │
│   └─────────────────┘   └─────────────────┘   └─────────────────┘         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Performance Characteristics

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PERFORMANCE COMPARISON                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   READ SPEED (relative)                                                     │
│   ═════════════════════                                                     │
│                                                                             │
│   SharedPrefs (memory)  ████████████████████████████████████████ Fastest   │
│   DataStore (memory)    ███████████████████████████████████████            │
│   Room (indexed)        ██████████████████████████████                     │
│   Raw SQLite            ████████████████████████████                       │
│   File I/O              ████████████████                                   │
│   MediaStore            ██████████                         Slowest         │
│                                                                             │
│   WRITE SPEED (relative)                                                    │
│   ══════════════════════                                                    │
│                                                                             │
│   SharedPrefs (apply)   ████████████████████████████████████████ Fastest   │
│   Room (batch)          ███████████████████████████████████                │
│   DataStore             ██████████████████████████████                     │
│   Raw SQLite            ████████████████████████████                       │
│   SharedPrefs (commit)  ████████████████                                   │
│   File I/O (large)      ████████████                                       │
│   MediaStore            ████████                           Slowest         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

| Storage Type | Best For | Data Size | Persistence |
|--------------|----------|-----------|-------------|
| **SharedPreferences** | Simple key-value settings | Small | App lifetime |
| **EncryptedSharedPrefs** | Sensitive settings | Small | App lifetime |
| **DataStore Preferences** | Settings with async API | Small-Medium | App lifetime |
| **DataStore Proto** | Typed structured data | Small-Medium | App lifetime |
| **Room** | Complex relational data | Any | App lifetime |
| **Internal Storage** | Private files | Any | App lifetime |
| **External Storage** | Shareable files | Any | Configurable |
| **MediaStore** | Media files (images, videos) | Large | User-controlled |
| **SAF** | User-selected documents | Any | User-controlled |
| **Raw SQLite** | Performance-critical DB | Any | App lifetime |

### Visual Decision Tree

```
Is it simple key-value data?
├── Yes → Is it sensitive (tokens, passwords)?
│         ├── Yes → Use EncryptedSharedPreferences
│         └── No → Need async/Flow API?
│                  ├── Yes → Use DataStore Preferences
│                  └── No → Use SharedPreferences
└── No → Is it structured/relational data?
         ├── Yes → Need maximum performance?
         │         ├── Yes → Use Raw SQLite
         │         └── No → Use Room
         └── No → Is it a file?
                  ├── Yes → Should other apps access it?
                  │         ├── Yes → Is it media?
                  │         │         ├── Yes → Use MediaStore
                  │         │         └── No → Use SAF
                  │         └── No → Use Internal Storage
                  └── No → Use Room with BLOB type
```
