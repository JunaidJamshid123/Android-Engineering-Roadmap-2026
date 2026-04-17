# Collaborative Filtering — Recommendation Systems

## Overview

Collaborative filtering recommends items based on the collective behavior of many users. It exploits the idea that users who agreed in the past will agree in the future.

```
Collaborative Filtering Theory:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Core Principle: "People who liked similar things to you     │
│  will probably like things you haven't seen yet"             │
│                                                              │
│  User-Item Rating Matrix:                                    │
│  ┌──────────┬─────┬─────┬─────┬─────┬─────┐                │
│  │          │ M1  │ M2  │ M3  │ M4  │ M5  │                │
│  ├──────────┼─────┼─────┼─────┼─────┼─────┤                │
│  │ Alice    │  5  │  4  │  ?  │  1  │  ?  │                │
│  │ Bob      │  4  │  5  │  4  │  ?  │  2  │                │
│  │ Charlie  │  ?  │  ?  │  5  │  3  │  4  │                │
│  │ Diana    │  1  │  2  │  ?  │  5  │  5  │                │
│  └──────────┴─────┴─────┴─────┴─────┴─────┘                │
│                                                              │
│  Two Approaches:                                             │
│                                                              │
│  ┌─────────────────────┐   ┌──────────────────────┐         │
│  │ USER-BASED          │   │ ITEM-BASED           │         │
│  │                     │   │                      │         │
│  │ "Users like you     │   │ "Items similar to    │         │
│  │  also liked..."     │   │  what you liked..."  │         │
│  │                     │   │                      │         │
│  │ Find similar users  │   │ Find similar items   │         │
│  │ → recommend their   │   │ → recommend items    │         │
│  │   favorites         │   │   similar to user's  │         │
│  │                     │   │   favorites           │         │
│  │ Better for:         │   │ Better for:          │         │
│  │ • Few items         │   │ • Many items          │         │
│  │ • Dynamic prefs     │   │ • Stable patterns    │         │
│  │ • Small user base   │   │ • Large user base    │         │
│  └─────────────────────┘   └──────────────────────┘         │
│                                                              │
│  Similarity Measures:                                        │
│  ┌──────────────────────────────────────────────────┐       │
│  │                                                  │       │
│  │ Cosine Similarity:                               │       │
│  │        A · B           Σ(ai × bi)                │       │
│  │ cos = ─────── = ─────────────────────────        │       │
│  │       |A||B|   √(Σai²) × √(Σbi²)                │       │
│  │                                                  │       │
│  │ Pearson Correlation:                             │       │
│  │          Σ(ai - ā)(bi - b̄)                       │       │
│  │ r = ────────────────────────────                  │       │
│  │     √(Σ(ai-ā)²) × √(Σ(bi-b̄)²)                  │       │
│  │                                                  │       │
│  │ Jaccard Similarity:                              │       │
│  │        |A ∩ B|                                    │       │
│  │ J = ───────────                                   │       │
│  │      |A ∪ B|                                      │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  Cold Start Problem:                                        │
│  ┌─────────────────────────────────────────────┐            │
│  │ New user? → No history → Can't find similar │            │
│  │ Solution: Ask preferences, use demographics │            │
│  │                                             │            │
│  │ New item? → No ratings → Can't recommend    │            │
│  │ Solution: Content-based fallback            │            │
│  └─────────────────────────────────────────────┘            │
└──────────────────────────────────────────────────────────────┘
```

---

## Gradle Setup

```kotlin
// app/build.gradle.kts
dependencies {
    // Room for local storage
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Math operations
    implementation("org.apache.commons:commons-math3:3.6.1")

    // Retrofit for API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
}
```

---

## 1. User-Based Collaborative Filtering

```kotlin
class UserBasedCF {

    // User-Item rating matrix: Map<UserId, Map<ItemId, Rating>>
    private val ratings = mutableMapOf<String, MutableMap<String, Float>>()

    fun addRating(userId: String, itemId: String, rating: Float) {
        ratings.getOrPut(userId) { mutableMapOf() }[itemId] = rating
    }

    fun addRatings(userId: String, itemRatings: Map<String, Float>) {
        ratings.getOrPut(userId) { mutableMapOf() }.putAll(itemRatings)
    }

    // Cosine similarity between two users
    fun cosineSimilarity(user1: String, user2: String): Float {
        val ratings1 = ratings[user1] ?: return 0f
        val ratings2 = ratings[user2] ?: return 0f

        // Find common items
        val commonItems = ratings1.keys.intersect(ratings2.keys)
        if (commonItems.isEmpty()) return 0f

        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f

        for (item in commonItems) {
            val r1 = ratings1[item]!!
            val r2 = ratings2[item]!!
            dotProduct += r1 * r2
            norm1 += r1 * r1
            norm2 += r2 * r2
        }

        val denominator = kotlin.math.sqrt(norm1) * kotlin.math.sqrt(norm2)
        return if (denominator > 0) dotProduct / denominator else 0f
    }

    // Pearson correlation between two users
    fun pearsonCorrelation(user1: String, user2: String): Float {
        val ratings1 = ratings[user1] ?: return 0f
        val ratings2 = ratings[user2] ?: return 0f

        val commonItems = ratings1.keys.intersect(ratings2.keys)
        if (commonItems.size < 2) return 0f

        val mean1 = commonItems.map { ratings1[it]!! }.average().toFloat()
        val mean2 = commonItems.map { ratings2[it]!! }.average().toFloat()

        var numerator = 0f
        var denom1 = 0f
        var denom2 = 0f

        for (item in commonItems) {
            val diff1 = ratings1[item]!! - mean1
            val diff2 = ratings2[item]!! - mean2
            numerator += diff1 * diff2
            denom1 += diff1 * diff1
            denom2 += diff2 * diff2
        }

        val denominator = kotlin.math.sqrt(denom1) * kotlin.math.sqrt(denom2)
        return if (denominator > 0) numerator / denominator else 0f
    }

    // Find K most similar users
    fun findSimilarUsers(userId: String, k: Int = 10): List<SimilarUser> {
        return ratings.keys
            .filter { it != userId }
            .map { otherUser ->
                SimilarUser(otherUser, pearsonCorrelation(userId, otherUser))
            }
            .filter { it.similarity > 0 }
            .sortedByDescending { it.similarity }
            .take(k)
    }

    // Predict rating for an item
    fun predictRating(userId: String, itemId: String, k: Int = 10): Float {
        val userRatings = ratings[userId] ?: return 0f
        val userMean = userRatings.values.average().toFloat()

        val similarUsers = findSimilarUsers(userId, k)
            .filter { ratings[it.userId]?.containsKey(itemId) == true }

        if (similarUsers.isEmpty()) return userMean

        var weightedSum = 0f
        var similaritySum = 0f

        for (similar in similarUsers) {
            val neighborRatings = ratings[similar.userId]!!
            val neighborMean = neighborRatings.values.average().toFloat()
            val neighborRating = neighborRatings[itemId]!!

            weightedSum += similar.similarity * (neighborRating - neighborMean)
            similaritySum += kotlin.math.abs(similar.similarity)
        }

        return userMean + (if (similaritySum > 0) weightedSum / similaritySum else 0f)
    }

    // Get top-N recommendations
    fun recommend(userId: String, n: Int = 10, k: Int = 10): List<Recommendation> {
        val userRatings = ratings[userId] ?: return emptyList()

        // Find all items the user hasn't rated
        val allItems = ratings.values.flatMap { it.keys }.toSet()
        val unratedItems = allItems - userRatings.keys

        return unratedItems.map { itemId ->
            Recommendation(itemId, predictRating(userId, itemId, k))
        }
        .filter { it.predictedRating > 0 }
        .sortedByDescending { it.predictedRating }
        .take(n)
    }

    data class SimilarUser(val userId: String, val similarity: Float)
    data class Recommendation(val itemId: String, val predictedRating: Float)
}
```

---

## 2. Item-Based Collaborative Filtering

```kotlin
class ItemBasedCF {

    private val ratings = mutableMapOf<String, MutableMap<String, Float>>()
    private val itemSimilarityCache = mutableMapOf<Pair<String, String>, Float>()

    fun addRating(userId: String, itemId: String, rating: Float) {
        ratings.getOrPut(userId) { mutableMapOf() }[itemId] = rating
    }

    // Get all ratings for a specific item
    private fun getItemRatings(itemId: String): Map<String, Float> {
        return ratings.filter { it.value.containsKey(itemId) }
            .mapValues { it.value[itemId]!! }
    }

    // Adjusted cosine similarity between two items
    fun itemSimilarity(item1: String, item2: String): Float {
        val cacheKey = if (item1 < item2) item1 to item2 else item2 to item1
        itemSimilarityCache[cacheKey]?.let { return it }

        val ratings1 = getItemRatings(item1)
        val ratings2 = getItemRatings(item2)
        val commonUsers = ratings1.keys.intersect(ratings2.keys)

        if (commonUsers.size < 2) return 0f

        var numerator = 0f
        var denom1 = 0f
        var denom2 = 0f

        for (user in commonUsers) {
            // Adjusted: subtract user's mean rating
            val userMean = ratings[user]!!.values.average().toFloat()
            val r1 = ratings1[user]!! - userMean
            val r2 = ratings2[user]!! - userMean

            numerator += r1 * r2
            denom1 += r1 * r1
            denom2 += r2 * r2
        }

        val denominator = kotlin.math.sqrt(denom1) * kotlin.math.sqrt(denom2)
        val similarity = if (denominator > 0) numerator / denominator else 0f

        itemSimilarityCache[cacheKey] = similarity
        return similarity
    }

    // Predict rating using item-item similarity
    fun predictRating(userId: String, targetItem: String, k: Int = 10): Float {
        val userRatings = ratings[userId] ?: return 0f

        // Find k most similar items that this user has rated
        val similarItems = userRatings.keys
            .filter { it != targetItem }
            .map { ratedItem ->
                ratedItem to itemSimilarity(targetItem, ratedItem)
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(k)

        if (similarItems.isEmpty()) return 0f

        var weightedSum = 0f
        var similaritySum = 0f

        for ((ratedItem, similarity) in similarItems) {
            weightedSum += similarity * userRatings[ratedItem]!!
            similaritySum += kotlin.math.abs(similarity)
        }

        return if (similaritySum > 0) weightedSum / similaritySum else 0f
    }

    // Recommend items
    fun recommend(userId: String, n: Int = 10): List<Recommendation> {
        val userRatings = ratings[userId] ?: return emptyList()
        val allItems = ratings.values.flatMap { it.keys }.toSet()
        val unratedItems = allItems - userRatings.keys

        return unratedItems.map { itemId ->
            Recommendation(itemId, predictRating(userId, itemId))
        }
        .filter { it.predictedRating > 0 }
        .sortedByDescending { it.predictedRating }
        .take(n)
    }

    data class Recommendation(val itemId: String, val predictedRating: Float)
}
```

---

## 3. Matrix Factorization (SVD / ALS)

```
Matrix Factorization Theory:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Decompose R (User×Item) into two low-rank matrices:        │
│                                                              │
│  R ≈ U × V^T                                                │
│                                                              │
│  ┌─────────────────┐   ┌────────┐   ┌─────────────────┐    │
│  │  R (m × n)     │ ≈ │U (m×k) │ × │ V^T (k × n)    │    │
│  │  User × Item   │   │User×   │   │ Factor × Item   │    │
│  │                 │   │Factor  │   │                  │    │
│  │  5 4 ? 1 ?     │   │.2  .8  │   │ .9 .7 .8 .1 .2 │    │
│  │  4 5 4 ? 2     │   │.3  .7  │   │ .1 .3 .2 .9 .8 │    │
│  │  ? ? 5 3 4     │   │.8  .3  │                      │    │
│  │  1 2 ? 5 5     │   │.7  .2  │                      │    │
│  └─────────────────┘   └────────┘   └─────────────────┘    │
│                                                              │
│  k = latent factors (typically 10-200)                      │
│                                                              │
│  Latent factors capture hidden patterns:                    │
│  • Factor 1 might represent "action vs romance"             │
│  • Factor 2 might represent "mainstream vs indie"           │
│  • Factor 3 might represent "old vs new"                    │
│                                                              │
│  Training: Minimize                                         │
│  L = Σ (r_ij - u_i · v_j)² + λ(||u_i||² + ||v_j||²)      │
│       known                     regularization               │
│                                                              │
│  Methods:                                                   │
│  • SGD: Gradient descent on each known rating              │
│  • ALS: Alternating Least Squares — fix U, solve V;       │
│         fix V, solve U; repeat                              │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class MatrixFactorization(
    private val numFactors: Int = 20,
    private val learningRate: Float = 0.01f,
    private val regularization: Float = 0.02f,
    private val epochs: Int = 100
) {
    // Latent factor matrices
    private lateinit var userFactors: Array<FloatArray>  // U: users × factors
    private lateinit var itemFactors: Array<FloatArray>  // V: items × factors
    private lateinit var userBias: FloatArray
    private lateinit var itemBias: FloatArray
    private var globalMean: Float = 0f

    private val userIndex = mutableMapOf<String, Int>()
    private val itemIndex = mutableMapOf<String, Int>()

    data class Rating(val userId: String, val itemId: String, val value: Float)

    // Train using Stochastic Gradient Descent
    fun train(ratings: List<Rating>) {
        // Build indices
        ratings.forEach { r ->
            userIndex.getOrPut(r.userId) { userIndex.size }
            itemIndex.getOrPut(r.itemId) { itemIndex.size }
        }

        val numUsers = userIndex.size
        val numItems = itemIndex.size

        // Initialize randomly
        val random = java.util.Random(42)
        userFactors = Array(numUsers) { FloatArray(numFactors) { random.nextFloat() * 0.1f } }
        itemFactors = Array(numItems) { FloatArray(numFactors) { random.nextFloat() * 0.1f } }
        userBias = FloatArray(numUsers)
        itemBias = FloatArray(numItems)
        globalMean = ratings.map { it.value }.average().toFloat()

        // SGD training
        for (epoch in 0 until epochs) {
            var totalError = 0.0

            for (rating in ratings.shuffled()) {
                val u = userIndex[rating.userId]!!
                val i = itemIndex[rating.itemId]!!

                // Predicted rating
                val predicted = predict(u, i)
                val error = rating.value - predicted

                totalError += error * error

                // Update biases
                userBias[u] += learningRate * (error - regularization * userBias[u])
                itemBias[i] += learningRate * (error - regularization * itemBias[i])

                // Update factors
                for (f in 0 until numFactors) {
                    val uf = userFactors[u][f]
                    val vf = itemFactors[i][f]

                    userFactors[u][f] += learningRate * (error * vf - regularization * uf)
                    itemFactors[i][f] += learningRate * (error * uf - regularization * vf)
                }
            }

            val rmse = kotlin.math.sqrt(totalError / ratings.size)
            if (epoch % 10 == 0) {
                android.util.Log.d("MF", "Epoch $epoch, RMSE: $rmse")
            }
        }
    }

    private fun predict(userIdx: Int, itemIdx: Int): Float {
        var dot = globalMean + userBias[userIdx] + itemBias[itemIdx]
        for (f in 0 until numFactors) {
            dot += userFactors[userIdx][f] * itemFactors[itemIdx][f]
        }
        return dot.coerceIn(1f, 5f)
    }

    fun predictRating(userId: String, itemId: String): Float? {
        val u = userIndex[userId] ?: return null
        val i = itemIndex[itemId] ?: return null
        return predict(u, i)
    }

    fun recommend(userId: String, n: Int = 10, excludeRated: Set<String> = emptySet()): List<Pair<String, Float>> {
        val u = userIndex[userId] ?: return emptyList()

        return itemIndex
            .filter { it.key !in excludeRated }
            .map { (itemId, itemIdx) ->
                itemId to predict(u, itemIdx)
            }
            .sortedByDescending { it.second }
            .take(n)
    }
}
```

---

## 4. Integration Patterns

```
Integration Architecture:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌─────────────────────┐                                    │
│  │   Android App       │                                    │
│  │                     │                                    │
│  │  User actions:      │                                    │
│  │  • View item        │────▶ Event Logging API             │
│  │  • Rate item        │                                    │
│  │  • Purchase item    │         │                           │
│  │  • Add to wishlist  │         ▼                           │
│  │                     │  ┌──────────────┐                  │
│  │  Show recs: ◄───────│──│ Rec API      │                  │
│  │  "You might like"   │  │ GET /recs/{u}│                  │
│  └─────────────────────┘  └──────┬───────┘                  │
│                                  │                           │
│                           ┌──────▼───────┐                  │
│                           │ Rec Engine   │                  │
│                           │              │                  │
│                           │ 1. Check     │                  │
│                           │    cache     │                  │
│                           │              │                  │
│                           │ 2. If miss,  │                  │
│                           │    compute   │                  │
│                           │              │                  │
│                           │ 3. Cache &   │                  │
│                           │    return    │                  │
│                           └──────────────┘                  │
│                                                              │
│  API Contract:                                               │
│  GET /api/recommendations/{userId}?limit=10&type=similar    │
│  POST /api/events { userId, itemId, eventType, timestamp }  │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
// --- API Models ---
@JsonClass(generateAdapter = true)
data class RecommendationResponse(
    val recommendations: List<RecommendedItem>,
    val algorithm: String,
    val generatedAt: String
)

@JsonClass(generateAdapter = true)
data class RecommendedItem(
    val itemId: String,
    val title: String,
    val score: Float,
    val reason: String?  // "Because you liked X"
)

@JsonClass(generateAdapter = true)
data class UserEvent(
    val userId: String,
    val itemId: String,
    val eventType: String,  // VIEW, RATE, PURCHASE, WISHLIST
    val value: Float? = null,  // rating value
    val timestamp: Long = System.currentTimeMillis()
)

// --- API Interface ---
interface RecommendationApi {
    @GET("api/recommendations/{userId}")
    suspend fun getRecommendations(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 10,
        @Query("type") type: String = "collaborative"
    ): RecommendationResponse

    @POST("api/events")
    suspend fun logEvent(@Body event: UserEvent)
}

// --- Repository ---
class RecommendationRepository(
    private val api: RecommendationApi,
    private val localCF: UserBasedCF  // Fallback for offline
) {
    // Cache recommendations locally
    private val cache = mutableMapOf<String, Pair<Long, List<RecommendedItem>>>()
    private val cacheTTL = 30 * 60 * 1000L  // 30 minutes

    suspend fun getRecommendations(userId: String, limit: Int = 10): List<RecommendedItem> {
        // Check cache
        cache[userId]?.let { (timestamp, items) ->
            if (System.currentTimeMillis() - timestamp < cacheTTL) {
                return items
            }
        }

        return try {
            val response = api.getRecommendations(userId, limit)
            cache[userId] = System.currentTimeMillis() to response.recommendations
            response.recommendations
        } catch (e: Exception) {
            // Fallback to local collaborative filtering
            localCF.recommend(userId, limit).map { rec ->
                RecommendedItem(
                    itemId = rec.itemId,
                    title = rec.itemId,
                    score = rec.predictedRating,
                    reason = "Based on similar users"
                )
            }
        }
    }

    suspend fun logUserEvent(userId: String, itemId: String, eventType: String, value: Float? = null) {
        try {
            api.logEvent(UserEvent(userId, itemId, eventType, value))
        } catch (e: Exception) {
            // Queue for later sync
            android.util.Log.w("Recs", "Failed to log event, queuing")
        }

        // Also update local model
        if (eventType == "RATE" && value != null) {
            localCF.addRating(userId, itemId, value)
        }
    }
}
```

---

## ViewModel

```kotlin
class RecommendationViewModel(
    private val repo: RecommendationRepository,
    private val userId: String
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<RecommendedItem>>(emptyList())
    val recommendations: StateFlow<List<RecommendedItem>> = _recommendations

    init { loadRecommendations() }

    fun loadRecommendations() {
        viewModelScope.launch {
            _recommendations.value = repo.getRecommendations(userId)
        }
    }

    fun rateItem(itemId: String, rating: Float) {
        viewModelScope.launch {
            repo.logUserEvent(userId, itemId, "RATE", rating)
            loadRecommendations()  // Refresh after rating
        }
    }

    fun viewItem(itemId: String) {
        viewModelScope.launch {
            repo.logUserEvent(userId, itemId, "VIEW")
        }
    }
}
```
