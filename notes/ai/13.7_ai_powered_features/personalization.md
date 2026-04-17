# Personalization — AI-Powered Features

## Overview

AI-powered personalization adapts app content, UI, and behavior to individual users based on their tracked behavior, learned preferences, and contextual signals — while preserving privacy.

```
Personalization Architecture:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                 User Signals                           │  │
│  │                                                       │  │
│  │  Explicit:              Implicit:                     │  │
│  │  • Ratings ★★★★☆       • View duration               │  │
│  │  • Preferences          • Scroll depth                │  │
│  │  • Saved/Bookmarked     • Click patterns              │  │
│  │  • Categories chosen    • Search queries              │  │
│  │  • Language selection   • Purchase history            │  │
│  │                         • Time of day                 │  │
│  │                         • Location context            │  │
│  │                         • Device type                 │  │
│  └────────────────────────┬──────────────────────────────┘  │
│                           │                                  │
│                           ▼                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              User Profile Engine                      │  │
│  │                                                       │  │
│  │  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐ │  │
│  │  │ Preference  │  │ Behavior     │  │ Context     │ │  │
│  │  │ Model       │  │ Model        │  │ Model       │ │  │
│  │  │             │  │              │  │             │ │  │
│  │  │ Categories  │  │ Engagement   │  │ Time-based  │ │  │
│  │  │ weights     │  │ patterns     │  │ patterns    │ │  │
│  │  └─────────────┘  └──────────────┘  └─────────────┘ │  │
│  └────────────────────────┬──────────────────────────────┘  │
│                           │                                  │
│                           ▼                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Personalization Outputs                   │  │
│  │                                                       │  │
│  │  • Content ranking & recommendations                 │  │
│  │  • Dynamic UI layout                                 │  │
│  │  • Notification timing                               │  │
│  │  • Feature prioritization                            │  │
│  │  • Search result personalization                     │  │
│  │  • Ad targeting (if applicable)                      │  │
│  └───────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. User Behavior Tracking

```kotlin
import androidx.room.*

// --- Events Database ---
@Entity(tableName = "user_events")
data class UserEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,       // VIEW, CLICK, SCROLL, SEARCH, PURCHASE
    val itemId: String?,
    val itemCategory: String?,
    val metadata: String?,       // JSON extra data
    val duration: Long? = null,  // milliseconds spent
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface UserEventDao {
    @Insert
    suspend fun insert(event: UserEventEntity)

    @Query("SELECT * FROM user_events WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getEventsSince(since: Long): List<UserEventEntity>

    @Query("SELECT itemCategory, COUNT(*) as count FROM user_events WHERE eventType = :type AND timestamp > :since GROUP BY itemCategory ORDER BY count DESC")
    suspend fun getCategoryCounts(type: String, since: Long): List<CategoryCount>

    @Query("SELECT AVG(duration) FROM user_events WHERE eventType = 'VIEW' AND itemCategory = :category AND timestamp > :since")
    suspend fun getAvgViewDuration(category: String, since: Long): Float?

    @Query("DELETE FROM user_events WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}

data class CategoryCount(val itemCategory: String, val count: Int)

@Database(entities = [UserEventEntity::class], version = 1)
abstract class PersonalizationDatabase : RoomDatabase() {
    abstract fun eventDao(): UserEventDao
}

// --- Event Tracker ---
class UserBehaviorTracker(private val dao: UserEventDao) {

    suspend fun trackView(itemId: String, category: String, durationMs: Long) {
        dao.insert(UserEventEntity(
            eventType = "VIEW",
            itemId = itemId,
            itemCategory = category,
            duration = durationMs
        ))
    }

    suspend fun trackClick(itemId: String, category: String) {
        dao.insert(UserEventEntity(
            eventType = "CLICK",
            itemId = itemId,
            itemCategory = category
        ))
    }

    suspend fun trackSearch(query: String) {
        dao.insert(UserEventEntity(
            eventType = "SEARCH",
            itemId = null,
            itemCategory = null,
            metadata = query
        ))
    }

    suspend fun trackPurchase(itemId: String, category: String, price: Float) {
        dao.insert(UserEventEntity(
            eventType = "PURCHASE",
            itemId = itemId,
            itemCategory = category,
            metadata = """{"price": $price}"""
        ))
    }

    suspend fun trackScroll(screenName: String, depthPercent: Float) {
        dao.insert(UserEventEntity(
            eventType = "SCROLL",
            itemId = screenName,
            itemCategory = null,
            metadata = """{"depth": $depthPercent}"""
        ))
    }

    // Cleanup old events (keep last 90 days)
    suspend fun cleanup() {
        val ninetyDaysAgo = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
        dao.deleteOlderThan(ninetyDaysAgo)
    }
}
```

---

## 2. Preference Learning

```
Preference Learning Model:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  User Preference Vector (learned from behavior):            │
│                                                              │
│  Category Weights (decay-weighted by recency):              │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Technology    ████████████████████  0.82          │       │
│  │ Science       ███████████████      0.65          │       │
│  │ Sports        ████████             0.35          │       │
│  │ Entertainment ██████               0.28          │       │
│  │ Fashion       ███                  0.12          │       │
│  │ Cooking       ██                   0.08          │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  Time-Decay Formula:                                        │
│  weight(event) = base_weight × e^(-λ × age_in_days)        │
│                                                              │
│  Event Type Weights:                                        │
│  ┌──────────────┬────────┐                                  │
│  │ Event        │ Weight │                                  │
│  ├──────────────┼────────┤                                  │
│  │ Purchase     │ 5.0    │                                  │
│  │ Bookmark     │ 3.0    │                                  │
│  │ Long View    │ 2.0    │ (> median view time)             │
│  │ Click        │ 1.0    │                                  │
│  │ Short View   │ 0.5    │                                  │
│  │ Scroll past  │ -0.2   │ (negative signal)               │
│  └──────────────┴────────┘                                  │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class PreferenceLearner(private val dao: UserEventDao) {

    // Event weights
    private val eventWeights = mapOf(
        "PURCHASE" to 5.0f,
        "BOOKMARK" to 3.0f,
        "CLICK" to 1.0f,
        "VIEW" to 0.5f,
        "SCROLL" to -0.2f
    )

    // Time decay factor (λ)
    private val decayRate = 0.02f // ~50% weight after 35 days

    // Learn user preference vector from events
    suspend fun learnPreferences(lookbackDays: Int = 90): UserPreferences {
        val since = System.currentTimeMillis() - lookbackDays.toLong() * 24 * 60 * 60 * 1000
        val events = dao.getEventsSince(since)

        val categoryScores = mutableMapOf<String, Float>()
        val now = System.currentTimeMillis()

        for (event in events) {
            val category = event.itemCategory ?: continue
            val baseWeight = eventWeights[event.eventType] ?: 0.5f

            // Time decay
            val ageDays = (now - event.timestamp) / (24.0 * 60 * 60 * 1000)
            val timeDecay = kotlin.math.exp(-decayRate * ageDays).toFloat()

            // Duration bonus for views
            val durationBonus = if (event.eventType == "VIEW" && event.duration != null) {
                if (event.duration > 30000) 1.5f  // > 30s = engaged
                else if (event.duration > 10000) 1.0f
                else 0.5f  // quick glance
            } else 1.0f

            val score = baseWeight * timeDecay * durationBonus
            categoryScores[category] = (categoryScores[category] ?: 0f) + score
        }

        // Normalize to 0-1
        val maxScore = categoryScores.values.maxOrNull() ?: 1f
        val normalizedPrefs = categoryScores.mapValues { (_, score) ->
            (score / maxScore).coerceIn(0f, 1f)
        }

        // Extract search interests
        val searchTerms = events
            .filter { it.eventType == "SEARCH" }
            .mapNotNull { it.metadata }
            .takeLast(50)

        return UserPreferences(
            categoryWeights = normalizedPrefs,
            recentSearches = searchTerms,
            totalEvents = events.size,
            mostActiveHour = findMostActiveHour(events),
            engagementLevel = calculateEngagement(events)
        )
    }

    private fun findMostActiveHour(events: List<UserEventEntity>): Int {
        val hourCounts = IntArray(24)
        val calendar = java.util.Calendar.getInstance()
        events.forEach { event ->
            calendar.timeInMillis = event.timestamp
            hourCounts[calendar.get(java.util.Calendar.HOUR_OF_DAY)]++
        }
        return hourCounts.indices.maxByOrNull { hourCounts[it] } ?: 12
    }

    private fun calculateEngagement(events: List<UserEventEntity>): EngagementLevel {
        val last7Days = events.count {
            System.currentTimeMillis() - it.timestamp < 7L * 24 * 60 * 60 * 1000
        }
        return when {
            last7Days > 50 -> EngagementLevel.HIGH
            last7Days > 20 -> EngagementLevel.MEDIUM
            last7Days > 5 -> EngagementLevel.LOW
            else -> EngagementLevel.INACTIVE
        }
    }

    data class UserPreferences(
        val categoryWeights: Map<String, Float>,
        val recentSearches: List<String>,
        val totalEvents: Int,
        val mostActiveHour: Int,
        val engagementLevel: EngagementLevel
    )

    enum class EngagementLevel { HIGH, MEDIUM, LOW, INACTIVE }
}
```

---

## 3. Dynamic Content Adaptation

```kotlin
class ContentPersonalizer(private val preferenceLearner: PreferenceLearner) {

    // Personalize a list of items by re-ranking
    suspend fun personalizeContent(
        items: List<ContentItem>,
        contextSignals: ContextSignals? = null
    ): List<ContentItem> {
        val prefs = preferenceLearner.learnPreferences()

        return items.map { item ->
            val personalScore = calculatePersonalScore(item, prefs, contextSignals)
            item.copy(personalizedScore = personalScore)
        }.sortedByDescending { it.personalizedScore }
    }

    private fun calculatePersonalScore(
        item: ContentItem,
        prefs: PreferenceLearner.UserPreferences,
        context: ContextSignals?
    ): Float {
        var score = 0f

        // Category preference match
        val categoryWeight = prefs.categoryWeights[item.category] ?: 0.1f
        score += categoryWeight * 0.5f

        // Freshness bonus
        val ageHours = (System.currentTimeMillis() - item.publishedAt) / (60.0 * 60 * 1000)
        val freshnessScore = (1.0 / (1.0 + ageHours / 24.0)).toFloat()
        score += freshnessScore * 0.2f

        // Popularity signal
        score += (item.popularityScore ?: 0f) * 0.15f

        // Context signals
        context?.let {
            // Time-of-day relevance
            if (item.bestTimeOfDay != null && it.hourOfDay in item.bestTimeOfDay) {
                score += 0.1f
            }
            // Location relevance
            if (item.isLocalContent && it.isLocalUser) {
                score += 0.05f
            }
        }

        return score
    }

    data class ContentItem(
        val id: String,
        val title: String,
        val category: String,
        val publishedAt: Long,
        val popularityScore: Float? = null,
        val bestTimeOfDay: IntRange? = null,
        val isLocalContent: Boolean = false,
        val personalizedScore: Float = 0f
    )

    data class ContextSignals(
        val hourOfDay: Int,
        val dayOfWeek: Int,
        val isLocalUser: Boolean,
        val connectionType: String  // "wifi", "cellular"
    )
}
```

---

## 4. Privacy-Preserving Personalization

```
Privacy Strategies:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  1. ON-DEVICE PROCESSING                                    │
│  ┌──────────────────────────────────────────────────┐       │
│  │ All user data stays on device                     │       │
│  │ Preference model trained locally                  │       │
│  │ Only aggregated, anonymized signals sent          │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  2. DIFFERENTIAL PRIVACY                                    │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Add noise to data before sending to server       │       │
│  │ Individual data can't be identified              │       │
│  │ Aggregate patterns still useful                  │       │
│  │                                                   │       │
│  │ value_sent = true_value + Laplace(0, 1/ε)       │       │
│  │ ε = privacy budget (smaller = more private)      │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  3. FEDERATED LEARNING                                      │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Train model on-device                            │       │
│  │ Send only model updates (gradients) to server    │       │
│  │ Server aggregates across users                   │       │
│  │ Send improved model back to devices              │       │
│  │ Raw data never leaves device                     │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  4. DATA MINIMIZATION                                       │
│  ┌──────────────────────────────────────────────────┐       │
│  │ • Collect only what's needed                     │       │
│  │ • Auto-delete after retention period             │       │
│  │ • Hash identifiers                               │       │
│  │ • Category-level tracking, not item-level         │       │
│  └──────────────────────────────────────────────────┘       │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class PrivacyPreservingPersonalizer(
    private val context: Context,
    private val preferenceLearner: PreferenceLearner
) {
    // On-device only — no server calls
    suspend fun getPersonalizedFeed(
        items: List<ContentPersonalizer.ContentItem>
    ): List<ContentPersonalizer.ContentItem> {
        val personalizer = ContentPersonalizer(preferenceLearner)
        return personalizer.personalizeContent(items)
    }

    // Differential privacy: add noise before sending analytics
    fun addDifferentialPrivacy(
        categoryWeights: Map<String, Float>,
        epsilon: Float = 1.0f  // Privacy budget
    ): Map<String, Float> {
        val random = java.util.Random()

        return categoryWeights.mapValues { (_, weight) ->
            // Laplace noise: b = sensitivity / epsilon
            val b = 1.0 / epsilon
            val u = random.nextDouble() - 0.5
            val noise = -b * kotlin.math.sign(u) * kotlin.math.ln(1 - 2 * kotlin.math.abs(u))
            (weight + noise.toFloat()).coerceIn(0f, 1f)
        }
    }

    // Hash user ID for anonymous tracking
    fun anonymizeUserId(userId: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(userId.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }.take(16)
    }

    // Auto-cleanup based on retention policy
    suspend fun enforceRetentionPolicy(tracker: UserBehaviorTracker, retentionDays: Int = 90) {
        tracker.cleanup()
    }
}
```

---

## Complete ViewModel

```kotlin
class PersonalizationViewModel(
    private val tracker: UserBehaviorTracker,
    private val preferenceLearner: PreferenceLearner,
    private val personalizer: ContentPersonalizer
) : ViewModel() {

    private val _personalizedFeed = MutableStateFlow<List<ContentPersonalizer.ContentItem>>(emptyList())
    val personalizedFeed: StateFlow<List<ContentPersonalizer.ContentItem>> = _personalizedFeed

    private val _preferences = MutableStateFlow<PreferenceLearner.UserPreferences?>(null)
    val preferences: StateFlow<PreferenceLearner.UserPreferences?> = _preferences

    fun loadPersonalizedFeed(items: List<ContentPersonalizer.ContentItem>) {
        viewModelScope.launch {
            _personalizedFeed.value = personalizer.personalizeContent(items)
            _preferences.value = preferenceLearner.learnPreferences()
        }
    }

    // Track user interactions
    fun onItemViewed(itemId: String, category: String, durationMs: Long) {
        viewModelScope.launch { tracker.trackView(itemId, category, durationMs) }
    }

    fun onItemClicked(itemId: String, category: String) {
        viewModelScope.launch { tracker.trackClick(itemId, category) }
    }

    fun onSearch(query: String) {
        viewModelScope.launch { tracker.trackSearch(query) }
    }
}
```
