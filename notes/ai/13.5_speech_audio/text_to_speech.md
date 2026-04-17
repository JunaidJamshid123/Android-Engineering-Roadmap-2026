# Text-to-Speech — Speech Synthesis AI

## Overview

Text-to-Speech (TTS) converts written text into natural-sounding speech. Android provides both built-in TTS and cloud-based neural voice synthesis.

```
TTS Architecture:
┌──────────────────────────────────────────────────────────────┐
│                  Text-to-Speech Pipeline                     │
│                                                              │
│  Input Text → Preprocessing → Synthesis → Audio Output      │
│                                                              │
│  ┌───────────────────────────────────────────────────┐       │
│  │ Preprocessing:                                     │       │
│  │  • Text normalization (numbers, abbreviations)    │       │
│  │  • Sentence segmentation                          │       │
│  │  • SSML parsing (if provided)                     │       │
│  └───────────────────────┬───────────────────────────┘       │
│                          │                                   │
│        ┌─────────────────┴──────────────────┐               │
│        ▼                                    ▼               │
│  ┌──────────────────┐          ┌────────────────────┐       │
│  │ Android TTS      │          │ Cloud TTS           │       │
│  │ (On-Device)      │          │ (Google Cloud)      │       │
│  │                  │          │                      │       │
│  │ • Free           │          │ • Neural voices      │       │
│  │ • Offline ✅     │          │ • 50+ languages     │       │
│  │ • ~30 languages  │          │ • 400+ voices       │       │
│  │ • Robotic quality│          │ • Studio quality    │       │
│  │ • No SSML*       │          │ • Full SSML         │       │
│  │ • System voices  │          │ • Custom voices     │       │
│  └──────────────────┘          └────────────────────┘       │
│                                                              │
│  Voice Quality Comparison:                                   │
│  ┌────────────────────┬────────────┬─────────────────┐      │
│  │                    │ On-Device  │ Cloud Neural     │      │
│  ├────────────────────┼────────────┼─────────────────┤      │
│  │ Naturalness        │ ████       │ █████████████   │      │
│  │ Expressiveness     │ ███        │ ████████████    │      │
│  │ Pronunciation      │ ██████     │ █████████████   │      │
│  │ Latency            │ <50ms      │ 200-1000ms      │      │
│  │ Cost               │ Free       │ $4-16/1M chars  │      │
│  └────────────────────┴────────────┴─────────────────┘      │
└──────────────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Android TTS - included in Android SDK, no extra dependency

    // Cloud TTS (REST)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")

    // For audio playback
    implementation("androidx.media3:media3-exoplayer:1.3.1")
}
```

---

## 1. Android TextToSpeech API

### Core Implementation

```kotlin
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class AndroidTTSManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var listener: TTSListener? = null

    interface TTSListener {
        fun onReady()
        fun onSpeakStart(utteranceId: String)
        fun onSpeakDone(utteranceId: String)
        fun onError(utteranceId: String, errorCode: Int)
    }

    fun initialize(ttsListener: TTSListener) {
        this.listener = ttsListener

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                setupProgressListener()
                ttsListener.onReady()
            } else {
                ttsListener.onError("init", status)
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                utteranceId?.let { listener?.onSpeakStart(it) }
            }

            override fun onDone(utteranceId: String?) {
                utteranceId?.let { listener?.onSpeakDone(it) }
            }

            override fun onError(utteranceId: String?) {
                utteranceId?.let { listener?.onError(it, -1) }
            }
        })
    }

    // Basic speak
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isInitialized) return

        val params = android.os.Bundle()
        val utteranceId = "tts_${System.currentTimeMillis()}"
        tts?.speak(text, queueMode, params, utteranceId)
    }

    // Speak with custom settings
    fun speakWithConfig(
        text: String,
        language: Locale = Locale.US,
        pitch: Float = 1.0f,       // 0.5 = low, 2.0 = high
        speechRate: Float = 1.0f,  // 0.5 = slow, 2.0 = fast
        queueMode: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        if (!isInitialized) return

        tts?.language = language
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speechRate)

        speak(text, queueMode)
    }

    // Queue multiple utterances
    fun speakSequence(texts: List<String>) {
        texts.forEachIndexed { index, text ->
            val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            speak(text, mode)
        }
    }

    // Get available voices
    fun getAvailableVoices(): List<VoiceInfo> {
        return tts?.voices?.map { voice ->
            VoiceInfo(
                name = voice.name,
                locale = voice.locale,
                quality = voice.quality,
                requiresNetwork = voice.isNetworkConnectionRequired,
                features = voice.features?.toList() ?: emptyList()
            )
        } ?: emptyList()
    }

    // Set specific voice
    fun setVoice(voiceName: String): Boolean {
        val voice = tts?.voices?.find { it.name == voiceName }
        return if (voice != null) {
            tts?.voice = voice
            true
        } else false
    }

    // Check language support
    fun isLanguageSupported(locale: Locale): Boolean {
        val result = tts?.isLanguageAvailable(locale)
        return result == TextToSpeech.LANG_AVAILABLE ||
               result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
               result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
    }

    // Save speech to file
    fun synthesizeToFile(text: String, outputFile: java.io.File) {
        if (!isInitialized) return
        val params = android.os.Bundle()
        val utteranceId = "file_${System.currentTimeMillis()}"
        tts?.synthesizeToFile(text, params, outputFile, utteranceId)
    }

    fun stop() = tts?.stop()
    fun isSpeaking() = tts?.isSpeaking ?: false

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    data class VoiceInfo(
        val name: String,
        val locale: Locale,
        val quality: Int,
        val requiresNetwork: Boolean,
        val features: List<String>
    )
}
```

---

## 2. Google Cloud Text-to-Speech with Neural Voices

### Data Models

```kotlin
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Request ---
@JsonClass(generateAdapter = true)
data class CloudTTSRequest(
    val input: SynthesisInput,
    val voice: VoiceSelectionParams,
    val audioConfig: AudioConfig
)

@JsonClass(generateAdapter = true)
data class SynthesisInput(
    val text: String? = null,   // Plain text
    val ssml: String? = null    // SSML markup
)

@JsonClass(generateAdapter = true)
data class VoiceSelectionParams(
    val languageCode: String,         // "en-US"
    val name: String? = null,         // "en-US-Neural2-A"
    val ssmlGender: String? = null    // MALE, FEMALE, NEUTRAL
)

@JsonClass(generateAdapter = true)
data class AudioConfig(
    val audioEncoding: String = "MP3",  // LINEAR16, MP3, OGG_OPUS
    val speakingRate: Double = 1.0,     // 0.25 - 4.0
    val pitch: Double = 0.0,           // -20.0 - 20.0
    val volumeGainDb: Double = 0.0,    // -96.0 - 16.0
    val sampleRateHertz: Int? = null,
    val effectsProfileId: List<String>? = null  // Audio device optimization
)

// --- Response ---
@JsonClass(generateAdapter = true)
data class CloudTTSResponse(
    val audioContent: String  // Base64-encoded audio
)

// --- Voice List ---
@JsonClass(generateAdapter = true)
data class VoiceListResponse(
    val voices: List<CloudVoice>
)

@JsonClass(generateAdapter = true)
data class CloudVoice(
    val languageCodes: List<String>,
    val name: String,
    val ssmlGender: String,
    val naturalSampleRateHertz: Int
)
```

### API & Client

```kotlin
interface CloudTTSApi {
    @POST("v1/text:synthesize")
    suspend fun synthesize(
        @Query("key") apiKey: String,
        @Body request: CloudTTSRequest
    ): CloudTTSResponse

    @GET("v1/voices")
    suspend fun listVoices(
        @Query("key") apiKey: String,
        @Query("languageCode") languageCode: String? = null
    ): VoiceListResponse
}

class CloudTTSRepository(private val apiKey: String) {

    private val api: CloudTTSApi = Retrofit.Builder()
        .baseUrl("https://texttospeech.googleapis.com/")
        .addConverterFactory(MoshiConverterFactory.create(
            Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
        ))
        .build()
        .create(CloudTTSApi::class.java)

    // Synthesize text to audio bytes
    suspend fun synthesize(
        text: String,
        voiceName: String = "en-US-Neural2-F",
        languageCode: String = "en-US",
        speakingRate: Double = 1.0,
        pitch: Double = 0.0
    ): ByteArray {
        val request = CloudTTSRequest(
            input = SynthesisInput(text = text),
            voice = VoiceSelectionParams(
                languageCode = languageCode,
                name = voiceName
            ),
            audioConfig = AudioConfig(
                audioEncoding = "MP3",
                speakingRate = speakingRate,
                pitch = pitch,
                effectsProfileId = listOf("handset-class-device")
            )
        )

        val response = api.synthesize(apiKey, request)
        return android.util.Base64.decode(response.audioContent, android.util.Base64.DEFAULT)
    }

    // Synthesize with SSML
    suspend fun synthesizeSSML(ssml: String, voiceName: String): ByteArray {
        val request = CloudTTSRequest(
            input = SynthesisInput(ssml = ssml),
            voice = VoiceSelectionParams(
                languageCode = voiceName.substringBeforeLast("-").substringBeforeLast("-"),
                name = voiceName
            ),
            audioConfig = AudioConfig(audioEncoding = "MP3")
        )

        val response = api.synthesize(apiKey, request)
        return android.util.Base64.decode(response.audioContent, android.util.Base64.DEFAULT)
    }

    // List available voices
    suspend fun getVoices(languageCode: String? = null): List<CloudVoice> {
        return api.listVoices(apiKey, languageCode).voices
    }
}
```

---

## 3. Custom Voice Selection

```
Voice Types:
┌────────────────────────────────────────────────────────┐
│                                                        │
│  Standard  ─── Concatenative TTS, lower quality       │
│                 "en-US-Standard-A", cheapest            │
│                                                        │
│  WaveNet   ─── DeepMind neural network                 │
│                 "en-US-Wavenet-A", better quality       │
│                                                        │
│  Neural2   ─── Next-gen neural, best quality           │
│                 "en-US-Neural2-A", recommended          │
│                                                        │
│  Studio    ─── Professional studio quality             │
│                 "en-US-Studio-O", premium price         │
│                                                        │
│  Naming Convention:                                    │
│  ┌──────────────────────────────────────────────┐     │
│  │  en-US  -  Neural2  -  F                     │     │
│  │  ─────     ───────     ─                     │     │
│  │  locale    type        variant (A=1st etc.)  │     │
│  └──────────────────────────────────────────────┘     │
│                                                        │
│  Pricing per 1M characters:                           │
│  Standard: $4    WaveNet: $16                         │
│  Neural2: $16    Studio: $160                         │
└────────────────────────────────────────────────────────┘
```

```kotlin
class VoiceSelector(private val ttsRepo: CloudTTSRepository) {

    // Voice presets for different use cases
    enum class VoicePreset(val voiceName: String, val description: String) {
        NARRATOR_FEMALE("en-US-Neural2-F", "Warm female narrator"),
        NARRATOR_MALE("en-US-Neural2-D", "Deep male narrator"),
        ASSISTANT_FEMALE("en-US-Neural2-C", "Friendly female assistant"),
        ASSISTANT_MALE("en-US-Neural2-A", "Clear male assistant"),
        NEWS_ANCHOR("en-US-Neural2-J", "Professional newsreader"),
        CHILD_STORY("en-US-Neural2-E", "Gentle storyteller"),
    }

    suspend fun speakWithPreset(text: String, preset: VoicePreset): ByteArray {
        val (rate, pitch) = when (preset) {
            VoicePreset.NARRATOR_FEMALE -> 0.9 to -1.0
            VoicePreset.NARRATOR_MALE -> 0.85 to -2.0
            VoicePreset.ASSISTANT_FEMALE -> 1.05 to 1.0
            VoicePreset.ASSISTANT_MALE -> 1.0 to 0.0
            VoicePreset.NEWS_ANCHOR -> 1.1 to 0.0
            VoicePreset.CHILD_STORY -> 0.8 to 2.0
        }

        return ttsRepo.synthesize(
            text = text,
            voiceName = preset.voiceName,
            speakingRate = rate,
            pitch = pitch
        )
    }

    // List voices filtered by language
    suspend fun getVoicesForLanguage(languageCode: String): List<CloudVoice> {
        return ttsRepo.getVoices(languageCode)
    }
}
```

---

## 4. SSML for Speech Control

```
SSML (Speech Synthesis Markup Language):
┌──────────────────────────────────────────────────────┐
│                                                      │
│  SSML gives fine-grained control over speech:       │
│                                                      │
│  <speak>                                             │
│    <p>Paragraph with a pause.</p>                   │
│    <s>Individual sentence.</s>                      │
│    <break time="500ms"/>  ← Pause                   │
│    <emphasis level="strong">Important!</emphasis>   │
│    <prosody rate="slow" pitch="+2st">               │
│      Slow and high pitched                          │
│    </prosody>                                       │
│    <say-as interpret-as="cardinal">42</say-as>      │
│    <say-as interpret-as="date">2026-04-15</say-as>  │
│    <sub alias="World Wide Web Consortium">W3C</sub> │
│    <phoneme alphabet="ipa" ph="təˈmeɪtoʊ">         │
│      tomato                                          │
│    </phoneme>                                        │
│  </speak>                                            │
│                                                      │
│  Common SSML Tags:                                   │
│  ┌──────────────┬────────────────────────────┐      │
│  │ Tag          │ Purpose                     │      │
│  ├──────────────┼────────────────────────────┤      │
│  │ <break>      │ Insert pause                │      │
│  │ <emphasis>   │ Stress words                │      │
│  │ <prosody>    │ Rate, pitch, volume         │      │
│  │ <say-as>     │ Interpret numbers, dates    │      │
│  │ <sub>        │ Pronunciation substitution  │      │
│  │ <phoneme>    │ Exact pronunciation (IPA)   │      │
│  │ <p>, <s>     │ Paragraphs, sentences       │      │
│  │ <mark>       │ Bookmark in audio stream    │      │
│  │ <audio>      │ Insert pre-recorded audio   │      │
│  └──────────────┴────────────────────────────┘      │
└──────────────────────────────────────────────────────┘
```

```kotlin
class SSMLBuilder {
    private val content = StringBuilder()

    fun text(text: String) = apply { content.append(escapeXml(text)) }

    fun pause(milliseconds: Int) = apply {
        content.append("""<break time="${milliseconds}ms"/>""")
    }

    fun emphasis(text: String, level: String = "moderate") = apply {
        // level: reduced, moderate, strong
        content.append("""<emphasis level="$level">${escapeXml(text)}</emphasis>""")
    }

    fun prosody(
        text: String,
        rate: String? = null,    // x-slow, slow, medium, fast, x-fast, or %
        pitch: String? = null,   // x-low, low, medium, high, x-high, or semitones
        volume: String? = null   // silent, x-soft, soft, medium, loud, x-loud, or dB
    ) = apply {
        val attrs = buildList {
            rate?.let { add("""rate="$it"""") }
            pitch?.let { add("""pitch="$it"""") }
            volume?.let { add("""volume="$it"""") }
        }.joinToString(" ")
        content.append("""<prosody $attrs>${escapeXml(text)}</prosody>""")
    }

    fun sayAs(text: String, interpretAs: String) = apply {
        // interpretAs: cardinal, ordinal, characters, fraction, date, time, telephone, currency
        content.append("""<say-as interpret-as="$interpretAs">${escapeXml(text)}</say-as>""")
    }

    fun substitute(text: String, alias: String) = apply {
        content.append("""<sub alias="${escapeXml(alias)}">${escapeXml(text)}</sub>""")
    }

    fun paragraph(block: SSMLBuilder.() -> Unit) = apply {
        content.append("<p>")
        block()
        content.append("</p>")
    }

    fun sentence(block: SSMLBuilder.() -> Unit) = apply {
        content.append("<s>")
        block()
        content.append("</s>")
    }

    fun build(): String = "<speak>$content</speak>"

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}

// Usage example
fun buildWeatherSSML(city: String, temp: Int, condition: String): String {
    return SSMLBuilder().apply {
        paragraph {
            sentence {
                text("Here's the weather for ")
                emphasis(city, "moderate")
                pause(300)
            }
            sentence {
                text("The temperature is ")
                sayAs("$temp", "cardinal")
                text(" degrees. ")
            }
            sentence {
                text("Conditions are ")
                prosody(condition, rate = "slow", pitch = "+1st")
                text(".")
            }
        }
    }.build()
}

// Produces:
// <speak><p>
//   <s>Here's the weather for <emphasis level="moderate">New York</emphasis>
//      <break time="300ms"/></s>
//   <s>The temperature is <say-as interpret-as="cardinal">72</say-as> degrees. </s>
//   <s>Conditions are <prosody rate="slow" pitch="+1st">sunny</prosody>.</s>
// </p></speak>
```

---

## Audio Playback Helper

```kotlin
import android.media.MediaPlayer

class TTSAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playAudioBytes(audioBytes: ByteArray) {
        release()

        // Write to temp file (MediaPlayer needs a file descriptor)
        val tempFile = java.io.File.createTempFile("tts_", ".mp3", context.cacheDir)
        tempFile.writeBytes(audioBytes)
        tempFile.deleteOnExit()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(tempFile.absolutePath)
            setOnCompletionListener {
                tempFile.delete()
                release()
            }
            prepare()
            start()
        }
    }

    fun stop() = mediaPlayer?.stop()
    fun pause() = mediaPlayer?.pause()
    fun resume() = mediaPlayer?.start()
    fun isPlaying() = mediaPlayer?.isPlaying ?: false

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
```

---

## Complete ViewModel

```kotlin
class TTSViewModel(application: Application) : AndroidViewModel(application) {

    private val onDeviceTTS = AndroidTTSManager(application)
    private val audioPlayer = TTSAudioPlayer(application)

    // Use backend proxy in production
    private val cloudTTS = CloudTTSRepository(BuildConfig.TTS_API_KEY)
    private val voiceSelector = VoiceSelector(cloudTTS)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    init {
        onDeviceTTS.initialize(object : AndroidTTSManager.TTSListener {
            override fun onReady() {}
            override fun onSpeakStart(utteranceId: String) { _isSpeaking.value = true }
            override fun onSpeakDone(utteranceId: String) { _isSpeaking.value = false }
            override fun onError(utteranceId: String, errorCode: Int) { _isSpeaking.value = false }
        })
    }

    // On-device (free, instant, offline)
    fun speakOnDevice(text: String) {
        onDeviceTTS.speak(text)
    }

    // Cloud neural voice (high quality)
    fun speakCloud(text: String, preset: VoiceSelector.VoicePreset) {
        viewModelScope.launch {
            _isSpeaking.value = true
            try {
                val audioBytes = voiceSelector.speakWithPreset(text, preset)
                audioPlayer.playAudioBytes(audioBytes)
            } catch (e: Exception) {
                // Fallback to on-device
                onDeviceTTS.speak(text)
            } finally {
                _isSpeaking.value = false
            }
        }
    }

    fun stop() {
        onDeviceTTS.stop()
        audioPlayer.stop()
        _isSpeaking.value = false
    }

    override fun onCleared() {
        super.onCleared()
        onDeviceTTS.shutdown()
        audioPlayer.release()
    }
}
```
