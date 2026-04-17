# TensorFlow Lite Support Library — Task Library

## Overview

The TFLite Support Library and Task Library provide **high-level APIs** that abstract away the complexity of raw TFLite Interpreter usage. Instead of manually handling ByteBuffers, normalization, and post-processing, you use task-specific classes.

```
Architecture Layers:
┌──────────────────────────────────────────────────────┐
│                   Your Android App                    │
├──────────────────────────────────────────────────────┤
│              Task Library (Highest Level)             │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐     │
│  │ ImageClass │  │ ObjectDet  │  │ NLClassify │     │
│  │ ifier     │  │ ector      │  │ er         │     │
│  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘     │
├────────┼───────────────┼───────────────┼─────────────┤
│        │    Support Library (Mid Level) │             │
│  ┌─────▼────────────────▼───────────────▼──────┐     │
│  │  TensorImage  │ TensorBuffer │ TensorAudio  │     │
│  │  ImageProcess │ TensorLabel  │ AudioRecord  │     │
│  └──────────────────────────────────────────────┘     │
├──────────────────────────────────────────────────────┤
│              TFLite Interpreter (Low Level)           │
│  ┌──────────────────────────────────────────────┐    │
│  │  Interpreter  │  Delegates  │  ByteBuffers   │    │
│  └──────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────┘

Why Task Library?
┌──────────────────────────────┬────────────────────────────────┐
│ Without Task Library         │ With Task Library              │
├──────────────────────────────┼────────────────────────────────┤
│ Load model manually          │ One-line model loading         │
│ Create ByteBuffer            │ Pass Bitmap directly           │
│ Normalize pixels manually    │ Auto normalization from meta   │
│ Run interpreter              │ Handled internally             │
│ Parse output float arrays    │ Typed result objects           │
│ Apply NMS / thresholding     │ Built-in post-processing       │
│ ~50 lines of code            │ ~5 lines of code               │
└──────────────────────────────┴────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Support Library (for TensorImage, TensorBuffer, etc.)
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    
    // Task Library — Vision
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    
    // Task Library — Text (NLP)
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.4")
    
    // Task Library — Audio
    implementation("org.tensorflow:tensorflow-lite-task-audio:0.4.4")
    
    // GPU Delegate (optional)
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
}
```

---

## 1. Vision Tasks

### 1.1 Image Classification

```
Classification Pipeline (Internal):
┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌────────────┐
│  Bitmap  │───▶│ Resize +     │───▶│ Run Model    │───▶│ Top-K      │
│  Input   │    │ Normalize    │    │ Inference    │    │ Results    │
│          │    │ (from meta)  │    │              │    │ + Labels   │
└──────────┘    └──────────────┘    └──────────────┘    └────────────┘
                  Automatic!          Automatic!          Automatic!
```

```kotlin
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.support.image.TensorImage

class TaskImageClassifier(private val context: Context) {

    private lateinit var classifier: ImageClassifier

    fun initialize() {
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setMaxResults(5)                    // Top 5 results
            .setScoreThreshold(0.3f)             // Minimum confidence
            .setNumThreads(4)
            .build()

        // Model must have metadata with labels
        classifier = ImageClassifier.createFromFileAndOptions(
            context,
            "mobilenet_v2.tflite",
            options
        )
    }

    fun classify(bitmap: Bitmap): List<Classifications> {
        val tensorImage = TensorImage.fromBitmap(bitmap)
        return classifier.classify(tensorImage)
    }

    // Usage
    fun example(bitmap: Bitmap) {
        val results = classify(bitmap)
        
        for (classification in results) {
            for (category in classification.categories) {
                val label = category.label       // "cat"
                val score = category.score       // 0.95
                val index = category.index       // 281
                Log.d("Classify", "$label: ${(score * 100).toInt()}%")
            }
        }
    }

    // With GPU acceleration
    fun initializeWithGpu() {
        val baseOptions = org.tensorflow.lite.task.core.BaseOptions.builder()
            .useGpu()
            .setNumThreads(4)
            .build()

        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(5)
            .build()

        classifier = ImageClassifier.createFromFileAndOptions(
            context, "mobilenet_v2.tflite", options
        )
    }

    fun close() = classifier.close()
}
```

### 1.2 Object Detection

```
Object Detection Output:
┌─────────────────────────────────────┐
│  Image                              │
│   ┌──────────┐                      │
│   │  Dog     │  conf: 0.92         │
│   │  (bbox)  │                      │
│   └──────────┘                      │
│         ┌─────────────┐             │
│         │  Cat        │ conf: 0.87  │
│         │  (bbox)     │             │
│         └─────────────┘             │
│                                     │
│  Detection {                        │
│    boundingBox: RectF(x, y, w, h)   │
│    categories: [                    │
│      Category(label, score, index)  │
│    ]                                │
│  }                                  │
└─────────────────────────────────────┘
```

```kotlin
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.task.vision.detector.Detection

class TaskObjectDetector(private val context: Context) {

    private lateinit var detector: ObjectDetector

    fun initialize() {
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(10)
            .setScoreThreshold(0.5f)
            .setNumThreads(4)
            .build()

        detector = ObjectDetector.createFromFileAndOptions(
            context,
            "ssd_mobilenet.tflite",
            options
        )
    }

    fun detect(bitmap: Bitmap): List<Detection> {
        val tensorImage = TensorImage.fromBitmap(bitmap)
        return detector.detect(tensorImage)
    }

    fun example(bitmap: Bitmap) {
        val results = detect(bitmap)

        for (detection in results) {
            val boundingBox = detection.boundingBox  // RectF
            
            for (category in detection.categories) {
                Log.d("Detect", """
                    Object: ${category.label}
                    Score: ${category.score}
                    Box: (${boundingBox.left}, ${boundingBox.top}) 
                         to (${boundingBox.right}, ${boundingBox.bottom})
                """.trimIndent())
            }
        }
    }

    fun close() = detector.close()
}
```

### 1.3 Image Segmentation

```
Segmentation Output:
┌───────────────────────────────────────┐
│ Input Image    → Segmentation Mask    │
│                                       │
│  ┌──────────┐    ┌──────────┐        │
│  │ Person   │    │ ████████ │ person │
│  │ on beach │    │ ░░░░░░░░ │ sand   │
│  │          │    │ ▓▓▓▓▓▓▓▓ │ sky    │
│  └──────────┘    └──────────┘        │
│                                       │
│ Each pixel gets a category label      │
│ and confidence for each category.     │
└───────────────────────────────────────┘
```

```kotlin
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter
import org.tensorflow.lite.task.vision.segmenter.Segmentation

class TaskImageSegmenter(private val context: Context) {

    private lateinit var segmenter: ImageSegmenter

    fun initialize() {
        val options = ImageSegmenter.ImageSegmenterOptions.builder()
            .setOutputType(ImageSegmenter.ImageSegmenterOptions.OutputType.CATEGORY_MASK)
            .setNumThreads(4)
            .build()

        segmenter = ImageSegmenter.createFromFileAndOptions(
            context,
            "deeplabv3.tflite",
            options
        )
    }

    fun segment(bitmap: Bitmap): List<Segmentation> {
        val tensorImage = TensorImage.fromBitmap(bitmap)
        return segmenter.segment(tensorImage)
    }

    fun example(bitmap: Bitmap) {
        val results = segment(bitmap)

        for (segmentation in results) {
            // Color labels associated with each category
            val coloredLabels = segmentation.coloredLabels
            for (label in coloredLabels) {
                Log.d("Segment", "Category: ${label.label}, Color: ${label.argb}")
            }

            // Category masks
            val masks = segmentation.masks
            for (mask in masks) {
                val width = mask.width
                val height = mask.height
                val buffer = mask.buffer

                // Process mask pixel by pixel
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val categoryIndex = buffer.get().toInt() and 0xFF
                        // categoryIndex maps to coloredLabels[categoryIndex]
                    }
                }
                buffer.rewind()
            }
        }
    }

    fun close() = segmenter.close()
}
```

---

## 2. NLP Tasks

### 2.1 Text Classification (Sentiment Analysis)

```
NLP Classification Pipeline:
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌────────────┐
│  Raw Text    │───▶│ Tokenizer    │───▶│ Model        │───▶│ Categories │
│  "Great      │    │ (from meta)  │    │ Inference    │    │ Positive:  │
│   movie!"    │    │              │    │              │    │  0.95      │
└──────────────┘    └──────────────┘    └──────────────┘    └────────────┘
```

```kotlin
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier

class TaskTextClassifier(private val context: Context) {

    // --- Simple Text Classifier ---
    private lateinit var nlClassifier: NLClassifier

    fun initializeSimple() {
        nlClassifier = NLClassifier.createFromFile(
            context,
            "text_classifier.tflite"
        )
    }

    fun classifySimple(text: String) {
        val results = nlClassifier.classify(text)
        for (category in results) {
            Log.d("NLC", "${category.label}: ${category.score}")
            // e.g., "Positive: 0.92", "Negative: 0.08"
        }
    }

    // --- BERT-based Classifier (more accurate) ---
    private lateinit var bertClassifier: BertNLClassifier

    fun initializeBert() {
        bertClassifier = BertNLClassifier.createFromFile(
            context,
            "bert_classifier.tflite"
        )
    }

    fun classifyBert(text: String) {
        val results = bertClassifier.classify(text)
        for (category in results) {
            Log.d("BERT", "${category.label}: ${category.score}")
        }
    }

    fun close() {
        nlClassifier.close()
        bertClassifier.close()
    }
}
```

### 2.2 Question Answering

```
Question Answering:
┌────────────────────────────────────────────────────────┐
│ Context:  "The Eiffel Tower is a wrought-iron lattice │
│           tower on the Champ de Mars in Paris, France.│
│           It was constructed from 1887 to 1889."      │
│                                                        │
│ Question: "When was the Eiffel Tower built?"          │
│                                                        │
│ Answer:   "from 1887 to 1889" (confidence: 0.89)      │
└────────────────────────────────────────────────────────┘
```

```kotlin
import org.tensorflow.lite.task.text.qa.BertQuestionAnswerer
import org.tensorflow.lite.task.text.qa.QaAnswer

class TaskQuestionAnswerer(private val context: Context) {

    private lateinit var answerer: BertQuestionAnswerer

    fun initialize() {
        answerer = BertQuestionAnswerer.createFromFile(
            context,
            "mobilebert_qa.tflite"  // MobileBERT for QA
        )
    }

    fun answer(context: String, question: String): List<QaAnswer> {
        val answers = answerer.answer(context, question)
        
        for (answer in answers) {
            Log.d("QA", "Answer: '${answer.text}', Score: ${answer.pos.logit}")
        }
        
        return answers
    }

    fun example() {
        val context = """
            Android is a mobile operating system based on a modified version 
            of the Linux kernel. It was first released in September 2008. 
            Android is developed by Google and the Open Handset Alliance.
        """.trimIndent()

        val question = "Who develops Android?"
        val answers = answer(context, question)
        // Expected: "Google and the Open Handset Alliance"
    }

    fun close() = answerer.close()
}
```

---

## 3. Audio Tasks

### 3.1 Audio Classification

```
Audio Classification Pipeline:
┌──────────┐    ┌────────────┐    ┌──────────────┐    ┌────────────┐
│Microphone│───▶│ Audio      │───▶│ Model        │───▶│ Categories │
│ Input    │    │ Buffer     │    │ (YAMNet)     │    │ Speech:0.8 │
│ (PCM)    │    │ (ring buf) │    │              │    │ Music: 0.1 │
└──────────┘    └────────────┘    └──────────────┘    └────────────┘
```

```kotlin
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.support.audio.TensorAudio

class TaskAudioClassifier(private val context: Context) {

    private lateinit var classifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio
    private lateinit var audioRecord: android.media.AudioRecord

    fun initialize() {
        classifier = AudioClassifier.createFromFile(
            context,
            "yamnet.tflite"  // Google's audio event classification model
        )

        // Create TensorAudio from classifier's required format
        tensorAudio = classifier.createInputTensorAudio()

        // Create AudioRecord matching model requirements
        audioRecord = classifier.createAudioRecord()
    }

    fun startClassification(onResult: (List<Pair<String, Float>>) -> Unit) {
        audioRecord.startRecording()

        // Periodic classification (use Handler or coroutine)
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val classifyRunnable = object : Runnable {
            override fun run() {
                // Load audio from AudioRecord into TensorAudio
                tensorAudio.load(audioRecord)

                // Classify
                val results = classifier.classify(tensorAudio)

                val categories = results.flatMap { it.categories }
                    .sortedByDescending { it.score }
                    .take(5)
                    .map { it.label to it.score }

                onResult(categories)

                handler.postDelayed(this, 500) // Classify every 500ms
            }
        }
        handler.post(classifyRunnable)
    }

    fun stop() {
        audioRecord.stop()
    }

    fun close() {
        audioRecord.release()
        classifier.close()
    }
}
```

---

## 4. Support Library Core Classes

### TensorImage, TensorBuffer, ImageProcessor

```
Support Library Data Flow:
┌──────────┐    ┌────────────────┐    ┌─────────────┐    ┌──────────┐
│ Bitmap   │───▶│ TensorImage    │───▶│ ImageProc   │───▶│ Processed│
│          │    │ .fromBitmap()  │    │ .process()  │    │ Tensor   │
└──────────┘    └────────────────┘    └─────────────┘    └──────────┘

ImageProcessor Operations:
  ┌──────────────────┐
  │ ResizeOp         │  Resize to model input size
  │ ResizeWithCropOr │  Resize preserving aspect ratio
  │ Rot90Op          │  Rotate by 90° increments
  │ NormalizeOp      │  Normalize pixel values
  │ QuantizeOp       │  Quantize to uint8
  │ CastOp           │  Cast data types
  │ DequantizeOp     │  Dequantize from uint8
  └──────────────────┘
```

```kotlin
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.common.ops.QuantizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.DataType

class SupportLibraryHelper {

    // --- TensorImage ---
    fun createTensorImage(bitmap: Bitmap): TensorImage {
        // Float32 tensor image
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        return tensorImage
    }

    // --- ImageProcessor (preprocessing pipeline) ---
    fun preprocessImage(bitmap: Bitmap): TensorImage {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(224, 224))  // Crop/pad to square
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(Rot90Op(0))  // Rotation
            .add(NormalizeOp(127.5f, 127.5f))  // Normalize to [-1, 1]
            .build()

        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)

        return imageProcessor.process(tensorImage)
    }

    // For quantized models
    fun preprocessQuantized(bitmap: Bitmap): TensorImage {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 1f))       // Normalize to [0, 255]
            .add(QuantizeOp(0f, 0.003921569f))  // 1/255
            .build()

        val tensorImage = TensorImage(DataType.UINT8)
        tensorImage.load(bitmap)

        return imageProcessor.process(tensorImage)
    }

    // --- TensorBuffer (for output) ---
    fun processOutput(outputBuffer: TensorBuffer, labels: List<String>): List<Pair<String, Float>> {
        val probabilities = outputBuffer.floatArray

        return probabilities
            .mapIndexed { idx, prob -> labels[idx] to prob }
            .sortedByDescending { it.second }
            .take(5)
    }

    // --- Full Pipeline Example ---
    fun fullExample(context: Context, bitmap: Bitmap) {
        // 1. Load model
        val modelFile = loadModelFile(context, "model.tflite")
        val interpreter = org.tensorflow.lite.Interpreter(modelFile)

        // 2. Preprocess input
        val processedImage = preprocessImage(bitmap)

        // 3. Create output buffer
        val outputShape = interpreter.getOutputTensor(0).shape()  // e.g., [1, 1000]
        val outputType = interpreter.getOutputTensor(0).dataType()
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, outputType)

        // 4. Run inference
        interpreter.run(processedImage.buffer, outputBuffer.buffer.rewind())

        // 5. Post-process
        val labels = context.assets.open("labels.txt")
            .bufferedReader().readLines()
        val results = processOutput(outputBuffer, labels)

        results.forEach { (label, score) ->
            Log.d("Result", "$label: ${(score * 100).toInt()}%")
        }

        interpreter.close()
    }

    private fun loadModelFile(context: Context, path: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(path)
        return java.io.FileInputStream(fd.fileDescriptor).channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )
    }
}
```

---

## Comparison: Raw Interpreter vs Task Library

```kotlin
// ❌ WITHOUT Task Library (50+ lines)
fun classifyRaw(context: Context, bitmap: Bitmap) {
    val modelFile = loadModel(context, "model.tflite")
    val interpreter = Interpreter(modelFile)
    val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
    val buffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
    buffer.order(ByteOrder.nativeOrder())
    val pixels = IntArray(224 * 224)
    resized.getPixels(pixels, 0, 224, 0, 0, 224, 224)
    for (pixel in pixels) {
        buffer.putFloat(((pixel shr 16) and 0xFF) / 127.5f - 1.0f)
        buffer.putFloat(((pixel shr 8) and 0xFF) / 127.5f - 1.0f)
        buffer.putFloat((pixel and 0xFF) / 127.5f - 1.0f)
    }
    buffer.rewind()
    val output = Array(1) { FloatArray(1000) }
    interpreter.run(buffer, output)
    val labels = context.assets.open("labels.txt").bufferedReader().readLines()
    output[0].mapIndexed { i, c -> labels[i] to c }
        .sortedByDescending { it.second }
        .take(5)
        .forEach { Log.d("Result", "${it.first}: ${it.second}") }
    interpreter.close()
}

// ✅ WITH Task Library (5 lines)
fun classifyTask(context: Context, bitmap: Bitmap) {
    val classifier = ImageClassifier.createFromFile(context, "model.tflite")
    val results = classifier.classify(TensorImage.fromBitmap(bitmap))
    results.flatMap { it.categories }
        .forEach { Log.d("Result", "${it.label}: ${it.score}") }
    classifier.close()
}
```
