# Hardware Acceleration — Edge AI

## Overview

Hardware acceleration offloads ML inference from CPU to specialized processors — GPU, DSP, or NPU — for faster, more power-efficient execution.

```
Android ML Hardware Stack:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │                  TensorFlow Lite                    │     │
│  │                  (Inference Engine)                  │     │
│  └────────────────────┬───────────────────────────────┘     │
│                       │                                     │
│          ┌────────────┼──────────────┬──────────────┐      │
│          ▼            ▼              ▼              ▼      │
│  ┌────────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐   │
│  │ CPU        │ │GPU       │ │ NNAPI     │ │ Hexagon  │   │
│  │ Delegate   │ │Delegate  │ │ Delegate  │ │ Delegate │   │
│  │            │ │          │ │           │ │          │   │
│  │ Default    │ │OpenGL ES │ │Abstracts  │ │Qualcomm  │   │
│  │ fallback   │ │/ OpenCL  │ │all HW     │ │DSP-only  │   │
│  │            │ │          │ │           │ │          │   │
│  │ ARM NEON   │ │Adreno/   │ │Routes to  │ │Ultra low │   │
│  │ XNNPACK    │ │Mali/     │ │best HW    │ │power     │   │
│  │            │ │PowerVR   │ │available  │ │          │   │
│  └────────────┘ └──────────┘ └─────┬─────┘ └──────────┘   │
│                                    │                        │
│                    ┌───────────────┼──────────────┐        │
│                    ▼               ▼              ▼        │
│              ┌──────────┐  ┌───────────┐  ┌──────────┐    │
│              │   GPU    │  │   DSP     │  │   NPU    │    │
│              │          │  │           │  │  (if     │    │
│              │ Parallel │  │ Signal    │  │  present)│    │
│              │ compute  │  │ processing│  │          │    │
│              └──────────┘  └───────────┘  └──────────┘    │
│                                                              │
│  Performance Comparison (MobileNet v2, Pixel 6):            │
│  ┌──────────────────┬───────────┬─────────────────┐        │
│  │ Delegate         │ Latency   │ Power Efficiency │        │
│  ├──────────────────┼───────────┼─────────────────┤        │
│  │ CPU (4 threads)  │ 15.3 ms   │ ██████          │        │
│  │ GPU              │ 5.8 ms    │ ████████████    │        │
│  │ NNAPI (GPU)      │ 6.2 ms    │ ████████████    │        │
│  │ NNAPI (DSP)      │ 4.1 ms    │ █████████████   │        │
│  │ NNAPI (NPU)      │ 2.3 ms    │ ██████████████  │        │
│  └──────────────────┴───────────┴─────────────────┘        │
└──────────────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // TFLite core
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // GPU Delegate
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-gpu-api:2.14.0")

    // NNAPI Delegate (included in TFLite core, but explicit for options)
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Hexagon Delegate (Qualcomm devices)
    implementation("org.tensorflow:tensorflow-lite-hexagon:2.14.0")
}
```

---

## 1. GPU Delegates for TensorFlow Lite

```
GPU Delegate Internals:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  How GPU Delegate Works:                                    │
│                                                              │
│  1. Model Analysis                                          │
│     TFLite checks which ops can run on GPU                  │
│                                                              │
│  2. Graph Partitioning                                      │
│     ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐               │
│     │Conv2D │ │ Pool  │ │Custom │ │ Dense │               │
│     │ GPU✅ │ │ GPU✅ │ │ CPU❌ │ │ GPU✅ │               │
│     └───┬───┘ └───┬───┘ └───┬───┘ └───┬───┘               │
│         │         │         │         │                     │
│     ┌───▼─────────▼───┐ ┌──▼──┐ ┌───▼───┐                │
│     │ GPU Subgraph 1  │ │ CPU │ │GPU S2 │                │
│     └─────────────────┘ └─────┘ └───────┘                │
│                                                              │
│  3. Shader Compilation (first run only)                     │
│     Generates OpenGL ES / OpenCL compute shaders           │
│                                                              │
│  4. Execution                                               │
│     Data stays on GPU between ops → minimal transfers       │
│                                                              │
│  Supported Ops (GPU):                                       │
│  ✅ Conv2D, DepthwiseConv2D, TransposeConv                 │
│  ✅ Pool (Avg, Max), Softmax, ReLU, PReLU                  │
│  ✅ Add, Mul, Sub, Concat, Reshape, Pad                    │
│  ✅ FullyConnected (Dense), BatchNorm                      │
│  ❌ Custom ops, some dynamic shapes, LSTM                   │
│                                                              │
│  Best Practices:                                            │
│  • Use FP16 models (GPUs are fast with FP16)               │
│  • Avoid models with many unsupported ops                   │
│  • First inference is slow (shader compilation)             │
│  • Cache compiled shaders with serialization                │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate

class GPUDelegateManager(private val context: Context) {

    private var gpuDelegate: GpuDelegate? = null
    private var interpreter: Interpreter? = null

    // Check GPU compatibility
    fun isGPUAvailable(): Boolean {
        val compatList = CompatibilityList()
        return compatList.isDelegateSupportedOnThisDevice
    }

    // Create interpreter with GPU delegate
    fun createWithGPU(modelPath: String): Interpreter {
        val compatList = CompatibilityList()

        val options = Interpreter.Options().apply {
            if (compatList.isDelegateSupportedOnThisDevice) {
                // GPU available — use it
                val delegateOptions = compatList.bestOptionsForThisDevice
                gpuDelegate = GpuDelegate(delegateOptions)
                addDelegate(gpuDelegate!!)
            } else {
                // Fallback to CPU with XNNPACK
                setNumThreads(4)
            }
        }

        val modelBuffer = loadModel(modelPath)
        interpreter = Interpreter(modelBuffer, options)
        return interpreter!!
    }

    // GPU with FP16 and serialization (cached shaders)
    fun createWithGPUOptimized(
        modelPath: String,
        serializationDir: String? = null
    ): Interpreter {
        val delegateOptions = GpuDelegate.Options().apply {
            // Use FP16 for 2x throughput on GPU
            setPrecisionLossAllowed(true)  // Allow FP16

            // Serialize compiled shaders (faster second startup)
            if (serializationDir != null) {
                setSerializationParams(serializationDir, "$modelPath.gpu_cache")
            }
        }

        gpuDelegate = GpuDelegate(delegateOptions)

        val options = Interpreter.Options().apply {
            addDelegate(gpuDelegate!!)
        }

        val modelBuffer = loadModel(modelPath)
        interpreter = Interpreter(modelBuffer, options)
        return interpreter!!
    }

    fun close() {
        interpreter?.close()
        gpuDelegate?.close()
    }

    private fun loadModel(name: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(name)
        val stream = java.io.FileInputStream(fd.fileDescriptor)
        return stream.channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )
    }
}
```

---

## 2. NNAPI for Heterogeneous Execution

```
NNAPI Architecture:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  NNAPI = Android Neural Networks API                        │
│  Introduced in Android 8.1 (API 27)                         │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │              Your App (TFLite)                      │     │
│  └────────────────────┬───────────────────────────────┘     │
│                       │                                     │
│                       ▼                                     │
│  ┌────────────────────────────────────────────────────┐     │
│  │              NNAPI Delegate                         │     │
│  │   • Translates TFLite ops → NNAPI ops              │     │
│  │   • Partitions model into NNAPI-supported subgraph │     │
│  └────────────────────┬───────────────────────────────┘     │
│                       │                                     │
│                       ▼                                     │
│  ┌────────────────────────────────────────────────────┐     │
│  │         Android NNAPI Runtime                       │     │
│  │   • Vendor-neutral abstraction layer               │     │
│  │   • Handles model compilation & execution           │     │
│  │   • Automatic HW selection                          │     │
│  └────────────────────┬───────────────────────────────┘     │
│                       │                                     │
│         ┌─────────────┼─────────────┐                      │
│         ▼             ▼             ▼                      │
│  ┌──────────┐  ┌───────────┐  ┌──────────┐                │
│  │ Qualcomm │  │ MediaTek  │  │ Samsung  │                │
│  │ HAL      │  │ HAL       │  │ HAL      │  ← Vendor      │
│  │          │  │           │  │          │    drivers       │
│  │ Adreno   │  │ APU       │  │ Exynos   │                │
│  │ GPU +    │  │ (AI       │  │ NPU +    │                │
│  │ Hexagon  │  │ Processing│  │ GPU      │                │
│  │ DSP      │  │ Unit)     │  │          │                │
│  └──────────┘  └───────────┘  └──────────┘                │
│                                                              │
│  NNAPI Versions & Features:                                 │
│  ┌──────────┬─────────────────────────────────────┐        │
│  │ Android  │ NNAPI Features                      │        │
│  ├──────────┼─────────────────────────────────────┤        │
│  │ 8.1 (27) │ Basic ops (Conv, Pool, FC)          │        │
│  │ 10 (29)  │ Control flow, quantized ops         │        │
│  │ 11 (30)  │ Quality of service, caching         │        │
│  │ 12 (31)  │ Priority hints, expanded ops        │        │
│  │ 13 (33)  │ Burst execution, INT16              │        │
│  └──────────┴─────────────────────────────────────┘        │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate

class NNAPIManager(private val context: Context) {

    private var nnApiDelegate: NnApiDelegate? = null
    private var interpreter: Interpreter? = null

    // Check NNAPI availability
    fun isNNAPIAvailable(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1
    }

    // Create with NNAPI
    fun createWithNNAPI(modelPath: String): Interpreter {
        val nnapiOptions = NnApiDelegate.Options().apply {
            // Allow the runtime to choose best accelerator
            setUseNnapiCpu(false)  // Don't fall back to NNAPI CPU impl

            // Execution preference
            setExecutionPreference(NnApiDelegate.Options.EXECUTION_PREFERENCE_SUSTAINED_SPEED)

            // Model caching (faster subsequent loads)
            setCacheDir(context.cacheDir.absolutePath)
            setModelToken(modelPath.hashCode().toString())

            // Allow FP16 computation for better GPU perf
            setAllowFp16(true)
        }

        nnApiDelegate = NnApiDelegate(nnapiOptions)

        val options = Interpreter.Options().apply {
            addDelegate(nnApiDelegate!!)
            // CPU fallback for unsupported ops
            setNumThreads(4)
        }

        val modelBuffer = loadModel(modelPath)
        interpreter = Interpreter(modelBuffer, options)
        return interpreter!!
    }

    // Create with specific accelerator preference
    fun createWithAccelerator(
        modelPath: String,
        acceleratorName: String? = null  // "qti-gpu", "qti-dsp", "google-edgetpu"
    ): Interpreter {
        val nnapiOptions = NnApiDelegate.Options().apply {
            if (acceleratorName != null) {
                setAcceleratorName(acceleratorName)
            }
            setCacheDir(context.cacheDir.absolutePath)
            setModelToken("${modelPath}_${acceleratorName ?: "auto"}")
        }

        nnApiDelegate = NnApiDelegate(nnapiOptions)

        val options = Interpreter.Options().apply {
            addDelegate(nnApiDelegate!!)
        }

        return Interpreter(loadModel(modelPath), options)
    }

    fun close() {
        interpreter?.close()
        nnApiDelegate?.close()
    }

    private fun loadModel(name: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(name)
        val stream = java.io.FileInputStream(fd.fileDescriptor)
        return stream.channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )
    }
}
```

---

## 3. Hexagon DSP Usage

```
Hexagon DSP:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Qualcomm Hexagon DSP — dedicated signal processor          │
│  Found in Snapdragon SoCs (most Android phones)             │
│                                                              │
│  Why Hexagon for ML?                                        │
│  ┌──────────────────────────────────────────────────┐       │
│  │ • Ultra-low power (10× less than CPU)            │       │
│  │ • VLIW architecture — 4 instructions/cycle       │       │
│  │ • HVX (Hexagon Vector eXtensions) — 1024-bit     │       │
│  │ • Fixed-point (INT8/INT16) specialist             │       │
│  │ • ~2-5× faster than CPU for quantized models     │       │
│  │ • Runs even when main CPU is idle (always-on)    │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  Requirements:                                              │
│  • Snapdragon 835+ (Hexagon v62+)                          │
│  • Model MUST be INT8 quantized                             │
│  • libhexagon_nn_skel.so libs (shipped by TFLite)          │
│  • Android 8.1+                                            │
│                                                              │
│  Supported Ops:                                             │
│  ✅ Conv2D, DepthwiseConv2D (quantized)                    │
│  ✅ AveragePool, MaxPool, Concatenation                    │
│  ✅ Add, Mul, Softmax, Logistic, ReLU, ReLU6              │
│  ✅ Reshape, Resize, Transpose, Pad                        │
│  ❌ Float ops, custom ops, most RNN/LSTM                    │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
import org.tensorflow.lite.HexagonDelegate

class HexagonDSPManager(private val context: Context) {

    private var hexagonDelegate: HexagonDelegate? = null
    private var interpreter: Interpreter? = null

    // Check if Hexagon DSP is available
    fun isHexagonAvailable(): Boolean {
        return try {
            HexagonDelegate.isHexagonLibAvailable()
        } catch (e: UnsatisfiedLinkError) {
            false
        }
    }

    // Initialize Hexagon libs (call once at app start)
    fun initialize(): Boolean {
        return try {
            HexagonDelegate.initHexagonDelegate(context)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Create interpreter with Hexagon DSP (INT8 models only)
    fun createWithHexagon(modelPath: String): Interpreter {
        val hexagonOptions = HexagonDelegate.Options().apply {
            setDebugLevel(0)  // 0 = off, 1 = basic, 2 = verbose
        }

        hexagonDelegate = HexagonDelegate(hexagonOptions)

        val options = Interpreter.Options().apply {
            addDelegate(hexagonDelegate!!)
        }

        val modelBuffer = loadModel(modelPath)
        interpreter = Interpreter(modelBuffer, options)
        return interpreter!!
    }

    fun close() {
        interpreter?.close()
        hexagonDelegate?.close()
        HexagonDelegate.closeHexagonDelegate()
    }

    private fun loadModel(name: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(name)
        val stream = java.io.FileInputStream(fd.fileDescriptor)
        return stream.channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )
    }
}
```

---

## 4. Qualcomm Snapdragon Neural Processing Engine

```
Snapdragon NPE (SNPE):
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  SNPE = Qualcomm's proprietary ML inference SDK             │
│  Bypasses NNAPI for direct hardware access                  │
│                                                              │
│  ┌──────────────────────────────────────────────────┐       │
│  │           Your App                                │       │
│  │              │                                    │       │
│  │      ┌───────▼────────┐                          │       │
│  │      │   SNPE SDK     │                          │       │
│  │      │ (C++ / Java)   │                          │       │
│  │      └───────┬────────┘                          │       │
│  │              │                                    │       │
│  │   ┌──────────┼──────────────┐                   │       │
│  │   ▼          ▼              ▼                   │       │
│  │ ┌─────┐  ┌──────┐  ┌───────────┐               │       │
│  │ │ CPU │  │ GPU  │  │ HTP (DSP  │               │       │
│  │ │     │  │Adreno│  │+ HVX +    │               │       │
│  │ │     │  │      │  │ HMX)      │               │       │
│  │ └─────┘  └──────┘  └───────────┘               │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  SNPE vs TFLite+NNAPI:                                     │
│  ┌───────────────┬─────────────────┬────────────────┐      │
│  │               │ SNPE            │ TFLite+NNAPI   │      │
│  ├───────────────┼─────────────────┼────────────────┤      │
│  │ Access        │ Direct HW       │ Via HAL        │      │
│  │ Performance   │ 10-30% faster   │ Good           │      │
│  │ Model format  │ .dlc            │ .tflite        │      │
│  │ Portability   │ Qualcomm only   │ All Android    │      │
│  │ Setup         │ Complex SDK     │ Simple dep     │      │
│  │ Model support │ TF, ONNX, etc   │ TFLite only    │      │
│  │ Maintenance   │ Qualcomm update │ Google update   │      │
│  └───────────────┴─────────────────┴────────────────┘      │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
// SNPE integration via JNI (simplified example)
// Full SNPE requires downloading Qualcomm AI Hub SDK
class SNPEInference(private val context: Context) {

    // JNI native methods (implemented in C++)
    private external fun initSNPE(modelPath: String, runtime: Int): Long
    private external fun runInference(handle: Long, inputData: FloatArray): FloatArray
    private external fun releaseSNPE(handle: Long)

    companion object {
        init {
            System.loadLibrary("snpe_jni")
        }

        // Runtime options
        const val RUNTIME_CPU = 0
        const val RUNTIME_GPU = 1
        const val RUNTIME_DSP = 2
        const val RUNTIME_AIP = 3  // AI Processor (NPU)
    }

    private var handle: Long = 0

    fun initialize(dlcModelPath: String, preferredRuntime: Int = RUNTIME_DSP) {
        // Copy model from assets to files dir
        val modelFile = java.io.File(context.filesDir, "model.dlc")
        if (!modelFile.exists()) {
            context.assets.open(dlcModelPath).use { input ->
                modelFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        handle = initSNPE(modelFile.absolutePath, preferredRuntime)
    }

    fun infer(inputData: FloatArray): FloatArray {
        return runInference(handle, inputData)
    }

    fun release() {
        if (handle != 0L) {
            releaseSNPE(handle)
            handle = 0
        }
    }
}
```

---

## Unified Accelerator Manager

```kotlin
class AcceleratorManager(private val context: Context) {

    enum class AcceleratorType { CPU, GPU, NNAPI, HEXAGON }

    // Auto-select best available accelerator
    fun selectBestAccelerator(modelIsQuantized: Boolean): AcceleratorType {
        if (modelIsQuantized) {
            // Quantized models → prefer Hexagon DSP > NNAPI > CPU
            if (HexagonDelegate.isHexagonLibAvailable()) return AcceleratorType.HEXAGON
            if (android.os.Build.VERSION.SDK_INT >= 27) return AcceleratorType.NNAPI
        } else {
            // Float models → prefer GPU > NNAPI > CPU
            if (CompatibilityList().isDelegateSupportedOnThisDevice) return AcceleratorType.GPU
            if (android.os.Build.VERSION.SDK_INT >= 27) return AcceleratorType.NNAPI
        }
        return AcceleratorType.CPU
    }

    // Create interpreter with best delegate
    fun createInterpreter(
        modelPath: String,
        isQuantized: Boolean = false
    ): Interpreter {
        val accelerator = selectBestAccelerator(isQuantized)
        android.util.Log.d("Accel", "Using: $accelerator")

        val options = Interpreter.Options()

        when (accelerator) {
            AcceleratorType.GPU -> {
                val gpuDelegate = GpuDelegate(GpuDelegate.Options().apply {
                    setPrecisionLossAllowed(true)
                })
                options.addDelegate(gpuDelegate)
            }
            AcceleratorType.NNAPI -> {
                val nnapi = NnApiDelegate(NnApiDelegate.Options().apply {
                    setAllowFp16(!isQuantized)
                    setCacheDir(context.cacheDir.absolutePath)
                    setModelToken(modelPath)
                })
                options.addDelegate(nnapi)
            }
            AcceleratorType.HEXAGON -> {
                HexagonDelegate.initHexagonDelegate(context)
                options.addDelegate(HexagonDelegate(HexagonDelegate.Options()))
            }
            AcceleratorType.CPU -> {
                options.setNumThreads(4)
            }
        }

        val fd = context.assets.openFd(modelPath)
        val stream = java.io.FileInputStream(fd.fileDescriptor)
        val modelBuffer = stream.channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )

        return Interpreter(modelBuffer, options)
    }
}
```
