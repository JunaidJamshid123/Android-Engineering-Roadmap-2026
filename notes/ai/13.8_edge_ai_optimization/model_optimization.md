# Model Optimization — Edge AI

## Overview

Model optimization reduces model size and inference latency for on-device deployment. The four main techniques are quantization, pruning, knowledge distillation, and neural architecture search.

```
Model Optimization Landscape:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Original Model: 100 MB, 50ms inference, FP32              │
│                                                              │
│  ┌─────────────────────────────────────────────────┐        │
│  │          Optimization Techniques                 │        │
│  │                                                  │        │
│  │  1. QUANTIZATION           2. PRUNING            │        │
│  │  ┌──────────────────┐     ┌──────────────────┐  │        │
│  │  │ FP32 → INT8      │     │ Remove zero/low  │  │        │
│  │  │                  │     │ weight connections│  │        │
│  │  │ 4× smaller       │     │                  │  │        │
│  │  │ 2-3× faster      │     │ 2-10× smaller    │  │        │
│  │  │ ~1-2% accuracy ↓ │     │ ~1-5% accuracy ↓ │  │        │
│  │  └──────────────────┘     └──────────────────┘  │        │
│  │                                                  │        │
│  │  3. DISTILLATION          4. NAS                 │        │
│  │  ┌──────────────────┐     ┌──────────────────┐  │        │
│  │  │ Large teacher →   │     │ Auto-search for  │  │        │
│  │  │ Small student     │     │ optimal arch.    │  │        │
│  │  │                  │     │                  │  │        │
│  │  │ Custom size       │     │ Best accuracy/   │  │        │
│  │  │ ~0-3% accuracy ↓ │     │ speed tradeoff   │  │        │
│  │  └──────────────────┘     └──────────────────┘  │        │
│  └─────────────────────────────────────────────────┘        │
│                                                              │
│  After all optimizations: 5 MB, 8ms inference, INT8         │
│                                                              │
│  Size Reduction Visual:                                     │
│  ┌──────────────────────────┐                               │
│  │ Original (FP32)          │ 100 MB                        │
│  │ ████████████████████████ │                               │
│  │                          │                               │
│  │ FP16 Quantized           │ 50 MB                         │
│  │ ████████████             │                               │
│  │                          │                               │
│  │ INT8 Quantized           │ 25 MB                         │
│  │ ██████                   │                               │
│  │                          │                               │
│  │ INT8 + Pruned            │ 8 MB                          │
│  │ ██                       │                               │
│  │                          │                               │
│  │ INT8 + Pruned + Distill  │ 3 MB                          │
│  │ █                        │                               │
│  └──────────────────────────┘                               │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. Quantization

### Theory

```
Quantization Types:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  DYNAMIC RANGE QUANTIZATION (Post-Training)                 │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ • Weights: FP32 → INT8 at conversion time           │   │
│  │ • Activations: Quantized dynamically at runtime      │   │
│  │ • Easiest to apply — just a flag                     │   │
│  │ • ~2-3× speedup on CPU                              │   │
│  │ • No calibration data needed                         │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  FULL INTEGER QUANTIZATION (Post-Training)                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ • Weights AND activations: FP32 → INT8               │   │
│  │ • Needs representative dataset for calibration       │   │
│  │ • 4× smaller, 3× faster                             │   │
│  │ • Works with GPU/NNAPI/DSP delegates                 │   │
│  │                                                      │   │
│  │ Calibration: Run sample data through model to        │   │
│  │ measure activation ranges → determine scale/offset   │   │
│  │                                                      │   │
│  │ quantized = round(float_val / scale) + zero_point   │   │
│  │ float_val = (quantized - zero_point) × scale        │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  QUANTIZATION-AWARE TRAINING (QAT)                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ • Simulate quantization DURING training              │   │
│  │ • Model learns to compensate for quantization error  │   │
│  │ • Best accuracy among quantization methods           │   │
│  │ • Requires retraining (higher effort)                │   │
│  │                                                      │   │
│  │ Training:                                            │   │
│  │ ┌──────┐   ┌──────────┐   ┌──────┐   ┌──────────┐  │   │
│  │ │ FP32 │──▶│ Fake     │──▶│ FP32 │──▶│ Loss +   │  │   │
│  │ │Weight│   │ Quantize │   │Output│   │ Backprop │  │   │
│  │ └──────┘   │ (simulate│   └──────┘   └──────────┘  │   │
│  │            │  INT8    │                              │   │
│  │            │  noise)  │                              │   │
│  │            └──────────┘                              │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  FP16 QUANTIZATION                                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ • Weights: FP32 → FP16 (half precision)              │   │
│  │ • 2× smaller, minimal accuracy loss                  │   │
│  │ • GPU delegates handle FP16 natively                 │   │
│  │ • Good first step — nearly zero accuracy loss        │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  Comparison:                                                │
│  ┌────────────────┬───────┬────────┬──────────┬──────────┐ │
│  │ Method         │ Size  │ Speed  │ Accuracy │ Effort   │ │
│  ├────────────────┼───────┼────────┼──────────┼──────────┤ │
│  │ FP16           │ 2×↓   │ 1.5×↑  │ ~0%↓     │ Trivial  │ │
│  │ Dynamic INT8   │ 4×↓   │ 2-3×↑  │ ~1%↓     │ Easy     │ │
│  │ Full INT8      │ 4×↓   │ 3-4×↑  │ ~1-2%↓   │ Medium   │ │
│  │ QAT INT8       │ 4×↓   │ 3-4×↑  │ ~0.5%↓   │ Hard     │ │
│  └────────────────┴───────┴────────┴──────────┴──────────┘ │
└──────────────────────────────────────────────────────────────┘
```

### Python — Post-Training Quantization

```python
import tensorflow as tf
import numpy as np

# Load trained model
model = tf.keras.models.load_model('my_model.h5')

# ========= 1. Dynamic Range Quantization =========
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
dynamic_quant_model = converter.convert()

with open('model_dynamic_quant.tflite', 'wb') as f:
    f.write(dynamic_quant_model)
print(f"Dynamic quant: {len(dynamic_quant_model) / 1024:.0f} KB")


# ========= 2. Full Integer Quantization =========
def representative_dataset():
    """Generate representative data for calibration."""
    # Use ~100-500 samples from training data
    for i in range(200):
        sample = np.random.rand(1, 224, 224, 3).astype(np.float32)
        yield [sample]

converter2 = tf.lite.TFLiteConverter.from_keras_model(model)
converter2.optimizations = [tf.lite.Optimize.DEFAULT]
converter2.representative_dataset = representative_dataset
# Force full integer (no float fallback)
converter2.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
converter2.inference_input_type = tf.uint8
converter2.inference_output_type = tf.uint8

full_int_model = converter2.convert()

with open('model_full_int8.tflite', 'wb') as f:
    f.write(full_int_model)
print(f"Full INT8: {len(full_int_model) / 1024:.0f} KB")


# ========= 3. FP16 Quantization =========
converter3 = tf.lite.TFLiteConverter.from_keras_model(model)
converter3.optimizations = [tf.lite.Optimize.DEFAULT]
converter3.target_spec.supported_types = [tf.float16]

fp16_model = converter3.convert()

with open('model_fp16.tflite', 'wb') as f:
    f.write(fp16_model)
print(f"FP16: {len(fp16_model) / 1024:.0f} KB")
```

### Python — Quantization-Aware Training

```python
import tensorflow_model_optimization as tfmot

# Original model
base_model = tf.keras.Sequential([
    tf.keras.layers.Conv2D(32, (3, 3), activation='relu', input_shape=(224, 224, 3)),
    tf.keras.layers.MaxPooling2D((2, 2)),
    tf.keras.layers.Conv2D(64, (3, 3), activation='relu'),
    tf.keras.layers.MaxPooling2D((2, 2)),
    tf.keras.layers.Flatten(),
    tf.keras.layers.Dense(128, activation='relu'),
    tf.keras.layers.Dense(10, activation='softmax')
])

# Apply QAT — wraps layers with fake quantization nodes
qat_model = tfmot.quantization.keras.quantize_model(base_model)

qat_model.compile(
    optimizer='adam',
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

# Train with quantization simulation
# Fine-tune for fewer epochs (model already pretrained)
qat_model.fit(train_ds, epochs=5, validation_data=val_ds)

# Convert QAT model to TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(qat_model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]

qat_tflite = converter.convert()
with open('model_qat.tflite', 'wb') as f:
    f.write(qat_tflite)
```

### Android — Loading Quantized Models

```kotlin
class QuantizedModelLoader(private val context: Context) {

    // Load quantized model — same API as non-quantized
    fun loadModel(modelPath: String): org.tensorflow.lite.Interpreter {
        val options = org.tensorflow.lite.Interpreter.Options().apply {
            setNumThreads(4)
            // INT8 models benefit from NNAPI
            setUseNNAPI(true)
        }

        val modelBuffer = loadModelFile(modelPath)
        return org.tensorflow.lite.Interpreter(modelBuffer, options)
    }

    // For full INT8 models, input/output are uint8
    fun runInt8Inference(interpreter: org.tensorflow.lite.Interpreter, bitmap: Bitmap): FloatArray {
        val inputShape = interpreter.getInputTensor(0).shape()  // [1, 224, 224, 3]
        val inputType = interpreter.getInputTensor(0).dataType()

        val outputShape = interpreter.getOutputTensor(0).shape()
        val numClasses = outputShape[1]

        if (inputType == org.tensorflow.lite.DataType.UINT8) {
            // INT8 model: use ByteBuffer with uint8 values
            val inputBuffer = preprocessToUint8(bitmap, inputShape[1], inputShape[2])
            val outputBuffer = ByteArray(numClasses)

            interpreter.run(inputBuffer, outputBuffer)

            // Dequantize output
            val outputParams = interpreter.getOutputTensor(0).quantizationParams()
            return FloatArray(numClasses) { i ->
                (outputBuffer[i].toInt() and 0xFF - outputParams.zeroPoint) * outputParams.scale
            }
        } else {
            // Float model
            val inputBuffer = preprocessToFloat(bitmap, inputShape[1], inputShape[2])
            val output = Array(1) { FloatArray(numClasses) }
            interpreter.run(inputBuffer, output)
            return output[0]
        }
    }

    private fun preprocessToUint8(bitmap: Bitmap, width: Int, height: Int): java.nio.ByteBuffer {
        val scaled = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val buffer = java.nio.ByteBuffer.allocateDirect(width * height * 3)
        buffer.order(java.nio.ByteOrder.nativeOrder())

        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        for (pixel in pixels) {
            buffer.put(((pixel shr 16) and 0xFF).toByte())  // R
            buffer.put(((pixel shr 8) and 0xFF).toByte())   // G
            buffer.put((pixel and 0xFF).toByte())            // B
        }
        buffer.rewind()
        return buffer
    }

    private fun preprocessToFloat(bitmap: Bitmap, width: Int, height: Int): java.nio.ByteBuffer {
        val scaled = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val buffer = java.nio.ByteBuffer.allocateDirect(4 * width * height * 3)
        buffer.order(java.nio.ByteOrder.nativeOrder())

        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
            buffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
            buffer.putFloat((pixel and 0xFF) / 255f)
        }
        buffer.rewind()
        return buffer
    }

    private fun loadModelFile(name: String): java.nio.MappedByteBuffer {
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

## 2. Pruning for Model Compression

```
Pruning Theory:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Idea: Many weights in neural networks are near-zero        │
│  and contribute little. Remove them.                        │
│                                                              │
│  Before Pruning:              After 50% Pruning:            │
│  ┌─────────────────┐         ┌─────────────────┐           │
│  │ ● ──── ●        │         │ ● ──── ●        │           │
│  │ ● ──── ●        │         │ ● ╌╌╌╌ ●        │  ╌ = cut  │
│  │ ● ──── ●        │         │ ● ──── ●        │           │
│  │ ● ──── ●        │         │ ● ╌╌╌╌ ●        │           │
│  │ ● ──── ●        │         │ ● ──── ●        │           │
│  │ ● ──── ●        │         │ ● ╌╌╌╌ ●        │           │
│  └─────────────────┘         └─────────────────┘           │
│  100% weights                 50% weights                   │
│  100% accuracy                ~99% accuracy                 │
│                                                              │
│  Pruning Strategies:                                        │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Magnitude Pruning:                                │       │
│  │   Remove weights with |w| < threshold            │       │
│  │   Most common, effective                          │       │
│  │                                                   │       │
│  │ Structured Pruning:                               │       │
│  │   Remove entire filters/channels/heads            │       │
│  │   Better hardware utilization                     │       │
│  │                                                   │       │
│  │ Gradual Pruning:                                  │       │
│  │   Start: 0% sparse → End: 80% sparse             │       │
│  │   Prune during training, let model adapt          │       │
│  │                                                   │       │
│  │ Schedule:                                         │       │
│  │   Sparsity                                        │       │
│  │   80%├─────────────────────────────────█████      │       │
│  │      │                          ████████          │       │
│  │      │                    ██████                   │       │
│  │      │              █████                          │       │
│  │   0% ├───█████───────────────────────────          │       │
│  │       0    20    40    60    80   100              │       │
│  │                  Training Steps (%)               │       │
│  └──────────────────────────────────────────────────┘       │
└──────────────────────────────────────────────────────────────┘
```

### Python — Pruning with TF Model Optimization

```python
import tensorflow_model_optimization as tfmot

# Original model
model = tf.keras.Sequential([
    tf.keras.layers.Conv2D(32, (3,3), activation='relu', input_shape=(224,224,3)),
    tf.keras.layers.MaxPooling2D((2,2)),
    tf.keras.layers.Conv2D(64, (3,3), activation='relu'),
    tf.keras.layers.Flatten(),
    tf.keras.layers.Dense(128, activation='relu'),
    tf.keras.layers.Dense(10, activation='softmax')
])

# Gradual pruning schedule
pruning_params = {
    'pruning_schedule': tfmot.sparsity.keras.PolynomialDecay(
        initial_sparsity=0.20,   # Start 20% sparse
        final_sparsity=0.80,     # End 80% sparse
        begin_step=1000,
        end_step=5000
    )
}

# Apply pruning
pruned_model = tfmot.sparsity.keras.prune_low_magnitude(model, **pruning_params)

pruned_model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

# Callbacks required for pruning
callbacks = [tfmot.sparsity.keras.UpdatePruningStep()]

pruned_model.fit(train_ds, epochs=10, callbacks=callbacks, validation_data=val_ds)

# Strip pruning wrappers for export
stripped_model = tfmot.sparsity.keras.strip_pruning(pruned_model)

# Convert with quantization for maximum compression
converter = tf.lite.TFLiteConverter.from_keras_model(stripped_model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
pruned_quant_model = converter.convert()

# Sparse + quantized = very small
with open('model_pruned_quant.tflite', 'wb') as f:
    f.write(pruned_quant_model)

# Compare sizes
import os
original_size = os.path.getsize('original.tflite')
optimized_size = len(pruned_quant_model)
print(f"Compression: {original_size/optimized_size:.1f}×")
```

---

## 3. Knowledge Distillation

```
Knowledge Distillation Theory:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Teacher (Large Model)         Student (Small Model)        │
│  ┌────────────────────┐       ┌──────────────┐             │
│  │ ResNet-152          │       │ MobileNet     │             │
│  │ 230 MB              │       │ 14 MB          │             │
│  │ 97.5% accuracy      │       │ 93.2% accuracy │             │
│  │                     │       │                │             │
│  │ Softmax output:     │       │ Trained to     │             │
│  │ cat: 0.85 ─────────────────▶ mimic teacher's │             │
│  │ dog: 0.10           │       │ soft outputs   │             │
│  │ fox: 0.04           │       │                │             │
│  │ wolf:0.01           │       │ Final accuracy:│             │
│  └────────────────────┘       │ 95.8%! ↑       │             │
│                                └──────────────┘             │
│                                                              │
│  Key Insight: Soft labels carry MORE information than        │
│  hard labels. "cat: 0.85, dog: 0.10" tells the student     │
│  that cats and dogs look similar — hard label "cat: 1.0"   │
│  throws away this inter-class relationship.                 │
│                                                              │
│  Loss Function:                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                                                      │   │
│  │ L = α × L_hard + (1-α) × T² × L_soft               │   │
│  │                                                      │   │
│  │ L_hard = CrossEntropy(student_out, true_labels)      │   │
│  │ L_soft = KL_Divergence(                              │   │
│  │            softmax(student_logits / T),              │   │
│  │            softmax(teacher_logits / T)               │   │
│  │          )                                           │   │
│  │                                                      │   │
│  │ T = Temperature (typically 3-10)                     │   │
│  │   Higher T → softer probability distribution         │   │
│  │   T=1: [0.97, 0.02, 0.01] (peaked)                 │   │
│  │   T=5: [0.45, 0.30, 0.25] (spread out — more info) │   │
│  │                                                      │   │
│  │ α = balance factor (typically 0.1-0.3)               │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

### Python — Knowledge Distillation

```python
import tensorflow as tf

class Distiller(tf.keras.Model):
    def __init__(self, student, teacher, temperature=5.0, alpha=0.1):
        super().__init__()
        self.student = student
        self.teacher = teacher
        self.temperature = temperature
        self.alpha = alpha
        # Freeze teacher
        self.teacher.trainable = False
    
    def compile(self, optimizer, student_loss, distillation_loss, metrics):
        super().compile(optimizer=optimizer, metrics=metrics)
        self.student_loss_fn = student_loss
        self.distillation_loss_fn = distillation_loss

    def train_step(self, data):
        x, y = data
        
        # Get teacher predictions (no gradient needed)
        teacher_preds = self.teacher(x, training=False)
        
        with tf.GradientTape() as tape:
            # Student predictions
            student_preds = self.student(x, training=True)
            
            # Hard label loss (student vs true labels)
            hard_loss = self.student_loss_fn(y, student_preds)
            
            # Soft label loss (student vs teacher, with temperature)
            soft_student = tf.nn.softmax(student_preds / self.temperature)
            soft_teacher = tf.nn.softmax(teacher_preds / self.temperature)
            soft_loss = self.distillation_loss_fn(soft_teacher, soft_student)
            
            # Combined loss
            total_loss = (self.alpha * hard_loss + 
                         (1 - self.alpha) * self.temperature**2 * soft_loss)
        
        # Update student weights
        grads = tape.gradient(total_loss, self.student.trainable_variables)
        self.optimizer.apply_gradients(zip(grads, self.student.trainable_variables))
        
        self.compiled_metrics.update_state(y, student_preds)
        return {m.name: m.result() for m in self.metrics}

# Usage
teacher = tf.keras.applications.ResNet152V2(weights='imagenet', include_top=True)

student = tf.keras.Sequential([
    tf.keras.applications.MobileNetV3Small(weights=None, include_top=False, input_shape=(224,224,3)),
    tf.keras.layers.GlobalAveragePooling2D(),
    tf.keras.layers.Dense(1000, activation='softmax')
])

distiller = Distiller(student, teacher, temperature=5.0, alpha=0.1)
distiller.compile(
    optimizer=tf.keras.optimizers.Adam(1e-4),
    student_loss=tf.keras.losses.SparseCategoricalCrossentropy(),
    distillation_loss=tf.keras.losses.KLDivergence(),
    metrics=[tf.keras.metrics.SparseCategoricalAccuracy()]
)

distiller.fit(train_ds, epochs=30, validation_data=val_ds)

# Export student only
converter = tf.lite.TFLiteConverter.from_keras_model(student)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()
```

---

## 4. Neural Architecture Search (NAS)

```
NAS Theory:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Problem: Designing neural network architectures manually   │
│  requires expert knowledge. Can we automate it?             │
│                                                              │
│  NAS Approach:                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                                                      │   │
│  │  Search Space × Search Strategy × Evaluation         │   │
│  │                                                      │   │
│  │  Search Space:                                       │   │
│  │  • Layer types (Conv, Pool, Dense, Skip)             │   │
│  │  • Kernel sizes (3×3, 5×5, 7×7)                     │   │
│  │  • Channel counts (16, 32, 64, 128)                  │   │
│  │  • Connections (residual, dense, none)               │   │
│  │                                                      │   │
│  │  Search Strategy:                                    │   │
│  │  • Reinforcement Learning (NASNet)                   │   │
│  │  • Evolutionary (AmoebaNet)                          │   │
│  │  • Differentiable (DARTS) — most efficient          │   │
│  │  • One-shot (EfficientNet)                          │   │
│  │                                                      │   │
│  │  Evaluation:                                         │   │
│  │  • Train & validate each candidate                   │   │
│  │  • Weight sharing (faster)                           │   │
│  │  • Proxy metrics (latency predictors)                │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  Notable NAS Models for Mobile:                             │
│  ┌─────────────────────┬───────────┬────────────┐          │
│  │ Model               │ Size (MB) │ Top-1 Acc  │          │
│  ├─────────────────────┼───────────┼────────────┤          │
│  │ MnasNet             │ 13        │ 75.2%      │          │
│  │ EfficientNet-B0     │ 20        │ 77.1%      │          │
│  │ EfficientNet-Lite0  │ 17        │ 75.1%      │          │
│  │ MobileNetV3-Small   │ 6.9       │ 67.5%      │          │
│  │ MobileNetV3-Large   │ 22        │ 75.2%      │          │
│  └─────────────────────┴───────────┴────────────┘          │
│  All found via NAS, optimized for mobile latency           │
└──────────────────────────────────────────────────────────────┘
```

### Using NAS-Optimized Models on Android

```kotlin
// EfficientNet-Lite (NAS-found, mobile-optimized)
class NASModelDeployer(private val context: Context) {

    // Use pre-built NAS models from TF Hub
    // Download efficientnet-lite0.tflite from TF Hub
    fun loadEfficientNetLite(): org.tensorflow.lite.Interpreter {
        val options = org.tensorflow.lite.Interpreter.Options().apply {
            setNumThreads(4)
        }
        return org.tensorflow.lite.Interpreter(
            loadModelFile("efficientnet_lite0.tflite"), options
        )
    }

    // Benchmark different NAS models
    fun benchmarkModel(
        interpreter: org.tensorflow.lite.Interpreter,
        inputShape: IntArray,
        warmupRuns: Int = 5,
        benchmarkRuns: Int = 50
    ): BenchmarkResult {
        val input = java.nio.ByteBuffer.allocateDirect(
            inputShape.reduce { a, b -> a * b } * 4
        ).apply { order(java.nio.ByteOrder.nativeOrder()) }

        val outputShape = interpreter.getOutputTensor(0).shape()
        val output = Array(1) { FloatArray(outputShape[1]) }

        // Warmup
        repeat(warmupRuns) { interpreter.run(input.rewind(), output) }

        // Benchmark
        val times = mutableListOf<Long>()
        repeat(benchmarkRuns) {
            val start = System.nanoTime()
            interpreter.run(input.rewind(), output)
            times.add(System.nanoTime() - start)
        }

        return BenchmarkResult(
            avgLatencyMs = times.average() / 1_000_000.0,
            p50LatencyMs = times.sorted()[times.size / 2] / 1_000_000.0,
            p95LatencyMs = times.sorted()[(times.size * 0.95).toInt()] / 1_000_000.0,
            minLatencyMs = times.min() / 1_000_000.0,
            maxLatencyMs = times.max() / 1_000_000.0
        )
    }

    data class BenchmarkResult(
        val avgLatencyMs: Double,
        val p50LatencyMs: Double,
        val p95LatencyMs: Double,
        val minLatencyMs: Double,
        val maxLatencyMs: Double
    )

    private fun loadModelFile(name: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(name)
        val stream = java.io.FileInputStream(fd.fileDescriptor)
        return stream.channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )
    }
}
```
