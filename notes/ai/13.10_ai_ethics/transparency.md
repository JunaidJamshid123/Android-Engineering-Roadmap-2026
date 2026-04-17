# Transparency in AI — Explainable AI, Model Cards & User Consent

## Overview

AI transparency ensures users and stakeholders understand how models make decisions, what data is used, and what limitations exist.

```
Transparency Pillars:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Explainable  │  │ Model        │  │ User Consent │      │
│  │ AI (XAI)     │  │ Cards        │  │ & Control    │      │
│  │              │  │              │  │              │      │
│  │ WHY did the  │  │ WHAT is the  │  │ WHO decides  │      │
│  │ model decide │  │ model? Its   │  │ what the AI  │      │
│  │ this?        │  │ capabilities │  │ can do?      │      │
│  │              │  │ & limits?    │  │              │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                 │               │
│         ▼                 ▼                 ▼               │
│  Feature         Documentation     Informed consent        │
│  attribution     & reporting       Privacy controls        │
│  Saliency maps   Bias disclosures  Opt-in/opt-out          │
│  Decision trees  Performance data  Data deletion           │
│                                                              │
│  Levels of Explainability:                                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                                                      │   │
│  │  Intrinsic (model is inherently interpretable):     │   │
│  │  • Linear regression: y = w₁x₁ + w₂x₂ + b         │   │
│  │  • Decision trees: if-then rules                    │   │
│  │  • Simple rule-based systems                        │   │
│  │                                                      │   │
│  │  Post-hoc (explain after the fact):                 │   │
│  │  • LIME: local linear approximation                 │   │
│  │  • SHAP: game-theoretic feature attribution         │   │
│  │  • Grad-CAM: visual attention for CNNs             │   │
│  │  • Attention weights: for transformers             │   │
│  │  • Counterfactual: "what would need to change?"    │   │
│  │                                                      │   │
│  │  Audience-specific:                                  │   │
│  │  • Developers: full technical explanations          │   │
│  │  • Regulators: fairness reports, bias audits        │   │
│  │  • End users: simple natural language reasons       │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. Explainable AI (XAI)

```
XAI Methods for Mobile:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  LIME (Local Interpretable Model-agnostic Explanations):   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 1. Pick instance to explain                          │   │
│  │ 2. Perturb input (create similar inputs)             │   │
│  │ 3. Get model predictions for perturbed inputs       │   │
│  │ 4. Fit simple linear model locally                  │   │
│  │ 5. Linear model weights = feature importance        │   │
│  │                                                      │   │
│  │ Original              Perturbed              Local  │   │
│  │ ┌──────┐  perturb   ┌──────┐  black-box    linear  │   │
│  │ │ x    │──────────▶ │x₁,x₂│──────────▶  weights  │   │
│  │ └──────┘  many      │x₃,x₄│  predict     w₁,w₂..│   │
│  │           variants  └──────┘                        │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  SHAP (SHapley Additive exPlanations):                     │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Based on Shapley values from game theory:            │   │
│  │ • Each feature is a "player"                        │   │
│  │ • Shapley value = average marginal contribution     │   │
│  │ • Sum of all SHAP values = prediction - avg pred   │   │
│  │                                                      │   │
│  │ φᵢ = Σ |S|!(N-|S|-1)! / N! × [f(S∪{i}) - f(S)]   │   │
│  │      S⊆N\{i}                                        │   │
│  │                                                      │   │
│  │ Properties:                                          │   │
│  │ • Local accuracy: f(x) = Σ φᵢ + E[f(x)]           │   │
│  │ • Consistency: if feature contributes more in       │   │
│  │   model B than A, SHAP is higher in B               │   │
│  │ • Additive: can sum for feature groups              │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  Grad-CAM (for image models):                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 1. Get gradients of target class w.r.t. feature map │   │
│  │ 2. Global average pool gradients → weights αₖ       │   │
│  │ 3. Weighted sum: L = ReLU(Σ αₖ × Aₖ)              │   │
│  │ 4. Overlay heatmap on input image                   │   │
│  │                                                      │   │
│  │ Input Image  →  Feature Maps  →  Heatmap Overlay   │   │
│  │ [  🐱  ]        [maps...]       [  🟥🐱  ]        │   │
│  │                                  Shows WHERE model  │   │
│  │                                  is "looking"       │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class ExplainableAI {

    // LIME-style local explanation for tabular data
    fun limeExplain(
        model: (FloatArray) -> Float,
        instance: FloatArray,
        featureNames: List<String>,
        numPerturbations: Int = 1000,
        numTopFeatures: Int = 5
    ): Explanation {
        val random = java.util.Random()
        val dim = instance.size

        // Generate perturbed samples
        val perturbedInputs = mutableListOf<FloatArray>()
        val perturbedMasks = mutableListOf<FloatArray>()
        val perturbedOutputs = mutableListOf<Float>()

        for (i in 0 until numPerturbations) {
            val mask = FloatArray(dim) { if (random.nextBoolean()) 1f else 0f }
            val perturbed = FloatArray(dim) { j ->
                if (mask[j] == 1f) instance[j]
                else instance[j] + random.nextGaussian().toFloat() * 0.1f
            }

            perturbedInputs.add(perturbed)
            perturbedMasks.add(mask)
            perturbedOutputs.add(model(perturbed))
        }

        // Distance weighting (closer perturbations matter more)
        val distances = perturbedInputs.map { perturbed ->
            val diff = FloatArray(dim) { (instance[it] - perturbed[it]) }
            kotlin.math.sqrt(diff.fold(0f) { acc, v -> acc + v * v })
        }
        val maxDist = distances.maxOrNull() ?: 1f
        val weights = distances.map { kotlin.math.exp(-it / maxDist).toFloat() }

        // Weighted linear regression to find feature importance
        val featureImportance = FloatArray(dim)
        for (j in 0 until dim) {
            var sumWXY = 0f; var sumWX = 0f; var sumWY = 0f; var sumW = 0f; var sumWXX = 0f
            for (i in perturbedInputs.indices) {
                val w = weights[i]
                val x = perturbedMasks[i][j]
                val y = perturbedOutputs[i]
                sumW += w; sumWXY += w * x * y; sumWX += w * x
                sumWY += w * y; sumWXX += w * x * x
            }
            featureImportance[j] = (sumW * sumWXY - sumWX * sumWY) /
                (sumW * sumWXX - sumWX * sumWX + 1e-10f)
        }

        // Get top features by absolute importance
        val topFeatures = featureImportance.indices
            .sortedByDescending { kotlin.math.abs(featureImportance[it]) }
            .take(numTopFeatures)
            .map { idx ->
                FeatureContribution(
                    name = featureNames.getOrElse(idx) { "feature_$idx" },
                    value = instance[idx],
                    importance = featureImportance[idx]
                )
            }

        return Explanation(
            prediction = model(instance),
            topContributions = topFeatures
        )
    }

    // Generate human-readable explanation
    fun formatExplanation(explanation: Explanation): String {
        val sb = StringBuilder()
        sb.appendLine("Prediction: ${explanation.prediction}")
        sb.appendLine("Top contributing factors:")

        for ((i, contrib) in explanation.topContributions.withIndex()) {
            val direction = if (contrib.importance > 0) "increases" else "decreases"
            sb.appendLine("  ${i + 1}. ${contrib.name} ($direction prediction by ${
                String.format("%.3f", kotlin.math.abs(contrib.importance))
            })")
        }
        return sb.toString()
    }

    // Saliency map for image classification (simplified Grad-CAM concept)
    fun computeSaliencyMap(
        model: (FloatArray) -> FloatArray,  // input pixels → class scores
        image: FloatArray,
        targetClass: Int,
        imageWidth: Int,
        imageHeight: Int
    ): FloatArray {
        val epsilon = 0.01f
        val saliency = FloatArray(image.size)

        // Approximate gradients via finite differences
        val baseOutput = model(image)[targetClass]

        for (i in image.indices) {
            val perturbed = image.copyOf()
            perturbed[i] += epsilon
            val perturbedOutput = model(perturbed)[targetClass]
            saliency[i] = (perturbedOutput - baseOutput) / epsilon
        }

        // Normalize to [0, 1]
        val maxVal = saliency.maxOrNull() ?: 1f
        val minVal = saliency.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1e-10f)

        return FloatArray(saliency.size) { (saliency[it] - minVal) / range }
    }

    data class Explanation(
        val prediction: Float,
        val topContributions: List<FeatureContribution>
    )

    data class FeatureContribution(
        val name: String,
        val value: Float,
        val importance: Float
    )
}
```

---

## 2. Model Cards

```
Model Card Template:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Model Card — [Model Name]                                  │
│  ──────────────────────────────────────                      │
│                                                              │
│  1. Model Details                                           │
│  ├── Model type (architecture)                              │
│  ├── Version                                                │
│  ├── Training date                                          │
│  ├── Framework (TFLite, PyTorch Mobile)                     │
│  └── License                                                │
│                                                              │
│  2. Intended Use                                            │
│  ├── Primary use cases                                      │
│  ├── Users                                                  │
│  └── Out-of-scope uses                                      │
│                                                              │
│  3. Training Data                                           │
│  ├── Dataset name & size                                    │
│  ├── Demographic breakdown                                  │
│  ├── Data collection method                                 │
│  └── Preprocessing steps                                    │
│                                                              │
│  4. Evaluation Data                                         │
│  ├── Test set description                                   │
│  └── Demographic breakdown                                  │
│                                                              │
│  5. Performance Metrics                                     │
│  ├── Overall accuracy/F1/etc.                               │
│  └── Per-group metrics (fairness)                           │
│                                                              │
│  6. Ethical Considerations                                  │
│  ├── Known biases                                           │
│  ├── Mitigation steps taken                                 │
│  └── Risks of misuse                                        │
│                                                              │
│  7. Limitations                                             │
│  ├── Known failure modes                                    │
│  ├── Data gaps                                              │
│  └── Environments where model shouldn't be used             │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
data class ModelCard(
    val modelName: String,
    val version: String,
    val description: String,
    val architecture: String,
    val trainingDate: String,
    val framework: String,
    val license: String,

    val intendedUse: IntendedUse,
    val trainingData: DatasetInfo,
    val evaluationData: DatasetInfo,
    val performanceMetrics: PerformanceMetrics,
    val ethicalConsiderations: EthicalConsiderations,
    val limitations: List<String>
) {
    data class IntendedUse(
        val primaryUseCases: List<String>,
        val intendedUsers: List<String>,
        val outOfScopeUses: List<String>
    )

    data class DatasetInfo(
        val name: String,
        val size: Int,
        val demographics: Map<String, Float>,  // group → percentage
        val collectionMethod: String
    )

    data class PerformanceMetrics(
        val overallAccuracy: Float,
        val overallF1: Float,
        val perGroupMetrics: Map<String, GroupPerformance>
    )

    data class GroupPerformance(
        val accuracy: Float,
        val precision: Float,
        val recall: Float,
        val f1: Float
    )

    data class EthicalConsiderations(
        val knownBiases: List<String>,
        val mitigationSteps: List<String>,
        val risksOfMisuse: List<String>
    )
}

// Embed model card in Android app
class ModelCardProvider(private val context: Context) {

    fun loadModelCard(modelName: String): ModelCard? {
        return try {
            val json = context.assets.open("model_cards/$modelName.json")
                .bufferedReader().readText()

            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            moshi.adapter(ModelCard::class.java).fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    // Show model card to user (Compose UI)
    @androidx.compose.runtime.Composable
    fun ModelCardScreen(modelCard: ModelCard) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                androidx.compose.material3.Text(
                    text = modelCard.modelName,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                )
                androidx.compose.material3.Text("Version: ${modelCard.version}")
                androidx.compose.material3.Text(modelCard.description)

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("Intended Use")
                for (use in modelCard.intendedUse.primaryUseCases) {
                    BulletPoint("✅ $use")
                }
                for (use in modelCard.intendedUse.outOfScopeUses) {
                    BulletPoint("❌ NOT for: $use")
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("Performance")
                androidx.compose.material3.Text(
                    "Overall Accuracy: ${String.format("%.1f%%", modelCard.performanceMetrics.overallAccuracy * 100)}"
                )

                for ((group, metrics) in modelCard.performanceMetrics.perGroupMetrics) {
                    BulletPoint("$group: acc=${String.format("%.1f%%", metrics.accuracy * 100)}, " +
                        "F1=${String.format("%.3f", metrics.f1)}")
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("Known Limitations")
                for (limitation in modelCard.limitations) {
                    BulletPoint("⚠️ $limitation")
                }

                SectionHeader("Ethical Considerations")
                for (bias in modelCard.ethicalConsiderations.knownBiases) {
                    BulletPoint("🔍 $bias")
                }
            }
        }
    }
}
```

---

## 3. User Consent and Control

```kotlin
import android.content.SharedPreferences

class AIConsentManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_consent", Context.MODE_PRIVATE)

    // Granular consent settings
    data class AIConsent(
        val onDeviceML: Boolean = true,          // On-device inference (default: allowed)
        val personalizedRecommendations: Boolean = false,
        val cloudProcessing: Boolean = false,
        val dataContributionForTraining: Boolean = false,
        val biometricAI: Boolean = false,         // Face/fingerprint AI features
        val voiceProcessing: Boolean = false,
        val locationBasedAI: Boolean = false
    )

    fun saveConsent(consent: AIConsent) {
        prefs.edit().apply {
            putBoolean("on_device_ml", consent.onDeviceML)
            putBoolean("personalized_rec", consent.personalizedRecommendations)
            putBoolean("cloud_processing", consent.cloudProcessing)
            putBoolean("data_contribution", consent.dataContributionForTraining)
            putBoolean("biometric_ai", consent.biometricAI)
            putBoolean("voice_processing", consent.voiceProcessing)
            putBoolean("location_ai", consent.locationBasedAI)
            putLong("consent_timestamp", System.currentTimeMillis())
            apply()
        }
    }

    fun loadConsent(): AIConsent {
        return AIConsent(
            onDeviceML = prefs.getBoolean("on_device_ml", true),
            personalizedRecommendations = prefs.getBoolean("personalized_rec", false),
            cloudProcessing = prefs.getBoolean("cloud_processing", false),
            dataContributionForTraining = prefs.getBoolean("data_contribution", false),
            biometricAI = prefs.getBoolean("biometric_ai", false),
            voiceProcessing = prefs.getBoolean("voice_processing", false),
            locationBasedAI = prefs.getBoolean("location_ai", false)
        )
    }

    // Check consent before running AI feature
    fun isFeatureAllowed(feature: AIFeature): Boolean {
        val consent = loadConsent()
        return when (feature) {
            AIFeature.ON_DEVICE_INFERENCE -> consent.onDeviceML
            AIFeature.RECOMMENDATIONS -> consent.personalizedRecommendations
            AIFeature.CLOUD_ML -> consent.cloudProcessing
            AIFeature.FEDERATED_LEARNING -> consent.dataContributionForTraining
            AIFeature.FACE_DETECTION -> consent.biometricAI
            AIFeature.SPEECH_RECOGNITION -> consent.voiceProcessing
            AIFeature.LOCATION_PREDICTION -> consent.locationBasedAI
        }
    }

    // Right to deletion: erase all ML-related user data
    fun deleteAllAIData() {
        // Clear local model personalization
        val mlDir = java.io.File(context.filesDir, "ml_data")
        mlDir.deleteRecursively()

        // Clear cached models
        val cacheDir = java.io.File(context.cacheDir, "models")
        cacheDir.deleteRecursively()

        // Clear training data
        val trainingDir = java.io.File(context.filesDir, "training_data")
        trainingDir.deleteRecursively()

        // Reset consent
        prefs.edit().clear().apply()

        // Notify server to delete cloud data
        // apiService.deleteUserMLData(userId)
    }

    // Right to explanation: provide reasoning for AI decisions
    fun getDecisionExplanation(
        feature: AIFeature,
        decision: Any
    ): String {
        return when (feature) {
            AIFeature.RECOMMENDATIONS ->
                "This recommendation is based on your viewing history in the following categories. " +
                "You can adjust or disable recommendations in Settings > AI Preferences."
            AIFeature.CLOUD_ML ->
                "This feature processes your data on secure cloud servers. " +
                "No personal data is stored after processing."
            else -> "This AI feature runs entirely on your device. No data leaves your phone."
        }
    }

    enum class AIFeature {
        ON_DEVICE_INFERENCE, RECOMMENDATIONS, CLOUD_ML,
        FEDERATED_LEARNING, FACE_DETECTION, SPEECH_RECOGNITION,
        LOCATION_PREDICTION
    }
}
```
