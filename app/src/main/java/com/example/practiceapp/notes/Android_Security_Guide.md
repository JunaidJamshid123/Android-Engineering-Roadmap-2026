# Android Security — Complete In-Depth Guide

---

## 8. Security in Android

Android security is a **multi-layered defense system**. No single mechanism protects your app
— it's the combination of encryption, authentication, network hardening, permissions, and
data isolation that creates a secure application.

```
╔══════════════════════════════════════════════════════════════╗
║                ANDROID SECURITY LAYERS                       ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║   Layer 7 ── Application Security                            ║
║              Input validation, secure coding, obfuscation    ║
║                          │                                   ║
║   Layer 6 ── Authentication & Authorization                  ║
║              Biometrics, OAuth, JWT tokens                   ║
║                          │                                   ║
║   Layer 5 ── Data Encryption (at rest)                       ║
║              EncryptedSharedPrefs, EncryptedFile, Keystore   ║
║                          │                                   ║
║   Layer 4 ── Network Security (in transit)                   ║
║              TLS/HTTPS, cert pinning, security config        ║
║                          │                                   ║
║   Layer 3 ── Permission System                               ║
║              Runtime permissions, least privilege             ║
║                          │                                   ║
║   Layer 2 ── App Sandbox                                     ║
║              Process isolation, UID, SELinux, seccomp        ║
║                          │                                   ║
║   Layer 1 ── Hardware Security                               ║
║              TEE, StrongBox, Verified Boot, dm-verity        ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

> **Key Principle:** Security is about **defense in depth**. Even if one layer is breached,
> other layers continue to protect your data. Never rely on a single mechanism.

---

### Frequently Asked Interview Topics — Quick Map

```
╔════════════════════════════════════════════════════════════════════╗
║  TOPIC                           │ WHAT INTERVIEWERS ASK          ║
╠════════════════════════════════════════════════════════════════════╣
║  EncryptedSharedPreferences      │ How does it work internally?   ║
║                                  │ Can root user read it?         ║
║──────────────────────────────────────────────────────────────────║
║  Android Keystore + TEE          │ Where are keys stored?         ║
║                                  │ What if user changes biometric?║
║──────────────────────────────────────────────────────────────────║
║  BiometricPrompt + CryptoObject  │ Why CryptoObject over boolean? ║
║                                  │ Class 2 vs Class 3 biometric?  ║
║──────────────────────────────────────────────────────────────────║
║  OAuth 2.0 + PKCE                │ Why PKCE? What's code_verifier?║
║                                  │ Implicit vs Auth Code flow?    ║
║──────────────────────────────────────────────────────────────────║
║  JWT                             │ Signed vs Encrypted? Parts?    ║
║                                  │ Access vs Refresh token?       ║
║──────────────────────────────────────────────────────────────────║
║  Certificate Pinning             │ OkHttp vs XML config? Risks?   ║
║                                  │ What if cert expires?          ║
║──────────────────────────────────────────────────────────────────║
║  Runtime Permissions             │ shouldShowRationale() logic?   ║
║                                  │ What changed in API 33?        ║
║──────────────────────────────────────────────────────────────────║
║  R8/ProGuard                     │ What can/can't it protect?     ║
║                                  │ When do you need -keep rules?  ║
║──────────────────────────────────────────────────────────────────║
║  App Sandbox + Scoped Storage    │ How does UID isolation work?   ║
║                                  │ What changed in Android 10/11? ║
║──────────────────────────────────────────────────────────────────║
║  Content Provider Security       │ SQL injection? exported flag?  ║
║                                  │ FileProvider vs file:// URI?   ║
╚════════════════════════════════════════════════════════════════════╝
```

---

---

## 8.1 Encryption

### What is Encryption and Why Does It Matter?

Encryption transforms readable data (plaintext) into unreadable data (ciphertext) using a
mathematical algorithm and a secret key. Only someone with the correct key can reverse the
process (decryption).

```
                 ENCRYPTION                        DECRYPTION
                 
  "Hello"  ──────────────▶  "xK9$mQ@"  ──────────────▶  "Hello"
  (plaintext)    │ key       (ciphertext)    │ key        (plaintext)
                 │                            │
            ┌────┴─────┐                ┌────┴─────┐
            │ AES-256  │                │ AES-256  │
            │ Algorithm│                │ Algorithm│
            └──────────┘                └──────────┘
```

**Two types of encryption Android uses:**

| Type | Algorithm | How It Works | Used For |
|------|-----------|--------------|----------|
| **Symmetric** | AES-256 | Same key encrypts AND decrypts | Local data (files, prefs) |
| **Asymmetric** | RSA-2048 | Public key encrypts, private key decrypts | Key exchange, signatures |

**AES Modes Android uses:**

| Mode | Full Name | Deterministic? | Tamper Detection | Use Case |
|------|-----------|----------------|------------------|----------|
| **GCM** | Galois/Counter Mode | No (uses random IV) | Yes (auth tag) | Value encryption |
| **SIV** | Synthetic IV | Yes (same input = same output) | Yes | Key/lookup encryption |

> **Why GCM for values?** Each encryption produces different ciphertext even for the same input.
> An attacker can't tell if two encrypted values are the same.
>
> **Why SIV for keys?** SharedPreferences needs to look up keys by name. Deterministic encryption
> means the same key name always maps to the same encrypted key, so lookups work.

**Understanding IV (Initialization Vector) — the most asked crypto concept:**
```
  What is an IV?
    A random value used to ensure the SAME plaintext + SAME key
    produces DIFFERENT ciphertext each time.

  Why does GCM need an IV?
    Without IV:   AES("hello", key) → always "xK9$m"  ← attacker spots patterns
    With IV:      AES("hello", key, iv1) → "xK9$m"
                  AES("hello", key, iv2) → "pQ3#r"    ← different each time!

  IV Properties:
    • 12 bytes (96 bits) for GCM
    • NOT secret — can be stored alongside ciphertext
    • MUST be unique per encryption with the same key
    • NEVER reuse! Reusing IV+key = catastrophic — attacker can XOR
      two ciphertexts to cancel out the key and recover both plaintexts

  Where is IV stored?
    Typically prepended to the ciphertext:
    [12-byte IV][encrypted data][16-byte GCM auth tag]
    └─────────────────── stored together ────────────────┘
```

> **Interview Q: What's the difference between IV and Salt?**
>
> Both add randomness, but for different purposes:
> - **IV** = used with encryption (AES). Ensures same plaintext encrypts differently.
> - **Salt** = used with hashing (bcrypt, PBKDF2). Ensures same password hashes differently.
> - IV must be unique per encryption. Salt must be unique per user/password.
> - IV is stored with ciphertext. Salt is stored with the hash.

---

### Jetpack Security Library (androidx.security.crypto)

Google's official library that wraps the complexity of Android Keystore + Tink crypto library
into simple, hard-to-misuse APIs.

**Setup:**
```gradle
dependencies {
    implementation "androidx.security:security-crypto:1.1.0-alpha06"
}
```

**Architecture — How Jetpack Security works under the hood:**
```
┌─────────────────────────────────────────────────────────────┐
│                    YOUR APP CODE                             │
│                                                              │
│   EncryptedSharedPreferences.create(...)                     │
│   EncryptedFile.Builder(...)                                 │
└──────────────────────┬───────────────────────────────────────┘
                       │ calls
┌──────────────────────▼───────────────────────────────────────┐
│               JETPACK SECURITY LIBRARY                       │
│                                                              │
│   MasterKey ─── wraps ───▶ Tink Keyset                      │
│       │                        │                             │
│       │                   ┌────┴────────────┐                │
│       │                   │ AEAD primitive  │ (encrypt/decrypt)
│       │                   │ (AES-256-GCM)   │                │
│       │                   └─────────────────┘                │
│       │                                                      │
└───────┼──────────────────────────────────────────────────────┘
        │ stores master key in
┌───────▼──────────────────────────────────────────────────────┐
│                   ANDROID KEYSTORE                            │
│               (hardware-backed on supported devices)         │
│                                                              │
│   Master Key never leaves this boundary                      │
│   Tink sub-keys are encrypted BY master key                  │
│   and stored in app's SharedPreferences                      │
└──────────────────────────────────────────────────────────────┘

Tink = Google's open-source crypto library
AEAD = Authenticated Encryption with Associated Data
```

**The Master Key concept:**
- One **master key** is generated inside the Android Keystore (hardware-protected)
- This master key encrypts/decrypts **data encryption keys (DEKs)** managed by Tink
- The DEKs are stored encrypted in the app's SharedPreferences as a "keyset"
- This is called **envelope encryption** — the same pattern used by AWS KMS, GCP KMS

```
Envelope Encryption:

  ┌──────────────────────────────────────────────────────┐
  │                  Android Keystore                     │
  │   ┌─────────────────────────────┐                    │
  │   │  Master Key (AES-256-GCM)  │ ◄── never leaves   │
  │   └──────────────┬──────────────┘     hardware       │
  └──────────────────┼───────────────────────────────────┘
                     │ encrypts/decrypts
  ┌──────────────────▼───────────────────────────────────┐
  │   Encrypted Keyset (stored in SharedPreferences)     │
  │   ┌─────────────────────────────────┐                │
  │   │ DEK₁ (for file encryption)      │                │
  │   │ DEK₂ (for pref key encryption)  │                │
  │   │ DEK₃ (for pref value encryption)│                │
  │   └─────────────────────────────────┘                │
  └──────────────────────────────────────────────────────┘
                     │ DEKs encrypt
  ┌──────────────────▼───────────────────────────────────┐
  │   Your Actual Data (encrypted on disk)               │
  │   Files, SharedPreference entries, etc.              │
  └──────────────────────────────────────────────────────┘

WHY envelope encryption?
  - Master key never leaves hardware = can't be extracted
  - If you need to rotate keys, you only re-encrypt the keyset
    (not ALL your data)
  - Different DEKs for different purposes = compromise of one
    doesn't expose others
```

---

### EncryptedSharedPreferences

Encrypts both **keys** and **values** in SharedPreferences. Implements the standard
`SharedPreferences` interface, so it's a drop-in replacement.

```kotlin
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// Step 1: Create or retrieve the Master Key from Android Keystore
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

// Step 2: Create EncryptedSharedPreferences
// This looks exactly like regular SharedPreferences usage
val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",                                                  // file name
    masterKey,                                                       // master key
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,   // key encryption
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM  // value encryption
)

// Step 3: Use it like normal SharedPreferences — encryption is transparent
encryptedPrefs.edit()
    .putString("auth_token", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    .putString("user_email", "user@example.com")
    .putInt("login_count", 42)
    .apply()

// Reading — decryption happens automatically
val token = encryptedPrefs.getString("auth_token", null)
val email = encryptedPrefs.getString("user_email", null)
```

**What the file looks like on disk:**
```
┌──────────────────────────────────────────────────────────────┐
│  REGULAR SharedPreferences (data/data/com.app/shared_prefs/) │
│                                                              │
│  <?xml version='1.0' encoding='utf-8'?>                     │
│  <map>                                                       │
│    <string name="auth_token">eyJhbGci...</string>           │ ← Readable!
│    <string name="user_email">user@example.com</string>      │ ← Readable!
│    <int name="login_count" value="42" />                     │ ← Readable!
│  </map>                                                      │
│                                                              │
│  ❌ Anyone with root/ADB can read this file                  │
├──────────────────────────────────────────────────────────────┤
│  ENCRYPTED SharedPreferences                                 │
│                                                              │
│  <?xml version='1.0' encoding='utf-8'?>                     │
│  <map>                                                       │
│    <string name="AXppbmcgdGhl...">ARjY2x5IG5v...</string>  │ ← Gibberish
│    <string name="AXNlY3VyZSBr...">ATM2MTkgdGh...</string>  │ ← Gibberish
│    <string name="AXZlcnkgc2Vj...">AQo0LjIwMiB...</string>  │ ← Gibberish
│  </map>                                                      │
│                                                              │
│  ✅ Even with root access, data is unreadable without key    │
└──────────────────────────────────────────────────────────────┘
```

> **Interview Q: Can an attacker with root access read EncryptedSharedPreferences?**
>
> They can read the encrypted bytes, but they **cannot decrypt** them because the master key
> lives in the Android Keystore (TEE/StrongBox). The key never exists in memory as a plaintext
> byte array — all crypto operations happen inside the secure hardware.

> **Interview Q: EncryptedSharedPreferences vs regular SharedPreferences — when to use which?**
>
> | Criteria | Regular SharedPreferences | EncryptedSharedPreferences |
> |----------|--------------------------|----------------------------|
> | Performance | Fast (~1ms read) | Slower (~5-10ms, crypto overhead) |
> | Thread safety | Not thread-safe for writes | Same (use `apply()`) |
> | Data type | Primitives + String + Set | Same (transparent encryption) |
> | Use for | Theme, language, UI prefs | Tokens, emails, sensitive settings |
> | Backup risk | Leaked if backed up | Encrypted but key not backed up |
>
> **Rule of thumb:** If the data would be embarrassing/dangerous on a police report,
> use EncryptedSharedPreferences. Otherwise, regular is fine.

> **Interview Q: What happens during backup/restore with EncryptedSharedPreferences?**
>
> The encrypted XML file IS backed up, but the Android Keystore key is NOT.
> On a new device, the app has the ciphertext but no key to decrypt it → crash.
> **Fix:** Either exclude the file from backup, or catch the decryption exception
> and re-create the preferences (user logs in again).

---

### EncryptedFile

Encrypts entire files using **streaming AEAD** (Authenticated Encryption with Associated Data),
meaning files are encrypted in chunks so you can handle large files without loading everything
into memory.

```kotlin
import androidx.security.crypto.EncryptedFile
import java.io.File

val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val file = File(context.filesDir, "medical_records.dat")

val encryptedFile = EncryptedFile.Builder(
    context,
    file,
    masterKey,
    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
).build()

// Writing — transparently encrypts
encryptedFile.openFileOutput().use { outputStream ->
    outputStream.write("Patient SSN: 123-45-6789".toByteArray())
}

// Reading — transparently decrypts
encryptedFile.openFileInput().use { inputStream ->
    val content = inputStream.bufferedReader().readText()
    // content = "Patient SSN: 123-45-6789"
}
```

**How AES256_GCM_HKDF_4KB works internally:**
```
Original File:
  ┌─────────────────────────────────────────────────────┐
  │            Plain text content (any size)             │
  └─────────────────────────────────────────────────────┘

Encrypted File on Disk:
  ┌────────┬───────────────────┬───────────────────┬─────────────────┐
  │ Header │ Segment 1 (4KB)   │ Segment 2 (4KB)   │ Segment 3 (last)│
  │        │ + 16-byte GCM tag │ + 16-byte GCM tag │ + 16-byte tag   │
  └────────┴───────────────────┴───────────────────┴─────────────────┘
      │              │                   │                  │
      │              │                   │                  │
  Contains       Each segment        Segments are       Last segment
  keyset ID      encrypted with      independent —      may be < 4KB
  + metadata     a UNIQUE key        corrupting one
                 derived via HKDF    doesn't affect
                                     others

  HKDF (HMAC-based Key Derivation Function):
    Master DEK ───► HKDF(master_dek, segment_number) ───► Segment Key
    
    Each 4KB segment gets its OWN derived key, so:
    ✅ Streaming — don't need entire file in memory
    ✅ Random access — can decrypt any segment independently
    ✅ Tamper detection — GCM tag on each segment detects modification
    ✅ Truncation detection — missing segments are detected

  ⚠️ LIMITATION: File must NOT already exist when calling openFileOutput().
     Delete it first if re-writing: file.delete()
```

---

### Android Keystore System

The Keystore is the **foundation** of all Android encryption. It's a system service that
stores cryptographic keys in a **secure hardware module** (TEE or StrongBox) where they are
**never exposed** to the application processor or operating system.

```
╔═══════════════════════════════════════════════════════════════╗
║                 KEYSTORE ARCHITECTURE                         ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  ┌──────────────────────────────────────────────────┐        ║
║  │                YOUR APP (User Space)              │        ║
║  │                                                    │        ║
║  │  val keyGenerator = KeyGenerator.getInstance(      │        ║
║  │      "AES", "AndroidKeyStore"                      │        ║
║  │  )                                                 │        ║
║  │  // You NEVER see the actual key bytes!            │        ║
║  │  // You get an opaque reference (handle)           │        ║
║  └──────────────────────┬─────────────────────────────┘        ║
║                         │ Binder IPC                           ║
║  ┌──────────────────────▼─────────────────────────────┐        ║
║  │           KEYSTORE DAEMON (System Service)          │        ║
║  │           (keystore2 — runs as system process)      │        ║
║  │                                                     │        ║
║  │  • Manages key access control policies              │        ║
║  │  • Enforces user authentication requirements        │        ║
║  │  • Routes crypto operations to hardware             │        ║
║  └──────────────────────┬──────────────────────────────┘        ║
║                         │ HAL (Hardware Abstraction Layer)      ║
║  ┌──────────────────────▼──────────────────────────────┐        ║
║  │     TEE (Trusted Execution Environment)              │        ║
║  │     e.g., ARM TrustZone, Intel SGX                   │        ║
║  │                                                      │        ║
║  │  ┌────────────────────────────────────────────┐     │        ║
║  │  │  Secure World (isolated from Android OS)   │     │        ║
║  │  │                                            │     │        ║
║  │  │  • Keys stored in encrypted blob           │     │        ║
║  │  │  • Crypto ops execute HERE                 │     │        ║
║  │  │  • Even if Android is rooted/compromised,  │     │        ║
║  │  │    keys remain protected                   │     │        ║
║  │  └────────────────────────────────────────────┘     │        ║
║  └─────────────────────────────────────────────────────┘        ║
║                                                               ║
║  ┌─────────────────────────────────────────────────────┐        ║
║  │     StrongBox (Optional — dedicated secure chip)     │        ║
║  │     e.g., Titan M (Pixel), Samsung eSE               │        ║
║  │                                                      │        ║
║  │  • Separate CPU, memory, storage                    │        ║
║  │  • Physically isolated from main processor          │        ║
║  │  • Tamper-resistant hardware                        │        ║
║  │  • Higher security than TEE (but slower)            │        ║
║  └─────────────────────────────────────────────────────┘        ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝

TEE vs StrongBox:
┌─────────────┬──────────────────────┬──────────────────────┐
│ Feature     │ TEE                  │ StrongBox            │
├─────────────┼──────────────────────┼──────────────────────┤
│ Hardware    │ Same CPU, isolated   │ Separate chip        │
│             │ memory region        │ entirely             │
│ Speed       │ Fast                 │ Slower               │
│ Security    │ High                 │ Highest              │
│ Availability│ Most devices (API 23+│ Newer flagships only │
│             │)                     │ (API 28+)            │
│ Tamper      │ Software isolation   │ Physical tamper      │
│ resistance  │                      │ resistance           │
└─────────────┴──────────────────────┴──────────────────────┘
```

**Complete Keystore code — Generate, Encrypt, Decrypt:**
```kotlin
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreManager {

    private const val KEY_ALIAS = "app_encryption_key"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128 // bits

    /**
     * Generate a key inside the Keystore.
     * The key material NEVER leaves the TEE/StrongBox.
     */
    fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            // Security policies — enforced by hardware
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(true)  // Forces random IV (prevents replay)
            .build()

        keyGenerator.init(spec)
        keyGenerator.generateKey() // Key is created inside TEE, you get a reference
    }

    /**
     * Encrypt data. Returns IV + ciphertext concatenated.
     * IV is generated randomly by the hardware for each encryption.
     */
    fun encrypt(plainText: String): ByteArray {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        // cipher.iv → 12-byte random IV generated by hardware
        // NEVER reuse an IV with the same key!

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Prepend IV to ciphertext for storage
        // IV is NOT secret — it just must be unique
        return cipher.iv + cipherText
    }

    /**
     * Decrypt data. Extracts IV from the first 12 bytes.
     */
    fun decrypt(encryptedData: ByteArray): String {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey

        // Split IV (first 12 bytes) from ciphertext (rest)
        val iv = encryptedData.copyOfRange(0, 12)
        val cipherText = encryptedData.copyOfRange(12, encryptedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val plainBytes = cipher.doFinal(cipherText)
        return String(plainBytes, Charsets.UTF_8)
    }

    /** Check if a key already exists in the Keystore */
    fun keyExists(): Boolean {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        return keyStore.containsAlias(KEY_ALIAS)
    }
}
```

**Complete KeyGenParameterSpec options:**

| Property | What It Does | When to Use |
|----------|-------------|-------------|
| `setUserAuthenticationRequired(true)` | Key locked until biometric/PIN verified | Banking, health apps |
| `setUserAuthenticationValidityDurationSeconds(30)` | Key usable for 30s after auth | Frequent operations after one auth |
| `setUnlockedDeviceRequired(true)` | Key unusable on locked device | Prevent access if phone stolen while locked |
| `setIsStrongBoxBacked(true)` | Force dedicated secure chip | Highest security needs |
| `setInvalidatedByBiometricEnrollment(true)` | Wipe key if new fingerprint added | Detect biometric changes |
| `setRandomizedEncryptionRequired(true)` | Force random IV per encryption | Always (prevents replay attacks) |
| `setKeyValidityStart(date)` / `End(date)` | Time-bounded key validity | Temporary access tokens |

> **Interview Q: What happens if the user changes their screen lock?**
>
> Keys created with `setUserAuthenticationRequired(true)` are **invalidated** when the user
> removes their screen lock. The key becomes permanently unusable. Your app must handle
> `KeyPermanentlyInvalidatedException` and re-create the key + re-encrypt data.

> **Interview Q: Can you export/extract a key from Android Keystore?**
>
> **No.** That's the entire point. The Keystore is designed so that the key material
> **never leaves** the TEE/StrongBox boundary. You can use the key (encrypt, decrypt, sign)
> but you can never call `key.getEncoded()` and get the raw bytes — it returns `null`.
> This means even if Android OS is fully compromised (rooted), the key is safe.

> **Interview Q: What is attestation in Android Keystore?**
>
> Key Attestation lets your **server** verify that a key was genuinely created inside
> a real Android device's TEE/StrongBox. The Keystore generates an attestation
> certificate chain rooted in a Google-certified root CA. Your server can verify
> this chain to confirm:
> - Key was generated in real hardware (not an emulator)
> - Device has verified boot
> - OS version and patch level
> - Key properties (auth required, etc.)
>
> Used by banking apps to ensure the user isn't on a compromised device.

> **Interview Q: Keystore key rotation — how do you handle it?**
>
> 1. Generate a new key with a new alias
> 2. Decrypt existing data with the old key
> 3. Re-encrypt the data with the new key
> 4. Delete the old key: `keyStore.deleteEntry("old_alias")`
> 5. Never rotate in the middle of a write — use a transaction/flag to track state

---

---

## 8.2 Authentication

### Biometric Authentication (BiometricPrompt)

BiometricPrompt is the **unified API** for all biometric authentication on Android (fingerprint,
face, iris). It replaces the deprecated `FingerprintManager`.

**Why not use FingerprintManager?**
- `FingerprintManager` = fingerprint only, deprecated in API 28
- `BiometricPrompt` = fingerprint, face, iris — any biometric the device supports
- `BiometricPrompt` shows a **system-controlled UI** — your app can't fake or manipulate it
- The verification happens in the TEE — your app never sees the biometric data

```
╔════════════════════════════════════════════════════════════════╗
║          BIOMETRIC AUTHENTICATION ARCHITECTURE                 ║
╠════════════════════════════════════════════════════════════════╣
║                                                                ║
║  ┌─────────────────────────────────────┐                      ║
║  │           YOUR APP                   │                      ║
║  │                                      │                      ║
║  │  BiometricPrompt.authenticate(       │                      ║
║  │      promptInfo,                     │                      ║
║  │      cryptoObject  // optional       │                      ║
║  │  )                                   │                      ║
║  └───────────────┬─────────────────────┘                      ║
║                  │                                             ║
║  ┌───────────────▼─────────────────────┐                      ║
║  │     SYSTEM UI (BiometricPrompt)      │                      ║
║  │   ┌─────────────────────────────┐   │                      ║
║  │   │  ┌─────────────────────┐    │   │  Your app CANNOT     ║
║  │   │  │  Biometric Login    │    │   │  customize or        ║
║  │   │  │                     │    │   │  intercept this UI   ║
║  │   │  │  Place your finger  │    │   │                      ║
║  │   │  │  on the sensor      │    │   │  This prevents       ║
║  │   │  │                     │    │   │  phishing attacks    ║
║  │   │  │  [Use Password]     │    │   │                      ║
║  │   │  └─────────────────────┘    │   │                      ║
║  │   └─────────────────────────────┘   │                      ║
║  └───────────────┬─────────────────────┘                      ║
║                  │                                             ║
║  ┌───────────────▼─────────────────────┐                      ║
║  │     BIOMETRIC HAL (Hardware Layer)   │                      ║
║  │     Sensor captures biometric data   │                      ║
║  └───────────────┬─────────────────────┘                      ║
║                  │                                             ║
║  ┌───────────────▼─────────────────────┐                      ║
║  │     TEE (Trusted Execution Env)      │                      ║
║  │                                      │                      ║
║  │  • Biometric template stored here    │ ◄── Your app NEVER  ║
║  │  • Matching happens HERE             │     sees fingerprint ║
║  │  • Returns match/no-match result     │     data             ║
║  │  • If match: unlocks CryptoObject    │                      ║
║  └──────────────────────────────────────┘                      ║
║                                                                ║
╚════════════════════════════════════════════════════════════════╝
```

**Setup:**
```gradle
dependencies {
    implementation "androidx.biometric:biometric:1.2.0-alpha05"
}
```

**Complete Production Implementation:**
```kotlin
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthManager(private val activity: FragmentActivity) {

    // ─── STEP 1: Check device capability ───────────────────────────
    
    sealed class BiometricStatus {
        object Available : BiometricStatus()
        object NoHardware : BiometricStatus()
        object HardwareUnavailable : BiometricStatus()
        object NoneEnrolled : BiometricStatus()
    }

    fun checkBiometricStatus(): BiometricStatus {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NoneEnrolled
            else -> BiometricStatus.HardwareUnavailable
        }
    }

    // ─── STEP 2: Build BiometricPrompt with callback ───────────────
    
    fun authenticate(
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (Int, String) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(result)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // errorCode meanings:
                // ERROR_CANCELED (5)       → User pressed back
                // ERROR_USER_CANCELED (10) → User tapped negative button
                // ERROR_LOCKOUT (7)        → Too many attempts (30s lockout)
                // ERROR_LOCKOUT_PERMANENT (9) → Too many lockouts (need PIN)
                // ERROR_NO_SPACE (4)       → Not enough storage
                // ERROR_TIMEOUT (3)        → Sensor timed out
                onError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Biometric was recognized but didn't match
                // User can retry — this is NOT a terminal error
                onFailed()
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        // ─── STEP 3: Configure what the user sees ─────────────────

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify Identity")
            .setSubtitle("Authenticate to access your account")
            .setNegativeButtonText("Use Password Instead")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG)
            .setConfirmationRequired(true)  // Implicit vs Explicit auth
            .build()

        // STEP 4: Launch the system biometric dialog
        biometricPrompt.authenticate(promptInfo)
    }
}
```

```
Authenticator Types — What each allows:
┌──────────────────────────┬──────────────┬──────────┬──────────┐
│ Type                     │ Fingerprint  │ Face/Iris│ PIN/Pass │
├──────────────────────────┼──────────────┼──────────┼──────────┤
│ BIOMETRIC_STRONG         │ ✅           │ ✅ *     │ ❌       │
│ BIOMETRIC_WEAK           │ ✅           │ ✅       │ ❌       │
│ DEVICE_CREDENTIAL        │ ❌           │ ❌       │ ✅       │
│ BIOMETRIC_STRONG |       │ ✅           │ ✅ *     │ ✅       │
│   DEVICE_CREDENTIAL      │              │          │(fallback)│
└──────────────────────────┴──────────────┴──────────┴──────────┘
* Only if device's face unlock is Class 3 (strong)

Confirmation Required:
  true  = "Explicit auth"  → user must tap confirm after scan (safer)
  false = "Implicit auth"  → success fires immediately (faster, for low-risk)
```

**Biometric + CryptoObject — The Cryptographically Secure Way:**

Without CryptoObject, biometric auth is just a "trust me" signal — the OS says "user
authenticated" but there's no cryptographic proof. A rooted device could fake this.

With CryptoObject, the biometric auth **unlocks a specific cryptographic key in the TEE**.
The key literally cannot be used until the biometric matches. This is unfakeable.

```kotlin
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class BiometricCryptoManager(private val activity: FragmentActivity) {

    companion object {
        private const val KEY_ALIAS = "biometric_bound_key"
    }

    // ─── Generate a key that REQUIRES biometric authentication ─────

    fun generateBiometricBoundKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(true) // ← KEY LINE: requires biometric
                .setInvalidatedByBiometricEnrollment(true) // invalidate if biometrics change
                .build()
        )
        keyGenerator.generateKey()
    }

    // ─── Create a CryptoObject containing an initialized Cipher ────

    fun getCryptoObject(): BiometricPrompt.CryptoObject? {
        return try {
            val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
                .apply { load(null) }
            val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            // This init() call will THROW UserNotAuthenticatedException
            // if biometric auth hasn't happened yet.
            // That's why we pass this cipher to BiometricPrompt —
            // after successful biometric auth, the cipher becomes usable.

            BiometricPrompt.CryptoObject(cipher)
        } catch (e: Exception) {
            // Key invalidated (biometric changed) or not yet created
            null
        }
    }

    // ─── Authenticate with crypto proof ────────────────────────────

    fun authenticateWithCrypto(
        onSuccess: (Cipher) -> Unit,
        onError: (String) -> Unit
    ) {
        val cryptoObject = getCryptoObject()
        if (cryptoObject == null) {
            onError("Biometric key unavailable — re-enroll required")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // result.cryptoObject?.cipher is now UNLOCKED by the TEE
                // Use it to encrypt/decrypt sensitive data
                val unlockedCipher = result.cryptoObject?.cipher
                if (unlockedCipher != null) {
                    onSuccess(unlockedCipher)
                } else {
                    onError("CryptoObject was null after auth")
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirm Transaction")
            .setSubtitle("Authenticate to authorize payment")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG)
            .build()

        BiometricPrompt(activity, executor, callback)
            .authenticate(promptInfo, cryptoObject)
    }
}

// ─── Usage in Activity ──────────────────────────────────────────

class PaymentActivity : AppCompatActivity() {
    private lateinit var cryptoManager: BiometricCryptoManager

    fun onPayButtonClick(paymentData: String) {
        cryptoManager.authenticateWithCrypto(
            onSuccess = { cipher ->
                // Cipher is now unlocked — encrypt the payment data
                val encrypted = cipher.doFinal(paymentData.toByteArray())
                sendEncryptedPayment(encrypted, cipher.iv)
            },
            onError = { message ->
                showError("Authentication failed: $message")
            }
        )
    }
}
```

```
Why CryptoObject matters — Attack comparison:

WITHOUT CryptoObject (weak):
  ┌────────┐    "auth ok"     ┌──────────┐
  │Biometric├───────────────▶ │ App code │──▶ encrypt with any key
  │ Prompt  │  just a boolean │          │   (key not tied to auth)
  └────────┘                  └──────────┘
  
  ⚠️ On rooted device, attacker can call onAuthenticationSucceeded() directly
     and skip the actual biometric check. The key works without real auth.

WITH CryptoObject (strong):
  ┌────────┐   unlocks key    ┌────────┐    cipher ready    ┌──────────┐
  │Biometric├───in TEE────────▶│Keystore├──────────────────▶ │ App code │
  │ Prompt  │                 │  (TEE) │                    │          │
  └────────┘                  └────────┘                    └──────────┘
  
  ✅ Even on rooted device, the cipher CANNOT work without real biometric.
     The key is locked inside secure hardware until TEE confirms the match.
     There is no software bypass.
```

> **Interview Q: What are Biometric Classes (Class 1/2/3)?**
>
> Android categorizes biometric sensors by security strength:
>
> | Class | Name | Spoofability | Can Use with CryptoObject? | Example |
> |-------|------|-------------|---------------------------|----------|
> | Class 3 | Strong | Very hard to spoof | ✅ Yes | Fingerprint, 3D Face (Pixel, iPhone-style) |
> | Class 2 | Weak | Easier to spoof | ❌ No | 2D Face unlock (photo can trick it) |
> | Class 1 | Convenience | Easy to spoof | ❌ No | Basic face detection |
>
> `BIOMETRIC_STRONG` = Class 3 only. `BIOMETRIC_WEAK` = Class 2 and above.
> For banking/payment apps, always use `BIOMETRIC_STRONG`.

> **Interview Q: What's the difference between `onAuthenticationFailed()` and `onAuthenticationError()`?**
>
> - **`onAuthenticationFailed()`** = biometric was recognized but **didn't match**.
>   User can retry. Called each failed attempt. NOT a terminal state.
> - **`onAuthenticationError()`** = something went **wrong** and auth can't continue.
>   Terminal state. Causes: too many attempts (lockout), user cancelled,
>   hardware error, no biometrics enrolled.
>
> Common mistake: treating `onAuthenticationFailed()` as final and closing the dialog.
> The system keeps the dialog open automatically for retries.

> **Interview Q: What happens when the user adds a new fingerprint?**
>
> If you created the key with `setInvalidatedByBiometricEnrollment(true)`,
> the key is **permanently invalidated**. Next time you try to init the Cipher,
> it throws `KeyPermanentlyInvalidatedException`. You must:
> 1. Catch the exception
> 2. Delete the old key
> 3. Generate a new key
> 4. Ask the user to re-authenticate / re-encrypt their data

---

### OAuth 2.0 Implementation

OAuth 2.0 is a **delegation protocol** — it lets users grant your app limited access to their
data on another service (Google, Facebook, etc.) **without sharing their password**.

```
╔═══════════════════════════════════════════════════════════════════╗
║              WHY OAuth EXISTS — The Problem                       ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  OLD WAY (Dangerous):                                            ║
║    User gives their Google password ──▶ Your App ──▶ Google API  ║
║    ❌ Your app has FULL access to their Google account            ║
║    ❌ If your app is hacked, attacker gets their Google password  ║
║    ❌ User can't revoke without changing password                 ║
║                                                                   ║
║  OAUTH WAY (Safe):                                               ║
║    Your App ──▶ Redirect to Google ──▶ User logs in DIRECTLY     ║
║                                        with Google               ║
║    Google gives Your App a LIMITED TOKEN (not password):          ║
║    ✅ Token only grants specific permissions (scopes)            ║
║    ✅ Token expires automatically                                ║
║    ✅ User can revoke token without changing password             ║
║    ✅ Your app never sees the password                            ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

**OAuth 2.0 Authorization Code + PKCE Flow (Recommended for mobile):**

```
┌─────────┐         ┌──────────────┐         ┌──────────────┐
│ Your App│         │System Browser│         │ Auth Server  │
│         │         │ (Chrome)     │         │ (Google)     │
└────┬────┘         └──────┬───────┘         └──────┬───────┘
     │                      │                        │
     │ 1. Generate random                           │
     │    code_verifier                              │
     │    (e.g., "dBjftJeZ4CVP")                    │
     │                                               │
     │ 2. Hash it to create                          │
     │    code_challenge                             │
     │    SHA256("dBjftJeZ4CVP") = "E9Melhoa2OwvFr"  │
     │                                               │
     │ 3. Open browser ───▶│                         │
     │    with auth URL +   │                         │
     │    code_challenge    │ 4. User sees ──────────▶│
     │                     │    Google login          │
     │                     │    page                  │
     │                     │                          │
     │                     │    5. User enters        │
     │                     │    credentials           │
     │                     │    (DIRECTLY to Google)  │
     │                     │                          │
     │                     │    6. User consents      │
     │                     │    to requested scopes   │
     │                     │                          │
     │                     │◀── 7. Google redirects ──│
     │                     │    with auth_code         │
     │◀── 8. Deep link ───│    "code=abc123"          │
     │    redirect to app  │                          │
     │                     │                          │
     │ 9. Exchange auth_code + code_verifier ────────▶│
     │    (POST to token endpoint)                    │
     │                                                │
     │    Google verifies:                            │
     │    SHA256(code_verifier) == code_challenge?    │
     │    ✅ Match! This is the same app that         │
     │       started the flow                        │
     │                                                │
     │◀── 10. Returns tokens ─────────────────────────│
     │    {                                           │
     │      "access_token": "ya29.xxx",               │
     │      "refresh_token": "1//0eXxx",              │
     │      "expires_in": 3600,                       │
     │      "id_token": "eyJhbGci..."                 │
     │    }                                           │
     │                                                │
     │ 11. Use access_token                           │
     │     for API calls                              │
     └────────────────────────────────────────────────┘

PKCE (Proof Key for Code Exchange) — WHY it's required:

  Without PKCE, a malicious app could:
  1. Register the same redirect URI (deep link hijacking)
  2. Intercept the auth_code from step 8
  3. Exchange the stolen code for tokens

  With PKCE:
  - Only the app that created code_verifier can exchange the code
  - The malicious app doesn't know the original code_verifier
  - The auth server rejects their request
```

> **Interview Q: What are the different OAuth 2.0 grant types?**
>
> | Grant Type | Use Case | Mobile? | Security |
> |------------|----------|---------|----------|
> | Authorization Code + PKCE | Mobile & SPA apps | ✅ Recommended | Highest |
> | Authorization Code | Server-side web apps | ❌ Needs client_secret | High |
> | Implicit (DEPRECATED) | Old SPAs | ❌ Token exposed in URL | Low |
> | Client Credentials | Server-to-server (no user) | ❌ No user context | High |
> | Resource Owner Password | Legacy (user gives password) | ❌ Anti-pattern | Lowest |
>
> **For Android: ALWAYS use Authorization Code + PKCE.**
> Implicit grant is deprecated because the access token appears in the URL fragment
> and can be leaked via browser history, referrer headers, and proxy logs.

> **Interview Q: Why use a system browser instead of a WebView for OAuth?**
>
> - **WebView** = your app controls the view. You COULD inject JavaScript to steal
>   the user's Google password. Google specifically blocks OAuth login in WebViews.
> - **System Browser (Chrome Custom Tabs)** = separate process. Your app CANNOT
>   see what the user types. Credentials go directly to Google.
>   Also, the user's existing Google session may already be active → SSO.

> **Interview Q: What is the difference between Access Token and Refresh Token?**
>
> | Property | Access Token | Refresh Token |
> |----------|-------------|---------------|
> | Purpose | Authorize API requests | Get new access tokens |
> | Lifetime | Short (15min – 1hr) | Long (days – months) |
> | Sent to | Resource server (API) | Auth server ONLY |
> | Format | Often JWT | Opaque string |
> | Revocation | Hard (need token blacklist) | Easy (delete from DB) |
> | If stolen | Limited damage (expires soon) | Full damage (new tokens) |
>
> **Why two tokens?** If we only had one long-lived token and it got stolen,
> the attacker has access forever. With a short-lived access token, even if
> stolen, it expires quickly. The refresh token is only sent to the auth server
> (one endpoint), reducing its exposure surface.

**Using AppAuth Library (Google-recommended):**
```gradle
dependencies {
    implementation "net.openid:appauth:0.11.1"
}
```

```kotlin
class OAuthManager(private val context: Context) {

    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
        Uri.parse("https://oauth2.googleapis.com/token")
    )

    // Build and launch authorization request
    fun createAuthRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfig,
            "YOUR_CLIENT_ID.apps.googleusercontent.com",
            ResponseTypeValues.CODE,                        // Authorization Code flow
            Uri.parse("com.example.app:/oauth2redirect")    // Your redirect URI
        )
            .setScope("openid profile email")
            .setCodeVerifier(CodeVerifierUtil.generateRandomCodeVerifier()) // PKCE
            .build()
    }

    fun startAuth(activity: Activity, requestCode: Int) {
        val authService = AuthorizationService(context)
        val authIntent = authService.getAuthorizationRequestIntent(createAuthRequest())
        activity.startActivityForResult(authIntent, requestCode)
    }

    // Handle the redirect callback
    fun handleAuthResponse(
        data: Intent?,
        onTokensReceived: (accessToken: String, refreshToken: String?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (data == null) {
            onError("No response data")
            return
        }

        val response = AuthorizationResponse.fromIntent(data)
        val exception = AuthorizationException.fromIntent(data)

        when {
            exception != null -> onError(exception.errorDescription ?: "Auth failed")
            response != null -> exchangeCodeForTokens(response, onTokensReceived, onError)
            else -> onError("Unknown error")
        }
    }

    private fun exchangeCodeForTokens(
        response: AuthorizationResponse,
        onTokensReceived: (String, String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val authService = AuthorizationService(context)
        authService.performTokenRequest(
            response.createTokenExchangeRequest()
        ) { tokenResponse, exception ->
            if (tokenResponse != null) {
                // Store these tokens in EncryptedSharedPreferences!
                val accessToken = tokenResponse.accessToken ?: ""
                val refreshToken = tokenResponse.refreshToken
                onTokensReceived(accessToken, refreshToken)
            } else {
                onError(exception?.errorDescription ?: "Token exchange failed")
            }
        }
    }
}
```

---

### JWT Token Management

JWT (JSON Web Token) is a compact, URL-safe token format used for transmitting claims
between parties.

```
JWT Structure — Three Base64URL-encoded parts separated by dots:

  eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0NSJ9.dGhpcyBpcyBhIHNpZ25hdHVyZQ
  \_______________________/ \____________________/ \__________________________/
         HEADER                    PAYLOAD                  SIGNATURE

  ┌─────────── HEADER ───────────┐
  │ {                             │
  │   "alg": "RS256",            │  ← Signing algorithm (RSA + SHA-256)
  │   "typ": "JWT",              │  ← Token type
  │   "kid": "key-id-123"        │  ← Which key to verify with
  │ }                             │
  └───────────────────────────────┘

  ┌─────────── PAYLOAD (Claims) ──┐
  │ {                              │
  │   "sub": "user_12345",        │  ← Subject (who this token is about)
  │   "iss": "auth.example.com",  │  ← Issuer (who created the token)
  │   "aud": "api.example.com",   │  ← Audience (who should accept it)
  │   "exp": 1700000000,          │  ← Expiration (Unix timestamp)
  │   "iat": 1699996400,          │  ← Issued At
  │   "scope": "read write"       │  ← Permissions granted
  │ }                              │
  └────────────────────────────────┘

  ┌─────────── SIGNATURE ─────────┐
  │                                │
  │  RS256(                        │
  │    base64(header) + "." +      │
  │    base64(payload),            │
  │    server_private_key          │  ← Only server can create this
  │  )                             │
  │                                │
  │  Verifiable with server's      │
  │  PUBLIC key (no shared secret) │
  └────────────────────────────────┘

  ⚠️ JWTs are NOT encrypted! They are SIGNED.
     Anyone can read the payload (it's just Base64).
     The signature ensures it hasn't been tampered with.
     Don't put passwords or secrets in JWT claims.
```

> **Interview Q: Can you modify a JWT token on the client?**
>
> You can decode and read it (it's just Base64), but if you **modify** anything
> (even one character), the signature becomes invalid. The server will reject it.
> Exception: if the alg is "none" (a known vulnerability in misconfigured servers).

> **Interview Q: Should the Android client verify the JWT signature?**
>
> **No.** Signature verification should happen **server-side**. The client received
> the token over HTTPS from a trusted server, so it can trust the content.
> The client only reads claims for UI decisions (show name, check expiry).
> Auth decisions ("is this user authorized?") always happen on the server.

> **Interview Q: What is token sliding expiration?**
>
> Each time the user makes an API call, the server issues a FRESH access token
> with a reset expiry. Active users never get logged out. Inactive users
> get logged out after the token expires. Alternative: just use refresh tokens.

**Production TokenManager:**
```kotlin
import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

class TokenManager private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "auth_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"

        @Volatile
        private var instance: TokenManager? = null

        // Thread-safe singleton — tokens should be managed centrally
        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // Tokens are stored encrypted
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    val isLoggedIn: Boolean
        get() = accessToken != null

    /**
     * Decode JWT payload without verifying signature.
     * NOTE: Signature verification should happen SERVER-SIDE.
     * Client only reads claims for UI decisions (never for auth decisions).
     */
    fun getTokenClaims(): JSONObject? {
        val token = accessToken ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
            JSONObject(String(payloadBytes, Charsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }

    /** Check if access token is expired (with 60s buffer) */
    fun isAccessTokenExpired(): Boolean {
        val claims = getTokenClaims() ?: return true
        val exp = claims.optLong("exp", 0L)
        val nowSeconds = System.currentTimeMillis() / 1000
        return nowSeconds >= (exp - 60) // 60s buffer to avoid edge-case expiry during request
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
```

**Automatic Token Refresh with OkHttp Authenticator:**
```kotlin
import okhttp3.*
import java.io.IOException

/**
 * OkHttp Authenticator — automatically refreshes tokens on 401 responses.
 *
 * WHY Authenticator instead of Interceptor?
 * - Interceptor: runs on EVERY request (proactive)
 * - Authenticator: runs ONLY when server returns 401 (reactive)
 * - Authenticator is the correct pattern — let the server tell us when to refresh
 */
class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val refreshApi: RefreshApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite refresh loops — if we already retried, give up
        if (responseCount(response) >= 2) {
            tokenManager.clearAll()
            return null // Returning null = give up, propagate 401 to caller
        }

        synchronized(this) {
            // Another thread may have already refreshed — check if token changed
            val currentToken = tokenManager.accessToken
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            if (currentToken != null && currentToken != requestToken) {
                // Token was already refreshed by another thread — retry with new token
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Actually refresh the token
            val refreshToken = tokenManager.refreshToken ?: return null

            val refreshResponse = refreshApi
                .refreshToken(RefreshRequest(refreshToken))
                .execute()

            return if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body()!!
                tokenManager.accessToken = body.accessToken
                tokenManager.refreshToken = body.refreshToken

                // Retry original request with new token
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${body.accessToken}")
                    .build()
            } else {
                tokenManager.clearAll() // Refresh failed — force re-login
                null
            }
        }
    }

    /** Count how many times this response chain has been through authenticate() */
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

// ─── Add an Interceptor to attach the token to every request ────

class AuthHeaderInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        tokenManager.accessToken?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}

// ─── Wire it all together ───────────────────────────────────────

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(AuthHeaderInterceptor(tokenManager))      // Adds token
    .authenticator(TokenAuthenticator(tokenManager, refreshApi)) // Refreshes on 401
    .build()
```

**Secure Token Storage Comparison:**
```
┌──────────────────────────┬──────────┬─────────────────────────────────────┐
│ Storage Method           │ Security │ Analysis                            │
├──────────────────────────┼──────────┼─────────────────────────────────────┤
│ Plain SharedPreferences  │ ❌ Bad   │ XML file readable with root/ADB    │
│ Room/SQLite              │ ❌ Bad   │ .db file can be copied & read      │
│ DataStore                │ ❌ Bad   │ Proto/prefs file is plaintext      │
│ Hardcoded in code        │ ❌ Worst │ Extractable via APK decompilation  │
│ EncryptedSharedPrefs     │ ✅ Good  │ AES-256 encrypted, keys in TEE     │
│ Android Keystore direct  │ ✅ Best  │ Keys never leave secure hardware   │
│ In-memory only           │ ✅ Good  │ Lost on process death (no persist) │
└──────────────────────────┴──────────┴─────────────────────────────────────┘

ABSOLUTE RULES for tokens:
  ❌ NEVER hardcode tokens/keys in source code
  ❌ NEVER store tokens in external storage
  ❌ NEVER log tokens (Log.d, Timber, etc.)
  ❌ NEVER pass tokens in URL query parameters (?token=xxx)
  ❌ NEVER store tokens in plain-text databases
  ✅ ALWAYS use EncryptedSharedPreferences or Keystore
  ✅ ALWAYS transmit tokens over HTTPS only
  ✅ ALWAYS check token expiry before use
```

---

---

## 8.3 Network Security

### Why Network Security Matters

Your app sends data over networks that YOU DON'T CONTROL. Coffee shop WiFi, hotel networks,
cellular connections — all of these can be intercepted.

```
THE THREAT: Man-in-the-Middle (MitM) Attack

  Normal secure connection:
  ┌──────┐  TLS/HTTPS  ┌──────────┐
  │ App  ├─────────────▶│ Server   │
  └──────┘  encrypted   └──────────┘

  MitM attack (public WiFi):
  ┌──────┐             ┌──────────┐             ┌──────────┐
  │ App  ├────────────▶│ Attacker ├────────────▶│ Server   │
  └──────┘             └──────────┘             └──────────┘
     │                      │
     │   App thinks it's    │   Attacker sees ALL
     │   talking to server  │   traffic in plaintext
     │                      │   (credentials, tokens, data)

  Defenses:
  1. HTTPS        → Encrypts traffic (stops passive eavesdropping)
  2. Cert Pinning → Stops MitM even with compromised CAs
  3. Network Config → Prevents accidental HTTP, controls trust
```

**TLS/HTTPS Handshake — What happens when your app connects to a server:**
```
  App (OkHttp)                                      Server
      │                                                │
      │ 1. ClientHello                                 │
      ├────────────────────────────────────────────────▶│
      │    "I support TLS 1.3, these cipher suites,     │
      │     and here's my random number"               │
      │                                                │
      │ 2. ServerHello + Certificate                    │
      │◀────────────────────────────────────────────────┤
      │    "I chose TLS 1.3 + AES-256-GCM.              │
      │     Here's my certificate (contains public key) │
      │     and my random number"                       │
      │                                                │
      │ 3. App VERIFIES certificate:                    │
      │    a) Is cert signed by a trusted CA?           │
      │    b) Does cert's domain match the URL?         │
      │    c) Is cert not expired?                      │
      │    d) Is cert's public key pinned? (if pinning)  │
      │                                                │
      │ 4. Key Exchange (Diffie-Hellman in TLS 1.3)     │
      ├────────────────────────────────────────────────▶│
      │◀────────────────────────────────────────────────┤
      │    Both sides now have a shared secret           │
      │    (without ever sending it over the wire!)      │
      │                                                │
      │ 5. Symmetric encryption begins (AES-256-GCM)   │
      ├════════════encrypted data══════════════════════▶│
      │◀════════════encrypted data══════════════════════┤
      │                                                │

  Key insight for interviews:
    • Asymmetric crypto (RSA/DH) is used ONLY for the handshake (key exchange)
    • Symmetric crypto (AES) is used for ALL actual data transfer
    • Why? AES is ~1000x faster than RSA. Asymmetric is only needed
      to securely agree on a shared AES key.
    • TLS 1.3 completes the handshake in 1 round-trip (1-RTT)
      vs TLS 1.2 which needed 2 round-trips (2-RTT)
```

> **Interview Q: What does HTTPS protect against and what doesn't it?**
>
> | Protected ✅ | NOT Protected ❌ |
> |------------|------------------|
> | Data confidentiality (encrypted) | Server-side leaks |
> | Data integrity (tamper detection) | Certificate authority compromise |
> | Server identity (cert verification) | Client identity (server doesn't know who you are) |
> | Replay prevention (TLS nonces) | Traffic analysis (attacker sees packet sizes/timing) |

---

### Certificate Pinning

By default, your app trusts **~150 Certificate Authorities (CAs)** installed on the device.
If ANY of these CAs is compromised (or if a user installs a rogue CA), an attacker can
create "valid" certificates for your domain and intercept all traffic.

Certificate pinning says: **"I only trust THIS specific certificate/key for my API domain."**

```
Without Pinning (default trust):
  ┌──────────────────────────────────────────────────────────┐
  │ Device Trust Store: ~150 CAs                              │
  │                                                          │
  │  DigiCert    Comodo    Let's Encrypt    Symantec   ...   │
  │  GlobalSign  GoDaddy   Entrust         VeriSign   ...   │
  │                                                          │
  │  ANY of these can issue a cert for "api.yourapp.com"     │
  │  ❌ If one CA is compromised → attacker gets valid cert   │
  └──────────────────────────────────────────────────────────┘

With Pinning:
  ┌──────────────────────────────────────────────────────────┐
  │ App trusts ONLY:                                         │
  │                                                          │
  │  ✅ SHA-256 hash of YOUR server's public key             │
  │  ✅ SHA-256 hash of backup key                           │
  │                                                          │
  │  ❌ All other CAs are ignored for api.yourapp.com        │
  │  ❌ Compromised CA → still can't issue trusted cert      │
  └──────────────────────────────────────────────────────────┘

  What gets pinned — Certificate vs Public Key:
  ┌─────────────────┬──────────────────────────────────────┐
  │ Pin Type        │ Trade-offs                            │
  ├─────────────────┼──────────────────────────────────────┤
  │ Certificate pin │ Must update app when cert renews      │
  │                 │ (certs expire every 1-2 years)        │
  │ Public key pin  │ ✅ Survives cert renewal if same key  │
  │ (RECOMMENDED)   │ Only breaks if you change server key  │
  └─────────────────┴──────────────────────────────────────┘
```

**Method 1: Network Security Configuration (recommended by Google):**
```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>

    <!-- Default: block cleartext, trust only system CAs -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>

    <!-- Pin certificates for your API domain -->
    <domain-config>
        <domain includeSubdomains="true">api.yourapp.com</domain>
        <pin-set expiration="2027-01-01">
            <!-- Primary pin: SHA-256 hash of your server's public key -->
            <pin digest="SHA-256">YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=</pin>
            <!-- Backup pin: hash of a DIFFERENT key (disaster recovery) -->
            <pin digest="SHA-256">sRHdihwgkaib1P1gN7SkKPuHkDN5bME8a4lRfCA/nnM=</pin>
        </pin-set>
    </domain-config>

    <!-- Debug only: trust user-installed CAs for proxy tools -->
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
            <certificates src="system" />
        </trust-anchors>
    </debug-overrides>

</network-security-config>
```

```xml
<!-- AndroidManifest.xml — register the config -->
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ... >
```

**Method 2: OkHttp CertificatePinner (programmatic):**
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add(
        "api.yourapp.com",
        "sha256/YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=" // Primary
    )
    .add(
        "api.yourapp.com",
        "sha256/sRHdihwgkaib1P1gN7SkKPuHkDN5bME8a4lRfCA/nnM=" // Backup
    )
    .build()

val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

```
How to get the pin hash:

  $ openssl s_client -connect api.yourapp.com:443 | \
    openssl x509 -pubkey -noout | \
    openssl pkey -pubin -outform der | \
    openssl dgst -sha256 -binary | \
    openssl enc -base64

  ⚠️ ALWAYS have at least 2 pins (primary + backup)
     If you lose access to the primary key and have no backup pin,
     ALL users are locked out until they update the app.
```

> **Interview Q: What happens when your pinned certificate expires?**
>
> If you pinned the **certificate itself**, the app stops working when the cert
> renews. All users must update the app. If you pinned the **public key**,
> and the server renews the cert with the **same key pair**, nothing breaks.
>
> **Best practice:**
> 1. Always pin the public key, not the full certificate
> 2. Always include a backup pin (different key)
> 3. Set a `pin-set expiration` date
> 4. Monitor cert expiry and push app updates before expiry
> 5. Have a server-side kill switch to disable pinning in emergencies

> **Interview Q: Certificate Pinning — OkHttp vs Network Security Config?**
>
> | Feature | OkHttp CertificatePinner | Network Security Config (XML) |
> |---------|--------------------------|-------------------------------|
> | Scope | Only OkHttp requests | ALL network traffic (WebView, etc.) |
> | Debug override | Manual code | Built-in `<debug-overrides>` |
> | Expiration | No built-in | `expiration` attribute |
> | Hot update | Change in code (redeploy) | Change in XML (rebuild) |
> | Recommendation | Use for fine control | **Preferred by Google** |

---

### Network Security Configuration — Complete Reference

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>

    <!-- ╔═══════════════════════════════════════════════╗ -->
    <!-- ║ BASE CONFIG — applies to ALL network traffic  ║ -->
    <!-- ╚═══════════════════════════════════════════════╝ -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
            <!-- 
              src="system"  → Trust OS-installed CAs (Google, DigiCert, etc.)
              src="user"    → Trust user-installed CAs (Charles Proxy, corporate)
              src="@raw/ca" → Trust a specific CA cert bundled in your APK
            -->
        </trust-anchors>
    </base-config>

    <!-- ╔═══════════════════════════════════════════════╗ -->
    <!-- ║ DOMAIN CONFIG — overrides for specific hosts  ║ -->
    <!-- ╚═══════════════════════════════════════════════╝ -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.yourapp.com</domain>
        <domain includeSubdomains="true">cdn.yourapp.com</domain>

        <!-- Certificate pinning -->
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">base64_hash_primary=</pin>
            <pin digest="SHA-256">base64_hash_backup=</pin>
        </pin-set>

        <!-- Custom trust (e.g., your own internal CA) -->
        <trust-anchors>
            <certificates src="system" />
            <certificates src="@raw/internal_ca" />
        </trust-anchors>
    </domain-config>

    <!-- ╔═══════════════════════════════════════════════╗ -->
    <!-- ║ DEBUG OVERRIDES — ONLY in debuggable builds   ║ -->
    <!-- ╚═══════════════════════════════════════════════╝ -->
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />  <!-- Trust Charles/Fiddler proxy CA -->
            <certificates src="system" />
        </trust-anchors>
    </debug-overrides>

</network-security-config>
```

```
Hierarchy — How configs are resolved:

  Request to "api.yourapp.com"
       │
       ▼
  Does a <domain-config> match?
       │
  ┌────┴────┐
  Yes       No
  │         │
  ▼         ▼
  Use       Use <base-config>
  domain-   (global defaults)
  config
       │
       ▼
  Is app debuggable AND <debug-overrides> exists?
       │
  ┌────┴────┐
  Yes       No
  │         │
  ▼         ▼
  MERGE     Use config
  debug     as-is
  overrides
  into config
```

---

### HTTPS Enforcement — Multiple Layers

```kotlin
// ╔════════════════════════════════════════════════════════════╗
// ║ Layer 1: Network Security Config (already shown above)    ║
// ║ cleartextTrafficPermitted="false"                         ║
// ║ This is the BEST method — OS-level enforcement            ║
// ╚════════════════════════════════════════════════════════════╝

// ╔════════════════════════════════════════════════════════════╗
// ║ Layer 2: Manifest attribute (simpler, less granular)      ║
// ╚════════════════════════════════════════════════════════════╝
// In AndroidManifest.xml:
// <application android:usesCleartextTraffic="false" ... >

// ╔════════════════════════════════════════════════════════════╗
// ║ Layer 3: StrictMode — detect HTTP in debug builds         ║
// ╚════════════════════════════════════════════════════════════╝
if (BuildConfig.DEBUG) {
    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectCleartextNetwork()  // Log/crash on any HTTP call
            .penaltyLog()
            .penaltyDeath()            // Crash immediately (find it early)
            .build()
    )
}

// ╔════════════════════════════════════════════════════════════╗
// ║ Layer 4: OkHttp Interceptor — application-level check     ║
// ╚════════════════════════════════════════════════════════════╝
val httpsEnforcingClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request()
        require(request.url.scheme == "https") {
            "Cleartext HTTP traffic to ${request.url.host} is not permitted"
        }
        chain.proceed(request)
    }
    .build()
```

---

### ProGuard / R8 — Code Obfuscation

R8 is the default code shrinker/obfuscator in Android (it replaced ProGuard). It makes
reverse-engineering your APK significantly harder.

**Enable in build.gradle.kts:**
```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true       // ← This enables R8
            isShrinkResources = true     // Remove unused resources (images, layouts)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

**What R8 does to your code:**
```
┌────────────────────────────────────────────────────────────────┐
│                    R8 PROCESSING PIPELINE                       │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  1. TREE SHAKING (Dead Code Removal)                           │
│     Removes classes, methods, fields that are never called     │
│                                                                │
│     Before: 10,000 methods (your code + libraries)             │
│     After:  3,000 methods (only what's actually used)          │
│                                                                │
│  2. OPTIMIZATION                                               │
│     Inlines short methods, removes dead branches,              │
│     simplifies control flow                                    │
│                                                                │
│     Before: fun isEmpty(list: List<*>) = list.size == 0       │
│     After:  (inlined at call site, method removed)             │
│                                                                │
│  3. OBFUSCATION (Name Mangling)                                │
│     Renames classes/methods/fields to meaningless names        │
│                                                                │
│     Before:                              After:                │
│     class UserRepository {               class a {             │
│       fun fetchUserById(                   fun a(              │
│         id: Long                             a: Long           │
│       ): User                              ): b               │
│       fun deleteUser(                      fun b(              │
│         user: User                           a: b             │
│       )                                    )                   │
│     }                                    }                     │
│     class User {                         class b {             │
│       val name: String                     val a: String       │
│       val email: String                    val b: String       │
│     }                                    }                     │
│                                                                │
│  4. RESOURCE SHRINKING (with isShrinkResources)                │
│     Removes unused drawables, layouts, strings                 │
│     Can reduce APK size by 30-50%                              │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

**Essential ProGuard Rules:**
```proguard
# proguard-rules.pro

# ═══ SERIALIZATION ═══════════════════════════════════════════
# Data classes used with Gson/Moshi/Kotlin Serialization
# R8 renames fields → JSON keys won't match → crash
-keep class com.example.app.data.model.** { *; }

# ═══ RETROFIT ════════════════════════════════════════════════
# Retrofit uses reflection to create API interface implementations
-keep,allowobfuscation interface com.example.app.data.api.** { *; }

# ═══ ROOM ════════════════════════════════════════════════════
# Room generates code based on entity class/field names
-keep class com.example.app.data.db.entity.** { *; }

# ═══ PARCELABLE ══════════════════════════════════════════════
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ═══ ENUMS ═══════════════════════════════════════════════════
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ═══ HILT/DAGGER ═════════════════════════════════════════════
# DI frameworks use reflection — keep injection targets
-keep class * extends dagger.hilt.android.internal.managers.** { *; }
```

**What R8 CANNOT protect:**
```
✅ R8 CAN protect:             ❌ R8 CANNOT protect:
  • Class/method/field names     • String literals
  • Code structure               • API URLs
  • Remove unused code           • Hardcoded API keys
  • Remove debug logs            • Resource files (XML, images)
                                 • AndroidManifest.xml
                                 • Native libraries (.so files)

String literals remain in plaintext in the DEX file.
An attacker can run: $ strings classes.dex | grep "api\|key\|secret"

For string protection → use runtime decryption or server-side config.
```

---

---

## 8.4 Permissions

### Understanding the Permission Model

Android's permission system controls what your app can access on the device.
It follows the **principle of least privilege** — apps should only request the minimum
permissions needed to function.

```
╔═══════════════════════════════════════════════════════════════════╗
║              PERMISSION CATEGORIES                                ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  ┌─────────────────────────────────────────────────────────────┐ ║
║  │  NORMAL PERMISSIONS (Install-time, auto-granted)            │ ║
║  │                                                             │ ║
║  │  • INTERNET                    • BLUETOOTH                  │ ║
║  │  • ACCESS_NETWORK_STATE        • ACCESS_WIFI_STATE          │ ║
║  │  • SET_ALARM                   • VIBRATE                    │ ║
║  │  • WAKE_LOCK                   • NFC                        │ ║
║  │  • RECEIVE_BOOT_COMPLETED      • FOREGROUND_SERVICE         │ ║
║  │                                                             │ ║
║  │  These are low-risk. User is informed at install but        │ ║
║  │  cannot deny them individually.                             │ ║
║  └─────────────────────────────────────────────────────────────┘ ║
║                                                                   ║
║  ┌─────────────────────────────────────────────────────────────┐ ║
║  │  DANGEROUS PERMISSIONS (Runtime — user must grant)          │ ║
║  │                                                             │ ║
║  │  Camera    │ CAMERA                                         │ ║
║  │  Location  │ ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION,  │ ║
║  │            │ ACCESS_BACKGROUND_LOCATION                     │ ║
║  │  Contacts  │ READ_CONTACTS, WRITE_CONTACTS, GET_ACCOUNTS   │ ║
║  │  Microphone│ RECORD_AUDIO                                   │ ║
║  │  Phone     │ READ_PHONE_STATE, CALL_PHONE, READ_CALL_LOG   │ ║
║  │  SMS       │ SEND_SMS, READ_SMS, RECEIVE_SMS               │ ║
║  │  Storage   │ READ_MEDIA_IMAGES / VIDEO / AUDIO (API 33+)   │ ║
║  │  Calendar  │ READ_CALENDAR, WRITE_CALENDAR                  │ ║
║  │  Sensors   │ BODY_SENSORS                                   │ ║
║  │  Nearby    │ NEARBY_WIFI_DEVICES (API 33+)                  │ ║
║  │  Notifs    │ POST_NOTIFICATIONS (API 33+)                   │ ║
║  │                                                             │ ║
║  │  These access sensitive user data. App must request at      │ ║
║  │  RUNTIME and user can deny or revoke anytime.               │ ║
║  └─────────────────────────────────────────────────────────────┘ ║
║                                                                   ║
║  ┌─────────────────────────────────────────────────────────────┐ ║
║  │  SPECIAL PERMISSIONS (Require user to go to Settings)       │ ║
║  │                                                             │ ║
║  │  • SYSTEM_ALERT_WINDOW           (Draw over other apps)     │ ║
║  │  • WRITE_SETTINGS                (Modify system settings)   │ ║
║  │  • REQUEST_INSTALL_PACKAGES      (Install APKs)             │ ║
║  │  • MANAGE_EXTERNAL_STORAGE       (All files access)         │ ║
║  │  • SCHEDULE_EXACT_ALARM          (Exact alarms, API 31+)    │ ║
║  │                                                             │ ║
║  │  So sensitive that the normal dialog isn't used.            │ ║
║  │  User must manually toggle in system Settings.              │ ║
║  └─────────────────────────────────────────────────────────────┘ ║
║                                                                   ║
║  ┌─────────────────────────────────────────────────────────────┐ ║
║  │  SIGNATURE PERMISSIONS (Same-developer apps only)           │ ║
║  │                                                             │ ║
║  │  Granted automatically if requesting app is signed with     │ ║
║  │  the SAME certificate as the app that declared them.        │ ║
║  │  Used for inter-app communication between your own apps.    │ ║
║  └─────────────────────────────────────────────────────────────┘ ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

---

### Runtime Permissions — Modern Implementation

**Using Activity Result API (replaces deprecated `requestPermissions()`):**

```kotlin
class PhotoActivity : AppCompatActivity() {

    // ─── Single Permission ──────────────────────────────────────

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        when {
            isGranted -> openCamera()
            // Check if we should show rationale (user denied but didn't check "Don't ask again")
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showDeniedMessage("Camera permission is needed to take photos")
            }
            // User permanently denied (checked "Don't ask again")
            else -> showGoToSettingsDialog("Camera")
        }
    }

    // ─── Multiple Permissions ───────────────────────────────────

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        when {
            fineGranted -> usePreciseLocation()
            coarseGranted -> useApproximateLocation()
            else -> showLocationDeniedMessage()
        }
    }

    // ─── Request Flow ───────────────────────────────────────────

    fun onTakePhotoClick() {
        when {
            // Already have permission
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            // Should show educational UI first
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRationaleDialog(
                    message = "We need camera access to take photos for your profile.",
                    onAccept = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }

            // First time or "Don't ask again" checked — just launch
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    fun onShowMapClick() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // ─── Helper: Guide user to Settings ─────────────────────────

    private fun showGoToSettingsDialog(permissionName: String) {
        AlertDialog.Builder(this)
            .setTitle("$permissionName Permission Required")
            .setMessage(
                "$permissionName permission was permanently denied. " +
                "Please enable it in Settings to use this feature."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    startActivity(this)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRationaleDialog(message: String, onAccept: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage(message)
            .setPositiveButton("Grant") { _, _ -> onAccept() }
            .setNegativeButton("No Thanks", null)
            .show()
    }
}
```

**Permission Decision Flow:**
```
                    User taps feature button
                              │
                              ▼
                 ┌────────────────────────┐
                 │ checkSelfPermission()  │
                 │ == PERMISSION_GRANTED? │
                 └───────────┬────────────┘
                        ┌────┴────┐
                       YES        NO
                        │         │
                        ▼         ▼
                   ┌─────────┐  ┌───────────────────────────┐
                   │ USE      │  │ shouldShowRequestPermission│
                   │ FEATURE  │  │ Rationale() ?              │
                   └─────────┘  └──────────────┬─────────────┘
                                          ┌────┴────┐
                                        TRUE       FALSE
                                          │         │
                                          ▼         │
                                    ┌───────────┐   │
                                    │Show dialog │   │
                                    │explaining  │   │
                                    │WHY you need│   │
                                    │permission  │   │
                                    └──────┬─────┘   │
                                           │         │
                                    User clicks      │
                                    "Grant"          │
                                           │         │
                                           ▼         ▼
                                    ┌─────────────────────┐
                                    │ launcher.launch()   │
                                    │ System dialog shows │
                                    └──────────┬──────────┘
                                          ┌────┴────┐
                                       GRANTED    DENIED
                                          │         │
                                          ▼         ▼
                                    ┌─────────┐  ┌────────────────────┐
                                    │ USE      │  │shouldShowRationale?│
                                    │ FEATURE  │  └────────┬───────────┘
                                    └─────────┘       ┌────┴────┐
                                                   TRUE       FALSE
                                                    │         │
                                                    ▼         ▼
                                              Show soft   "Don't ask
                                              denial      again" checked
                                              message     → Guide to
                                                          Settings

  shouldShowRequestPermissionRationale() returns:
    • FALSE the first time (user hasn't denied yet)
    • TRUE after user denied once (but didn't check "Don't ask again")
    • FALSE after user checked "Don't ask again" (permanently denied)
```

> **Interview Q: How do you know if this is the first time asking vs permanently denied?**
>
> Both return `false` from `shouldShowRequestPermissionRationale()`. There is
> **no official API** to distinguish them. Common workaround:
> Save a boolean in SharedPreferences (`"camera_permission_asked" = true`)
> after the first request. If `shouldShowRationale()` returns `false` AND
> your flag is `true`, the user permanently denied.

> **Interview Q: How did permissions change across Android versions?**
>
> ```
> API 23 (Android 6)  ── Runtime permissions introduced
> API 26 (Android 8)  ── Auto-reset unused app permissions (limited)
> API 29 (Android 10) ── ACCESS_BACKGROUND_LOCATION requires separate request
> API 30 (Android 11) ── Permission auto-reset after months of non-use
>                        One-time permissions (location, mic, camera)
>                        Granting one group permission no longer auto-grants others
> API 31 (Android 12) ── Approximate location option in dialog
>                        Bluetooth permissions split (BLUETOOTH_SCAN, etc.)
>                        SCHEDULE_EXACT_ALARM requires special permission
> API 33 (Android 13) ── POST_NOTIFICATIONS (runtime permission for notifications!)
>                        Granular media permissions (IMAGES, VIDEO, AUDIO)
>                        READ_EXTERNAL_STORAGE deprecated
>                        Photo Picker (no permission needed)
> API 34 (Android 14) ── Selected photos access (partial grant)
>                        Must declare foreground service type
> ```

---

### Special Permissions

```kotlin
// These require sending the user to system Settings — no runtime dialog.

class SpecialPermissionsHelper(private val activity: Activity) {

    // ═══ Draw Over Other Apps ═════════════════════════════════
    // Use case: Chat heads, floating widgets, screen overlays
    fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(activity)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivityForResult(intent, REQUEST_OVERLAY)
        }
    }

    // ═══ Modify System Settings ═══════════════════════════════
    // Use case: Change screen brightness, volume
    fun requestWriteSettingsPermission() {
        if (!Settings.System.canWrite(activity)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivityForResult(intent, REQUEST_WRITE_SETTINGS)
        }
    }

    // ═══ Install Unknown Apps ═════════════════════════════════
    // Use case: In-app updates from your own server
    fun requestInstallPermission() {
        if (!activity.packageManager.canRequestPackageInstalls()) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivityForResult(intent, REQUEST_INSTALL)
        }
    }

    // ═══ All Files Access (Android 11+) ═══════════════════════
    // Use case: File managers, backup apps (rarely needed!)
    fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivity(intent)
            }
        }
    }

    // ═══ Exact Alarms (Android 12+) ═══════════════════════════
    // Use case: Alarm clock apps, medication reminders
    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = activity.getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                activity.startActivity(intent)
            }
        }
    }
}
```

---

### Permission Best Practices

```
╔═══════════════════════════════════════════════════════════════╗
║               PERMISSION BEST PRACTICES                       ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  ✅ DO:                                                      ║
║                                                               ║
║  • Request in context — when user taps the feature            ║
║    "User taps camera button → request CAMERA permission"      ║
║                                                               ║
║  • Show rationale BEFORE asking (explain WHY)                ║
║    "We need camera access to scan QR codes for login"         ║
║                                                               ║
║  • Gracefully degrade when denied                            ║
║    Location denied? Show manual city selector instead         ║
║                                                               ║
║  • Use the LEAST privileged alternative:                     ║
║    Instead of...           Use...                             ║
║    READ_EXTERNAL_STORAGE → Photo Picker API                  ║
║    CAMERA                → ACTION_IMAGE_CAPTURE intent       ║
║    ACCESS_FINE_LOCATION  → ACCESS_COARSE_LOCATION            ║
║    MANAGE_EXTERNAL_STORAGE → MediaStore API                  ║
║    READ_CONTACTS         → Contact Picker intent             ║
║                                                               ║
║  ❌ DON'T:                                                   ║
║                                                               ║
║  • Request ALL permissions at app launch (users hate this)   ║
║  • Request permissions you don't actively use                ║
║  • Block the entire app if one permission is denied          ║
║  • Request FINE_LOCATION when COARSE is sufficient           ║
║  • Use READ_EXTERNAL_STORAGE on API 33+ (use media perms)   ║
║  • Repeatedly request after "Don't ask again" (guide to     ║
║    Settings instead)                                         ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

---

---

## 8.5 Data Protection

### App Sandboxing

Android's most fundamental security feature. Every app is sandboxed — isolated in its own
environment with its own Linux user ID, process, and private file storage.

> **Interview Q: Can two apps share the same UID (sandbox)?**
>
> Yes, but ONLY if they:
> 1. Declare the same `android:sharedUserId` in their manifests
> 2. Are signed with the **same certificate**
>
> This lets your own suite of apps share data. However, `sharedUserId`
> is **deprecated** since Android 10 because it weakens the sandbox.
> Use Content Providers with signature-level permissions instead.

```
╔═══════════════════════════════════════════════════════════════════╗
║                    ANDROID APP SANDBOXING                         ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  ┌────────────────────────────────────────────────────────────┐  ║
║  │                     LINUX KERNEL                            │  ║
║  │                                                             │  ║
║  │  Process 1 (UID 10001)    Process 2 (UID 10002)            │  ║
║  │  ┌────────────────────┐   ┌────────────────────┐           │  ║
║  │  │  com.app.banking   │   │  com.app.social    │           │  ║
║  │  │                    │   │                    │           │  ║
║  │  │  ART Runtime       │   │  ART Runtime       │           │  ║
║  │  │  ┌──────────────┐  │   │  ┌──────────────┐  │           │  ║
║  │  │  │ App Code     │  │   │  │ App Code     │  │           │  ║
║  │  │  │ Libraries    │  │   │  │ Libraries    │  │           │  ║
║  │  │  └──────────────┘  │   │  └──────────────┘  │           │  ║
║  │  │                    │   │                    │           │  ║
║  │  │  ❌ Can't access   │   │  ❌ Can't access   │           │  ║
║  │  │  Process 2's       │   │  Process 1's       │           │  ║
║  │  │  memory or files   │   │  memory or files   │           │  ║
║  │  └────────────────────┘   └────────────────────┘           │  ║
║  │                                                             │  ║
║  │  Private Storage:                                           │  ║
║  │  /data/data/com.app.banking/    /data/data/com.app.social/ │  ║
║  │  ├── files/                     ├── files/                 │  ║
║  │  ├── cache/                     ├── cache/                 │  ║
║  │  ├── databases/                 ├── databases/             │  ║
║  │  └── shared_prefs/              └── shared_prefs/          │  ║
║  │  (chmod 0700: UID 10001 only)   (chmod 0700: UID 10002)   │  ║
║  │                                                             │  ║
║  └────────────────────────────────────────────────────────────┘  ║
║                                                                   ║
║  Security Layers that enforce the sandbox:                       ║
║                                                                   ║
║  1. DAC (Unix Permissions) — Each app has unique UID, files are  ║
║     owned by that UID with 0700 permissions                      ║
║                                                                   ║
║  2. SELinux (Mandatory Access Control) — Even root can't bypass  ║
║     SELinux policies without disabling SELinux entirely           ║
║                                                                   ║
║  3. Seccomp-BPF — Restricts which Linux system calls each       ║
║     process can make (e.g., can't call mount, reboot, etc.)     ║
║                                                                   ║
║  4. Scoped Storage (Android 10+) — Apps can only access their   ║
║     own directories + shared media via MediaStore API            ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

---

### Secure File Storage

**Scoped Storage Evolution — the most asked storage interview topic:**
```
╔════════════════════════════════════════════════════════════════════╗
║                   SCOPED STORAGE EVOLUTION                      ║
╠════════════════════════════════════════════════════════════════════╣
║                                                                ║
║  BEFORE Android 10 (Wild West):                                ║
║    • READ/WRITE_EXTERNAL_STORAGE = access EVERYTHING            ║
║    • Any app could read any other app's files                   ║
║    • Photos, downloads, documents — all accessible              ║
║    • Privacy nightmare                                         ║
║                                                                ║
║  Android 10 (Scoped Storage introduced):                       ║
║    • Apps can only see their OWN external app-specific dir      ║
║    • Shared storage (photos, etc.) via MediaStore API only      ║
║    • No direct file path access to other apps' files            ║
║    • requestLegacyExternalStorage=true to opt out (temporary)   ║
║                                                                ║
║  Android 11 (Enforced):                                        ║
║    • requestLegacyExternalStorage IGNORED for targetSdk 30+     ║
║    • MANAGE_EXTERNAL_STORAGE for file managers (rare, reviewed) ║
║    • MediaStore or SAF (Storage Access Framework) required      ║
║                                                                ║
║  Android 13+ (Granular):                                       ║
║    • READ_EXTERNAL_STORAGE → split into:                       ║
║      READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO     ║
║    • Photo Picker — no permission needed at all!                ║
║    • Partial media access (Android 14)                          ║
║                                                                ║
╚════════════════════════════════════════════════════════════════════╝
```

> **Interview Q: How do you access photos in a modern Android app (API 33+)?**
>
> **Option 1: Photo Picker (✅ Best, no permission needed)**
> ```kotlin
> val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
>     if (uri != null) loadImage(uri)
> }
> pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
> ```
>
> **Option 2: MediaStore (requires READ_MEDIA_IMAGES permission)**
> Query the MediaStore content provider for all images.
>
> **Option 3: SAF (Storage Access Framework)**
> Opens system file picker. No permission needed. User chooses file.

```kotlin
// ════════════════════════════════════════════════════════════
// INTERNAL STORAGE — Sandboxed, private, no permission needed
// ════════════════════════════════════════════════════════════

// Private files directory: /data/data/<package>/files/
val privateFile = File(context.filesDir, "user_data.json")
privateFile.writeText("""{"name":"John","email":"john@example.com"}""")

// Private cache directory: /data/data/<package>/cache/
// System may delete cache files when storage is low
val cacheFile = File(context.cacheDir, "image_cache.tmp")

// ════════════════════════════════════════════════════════════
// EXTERNAL APP-SPECIFIC — No permission, deleted on uninstall
// ════════════════════════════════════════════════════════════

// Path: /storage/emulated/0/Android/data/<package>/files/
val externalFile = File(
    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
    "report.pdf"
)
// ⚠️ User can see these files via file manager (not truly private)

// ════════════════════════════════════════════════════════════
// ANTI-PATTERNS — Things you should NEVER do
// ════════════════════════════════════════════════════════════

// ❌ MODE_WORLD_READABLE — Deprecated API 17, removed API 24
// openFileOutput("data.txt", Context.MODE_WORLD_READABLE) // INSECURE!

// ❌ Writing sensitive data to external storage
// File(Environment.getExternalStorageDirectory(), "passwords.txt")

// ✅ Always use MODE_PRIVATE for internal files
context.openFileOutput("secrets.txt", Context.MODE_PRIVATE).use { stream ->
    stream.write("sensitive data".toByteArray())
}
```

**Storage Options and Security:**
```
┌──────────────────────┬──────────┬────────────┬───────────┬──────────────┐
│ Storage Type         │ Private? │ Permission │ Survives  │ Best For     │
│                      │          │ Needed?    │ Uninstall?│              │
├──────────────────────┼──────────┼────────────┼───────────┼──────────────┤
│ Internal files       │ ✅ Yes   │ ❌ No      │ ❌ No     │ App config,  │
│ (context.filesDir)   │          │            │           │ tokens, keys │
├──────────────────────┼──────────┼────────────┼───────────┼──────────────┤
│ Internal cache       │ ✅ Yes   │ ❌ No      │ ❌ No     │ Temp files,  │
│ (context.cacheDir)   │          │            │           │ thumbnails   │
├──────────────────────┼──────────┼────────────┼───────────┼──────────────┤
│ External app-specific│ Partially│ ❌ No      │ ❌ No     │ User docs,   │
│ (getExternalFilesDir)│ (visible)│            │           │ downloads    │
├──────────────────────┼──────────┼────────────┼───────────┼──────────────┤
│ MediaStore           │ ❌ No    │ ✅ Yes*    │ ✅ Yes    │ Photos,      │
│                      │          │            │           │ videos, music│
├──────────────────────┼──────────┼────────────┼───────────┼──────────────┤
│ SharedPreferences    │ ✅ Yes   │ ❌ No      │ ❌ No     │ Settings,    │
│                      │          │            │           │ preferences  │
├──────────────────────┼──────────┼────────────┼───────────┼──────────────┤
│ Room/SQLite          │ ✅ Yes   │ ❌ No      │ ❌ No     │ Structured   │
│                      │          │            │           │ app data     │
└──────────────────────┴──────────┴────────────┴───────────┴──────────────┘
* MediaStore: READ_MEDIA_IMAGES/VIDEO/AUDIO on Android 13+
              READ_EXTERNAL_STORAGE on Android 12 and below
```

---

### Content Provider Security

Content Providers are one of the four Android components. They expose data to other apps
via a URI-based interface. If misconfigured, they can **leak your entire database**.

```
Content Provider Attack Surface:

  YOUR APP
  ┌───────────────────────────────┐
  │  ContentProvider              │
  │  authority: com.app.provider  │
  │                               │
  │  content://com.app.provider/  │◄──── Attacker's app calls
  │    users/                     │      contentResolver.query(...)
  │    transactions/              │
  │    private_data/              │      If exported=true and no
  │                               │      permissions → ALL data
  └───────────────────────────────┘      is accessible!
```

**Securing your Content Provider:**

```xml
<!-- AndroidManifest.xml -->
<provider
    android:name=".data.SecureProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:readPermission="${applicationId}.permission.READ"
    android:writePermission="${applicationId}.permission.WRITE"
    android:grantUriPermissions="true">

    <!-- Limit which URIs can be temporarily shared -->
    <grant-uri-permission android:pathPrefix="/shared/" />
</provider>

<!-- Define permissions with 'signature' protection level -->
<!-- Only apps signed with YOUR certificate can get these -->
<permission
    android:name="${applicationId}.permission.READ"
    android:protectionLevel="signature" />
<permission
    android:name="${applicationId}.permission.WRITE"
    android:protectionLevel="signature" />
```

```
Protection Levels:

  ┌──────────────┬──────────────────────────────────────────────┐
  │ Level        │ Who Gets the Permission                      │
  ├──────────────┼──────────────────────────────────────────────┤
  │ normal       │ Any app that declares it in manifest         │
  │ dangerous    │ Any app — but user must approve at runtime   │
  │ signature    │ Only apps signed with the SAME certificate   │
  │ signatureOr  │ Signature OR system apps (pre-installed)     │
  │   System     │                                              │
  └──────────────┴──────────────────────────────────────────────┘

  For your own ContentProvider, ALWAYS use "signature" level.
  This means only YOUR other apps can access the data.
```

**Preventing SQL Injection in Content Providers:**
```kotlin
class SecureProvider : ContentProvider() {

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {

        // ═══ Security Check 1: Verify caller identity ═══
        val caller = callingPackage ?: throw SecurityException("Unknown caller")

        // ═══ Security Check 2: Validate URI ═══
        val matchCode = uriMatcher.match(uri)
        if (matchCode == UriMatcher.NO_MATCH) {
            throw IllegalArgumentException("Unknown URI: $uri")
        }

        // ═══ Security Check 3: Prevent SQL injection ═══

        // ❌ VULNERABLE — string concatenation
        // val sql = "SELECT * FROM users WHERE name = '$selection'"
        // db.rawQuery(sql, null)
        // Attacker sends: selection = "'; DROP TABLE users; --"

        // ✅ SAFE — parameterized query
        // selection contains "?" placeholders
        // selectionArgs contains values — automatically escaped
        return db.query(
            getTableForUri(uri),  // which table
            projection,           // columns
            selection,            // WHERE with ? placeholders
            selectionArgs,        // values for ? (SQL-escaped by framework)
            null,                 // groupBy
            null,                 // having
            sortOrder             // orderBy
        )
    }
}
```

**FileProvider — Secure File Sharing:**

Never share files using `file://` URIs — they expose your internal file path and bypass
the sandbox. Always use `FileProvider` with `content://` URIs.

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
<!-- Define which directories FileProvider can share from -->
<paths>
    <files-path name="internal" path="shared/" />
    <cache-path name="cache" path="images/" />
    <external-files-path name="external" path="exports/" />
    <!--
      files-path         → context.filesDir/shared/
      cache-path         → context.cacheDir/images/
      external-files-path→ getExternalFilesDir()/exports/
      
      "name" is used in the content:// URI — actual path is HIDDEN
      content://com.app.fileprovider/internal/file.pdf
      ↓ maps to ↓
      /data/data/com.app/files/shared/file.pdf
    -->
</paths>
```

```kotlin
// Share a file securely via Intent
fun shareFile(context: Context, file: File) {
    val contentUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    // contentUri = content://com.example.app.fileprovider/internal/report.pdf
    // (actual path /data/data/.../files/shared/report.pdf is HIDDEN)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        // Grant TEMPORARY read permission to the receiving app
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    // Permission is auto-revoked when receiver's Activity/task ends
}
```

---

### Backup and Restore Security

Android Auto Backup backs up your app's data to Google Drive. Without proper configuration,
**tokens, keys, and other secrets could be backed up in the cloud**.

```
What gets backed up by default:
  
  /data/data/<package>/
  ├── shared_prefs/     ✅ Backed up (incl. encrypted prefs!)
  ├── databases/        ✅ Backed up
  ├── files/            ✅ Backed up
  ├── cache/            ❌ NOT backed up (too volatile)
  └── no_backup/        ❌ NOT backed up (use getNoBackupFilesDir())

  ⚠️ If you use EncryptedSharedPreferences, the encrypted FILE
     is backed up, but the Keystore KEY is NOT. On restore, the
     data is unreadable → app crash.

  ✅ Solution: Exclude encrypted files from backup, OR use
     getNoBackupFilesDir() for sensitive files.
```

**Configuring backup rules:**
```xml
<!-- AndroidManifest.xml -->
<application
    android:allowBackup="true"
    android:fullBackupContent="@xml/backup_rules"
    android:dataExtractionRules="@xml/data_extraction_rules"
    ... >
```

```xml
<!-- res/xml/backup_rules.xml (Android 11 and below) -->
<full-backup-content>
    <!-- Include non-sensitive data -->
    <include domain="sharedpref" path="app_settings.xml" />
    <include domain="database" path="bookmarks.db" />

    <!-- Exclude sensitive data -->
    <exclude domain="sharedpref" path="auth_tokens.xml" />
    <exclude domain="sharedpref" path="secure_prefs.xml" />
    <exclude domain="database" path="sensitive.db" />
    <exclude domain="file" path="encryption_keys/" />
    <exclude domain="external" path="." />
</full-backup-content>
```

```xml
<!-- res/xml/data_extraction_rules.xml (Android 12+) -->
<data-extraction-rules>
    <cloud-backup>
        <include domain="sharedpref" path="app_settings.xml" />
        <exclude domain="sharedpref" path="auth_tokens.xml" />
        <exclude domain="database" path="sensitive.db" />
    </cloud-backup>

    <device-transfer>
        <include domain="sharedpref" path="." />
        <include domain="database" path="." />
        <exclude domain="sharedpref" path="auth_tokens.xml" />
    </device-transfer>
</data-extraction-rules>
```

**Use `noBackupFilesDir` for truly sensitive files:**
```kotlin
// Files stored here are NEVER backed up — guaranteed by the OS
val sensitiveFile = File(context.noBackupFilesDir, "device_key.bin")
sensitiveFile.writeBytes(keyBytes)
```

```
What to include/exclude:

  ❌ NEVER back up:                    ✅ SAFE to back up:
  • auth_tokens.xml                    • app_settings.xml
  • encrypted_prefs.xml                • theme preferences
  • sensitive.db                       • bookmarks.db
  • encryption keys                    • favorites list
  • device-specific IDs                • UI state
  • FCM push tokens                    • non-sensitive user data
  • session cookies                    • search history
```

---

---

## Security Checklist — Production Ready

> **Interview Q: If you were auditing an Android app's security, what would you check first?**
>
> 1. `exported` flags on all components (Activities, Services, Providers, Receivers)
> 2. Network security config — is cleartext blocked? Are certs pinned?
> 3. How are tokens stored? Plain SharedPrefs = critical vulnerability
> 4. Are SQL queries parameterized in Content Providers?
> 5. Does the app log any sensitive data?
> 6. Is R8/ProGuard enabled for release builds?
> 7. Backup rules — are secrets excluded?

```
╔════════════════════════════════════════════════════════════════════╗
║                    ANDROID SECURITY CHECKLIST                      ║
╠════════════════════════════════════════════════════════════════════╣
║                                                                    ║
║  DATA AT REST                                                      ║
║  ☐ Use EncryptedSharedPreferences for tokens & sensitive prefs     ║
║  ☐ Use EncryptedFile for sensitive documents                       ║
║  ☐ Store crypto keys in Android Keystore (NEVER hardcode)          ║
║  ☐ Use noBackupFilesDir for device-specific secrets                ║
║  ☐ Never log sensitive data (tokens, PII, passwords)               ║
║                                                                    ║
║  AUTHENTICATION                                                    ║
║  ☐ Use BiometricPrompt with CryptoObject (not plain boolean)      ║
║  ☐ Implement token refresh via OkHttp Authenticator                ║
║  ☐ Store tokens encrypted (EncryptedSharedPreferences)             ║
║  ☐ Use OAuth 2.0 + PKCE for third-party auth                      ║
║  ☐ Implement session timeout on inactivity                         ║
║  ☐ Clear tokens on logout                                          ║
║                                                                    ║
║  NETWORK                                                           ║
║  ☐ Enforce HTTPS everywhere (cleartextTrafficPermitted=false)      ║
║  ☐ Configure network_security_config.xml                           ║
║  ☐ Implement certificate pinning with backup pins                  ║
║  ☐ Don't trust user-installed CAs in release builds                ║
║  ☐ Set pin expiration dates and have rotation plan                 ║
║                                                                    ║
║  PERMISSIONS                                                       ║
║  ☐ Request only needed permissions, at point of use                ║
║  ☐ Handle denial gracefully (degrade, not block)                   ║
║  ☐ Use permission-free alternatives when possible                  ║
║  ☐ Handle "Don't ask again" → guide user to Settings               ║
║                                                                    ║
║  CODE PROTECTION                                                   ║
║  ☐ Enable R8 (isMinifyEnabled=true) in release builds              ║
║  ☐ Enable resource shrinking (isShrinkResources=true)              ║
║  ☐ Configure ProGuard keep rules for reflection-based code         ║
║  ☐ Remove debug logging in production (Timber planted trees)       ║
║                                                                    ║
║  COMPONENTS                                                        ║
║  ☐ Set android:exported="false" on all components unless needed    ║
║  ☐ Validate ALL data from Intents (never trust external input)     ║
║  ☐ Use signature-level permissions for ContentProviders            ║
║  ☐ Use FileProvider (content://) instead of file:// URIs           ║
║  ☐ Parameterize all SQL queries (prevent injection)                ║
║                                                                    ║
║  BACKUP                                                            ║
║  ☐ Configure backup rules to exclude tokens & keys                 ║
║  ☐ Use noBackupFilesDir for device-specific secrets                ║
║  ☐ Test backup/restore flow to ensure no data leakage              ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## Common Security Mistakes & Fixes

```kotlin
// ═══════════════════════════════════════════════════════
// MISTAKE 1: Hardcoded Secrets
// ═══════════════════════════════════════════════════════
// ❌ BAD — extractable by decompiling APK with 'apktool' or 'jadx'
const val API_KEY = "sk-1234567890abcdef"

// ✅ FIX — load from BuildConfig (set in local.properties, git-ignored)
// In local.properties: API_KEY=sk-1234567890abcdef
// In build.gradle.kts: buildConfigField("String", "API_KEY", "\"${properties["API_KEY"]}\"")
val apiKey = BuildConfig.API_KEY


// ═══════════════════════════════════════════════════════
// MISTAKE 2: Logging Sensitive Data
// ═══════════════════════════════════════════════════════
// ❌ BAD — any app with READ_LOGS or ADB access can read Logcat
Log.d("Auth", "Token: $accessToken, Password: $password")

// ✅ FIX — use Timber with a release tree that strips logs
// Debug: Timber.plant(Timber.DebugTree())
// Release: don't plant any tree OR plant a CrashReportingTree


// ═══════════════════════════════════════════════════════
// MISTAKE 3: Cleartext HTTP
// ═══════════════════════════════════════════════════════
// ❌ BAD — traffic is readable by anyone on the same network
val url = "http://api.example.com/login"

// ✅ FIX — always HTTPS + enforce via network security config
val url = "https://api.example.com/login"


// ═══════════════════════════════════════════════════════
// MISTAKE 4: SQL Injection
// ═══════════════════════════════════════════════════════
// ❌ BAD — attacker sends: userInput = "'; DROP TABLE users; --"
val cursor = db.rawQuery("SELECT * FROM users WHERE id = '$userInput'", null)

// ✅ FIX — parameterized query (input is auto-escaped)
val cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", arrayOf(userInput))


// ═══════════════════════════════════════════════════════
// MISTAKE 5: Trusting Intent Data
// ═══════════════════════════════════════════════════════
// ❌ BAD — any app can send an Intent to an exported Activity
val userId = intent.getStringExtra("user_id")
loadUserProfile(userId!!)  // NullPointerException + injection risk

// ✅ FIX — validate and sanitize ALL external input
val userId = intent.getStringExtra("user_id")
if (userId != null && userId.matches(Regex("^[a-zA-Z0-9_]{1,64}$"))) {
    loadUserProfile(userId)
} else {
    finish() // reject malformed input
}


// ═══════════════════════════════════════════════════════
// MISTAKE 6: Exported Components Without Protection
// ═══════════════════════════════════════════════════════
// ❌ BAD — any app can start this Activity
// <activity android:name=".AdminActivity" android:exported="true" />

// ✅ FIX — either don't export, or require a signature permission
// <activity android:name=".AdminActivity"
//     android:exported="true"
//     android:permission="com.example.app.ADMIN" />


// ═══════════════════════════════════════════════════════
// MISTAKE 7: WebView XSS
// ═══════════════════════════════════════════════════════
// ❌ BAD — loading untrusted content with JS enabled
webView.settings.javaScriptEnabled = true
webView.loadUrl(userProvidedUrl)   // XSS, phishing, data theft

// ✅ FIX — validate URLs, use Safe Browsing, restrict JS bridge
webView.settings.javaScriptEnabled = true  // only if truly needed
webView.settings.setSafeBrowsingEnabled(true)

val allowedHosts = setOf("example.com", "cdn.example.com")
val uri = Uri.parse(userProvidedUrl)
if (uri.scheme == "https" && uri.host in allowedHosts) {
    webView.loadUrl(userProvidedUrl)
} else {
    showError("URL not allowed")
}
```

---

---

## Rapid-Fire Interview Q&A

These are the quick-answer questions that come up in almost every Android security interview.

> **Q: What is ProGuard mapping.txt and why is it important?**
>
> When R8 obfuscates your code, it generates `mapping.txt` that maps obfuscated names
> back to original names. Without it, crash stack traces are unreadable.
> **Always keep mapping.txt** for every release build. Upload it to Firebase Crashlytics
> or Play Console for automatic de-obfuscation.

> **Q: How does Android verify app identity?**
>
> Every APK is signed with a developer certificate. The signing key is the app's identity.
> Two apps with the same package name but different signing keys are treated as different apps.
> Android uses this for: updates (must have same key), signature permissions, shared UID.

> **Q: What is SafetyNet / Play Integrity API?**
>
> A Google API that lets your **server** verify if the app is running on a genuine,
> unmodified Android device. Checks for: root, bootloader unlock, custom ROM,
> emulator, tampered app. Used by banking apps. SafetyNet is deprecated →
> replaced by **Play Integrity API** which provides a signed integrity verdict.

> **Q: What's the difference between `exported="true"` and `exported="false"`?**
>
> - `exported="true"` → Other apps CAN start this component (Activity, Service, etc.)
> - `exported="false"` → Only YOUR app (same UID) can start it
> - Since Android 12 (API 31), you MUST explicitly declare this for every component
>   that has an intent-filter. Previously it defaulted to `true` if you had a filter.

> **Q: How do you securely pass sensitive data between Activities?**
>
> **Within your own app:** Use a shared ViewModel (Hilt-scoped), a singleton repository,
> or encrypt the data before putting it in the Intent Bundle.
> **Never** put passwords, tokens, or PII directly in Intent extras — on older
> Android versions, any app with `READ_LOGS` could see the Intent contents.
>
> **Between apps:** Use a Content Provider with signature permission, or
> pass a content:// URI with `FLAG_GRANT_READ_URI_PERMISSION`.

> **Q: What is Tapjacking / Overlay Attack?**
>
> A malicious app draws a transparent overlay on top of your app's permission dialog.
> User thinks they're tapping "Cancel" but actually tapping "Allow" underneath.
> **Defense:** Use `filterTouchesWhenObscured="true"` on sensitive views:
> ```xml
> <Button
>     android:filterTouchesWhenObscured="true"
>     android:text="Confirm Payment" />
> ```
> This makes the view ignore touches when another app's window is visible on top.

> **Q: What is the Network Security Config's default behavior?**
>
> - Android 6 (API 23) and below: trusts both system AND user-installed CAs
> - Android 7 (API 24) and above: trusts ONLY system CAs (user CAs ignored)
> - This is why Charles Proxy / Fiddler stopped working for HTTPS debugging
>   on Android 7+ without explicit `<debug-overrides>`

> **Q: How do you prevent screenshot/screen recording of sensitive screens?**
>
> ```kotlin
> // In Activity's onCreate or onResume:
> window.setFlags(
>     WindowManager.LayoutParams.FLAG_SECURE,
>     WindowManager.LayoutParams.FLAG_SECURE
> )
> ```
> This prevents screenshots, screen recording, and the screen content
> from appearing in the Recent Apps thumbnail. Used by banking apps.

> **Q: What is the difference between hashing and encryption?**
>
> | | Hashing | Encryption |
> |--|---------|------------|
> | Reversible? | ❌ No (one-way) | ✅ Yes (with key) |
> | Purpose | Verify integrity | Protect confidentiality |
> | Output size | Fixed (SHA-256 = 32 bytes always) | Proportional to input |
> | Use case | Password storage, checksums | Storing tokens, files |
> | Example | SHA-256, bcrypt, PBKDF2 | AES-256-GCM, RSA |

> **Q: WebView security — what's `addJavascriptInterface()` vulnerability?**
>
> `addJavascriptInterface()` exposes a Kotlin/Java object to JavaScript code.
> On Android < 4.2 (API 17), JavaScript could call ANY public method on
> ANY object via reflection — including `Runtime.exec()` to run shell commands.
> **Fix (Android 4.2+):** Only methods annotated with `@JavascriptInterface`
> are accessible. But you should still validate all data coming from JS.

> **Q: What's the role of `android:allowBackup="false"` vs backup rules?**
>
> - `allowBackup="false"` → Disables ALL backup (nuclear option)
> - Backup rules → Fine-grained control (include safe data, exclude secrets)
> - If your app has ANY sensitive data, either disable backup entirely
>   or carefully configure exclusion rules. Most apps should use rules.

---
