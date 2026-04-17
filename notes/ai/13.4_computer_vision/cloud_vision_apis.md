# Cloud Vision APIs — Computer Vision Services

## Overview

Cloud Vision APIs provide powerful image analysis powered by Google's large-scale ML models. They offer higher accuracy and more features than on-device solutions, at the cost of network latency and API costs.

```
Cloud Vision Architecture:
┌──────────────────────────────────────────────────────┐
│                 Android App                           │
│  ┌────────────────────────────────────────────────┐  │
│  │  Image → Base64 encode → REST API call          │  │
│  └─────────────────────┬──────────────────────────┘  │
└────────────────────────┼─────────────────────────────┘
                         │ HTTPS (Image as base64 or GCS URI)
┌────────────────────────▼─────────────────────────────┐
│          Google Cloud Vision API                      │
│  ┌──────────────────────────────────────────────┐    │
│  │ Features:                                     │    │
│  │  • Label Detection       (What's in image?)   │    │
│  │  • Text Detection/OCR    (Read text)          │    │
│  │  • Face Detection        (Faces + emotions)   │    │
│  │  • Object Localization   (Objects + bboxes)   │    │
│  │  • Logo Detection        (Brand logos)        │    │
│  │  • Landmark Detection    (Famous places)      │    │
│  │  • Image Properties      (Colors, crops)      │    │
│  │  • Safe Search Detection (Content safety)     │    │
│  │  • Web Detection         (Similar web images) │    │
│  │  • Product Search        (Visual product ID)  │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  Pricing (per 1000 images):                          │
│  ┌──────────────────────────────────────────────┐    │
│  │ First 1000/month: FREE                        │    │
│  │ Label Detection: $1.50                        │    │
│  │ Text Detection: $1.50                         │    │
│  │ Face Detection: $1.50                         │    │
│  │ Object Localization: $2.25                    │    │
│  │ Safe Search: $1.50                            │    │
│  │ Multiple features: $3.50                      │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  Cloud vs On-Device:                                 │
│  ┌──────────────┬──────────────┬──────────────┐     │
│  │              │ Cloud Vision │ On-Device     │     │
│  ├──────────────┼──────────────┼──────────────┤     │
│  │ Accuracy     │ ████████████ │ ████████     │     │
│  │ Languages    │ 100+ (OCR)   │ ~10-20       │     │
│  │ Categories   │ 10000+       │ ~400-600     │     │
│  │ Offline      │ ❌           │ ✅           │     │
│  │ Latency      │ 200-2000ms   │ 20-100ms     │     │
│  │ Cost         │ Per-request  │ Free         │     │
│  │ Privacy      │ ❌ (sent)    │ ✅ (local)   │     │
│  │ Model update │ Automatic    │ SDK update   │     │
│  └──────────────┴──────────────┴──────────────┘     │
└──────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Option 1: REST API with Retrofit (recommended for Android)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Option 2: Google Cloud client library (heavier)
    // implementation("com.google.cloud:google-cloud-vision:3.31.0")
}
```

---

## 1. Google Cloud Vision API

### Data Models

```kotlin
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Request ---
@JsonClass(generateAdapter = true)
data class VisionRequest(
    val requests: List<AnnotateImageRequest>
)

@JsonClass(generateAdapter = true)
data class AnnotateImageRequest(
    val image: ImageInput,
    val features: List<Feature>,
    val imageContext: ImageContext? = null
)

@JsonClass(generateAdapter = true)
data class ImageInput(
    val content: String? = null,    // Base64-encoded image
    val source: ImageSource? = null  // GCS URI
)

@JsonClass(generateAdapter = true)
data class ImageSource(
    @Json(name = "imageUri") val imageUri: String?,    // e.g., gs://bucket/image.jpg
    @Json(name = "gcsImageUri") val gcsImageUri: String? = null
)

@JsonClass(generateAdapter = true)
data class Feature(
    val type: String,        // LABEL_DETECTION, TEXT_DETECTION, etc.
    val maxResults: Int = 10
)

@JsonClass(generateAdapter = true)
data class ImageContext(
    val languageHints: List<String>? = null,  // ["en", "fr"]
    val cropHintsParams: CropHintsParams? = null
)

@JsonClass(generateAdapter = true)
data class CropHintsParams(
    val aspectRatios: List<Float>  // [1.0, 1.78]
)

// --- Response ---
@JsonClass(generateAdapter = true)
data class VisionResponse(
    val responses: List<AnnotateImageResponse>
)

@JsonClass(generateAdapter = true)
data class AnnotateImageResponse(
    val labelAnnotations: List<EntityAnnotation>? = null,
    val textAnnotations: List<EntityAnnotation>? = null,
    val fullTextAnnotation: FullTextAnnotation? = null,
    val faceAnnotations: List<FaceAnnotation>? = null,
    val localizedObjectAnnotations: List<LocalizedObjectAnnotation>? = null,
    val logoAnnotations: List<EntityAnnotation>? = null,
    val landmarkAnnotations: List<EntityAnnotation>? = null,
    val safeSearchAnnotation: SafeSearchAnnotation? = null,
    val imagePropertiesAnnotation: ImagePropertiesAnnotation? = null,
    val webDetection: WebDetection? = null,
    val error: StatusError? = null
)

@JsonClass(generateAdapter = true)
data class EntityAnnotation(
    val mid: String? = null,
    val locale: String? = null,
    val description: String,
    val score: Float? = null,
    val confidence: Float? = null,
    val topicality: Float? = null,
    val boundingPoly: BoundingPoly? = null,
    val locations: List<LocationInfo>? = null
)

@JsonClass(generateAdapter = true)
data class BoundingPoly(
    val vertices: List<Vertex>? = null,
    val normalizedVertices: List<NormalizedVertex>? = null
)

@JsonClass(generateAdapter = true)
data class Vertex(val x: Int?, val y: Int?)

@JsonClass(generateAdapter = true)
data class NormalizedVertex(val x: Float?, val y: Float?)

@JsonClass(generateAdapter = true)
data class LocationInfo(val latLng: LatLng?)

@JsonClass(generateAdapter = true)
data class LatLng(val latitude: Double, val longitude: Double)

@JsonClass(generateAdapter = true)
data class FaceAnnotation(
    val boundingPoly: BoundingPoly,
    val fdBoundingPoly: BoundingPoly,
    val landmarks: List<FaceLandmark>,
    val rollAngle: Float,
    val panAngle: Float,
    val tiltAngle: Float,
    val detectionConfidence: Float,
    val landmarkingConfidence: Float,
    val joyLikelihood: String,
    val sorrowLikelihood: String,
    val angerLikelihood: String,
    val surpriseLikelihood: String,
    val underExposedLikelihood: String,
    val blurredLikelihood: String,
    val headwearLikelihood: String
)

@JsonClass(generateAdapter = true)
data class FaceLandmark(
    val type: String,
    val position: Position3D
)

@JsonClass(generateAdapter = true)
data class Position3D(val x: Float, val y: Float, val z: Float)

@JsonClass(generateAdapter = true)
data class LocalizedObjectAnnotation(
    val mid: String?,
    val name: String,
    val score: Float,
    val boundingPoly: BoundingPoly
)

@JsonClass(generateAdapter = true)
data class SafeSearchAnnotation(
    val adult: String,      // VERY_UNLIKELY to VERY_LIKELY
    val spoof: String,
    val medical: String,
    val violence: String,
    val racy: String
)

@JsonClass(generateAdapter = true)
data class ImagePropertiesAnnotation(
    val dominantColors: DominantColors
)

@JsonClass(generateAdapter = true)
data class DominantColors(val colors: List<ColorInfo>)

@JsonClass(generateAdapter = true)
data class ColorInfo(
    val color: RGBColor,
    val score: Float,
    val pixelFraction: Float
)

@JsonClass(generateAdapter = true)
data class RGBColor(val red: Float?, val green: Float?, val blue: Float?)

@JsonClass(generateAdapter = true)
data class FullTextAnnotation(
    val text: String,
    val pages: List<Page>?
)

@JsonClass(generateAdapter = true)
data class Page(val blocks: List<Block>?)

@JsonClass(generateAdapter = true)
data class Block(val paragraphs: List<Paragraph>?)

@JsonClass(generateAdapter = true)
data class Paragraph(val words: List<Word>?)

@JsonClass(generateAdapter = true)
data class Word(val symbols: List<Symbol>?)

@JsonClass(generateAdapter = true)
data class Symbol(val text: String, val confidence: Float?)

@JsonClass(generateAdapter = true)
data class WebDetection(
    val webEntities: List<WebEntity>?,
    val fullMatchingImages: List<WebImage>?,
    val partialMatchingImages: List<WebImage>?,
    val pagesWithMatchingImages: List<WebPage>?,
    val visuallySimilarImages: List<WebImage>?,
    val bestGuessLabels: List<BestGuessLabel>?
)

@JsonClass(generateAdapter = true)
data class WebEntity(val entityId: String?, val score: Float?, val description: String?)

@JsonClass(generateAdapter = true)
data class WebImage(val url: String?, val score: Float?)

@JsonClass(generateAdapter = true)
data class WebPage(val url: String?, val pageTitle: String?, val score: Float?)

@JsonClass(generateAdapter = true)
data class BestGuessLabel(val label: String?)

@JsonClass(generateAdapter = true)
data class StatusError(val code: Int?, val message: String?)
```

### API Interface & Client

```kotlin
// Retrofit API
interface CloudVisionApi {
    @POST("v1/images:annotate")
    suspend fun annotateImage(
        @Query("key") apiKey: String,
        @Body request: VisionRequest
    ): VisionResponse
}

// Client
class CloudVisionClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://vision.googleapis.com/")
        .addConverterFactory(MoshiConverterFactory.create(
            Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
        ))
        .client(OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build())
        .build()

    val api: CloudVisionApi = retrofit.create(CloudVisionApi::class.java)
}
```

### Repository — All Features

```kotlin
class CloudVisionRepository(private val apiKey: String) {

    private val client = CloudVisionClient()

    // Convert bitmap to base64
    private fun bitmapToBase64(bitmap: Bitmap, maxSize: Int = 1024): String {
        // Resize if too large
        val scaledBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else bitmap

        val stream = java.io.ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
    }

    // --- Label Detection (What's in the image) ---
    suspend fun detectLabels(bitmap: Bitmap, maxResults: Int = 10): List<LabelResult> {
        val request = createRequest(bitmap, "LABEL_DETECTION", maxResults)
        val response = client.api.annotateImage(apiKey, request)

        return response.responses.first().labelAnnotations?.map {
            LabelResult(it.description, it.score ?: 0f)
        } ?: emptyList()
    }

    data class LabelResult(val label: String, val confidence: Float)

    // --- OCR (Text Detection) ---
    suspend fun detectText(bitmap: Bitmap): OCRResult {
        val request = VisionRequest(
            requests = listOf(
                AnnotateImageRequest(
                    image = ImageInput(content = bitmapToBase64(bitmap)),
                    features = listOf(
                        Feature("TEXT_DETECTION", 1),
                        Feature("DOCUMENT_TEXT_DETECTION", 1)  // Better for documents
                    )
                )
            )
        )

        val response = client.api.annotateImage(apiKey, request)
        val result = response.responses.first()

        return OCRResult(
            fullText = result.fullTextAnnotation?.text ?: "",
            blocks = result.textAnnotations?.map { annotation ->
                TextBlock(
                    text = annotation.description,
                    boundingBox = annotation.boundingPoly,
                    language = annotation.locale
                )
            } ?: emptyList()
        )
    }

    data class OCRResult(val fullText: String, val blocks: List<TextBlock>)
    data class TextBlock(val text: String, val boundingBox: BoundingPoly?, val language: String?)

    // --- Face Detection ---
    suspend fun detectFaces(bitmap: Bitmap): List<FaceResult> {
        val request = createRequest(bitmap, "FACE_DETECTION", 10)
        val response = client.api.annotateImage(apiKey, request)

        return response.responses.first().faceAnnotations?.map { face ->
            FaceResult(
                confidence = face.detectionConfidence,
                joy = face.joyLikelihood,
                sorrow = face.sorrowLikelihood,
                anger = face.angerLikelihood,
                surprise = face.surpriseLikelihood,
                headwear = face.headwearLikelihood,
                rollAngle = face.rollAngle,
                panAngle = face.panAngle,
                tiltAngle = face.tiltAngle,
                boundingPoly = face.boundingPoly
            )
        } ?: emptyList()
    }

    data class FaceResult(
        val confidence: Float,
        val joy: String,
        val sorrow: String,
        val anger: String,
        val surprise: String,
        val headwear: String,
        val rollAngle: Float,
        val panAngle: Float,
        val tiltAngle: Float,
        val boundingPoly: BoundingPoly
    ) {
        val dominantEmotion: String get() {
            val emotions = mapOf(
                "Joy" to likelihoodToScore(joy),
                "Sorrow" to likelihoodToScore(sorrow),
                "Anger" to likelihoodToScore(anger),
                "Surprise" to likelihoodToScore(surprise)
            )
            return emotions.maxByOrNull { it.value }?.key ?: "Neutral"
        }

        private fun likelihoodToScore(likelihood: String): Int = when (likelihood) {
            "VERY_LIKELY" -> 5
            "LIKELY" -> 4
            "POSSIBLE" -> 3
            "UNLIKELY" -> 2
            "VERY_UNLIKELY" -> 1
            else -> 0
        }
    }

    // --- Object Localization ---
    suspend fun localizeObjects(bitmap: Bitmap): List<ObjectResult> {
        val request = createRequest(bitmap, "OBJECT_LOCALIZATION", 10)
        val response = client.api.annotateImage(apiKey, request)

        return response.responses.first().localizedObjectAnnotations?.map { obj ->
            ObjectResult(
                name = obj.name,
                confidence = obj.score,
                boundingPoly = obj.boundingPoly
            )
        } ?: emptyList()
    }

    data class ObjectResult(
        val name: String,
        val confidence: Float,
        val boundingPoly: BoundingPoly
    )

    // --- Safe Search Detection ---
    suspend fun safeSearchDetection(bitmap: Bitmap): SafeSearchResult {
        val request = createRequest(bitmap, "SAFE_SEARCH_DETECTION", 1)
        val response = client.api.annotateImage(apiKey, request)

        val annotation = response.responses.first().safeSearchAnnotation
        return SafeSearchResult(
            adult = annotation?.adult ?: "UNKNOWN",
            violence = annotation?.violence ?: "UNKNOWN",
            racy = annotation?.racy ?: "UNKNOWN",
            medical = annotation?.medical ?: "UNKNOWN",
            spoof = annotation?.spoof ?: "UNKNOWN"
        )
    }

    data class SafeSearchResult(
        val adult: String,
        val violence: String,
        val racy: String,
        val medical: String,
        val spoof: String
    ) {
        val isSafe: Boolean get() {
            val unsafeLevels = setOf("LIKELY", "VERY_LIKELY")
            return adult !in unsafeLevels &&
                violence !in unsafeLevels &&
                racy !in unsafeLevels
        }
    }

    // --- Multi-feature analysis (most efficient) ---
    suspend fun fullAnalysis(bitmap: Bitmap): FullAnalysisResult {
        val request = VisionRequest(
            requests = listOf(
                AnnotateImageRequest(
                    image = ImageInput(content = bitmapToBase64(bitmap)),
                    features = listOf(
                        Feature("LABEL_DETECTION", 10),
                        Feature("TEXT_DETECTION", 10),
                        Feature("FACE_DETECTION", 5),
                        Feature("OBJECT_LOCALIZATION", 10),
                        Feature("SAFE_SEARCH_DETECTION", 1),
                        Feature("IMAGE_PROPERTIES", 1),
                        Feature("LOGO_DETECTION", 5),
                        Feature("LANDMARK_DETECTION", 5),
                        Feature("WEB_DETECTION", 5)
                    )
                )
            )
        )

        val response = client.api.annotateImage(apiKey, request)
        val result = response.responses.first()

        return FullAnalysisResult(
            labels = result.labelAnnotations?.map { LabelResult(it.description, it.score ?: 0f) },
            text = result.fullTextAnnotation?.text,
            faces = result.faceAnnotations?.size ?: 0,
            objects = result.localizedObjectAnnotations?.map { ObjectResult(it.name, it.score, it.boundingPoly) },
            safeSearch = result.safeSearchAnnotation?.let {
                SafeSearchResult(it.adult, it.violence, it.racy, it.medical, it.spoof)
            },
            dominantColors = result.imagePropertiesAnnotation?.dominantColors?.colors?.map {
                DominantColor(
                    red = it.color.red?.toInt() ?: 0,
                    green = it.color.green?.toInt() ?: 0,
                    blue = it.color.blue?.toInt() ?: 0,
                    score = it.score,
                    pixelFraction = it.pixelFraction
                )
            },
            logos = result.logoAnnotations?.map { it.description },
            landmarks = result.landmarkAnnotations?.map {
                LandmarkResult(
                    name = it.description,
                    confidence = it.score ?: 0f,
                    location = it.locations?.firstOrNull()?.latLng
                )
            },
            webDetection = result.webDetection
        )
    }

    data class FullAnalysisResult(
        val labels: List<LabelResult>?,
        val text: String?,
        val faces: Int,
        val objects: List<ObjectResult>?,
        val safeSearch: SafeSearchResult?,
        val dominantColors: List<DominantColor>?,
        val logos: List<String>?,
        val landmarks: List<LandmarkResult>?,
        val webDetection: WebDetection?
    )

    data class DominantColor(val red: Int, val green: Int, val blue: Int, val score: Float, val pixelFraction: Float)
    data class LandmarkResult(val name: String, val confidence: Float, val location: LatLng?)

    // Helper to create simple request
    private fun createRequest(bitmap: Bitmap, featureType: String, maxResults: Int): VisionRequest {
        return VisionRequest(
            requests = listOf(
                AnnotateImageRequest(
                    image = ImageInput(content = bitmapToBase64(bitmap)),
                    features = listOf(Feature(featureType, maxResults))
                )
            )
        )
    }
}
```

---

## 2. Custom Image Classification with AutoML

### Theory

```
AutoML Vision:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Train custom image classifiers WITHOUT ML expertise │
│                                                      │
│  Workflow:                                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────┐ │
│  │ Upload   │─▶│ Label    │─▶│ Train    │─▶│Test │ │
│  │ Images   │  │ Images   │  │ Model    │  │& Use│ │
│  │ (~100+   │  │ Classes  │  │ (AutoML) │  │     │ │
│  │ per class)│  │          │  │          │  │     │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────┘ │
│                                                      │
│  Deployment Options:                                 │
│  1. Cloud API - Higher accuracy, pay per request     │
│  2. Edge model (TFLite) - On-device, free inference  │
│  3. Container - Self-hosted                          │
│                                                      │
│  Minimum Data:                                       │
│  • 100 images per class (recommended: 1000+)         │
│  • At least 2 classes                                │
│  • Balanced dataset preferred                        │
│                                                      │
│  Training Time: 1-24 hours (cloud compute)           │
│  Cost: ~$3.15/hour of training                       │
└──────────────────────────────────────────────────────┘
```

### Code — Using AutoML Edge Model on Device

```kotlin
class AutoMLClassifier(private val context: Context) {

    private var classifier: org.tensorflow.lite.task.vision.classifier.ImageClassifier? = null

    // Load exported AutoML Edge model (.tflite)
    fun initialize(modelPath: String = "automl_model.tflite") {
        val options = org.tensorflow.lite.task.vision.classifier.ImageClassifier
            .ImageClassifierOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.3f)
            .setNumThreads(4)
            .build()

        classifier = org.tensorflow.lite.task.vision.classifier.ImageClassifier
            .createFromFileAndOptions(context, modelPath, options)
    }

    fun classify(bitmap: Bitmap): List<ClassificationResult> {
        val tensorImage = org.tensorflow.lite.support.image.TensorImage.fromBitmap(bitmap)
        val results = classifier?.classify(tensorImage) ?: return emptyList()

        return results.flatMap { classification ->
            classification.categories.map { category ->
                ClassificationResult(
                    label = category.label,
                    confidence = category.score
                )
            }
        }.sortedByDescending { it.confidence }
    }

    data class ClassificationResult(
        val label: String,
        val confidence: Float
    )

    fun close() = classifier?.close()
}
```

---

## 3. OCR with High Accuracy (Document AI)

```kotlin
class HighAccuracyOCR(private val visionRepo: CloudVisionRepository) {

    // Multi-language OCR with hints
    suspend fun ocrWithLanguageHints(bitmap: Bitmap, languages: List<String>): String {
        val request = VisionRequest(
            requests = listOf(
                AnnotateImageRequest(
                    image = ImageInput(content = bitmapToBase64(bitmap)),
                    features = listOf(Feature("DOCUMENT_TEXT_DETECTION", 1)),
                    imageContext = ImageContext(languageHints = languages)
                )
            )
        )

        val response = CloudVisionClient().api.annotateImage("API_KEY", request)
        return response.responses.first().fullTextAnnotation?.text ?: ""
    }

    // Extract structured data from receipt
    suspend fun parseReceipt(bitmap: Bitmap): ReceiptData {
        val ocrResult = visionRepo.detectText(bitmap)
        val fullText = ocrResult.fullText

        return ReceiptData(
            storeName = extractStoreName(fullText),
            date = extractDate(fullText),
            items = extractLineItems(fullText),
            total = extractTotal(fullText),
            rawText = fullText
        )
    }

    private fun extractStoreName(text: String): String {
        return text.lines().firstOrNull()?.trim() ?: ""
    }

    private fun extractDate(text: String): String? {
        val dateRegex = Regex("\\d{1,2}/\\d{1,2}/\\d{2,4}|\\d{4}-\\d{2}-\\d{2}")
        return dateRegex.find(text)?.value
    }

    private fun extractLineItems(text: String): List<ReceiptItem> {
        val priceRegex = Regex("(.+?)\\s+\\$?(\\d+\\.\\d{2})")
        return priceRegex.findAll(text).map { match ->
            ReceiptItem(
                name = match.groupValues[1].trim(),
                price = match.groupValues[2].toDoubleOrNull() ?: 0.0
            )
        }.toList()
    }

    private fun extractTotal(text: String): Double? {
        val totalRegex = Regex("(?i)total[:\\s]+\\$?(\\d+\\.\\d{2})")
        return totalRegex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
    }

    data class ReceiptData(
        val storeName: String,
        val date: String?,
        val items: List<ReceiptItem>,
        val total: Double?,
        val rawText: String
    )

    data class ReceiptItem(val name: String, val price: Double)
}
```

---

## 4. Safe Search Detection

```kotlin
class ContentModerationHelper(private val visionRepo: CloudVisionRepository) {

    // Check if image is safe to display
    suspend fun isImageSafe(bitmap: Bitmap): ContentSafetyResult {
        val safeSearch = visionRepo.safeSearchDetection(bitmap)

        val isSafe = safeSearch.isSafe
        val reasons = mutableListOf<String>()

        if (safeSearch.adult in setOf("LIKELY", "VERY_LIKELY"))
            reasons.add("Adult content")
        if (safeSearch.violence in setOf("LIKELY", "VERY_LIKELY"))
            reasons.add("Violence")
        if (safeSearch.racy in setOf("LIKELY", "VERY_LIKELY"))
            reasons.add("Racy content")

        return ContentSafetyResult(
            isSafe = isSafe,
            reasons = reasons,
            details = safeSearch
        )
    }

    data class ContentSafetyResult(
        val isSafe: Boolean,
        val reasons: List<String>,
        val details: CloudVisionRepository.SafeSearchResult
    )

    // Use in upload flow
    suspend fun validateUpload(bitmap: Bitmap): Boolean {
        val safety = isImageSafe(bitmap)
        if (!safety.isSafe) {
            Log.w("Moderation", "Image rejected: ${safety.reasons.joinToString()}")
            return false
        }
        return true
    }
}
```

---

## Complete ViewModel

```kotlin
class VisionAnalysisViewModel : ViewModel() {

    // Use backend proxy in production
    private val visionRepo = CloudVisionRepository(BuildConfig.VISION_API_KEY)

    sealed class VisionState {
        object Idle : VisionState()
        object Loading : VisionState()
        data class Success(val result: CloudVisionRepository.FullAnalysisResult) : VisionState()
        data class Error(val message: String) : VisionState()
    }

    private val _state = MutableStateFlow<VisionState>(VisionState.Idle)
    val state: StateFlow<VisionState> = _state.asStateFlow()

    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.value = VisionState.Loading
            try {
                val result = visionRepo.fullAnalysis(bitmap)

                // Check safety first
                if (result.safeSearch?.isSafe == false) {
                    _state.value = VisionState.Error("Image flagged by content safety")
                    return@launch
                }

                _state.value = VisionState.Success(result)

                // Log summary
                Log.d("Vision", """
                    Labels: ${result.labels?.take(3)?.joinToString { it.label }}
                    Objects: ${result.objects?.take(3)?.joinToString { it.name }}
                    Faces: ${result.faces}
                    Text: ${result.text?.take(50)}...
                    Logos: ${result.logos?.joinToString()}
                    Landmarks: ${result.landmarks?.joinToString { it.name }}
                """.trimIndent())
            } catch (e: Exception) {
                _state.value = VisionState.Error(e.message ?: "Analysis failed")
            }
        }
    }

    // Individual feature analysis
    fun detectLabelsOnly(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.value = VisionState.Loading
            try {
                val labels = visionRepo.detectLabels(bitmap)
                labels.forEach { Log.d("Label", "${it.label}: ${(it.confidence * 100).toInt()}%") }
            } catch (e: Exception) {
                _state.value = VisionState.Error(e.message ?: "Failed")
            }
        }
    }
}
```
