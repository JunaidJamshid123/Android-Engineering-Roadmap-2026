# Anthropic Claude API — Advanced AI Integration

## Overview

Claude is Anthropic's AI assistant known for **advanced reasoning, long context windows (200K+ tokens), and strong safety/alignment**. It integrates with Android apps via REST API using Retrofit.

```
Claude Model Family:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Claude 3.5 Sonnet  ─ Best balance (speed + quality) │
│  Claude 3 Opus      ─ Most capable, complex tasks    │
│  Claude 3 Sonnet    ─ Balanced mid-tier              │
│  Claude 3 Haiku     ─ Fastest, most affordable       │
│                                                      │
│  Key Strengths:                                      │
│  ┌────────────────────────────────────────────────┐  │
│  │ ✅ 200K token context (≈ 150K words / 500 pgs) │  │
│  │ ✅ Superior reasoning on complex tasks          │  │
│  │ ✅ Vision capabilities (image analysis)         │  │
│  │ ✅ Strong instruction following                 │  │
│  │ ✅ Constitutional AI safety approach            │  │
│  │ ✅ Extended thinking for step-by-step reasoning │  │
│  └────────────────────────────────────────────────┘  │
│                                                      │
│  Architecture:                                       │
│  ┌────────────────────────────────────────────┐      │
│  │  Android App                                │      │
│  │  ┌──────────────────────────────────────┐  │      │
│  │  │  Retrofit / OkHttp Client            │  │      │
│  │  └─────────────┬────────────────────────┘  │      │
│  └────────────────┼───────────────────────────┘      │
│                   │ HTTPS                            │
│  ┌────────────────▼───────────────────────────┐      │
│  │  api.anthropic.com/v1/messages              │      │
│  │  ┌──────────────────────────────────────┐  │      │
│  │  │  Claude Models                        │  │      │
│  │  └──────────────────────────────────────┘  │      │
│  └────────────────────────────────────────────┘      │
│                                                      │
│  ⚠️ Use backend proxy — never expose API key in app  │
└──────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

---

## 1. Advanced Reasoning Capabilities

### Theory

```
Claude's Reasoning Architecture:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Claude excels at multi-step reasoning:              │
│                                                      │
│  1. CHAIN-OF-THOUGHT                                 │
│     Prompt: "Think step by step"                     │
│     Claude reasons through problems methodically     │
│                                                      │
│  2. EXTENDED THINKING                                │
│     Claude shows its reasoning process:              │
│     <thinking>                                       │
│       Let me analyze this...                         │
│       First, I need to consider...                   │
│       The key insight is...                          │
│     </thinking>                                      │
│     Final answer: ...                                │
│                                                      │
│  3. TOOL USE (Function Calling)                      │
│     Claude can invoke tools you define               │
│                                                      │
│  vs GPT-4:                                          │
│  ┌──────────────┬──────────┬──────────┐             │
│  │ Feature      │ Claude   │ GPT-4    │             │
│  ├──────────────┼──────────┼──────────┤             │
│  │ Context      │ 200K     │ 128K     │             │
│  │ Reasoning    │ Strong   │ Strong   │             │
│  │ Coding       │ Strong   │ Strong   │             │
│  │ Safety       │ Const AI │ RLHF     │             │
│  │ Vision       │ ✅       │ ✅       │             │
│  │ Speed        │ Variable │ Variable │             │
│  └──────────────┴──────────┴──────────┘             │
└──────────────────────────────────────────────────────┘
```

### Code — API Data Models

```kotlin
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Request Models ---
@JsonClass(generateAdapter = true)
data class ClaudeRequest(
    val model: String = "claude-3-5-sonnet-20241022",
    @Json(name = "max_tokens") val maxTokens: Int = 4096,
    val messages: List<ClaudeMessage>,
    val system: String? = null,         // System prompt
    val temperature: Float? = 0.7f,
    val stream: Boolean = false,
    @Json(name = "top_p") val topP: Float? = null,
    @Json(name = "top_k") val topK: Int? = null,
    val tools: List<ClaudeTool>? = null  // Function calling
)

@JsonClass(generateAdapter = true)
data class ClaudeMessage(
    val role: String,  // "user" or "assistant"
    val content: Any   // String or List<ContentBlock> for multimodal
)

@JsonClass(generateAdapter = true)
data class ContentBlock(
    val type: String,  // "text", "image"
    val text: String? = null,
    val source: ImageSource? = null
)

@JsonClass(generateAdapter = true)
data class ImageSource(
    val type: String = "base64",
    @Json(name = "media_type") val mediaType: String,  // "image/jpeg", "image/png"
    val data: String  // base64-encoded image
)

// --- Response Models ---
@JsonClass(generateAdapter = true)
data class ClaudeResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ResponseContentBlock>,
    val model: String,
    @Json(name = "stop_reason") val stopReason: String?,
    val usage: ClaudeUsage
)

@JsonClass(generateAdapter = true)
data class ResponseContentBlock(
    val type: String,  // "text" or "tool_use"
    val text: String? = null,
    val id: String? = null,      // For tool_use
    val name: String? = null,    // Tool name
    val input: Map<String, Any>? = null  // Tool arguments
)

@JsonClass(generateAdapter = true)
data class ClaudeUsage(
    @Json(name = "input_tokens") val inputTokens: Int,
    @Json(name = "output_tokens") val outputTokens: Int
)

// --- Tool/Function Models ---
@JsonClass(generateAdapter = true)
data class ClaudeTool(
    val name: String,
    val description: String,
    @Json(name = "input_schema") val inputSchema: ToolSchema
)

@JsonClass(generateAdapter = true)
data class ToolSchema(
    val type: String = "object",
    val properties: Map<String, PropertySchema>,
    val required: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class PropertySchema(
    val type: String,
    val description: String,
    val enum: List<String>? = null
)
```

### API Interface & Client

```kotlin
// Retrofit API Interface
interface ClaudeApi {
    @POST("v1/messages")
    suspend fun createMessage(@Body request: ClaudeRequest): ClaudeResponse
}

// API Client
class ClaudeClient(private val apiKey: String) {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.anthropic.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: ClaudeApi = retrofit.create(ClaudeApi::class.java)
}

// Repository
class ClaudeRepository(private val client: ClaudeClient) {

    private val conversationHistory = mutableListOf<ClaudeMessage>()

    // Simple text message
    suspend fun sendMessage(userMessage: String): String {
        conversationHistory.add(ClaudeMessage("user", userMessage))

        val request = ClaudeRequest(
            model = "claude-3-5-sonnet-20241022",
            maxTokens = 4096,
            messages = conversationHistory.toList(),
            system = "You are a helpful Android development assistant. Be concise and provide Kotlin code examples."
        )

        val response = client.api.createMessage(request)
        val assistantText = response.content
            .filter { it.type == "text" }
            .joinToString("") { it.text ?: "" }

        conversationHistory.add(ClaudeMessage("assistant", assistantText))

        Log.d("Claude", "Tokens: ${response.usage.inputTokens} in, ${response.usage.outputTokens} out")

        return assistantText
    }

    // Advanced reasoning with chain-of-thought
    suspend fun reasonAbout(problem: String): String {
        val request = ClaudeRequest(
            model = "claude-3-5-sonnet-20241022",
            maxTokens = 4096,
            messages = listOf(
                ClaudeMessage("user", """
                    Think through this problem step by step, showing your reasoning:
                    
                    $problem
                    
                    First show your thinking process, then provide the final answer.
                """.trimIndent())
            ),
            temperature = 0.3f  // Lower temperature for reasoning
        )

        val response = client.api.createMessage(request)
        return response.content.first().text ?: ""
    }

    fun clearHistory() = conversationHistory.clear()
}
```

---

## 2. Long Context Windows

### Theory

```
Context Window Comparison:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Claude 3:    200,000 tokens (~150,000 words)        │
│  GPT-4:      128,000 tokens (~96,000 words)          │
│  Gemini 1.5: 1,000,000 tokens (~750,000 words)       │
│                                                      │
│  What fits in 200K tokens:                           │
│  ┌──────────────────────────────────────────────┐    │
│  │ ✅ Entire codebase for code review            │    │
│  │ ✅ Full book for analysis                     │    │
│  │ ✅ Hundreds of documents for RAG              │    │
│  │ ✅ Complete conversation history               │    │
│  │ ✅ Large CSV/JSON data files                   │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  Use Cases:                                          │
│  • Analyze entire Android project for code review    │
│  • Process long documents (legal, medical, etc.)     │
│  • Maintain very long conversation context           │
│  • in-context learning with many examples            │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class LongContextHelper(private val client: ClaudeClient) {

    // Analyze entire file/codebase
    suspend fun analyzeCode(codeFiles: Map<String, String>): String {
        val codeContent = codeFiles.entries.joinToString("\n\n") { (filename, code) ->
            "--- File: $filename ---\n$code"
        }

        val request = ClaudeRequest(
            model = "claude-3-5-sonnet-20241022",
            maxTokens = 4096,
            messages = listOf(
                ClaudeMessage("user", """
                    Analyze the following Android project code. Identify:
                    1. Potential bugs
                    2. Performance issues
                    3. Security vulnerabilities
                    4. Best practice violations
                    
                    $codeContent
                """.trimIndent())
            ),
            system = "You are an expert Android code reviewer."
        )

        return client.api.createMessage(request).content.first().text ?: ""
    }

    // Document analysis
    suspend fun analyzeDocument(documentText: String, question: String): String {
        val request = ClaudeRequest(
            model = "claude-3-5-sonnet-20241022",
            maxTokens = 4096,
            messages = listOf(
                ClaudeMessage("user", """
                    Here is a document:
                    
                    <document>
                    $documentText
                    </document>
                    
                    Question: $question
                    
                    Answer based only on the document content.
                """.trimIndent())
            )
        )

        return client.api.createMessage(request).content.first().text ?: ""
    }

    // Process large data
    suspend fun analyzeLargeDataset(csvData: String): String {
        val request = ClaudeRequest(
            model = "claude-3-5-sonnet-20241022",
            maxTokens = 4096,
            messages = listOf(
                ClaudeMessage("user", """
                    Analyze this dataset and provide insights:
                    
                    $csvData
                    
                    Provide:
                    1. Summary statistics
                    2. Key patterns and trends
                    3. Anomalies or outliers
                    4. Actionable recommendations
                """.trimIndent())
            )
        )

        return client.api.createMessage(request).content.first().text ?: ""
    }
}
```

---

## 3. Vision Capabilities

### Theory

```
Claude Vision:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Input: Base64-encoded images + text prompt          │
│                                                      │
│  Supported formats: JPEG, PNG, GIF, WebP             │
│  Max image size: ~5MB per image                      │
│  Multiple images: Up to 20 per request               │
│                                                      │
│  Vision Tasks:                                       │
│  ┌──────────────────────────────────────────────┐    │
│  │ • Image description and analysis              │    │
│  │ • Text extraction (OCR)                       │    │
│  │ • Chart/graph interpretation                  │    │
│  │ • UI/UX analysis of screenshots               │    │
│  │ • Code from screenshots                       │    │
│  │ • Object identification                       │    │
│  │ • Comparing multiple images                   │    │
│  │ • Document parsing (receipts, forms)          │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  Message structure with image:                       │
│  {                                                   │
│    "role": "user",                                   │
│    "content": [                                      │
│      { "type": "image",                              │
│        "source": {                                   │
│          "type": "base64",                           │
│          "media_type": "image/jpeg",                 │
│          "data": "<base64 string>"                   │
│        }                                             │
│      },                                              │
│      { "type": "text",                               │
│        "text": "Describe this image"                 │
│      }                                               │
│    ]                                                 │
│  }                                                   │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class ClaudeVisionHelper(private val client: ClaudeClient) {

    // Convert bitmap to base64
    private fun bitmapToBase64(bitmap: Bitmap, quality: Int = 85): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val bytes = outputStream.toByteArray()
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }

    // Analyze single image
    suspend fun analyzeImage(bitmap: Bitmap, prompt: String): String {
        val base64Image = bitmapToBase64(bitmap)

        val content = listOf(
            ContentBlock(
                type = "image",
                source = ImageSource(
                    type = "base64",
                    mediaType = "image/jpeg",
                    data = base64Image
                )
            ),
            ContentBlock(type = "text", text = prompt)
        )

        val request = ClaudeRequest(
            model = "claude-3-5-sonnet-20241022",
            maxTokens = 4096,
            messages = listOf(ClaudeMessage("user", content))
        )

        val response = client.api.createMessage(request)
        return response.content.first().text ?: ""
    }

    // Compare multiple images
    suspend fun compareImages(bitmap1: Bitmap, bitmap2: Bitmap, prompt: String): String {
        val content = listOf(
            ContentBlock(
                type = "image",
                source = ImageSource("base64", "image/jpeg", bitmapToBase64(bitmap1))
            ),
            ContentBlock(
                type = "image",
                source = ImageSource("base64", "image/jpeg", bitmapToBase64(bitmap2))
            ),
            ContentBlock(type = "text", text = prompt)
        )

        val request = ClaudeRequest(
            model = "claude-3-5-sonnet-20241022",
            maxTokens = 4096,
            messages = listOf(ClaudeMessage("user", content))
        )

        val response = client.api.createMessage(request)
        return response.content.first().text ?: ""
    }

    // Analyze UI screenshot
    suspend fun analyzeUI(screenshotBitmap: Bitmap): String {
        return analyzeImage(screenshotBitmap, """
            Analyze this mobile app UI screenshot:
            1. Identify all UI components visible
            2. Assess the layout and spacing
            3. Check text readability and contrast
            4. Suggest UX improvements
            5. Rate accessibility (1-10)
            6. Generate Jetpack Compose code to recreate this UI
        """.trimIndent())
    }

    // Extract structured data from document image
    suspend fun extractFromDocument(documentBitmap: Bitmap): String {
        return analyzeImage(documentBitmap, """
            Extract all information from this document image.
            Return the data as structured JSON with appropriate fields.
            Include all text, numbers, dates, and any structured data.
        """.trimIndent())
    }
}
```

---

## 4. Tool Use (Function Calling)

```kotlin
class ClaudeToolsHelper(private val client: ClaudeClient) {

    // Define tools
    private val tools = listOf(
        ClaudeTool(
            name = "get_weather",
            description = "Get current weather for a city",
            inputSchema = ToolSchema(
                type = "object",
                properties = mapOf(
                    "city" to PropertySchema("string", "City name"),
                    "unit" to PropertySchema("string", "Temperature unit", enum = listOf("celsius", "fahrenheit"))
                ),
                required = listOf("city")
            )
        ),
        ClaudeTool(
            name = "search_products",
            description = "Search product catalog",
            inputSchema = ToolSchema(
                type = "object",
                properties = mapOf(
                    "query" to PropertySchema("string", "Search query"),
                    "category" to PropertySchema("string", "Product category"),
                    "max_price" to PropertySchema("number", "Maximum price")
                ),
                required = listOf("query")
            )
        )
    )

    suspend fun chatWithTools(userMessage: String): String {
        val messages = mutableListOf<ClaudeMessage>(
            ClaudeMessage("user", userMessage)
        )

        var request = ClaudeRequest(
            model = "claude-3-5-sonnet-20241022",
            maxTokens = 4096,
            messages = messages,
            tools = tools
        )

        var response = client.api.createMessage(request)

        // Handle tool use loop
        while (response.stopReason == "tool_use") {
            // Get tool call
            val toolUse = response.content.find { it.type == "tool_use" }!!

            Log.d("Claude", "Tool called: ${toolUse.name}, Args: ${toolUse.input}")

            // Execute tool
            val toolResult = when (toolUse.name) {
                "get_weather" -> executeGetWeather(toolUse.input!!)
                "search_products" -> executeSearchProducts(toolUse.input!!)
                else -> mapOf("error" to "Unknown tool")
            }

            // Add assistant response and tool result to messages
            messages.add(ClaudeMessage("assistant", response.content))
            messages.add(ClaudeMessage("user", listOf(
                mapOf(
                    "type" to "tool_result",
                    "tool_use_id" to toolUse.id,
                    "content" to toolResult.toString()
                )
            )))

            // Continue conversation
            request = request.copy(messages = messages)
            response = client.api.createMessage(request)
        }

        return response.content.first().text ?: ""
    }

    private fun executeGetWeather(args: Map<String, Any>): Map<String, Any> {
        val city = args["city"] as String
        return mapOf(
            "city" to city,
            "temperature" to 22,
            "condition" to "Sunny",
            "humidity" to 55
        )
    }

    private fun executeSearchProducts(args: Map<String, Any>): Map<String, Any> {
        return mapOf(
            "products" to listOf(
                mapOf("name" to "Product A", "price" to 19.99),
                mapOf("name" to "Product B", "price" to 29.99)
            )
        )
    }
}
```

---

## 5. Streaming Responses

```kotlin
class ClaudeStreamHelper(private val apiKey: String) {

    fun streamMessage(
        messages: List<ClaudeMessage>,
        onChunk: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val moshi = Moshi.Builder()
            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        val request = ClaudeRequest(
            model = "claude-3-5-sonnet-20241022",
            maxTokens = 4096,
            messages = messages,
            stream = true
        )

        val jsonAdapter = moshi.adapter(ClaudeRequest::class.java)
        val jsonBody = jsonAdapter.toJson(request)

        val httpRequest = okhttp3.Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        val client = OkHttpClient.Builder()
            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        val eventSourceFactory = EventSources.createFactory(client)
        eventSourceFactory.newEventSource(httpRequest, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val json = org.json.JSONObject(data)
                    val eventType = json.getString("type")

                    when (eventType) {
                        "content_block_delta" -> {
                            val delta = json.getJSONObject("delta")
                            if (delta.getString("type") == "text_delta") {
                                onChunk(delta.getString("text"))
                            }
                        }
                        "message_stop" -> onComplete()
                    }
                } catch (e: Exception) {
                    // Skip parse errors
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

## Complete ViewModel Example

```kotlin
class ClaudeChatViewModel : ViewModel() {

    // In production, get API key from your backend
    private val client = ClaudeClient(BuildConfig.CLAUDE_API_KEY)
    private val repository = ClaudeRepository(client)
    private val visionHelper = ClaudeVisionHelper(client)

    data class ChatMessage(val text: String, val isUser: Boolean)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _messages.value += ChatMessage(message, isUser = true)
            _isLoading.value = true
            try {
                val response = repository.sendMessage(message)
                _messages.value += ChatMessage(response, isUser = false)
            } catch (e: Exception) {
                _messages.value += ChatMessage("Error: ${e.message}", isUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analyzeImage(bitmap: Bitmap, prompt: String) {
        viewModelScope.launch {
            _messages.value += ChatMessage("[Image] $prompt", isUser = true)
            _isLoading.value = true
            try {
                val response = visionHelper.analyzeImage(bitmap, prompt)
                _messages.value += ChatMessage(response, isUser = false)
            } catch (e: Exception) {
                _messages.value += ChatMessage("Error: ${e.message}", isUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```
