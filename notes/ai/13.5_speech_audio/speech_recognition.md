# Speech Recognition — Speech-to-Text AI

## Overview

Speech recognition converts spoken language into text. Android supports both cloud-based (Google Cloud Speech-to-Text) and on-device (ML Kit, Android SpeechRecognizer) approaches.

```
Speech Recognition Architecture:
┌──────────────────────────────────────────────────────────────┐
│                    Audio Input Pipeline                       │
│                                                              │
│  Microphone → AudioRecord/MediaRecorder → PCM/WAV Buffer    │
│       │                                                      │
│       ▼                                                      │
│  ┌─────────────────────────────────────────────────────┐     │
│  │            Preprocessing                             │     │
│  │  • Noise suppression (AGC, NS)                      │     │
│  │  • Voice Activity Detection (VAD)                    │     │
│  │  • Feature extraction (MFCC / Mel Spectrogram)      │     │
│  └────────────────────┬────────────────────────────────┘     │
│                       │                                      │
│         ┌─────────────┴─────────────┐                       │
│         ▼                           ▼                       │
│  ┌──────────────┐           ┌──────────────────┐            │
│  │  On-Device   │           │  Cloud API        │            │
│  │  (ML Kit /   │           │  (Google Cloud    │            │
│  │  SpeechRec)  │           │   Speech-to-Text) │            │
│  │              │           │                    │            │
│  │ • Offline ✅ │           │ • 125+ languages   │            │
│  │ • Fast ~50ms │           │ • Custom vocab     │            │
│  │ • Free       │           │ • Speaker diarize  │            │
│  │ • ~20 langs  │           │ • Word timestamps  │            │
│  └──────┬───────┘           └────────┬───────────┘            │
│         │                            │                       │
│         └────────────┬───────────────┘                       │
│                      ▼                                       │
│  ┌─────────────────────────────────────────────────────┐     │
│  │              Recognized Text Output                  │     │
│  │  • Partial results (streaming)                      │     │
│  │  • Final transcript                                  │     │
│  │  • Confidence scores                                 │     │
│  │  • Word-level timestamps                            │     │
│  │  • Alternative transcripts                          │     │
│  └─────────────────────────────────────────────────────┘     │
│                                                              │
│  Comparison:                                                 │
│  ┌────────────────┬─────────────┬────────────────────┐      │
│  │                │ On-Device   │ Cloud STT          │      │
│  ├────────────────┼─────────────┼────────────────────┤      │
│  │ Accuracy       │ ██████████  │ █████████████████  │      │
│  │ Languages      │ ~20         │ 125+               │      │
│  │ Offline        │ ✅          │ ❌                 │      │
│  │ Latency        │ 50-200ms    │ 200-1500ms         │      │
│  │ Custom vocab   │ Limited     │ Full support       │      │
│  │ Long audio     │ ❌          │ Up to 8 hours      │      │
│  │ Cost           │ Free        │ $0.006-0.009/15s   │      │
│  │ Diarization    │ ❌          │ ✅                 │      │
│  └────────────────┴─────────────┴────────────────────┘      │
└──────────────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Android built-in SpeechRecognizer (no extra dependency)

    // ML Kit Speech (not yet GA — use SpeechRecognizer as primary)

    // Google Cloud Speech-to-Text (REST)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Audio recording helper
    implementation("androidx.core:core-ktx:1.13.1")
}
```

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 1. Android SpeechRecognizer (On-Device)

### How It Works

```
SpeechRecognizer Flow:
┌─────────┐    ┌────────────────┐    ┌────────────────┐
│ Mic     │───▶│ SpeechRecognizer│───▶│ RecognitionLsnr│
│ Input   │    │ (System Service)│    │ (Callbacks)    │
└─────────┘    └────────────────┘    └────────┬───────┘
                                              │
                    ┌─────────────────────────┘
                    ▼
    ┌───────────────────────────────────┐
    │ Callbacks:                        │
    │  onReadyForSpeech()              │
    │  onBeginningOfSpeech()           │
    │  onRmsChanged(rmsdB)             │
    │  onPartialResults(Bundle)        │
    │  onResults(Bundle)               │
    │  onError(errorCode)              │
    │  onEndOfSpeech()                 │
    └───────────────────────────────────┘
```

### Code

```kotlin
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var listener: SpeechResultListener? = null

    interface SpeechResultListener {
        fun onPartialResult(text: String)
        fun onFinalResult(text: String, confidence: Float)
        fun onError(errorMessage: String)
        fun onListeningStateChanged(isListening: Boolean)
        fun onVolumeChanged(rmsdB: Float)
    }

    fun initialize(resultListener: SpeechResultListener) {
        this.listener = resultListener

        // Check if on-device recognition is available
        val isAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        if (!isAvailable) {
            resultListener.onError("Speech recognition not available")
            return
        }

        // Use on-device recognizer (Android 12+)
        speechRecognizer = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        speechRecognizer?.setRecognitionListener(createListener())
    }

    fun startListening(
        language: String = "en-US",
        continuous: Boolean = false,
        partialResults: Boolean = true
    ) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, partialResults)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            if (continuous) {
                // Keep listening until manually stopped
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 60000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
            }
        }

        listener?.onListeningStateChanged(true)
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        listener?.onListeningStateChanged(false)
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            listener?.onListeningStateChanged(true)
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {
            listener?.onVolumeChanged(rmsdB)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            listener?.onListeningStateChanged(false)
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                else -> "Unknown error: $error"
            }
            listener?.onError(message)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

            if (!matches.isNullOrEmpty()) {
                listener?.onFinalResult(
                    matches[0],
                    confidences?.firstOrNull() ?: 0f
                )
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                listener?.onPartialResult(matches[0])
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
```

---

## 2. Google Cloud Speech-to-Text API

### Data Models

```kotlin
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Request ---
@JsonClass(generateAdapter = true)
data class SpeechRecognizeRequest(
    val config: RecognitionConfig,
    val audio: RecognitionAudio
)

@JsonClass(generateAdapter = true)
data class RecognitionConfig(
    val encoding: String = "LINEAR16",          // LINEAR16, FLAC, MP3, OGG_OPUS
    val sampleRateHertz: Int = 16000,
    val languageCode: String = "en-US",
    val alternativeLanguageCodes: List<String>? = null,
    val maxAlternatives: Int = 1,
    val enableAutomaticPunctuation: Boolean = true,
    val enableWordTimeOffsets: Boolean = false,
    val enableWordConfidence: Boolean = false,
    val model: String = "latest_long",          // latest_long, latest_short, phone_call, video
    val useEnhanced: Boolean = true,
    val speechContexts: List<SpeechContext>? = null,
    val diarizationConfig: DiarizationConfig? = null
)

@JsonClass(generateAdapter = true)
data class SpeechContext(
    val phrases: List<String>,  // Custom vocabulary
    val boost: Float = 10f      // 0-20, how much to boost
)

@JsonClass(generateAdapter = true)
data class DiarizationConfig(
    val enableSpeakerDiarization: Boolean = true,
    val minSpeakerCount: Int = 2,
    val maxSpeakerCount: Int = 6
)

@JsonClass(generateAdapter = true)
data class RecognitionAudio(
    val content: String? = null,      // Base64-encoded audio
    val uri: String? = null           // gs://bucket/audio.wav
)

// --- Response ---
@JsonClass(generateAdapter = true)
data class SpeechRecognizeResponse(
    val results: List<SpeechRecognitionResult>?,
    val totalBilledTime: String?
)

@JsonClass(generateAdapter = true)
data class SpeechRecognitionResult(
    val alternatives: List<SpeechRecognitionAlternative>,
    val channelTag: Int? = null,
    val languageCode: String? = null
)

@JsonClass(generateAdapter = true)
data class SpeechRecognitionAlternative(
    val transcript: String,
    val confidence: Float? = null,
    val words: List<WordInfo>? = null
)

@JsonClass(generateAdapter = true)
data class WordInfo(
    val startTime: String?,
    val endTime: String?,
    val word: String,
    val confidence: Float?,
    val speakerTag: Int?
)
```

### API Interface

```kotlin
interface CloudSpeechApi {
    @POST("v1/speech:recognize")
    suspend fun recognize(
        @Query("key") apiKey: String,
        @Body request: SpeechRecognizeRequest
    ): SpeechRecognizeResponse

    // Streaming (long audio) via long-running operation
    @POST("v1/speech:longrunningrecognize")
    suspend fun longRunningRecognize(
        @Query("key") apiKey: String,
        @Body request: SpeechRecognizeRequest
    ): LongRunningOperation

    @GET("v1/operations/{name}")
    suspend fun getOperation(
        @Path("name") name: String,
        @Query("key") apiKey: String
    ): LongRunningOperation
}

@JsonClass(generateAdapter = true)
data class LongRunningOperation(
    val name: String,
    val done: Boolean? = null,
    val response: SpeechRecognizeResponse? = null,
    val error: StatusError? = null
)

@JsonClass(generateAdapter = true)
data class StatusError(val code: Int?, val message: String?)
```

### Repository

```kotlin
class CloudSpeechRepository(private val apiKey: String) {

    private val api: CloudSpeechApi = Retrofit.Builder()
        .baseUrl("https://speech.googleapis.com/")
        .addConverterFactory(MoshiConverterFactory.create(
            Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
        ))
        .build()
        .create(CloudSpeechApi::class.java)

    // Short audio (< 1 minute, < 10 MB)
    suspend fun recognizeShort(
        audioBytes: ByteArray,
        languageCode: String = "en-US",
        customVocab: List<String>? = null
    ): TranscriptionResult {
        val base64Audio = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)

        val request = SpeechRecognizeRequest(
            config = RecognitionConfig(
                languageCode = languageCode,
                enableAutomaticPunctuation = true,
                enableWordTimeOffsets = true,
                enableWordConfidence = true,
                speechContexts = customVocab?.let {
                    listOf(SpeechContext(phrases = it, boost = 15f))
                }
            ),
            audio = RecognitionAudio(content = base64Audio)
        )

        val response = api.recognize(apiKey, request)
        return parseResponse(response)
    }

    // Long audio (up to 8 hours)
    suspend fun recognizeLong(
        gcsUri: String,
        languageCode: String = "en-US",
        enableDiarization: Boolean = false
    ): TranscriptionResult {
        val request = SpeechRecognizeRequest(
            config = RecognitionConfig(
                languageCode = languageCode,
                enableAutomaticPunctuation = true,
                enableWordTimeOffsets = true,
                model = "latest_long",
                diarizationConfig = if (enableDiarization) {
                    DiarizationConfig(enableSpeakerDiarization = true)
                } else null
            ),
            audio = RecognitionAudio(uri = gcsUri)
        )

        val operation = api.longRunningRecognize(apiKey, request)

        // Poll for completion
        var result = operation
        while (result.done != true) {
            kotlinx.coroutines.delay(5000)
            result = api.getOperation(result.name, apiKey)
        }

        return result.response?.let { parseResponse(it) }
            ?: TranscriptionResult("", emptyList(), 0f)
    }

    private fun parseResponse(response: SpeechRecognizeResponse): TranscriptionResult {
        val allResults = response.results ?: emptyList()
        val fullTranscript = allResults.joinToString(" ") {
            it.alternatives.firstOrNull()?.transcript ?: ""
        }

        val wordTimestamps = allResults.flatMap {
            it.alternatives.firstOrNull()?.words ?: emptyList()
        }.map {
            TimestampedWord(
                word = it.word,
                startTime = parseTime(it.startTime),
                endTime = parseTime(it.endTime),
                confidence = it.confidence ?: 0f,
                speakerTag = it.speakerTag
            )
        }

        val avgConfidence = allResults
            .mapNotNull { it.alternatives.firstOrNull()?.confidence }
            .average().toFloat()

        return TranscriptionResult(fullTranscript, wordTimestamps, avgConfidence)
    }

    private fun parseTime(time: String?): Float {
        if (time == null) return 0f
        // Format: "1.500s"
        return time.removeSuffix("s").toFloatOrNull() ?: 0f
    }

    data class TranscriptionResult(
        val transcript: String,
        val words: List<TimestampedWord>,
        val confidence: Float
    )

    data class TimestampedWord(
        val word: String,
        val startTime: Float,
        val endTime: Float,
        val confidence: Float,
        val speakerTag: Int?
    )
}
```

---

## 3. Real-Time Transcription

```
Real-Time Transcription Pipeline:
┌─────────┐   ┌────────────┐   ┌──────────────┐   ┌──────────┐
│ AudioRec│──▶│ Buffer     │──▶│ Recognizer   │──▶│ UI       │
│ (16kHz) │   │ (Chunked)  │   │ (Continuous) │   │ (Live)   │
└─────────┘   └────────────┘   └──────────────┘   └──────────┘
                                      │
                                      ▼
                          ┌───────────────────────┐
                          │ Partial Results:       │
                          │ "Hello"                │
                          │ "Hello how"            │
                          │ "Hello how are"        │
                          │ "Hello how are you"  ← Final
                          └───────────────────────┘
```

```kotlin
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RealTimeTranscriber(private val context: Context) {

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val speechManager = SpeechRecognitionManager(context)
    private var fullTranscript = StringBuilder()

    fun start(language: String = "en-US") {
        speechManager.initialize(object : SpeechRecognitionManager.SpeechResultListener {
            override fun onPartialResult(text: String) {
                _partialText.value = text
            }

            override fun onFinalResult(text: String, confidence: Float) {
                fullTranscript.append(text).append(" ")
                _transcription.value = fullTranscript.toString().trim()
                _partialText.value = ""

                // Auto-restart for continuous transcription
                if (_isListening.value) {
                    speechManager.startListening(language, continuous = true)
                }
            }

            override fun onError(errorMessage: String) {
                if (errorMessage.contains("No match") || errorMessage.contains("No speech")) {
                    // Restart on silence
                    if (_isListening.value) {
                        speechManager.startListening(language, continuous = true)
                    }
                }
            }

            override fun onListeningStateChanged(isListening: Boolean) {
                _isListening.value = isListening
            }

            override fun onVolumeChanged(rmsdB: Float) {}
        })

        _isListening.value = true
        speechManager.startListening(language, continuous = true)
    }

    fun stop() {
        _isListening.value = false
        speechManager.stopListening()
    }

    fun clear() {
        fullTranscript.clear()
        _transcription.value = ""
        _partialText.value = ""
    }

    fun destroy() {
        stop()
        speechManager.destroy()
    }
}
```

---

## 4. Custom Vocabulary and Models

```
Custom Vocabulary:
┌──────────────────────────────────────────────────┐
│  SpeechContext Boosting                          │
│                                                  │
│  Without context:                                │
│    "I need to see the cue"      ← wrong         │
│                                                  │
│  With context ["queue", "Kubernetes", "kubectl"]:│
│    "I need to see the queue"    ← correct        │
│                                                  │
│  Boost values:                                   │
│    0  = no boost                                 │
│    10 = moderate boost                           │
│    20 = maximum boost                            │
│                                                  │
│  Use cases:                                      │
│    • Domain-specific terms (medical, legal)      │
│    • Product/brand names                         │
│    • Uncommon proper nouns                        │
│    • Technical jargon                            │
└──────────────────────────────────────────────────┘
```

```kotlin
class CustomVocabSpeechRecognition(private val apiKey: String) {

    private val repo = CloudSpeechRepository(apiKey)

    // Medical domain vocabulary
    private val medicalVocab = listOf(
        "acetaminophen", "ibuprofen", "metformin",
        "hypertension", "tachycardia", "bradycardia",
        "myocardial infarction", "cerebrovascular",
        "MRI", "CT scan", "ECG", "EKG"
    )

    // Technical domain vocabulary
    private val techVocab = listOf(
        "Kubernetes", "kubectl", "Docker",
        "microservices", "API gateway",
        "GraphQL", "REST", "gRPC",
        "Kotlin", "coroutines", "Jetpack Compose"
    )

    suspend fun recognizeMedical(audioBytes: ByteArray): CloudSpeechRepository.TranscriptionResult {
        return repo.recognizeShort(
            audioBytes = audioBytes,
            customVocab = medicalVocab
        )
    }

    suspend fun recognizeTechnical(audioBytes: ByteArray): CloudSpeechRepository.TranscriptionResult {
        return repo.recognizeShort(
            audioBytes = audioBytes,
            customVocab = techVocab
        )
    }

    // Dynamic vocabulary from user data
    suspend fun recognizeWithUserVocab(
        audioBytes: ByteArray,
        contactNames: List<String>,
        appSpecificTerms: List<String>
    ): CloudSpeechRepository.TranscriptionResult {
        val combined = (contactNames + appSpecificTerms).take(500) // API limit: 500 phrases
        return repo.recognizeShort(audioBytes = audioBytes, customVocab = combined)
    }
}
```

---

## Complete ViewModel

```kotlin
class SpeechViewModel(application: Application) : AndroidViewModel(application) {

    private val transcriber = RealTimeTranscriber(application)

    val transcription = transcriber.transcription
    val partialText = transcriber.partialText
    val isListening = transcriber.isListening

    fun toggleListening() {
        if (isListening.value) {
            transcriber.stop()
        } else {
            transcriber.start("en-US")
        }
    }

    fun clearTranscription() = transcriber.clear()

    override fun onCleared() {
        super.onCleared()
        transcriber.destroy()
    }
}
```
