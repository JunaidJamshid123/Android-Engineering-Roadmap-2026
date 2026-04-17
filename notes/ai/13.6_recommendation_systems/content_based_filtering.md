# Content-Based Filtering — Recommendation Systems

## Overview

Content-based filtering recommends items based on item features and user preference profiles. It analyzes what a user liked before and finds items with similar attributes.

```
Content-Based Filtering Theory:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Core Principle: "Recommend items similar to what the       │
│  user has liked in the past"                                │
│                                                              │
│  ┌──────────────┐     ┌───────────────┐     ┌────────────┐ │
│  │ Item Features │────▶│ User Profile  │────▶│ Recommend  │ │
│  │               │     │ (learned)     │     │ Similar    │ │
│  └──────────────┘     └───────────────┘     └────────────┘ │
│                                                              │
│  Example — Movie Recommendation:                            │
│                                                              │
│  User liked: The Matrix (5★), Inception (4★), Alien (4★)   │
│                                                              │
│  Item Feature Vectors:                                      │
│  ┌────────────────┬──────┬───────┬──────┬───────┬──────┐   │
│  │ Movie          │Sci-Fi│Action │Drama │Horror │Comedy│   │
│  ├────────────────┼──────┼───────┼──────┼───────┼──────┤   │
│  │ The Matrix     │ 0.9  │ 0.8   │ 0.4  │ 0.1   │ 0.0  │   │
│  │ Inception      │ 0.8  │ 0.6   │ 0.7  │ 0.1   │ 0.0  │   │
│  │ Alien          │ 0.9  │ 0.5   │ 0.3  │ 0.8   │ 0.0  │   │
│  │ ────────────── │──────│───────│──────│───────│──────│   │
│  │ User Profile → │ 0.87 │ 0.63  │ 0.47 │ 0.33  │ 0.0  │   │
│  │ (weighted avg) │      │       │      │       │      │   │
│  └────────────────┴──────┴───────┴──────┴───────┴──────┘   │
│                                                              │
│  Now score unseen movies against User Profile:              │
│  Interstellar [0.9, 0.4, 0.7, 0.1, 0.0] → sim = 0.91 ✅  │
│  The Hangover  [0.0, 0.1, 0.2, 0.0, 0.9] → sim = 0.12 ❌  │
│                                                              │
│  Pros & Cons:                                               │
│  ┌──────────────────────┬───────────────────────────┐      │
│  │ ✅ Pros              │ ❌ Cons                    │      │
│  ├──────────────────────┼───────────────────────────┤      │
│  │ No cold-start (item) │ Feature engineering needed│      │
│  │ Transparent reasons  │ Limited discovery          │      │
│  │ No other users needed│ "Filter bubble"           │      │
│  │ Domain independent   │ Overspecialization         │      │
│  │ Privacy preserving   │ New user cold start        │      │
│  └──────────────────────┴───────────────────────────┘      │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. Feature Extraction

```
Feature Types:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Structured Features (known attributes):                    │
│  ┌────────────────────────────────────────────────┐         │
│  │ • Category:     "Electronics", "Clothing"      │         │
│  │ • Price range:  [0-50, 50-100, 100+]          │         │
│  │ • Brand:        "Nike", "Apple"                │         │
│  │ • Attributes:   color, size, weight            │         │
│  │ • Tags:         "wireless", "waterproof"       │         │
│  └────────────────────────────────────────────────┘         │
│                                                              │
│  Text-Derived Features (from descriptions):                 │
│  ┌────────────────────────────────────────────────┐         │
│  │ • TF-IDF vectors from descriptions             │         │
│  │ • Word embeddings (Word2Vec, BERT)             │         │
│  │ • Named entities                               │         │
│  │ • Topics (LDA / NMF)                           │         │
│  └────────────────────────────────────────────────┘         │
│                                                              │
│  Visual Features (from images):                             │
│  ┌────────────────────────────────────────────────┐         │
│  │ • CNN embeddings (ResNet, MobileNet)           │         │
│  │ • Color histograms                             │         │
│  │ • Style attributes                             │         │
│  └────────────────────────────────────────────────┘         │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
data class Item(
    val id: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    val tags: List<String>,
    val price: Float,
    val attributes: Map<String, String>
)

class FeatureExtractor {

    // One-hot encode categories
    fun encodeCategorical(
        categories: List<String>,
        allCategories: List<String>
    ): FloatArray {
        return FloatArray(allCategories.size) { idx ->
            if (allCategories[idx] in categories) 1f else 0f
        }
    }

    // Encode numerical features with min-max normalization
    fun encodeNumerical(value: Float, min: Float, max: Float): Float {
        return if (max > min) (value - min) / (max - min) else 0f
    }

    // Build feature vector for an item
    fun extractFeatures(item: Item, schema: FeatureSchema): FloatArray {
        val features = mutableListOf<Float>()

        // Category one-hot
        features.addAll(encodeCategorical(item.categories, schema.allCategories).toList())

        // Tag one-hot
        features.addAll(encodeCategorical(item.tags, schema.allTags).toList())

        // Price (normalized)
        features.add(encodeNumerical(item.price, schema.priceRange.first, schema.priceRange.second))

        return features.toFloatArray()
    }

    data class FeatureSchema(
        val allCategories: List<String>,
        val allTags: List<String>,
        val priceRange: Pair<Float, Float>
    )
}
```

---

## 2. TF-IDF and Embeddings

```
TF-IDF Theory:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  TF-IDF = Term Frequency × Inverse Document Frequency       │
│                                                              │
│  TF(t, d) = count(t in d) / total_words(d)                 │
│  IDF(t) = log(N / df(t))                                    │
│                                                              │
│  Example:                                                   │
│  Doc1: "wireless bluetooth headphones noise cancelling"     │
│  Doc2: "wired headphones studio monitor"                    │
│  Doc3: "wireless bluetooth speaker waterproof portable"     │
│                                                              │
│  "wireless":                                                │
│    TF(Doc1) = 1/5 = 0.20                                    │
│    IDF = log(3/2) = 0.41                                    │
│    TF-IDF(Doc1) = 0.20 × 0.41 = 0.082                     │
│                                                              │
│  "headphones":                                              │
│    TF(Doc1) = 1/5 = 0.20                                    │
│    IDF = log(3/2) = 0.41      (appears in 2 docs)          │
│    TF-IDF(Doc1) = 0.082                                     │
│                                                              │
│  "noise":                                                   │
│    TF(Doc1) = 1/5 = 0.20                                    │
│    IDF = log(3/1) = 1.10      (appears in 1 doc → rarer)   │
│    TF-IDF(Doc1) = 0.22        ← Higher score for unique    │
│                                                              │
│  Embeddings vs TF-IDF:                                      │
│  ┌──────────────┬──────────────────┬────────────────────┐   │
│  │              │ TF-IDF           │ Embeddings          │   │
│  ├──────────────┼──────────────────┼────────────────────┤   │
│  │ Semantics    │ ❌ Bag-of-words  │ ✅ Captures meaning│   │
│  │ Dimension    │ Vocab size       │ Fixed (384-768)    │   │
│  │ "king"="man" │ 0 (different)    │ ~0.8 (similar)    │   │
│  │ Speed        │ Fast             │ Needs model        │   │
│  │ Quality      │ Good baseline    │ State-of-art       │   │
│  └──────────────┴──────────────────┴────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class TfIdfVectorizer {

    private val vocabulary = mutableMapOf<String, Int>()
    private val idfValues = mutableMapOf<String, Float>()
    private var fitted = false

    // Tokenize and preprocess
    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .split("\\s+".toRegex())
            .filter { it.length > 2 }  // Remove short words
    }

    // Fit on corpus to compute IDF
    fun fit(documents: List<String>) {
        val n = documents.size
        val docFrequency = mutableMapOf<String, Int>()

        documents.forEach { doc ->
            val uniqueTokens = tokenize(doc).toSet()
            uniqueTokens.forEach { token ->
                docFrequency[token] = (docFrequency[token] ?: 0) + 1
            }
        }

        // Build vocabulary and IDF
        var index = 0
        docFrequency.forEach { (token, df) ->
            vocabulary[token] = index++
            idfValues[token] = kotlin.math.ln((n.toFloat() + 1) / (df + 1)) + 1
        }

        fitted = true
    }

    // Transform a document to TF-IDF vector
    fun transform(document: String): FloatArray {
        if (!fitted) throw IllegalStateException("Must call fit() first")

        val tokens = tokenize(document)
        val vector = FloatArray(vocabulary.size)

        // Count term frequencies
        val tf = mutableMapOf<String, Int>()
        tokens.forEach { tf[it] = (tf[it] ?: 0) + 1 }

        // Compute TF-IDF
        tf.forEach { (token, count) ->
            val idx = vocabulary[token] ?: return@forEach
            val termFreq = count.toFloat() / tokens.size
            vector[idx] = termFreq * (idfValues[token] ?: 0f)
        }

        // L2 normalize
        val norm = kotlin.math.sqrt(vector.fold(0f) { acc, v -> acc + v * v })
        if (norm > 0) {
            for (i in vector.indices) vector[i] /= norm
        }

        return vector
    }

    // Fit and transform all documents
    fun fitTransform(documents: List<String>): List<FloatArray> {
        fit(documents)
        return documents.map { transform(it) }
    }
}
```

---

## 3. Similarity Computations

```kotlin
object SimilarityUtils {

    // Cosine similarity between two vectors
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Vectors must be same size" }

        var dotProduct = 0f
        var normA = 0f
        var normB = 0f

        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }

        val denominator = kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB)
        return if (denominator > 0) dotProduct / denominator else 0f
    }

    // Euclidean distance (smaller = more similar)
    fun euclideanDistance(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size)
        var sum = 0f
        for (i in a.indices) {
            val diff = a[i] - b[i]
            sum += diff * diff
        }
        return kotlin.math.sqrt(sum)
    }

    // Jaccard similarity for sets
    fun jaccardSimilarity(a: Set<String>, b: Set<String>): Float {
        val intersection = a.intersect(b).size
        val union = a.union(b).size
        return if (union > 0) intersection.toFloat() / union else 0f
    }

    // Manhattan distance
    fun manhattanDistance(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size)
        return a.zip(b.toList()) { x, y -> kotlin.math.abs(x - y) }.sum()
    }
}
```

---

## Content-Based Recommender

```kotlin
class ContentBasedRecommender {

    private val tfidf = TfIdfVectorizer()
    private val featureExtractor = FeatureExtractor()

    private val items = mutableMapOf<String, Item>()
    private val itemVectors = mutableMapOf<String, FloatArray>()  // Combined vectors
    private val userProfiles = mutableMapOf<String, FloatArray>()

    // Index all items
    fun indexItems(newItems: List<Item>, schema: FeatureExtractor.FeatureSchema) {
        items.clear()
        itemVectors.clear()

        newItems.forEach { items[it.id] = it }

        // Compute TF-IDF on descriptions
        val descriptions = newItems.map { "${it.title} ${it.description} ${it.tags.joinToString(" ")}" }
        val tfidfVectors = tfidf.fitTransform(descriptions)

        // Combine structured + text features
        newItems.forEachIndexed { index, item ->
            val structuredFeatures = featureExtractor.extractFeatures(item, schema)
            val textFeatures = tfidfVectors[index]

            // Concatenate feature vectors
            itemVectors[item.id] = structuredFeatures + textFeatures
        }
    }

    // Build/update user profile from liked items
    fun updateUserProfile(userId: String, ratedItems: Map<String, Float>) {
        val relevantVectors = ratedItems
            .filter { it.value >= 3.5f }  // Only positive ratings
            .mapNotNull { (itemId, rating) ->
                itemVectors[itemId]?.let { vec ->
                    FloatArray(vec.size) { i -> vec[i] * rating }  // Weight by rating
                }
            }

        if (relevantVectors.isEmpty()) return

        // Average weighted vectors
        val profileSize = relevantVectors.first().size
        val profile = FloatArray(profileSize)

        for (vec in relevantVectors) {
            for (i in profile.indices) profile[i] += vec[i]
        }

        val totalWeight = ratedItems.values.filter { it >= 3.5f }.sum()
        for (i in profile.indices) profile[i] /= totalWeight

        userProfiles[userId] = profile
    }

    // Recommend items for a user
    fun recommend(userId: String, n: Int = 10, excludeIds: Set<String> = emptySet()): List<ContentRecommendation> {
        val profile = userProfiles[userId] ?: return emptyList()

        return itemVectors
            .filter { it.key !in excludeIds }
            .map { (itemId, itemVector) ->
                ContentRecommendation(
                    item = items[itemId]!!,
                    score = SimilarityUtils.cosineSimilarity(profile, itemVector)
                )
            }
            .sortedByDescending { it.score }
            .take(n)
    }

    // Find similar items
    fun findSimilarItems(itemId: String, n: Int = 5): List<ContentRecommendation> {
        val targetVector = itemVectors[itemId] ?: return emptyList()

        return itemVectors
            .filter { it.key != itemId }
            .map { (id, vector) ->
                ContentRecommendation(
                    item = items[id]!!,
                    score = SimilarityUtils.cosineSimilarity(targetVector, vector)
                )
            }
            .sortedByDescending { it.score }
            .take(n)
    }

    data class ContentRecommendation(val item: Item, val score: Float)
}
```

---

## ViewModel

```kotlin
class ContentRecViewModel(private val recommender: ContentBasedRecommender) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<ContentBasedRecommender.ContentRecommendation>>(emptyList())
    val recommendations: StateFlow<List<ContentBasedRecommender.ContentRecommendation>> = _recommendations

    private val _similarItems = MutableStateFlow<List<ContentBasedRecommender.ContentRecommendation>>(emptyList())
    val similarItems: StateFlow<List<ContentBasedRecommender.ContentRecommendation>> = _similarItems

    fun loadRecommendations(userId: String, ratedItems: Map<String, Float>) {
        viewModelScope.launch(Dispatchers.Default) {
            recommender.updateUserProfile(userId, ratedItems)
            _recommendations.value = recommender.recommend(userId, 10, ratedItems.keys)
        }
    }

    fun loadSimilarItems(itemId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _similarItems.value = recommender.findSimilarItems(itemId)
        }
    }
}
```
