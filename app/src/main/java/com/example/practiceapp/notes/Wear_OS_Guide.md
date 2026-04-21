# 15.4 Wear OS

## Overview

Wear OS is Google's platform for **smartwatches and wearables**. Modern Wear OS development uses **Jetpack Compose for Wear OS**, provides access to **Health Services**, and supports creating **Complications**, **Tiles**, and **Watch Faces**. Wear OS 3+ is based on a Samsung-Google collaboration and runs on devices from Samsung, Google (Pixel Watch), and other OEMs.

```
┌─────────────────────────────────────────────────────────────────┐
│                  Wear OS Architecture                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌───────────────────────────────────────────────────┐          │
│  │                   Watch Face                       │          │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐           │          │
│  │  │ Compl.  │  │  Time   │  │ Compl.  │           │          │
│  │  │  (HR)   │  │ Display │  │ (Steps) │           │          │
│  │  └─────────┘  └─────────┘  └─────────┘           │          │
│  └───────────────────────────────────────────────────┘          │
│                         │                                        │
│            ┌────────────┼────────────┐                          │
│            │            │            │                           │
│            ▼            ▼            ▼                           │
│  ┌──────────────┐ ┌──────────┐ ┌──────────────┐               │
│  │   Tiles      │ │   App    │ │ Complications│               │
│  │  (Glanceable │ │  (Full   │ │ (Watch face  │               │
│  │   info cards)│ │  Compose │ │  data slots) │               │
│  │              │ │   UI)    │ │              │               │
│  └──────┬───────┘ └────┬─────┘ └──────┬───────┘               │
│         │              │              │                         │
│         └──────────────┼──────────────┘                         │
│                        │                                        │
│              ┌─────────▼──────────┐                            │
│              │  Health Services   │                            │
│              │  API               │                            │
│              │  ┌──────────────┐  │                            │
│              │  │ Heart Rate   │  │                            │
│              │  │ Steps        │  │                            │
│              │  │ Exercise     │  │                            │
│              │  │ Body Sensors │  │                            │
│              │  └──────────────┘  │                            │
│              └────────────────────┘                            │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1. Compose for Wear OS

### Theory

Jetpack Compose for Wear OS provides **watch-optimized composables** that look and feel native on small, round screens. It replaces the older Wear OS XML-based UI development.

**Key Differences from Phone Compose:**
- **Round screens**: Layouts must handle circular displays
- **Smaller touch targets**: Minimum 48dp touch targets
- **ScalingLazyColumn**: Replaces `LazyColumn` — items scale/fade at edges
- **SwipeToDismiss**: Primary navigation pattern (swipe right to go back)
- **Horologist**: Google's companion library for common Wear OS patterns
- **TimeText**: Always-visible time display (Wear OS convention)

```
┌─────────────────────────────────────────────────────────────────┐
│            Compose for Wear OS Components                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Phone Compose          →    Wear OS Compose                    │
│  ─────────────────           ──────────────────                 │
│  Scaffold              →    Scaffold (with TimeText, Vignette)  │
│  LazyColumn            →    ScalingLazyColumn                   │
│  Text                  →    Text (same)                         │
│  Button                →    Button (round by default)           │
│  Card                  →    TitleCard / AppCard / Card          │
│  Checkbox              →    ToggleChip / SplitToggleChip        │
│  Switch                →    ToggleChip                          │
│  Slider                →    InlineSlider / Stepper              │
│  AlertDialog           →    Alert / Confirmation                │
│  Navigation            →    SwipeDismissableNavHost             │
│  BottomNavigation      →    (Not used — swipe-based)            │
│  TopAppBar             →    (Not used — TimeText instead)       │
│                                                                  │
│  Wear-only:                                                     │
│  • TimeText — Shows current time at top                        │
│  • Vignette — Darkens edges for depth                          │
│  • CurvedText — Text that follows the screen curve             │
│  • PositionIndicator — Scroll position indicator               │
│  • SwipeToDismissBox — Swipe-right to navigate back            │
│                                                                  │
│       ┌──────────────────┐                                      │
│       │   ╭────────────╮ │   TimeText (top)                    │
│       │   │  12:30     │ │                                      │
│       │ ╭─┤            ├─╮  Vignette (edges)                   │
│       │ │ │  Content   │ │                                      │
│       │ │ │  Scaling   │ │  ScalingLazyColumn                  │
│       │ │ │  Items     │ │  (center)                           │
│       │ ╰─┤            ├─╯                                      │
│       │   │            │ ●  PositionIndicator                  │
│       │   ╰────────────╯ │                                      │
│       └──────────────────┘                                      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Wear OS Project Setup

```kotlin
// build.gradle.kts (app)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 34
    defaultConfig {
        minSdk = 30  // Wear OS 3.0
        targetSdk = 34
    }
}

dependencies {
    // Compose for Wear OS
    implementation("androidx.wear.compose:compose-material:1.3.0")
    implementation("androidx.wear.compose:compose-foundation:1.3.0")
    implementation("androidx.wear.compose:compose-navigation:1.3.0")

    // Horologist — common Wear OS patterns
    implementation("com.google.android.horologist:horologist-compose-layout:0.5.19")
    implementation("com.google.android.horologist:horologist-compose-material:0.5.19")

    // Health Services
    implementation("androidx.health:health-services-client:1.0.0-rc02")

    // Tiles
    implementation("androidx.wear.tiles:tiles:1.3.0")
    implementation("androidx.wear.tiles:tiles-material:1.3.0")
    implementation("androidx.wear.tiles:tiles-renderer:1.3.0")

    // Watch Face
    implementation("androidx.wear.watchface:watchface:1.2.1")
    implementation("androidx.wear.watchface:watchface-complications-data-source:1.2.1")
    implementation("androidx.wear.watchface:watchface-complications-rendering:1.2.1")
}
```

```xml
<!-- AndroidManifest.xml for Wear OS -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-feature android:name="android.hardware.type.watch" />
    
    <application
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:theme="@android:style/Theme.DeviceDefault">
        
        <uses-library
            android:name="com.google.android.wearable"
            android:required="true" />
        
        <activity
            android:name=".WearMainActivity"
            android:exported="true"
            android:taskAffinity=""
            android:theme="@android:style/Theme.DeviceDefault">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### Code: Basic Wear OS App with Compose (Kotlin)

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    val listState = rememberScalingLazyListState()

    MaterialTheme {
        Scaffold(
            timeText = {
                // Always show current time at top
                TimeText()
            },
            vignette = {
                // Darken edges for depth effect
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                // Scroll position indicator on the side
                PositionIndicator(scalingLazyListState = listState)
            }
        ) {
            // Main content
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 40.dp,
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 40.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Wear OS App",
                        style = MaterialTheme.typography.title1,
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Chips (primary interactive element on Wear)
                item {
                    Chip(
                        onClick = { /* Handle click */ },
                        label = { Text("Start Workout") },
                        secondaryLabel = { Text("Running") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Chip(
                        onClick = { /* Handle click */ },
                        label = { Text("Heart Rate") },
                        secondaryLabel = { Text("72 BPM") },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Toggle Chip (like a switch)
                item {
                    var checked by remember { mutableStateOf(true) }
                    ToggleChip(
                        checked = checked,
                        onCheckedChange = { checked = it },
                        label = { Text("Notifications") },
                        toggleControl = {
                            Switch(checked = checked)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Card
                item {
                    TitleCard(
                        onClick = { },
                        title = { Text("Today's Stats") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Steps: 8,432")
                        Text("Calories: 342 kcal")
                    }
                }

                // Button (round)
                item {
                    Button(
                        onClick = { },
                        modifier = Modifier.size(ButtonDefaults.LargeButtonSize)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Exercise"
                        )
                    }
                }
            }
        }
    }
}
```

### Code: Navigation in Wear OS Compose (Kotlin)

```kotlin
import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController

@Composable
fun WearNavigation() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToWorkout = { navController.navigate("workout") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("workout") {
            WorkoutScreen(
                onNavigateToActive = { type ->
                    navController.navigate("active/$type")
                }
            )
            // Swipe right automatically goes back (SwipeDismissableNavHost)
        }

        composable("active/{workoutType}") { backStackEntry ->
            val workoutType = backStackEntry.arguments?.getString("workoutType") ?: "run"
            ActiveWorkoutScreen(workoutType = workoutType)
        }

        composable("settings") {
            SettingsScreen()
        }
    }
}
```

---

## 2. Health Services API

### Theory

The Health Services API provides **unified access to health and fitness sensors** on Wear OS. It abstracts the complexity of different sensor hardware and provides high-level APIs for:

- **Exercise tracking**: Start/stop workouts, track metrics in real-time
- **Passive monitoring**: Background health data collection
- **Measure client**: One-time sensor readings

```
┌─────────────────────────────────────────────────────────────────┐
│               Health Services Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────┐              │
│  │              Your Wear OS App                 │              │
│  ├──────────────────────────────────────────────┤              │
│  │                                               │              │
│  │  ┌──────────────┐ ┌───────────┐ ┌──────────┐ │              │
│  │  │ Exercise     │ │ Passive   │ │ Measure  │ │              │
│  │  │ Client       │ │ Monitoring│ │ Client   │ │              │
│  │  │              │ │ Client    │ │          │ │              │
│  │  │ Real-time    │ │ Background│ │ One-shot │ │              │
│  │  │ workout data │ │ data      │ │ readings │ │              │
│  │  └──────┬───────┘ └─────┬─────┘ └────┬─────┘ │              │
│  │         │               │            │        │              │
│  └─────────┼───────────────┼────────────┼────────┘              │
│            │               │            │                       │
│  ┌─────────▼───────────────▼────────────▼────────┐              │
│  │           Health Services Platform             │              │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐         │              │
│  │  │  HR     │ │  Accel  │ │  GPS    │  ...    │              │
│  │  │ Sensor  │ │  Sensor │ │  Sensor │         │              │
│  │  └─────────┘ └─────────┘ └─────────┘         │              │
│  └───────────────────────────────────────────────┘              │
│                                                                  │
│  Data Types:                                                    │
│  ├── HEART_RATE_BPM                                            │
│  ├── STEPS                                                     │
│  ├── DISTANCE                                                  │
│  ├── CALORIES                                                  │
│  ├── SPEED                                                     │
│  ├── PACE                                                      │
│  ├── ELEVATION_GAIN                                            │
│  ├── FLOORS                                                    │
│  └── LOCATION                                                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Exercise Client — Workout Tracking (Kotlin)

```kotlin
import android.util.Log
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExerciseService(
    private val healthServicesClient: HealthServicesClient
) {
    private val exerciseClient: ExerciseClient = healthServicesClient.exerciseClient

    private val _exerciseState = MutableStateFlow(ExerciseUiState())
    val exerciseState: StateFlow<ExerciseUiState> = _exerciseState

    // Check what exercise types and data types are supported
    suspend fun getCapabilities(): ExerciseCapabilities {
        return exerciseClient.getCapabilitiesAsync().await()
    }

    // Start an exercise (e.g., Running)
    suspend fun startExercise() {
        val capabilities = getCapabilities()
        val runCapabilities = capabilities.getExerciseTypeCapabilities(
            ExerciseType.RUNNING
        )

        // Build exercise configuration
        val config = ExerciseConfig.builder(ExerciseType.RUNNING)
            .setDataTypes(
                setOf(
                    DataType.HEART_RATE_BPM,
                    DataType.DISTANCE,
                    DataType.SPEED,
                    DataType.CALORIES_TOTAL,
                    DataType.STEPS,
                    DataType.PACE
                )
            )
            .setExerciseGoals(
                listOf(
                    // Alert when distance reaches 5km
                    ExerciseGoal.createOneTimeGoal(
                        DataTypeCondition(
                            DataType.DISTANCE,
                            5000.0,  // 5km in meters
                            ComparisonType.GREATER_THAN_OR_EQUAL
                        )
                    )
                )
            )
            .setIsAutoPauseAndResumeEnabled(true)
            .setIsGpsEnabled(true)
            .build()

        // Register callback for updates
        exerciseClient.setUpdateCallback(exerciseUpdateCallback)

        // Start the exercise
        exerciseClient.startExerciseAsync(config).await()
    }

    // Pause/Resume exercise
    suspend fun pauseExercise() {
        exerciseClient.pauseExerciseAsync().await()
    }

    suspend fun resumeExercise() {
        exerciseClient.resumeExerciseAsync().await()
    }

    // End exercise
    suspend fun endExercise() {
        exerciseClient.endExerciseAsync().await()
    }

    // Callback for exercise updates
    private val exerciseUpdateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val heartRate = update.latestMetrics
                .getData(DataType.HEART_RATE_BPM)
                .lastOrNull()?.value ?: 0.0

            val distance = update.latestMetrics
                .getData(DataType.DISTANCE)
                .lastOrNull()?.value ?: 0.0

            val calories = update.latestMetrics
                .getData(DataType.CALORIES_TOTAL)
                .lastOrNull()?.value ?: 0.0

            val duration = update.activeDurationCheckpoint?.activeDuration

            _exerciseState.value = ExerciseUiState(
                heartRate = heartRate,
                distance = distance,
                calories = calories,
                duration = duration,
                exerciseState = update.exerciseStateInfo.state
            )
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
            Log.d("Exercise", "Lap completed: ${lapSummary.lapCount}")
        }

        override fun onAvailabilityChanged(
            dataType: DataType<*, *>,
            availability: Availability
        ) {
            Log.d("Exercise", "$dataType availability: $availability")
        }

        override fun onExerciseGoalAchieved(achievedGoal: ExerciseGoal<*>) {
            Log.d("Exercise", "Goal achieved: $achievedGoal")
        }
    }
}

data class ExerciseUiState(
    val heartRate: Double = 0.0,
    val distance: Double = 0.0,
    val calories: Double = 0.0,
    val duration: java.time.Duration? = null,
    val exerciseState: ExerciseState = ExerciseState.USER_ENDED
)
```

### Code: Measure Client — One-time Heart Rate (Kotlin)

```kotlin
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class MeasureService(healthServicesClient: HealthServicesClient) {
    private val measureClient = healthServicesClient.measureClient

    // Get heart rate as a Flow
    fun heartRateFlow() = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                // Heart rate sensor availability changed
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRatePoints = data.getData(DataType.HEART_RATE_BPM)
                heartRatePoints.forEach { dataPoint ->
                    trySend(dataPoint.value)
                }
            }
        }

        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)

        awaitClose {
            measureClient.unregisterMeasureCallbackAsync(
                DataType.HEART_RATE_BPM, callback
            )
        }
    }
}
```

### Code: Passive Monitoring (Kotlin)

```kotlin
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.*

class PassiveHealthService : Service() {

    private lateinit var healthServicesClient: HealthServicesClient

    override fun onCreate() {
        super.onCreate()
        healthServicesClient = HealthServicesClient.getClient(this)
        startPassiveMonitoring()
    }

    private fun startPassiveMonitoring() {
        val passiveClient = healthServicesClient.passiveMonitoringClient

        val config = PassiveListenerConfig.builder()
            .setDataTypes(
                setOf(
                    DataType.STEPS_DAILY,
                    DataType.CALORIES_DAILY,
                    DataType.DISTANCE_DAILY,
                    DataType.HEART_RATE_BPM
                )
            )
            .setDailyGoals(
                setOf(
                    PassiveGoal(
                        DataTypeCondition(
                            DataType.STEPS_DAILY,
                            10000.0,
                            ComparisonType.GREATER_THAN_OR_EQUAL
                        )
                    )
                )
            )
            .build()

        passiveClient.setPassiveListenerCallback(
            config,
            object : PassiveListenerCallback {
                override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                    val steps = dataPoints.getData(DataType.STEPS_DAILY)
                    val calories = dataPoints.getData(DataType.CALORIES_DAILY)
                    // Store or process data
                }

                override fun onGoalCompleted(goal: PassiveGoal) {
                    // Daily step goal reached — notify user
                    showNotification("10,000 steps reached!")
                }
            }
        )
    }

    private fun showNotification(message: String) { /* ... */ }
    override fun onBind(intent: Intent?): IBinder? = null
}
```

---

## 3. Complications

### Theory

Complications are **small pieces of data displayed on watch faces**. Your app can provide data to any watch face via a `ComplicationDataSourceService`. Users choose which complications to show on their watch face.

```
┌─────────────────────────────────────────────────────────────────┐
│               Complication Types                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐    │
│  │  SHORT_TEXT     │  │  LONG_TEXT     │  │  RANGED_VALUE  │    │
│  │                │  │                │  │                │    │
│  │   72 BPM       │  │  Next: Meeting │  │  ████░░░ 72%  │    │
│  │   ♥            │  │  at 3:00 PM    │  │  Battery      │    │
│  └────────────────┘  └────────────────┘  └────────────────┘    │
│                                                                  │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐    │
│  │  MONOCHROMATIC │  │  SMALL_IMAGE   │  │  PHOTO_IMAGE   │    │
│  │  _IMAGE        │  │                │  │                │    │
│  │    🏃          │  │   [user pic]   │  │  [Full photo]  │    │
│  │                │  │                │  │                │    │
│  └────────────────┘  └────────────────┘  └────────────────┘    │
│                                                                  │
│  ┌────────────────┐  ┌────────────────┐                        │
│  │  GOAL_PROGRESS │  │  WEIGHTED_     │                        │
│  │                │  │  ELEMENTS      │                        │
│  │  ████░░ 8K     │  │  ■■□□□ Ring    │                        │
│  │  Steps / 10K   │  │  Activity      │                        │
│  └────────────────┘  └────────────────┘                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Complication Data Source (Kotlin)

```kotlin
import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest

class StepCountComplicationService : ComplicationDataSourceService() {

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        val currentSteps = getStepCount()  // Your step counting logic
        val goalSteps = 10000

        // Create complication data based on type
        val complicationData = when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("$currentSteps").build(),
                    contentDescription = PlainComplicationText.Builder(
                        "$currentSteps steps"
                    ).build()
                )
                    .setTitle(
                        PlainComplicationText.Builder("Steps").build()
                    )
                    .setTapAction(createTapAction())
                    .build()
            }

            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = currentSteps.toFloat(),
                    min = 0f,
                    max = goalSteps.toFloat(),
                    contentDescription = PlainComplicationText.Builder(
                        "$currentSteps of $goalSteps steps"
                    ).build()
                )
                    .setText(
                        PlainComplicationText.Builder("$currentSteps").build()
                    )
                    .setTitle(
                        PlainComplicationText.Builder("Steps").build()
                    )
                    .setTapAction(createTapAction())
                    .build()
            }

            ComplicationType.GOAL_PROGRESS -> {
                GoalProgressComplicationData.Builder(
                    value = currentSteps.toFloat(),
                    targetValue = goalSteps.toFloat(),
                    contentDescription = PlainComplicationText.Builder(
                        "$currentSteps of $goalSteps steps"
                    ).build()
                )
                    .setText(
                        PlainComplicationText.Builder("$currentSteps").build()
                    )
                    .setTitle(
                        PlainComplicationText.Builder("Steps").build()
                    )
                    .setTapAction(createTapAction())
                    .build()
            }

            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(
                        "$currentSteps / $goalSteps steps today"
                    ).build(),
                    contentDescription = PlainComplicationText.Builder(
                        "$currentSteps steps today"
                    ).build()
                )
                    .setTapAction(createTapAction())
                    .build()
            }

            else -> null
        }

        listener.onComplicationData(complicationData)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        // Preview shown in watch face editor
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("8,432").build(),
                    contentDescription = PlainComplicationText.Builder("Steps").build()
                ).build()
            }
            else -> null
        }
    }

    private fun createTapAction(): PendingIntent {
        val intent = Intent(this, WearMainActivity::class.java)
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getStepCount(): Int {
        // Retrieve step count from repository/sensor
        return 8432
    }
}
```

```xml
<!-- AndroidManifest.xml — Register complication service -->
<service
    android:name=".StepCountComplicationService"
    android:exported="true"
    android:icon="@drawable/ic_steps"
    android:label="Steps"
    android:permission="com.google.android.wearable.permission.BIND_COMPLICATION_PROVIDER">
    <intent-filter>
        <action android:name="android.support.wearable.complications.ACTION_COMPLICATION_UPDATE_REQUEST" />
    </intent-filter>
    <meta-data
        android:name="android.support.wearable.complications.SUPPORTED_TYPES"
        android:value="SHORT_TEXT,RANGED_VALUE,LONG_TEXT,GOAL_PROGRESS" />
    <meta-data
        android:name="android.support.wearable.complications.UPDATE_PERIOD_SECONDS"
        android:value="300" />  <!-- Update every 5 minutes -->
</service>
```

---

## 4. Tiles

### Theory

Tiles are **glanceable, swipeable cards** that users can access by swiping left/right from the watch face. They show timely, relevant information and offer quick actions without opening the full app.

```
┌─────────────────────────────────────────────────────────────────┐
│                  Tiles Concept                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ◄─── Swipe ───►                                                │
│                                                                  │
│  ┌────────┐   ┌────────┐   ┌────────┐   ┌────────┐            │
│  │        │   │ Watch  │   │ Tile 1 │   │ Tile 2 │            │
│  │ Tile N │   │ Face   │   │ (Your  │   │ (Your  │            │
│  │        │   │        │   │  App)  │   │  App)  │            │
│  │        │   │ 12:30  │   │        │   │        │            │
│  │        │   │        │   │ 🏃8.4K │   │ ♥72BPM │            │
│  │        │   │        │   │ Steps  │   │ HR     │            │
│  └────────┘   └────────┘   └────────┘   └────────┘            │
│                                                                  │
│  Tile Lifecycle:                                                │
│  1. onTileRequest() — Build tile layout                        │
│  2. onTileResourcesRequest() — Provide images/icons            │
│  3. System caches and displays the tile                        │
│  4. Tile refreshes on timeline or user interaction             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Tile Service (Kotlin)

```kotlin
import androidx.wear.protolayout.*
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.*
import androidx.wear.protolayout.LayoutElementBuilders.*
import androidx.wear.protolayout.ResourceBuilders.*
import androidx.wear.protolayout.TimelineBuilders.*
import androidx.wear.protolayout.material.*
import androidx.wear.tiles.*

class FitnessTileService : TileService() {

    private val RESOURCES_VERSION = "1"

    override suspend fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        // Fetch current data
        val steps = getStepCount()
        val calories = getCalories()
        val heartRate = getHeartRate()

        // Build the tile
        val timeline = Timeline.Builder()
            .addTimelineEntry(
                TimelineEntry.Builder()
                    .setLayout(
                        Layout.Builder()
                            .setRoot(buildTileLayout(steps, calories, heartRate))
                            .build()
                    )
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(timeline)
            .setFreshnessIntervalMillis(300_000)  // Refresh every 5 min
            .build()
    }

    override suspend fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): Resources {
        return Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                "ic_steps",
                ImageResource.Builder()
                    .setAndroidResourceByResId(
                        AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.ic_steps)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun buildTileLayout(
        steps: Int,
        calories: Int,
        heartRate: Int
    ): LayoutElement {
        return Column.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
            .addContent(
                // Title
                Text.Builder()
                    .setText("Today's Activity")
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(16f))
                            .setWeight(FONT_WEIGHT_BOLD)
                            .setColor(argb(0xFFFFFFFF.toInt()))
                            .build()
                    )
                    .build()
            )
            .addContent(Spacer.Builder().setHeight(dp(8f)).build())
            .addContent(
                // Steps row
                buildStatRow("🏃", "$steps", "steps")
            )
            .addContent(
                // Calories row
                buildStatRow("🔥", "$calories", "kcal")
            )
            .addContent(
                // Heart rate row
                buildStatRow("❤️", "$heartRate", "BPM")
            )
            .build()
    }

    private fun buildStatRow(
        icon: String,
        value: String,
        unit: String
    ): LayoutElement {
        return Row.Builder()
            .setWidth(wrap())
            .setVerticalAlignment(VERTICAL_ALIGN_CENTER)
            .addContent(
                Text.Builder()
                    .setText(icon)
                    .setFontStyle(FontStyle.Builder().setSize(sp(20f)).build())
                    .build()
            )
            .addContent(Spacer.Builder().setWidth(dp(8f)).build())
            .addContent(
                Text.Builder()
                    .setText(value)
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(24f))
                            .setWeight(FONT_WEIGHT_BOLD)
                            .setColor(argb(0xFF4CAF50.toInt()))
                            .build()
                    )
                    .build()
            )
            .addContent(Spacer.Builder().setWidth(dp(4f)).build())
            .addContent(
                Text.Builder()
                    .setText(unit)
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(12f))
                            .setColor(argb(0xFFBBBBBB.toInt()))
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun getStepCount(): Int = 8432
    private fun getCalories(): Int = 342
    private fun getHeartRate(): Int = 72
}
```

```xml
<!-- AndroidManifest.xml — Register tile service -->
<service
    android:name=".FitnessTileService"
    android:exported="true"
    android:label="Fitness Stats"
    android:permission="com.google.android.wearable.permission.BIND_TILE_PROVIDER">
    <intent-filter>
        <action android:name="androidx.wear.tiles.action.BIND_TILE_PROVIDER" />
    </intent-filter>
    <meta-data
        android:name="androidx.wear.tiles.PREVIEW"
        android:resource="@drawable/tile_preview" />
</service>
```

---

## 5. Watch Face Development

### Theory

Watch faces are the primary UI of a smartwatch. The Jetpack Watch Face library provides a modern API for creating custom watch faces with complication support, style configuration, and efficient rendering.

```
┌─────────────────────────────────────────────────────────────────┐
│               Watch Face Architecture                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────┐              │
│  │               Watch Face                      │              │
│  │                                               │              │
│  │  ┌─────────────────────────────────┐          │              │
│  │  │         Renderer               │          │              │
│  │  │  (Canvas / GLES)               │          │              │
│  │  │  • Draw background             │          │              │
│  │  │  • Draw hour/minute/sec hands  │          │              │
│  │  │  • Draw complications          │          │              │
│  │  │  • Handle ambient mode         │          │              │
│  │  └─────────────────────────────────┘          │              │
│  │                                               │              │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐    │              │
│  │  │  Compl.  │  │  Compl.  │  │  Compl.  │    │              │
│  │  │  Slot 1  │  │  Slot 2  │  │  Slot 3  │    │              │
│  │  └──────────┘  └──────────┘  └──────────┘    │              │
│  │                                               │              │
│  │  ┌───────────────────────────────────┐        │              │
│  │  │  User Style Schema               │        │              │
│  │  │  • Color options                  │        │              │
│  │  │  • Hand style options            │        │              │
│  │  │  • Complication layout options   │        │              │
│  │  └───────────────────────────────────┘        │              │
│  │                                               │              │
│  │  Modes:                                       │              │
│  │  • Interactive: Full color, smooth animation  │              │
│  │  • Ambient: Low power, limited colors, no sec │              │
│  │  • Always-On: Minimal updates                 │              │
│  │                                               │              │
│  └──────────────────────────────────────────────┘              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Basic Watch Face (Kotlin)

```kotlin
import android.graphics.*
import android.view.SurfaceHolder
import androidx.wear.watchface.*
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import java.time.ZonedDateTime

class MyWatchFaceService : WatchFaceService() {

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = MyWatchFaceRenderer(
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            currentUserStyleRepository = currentUserStyleRepository,
            complicationSlotsManager = complicationSlotsManager
        )

        return WatchFace(
            watchFaceType = WatchFaceType.ANALOG,
            renderer = renderer
        )
    }

    override fun createUserStyleSchema(): UserStyleSchema {
        // Define user-configurable options
        val colorSetting = UserStyleSetting.ListUserStyleSetting(
            UserStyleSetting.Id("color_style"),
            resources,
            R.string.color_style_name,
            R.string.color_style_description,
            icon = null,
            options = listOf(
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id("blue"),
                    resources, R.string.blue, R.string.blue, null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id("red"),
                    resources, R.string.red, R.string.red, null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id("green"),
                    resources, R.string.green, R.string.green, null
                )
            ),
            listOf(WatchFaceLayer.BASE, WatchFaceLayer.COMPLICATIONS_OVERLAY)
        )

        return UserStyleSchema(listOf(colorSetting))
    }

    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository
    ): ComplicationSlotsManager {
        val leftComplication = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = 100,
            canvasComplicationFactory = { watchState, listener ->
                CanvasComplicationDrawable(
                    ComplicationDrawable(this),
                    watchState,
                    listener
                )
            },
            supportedTypes = listOf(
                ComplicationType.SHORT_TEXT,
                ComplicationType.RANGED_VALUE,
                ComplicationType.MONOCHROMATIC_IMAGE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_STEP_COUNT,
                ComplicationType.SHORT_TEXT
            ),
            bounds = ComplicationSlotBounds(
                RectF(0.1f, 0.4f, 0.4f, 0.6f)  // Left side
            )
        ).build()

        val rightComplication = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = 101,
            canvasComplicationFactory = { watchState, listener ->
                CanvasComplicationDrawable(
                    ComplicationDrawable(this),
                    watchState,
                    listener
                )
            },
            supportedTypes = listOf(
                ComplicationType.SHORT_TEXT,
                ComplicationType.RANGED_VALUE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_HEART_RATE,
                ComplicationType.SHORT_TEXT
            ),
            bounds = ComplicationSlotBounds(
                RectF(0.6f, 0.4f, 0.9f, 0.6f)  // Right side
            )
        ).build()

        return ComplicationSlotsManager(
            listOf(leftComplication, rightComplication),
            currentUserStyleRepository
        )
    }
}

class MyWatchFaceRenderer(
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    currentUserStyleRepository: CurrentUserStyleRepository,
    private val complicationSlotsManager: ComplicationSlotsManager
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = CanvasType.HARDWARE,
    interactiveDrawModeUpdateDelayMillis = 16,  // ~60fps
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false
) {
    private val hourPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val minutePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val secondPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 2f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    override suspend fun createSharedAssets(): SharedAssets {
        return object : SharedAssets {
            override fun onDestroy() {}
        }
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val radius = minOf(centerX, centerY)

        // Draw background
        canvas.drawColor(Color.BLACK)

        // Draw hour markers
        drawHourMarkers(canvas, centerX, centerY, radius)

        // Draw complications
        for ((_, slot) in complicationSlotsManager.complicationSlots) {
            if (slot.enabled) {
                slot.render(canvas, zonedDateTime, renderParameters)
            }
        }

        // Calculate hand angles
        val hours = zonedDateTime.hour % 12
        val minutes = zonedDateTime.minute
        val seconds = zonedDateTime.second

        val hourAngle = (hours + minutes / 60f) * 30f  // 360/12 = 30
        val minuteAngle = minutes * 6f                   // 360/60 = 6
        val secondAngle = seconds * 6f

        // Draw hands
        drawHand(canvas, centerX, centerY, hourAngle, radius * 0.5f, hourPaint)
        drawHand(canvas, centerX, centerY, minuteAngle, radius * 0.7f, minutePaint)

        // Only draw second hand in interactive mode (not ambient)
        if (renderParameters.drawMode != DrawMode.AMBIENT) {
            drawHand(canvas, centerX, centerY, secondAngle, radius * 0.85f, secondPaint)
        }

        // Draw center circle
        canvas.drawCircle(centerX, centerY, 8f, hourPaint)
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        // Render highlight for complication editing
        for ((_, slot) in complicationSlotsManager.complicationSlots) {
            if (slot.enabled) {
                slot.renderHighlightLayer(canvas, zonedDateTime, renderParameters)
            }
        }
    }

    private fun drawHand(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        angle: Float,
        length: Float,
        paint: Paint
    ) {
        val radians = Math.toRadians((angle - 90).toDouble())
        val endX = cx + (length * Math.cos(radians)).toFloat()
        val endY = cy + (length * Math.sin(radians)).toFloat()
        canvas.drawLine(cx, cy, endX, endY, paint)
    }

    private fun drawHourMarkers(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float
    ) {
        val markerPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 3f
            isAntiAlias = true
        }

        for (i in 0..11) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val startR = radius * 0.9f
            val endR = radius * 0.95f
            canvas.drawLine(
                cx + (startR * Math.cos(angle)).toFloat(),
                cy + (startR * Math.sin(angle)).toFloat(),
                cx + (endR * Math.cos(angle)).toFloat(),
                cy + (endR * Math.sin(angle)).toFloat(),
                markerPaint
            )
        }
    }
}
```

```xml
<!-- AndroidManifest.xml — Watch Face Service -->
<service
    android:name=".MyWatchFaceService"
    android:directBootAware="true"
    android:exported="true"
    android:label="My Watch Face"
    android:permission="android.permission.BIND_WALLPAPER">
    <intent-filter>
        <action android:name="android.service.wallpaper.WallpaperService" />
        <category android:name="com.google.android.wearable.watchface.category.WATCH_FACE" />
    </intent-filter>
    <meta-data
        android:name="com.google.android.wearable.watchface.preview"
        android:resource="@drawable/preview_watchface" />
    <meta-data
        android:name="android.service.wallpaper"
        android:resource="@xml/watch_face" />
    <meta-data
        android:name="com.google.android.wearable.watchface.wearableConfigurationAction"
        android:value="androidx.wear.watchface.editor.action.WATCH_FACE_EDITOR" />
</service>
```

---

## Summary Table

| Feature | Key API | Description |
|---------|---------|-------------|
| Compose for Wear | `wear.compose.material` | Watch-optimized UI components |
| ScalingLazyColumn | `ScalingLazyColumn` | Edge-scaling scrollable list |
| Navigation | `SwipeDismissableNavHost` | Swipe-to-dismiss navigation |
| Health Services | `HealthServicesClient` | Unified sensor access |
| Exercise Client | `ExerciseClient` | Real-time workout tracking |
| Measure Client | `MeasureClient` | One-time sensor readings |
| Passive Monitoring | `PassiveMonitoringClient` | Background health data |
| Complications | `ComplicationDataSourceService` | Watch face data providers |
| Tiles | `TileService` | Glanceable swipeable cards |
| Watch Faces | `WatchFaceService` | Custom watch face rendering |

---

## Best Practices

1. **Keep it glanceable** — users spend ~5 seconds looking at their watch
2. **Minimize battery usage** — use ambient mode, reduce updates
3. **Use Health Services** instead of raw sensors — it handles batching and power
4. **Test on real hardware** — emulator doesn't perfectly simulate sensors
5. **Support round and square screens** — test both form factors
6. **Follow Wear OS design guidelines** — large touch targets (48dp+), clear typography
7. **Use Horologist library** — it provides production-ready Wear OS patterns
8. **Handle Always-On Display (AOD)** — simplify UI in ambient mode, no animations
