# Federated Learning — Privacy-Preserving On-Device Training

## Overview

Federated Learning (FL) trains ML models across many devices without sharing raw data. Each device trains locally and sends only model updates (gradients) to a central server.

```
Federated Learning Architecture:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Traditional ML:                Federated Learning:         │
│  ┌──────┐ data ┌──────┐       ┌──────┐ grads ┌──────┐    │
│  │Device│─────▶│Server│       │Device│──────▶│Server│    │
│  │  1   │      │Train │       │  1   │       │Aggre-│    │
│  │Device│─────▶│ on   │       │Device│──────▶│gate  │    │
│  │  2   │      │ ALL  │       │  2   │       │only  │    │
│  │Device│─────▶│ data │       │Device│──────▶│      │    │
│  │  3   │      └──────┘       │  3   │       └──┬───┘    │
│  └──────┘                     └──────┘          │         │
│  ❌ Raw data sent             ✅ Data stays      │         │
│     to server                    on device        │         │
│                                                   │         │
│                               Global model ◄─────┘         │
│                               sent back to all devices      │
│                                                              │
│  Federated Averaging (FedAvg) Protocol:                     │
│  ┌──────────────────────────────────────────────────┐       │
│  │                                                  │       │
│  │  Round t:                                        │       │
│  │                                                  │       │
│  │  1. Server → broadcasts global model W_t        │       │
│  │                                                  │       │
│  │  2. Each device k:                               │       │
│  │     • Downloads W_t                              │       │
│  │     • Trains on LOCAL data for E epochs          │       │
│  │     • Computes update: ΔW_k = W_k - W_t        │       │
│  │     • Sends ΔW_k to server                      │       │
│  │                                                  │       │
│  │  3. Server aggregates:                           │       │
│  │     W_{t+1} = W_t + (1/K) × Σ ΔW_k            │       │
│  │     (weighted by # samples per device)           │       │
│  │                                                  │       │
│  │  4. Repeat for T rounds                          │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  Privacy Guarantees:                                        │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Layer 1: Data never leaves device               │       │
│  │ Layer 2: Secure Aggregation — server can't see  │       │
│  │          individual updates                      │       │
│  │ Layer 3: Differential Privacy — add noise to    │       │
│  │          updates before sending                  │       │
│  │ Layer 4: Minimum participants — only aggregate  │       │
│  │          when enough devices participate          │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  Google's FL in Production:                                 │
│  ┌──────────────────────────────────────────────────┐       │
│  │ • Gboard: Next-word prediction (billions of     │       │
│  │   keyboard inputs, never sent to Google)         │       │
│  │ • Smart Reply: Suggested quick responses        │       │
│  │ • Now Playing: On-device music recognition      │       │
│  │ • Pixel call screening: Spam detection          │       │
│  └──────────────────────────────────────────────────┘       │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. On-Device Model Training

```kotlin
import org.tensorflow.lite.Interpreter

class OnDeviceTrainer(private val context: Context) {

    // Simulated on-device training with TFLite
    // TFLite supports training via signatures (TF 2.7+)
    private var interpreter: Interpreter? = null

    fun loadTrainableModel(modelPath: String) {
        val fd = context.assets.openFd(modelPath)
        val stream = java.io.FileInputStream(fd.fileDescriptor)
        val model = stream.channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )

        val options = Interpreter.Options().apply { setNumThreads(4) }
        interpreter = Interpreter(model, options)
    }

    // Simple on-device fine-tuning loop
    fun trainOnLocalData(
        trainingData: List<Pair<FloatArray, FloatArray>>,
        epochs: Int = 5,
        learningRate: Float = 0.001f
    ): TrainingResult {
        val losses = mutableListOf<Float>()

        for (epoch in 0 until epochs) {
            var epochLoss = 0f

            for ((input, label) in trainingData.shuffled()) {
                // Run training step via TFLite signature
                val inputs = mapOf(
                    "input" to arrayOf(input),
                    "label" to arrayOf(label)
                )

                val outputs = mapOf(
                    "loss" to arrayOf(FloatArray(1))
                )

                // Use training signature runner
                interpreter?.runSignature(inputs, outputs, "train")

                epochLoss += outputs["loss"]?.let {
                    (it as? Array<*>)?.firstOrNull()?.let { arr ->
                        (arr as? FloatArray)?.firstOrNull() ?: 0f
                    } ?: 0f
                } ?: 0f
            }

            val avgLoss = epochLoss / trainingData.size
            losses.add(avgLoss)
            android.util.Log.d("Training", "Epoch $epoch: loss=$avgLoss")
        }

        return TrainingResult(losses, epochs)
    }

    // Export trained weights
    fun exportWeights(): ByteArray {
        val outputs = mapOf(
            "weights" to arrayOf(FloatArray(0))  // Model determines size
        )
        interpreter?.runSignature(emptyMap(), outputs, "export")

        // Serialize weights to ByteArray
        val weights = outputs["weights"]
        return serializeWeights(weights)
    }

    private fun serializeWeights(weights: Any?): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        val oos = java.io.ObjectOutputStream(stream)
        oos.writeObject(weights)
        return stream.toByteArray()
    }

    data class TrainingResult(val lossHistory: List<Float>, val epochs: Int)

    fun close() = interpreter?.close()
}
```

---

## 2. Federated Averaging Implementation

```
FedAvg Algorithm Detail:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Input: K devices, T rounds, E local epochs, η learn rate  │
│                                                              │
│  Server:                                                    │
│    Initialize W₀ randomly                                   │
│    for t = 1 to T:                                          │
│      Select subset S of K devices (e.g., 100 of 10M)       │
│      Send W_t to each device in S                           │
│      for each device k in S (in parallel):                  │
│        W_k ← LocalTrain(W_t, local_data_k, E, η)          │
│        ΔW_k ← W_k - W_t                                    │
│      Aggregate:                                             │
│        W_{t+1} = W_t + Σ (n_k / n) × ΔW_k                │
│                        k                                    │
│        where n_k = # samples on device k                   │
│              n = total samples across all selected          │
│                                                              │
│  Challenges:                                                │
│  ┌──────────────────────┬───────────────────────────┐      │
│  │ Non-IID data         │ Each device has different │      │
│  │                      │ data distribution          │      │
│  ├──────────────────────┼───────────────────────────┤      │
│  │ Communication cost   │ Models can be 100s of MB  │      │
│  │                      │ → compress updates         │      │
│  ├──────────────────────┼───────────────────────────┤      │
│  │ Device heterogeneity │ Different CPUs, battery   │      │
│  │                      │ → adaptive scheduling     │      │
│  ├──────────────────────┼───────────────────────────┤      │
│  │ Stragglers           │ Slow devices delay round  │      │
│  │                      │ → async aggregation        │      │
│  └──────────────────────┴───────────────────────────┘      │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
// --- Server-Side API Models ---
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class FLRoundConfig(
    val roundNumber: Int,
    val modelVersion: String,
    val modelUrl: String,       // URL to download global model
    val localEpochs: Int,
    val learningRate: Float,
    val minSamples: Int         // Minimum local samples to participate
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class FLModelUpdate(
    val roundNumber: Int,
    val deviceId: String,
    val numSamples: Int,
    val updateData: String,      // Base64-encoded weight deltas
    val metrics: Map<String, Float>  // loss, accuracy, etc.
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class FLRoundResult(
    val roundNumber: Int,
    val aggregatedModelUrl: String,
    val participantCount: Int,
    val globalMetrics: Map<String, Float>
)

// --- API ---
interface FederatedLearningApi {
    @retrofit2.http.GET("fl/round/current")
    suspend fun getCurrentRound(): FLRoundConfig

    @retrofit2.http.POST("fl/updates")
    suspend fun submitUpdate(@retrofit2.http.Body update: FLModelUpdate): FLRoundResult

    @retrofit2.http.GET("fl/model/{version}")
    suspend fun downloadModel(@retrofit2.http.Path("version") version: String): okhttp3.ResponseBody
}

// --- Client ---
class FederatedLearningClient(
    private val context: Context,
    private val api: FederatedLearningApi,
    private val trainer: OnDeviceTrainer,
    private val deviceId: String
) {
    // Check conditions before participating
    fun canParticipate(): Boolean {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        val batteryLevel = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val isWifi = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
        } ?: false

        // Only train when: charging + WiFi + battery > 80%
        return isCharging && isWifi && batteryLevel > 80
    }

    // Participate in a federated learning round
    suspend fun participateInRound(localData: List<Pair<FloatArray, FloatArray>>) {
        if (!canParticipate()) {
            android.util.Log.d("FL", "Conditions not met, skipping round")
            return
        }

        try {
            // 1. Get current round config
            val roundConfig = api.getCurrentRound()

            if (localData.size < roundConfig.minSamples) {
                android.util.Log.d("FL", "Not enough local data")
                return
            }

            // 2. Download global model
            val modelBody = api.downloadModel(roundConfig.modelVersion)
            val modelFile = java.io.File(context.cacheDir, "fl_model.tflite")
            modelFile.writeBytes(modelBody.bytes())

            // 3. Load and save initial weights
            trainer.loadTrainableModel(modelFile.absolutePath)
            val initialWeights = trainer.exportWeights()

            // 4. Train locally
            val result = trainer.trainOnLocalData(
                trainingData = localData,
                epochs = roundConfig.localEpochs,
                learningRate = roundConfig.learningRate
            )

            // 5. Compute weight delta
            val trainedWeights = trainer.exportWeights()
            val delta = computeWeightDelta(initialWeights, trainedWeights)

            // 6. Submit update
            val update = FLModelUpdate(
                roundNumber = roundConfig.roundNumber,
                deviceId = deviceId,
                numSamples = localData.size,
                updateData = android.util.Base64.encodeToString(delta, android.util.Base64.NO_WRAP),
                metrics = mapOf(
                    "final_loss" to (result.lossHistory.lastOrNull() ?: 0f),
                    "num_epochs" to result.epochs.toFloat()
                )
            )

            api.submitUpdate(update)
            android.util.Log.d("FL", "Round ${roundConfig.roundNumber} complete")

        } catch (e: Exception) {
            android.util.Log.e("FL", "FL round failed", e)
        } finally {
            trainer.close()
        }
    }

    private fun computeWeightDelta(initial: ByteArray, trained: ByteArray): ByteArray {
        // Simplified: in production, compute actual weight differences
        return trained
    }
}
```

---

## 3. Privacy-Preserving ML

```kotlin
class PrivacyPreservingFL {

    // Differential Privacy — add calibrated noise to model updates
    fun addDifferentialPrivacy(
        gradients: FloatArray,
        epsilon: Float = 1.0f,    // Privacy budget (lower = more private)
        delta: Float = 1e-5f,     // Failure probability
        clipNorm: Float = 1.0f    // Max gradient norm (sensitivity)
    ): FloatArray {
        // Step 1: Clip gradients (bound sensitivity)
        val clipped = clipGradients(gradients, clipNorm)

        // Step 2: Add Gaussian noise
        val sigma = clipNorm * kotlin.math.sqrt(2 * kotlin.math.ln(1.25 / delta)) / epsilon
        val random = java.util.Random()

        return FloatArray(clipped.size) { i ->
            clipped[i] + (random.nextGaussian() * sigma).toFloat()
        }
    }

    private fun clipGradients(gradients: FloatArray, maxNorm: Float): FloatArray {
        val norm = kotlin.math.sqrt(gradients.fold(0f) { acc, v -> acc + v * v })

        return if (norm > maxNorm) {
            val scale = maxNorm / norm
            FloatArray(gradients.size) { gradients[it] * scale }
        } else {
            gradients.copyOf()
        }
    }

    // Secure Aggregation — cryptographic protocol
    // (simplified concept — real implementation uses MPC)
    fun generateMask(
        seed: Long,
        size: Int
    ): FloatArray {
        val random = java.util.Random(seed)
        return FloatArray(size) { random.nextGaussian().toFloat() }
    }

    // Each device adds a random mask before sending
    // Server aggregates — masks cancel out
    fun maskUpdate(update: FloatArray, mask: FloatArray): FloatArray {
        return FloatArray(update.size) { update[it] + mask[it] }
    }

    fun unmaskAggregate(
        maskedUpdates: List<FloatArray>,
        masks: List<FloatArray>
    ): FloatArray {
        val n = maskedUpdates.first().size
        // Sum of all masked updates - sum of all masks = sum of true updates
        val result = FloatArray(n)
        for (update in maskedUpdates) {
            for (i in 0 until n) result[i] += update[i]
        }
        for (mask in masks) {
            for (i in 0 until n) result[i] -= mask[i]
        }
        // Average
        for (i in 0 until n) result[i] /= maskedUpdates.size
        return result
    }
}
```

---

## 4. Google's Federated Learning for Mobile

```
Google FL on Android:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Google uses FL in production via:                           │
│  • Private Compute Core (Android 12+)                       │
│  • Federated Compute library                               │
│                                                              │
│  Key Design Decisions:                                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                                                      │   │
│  │ 1. Only train when:                                  │   │
│  │    • Device is idle                                  │   │
│  │    • Charging                                        │   │
│  │    • On WiFi (unmetered network)                    │   │
│  │                                                      │   │
│  │ 2. Minimum participation:                            │   │
│  │    • At least 100-1000 devices per round            │   │
│  │    • Prevents individual identification             │   │
│  │                                                      │   │
│  │ 3. Communication efficiency:                         │   │
│  │    • Quantize updates to 1-2 bits                   │   │
│  │    • Top-K sparsification (only send top 1% of      │   │
│  │      changed weights)                                │   │
│  │    • Compression: 100× reduction                    │   │
│  │                                                      │   │
│  │ 4. Secure aggregation:                               │   │
│  │    • Server never sees individual updates            │   │
│  │    • Only the aggregate is decryptable               │   │
│  │                                                      │   │
│  │ 5. Differential privacy:                             │   │
│  │    • ε ≈ 1-10 per round                             │   │
│  │    • Tracked cumulatively across rounds              │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  Using WorkManager for FL Scheduling:                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Constraints:                                         │   │
│  │  • NetworkType.UNMETERED (WiFi)                     │   │
│  │  • BatteryNotLow                                    │   │
│  │  • DeviceIdle                                       │   │
│  │  • StorageNotLow                                    │   │
│  │                                                      │   │
│  │ Schedule: Periodic (every 4-24 hours)               │   │
│  │ Backoff: Exponential on failure                     │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
import androidx.work.*
import java.util.concurrent.TimeUnit

// WorkManager-based FL scheduler
class FLWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val client = FederatedLearningClient(
            context = applicationContext,
            api = /* inject via Hilt/provider */,
            trainer = OnDeviceTrainer(applicationContext),
            deviceId = getDeviceId()
        )

        // Check if conditions are met
        if (!client.canParticipate()) {
            return Result.retry()  // Try later
        }

        // Get local training data
        val localData = getLocalTrainingData()

        return try {
            client.participateInRound(localData)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun getDeviceId(): String {
        // Anonymous device ID for FL
        val prefs = applicationContext.getSharedPreferences("fl_prefs", Context.MODE_PRIVATE)
        return prefs.getString("device_id", null) ?: run {
            val id = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_id", id).apply()
            id
        }
    }

    private suspend fun getLocalTrainingData(): List<Pair<FloatArray, FloatArray>> {
        // Collect data from local app usage
        return emptyList() // App-specific implementation
    }
}

// Schedule FL participation
class FLScheduler(private val context: Context) {

    fun scheduleFL() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()

        val flWork = PeriodicWorkRequestBuilder<FLWorker>(
            repeatInterval = 6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "federated_learning",
            ExistingPeriodicWorkPolicy.KEEP,
            flWork
        )
    }

    fun cancelFL() {
        WorkManager.getInstance(context).cancelUniqueWork("federated_learning")
    }
}
```
