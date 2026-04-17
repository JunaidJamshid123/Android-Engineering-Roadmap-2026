# Inference Optimization — Edge AI

## Overview

Inference optimization ensures ML models run efficiently on mobile — through batching, thread pool management, memory mapping, and model caching strategies.

```
Inference Optimization Areas:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │ Batching   │  │ Threading  │  │ Memory     │            │
│  │            │  │            │  │            │            │
│  │ Group      │  │ Right #    │  │ mmap() for │            │
│  │ requests   │  │ of threads │  │ zero-copy  │            │
│  │ → amortize │  │ → avoid    │  │ model load │            │
│  │ overhead   │  │ contention │  │            │            │
│  └────────────┘  └────────────┘  └────────────┘            │
│                                                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │ Caching    │  │ Warm-up    │  │ Pipeline   │            │
│  │            │  │            │  │            │            │
│  │ Keep model │  │ Run dummy  │  │ Overlap    │            │
│  │ in memory  │  │ inference  │  │ preprocess │            │
│  │ across     │  │ at startup │  │ + inference│            │
│  │ requests   │  │ to prime   │  │ + postproc │            │
│  └────────────┘  └────────────┘  └────────────┘            │
│                                                              │
│  Latency Breakdown (typical):                               │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Model Load:    ████████████████████  30-500ms    │       │
│  │ Preprocess:    ████                   5-20ms     │       │
│  │ Inference:     ████████               10-50ms    │       │
│  │ Postprocess:   ██                     2-10ms     │       │
│  │                                                  │       │
│  │ Cold start:    ██████████████████████████████████ │       │
│  │ Warm:          ██████████████                     │       │
│  │ Cached+Warm:   ████████                           │       │
│  └──────────────────────────────────────────────────┘       │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. Batching Strategies

```
Batching Theory:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Without batching (one-at-a-time):                         │
│  Request1 → [Infer] → Result1                              │
│  Request2 →          [Infer] → Result2                     │
│  Request3 →                  [Infer] → Result3             │
│  Total: 3 × overhead + 3 × inference                       │
│                                                              │
│  With batching:                                             │
│  Request1 ─┐                                               │
│  Request2 ─┼─▶ [Batch Infer] → Results (1,2,3)           │
│  Request3 ─┘                                               │
│  Total: 1 × overhead + 1 × batch inference                 │
│                                                              │
│  On Mobile — Dynamic Batching:                             │
│  • Collect requests within a time window (e.g., 50ms)      │
│  • Batch up to max_batch_size                              │
│  • Process together (GPU excels at batches)                │
│  • Return individual results                               │
│                                                              │
│  When batching helps:                                      │
│  ✅ Multiple images to classify (gallery scan)             │
│  ✅ Real-time video frames (batch N frames)                │
│  ✅ Multiple text inputs (search suggestions)              │
│  ❌ Single user request (no benefit)                        │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class BatchedInferenceEngine(
    private val interpreter: org.tensorflow.lite.Interpreter,
    private val maxBatchSize: Int = 8,
    private val maxWaitMs: Long = 50  // Max time to collect batch
) {
    data class InferenceRequest(
        val input: FloatArray,
        val result: CompletableDeferred<FloatArray>
    )

    private val requestChannel = Channel<InferenceRequest>(Channel.BUFFERED)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        // Background batch processor
        scope.launch {
            val batch = mutableListOf<InferenceRequest>()

            while (true) {
                // Collect requests up to batch size or timeout
                val first = requestChannel.receive()
                batch.add(first)

                val deadline = System.currentTimeMillis() + maxWaitMs
                while (batch.size < maxBatchSize) {
                    val remaining = deadline - System.currentTimeMillis()
                    if (remaining <= 0) break

                    val next = withTimeoutOrNull(remaining) {
                        requestChannel.receive()
                    } ?: break

                    batch.add(next)
                }

                // Process batch
                processBatch(batch)
                batch.clear()
            }
        }
    }

    // Submit single inference request (returns when batch completes)
    suspend fun infer(input: FloatArray): FloatArray {
        val request = InferenceRequest(input, CompletableDeferred())
        requestChannel.send(request)
        return request.result.await()
    }

    private fun processBatch(batch: List<InferenceRequest>) {
        if (batch.size == 1) {
            // Single request — no batching overhead
            val output = runSingleInference(batch[0].input)
            batch[0].result.complete(output)
            return
        }

        // Batch all inputs into single array
        val inputSize = batch[0].input.size
        val outputShape = interpreter.getOutputTensor(0).shape()
        val outputSize = outputShape.last()

        // Create batched input [batchSize, ...inputShape]
        val batchedInput = Array(batch.size) { batch[it].input }
        val batchedOutput = Array(batch.size) { FloatArray(outputSize) }

        try {
            // Resize input tensor for batch
            interpreter.resizeInput(0, intArrayOf(batch.size) + interpreter.getInputTensor(0).shape().drop(1).toIntArray())
            interpreter.allocateTensors()
            interpreter.run(batchedInput, batchedOutput)

            // Distribute results
            batch.forEachIndexed { index, request ->
                request.result.complete(batchedOutput[index])
            }
        } catch (e: Exception) {
            // Fallback: process individually
            batch.forEach { request ->
                try {
                    val output = runSingleInference(request.input)
                    request.result.complete(output)
                } catch (ex: Exception) {
                    request.result.completeExceptionally(ex)
                }
            }
        }
    }

    private fun runSingleInference(input: FloatArray): FloatArray {
        val outputShape = interpreter.getOutputTensor(0).shape()
        val output = Array(1) { FloatArray(outputShape.last()) }
        interpreter.run(arrayOf(input), output)
        return output[0]
    }

    fun close() {
        scope.cancel()
        requestChannel.close()
    }
}
```

---

## 2. Thread Pool Management

```
Threading for TFLite:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  CPU Inference Threading:                                    │
│                                                              │
│  Thread count impact on performance:                        │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Threads │ Latency (MobileNet) │ CPU Load │ Note │       │
│  ├─────────┼─────────────────────┼──────────┤──────│       │
│  │    1    │      45 ms          │   25%    │      │       │
│  │    2    │      28 ms          │   50%    │      │       │
│  │    4    │      18 ms          │  100%    │ Best │       │
│  │    8    │      20 ms          │  100%    │ Worse│       │
│  └─────────┴─────────────────────┴──────────┘──────┘       │
│                                                              │
│  Rules:                                                     │
│  • # threads = # big cores (typically 4)                    │
│  • More threads ≠ faster (contention above core count)     │
│  • bg tasks: use fewer threads (don't starve UI)           │
│  • GPU delegate: thread count irrelevant (GPU has own)     │
│                                                              │
│  Thread Affinity (big.LITTLE):                             │
│  ┌──────────────────────────────────────┐                  │
│  │ Big cores   (A78/X4) — fast, power   │                  │
│  │ Little cores (A55)   — slow, efficient│                  │
│  │                                       │                  │
│  │ For latency: Pin to big cores        │                  │
│  │ For battery: Use little cores        │                  │
│  └──────────────────────────────────────┘                  │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class ThreadOptimizedInference(private val context: Context) {

    // Detect optimal thread count
    fun getOptimalThreadCount(): Int {
        val processors = Runtime.getRuntime().availableProcessors()
        // Use big cores only (typically half the cores on big.LITTLE)
        return (processors / 2).coerceIn(2, 4)
    }

    // Create interpreter with optimized threading
    fun createOptimizedInterpreter(modelPath: String): org.tensorflow.lite.Interpreter {
        val options = org.tensorflow.lite.Interpreter.Options().apply {
            setNumThreads(getOptimalThreadCount())

            // Use XNNPACK delegate (optimized CPU delegate)
            // Automatically enabled in recent TFLite versions
            setUseXNNPACK(true)
        }

        val fd = context.assets.openFd(modelPath)
        val stream = java.io.FileInputStream(fd.fileDescriptor)
        val modelBuffer = stream.channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )

        return org.tensorflow.lite.Interpreter(modelBuffer, options)
    }

    // Run inference off main thread with proper dispatcher
    suspend fun runInference(
        interpreter: org.tensorflow.lite.Interpreter,
        input: Any,
        output: Any
    ) = withContext(Dispatchers.Default) {  // Use Default, not IO (CPU-bound)
        interpreter.run(input, output)
    }

    // Dedicated inference thread pool (avoid contention)
    private val inferenceDispatcher = Dispatchers.Default.limitedParallelism(1)

    // Serialize inference to avoid concurrent TFLite calls
    // (TFLite Interpreter is NOT thread-safe)
    suspend fun runInferenceSerialized(
        interpreter: org.tensorflow.lite.Interpreter,
        input: Any,
        output: Any
    ) = withContext(inferenceDispatcher) {
        interpreter.run(input, output)
    }
}
```

---

## 3. Memory Mapping

```
Memory Mapping for Model Loading:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Traditional Load:                                          │
│  ┌──────────┐  read()  ┌──────────┐  copy  ┌──────────┐  │
│  │ Disk     │ ────────▶│ Kernel   │ ──────▶│ App      │  │
│  │ (model)  │          │ Buffer   │        │ Heap     │  │
│  └──────────┘          └──────────┘        └──────────┘  │
│  → 2 copies, heap allocation, GC pressure                  │
│                                                              │
│  Memory Mapped (mmap):                                      │
│  ┌──────────┐  mmap()  ┌──────────────────────────┐       │
│  │ Disk     │ ────────▶│ Virtual Memory            │       │
│  │ (model)  │          │ (pages loaded on demand)  │       │
│  └──────────┘          └──────────────────────────┘       │
│  → Zero copy, no heap allocation, OS manages pages         │
│                                                              │
│  Benefits:                                                  │
│  ┌──────────────────────────────────────────────────┐       │
│  │ • Zero-copy: file pages map directly to memory  │       │
│  │ • Lazy loading: pages loaded only when accessed  │       │
│  │ • No GC pressure: not on Java heap              │       │
│  │ • Shared: multiple interpreters share same pages│       │
│  │ • Large models: can exceed available RAM        │       │
│  │ • Fast startup: model "loads" instantly         │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  Loading Time Comparison:                                   │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Model Size │ read() time │ mmap() time │ Speedup│       │
│  ├────────────┼─────────────┼─────────────┼────────┤       │
│  │ 5 MB       │ 15 ms       │ <1 ms       │ 15×    │       │
│  │ 25 MB      │ 80 ms       │ <1 ms       │ 80×    │       │
│  │ 100 MB     │ 350 ms      │ <1 ms       │ 350×   │       │
│  └────────────┴─────────────┴─────────────┴────────┘       │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class MemoryMappedModelLoader(private val context: Context) {

    // Memory-mapped loading from assets (standard TFLite approach)
    fun loadFromAssets(modelName: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(modelName)
        val inputStream = java.io.FileInputStream(fd.fileDescriptor)
        val channel = inputStream.channel
        return channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
    }

    // Memory-mapped loading from file (e.g., downloaded model)
    fun loadFromFile(file: java.io.File): java.nio.MappedByteBuffer {
        val inputStream = java.io.FileInputStream(file)
        val channel = inputStream.channel
        return channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            0,
            file.length()
        )
    }

    // Direct ByteBuffer for input tensors (avoids heap allocation)
    fun createDirectBuffer(size: Int): java.nio.ByteBuffer {
        return java.nio.ByteBuffer.allocateDirect(size).apply {
            order(java.nio.ByteOrder.nativeOrder())
        }
    }

    // Pre-allocate reusable I/O buffers
    class ReusableBuffers(
        inputSize: Int,
        outputSize: Int
    ) {
        val inputBuffer: java.nio.ByteBuffer = java.nio.ByteBuffer
            .allocateDirect(inputSize * 4) // 4 bytes per float
            .apply { order(java.nio.ByteOrder.nativeOrder()) }

        val outputBuffer: java.nio.ByteBuffer = java.nio.ByteBuffer
            .allocateDirect(outputSize * 4)
            .apply { order(java.nio.ByteOrder.nativeOrder()) }

        fun resetInput() = inputBuffer.rewind()
        fun resetOutput() = outputBuffer.rewind()
        fun resetAll() { resetInput(); resetOutput() }
    }
}
```

---

## 4. Model Caching

```
Model Caching Strategy:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Levels of Caching:                                         │
│                                                              │
│  L1: Interpreter Cache                                      │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Keep interpreter alive across inference calls    │       │
│  │ Avoid re-parsing model graph                     │       │
│  │ Biggest win: eliminates cold-start per call      │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  L2: Delegate Compilation Cache                             │
│  ┌──────────────────────────────────────────────────┐       │
│  │ GPU: Cache compiled shaders (OpenGL programs)    │       │
│  │ NNAPI: Cache compiled model                      │       │
│  │ Hexagon: Cache DSP program                       │       │
│  │ Eliminates GPU warm-up (200-1000ms savings)      │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  L3: Model File Cache                                       │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Downloaded models cached to local storage        │       │
│  │ Version check before re-download                 │       │
│  │ LRU eviction when storage is low                 │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  Startup Flow:                                              │
│  ┌──────────────────────────────────────────────┐           │
│  │ App Start                                    │           │
│  │    │                                         │           │
│  │    ▼                                         │           │
│  │ Model in file cache? ──No──▶ Download model │           │
│  │    │ Yes                         │           │           │
│  │    ▼                             ▼           │           │
│  │ Load via mmap (instant)    Save to cache    │           │
│  │    │                             │           │           │
│  │    ▼                             │           │           │
│  │ Delegate cache exists? ◀─────────┘           │           │
│  │    │ Yes           │ No                      │           │
│  │    ▼               ▼                         │           │
│  │ Fast init      Compile+Cache delegate        │           │
│  │ (~10ms)        (~200-1000ms, once only)      │           │
│  └──────────────────────────────────────────────┘           │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class ModelCacheManager(private val context: Context) {

    private val modelDir = java.io.File(context.filesDir, "ml_models")
    private val interpreterCache = mutableMapOf<String, org.tensorflow.lite.Interpreter>()

    init {
        modelDir.mkdirs()
    }

    // L1: Interpreter cache — reuse across calls
    fun getOrCreateInterpreter(
        modelKey: String,
        modelLoader: () -> java.nio.MappedByteBuffer,
        optionsBuilder: (org.tensorflow.lite.Interpreter.Options) -> Unit = {}
    ): org.tensorflow.lite.Interpreter {
        return interpreterCache.getOrPut(modelKey) {
            val options = org.tensorflow.lite.Interpreter.Options()
            optionsBuilder(options)
            val model = modelLoader()
            val interpreter = org.tensorflow.lite.Interpreter(model, options)

            // Warm up — first inference allocates internal buffers
            warmUp(interpreter)

            interpreter
        }
    }

    // Warm-up run to eliminate first-inference latency
    private fun warmUp(interpreter: org.tensorflow.lite.Interpreter) {
        val inputShape = interpreter.getInputTensor(0).shape()
        val inputSize = inputShape.reduce { a, b -> a * b }
        val dummyInput = java.nio.ByteBuffer.allocateDirect(inputSize * 4)
            .apply { order(java.nio.ByteOrder.nativeOrder()) }

        val outputShape = interpreter.getOutputTensor(0).shape()
        val outputSize = outputShape.reduce { a, b -> a * b }
        val dummyOutput = java.nio.ByteBuffer.allocateDirect(outputSize * 4)
            .apply { order(java.nio.ByteOrder.nativeOrder()) }

        interpreter.run(dummyInput, dummyOutput)
    }

    // L3: File-level model caching for downloaded models
    suspend fun getOrDownloadModel(
        modelUrl: String,
        modelName: String,
        expectedVersion: String
    ): java.io.File = withContext(Dispatchers.IO) {
        val modelFile = java.io.File(modelDir, modelName)
        val versionFile = java.io.File(modelDir, "$modelName.version")

        // Check if cached version matches
        val cachedVersion = if (versionFile.exists()) versionFile.readText() else ""

        if (modelFile.exists() && cachedVersion == expectedVersion) {
            return@withContext modelFile
        }

        // Download new model
        val url = java.net.URL(modelUrl)
        url.openStream().use { input ->
            modelFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        versionFile.writeText(expectedVersion)

        modelFile
    }

    // Release specific interpreter
    fun releaseInterpreter(modelKey: String) {
        interpreterCache.remove(modelKey)?.close()
    }

    // Release all
    fun releaseAll() {
        interpreterCache.values.forEach { it.close() }
        interpreterCache.clear()
    }

    // Cleanup old cached models (LRU by last modified)
    fun cleanupCache(maxCacheSizeMB: Long = 200) {
        val maxBytes = maxCacheSizeMB * 1024 * 1024
        val files = modelDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return

        var totalSize = 0L
        for (file in files) {
            totalSize += file.length()
            if (totalSize > maxBytes) {
                file.delete()
            }
        }
    }
}
```

---

## Complete Pipeline

```kotlin
class OptimizedMLPipeline(private val context: Context) {

    private val cacheManager = ModelCacheManager(context)
    private val threadOptimizer = ThreadOptimizedInference(context)
    private val loader = MemoryMappedModelLoader(context)
    private var batchEngine: BatchedInferenceEngine? = null

    // Initialize optimized pipeline
    fun initialize(modelPath: String, enableBatching: Boolean = false) {
        val interpreter = cacheManager.getOrCreateInterpreter(
            modelKey = modelPath,
            modelLoader = { loader.loadFromAssets(modelPath) },
            optionsBuilder = { options ->
                options.setNumThreads(threadOptimizer.getOptimalThreadCount())
            }
        )

        if (enableBatching) {
            batchEngine = BatchedInferenceEngine(interpreter)
        }
    }

    // Run optimized inference
    suspend fun infer(input: FloatArray): FloatArray {
        return batchEngine?.infer(input) ?: withContext(Dispatchers.Default) {
            val interpreter = cacheManager.getOrCreateInterpreter("default",
                modelLoader = { throw IllegalStateException("Not initialized") })
            val output = Array(1) { FloatArray(10) }
            interpreter.run(arrayOf(input), output)
            output[0]
        }
    }

    fun release() {
        batchEngine?.close()
        cacheManager.releaseAll()
    }
}
```
