# On-Device Image Processing — Computer Vision

## Overview

On-device computer vision combines CameraX for camera input with ML Kit/TFLite/OpenCV for real-time image analysis — all without network connectivity.

```
On-Device Vision Pipeline:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  ┌──────────┐    ┌──────────────┐    ┌────────────┐ │
│  │ CameraX  │───▶│ Image        │───▶│ ML Model   │ │
│  │ Preview  │    │ Analysis     │    │ Inference  │ │
│  │          │    │ (frames)     │    │            │ │
│  └──────────┘    └──────────────┘    └─────┬──────┘ │
│                                            │        │
│                                     ┌──────▼──────┐ │
│                                     │ Render      │ │
│                                     │ Results     │ │
│                                     │ (Overlay)   │ │
│                                     └─────────────┘ │
│                                                      │
│  Processing Stack:                                   │
│  ┌──────────────────────────────────────────────┐    │
│  │ CameraX          │ Camera capture & preview  │    │
│  │ ML Kit           │ Pre-built ML features     │    │
│  │ MediaPipe         │ Real-time pipelines       │    │
│  │ TensorFlow Lite   │ Custom models             │    │
│  │ OpenCV            │ Classic computer vision   │    │
│  │ RenderScript (dep)│ GPU image processing      │    │
│  └──────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    
    // ML Kit (Vision)
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:face-detection:16.1.5")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.mlkit:object-detection:17.0.1")
    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation("com.google.mlkit:segmentation-selfie:16.0.0-beta4")
    
    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    
    // OpenCV (optional)
    implementation("org.opencv:opencv-android:4.9.0")
}
```

---

## 1. CameraX with ML Kit Integration

### Theory

```
CameraX + ML Kit Architecture:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  CameraX Use Cases:                                  │
│  ┌──────────────────────────────────────────────┐    │
│  │ Preview       → Display camera feed on screen│    │
│  │ ImageCapture  → Take photos                  │    │
│  │ ImageAnalysis → Process frames with ML       │    │
│  │ VideoCapture  → Record video                 │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  ImageAnalysis + ML Kit Flow:                        │
│  ┌────────┐  ┌────────────┐  ┌──────────┐  ┌─────┐ │
│  │Camera  │─▶│ImageProxy  │─▶│InputImage│─▶│MLKit│ │
│  │Frames  │  │(YUV_420)   │  │.fromMedia│  │API  │ │
│  │30fps   │  │            │  │ Image()  │  │     │ │
│  └────────┘  └────────────┘  └──────────┘  └──┬──┘ │
│                                               │     │
│                                        ┌──────▼───┐ │
│                                        │ Results  │ │
│                                        │ Overlay  │ │
│                                        └──────────┘ │
│                                                      │
│  Backpressure Strategies:                            │
│  STRATEGY_KEEP_ONLY_LATEST → Drop frames if busy    │
│  STRATEGY_BLOCK_PRODUCER   → Block camera if busy   │
│                                                      │
│  Recommended: KEEP_ONLY_LATEST for real-time ML      │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class CameraMLKitHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private lateinit var cameraProvider: ProcessCameraProvider

    // Generic ML Kit analyzer
    abstract class MLKitAnalyzer<T> : ImageAnalysis.Analyzer {
        abstract fun processImage(image: InputImage): com.google.android.gms.tasks.Task<T>
        abstract fun onResults(results: T)

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                return
            }

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            processImage(image)
                .addOnSuccessListener { results -> onResults(results) }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    // Combined multi-feature analyzer
    class MultiFeatureAnalyzer(
        private val onFaces: (List<com.google.mlkit.vision.face.Face>) -> Unit,
        private val onText: (com.google.mlkit.vision.text.Text) -> Unit,
        private val onLabels: (List<com.google.mlkit.vision.label.ImageLabel>) -> Unit
    ) : ImageAnalysis.Analyzer {

        private val faceDetector = com.google.mlkit.vision.face.FaceDetection.getClient()
        private val textRecognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
            com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
        )
        private val imageLabeler = com.google.mlkit.vision.label.ImageLabeling.getClient(
            com.google.mlkit.vision.label.defaults.ImageLabelerOptions.DEFAULT_OPTIONS
        )

        private var currentDetector = 0  // Rotate between detectors

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                return
            }

            val image = InputImage.fromMediaImage(
                mediaImage, imageProxy.imageInfo.rotationDegrees
            )

            // Rotate: faces → text → labels (one per frame for performance)
            when (currentDetector % 3) {
                0 -> faceDetector.process(image)
                    .addOnSuccessListener { onFaces(it) }
                    .addOnCompleteListener { imageProxy.close() }
                1 -> textRecognizer.process(image)
                    .addOnSuccessListener { onText(it) }
                    .addOnCompleteListener { imageProxy.close() }
                2 -> imageLabeler.process(image)
                    .addOnSuccessListener { onLabels(it) }
                    .addOnCompleteListener { imageProxy.close() }
            }
            currentDetector++
        }
    }

    fun startCamera(
        previewView: PreviewView,
        analyzer: ImageAnalysis.Analyzer,
        cameraFacing: Int = CameraSelector.LENS_FACING_BACK
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        java.util.concurrent.Executors.newSingleThreadExecutor(),
                        analyzer
                    )
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(cameraFacing)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalysis
            )
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        if (::cameraProvider.isInitialized) {
            cameraProvider.unbindAll()
        }
    }
}
```

---

## 2. Real-Time Image Analysis

### Theory

```
Real-Time Analysis Performance:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Frame Processing Pipeline:                          │
│  ┌──────┐  ┌──────────┐  ┌─────────┐  ┌──────────┐ │
│  │Frame │─▶│Preprocess│─▶│Inference│─▶│Postproc. │ │
│  │30fps │  │~2ms      │  │~20-50ms │  │~2ms      │ │
│  └──────┘  └──────────┘  └─────────┘  └──────────┘ │
│                                                      │
│  Total: ~25-55ms per frame = 18-40 fps               │
│                                                      │
│  Optimization Strategies:                            │
│  ┌──────────────────────────────────────────────┐    │
│  │ 1. Process every Nth frame (skip frames)     │    │
│  │ 2. Reduce input resolution                   │    │
│  │ 3. Use GPU delegate for model inference      │    │
│  │ 4. Run analysis on background thread         │    │
│  │ 5. Use KEEP_ONLY_LATEST backpressure         │    │
│  │ 6. Batch operations where possible           │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  FPS vs Accuracy Tradeoffs:                          │
│  ┌────────────────────────────────────────────┐      │
│  │ 30fps analysis → High CPU, battery drain   │      │
│  │ 15fps analysis → Balanced                  │      │
│  │  5fps analysis → Low power, still smooth   │      │
│  │  1fps analysis → Minimal impact             │      │
│  └────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class RealTimeImageAnalyzer(
    private val context: Context
) {
    // Track FPS
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    private var currentFps = 0f

    // Frame skipping for performance
    class ThrottledAnalyzer(
        private val processingIntervalMs: Long = 100, // Process every 100ms
        private val onResult: (AnalysisResult) -> Unit
    ) : ImageAnalysis.Analyzer {

        private var lastAnalyzedTimestamp = 0L

        private val objectDetector = com.google.mlkit.vision.objects.ObjectDetection.getClient(
            com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions.Builder()
                .setDetectorMode(com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .enableMultipleObjects()
                .build()
        )

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val currentTimestamp = System.currentTimeMillis()

            // Skip frames if processing too fast
            if (currentTimestamp - lastAnalyzedTimestamp < processingIntervalMs) {
                imageProxy.close()
                return
            }

            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                return
            }

            val image = InputImage.fromMediaImage(
                mediaImage, imageProxy.imageInfo.rotationDegrees
            )

            val startTime = System.nanoTime()

            objectDetector.process(image)
                .addOnSuccessListener { objects ->
                    val inferenceTimeMs = (System.nanoTime() - startTime) / 1_000_000

                    val result = AnalysisResult(
                        objects = objects.map { obj ->
                            DetectedObject(
                                boundingBox = obj.boundingBox,
                                labels = obj.labels.map { "${it.text} (${(it.confidence * 100).toInt()}%)" },
                                trackingId = obj.trackingId
                            )
                        },
                        inferenceTimeMs = inferenceTimeMs,
                        imageWidth = imageProxy.width,
                        imageHeight = imageProxy.height
                    )
                    onResult(result)
                }
                .addOnCompleteListener {
                    lastAnalyzedTimestamp = currentTimestamp
                    imageProxy.close()
                }
        }
    }

    data class AnalysisResult(
        val objects: List<DetectedObject>,
        val inferenceTimeMs: Long,
        val imageWidth: Int,
        val imageHeight: Int
    )

    data class DetectedObject(
        val boundingBox: android.graphics.Rect,
        val labels: List<String>,
        val trackingId: Int?
    )
}
```

---

## 3. Edge Detection with OpenCV Android

### Theory

```
Edge Detection Algorithms:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  1. CANNY EDGE DETECTOR (Most popular)               │
│     Steps:                                           │
│     ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│     │Gaussian  │─▶│Gradient  │─▶│Non-Max   │       │
│     │Blur      │  │(Sobel)   │  │Suppress  │       │
│     └──────────┘  └──────────┘  └──────────┘       │
│           │                          │               │
│     ┌─────▼──────┐  ┌──────────┐    │               │
│     │Double      │─▶│Edge      │◀───┘               │
│     │Threshold   │  │Tracking  │                     │
│     └────────────┘  └──────────┘                     │
│                                                      │
│  2. SOBEL OPERATOR                                   │
│     Computes gradient in X and Y directions          │
│     Good for finding directional edges               │
│                                                      │
│  3. LAPLACIAN                                        │
│     Second derivative — finds edges in all directions│
│                                                      │
│  Kernel Examples:                                    │
│  Sobel X:         Sobel Y:         Laplacian:        │
│  [-1  0  1]       [-1 -2 -1]       [0  1  0]        │
│  [-2  0  2]       [ 0  0  0]       [1 -4  1]        │
│  [-1  0  1]       [ 1  2  1]       [0  1  0]        │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class OpenCVHelper {

    init {
        if (!OpenCVLoader.initLocal()) {
            Log.e("OpenCV", "OpenCV initialization failed")
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully")
        }
    }

    // --- Canny Edge Detection ---
    fun cannyEdgeDetection(
        bitmap: Bitmap,
        lowThreshold: Double = 50.0,
        highThreshold: Double = 150.0
    ): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        // Convert to grayscale
        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

        // Apply Gaussian blur to reduce noise
        val blurred = Mat()
        Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 1.4)

        // Apply Canny edge detection
        val edges = Mat()
        Imgproc.Canny(blurred, edges, lowThreshold, highThreshold)

        // Convert back to bitmap
        val result = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(edges, result)

        // Release Mats
        src.release()
        gray.release()
        blurred.release()
        edges.release()

        return result
    }

    // --- Sobel Edge Detection ---
    fun sobelEdgeDetection(bitmap: Bitmap): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

        // Sobel X
        val gradX = Mat()
        Imgproc.Sobel(gray, gradX, CvType.CV_16S, 1, 0)

        // Sobel Y
        val gradY = Mat()
        Imgproc.Sobel(gray, gradY, CvType.CV_16S, 0, 1)

        // Convert to absolute and combine
        val absGradX = Mat()
        val absGradY = Mat()
        Core.convertScaleAbs(gradX, absGradX)
        Core.convertScaleAbs(gradY, absGradY)

        val combined = Mat()
        Core.addWeighted(absGradX, 0.5, absGradY, 0.5, 0.0, combined)

        val result = Bitmap.createBitmap(combined.cols(), combined.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(combined, result)

        // Cleanup
        listOf(src, gray, gradX, gradY, absGradX, absGradY, combined).forEach { it.release() }

        return result
    }

    // --- Contour Detection ---
    fun findContours(bitmap: Bitmap): Pair<Bitmap, List<MatOfPoint>> {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

        val blurred = Mat()
        Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)

        val edges = Mat()
        Imgproc.Canny(blurred, edges, 50.0, 150.0)

        // Find contours
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edges, contours, hierarchy, 
            Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        // Draw contours on original
        val output = src.clone()
        Imgproc.drawContours(output, contours, -1, Scalar(0.0, 255.0, 0.0), 2)

        val result = Bitmap.createBitmap(output.cols(), output.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(output, result)

        // Cleanup
        listOf(src, gray, blurred, edges, hierarchy, output).forEach { it.release() }

        return Pair(result, contours)
    }

    // --- Image Filters ---
    fun applyGaussianBlur(bitmap: Bitmap, kernelSize: Int = 15): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val blurred = Mat()
        Imgproc.GaussianBlur(src, blurred, Size(kernelSize.toDouble(), kernelSize.toDouble()), 0.0)

        val result = Bitmap.createBitmap(blurred.cols(), blurred.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(blurred, result)

        src.release()
        blurred.release()
        return result
    }

    fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

        val result = Bitmap.createBitmap(gray.cols(), gray.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(gray, result)

        src.release()
        gray.release()
        return result
    }

    fun adaptiveThreshold(bitmap: Bitmap): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

        val threshold = Mat()
        Imgproc.adaptiveThreshold(gray, threshold, 255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY, 11, 2.0)

        val result = Bitmap.createBitmap(threshold.cols(), threshold.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(threshold, result)

        src.release()
        gray.release()
        threshold.release()
        return result
    }
}
```

---

## 4. Image Segmentation

### Theory

```
Segmentation Types:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  1. SEMANTIC SEGMENTATION                            │
│     Every pixel gets a class label                   │
│     ┌─────────┐    ┌─────────┐                      │
│     │ Input   │───▶│ ████ Sky│                      │
│     │ Image   │    │ ░░░░ Tree│                      │
│     │         │    │ ▓▓▓▓ Road│                      │
│     └─────────┘    └─────────┘                      │
│                                                      │
│  2. INSTANCE SEGMENTATION                            │
│     Distinguishes individual objects of same class    │
│     ┌─────────┐    ┌─────────┐                      │
│     │ 2 cats  │───▶│ ▓▓ Cat1 │                      │
│     │ in pic  │    │ ░░ Cat2 │                      │
│     └─────────┘    └─────────┘                      │
│                                                      │
│  3. PANOPTIC SEGMENTATION                            │
│     Semantic + Instance combined                     │
│                                                      │
│  Models for Mobile:                                  │
│  ┌────────────────┬──────┬────────────────────────┐  │
│  │ Model          │ Size │ Classes                │  │
│  ├────────────────┼──────┼────────────────────────┤  │
│  │ DeepLabV3      │ 2 MB │ 21 (PASCAL VOC)       │  │
│  │ DeepLabV3+     │ 8 MB │ 21 (PASCAL VOC)       │  │
│  │ Selfie Segment │ 1 MB │ 2 (person/background) │  │
│  │ Custom TFLite  │ Var  │ Your classes           │  │
│  └────────────────┴──────┴────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter

class ImageSegmentationHelper(private val context: Context) {

    private var segmenter: ImageSegmenter? = null

    fun initialize() {
        val options = ImageSegmenter.ImageSegmenterOptions.builder()
            .setOutputType(ImageSegmenter.ImageSegmenterOptions.OutputType.CATEGORY_MASK)
            .setNumThreads(4)
            .build()

        segmenter = ImageSegmenter.createFromFileAndOptions(
            context, "deeplabv3.tflite", options
        )
    }

    fun segment(bitmap: Bitmap): SegmentationResult {
        val tensorImage = org.tensorflow.lite.support.image.TensorImage.fromBitmap(bitmap)
        val results = segmenter?.segment(tensorImage) ?: return SegmentationResult(bitmap, emptyList())

        val segmentation = results.first()
        val coloredLabels = segmentation.coloredLabels

        // Create colored mask overlay
        val masks = segmentation.masks
        val maskBitmap = createColoredMask(masks.first(), coloredLabels, bitmap.width, bitmap.height)

        // Blend original with mask
        val blended = blendImages(bitmap, maskBitmap, 0.5f)

        return SegmentationResult(
            overlayBitmap = blended,
            labels = coloredLabels.map { it.label }
        )
    }

    private fun createColoredMask(
        mask: org.tensorflow.lite.support.image.TensorImage,
        labels: List<org.tensorflow.lite.task.vision.segmenter.ColoredLabel>,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {
        val maskBuffer = mask.buffer
        val maskWidth = mask.width
        val maskHeight = mask.height

        val coloredBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(maskWidth * maskHeight)

        maskBuffer.rewind()
        for (i in pixels.indices) {
            val categoryIndex = (maskBuffer.get().toInt() and 0xFF).coerceIn(0, labels.size - 1)
            pixels[i] = labels[categoryIndex].argb
        }

        coloredBitmap.setPixels(pixels, 0, maskWidth, 0, 0, maskWidth, maskHeight)
        return Bitmap.createScaledBitmap(coloredBitmap, targetWidth, targetHeight, true)
    }

    private fun blendImages(original: Bitmap, overlay: Bitmap, alpha: Float): Bitmap {
        val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        canvas.drawBitmap(original, 0f, 0f, null)

        val paint = Paint().apply { this.alpha = (alpha * 255).toInt() }
        canvas.drawBitmap(overlay, 0f, 0f, paint)

        return result
    }

    data class SegmentationResult(
        val overlayBitmap: Bitmap,
        val labels: List<String>
    )

    fun close() = segmenter?.close()
}
```

---

## 5. Style Transfer Models

### Theory

```
Style Transfer:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Combines CONTENT of one image with STYLE of another │
│                                                      │
│  ┌──────────┐   ┌──────────┐   ┌──────────────────┐ │
│  │ Content  │ + │ Style    │ = │ Stylized Output  │ │
│  │ (Photo)  │   │ (Art)    │   │ (Photo as Art)   │ │
│  └──────────┘   └──────────┘   └──────────────────┘ │
│                                                      │
│  Two Approaches:                                     │
│  ┌──────────────────────────────────────────────┐    │
│  │ 1. ARBITRARY STYLE TRANSFER                  │    │
│  │    Any style image → any content image       │    │
│  │    Uses: Style prediction net + Transfer net  │    │
│  │    Speed: ~1 second on mobile                 │    │
│  │                                               │    │
│  │ 2. FIXED STYLE TRANSFER                      │    │
│  │    Pre-trained for specific styles            │    │
│  │    Much faster (~50ms)                        │    │
│  │    Limited to trained styles only             │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  Pipeline:                                           │
│  ┌──────────┐  ┌──────────────┐  ┌────────────────┐ │
│  │ Style    │─▶│ Style Pred.  │─▶│ Style Bottlnk │ │
│  │ Image    │  │ Network      │  │ (100-dim vec)  │ │
│  └──────────┘  └──────────────┘  └───────┬────────┘ │
│                                          │          │
│  ┌──────────┐  ┌──────────────┐  ┌───────▼────────┐ │
│  │ Content  │─▶│ Style        │─▶│ Stylized       │ │
│  │ Image    │  │ Transfer Net │  │ Output         │ │
│  └──────────┘  └──────────────┘  └────────────────┘ │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class StyleTransferHelper(private val context: Context) {

    private var stylePredictInterpreter: Interpreter? = null
    private var styleTransferInterpreter: Interpreter? = null

    private val styleImageSize = 256
    private val contentImageSize = 384
    private val styleBottleneckSize = 100

    fun initialize() {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }

        // Load style prediction model
        val stylePredictModel = loadModelFile("style_predict.tflite")
        stylePredictInterpreter = Interpreter(stylePredictModel, options)

        // Load style transfer model
        val styleTransferModel = loadModelFile("style_transfer.tflite")
        styleTransferInterpreter = Interpreter(styleTransferModel, options)
    }

    fun applyStyle(contentBitmap: Bitmap, styleBitmap: Bitmap): Bitmap {
        // Step 1: Extract style bottleneck from style image
        val styleBottleneck = predictStyle(styleBitmap)

        // Step 2: Transfer style to content image
        return transferStyle(contentBitmap, styleBottleneck)
    }

    private fun predictStyle(styleBitmap: Bitmap): FloatArray {
        val resized = Bitmap.createScaledBitmap(styleBitmap, styleImageSize, styleImageSize, true)
        val input = bitmapToFloatBuffer(resized, styleImageSize)

        // Output: style bottleneck [1, 1, 1, 100]
        val output = Array(1) { Array(1) { Array(1) { FloatArray(styleBottleneckSize) } } }

        stylePredictInterpreter?.run(input, output)

        return output[0][0][0]
    }

    private fun transferStyle(contentBitmap: Bitmap, styleBottleneck: FloatArray): Bitmap {
        val resized = Bitmap.createScaledBitmap(contentBitmap, contentImageSize, contentImageSize, true)
        val contentInput = bitmapToFloatBuffer(resized, contentImageSize)

        // Prepare style bottleneck input [1, 1, 1, 100]
        val styleInput = Array(1) { Array(1) { Array(1) { styleBottleneck } } }

        // Output: stylized image [1, 384, 384, 3]
        val output = Array(1) { Array(contentImageSize) { Array(contentImageSize) { FloatArray(3) } } }

        val inputs = arrayOf(contentInput, styleInput)
        val outputs = mapOf(0 to output)
        styleTransferInterpreter?.runForMultipleInputsOutputs(inputs, outputs)

        // Convert output to bitmap
        return floatArrayToBitmap(output[0], contentImageSize)
    }

    private fun bitmapToFloatBuffer(bitmap: Bitmap, size: Int): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * size * size * 3 * 4)
        buffer.order(java.nio.ByteOrder.nativeOrder())

        val pixels = IntArray(size * size)
        bitmap.getPixels(pixels, 0, size, 0, 0, size, size)

        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            buffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
            buffer.putFloat((pixel and 0xFF) / 255.0f)
        }
        buffer.rewind()
        return buffer
    }

    private fun floatArrayToBitmap(array: Array<Array<FloatArray>>, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        for (y in 0 until size) {
            for (x in 0 until size) {
                val r = (array[y][x][0].coerceIn(0f, 1f) * 255).toInt()
                val g = (array[y][x][1].coerceIn(0f, 1f) * 255).toInt()
                val b = (array[y][x][2].coerceIn(0f, 1f) * 255).toInt()
                bitmap.setPixel(x, y, android.graphics.Color.argb(255, r, g, b))
            }
        }

        return bitmap
    }

    private fun loadModelFile(path: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(path)
        return java.io.FileInputStream(fd.fileDescriptor).channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength
        )
    }

    fun close() {
        stylePredictInterpreter?.close()
        styleTransferInterpreter?.close()
    }
}
```
