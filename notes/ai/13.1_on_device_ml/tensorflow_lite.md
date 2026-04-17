# TensorFlow Lite — On-Device Machine Learning

## Overview

TensorFlow Lite (TFLite) is Google's lightweight ML framework for deploying machine learning models on mobile, embedded, and edge devices. It enables **on-device inference** with low latency and small binary size.

```
TensorFlow Lite Architecture:
┌──────────────────────────────────────────────────────────┐
│                    Training Phase                        │
│  ┌────────────┐    ┌──────────────┐    ┌──────────────┐ │
│  │ TensorFlow │───▶│  SavedModel  │───▶│  TFLite      │ │
│  │ / Keras /  │    │  / .h5 /     │    │  Converter   │ │
│  │ PyTorch    │    │  ONNX        │    │              │ │
│  └────────────┘    └──────────────┘    └──────┬───────┘ │
└───────────────────────────────────────────────┼─────────┘
                                                │
                                     ┌──────────▼───────────┐
                                     │   .tflite model      │
                                     │   (FlatBuffer format) │
                                     └──────────┬───────────┘
                                                │
┌───────────────────────────────────────────────┼─────────┐
│                  Inference Phase (Android)     │         │
│                                     ┌─────────▼───────┐ │
│  ┌──────────┐    ┌──────────────┐   │  TFLite Model   │ │
│  │  Input   │───▶│ Interpreter  │◀──│  (.tflite)      │ │
│  │  Data    │    │              │   └─────────────────┘ │
│  └──────────┘    └──────┬───────┘                       │
│                         │                               │
│              ┌──────────▼───────────┐                   │
│              │ Hardware Acceleration │                   │
│              │  ┌─────┐ ┌────────┐  │                   │
│              │  │ CPU │ │  GPU   │  │                   │
│              │  └─────┘ └────────┘  │                   │
│              │  ┌─────┐ ┌────────┐  │                   │
│              │  │NNAPI│ │Hexagon │  │                   │
│              │  └─────┘ └────────┘  │                   │
│              └──────────────────────┘                   │
│                         │                               │
│                  ┌──────▼──────┐                        │
│                  │   Output    │                        │
│                  │  (Results)  │                        │
│                  └─────────────┘                        │
└─────────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
android {
    // Prevent compressing TFLite models
    aaptOptions {
        noCompress("tflite")
    }
}

dependencies {
    // TensorFlow Lite core
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    
    // GPU Delegate
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    
    // GPU Delegate plugin (new API)
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.4")
    
    // NNAPI Delegate
    implementation("org.tensorflow:tensorflow-lite-nnapi:2.14.0")
    
    // Support library
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    
    // Metadata library
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
    
    // Task Library (Vision)
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    
    // Task Library (Text)
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.4")
}
```

---

## 1. Model Conversion from TensorFlow

### Theory

TFLite models are converted from TensorFlow (or other frameworks) using the **TFLite Converter**. The converter transforms the model into a **FlatBuffer** format (`.tflite`) optimized for mobile.

```
Conversion Pipeline:
┌──────────────────┐     ┌──────────────────┐     ┌──────────────┐
│ TensorFlow Model │     │  TFLite          │     │ .tflite      │
│                  │────▶│  Converter       │────▶│  Model       │
│ - SavedModel     │     │                  │     │              │
│ - Keras .h5      │     │ - Quantization   │     │ ~1/4 size    │
│ - Concrete func  │     │ - Op selection   │     │ of original  │
└──────────────────┘     └──────────────────┘     └──────────────┘

Model Size Reduction:
┌──────────────────────────────────────────────┐
│ Original TF Model:     100 MB               │
│ TFLite (float32):       25 MB               │
│ TFLite (float16):       12.5 MB             │
│ TFLite (int8 quant):     6 MB               │
│ TFLite (dynamic range):  6 MB               │
└──────────────────────────────────────────────┘
```

### Python Conversion Code

```python
import tensorflow as tf

# --- Method 1: From SavedModel ---
converter = tf.lite.TFLiteConverter.from_saved_model("saved_model_dir")
tflite_model = converter.convert()

with open("model.tflite", "wb") as f:
    f.write(tflite_model)

# --- Method 2: From Keras model ---
model = tf.keras.models.load_model("my_model.h5")
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

with open("model.tflite", "wb") as f:
    f.write(tflite_model)

# --- Method 3: From Concrete Functions ---
@tf.function(input_signature=[tf.TensorSpec(shape=[1, 224, 224, 3], dtype=tf.float32)])
def predict(image):
    return model(image)

concrete_func = predict.get_concrete_function()
converter = tf.lite.TFLiteConverter.from_concrete_functions([concrete_func])
tflite_model = converter.convert()
```

---

## 2. Quantization for Size Reduction

### Theory

Quantization reduces model size and improves inference speed by converting floating-point weights to lower-precision formats.

```
Quantization Types:

1. DYNAMIC RANGE QUANTIZATION (Easiest)
   ┌──────────────┐         ┌──────────────┐
   │ float32      │  ──▶    │ int8 weights │
   │ weights      │         │ float32 acts │
   │ (4 bytes)    │         │ (~1 byte)    │
   └──────────────┘         └──────────────┘
   Size: ~4x smaller | Speed: 2-3x faster on CPU

2. FULL INTEGER QUANTIZATION (Best speed)
   ┌──────────────┐         ┌──────────────┐
   │ float32      │  ──▶    │ int8 weights │
   │ weights +    │         │ int8 acts    │
   │ activations  │         │ (~1 byte)    │
   └──────────────┘         └──────────────┘
   Size: ~4x smaller | Speed: 3-4x faster | Needs representative dataset

3. FLOAT16 QUANTIZATION (Best accuracy)
   ┌──────────────┐         ┌──────────────┐
   │ float32      │  ──▶    │ float16      │
   │ weights      │         │ weights      │
   │ (4 bytes)    │         │ (2 bytes)    │
   └──────────────┘         └──────────────┘
   Size: ~2x smaller | GPU acceleration friendly

Accuracy vs Size Tradeoff:
                    ┌─────────────────────────┐
  Accuracy  100% ─ │ ████ float32            │
             99% ─ │ ████████ float16        │
             97% ─ │ ████████████ dynamic    │
             95% ─ │ ████████████████ int8   │
                    └─────────────────────────┘
                     100MB  50MB  25MB  12MB
```

### Python Quantization Code

```python
import tensorflow as tf
import numpy as np

# --- Dynamic Range Quantization ---
converter = tf.lite.TFLiteConverter.from_saved_model("saved_model_dir")
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()
# Result: int8 weights, float32 activations

# --- Full Integer Quantization ---
def representative_dataset():
    """Provide sample data for calibration (100-500 samples)"""
    for i in range(100):
        data = np.random.rand(1, 224, 224, 3).astype(np.float32)
        yield [data]

converter = tf.lite.TFLiteConverter.from_saved_model("saved_model_dir")
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.representative_dataset = representative_dataset
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
converter.inference_input_type = tf.int8   # or tf.uint8
converter.inference_output_type = tf.int8  # or tf.uint8
tflite_model = converter.convert()

# --- Float16 Quantization ---
converter = tf.lite.TFLiteConverter.from_saved_model("saved_model_dir")
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.target_spec.supported_types = [tf.float16]
tflite_model = converter.convert()
```

---

## 3. GPU Acceleration with Delegates

### Theory

Delegates offload computation from CPU to specialized hardware accelerators.

```
Delegate Architecture:
┌─────────────────────────────────────────────────┐
│                TFLite Interpreter                │
│  ┌───────────────────────────────────────────┐  │
│  │              Operation Graph              │  │
│  │  ┌────┐  ┌────┐  ┌────┐  ┌────┐  ┌────┐ │  │
│  │  │Op 1│─▶│Op 2│─▶│Op 3│─▶│Op 4│─▶│Op 5│ │  │
│  │  └──┬─┘  └──┬─┘  └──┬─┘  └──┬─┘  └──┬─┘ │  │
│  └─────┼───────┼───────┼───────┼───────┼────┘  │
│        │       │       │       │       │        │
│   ┌────▼───────▼──┐  ┌─▼──────▼───────▼────┐  │
│   │  CPU Default  │  │   GPU Delegate       │  │
│   │  (Op 1, 2)    │  │   (Op 3, 4, 5)      │  │
│   └───────────────┘  └─────────────────────┘   │
└─────────────────────────────────────────────────┘

Hardware Accelerators Available:
┌──────────────┬──────────────────────────────────┐
│ Delegate     │ Details                          │
├──────────────┼──────────────────────────────────┤
│ CPU          │ Default, all ops supported       │
│ GPU          │ OpenGL/OpenCL, float16/32        │
│ NNAPI        │ Android 8.1+, vendor-specific    │
│ Hexagon DSP  │ Qualcomm chips only              │
│ CoreML       │ iOS only (Apple Neural Engine)   │
│ XNNPACK      │ Optimized CPU (default in 2.14)  │
└──────────────┴──────────────────────────────────┘

Performance Comparison (Typical):
┌──────────────────────────────────────────────────┐
│                                                  │
│  CPU:    ██████████████████████  100ms            │
│  GPU:    ████████              40ms              │
│  NNAPI:  ██████                30ms              │
│  XNN:    ████████████          55ms              │
│                                                  │
└──────────────────────────────────────────────────┘
```

### Code

```kotlin
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.nnapi.NnApiDelegate

class DelegateHelper(private val context: Context) {

    // --- GPU Delegate ---
    fun createGpuInterpreter(modelPath: String): Interpreter? {
        val compatList = CompatibilityList()

        if (compatList.isDelegateSupportedOnThisDevice) {
            val gpuDelegate = GpuDelegate(compatList.bestOptionsForThisDevice)

            val options = Interpreter.Options()
                .addDelegate(gpuDelegate)
                .setNumThreads(4)

            val modelFile = loadModelFile(modelPath)
            return Interpreter(modelFile, options)
        } else {
            Log.w("GPU", "GPU delegate not supported, falling back to CPU")
            return createCpuInterpreter(modelPath)
        }
    }

    // --- NNAPI Delegate ---
    fun createNnapiInterpreter(modelPath: String): Interpreter {
        val nnApiDelegate = NnApiDelegate(
            NnApiDelegate.Options().apply {
                setAllowFp16(true)
                setUseNnapiCpu(false)  // Don't fall back to NNAPI CPU
                // setAcceleratorName("google-edgetpu")  // Target specific accelerator
            }
        )

        val options = Interpreter.Options()
            .addDelegate(nnApiDelegate)

        val modelFile = loadModelFile(modelPath)
        return Interpreter(modelFile, options)
    }

    // --- CPU with XNNPACK ---
    fun createCpuInterpreter(modelPath: String): Interpreter {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            setUseXNNPACK(true)  // Optimized CPU kernels
        }

        val modelFile = loadModelFile(modelPath)
        return Interpreter(modelFile, options)
    }

    // --- Auto-select best delegate ---
    fun createOptimalInterpreter(modelPath: String): Interpreter {
        val compatList = CompatibilityList()
        val modelFile = loadModelFile(modelPath)

        val options = Interpreter.Options().apply {
            if (compatList.isDelegateSupportedOnThisDevice) {
                addDelegate(GpuDelegate(compatList.bestOptionsForThisDevice))
                Log.d("TFLite", "Using GPU delegate")
            } else {
                setNumThreads(4)
                setUseXNNPACK(true)
                Log.d("TFLite", "Using CPU with XNNPACK")
            }
        }

        return Interpreter(modelFile, options)
    }

    private fun loadModelFile(modelPath: String): java.nio.MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val fileInputStream = java.io.FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        return fileChannel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        )
    }
}
```

---

## 4. Neural Networks API (NNAPI) Integration

### Theory

NNAPI is an Android C API (available from API 27+) that provides hardware-accelerated inference using the device's ML accelerators (GPU, DSP, NPU).

```
NNAPI Architecture:
┌─────────────────────────────────────────────────┐
│ Android App                                     │
│  ┌─────────────────────────────────────────┐    │
│  │ TensorFlow Lite                          │    │
│  │  ┌──────────────────────────────────┐   │    │
│  │  │     NNAPI Delegate               │   │    │
│  │  └───────────────┬──────────────────┘   │    │
│  └──────────────────┼──────────────────────┘    │
│                     │                            │
│  ┌──────────────────▼──────────────────────┐    │
│  │        Android NNAPI Runtime            │    │
│  └──────────────────┬──────────────────────┘    │
│                     │                            │
│  ┌──────────────────▼──────────────────────┐    │
│  │        Vendor HAL Drivers               │    │
│  │  ┌──────┐  ┌──────┐  ┌──────────────┐  │    │
│  │  │ GPU  │  │ DSP  │  │ NPU/TPU/APU  │  │    │
│  │  └──────┘  └──────┘  └──────────────┘  │    │
│  └─────────────────────────────────────────┘    │
└─────────────────────────────────────────────────┘

NNAPI Device Selection:
  Android 10+ allows querying and selecting specific accelerators.
  Each vendor implements their own drivers with different op support.
```

### Code

```kotlin
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate

class NnapiHelper(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var nnApiDelegate: NnApiDelegate? = null

    fun initialize(modelPath: String) {
        // Configure NNAPI
        val nnApiOptions = NnApiDelegate.Options().apply {
            setAllowFp16(true)                    // Allow float16 computation
            setUseNnapiCpu(false)                  // Don't use NNAPI CPU reference
            setExecutionPreference(                // Optimize for:
                NnApiDelegate.Options.EXECUTION_PREFERENCE_SUSTAINED_SPEED
            )
            // EXECUTION_PREFERENCE_LOW_POWER        → Battery saving
            // EXECUTION_PREFERENCE_FAST_SINGLE_ANSWER → Lowest latency
            // EXECUTION_PREFERENCE_SUSTAINED_SPEED    → Sustained throughput
        }

        nnApiDelegate = NnApiDelegate(nnApiOptions)

        val options = Interpreter.Options().apply {
            addDelegate(nnApiDelegate!!)
        }

        val modelBuffer = loadModelFile(modelPath)
        interpreter = Interpreter(modelBuffer, options)

        // Log input/output tensor info
        val inputTensor = interpreter!!.getInputTensor(0)
        val outputTensor = interpreter!!.getOutputTensor(0)
        Log.d("NNAPI", """
            Input shape: ${inputTensor.shape().contentToString()}
            Input type: ${inputTensor.dataType()}
            Output shape: ${outputTensor.shape().contentToString()}
            Output type: ${outputTensor.dataType()}
        """.trimIndent())
    }

    fun runInference(inputData: FloatArray): FloatArray {
        val outputData = FloatArray(1000) // Adjust to your model
        interpreter?.run(inputData, outputData)
        return outputData
    }

    fun close() {
        interpreter?.close()
        nnApiDelegate?.close()
    }

    private fun loadModelFile(modelPath: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(modelPath)
        val input = java.io.FileInputStream(fd.fileDescriptor)
        return input.channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
    }
}
```

---

## 5. Interpreter Usage in Android

### Theory

The `Interpreter` is the core class for running TFLite models. It loads a model and runs inference.

```
Interpreter Lifecycle:
┌─────────────┐    ┌──────────────┐    ┌──────────────┐    ┌─────────┐
│ Load Model  │───▶│ Create       │───▶│ Allocate     │───▶│ Run     │
│ (.tflite)   │    │ Interpreter  │    │ Tensors      │    │ Inference│
└─────────────┘    └──────────────┘    └──────────────┘    └────┬────┘
                                                                │
                   ┌──────────────┐    ┌──────────────┐    ┌────▼────┐
                   │Close/Release │◀───│ Process      │◀───│ Read    │
                   │ Resources    │    │ Output       │    │ Output  │
                   └──────────────┘    └──────────────┘    └─────────┘

Input/Output Tensor Types:
┌────────────────────────────────────────────────────┐
│                                                    │
│  Input:   ByteBuffer / float[] / Bitmap            │
│           Shape: [batch, height, width, channels]  │
│           e.g., [1, 224, 224, 3]                   │
│                                                    │
│  Output:  float[][] / ByteBuffer                   │
│           Shape: [batch, num_classes]               │
│           e.g., [1, 1000] for ImageNet             │
│                                                    │
└────────────────────────────────────────────────────┘
```

### Code

```kotlin
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null

    // Model parameters
    private val modelInputSize = 224
    private val modelNumClasses = 1000
    private val pixelSize = 3  // RGB
    private val quantized = false

    fun initialize(modelFileName: String = "mobilenet_v2.tflite") {
        val modelFile = loadModelFile(modelFileName)

        val options = Interpreter.Options().apply {
            setNumThreads(4)
            setUseXNNPACK(true)
        }

        interpreter = Interpreter(modelFile, options)

        // Resize input if needed
        interpreter?.resizeInput(0, intArrayOf(1, modelInputSize, modelInputSize, pixelSize))
        interpreter?.allocateTensors()

        // Print tensor info
        logTensorInfo()
    }

    private fun logTensorInfo() {
        interpreter?.let { interp ->
            val inputCount = interp.inputTensorCount
            val outputCount = interp.outputTensorCount
            Log.d("TFLite", "Input tensors: $inputCount, Output tensors: $outputCount")

            for (i in 0 until inputCount) {
                val tensor = interp.getInputTensor(i)
                Log.d("TFLite", "Input[$i]: shape=${tensor.shape().contentToString()}, " +
                    "type=${tensor.dataType()}, bytes=${tensor.numBytes()}")
            }

            for (i in 0 until outputCount) {
                val tensor = interp.getOutputTensor(i)
                Log.d("TFLite", "Output[$i]: shape=${tensor.shape().contentToString()}, " +
                    "type=${tensor.dataType()}, bytes=${tensor.numBytes()}")
            }
        }
    }

    // --- Basic inference with float array ---
    fun classifyWithArray(bitmap: Bitmap): List<Pair<Int, Float>> {
        val resized = Bitmap.createScaledBitmap(bitmap, modelInputSize, modelInputSize, true)
        val input = bitmapToFloatArray(resized)
        val output = Array(1) { FloatArray(modelNumClasses) }

        interpreter?.run(input, output)

        return output[0].mapIndexed { index, confidence ->
            index to confidence
        }.sortedByDescending { it.second }.take(5)
    }

    // --- Inference with ByteBuffer (more efficient) ---
    fun classifyWithByteBuffer(bitmap: Bitmap): List<Pair<Int, Float>> {
        val resized = Bitmap.createScaledBitmap(bitmap, modelInputSize, modelInputSize, true)
        val inputBuffer = bitmapToByteBuffer(resized)
        val outputBuffer = Array(1) { FloatArray(modelNumClasses) }

        interpreter?.run(inputBuffer, outputBuffer)

        return outputBuffer[0].mapIndexed { index, confidence ->
            index to confidence
        }.sortedByDescending { it.second }.take(5)
    }

    // --- Multi-input/output with runForMultipleInputsOutputs ---
    fun multiInputInference(input1: ByteBuffer, input2: ByteBuffer): Map<Int, Any> {
        val inputs = arrayOf(input1, input2)
        val outputs = mutableMapOf<Int, Any>()

        // Prepare output buffers for each output tensor
        val output0 = Array(1) { FloatArray(modelNumClasses) }
        val output1 = Array(1) { FloatArray(4) }  // e.g., bounding box

        outputs[0] = output0
        outputs[1] = output1

        interpreter?.runForMultipleInputsOutputs(inputs, outputs)

        return outputs
    }

    // Convert Bitmap to ByteBuffer
    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteSize = if (quantized) 1 else 4  // uint8 vs float32
        val buffer = ByteBuffer.allocateDirect(
            1 * modelInputSize * modelInputSize * pixelSize * byteSize
        )
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(modelInputSize * modelInputSize)
        bitmap.getPixels(pixels, 0, modelInputSize, 0, 0, modelInputSize, modelInputSize)

        for (pixel in pixels) {
            if (quantized) {
                buffer.put(((pixel shr 16) and 0xFF).toByte())  // R
                buffer.put(((pixel shr 8) and 0xFF).toByte())   // G
                buffer.put((pixel and 0xFF).toByte())            // B
            } else {
                // Normalize to [-1, 1] for MobileNet
                buffer.putFloat(((pixel shr 16) and 0xFF) / 127.5f - 1.0f)  // R
                buffer.putFloat(((pixel shr 8) and 0xFF) / 127.5f - 1.0f)   // G
                buffer.putFloat((pixel and 0xFF) / 127.5f - 1.0f)            // B
            }
        }

        buffer.rewind()
        return buffer
    }

    // Convert Bitmap to float array
    private fun bitmapToFloatArray(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val input = Array(1) {
            Array(modelInputSize) {
                Array(modelInputSize) {
                    FloatArray(pixelSize)
                }
            }
        }

        for (y in 0 until modelInputSize) {
            for (x in 0 until modelInputSize) {
                val pixel = bitmap.getPixel(x, y)
                input[0][y][x][0] = ((pixel shr 16) and 0xFF) / 255.0f  // R
                input[0][y][x][1] = ((pixel shr 8) and 0xFF) / 255.0f   // G
                input[0][y][x][2] = (pixel and 0xFF) / 255.0f            // B
            }
        }

        return input
    }

    private fun loadModelFile(modelPath: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(modelPath)
        val input = java.io.FileInputStream(fd.fileDescriptor)
        return input.channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
    }

    fun close() {
        interpreter?.close()
    }
}
```

---

## 6. Custom Operations

### Theory

When a model uses ops not supported by TFLite's built-in set, you can use **Select TensorFlow Ops** or implement custom ops.

```
Op Resolution Strategy:
┌──────────────────────────────────────────────────┐
│ Model Operation                                  │
│       │                                          │
│       ▼                                          │
│ ┌──────────────┐                                 │
│ │ Built-in     │── Yes ──▶ Use TFLite kernel     │
│ │ TFLite Op?   │                                 │
│ └──────┬───────┘                                 │
│        │ No                                      │
│        ▼                                         │
│ ┌──────────────┐                                 │
│ │ Select TF    │── Yes ──▶ Use TF kernel         │
│ │ Op enabled?  │          (larger binary)        │
│ └──────┬───────┘                                 │
│        │ No                                      │
│        ▼                                         │
│ ┌──────────────┐                                 │
│ │ Custom Op    │── Yes ──▶ Use custom impl       │
│ │ registered?  │                                 │
│ └──────┬───────┘                                 │
│        │ No                                      │
│        ▼                                         │
│   ❌ Error: Unsupported op                       │
└──────────────────────────────────────────────────┘
```

### Python — Enable Select TF Ops

```python
# During conversion, allow Select TF Ops
converter = tf.lite.TFLiteConverter.from_saved_model("saved_model_dir")
converter.target_spec.supported_ops = [
    tf.lite.OpsSet.TFLITE_BUILTINS,        # Default TFLite ops
    tf.lite.OpsSet.SELECT_TF_OPS            # Enable select TF ops
]
tflite_model = converter.convert()
```

### Kotlin — Use Select TF Ops

```kotlin
// app/build.gradle.kts — add select ops dependency
dependencies {
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")
}

// Select TF ops are automatically registered when the library is included.
// No code changes needed — just add the dependency.
```

---

## 7. Model Maker for Creating Custom Models

### Theory

TFLite Model Maker simplifies training custom TFLite models with transfer learning.

```
Model Maker Workflow:
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌────────────┐
│ Your Dataset │───▶│ Pre-trained  │───▶│ Fine-tune    │───▶│ Export     │
│ (labeled     │    │ Base Model   │    │ (transfer    │    │ .tflite    │
│  images/text)│    │ (EfficientNet│    │  learning)   │    │ model     │
│              │    │  /BERT/etc.) │    │              │    │           │
└──────────────┘    └──────────────┘    └──────────────┘    └────────────┘

Supported Tasks:
┌──────────────────────────────────────────────┐
│ Image Classification  │ Audio Classification │
│ Object Detection      │ Text Classification  │
│ Image Segmentation    │ Recommendation       │
│ Question Answering    │ Searcher             │
└──────────────────────────────────────────────┘
```

### Python — Create Custom Image Classifier

```python
# pip install tflite-model-maker
from tflite_model_maker import image_classifier
from tflite_model_maker.image_classifier import DataLoader

# Prepare data (folder structure: dataset/class_name/images)
# dataset/
#   ├── cats/
#   │   ├── cat1.jpg
#   │   └── cat2.jpg
#   └── dogs/
#       ├── dog1.jpg
#       └── dog2.jpg

data = DataLoader.from_folder("dataset/")
train_data, test_data = data.split(0.9)

# Create and train model (EfficientNet by default)
model = image_classifier.create(
    train_data,
    model_spec='efficientnet_lite0',  # Options: lite0-lite4, mobilenet_v2
    epochs=10,
    batch_size=32
)

# Evaluate
loss, accuracy = model.evaluate(test_data)
print(f"Test accuracy: {accuracy:.4f}")

# Export to TFLite
model.export(export_dir="exported_model/")
# Creates: model.tflite + labels.txt
```

### Python — Custom Text Classifier

```python
from tflite_model_maker import text_classifier
from tflite_model_maker.text_classifier import DataLoader

# CSV format: text, label
data = DataLoader.from_csv(
    filename="reviews.csv",
    text_column="review",
    label_column="sentiment",
    delimiter=","
)

train_data, test_data = data.split(0.9)

# Average Word Embedding (fast, small)
model = text_classifier.create(
    train_data,
    model_spec='average_word_vec',
    epochs=20
)

# Or use BERT (more accurate, larger)
# model = text_classifier.create(
#     train_data,
#     model_spec='mobilebert',
#     epochs=5
# )

model.export(export_dir="text_model/")
```

---

## 8. Metadata and Signatures

### Theory

TFLite Metadata describes what a model expects as input and produces as output, enabling auto-code generation and easier integration.

```
Model Metadata Structure:
┌─────────────────────────────────────────────────┐
│                .tflite model                     │
│  ┌──────────────────────────────────────────┐   │
│  │ Model Buffer (weights + graph)            │   │
│  └──────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────┐   │
│  │ Metadata (JSON-like, stored in FlatBuffer)│   │
│  │                                           │   │
│  │  - Model name & description               │   │
│  │  - Version, author, license               │   │
│  │  - Input tensor metadata:                 │   │
│  │    - Name: "image"                        │   │
│  │    - Type: IMAGE                          │   │
│  │    - Normalization params: mean, std      │   │
│  │    - Color space: RGB                     │   │
│  │  - Output tensor metadata:                │   │
│  │    - Name: "probability"                  │   │
│  │    - Type: TENSOR_AXIS_LABELS             │   │
│  │    - Associated file: labels.txt          │   │
│  │                                           │   │
│  └──────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────┐   │
│  │ Associated Files (packed inside model)    │   │
│  │  - labels.txt                             │   │
│  │  - vocab.txt                              │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

### Python — Add Metadata

```python
from tflite_support import metadata as _metadata
from tflite_support import metadata_schema_py_generated as _metadata_fb
import flatbuffers

# Create model metadata
model_meta = _metadata_fb.ModelMetadataT()
model_meta.name = "MobileNet V2 Image Classifier"
model_meta.description = "Classifies images into 1000 ImageNet categories"
model_meta.version = "v1.0"
model_meta.author = "Your Name"
model_meta.license = "Apache License 2.0"

# Input metadata
input_meta = _metadata_fb.TensorMetadataT()
input_meta.name = "image"
input_meta.description = "Input image (224x224 RGB)"
input_meta.content = _metadata_fb.ContentT()
input_meta.content.contentProperties = _metadata_fb.ImagePropertiesT()
input_meta.content.contentProperties.colorSpace = _metadata_fb.ColorSpaceType.RGB
input_meta.content.contentPropertiesType = _metadata_fb.ContentProperties.ImageProperties

# Normalization
input_normalization = _metadata_fb.ProcessUnitT()
input_normalization.optionsType = _metadata_fb.ProcessUnitOptions.NormalizationOptions
input_normalization.options = _metadata_fb.NormalizationOptionsT()
input_normalization.options.mean = [127.5]
input_normalization.options.std = [127.5]
input_meta.processUnits = [input_normalization]

# Output metadata
output_meta = _metadata_fb.TensorMetadataT()
output_meta.name = "probability"
output_meta.description = "Classification probabilities"

# Associate labels file
label_file = _metadata_fb.AssociatedFileT()
label_file.name = "labels.txt"
label_file.type = _metadata_fb.AssociatedFileType.TENSOR_AXIS_LABELS
output_meta.associatedFiles = [label_file]

# Pack metadata into model
model_meta.subgraphMetadata = [_metadata_fb.SubGraphMetadataT()]
model_meta.subgraphMetadata[0].inputTensorMetadata = [input_meta]
model_meta.subgraphMetadata[0].outputTensorMetadata = [output_meta]

# Write metadata
populator = _metadata.MetadataPopulator.with_model_file("model.tflite")
populator.load_metadata_buffer(
    _metadata.MetadataPopulator.create_model_metadata(model_meta)
)
populator.load_associated_files(["labels.txt"])
populator.populate()
```

### Kotlin — Read Metadata

```kotlin
import org.tensorflow.lite.support.metadata.MetadataExtractor

class MetadataReader(private val context: Context) {

    fun readModelMetadata(modelPath: String) {
        val modelBuffer = loadModelFile(modelPath)
        val extractor = MetadataExtractor(modelBuffer)

        if (extractor.hasMetadata()) {
            // Model info
            val metadata = extractor.modelMetadata
            Log.d("Meta", "Model: ${metadata?.name()}")
            Log.d("Meta", "Description: ${metadata?.description()}")
            Log.d("Meta", "Version: ${metadata?.version()}")

            // Input tensor metadata
            val inputCount = extractor.inputTensorCount
            for (i in 0 until inputCount) {
                val inputMeta = extractor.getInputTensorMetadata(i)
                Log.d("Meta", "Input[$i]: ${inputMeta?.name()}")
            }

            // Output tensor metadata
            val outputCount = extractor.outputTensorCount
            for (i in 0 until outputCount) {
                val outputMeta = extractor.getOutputTensorMetadata(i)
                Log.d("Meta", "Output[$i]: ${outputMeta?.name()}")
            }

            // Read associated files (e.g., labels)
            val labelsFile = extractor.getAssociatedFile("labels.txt")
            val labels = labelsFile?.bufferedReader()?.readLines() ?: emptyList()
            Log.d("Meta", "Labels: ${labels.take(5)}")
        } else {
            Log.d("Meta", "No metadata found in model")
        }
    }

    private fun loadModelFile(modelPath: String): java.nio.ByteBuffer {
        val fd = context.assets.openFd(modelPath)
        val input = java.io.FileInputStream(fd.fileDescriptor)
        val channel = input.channel
        return channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
    }
}
```

---

## Complete Image Classification Example

```kotlin
class ImageClassifierApp(private val context: Context) {

    private lateinit var interpreter: Interpreter
    private lateinit var labels: List<String>

    fun setup() {
        // Load model
        val modelBuffer = loadModelFile("mobilenet_v2.tflite")
        
        // Configure interpreter
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            val compatList = CompatibilityList()
            if (compatList.isDelegateSupportedOnThisDevice) {
                addDelegate(GpuDelegate(compatList.bestOptionsForThisDevice))
            }
        }
        interpreter = Interpreter(modelBuffer, options)

        // Load labels
        labels = context.assets.open("labels.txt")
            .bufferedReader()
            .readLines()
    }

    fun classify(bitmap: Bitmap): List<ClassificationResult> {
        // Preprocess
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = preprocessBitmap(resized)

        // Run inference
        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(input, output)

        // Post-process
        return output[0]
            .mapIndexed { idx, conf -> ClassificationResult(labels[idx], conf) }
            .sortedByDescending { it.confidence }
            .take(5)
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(224 * 224)
        bitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)

        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16) and 0xFF) / 127.5f - 1.0f)
            buffer.putFloat(((pixel shr 8) and 0xFF) / 127.5f - 1.0f)
            buffer.putFloat((pixel and 0xFF) / 127.5f - 1.0f)
        }
        buffer.rewind()
        return buffer
    }

    private fun loadModelFile(path: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(path)
        return java.io.FileInputStream(fd.fileDescriptor).channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength
        )
    }

    fun close() = interpreter.close()
}

data class ClassificationResult(val label: String, val confidence: Float)
```
