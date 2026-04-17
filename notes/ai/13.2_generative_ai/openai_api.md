# OpenAI API Integration — Generative AI for Android

## Overview

OpenAI provides powerful AI models (GPT, DALL-E, Whisper, TTS) accessible via REST APIs. Android apps integrate using Retrofit or OkHttp.

```
OpenAI API Architecture for Android:
┌──────────────────────────────────────────────────────┐
│                 Android App                           │
│  ┌────────────────────────────────────────────────┐  │
│  │              ViewModel / Repository             │  │
│  │  ┌──────────────────────────────────────────┐  │  │
│  │  │  Retrofit + OkHttp                        │  │  │
│  │  │  (JSON serialization with Moshi/Gson)     │  │  │
│  │  └──────────────────┬───────────────────────┘  │  │
│  └─────────────────────┼──────────────────────────┘  │
└────────────────────────┼─────────────────────────────┘
                         │ HTTPS
┌────────────────────────▼─────────────────────────────┐
│              api.openai.com                           │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌────────┐ │
│  │  GPT-4  │  │ DALL-E  │  │ Whisper │  │  TTS   │ │
│  │  /Chat  │  │ /Image  │  │ /Audio  │  │ /Audio │ │
│  └─────────┘  └─────────┘  └─────────┘  └────────┘ │
│  ┌─────────┐                                         │
│  │Embedding│                                         │
│  │ /Search │                                         │
│  └─────────┘                                         │
└──────────────────────────────────────────────────────┘

⚠️ SECURITY: Never hardcode API keys in Android apps!
   Use a backend proxy server instead.
   
   Client ──▶ Your Backend ──▶ OpenAI API
                (holds key)
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    
    // Moshi for JSON
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.moshi:moshi-adapters:1.15.0")
    
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")  // Server-Sent Events (streaming)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

---

## 1. GPT Models for Chat and Completion

### Theory

```
Chat Completion API:
┌──────────────────────────────────────────────────────┐
│ POST /v1/chat/completions                            │
│                                                      │
│ Request:                                             │
│ {                                                    │
│   "model": "gpt-4",                                 │
│   "messages": [                                      │
│     {"role": "system", "content": "You are..."},     │
│     {"role": "user", "content": "Hello"},            │
│     {"role": "assistant", "content": "Hi!"},         │
│     {"role": "user", "content": "How are you?"}      │
│   ],                                                 │
│   "temperature": 0.7,                                │
│   "max_tokens": 1000                                 │
│ }                                                    │
│                                                      │
│ Roles:                                               │
│ ┌──────────┬────────────────────────────────────┐    │
│ │ system   │ Sets behavior/persona (invisible)  │    │
│ │ user     │ Human messages                     │    │
│ │ assistant│ AI responses (history)             │    │
│ │ function │ Function call results              │    │
│ └──────────┴────────────────────────────────────┘    │
│                                                      │
│ Models:                                              │
│ ┌──────────────┬─────────────────────────────────┐   │
│ │ gpt-4o       │ Latest, multimodal, fast        │   │
│ │ gpt-4-turbo  │ GPT-4 optimized, cheaper        │   │
│ │ gpt-4        │ Most capable reasoning          │   │
│ │ gpt-3.5-turbo│ Fast, cheap, good for most     │   │
│ └──────────────┴─────────────────────────────────┘   │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
// --- Data Models ---
@JsonClass(generateAdapter = true)
data class ChatRequest(
    val model: String = "gpt-4",
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    @Json(name = "max_tokens") val maxTokens: Int = 1000,
    val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,  // "system", "user", "assistant"
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int,
    val message: Message,
    @Json(name = "finish_reason") val finishReason: String?
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "prompt_tokens") val promptTokens: Int,
    @Json(name = "completion_tokens") val completionTokens: Int,
    @Json(name = "total_tokens") val totalTokens: Int
)

// --- Retrofit API Interface ---
interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Body request: ChatRequest
    ): ChatResponse
}

// --- API Client ---
class OpenAIClient(private val apiKey: String) {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: OpenAIApi = retrofit.create(OpenAIApi::class.java)
}

// --- Repository ---
class ChatRepository(private val client: OpenAIClient) {

    private val conversationHistory = mutableListOf<Message>()

    init {
        // System prompt
        conversationHistory.add(
            Message("system", "You are a helpful Android development assistant.")
        )
    }

    suspend fun sendMessage(userMessage: String): String {
        conversationHistory.add(Message("user", userMessage))

        val request = ChatRequest(
            model = "gpt-4",
            messages = conversationHistory.toList(),
            temperature = 0.7f,
            maxTokens = 1000
        )

        val response = client.api.chatCompletion(request)
        val assistantMessage = response.choices.first().message.content

        // Add to history for context
        conversationHistory.add(Message("assistant", assistantMessage))

        Log.d("OpenAI", "Tokens used: ${response.usage.totalTokens}")

        return assistantMessage
    }

    fun clearHistory() {
        val systemMessage = conversationHistory.first()
        conversationHistory.clear()
        conversationHistory.add(systemMessage)
    }
}

// --- ViewModel ---
class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _response = MutableStateFlow("")
    val response: StateFlow<String> = _response.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _response.value = repository.sendMessage(message)
            } catch (e: Exception) {
                _response.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

### Streaming with SSE (Server-Sent Events)

```kotlin
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

class OpenAIStreamClient(private val apiKey: String) {

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS) // No timeout for SSE
        .build()

    fun streamChat(
        messages: List<Message>,
        onChunk: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val requestBody = ChatRequest(
            model = "gpt-4",
            messages = messages,
            stream = true
        )

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(ChatRequest::class.java)
        val jsonBody = jsonAdapter.toJson(requestBody)

        val request = okhttp3.Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        val eventSourceFactory = EventSources.createFactory(okHttpClient)
        eventSourceFactory.newEventSource(request, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") {
                    onComplete()
                    return
                }

                try {
                    // Parse SSE data chunk
                    val json = org.json.JSONObject(data)
                    val delta = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("delta")

                    if (delta.has("content")) {
                        onChunk(delta.getString("content"))
                    }
                } catch (e: Exception) {
                    // Skip malformed chunks
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                t?.let { onError(it) }
            }
        })
    }
}
```

---

## 2. DALL-E for Image Generation

### Theory

```
DALL-E API:
┌──────────────────────────────────────────────────────┐
│ POST /v1/images/generations                          │
│                                                      │
│ Input: Text prompt                                   │
│ Output: Generated image(s) as URL or base64          │
│                                                      │
│ Models:                                              │
│  dall-e-3  │ Best quality, 1024x1024,1792x1024,etc. │
│  dall-e-2  │ Faster, 256x256, 512x512, 1024x1024    │
│                                                      │
│ Sizes: 256x256, 512x512, 1024x1024,                 │
│        1024x1792, 1792x1024 (DALL-E 3)               │
│                                                      │
│ Endpoints:                                           │
│  /generations  → Create new images                   │
│  /edits        → Edit existing images (inpainting)   │
│  /variations   → Create variations of an image       │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
// Data models
@JsonClass(generateAdapter = true)
data class ImageGenerationRequest(
    val model: String = "dall-e-3",
    val prompt: String,
    val n: Int = 1,
    val size: String = "1024x1024",  // "256x256", "512x512", "1024x1024"
    val quality: String = "standard",  // "standard" or "hd"
    @Json(name = "response_format") val responseFormat: String = "url"  // "url" or "b64_json"
)

@JsonClass(generateAdapter = true)
data class ImageResponse(
    val created: Long,
    val data: List<ImageData>
)

@JsonClass(generateAdapter = true)
data class ImageData(
    val url: String?,
    @Json(name = "b64_json") val b64Json: String?,
    @Json(name = "revised_prompt") val revisedPrompt: String?
)

// API Interface
interface OpenAIDalleApi {
    @POST("v1/images/generations")
    suspend fun generateImage(@Body request: ImageGenerationRequest): ImageResponse
}

// Usage
class ImageGenerationRepository(private val client: OpenAIClient) {

    suspend fun generateImage(prompt: String): String {
        val request = ImageGenerationRequest(
            prompt = prompt,
            model = "dall-e-3",
            size = "1024x1024"
        )

        val response = client.api.generateImage(request)  // Add to API interface
        return response.data.first().url ?: ""
    }

    // Load generated image into ImageView
    suspend fun generateAndDisplay(prompt: String, imageView: ImageView) {
        val imageUrl = generateImage(prompt)

        // Use Coil to load
        imageView.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
        }
    }
}
```

---

## 3. Whisper for Speech-to-Text

### Theory

```
Whisper API:
┌──────────────────────────────────────────────────────┐
│ POST /v1/audio/transcriptions                        │
│                                                      │
│ Input: Audio file (mp3, wav, m4a, webm, etc.)        │
│ Output: Transcribed text                             │
│                                                      │
│ Max file size: 25 MB                                 │
│ Supported languages: 99+                             │
│                                                      │
│ Features:                                            │
│  - Automatic language detection                      │
│  - Word-level timestamps                             │
│  - Translation (any language → English)              │
│                                                      │
│ Pipeline:                                            │
│ ┌──────┐    ┌──────────┐    ┌───────────┐            │
│ │Record│───▶│ Upload   │───▶│ Whisper   │───▶ Text   │
│ │Audio │    │ to API   │    │ Model     │            │
│ └──────┘    └──────────┘    └───────────┘            │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
// Multipart upload for audio
interface OpenAIWhisperApi {
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribe(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("language") language: RequestBody? = null,
        @Part("response_format") responseFormat: RequestBody? = null
    ): TranscriptionResponse
    
    @Multipart
    @POST("v1/audio/translations")
    suspend fun translate(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody
    ): TranscriptionResponse
}

@JsonClass(generateAdapter = true)
data class TranscriptionResponse(
    val text: String
)

class WhisperRepository(private val apiKey: String) {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create(OpenAIWhisperApi::class.java)

    // Transcribe audio file
    suspend fun transcribeAudio(audioFile: File): String {
        val requestFile = audioFile.asRequestBody("audio/wav".toMediaType())
        val filePart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
        val modelPart = "whisper-1".toRequestBody("text/plain".toMediaType())

        val response = api.transcribe(filePart, modelPart)
        return response.text
    }

    // Translate audio to English
    suspend fun translateAudio(audioFile: File): String {
        val requestFile = audioFile.asRequestBody("audio/wav".toMediaType())
        val filePart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
        val modelPart = "whisper-1".toRequestBody("text/plain".toMediaType())

        val response = api.translate(filePart, modelPart)
        return response.text
    }

    // Record and transcribe
    class AudioRecorderHelper(private val context: Context) {
        private var mediaRecorder: MediaRecorder? = null
        private var outputFile: File? = null

        fun startRecording(): File {
            outputFile = File(context.cacheDir, "recording_${System.currentTimeMillis()}.wav")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setOutputFile(outputFile!!.absolutePath)
                prepare()
                start()
            }

            return outputFile!!
        }

        fun stopRecording(): File {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            return outputFile!!
        }
    }
}
```

---

## 4. Text-to-Speech API

### Theory

```
TTS API:
┌──────────────────────────────────────────────────────┐
│ POST /v1/audio/speech                                │
│                                                      │
│ Input: Text                                          │
│ Output: Audio file (mp3, opus, aac, flac, wav, pcm)  │
│                                                      │
│ Models:                                              │
│  tts-1     │ Faster, lower quality                   │
│  tts-1-hd  │ Higher quality                          │
│                                                      │
│ Voices: alloy, echo, fable, onyx, nova, shimmer      │
│                                                      │
│ Max: 4096 characters per request                     │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
@JsonClass(generateAdapter = true)
data class TTSRequest(
    val model: String = "tts-1",
    val input: String,
    val voice: String = "alloy",  // alloy, echo, fable, onyx, nova, shimmer
    @Json(name = "response_format") val responseFormat: String = "mp3",
    val speed: Float = 1.0f  // 0.25 to 4.0
)

interface OpenAITTSApi {
    @POST("v1/audio/speech")
    @Streaming
    suspend fun textToSpeech(@Body request: TTSRequest): ResponseBody
}

class TTSRepository(private val apiKey: String, private val context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create(OpenAITTSApi::class.java)

    suspend fun speak(text: String, voice: String = "alloy") {
        val request = TTSRequest(input = text, voice = voice)
        val responseBody = api.textToSpeech(request)

        // Save to file
        val audioFile = File(context.cacheDir, "tts_${System.currentTimeMillis()}.mp3")
        responseBody.byteStream().use { input ->
            audioFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // Play with MediaPlayer
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFile.absolutePath)
            prepare()
            start()
        }

        mediaPlayer.setOnCompletionListener {
            it.release()
            audioFile.delete()
        }
    }
}
```

---

## 5. Embeddings for Semantic Search

### Theory

```
Embeddings:
┌──────────────────────────────────────────────────────┐
│                                                      │
│ Text → Vector (array of 1536 floats)                 │
│                                                      │
│ "Android development"  → [0.023, -0.045, 0.012, ...]│
│ "Mobile app coding"    → [0.021, -0.043, 0.015, ...]│  Similar vectors!
│ "Cooking recipes"      → [-0.08, 0.067, -0.031, ...]│  Different vector
│                                                      │
│ Similarity = Cosine Distance between vectors         │
│                                                      │
│ Use Cases:                                           │
│ ┌──────────────────────────────────────────────────┐ │
│ │ 1. Semantic Search: Find relevant documents      │ │
│ │ 2. Recommendations: Find similar items           │ │
│ │ 3. Clustering: Group similar text                │ │
│ │ 4. Classification: Categorize text               │ │
│ │ 5. RAG: Retrieval-Augmented Generation          │ │
│ └──────────────────────────────────────────────────┘ │
│                                                      │
│ Workflow:                                            │
│  Index Phase:                                        │
│  Documents ──▶ API ──▶ Vectors ──▶ Store in DB      │
│                                                      │
│  Query Phase:                                        │
│  Query ──▶ API ──▶ Vector ──▶ Find closest in DB    │
│                           ──▶ Return matching docs   │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
@JsonClass(generateAdapter = true)
data class EmbeddingRequest(
    val model: String = "text-embedding-3-small",
    val input: List<String>
)

@JsonClass(generateAdapter = true)
data class EmbeddingResponse(
    val data: List<EmbeddingData>,
    val usage: Usage
)

@JsonClass(generateAdapter = true)
data class EmbeddingData(
    val index: Int,
    val embedding: List<Float>
)

interface OpenAIEmbeddingApi {
    @POST("v1/embeddings")
    suspend fun createEmbedding(@Body request: EmbeddingRequest): EmbeddingResponse
}

class EmbeddingRepository(private val client: OpenAIClient) {

    // Get embedding for text
    suspend fun getEmbedding(text: String): List<Float> {
        val request = EmbeddingRequest(input = listOf(text))
        val response = client.api.createEmbedding(request)
        return response.data.first().embedding
    }

    // Get embeddings for multiple texts
    suspend fun getEmbeddings(texts: List<String>): List<List<Float>> {
        val request = EmbeddingRequest(input = texts)
        val response = client.api.createEmbedding(request)
        return response.data.sortedBy { it.index }.map { it.embedding }
    }

    // Cosine similarity between two vectors
    fun cosineSimilarity(a: List<Float>, b: List<Float>): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f

        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }

        return dotProduct / (Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble())).toFloat()
    }

    // Semantic search
    suspend fun semanticSearch(
        query: String,
        documents: List<String>,
        topK: Int = 5
    ): List<Pair<String, Float>> {
        // Get all embeddings at once (more efficient)
        val allTexts = listOf(query) + documents
        val embeddings = getEmbeddings(allTexts)

        val queryEmbedding = embeddings.first()
        val docEmbeddings = embeddings.drop(1)

        // Calculate similarity scores
        return documents.zip(docEmbeddings)
            .map { (doc, embedding) ->
                doc to cosineSimilarity(queryEmbedding, embedding)
            }
            .sortedByDescending { it.second }
            .take(topK)
    }
}
```

---

## 6. Retrofit Integration Patterns (Production)

### Secure API Key Pattern (Backend Proxy)

```kotlin
// ✅ CORRECT: Use your own backend as a proxy
// Your backend server holds the OpenAI API key

interface BackendApi {
    @POST("api/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @POST("api/generate-image")
    suspend fun generateImage(@Body request: ImageGenerationRequest): ImageResponse
}

class SecureOpenAIRepository(context: Context) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://your-backend.example.com/")  // Your backend
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create(BackendApi::class.java)

    suspend fun chat(message: String): String {
        val request = ChatRequest(
            messages = listOf(Message("user", message))
        )
        return api.chat(request).choices.first().message.content
    }
}

// Backend (Node.js/Python) holds the OpenAI key and proxies requests:
// Client → Your Server (validates, rate-limits) → OpenAI API
```

### Error Handling Pattern

```kotlin
class OpenAIRepository(private val api: OpenAIApi) {

    sealed class ApiResult<out T> {
        data class Success<T>(val data: T) : ApiResult<T>()
        data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
        data class NetworkError(val exception: Exception) : ApiResult<Nothing>()
    }

    suspend fun sendMessage(messages: List<Message>): ApiResult<String> {
        return try {
            val request = ChatRequest(messages = messages)
            val response = api.chatCompletion(request)
            ApiResult.Success(response.choices.first().message.content)
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                401 -> ApiResult.Error(401, "Invalid API key")
                429 -> ApiResult.Error(429, "Rate limit exceeded. Try again later.")
                500 -> ApiResult.Error(500, "OpenAI server error")
                else -> ApiResult.Error(e.code(), e.message())
            }
        } catch (e: java.net.UnknownHostException) {
            ApiResult.NetworkError(e)
        } catch (e: java.net.SocketTimeoutException) {
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Unknown error")
        }
    }
}
```
