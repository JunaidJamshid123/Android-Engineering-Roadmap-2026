# Gemini API (Google AI) — Generative AI Integration

## Overview

Gemini is Google's most capable AI model family, offering text generation, multimodal understanding, and function calling. The Google AI SDK provides direct integration for Android apps.

```
Gemini Model Family (2026):
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  Gemini Ultra    ─ Most capable, complex reasoning       │
│  Gemini Pro      ─ Best balance of capability & speed    │
│  Gemini Flash    ─ Fastest, most cost-effective          │
│  Gemini Nano     ─ On-device, privacy-first              │
│                                                          │
│  Capabilities:                                           │
│  ┌────────────────────────────────────────────────────┐  │
│  │ Text Generation  │ Image Understanding │ Code Gen  │  │
│  │ Chat/Dialog      │ Video Understanding │ Reasoning │  │
│  │ Summarization    │ Audio Understanding │ Math      │  │
│  │ Translation      │ Document Analysis   │ Planning  │  │
│  │ Function Calling │ Grounding           │ Safety    │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  Architecture:                                           │
│  ┌──────────────────────────────────────────────────┐    │
│  │             Android App                           │    │
│  │  ┌──────────────────────────────────────────┐    │    │
│  │  │     Google AI SDK for Android             │    │    │
│  │  │     (com.google.ai.client.generativeai)   │    │    │
│  │  └──────────────────┬───────────────────────┘    │    │
│  └─────────────────────┼────────────────────────────┘    │
│                        │ HTTPS/REST                      │
│  ┌─────────────────────▼────────────────────────────┐    │
│  │            Google AI API Servers                  │    │
│  │         (generativelanguage.googleapis.com)        │    │
│  └──────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Google AI SDK for Android (Gemini)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    
    // Kotlin Coroutines (required for streaming)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

---

## 1. Text Generation and Chat

### Theory

```
Text Generation Types:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  1. SINGLE PROMPT (generateContent)                  │
│     User ──▶ Model ──▶ Complete Response             │
│                                                      │
│  2. STREAMING (generateContentStream)                │
│     User ──▶ Model ──▶ Token1 Token2 Token3 ...     │
│                        (real-time, chunk by chunk)   │
│                                                      │
│  3. MULTI-TURN CHAT (startChat)                      │
│     User: "Hi"                                       │
│     Model: "Hello! How can I help?"                  │
│     User: "Explain AI"                               │
│     Model: "AI is..." (has full conversation context)│
│                                                      │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*

class GeminiHelper {

    // Initialize the model
    // IMPORTANT: In production, use BuildConfig or server-side proxy for API key
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY  // Never hardcode API keys!
    )

    // --- 1. Simple Text Generation ---
    suspend fun generateText(prompt: String): String {
        val response = model.generateContent(prompt)
        return response.text ?: "No response"
    }

    // Usage
    suspend fun example() {
        val result = generateText("Explain quantum computing in simple terms")
        Log.d("Gemini", result)
    }

    // --- 2. Streaming Response ---
    suspend fun generateTextStream(
        prompt: String,
        onChunk: (String) -> Unit
    ) {
        val responseStream = model.generateContentStream(prompt)

        responseStream.collect { chunk ->
            chunk.text?.let { text ->
                onChunk(text)  // Called for each chunk
            }
        }
    }

    // Usage with UI
    suspend fun streamExample(textView: android.widget.TextView) {
        val fullResponse = StringBuilder()

        generateTextStream("Write a short story about a robot") { chunk ->
            fullResponse.append(chunk)
            // Update UI on main thread
            textView.post {
                textView.text = fullResponse.toString()
            }
        }
    }

    // --- 3. Multi-turn Chat ---
    private val chat = model.startChat(
        history = listOf(
            content(role = "user") { text("You are a helpful Android development assistant.") },
            content(role = "model") { text("I'm ready to help with Android development! Ask me anything.") }
        )
    )

    suspend fun sendChatMessage(message: String): String {
        val response = chat.sendMessage(message)
        return response.text ?: "No response"
    }

    suspend fun sendChatMessageStream(
        message: String,
        onChunk: (String) -> Unit
    ) {
        val responseStream = chat.sendMessageStream(message)
        responseStream.collect { chunk ->
            chunk.text?.let { onChunk(it) }
        }
    }

    // Chat with history
    suspend fun chatExample() {
        val r1 = sendChatMessage("What is Jetpack Compose?")
        Log.d("Chat", "Response 1: $r1")

        val r2 = sendChatMessage("Show me a simple example")
        Log.d("Chat", "Response 2: $r2")  // Model remembers context

        // Access full chat history
        val history = chat.history
        history.forEach { content ->
            Log.d("Chat", "${content.role}: ${content.parts.joinToString()}")
        }
    }
}
```

---

## 2. Multimodal Understanding (Text + Images)

### Theory

```
Multimodal Input:
┌────────────────────────────────────────────────────┐
│                                                    │
│  Gemini Pro Vision / Gemini Pro (multimodal)       │
│                                                    │
│  ┌──────────┐                                      │
│  │  Image   │──┐                                   │
│  │  (Bitmap)│  │                                   │
│  └──────────┘  │    ┌──────────────┐               │
│                ├───▶│   Gemini     │──▶ Response    │
│  ┌──────────┐  │    │   Model     │               │
│  │  Text    │──┘    └──────────────┘               │
│  │ (Prompt) │                                      │
│  └──────────┘                                      │
│                                                    │
│  Supported Inputs:                                 │
│  ✅ Images (JPEG, PNG, WebP, HEIC, HEIF)           │
│  ✅ Text prompts                                   │
│  ✅ Multiple images in one request                 │
│  ✅ PDF documents                                  │
│  ✅ Video clips                                    │
│  ❌ Audio (use Gemini 1.5+ for audio)              │
│                                                    │
│  Max image size: 20MB per image                    │
│  Max images: 16 per request (Gemini Pro Vision)    │
└────────────────────────────────────────────────────┘
```

### Code

```kotlin
class GeminiMultimodalHelper {

    // Use gemini-pro-vision for image+text
    private val multimodalModel = GenerativeModel(
        modelName = "gemini-pro-vision",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // --- Image + Text ---
    suspend fun analyzeImage(bitmap: Bitmap, prompt: String): String {
        val content = content {
            image(bitmap)
            text(prompt)
        }

        val response = multimodalModel.generateContent(content)
        return response.text ?: "No response"
    }

    // Usage
    suspend fun imageExamples(bitmap: Bitmap) {
        // Describe image
        val description = analyzeImage(bitmap, "Describe this image in detail")
        Log.d("Gemini", "Description: $description")

        // Extract text from image
        val ocrResult = analyzeImage(bitmap, "Extract all text visible in this image")
        Log.d("Gemini", "OCR: $ocrResult")

        // Analyze UI screenshot
        val uiAnalysis = analyzeImage(bitmap, 
            "Analyze this mobile UI screenshot. Identify UI elements and suggest improvements")
        Log.d("Gemini", "UI Analysis: $uiAnalysis")
    }

    // --- Multiple Images ---
    suspend fun compareImages(bitmap1: Bitmap, bitmap2: Bitmap): String {
        val content = content {
            image(bitmap1)
            image(bitmap2)
            text("Compare these two images. What are the similarities and differences?")
        }

        val response = multimodalModel.generateContent(content)
        return response.text ?: "No response"
    }

    // --- Image from URI ---
    suspend fun analyzeImageFromUri(context: Context, uri: Uri, prompt: String): String {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        return analyzeImage(bitmap, prompt)
    }
}
```

---

## 3. Streaming Responses

### Theory

```
Streaming vs Non-Streaming:

Non-Streaming:
┌──────┐         ┌─────────────────┐         ┌──────────────────────┐
│Client│────────▶│   Processing    │────────▶│ Complete Response    │
│      │         │   (3-10 sec)    │         │ "The answer is..."   │
└──────┘         └─────────────────┘         └──────────────────────┘
  Wait...           All at once                 Time: 5 sec total

Streaming:
┌──────┐         ┌─────────┐
│Client│────────▶│ Process │
│      │◀────────│         │──▶ "The"        (200ms)
│      │◀────────│         │──▶ " answer"    (400ms)
│      │◀────────│         │──▶ " is..."     (600ms)
│      │◀────────│         │──▶ [DONE]       (800ms)
└──────┘         └─────────┘
  Immediate feedback!          Time to first token: 200ms
```

### Code

```kotlin
class GeminiStreamHelper {

    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // Streaming with Kotlin Flow
    suspend fun streamResponse(prompt: String): kotlinx.coroutines.flow.Flow<String> {
        return kotlinx.coroutines.flow.flow {
            val stream = model.generateContentStream(prompt)
            stream.collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        }
    }

    // Streaming in a ViewModel
    class ChatViewModel : ViewModel() {
        private val model = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )

        private val _response = MutableStateFlow("")
        val response: StateFlow<String> = _response.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        fun generateResponse(prompt: String) {
            viewModelScope.launch {
                _isLoading.value = true
                _response.value = ""

                try {
                    val stream = model.generateContentStream(prompt)
                    stream.collect { chunk ->
                        chunk.text?.let { text ->
                            _response.value += text
                        }
                    }
                } catch (e: Exception) {
                    _response.value = "Error: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    // Jetpack Compose UI
    @Composable
    fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
        val response by viewModel.response.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        var prompt by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Response area
            Text(
                text = response,
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // Input area
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask something...") }
                )
                Button(
                    onClick = {
                        viewModel.generateResponse(prompt)
                        prompt = ""
                    },
                    enabled = !isLoading && prompt.isNotEmpty()
                ) {
                    Text("Send")
                }
            }
        }
    }
}
```

---

## 4. Function Calling for Tool Use

### Theory

Function calling lets Gemini invoke functions you define, enabling the model to interact with external systems.

```
Function Calling Flow:
┌──────┐    ┌────────────────┐    ┌──────────────────┐
│ User │───▶│ "What's the    │───▶│ Gemini Model     │
│      │    │  weather in    │    │                  │
│      │    │  Tokyo?"       │    │ Decides to call: │
│      │    └────────────────┘    │ getWeather(       │
│      │                         │   city="Tokyo")   │
│      │                         └────────┬──────────┘
│      │                                  │
│      │    ┌────────────────┐            │ Function Call
│      │    │ Your App       │◀───────────┘
│      │    │ Executes:      │
│      │    │ getWeather(    │
│      │    │   "Tokyo")     │
│      │    │ Returns: 22°C  │
│      │    └───────┬────────┘
│      │            │ Function Response
│      │    ┌───────▼────────┐    ┌──────────────────┐
│      │◀───│ Gemini Model   │◀───│ "It's currently  │
│      │    │ Formats answer │    │  22°C in Tokyo"  │
│      │    └────────────────┘    └──────────────────┘
└──────┘
```

### Code

```kotlin
import com.google.ai.client.generativeai.type.*

class GeminiFunctionCallingHelper {

    // Step 1: Define functions the model can call
    private val getWeatherFunction = defineFunction(
        name = "getWeather",
        description = "Get the current weather for a given city",
        parameters = listOf(
            Schema.str("city", "The city name, e.g., 'Tokyo'"),
            Schema.enum("unit", "Temperature unit", listOf("celsius", "fahrenheit"))
        )
    )

    private val searchDatabaseFunction = defineFunction(
        name = "searchDatabase",
        description = "Search the product database for items matching a query",
        parameters = listOf(
            Schema.str("query", "Search query string"),
            Schema.int("maxResults", "Maximum number of results to return"),
            Schema.str("category", "Product category filter")
        )
    )

    // Step 2: Create model with tools
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY,
        tools = listOf(
            Tool(listOf(getWeatherFunction, searchDatabaseFunction))
        )
    )

    // Step 3: Handle function calls
    suspend fun chat(userMessage: String): String {
        val chat = model.startChat()
        var response = chat.sendMessage(userMessage)

        // Check if model wants to call a function
        while (true) {
            val functionCall = response.functionCalls.firstOrNull() ?: break

            Log.d("FunctionCall", "Model called: ${functionCall.name}")
            Log.d("FunctionCall", "Args: ${functionCall.args}")

            // Execute the function
            val functionResult = when (functionCall.name) {
                "getWeather" -> executeGetWeather(
                    city = functionCall.args["city"] as String,
                    unit = functionCall.args["unit"] as? String ?: "celsius"
                )
                "searchDatabase" -> executeSearchDatabase(
                    query = functionCall.args["query"] as String,
                    maxResults = (functionCall.args["maxResults"] as? Double)?.toInt() ?: 10,
                    category = functionCall.args["category"] as? String
                )
                else -> mapOf("error" to "Unknown function")
            }

            // Send function result back to model
            response = chat.sendMessage(
                content(role = "function") {
                    part(FunctionResponsePart(functionCall.name, functionResult))
                }
            )
        }

        return response.text ?: "No response"
    }

    // Function implementations
    private suspend fun executeGetWeather(city: String, unit: String): Map<String, Any> {
        // In real app, call a weather API
        return mapOf(
            "city" to city,
            "temperature" to 22,
            "unit" to unit,
            "condition" to "Sunny",
            "humidity" to 65
        )
    }

    private suspend fun executeSearchDatabase(
        query: String,
        maxResults: Int,
        category: String?
    ): Map<String, Any> {
        // In real app, query your database
        return mapOf(
            "results" to listOf(
                mapOf("name" to "Product A", "price" to 29.99),
                mapOf("name" to "Product B", "price" to 49.99)
            ),
            "totalCount" to 2
        )
    }
}
```

---

## 5. Context Management

### Theory

```
Context Window:
┌──────────────────────────────────────────────────────┐
│ Gemini Pro:     ~32K tokens                          │
│ Gemini 1.5 Pro: ~1M tokens (!)                       │
│ Gemini 1.5 Flash: ~1M tokens                        │
│                                                      │
│ Token ≈ 4 characters (English)                       │
│ 1M tokens ≈ ~700 pages of text                       │
│                                                      │
│ Context Management Strategies:                       │
│ ┌──────────────────────────────────────────────────┐ │
│ │ 1. System Instructions: Set behavior/persona    │ │
│ │ 2. Chat History: Maintain conversation context  │ │
│ │ 3. Token Counting: Track usage                  │ │
│ │ 4. Sliding Window: Drop old messages           │ │
│ │ 5. Summarization: Compress old context          │ │
│ └──────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class GeminiContextHelper {

    // --- System Instructions (persona/behavior) ---
    private val modelWithSystem = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text("""
                You are a helpful Android development tutor.
                - Answer in concise, clear language
                - Provide Kotlin code examples when relevant
                - Focus on Jetpack Compose and modern Android practices
                - If unsure, say so rather than guessing
            """.trimIndent())
        }
    )

    // --- Token Counting ---
    suspend fun countTokens(prompt: String): Int {
        val model = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
        val tokenCount = model.countTokens(prompt)
        return tokenCount.totalTokens
    }

    suspend fun countChatTokens() {
        val model = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
        val chat = model.startChat()

        chat.sendMessage("What is Android?")
        chat.sendMessage("Tell me about Jetpack Compose")

        val historyTokens = model.countTokens(*chat.history.toTypedArray())
        Log.d("Tokens", "Chat history tokens: ${historyTokens.totalTokens}")
    }

    // --- Context Window Management ---
    class ManagedChat(
        private val model: GenerativeModel,
        private val maxHistorySize: Int = 20
    ) {
        private val chatHistory = mutableListOf<Content>()

        suspend fun sendMessage(message: String): String {
            // Create chat with current history
            val chat = model.startChat(history = chatHistory.toList())

            val response = chat.sendMessage(message)
            val responseText = response.text ?: ""

            // Append to our managed history
            chatHistory.add(content(role = "user") { text(message) })
            chatHistory.add(content(role = "model") { text(responseText) })

            // Trim old messages if too long (keep system + last N)
            if (chatHistory.size > maxHistorySize) {
                val toRemove = chatHistory.size - maxHistorySize
                chatHistory.subList(0, toRemove).clear()
            }

            return responseText
        }

        fun clearHistory() = chatHistory.clear()
    }
}
```

---

## 6. Safety Settings and Content Filtering

### Theory

```
Safety Categories:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  HARASSMENT         │ Content targeting identity      │
│  HATE_SPEECH        │ Hateful content                 │
│  SEXUALLY_EXPLICIT  │ Sexual content                  │
│  DANGEROUS_CONTENT  │ Self-harm, weapons, etc.        │
│                                                      │
│  Threshold Levels:                                   │
│  ┌─────────────────────────────────────────────────┐ │
│  │ BLOCK_NONE        │ Allow all content           │ │
│  │ BLOCK_ONLY_HIGH   │ Block only high probability │ │
│  │ BLOCK_MEDIUM_AND_ABOVE │ Block medium+          │ │
│  │ BLOCK_LOW_AND_ABOVE    │ Block most (default)   │ │
│  └─────────────────────────────────────────────────┘ │
│                                                      │
│  Response Safety Ratings:                            │
│  Each response includes ratings for each category:   │
│  - category: HARASSMENT                              │
│  - probability: LOW / MEDIUM / HIGH                  │
│  - blocked: true/false                               │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting

class GeminiSafetyHelper {

    // Configure safety settings
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.LOW_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
        )
    )

    suspend fun generateSafely(prompt: String): String {
        return try {
            val response = model.generateContent(prompt)

            // Check safety ratings
            response.candidates.firstOrNull()?.safetyRatings?.forEach { rating ->
                Log.d("Safety", "${rating.category}: ${rating.probability}")
            }

            // Check if response was blocked
            val finishReason = response.candidates.firstOrNull()?.finishReason
            if (finishReason == FinishReason.SAFETY) {
                return "Response blocked due to safety filters."
            }

            response.text ?: "No response"
        } catch (e: com.google.ai.client.generativeai.type.GoogleGenerativeAIException) {
            "Error: ${e.message}"
        }
    }

    // Check prompt before sending
    suspend fun validatePrompt(prompt: String): Boolean {
        return try {
            val response = model.generateContent(prompt)
            response.candidates.firstOrNull()?.finishReason != FinishReason.SAFETY
        } catch (e: Exception) {
            false
        }
    }
}
```

---

## 7. Complete SDK Integration in Android

### Full ViewModel + Compose Example

```kotlin
// ViewModel
class GeminiChatViewModel : ViewModel() {
    
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f       // Creativity (0.0 - 1.0)
            topK = 40                // Top-K sampling
            topP = 0.95f             // Nucleus sampling
            maxOutputTokens = 2048   // Max response length
            stopSequences = listOf("---")  // Stop generation at these
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE)
        ),
        systemInstruction = content {
            text("You are a helpful Android development assistant.")
        }
    )

    private val chat = model.startChat()

    data class ChatMessage(
        val text: String,
        val isUser: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            // Add user message
            _messages.value += ChatMessage(userMessage, isUser = true)
            _isLoading.value = true

            try {
                val responseBuilder = StringBuilder()
                // Add placeholder for AI response
                _messages.value += ChatMessage("", isUser = false)

                // Stream response
                val stream = chat.sendMessageStream(userMessage)
                stream.collect { chunk ->
                    chunk.text?.let { text ->
                        responseBuilder.append(text)
                        // Update last message
                        val updatedMessages = _messages.value.toMutableList()
                        updatedMessages[updatedMessages.lastIndex] = ChatMessage(
                            responseBuilder.toString(),
                            isUser = false
                        )
                        _messages.value = updatedMessages
                    }
                }
            } catch (e: Exception) {
                _messages.value += ChatMessage("Error: ${e.message}", isUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Send image + text
    fun sendImageMessage(bitmap: Bitmap, prompt: String) {
        viewModelScope.launch {
            _messages.value += ChatMessage("[Image] $prompt", isUser = true)
            _isLoading.value = true

            try {
                val visionModel = GenerativeModel(
                    modelName = "gemini-pro-vision",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )

                val content = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = visionModel.generateContent(content)
                _messages.value += ChatMessage(response.text ?: "No response", isUser = false)
            } catch (e: Exception) {
                _messages.value += ChatMessage("Error: ${e.message}", isUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// Compose UI
@Composable
fun GeminiChatScreen(viewModel: GeminiChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(16.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                ChatBubble(message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(input.trim())
                        input = ""
                    }
                },
                enabled = !isLoading && input.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatBubble(message: GeminiChatViewModel.ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) 
        MaterialTheme.colorScheme.primaryContainer 
    else 
        MaterialTheme.colorScheme.secondaryContainer

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.align(alignment).widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```
