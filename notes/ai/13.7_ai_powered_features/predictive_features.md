# Predictive Features — AI-Powered Features

## Overview

Predictive features use machine learning to anticipate user needs — predicting next actions, optimizing notification timing, suggesting text input, and learning usage patterns.

```
Predictive Features Overview:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌─────────────────────┐  ┌──────────────────────────────┐  │
│  │ Next-Action          │  │ Smart Notifications          │  │
│  │ Prediction           │  │                              │  │
│  │                     │  │  Time: 8:15 AM ← Best time   │  │
│  │ User opened app →   │  │  for user engagement         │  │
│  │ Predict: 73% will   │  │                              │  │
│  │ go to "Messages"    │  │  Content: Personalized       │  │
│  │ → Pre-load screen   │  │  based on user prefs         │  │
│  └─────────────────────┘  └──────────────────────────────┘  │
│                                                              │
│  ┌─────────────────────┐  ┌──────────────────────────────┐  │
│  │ Predictive Text     │  │ Usage Pattern Learning       │  │
│  │                     │  │                              │  │
│  │ "Hey, how a|"       │  │  Mon-Fri: Active 8-9 AM     │  │
│  │       ↓             │  │  Weekend: Active 10-11 AM    │  │
│  │ Suggestions:        │  │  Peak: Wednesday 2 PM        │  │
│  │ [are you] [are]     │  │  Churn risk: Low             │  │
│  │ [about]             │  │                              │  │
│  └─────────────────────┘  └──────────────────────────────┘  │
│                                                              │
│  ML Models Used:                                            │
│  ┌──────────────────────────────────────────────┐           │
│  │ • Markov Chains — Simple sequence prediction │           │
│  │ • LSTM/GRU — Complex sequence patterns       │           │
│  │ • Gradient Boosting — Tabular features       │           │
│  │ • N-gram models — Text prediction            │           │
│  │ • Time series — Usage patterns               │           │
│  └──────────────────────────────────────────────┘           │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. Next-Action Prediction

```
Markov Chain for Action Prediction:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  State = current screen/action                              │
│  Transition = probability of going to next screen           │
│                                                              │
│  Transition Matrix:                                         │
│  ┌──────────┬──────┬──────┬──────┬──────┬──────┐           │
│  │ From\To  │ Home │ Msgs │ Prof │ Cart │ Srch │           │
│  ├──────────┼──────┼──────┼──────┼──────┼──────┤           │
│  │ Home     │ 0.0  │ 0.35 │ 0.15 │ 0.20 │ 0.30 │           │
│  │ Messages │ 0.20 │ 0.0  │ 0.40 │ 0.10 │ 0.30 │           │
│  │ Profile  │ 0.40 │ 0.20 │ 0.0  │ 0.30 │ 0.10 │           │
│  │ Cart     │ 0.30 │ 0.10 │ 0.10 │ 0.0  │ 0.50 │           │
│  │ Search   │ 0.20 │ 0.10 │ 0.10 │ 0.40 │ 0.20 │           │
│  └──────────┴──────┴──────┴──────┴──────┴──────┘           │
│                                                              │
│  From Home → 35% chance next is Messages → prefetch it     │
│                                                              │
│  Higher order: Use last 2-3 actions as state                │
│  Home→Search→? → different than Home→Messages→?             │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class NextActionPredictor {

    // First-order Markov chain: P(next | current)
    private val transitionCounts = mutableMapOf<String, MutableMap<String, Int>>()
    private val sessionHistory = mutableListOf<String>()

    // Record a user action
    fun recordAction(action: String) {
        if (sessionHistory.isNotEmpty()) {
            val current = sessionHistory.last()
            transitionCounts
                .getOrPut(current) { mutableMapOf() }
                .let { it[action] = (it[action] ?: 0) + 1 }
        }
        sessionHistory.add(action)
    }

    // Predict next action probabilities
    fun predictNext(currentAction: String): List<ActionPrediction> {
        val transitions = transitionCounts[currentAction] ?: return emptyList()
        val total = transitions.values.sum().toFloat()

        return transitions.map { (action, count) ->
            ActionPrediction(action, count / total)
        }.sortedByDescending { it.probability }
    }

    // Get top predicted action
    fun topPrediction(currentAction: String): ActionPrediction? {
        return predictNext(currentAction).firstOrNull()
    }

    data class ActionPrediction(val action: String, val probability: Float)

    // Second-order: P(next | prev, current)
    private val secondOrderCounts = mutableMapOf<Pair<String, String>, MutableMap<String, Int>>()

    fun recordActionHigherOrder(action: String) {
        if (sessionHistory.size >= 2) {
            val key = sessionHistory[sessionHistory.size - 2] to sessionHistory.last()
            secondOrderCounts
                .getOrPut(key) { mutableMapOf() }
                .let { it[action] = (it[action] ?: 0) + 1 }
        }
        recordAction(action)
    }

    fun predictNextHigherOrder(prevAction: String, currentAction: String): List<ActionPrediction> {
        val key = prevAction to currentAction
        val transitions = secondOrderCounts[key]

        // Fall back to first-order if not enough data
        if (transitions == null || transitions.values.sum() < 5) {
            return predictNext(currentAction)
        }

        val total = transitions.values.sum().toFloat()
        return transitions.map { (action, count) ->
            ActionPrediction(action, count / total)
        }.sortedByDescending { it.probability }
    }

    // Serialize model for persistence
    fun serialize(): Map<String, Map<String, Int>> {
        return transitionCounts.mapValues { it.value.toMap() }
    }

    fun deserialize(data: Map<String, Map<String, Int>>) {
        transitionCounts.clear()
        data.forEach { (key, transitions) ->
            transitionCounts[key] = transitions.toMutableMap()
        }
    }
}
```

---

## 2. Smart Notifications

```
Smart Notification Timing:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Goal: Send notifications when user is most likely to       │
│  engage, not at random times                                │
│                                                              │
│  User Activity Heatmap (hourly open rate):                  │
│  ┌──────────────────────────────────────────────────┐       │
│  │ Hour  0  2  4  6  8  10  12  14  16  18  20  22 │       │
│  │       ▁  ▁  ▁  ▂  █  ▆   ▄   ▇   ▅   ▃   ▂  ▁ │       │
│  │                   ↑              ↑               │       │
│  │              Morning peak    Afternoon peak      │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  Factors:                                                   │
│  • Historical open times (per user)                        │
│  • Day of week patterns                                    │
│  • Notification fatigue (too many = unsubscribe)           │
│  • Content urgency                                         │
│  • User timezone                                           │
│  • Do Not Disturb inference                                │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class SmartNotificationScheduler(private val dao: UserEventDao) {

    // Learn best notification times from app open patterns
    suspend fun findBestNotificationTimes(lookbackDays: Int = 30): NotificationSchedule {
        val since = System.currentTimeMillis() - lookbackDays.toLong() * 24 * 60 * 60 * 1000
        val events = dao.getEventsSince(since)

        // Count opens by hour and day of week
        val hourCounts = IntArray(24)
        val dayHourCounts = Array(7) { IntArray(24) }
        val calendar = java.util.Calendar.getInstance()

        events.filter { it.eventType == "VIEW" || it.eventType == "CLICK" }.forEach { event ->
            calendar.timeInMillis = event.timestamp
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1

            hourCounts[hour]++
            dayHourCounts[dayOfWeek][hour]++
        }

        // Find peak hours (top 3)
        val peakHours = hourCounts.indices
            .sortedByDescending { hourCounts[it] }
            .take(3)

        // Find best time for each day
        val bestTimePerDay = dayHourCounts.map { dayHours ->
            dayHours.indices.maxByOrNull { dayHours[it] } ?: 9
        }

        // Calculate quiet hours
        val quietHours = hourCounts.indices.filter { hourCounts[it] == 0 }.toSet()

        return NotificationSchedule(
            peakHours = peakHours,
            bestTimePerDay = bestTimePerDay,
            quietHours = quietHours,
            maxNotificationsPerDay = calculateMaxPerDay(events.size, lookbackDays)
        )
    }

    private fun calculateMaxPerDay(totalEvents: Int, days: Int): Int {
        val avgDailyEngagement = totalEvents.toFloat() / days
        return when {
            avgDailyEngagement > 20 -> 5  // Power user
            avgDailyEngagement > 10 -> 3  // Regular user
            avgDailyEngagement > 3 -> 2   // Light user
            else -> 1                      // At-risk, don't annoy
        }
    }

    // Should we send a notification right now?
    fun shouldSendNow(
        schedule: NotificationSchedule,
        priority: NotificationPriority,
        notificationsSentToday: Int
    ): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

        // Never during quiet hours (unless urgent)
        if (currentHour in schedule.quietHours && priority != NotificationPriority.URGENT) {
            return false
        }

        // Check daily limit
        if (notificationsSentToday >= schedule.maxNotificationsPerDay &&
            priority != NotificationPriority.URGENT) {
            return false
        }

        // High priority: send during any active hour
        // Normal priority: only during peak hours
        return when (priority) {
            NotificationPriority.URGENT -> true
            NotificationPriority.HIGH -> currentHour !in schedule.quietHours
            NotificationPriority.NORMAL -> currentHour in schedule.peakHours
            NotificationPriority.LOW -> currentHour == schedule.peakHours.firstOrNull()
        }
    }

    // Schedule notification for optimal time
    fun getNextOptimalTime(schedule: NotificationSchedule): Long {
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1

        val bestHour = schedule.bestTimePerDay.getOrElse(dayOfWeek) { 9 }

        if (currentHour < bestHour) {
            calendar.set(java.util.Calendar.HOUR_OF_DAY, bestHour)
            calendar.set(java.util.Calendar.MINUTE, 0)
        } else {
            // Schedule for tomorrow
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val tomorrowDay = (dayOfWeek + 1) % 7
            calendar.set(java.util.Calendar.HOUR_OF_DAY,
                schedule.bestTimePerDay.getOrElse(tomorrowDay) { 9 })
            calendar.set(java.util.Calendar.MINUTE, 0)
        }

        return calendar.timeInMillis
    }

    data class NotificationSchedule(
        val peakHours: List<Int>,
        val bestTimePerDay: List<Int>,  // Index 0=Sunday
        val quietHours: Set<Int>,
        val maxNotificationsPerDay: Int
    )

    enum class NotificationPriority { URGENT, HIGH, NORMAL, LOW }
}
```

---

## 3. Predictive Text Input

```
N-Gram Language Model:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  Unigram:  P("the") = 0.07                                 │
│  Bigram:   P("you" | "how are") = 0.45                     │
│  Trigram:  P("doing" | "how are you") = 0.30               │
│                                                              │
│  User types: "how are "                                     │
│  Model predicts: "you" (0.45), "things" (0.20), "we" (0.1) │
│                                                              │
│  Personalized model:                                        │
│  Base model (general English) + User-specific model         │
│  (learned from user's typing history)                       │
│                                                              │
│  P_final = α × P_base + (1-α) × P_user                    │
│  α = 0.7 (more weight on base model initially)             │
│  α decreases as user model gets more data                  │
└──────────────────────────────────────────────────────────────┘
```

```kotlin
class PredictiveTextEngine {

    // Bigram model: P(word | prev_word)
    private val bigramCounts = mutableMapOf<String, MutableMap<String, Int>>()
    // Trigram model: P(word | prev2_words)
    private val trigramCounts = mutableMapOf<Pair<String, String>, MutableMap<String, Int>>()
    // Word frequency
    private val wordFrequency = mutableMapOf<String, Int>()

    // Train on text corpus
    fun train(text: String) {
        val words = tokenize(text)

        for (word in words) {
            wordFrequency[word] = (wordFrequency[word] ?: 0) + 1
        }

        // Bigrams
        for (i in 0 until words.size - 1) {
            bigramCounts
                .getOrPut(words[i]) { mutableMapOf() }
                .let { it[words[i + 1]] = (it[words[i + 1]] ?: 0) + 1 }
        }

        // Trigrams
        for (i in 0 until words.size - 2) {
            val key = words[i] to words[i + 1]
            trigramCounts
                .getOrPut(key) { mutableMapOf() }
                .let { it[words[i + 2]] = (it[words[i + 2]] ?: 0) + 1 }
        }
    }

    // Predict next word
    fun predict(inputText: String, maxSuggestions: Int = 3): List<TextSuggestion> {
        val words = tokenize(inputText)
        if (words.isEmpty()) return emptyList()

        // Try trigram first (more specific)
        if (words.size >= 2) {
            val key = words[words.size - 2] to words.last()
            val trigramPredictions = trigramCounts[key]
            if (trigramPredictions != null && trigramPredictions.isNotEmpty()) {
                return topPredictions(trigramPredictions, maxSuggestions)
            }
        }

        // Fall back to bigram
        val bigramPredictions = bigramCounts[words.last()]
        if (bigramPredictions != null && bigramPredictions.isNotEmpty()) {
            return topPredictions(bigramPredictions, maxSuggestions)
        }

        // Fall back to most frequent words
        return wordFrequency.entries
            .sortedByDescending { it.value }
            .take(maxSuggestions)
            .map { TextSuggestion(it.key, it.value.toFloat() / wordFrequency.values.sum()) }
    }

    // Predict completion for partial word
    fun autocomplete(partialWord: String, maxSuggestions: Int = 5): List<String> {
        if (partialWord.length < 2) return emptyList()

        val prefix = partialWord.lowercase()
        return wordFrequency.keys
            .filter { it.startsWith(prefix) && it != prefix }
            .sortedByDescending { wordFrequency[it] }
            .take(maxSuggestions)
    }

    private fun topPredictions(counts: Map<String, Int>, n: Int): List<TextSuggestion> {
        val total = counts.values.sum().toFloat()
        return counts.entries
            .sortedByDescending { it.value }
            .take(n)
            .map { TextSuggestion(it.key, it.value / total) }
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s']"), "")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
    }

    data class TextSuggestion(val word: String, val probability: Float)
}
```

---

## 4. Usage Pattern Learning

```kotlin
class UsagePatternLearner(private val dao: UserEventDao) {

    // Analyze daily usage patterns
    suspend fun analyzePatterns(lookbackDays: Int = 60): UsagePatterns {
        val since = System.currentTimeMillis() - lookbackDays.toLong() * 24 * 60 * 60 * 1000
        val events = dao.getEventsSince(since)

        val calendar = java.util.Calendar.getInstance()

        // Sessions (gap > 30min = new session)
        val sessions = mutableListOf<Session>()
        var sessionStart = 0L
        var lastEventTime = 0L

        for (event in events.sortedBy { it.timestamp }) {
            if (event.timestamp - lastEventTime > 30 * 60 * 1000 || sessionStart == 0L) {
                if (sessionStart > 0L) {
                    sessions.add(Session(sessionStart, lastEventTime))
                }
                sessionStart = event.timestamp
            }
            lastEventTime = event.timestamp
        }
        if (sessionStart > 0L) sessions.add(Session(sessionStart, lastEventTime))

        // Daily active pattern
        val dailyCounts = mutableMapOf<String, Int>()
        events.forEach { event ->
            calendar.timeInMillis = event.timestamp
            val dateKey = "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.DAY_OF_YEAR)}"
            dailyCounts[dateKey] = (dailyCounts[dateKey] ?: 0) + 1
        }

        // Weekly pattern
        val weekdayCounts = IntArray(7)
        events.forEach { event ->
            calendar.timeInMillis = event.timestamp
            weekdayCounts[calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1]++
        }

        // Churn risk
        val recentDays = 7
        val recentEvents = events.count {
            System.currentTimeMillis() - it.timestamp < recentDays.toLong() * 24 * 60 * 60 * 1000
        }
        val avgDailyEvents = dailyCounts.values.average()

        val churnRisk = when {
            recentEvents == 0 -> ChurnRisk.CRITICAL
            recentEvents < avgDailyEvents * 0.3 * recentDays -> ChurnRisk.HIGH
            recentEvents < avgDailyEvents * 0.6 * recentDays -> ChurnRisk.MEDIUM
            else -> ChurnRisk.LOW
        }

        return UsagePatterns(
            avgSessionDuration = sessions.map { it.durationMs }.average().toLong(),
            avgSessionsPerDay = sessions.size.toFloat() / lookbackDays,
            avgEventsPerDay = avgDailyEvents.toFloat(),
            peakDayOfWeek = weekdayCounts.indices.maxByOrNull { weekdayCounts[it] } ?: 0,
            weekdayDistribution = weekdayCounts.toList(),
            churnRisk = churnRisk,
            totalSessions = sessions.size,
            streak = calculateStreak(dailyCounts)
        )
    }

    private fun calculateStreak(dailyCounts: Map<String, Int>): Int {
        val calendar = java.util.Calendar.getInstance()
        var streak = 0
        var day = 0

        while (true) {
            calendar.timeInMillis = System.currentTimeMillis() - day.toLong() * 24 * 60 * 60 * 1000
            val key = "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.DAY_OF_YEAR)}"
            if ((dailyCounts[key] ?: 0) > 0) {
                streak++
                day++
            } else {
                break
            }
        }
        return streak
    }

    data class Session(val startTime: Long, val endTime: Long) {
        val durationMs: Long get() = endTime - startTime
    }

    data class UsagePatterns(
        val avgSessionDuration: Long,
        val avgSessionsPerDay: Float,
        val avgEventsPerDay: Float,
        val peakDayOfWeek: Int,
        val weekdayDistribution: List<Int>,
        val churnRisk: ChurnRisk,
        val totalSessions: Int,
        val streak: Int
    )

    enum class ChurnRisk { LOW, MEDIUM, HIGH, CRITICAL }
}
```

---

## Complete ViewModel

```kotlin
class PredictiveViewModel(
    private val actionPredictor: NextActionPredictor,
    private val notificationScheduler: SmartNotificationScheduler,
    private val textEngine: PredictiveTextEngine,
    private val patternLearner: UsagePatternLearner
) : ViewModel() {

    private val _nextActions = MutableStateFlow<List<NextActionPredictor.ActionPrediction>>(emptyList())
    val nextActions: StateFlow<List<NextActionPredictor.ActionPrediction>> = _nextActions

    private val _textSuggestions = MutableStateFlow<List<PredictiveTextEngine.TextSuggestion>>(emptyList())
    val textSuggestions: StateFlow<List<PredictiveTextEngine.TextSuggestion>> = _textSuggestions

    private val _usagePatterns = MutableStateFlow<UsagePatternLearner.UsagePatterns?>(null)
    val usagePatterns: StateFlow<UsagePatternLearner.UsagePatterns?> = _usagePatterns

    fun onScreenVisited(screenName: String) {
        actionPredictor.recordAction(screenName)
        _nextActions.value = actionPredictor.predictNext(screenName)
    }

    fun onTextInput(text: String) {
        _textSuggestions.value = textEngine.predict(text)
    }

    fun loadUsagePatterns() {
        viewModelScope.launch {
            _usagePatterns.value = patternLearner.analyzePatterns()
        }
    }
}
```
