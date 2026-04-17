# Local LLM Integration — On-Device Generative AI

## Overview

Running Large Language Models (LLMs) directly on Android devices provides **privacy, offline access, and zero latency**. This uses quantized models (GGUF format) with llama.cpp C++ inference engine.

```
Local LLM Architecture:
┌──────────────────────────────────────────────────────────┐
│                     Android App                           │
│  ┌────────────────────────────────────────────────────┐  │
│  │                 Kotlin/Java Layer                   │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │           JNI Bindings                        │  │  │
│  │  └────────────────────┬─────────────────────────┘  │  │
│  │                       │ Native calls                │  │
│  │  ┌────────────────────▼─────────────────────────┐  │  │
│  │  │        llama.cpp C++ Library                  │  │  │
│  │  │  ┌─────────────────────────────────────────┐ │  │  │
│  │  │  │ Inference Engine                         │ │  │  │
│  │  │  │  • Token sampling                       │ │  │  │
│  │  │  │  • KV cache management                  │ │  │  │
│  │  │  │  • GGUF model loading                   │ │  │  │
│  │  │  └─────────────┬───────────────────────────┘ │  │  │
│  │  │                │                              │  │  │
│  │  │  ┌─────────────▼───────────────────────────┐ │  │  │
│  │  │  │ Hardware Acceleration                    │ │  │  │
│  │  │  │  • CPU (NEON SIMD)                      │ │  │  │
│  │  │  │  • GPU (Vulkan/OpenCL)                  │ │  │  │
│  │  │  │  • NPU (vendor-specific)                │ │  │  │
│  │  │  └─────────────────────────────────────────┘ │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │              GGUF Model File                       │  │
│  │  ┌──────────────────────────────────────────────┐ │  │
│  │  │  Model: Llama 3, Mistral, Phi, Gemma, etc.  │ │  │
│  │  │  Quantization: Q4_K_M (~4 bits per weight)  │ │  │
│  │  │  Size: 2-7 GB (depending on model/quant)    │ │  │
│  │  └──────────────────────────────────────────────┘ │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘

Model Size vs Quality:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Quantization   │ Size (7B)  │ Quality  │ Speed     │
│  ───────────────┼────────────┼──────────┼────────── │
│  FP16 (16-bit)  │ ~14 GB     │ ████████ │ Slow      │
│  Q8_0 (8-bit)   │  ~7 GB     │ ███████  │ Medium    │
│  Q5_K_M (5-bit) │  ~5 GB     │ ██████   │ Fast      │
│  Q4_K_M (4-bit) │  ~4 GB     │ █████    │ Fast      │
│  Q3_K_M (3-bit) │  ~3 GB     │ ████     │ Fastest   │
│  Q2_K (2-bit)   │  ~2.5 GB   │ ███      │ Fastest   │
│                                                      │
│  Recommended for mobile: Q4_K_M or Q4_K_S           │
│  Recommended model sizes: 1B - 7B parameters        │
│                                                      │
│  Device Requirements (7B Q4):                        │
│  • RAM: 6+ GB free                                   │
│  • Storage: 4+ GB for model                          │
│  • CPU: Snapdragon 8 Gen 2+ / Tensor G3+            │
│  • Speed: ~5-15 tokens/sec                           │
└──────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        ndk {
            abiFilters += listOf("arm64-v8a")  // 64-bit ARM only (recommended)
        }
    }
    
    // Enable prefab for native libraries
    buildFeatures {
        prefab = true
    }
}

dependencies {
    // llama.cpp Android bindings
    // Option 1: Use a community wrapper
    implementation("com.github.anthropics:llama-android:0.1.0")
    
    // Option 2: Build llama.cpp from source (recommended for control)
    // See build instructions below
    
    // Coroutines for async inference
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

---

## 1. GGUF Models with llama.cpp Android Bindings

### Theory

```
GGUF (GPT-Generated Unified Format):
┌──────────────────────────────────────────────────────┐
│                                                      │
│  GGUF File Structure:                                │
│  ┌──────────────────────────────────────────────┐    │
│  │ Header                                        │    │
│  │  • Magic number                               │    │
│  │  • Version                                    │    │
│  │  • Tensor count                               │    │
│  │  • Metadata count                             │    │
│  ├──────────────────────────────────────────────┤    │
│  │ Metadata (key-value pairs)                    │    │
│  │  • Architecture (llama, mistral, etc.)        │    │
│  │  • Context length                             │    │
│  │  • Embedding size                             │    │
│  │  • Vocabulary size                            │    │
│  │  • Tokenizer data                             │    │
│  ├──────────────────────────────────────────────┤    │
│  │ Tensor Data (quantized weights)               │    │
│  │  • Stored in specified quantization format    │    │
│  │  • Memory-mapped for efficient loading        │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  Popular Models for Mobile (2026):                   │
│  ┌──────────────────────────────────────────────┐    │
│  │ Phi-3 Mini (3.8B)     │ Q4_K_M ~2.2 GB      │    │
│  │ Gemma 2B              │ Q4_K_M ~1.5 GB      │    │
│  │ Llama 3.2 3B          │ Q4_K_M ~1.8 GB      │    │
│  │ Mistral 7B            │ Q4_K_M ~4.1 GB      │    │
│  │ TinyLlama 1.1B       │ Q4_K_M ~0.7 GB      │    │
│  │ SmolLM 1.7B           │ Q4_K_M ~1.0 GB      │    │
│  └──────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────┘
```

### Building llama.cpp for Android (CMakeLists)

```cmake
# app/src/main/cpp/CMakeLists.txt
cmake_minimum_required(VERSION 3.22)
project(llama-android)

# Set llama.cpp source directory
set(LLAMA_CPP_DIR ${CMAKE_SOURCE_DIR}/llama.cpp)

# Build llama.cpp as a library
add_subdirectory(${LLAMA_CPP_DIR} llama.cpp)

# JNI binding library
add_library(llama-android SHARED
    llama-android.cpp
)

target_link_libraries(llama-android
    llama
    common
    log
)

target_include_directories(llama-android PRIVATE
    ${LLAMA_CPP_DIR}
    ${LLAMA_CPP_DIR}/common
)
```

### JNI Binding (C++)

```cpp
// app/src/main/cpp/llama-android.cpp
#include <jni.h>
#include <string>
#include <android/log.h>
#include "llama.h"
#include "common.h"

#define TAG "LlamaAndroid"
#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

static llama_model *model = nullptr;
static llama_context *ctx = nullptr;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_llm_LlamaModel_loadModel(
    JNIEnv *env, jobject thiz, jstring model_path, jint n_ctx, jint n_gpu_layers) {
    
    const char *path = env->GetStringUTFChars(model_path, nullptr);
    
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = n_gpu_layers;
    
    model = llama_load_model_from_file(path, model_params);
    env->ReleaseStringUTFChars(model_path, path);
    
    if (!model) {
        LOG_INFO("Failed to load model");
        return 0;
    }
    
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = n_ctx;
    ctx_params.n_threads = 4;
    ctx_params.n_threads_batch = 4;
    
    ctx = llama_new_context_with_model(model, ctx_params);
    
    if (!ctx) {
        LOG_INFO("Failed to create context");
        llama_free_model(model);
        return 0;
    }
    
    LOG_INFO("Model loaded successfully, ctx_size=%d", n_ctx);
    return reinterpret_cast<jlong>(model);
}

JNIEXPORT jstring JNICALL
Java_com_example_llm_LlamaModel_generate(
    JNIEnv *env, jobject thiz, jstring prompt_str, jint max_tokens, 
    jfloat temperature, jfloat top_p) {
    
    const char *prompt = env->GetStringUTFChars(prompt_str, nullptr);
    
    // Tokenize
    std::vector<llama_token> tokens = llama_tokenize(ctx, prompt, true);
    env->ReleaseStringUTFChars(prompt_str, prompt);
    
    // Evaluate prompt tokens
    llama_batch batch = llama_batch_get_one(tokens.data(), tokens.size(), 0, 0);
    llama_decode(ctx, batch);
    
    // Generate tokens
    std::string result;
    int n_cur = tokens.size();
    
    for (int i = 0; i < max_tokens; i++) {
        // Sample next token
        llama_token new_token = llama_sample_token(ctx, temperature, top_p);
        
        // Check for end of generation
        if (llama_token_is_eog(model, new_token)) break;
        
        // Convert token to text
        char buf[128];
        int n = llama_token_to_piece(model, new_token, buf, sizeof(buf), 0, true);
        result.append(buf, n);
        
        // Prepare next batch
        batch = llama_batch_get_one(&new_token, 1, n_cur, 0);
        llama_decode(ctx, batch);
        n_cur++;
    }
    
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_example_llm_LlamaModel_freeModel(JNIEnv *env, jobject thiz) {
    if (ctx) { llama_free(ctx); ctx = nullptr; }
    if (model) { llama_free_model(model); model = nullptr; }
}

} // extern "C"
```

### Kotlin Wrapper

```kotlin
class LlamaModel {

    companion object {
        init {
            System.loadLibrary("llama-android")
        }
    }

    // Native methods
    private external fun loadModel(modelPath: String, contextSize: Int, gpuLayers: Int): Long
    private external fun generate(prompt: String, maxTokens: Int, temperature: Float, topP: Float): String
    private external fun freeModel()

    private var modelPtr: Long = 0

    fun load(modelPath: String, contextSize: Int = 2048, gpuLayers: Int = 0): Boolean {
        modelPtr = loadModel(modelPath, contextSize, gpuLayers)
        return modelPtr != 0L
    }

    fun generateText(
        prompt: String,
        maxTokens: Int = 256,
        temperature: Float = 0.7f,
        topP: Float = 0.9f
    ): String {
        if (modelPtr == 0L) throw IllegalStateException("Model not loaded")
        return generate(prompt, maxTokens, temperature, topP)
    }

    fun close() {
        freeModel()
        modelPtr = 0
    }
}
```

---

## 2. Optimized Inference on Mobile

### Theory

```
Mobile Inference Optimization Strategies:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  1. QUANTIZATION                                     │
│     • Use Q4_K_M for best size/quality balance       │
│     • Q4_K_S slightly smaller, slightly worse        │
│     • Never use FP16 on mobile (too large)           │
│                                                      │
│  2. CONTEXT LENGTH                                   │
│     • Smaller context = less memory                  │
│     • 2048 tokens recommended for mobile             │
│     • Each token uses ~2MB RAM (for 7B model)        │
│                                                      │
│  3. BATCH SIZE                                       │
│     • Batch prompt evaluation for speed              │
│     • Batch size of 512 is good default              │
│                                                      │
│  4. THREADING                                        │
│     • Use performance cores (big cores)              │
│     • 4 threads optimal for most devices             │
│     • Don't use all cores (saves battery)            │
│                                                      │
│  5. MEMORY MAPPING (mmap)                            │
│     • Map model file directly into memory            │
│     • OS handles page loading automatically          │
│     • Reduces initial load time                      │
│                                                      │
│  6. KV CACHE QUANTIZATION                            │
│     • Quantize KV cache to Q8_0 or Q4_0             │
│     • Reduces memory by 2-4x                        │
│     • Slight quality loss                            │
│                                                      │
│  Memory Usage Breakdown (7B Q4_K_M, 2048 ctx):      │
│  ┌────────────────────────────────────────────┐      │
│  │ Model weights:     ~4.0 GB                 │      │
│  │ KV cache (FP16):   ~1.0 GB                 │      │
│  │ KV cache (Q8_0):   ~0.5 GB                 │      │
│  │ Scratch buffers:   ~0.2 GB                 │      │
│  │ Total:             ~4.7-5.2 GB             │      │
│  └────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class OptimizedLlamaInference(private val context: Context) {

    private val llamaModel = LlamaModel()

    // Check device capabilities
    fun canRunModel(modelSizeMB: Long): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val availableMemoryMB = memoryInfo.availMem / (1024 * 1024)
        val totalMemoryMB = memoryInfo.totalMem / (1024 * 1024)

        Log.d("LLM", "Available RAM: ${availableMemoryMB}MB, Total: ${totalMemoryMB}MB")

        // Need at least 1.5x model size in available RAM
        return availableMemoryMB > modelSizeMB * 1.5
    }

    // Download model to internal storage
    suspend fun downloadModel(url: String, filename: String): File {
        val modelDir = File(context.filesDir, "models")
        modelDir.mkdirs()
        val modelFile = File(modelDir, filename)

        if (modelFile.exists()) return modelFile

        withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .build()

            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            response.body?.byteStream()?.use { input ->
                modelFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var totalBytesRead = 0L
                    val contentLength = response.body?.contentLength() ?: -1L

                    while (true) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) break
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (contentLength > 0) {
                            val progress = (totalBytesRead * 100 / contentLength).toInt()
                            Log.d("LLM", "Download: $progress%")
                        }
                    }
                }
            }
        }

        return modelFile
    }

    // Load with optimal settings
    fun loadOptimal(modelPath: String) {
        val loaded = llamaModel.load(
            modelPath = modelPath,
            contextSize = 2048,  // Conservative for mobile
            gpuLayers = 0        // CPU-only (GPU varies by device)
        )

        if (!loaded) {
            throw RuntimeException("Failed to load model")
        }
    }

    // Generate with streaming callback
    suspend fun generateStreaming(
        prompt: String,
        maxTokens: Int = 256,
        onToken: (String) -> Unit
    ) = withContext(Dispatchers.Default) {
        // Format prompt with chat template
        val formattedPrompt = formatChatPrompt(prompt)

        val response = llamaModel.generateText(
            prompt = formattedPrompt,
            maxTokens = maxTokens,
            temperature = 0.7f,
            topP = 0.9f
        )

        // In a real implementation, tokens would be streamed via callback
        onToken(response)
    }

    // Format for different chat templates
    private fun formatChatPrompt(userMessage: String): String {
        // Llama 3 chat template
        return """<|begin_of_text|><|start_header_id|>system<|end_header_id|>

You are a helpful assistant.<|eot_id|><|start_header_id|>user<|end_header_id|>

$userMessage<|eot_id|><|start_header_id|>assistant<|end_header_id|>

"""
    }

    fun close() = llamaModel.close()
}
```

---

## 3. LoRA Adapters for Customization

### Theory

```
LoRA (Low-Rank Adaptation):
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Instead of fine-tuning all model weights (large),   │
│  LoRA trains small "adapter" matrices that modify    │
│  the model's behavior.                               │
│                                                      │
│  Original Weight Matrix W (frozen):                  │
│  ┌──────────────┐                                    │
│  │              │  Size: d × d (e.g., 4096 × 4096)   │
│  │      W       │  = 16M parameters                  │
│  │   (frozen)   │                                    │
│  └──────────────┘                                    │
│         +                                            │
│  LoRA Adapter (trainable):                           │
│  ┌──────┐   ┌──────┐                                │
│  │  A   │ × │  B   │  Size: d × r + r × d           │
│  │(d×r) │   │(r×d) │  r = rank (e.g., 16)           │
│  └──────┘   └──────┘  = 2 × 4096 × 16 = 131K params │
│                                                      │
│  Output = W·x + A·B·x (adapter adds to original)    │
│                                                      │
│  Benefits for Mobile:                                │
│  ┌──────────────────────────────────────────────┐    │
│  │ • Adapter file is tiny: 10-100 MB            │    │
│  │ • Base model stays the same                  │    │
│  │ • Swap adapters for different tasks          │    │
│  │ • Multiple adapters for same base model      │    │
│  │ • No need to store multiple full models      │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  Example: One 4GB base model + adapters:             │
│  Base Model (4GB) + Medical LoRA (50MB)              │
│                   + Legal LoRA (50MB)                 │
│                   + Code LoRA (50MB)                  │
│  Instead of 3 × 4GB = 12GB                          │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class LoRAHelper(private val context: Context) {

    private val llamaModel = LlamaModel()

    // Load base model + LoRA adapter
    fun loadWithLoRA(baseModelPath: String, loraPath: String) {
        // llama.cpp supports loading LoRA adapters
        // The native code needs to call llama_model_apply_lora_from_file
        llamaModel.load(baseModelPath, contextSize = 2048)
        // Apply LoRA adapter
        // llamaModel.applyLoRA(loraPath, scale = 1.0f)
    }

    // Manage multiple adapters
    data class LoRAAdapter(
        val name: String,
        val path: String,
        val description: String,
        val sizeMB: Long
    )

    fun listAvailableAdapters(): List<LoRAAdapter> {
        val adapterDir = File(context.filesDir, "lora_adapters")
        if (!adapterDir.exists()) return emptyList()

        return adapterDir.listFiles()
            ?.filter { it.extension == "gguf" }
            ?.map { file ->
                LoRAAdapter(
                    name = file.nameWithoutExtension,
                    path = file.absolutePath,
                    description = "LoRA adapter: ${file.nameWithoutExtension}",
                    sizeMB = file.length() / (1024 * 1024)
                )
            } ?: emptyList()
    }
}
```

---

## 4. Memory Management for Large Models

### Theory

```
Memory Management Strategies:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  1. MEMORY-MAPPED I/O (mmap)                        │
│     ┌────────────────────────────────────────────┐   │
│     │ Model file on storage                       │   │
│     │ ↕ Memory-mapped (OS pages in/out)           │   │
│     │ Only active pages in RAM                    │   │
│     └────────────────────────────────────────────┘   │
│                                                      │
│  2. PROGRESSIVE LOADING                              │
│     Load model layers incrementally                  │
│     Show progress to user                            │
│                                                      │
│  3. BACKGROUND LOADING                               │
│     Use WorkManager / coroutines                     │
│     Don't block UI thread                            │
│                                                      │
│  4. MEMORY PRESSURE HANDLING                         │
│     Monitor ComponentCallbacks2.onTrimMemory()       │
│     Release model when memory is low                 │
│                                                      │
│  5. MODEL LIFECYCLE                                  │
│     Load on demand, release when not needed          │
│     Tie to Activity/Service lifecycle                │
│                                                      │
│  Android Process Limits:                             │
│  ┌──────────────────────────────────────┐            │
│  │ Typical app heap: 256-512 MB        │            │
│  │ Native memory: Limited by device RAM │            │
│  │ Total process: Usually < 4 GB       │            │
│  │ Model uses native memory (not heap) │            │
│  └──────────────────────────────────────┘            │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class LLMMemoryManager(private val context: Context) : ComponentCallbacks2 {

    private var llamaModel: LlamaModel? = null
    private var isModelLoaded = false

    init {
        context.registerComponentCallbacks(this)
    }

    // Check if model can fit in memory
    fun checkMemoryForModel(modelSizeMB: Long): MemoryStatus {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val availMB = memInfo.availMem / (1024 * 1024)
        val totalMB = memInfo.totalMem / (1024 * 1024)
        val requiredMB = modelSizeMB * 1.3  // 30% overhead

        return when {
            availMB < requiredMB -> MemoryStatus.INSUFFICIENT
            availMB < requiredMB * 1.5 -> MemoryStatus.TIGHT
            else -> MemoryStatus.OK
        }
    }

    enum class MemoryStatus { OK, TIGHT, INSUFFICIENT }

    // Load model in background with progress
    suspend fun loadModelAsync(
        modelPath: String,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)

            llamaModel = LlamaModel()
            onProgress(0.3f)

            val loaded = llamaModel!!.load(
                modelPath = modelPath,
                contextSize = 2048
            )
            onProgress(0.9f)

            isModelLoaded = loaded
            onProgress(1.0f)

            return@withContext loaded
        } catch (e: OutOfMemoryError) {
            Log.e("LLM", "OOM when loading model", e)
            releaseModel()
            return@withContext false
        }
    }

    // Generate with memory safety
    suspend fun generateSafely(prompt: String): Result<String> {
        if (!isModelLoaded || llamaModel == null) {
            return Result.failure(IllegalStateException("Model not loaded"))
        }

        return try {
            val result = withContext(Dispatchers.Default) {
                llamaModel!!.generateText(prompt)
            }
            Result.success(result)
        } catch (e: OutOfMemoryError) {
            releaseModel()
            Result.failure(e)
        }
    }

    // Handle memory pressure
    override fun onTrimMemory(level: Int) {
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w("LLM", "Memory pressure! Level: $level")
                // Could reduce context or release model
            }
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                Log.w("LLM", "Critical memory! Releasing model.")
                releaseModel()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {}
    override fun onLowMemory() {
        Log.w("LLM", "Low memory callback! Releasing model.")
        releaseModel()
    }

    fun releaseModel() {
        llamaModel?.close()
        llamaModel = null
        isModelLoaded = false
        System.gc()  // Suggest GC
        Log.d("LLM", "Model released")
    }

    fun destroy() {
        releaseModel()
        context.unregisterComponentCallbacks(this)
    }
}

// Foreground Service for long inference
class LLMInferenceService : Service() {

    private var memoryManager: LLMMemoryManager? = null

    override fun onCreate() {
        super.onCreate()
        memoryManager = LLMMemoryManager(this)

        // Create notification for foreground service
        val notification = NotificationCompat.Builder(this, "llm_channel")
            .setContentTitle("AI Processing")
            .setContentText("Running AI model...")
            .setSmallIcon(R.drawable.ic_ai)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        memoryManager?.destroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
```

---

## Complete Local LLM Chat App

```kotlin
class LocalLLMViewModel(application: Application) : AndroidViewModel(application) {

    private val memoryManager = LLMMemoryManager(application)

    data class ChatMessage(val text: String, val isUser: Boolean)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _modelStatus = MutableStateFlow("Not loaded")
    val modelStatus: StateFlow<String> = _modelStatus.asStateFlow()

    fun loadModel() {
        viewModelScope.launch {
            _modelStatus.value = "Loading model..."
            _isLoading.value = true

            val modelFile = File(getApplication<Application>().filesDir, "models/phi3-mini-q4.gguf")
            if (!modelFile.exists()) {
                _modelStatus.value = "Model file not found"
                _isLoading.value = false
                return@launch
            }

            val memStatus = memoryManager.checkMemoryForModel(modelFile.length() / (1024 * 1024))
            if (memStatus == LLMMemoryManager.MemoryStatus.INSUFFICIENT) {
                _modelStatus.value = "Insufficient memory"
                _isLoading.value = false
                return@launch
            }

            val success = memoryManager.loadModelAsync(modelFile.absolutePath) { progress ->
                _modelStatus.value = "Loading: ${(progress * 100).toInt()}%"
            }

            _modelStatus.value = if (success) "Model ready" else "Failed to load"
            _isLoading.value = false
        }
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            _messages.value += ChatMessage(userMessage, isUser = true)
            _isLoading.value = true

            val result = memoryManager.generateSafely(userMessage)
            result.onSuccess { response ->
                _messages.value += ChatMessage(response, isUser = false)
            }.onFailure { error ->
                _messages.value += ChatMessage("Error: ${error.message}", isUser = false)
            }

            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        memoryManager.destroy()
    }
}
```
