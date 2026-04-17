# 10. Android Performance Optimization — Complete Guide

---

## 10.1 Memory Management

### Theory: How Android Memory Works

Android uses the **ART (Android Runtime)** which manages memory through automatic garbage collection.
Every app runs in its own process with a **limited heap size** (typically 128–512 MB depending on device).
When an app exceeds its heap limit, the system throws `OutOfMemoryError` and kills the process.

**Three types of memory in an Android process:**
- **Heap Memory** — Java/Kotlin objects (managed by GC)
- **Stack Memory** — Local variables and method call frames (auto-freed when method returns)
- **Native Memory** — Bitmaps (pre-API 26), NDK allocations (NOT managed by GC)

```
┌──────────────────────────────────────────────────────────────┐
│                    Android Process Memory                    │
│                                                              │
│  ┌────────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   Java Heap    │  │    Stack     │  │  Native Memory   │  │
│  │                │  │              │  │                  │  │
│  │ ┌────────────┐ │  │ method()     │  │ Bitmaps(<API26)  │  │
│  │ │ Activity   │ │  │  ├─ local1   │  │ NDK allocations  │  │
│  │ │ Fragment   │ │  │  ├─ local2   │  │ MediaCodec bufs  │  │
│  │ │ ViewModel  │ │  │  └─ local3   │  │ GL textures      │  │
│  │ │ Bitmap*    │ │  │ onCreate()   │  │                  │  │
│  │ │ Collections│ │  │  ├─ savedSt  │  │                  │  │
│  │ └────────────┘ │  │  └─ bundle   │  │                  │  │
│  │ *API 26+ bitmaps│ │              │  │                  │  │
│  │  stored on heap │  │              │  │                  │  │
│  └───────┬────────┘  └──────────────┘  └──────────────────┘  │
│          │                                                    │
│  ┌───────▼────────────────────────────────────────────────┐   │
│  │              Garbage Collector (GC)                    │   │
│  │                                                        │   │
│  │  Phase 1: MARK     Phase 2: SWEEP     Phase 3: COMPACT │   │
│  │  ┌──┐ ┌──┐ ┌──┐   ┌──┐ ┌──┐ ┌──┐   ┌──┐┌──┐┌──┐     │   │
│  │  │✓ │ │✗ │ │✓ │   │██│ │░░│ │██│   │██││██││  │     │   │
│  │  └──┘ └──┘ └──┘   └──┘ └──┘ └──┘   └──┘└──┘└──┘     │   │
│  │  Find live  objs   Free dead objs   Defragment heap   │   │
│  └────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

**GC Root References** — Objects that the GC will never collect:
- Active `Thread` objects
- `static` fields
- Local variables on the stack
- JNI references

**A memory leak occurs** when an object that should be garbage collected is still reachable
from a GC root — typically via a forgotten reference chain.

```
Memory Leak Lifecycle:
                                                        ❌ Object stays
┌──────────┐   ┌──────────┐   ┌──────────────┐        in memory forever
│ Activity │──►│ onDestroy│──►│ Should be GC'd│──────────────────►
│ created  │   │ called   │   │ but a static  │   GC cannot reach
└──────────┘   └──────────┘   │ ref still     │   and free it
                               │ points to it  │
                               └──────────────┘
                                   │
                                   ▼
                              ┌──────────┐
                              │ LEAK!    │
                              │ Activity │
                              │ + Views  │
                              │ + Bitmap │ ← All retained
                              └──────────┘
```

---

### LeakCanary — Automatic Leak Detection

**How it works:**
1. Watches objects after they're expected to be GC'd (Activity after `onDestroy`, Fragment after `onDestroyView`)
2. Forces a GC and checks if the object was collected
3. If still alive → dumps the heap and analyzes the reference chain
4. Shows a notification with the leak trace

```kotlin
// build.gradle.kts — only add to debug builds (zero config)
dependencies {
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    // Auto-installs via AppWatcherInstaller ContentProvider
    // No Application subclass code needed
}
```

```
LeakCanary Detection Flow:
┌──────────────┐     ┌──────────────┐     ┌─────────────────┐
│ Object       │────►│ Add to       │────►│ Wait 5 seconds  │
│ destroyed    │     │ watch queue  │     │ then force GC   │
└──────────────┘     └──────────────┘     └────────┬────────┘
                                                    │
                                          ┌─────────▼─────────┐
                                     No   │ Was object         │  Yes
                                  ┌───────│ garbage collected? │──────┐
                                  │       └───────────────────┘      │
                           ┌──────▼──────┐                    ┌──────▼──────┐
                           │ Heap Dump   │                    │  No leak!   │
                           │ + Analysis  │                    │  All good.  │
                           │ → Show leak │                    └─────────────┘
                           │   trace     │
                           └─────────────┘
```

---

### Common Leak Patterns & Fixes

```kotlin
// ═══════════════════════════════════════════════════════
// PATTERN 1: Static reference holding Activity context
// ═══════════════════════════════════════════════════════

// ❌ BAD — Activity context outlives Activity lifecycle
object AppManager {
    lateinit var currentContext: Context  // Activity stored here = LEAK
}

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppManager.currentContext = this  // this Activity never gets GC'd!
    }
}

// ✅ FIXED — Use Application context (lives as long as the app process)
object AppManager {
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext  // Safe — singleton context
    }
}


// ═══════════════════════════════════════════════════════
// PATTERN 2: Anonymous inner class / Lambda capturing Activity
// ═══════════════════════════════════════════════════════

// ❌ BAD — Anonymous inner class implicitly holds outer class reference
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This Runnable holds an implicit reference to ProfileActivity
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateUI()  // ← references ProfileActivity.this
            }
        }, 60_000)  // 60 seconds — if Activity destroyed before, LEAK!
    }
}

// ✅ FIXED — Use WeakReference + remove callbacks on destroy
class ProfileActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = Runnable {
        // Safe — if Activity is gone, we just won't get here after removeCallbacks
        updateUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler.postDelayed(updateRunnable, 60_000)
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)  // Cancel pending work
        super.onDestroy()
    }
}


// ═══════════════════════════════════════════════════════
// PATTERN 3: Unregistered listeners / callbacks
// ═══════════════════════════════════════════════════════

// ❌ BAD — Listener registered but never unregistered
class SensorActivity : AppCompatActivity(), SensorEventListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
        // Never unregistered → SensorManager holds ref to Activity = LEAK
    }
    override fun onSensorChanged(event: SensorEvent?) {}
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

// ✅ FIXED — Symmetrical register/unregister in lifecycle methods
class SensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    override fun onResume() {
        super.onResume()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)  // Always unregister!
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {}
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}


// ═══════════════════════════════════════════════════════
// PATTERN 4: CoroutineScope not cancelled
// ═══════════════════════════════════════════════════════

// ❌ BAD — Custom scope not tied to lifecycle
class DataActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.Main)  // Never cancelled!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scope.launch {
            val data = withContext(Dispatchers.IO) { api.fetchData() }
            showData(data)  // May reference destroyed Activity
        }
    }
}

// ✅ FIXED — Use lifecycleScope (auto-cancels on destroy)
class DataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) { api.fetchData() }
            showData(data)  // Safe — cancelled if Activity destroyed
        }
    }
}
```

---

### Bitmap Optimization — In Depth

A `Bitmap` of 4000x3000 pixels at ARGB_8888 uses: **4000 × 3000 × 4 bytes = 48 MB**.
If your ImageView is only 400x300, you're wasting **99% of that memory**.

```
Bitmap Loading Strategy:
┌──────────────────────────────────────────────────────────┐
│                                                          │
│  Step 1: Read ONLY dimensions (no pixel allocation)      │
│  ┌──────────────────────────────────────┐                │
│  │ inJustDecodeBounds = true            │                │
│  │ → outWidth = 4000, outHeight = 3000  │                │
│  │ → Memory used: ~0 bytes              │                │
│  └──────────────┬───────────────────────┘                │
│                 │                                        │
│  Step 2: Calculate sample size                           │
│  ┌──────────────▼───────────────────────┐                │
│  │ Required: 400x300                     │                │
│  │ inSampleSize = 8 (4000/8=500 ≈ 400)  │                │
│  │ → Decoded size = 500 × 375           │                │
│  │ → Memory: 500×375×4 = 750 KB         │ ← 98% savings │
│  └──────────────┬───────────────────────┘                │
│                 │                                        │
│  Step 3: Decode with sampling                            │
│  ┌──────────────▼───────────────────────┐                │
│  │ inJustDecodeBounds = false           │                │
│  │ Actual pixels loaded at reduced size │                │
│  └──────────────────────────────────────┘                │
└──────────────────────────────────────────────────────────┘
```

```kotlin
object BitmapUtils {

    fun decodeSampledBitmap(
        resources: Resources,
        resId: Int,
        requiredWidth: Int,
        requiredHeight: Int
    ): Bitmap {
        // Step 1 — Read dimensions without loading pixels
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, resId, options)

        // Step 2 — Calculate optimal sample size
        options.inSampleSize = calculateInSampleSize(
            outWidth = options.outWidth,
            outHeight = options.outHeight,
            reqWidth = requiredWidth,
            reqHeight = requiredHeight
        )

        // Step 3 — Decode actual pixels at reduced resolution
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565  // 2 bytes/pixel vs 4
        return BitmapFactory.decodeResource(resources, resId, options)
    }

    private fun calculateInSampleSize(
        outWidth: Int, outHeight: Int,
        reqWidth: Int, reqHeight: Int
    ): Int {
        var sampleSize = 1
        if (outHeight > reqHeight || outWidth > reqWidth) {
            val halfHeight = outHeight / 2
            val halfWidth = outWidth / 2
            // Use powers of 2 — BitmapFactory rounds down to nearest power of 2
            while (halfHeight / sampleSize >= reqHeight
                && halfWidth / sampleSize >= reqWidth
            ) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }
}

// Usage
val bitmap = BitmapUtils.decodeSampledBitmap(resources, R.drawable.photo, 400, 300)
imageView.setImageBitmap(bitmap)
```

```
Bitmap Config Memory Comparison:
┌──────────────┬────────────┬─────────────────────────────┐
│ Config       │ Bytes/Pixel│ Use Case                    │
├──────────────┼────────────┼─────────────────────────────┤
│ ARGB_8888    │ 4          │ Default, best quality       │
│ RGB_565      │ 2          │ No transparency needed      │
│ ALPHA_8      │ 1          │ Masks only                  │
│ HARDWARE     │ 0 (GPU)    │ API 26+, read-only display  │
└──────────────┴────────────┴─────────────────────────────┘
```

---

### ViewBinding vs findViewById vs DataBinding

```
How Each Works Internally:
┌──────────────────────────────────────────────────────────┐
│ findViewById                                             │
│ ┌───────────────────────────────────┐                    │
│ │ View tree traversal at RUNTIME    │                    │
│ │ Activity → DecorView → ... → find │                    │
│ │ Returns View (needs manual cast)  │← ClassCastException│
│ │ Returns null if ID wrong          │← NullPointerExcep. │
│ └───────────────────────────────────┘                    │
│                                                          │
│ ViewBinding                                              │
│ ┌───────────────────────────────────┐                    │
│ │ Generated class at COMPILE time   │                    │
│ │ Direct field references (no search│                    │
│ │ Type-safe, null-safe              │                    │
│ │ No reflection, no annotation proc │← Fastest           │
│ └───────────────────────────────────┘                    │
│                                                          │
│ DataBinding                                              │
│ ┌───────────────────────────────────┐                    │
│ │ Generated class + annotation proc │                    │
│ │ Supports expressions in XML       │                    │
│ │ Two-way binding, LiveData observe │                    │
│ │ Slower build, more generated code │← Most features     │
│ └───────────────────────────────────┘                    │
└──────────────────────────────────────────────────────────┘
```

```kotlin
// ══════════════════════════════════════
// ViewBinding — Activity usage
// ══════════════════════════════════════
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.welcomeText.text = "Hello"
        binding.loginButton.setOnClickListener { navigateToLogin() }
    }
}

// ══════════════════════════════════════
// ViewBinding — Fragment usage (must null out in onDestroyView!)
// ══════════════════════════════════════
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!  // Only access between onViewCreated and onDestroyView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        binding.nameText.text = "Junaid"
        binding.editButton.setOnClickListener { openEditor() }
    }

    override fun onDestroyView() {
        _binding = null  // CRITICAL: Fragment outlives its view — prevents leak
        super.onDestroyView()
    }
}
```

---

### Weak References and Reference Types

```
Reference Types — Strength Hierarchy:
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  Strong Reference          (default)                        │
│  ├── val obj = MyObject()                                   │
│  ├── GC will NEVER collect while reachable                  │
│  └── Most references in your code                           │
│                                                             │
│  Soft Reference            (memory-sensitive cache)         │
│  ├── val ref = SoftReference(bitmap)                        │
│  ├── GC collects ONLY when memory is low                    │
│  └── Good for memory-sensitive caches                       │
│                                                             │
│  Weak Reference            (non-preventing reference)       │
│  ├── val ref = WeakReference(activity)                      │
│  ├── GC collects at NEXT GC cycle if no strong refs exist   │
│  └── Good for avoiding leaks with callbacks                 │
│                                                             │
│  Phantom Reference         (cleanup tracking)               │
│  ├── val ref = PhantomReference(obj, queue)                 │
│  ├── Cannot retrieve object — always returns null           │
│  └── Used for native resource cleanup (rare)                │
│                                                             │
│  GC Collection Priority:                                    │
│  Phantom → Weak → Soft → [never collect Strong]             │
└─────────────────────────────────────────────────────────────┘
```

```kotlin
// Practical example: Image cache with WeakReferences
class WeakImageCache {
    private val cache = mutableMapOf<String, WeakReference<Bitmap>>()

    fun put(key: String, bitmap: Bitmap) {
        cleanStaleEntries()
        cache[key] = WeakReference(bitmap)
    }

    fun get(key: String): Bitmap? {
        val bitmap = cache[key]?.get()
        if (bitmap == null) cache.remove(key)  // Clean up dead reference
        return bitmap
    }

    private fun cleanStaleEntries() {
        cache.entries.removeAll { it.value.get() == null }
    }
}

// Practical example: Listener pattern without leaking
class EventBus {
    private val listeners = mutableListOf<WeakReference<EventListener>>()

    fun register(listener: EventListener) {
        listeners.add(WeakReference(listener))
    }

    fun emit(event: Event) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val listener = iterator.next().get()
            if (listener == null) {
                iterator.remove()  // Dead reference, clean up
            } else {
                listener.onEvent(event)
            }
        }
    }
}
```

---

## 10.2 Battery Optimization

### Theory: What Drains Battery

The biggest battery consumers on Android are:

| Component | Power Draw | Notes |
|-----------|-----------|-------|
| **Screen** | Very High | Brightness, refresh rate |
| **Cellular radio** | High | Transitions between idle/active states are expensive |
| **GPS** | High | Continuous location updates |
| **CPU** | Medium-High | Wakelocks keeping CPU awake |
| **WiFi** | Medium | Active scanning |
| **Bluetooth** | Low-Medium | BLE is efficient |

**Key principle:** Batch work together rather than many small wakeups. Each CPU wakeup
costs ~20ms of overhead regardless of how small the task is.

```
Battery Impact of Scheduling Strategies:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ❌ BAD: Frequent individual wakeups                         │
│  ──────────────────────────────────────────► Time             │
│  │ │ │ │ │ │ │ │ │ │ │ │ │ │ │ │ │ │ │   (24 wakeups)       │
│                                                              │
│  ✅ GOOD: Batched work in maintenance windows                │
│  ──────────────────────────────────────────► Time             │
│  │████│              │████│             │████│ (3 wakeups)    │
│   batch               batch              batch               │
│                                                              │
│  Battery savings: ~80% from batching alone                   │
└──────────────────────────────────────────────────────────────┘
```

---

### Doze Mode and App Standby (API 23+)

**Doze Mode** activates when the device is stationary, unplugged, and screen-off for a period.
It progressively restricts app activity to save power.

**App Standby Buckets** (API 28+) categorize apps by recent usage:

```
App Standby Buckets:
┌───────────┬──────────────────────┬─────────────────────────────┐
│ Bucket    │ Criteria             │ Restrictions                │
├───────────┼──────────────────────┼─────────────────────────────┤
│ Active    │ Currently in use     │ None                        │
│ Working   │ Used regularly       │ Jobs deferred up to 2 hrs   │
│ Frequent  │ Used often, not daily│ Jobs deferred up to 8 hrs   │
│ Rare      │ Rarely used          │ Jobs deferred up to 24 hrs  │
│ Restricted│ Not used + high drain│ 1 job/day, no network       │
└───────────┴──────────────────────┴─────────────────────────────┘

Doze Mode Progressive Restrictions:
┌─────────┐                              ┌──────────────────┐
│ Screen  │   Stationary +  Unplugged    │  LIGHT DOZE      │
│  OFF    │ ────────────────────────────► │  (API 24+)       │
└─────────┘        ~minutes              │  • Deferred jobs  │
                                          │  • No network     │
                                          │  • Alarms batched │
                                          └────────┬─────────┘
                                                   │ ~1 hour
                                          ┌────────▼─────────┐
                                          │  DEEP DOZE       │
                                          │  (API 23+)       │
                                          │  • No wakelocks   │
                                          │  • No WiFi scans  │
                                          │  • No syncs       │
                                          │  • GPS suspended  │
                                          └────────┬─────────┘
                                                   │
                                          ┌────────▼─────────┐
                                          │ MAINTENANCE      │
                                          │ WINDOW           │
                                          │ (brief, periodic)│
                                          │ • Batch all work │
                                          │ • Network OK     │
                                          │ • Then back to   │
                                          │   deep doze      │
                                          └──────────────────┘
```

**What still works in Doze:**
- `setAlarmClock()` — user-visible alarms (alarm clocks)
- `setExactAndAllowWhileIdle()` — limited to ~1 per 9 minutes
- High-priority FCM messages — brief network access
- Foreground services — but must show notification

---

### WorkManager — Battery-Aware Background Work

**Why WorkManager over AlarmManager / JobScheduler?**
- Backward compatible to API 14
- Battery-aware constraint system
- Survives app restarts and device reboots
- Chainable work with input/output
- Supports one-time, periodic, and expedited work

```kotlin
// ═══════════════════════════════════════════
// Step 1: Define the Worker
// ═══════════════════════════════════════════
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = SyncRepository(applicationContext)
            repository.syncPendingChanges()
            Result.success()
        } catch (e: IOException) {
            if (runAttemptCount < 3) Result.retry()  // Retry with backoff
            else Result.failure()
        }
    }
}

// ═══════════════════════════════════════════
// Step 2: Schedule with battery-friendly constraints
// ═══════════════════════════════════════════
fun scheduleBatteryFriendlySync(context: Context) {
    // Periodic sync — minimum interval is 15 minutes
    val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
        repeatInterval = 1, TimeUnit.HOURS,
        flexInterval = 15, TimeUnit.MINUTES  // Run within last 15 min of interval
    ).setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)  // WiFi only
            .setRequiresBatteryNotLow(true)                 // Battery > 15%
            .setRequiresCharging(false)                     // OK not charging
            .setRequiresStorageNotLow(true)                 // Storage available
            .build()
    ).setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        OneTimeWorkRequest.MIN_BACKOFF_MILLIS,  // 10 seconds, doubles each retry
        TimeUnit.MILLISECONDS
    ).addTag("periodic-sync")
    .build()

    // UniquePeriodicWork prevents duplicates
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "data-sync",
        ExistingPeriodicWorkPolicy.KEEP,  // KEEP = don't replace existing
        syncRequest
    )
}

// ═══════════════════════════════════════════
// Step 3: Chain dependent work
// ═══════════════════════════════════════════
fun scheduleUploadPipeline(context: Context) {
    val compress = OneTimeWorkRequestBuilder<CompressWorker>().build()
    val encrypt = OneTimeWorkRequestBuilder<EncryptWorker>().build()
    val upload = OneTimeWorkRequestBuilder<UploadWorker>()
        .setConstraints(Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build())
        .build()
    val cleanup = OneTimeWorkRequestBuilder<CleanupWorker>().build()

    // Sequential chain: compress → encrypt → upload → cleanup
    WorkManager.getInstance(context)
        .beginWith(compress)
        .then(encrypt)
        .then(upload)
        .then(cleanup)
        .enqueue()
}
```

```
WorkManager Decision Flow:
┌──────────────────┐
│ Background task? │
└────────┬─────────┘
         │
    ┌────▼────────────────────┐     YES    ┌────────────────┐
    │ Needs to run IMMEDIATELY│────────────│ Foreground     │
    │ with user awareness?    │            │ Service        │
    └────┬────────────────────┘            └────────────────┘
         │ NO
    ┌────▼────────────────────┐     YES    ┌────────────────┐
    │ Needs exact timing?     │────────────│ AlarmManager   │
    │ (alarm clocks, reminders│            │ setAlarmClock()│
    └────┬────────────────────┘            └────────────────┘
         │ NO
    ┌────▼────────────────────┐
    │ Use WorkManager ✅      │   ← Best default choice
    │ Deferrable, guaranteed, │
    │ battery-efficient       │
    └─────────────────────────┘
```

### Sensor Batching

```kotlin
// Without batching: sensor wakes CPU for EVERY event
// With batching: sensor hardware buffers events, delivers in batch

class SensorBatchExample(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun startBatchedSensing() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // maxReportLatencyMicros = how long hardware can buffer events
        // 5_000_000 = 5 seconds → CPU wakes up every 5s instead of every 20ms
        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME,     // 20ms between samples
            5_000_000                              // But batch for 5 seconds
        )
        // Result: ~250 events delivered at once every 5 seconds
        // vs 250 individual CPU wakeups
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // Process batch of events efficiently
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
}
```

---

## 10.3 Rendering Performance

### Theory: The Android Rendering Pipeline

Android targets **60 FPS** (16.67ms per frame). Every frame goes through these stages:

```
Complete Rendering Pipeline (per frame):
┌───────────────────────────────────────────────────────────────────┐
│                          UI Thread (CPU)                         │
│                                                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │ Input    │  │ Animation│  │ Measure  │  │ Layout           │  │
│  │ Handling │─►│ Updates  │─►│ Pass     │─►│ Pass             │  │
│  │          │  │          │  │(top-down)│  │(position children│  │
│  └──────────┘  └──────────┘  └──────────┘  └────────┬─────────┘  │
│                                                      │            │
│  ┌──────────────────────────────────────────────────▼──────────┐  │
│  │ Draw Pass — Build Display List (record Canvas commands)     │  │
│  └─────────────────────────────────┬──────────────────────────┘  │
└────────────────────────────────────┼─────────────────────────────┘
                                     │ Sync
┌────────────────────────────────────┼─────────────────────────────┐
│                          Render Thread (GPU)                     │
│  ┌─────────────────────────────────▼──────────────────────────┐  │
│  │ Execute Display List → Issue OpenGL/Vulkan commands        │  │
│  └─────────────────────────────────┬──────────────────────────┘  │
│                                    │                             │
│  ┌─────────────────────────────────▼──────────────────────────┐  │
│  │ Swap Buffers → SurfaceFlinger composites → Display         │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘

 ◄─────────────────── Must complete in 16.67ms ──────────────────►
 If exceeded → JANK (dropped frame, user sees stutter)
```

**What causes jank (dropped frames):**
- Deep view hierarchies → expensive measure/layout passes
- Overdraw → GPU draws same pixel multiple times
- Main thread blocking → I/O, heavy computation
- Excessive object allocation → triggers GC pauses

---

### Layout Optimization and Hierarchy Flattening

Each level of nesting multiplies the number of measure/layout passes:

```
Measure/Layout Pass Count:
┌──────────────────┬───────────────────────────────────────────┐
│ Layout           │ Measure passes for N-deep nesting         │
├──────────────────┼───────────────────────────────────────────┤
│ LinearLayout     │ 1 pass  (O(n) — each child once)         │
│ LinearLayout     │ 2 passes if weight used (O(2n))          │
│ RelativeLayout   │ 2 passes always (double measure)          │
│ ConstraintLayout │ ~1-2 passes (optimized solver)           │
│ Nested 3-deep LL│ Up to 8 passes (2^depth exponential!)    │
└──────────────────┴───────────────────────────────────────────┘

Problem — Nested layouts:
                        LinearLayout       ← Pass 1
                       /            \
                LinearLayout    LinearLayout   ← Pass 1 × 2 children
               /     \          /        \
         RelativeLayout  TV   RelativeLayout  TV  ← 2 passes each
          /    \                /    \
        TV    IV              TV    IV     ← 4 levels deep = 16 passes!

Solution — Flat ConstraintLayout:
                    ConstraintLayout       ← 1-2 passes total
              /    /      |      \     \
            TV1  TV2     IV1     TV3   TV4  ← All siblings, ~2 passes
```

```xml
<!-- ❌ 4-level nested layout → exponential measure passes -->
<LinearLayout android:orientation="vertical">
    <LinearLayout android:orientation="horizontal">
        <RelativeLayout>
            <LinearLayout android:orientation="vertical">
                <TextView android:id="@+id/title" />
                <TextView android:id="@+id/subtitle" />
            </LinearLayout>
            <ImageView android:id="@+id/avatar" />
        </RelativeLayout>
    </LinearLayout>
    <TextView android:id="@+id/description" />
</LinearLayout>

<!-- ✅ Single-level ConstraintLayout → 2 measure passes max -->
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp">

    <ImageView
        android:id="@+id/avatar"
        android:layout_width="48dp"
        android:layout_height="48dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/title"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toEndOf="@id/avatar"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="@id/avatar"
        android:layout_marginStart="12dp" />

    <TextView
        android:id="@+id/subtitle"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toStartOf="@id/title"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toBottomOf="@id/title" />

    <TextView
        android:id="@+id/description"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toBottomOf="@id/avatar"
        android:layout_marginTop="8dp" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

---

### Overdraw — Theory and Reduction

**Overdraw** = drawing the same pixel more than once per frame. GPU does redundant work.

```
Overdraw Visualization (Developer Options > Debug GPU Overdraw):
┌────────────────────────────────────────────────────┐
│                                                    │
│   No color = 0x overdraw (1 draw) ← TARGET         │
│   Blue     = 1x overdraw (drawn 2 times)           │
│   Green    = 2x overdraw (drawn 3 times) ← Warning │
│   Pink     = 3x overdraw (drawn 4 times) ← Bad     │
│   Red      = 4x+ overdraw              ← Fix this! │
│                                                    │
└────────────────────────────────────────────────────┘

Common Overdraw Sources:
┌──────────────────────────────────────────────────────┐
│                                                      │
│  Window Background    ████████████████████  Draw 1   │
│  Activity Background  ████████████████████  Draw 2   │
│  Fragment Background  ████████████████████  Draw 3   │
│  Card Background      ████████████████████  Draw 4   │
│  Text on Card         ████████████████████  Draw 5   │
│                                                      │
│  Result: 4x overdraw for the text pixel!             │
└──────────────────────────────────────────────────────┘
```

```kotlin
// Fix 1: Remove window background when activity has its own
// res/values/themes.xml
// <style name="AppTheme" parent="Theme.Material3.DayNight">
//     <item name="android:windowBackground">@null</item>
// </style>

// Fix 2: Use canvas.clipRect() in custom views
class CustomView(context: Context) : View(context) {
    override fun onDraw(canvas: Canvas) {
        // Only draw within visible area
        canvas.clipRect(visibleRect)
        super.onDraw(canvas)
    }
}

// Fix 3: Avoid unnecessary backgrounds on nested containers
// Only set background on the outermost container
```

---

### RecyclerView Optimization — Deep Dive

```
RecyclerView Internal Architecture:
┌───────────────────────────────────────────────────────────────┐
│                       RecyclerView                            │
│                                                               │
│  ┌──────────────┐  ┌───────────────┐  ┌────────────────────┐  │
│  │ LayoutManager│  │    Adapter    │  │    Recycler        │  │
│  │              │  │               │  │ (View Pool)        │  │
│  │ Decides      │  │ Creates +     │  │                    │  │
│  │ positions    │  │ binds views   │  │ ┌────┐┌────┐┌────┐│  │
│  │ of children  │  │               │  │ │ VH ││ VH ││ VH ││  │
│  │              │  │               │  │ └────┘└────┘└────┘│  │
│  └──────┬───────┘  └───────┬───────┘  │  Recycled views   │  │
│         │                  │          └────────┬───────────┘  │
│         │                  │                   │              │
│  ┌──────▼──────────────────▼───────────────────▼───────────┐  │
│  │                  View Cache Layers                      │  │
│  │                                                         │  │
│  │  Layer 1: Scrap (attached views being re-laid)          │  │
│  │  Layer 2: Cache (mCachedViews — default size 2)         │  │
│  │  Layer 3: ViewCacheExtension (custom, optional)         │  │
│  │  Layer 4: RecycledViewPool (by viewType, default 5)     │  │
│  │                                                         │  │
│  │  Miss all layers → onCreateViewHolder() (expensive!)    │  │
│  └─────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘
```

```kotlin
// ═══════════════════════════════════════════
// Production-grade RecyclerView Adapter
// ═══════════════════════════════════════════
class UserAdapter : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback) {

    // Stable IDs → RecyclerView can optimize animations
    init { setHasStableIds(true) }
    override fun getItemId(position: Int): Long = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.nameText.text = user.name
            binding.emailText.text = user.email
            // ✅ Load images with Coil/Glide — they handle recycling
            binding.avatarImage.load(user.avatarUrl) {
                placeholder(R.drawable.avatar_placeholder)
                crossfade(200)
            }
        }
    }

    // DiffUtil — calculates minimal updates on background thread
    companion object UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(old: User, new: User) = old.id == new.id
        override fun areContentsTheSame(old: User, new: User) = old == new
    }
}

// ═══════════════════════════════════════════
// RecyclerView setup with all optimizations
// ═══════════════════════════════════════════
fun setupOptimizedRecyclerView(recyclerView: RecyclerView, adapter: UserAdapter) {
    recyclerView.apply {
        this.adapter = adapter
        layoutManager = LinearLayoutManager(context).apply {
            initialPrefetchItemCount = 5   // Prefetch during scroll
        }
        setHasFixedSize(true)              // Skip layout on adapter change
        setItemViewCacheSize(10)           // Cache 10 views (default 2)
        itemAnimator = DefaultItemAnimator().apply {
            supportsChangeAnimations = false  // Faster partial updates
        }

        // Shared RecycledViewPool for nested RecyclerViews
        recycledViewPool.setMaxRecycledViews(0, 20)
    }
}
```

---

### Compose Performance — Stability and Recomposition

```
Compose Recomposition Model:
┌───────────────────────────────────────────────────────────────┐
│                                                               │
│  Composition Phase:                                           │
│  ┌──────────┐    ┌─────────────┐    ┌──────────────────────┐  │
│  │@Composable│──►│ Slot Table  │──►│ Changes (diff)        │  │
│  │ function  │   │ (tree of    │   │ Only nodes that       │  │
│  │ execution │   │  remembered │   │ actually changed      │  │
│  └──────────┘   │  state)     │   │ get re-executed       │  │
│                  └─────────────┘   └──────────┬───────────┘  │
│                                                │              │
│  Layout Phase:                                 │              │
│  ┌─────────────────────────────────────────────▼───────────┐  │
│  │ Measure → Place (single pass, no re-measure possible)  │  │
│  └─────────────────────────────────┬───────────────────────┘  │
│                                    │                          │
│  Drawing Phase:                    │                          │
│  ┌─────────────────────────────────▼───────────────────────┐  │
│  │ Canvas draw commands → GPU                              │  │
│  └─────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘

Skippable vs Non-skippable:
┌──────────────────────────────────┐
│ STABLE params (skip-eligible)    │ ← Compose can skip recomposition
│ • Primitives (Int, String, etc.) │    if inputs haven't changed
│ • @Immutable data classes        │
│ • @Stable annotated classes      │
│ • Enum values                    │
└──────────────────────────────────┘
┌──────────────────────────────────┐
│ UNSTABLE params (always recomp.) │ ← Compose MUST recompose every time
│ • List, Map, Set (mutable)       │
│ • Classes from external modules  │
│ • var properties                 │
└──────────────────────────────────┘
```

```kotlin
// ═══════════════════════════════════════════
// STABILITY: Mark data classes for smart recomposition
// ═══════════════════════════════════════════

// ❌ UNSTABLE — List is mutable interface, Compose recomposes every time
data class UserProfile(
    val name: String,
    val tags: List<String>  // List = unstable!
)

// ✅ STABLE — Use @Immutable and ImmutableList from kotlinx.collections
@Immutable
data class UserProfile(
    val name: String,
    val tags: ImmutableList<String>  // Stable collection
)

// ═══════════════════════════════════════════
// derivedStateOf — Compute derived values efficiently
// ═══════════════════════════════════════════

@Composable
fun SearchableList(allItems: List<String>) {
    var query by remember { mutableStateOf("") }

    // ❌ BAD: Filters on every recomposition, even if query didn't change
    // val filtered = allItems.filter { it.contains(query) }

    // ✅ GOOD: Only recomputes when the filter RESULT changes
    val filtered by remember(allItems) {
        derivedStateOf {
            if (query.isBlank()) allItems
            else allItems.filter { it.contains(query, ignoreCase = true) }
        }
    }

    Column {
        TextField(value = query, onValueChange = { query = it })
        LazyColumn {
            items(filtered, key = { it }) { item ->
                Text(item, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════
// remember — Cache expensive computations
// ═══════════════════════════════════════════

@Composable
fun AnalyticsDashboard(rawData: List<DataPoint>) {
    // ✅ Only recalculates when rawData changes
    val processedChart = remember(rawData) {
        rawData.groupBy { it.category }
            .mapValues { (_, points) -> points.sumOf { it.value } }
            .entries.sortedByDescending { it.value }
    }

    val dateFormatter = remember {
        SimpleDateFormat("MMM dd", Locale.getDefault())  // Created once
    }

    // ✅ Use key for LazyColumn items — enables smart diffing
    LazyColumn {
        items(items = processedChart, key = { it.key }) { entry ->
            ChartBar(label = entry.key, value = entry.value)
        }
    }
}

// ═══════════════════════════════════════════
// Defer reads to reduce recomposition scope
// ═══════════════════════════════════════════

@Composable
fun ScrollingContent() {
    val scrollState = rememberScrollState()

    Column(Modifier.verticalScroll(scrollState)) {
        // ❌ BAD: Reading scroll position here recomposes entire Column
        // Text("Scrolled: ${scrollState.value}")
    }

    // ✅ GOOD: Isolate the scroll-reading composable
    ScrollPositionIndicator(scrollState)
}

@Composable
fun ScrollPositionIndicator(scrollState: ScrollState) {
    // Only THIS composable recomposes on scroll — not the entire Column
    val progress by remember {
        derivedStateOf { scrollState.value.toFloat() / scrollState.maxValue }
    }
    LinearProgressIndicator(progress = { progress })
}
```

---

## 10.4 Startup Performance

### Theory: Cold vs Warm vs Hot Start

```
┌────────────────────────────────────────────────────────────────┐
│                     App Startup Types                          │
│                                                                │
│  COLD START (slowest — full initialization)                    │
│  ┌────────┐ ┌──────────┐ ┌────────────┐ ┌────────┐ ┌────────┐ │
│  │ Fork   │►│ Create   │►│ Application│►│Activity│►│ First  │ │
│  │ zygote │ │ process  │ │ onCreate() │ │onCreate│ │ frame  │ │
│  │        │ │+load libs│ │            │ │+inflate│ │rendered│ │
│  └────────┘ └──────────┘ └────────────┘ └────────┘ └────────┘ │
│  ◄──── typically 500ms — 3000ms+ ────────────────────────────► │
│                                                                │
│  WARM START (medium — process exists, activity recreated)      │
│  ┌────────────┐ ┌────────┐ ┌────────┐                          │
│  │ Activity   │►│onCreate│►│ First  │                          │
│  │ recreated  │ │+inflate│ │ frame  │                          │
│  └────────────┘ └────────┘ └────────┘                          │
│  ◄───── typically 150-300ms ─────────►                         │
│                                                                │
│  HOT START (fastest — just bring to foreground)                │
│  ┌────────────┐ ┌────────┐                                     │
│  │ onResume() │►│ Display│                                     │
│  └────────────┘ └────────┘                                     │
│  ◄── typically <100ms ──►                                      │
└────────────────────────────────────────────────────────────────┘

Key Metrics:
┌──────┬──────────────────────────────────────────────────┐
│ TTID │ Time To Initial Display — first frame appears    │
│ TTFD │ Time To Full Display — content loaded & usable   │
└──────┴──────────────────────────────────────────────────┘
```

### Lazy Initialization Strategies

```kotlin
// ═══════════════════════════════════════════
// PROBLEM: Everything initialized at startup
// ═══════════════════════════════════════════

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // ❌ All of these block app startup
        Firebase.initialize(this)         // ~100ms
        CrashReporter.init(this)          // ~50ms
        AnalyticsSDK.init(this)           // ~80ms
        ImageLoader.init(this)            // ~40ms
        AdNetwork.init(this)              // ~120ms
        FeatureFlags.refresh(this)        // ~200ms (network!)
        // Total: ~590ms added to cold start!
    }
}

// ═══════════════════════════════════════════
// SOLUTION 1: Kotlin lazy — init on first access
// ═══════════════════════════════════════════

class MyApplication : Application() {
    // Only initialized when first accessed
    val analytics by lazy { AnalyticsSDK.init(this) }
    val imageLoader by lazy { ImageLoader.Builder(this).build() }
    val featureFlags by lazy { FeatureFlags(this) }

    override fun onCreate() {
        super.onCreate()
        // Only essential SDK that MUST run first
        Firebase.initialize(this)
        CrashReporter.init(this)
    }
}

// ═══════════════════════════════════════════
// SOLUTION 2: App Startup library — ordered lazy init with dependencies
// ═══════════════════════════════════════════

// Define initializers with dependency graph
class CrashReporterInitializer : Initializer<CrashReporter> {
    override fun create(context: Context): CrashReporter {
        return CrashReporter.init(context)
    }
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

class AnalyticsInitializer : Initializer<AnalyticsSDK> {
    override fun create(context: Context): AnalyticsSDK {
        return AnalyticsSDK.init(context)
    }
    // Analytics depends on CrashReporter being initialized first
    override fun dependencies() = listOf(CrashReporterInitializer::class.java)
}

// ═══════════════════════════════════════════
// SOLUTION 3: Background thread initialization
// ═══════════════════════════════════════════

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Critical path — main thread
        Firebase.initialize(this)

        // Non-critical — background thread
        Executors.newSingleThreadExecutor().execute {
            AnalyticsSDK.init(this)
            AdNetwork.init(this)
            FeatureFlags.refresh(this)
        }
    }
}
```

### Baseline Profiles — AOT Compilation

**Theory:** ART compiles code in 3 modes:
1. **Interpreted** — Slowest, no compilation
2. **JIT (Just-In-Time)** — Compiles hot methods at runtime (takes ~30 runs to identify)
3. **AOT (Ahead-Of-Time)** — Pre-compiled at install time using profiles

**Baseline Profiles** tell ART which methods to AOT compile at install time,
so the first run is as fast as the 30th run.

```
Without Baseline Profile:              With Baseline Profile:
┌─────────────────────────┐            ┌─────────────────────────┐
│ Run 1:  Interpreted ▓▓▓▓│ slow       │ Run 1:  AOT compiled ██│ fast!
│ Run 2:  Interpreted ▓▓▓▓│            │ Run 2:  AOT compiled ██│
│ Run 5:  JIT partial  ▓██│            │ Run 5:  AOT compiled ██│
│ Run 10: JIT more     ▓██│            │ Run 10: AOT compiled ██│
│ Run 30: Fully JIT'd  ███│ fast       │ Run 30: AOT compiled ██│
└─────────────────────────┘            └─────────────────────────┘
                                        ↑ Consistent from first launch
```

```kotlin
// benchmark/build.gradle.kts
plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}

dependencies {
    implementation("androidx.benchmark:benchmark-macro-junit4:1.2.4")
}

// Generate baseline profile by exercising critical user journeys
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateProfile() {
        rule.collect(
            packageName = "com.example.myapp",
            stableIterations = 3,
            maxIterations = 10
        ) {
            // Journey 1: App launch
            startActivityAndWait()

            // Journey 2: Main list scroll
            val list = device.findObject(By.res("com.example.myapp", "mainList"))
            list.fling(Direction.DOWN)
            device.waitForIdle()

            // Journey 3: Navigate to detail
            device.findObject(By.text("Item 1")).click()
            device.waitForIdle()
            pressBack()
        }
    }
}
// Output: src/main/baseline-prof.txt (compiled into APK/AAB)
```

### Startup Tracing with Macrobenchmark

```kotlin
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartup() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD,           // Kill process each time
            compilationMode = CompilationMode.Partial() // Use baseline profile
        ) {
            pressHome()
            startActivityAndWait()
        }
        // Output: timeToInitialDisplayMs, timeToFullDisplayMs
    }

    @Test
    fun warmStartup() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.WARM  // Activity recreated, process alive
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}
```

---

## 10.5 Network Optimization

### Theory: Why Network Calls Are Expensive

```
Mobile Radio State Machine:
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ┌──────────┐  Send data   ┌───────────┐  ~2sec  ┌───────┐ │
│  │   IDLE   │─────────────►│ CONNECTED │────────►│ TAIL  │ │
│  │ (no power│              │ (full     │         │ (half │ │
│  │  drain)  │              │  power)   │         │ power)│ │
│  └──────────┘              └───────────┘         └───┬───┘ │
│       ▲                                              │     │
│       │              ~10-30 seconds timeout           │     │
│       └──────────────────────────────────────────────┘     │
│                                                             │
│  Key insight: The "tail" period draws power even with       │
│  no data transfer. Each request costs ~15-20 sec of         │
│  radio active time including the tail.                      │
│                                                             │
│  10 requests 10 seconds apart = 10 × 20s = 200s of drain   │
│  10 requests batched together  = 1 × 20s  =  20s of drain  │
└─────────────────────────────────────────────────────────────┘
```

### Request Batching

```kotlin
// Batch API requests with Retrofit

// ❌ BAD: Individual requests keep radio alive longer
suspend fun loadDashboard() {
    val user = api.getUser()           // Request 1 → radio ON 20s
    val posts = api.getPosts()         // Request 2 → radio ON 20s
    val notifications = api.getNotifs() // Request 3 → radio ON 20s
}                                       // Total: ~60s radio active

// ✅ GOOD: Parallel requests share one radio session
suspend fun loadDashboard() = coroutineScope {
    val user = async { api.getUser() }
    val posts = async { api.getPosts() }
    val notifications = async { api.getNotifs() }

    DashboardData(
        user = user.await(),
        posts = posts.await(),
        notifications = notifications.await()
    )
}  // Total: ~20s radio active (3 requests in parallel)

// ✅ BEST: Single endpoint returns everything
suspend fun loadDashboard(): DashboardData {
    return api.getDashboard()  // 1 request, 1 radio session
}
```

### Caching Strategies

```
Cache Decision Flow:
┌──────────────────┐
│  App makes       │
│  HTTP request    │
└────────┬─────────┘
         │
    ┌────▼───────────────┐   HIT   ┌────────────────────┐
    │ Check Memory Cache │────────►│ Return immediately  │
    │ (OkHttp cache)     │         │ (0ms latency)       │
    └────┬───────────────┘         └────────────────────┘
         │ MISS
    ┌────▼───────────────┐   HIT   ┌────────────────────┐
    │ Check Disk Cache   │────────►│ Return from disk    │
    │ (50MB default)     │         │ (~5ms latency)      │
    └────┬───────────────┘         └────────────────────┘
         │ MISS
    ┌────▼───────────────┐         ┌────────────────────┐
    │ Network Request    │────────►│ Store in caches     │
    │ (with ETag/304)    │         │ Return to app       │
    └────────────────────┘         │ (~100-2000ms)       │
                                   └────────────────────┘
```

```kotlin
// Complete OkHttp setup with caching, compression, and timeouts
fun createHttpClient(context: Context): OkHttpClient {
    return OkHttpClient.Builder()
        // Disk cache — stores HTTP responses
        .cache(Cache(
            directory = File(context.cacheDir, "http_cache"),
            maxSize = 50L * 1024 * 1024  // 50 MB
        ))
        // Timeouts — fail fast instead of draining battery
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        // Offline support — serve stale cache when no network
        .addInterceptor { chain ->
            var request = chain.request()
            if (!isNetworkAvailable(context)) {
                val cacheControl = CacheControl.Builder()
                    .maxStale(7, TimeUnit.DAYS)  // Serve week-old cache if offline
                    .build()
                request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build()
            }
            chain.proceed(request)
        }
        // Cache-Control header policy
        .addNetworkInterceptor { chain ->
            val response = chain.proceed(chain.request())
            val cacheControl = CacheControl.Builder()
                .maxAge(5, TimeUnit.MINUTES)  // Cache for 5 min
                .build()
            response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .removeHeader("Pragma")
                .build()
        }
        .build()
}

private fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
```

### Image Optimization

```kotlin
// Coil — Modern image loading with automatic optimization
fun setupImageLoading(context: Context): ImageLoader {
    return ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.20)  // 20% of available app memory
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizeBytes(100L * 1024 * 1024)  // 100 MB
                .build()
        }
        .crossfade(150)
        .respectCacheHeaders(true)
        .build()
}

// Loading with proper size constraints
fun ImageView.loadOptimized(url: String) {
    this.load(url) {
        size(ViewSizeResolver(this@loadOptimized))  // Match ImageView size
        placeholder(R.drawable.shimmer_placeholder)
        error(R.drawable.broken_image)
        memoryCachePolicy(CachePolicy.ENABLED)
        diskCachePolicy(CachePolicy.ENABLED)
    }
}
```

```
Image Format Decision:
┌────────┬──────────┬───────────┬──────────┬────────────────────┐
│ Format │ Size     │ Lossless? │ Alpha?   │ Best For           │
├────────┼──────────┼───────────┼──────────┼────────────────────┤
│ JPEG   │ Medium   │ No        │ No       │ Photos             │
│ PNG    │ Large    │ Yes       │ Yes      │ Icons, screenshots │
│ WebP   │ 25-34%  │ Both      │ Yes      │ Everything (API14+)│
│        │ smaller  │           │          │                    │
│ AVIF   │ 30-50%  │ Both      │ Yes      │ API 31+ only       │
│        │ smaller  │           │          │                    │
└────────┴──────────┴───────────┴──────────┴────────────────────┘
Recommendation: Use WebP for broad compatibility, AVIF for API 31+
```

---

## 10.6 Code Optimization

### Theory: ProGuard vs R8

```
Build Pipeline:
┌────────┐    ┌────────┐    ┌─────────────────────────────────┐
│ .kt    │───►│ kotlinc│───►│         R8 Optimizer            │
│ .java  │    │ javac  │    │  (replaced ProGuard since AGP 3.4)│
└────────┘    └────┬───┘    │                                 │
                   │        │  1. TREE SHAKING (shrink)       │
              .class files  │     Remove unreachable code     │
                   │        │                                 │
                   └───────►│  2. OPTIMIZATION                │
                            │     Inline methods, simplify    │
                            │     control flow, merge classes │
                            │                                 │
                            │  3. OBFUSCATION                 │
                            │     a.b.c() instead of          │
                            │     com.example.MyClass.method()│
                            │                                 │
                            │  4. RESOURCE SHRINKING           │
                            │     Remove unused drawables,    │
                            │     layouts, strings            │
                            └──────────────┬──────────────────┘
                                           │
                                      ┌────▼─────┐
                                      │ .dex     │
                                      │(optimized│
                                      │ smaller) │
                                      └──────────┘

Size Impact Example:
┌──────────────────────┬──────────┬───────────────┐
│ Optimization         │ Before   │ After         │
├──────────────────────┼──────────┼───────────────┤
│ No optimization      │ 15 MB    │ 15 MB         │
│ + Code shrinking     │ 15 MB    │ 9 MB (-40%)   │
│ + Resource shrinking │ 9 MB     │ 7 MB (-22%)   │
│ + Obfuscation        │ 7 MB     │ 6.5 MB (-7%)  │
│ + AAB (per device)   │ 6.5 MB   │ 4 MB (-38%)   │
└──────────────────────┴──────────┴───────────────┘
```

```kotlin
// app/build.gradle.kts — Complete release configuration
android {
    buildTypes {
        release {
            isMinifyEnabled = true       // Enable R8 code shrinking
            isShrinkResources = true     // Remove unused resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

```proguard
# ═══════════════════════════════════════════
# proguard-rules.pro — Production rules
# ═══════════════════════════════════════════

# ── Keep rules for serialization ──
# Gson/Moshi need to reflect on data classes
-keep class com.example.app.data.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── Keep rules for Retrofit ──
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ── Keep Parcelable implementations ──
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ── Keep enums (used in serialization) ──
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Remove debug logging from release ──
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ── Keep crash reporter symbols for readable stack traces ──
# Upload mapping.txt to Firebase/Sentry for symbolication
-printmapping mapping.txt
```

### APK/AAB Size Reduction

```kotlin
android {
    defaultConfig {
        // Only include ARM ABIs (99% of modern devices)
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }

        // Only include languages you support
        resourceConfigurations += listOf("en", "ur")
    }

    buildFeatures {
        buildConfig = false     // Disable if not using BuildConfig
        aidl = false            // Disable AIDL if not used
        renderScript = false    // Disable RenderScript if not used
    }

    // AAB splits — Google Play delivers only what device needs
    bundle {
        language { enableSplit = true }   // Only device's language
        density { enableSplit = true }    // Only matching DPI
        abi { enableSplit = true }        // Only matching CPU arch
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/*.kotlin_module",
                "kotlin/**",
                "DebugProbesKt.bin"
            )
        }
    }
}
```

```
APK vs AAB Delivery:
┌─────────────────────────────────────────────────────────┐
│ Universal APK:                                          │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ arm64 + arm32 + x86 + x86_64 code                  │ │
│ │ mdpi + hdpi + xhdpi + xxhdpi + xxxhdpi resources    │ │
│ │ en + fr + de + ja + ko + ... strings                │ │
│ │ Total: ~25 MB download for ALL users                │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                         │
│ AAB (Android App Bundle):                               │
│ ┌──────────────────┐                                    │
│ │ arm64 code only  │ ← Only for this device             │
│ │ xxhdpi resources │ ← Only matching density             │
│ │ en strings only  │ ← Only device language              │
│ │ Total: ~10 MB    │ ← 60% smaller download!             │
│ └──────────────────┘                                    │
└─────────────────────────────────────────────────────────┘
```

---

## 10.7 Profiling Tools

### When to Use Which Tool

```
┌────────────────────────────────────────────────────────────────┐
│                    Profiling Tool Decision Tree                │
│                                                                │
│  "My app is slow"                                              │
│      │                                                         │
│      ├─ Slow startup? ──────────► Macrobenchmark + Perfetto    │
│      │                            + Baseline Profiles          │
│      │                                                         │
│      ├─ Janky scrolling? ───────► Macrobenchmark (FrameTiming) │
│      │                            + GPU Profiler               │
│      │                                                         │
│      ├─ Memory growing? ───────► Memory Profiler + LeakCanary  │
│      │                                                         │
│      ├─ Battery draining? ─────► Energy Profiler + Battery     │
│      │                            Historian                    │
│      │                                                         │
│      ├─ Slow API calls? ───────► Network Profiler + OkHttp     │
│      │                            logging                      │
│      │                                                         │
│      └─ Slow function? ────────► Microbenchmark                │
│                                   + CPU Profiler               │
└────────────────────────────────────────────────────────────────┘
```

### Android Profiler — Real-time Analysis

```
Android Studio → View → Tool Windows → Profiler
                    OR
Click the "Profile" icon (bug with chart) next to Run

┌─────────────────────────────────────────────────────────────┐
│                  ANDROID PROFILER                            │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐   │
│  │ CPU PROFILER                                          │   │
│  │ • Record method traces (sample or instrumented)       │   │
│  │ • Call Chart: timeline of method execution             │   │
│  │ • Flame Chart: aggregated time per method stack       │   │
│  │ • Top Down: methods sorted by self time               │   │
│  │ • Bottom Up: find what called the slow method         │   │
│  │                                                       │   │
│  │ ┌─────────────────────────────────────────────────┐   │   │
│  │ │ main  │████ onCreate ████│░ onDraw ░│██ bind ██│   │   │
│  │ │ io-1  │    │██ network █████│                   │   │   │
│  │ │ io-2  │        │████ database ████│             │   │   │
│  │ └─────────────────────────────────────────────────┘   │   │
│  └───────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐   │
│  │ MEMORY PROFILER                                       │   │
│  │ • Real-time heap size graph                           │   │
│  │ • Allocation tracking (which objects + where created) │   │
│  │ • Heap dump analysis (find leaks)                     │   │
│  │ • GC event markers on timeline                        │   │
│  │                                                       │   │
│  │  256MB─┐     ┌──┐                                    │   │
│  │        │  ┌──┘  └──┐  ┌─ GC                          │   │
│  │  128MB─┤──┘        └──┘                               │   │
│  │        │                  ↑ each drop = GC freed objs │   │
│  │    0MB─┴──────────────────────────────► Time           │   │
│  └───────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐   │
│  │ NETWORK PROFILER                                      │   │
│  │ • Timeline of all HTTP requests                       │   │
│  │ • Request/Response headers and body inspection        │   │
│  │ • Connection type (WiFi/Cellular)                     │   │
│  │ • Data sent/received per request                      │   │
│  └───────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐   │
│  │ ENERGY PROFILER                                       │   │
│  │ • Combines CPU + Network + GPS + Wakelocks            │   │
│  │ • Shows estimated battery impact over time            │   │
│  │ • Highlights which components cause drain             │   │
│  │                                                       │   │
│  │  High─┐  ┌──── Network burst                          │   │
│  │       │──┘ ┌── GPS active                             │   │
│  │  Med──┤────┘                                          │   │
│  │  Low──┤─────────────── Idle ──────────► Time          │   │
│  └───────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

### Perfetto — System-wide Tracing

**Theory:** Perfetto captures system-wide traces including:
- CPU scheduling (which thread ran on which core)
- Binder transactions (IPC calls)
- Frame rendering timeline
- Custom trace sections from your code

```kotlin
// Add custom trace sections to identify bottlenecks
import androidx.tracing.trace

class UserRepository(private val api: ApiService, private val db: UserDao) {

    suspend fun loadUser(id: String): User = trace("UserRepo.loadUser") {
        // Each section shows up as a named block in Perfetto

        val cached = trace("checkCache") {
            db.getUserById(id)
        }

        if (cached != null && !cached.isStale()) {
            return@trace cached
        }

        val remote = trace("networkFetch") {
            api.getUser(id)
        }

        trace("saveToDb") {
            db.insertUser(remote)
        }

        return@trace remote
    }
}
```

```
Perfetto Trace Visualization:
┌─────────────────────────────────────────────────────────────┐
│ ui.perfetto.dev                                             │
│                                                             │
│  CPU 0 │████ zygote ████│░░│██ myapp:main ███│░░░░│         │
│  CPU 1 │░░░│███ surfaceflinger ███│░░░░░░░░░░│              │
│  CPU 2 │░░░░░░░│█ myapp:io █│░░░░░░░░░░░░│                 │
│                                                             │
│  myapp                                                      │
│  ├─ main    │████ onCreate ████│█ inflate █│░░│█ draw █│    │
│  │          │                  │           │  │        │    │
│  ├─ io-1    │    │████ loadUser ████│              │        │
│  │          │    │█ cache █│█ fetch █│█ save █│             │
│  │          │                                              │
│  └─ render  │                         │███ drawFrame ███│  │
│                                                             │
│  ◄─── 0ms ──────── 500ms ──────── 1000ms ──────► Time      │
└─────────────────────────────────────────────────────────────┘

Capture from terminal:
  adb shell perfetto --app com.example.myapp -o /data/misc/perfetto-traces/trace
  adb pull /data/misc/perfetto-traces/trace trace.perfetto
  # Open at https://ui.perfetto.dev
```

### Macrobenchmark — Startup & Scrolling Measurement

```kotlin
// Measures real-world performance on actual devices

@RunWith(AndroidJUnit4::class)
class AppBenchmarks {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    // ── Startup Performance ──
    @Test
    fun measureColdStartup() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.DEFAULT  // Uses baseline profile
        ) {
            pressHome()
            startActivityAndWait()
            // Result: timeToInitialDisplayMs, timeToFullDisplayMs
        }
    }

    // ── Scroll Performance (Jank) ──
    @Test
    fun measureScrollJank() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.myapp",
            metrics = listOf(FrameTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.WARM
        ) {
            startActivityAndWait()

            val list = device.findObject(By.res("com.example.myapp", "userList"))
            list.setGestureMargin(device.displayWidth / 5)

            repeat(3) {
                list.fling(Direction.DOWN)
                device.waitForIdle()
            }
            // Result: frameDurationCpuMs (p50, p90, p95, p99)
            // P99 > 16ms = jank visible to users
        }
    }
}
```

### Microbenchmark — Code-level Performance

```kotlin
// Measures individual function performance in nanoseconds

@RunWith(AndroidJUnit4::class)
class AlgorithmBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val data = (1..10_000).shuffled()

    @Test
    fun benchmarkListSort() {
        benchmarkRule.measureRepeated {
            // runWithTimingDisabled — exclude setup from measurement
            val list = runWithTimingDisabled { data.toMutableList() }
            list.sort()
        }
    }

    @Test
    fun benchmarkSequenceVsList() {
        val items = (1..100_000).toList()

        benchmarkRule.measureRepeated {
            runWithTimingDisabled { }

            items.asSequence()
                .filter { it % 2 == 0 }
                .map { it * 2 }
                .take(100)
                .toList()
        }
    }

    @Test
    fun benchmarkStringConcatenation() {
        benchmarkRule.measureRepeated {
            // StringBuilder vs string concatenation
            val sb = StringBuilder()
            repeat(1000) { sb.append("item_$it,") }
            sb.toString()
        }
    }
}

// Sample Output:
// AlgorithmBenchmark.benchmarkListSort
//   min    234,567 ns
//   median 256,789 ns
//   max    312,456 ns
//   allocationCount: 1
```

---

## Quick Reference — Performance Checklist

```
┌─────────────────────────────────────────────────────────────┐
│                 PERFORMANCE OPTIMIZATION CHECKLIST           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  MEMORY                                                     │
│  ☐ Null out Fragment binding in onDestroyView()             │
│  ☐ Use lifecycleScope instead of custom CoroutineScope      │
│  ☐ Unregister listeners in onPause/onDestroy                │
│  ☐ Sample bitmaps down to display size                      │
│  ☐ Use WeakReference for caches and callbacks               │
│  ☐ Add LeakCanary to debug builds                           │
│                                                             │
│  BATTERY                                                    │
│  ☐ Use WorkManager (not AlarmManager) for deferrable work   │
│  ☐ Batch sensor events with maxReportLatency                │
│  ☐ Respect Doze mode — don't fight the system               │
│  ☐ Remove unnecessary wakelocks                             │
│                                                             │
│  RENDERING                                                  │
│  ☐ Use ConstraintLayout — keep hierarchy flat               │
│  ☐ Check for overdraw (Developer Options)                   │
│  ☐ RecyclerView: stable IDs, DiffUtil, fixed size           │
│  ☐ Compose: @Immutable data classes, remember, key()        │
│                                                             │
│  STARTUP                                                    │
│  ☐ Lazy init non-critical SDKs                              │
│  ☐ Generate and ship baseline profiles                      │
│  ☐ Measure with Macrobenchmark (cold start < 500ms target)  │
│  ☐ Defer heavy work to background threads                   │
│                                                             │
│  NETWORK                                                    │
│  ☐ Enable OkHttp disk cache (50-100 MB)                     │
│  ☐ Batch concurrent requests with async/await               │
│  ☐ Use WebP for images (25-34% smaller than JPEG/PNG)       │
│  ☐ Add offline support with stale cache fallback            │
│                                                             │
│  APK SIZE                                                   │
│  ☐ Enable R8 (isMinifyEnabled = true)                       │
│  ☐ Enable resource shrinking (isShrinkResources = true)     │
│  ☐ Publish AAB instead of APK                               │
│  ☐ Filter ABIs to arm64-v8a + armeabi-v7a only              │
│  ☐ Strip unnecessary META-INF and Kotlin metadata           │
│                                                             │
│  PROFILING                                                  │
│  ☐ Profile before optimizing (don't guess — measure!)       │
│  ☐ Add custom trace() sections to critical paths            │
│  ☐ Set up Macrobenchmark in CI for regression detection     │
│  ☐ Use Perfetto for system-wide analysis                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```
