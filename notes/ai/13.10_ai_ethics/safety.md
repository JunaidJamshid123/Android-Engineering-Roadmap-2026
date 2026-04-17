# AI Safety — Content Filtering, Harmful Content Detection & Safe Mode

## Overview

AI safety in Android apps ensures models don't produce harmful, offensive, or dangerous content, and that safeguards exist at every layer.

```
AI Safety Defense-in-Depth:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Layer 1: Input Filtering (pre-model)                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ • Prompt injection detection                         │   │
│  │ • Profanity / hate speech blocklist                 │   │
│  │ • PII (personal info) detection & redaction         │   │
│  │ • Rate limiting                                      │   │
│  │ • Input length / format validation                  │   │
│  └──────────────────────────────────────────────────────┘   │
│                          │                                   │
│                          ▼                                   │
│  Layer 2: Model-Level Safety                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ • Safety-tuned models (RLHF, constitutional AI)     │   │
│  │ • Guardrails built into model training              │   │
│  │ • Content classifiers running in parallel            │   │
│  │ • Confidence thresholds                              │   │
│  └──────────────────────────────────────────────────────┘   │
│                          │                                   │
│                          ▼                                   │
│  Layer 3: Output Filtering (post-model)                     │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ • Toxicity classifier on outputs                    │   │
│  │ • Harmful content categories check                  │   │
│  │ • Factuality verification (where possible)          │   │
│  │ • Watermarking / provenance                         │   │
│  └──────────────────────────────────────────────────────┘   │
│                          │                                   │
│                          ▼                                   │
│  Layer 4: Application-Level Controls                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ • Human-in-the-loop for high-stakes decisions       │   │
│  │ • User reporting / feedback mechanism               │   │
│  │ • Audit logging                                      │   │
│  │ • Kill switch / safe mode fallback                  │   │
│  │ • Age-appropriate content controls                  │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  Severity Levels:                                           │
│  ┌────────────┬─────────────────────────────────────────┐   │
│  │ BLOCK      │ Violence, CSAM, self-harm instructions │   │
│  │ (always)   │ Illegal activities, weapons             │   │
│  ├────────────┼─────────────────────────────────────────┤   │
│  │ WARN       │ Mild profanity, controversial topics    │   │
│  │ (flag)     │ Unverified medical/legal advice         │   │
│  ├────────────┼─────────────────────────────────────────┤   │
│  │ MONITOR    │ Edge cases, ambiguous content           │   │
│  │ (log)      │ Context-dependent sensitivity           │   │
│  └────────────┴─────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. Content Filtering

```kotlin
class ContentFilter {

    enum class ContentCategory {
        SAFE,
        PROFANITY,
        HATE_SPEECH,
        VIOLENCE,
        SEXUAL_CONTENT,
        SELF_HARM,
        DANGEROUS_ACTIVITY,
        PII_DETECTED,
        PROMPT_INJECTION
    }

    enum class Action { ALLOW, WARN, BLOCK }

    data class FilterResult(
        val category: ContentCategory,
        val action: Action,
        val confidence: Float,
        val reason: String
    )

    // Input filtering pipeline
    fun filterInput(text: String): FilterResult {
        // 1. Check for prompt injection attempts
        val injectionResult = detectPromptInjection(text)
        if (injectionResult.action == Action.BLOCK) return injectionResult

        // 2. Check for PII
        val piiResult = detectPII(text)
        if (piiResult.action == Action.BLOCK) return piiResult

        // 3. Check content categories
        val contentResult = classifyContent(text)
        if (contentResult.action != Action.ALLOW) return contentResult

        return FilterResult(ContentCategory.SAFE, Action.ALLOW, 1.0f, "Content passed all filters")
    }

    // Prompt injection detection
    private fun detectPromptInjection(text: String): FilterResult {
        val injectionPatterns = listOf(
            Regex("ignore\\s+(previous|above|all)\\s+(instructions|prompts)", RegexOption.IGNORE_CASE),
            Regex("you\\s+are\\s+now\\s+(a|an)\\s+", RegexOption.IGNORE_CASE),
            Regex("system\\s*:\\s*", RegexOption.IGNORE_CASE),
            Regex("\\[\\s*INST\\s*\\]", RegexOption.IGNORE_CASE),
            Regex("do\\s+not\\s+follow\\s+(your|the)\\s+(rules|guidelines)", RegexOption.IGNORE_CASE),
            Regex("pretend\\s+(you|to)\\s+(are|be)\\s+", RegexOption.IGNORE_CASE)
        )

        for (pattern in injectionPatterns) {
            if (pattern.containsMatchIn(text)) {
                return FilterResult(
                    ContentCategory.PROMPT_INJECTION,
                    Action.BLOCK,
                    0.95f,
                    "Potential prompt injection detected"
                )
            }
        }

        return FilterResult(ContentCategory.SAFE, Action.ALLOW, 0f, "")
    }

    // PII detection and redaction
    private fun detectPII(text: String): FilterResult {
        val piiPatterns = mapOf(
            "email" to Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"),
            "phone" to Regex("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b"),
            "ssn" to Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b"),
            "credit_card" to Regex("\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b")
        )

        for ((type, pattern) in piiPatterns) {
            if (pattern.containsMatchIn(text)) {
                return FilterResult(
                    ContentCategory.PII_DETECTED,
                    Action.WARN,
                    0.9f,
                    "Detected $type in input — consider redacting"
                )
            }
        }

        return FilterResult(ContentCategory.SAFE, Action.ALLOW, 0f, "")
    }

    // Content classification (would use ML model in production)
    private fun classifyContent(text: String): FilterResult {
        // In production: use a trained toxicity classifier (e.g., Perspective API, ML Kit)
        // Simplified rule-based fallback:
        val lowerText = text.lowercase()

        // Check against category keyword sets (simplified)
        val categories = mapOf(
            ContentCategory.VIOLENCE to setOf("kill", "murder", "attack", "bomb"),
            ContentCategory.SELF_HARM to setOf("suicide", "self-harm", "hurt myself"),
            ContentCategory.HATE_SPEECH to setOf("slur examples would go here")
        )

        for ((category, keywords) in categories) {
            if (keywords.any { lowerText.contains(it) }) {
                return FilterResult(
                    category,
                    if (category == ContentCategory.VIOLENCE || category == ContentCategory.SELF_HARM)
                        Action.BLOCK else Action.WARN,
                    0.7f,
                    "Content flagged for $category"
                )
            }
        }

        return FilterResult(ContentCategory.SAFE, Action.ALLOW, 1.0f, "")
    }

    // Redact PII from text
    fun redactPII(text: String): String {
        var redacted = text
        redacted = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
            .replace(redacted, "[EMAIL]")
        redacted = Regex("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b")
            .replace(redacted, "[PHONE]")
        redacted = Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b")
            .replace(redacted, "[SSN]")
        redacted = Regex("\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b")
            .replace(redacted, "[CARD]")
        return redacted
    }
}
```

---

## 2. Harmful Content Detection

```kotlin
class HarmfulContentDetector(private val context: Context) {

    // Use ML Kit or custom TFLite model for toxicity detection
    private var toxicityModel: org.tensorflow.lite.Interpreter? = null

    fun loadToxicityModel() {
        val fd = context.assets.openFd("toxicity_classifier.tflite")
        val stream = java.io.FileInputStream(fd.fileDescriptor)
        val model = stream.channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )
        toxicityModel = org.tensorflow.lite.Interpreter(model)
    }

    data class ToxicityResult(
        val overallScore: Float,          // 0 = safe, 1 = toxic
        val categoryScores: Map<String, Float>,
        val isToxic: Boolean,
        val flaggedCategories: List<String>
    )

    // Classify text toxicity
    fun detectToxicity(text: String, threshold: Float = 0.7f): ToxicityResult {
        // Tokenize input
        val tokens = tokenize(text)
        val inputBuffer = java.nio.ByteBuffer.allocateDirect(512 * 4)
            .order(java.nio.ByteOrder.nativeOrder())

        for (i in 0 until minOf(tokens.size, 512)) {
            inputBuffer.putFloat(tokens[i].toFloat())
        }
        // Pad remaining
        for (i in tokens.size until 512) {
            inputBuffer.putFloat(0f)
        }
        inputBuffer.rewind()

        // Output: scores for each toxicity category
        val categories = listOf(
            "toxic", "severe_toxic", "obscene",
            "threat", "insult", "identity_hate"
        )
        val outputBuffer = java.nio.ByteBuffer.allocateDirect(categories.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())

        toxicityModel?.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()

        val scores = mutableMapOf<String, Float>()
        for (cat in categories) {
            scores[cat] = outputBuffer.float
        }

        val overallScore = scores.values.maxOrNull() ?: 0f
        val flagged = scores.filter { it.value >= threshold }.keys.toList()

        return ToxicityResult(
            overallScore = overallScore,
            categoryScores = scores,
            isToxic = overallScore >= threshold,
            flaggedCategories = flagged
        )
    }

    // Image safety check
    fun checkImageSafety(bitmap: android.graphics.Bitmap): ImageSafetyResult {
        // Use ML Kit SafetyNet or custom NSFW classifier
        val resized = android.graphics.Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val pixels = IntArray(224 * 224)
        resized.getPixels(pixels, 0, 224, 0, 0, 224, 224)

        val inputBuffer = java.nio.ByteBuffer.allocateDirect(224 * 224 * 3 * 4)
            .order(java.nio.ByteOrder.nativeOrder())

        for (pixel in pixels) {
            inputBuffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
            inputBuffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
            inputBuffer.putFloat((pixel and 0xFF) / 255f)
        }
        inputBuffer.rewind()

        // Categories: safe, suggestive, explicit, violence
        val output = FloatArray(4)
        val outputBuffer = java.nio.ByteBuffer.allocateDirect(4 * 4)
            .order(java.nio.ByteOrder.nativeOrder())

        // Run NSFW classifier
        // nsfwModel?.run(inputBuffer, outputBuffer)
        // Placeholder scores:
        val scores = mapOf("safe" to 0.9f, "suggestive" to 0.05f, "explicit" to 0.03f, "violence" to 0.02f)

        return ImageSafetyResult(
            isSafe = (scores["safe"] ?: 0f) > 0.5f,
            scores = scores,
            recommendation = if ((scores["safe"] ?: 0f) > 0.8f) "ALLOW"
                else if ((scores["safe"] ?: 0f) > 0.5f) "WARN" else "BLOCK"
        )
    }

    private fun tokenize(text: String): IntArray {
        // Simplified word-piece tokenization
        return text.lowercase().split(Regex("\\s+"))
            .map { word -> word.hashCode() and 0x7FFF }
            .toIntArray()
    }

    data class ImageSafetyResult(
        val isSafe: Boolean,
        val scores: Map<String, Float>,
        val recommendation: String
    )

    fun close() = toxicityModel?.close()
}
```

---

## 3. Safe Mode Implementation

```kotlin
class SafeModeManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("safe_mode", Context.MODE_PRIVATE)

    enum class SafetyLevel {
        STRICT,     // Maximum safety — block anything remotely risky
        STANDARD,   // Balanced — block harmful, warn on borderline
        RELAXED     // Minimal filtering — only block clearly harmful content
    }

    data class SafetyConfig(
        val level: SafetyLevel = SafetyLevel.STANDARD,
        val blockExplicitContent: Boolean = true,
        val blockViolence: Boolean = true,
        val filterProfanity: Boolean = true,
        val requireHumanApproval: Boolean = false,  // For high-stakes decisions
        val maxOutputLength: Int = 2000,
        val enableAuditLog: Boolean = true,
        val ageGroup: AgeGroup = AgeGroup.ADULT
    )

    enum class AgeGroup {
        CHILD,      // Under 13 — strictest filtering
        TEEN,       // 13-17 — strict filtering
        ADULT       // 18+ — standard filtering
    }

    fun getSafetyConfig(): SafetyConfig {
        val level = SafetyLevel.valueOf(
            prefs.getString("safety_level", SafetyLevel.STANDARD.name) ?: SafetyLevel.STANDARD.name
        )
        return SafetyConfig(
            level = level,
            blockExplicitContent = prefs.getBoolean("block_explicit", true),
            blockViolence = prefs.getBoolean("block_violence", true),
            filterProfanity = prefs.getBoolean("filter_profanity", true),
            requireHumanApproval = prefs.getBoolean("human_approval", false),
            ageGroup = AgeGroup.valueOf(
                prefs.getString("age_group", AgeGroup.ADULT.name) ?: AgeGroup.ADULT.name
            )
        )
    }

    fun saveSafetyConfig(config: SafetyConfig) {
        prefs.edit().apply {
            putString("safety_level", config.level.name)
            putBoolean("block_explicit", config.blockExplicitContent)
            putBoolean("block_violence", config.blockViolence)
            putBoolean("filter_profanity", config.filterProfanity)
            putBoolean("human_approval", config.requireHumanApproval)
            putString("age_group", config.ageGroup.name)
            apply()
        }
    }

    // Get filtering threshold based on safety level
    fun getThreshold(config: SafetyConfig): Float {
        return when (config.level) {
            SafetyLevel.STRICT -> 0.3f    // Very sensitive
            SafetyLevel.STANDARD -> 0.6f  // Balanced
            SafetyLevel.RELAXED -> 0.85f  // Only block obvious
        }
    }
}

// Complete Safety Pipeline
class AISafetyPipeline(
    private val contentFilter: ContentFilter,
    private val harmDetector: HarmfulContentDetector,
    private val safeModeManager: SafeModeManager,
    private val auditLogger: AuditLogger
) {
    data class SafeResponse(
        val content: String?,
        val allowed: Boolean,
        val warnings: List<String>,
        val blockedReason: String?
    )

    // End-to-end safety check for text generation
    suspend fun processWithSafety(
        input: String,
        generateFn: suspend (String) -> String
    ): SafeResponse {
        val config = safeModeManager.getSafetyConfig()
        val threshold = safeModeManager.getThreshold(config)
        val warnings = mutableListOf<String>()

        // 1. Filter input
        val inputFilter = contentFilter.filterInput(input)
        auditLogger.logEvent("input_filter", inputFilter.category.name, inputFilter.confidence)

        if (inputFilter.action == ContentFilter.Action.BLOCK) {
            return SafeResponse(null, false, emptyList(), "Input blocked: ${inputFilter.reason}")
        }
        if (inputFilter.action == ContentFilter.Action.WARN) {
            warnings.add("Input warning: ${inputFilter.reason}")
        }

        // 2. Redact PII from input
        val sanitizedInput = contentFilter.redactPII(input)

        // 3. Generate output
        val output = generateFn(sanitizedInput)

        // 4. Check output toxicity
        val toxicity = harmDetector.detectToxicity(output, threshold)
        auditLogger.logEvent("output_toxicity", toxicity.overallScore.toString(), toxicity.overallScore)

        if (toxicity.isToxic) {
            return SafeResponse(
                null, false, warnings,
                "Output blocked: toxic content detected (${toxicity.flaggedCategories.joinToString()})"
            )
        }

        // 5. Filter output
        val outputFilter = contentFilter.filterInput(output)
        if (outputFilter.action == ContentFilter.Action.BLOCK) {
            return SafeResponse(null, false, warnings, "Output blocked: ${outputFilter.reason}")
        }

        // 6. Truncate if needed
        val finalOutput = if (output.length > config.maxOutputLength) {
            output.take(config.maxOutputLength) + "..."
        } else output

        // 7. Redact any PII in output
        val safeOutput = contentFilter.redactPII(finalOutput)

        return SafeResponse(safeOutput, true, warnings, null)
    }
}

// Audit logging for safety events
class AuditLogger(private val context: Context) {
    private val db by lazy { AuditDatabase.getInstance(context) }

    fun logEvent(eventType: String, detail: String, score: Float) {
        // Store audit log locally (encrypted)
        val event = AuditEvent(
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            detail = detail,
            score = score
        )
        // db.auditDao().insert(event) — Room DAO
    }

    data class AuditEvent(
        val timestamp: Long,
        val eventType: String,
        val detail: String,
        val score: Float
    )
}

// Kill switch - remotely disable AI features
class AIKillSwitch(private val context: Context) {

    // Check remote config for kill switch status
    suspend fun isAIEnabled(): Boolean {
        return try {
            val remoteConfig = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance()
            remoteConfig.fetchAndActivate()
            remoteConfig.getBoolean("ai_features_enabled")
        } catch (e: Exception) {
            // Default to enabled if remote config unavailable
            true
        }
    }

    // Graceful degradation when AI is disabled
    fun getFallbackResponse(feature: String): String {
        return when (feature) {
            "chat" -> "AI chat is temporarily unavailable. Please try again later."
            "recommendation" -> "Showing popular items instead of personalized recommendations."
            "search" -> "Using basic search. Smart search is temporarily unavailable."
            else -> "This feature is temporarily unavailable."
        }
    }
}
```
