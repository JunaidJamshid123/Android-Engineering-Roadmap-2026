# Smart Search — AI-Powered Features

## Overview

Smart search combines traditional keyword search with AI-powered semantic understanding using vector databases, embeddings, and RAG (Retrieval Augmented Generation) patterns.

```
Smart Search Architecture:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  User Query: "comfortable shoes for running in rain"        │
│                                                              │
│       ┌─────────────────────────────────────────┐           │
│       │              Search Pipeline             │           │
│       │                                          │           │
│       │  1. Keyword Search (BM25/Elasticsearch)  │           │
│       │     └─ Matches: "running shoes", "rain"  │           │
│       │                                          │           │
│       │  2. Semantic Search (Vector DB)           │           │
│       │     └─ Finds: "waterproof trainers",     │           │
│       │        "all-weather athletic footwear"   │           │
│       │        (semantically similar even with   │           │
│       │         different words)                 │           │
│       │                                          │           │
│       │  3. Hybrid Fusion (RRF / weighted)       │           │
│       │     └─ Merges & re-ranks both           │           │
│       │                                          │           │
│       │  4. RAG (optional)                       │           │
│       │     └─ LLM generates answer from        │           │
│       │        retrieved documents               │           │
│       └─────────────────────────────────────────┘           │
│                                                              │
│  Vector Databases Comparison:                               │
│  ┌───────────┬───────────┬──────────┬──────────────┐       │
│  │           │ Qdrant    │ Pinecone │ Weaviate     │       │
│  ├───────────┼───────────┼──────────┼──────────────┤       │
│  │ Type      │ Self-host │ Managed  │ Self/Managed │       │
│  │           │ + Cloud   │ only     │              │       │
│  │ Open src  │ ✅ Rust   │ ❌       │ ✅ Go        │       │
│  │ Filtering │ ✅ Rich   │ ✅       │ ✅ GraphQL   │       │
│  │ Hybrid    │ ✅        │ ❌       │ ✅ BM25+vec  │       │
│  │ Free tier │ ✅ 1GB    │ ✅ 100K  │ ✅ Sandbox   │       │
│  │ Latency   │ <10ms     │ <50ms    │ <20ms        │       │
│  │ Scale     │ Billions  │ Billions │ Billions     │       │
│  └───────────┴───────────┴──────────┴──────────────┘       │
│                                                              │
│  Embedding Models:                                          │
│  ┌────────────────────────────────────────────┐             │
│  │ Model                  │ Dims │ Quality    │             │
│  ├────────────────────────┼──────┼────────────┤             │
│  │ text-embedding-3-small │ 1536 │ ████████   │             │
│  │ text-embedding-3-large │ 3072 │ ██████████ │             │
│  │ all-MiniLM-L6-v2      │ 384  │ ███████    │             │
│  │ Gemini embedding       │ 768  │ █████████  │             │
│  │ Cohere embed-v3        │ 1024 │ █████████  │             │
│  └────────────────────────┴──────┴────────────┘             │
└──────────────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

---

## 1. Vector Databases — Qdrant Integration

```
Qdrant Concepts:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Collection = Table (stores vectors + payload)              │
│  Point = Row (id + vector + metadata payload)               │
│  Payload = JSON metadata for filtering                      │
│                                                              │
│  ┌──────────────────────────────────────────────┐           │
│  │ Collection: "products"                        │           │
│  │                                               │           │
│  │ Point {                                       │           │
│  │   id: "shoe_123",                             │           │
│  │   vector: [0.12, -0.45, 0.78, ...],  // 384d │           │
│  │   payload: {                                  │           │
│  │     "name": "Nike Air Max",                   │           │
│  │     "category": "shoes",                      │           │
│  │     "price": 129.99,                          │           │
│  │     "in_stock": true                          │           │
│  │   }                                           │           │
│  │ }                                             │           │
│  └──────────────────────────────────────────────┘           │
│                                                              │
│  Search: Query vector → Find K nearest vectors              │
│  + optional payload filters                                 │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
// Qdrant REST API client
@JsonClass(generateAdapter = true)
data class QdrantSearchRequest(
    val vector: List<Float>,
    val limit: Int = 10,
    val filter: QdrantFilter? = null,
    @Json(name = "with_payload") val withPayload: Boolean = true,
    @Json(name = "score_threshold") val scoreThreshold: Float? = null
)

@JsonClass(generateAdapter = true)
data class QdrantFilter(
    val must: List<QdrantCondition>? = null,
    val should: List<QdrantCondition>? = null,
    @Json(name = "must_not") val mustNot: List<QdrantCondition>? = null
)

@JsonClass(generateAdapter = true)
data class QdrantCondition(
    val key: String,
    val match: QdrantMatch? = null,
    val range: QdrantRange? = null
)

@JsonClass(generateAdapter = true)
data class QdrantMatch(val value: Any)

@JsonClass(generateAdapter = true)
data class QdrantRange(
    val gte: Float? = null,
    val lte: Float? = null
)

@JsonClass(generateAdapter = true)
data class QdrantSearchResponse(
    val result: List<QdrantScoredPoint>
)

@JsonClass(generateAdapter = true)
data class QdrantScoredPoint(
    val id: String,
    val score: Float,
    val payload: Map<String, Any>?
)

@JsonClass(generateAdapter = true)
data class QdrantUpsertRequest(
    val points: List<QdrantPoint>
)

@JsonClass(generateAdapter = true)
data class QdrantPoint(
    val id: String,
    val vector: List<Float>,
    val payload: Map<String, Any>? = null
)

interface QdrantApi {
    @POST("collections/{collection}/points/search")
    suspend fun search(
        @Path("collection") collection: String,
        @Body request: QdrantSearchRequest
    ): QdrantSearchResponse

    @PUT("collections/{collection}/points")
    suspend fun upsert(
        @Path("collection") collection: String,
        @Body request: QdrantUpsertRequest
    )
}

class QdrantClient(baseUrl: String, apiKey: String) {
    val api: QdrantApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder()
                    .addHeader("api-key", apiKey)
                    .build())
            }
            .build())
        .build()
        .create(QdrantApi::class.java)
}
```

---

## 2. Semantic Search with Embeddings

```
Semantic Search Flow:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Indexing (Offline):                                        │
│  ┌──────────┐  ┌──────────────┐  ┌─────────────┐          │
│  │ Documents │─▶│ Embed Model │─▶│ Vector DB   │          │
│  │ (text)    │  │ (API call)   │  │ (store vecs)│          │
│  └──────────┘  └──────────────┘  └─────────────┘          │
│                                                              │
│  Querying (Online):                                         │
│  ┌──────────┐  ┌──────────────┐  ┌─────────────┐          │
│  │ User     │─▶│ Embed Model │─▶│ Vector DB   │          │
│  │ Query    │  │ (same model) │  │ (KNN search)│          │
│  └──────────┘  └──────────────┘  └──────┬──────┘          │
│                                         │                   │
│                                         ▼                   │
│                                  ┌─────────────┐           │
│                                  │ Top K docs  │           │
│                                  │ ranked by    │           │
│                                  │ cosine sim   │           │
│                                  └─────────────┘           │
│                                                              │
│  Why it works:                                              │
│  "dog" ≈ "puppy" ≈ "canine"  (close in vector space)      │
│  "dog" ≠ "hotdog"            (far in vector space)         │
│  "king - man + woman ≈ queen" (captures relationships)     │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
// Embedding service (using OpenAI or Gemini)
interface EmbeddingApi {
    @POST("v1/embeddings")
    suspend fun createEmbedding(
        @Header("Authorization") auth: String,
        @Body request: EmbeddingRequest
    ): EmbeddingResponse
}

@JsonClass(generateAdapter = true)
data class EmbeddingRequest(
    val input: List<String>,
    val model: String = "text-embedding-3-small"
)

@JsonClass(generateAdapter = true)
data class EmbeddingResponse(
    val data: List<EmbeddingData>
)

@JsonClass(generateAdapter = true)
data class EmbeddingData(
    val embedding: List<Float>,
    val index: Int
)

class SemanticSearchEngine(
    private val qdrant: QdrantClient,
    private val embeddingApi: EmbeddingApi,
    private val apiKey: String,
    private val collection: String = "documents"
) {
    // Index documents
    suspend fun indexDocuments(documents: List<SearchDocument>) {
        // Batch embed (max ~2000 tokens per batch)
        val batchSize = 100
        for (batch in documents.chunked(batchSize)) {
            val texts = batch.map { "${it.title} ${it.content}" }
            val embeddings = embeddingApi.createEmbedding(
                auth = "Bearer $apiKey",
                request = EmbeddingRequest(input = texts)
            )

            val points = batch.mapIndexed { index, doc ->
                QdrantPoint(
                    id = doc.id,
                    vector = embeddings.data[index].embedding,
                    payload = mapOf(
                        "title" to doc.title,
                        "content" to doc.content,
                        "category" to doc.category,
                        "timestamp" to doc.timestamp
                    )
                )
            }

            qdrant.api.upsert(collection, QdrantUpsertRequest(points))
        }
    }

    // Semantic search
    suspend fun search(
        query: String,
        limit: Int = 10,
        categoryFilter: String? = null,
        minScore: Float = 0.5f
    ): List<SearchResult> {
        // Embed query
        val queryEmbedding = embeddingApi.createEmbedding(
            auth = "Bearer $apiKey",
            request = EmbeddingRequest(input = listOf(query))
        ).data.first().embedding

        // Build filter
        val filter = categoryFilter?.let {
            QdrantFilter(
                must = listOf(QdrantCondition(key = "category", match = QdrantMatch(value = it)))
            )
        }

        // Search
        val response = qdrant.api.search(
            collection,
            QdrantSearchRequest(
                vector = queryEmbedding,
                limit = limit,
                filter = filter,
                scoreThreshold = minScore
            )
        )

        return response.result.map { point ->
            SearchResult(
                id = point.id,
                title = point.payload?.get("title")?.toString() ?: "",
                content = point.payload?.get("content")?.toString() ?: "",
                score = point.score
            )
        }
    }

    data class SearchDocument(
        val id: String,
        val title: String,
        val content: String,
        val category: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class SearchResult(
        val id: String,
        val title: String,
        val content: String,
        val score: Float
    )
}
```

---

## 3. Hybrid Search (Keyword + Semantic)

```
Hybrid Search — Reciprocal Rank Fusion (RRF):
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Query: "waterproof running shoes"                          │
│                                                              │
│  Keyword Results:           Semantic Results:               │
│  1. Nike Waterproof Run     1. Adidas Storm Runner          │
│  2. Asics Rain Runner       2. Nike Waterproof Run          │
│  3. Puma AllWeather         3. Brooks Weather Shield        │
│  4. Reebok WetGrip          4. New Balance AquaFlex         │
│                                                              │
│  RRF Formula:                                               │
│  score(d) = Σ 1/(k + rank_i(d))                            │
│             i                                                │
│  where k = 60 (constant)                                    │
│                                                              │
│  Nike Waterproof Run:                                       │
│    Keyword rank 1: 1/(60+1) = 0.0164                       │
│    Semantic rank 2: 1/(60+2) = 0.0161                      │
│    RRF score = 0.0325  ← Best combined score               │
│                                                              │
│  Adidas Storm Runner:                                       │
│    Keyword rank: ∞ (not found): 0                           │
│    Semantic rank 1: 1/(60+1) = 0.0164                      │
│    RRF score = 0.0164                                       │
│                                                              │
│  Final Ranking:                                             │
│  1. Nike Waterproof Run (0.0325) ← appears in both         │
│  2. Asics Rain Runner (0.0161)                              │
│  3. Adidas Storm Runner (0.0164)                            │
│  4. ...                                                     │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class HybridSearchEngine(
    private val semanticEngine: SemanticSearchEngine,
    private val keywordSearchApi: KeywordSearchApi  // e.g., Elasticsearch
) {
    // Simple keyword search interface
    interface KeywordSearchApi {
        suspend fun search(query: String, limit: Int): List<KeywordResult>
    }

    data class KeywordResult(val id: String, val title: String, val score: Float)

    // Reciprocal Rank Fusion
    fun reciprocalRankFusion(
        rankedLists: List<List<String>>,
        k: Int = 60
    ): List<Pair<String, Float>> {
        val scores = mutableMapOf<String, Float>()

        for (rankedList in rankedLists) {
            for ((rank, docId) in rankedList.withIndex()) {
                scores[docId] = (scores[docId] ?: 0f) + 1f / (k + rank + 1)
            }
        }

        return scores.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }
    }

    // Hybrid search combining keyword + semantic
    suspend fun hybridSearch(
        query: String,
        limit: Int = 10,
        keywordWeight: Float = 0.4f,
        semanticWeight: Float = 0.6f
    ): List<HybridSearchResult> {
        // Run both searches
        val keywordResults = keywordSearchApi.search(query, limit * 2)
        val semanticResults = semanticEngine.search(query, limit * 2)

        // Method 1: RRF (rank-based fusion)
        val rrfScores = reciprocalRankFusion(
            listOf(
                keywordResults.map { it.id },
                semanticResults.map { it.id }
            )
        )

        // Method 2: Weighted score fusion (alternative)
        val keywordScoreMap = keywordResults.associate { it.id to it.score }
        val semanticScoreMap = semanticResults.associate { it.id to it.score }
        val allIds = keywordScoreMap.keys + semanticScoreMap.keys

        val weightedScores = allIds.map { id ->
            val kwScore = keywordScoreMap[id] ?: 0f
            val semScore = semanticScoreMap[id] ?: 0f
            id to (keywordWeight * kwScore + semanticWeight * semScore)
        }.sortedByDescending { it.second }

        // Use RRF scores as primary
        val titleMap = (keywordResults.associate { it.id to it.title } +
                       semanticResults.associate { it.id to it.title })
        val contentMap = semanticResults.associate { it.id to it.content }

        return rrfScores.take(limit).map { (id, score) ->
            HybridSearchResult(
                id = id,
                title = titleMap[id] ?: "",
                content = contentMap[id] ?: "",
                score = score,
                keywordScore = keywordScoreMap[id],
                semanticScore = semanticScoreMap[id]
            )
        }
    }

    data class HybridSearchResult(
        val id: String,
        val title: String,
        val content: String,
        val score: Float,
        val keywordScore: Float?,
        val semanticScore: Float?
    )
}
```

---

## 4. RAG (Retrieval Augmented Generation)

```
RAG Pipeline:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Without RAG:                                               │
│  User: "What's our refund policy?"                          │
│  LLM:  "I don't know your specific policy..." ← Halluc.    │
│                                                              │
│  With RAG:                                                  │
│  ┌──────────┐  ┌─────────────┐  ┌───────────────────────┐  │
│  │ Question │─▶│ Vector      │─▶│ Relevant docs:        │  │
│  │          │  │ Search      │  │ "30-day refund policy  │  │
│  │          │  │ (retrieve)  │  │  for unused items..."  │  │
│  └──────────┘  └─────────────┘  └───────────┬───────────┘  │
│                                             │               │
│                                             ▼               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Prompt to LLM:                                       │   │
│  │                                                      │   │
│  │ Context: [retrieved documents inserted here]         │   │
│  │                                                      │   │
│  │ Question: "What's our refund policy?"                │   │
│  │                                                      │   │
│  │ Answer based ONLY on the context above.              │   │
│  └──────────────────────────────────┬───────────────────┘   │
│                                     │                       │
│                                     ▼                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ LLM Answer:                                          │   │
│  │ "Our refund policy allows returns within 30 days    │   │
│  │  for unused items with original packaging..."        │   │
│  │                                 ← Accurate! ✅       │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  RAG Benefits:                                              │
│  • Grounded answers (no hallucination)                     │
│  • Up-to-date information                                  │
│  • Domain-specific knowledge                               │
│  • Source attribution                                      │
│  • No fine-tuning needed                                   │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class RAGEngine(
    private val searchEngine: SemanticSearchEngine,
    private val llmApi: LLMApi  // Gemini, OpenAI, etc.
) {
    interface LLMApi {
        suspend fun generate(prompt: String): String
    }

    // RAG query with retrieval
    suspend fun query(
        question: String,
        topK: Int = 5,
        maxContextLength: Int = 4000
    ): RAGResponse {
        // Step 1: Retrieve relevant documents
        val searchResults = searchEngine.search(question, topK)

        // Step 2: Build context from retrieved docs
        val context = buildContext(searchResults, maxContextLength)

        // Step 3: Build prompt
        val prompt = buildRAGPrompt(question, context)

        // Step 4: Generate answer
        val answer = llmApi.generate(prompt)

        return RAGResponse(
            answer = answer,
            sources = searchResults.map { Source(it.id, it.title, it.score) },
            contextUsed = context
        )
    }

    private fun buildContext(
        results: List<SemanticSearchEngine.SearchResult>,
        maxLength: Int
    ): String {
        val sb = StringBuilder()
        for (result in results) {
            val entry = "[Source: ${result.title}]\n${result.content}\n\n"
            if (sb.length + entry.length > maxLength) break
            sb.append(entry)
        }
        return sb.toString()
    }

    private fun buildRAGPrompt(question: String, context: String): String {
        return """You are a helpful assistant. Answer the user's question based ONLY on the following context.
If the context doesn't contain enough information, say "I don't have enough information to answer that."
Do not make up information.

Context:
$context

Question: $question

Answer:"""
    }

    data class RAGResponse(
        val answer: String,
        val sources: List<Source>,
        val contextUsed: String
    )

    data class Source(val id: String, val title: String, val relevanceScore: Float)
}
```

---

## Complete ViewModel

```kotlin
class SmartSearchViewModel(
    private val hybridSearch: HybridSearchEngine,
    private val ragEngine: RAGEngine
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<HybridSearchEngine.HybridSearchResult>>(emptyList())
    val searchResults: StateFlow<List<HybridSearchEngine.HybridSearchResult>> = _searchResults

    private val _ragAnswer = MutableStateFlow<RAGEngine.RAGResponse?>(null)
    val ragAnswer: StateFlow<RAGEngine.RAGResponse?> = _ragAnswer

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Hybrid search (keyword + semantic)
    fun search(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _searchResults.value = hybridSearch.hybridSearch(query)
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // RAG-powered Q&A
    fun askQuestion(question: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _ragAnswer.value = ragEngine.query(question)
            } catch (e: Exception) {
                _ragAnswer.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```
