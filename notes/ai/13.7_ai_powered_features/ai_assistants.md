# AI Assistants — AI-Powered Features

## Overview

AI Assistants provide conversational interfaces within Android apps — chatbots that understand intent, maintain context across multi-turn conversations, and take actions based on user requests.

```
AI Assistant Architecture:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                  User Message                          │ │
│  │  "Can you find me a flight to Tokyo next Friday?"     │ │
│  └─────────────────────────┬──────────────────────────────┘ │
│                            │                                │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              NLU (Intent Recognition)                │   │
│  │                                                      │   │
│  │  Intent:  SEARCH_FLIGHTS                             │   │
│  │  Entities:                                           │   │
│  │    destination = "Tokyo"                             │   │
│  │    date = "next Friday" → 2026-04-24                │   │
│  │  Confidence: 0.94                                    │   │
│  └─────────────────────────┬───────────────────────────┘   │
│                            │                                │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Dialog Manager                          │   │
│  │                                                      │   │
│  │  Context:                                            │   │
│  │    conversation_id: "abc123"                         │   │
│  │    turn: 3                                           │   │
│  │    slots_filled: {destination, date}                 │   │
│  │    slots_missing: {departure_city, passengers}       │   │
│  │                                                      │   │
│  │  Action: ASK_MISSING_SLOT (departure_city)           │   │
│  └─────────────────────────┬───────────────────────────┘   │
│                            │                                │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Response Generation                     │   │
│  │                                                      │   │
│  │  "I'd love to help! Where will you be flying from?" │   │
│  │                                                      │   │
│  │  Quick Replies: [New York] [Los Angeles] [Chicago]   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  Implementation Options:                                    │
│  ┌──────────────────┬──────────────────────────────────┐   │
│  │ Approach          │ Best For                         │   │
│  ├──────────────────┼──────────────────────────────────┤   │
│  │ Rule-based        │ Simple FAQs, known flows         │   │
│  │ Intent classif.   │ Structured tasks, finite intents │   │
│  │ LLM-powered       │ Open-ended, complex reasoning    │   │
│  │ Hybrid            │ Production: LLM + guardrails     │   │
│  └──────────────────┴──────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Gemini SDK (for LLM-powered assistant)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Retrofit (for custom API backend)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")

    // Room (conversation history)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}
```

---

## 1. Chatbot Integration

### Conversation Data Model

```kotlin
import androidx.room.*

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages", foreignKeys = [
    ForeignKey(entity = ConversationEntity::class, parentColumns = ["id"], childColumns = ["conversationId"])
])
data class MessageEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val conversationId: String,
    val role: String,         // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null  // JSON: intent, entities, etc.
)

data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val intent: String? = null,
    val quickReplies: List<String>? = null,
    val actions: List<AssistantAction>? = null
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}

data class AssistantAction(
    val type: String,       // "open_url", "call_api", "navigate", "show_card"
    val label: String,
    val payload: Map<String, String>
)

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    suspend fun getMessages(convId: String): List<MessageEntity>

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    suspend fun getConversations(): List<ConversationEntity>

    @Query("DELETE FROM conversations WHERE id = :convId")
    suspend fun deleteConversation(convId: String)
}
```

---

## 2. Context-Aware Responses

```
Multi-Turn Context Management:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Turn 1:                                                    │
│  User: "Show me Italian restaurants nearby"                 │
│  Context: {intent: SEARCH, cuisine: Italian, location: near}│
│                                                              │
│  Turn 2:                                                    │
│  User: "What about ones with outdoor seating?"              │
│  Context: {intent: FILTER, cuisine: Italian,                │
│            location: near, feature: outdoor}                │
│  ← "ones" resolved to "Italian restaurants" from Turn 1     │
│                                                              │
│  Turn 3:                                                    │
│  User: "Book the second one for 7pm"                        │
│  Context: {intent: BOOK, restaurant: result[1],            │
│            time: 19:00}                                      │
│  ← "second one" resolved from search results               │
│                                                              │
│  Context Window Strategy:                                   │
│  ┌──────────────────────────────────────────────────┐       │
│  │ System prompt:     Always included                │       │
│  │ Last N messages:   Sliding window (e.g., 20)     │       │
│  │ Summarized history: For long conversations       │       │
│  │ Entity memory:     Extracted facts persist        │       │
│  └──────────────────────────────────────────────────┘       │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class ConversationManager(
    private val dao: ConversationDao
) {
    // Active conversation context
    private val conversationContexts = mutableMapOf<String, ConversationContext>()

    data class ConversationContext(
        val conversationId: String,
        val messages: MutableList<Message> = mutableListOf(),
        val extractedEntities: MutableMap<String, String> = mutableMapOf(),
        val currentIntent: String? = null,
        val pendingSlots: MutableMap<String, String?> = mutableMapOf()
    )

    fun getOrCreateContext(conversationId: String): ConversationContext {
        return conversationContexts.getOrPut(conversationId) {
            ConversationContext(conversationId)
        }
    }

    // Build prompt with context for LLM
    fun buildContextualPrompt(
        conversationId: String,
        userMessage: String,
        systemPrompt: String,
        maxHistoryMessages: Int = 20
    ): List<Map<String, String>> {
        val context = getOrCreateContext(conversationId)

        val prompt = mutableListOf<Map<String, String>>()

        // System prompt with entity context
        val enrichedSystem = buildString {
            append(systemPrompt)
            if (context.extractedEntities.isNotEmpty()) {
                append("\n\nKnown context about this user/conversation:")
                context.extractedEntities.forEach { (key, value) ->
                    append("\n- $key: $value")
                }
            }
        }
        prompt.add(mapOf("role" to "system", "content" to enrichedSystem))

        // Recent message history
        val recentMessages = context.messages.takeLast(maxHistoryMessages)
        for (msg in recentMessages) {
            prompt.add(mapOf("role" to msg.role.name.lowercase(), "content" to msg.content))
        }

        // Current user message
        prompt.add(mapOf("role" to "user", "content" to userMessage))

        return prompt
    }

    // Add message and extract entities
    fun addMessage(conversationId: String, message: Message) {
        val context = getOrCreateContext(conversationId)
        context.messages.add(message)

        // Simple entity extraction from user messages
        if (message.role == Message.Role.USER) {
            extractEntities(message.content, context)
        }
    }

    private fun extractEntities(text: String, context: ConversationContext) {
        // Simple pattern-based extraction (replace with NER model in production)
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        emailRegex.find(text)?.let { context.extractedEntities["email"] = it.value }

        val phoneRegex = Regex("\\+?\\d[\\d\\s-]{8,}")
        phoneRegex.find(text)?.let { context.extractedEntities["phone"] = it.value.trim() }

        // Name detection (simple: "my name is X" or "I'm X")
        val nameRegex = Regex("(?:my name is|I'm|I am)\\s+([A-Z][a-z]+)", RegexOption.IGNORE_CASE)
        nameRegex.find(text)?.let { context.extractedEntities["user_name"] = it.groupValues[1] }
    }

    // Persist conversation
    suspend fun saveConversation(conversationId: String) {
        val context = conversationContexts[conversationId] ?: return
        dao.insertConversation(ConversationEntity(
            id = conversationId,
            title = context.messages.firstOrNull()?.content?.take(50) ?: "New Chat"
        ))
        context.messages.forEach { msg ->
            dao.insertMessage(MessageEntity(
                id = msg.id,
                conversationId = conversationId,
                role = msg.role.name.lowercase(),
                content = msg.content,
                timestamp = msg.timestamp
            ))
        }
    }
}
```

---

## 3. Multi-Turn Conversations with LLM

```kotlin
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class AIAssistant(
    apiKey: String,
    private val conversationManager: ConversationManager
) {
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private val systemPrompt = """You are a helpful in-app assistant. Follow these rules:
1. Be concise and friendly
2. If you need more info, ask one question at a time
3. When suggesting actions, use [ACTION:type:label:payload] format
4. Provide quick reply suggestions in [QUICK:option1|option2|option3] format
5. Never make up information you don't have
6. If unsure, say so and suggest alternatives"""

    // Chat with context
    suspend fun chat(
        conversationId: String,
        userMessage: String
    ): Message {
        // Add user message to context
        val userMsg = Message(role = Message.Role.USER, content = userMessage)
        conversationManager.addMessage(conversationId, userMsg)

        // Build contextual chat
        val context = conversationManager.getOrCreateContext(conversationId)

        val chat = model.startChat(
            history = context.messages.dropLast(1).map { msg ->
                content(role = if (msg.role == Message.Role.USER) "user" else "model") {
                    text(msg.content)
                }
            }
        )

        // Generate response
        val response = chat.sendMessage(userMessage)
        val responseText = response.text ?: "I'm sorry, I couldn't process that."

        // Parse response for actions and quick replies
        val parsedResponse = parseResponse(responseText)

        val assistantMsg = Message(
            role = Message.Role.ASSISTANT,
            content = parsedResponse.cleanText,
            quickReplies = parsedResponse.quickReplies,
            actions = parsedResponse.actions
        )

        conversationManager.addMessage(conversationId, assistantMsg)
        return assistantMsg
    }

    // Streaming response
    suspend fun chatStream(
        conversationId: String,
        userMessage: String,
        onChunk: (String) -> Unit
    ): Message {
        val userMsg = Message(role = Message.Role.USER, content = userMessage)
        conversationManager.addMessage(conversationId, userMsg)

        val context = conversationManager.getOrCreateContext(conversationId)

        val chat = model.startChat(
            history = context.messages.dropLast(1).map { msg ->
                content(role = if (msg.role == Message.Role.USER) "user" else "model") {
                    text(msg.content)
                }
            }
        )

        val fullResponse = StringBuilder()
        val stream = chat.sendMessageStream(userMessage)

        stream.collect { chunk ->
            chunk.text?.let { text ->
                fullResponse.append(text)
                onChunk(text)
            }
        }

        val parsedResponse = parseResponse(fullResponse.toString())
        val assistantMsg = Message(
            role = Message.Role.ASSISTANT,
            content = parsedResponse.cleanText,
            quickReplies = parsedResponse.quickReplies,
            actions = parsedResponse.actions
        )

        conversationManager.addMessage(conversationId, assistantMsg)
        return assistantMsg
    }

    private fun parseResponse(text: String): ParsedResponse {
        val actions = mutableListOf<AssistantAction>()
        val quickReplies = mutableListOf<String>()
        var cleanText = text

        // Extract actions: [ACTION:type:label:payload]
        val actionRegex = Regex("\\[ACTION:(\\w+):([^:]+):([^\\]]+)]")
        actionRegex.findAll(text).forEach { match ->
            actions.add(AssistantAction(
                type = match.groupValues[1],
                label = match.groupValues[2],
                payload = mapOf("data" to match.groupValues[3])
            ))
            cleanText = cleanText.replace(match.value, "")
        }

        // Extract quick replies: [QUICK:option1|option2|option3]
        val quickRegex = Regex("\\[QUICK:([^\\]]+)]")
        quickRegex.find(text)?.let { match ->
            quickReplies.addAll(match.groupValues[1].split("|").map { it.trim() })
            cleanText = cleanText.replace(match.value, "")
        }

        return ParsedResponse(cleanText.trim(), actions, quickReplies.ifEmpty { null })
    }

    data class ParsedResponse(
        val cleanText: String,
        val actions: List<AssistantAction>,
        val quickReplies: List<String>?
    )
}
```

---

## 4. Intent Recognition

```
Intent Recognition Flow:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Input: "Set an alarm for 7 AM tomorrow"                    │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Intent Classification                                 │   │
│  │                                                       │   │
│  │ Intents:                                              │   │
│  │  SET_ALARM:    0.92 ← Winner                         │   │
│  │  SET_REMINDER: 0.05                                   │   │
│  │  CHECK_TIME:   0.02                                   │   │
│  │  OTHER:        0.01                                   │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Entity/Slot Extraction                                │   │
│  │                                                       │   │
│  │ "Set an alarm for [7 AM](time) [tomorrow](date)"    │   │
│  │                                                       │   │
│  │ Slots:                                                │   │
│  │  time = "07:00"                                       │   │
│  │  date = "2026-04-16"                                  │   │
│  │  recurring = null (not specified)                     │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Fulfillment                                           │   │
│  │                                                       │   │
│  │ All required slots filled → Execute action            │   │
│  │ Missing slots → Ask user to fill                     │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class IntentRecognizer {

    // Define intents with example phrases
    data class IntentDefinition(
        val name: String,
        val examples: List<String>,
        val requiredSlots: List<String> = emptyList(),
        val handler: String  // Name of handler function
    )

    private val intents = listOf(
        IntentDefinition(
            name = "SEARCH_PRODUCT",
            examples = listOf(
                "find me a", "search for", "show me", "looking for",
                "I want to buy", "where can I find"
            ),
            requiredSlots = listOf("query"),
            handler = "handleProductSearch"
        ),
        IntentDefinition(
            name = "CHECK_ORDER",
            examples = listOf(
                "where is my order", "track my package", "order status",
                "when will my order arrive", "shipping status"
            ),
            requiredSlots = listOf("order_id"),
            handler = "handleOrderCheck"
        ),
        IntentDefinition(
            name = "GET_HELP",
            examples = listOf(
                "help", "I need help", "support", "contact support",
                "talk to agent", "customer service"
            ),
            handler = "handleGetHelp"
        ),
        IntentDefinition(
            name = "GREETING",
            examples = listOf("hi", "hello", "hey", "good morning", "what's up"),
            handler = "handleGreeting"
        ),
        IntentDefinition(
            name = "GOODBYE",
            examples = listOf("bye", "goodbye", "see you", "thanks bye", "that's all"),
            handler = "handleGoodbye"
        )
    )

    // Simple keyword-based intent matching (replace with ML model in production)
    fun recognizeIntent(text: String): IntentResult {
        val lowerText = text.lowercase()
        var bestIntent: IntentDefinition? = null
        var bestScore = 0f

        for (intent in intents) {
            val matchCount = intent.examples.count { example ->
                lowerText.contains(example.lowercase())
            }
            val score = matchCount.toFloat() / intent.examples.size

            if (score > bestScore) {
                bestScore = score
                bestIntent = intent
            }
        }

        // Confidence threshold
        if (bestScore < 0.1f) {
            return IntentResult("UNKNOWN", 0f, emptyMap(), emptyList())
        }

        // Extract slots
        val slots = extractSlots(lowerText, bestIntent!!)
        val missingSlots = bestIntent.requiredSlots.filter { it !in slots }

        return IntentResult(
            intent = bestIntent.name,
            confidence = bestScore.coerceAtMost(1f),
            slots = slots,
            missingSlots = missingSlots
        )
    }

    private fun extractSlots(text: String, intent: IntentDefinition): Map<String, String> {
        val slots = mutableMapOf<String, String>()

        // Order ID extraction
        val orderRegex = Regex("#?(\\d{6,})|(?:order|tracking)[\\s#]*([A-Z0-9]{6,})", RegexOption.IGNORE_CASE)
        orderRegex.find(text)?.let {
            slots["order_id"] = (it.groupValues[1].ifEmpty { it.groupValues[2] })
        }

        // For search, everything after the intent keyword is the query
        if (intent.name == "SEARCH_PRODUCT") {
            for (example in intent.examples) {
                val idx = text.indexOf(example.lowercase())
                if (idx >= 0) {
                    val query = text.substring(idx + example.length).trim()
                    if (query.isNotEmpty()) {
                        slots["query"] = query
                    }
                    break
                }
            }
        }

        return slots
    }

    data class IntentResult(
        val intent: String,
        val confidence: Float,
        val slots: Map<String, String>,
        val missingSlots: List<String>
    ) {
        val isComplete: Boolean get() = missingSlots.isEmpty()
    }
}

// Intent-based dialog handler
class DialogHandler(
    private val intentRecognizer: IntentRecognizer,
    private val aiAssistant: AIAssistant
) {
    // Process message: try intent first, fall back to LLM
    suspend fun processMessage(conversationId: String, userMessage: String): Message {
        val intentResult = intentRecognizer.recognizeIntent(userMessage)

        // High-confidence structured intent → handle directly
        if (intentResult.confidence > 0.5f && intentResult.intent != "UNKNOWN") {
            return handleStructuredIntent(intentResult, conversationId)
        }

        // Low confidence or unknown → delegate to LLM
        return aiAssistant.chat(conversationId, userMessage)
    }

    private suspend fun handleStructuredIntent(
        intent: IntentRecognizer.IntentResult,
        conversationId: String
    ): Message {
        // Check if all slots are filled
        if (!intent.isComplete) {
            val missingSlot = intent.missingSlots.first()
            val promptText = when (missingSlot) {
                "order_id" -> "Could you please provide your order number?"
                "query" -> "What are you looking for?"
                else -> "Could you tell me your $missingSlot?"
            }

            return Message(
                role = Message.Role.ASSISTANT,
                content = promptText,
                intent = intent.intent
            )
        }

        // All slots filled — execute action
        return when (intent.intent) {
            "SEARCH_PRODUCT" -> Message(
                role = Message.Role.ASSISTANT,
                content = "Here are results for '${intent.slots["query"]}':",
                actions = listOf(AssistantAction("navigate", "View Results",
                    mapOf("screen" to "search", "query" to (intent.slots["query"] ?: ""))))
            )
            "CHECK_ORDER" -> Message(
                role = Message.Role.ASSISTANT,
                content = "Let me look up order #${intent.slots["order_id"]}...",
                actions = listOf(AssistantAction("call_api", "Track Order",
                    mapOf("order_id" to (intent.slots["order_id"] ?: ""))))
            )
            "GREETING" -> Message(
                role = Message.Role.ASSISTANT,
                content = "Hi there! How can I help you today?",
                quickReplies = listOf("Search products", "Track order", "Get help")
            )
            "GOODBYE" -> Message(
                role = Message.Role.ASSISTANT,
                content = "Goodbye! Feel free to come back anytime."
            )
            else -> aiAssistant.chat(conversationId, "User intent: ${intent.intent}")
        }
    }
}
```

---

## Complete ViewModel

```kotlin
class AIAssistantViewModel(
    private val dialogHandler: DialogHandler,
    private val conversationManager: ConversationManager
) : ViewModel() {

    private val conversationId = java.util.UUID.randomUUID().toString()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun sendMessage(text: String) {
        // Add user message immediately
        val userMessage = Message(role = Message.Role.USER, content = text)
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            _isTyping.value = true
            try {
                val response = dialogHandler.processMessage(conversationId, text)
                _messages.value = _messages.value + response
            } catch (e: Exception) {
                val errorMsg = Message(
                    role = Message.Role.ASSISTANT,
                    content = "Sorry, I encountered an error. Please try again."
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isTyping.value = false
            }
        }
    }

    fun selectQuickReply(reply: String) = sendMessage(reply)

    fun executeAction(action: AssistantAction) {
        // Handle different action types
        when (action.type) {
            "navigate" -> { /* Navigate to screen */ }
            "open_url" -> { /* Open URL */ }
            "call_api" -> { /* Make API call and show result */ }
        }
    }

    fun clearConversation() {
        _messages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            conversationManager.saveConversation(conversationId)
        }
    }
}
```
