# Cloud NLP Services — Natural Language Processing

## Overview

Cloud NLP services provide powerful text analysis powered by large-scale models hosted on cloud infrastructure. They offer higher accuracy than on-device models at the cost of requiring network connectivity.

```
Cloud NLP Architecture:
┌──────────────────────────────────────────────────────┐
│                 Android App                           │
│  ┌────────────────────────────────────────────────┐  │
│  │   Text Input → Retrofit API Call                │  │
│  └─────────────────────┬──────────────────────────┘  │
└────────────────────────┼─────────────────────────────┘
                         │ HTTPS
┌────────────────────────▼─────────────────────────────┐
│          Google Cloud Natural Language API            │
│  ┌──────────────────────────────────────────────┐    │
│  │ • Entity Analysis                             │    │
│  │ • Sentiment Analysis                          │    │
│  │ • Syntax Analysis                             │    │
│  │ • Content Classification                      │    │
│  │ • Entity Sentiment Analysis                   │    │
│  │ • Custom Entity Extraction                    │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  On-Device vs Cloud NLP:                             │
│  ┌──────────────────┬──────────┬──────────┐         │
│  │ Feature          │ On-Device│ Cloud    │         │
│  ├──────────────────┼──────────┼──────────┤         │
│  │ Accuracy         │ Good     │ Excellent│         │
│  │ Offline          │ ✅       │ ❌       │         │
│  │ Latency          │ ~50ms    │ ~200ms+  │         │
│  │ Privacy          │ ✅       │ ❌       │         │
│  │ Languages        │ Limited  │ 100+     │         │
│  │ Cost             │ Free     │ Pay/use  │         │
│  │ Model updates    │ Manual   │ Auto     │         │
│  │ Complex analysis │ Limited  │ Rich     │         │
│  └──────────────────┴──────────┴──────────┘         │
└──────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Google Cloud client library
    implementation("com.google.cloud:google-cloud-language:2.31.0")
    
    // OR use REST API with Retrofit (lighter weight)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

---

## 1. Google Cloud Natural Language API

### Theory

```
API Endpoints:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  1. analyzeEntities                                  │
│     Finds named entities (people, orgs, locations)   │
│     + Wikipedia links, salience scores               │
│                                                      │
│  2. analyzeSentiment                                 │
│     Document-level and sentence-level sentiment      │
│     Score: -1.0 (negative) to 1.0 (positive)        │
│     Magnitude: 0.0 to ∞ (strength of emotion)       │
│                                                      │
│  3. analyzeSyntax                                    │
│     Part-of-speech tags, dependency parse tree        │
│     Morphological features, lemmas                   │
│                                                      │
│  4. classifyText                                     │
│     700+ content categories                          │
│     e.g., /Computers & Electronics/Software          │
│                                                      │
│  5. analyzeEntitySentiment                           │
│     Entities + sentiment per entity                  │
│                                                      │
│  6. annotateText                                     │
│     All of the above in one API call                 │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### Code — REST API with Retrofit

```kotlin
// --- Data Models ---
@JsonClass(generateAdapter = true)
data class NLPRequest(
    val document: DocumentInput,
    val encodingType: String = "UTF8",
    val features: Features? = null  // For annotateText
)

@JsonClass(generateAdapter = true)
data class DocumentInput(
    val type: String = "PLAIN_TEXT",  // or "HTML"
    val content: String,
    val language: String? = null     // Auto-detect if null
)

@JsonClass(generateAdapter = true)
data class Features(
    val extractSyntax: Boolean = false,
    val extractEntities: Boolean = false,
    val extractDocumentSentiment: Boolean = false,
    val extractEntitySentiment: Boolean = false,
    val classifyText: Boolean = false
)

// Entity response
@JsonClass(generateAdapter = true)
data class EntityResponse(val entities: List<EntityResult>)

@JsonClass(generateAdapter = true)
data class EntityResult(
    val name: String,
    val type: String,           // PERSON, LOCATION, ORGANIZATION, EVENT, etc.
    val salience: Float,        // 0.0 to 1.0 (importance)
    val metadata: Map<String, String>?,  // Wikipedia URL, etc.
    val mentions: List<EntityMention>
)

@JsonClass(generateAdapter = true)
data class EntityMention(
    val text: TextSpan,
    val type: String   // PROPER, COMMON
)

@JsonClass(generateAdapter = true)
data class TextSpan(
    val content: String,
    val beginOffset: Int
)

// Sentiment response
@JsonClass(generateAdapter = true)
data class SentimentResponse(
    val documentSentiment: Sentiment,
    val sentences: List<SentenceSentiment>
)

@JsonClass(generateAdapter = true)
data class Sentiment(
    val score: Float,      // -1.0 to 1.0
    val magnitude: Float   // 0.0 to ∞
)

@JsonClass(generateAdapter = true)
data class SentenceSentiment(
    val text: TextSpan,
    val sentiment: Sentiment
)

// Syntax response
@JsonClass(generateAdapter = true)
data class SyntaxResponse(
    val sentences: List<SyntaxSentence>,
    val tokens: List<SyntaxToken>
)

@JsonClass(generateAdapter = true)
data class SyntaxSentence(val text: TextSpan)

@JsonClass(generateAdapter = true)
data class SyntaxToken(
    val text: TextSpan,
    val partOfSpeech: PartOfSpeech,
    val dependencyEdge: DependencyEdge,
    val lemma: String
)

@JsonClass(generateAdapter = true)
data class PartOfSpeech(
    val tag: String  // NOUN, VERB, ADJ, ADV, PRON, DET, etc.
)

@JsonClass(generateAdapter = true)
data class DependencyEdge(
    val headTokenIndex: Int,
    val label: String  // ROOT, NSUBJ, DOBJ, AMOD, etc.
)

// Classification response
@JsonClass(generateAdapter = true)
data class ClassificationResponse(
    val categories: List<CategoryResult>
)

@JsonClass(generateAdapter = true)
data class CategoryResult(
    val name: String,       // e.g., "/Science/Computer Science"
    val confidence: Float   // 0.0 to 1.0
)

// --- API Interface ---
interface GoogleNLPApi {
    @POST("v1/documents:analyzeEntities")
    suspend fun analyzeEntities(
        @Query("key") apiKey: String,
        @Body request: NLPRequest
    ): EntityResponse

    @POST("v1/documents:analyzeSentiment")
    suspend fun analyzeSentiment(
        @Query("key") apiKey: String,
        @Body request: NLPRequest
    ): SentimentResponse

    @POST("v1/documents:analyzeSyntax")
    suspend fun analyzeSyntax(
        @Query("key") apiKey: String,
        @Body request: NLPRequest
    ): SyntaxResponse

    @POST("v1/documents:classifyText")
    suspend fun classifyText(
        @Query("key") apiKey: String,
        @Body request: NLPRequest
    ): ClassificationResponse

    @POST("v1/documents:annotateText")
    suspend fun annotateText(
        @Query("key") apiKey: String,
        @Body request: NLPRequest
    ): AnnotateTextResponse
}

@JsonClass(generateAdapter = true)
data class AnnotateTextResponse(
    val sentences: List<SyntaxSentence>?,
    val tokens: List<SyntaxToken>?,
    val entities: List<EntityResult>?,
    val documentSentiment: Sentiment?,
    val categories: List<CategoryResult>?
)

// --- Client ---
class CloudNLPClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://language.googleapis.com/")
        .addConverterFactory(MoshiConverterFactory.create(
            Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
        ))
        .build()

    val api: GoogleNLPApi = retrofit.create(GoogleNLPApi::class.java)
}
```

### Repository

```kotlin
class CloudNLPRepository(private val apiKey: String) {

    private val client = CloudNLPClient()

    // --- Entity Analysis ---
    suspend fun analyzeEntities(text: String): List<EntityResult> {
        val request = NLPRequest(
            document = DocumentInput(content = text)
        )
        val response = client.api.analyzeEntities(apiKey, request)
        return response.entities
    }

    // --- Sentiment Analysis ---
    suspend fun analyzeSentiment(text: String): SentimentAnalysisResult {
        val request = NLPRequest(
            document = DocumentInput(content = text)
        )
        val response = client.api.analyzeSentiment(apiKey, request)

        return SentimentAnalysisResult(
            overallScore = response.documentSentiment.score,
            overallMagnitude = response.documentSentiment.magnitude,
            sentenceSentiments = response.sentences.map {
                SentenceSentimentResult(
                    text = it.text.content,
                    score = it.sentiment.score,
                    magnitude = it.sentiment.magnitude
                )
            }
        )
    }

    data class SentimentAnalysisResult(
        val overallScore: Float,
        val overallMagnitude: Float,
        val sentenceSentiments: List<SentenceSentimentResult>
    ) {
        val label: String get() = when {
            overallScore > 0.3 -> "Positive"
            overallScore < -0.3 -> "Negative"
            else -> "Neutral"
        }
    }

    data class SentenceSentimentResult(
        val text: String,
        val score: Float,
        val magnitude: Float
    )

    // --- Syntax Analysis ---
    suspend fun analyzeSyntax(text: String): SyntaxResponse {
        val request = NLPRequest(
            document = DocumentInput(content = text)
        )
        return client.api.analyzeSyntax(apiKey, request)
    }

    // --- Content Classification ---
    suspend fun classifyContent(text: String): List<CategoryResult> {
        val request = NLPRequest(
            document = DocumentInput(content = text)
        )
        val response = client.api.classifyText(apiKey, request)
        return response.categories.sortedByDescending { it.confidence }
    }

    // --- All-in-One Analysis ---
    suspend fun fullAnalysis(text: String): AnnotateTextResponse {
        val request = NLPRequest(
            document = DocumentInput(content = text),
            features = Features(
                extractSyntax = true,
                extractEntities = true,
                extractDocumentSentiment = true,
                extractEntitySentiment = true,
                classifyText = true
            )
        )
        return client.api.annotateText(apiKey, request)
    }
}
```

---

## 2. Custom Entity Extraction

### Theory

```
Custom Entity Extraction:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Built-in entities: PERSON, LOCATION, ORG, etc.      │
│  Custom entities: YOUR domain-specific types         │
│                                                      │
│  Example - Medical Domain:                           │
│  "Patient reports headache and took aspirin"         │
│  ┌────────────┬──────────────────────┐               │
│  │ headache   │ SYMPTOM (custom)     │               │
│  │ aspirin    │ MEDICATION (custom)  │               │
│  └────────────┴──────────────────────┘               │
│                                                      │
│  Training Custom Entity Models:                      │
│  1. Prepare labeled training data (JSONL format)     │
│  2. Upload to Google Cloud AutoML                    │
│  3. Train custom model                               │
│  4. Deploy endpoint                                  │
│  5. Call from Android via REST API                   │
│                                                      │
│  OR use pattern-based extraction:                    │
│  Combine Cloud NLP entities with regex patterns      │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class CustomEntityExtractor(private val apiKey: String) {

    private val cloudNLP = CloudNLPRepository(apiKey)

    // Combine Cloud NLP with custom rules
    suspend fun extractCustomEntities(text: String): List<CustomEntity> {
        val entities = mutableListOf<CustomEntity>()

        // 1. Get Cloud NLP entities
        val nlpEntities = cloudNLP.analyzeEntities(text)
        entities.addAll(nlpEntities.map { entity ->
            CustomEntity(
                text = entity.name,
                type = entity.type,
                confidence = entity.salience,
                source = "cloud_nlp"
            )
        })

        // 2. Apply custom pattern rules
        entities.addAll(extractWithPatterns(text))

        return entities.distinctBy { it.text to it.type }
    }

    // Pattern-based extraction for domain-specific entities
    private fun extractWithPatterns(text: String): List<CustomEntity> {
        val entities = mutableListOf<CustomEntity>()

        // Email pattern
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        emailRegex.findAll(text).forEach { match ->
            entities.add(CustomEntity(match.value, "EMAIL", 1.0f, "pattern"))
        }

        // Phone pattern
        val phoneRegex = Regex("\\+?\\d{1,3}[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}")
        phoneRegex.findAll(text).forEach { match ->
            entities.add(CustomEntity(match.value, "PHONE", 1.0f, "pattern"))
        }

        // Price pattern
        val priceRegex = Regex("\\$\\d+\\.?\\d{0,2}")
        priceRegex.findAll(text).forEach { match ->
            entities.add(CustomEntity(match.value, "PRICE", 1.0f, "pattern"))
        }

        // Date pattern
        val dateRegex = Regex("\\d{1,2}/\\d{1,2}/\\d{2,4}|\\d{4}-\\d{2}-\\d{2}")
        dateRegex.findAll(text).forEach { match ->
            entities.add(CustomEntity(match.value, "DATE", 1.0f, "pattern"))
        }

        return entities
    }

    data class CustomEntity(
        val text: String,
        val type: String,
        val confidence: Float,
        val source: String  // "cloud_nlp" or "pattern"
    )
}
```

---

## 3. Content Classification

### Theory

```
Content Categories (700+ categories):
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Category Hierarchy:                                 │
│  /Arts & Entertainment                               │
│    /Arts & Entertainment/Music & Audio                │
│    /Arts & Entertainment/Movies                      │
│  /Business & Industrial                              │
│    /Business & Industrial/Finance                    │
│  /Computers & Electronics                            │
│    /Computers & Electronics/Software                 │
│    /Computers & Electronics/Programming              │
│  /Food & Drink                                       │
│    /Food & Drink/Cooking & Recipes                   │
│  /Health                                             │
│    /Health/Medical Devices & Equipment               │
│  /Science                                            │
│    /Science/Computer Science                         │
│    /Science/Computer Science/Machine Learning        │
│  /Sports                                             │
│    /Sports/Team Sports/Soccer                        │
│  ...and many more                                    │
│                                                      │
│  Minimum text: 20+ words recommended                 │
│  Returns: Top categories with confidence scores      │
│                                                      │
│  Use Cases:                                          │
│  • Content moderation                                │
│  • Article tagging                                   │
│  • News categorization                               │
│  • Ad targeting                                      │
│  • Content recommendation                            │
└──────────────────────────────────────────────────────┘
```

### Code

```kotlin
class ContentClassifier(private val nlpRepo: CloudNLPRepository) {

    // Classify a single text
    suspend fun classify(text: String): List<ContentCategory> {
        val categories = nlpRepo.classifyContent(text)
        return categories.map { cat ->
            ContentCategory(
                fullPath = cat.name,
                topLevel = cat.name.split("/").getOrNull(1) ?: cat.name,
                confidence = cat.confidence
            )
        }
    }

    // Classify and filter by confidence
    suspend fun classifyFiltered(text: String, minConfidence: Float = 0.5f): List<ContentCategory> {
        return classify(text).filter { it.confidence >= minConfidence }
    }

    // Batch classify articles
    suspend fun classifyArticles(articles: List<String>): Map<String, List<ContentCategory>> {
        return articles.associateWith { article ->
            try {
                classify(article)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    data class ContentCategory(
        val fullPath: String,
        val topLevel: String,
        val confidence: Float
    )
}

// Usage
suspend fun classificationExample() {
    val nlpRepo = CloudNLPRepository("YOUR_API_KEY")
    val classifier = ContentClassifier(nlpRepo)

    val text = """
        TensorFlow Lite enables on-device machine learning inference 
        with low latency. It supports Android and iOS platforms and 
        provides GPU acceleration for faster processing.
    """.trimIndent()

    val categories = classifier.classify(text)
    // Expected: /Computers & Electronics/Software → 0.85
    //           /Science/Computer Science/Machine Learning → 0.72
}
```

---

## ViewModel Integration

```kotlin
class NLPAnalysisViewModel : ViewModel() {

    // Use backend proxy in production
    private val nlpRepo = CloudNLPRepository(BuildConfig.NLP_API_KEY)

    data class AnalysisResult(
        val entities: List<EntityResult> = emptyList(),
        val sentiment: CloudNLPRepository.SentimentAnalysisResult? = null,
        val categories: List<CategoryResult> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _result = MutableStateFlow(AnalysisResult())
    val result: StateFlow<AnalysisResult> = _result.asStateFlow()

    fun analyzeText(text: String) {
        viewModelScope.launch {
            _result.value = AnalysisResult(isLoading = true)

            try {
                val fullAnalysis = nlpRepo.fullAnalysis(text)

                _result.value = AnalysisResult(
                    entities = fullAnalysis.entities ?: emptyList(),
                    sentiment = fullAnalysis.documentSentiment?.let {
                        CloudNLPRepository.SentimentAnalysisResult(
                            overallScore = it.score,
                            overallMagnitude = it.magnitude,
                            sentenceSentiments = emptyList()
                        )
                    },
                    categories = fullAnalysis.categories ?: emptyList()
                )
            } catch (e: Exception) {
                _result.value = AnalysisResult(error = e.message)
            }
        }
    }
}
```
