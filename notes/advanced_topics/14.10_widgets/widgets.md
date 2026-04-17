# App Widgets

## Overview

App Widgets are miniature app views displayed on the **home screen** (or lock screen). They provide at-a-glance information and quick actions without opening the full app. Android supports two approaches: traditional **RemoteViews** and modern **Glance** (Jetpack Compose-style).

```
┌──────────────────────────────────────────────────────────────┐
│                Widget Architecture                            │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Home Screen (Launcher)                                       │
│  ┌──────────────────────────────────────────────┐            │
│  │  ┌─────────────┐  ┌─────────────────────┐   │            │
│  │  │ Weather      │  │ Tasks               │   │            │
│  │  │ ☀️ 28°C      │  │ □ Buy groceries     │   │            │
│  │  │ Sunny        │  │ ☑ Call dentist      │   │            │
│  │  │ Islamabad    │  │ □ Finish report     │   │            │
│  │  └─────────────┘  │ [+ Add Task]        │   │            │
│  │                    └─────────────────────┘   │            │
│  │  ┌───────────────────────────────────────┐   │            │
│  │  │  Music Player              ▶ ⏭ ❤️    │   │            │
│  │  └───────────────────────────────────────┘   │            │
│  └──────────────────────────────────────────────┘            │
│                                                               │
│  HOW IT WORKS:                                                │
│  ┌──────────────┐     ┌──────────────────┐                   │
│  │ Your App     │     │ Launcher Process │                   │
│  │              │     │ (different proc) │                   │
│  │ AppWidget    │────▶│                  │                   │
│  │ Provider     │ IPC │ RemoteViews      │                   │
│  │ (sends       │     │ (renders widget) │                   │
│  │  RemoteViews)│     │                  │                   │
│  └──────────────┘     └──────────────────┘                   │
│                                                               │
│  ⚠️ Widgets run in a DIFFERENT process (the launcher).        │
│  You can't use arbitrary Views — only RemoteViews-supported  │
│  widgets (TextView, ImageView, Button, LinearLayout, etc.)   │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. App Widgets with RemoteViews (Traditional Approach)

### Step 1: Widget Layout (`res/layout/widget_weather.xml`)

```xml
<!-- Only RemoteViews-supported views can be used! -->
<!-- Supported: FrameLayout, LinearLayout, RelativeLayout, GridLayout,
     TextView, ImageView, Button, ProgressBar, Chronometer,
     ListView, GridView, StackView, ViewFlipper, AdapterViewFlipper -->

<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@drawable/widget_background">

    <TextView
        android:id="@+id/tv_city"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Islamabad"
        android:textSize="14sp"
        android:textColor="@color/white" />

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">

        <ImageView
            android:id="@+id/iv_weather_icon"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:src="@drawable/ic_sunny"
            android:contentDescription="Weather icon" />

        <TextView
            android:id="@+id/tv_temperature"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="28°"
            android:textSize="36sp"
            android:textColor="@color/white"
            android:layout_marginStart="8dp" />
    </LinearLayout>

    <TextView
        android:id="@+id/tv_description"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Sunny"
        android:textSize="12sp"
        android:textColor="@color/white_70" />

    <TextView
        android:id="@+id/tv_last_updated"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Updated: 3:00 PM"
        android:textSize="10sp"
        android:textColor="@color/white_50"
        android:layout_marginTop="4dp" />
</LinearLayout>
```

### Step 2: Widget Info (`res/xml/weather_widget_info.xml`)

```xml
<appwidget-provider
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:targetCellWidth="3"
    android:targetCellHeight="2"
    android:minResizeWidth="110dp"
    android:minResizeHeight="80dp"
    android:maxResizeWidth="400dp"
    android:maxResizeHeight="200dp"
    android:resizeMode="horizontal|vertical"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/widget_weather"
    android:previewImage="@drawable/widget_preview"
    android:previewLayout="@layout/widget_weather"
    android:description="@string/widget_description"
    android:widgetCategory="home_screen|keyguard"
    android:widgetFeatures="reconfigurable|configuration_optional" />

<!-- updatePeriodMillis: Minimum 30 min (1800000ms)
     For more frequent updates, use WorkManager -->
```

### Step 3: AppWidgetProvider (BroadcastReceiver)

```kotlin
class WeatherWidgetProvider : AppWidgetProvider() {

    // Called for each widget update interval and when first placed
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    // Called when the first widget is placed
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Start periodic updates with WorkManager
        schedulePeriodicUpdates(context)
    }

    // Called when the last widget is removed
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Cancel periodic updates
        WorkManager.getInstance(context).cancelUniqueWork("weather_widget_update")
    }

    // Called when a widget is deleted
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Clean up per-widget preferences
        appWidgetIds.forEach { id ->
            context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                .edit().remove("city_$id").apply()
        }
    }

    // Called for any broadcast (including custom actions)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_REFRESH) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateWidget(
                    context,
                    AppWidgetManager.getInstance(context),
                    appWidgetId
                )
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.app.REFRESH_WIDGET"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Build RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_weather)

            // Set text
            views.setTextViewText(R.id.tv_city, "Islamabad")
            views.setTextViewText(R.id.tv_temperature, "28°")
            views.setTextViewText(R.id.tv_description, "Sunny")
            views.setTextViewText(R.id.tv_last_updated,
                "Updated: ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())}")

            // Set image
            views.setImageViewResource(R.id.iv_weather_icon, R.drawable.ic_sunny)

            // Click action: open app
            val openAppIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tv_city, pendingIntent)

            // Click action: refresh widget
            val refreshIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val refreshPending = PendingIntent.getBroadcast(
                context, appWidgetId, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.iv_weather_icon, refreshPending)

            // Push update to widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun schedulePeriodicUpdates(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                15, TimeUnit.MINUTES
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "weather_widget_update",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}

// WorkManager worker for background updates
class WeatherUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val widgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(applicationContext, WeatherWidgetProvider::class.java)
        )

        // Fetch fresh weather data
        // val weatherData = weatherApi.getCurrentWeather()

        // Update all widget instances
        widgetIds.forEach { id ->
            WeatherWidgetProvider.updateWidget(applicationContext, appWidgetManager, id)
        }

        return Result.success()
    }
}
```

### Step 4: Register in Manifest

```xml
<!-- AndroidManifest.xml -->
<receiver
    android:name=".widget.WeatherWidgetProvider"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        <action android:name="com.example.app.REFRESH_WIDGET" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/weather_widget_info" />
</receiver>
```

---

## 2. Widget Configuration Activity

A configuration activity lets users customize the widget when they first place it (e.g., choose a city).

```kotlin
class WeatherWidgetConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set result to CANCELED — if user backs out, widget won't be placed
        setResult(RESULT_CANCELED)

        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            WidgetConfigScreen(
                onCitySelected = { city -> configureWidget(city) }
            )
        }
    }

    private fun configureWidget(city: String) {
        // Save configuration
        getSharedPreferences("widget_prefs", MODE_PRIVATE)
            .edit()
            .putString("city_$appWidgetId", city)
            .apply()

        // Update the widget with the selected city
        val appWidgetManager = AppWidgetManager.getInstance(this)
        WeatherWidgetProvider.updateWidget(this, appWidgetManager, appWidgetId)

        // Return success
        val resultIntent = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

@Composable
fun WidgetConfigScreen(onCitySelected: (String) -> Unit) {
    val cities = listOf("Islamabad", "Lahore", "Karachi", "London", "New York")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Select City for Widget", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        cities.forEach { city ->
            OutlinedButton(
                onClick = { onCitySelected(city) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(city)
            }
        }
    }
}
```

```xml
<!-- Register config activity in AndroidManifest.xml -->
<activity
    android:name=".widget.WeatherWidgetConfigActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>

<!-- In widget_info.xml, add: -->
<!-- android:configure="com.example.app.widget.WeatherWidgetConfigActivity" -->
```

---

## 3. Glance — Jetpack Compose Widgets

Glance provides a **Compose-like** API for building widgets. It generates RemoteViews under the hood but uses a declarative syntax.

```
┌──────────────────────────────────────────────────────────────┐
│              Glance vs RemoteViews                            │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  RemoteViews (Traditional)    │  Glance (Modern)             │
│  ─────────────────────────────┼──────────────────────────    │
│  XML layouts                  │  Compose-like Kotlin DSL     │
│  Limited view types           │  Glance composables          │
│  Manual RemoteViews updates   │  Declarative state-based     │
│  Verbose, error-prone         │  Concise, type-safe          │
│  No state management          │  Built-in state management   │
│  PendingIntent for clicks     │  Lambda-based actions        │
│                               │                               │
│  Glance composables ≠ Jetpack Compose composables!           │
│  Use: androidx.glance.* (not androidx.compose.*)              │
└──────────────────────────────────────────────────────────────┘
```

### Setup

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.glance:glance-material3:1.1.0")
}
```

### Glance Widget Implementation

```kotlin
// ---- 1. Define the Widget Content ----

class TaskListWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact // or SizeMode.Responsive

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load data before composing
        val tasks = TaskRepository(context).getTasks()

        provideContent {
            GlanceTheme {
                TaskWidgetContent(tasks = tasks)
            }
        }
    }
}

@Composable
fun TaskWidgetContent(tasks: List<Task>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp)
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Tasks",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            // Refresh button
            CircleIconButton(
                imageProvider = ImageProvider(R.drawable.ic_refresh),
                contentDescription = "Refresh",
                onClick = actionRunCallback<RefreshAction>()
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (tasks.isEmpty()) {
            // Empty state
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks! 🎉",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                )
            }
        } else {
            // Task list
            LazyColumn {
                items(tasks.take(5)) { task ->
                    TaskItem(task = task)
                }

                // "Open app" footer
                item {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable(
                                actionStartActivity<MainActivity>()
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "View all ${tasks.size} tasks →",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                actionRunCallback<ToggleTaskAction>(
                    actionParametersOf(ActionParameters.Key<String>("taskId") to task.id)
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        CheckBox(
            checked = task.isCompleted,
            onCheckedChange = actionRunCallback<ToggleTaskAction>(
                actionParametersOf(ActionParameters.Key<String>("taskId") to task.id)
            )
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Text(
            text = task.title,
            style = TextStyle(
                color = if (task.isCompleted)
                    GlanceTheme.colors.onSurfaceVariant
                else
                    GlanceTheme.colors.onSurface,
                textDecoration = if (task.isCompleted)
                    TextDecoration.LineThrough
                else
                    TextDecoration.None
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
    }
}
```

### Glance Actions (Click Handlers)

```kotlin
// ---- 2. Define Actions ----

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[ActionParameters.Key<String>("taskId")] ?: return

        // Update data
        TaskRepository(context).toggleTask(taskId)

        // Refresh the widget
        TaskListWidget().update(context, glanceId)
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Re-fetch data and update widget
        TaskListWidget().update(context, glanceId)
    }
}

class AddTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Open app to add task screen
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("destination", "add_task")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
```

### Glance Widget Receiver

```kotlin
// ---- 3. Widget Receiver (replaces AppWidgetProvider) ----

class TaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TaskListWidget()
}
```

### Glance Widget Info and Manifest

```xml
<!-- res/xml/task_widget_info.xml -->
<appwidget-provider
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="180dp"
    android:targetCellWidth="3"
    android:targetCellHeight="3"
    android:resizeMode="horizontal|vertical"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:previewImage="@drawable/task_widget_preview"
    android:description="@string/task_widget_description"
    android:widgetCategory="home_screen" />
```

```xml
<!-- AndroidManifest.xml -->
<receiver
    android:name=".widget.TaskWidgetReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/task_widget_info" />
</receiver>
```

### Glance State Management

```kotlin
// Using Glance preferences for widget state

class CounterWidget : GlanceAppWidget() {

    companion object {
        val countKey = intPreferencesKey("count")
    }

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val count = prefs[countKey] ?: 0

            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .background(GlanceTheme.colors.surface)
                        .cornerRadius(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Count: $count",
                        style = TextStyle(fontSize = 24.sp)
                    )
                    Spacer(GlanceModifier.height(8.dp))
                    Row {
                        Button(
                            text = "-",
                            onClick = actionRunCallback<DecrementAction>()
                        )
                        Spacer(GlanceModifier.width(16.dp))
                        Button(
                            text = "+",
                            onClick = actionRunCallback<IncrementAction>()
                        )
                    }
                }
            }
        }
    }
}

class IncrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentCount = prefs[CounterWidget.countKey] ?: 0
            prefs[CounterWidget.countKey] = currentCount + 1
        }
        CounterWidget().update(context, glanceId)
    }
}

class DecrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentCount = prefs[CounterWidget.countKey] ?: 0
            prefs[CounterWidget.countKey] = maxOf(0, currentCount - 1)
        }
        CounterWidget().update(context, glanceId)
    }
}
```

### Responsive Glance Widgets

```kotlin
class ResponsiveWidget : GlanceAppWidget() {

    companion object {
        private val SMALL = DpSize(120.dp, 80.dp)
        private val MEDIUM = DpSize(200.dp, 120.dp)
        private val LARGE = DpSize(300.dp, 200.dp)
    }

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM, LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current
            GlanceTheme {
                when {
                    size.width < MEDIUM.width -> SmallLayout()
                    size.width < LARGE.width -> MediumLayout()
                    else -> LargeLayout()
                }
            }
        }
    }
}

@Composable
fun SmallLayout() {
    // Compact: just icon + number
    Row(modifier = GlanceModifier.fillMaxSize().padding(8.dp)) {
        Image(ImageProvider(R.drawable.ic_tasks), "Tasks")
        Text("5", style = TextStyle(fontSize = 24.sp))
    }
}

@Composable
fun MediumLayout() {
    // Medium: title + short list
    Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp)) {
        Text("Tasks (5)", style = TextStyle(fontWeight = FontWeight.Bold))
        Text("• Buy groceries")
        Text("• Call dentist")
        Text("+ 3 more...")
    }
}

@Composable
fun LargeLayout() {
    // Full: header + list + add button
    Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
        Text("My Tasks", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
        Spacer(GlanceModifier.height(8.dp))
        // Full task list with checkboxes...
        Button(text = "+ Add Task", onClick = actionRunCallback<AddTaskAction>())
    }
}
```

---

## Widget Size Reference

```
┌──────────────────────────────────────────────────────────────┐
│              Widget Size to Grid Cell Mapping                 │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Cells  │  minWidth  │  minHeight                            │
│  ───────┼────────────┼────────────                           │
│  1      │   40dp     │   40dp                                │
│  2      │  110dp     │  110dp                                │
│  3      │  180dp     │  180dp                                │
│  4      │  250dp     │  250dp                                │
│  5      │  320dp     │  320dp                                │
│                                                               │
│  Formula: minSize = 70 × n − 30                              │
│  (where n = number of cells)                                 │
│                                                               │
│  API 31+ (Android 12): Use targetCellWidth/Height instead    │
└──────────────────────────────────────────────────────────────┘
```
