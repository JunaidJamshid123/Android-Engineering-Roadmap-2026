# Audio Analysis — Sound Classification & Feature Extraction

## Overview

Audio analysis on Android uses TensorFlow Lite models like YamNet for classifying environmental sounds, detecting audio events, and extracting features from audio signals.

```
Audio Analysis Pipeline:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Microphone / Audio File                                     │
│       │                                                      │
│       ▼                                                      │
│  ┌────────────────────────────────────────────┐              │
│  │ Audio Preprocessing                        │              │
│  │  • Resample to 16kHz mono                  │              │
│  │  • Windowing (Hann window)                 │              │
│  │  • Short-Time Fourier Transform (STFT)     │              │
│  │  • Mel Spectrogram (128 mel bins)          │              │
│  │  • Log scaling                             │              │
│  └─────────────────────┬──────────────────────┘              │
│                        │                                     │
│    ┌───────────────────┼────────────────────┐               │
│    ▼                   ▼                    ▼               │
│ ┌────────────┐  ┌──────────────┐  ┌───────────────┐        │
│ │ YamNet     │  │ Custom Model │  │ Feature       │        │
│ │ (521 cls)  │  │ (Genre etc.) │  │ Extraction    │        │
│ │            │  │              │  │               │        │
│ │ Dog bark   │  │ Rock         │  │ • MFCC        │        │
│ │ Siren      │  │ Jazz         │  │ • Chroma      │        │
│ │ Speech     │  │ Classical    │  │ • Spectral    │        │
│ │ Music      │  │ Hip-Hop      │  │ • Tempo       │        │
│ │ Glass      │  │ Electronic   │  │ • Zero-cross  │        │
│ └────────────┘  └──────────────┘  └───────────────┘        │
│                                                              │
│  YamNet Architecture:                                        │
│  ┌──────────────────────────────────────────────┐           │
│  │                                              │           │
│  │  Waveform (0.975s @ 16kHz = 15600 samples)  │           │
│  │       │                                      │           │
│  │       ▼                                      │           │
│  │  Mel Spectrogram (96 frames × 64 mel bins)   │           │
│  │       │                                      │           │
│  │       ▼                                      │           │
│  │  MobileNet v1 Backbone                       │           │
│  │       │                                      │           │
│  │       ▼                                      │           │
│  │  Global Average Pooling                      │           │
│  │       │                                      │           │
│  │       ▼                                      │           │
│  │  Dense → 521 AudioSet classes                │           │
│  │                                              │           │
│  │  Model size: ~14 MB                          │           │
│  │  Inference: ~15ms on modern phones           │           │
│  └──────────────────────────────────────────────┘           │
│                                                              │
│  AudioSet Top Categories:                                    │
│  ┌─────────────────────────────────────────────┐            │
│  │ Human sounds:  Speech, Singing, Laughter    │            │
│  │ Animal:        Dog, Cat, Bird               │            │
│  │ Music:         Guitar, Piano, Drum          │            │
│  │ Environment:   Wind, Rain, Thunder          │            │
│  │ Vehicle:       Car, Truck, Train            │            │
│  │ Domestic:      Door, Alarm, Glass break     │            │
│  │ Tools:         Drill, Hammer, Saw           │            │
│  └─────────────────────────────────────────────┘            │
└──────────────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // TFLite runtime
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-task-audio:0.4.4")

    // Audio recording
    implementation("androidx.core:core-ktx:1.13.1")
}
```

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

---

## 1. YamNet for Audio Classification

### Using TFLite Task Library

```kotlin
import org.tensorflow.lite.task.audio.classifier.AudioClassifier

class YamNetClassifier(private val context: Context) {

    private var classifier: AudioClassifier? = null
    private var audioRecord: android.media.AudioRecord? = null

    // Initialize YamNet model
    fun initialize() {
        val options = AudioClassifier.AudioClassifierOptions.builder()
            .setMaxResults(10)
            .setScoreThreshold(0.3f)
            .build()

        // Download yamnet.tflite from TF Hub and place in assets
        classifier = AudioClassifier.createFromFileAndOptions(
            context, "yamnet.tflite", options
        )
    }

    // Create audio recorder matching model's expected format
    fun createAudioRecord(): android.media.AudioRecord {
        val record = classifier!!.createAudioRecord()
        this.audioRecord = record
        return record
    }

    // Classify single audio buffer
    fun classify(): List<AudioClassificationResult> {
        val tensorAudio = classifier!!.createInputTensorAudio()

        // Load from AudioRecord
        tensorAudio.load(audioRecord!!)

        val results = classifier!!.classify(tensorAudio)

        return results.flatMap { classification ->
            classification.categories.map { category ->
                AudioClassificationResult(
                    label = category.label,
                    displayName = category.displayName ?: category.label,
                    score = category.score,
                    index = category.index
                )
            }
        }.sortedByDescending { it.score }
    }

    data class AudioClassificationResult(
        val label: String,
        val displayName: String,
        val score: Float,
        val index: Int
    )

    fun close() {
        classifier?.close()
        audioRecord?.release()
    }
}
```

### Real-Time Audio Classification

```kotlin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class RealtimeAudioClassifier(private val context: Context) {

    private val yamnet = YamNetClassifier(context)
    private var executor: ScheduledThreadPoolExecutor? = null

    private val _classifications = MutableStateFlow<List<YamNetClassifier.AudioClassificationResult>>(emptyList())
    val classifications: StateFlow<List<YamNetClassifier.AudioClassificationResult>> = _classifications

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    fun start() {
        yamnet.initialize()
        val audioRecord = yamnet.createAudioRecord()
        audioRecord.startRecording()
        _isRecording.value = true

        // Classify every 500ms
        executor = ScheduledThreadPoolExecutor(1)
        executor?.scheduleAtFixedRate({
            try {
                val results = yamnet.classify()
                _classifications.value = results.take(5)
            } catch (e: Exception) {
                android.util.Log.e("AudioClassifier", "Classification error", e)
            }
        }, 0, 500, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        _isRecording.value = false
        executor?.shutdownNow()
        executor = null
        yamnet.close()
    }
}
```

---

## 2. Sound Event Detection

```
Sound Event Detection vs Classification:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Classification:  "What sound is this?"              │
│  ┌──────────────┐                                   │
│  │ Audio Clip   │ ──▶ "Dog bark" (0.92)            │
│  └──────────────┘                                   │
│                                                      │
│  Event Detection: "When does each sound occur?"      │
│  ┌──────────────────────────────────────────┐       │
│  │ Time ──────────────────────────▶         │       │
│  │ ┌───┐   ┌────┐  ┌─┐  ┌──────┐          │       │
│  │ │Dog│   │Car │  │!│  │Music │          │       │
│  │ └───┘   └────┘  └─┘  └──────┘          │       │
│  │ 0-2s   3-5s   6s   7-12s               │       │
│  └──────────────────────────────────────────┘       │
│                                                      │
│  Use Cases:                                          │
│  • Baby cry detection (baby monitor)                │
│  • Glass break detection (security)                 │
│  • Doorbell / knock detection (hearing aid)         │
│  • Gunshot detection (safety)                       │
│  • Cough detection (health)                         │
└──────────────────────────────────────────────────────┘
```

```kotlin
class SoundEventDetector(private val context: Context) {

    private val classifier = RealtimeAudioClassifier(context)

    // Define events of interest with their YamNet labels
    data class SoundEvent(
        val name: String,
        val yamnetLabels: Set<String>,
        val threshold: Float = 0.5f
    )

    private val monitoredEvents = listOf(
        SoundEvent("Baby Crying", setOf("Baby cry", "Infant cry", "Crying"), 0.6f),
        SoundEvent("Dog Barking", setOf("Dog", "Bark", "Bow-wow"), 0.5f),
        SoundEvent("Doorbell", setOf("Doorbell", "Ding-dong", "Buzzer"), 0.5f),
        SoundEvent("Glass Breaking", setOf("Glass", "Shatter", "Breaking"), 0.4f),
        SoundEvent("Smoke Alarm", setOf("Smoke detector", "Fire alarm", "Alarm"), 0.5f),
        SoundEvent("Siren", setOf("Siren", "Emergency vehicle", "Ambulance"), 0.5f),
        SoundEvent("Speech", setOf("Speech", "Narration", "Conversation"), 0.6f),
        SoundEvent("Music", setOf("Music", "Musical instrument", "Song"), 0.5f)
    )

    private val _detectedEvents = MutableStateFlow<List<DetectedEvent>>(emptyList())
    val detectedEvents: StateFlow<List<DetectedEvent>> = _detectedEvents

    private val eventHistory = mutableListOf<DetectedEvent>()
    private var eventCallback: ((DetectedEvent) -> Unit)? = null

    data class DetectedEvent(
        val event: SoundEvent,
        val confidence: Float,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun startMonitoring(onEvent: ((DetectedEvent) -> Unit)? = null) {
        this.eventCallback = onEvent
        classifier.start()

        // Observe classifications and match to events
        kotlinx.coroutines.GlobalScope.launch {
            classifier.classifications.collect { results ->
                val detected = mutableListOf<DetectedEvent>()

                for (event in monitoredEvents) {
                    val matchingResult = results.find { result ->
                        event.yamnetLabels.any { label ->
                            result.label.contains(label, ignoreCase = true) ||
                            result.displayName.contains(label, ignoreCase = true)
                        }
                    }

                    if (matchingResult != null && matchingResult.score >= event.threshold) {
                        val detectedEvent = DetectedEvent(event, matchingResult.score)
                        detected.add(detectedEvent)
                        eventHistory.add(detectedEvent)
                        eventCallback?.invoke(detectedEvent)
                    }
                }

                _detectedEvents.value = detected
            }
        }
    }

    fun stopMonitoring() {
        classifier.stop()
    }

    fun getEventHistory(): List<DetectedEvent> = eventHistory.toList()
}
```

---

## 3. Music Genre Classification

```
Custom Genre Classifier:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Training Pipeline (Python):                        │
│                                                      │
│  GTZAN Dataset (10 genres × 100 clips × 30s)       │
│       │                                              │
│       ▼                                              │
│  Extract Mel Spectrograms (librosa)                 │
│       │                                              │
│       ▼                                              │
│  Train CNN (TensorFlow/Keras)                       │
│       │                                              │
│       ▼                                              │
│  Convert to TFLite                                  │
│       │                                              │
│       ▼                                              │
│  Deploy on Android                                   │
│                                                      │
│  Genres:                                             │
│  ┌──────────┬──────────┬──────────┐                 │
│  │ Blues     │ Country  │ Disco    │                 │
│  │ Hip-Hop  │ Jazz     │ Metal    │                 │
│  │ Pop      │ Reggae   │ Rock     │                 │
│  │ Classical│          │          │                 │
│  └──────────┴──────────┴──────────┘                 │
└──────────────────────────────────────────────────────┘
```

### Python Training

```python
import tensorflow as tf
import librosa
import numpy as np
import os

# 1. Extract Mel Spectrogram features
def extract_mel_spectrogram(audio_path, sr=22050, n_mels=128, duration=3.0):
    y, sr = librosa.load(audio_path, sr=sr, duration=duration)
    
    # Pad if shorter than duration
    target_length = int(sr * duration)
    if len(y) < target_length:
        y = np.pad(y, (0, target_length - len(y)))
    else:
        y = y[:target_length]
    
    mel = librosa.feature.melspectrogram(y=y, sr=sr, n_mels=n_mels)
    mel_db = librosa.power_to_db(mel, ref=np.max)
    return mel_db

# 2. Build dataset
genres = ['blues', 'classical', 'country', 'disco', 'hiphop',
          'jazz', 'metal', 'pop', 'reggae', 'rock']

X, y = [], []
data_path = "genres/"
for genre_idx, genre in enumerate(genres):
    genre_path = os.path.join(data_path, genre)
    for filename in os.listdir(genre_path):
        if filename.endswith('.wav'):
            filepath = os.path.join(genre_path, filename)
            # Extract multiple 3-second segments per file
            audio, sr = librosa.load(filepath, sr=22050)
            for start in range(0, len(audio) - 22050*3, 22050*3):
                segment = audio[start:start + 22050*3]
                mel = librosa.feature.melspectrogram(y=segment, sr=sr, n_mels=128)
                mel_db = librosa.power_to_db(mel, ref=np.max)
                X.append(mel_db)
                y.append(genre_idx)

X = np.array(X)[..., np.newaxis]  # Add channel dim
y = tf.keras.utils.to_categorical(np.array(y), num_classes=10)

# 3. Build model
model = tf.keras.Sequential([
    tf.keras.layers.Input(shape=(128, 130, 1)),
    tf.keras.layers.Conv2D(32, (3, 3), activation='relu'),
    tf.keras.layers.BatchNormalization(),
    tf.keras.layers.MaxPooling2D((2, 2)),
    tf.keras.layers.Conv2D(64, (3, 3), activation='relu'),
    tf.keras.layers.BatchNormalization(),
    tf.keras.layers.MaxPooling2D((2, 2)),
    tf.keras.layers.Conv2D(128, (3, 3), activation='relu'),
    tf.keras.layers.BatchNormalization(),
    tf.keras.layers.MaxPooling2D((2, 2)),
    tf.keras.layers.GlobalAveragePooling2D(),
    tf.keras.layers.Dense(128, activation='relu'),
    tf.keras.layers.Dropout(0.3),
    tf.keras.layers.Dense(10, activation='softmax')
])

model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
model.fit(X, y, epochs=50, batch_size=32, validation_split=0.2)

# 4. Convert to TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

with open('genre_classifier.tflite', 'wb') as f:
    f.write(tflite_model)
```

### Android Inference

```kotlin
class MusicGenreClassifier(private val context: Context) {

    private var interpreter: org.tensorflow.lite.Interpreter? = null
    private val genres = listOf(
        "Blues", "Classical", "Country", "Disco", "Hip-Hop",
        "Jazz", "Metal", "Pop", "Reggae", "Rock"
    )

    fun initialize() {
        val model = loadModelFile("genre_classifier.tflite")
        interpreter = org.tensorflow.lite.Interpreter(model)
    }

    private fun loadModelFile(modelName: String): java.nio.MappedByteBuffer {
        val fd = context.assets.openFd(modelName)
        val inputStream = java.io.FileInputStream(fd.fileDescriptor)
        val channel = inputStream.channel
        return channel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fd.startOffset, fd.declaredLength
        )
    }

    // Extract mel spectrogram from raw audio PCM data
    private fun extractMelSpectrogram(audioData: ShortArray, sampleRate: Int = 22050): Array<Array<FloatArray>> {
        val nMels = 128
        val nFft = 2048
        val hopLength = 512
        val numFrames = 130

        // Convert to float
        val floatData = FloatArray(audioData.size) { audioData[it] / 32768f }

        // Simple mel spectrogram extraction
        // In production, use a proper DSP library
        val melSpec = Array(nMels) { FloatArray(numFrames) { 0f } }

        // ... FFT + mel filterbank computation ...
        // For production, use libraries like TarsosDSP or JLibrosa

        return arrayOf(melSpec.map { row ->
            arrayOf(row).first()  // Shape: [128, 130, 1]
        }.toTypedArray())
    }

    fun classify(audioData: ShortArray): List<GenreResult> {
        val input = extractMelSpectrogram(audioData)
        val output = Array(1) { FloatArray(genres.size) }

        interpreter?.run(input, output)

        return output[0].mapIndexed { index, score ->
            GenreResult(genres[index], score)
        }.sortedByDescending { it.confidence }
    }

    data class GenreResult(val genre: String, val confidence: Float)

    fun close() {
        interpreter?.close()
    }
}
```

---

## 4. Audio Feature Extraction

```
Audio Features:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Time Domain Features:                                       │
│  ┌────────────────────────────────────────────┐              │
│  │ • Zero Crossing Rate (ZCR)                 │              │
│  │   Signal sign changes per frame            │              │
│  │   High ZCR → noisy/percussive              │              │
│  │                                            │              │
│  │ • RMS Energy                               │              │
│  │   Root mean square amplitude               │              │
│  │   Loudness measure                         │              │
│  │                                            │              │
│  │ • Tempo / BPM                              │              │
│  │   Beats per minute from onset detection    │              │
│  └────────────────────────────────────────────┘              │
│                                                              │
│  Frequency Domain Features:                                  │
│  ┌────────────────────────────────────────────┐              │
│  │ • MFCC (Mel-Frequency Cepstral Coefficients)│             │
│  │   13-40 coefficients capturing timbre       │              │
│  │   Most important for speech/audio ML       │              │
│  │                                            │              │
│  │ • Spectral Centroid                        │              │
│  │   "Center of mass" of spectrum             │              │
│  │   High → bright sound                      │              │
│  │                                            │              │
│  │ • Spectral Rolloff                         │              │
│  │   Frequency below which 85% energy lies    │              │
│  │                                            │              │
│  │ • Chroma Features                          │              │
│  │   12 pitch classes (C, C#, D, ..., B)      │              │
│  │   Music harmony / chord detection          │              │
│  │                                            │              │
│  │ • Mel Spectrogram                          │              │
│  │   Human-perception-scaled frequency repr.  │              │
│  └────────────────────────────────────────────┘              │
│                                                              │
│  Feature Pipeline:                                           │
│  Audio → Window → FFT → |FFT|² → Mel Bank → Log → DCT=MFCC │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
import kotlin.math.*

class AudioFeatureExtractor {

    // Zero Crossing Rate
    fun zeroCrossingRate(signal: FloatArray): Float {
        var crossings = 0
        for (i in 1 until signal.size) {
            if ((signal[i] >= 0 && signal[i - 1] < 0) ||
                (signal[i] < 0 && signal[i - 1] >= 0)) {
                crossings++
            }
        }
        return crossings.toFloat() / signal.size
    }

    // RMS Energy
    fun rmsEnergy(signal: FloatArray): Float {
        val sum = signal.fold(0.0) { acc, value -> acc + value * value }
        return sqrt(sum / signal.size).toFloat()
    }

    // Spectral Centroid (simplified)
    fun spectralCentroid(magnitudes: FloatArray, sampleRate: Int): Float {
        val frequencies = FloatArray(magnitudes.size) { it * sampleRate.toFloat() / (2 * magnitudes.size) }
        val weightedSum = magnitudes.zip(frequencies.toList()) { m, f -> m * f }.sum()
        val totalMagnitude = magnitudes.sum()
        return if (totalMagnitude > 0) weightedSum / totalMagnitude else 0f
    }

    // Simple FFT magnitude
    fun fftMagnitude(signal: FloatArray): FloatArray {
        val n = signal.size
        val magnitudes = FloatArray(n / 2)

        for (k in 0 until n / 2) {
            var real = 0.0
            var imag = 0.0
            for (t in 0 until n) {
                val angle = 2.0 * PI * k * t / n
                real += signal[t] * cos(angle)
                imag -= signal[t] * sin(angle)
            }
            magnitudes[k] = sqrt(real * real + imag * imag).toFloat()
        }

        return magnitudes
    }

    // Extract all features from audio segment
    fun extractFeatures(
        audioData: ShortArray,
        sampleRate: Int,
        frameSize: Int = 2048,
        hopSize: Int = 512
    ): AudioFeatures {
        val signal = FloatArray(audioData.size) { audioData[it] / 32768f }
        val numFrames = (signal.size - frameSize) / hopSize + 1

        val zcrValues = mutableListOf<Float>()
        val rmsValues = mutableListOf<Float>()
        val centroidValues = mutableListOf<Float>()

        for (i in 0 until numFrames) {
            val start = i * hopSize
            val frame = signal.sliceArray(start until minOf(start + frameSize, signal.size))

            // Apply Hann window
            val windowed = FloatArray(frame.size) { idx ->
                frame[idx] * (0.5f - 0.5f * cos(2.0 * PI * idx / frame.size).toFloat())
            }

            zcrValues.add(zeroCrossingRate(windowed))
            rmsValues.add(rmsEnergy(windowed))

            val magnitudes = fftMagnitude(windowed)
            centroidValues.add(spectralCentroid(magnitudes, sampleRate))
        }

        return AudioFeatures(
            zeroCrossingRate = zcrValues.average().toFloat(),
            rmsEnergy = rmsValues.average().toFloat(),
            spectralCentroid = centroidValues.average().toFloat(),
            duration = audioData.size.toFloat() / sampleRate,
            sampleRate = sampleRate
        )
    }

    data class AudioFeatures(
        val zeroCrossingRate: Float,
        val rmsEnergy: Float,
        val spectralCentroid: Float,
        val duration: Float,
        val sampleRate: Int
    ) {
        // Simple heuristic categorization
        val isSpeech: Boolean get() = zeroCrossingRate in 0.02f..0.15f
        val isMusic: Boolean get() = rmsEnergy > 0.05f && zeroCrossingRate < 0.1f
        val isSilence: Boolean get() = rmsEnergy < 0.01f
        val isNoise: Boolean get() = zeroCrossingRate > 0.2f && rmsEnergy > 0.02f
    }
}
```

---

## Complete ViewModel

```kotlin
class AudioAnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val audioClassifier = RealtimeAudioClassifier(application)
    private val eventDetector = SoundEventDetector(application)
    private val featureExtractor = AudioFeatureExtractor()

    val classifications = audioClassifier.classifications
    val detectedEvents = eventDetector.detectedEvents
    val isRecording = audioClassifier.isRecording

    fun startClassification() = audioClassifier.start()
    fun stopClassification() = audioClassifier.stop()

    fun startEventDetection(onEvent: (SoundEventDetector.DetectedEvent) -> Unit) {
        eventDetector.startMonitoring(onEvent)
    }

    fun stopEventDetection() = eventDetector.stopMonitoring()

    fun analyzeAudioBuffer(audioData: ShortArray, sampleRate: Int): AudioFeatureExtractor.AudioFeatures {
        return featureExtractor.extractFeatures(audioData, sampleRate)
    }

    override fun onCleared() {
        super.onCleared()
        audioClassifier.stop()
        eventDetector.stopMonitoring()
    }
}
```
