# Android Fundamentals - Complete Guide with Code Examples

---

## 2. Android Fundamentals

### 2.1 Application Components

---

## Activities

An Activity represents a single screen with a user interface. It's the entry point for user interaction.

### Activity Lifecycle

The Activity lifecycle consists of callback methods that the system calls as the activity transitions between states.

```
[Created] → [Started] → [Resumed] ↔ [Paused] → [Stopped] → [Destroyed]
```

```kotlin
class MainActivity : AppCompatActivity() {

    // Called when activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("Lifecycle", "onCreate - Activity is being created")
        // Initialize UI, bind data, restore saved state
    }

    // Called when activity becomes visible
    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "onStart - Activity is becoming visible")
        // Register broadcast receivers, start animations
    }

    // Called when activity is ready for user interaction
    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "onResume - Activity is in foreground")
        // Resume paused operations, start camera preview
    }

    // Called when activity loses focus but is still visible
    override fun onPause() {
        super.onPause()
        Log.d("Lifecycle", "onPause - Another activity is taking focus")
        // Pause animations, save draft data, release camera
    }

    // Called when activity is no longer visible
    override fun onStop() {
        super.onStop()
        Log.d("Lifecycle", "onStop - Activity is hidden")
        // Unregister receivers, release heavy resources
    }

    // Called before activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        Log.d("Lifecycle", "onDestroy - Activity is being destroyed")
        // Final cleanup
    }

    // Called when activity is restarted from stopped state
    override fun onRestart() {
        super.onRestart()
        Log.d("Lifecycle", "onRestart - Activity is restarting")
    }
}
```

### Lifecycle-Aware Components

Components that can observe lifecycle state changes and react accordingly.

```kotlin
// Custom LifecycleObserver
class MyLocationObserver(private val context: Context) : DefaultLifecycleObserver {

    override fun onResume(owner: LifecycleOwner) {
        // Start location updates
        Log.d("Location", "Starting location updates")
    }

    override fun onPause(owner: LifecycleOwner) {
        // Stop location updates
        Log.d("Location", "Stopping location updates")
    }
}

// Usage in Activity
class LocationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register lifecycle observer
        lifecycle.addObserver(MyLocationObserver(this))
    }
}
```

### Configuration Changes Handling

Handle configuration changes like screen rotation without destroying the activity.

```kotlin
// Method 1: Using ViewModel (Recommended)
class MainViewModel : ViewModel() {
    var userData: User? = null  // Survives configuration changes
}

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // viewModel.userData persists across rotation
    }
}

// Method 2: Save/Restore Instance State
class MainActivity : AppCompatActivity() {
    private var counter = 0

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("COUNTER_KEY", counter)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        counter = savedInstanceState.getInt("COUNTER_KEY", 0)
    }
}

// Method 3: Handle in Manifest (Not recommended for most cases)
// In AndroidManifest.xml:
// android:configChanges="orientation|screenSize|keyboardHidden"
```

### Task and Back Stack Management

A task is a collection of activities arranged in a stack (back stack).

```kotlin
// Clear back stack and start fresh
val intent = Intent(this, HomeActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
}
startActivity(intent)

// Bring existing activity to front
val intent = Intent(this, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
}
startActivity(intent)

// Clear activities above target
val intent = Intent(this, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
}
startActivity(intent)
```

### Launch Modes

Control how activities are instantiated and associated with tasks.

```xml
<!-- AndroidManifest.xml -->
<activity android:name=".StandardActivity"
    android:launchMode="standard" />
<!-- Default. New instance every time -->

<activity android:name=".SingleTopActivity"
    android:launchMode="singleTop" />
<!-- Reuse if at top of stack, else create new -->

<activity android:name=".SingleTaskActivity"
    android:launchMode="singleTask" />
<!-- Only one instance in task, clears above -->

<activity android:name=".SingleInstanceActivity"
    android:launchMode="singleInstance" />
<!-- Alone in its task, no other activities -->
```

```kotlin
// singleTop behavior via Intent flags
val intent = Intent(this, DetailActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
}
startActivity(intent)

// Handle new intent in singleTop/singleTask
class DetailActivity : AppCompatActivity() {
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Called instead of creating new instance
        handleIntent(intent)
    }
}
```

### Intent Filters and Deep Linking

Allow other apps to launch your activity.

```xml
<!-- AndroidManifest.xml -->
<activity android:name=".ProductActivity"
    android:exported="true">
    
    <!-- Deep link: myapp://product/123 -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="myapp"
              android:host="product" />
    </intent-filter>
    
    <!-- Web link: https://www.example.com/product/123 -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https"
              android:host="www.example.com"
              android:pathPrefix="/product" />
    </intent-filter>
</activity>
```

```kotlin
// Handle deep link in activity
class ProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        intent?.data?.let { uri ->
            val productId = uri.lastPathSegment
            Log.d("DeepLink", "Product ID: $productId")
            loadProduct(productId)
        }
    }
}
```

---

## Fragments

A Fragment represents a reusable portion of UI. Can be combined in one activity for tablet layouts.

### Fragment Lifecycle

Fragment lifecycle is tied to its host activity but has additional callbacks.

```kotlin
class MyFragment : Fragment() {

    // Called when fragment is attached to activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("Fragment", "onAttach")
    }

    // Called to do initial creation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Fragment", "onCreate")
    }

    // Called to create the view hierarchy
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Fragment", "onCreateView")
        return inflater.inflate(R.layout.fragment_my, container, false)
    }

    // Called after view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Fragment", "onViewCreated")
        // Setup UI here
    }

    override fun onStart() {
        super.onStart()
        Log.d("Fragment", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Fragment", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("Fragment", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Fragment", "onStop")
    }

    // Called when view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Fragment", "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Fragment", "onDestroy")
    }

    // Called when detached from activity
    override fun onDetach() {
        super.onDetach()
        Log.d("Fragment", "onDetach")
    }
}
```

### Fragment Transactions

Operations to add, replace, or remove fragments.

```kotlin
class MainActivity : AppCompatActivity() {

    // Add fragment
    fun addFragment() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, MyFragment(), "MY_FRAGMENT")
            .addToBackStack("add_fragment")  // Allow back navigation
            .commit()
    }

    // Replace fragment
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Remove fragment
    fun removeFragment() {
        val fragment = supportFragmentManager.findFragmentByTag("MY_FRAGMENT")
        fragment?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
    }

    // commitNow() - Synchronous, can't add to back stack
    fun immediateTransaction() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MyFragment())
            .commitNow()  // Executes immediately
    }

    // commitAllowingStateLoss() - Use when state might be lost
    fun safeCommit() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MyFragment())
            .commitAllowingStateLoss()
    }
}
```

### Fragment Communication Patterns

Ways fragments communicate with activities and other fragments.

```kotlin
// Method 1: Shared ViewModel (Recommended)
class SharedViewModel : ViewModel() {
    val selectedItem = MutableLiveData<String>()
}

class ListFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    
    fun onItemClick(item: String) {
        sharedViewModel.selectedItem.value = item
    }
}

class DetailFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            // Update UI with selected item
        }
    }
}

// Method 2: Fragment Result API
class FragmentA : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Listen for result
        setFragmentResultListener("requestKey") { key, bundle ->
            val result = bundle.getString("resultKey")
            // Handle result
        }
    }
}

class FragmentB : Fragment() {
    fun sendResult() {
        setFragmentResult("requestKey", bundleOf("resultKey" to "Hello"))
    }
}

// Method 3: Interface (Traditional)
interface OnItemSelectedListener {
    fun onItemSelected(item: String)
}

class ItemFragment : Fragment() {
    private var listener: OnItemSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnItemSelectedListener
    }

    fun selectItem(item: String) {
        listener?.onItemSelected(item)
    }
}
```

### Navigation Component Integration

Modern way to handle fragment navigation.

```xml
<!-- nav_graph.xml -->
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    app:startDestination="@id/homeFragment">

    <fragment
        android:id="@+id/homeFragment"
        android:name="com.example.HomeFragment"
        android:label="Home">
        <action
            android:id="@+id/action_home_to_detail"
            app:destination="@id/detailFragment" />
    </fragment>

    <fragment
        android:id="@+id/detailFragment"
        android:name="com.example.DetailFragment"
        android:label="Detail">
        <argument
            android:name="itemId"
            app:argType="string" />
    </fragment>
</navigation>
```

```kotlin
// Navigate with arguments
class HomeFragment : Fragment() {
    fun navigateToDetail(itemId: String) {
        val action = HomeFragmentDirections.actionHomeToDetail(itemId)
        findNavController().navigate(action)
    }
}

// Receive arguments
class DetailFragment : Fragment() {
    private val args: DetailFragmentArgs by navArgs()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemId = args.itemId
    }
}
```

### DialogFragment and BottomSheetDialogFragment

Special fragments for dialogs.

```kotlin
// DialogFragment
class ConfirmDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Confirm")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                // Set result
                setFragmentResult("confirmKey", bundleOf("confirmed" to true))
            }
            .setNegativeButton("No", null)
            .create()
    }

    companion object {
        fun show(fragmentManager: FragmentManager) {
            ConfirmDialog().show(fragmentManager, "ConfirmDialog")
        }
    }
}

// BottomSheetDialogFragment
class OptionsBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<Button>(R.id.btnOption1).setOnClickListener {
            // Handle option 1
            dismiss()
        }
    }

    companion object {
        fun show(fragmentManager: FragmentManager) {
            OptionsBottomSheet().show(fragmentManager, "OptionsBottomSheet")
        }
    }
}
```

---

## Services

A Service runs in the background without a UI. Used for long-running operations.

### Started Services vs Bound Services

```kotlin
// Started Service - runs independently
class DownloadService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url")
        
        // Do work in background thread
        Thread {
            downloadFile(url)
            stopSelf(startId)  // Stop when done
        }.start()

        return START_NOT_STICKY  // Don't restart if killed
        // START_STICKY - Restart with null intent
        // START_REDELIVER_INTENT - Restart with last intent
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun downloadFile(url: String?) {
        // Download logic
    }
}

// Start the service
val intent = Intent(this, DownloadService::class.java).apply {
    putExtra("url", "https://example.com/file.zip")
}
startService(intent)
```

```kotlin
// Bound Service - allows component interaction
class MusicService : Service() {
    
    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun play(songUri: Uri) {
        mediaPlayer = MediaPlayer.create(this, songUri)
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}

// Activity binding to service
class MusicActivity : AppCompatActivity() {
    
    private var musicService: MusicService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    fun playMusic() {
        musicService?.play(songUri)
    }
}
```

### Foreground Services with Notifications

Required for visible, ongoing operations (Android 8.0+).

```kotlin
class LocationTrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "LocationChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, notification)
        
        // Start location tracking
        startTracking()
        
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Location")
            .setContentText("Your location is being tracked")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        // Location tracking logic
    }
}

// In AndroidManifest.xml
// <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
// <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

// Start foreground service
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    startForegroundService(Intent(this, LocationTrackingService::class.java))
} else {
    startService(Intent(this, LocationTrackingService::class.java))
}
```

### WorkManager for Background Tasks (Modern Approach)

Recommended for deferrable, guaranteed background work.

```kotlin
// Simple Worker
class UploadWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val imageUri = inputData.getString("image_uri")
            uploadImage(imageUri)
            Result.success()
        } catch (e: Exception) {
            Result.retry()  // or Result.failure()
        }
    }

    private fun uploadImage(uri: String?) {
        // Upload logic
    }
}

// CoroutineWorker for coroutines
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                syncData()
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}

// Enqueue work
class MyApplication : Application() {
    
    fun scheduleUpload(imageUri: String) {
        // Input data
        val inputData = workDataOf("image_uri" to imageUri)

        // Constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        // One-time work
        val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(uploadRequest)
    }

    fun schedulePeriodicSync() {
        // Periodic work (minimum 15 minutes)
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "sync_work",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
    }

    // Chain work
    fun chainedWork() {
        val work1 = OneTimeWorkRequestBuilder<Worker1>().build()
        val work2 = OneTimeWorkRequestBuilder<Worker2>().build()
        val work3 = OneTimeWorkRequestBuilder<Worker3>().build()

        WorkManager.getInstance(this)
            .beginWith(work1)
            .then(work2)
            .then(work3)
            .enqueue()
    }

    // Observe work status
    fun observeWork(workId: UUID) {
        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(workId)
            .observe(lifecycleOwner) { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> { /* Success */ }
                    WorkInfo.State.FAILED -> { /* Failed */ }
                    WorkInfo.State.RUNNING -> { /* In progress */ }
                    else -> {}
                }
            }
    }
}
```

---

## Broadcast Receivers

Receives system-wide or app broadcasts.

### Static vs Dynamic Registration

```kotlin
// Broadcast Receiver
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Device just booted
            Log.d("BootReceiver", "Device booted")
            // Schedule background work
        }
    }
}

// Static registration in AndroidManifest.xml
// <receiver android:name=".BootReceiver"
//     android:exported="true"
//     android:enabled="true">
//     <intent-filter>
//         <action android:name="android.intent.action.BOOT_COMPLETED" />
//     </intent-filter>
// </receiver>
// <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

```kotlin
// Dynamic Registration (in Activity/Fragment)
class MainActivity : AppCompatActivity() {

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = level * 100 / scale.toFloat()
            Log.d("Battery", "Battery level: $batteryPct%")
        }
    }

    override fun onResume() {
        super.onResume()
        // Register receiver
        registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    override fun onPause() {
        super.onPause()
        // Unregister to prevent leaks
        unregisterReceiver(batteryReceiver)
    }
}
```

### Custom Broadcasts
```kotlin
// Send custom broadcast
fun sendCustomBroadcast(context: Context) {
    val intent = Intent("com.example.MY_CUSTOM_ACTION").apply {
        putExtra("message", "Hello from broadcast!")
        setPackage(context.packageName)  // Explicit broadcast (security)
    }
    context.sendBroadcast(intent)
}

// Receive custom broadcast
class CustomReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.MY_CUSTOM_ACTION") {
            val message = intent.getStringExtra("message")
            Log.d("Custom", "Received: $message")
        }
    }
}

// Register dynamically
val filter = IntentFilter("com.example.MY_CUSTOM_ACTION")
registerReceiver(customReceiver, filter, RECEIVER_NOT_EXPORTED)
```

### Security Considerations

```kotlin
// Send broadcast with permission
sendBroadcast(intent, "com.example.MY_PERMISSION")

// Receiver requires permission
registerReceiver(
    receiver,
    intentFilter,
    "com.example.MY_PERMISSION",
    null,
    RECEIVER_NOT_EXPORTED
)

// In manifest, define custom permission
// <permission android:name="com.example.MY_PERMISSION"
//     android:protectionLevel="signature" />
```

---

## Content Providers

Manages shared app data. Provides standardized interface for data access.

### CRUD Operations

```kotlin
// Query contacts (requires READ_CONTACTS permission)
fun queryContacts(context: Context): List<Contact> {
    val contacts = mutableListOf<Contact>()
    
    val cursor = context.contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,  // URI
        arrayOf(                                  // Projection
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        ),
        null,  // Selection
        null,  // Selection args
        ContactsContract.Contacts.DISPLAY_NAME + " ASC"  // Sort order
    )

    cursor?.use {
        while (it.moveToNext()) {
            val id = it.getLong(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
            contacts.add(Contact(id, name))
        }
    }
    return contacts
}

// Insert data
fun insertNote(context: Context, title: String, content: String): Uri? {
    val values = ContentValues().apply {
        put("title", title)
        put("content", content)
        put("created_at", System.currentTimeMillis())
    }
    return context.contentResolver.insert(NotesProvider.CONTENT_URI, values)
}

// Update data
fun updateNote(context: Context, id: Long, newTitle: String): Int {
    val values = ContentValues().apply {
        put("title", newTitle)
    }
    return context.contentResolver.update(
        ContentUris.withAppendedId(NotesProvider.CONTENT_URI, id),
        values,
        null,
        null
    )
}

// Delete data
fun deleteNote(context: Context, id: Long): Int {
    return context.contentResolver.delete(
        ContentUris.withAppendedId(NotesProvider.CONTENT_URI, id),
        null,
        null
    )
}
```

### Custom Content Provider

```kotlin
class NotesProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.notes.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/notes")
        
        const val NOTES = 1
        const val NOTE_ID = 2
        
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "notes", NOTES)
            addURI(AUTHORITY, "notes/#", NOTE_ID)
        }
    }

    private lateinit var dbHelper: NotesDatabaseHelper

    override fun onCreate(): Boolean {
        dbHelper = NotesDatabaseHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        
        return when (uriMatcher.match(uri)) {
            NOTES -> db.query("notes", projection, selection, selectionArgs, null, null, sortOrder)
            NOTE_ID -> {
                val id = ContentUris.parseId(uri)
                db.query("notes", projection, "_id = ?", arrayOf(id.toString()), null, null, sortOrder)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        
        return when (uriMatcher.match(uri)) {
            NOTES -> {
                val id = db.insert("notes", null, values)
                context?.contentResolver?.notifyChange(uri, null)
                ContentUris.withAppendedId(CONTENT_URI, id)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase
        
        val count = when (uriMatcher.match(uri)) {
            NOTES -> db.update("notes", values, selection, selectionArgs)
            NOTE_ID -> {
                val id = ContentUris.parseId(uri)
                db.update("notes", values, "_id = ?", arrayOf(id.toString()))
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        
        if (count > 0) context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase
        
        val count = when (uriMatcher.match(uri)) {
            NOTES -> db.delete("notes", selection, selectionArgs)
            NOTE_ID -> {
                val id = ContentUris.parseId(uri)
                db.delete("notes", "_id = ?", arrayOf(id.toString()))
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        
        if (count > 0) context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            NOTES -> "vnd.android.cursor.dir/vnd.$AUTHORITY.notes"
            NOTE_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.notes"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }
}

// Register in AndroidManifest.xml
// <provider
//     android:name=".NotesProvider"
//     android:authorities="com.example.notes.provider"
//     android:exported="false" />
```

---

## 2.2 Intents and Navigation

### Explicit vs Implicit Intents

```kotlin
// Explicit Intent - specific component
val explicitIntent = Intent(this, DetailActivity::class.java)
startActivity(explicitIntent)

// Implicit Intent - action-based
val implicitIntent = Intent(Intent.ACTION_VIEW).apply {
    data = Uri.parse("https://www.google.com")
}
startActivity(implicitIntent)

// Implicit Intent - share text
val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, "Check out this app!")
}
startActivity(Intent.createChooser(shareIntent, "Share via"))

// Implicit Intent - open email
val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
    data = Uri.parse("mailto:")
    putExtra(Intent.EXTRA_EMAIL, arrayOf("email@example.com"))
    putExtra(Intent.EXTRA_SUBJECT, "Subject")
}
if (emailIntent.resolveActivity(packageManager) != null) {
    startActivity(emailIntent)
}

// Implicit Intent - dial phone
val dialIntent = Intent(Intent.ACTION_DIAL).apply {
    data = Uri.parse("tel:1234567890")
}
startActivity(dialIntent)
```

### Intent Extras and Data Passing

```kotlin
// Passing data with Intent
class MainActivity : AppCompatActivity() {
    
    fun openDetail(user: User) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            // Primitives
            putExtra("user_id", user.id)
            putExtra("user_name", user.name)
            
            // Bundle
            putExtra("user_bundle", Bundle().apply {
                putString("name", user.name)
                putInt("age", user.age)
            })
            
            // Parcelable (recommended)
            putExtra("user", user)  // User implements Parcelable
            
            // Serializable (slower)
            putExtra("user_serializable", user)  // User implements Serializable
        }
        startActivity(intent)
    }
}

// Receiving data
class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get primitives
        val userId = intent.getIntExtra("user_id", -1)
        val userName = intent.getStringExtra("user_name")
        
        // Get Parcelable
        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("user", User::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("user")
        }
    }
}

// Parcelable with @Parcelize (requires kotlin-parcelize plugin)
@Parcelize
data class User(
    val id: Int,
    val name: String,
    val age: Int
) : Parcelable
```

### PendingIntent Usage

```kotlin
// PendingIntent for notifications
fun createNotificationWithAction(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,  // Request code
        intent,
        PendingIntent.FLAG_IMMUTABLE  // Required for Android 12+
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle("New Message")
        .setContentText("Tap to open")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()
}

// PendingIntent for AlarmManager
fun scheduleAlarm(context: Context) {
    val intent = Intent(context, AlarmReceiver::class.java)
    
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        System.currentTimeMillis() + 60000,  // 1 minute
        pendingIntent
    )
}

// PendingIntent for widgets
fun createWidgetPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, WidgetProvider::class.java).apply {
        action = "WIDGET_CLICK"
    }
    return PendingIntent.getBroadcast(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
```

### Navigation Component

Modern navigation with type-safe arguments.

```xml
<!-- nav_graph.xml -->
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/homeFragment">

    <fragment
        android:id="@+id/homeFragment"
        android:name="com.example.HomeFragment"
        android:label="Home">
        
        <action
            android:id="@+id/action_home_to_profile"
            app:destination="@id/profileFragment"
            app:enterAnim="@anim/slide_in_right"
            app:exitAnim="@anim/slide_out_left"
            app:popEnterAnim="@anim/slide_in_left"
            app:popExitAnim="@anim/slide_out_right" />
    </fragment>

    <fragment
        android:id="@+id/profileFragment"
        android:name="com.example.ProfileFragment"
        android:label="Profile">
        
        <argument
            android:name="userId"
            app:argType="integer" />
        <argument
            android:name="userName"
            app:argType="string"
            android:defaultValue="Guest" />
    </fragment>

    <!-- Deep link -->
    <fragment
        android:id="@+id/productFragment"
        android:name="com.example.ProductFragment">
        
        <deepLink
            android:id="@+id/deepLink"
            app:uri="myapp://product/{productId}" />
        
        <argument
            android:name="productId"
            app:argType="string" />
    </fragment>
</navigation>
```

```kotlin
// Activity setup
class MainActivity : AppCompatActivity() {
    
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup with toolbar
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        // Setup with bottom navigation
        bottomNav.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

// Navigate with Safe Args
class HomeFragment : Fragment() {
    
    fun navigateToProfile(userId: Int, userName: String) {
        // Generated action with type-safe args
        val action = HomeFragmentDirections.actionHomeToProfile(
            userId = userId,
            userName = userName
        )
        findNavController().navigate(action)
    }
}

// Receive arguments
class ProfileFragment : Fragment() {
    
    private val args: ProfileFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val userId = args.userId
        val userName = args.userName
        
        textView.text = "User: $userName (ID: $userId)"
    }
}
```

### Single Activity Architecture

One activity hosts all fragments, navigation managed by Navigation Component.

```kotlin
// Single MainActivity
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

// activity_main.xml
// <androidx.fragment.app.FragmentContainerView
//     android:id="@+id/nav_host_fragment"
//     android:name="androidx.navigation.fragment.NavHostFragment"
//     android:layout_width="match_parent"
//     android:layout_height="match_parent"
//     app:defaultNavHost="true"
//     app:navGraph="@navigation/nav_graph" />

// Benefits:
// - Single source of truth for navigation
// - Easier deep linking
// - Better transition animations
// - Shared ViewModel between fragments
// - Less memory overhead
```

---

## 2.3 Android Manifest

The manifest file describes essential information about the app.

### Permissions Declaration and Handling

```xml
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Normal permissions (auto-granted) -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <!-- Dangerous permissions (require runtime request) -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <!-- Background location (Android 10+) -->
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

    <!-- Foreground service types (Android 14+) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />

    <!-- Post notifications (Android 13+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

</manifest>
```

```kotlin
// Runtime permission handling
class CameraActivity : AppCompatActivity() {

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showPermissionDenied()
        }
    }

    private val multiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startLocationTracking()
        }
    }

    fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRationaleDialog()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    fun requestLocationPermissions() {
        multiplePermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
```

### Application Components Registration

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".MyApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApp"
        android:networkSecurityConfig="@xml/network_security_config">

        <!-- Activities -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".DetailActivity"
            android:exported="false"
            android:parentActivityName=".MainActivity" />

        <!-- Services -->
        <service
            android:name=".services.DownloadService"
            android:exported="false" />

        <service
            android:name=".services.LocationService"
            android:exported="false"
            android:foregroundServiceType="location" />

        <!-- Broadcast Receivers -->
        <receiver
            android:name=".receivers.BootReceiver"
            android:exported="true"
            android:enabled="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

        <!-- Content Providers -->
        <provider
            android:name=".providers.NotesProvider"
            android:authorities="com.example.notes.provider"
            android:exported="false"
            android:grantUriPermissions="true" />

        <!-- FileProvider for sharing files -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

    </application>
</manifest>
```

### Intent Filters

```xml
<!-- Main launcher activity -->
<activity android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<!-- Handle specific file types -->
<activity android:name=".PdfViewerActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="application/pdf" />
    </intent-filter>
</activity>

<!-- Handle custom scheme -->
<activity android:name=".DeepLinkActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="myapp"
            android:host="open" />
    </intent-filter>
</activity>

<!-- Handle web URLs (App Links) -->
<activity android:name=".WebLinkActivity"
    android:exported="true">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="www.example.com"
            android:pathPattern="/product/.*" />
    </intent-filter>
</activity>

<!-- Share receiver -->
<activity android:name=".ShareReceiverActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="image/*" />
    </intent-filter>
</activity>
```

### Metadata and Configurations

```xml
<application ...>

    <!-- Google Maps API Key -->
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="${MAPS_API_KEY}" />

    <!-- Firebase Cloud Messaging -->
    <meta-data
        android:name="firebase_messaging_auto_init_enabled"
        android:value="false" />

    <!-- Default notification channel -->
    <meta-data
        android:name="com.google.firebase.messaging.default_notification_channel_id"
        android:value="@string/default_notification_channel_id" />

    <!-- Disable Google Play Services ads -->
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-xxxxx~xxxxx" />

    <!-- WorkManager initialization -->
    <provider
        android:name="androidx.startup.InitializationProvider"
        android:authorities="${applicationId}.androidx-startup"
        android:exported="false"
        tools:node="merge">
        <meta-data
            android:name="androidx.work.WorkManagerInitializer"
            android:value="androidx.startup"
            tools:node="remove" />
    </provider>

</application>

<!-- Hardware features -->
<uses-feature
    android:name="android.hardware.camera"
    android:required="true" />

<uses-feature
    android:name="android.hardware.location.gps"
    android:required="false" />

<!-- Queries (Android 11+ package visibility) -->
<queries>
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <data android:scheme="https" />
    </intent>
    <package android:name="com.whatsapp" />
</queries>
```

---

## Quick Reference Table

| Component | Purpose | Lifecycle Owner | Communication |
|-----------|---------|-----------------|---------------|
| Activity | Single screen UI | Self | Intent, ViewModel |
| Fragment | Reusable UI portion | Activity | ViewModel, Result API |
| Service | Background work | Self | Binder, Broadcast |
| BroadcastReceiver | System events | None | Intent |
| ContentProvider | Data sharing | None | ContentResolver |

---

## Best Practices Summary

1. **Activities**: Use ViewModel for configuration changes, single activity architecture for modern apps
2. **Fragments**: Prefer ViewModel for communication, use Navigation Component
3. **Services**: Use WorkManager for most background tasks, foreground service only when necessary
4. **Broadcasts**: Prefer dynamic registration, always unregister to prevent leaks
5. **Content Providers**: Use for sharing data between apps, implement proper security
6. **Intents**: Use explicit intents when possible, check resolveActivity() for implicit intents
7. **Permissions**: Request at runtime, explain why needed, handle denial gracefully




