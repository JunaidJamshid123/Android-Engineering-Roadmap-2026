# ML Kit (Google) — On-Device Machine Learning

## Overview

ML Kit is Google's mobile SDK that brings powerful machine learning capabilities to Android and iOS apps **without requiring deep ML expertise**. It provides both on-device and cloud-based APIs.

```
┌─────────────────────────────────────────────────────┐
│                   YOUR ANDROID APP                   │
├─────────────────────────────────────────────────────┤
│                    ML Kit SDK                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐            │
│  │   Text   │ │   Face   │ │ Barcode  │  ...more   │
│  │  Recog.  │ │ Detect.  │ │ Scanning │            │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘            │
│       │             │            │                   │
│  ┌────▼─────────────▼────────────▼─────┐            │
│  │        On-Device ML Models          │            │
│  │     (Bundled or Downloaded)         │            │
│  └─────────────────────────────────────┘            │
│       │                                              │
│  ┌────▼─────────────────────────────────┐           │
│  │     TensorFlow Lite Runtime          │           │
│  └──────────────────────────────────────┘           │
└─────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")
    
    // Face Detection
    implementation("com.google.mlkit:face-detection:16.1.5")
    
    // Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    
    // Image Labeling
    implementation("com.google.mlkit:image-labeling:17.0.7")
    
    // Object Detection
    implementation("com.google.mlkit:object-detection:17.0.1")
    
    // Pose Detection
    implementation("com.google.mlkit:pose-detection:18.0.0-beta3")
    implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta3")
    
    // Selfie Segmentation
    implementation("com.google.mlkit:segmentation-selfie:16.0.0-beta4")
    
    // Smart Reply
    implementation("com.google.mlkit:smart-reply:17.0.2")
    
    // Language Identification
    implementation("com.google.mlkit:language-id:17.0.4")
    
    // Translation
    implementation("com.google.mlkit:translate:17.0.1")
}
```

---

## 1. Text Recognition (OCR)

### Theory

Optical Character Recognition (OCR) converts images of text into machine-readable text. ML Kit uses deep learning models to detect and recognize text in Latin, Chinese, Devanagari, Japanese, and Korean scripts.

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Input Image │────▶│ Text Detect  │────▶│ Text Recog.  │
│  (Camera/    │     │ (Find text   │     │ (Convert to  │
│   Gallery)   │     │  regions)    │     │  strings)    │
└──────────────┘     └──────────────┘     └──────────────┘
                                                │
                                                ▼
                                    ┌──────────────────┐
                                    │   Structured     │
                                    │   Output:        │
                                    │   - Blocks       │
                                    │     - Lines      │
                                    │       - Elements │
                                    │         (Words)  │
                                    └──────────────────┘
```

**Text Hierarchy:**
- **Block** → A contiguous set of text lines (paragraph)
- **Line** → A contiguous set of words on the same axis
- **Element** → A single word

### Code

```kotlin
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionHelper(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // From Bitmap
    fun recognizeTextFromBitmap(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0) // 0 = rotation degrees
        processImage(image)
    }

    // From Camera (CameraX)
    fun recognizeFromCameraImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        processImage(image)
        imageProxy.close()
    }

    // From URI
    fun recognizeFromUri(uri: Uri) {
        val image = InputImage.fromFilePath(context, uri)
        processImage(image)
    }

    private fun processImage(image: InputImage) {
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                Log.d("OCR", "Full text: $fullText")

                // Iterate through blocks, lines, elements
                for (block in visionText.textBlocks) {
                    val blockText = block.text
                    val blockCornerPoints = block.cornerPoints
                    val blockFrame = block.boundingBox

                    for (line in block.lines) {
                        val lineText = line.text
                        val lineConfidence = line.confidence

                        for (element in line.elements) {
                            val elementText = element.text
                            val elementBounds = element.boundingBox
                            Log.d("OCR", "Word: $elementText, Bounds: $elementBounds")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Text recognition failed", e)
            }
    }

    fun close() {
        recognizer.close()
    }
}
```

### CameraX + Real-time OCR

```kotlin
class OcrAnalyzer(
    private val onTextDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

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

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotEmpty()) {
                    onTextDetected(visionText.text)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

// Usage with CameraX
fun setupCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    OcrAnalyzer { detectedText ->
                        Log.d("RealTimeOCR", detectedText)
                    }
                )
            }

        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis
        )
    }, ContextCompat.getMainExecutor(context))
}
```

---

## 2. Face Detection and Contours

### Theory

ML Kit Face Detection identifies faces in images and videos. It can detect:
- **Face bounding box** — rectangle around the face
- **Landmarks** — eyes, nose, mouth, ears, cheeks
- **Contours** — 133 points outlining facial features
- **Classification** — smiling probability, eyes open probability
- **Tracking** — consistent face ID across frames

```
Face Detection Pipeline:
┌──────┐    ┌───────────┐    ┌──────────┐    ┌───────────────┐
│Image │───▶│ Face      │───▶│ Landmark │───▶│ Classification│
│Input │    │ Detection │    │ Detection│    │ & Contours    │
└──────┘    └───────────┘    └──────────┘    └───────────────┘

Face Contour Points (133 total):
         ┌─────────────┐
        /   Forehead    \
       / ┌─────────────┐ \
      │  │  Left Eyebrow│  │
      │  └──────────────┘  │
      │  ●Left    Right●   │  ← Eye landmarks
      │   Eye      Eye     │
      │       ●            │  ← Nose
      │    ┌─────┐         │
      │    │Mouth│         │
      │    └─────┘         │
       \                  /
        \   Jawline      /
         └──────────────┘
```

### Code

```kotlin
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.google.mlkit.vision.face.FaceContour

class FaceDetectionHelper {

    // Configure options
    private val highAccuracyOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)  // Minimum face size relative to image
        .enableTracking()        // Assign consistent IDs
        .build()

    // Real-time options (faster, less accurate)
    private val realTimeOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(highAccuracyOptions)

    fun detectFaces(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        detector.process(image)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    // Bounding box
                    val bounds = face.boundingBox

                    // Head rotation
                    val rotY = face.headEulerAngleY  // Left-right rotation
                    val rotZ = face.headEulerAngleZ  // Tilt

                    // Classification
                    val smileProb = face.smilingProbability ?: -1f
                    val leftEyeOpenProb = face.leftEyeOpenProbability ?: -1f
                    val rightEyeOpenProb = face.rightEyeOpenProbability ?: -1f

                    Log.d("Face", """
                        Smile: ${smileProb * 100}%
                        Left eye open: ${leftEyeOpenProb * 100}%
                        Right eye open: ${rightEyeOpenProb * 100}%
                    """.trimIndent())

                    // Landmarks
                    face.getLandmark(FaceLandmark.LEFT_EYE)?.let { landmark ->
                        val leftEyePos = landmark.position
                        Log.d("Face", "Left eye at: (${leftEyePos.x}, ${leftEyePos.y})")
                    }

                    face.getLandmark(FaceLandmark.NOSE_BASE)?.let { landmark ->
                        val nosePos = landmark.position
                        Log.d("Face", "Nose at: (${nosePos.x}, ${nosePos.y})")
                    }

                    // Contours (133 points)
                    face.getContour(FaceContour.FACE)?.let { contour ->
                        val faceOval = contour.points
                        Log.d("Face", "Face contour has ${faceOval.size} points")
                    }

                    face.getContour(FaceContour.LEFT_EYE)?.let { contour ->
                        val leftEyeContour = contour.points
                        // Draw these points to create eye outline
                    }

                    // Tracking ID (consistent across frames)
                    val trackingId = face.trackingId
                    Log.d("Face", "Tracking ID: $trackingId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Face", "Detection failed", e)
            }
    }

    // Draw face annotations on canvas
    fun drawFaceAnnotations(canvas: Canvas, faces: List<com.google.mlkit.vision.face.Face>) {
        val boxPaint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        val pointPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
            strokeWidth = 5f
        }

        for (face in faces) {
            // Draw bounding box
            canvas.drawRect(face.boundingBox, boxPaint)

            // Draw contour points
            face.getContour(FaceContour.FACE)?.points?.forEach { point ->
                canvas.drawCircle(point.x, point.y, 3f, pointPaint)
            }
        }
    }

    fun close() {
        detector.close()
    }
}
```

---

## 3. Barcode Scanning

### Theory

ML Kit Barcode Scanning reads **1D and 2D barcodes** in real-time. It can decode structured data like URLs, Wi-Fi credentials, calendar events, and contact info.

```
Supported Barcode Formats:
┌─────────────────────────────────────────────┐
│ 1D Barcodes        │ 2D Barcodes           │
├─────────────────────┼───────────────────────┤
│ Code 128            │ QR Code               │
│ Code 39             │ Aztec                 │
│ Code 93             │ Data Matrix           │
│ Codabar             │ PDF417                │
│ EAN-13              │                       │
│ EAN-8               │                       │
│ ITF                 │                       │
│ UPC-A               │                       │
│ UPC-E               │                       │
└─────────────────────┴───────────────────────┘

Structured Data Types:
┌──────────────┬────────────────────────────┐
│ Type         │ Extracted Data             │
├──────────────┼────────────────────────────┤
│ URL          │ url, title                 │
│ WiFi         │ ssid, password, encryption │
│ Contact      │ name, phone, email, org    │
│ Calendar     │ summary, start, end, loc   │
│ Email        │ address, subject, body     │
│ Phone        │ number                     │
│ SMS          │ number, message            │
│ Geo          │ latitude, longitude        │
└──────────────┴────────────────────────────┘
```

### Code

```kotlin
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode

class BarcodeScannerHelper {

    // Restrict to specific formats for better performance
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_CODE_128
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    fun scanBarcodes(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    val format = barcode.format
                    val bounds = barcode.boundingBox

                    // Handle different value types
                    when (barcode.valueType) {
                        Barcode.TYPE_URL -> {
                            val url = barcode.url
                            Log.d("Barcode", "URL: ${url?.url}, Title: ${url?.title}")
                        }
                        Barcode.TYPE_WIFI -> {
                            val wifi = barcode.wifi
                            Log.d("Barcode", """
                                SSID: ${wifi?.ssid}
                                Password: ${wifi?.password}
                                Encryption: ${wifi?.encryptionType}
                            """.trimIndent())
                        }
                        Barcode.TYPE_CONTACT_INFO -> {
                            val contact = barcode.contactInfo
                            Log.d("Barcode", """
                                Name: ${contact?.name?.formattedName}
                                Phone: ${contact?.phones?.firstOrNull()?.number}
                                Email: ${contact?.emails?.firstOrNull()?.address}
                            """.trimIndent())
                        }
                        Barcode.TYPE_CALENDAR_EVENT -> {
                            val event = barcode.calendarEvent
                            Log.d("Barcode", """
                                Summary: ${event?.summary}
                                Start: ${event?.start}
                                End: ${event?.end}
                            """.trimIndent())
                        }
                        else -> {
                            Log.d("Barcode", "Raw value: $rawValue")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Barcode", "Scanning failed", e)
            }
    }

    // Real-time scanner with CameraX
    class BarcodeAnalyzer(
        private val onBarcodeDetected: (List<Barcode>) -> Unit
    ) : ImageAnalysis.Analyzer {

        private val scanner = BarcodeScanning.getClient()

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

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        onBarcodeDetected(barcodes)
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    fun close() {
        scanner.close()
    }
}
```

---

## 4. Image Labeling

### Theory

Image labeling identifies objects, locations, activities, animal species, products, and more in an image. Each label comes with a **confidence score**.

```
Input Image ──▶ ML Kit Image Labeler ──▶ Labels with Confidence

Example Output:
┌─────────────────────────────────────┐
│  📷 Photo of a park                 │
│                                     │
│  Label          Confidence          │
│  ─────────────  ──────────          │
│  Tree           0.95                │
│  Sky            0.92                │
│  Grass          0.89                │
│  Park           0.85                │
│  Nature         0.82                │
│  Outdoor        0.78                │
│  Cloud          0.65                │
└─────────────────────────────────────┘
```

### Code

```kotlin
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class ImageLabelingHelper {

    private val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)  // Only labels with 70%+ confidence
        .build()

    private val labeler = ImageLabeling.getClient(options)

    fun labelImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                for (label in labels) {
                    val text = label.text          // e.g., "Dog"
                    val confidence = label.confidence  // 0.0 to 1.0
                    val index = label.index         // Index in label list

                    Log.d("Label", "$text: ${(confidence * 100).toInt()}%")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Label", "Labeling failed", e)
            }
    }

    // Custom model labeling
    fun labelWithCustomModel(bitmap: Bitmap, modelPath: String) {
        val localModel = com.google.mlkit.common.model.LocalModel.Builder()
            .setAssetFilePath(modelPath)  // e.g., "custom_model.tflite"
            .build()

        val customOptions = com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
            .Builder(localModel)
            .setConfidenceThreshold(0.5f)
            .setMaxResultCount(5)
            .build()

        val customLabeler = ImageLabeling.getClient(customOptions)
        val image = InputImage.fromBitmap(bitmap, 0)

        customLabeler.process(image)
            .addOnSuccessListener { labels ->
                labels.forEach { label ->
                    Log.d("CustomLabel", "${label.text}: ${label.confidence}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("CustomLabel", "Failed", e)
            }
    }

    fun close() {
        labeler.close()
    }
}
```

---

## 5. Object Detection and Tracking

### Theory

Detects up to 5 objects in an image, identifies them, and tracks them across video frames with consistent IDs.

```
Detection Modes:
┌─────────────────────────────────────────────────┐
│ STREAM MODE (Real-time)                         │
│  - Low latency                                  │
│  - Returns most prominent object first          │
│  - Tracking IDs persist across frames           │
│                                                 │
│ SINGLE IMAGE MODE (Static)                      │
│  - Higher accuracy                              │
│  - Returns up to 5 objects                      │
│  - No tracking IDs                              │
└─────────────────────────────────────────────────┘

Output for each object:
┌──────────────────┐
│ Bounding Box     │ ← Rectangle around detected object
│ Tracking ID      │ ← Unique ID across frames
│ Labels[]         │ ← Category + confidence score
│  - Food (0.92)   │
│  - Plant (0.05)  │
└──────────────────┘
```

### Code

```kotlin
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

class ObjectDetectionHelper {

    // Stream mode (real-time from camera)
    private val streamOptions = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .enableMultipleObjects()
        .build()

    // Single image mode
    private val singleOptions = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableClassification()
        .enableMultipleObjects()
        .build()

    private val detector = ObjectDetection.getClient(streamOptions)

    fun detectObjects(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        detector.process(image)
            .addOnSuccessListener { detectedObjects ->
                for (obj in detectedObjects) {
                    val boundingBox = obj.boundingBox
                    val trackingId = obj.trackingId

                    for (label in obj.labels) {
                        val text = label.text           // e.g., "Fashion good"
                        val confidence = label.confidence
                        val index = label.index          // Category index

                        Log.d("Object", "[$trackingId] $text ($confidence) at $boundingBox")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Object", "Detection failed", e)
            }
    }

    fun close() {
        detector.close()
    }
}
```

---

## 6. Pose Detection

### Theory

Detects 33 body landmarks in real-time, enabling fitness tracking, gesture recognition, and AR effects.

```
33 Pose Landmarks:
                    0 (nose)
                   / \
         11(L.sh) /   \ 12(R.sh)
                 / │   \
        13(L.el)/  │    \14(R.el)
               /   │     \
      15(L.wr)/    │      \16(R.wr)
             /     │       \
   23(L.hip)───────┼────────24(R.hip)
             \     │       /
     25(L.kn) \    │      / 26(R.kn)
               \   │     /
      27(L.an)  \  │    / 28(R.an)
                 \ │   /
        29(L.heel)\│  / 30(R.heel)
                   │ /
           31(L.ft)│/ 32(R.ft)

Each landmark has:
- x, y coordinates (image space)
- z coordinate (depth, relative to hip)
- inFrameLikelihood (0.0 to 1.0)
```

### Code

```kotlin
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.math.atan2

class PoseDetectionHelper {

    // Base model (faster)
    private val baseOptions = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    // Accurate model
    private val accurateOptions = com.google.mlkit.vision.pose.accurate
        .AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(com.google.mlkit.vision.pose.accurate
            .AccuratePoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector = PoseDetection.getClient(baseOptions)

    fun detectPose(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                // Get specific landmarks
                val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
                val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
                val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
                val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)

                leftShoulder?.let { shoulder ->
                    val position = shoulder.position
                    val x = position.x
                    val y = position.y
                    val inFrame = shoulder.inFrameLikelihood
                    Log.d("Pose", "Left shoulder: ($x, $y), confidence: $inFrame")
                }

                // Calculate angle between three points (e.g., elbow angle)
                if (leftShoulder != null && leftElbow != null && leftWrist != null) {
                    val angle = calculateAngle(
                        leftShoulder.position,
                        leftElbow.position,
                        leftWrist.position
                    )
                    Log.d("Pose", "Left elbow angle: $angle°")
                }

                // All landmarks
                val allLandmarks = pose.allPoseLandmarks
                Log.d("Pose", "Detected ${allLandmarks.size} landmarks")
            }
            .addOnFailureListener { e ->
                Log.e("Pose", "Detection failed", e)
            }
    }

    // Calculate angle between three points
    private fun calculateAngle(
        first: com.google.mlkit.vision.common.PointF3D,
        mid: com.google.mlkit.vision.common.PointF3D,
        last: com.google.mlkit.vision.common.PointF3D
    ): Double {
        val angle = Math.toDegrees(
            (atan2(
                (last.y - mid.y).toDouble(),
                (last.x - mid.x).toDouble()
            ) - atan2(
                (first.y - mid.y).toDouble(),
                (first.x - mid.x).toDouble()
            ))
        )
        var result = Math.abs(angle)
        if (result > 180) result = 360.0 - result
        return result
    }

    fun close() {
        poseDetector.close()
    }
}
```

---

## 7. Selfie Segmentation

### Theory

Separates the subject (person) from the background in real-time, producing a **segmentation mask**.

```
┌─────────────┐    ┌─────────────┐    ┌─────────────────┐
│ Input Image │───▶│ Segmentation│───▶│ Mask (0.0-1.0)  │
│ (with       │    │ Model       │    │ per pixel        │
│  person)    │    │             │    │                  │
└─────────────┘    └─────────────┘    └─────────────────┘
                                            │
                          ┌─────────────────┼──────────────────┐
                          ▼                 ▼                  ▼
                   ┌────────────┐   ┌─────────────┐   ┌────────────┐
                   │ Background │   │ Background   │   │ Portrait   │
                   │ Blur       │   │ Replacement  │   │ Mode       │
                   └────────────┘   └─────────────┘   └────────────┘

Mask values:
  0.0 = definitely background
  1.0 = definitely foreground (person)
  0.5 = uncertain boundary
```

### Code

```kotlin
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions

class SelfieSegmentationHelper {

    private val options = SelfieSegmenterOptions.Builder()
        .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
        .enableRawSizeMask() // Get mask at original image resolution
        .build()

    private val segmenter = Segmentation.getClient(options)

    fun segmentSelfie(bitmap: Bitmap, onResult: (Bitmap) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        segmenter.process(image)
            .addOnSuccessListener { segmentationMask ->
                val mask = segmentationMask.buffer
                val maskWidth = segmentationMask.width
                val maskHeight = segmentationMask.height

                // Create blurred background
                val result = applyBackgroundBlur(bitmap, mask, maskWidth, maskHeight)
                onResult(result)
            }
            .addOnFailureListener { e ->
                Log.e("Selfie", "Segmentation failed", e)
            }
    }

    private fun applyBackgroundBlur(
        original: Bitmap,
        mask: java.nio.ByteBuffer,
        maskWidth: Int,
        maskHeight: Int
    ): Bitmap {
        val width = original.width
        val height = original.height
        val result = original.copy(Bitmap.Config.ARGB_8888, true)

        // Scale mask to match image dimensions
        mask.rewind()

        for (y in 0 until maskHeight) {
            for (x in 0 until maskWidth) {
                val confidence = mask.float  // foreground confidence

                // Map mask coordinates to image coordinates
                val imgX = (x.toFloat() / maskWidth * width).toInt().coerceIn(0, width - 1)
                val imgY = (y.toFloat() / maskHeight * height).toInt().coerceIn(0, height - 1)

                if (confidence < 0.5f) {
                    // Background pixel — apply effect (e.g., dim, blur, replace)
                    val pixel = original.getPixel(imgX, imgY)
                    val dimmed = Color.argb(
                        Color.alpha(pixel),
                        (Color.red(pixel) * 0.3).toInt(),
                        (Color.green(pixel) * 0.3).toInt(),
                        (Color.blue(pixel) * 0.3).toInt()
                    )
                    result.setPixel(imgX, imgY, dimmed)
                }
            }
        }
        return result
    }

    fun close() {
        segmenter.close()
    }
}
```

---

## 8. Smart Reply Suggestions

### Theory

Generates contextually appropriate reply suggestions for chat conversations.

```
Conversation Input:
┌─────────────────────────────────────┐
│ User A: "Want to grab lunch?"       │
│ User A: "I know a great new place" │
│ User B: ???                         │
└─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────┐
│ Smart Reply Suggestions:            │
│  1. "Sure, sounds great!"          │
│  2. "Where is it?"                 │
│  3. "Sorry, I can't today"         │
└─────────────────────────────────────┘
```

### Code

```kotlin
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.TextMessage

class SmartReplyHelper {

    private val smartReply = SmartReply.getClient()

    fun getSuggestions(
        conversation: List<ChatMessage>,
        localUserId: String,
        onSuggestions: (List<String>) -> Unit
    ) {
        val mlConversation = conversation.map { msg ->
            if (msg.senderId == localUserId) {
                TextMessage.createForLocalUser(msg.text, msg.timestamp)
            } else {
                TextMessage.createForRemoteUser(msg.text, msg.timestamp, msg.senderId)
            }
        }

        smartReply.suggestReplies(mlConversation)
            .addOnSuccessListener { result ->
                if (result.status == com.google.mlkit.nl.smartreply.SmartReplySuggestionResult.STATUS_SUCCESS) {
                    val suggestions = result.suggestions.map { it.text }
                    onSuggestions(suggestions)
                } else {
                    Log.d("SmartReply", "No suggestions available")
                    onSuggestions(emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.e("SmartReply", "Failed to get suggestions", e)
            }
    }

    fun close() {
        smartReply.close()
    }
}

data class ChatMessage(
    val text: String,
    val senderId: String,
    val timestamp: Long
)
```

---

## 9. Language Identification and Translation

### Theory

```
Language Identification:
  Input: "Bonjour le monde"  ──▶  Output: "fr" (French, 0.98 confidence)

Translation Pipeline:
┌──────────┐    ┌──────────────┐    ┌───────────────┐    ┌──────────┐
│  Source   │───▶│   Download   │───▶│   Translate   │───▶│  Target  │
│  Text     │    │   Language   │    │   On-Device   │    │  Text    │
│ (French)  │    │   Model      │    │               │    │(English) │
│           │    │   (~30MB)    │    │               │    │          │
└──────────┘    └──────────────┘    └───────────────┘    └──────────┘

Supports 58+ languages with on-device translation.
Models are ~30MB each, downloaded on demand.
```

### Code

```kotlin
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class LanguageHelper {

    // --- Language Identification ---
    private val languageIdentifier = LanguageIdentification.getClient()

    fun identifyLanguage(text: String) {
        // Get most likely language
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    Log.d("Lang", "Can't identify language")
                } else {
                    Log.d("Lang", "Language: $languageCode")
                }
            }

        // Get all possible languages with confidence
        languageIdentifier.identifyPossibleLanguages(text)
            .addOnSuccessListener { identifiedLanguages ->
                for (lang in identifiedLanguages) {
                    Log.d("Lang", "${lang.languageTag}: ${lang.confidence}")
                    // e.g., "fr: 0.98", "it: 0.01"
                }
            }
    }

    // --- Translation ---
    fun translateText(
        sourceText: String,
        sourceLang: String,  // e.g., "fr"
        targetLang: String,  // e.g., "en"
        onResult: (String) -> Unit
    ) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()

        val translator = Translation.getClient(options)

        // Download model if needed (requires Wi-Fi by default)
        val conditions = com.google.mlkit.common.model.DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // Model downloaded, now translate
                translator.translate(sourceText)
                    .addOnSuccessListener { translatedText ->
                        onResult(translatedText)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Translate", "Translation failed", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Translate", "Model download failed", e)
            }
    }

    // Manage downloaded models
    fun listDownloadedModels() {
        val modelManager = com.google.mlkit.common.model.RemoteModelManager.getInstance()
        modelManager.getDownloadedModels(com.google.mlkit.nl.translate.TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                models.forEach { model ->
                    Log.d("Translate", "Downloaded model: ${model.language}")
                }
            }
    }

    fun deleteModel(languageCode: String) {
        val modelManager = com.google.mlkit.common.model.RemoteModelManager.getInstance()
        val model = com.google.mlkit.nl.translate.TranslateRemoteModel.Builder(languageCode).build()
        modelManager.deleteDownloadedModel(model)
            .addOnSuccessListener {
                Log.d("Translate", "$languageCode model deleted")
            }
    }

    fun close() {
        languageIdentifier.close()
    }
}
```

---

## 10. Custom Model Deployment with ML Kit

### Theory

Deploy your own TensorFlow Lite models through ML Kit's Custom Model APIs.

```
Custom Model Deployment Options:
┌─────────────────────────────────────────────────────┐
│                                                     │
│  Option 1: BUNDLED with APK                         │
│  ┌──────────┐                                       │
│  │ .tflite  │ ──▶ assets/ folder ──▶ LocalModel     │
│  │  model   │                                       │
│  └──────────┘     Pros: Always available            │
│                   Cons: Increases APK size           │
│                                                     │
│  Option 2: DOWNLOADED from Firebase                 │
│  ┌──────────┐                                       │
│  │ Firebase  │ ──▶ Download on demand ──▶ RemoteModel│
│  │ ML Model │                                       │
│  │ Hosting  │     Pros: Smaller APK, A/B testing    │
│  └──────────┘     Cons: Requires network initially  │
│                                                     │
│  Option 3: BOTH (Recommended)                       │
│  Bundled as fallback + remote for updates           │
└─────────────────────────────────────────────────────┘
```

### Code

```kotlin
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.common.model.CustomRemoteModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader

class CustomModelHelper {

    // Local model bundled in assets
    private val localModel = LocalModel.Builder()
        .setAssetFilePath("custom_classifier.tflite")
        .build()

    // Remote model from Firebase
    private val remoteModel = CustomRemoteModel.Builder(
        com.google.firebase.ml.modeldownloader.CustomModel.Builder("my_model").build()
    ).build()

    // Use for image labeling
    fun labelWithCustomModel(bitmap: Bitmap) {
        val customLabelerOptions = com.google.mlkit.vision.label.custom
            .CustomImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.5f)
            .setMaxResultCount(10)
            .build()

        val labeler = com.google.mlkit.vision.label.ImageLabeling
            .getClient(customLabelerOptions)

        val image = InputImage.fromBitmap(bitmap, 0)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                labels.forEach { label ->
                    Log.d("Custom", "${label.text}: ${label.confidence}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Custom", "Labeling failed", e)
            }
    }

    // Download remote model from Firebase
    fun downloadRemoteModel() {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()

        FirebaseModelDownloader.getInstance()
            .getModel("my_model",
                com.google.firebase.ml.modeldownloader.DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
                conditions)
            .addOnSuccessListener { model ->
                val modelFile = model.file
                if (modelFile != null) {
                    Log.d("Custom", "Model downloaded: ${modelFile.absolutePath}")
                    // Use the model file with TFLite interpreter
                }
            }
            .addOnFailureListener { e ->
                Log.e("Custom", "Model download failed", e)
            }
    }
}
```

---

## Summary Comparison Table

```
┌───────────────────────┬───────────┬────────────┬────────────────┐
│ Feature               │ On-Device │ Cloud      │ Custom Model   │
├───────────────────────┼───────────┼────────────┼────────────────┤
│ Text Recognition      │ ✅         │ ✅ (better)│ ❌              │
│ Face Detection        │ ✅         │ ❌         │ ❌              │
│ Barcode Scanning      │ ✅         │ ❌         │ ❌              │
│ Image Labeling        │ ✅         │ ✅ (better)│ ✅              │
│ Object Detection      │ ✅         │ ❌         │ ✅              │
│ Pose Detection        │ ✅         │ ❌         │ ❌              │
│ Selfie Segmentation   │ ✅         │ ❌         │ ❌              │
│ Smart Reply           │ ✅         │ ❌         │ ❌              │
│ Language ID           │ ✅         │ ❌         │ ❌              │
│ Translation           │ ✅         │ ❌         │ ❌              │
├───────────────────────┼───────────┼────────────┼────────────────┤
│ Latency               │ Low       │ Higher     │ Varies         │
│ Offline               │ ✅         │ ❌         │ ✅ (bundled)    │
│ Cost                  │ Free      │ Pay-per-use│ Free (on-dev)  │
└───────────────────────┴───────────┴────────────┴────────────────┘
```
