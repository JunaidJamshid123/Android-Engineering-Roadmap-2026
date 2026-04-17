# Hybrid Approaches — Recommendation Systems

## Overview

Hybrid recommenders combine collaborative filtering (CF) and content-based filtering (CBF) to overcome the limitations of each individual approach.

```
Hybrid Recommendation Theory:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Why Hybrid?                                                │
│  ┌──────────────────┬──────────────────┬──────────────────┐ │
│  │ Problem          │ CF handles?      │ CBF handles?     │ │
│  ├──────────────────┼──────────────────┼──────────────────┤ │
│  │ Cold start (user)│ ❌ Need history  │ ❌ Need prefs    │ │
│  │ Cold start (item)│ ❌ Need ratings  │ ✅ Has features  │ │
│  │ Sparsity         │ ❌ Sparse matrix │ ✅ Dense features│ │
│  │ Serendipity      │ ✅ Cross-user    │ ❌ Filter bubble │ │
│  │ Popularity bias  │ ❌ Biased        │ ✅ Feature-based │ │
│  │ Scalability      │ ❌ O(users×items)│ ✅ Per-item      │ │
│  └──────────────────┴──────────────────┴──────────────────┘ │
│                                                              │
│  Hybrid Strategies:                                         │
│                                                              │
│  1. WEIGHTED                                                │
│  ┌──────┐ ── 0.6 ──▶ ┐                                    │
│  │  CF  │             ├──▶ Combined Score                   │
│  └──────┘   ┌──────┐  │                                    │
│             │ CBF  │──┘                                    │
│             └──────┘ ── 0.4 ──▶                            │
│  Score = w₁·CF(u,i) + w₂·CBF(u,i)                         │
│                                                              │
│  2. SWITCHING                                               │
│  ┌──────────────┐                                          │
│  │ Has ratings?  │── Yes ──▶ Use CF                        │
│  │   (user)     │── No  ──▶ Use CBF (content fallback)    │
│  └──────────────┘                                          │
│                                                              │
│  3. CASCADE                                                 │
│  ┌──────┐ Top 100 ┌──────┐ Top 10  ┌────────┐             │
│  │  CF  │────────▶│ CBF  │────────▶│ Result │             │
│  └──────┘ rough   └──────┘ refine  └────────┘             │
│                                                              │
│  4. FEATURE AUGMENTATION                                   │
│  ┌──────┐ features ┌──────┐                                │
│  │ CBF  │─────────▶│  CF  │──▶ Final score                │
│  └──────┘ as input └──────┘                                │
│                                                              │
│  5. META-LEARNING                                          │
│  ┌──────┐ score1 ┐                                         │
│  │  CF  │───────▶├──▶ ML Model ──▶ Final score            │
│  │ CBF  │───────▶│   (learns weights)                      │
│  └──────┘ score2 ┘                                         │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. Combining Multiple Algorithms

### Weighted Hybrid

```kotlin
class WeightedHybridRecommender(
    private val collaborativeCF: UserBasedCF,
    private val contentBased: ContentBasedRecommender,
    private var cfWeight: Float = 0.6f,
    private var cbfWeight: Float = 0.4f
) {
    init {
        require(kotlin.math.abs(cfWeight + cbfWeight - 1.0f) < 0.01f) {
            "Weights must sum to 1.0"
        }
    }

    fun recommend(
        userId: String,
        ratedItems: Map<String, Float>,
        n: Int = 10
    ): List<HybridRecommendation> {
        // Get CF recommendations
        val cfRecs = collaborativeCF.recommend(userId, n * 2)
            .associate { it.itemId to it.predictedRating }

        // Get CBF recommendations
        contentBased.updateUserProfile(userId, ratedItems)
        val cbfRecs = contentBased.recommend(userId, n * 2, ratedItems.keys)
            .associate { it.item.id to it.score }

        // Combine scores
        val allItemIds = cfRecs.keys + cbfRecs.keys

        return allItemIds.map { itemId ->
            val cfScore = cfRecs[itemId] ?: 0f
            val cbfScore = cbfRecs[itemId] ?: 0f

            // Normalize CF score to 0-1 range (from 1-5 rating)
            val normalizedCF = (cfScore - 1f) / 4f

            val combinedScore = cfWeight * normalizedCF + cbfWeight * cbfScore

            HybridRecommendation(
                itemId = itemId,
                score = combinedScore,
                cfScore = cfScore,
                cbfScore = cbfScore,
                source = when {
                    cfScore > 0 && cbfScore > 0 -> "hybrid"
                    cfScore > 0 -> "collaborative"
                    else -> "content"
                }
            )
        }
        .sortedByDescending { it.score }
        .take(n)
    }

    data class HybridRecommendation(
        val itemId: String,
        val score: Float,
        val cfScore: Float,
        val cbfScore: Float,
        val source: String
    )
}
```

### Switching Hybrid

```kotlin
class SwitchingHybridRecommender(
    private val collaborativeCF: UserBasedCF,
    private val contentBased: ContentBasedRecommender,
    private val minRatingsForCF: Int = 5  // Threshold to switch
) {
    fun recommend(
        userId: String,
        ratedItems: Map<String, Float>,
        n: Int = 10
    ): List<SwitchRecommendation> {
        // Decide which algorithm to use
        val strategy = if (ratedItems.size >= minRatingsForCF) {
            RecommendationStrategy.COLLABORATIVE
        } else {
            RecommendationStrategy.CONTENT_BASED
        }

        return when (strategy) {
            RecommendationStrategy.COLLABORATIVE -> {
                val cfRecs = collaborativeCF.recommend(userId, n)
                cfRecs.map {
                    SwitchRecommendation(it.itemId, it.predictedRating, "collaborative")
                }
            }
            RecommendationStrategy.CONTENT_BASED -> {
                contentBased.updateUserProfile(userId, ratedItems)
                val cbfRecs = contentBased.recommend(userId, n, ratedItems.keys)
                cbfRecs.map {
                    SwitchRecommendation(it.item.id, it.score, "content")
                }
            }
        }
    }

    enum class RecommendationStrategy { COLLABORATIVE, CONTENT_BASED }

    data class SwitchRecommendation(
        val itemId: String,
        val score: Float,
        val strategy: String
    )
}
```

### Cascade Hybrid

```kotlin
class CascadeHybridRecommender(
    private val collaborativeCF: UserBasedCF,
    private val contentBased: ContentBasedRecommender
) {
    fun recommend(
        userId: String,
        ratedItems: Map<String, Float>,
        roughCutN: Int = 50,
        finalN: Int = 10
    ): List<CascadeRecommendation> {
        // Stage 1: CF produces rough candidates
        val cfCandidates = collaborativeCF.recommend(userId, roughCutN)
            .map { it.itemId }
            .toSet()

        // Stage 2: CBF re-ranks the candidates
        contentBased.updateUserProfile(userId, ratedItems)
        val allCBFRecs = contentBased.recommend(userId, roughCutN * 2, ratedItems.keys)

        // Filter CBF recs to only those in CF candidates
        return allCBFRecs
            .filter { it.item.id in cfCandidates }
            .take(finalN)
            .map {
                CascadeRecommendation(
                    itemId = it.item.id,
                    score = it.score,
                    stage1 = "collaborative_filter",
                    stage2 = "content_rerank"
                )
            }
    }

    data class CascadeRecommendation(
        val itemId: String,
        val score: Float,
        val stage1: String,
        val stage2: String
    )
}
```

---

## 2. A/B Testing Recommendations

```
A/B Testing Architecture:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌─────────┐    ┌───────────────┐                           │
│  │ User    │───▶│ Assignment    │                           │
│  │ Request │    │ Service       │                           │
│  └─────────┘    │               │                           │
│                 │ hash(userId)  │                           │
│                 │ % 100         │                           │
│                 └───────┬───────┘                           │
│                         │                                   │
│           ┌─────────────┼──────────────┐                   │
│           ▼             ▼              ▼                   │
│    ┌──────────┐  ┌──────────┐  ┌──────────────┐           │
│    │ Group A  │  │ Group B  │  │ Group C      │           │
│    │ (50%)    │  │ (30%)    │  │ (20%)        │           │
│    │          │  │          │  │              │           │
│    │ CF Only  │  │ Hybrid   │  │ CBF Only     │           │
│    └────┬─────┘  └────┬─────┘  └──────┬───────┘           │
│         │             │               │                    │
│         └─────────────┼───────────────┘                    │
│                       ▼                                    │
│              ┌─────────────────┐                           │
│              │ Metrics Tracker │                           │
│              │                 │                           │
│              │ • Click-through │                           │
│              │ • Conversion    │                           │
│              │ • Engagement    │                           │
│              │ • Diversity     │                           │
│              │ • Revenue       │                           │
│              └─────────────────┘                           │
│                                                              │
│  Key Metrics:                                               │
│  ┌──────────────────────────────────────────────────┐       │
│  │ CTR = clicks / impressions                       │       │
│  │ Precision@K = relevant in top K / K              │       │
│  │ NDCG = quality of ranking                        │       │
│  │ Coverage = % of items ever recommended           │       │
│  │ Diversity = avg dissimilarity in rec list        │       │
│  └──────────────────────────────────────────────────┘       │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class ABTestRecommendationEngine(
    private val cfRecommender: UserBasedCF,
    private val contentRecommender: ContentBasedRecommender,
    private val hybridRecommender: WeightedHybridRecommender
) {
    // Experiment configuration
    data class Experiment(
        val id: String,
        val name: String,
        val variants: List<Variant>,
        val isActive: Boolean = true
    )

    data class Variant(
        val id: String,
        val name: String,
        val algorithm: String,  // "cf", "cbf", "hybrid"
        val weight: Int         // Traffic percentage
    )

    // Deterministic assignment based on userId
    fun assignVariant(userId: String, experiment: Experiment): Variant {
        val hash = userId.hashCode().toLong() and 0xFFFFFFFFL
        val bucket = (hash % 100).toInt()

        var cumulative = 0
        for (variant in experiment.variants) {
            cumulative += variant.weight
            if (bucket < cumulative) return variant
        }
        return experiment.variants.last()
    }

    // Get recommendations based on assigned variant
    fun getRecommendations(
        userId: String,
        ratedItems: Map<String, Float>,
        experiment: Experiment,
        n: Int = 10
    ): ABTestResult {
        val variant = assignVariant(userId, experiment)
        val startTime = System.currentTimeMillis()

        val recommendations = when (variant.algorithm) {
            "cf" -> cfRecommender.recommend(userId, n).map {
                RecItem(it.itemId, it.predictedRating)
            }
            "cbf" -> {
                contentRecommender.updateUserProfile(userId, ratedItems)
                contentRecommender.recommend(userId, n, ratedItems.keys).map {
                    RecItem(it.item.id, it.score)
                }
            }
            "hybrid" -> hybridRecommender.recommend(userId, ratedItems, n).map {
                RecItem(it.itemId, it.score)
            }
            else -> emptyList()
        }

        val latency = System.currentTimeMillis() - startTime

        return ABTestResult(
            experimentId = experiment.id,
            variantId = variant.id,
            algorithm = variant.algorithm,
            recommendations = recommendations,
            latencyMs = latency
        )
    }

    data class RecItem(val itemId: String, val score: Float)

    data class ABTestResult(
        val experimentId: String,
        val variantId: String,
        val algorithm: String,
        val recommendations: List<RecItem>,
        val latencyMs: Long
    )

    // Track user interaction for metrics
    fun trackInteraction(
        userId: String,
        experimentId: String,
        variantId: String,
        itemId: String,
        action: String  // "impression", "click", "purchase", "dismiss"
    ): InteractionEvent {
        return InteractionEvent(
            userId = userId,
            experimentId = experimentId,
            variantId = variantId,
            itemId = itemId,
            action = action,
            timestamp = System.currentTimeMillis()
        )
    }

    data class InteractionEvent(
        val userId: String,
        val experimentId: String,
        val variantId: String,
        val itemId: String,
        val action: String,
        val timestamp: Long
    )
}

// Metrics calculator
class RecommendationMetrics {

    // Click-Through Rate
    fun calculateCTR(impressions: Int, clicks: Int): Float {
        return if (impressions > 0) clicks.toFloat() / impressions else 0f
    }

    // Precision@K — fraction of recommended items that are relevant
    fun precisionAtK(recommended: List<String>, relevant: Set<String>, k: Int): Float {
        val topK = recommended.take(k)
        val hits = topK.count { it in relevant }
        return hits.toFloat() / k
    }

    // Recall@K — fraction of relevant items found in top K
    fun recallAtK(recommended: List<String>, relevant: Set<String>, k: Int): Float {
        val topK = recommended.take(k)
        val hits = topK.count { it in relevant }
        return if (relevant.isNotEmpty()) hits.toFloat() / relevant.size else 0f
    }

    // NDCG@K — Normalized Discounted Cumulative Gain
    fun ndcgAtK(recommended: List<String>, relevanceScores: Map<String, Float>, k: Int): Float {
        val topK = recommended.take(k)

        // DCG
        var dcg = 0.0
        for (i in topK.indices) {
            val rel = relevanceScores[topK[i]] ?: 0f
            dcg += (kotlin.math.pow(2.0, rel.toDouble()) - 1) / kotlin.math.ln(i + 2.0)
        }

        // Ideal DCG
        val idealOrder = relevanceScores.values.sortedDescending().take(k)
        var idcg = 0.0
        for (i in idealOrder.indices) {
            idcg += (kotlin.math.pow(2.0, idealOrder[i].toDouble()) - 1) / kotlin.math.ln(i + 2.0)
        }

        return if (idcg > 0) (dcg / idcg).toFloat() else 0f
    }

    // Coverage — percentage of items ever recommended
    fun catalogCoverage(allRecommended: Set<String>, catalog: Set<String>): Float {
        return if (catalog.isNotEmpty()) allRecommended.size.toFloat() / catalog.size else 0f
    }
}
```

---

## Complete ViewModel

```kotlin
class HybridRecViewModel(
    private val abTestEngine: ABTestRecommendationEngine,
    private val userId: String
) : ViewModel() {

    private val _results = MutableStateFlow<ABTestRecommendationEngine.ABTestResult?>(null)
    val results: StateFlow<ABTestRecommendationEngine.ABTestResult?> = _results

    private val experiment = ABTestRecommendationEngine.Experiment(
        id = "rec_v2_2026",
        name = "Hybrid vs CF vs CBF",
        variants = listOf(
            ABTestRecommendationEngine.Variant("A", "CF Only", "cf", 40),
            ABTestRecommendationEngine.Variant("B", "Hybrid", "hybrid", 40),
            ABTestRecommendationEngine.Variant("C", "CBF Only", "cbf", 20)
        )
    )

    fun loadRecommendations(ratedItems: Map<String, Float>) {
        viewModelScope.launch(Dispatchers.Default) {
            _results.value = abTestEngine.getRecommendations(
                userId, ratedItems, experiment
            )
        }
    }

    fun trackClick(itemId: String) {
        val result = _results.value ?: return
        viewModelScope.launch {
            abTestEngine.trackInteraction(
                userId, result.experimentId, result.variantId, itemId, "click"
            )
        }
    }
}
```
