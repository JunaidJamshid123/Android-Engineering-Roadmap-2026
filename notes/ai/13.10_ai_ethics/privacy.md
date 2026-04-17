# Privacy in AI — Differential Privacy, Secure Enclaves & Data Minimization

## Overview

Privacy-preserving AI ensures user data is protected throughout the ML lifecycle — from collection through training to inference.

```
Privacy Threat Model for Mobile AI:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Threats at Each Stage:                                     │
│                                                              │
│  Collection ──▶ Storage ──▶ Training ──▶ Inference ──▶ Output│
│     │             │           │            │            │    │
│     ▼             ▼           ▼            ▼            ▼    │
│  Over-       Breach,     Memorize     Side-channel  Leaks   │
│  collection  Theft       training     timing attacks info   │
│                           data                     via output│
│                                                              │
│  Privacy Principles:                                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 1. Data Minimization — collect only what's needed    │   │
│  │ 2. Purpose Limitation — use data only for stated     │   │
│  │    purpose                                           │   │
│  │ 3. On-Device Processing — prefer local inference     │   │
│  │ 4. Differential Privacy — mathematical privacy       │   │
│  │    guarantees                                        │   │
│  │ 5. Secure Computation — encrypt data during          │   │
│  │    processing                                        │   │
│  │ 6. Anonymization — remove identifying information    │   │
│  │ 7. Transparency — tell users what data is used       │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  Privacy Hierarchy (prefer higher):                         │
│  ┌──────────────────────────────────────┐                   │
│  │ ████████████ On-device only          │ Most private     │
│  │ ███████████  Federated learning      │                  │
│  │ ██████████   Differential privacy    │                  │
│  │ █████████    Secure enclaves         │                  │
│  │ ████████     Encrypted computation   │                  │
│  │ ███████      Anonymized cloud        │                  │
│  │ ██████       Pseudonymized cloud     │                  │
│  │ █████        Raw data to cloud       │ Least private    │
│  └──────────────────────────────────────┘                   │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. Differential Privacy

```
Differential Privacy (DP) — Formal Definition:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  A randomized mechanism M satisfies (ε, δ)-DP if:          │
│                                                              │
│  For all datasets D₁, D₂ differing on one record:          │
│  For all possible outputs S:                                │
│                                                              │
│    P(M(D₁) ∈ S) ≤ e^ε × P(M(D₂) ∈ S) + δ               │
│                                                              │
│  ε (epsilon) = privacy budget                               │
│    • ε = 0: Perfect privacy (output independent of data)   │
│    • ε = 1: Strong privacy                                 │
│    • ε = 10: Weak privacy                                  │
│    • ε = ∞: No privacy                                     │
│                                                              │
│  δ (delta) = probability of privacy failure                 │
│    • Typically δ = 1/n² where n = dataset size             │
│                                                              │
│  Mechanisms:                                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Laplace Mechanism:                                   │   │
│  │   M(x) = f(x) + Lap(Δf / ε)                        │   │
│  │   For numeric queries with sensitivity Δf            │   │
│  │                                                      │   │
│  │ Gaussian Mechanism:                                  │   │
│  │   M(x) = f(x) + N(0, σ²)                           │   │
│  │   σ = Δf × √(2 ln(1.25/δ)) / ε                     │   │
│  │   For (ε,δ)-DP                                       │   │
│  │                                                      │   │
│  │ Exponential Mechanism:                               │   │
│  │   Select output with probability ∝ exp(ε × score/2Δ)│   │
│  │   For categorical/non-numeric outputs               │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  Composition:                                               │
│  • Sequential: ε_total = Σ ε_i (budgets add up)           │
│  • Parallel: ε_total = max(ε_i) (on disjoint data)        │
│  • Advanced: Use Rényi DP for tighter accounting           │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class DifferentialPrivacy {

    private val random = java.security.SecureRandom()

    // Laplace mechanism for numeric queries
    fun laplaceMechanism(
        trueValue: Float,
        sensitivity: Float,  // max change from one record
        epsilon: Float
    ): Float {
        val scale = sensitivity / epsilon
        val noise = laplaceSample(scale)
        return trueValue + noise.toFloat()
    }

    private fun laplaceSample(scale: Float): Double {
        val u = random.nextDouble() - 0.5
        return -scale * kotlin.math.sign(u) * kotlin.math.ln(1 - 2 * kotlin.math.abs(u))
    }

    // Gaussian mechanism for (ε,δ)-DP
    fun gaussianMechanism(
        trueValue: Float,
        sensitivity: Float,
        epsilon: Float,
        delta: Float = 1e-5f
    ): Float {
        val sigma = sensitivity * kotlin.math.sqrt(2 * kotlin.math.ln(1.25 / delta)) / epsilon
        val noise = random.nextGaussian() * sigma
        return trueValue + noise.toFloat()
    }

    // Privacy budget tracker
    class PrivacyAccountant(private val totalBudget: Float) {
        private var spent = 0f
        private val queryLog = mutableListOf<PrivacyQuery>()

        data class PrivacyQuery(
            val description: String,
            val epsilon: Float,
            val timestamp: Long
        )

        fun canQuery(epsilon: Float): Boolean = (spent + epsilon) <= totalBudget

        fun recordQuery(description: String, epsilon: Float) {
            if (!canQuery(epsilon)) {
                throw IllegalStateException("Privacy budget exceeded: spent=$spent, requested=$epsilon, total=$totalBudget")
            }
            spent += epsilon
            queryLog.add(PrivacyQuery(description, epsilon, System.currentTimeMillis()))
        }

        val remainingBudget: Float get() = totalBudget - spent
        val isExhausted: Boolean get() = spent >= totalBudget
    }

    // DP-SGD: Differentially private model training
    fun dpSgdStep(
        gradients: List<FloatArray>,  // per-sample gradients
        clipNorm: Float,              // max gradient norm
        noiseMultiplier: Float,       // σ = noiseMultiplier × clipNorm
        learningRate: Float
    ): FloatArray {
        // 1. Clip each per-sample gradient
        val clipped = gradients.map { grad ->
            val norm = kotlin.math.sqrt(grad.fold(0f) { acc, v -> acc + v * v })
            if (norm > clipNorm) {
                FloatArray(grad.size) { grad[it] * clipNorm / norm }
            } else {
                grad.copyOf()
            }
        }

        // 2. Average clipped gradients
        val dim = clipped.first().size
        val avgGrad = FloatArray(dim)
        for (grad in clipped) {
            for (i in 0 until dim) avgGrad[i] += grad[i]
        }
        for (i in 0 until dim) avgGrad[i] /= clipped.size

        // 3. Add noise
        val sigma = noiseMultiplier * clipNorm / clipped.size
        return FloatArray(dim) { i ->
            avgGrad[i] + (random.nextGaussian() * sigma).toFloat()
        }
    }
}
```

---

## 2. Secure Enclaves & On-Device Processing

```
Android Secure Processing Architecture:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Application Processor (AP)                                 │
│  ┌────────────────────────────────────────────────────┐     │
│  │ Normal World              │  Secure World (TEE)   │     │
│  │                           │  ┌──────────────────┐ │     │
│  │  App ─── ML Framework     │  │  TrustZone       │ │     │
│  │  │           │            │  │  ┌────────────┐  │ │     │
│  │  │   ┌──────────────┐     │  │  │ Key storage│  │ │     │
│  │  │   │ TFLite /     │     │  │  │ Biometrics │  │ │     │
│  │  └──▶│ NNAPI        │◄────┼──┤  │ DRM        │  │ │     │
│  │      └──────────────┘     │  │  │ Attestation│  │ │     │
│  │                           │  │  └────────────┘  │ │     │
│  │  Keystore API ────────────┼──┤                  │ │     │
│  └───────────────────────────┤  └──────────────────┘ │     │
│                              │                        │     │
│  Titan M / StrongBox (Pixel) │                        │     │
│  ┌───────────────────────────┤                        │     │
│  │ Separate secure chip      │                        │     │
│  │ • Tamper-resistant        │                        │     │
│  │ • Independent processor   │                        │     │
│  │ • Key generation/storage  │                        │     │
│  └───────────────────────────┘────────────────────────┘     │
│                                                              │
│  Private Compute Core (Android 12+):                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Isolated sandbox for sensitive ML features:          │   │
│  │ • Smart Reply                                        │   │
│  │ • Live Translate                                     │   │
│  │ • Now Playing                                        │   │
│  │                                                      │   │
│  │ NO direct network access                             │   │
│  │ Updates only via Private Compute Services            │   │
│  │ (auditable open-source pipeline)                     │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureMLDataManager(private val context: Context) {

    companion object {
        private const val KEYSTORE_ALIAS = "ml_data_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val GCM_TAG_LENGTH = 128
    }

    // Create encryption key in hardware-backed Keystore
    fun createKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)  // Set true for biometric guard
            .setIsStrongBoxBacked(true)  // Use Titan M if available
            .build()

        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }

    private fun getKey(): SecretKey {
        val keyStore = java.security.KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    // Encrypt ML training data before storage
    fun encryptData(plaintext: ByteArray): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        return EncryptedData(
            ciphertext = cipher.doFinal(plaintext),
            iv = cipher.iv
        )
    }

    // Decrypt for on-device training only
    fun decryptData(encrypted: EncryptedData): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, encrypted.iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        return cipher.doFinal(encrypted.ciphertext)
    }

    data class EncryptedData(val ciphertext: ByteArray, val iv: ByteArray)
}
```

---

## 3. Data Minimization

```kotlin
class DataMinimizationPolicy {

    // Policy defining what data is needed for each ML feature
    data class DataPolicy(
        val featureName: String,
        val requiredFields: Set<String>,
        val retentionDays: Int,
        val anonymize: Boolean,
        val aggregateOnly: Boolean  // Only use aggregate stats, not individual records
    )

    private val policies = mapOf(
        "recommendation" to DataPolicy(
            featureName = "Content Recommendation",
            requiredFields = setOf("category_views", "time_spent"),
            retentionDays = 30,
            anonymize = true,
            aggregateOnly = false
        ),
        "search_ranking" to DataPolicy(
            featureName = "Search Personalization",
            requiredFields = setOf("query_hash", "click_position"),
            retentionDays = 7,
            anonymize = true,
            aggregateOnly = true
        )
    )

    // Strip unnecessary fields before ML processing
    fun minimizeData(
        rawData: Map<String, Any>,
        featureKey: String
    ): Map<String, Any> {
        val policy = policies[featureKey] ?: throw IllegalArgumentException("No policy for $featureKey")

        val minimized = rawData.filterKeys { it in policy.requiredFields }.toMutableMap()

        if (policy.anonymize) {
            minimized.remove("user_id")
            minimized.remove("device_id")
            minimized.remove("ip_address")
            minimized.remove("name")
            minimized.remove("email")
        }

        return minimized
    }

    // Auto-delete expired training data
    fun enforceRetention(
        data: MutableList<TimestampedRecord>,
        featureKey: String
    ) {
        val policy = policies[featureKey] ?: return
        val cutoff = System.currentTimeMillis() - (policy.retentionDays * 86400000L)

        data.removeAll { it.timestamp < cutoff }
    }

    // K-Anonymity: ensure each record shares attributes with k-1 others
    fun kAnonymize(
        records: List<Map<String, Any>>,
        quasiIdentifiers: List<String>,
        k: Int = 5
    ): List<Map<String, Any>> {
        // Group by quasi-identifiers
        val groups = records.groupBy { record ->
            quasiIdentifiers.map { qi -> record[qi] }
        }

        // Remove groups smaller than k (suppress)
        return groups.filter { it.value.size >= k }
            .values.flatten()
    }

    data class TimestampedRecord(
        val data: Map<String, Any>,
        val timestamp: Long
    )
}
```

---

## 4. On-Device Processing Preference

```kotlin
class PrivacyAwareMLRouter(private val context: Context) {

    enum class ProcessingMode {
        ON_DEVICE,      // Fully local — maximum privacy
        FEDERATED,      // Local training, aggregated updates
        ANONYMIZED_CLOUD,  // Cloud with anonymization
        CLOUD           // Cloud processing (requires consent)
    }

    // Decide processing mode based on data sensitivity
    fun routeMLTask(
        task: MLTask,
        userConsent: UserConsent
    ): ProcessingMode {
        return when {
            // Sensitive data — always on-device
            task.sensitivity == Sensitivity.HIGH -> ProcessingMode.ON_DEVICE

            // Medium sensitivity — federated if available
            task.sensitivity == Sensitivity.MEDIUM && task.supportsFederated ->
                ProcessingMode.FEDERATED

            // Low sensitivity with consent — cloud OK
            task.sensitivity == Sensitivity.LOW && userConsent.cloudProcessing ->
                ProcessingMode.ANONYMIZED_CLOUD

            // Default to on-device
            else -> ProcessingMode.ON_DEVICE
        }
    }

    data class MLTask(
        val name: String,
        val sensitivity: Sensitivity,
        val supportsFederated: Boolean,
        val requiresCloudModel: Boolean
    )

    enum class Sensitivity { LOW, MEDIUM, HIGH }

    data class UserConsent(
        val cloudProcessing: Boolean,
        val analytics: Boolean,
        val personalizedAds: Boolean
    )
}
```
