# Android Jetpack Libraries - Complete Guide with Code Examples

## Table of Contents
1. [Lifecycle](#51-lifecycle)
   - [LifecycleOwner and LifecycleObserver](#lifecycleowner-and-lifecycleobserver)
   - [Lifecycle-aware Components](#lifecycle-aware-components)
   - [ProcessLifecycleOwner](#processlifecycleowner-for-app-lifecycle)
   - [repeatOnLifecycle for Safe Collection](#lifecyclerepeatonlifecycle-for-safe-collection)
2. [ViewModel](#52-viewmodel)
   - [ViewModelProvider and Factory Pattern](#viewmodelprovider-and-factory-pattern)
   - [SavedStateHandle for Process Death](#savedstatehandle-for-process-death)
   - [Sharing ViewModels Between Fragments](#sharing-viewmodels-between-fragments)
   - [ViewModel with Hilt Injection](#viewmodel-with-hilt-injection)
3. [LiveData vs StateFlow/SharedFlow](#53-livedata-legacy-vs-stateflowsharedflow)
   - [Observers and Lifecycle Awareness](#observers-and-lifecycle-awareness)
   - [Transformations: map, switchMap](#transformations-map-switchmap)
   - [MediatorLiveData](#mediatorlivedata-for-combining-sources)
4. [Room Database](#54-room-database)
   - [Core Concepts](#core-concepts)
   - [Advanced Room](#advanced-room)

---

# 5. Jetpack Libraries

Jetpack is a suite of libraries that help developers follow best practices, reduce boilerplate code, and write code that works consistently across Android versions and devices.

---

# 5.1 Lifecycle

## Overview

**Theory:**
The Lifecycle library provides classes and interfaces that let you build lifecycle-aware components. These components automatically adjust their behavior based on the current lifecycle state of an Activity or Fragment.

**Key Benefits:**
- Prevents memory leaks by proper cleanup
- Avoids crashes due to stopped activities
- Cleaner, more maintainable code
- Decouples lifecycle logic from UI components

```
┌──────────────────────────────────────────────────────────────────┐
│                        LIFECYCLE STATES                          │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│   INITIALIZED ──► CREATED ──► STARTED ──► RESUMED               │
│        │              │           │           │                  │
│        │              │           │           ▼                  │
│        │              │           │       (Active)               │
│        │              │           │           │                  │
│        │              │           ◄───────────┘                  │
│        │              │       STARTED                            │
│        │              │           │                              │
│        │              ◄───────────┘                              │
│        │          CREATED                                        │
│        │              │                                          │
│        ◄──────────────┘                                          │
│    DESTROYED                                                     │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## LifecycleOwner and LifecycleObserver

**Theory:**
- **LifecycleOwner**: An interface that indicates the class has a Lifecycle (Activities and Fragments implement this)
- **LifecycleObserver**: An interface for observing lifecycle changes (deprecated in favor of DefaultLifecycleObserver)
- **DefaultLifecycleObserver**: Modern replacement with default method implementations

### LifecycleOwner Interface

```kotlin
// LifecycleOwner is implemented by Activity and Fragment
interface LifecycleOwner {
    val lifecycle: Lifecycle
}

// Access lifecycle in Activity
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 'this' is a LifecycleOwner
        val currentState = lifecycle.currentState
        
        // Check if at least STARTED
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            // Safe to update UI
        }
    }
}
```

### DefaultLifecycleObserver (Recommended)

```kotlin
/**
 * Modern way to observe lifecycle events
 * Implement only the callbacks you need
 */
class LocationManager(
    private val context: Context,
    private val onLocationUpdate: (Location) -> Unit
) : DefaultLifecycleObserver {
    
    private var fusedLocationClient: FusedLocationProviderClient? = null
    
    override fun onCreate(owner: LifecycleOwner) {
        // Initialize resources
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        Log.d("LocationManager", "Initialized in onCreate")
    }
    
    override fun onStart(owner: LifecycleOwner) {
        // Component is visible, might start observing
        Log.d("LocationManager", "onStart - Component visible")
    }
    
    override fun onResume(owner: LifecycleOwner) {
        // Start location updates when in foreground
        startLocationUpdates()
        Log.d("LocationManager", "Started location updates")
    }
    
    override fun onPause(owner: LifecycleOwner) {
        // Stop updates when leaving foreground
        stopLocationUpdates()
        Log.d("LocationManager", "Stopped location updates")
    }
    
    override fun onStop(owner: LifecycleOwner) {
        // Component is no longer visible
        Log.d("LocationManager", "onStop - Component not visible")
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        // Clean up resources
        fusedLocationClient = null
        Log.d("LocationManager", "Destroyed - cleaned up resources")
    }
    
    private fun startLocationUpdates() {
        // Implementation for location updates
    }
    
    private fun stopLocationUpdates() {
        // Implementation to stop updates
    }
}

// Usage in Activity
class MapsActivity : AppCompatActivity() {
    
    private lateinit var locationManager: LocationManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        
        locationManager = LocationManager(this) { location ->
            updateMapLocation(location)
        }
        
        // Register the observer - it will automatically receive lifecycle callbacks
        lifecycle.addObserver(locationManager)
    }
    
    private fun updateMapLocation(location: Location) {
        // Update map with new location
    }
}
```

### Legacy LifecycleObserver with Annotations (Deprecated)

```kotlin
/**
 * @Deprecated - Use DefaultLifecycleObserver instead
 * This approach uses annotations which is less type-safe
 */
class LegacyLocationObserver : LifecycleObserver {
    
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun startTracking() {
        // Start location tracking
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stopTracking() {
        // Stop location tracking
    }
}
```

### LifecycleEventObserver

```kotlin
/**
 * Alternative when you need to handle events generically
 * or respond to all events with custom logic
 */
class GenericLifecycleObserver : LifecycleEventObserver {
    
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                Log.d("Lifecycle", "ON_CREATE from ${source::class.simpleName}")
            }
            Lifecycle.Event.ON_START -> {
                Log.d("Lifecycle", "ON_START")
            }
            Lifecycle.Event.ON_RESUME -> {
                Log.d("Lifecycle", "ON_RESUME")
            }
            Lifecycle.Event.ON_PAUSE -> {
                Log.d("Lifecycle", "ON_PAUSE")
            }
            Lifecycle.Event.ON_STOP -> {
                Log.d("Lifecycle", "ON_STOP")
            }
            Lifecycle.Event.ON_DESTROY -> {
                Log.d("Lifecycle", "ON_DESTROY")
            }
            Lifecycle.Event.ON_ANY -> {
                // Triggered for any event
            }
        }
    }
}
```

---

## Lifecycle-aware Components

**Theory:**
Lifecycle-aware components perform actions in response to lifecycle state changes. This approach moves lifecycle-dependent code from activities/fragments into the components themselves.

### Creating Custom Lifecycle-aware Components

```kotlin
/**
 * A lifecycle-aware video player that automatically
 * pauses/resumes based on lifecycle state
 */
class LifecycleAwareVideoPlayer(
    private val context: Context,
    private val videoView: VideoView
) : DefaultLifecycleObserver {
    
    private var playbackPosition: Int = 0
    private var wasPlaying: Boolean = false
    
    override fun onResume(owner: LifecycleOwner) {
        // Resume playback if it was playing before pause
        if (wasPlaying) {
            videoView.seekTo(playbackPosition)
            videoView.start()
        }
    }
    
    override fun onPause(owner: LifecycleOwner) {
        // Save state and pause
        wasPlaying = videoView.isPlaying
        playbackPosition = videoView.currentPosition
        videoView.pause()
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        // Clean up resources
        videoView.stopPlayback()
    }
    
    fun play(videoUri: Uri) {
        videoView.setVideoURI(videoUri)
        videoView.start()
        wasPlaying = true
    }
    
    fun pause() {
        playbackPosition = videoView.currentPosition
        videoView.pause()
        wasPlaying = false
    }
}

/**
 * A lifecycle-aware network connectivity monitor
 */
class NetworkMonitor(
    private val context: Context,
    private val onNetworkChanged: (Boolean) -> Unit
) : DefaultLifecycleObserver {
    
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onNetworkChanged(true)
        }
        
        override fun onLost(network: Network) {
            onNetworkChanged(false)
        }
    }
    
    override fun onStart(owner: LifecycleOwner) {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    override fun onStop(owner: LifecycleOwner) {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

// Usage
class VideoActivity : AppCompatActivity() {
    
    private lateinit var videoPlayer: LifecycleAwareVideoPlayer
    private lateinit var networkMonitor: NetworkMonitor
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        
        val videoView = findViewById<VideoView>(R.id.videoView)
        
        // Create lifecycle-aware components
        videoPlayer = LifecycleAwareVideoPlayer(this, videoView)
        networkMonitor = NetworkMonitor(this) { isConnected ->
            runOnUiThread {
                if (!isConnected) {
                    showNoConnectionWarning()
                }
            }
        }
        
        // Register observers - they handle their own lifecycle
        lifecycle.addObserver(videoPlayer)
        lifecycle.addObserver(networkMonitor)
        
        // Just start playback, component handles pause/resume automatically
        videoPlayer.play(Uri.parse("https://example.com/video.mp4"))
    }
}
```

### Built-in Lifecycle-aware Components

```kotlin
// LiveData is lifecycle-aware
class MyActivity : AppCompatActivity() {
    private val viewModel: MyViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // LiveData automatically stops observing when activity stops
        viewModel.data.observe(this) { data ->
            // Only called when activity is at least STARTED
            updateUI(data)
        }
    }
}

// LifecycleCoroutineScope
class MyFragment : Fragment() {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Automatically cancelled when lifecycle is destroyed
        viewLifecycleOwner.lifecycleScope.launch {
            val data = repository.fetchData()
            updateUI(data)
        }
        
        // Launch when lifecycle reaches STARTED, cancelled when below STARTED
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            // Only runs when fragment is at least STARTED
        }
    }
}
```

---

## ProcessLifecycleOwner for App Lifecycle

**Theory:**
`ProcessLifecycleOwner` provides a lifecycle for the whole application process. It allows you to observe when the app goes to the foreground or background, rather than individual activities.

**Use Cases:**
- Analytics: Track app foreground/background time
- Logging: Log when user opens/closes app
- Cleanup: Release resources when app goes to background
- Authentication: Lock app after going to background

```kotlin
/**
 * Observe entire application lifecycle
 * Useful for detecting app foreground/background state
 */
class AppLifecycleObserver : DefaultLifecycleObserver {
    
    override fun onStart(owner: LifecycleOwner) {
        // App moved to foreground (at least one activity visible)
        Log.d("AppLifecycle", "App in FOREGROUND")
        Analytics.trackEvent("app_foreground")
    }
    
    override fun onStop(owner: LifecycleOwner) {
        // App moved to background (all activities stopped)
        Log.d("AppLifecycle", "App in BACKGROUND")
        Analytics.trackEvent("app_background")
    }
    
    override fun onCreate(owner: LifecycleOwner) {
        // Called once when application is created (cold start)
        Log.d("AppLifecycle", "App CREATED (cold start)")
    }
}

// Register in Application class
class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Register app lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }
}
```

### Advanced App Lifecycle Monitoring

```kotlin
/**
 * Comprehensive app lifecycle manager
 * Handles various app state transitions
 */
class AppStateManager private constructor(
    private val application: Application
) : DefaultLifecycleObserver {
    
    companion object {
        @Volatile
        private var instance: AppStateManager? = null
        
        fun init(application: Application): AppStateManager {
            return instance ?: synchronized(this) {
                instance ?: AppStateManager(application).also {
                    instance = it
                    ProcessLifecycleOwner.get().lifecycle.addObserver(it)
                }
            }
        }
        
        fun getInstance(): AppStateManager {
            return instance ?: throw IllegalStateException("AppStateManager not initialized")
        }
    }
    
    private var isAppInForeground = false
    private var backgroundTimeMillis: Long = 0
    private val listeners = mutableListOf<AppStateListener>()
    
    // Time threshold for "significant" background duration (e.g., require re-auth)
    private val significantBackgroundTime = 5 * 60 * 1000L // 5 minutes
    
    interface AppStateListener {
        fun onAppWentToForeground()
        fun onAppWentToBackground()
        fun onAppReturnedAfterSignificantTime()
    }
    
    fun addListener(listener: AppStateListener) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: AppStateListener) {
        listeners.remove(listener)
    }
    
    fun isInForeground(): Boolean = isAppInForeground
    
    override fun onStart(owner: LifecycleOwner) {
        val wasInBackground = !isAppInForeground
        isAppInForeground = true
        
        if (wasInBackground) {
            val backgroundDuration = System.currentTimeMillis() - backgroundTimeMillis
            
            listeners.forEach { it.onAppWentToForeground() }
            
            if (backgroundDuration > significantBackgroundTime) {
                listeners.forEach { it.onAppReturnedAfterSignificantTime() }
            }
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        isAppInForeground = false
        backgroundTimeMillis = System.currentTimeMillis()
        
        listeners.forEach { it.onAppWentToBackground() }
    }
}

// Security-focused use case: Auto-lock app
class SecurityManager(
    private val authRepository: AuthRepository
) : AppStateManager.AppStateListener {
    
    override fun onAppWentToForeground() {
        // App came to foreground
    }
    
    override fun onAppWentToBackground() {
        // Optionally clear sensitive data from memory
        clearSensitiveDataFromClipboard()
    }
    
    override fun onAppReturnedAfterSignificantTime() {
        // Require re-authentication
        authRepository.requireReAuth()
    }
    
    private fun clearSensitiveDataFromClipboard() {
        // Clear clipboard if it contains sensitive data
    }
}

// Initialize in Application
class SecureApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        val appStateManager = AppStateManager.init(this)
        val securityManager = SecurityManager(AuthRepository())
        appStateManager.addListener(securityManager)
    }
}
```

---

## Lifecycle.repeatOnLifecycle for Safe Collection

**Theory:**
`repeatOnLifecycle` is the recommended way to collect Flows in UI layers. It automatically starts collection when lifecycle reaches a certain state and cancels it when below that state. This prevents wasted resources and potential crashes.

**Why Not Just `launchWhenStarted`?**
- `launchWhenStarted` suspends collection but keeps the coroutine alive
- `repeatOnLifecycle` actually cancels and restarts the collection
- The latter is more efficient and prevents "backpressure" issues

```
┌────────────────────────────────────────────────────────────────────────────┐
│                  repeatOnLifecycle vs launchWhenStarted                     │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  launchWhenStarted:                                                        │
│  ┌─────────┐ PAUSE ┌─────────┐ PAUSE ┌─────────┐                          │
│  │Collecting├──────►│SUSPENDED├──────►│SUSPENDED│ (coroutine still alive) │
│  └─────────┘       └─────────┘       └─────────┘                          │
│     STARTED         STOPPED           STOPPED                              │
│                                                                            │
│  repeatOnLifecycle:                                                        │
│  ┌─────────┐ PAUSE ┌─────────┐ RESUME ┌─────────┐                         │
│  │Collecting├──────►│CANCELLED├───────►│Collecting│ (fresh start each time)│
│  └─────────┘       └─────────┘        └─────────┘                         │
│     STARTED         STOPPED            STARTED                             │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
```

### Basic Usage

```kotlin
class UserFragment : Fragment() {
    
    private val viewModel: UserViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // ✅ CORRECT: Safe collection with repeatOnLifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // This block is executed when STARTED, cancelled when below STARTED
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }
    
    private fun updateUI(state: UiState) {
        // Update UI based on state
    }
}
```

### Collecting Multiple Flows

```kotlin
class DashboardFragment : Fragment() {
    
    private val viewModel: DashboardViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Launch multiple collectors inside repeatOnLifecycle
                // ALL collectors will be cancelled when lifecycle goes below STARTED
                
                launch {
                    viewModel.userState.collect { user ->
                        updateUserSection(user)
                    }
                }
                
                launch {
                    viewModel.notificationsState.collect { notifications ->
                        updateNotificationsBadge(notifications)
                    }
                }
                
                launch {
                    viewModel.feedState.collect { feed ->
                        updateFeedSection(feed)
                    }
                }
            }
        }
    }
}
```

### Extension Function for Cleaner Syntax

```kotlin
// Utility extension function
inline fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (T) -> Unit
): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state) {
            collect { action(it) }
        }
    }
}

// Usage
class ProfileFragment : Fragment() {
    
    private val viewModel: ProfileViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Much cleaner syntax
        viewModel.profileState.collectWithLifecycle(viewLifecycleOwner) { state ->
            updateUI(state)
        }
    }
}
```

### Jetpack Compose Integration

```kotlin
// In Compose, use collectAsStateWithLifecycle
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    // Lifecycle-aware collection - automatically handles lifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (uiState) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Success -> ProfileContent((uiState as UiState.Success).data)
        is UiState.Error -> ErrorMessage((uiState as UiState.Error).message)
    }
}

// Multiple states
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val feedState by viewModel.feedState.collectAsStateWithLifecycle()
    val notificationsState by viewModel.notificationsState.collectAsStateWithLifecycle()
    
    Column {
        UserHeader(userState)
        NotificationBadge(notificationsState)
        FeedList(feedState)
    }
}
```

### Comparison: Different Collection Approaches

```kotlin
class FlowCollectionExamples : Fragment() {
    
    private val viewModel: MyViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // ❌ BAD: Not lifecycle-aware, can cause crashes/leaks
        lifecycleScope.launch {
            viewModel.data.collect { 
                updateUI(it) // May update UI even when fragment is stopped!
            }
        }
        
        // ⚠️ DEPRECATED: Uses launchWhenStarted (suspends, doesn't cancel)
        lifecycleScope.launchWhenStarted {
            viewModel.data.collect {
                updateUI(it) // Suspended but not cancelled when stopped
            }
        }
        
        // ❌ INEFFICIENT: Creates new collector on each start without proper cancellation
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                lifecycleScope.launch {
                    viewModel.data.collect { updateUI(it) }
                }
                // This creates a new collector each time without cancelling previous!
            }
        })
        
        // ✅ CORRECT: repeatOnLifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collect {
                    updateUI(it)
                }
            }
        }
        
        // ✅ CORRECT: flowWithLifecycle (alternative for single flow)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.data
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { updateUI(it) }
        }
    }
}
```

---

# 5.2 ViewModel

## Overview

**Theory:**
ViewModel is designed to store and manage UI-related data in a lifecycle-conscious way. It survives configuration changes and provides a clean separation between UI logic and data management.

**Key Benefits:**
- Survives configuration changes (rotation, language change, etc.)
- Separates UI data from UI controllers (Activity/Fragment)
- Provides a scope for coroutines that survives configuration changes
- Can be shared between multiple fragments

```
┌────────────────────────────────────────────────────────────────────┐
│                    VIEWMODEL LIFECYCLE                             │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│    Activity/Fragment Created                                       │
│            │                                                       │
│            ▼                                                       │
│    ┌──────────────────┐                                           │
│    │ ViewModelProvider│──► Gets or Creates ViewModel              │
│    └──────────────────┘                                           │
│            │                                                       │
│            ▼                                                       │
│    ┌──────────────────┐                                           │
│    │    ViewModel     │◄── Same instance survives rotation        │
│    │   (Instance)     │                                           │
│    └──────────────────┘                                           │
│            │                                                       │
│    Configuration Change (Rotation)                                 │
│            │                                                       │
│            ▼                                                       │
│    Activity Destroyed ──► Activity Recreated                       │
│            │                                                       │
│            ▼                                                       │
│    ┌──────────────────┐                                           │
│    │    SAME          │◄── ViewModel still alive!                  │
│    │   ViewModel      │    Data preserved                         │
│    └──────────────────┘                                           │
│            │                                                       │
│    User presses Back / finish()                                    │
│            │                                                       │
│            ▼                                                       │
│    ┌──────────────────┐                                           │
│    │   onCleared()    │◄── ViewModel destroyed                     │
│    │    Called        │    Clean up resources                      │
│    └──────────────────┘                                           │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

---

## ViewModelProvider and Factory Pattern

**Theory:**
`ViewModelProvider` is responsible for creating and retaining ViewModels. When a ViewModel needs dependencies (like repositories), you need to provide a `ViewModelProvider.Factory` to create it properly.

### Basic ViewModel Creation

```kotlin
// Simple ViewModel without dependencies
class CounterViewModel : ViewModel() {
    
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()
    
    fun increment() {
        _count.value++
    }
    
    fun decrement() {
        _count.value--
    }
}

// Usage in Activity - using property delegate (recommended)
class CounterActivity : AppCompatActivity() {
    
    // Lazy instantiation using viewModels() delegate
    private val viewModel: CounterViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)
        
        // viewModel is automatically scoped to this activity
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.count.collect { count ->
                    counterTextView.text = "Count: $count"
                }
            }
        }
    }
}

// Alternative: Using ViewModelProvider directly
class CounterActivity : AppCompatActivity() {
    
    private lateinit var viewModel: CounterViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual creation using ViewModelProvider
        viewModel = ViewModelProvider(this)[CounterViewModel::class.java]
        
        // Or with ViewModelStore owner
        viewModel = ViewModelProvider(
            owner = this,  // ViewModelStoreOwner
            factory = ViewModelProvider.NewInstanceFactory()
        )[CounterViewModel::class.java]
    }
}
```

### Custom Factory for Dependencies

```kotlin
// ViewModel with dependencies
class UserViewModel(
    private val userRepository: UserRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    init {
        loadUsers()
    }
    
    private fun loadUsers() {
        viewModelScope.launch {
            _users.value = userRepository.getUsers()
            analyticsService.trackEvent("users_loaded")
        }
    }
}

// Custom Factory
class UserViewModelFactory(
    private val userRepository: UserRepository,
    private val analyticsService: AnalyticsService
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository, analyticsService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// Usage with factory
class UserActivity : AppCompatActivity() {
    
    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            userRepository = UserRepositoryImpl(),
            analyticsService = AnalyticsServiceImpl()
        )
    }
    
    // Alternative: Manual creation
    private fun createViewModelManually() {
        val factory = UserViewModelFactory(
            UserRepositoryImpl(),
            AnalyticsServiceImpl()
        )
        val viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }
}
```

### Modern Factory with CreationExtras

```kotlin
// Modern approach using CreationExtras (recommended for Kotlin)
class ModernUserViewModel(
    private val repository: UserRepository,
    private val userId: String
) : ViewModel() {
    
    companion object {
        // Define custom keys for extras
        val USER_ID_KEY = object : CreationExtras.Key<String> {}
        val REPOSITORY_KEY = object : CreationExtras.Key<UserRepository> {}
        
        // Factory using ViewModelProvider.Factory with createExtras
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val repository = extras[REPOSITORY_KEY] 
                    ?: throw IllegalArgumentException("Repository required")
                val userId = extras[USER_ID_KEY]
                    ?: throw IllegalArgumentException("UserID required")
                    
                return ModernUserViewModel(repository, userId) as T
            }
        }
    }
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    init {
        loadUser()
    }
    
    private fun loadUser() {
        viewModelScope.launch {
            _user.value = repository.getUser(userId)
        }
    }
}

// Usage with CreationExtras
class UserDetailActivity : AppCompatActivity() {
    
    private val viewModel: ModernUserViewModel by viewModels(
        extrasProducer = {
            // Build custom creation extras
            MutableCreationExtras(defaultViewModelCreationExtras).apply {
                set(ModernUserViewModel.USER_ID_KEY, intent.getStringExtra("userId") ?: "")
                set(ModernUserViewModel.REPOSITORY_KEY, UserRepositoryImpl())
            }
        },
        factoryProducer = { ModernUserViewModel.Factory }
    )
}
```

---

## SavedStateHandle for Process Death

**Theory:**
When Android kills your app's process to reclaim memory, all ViewModel data is lost. `SavedStateHandle` allows you to save and restore small amounts of UI state data that survives process death.

**What SavedStateHandle Can Save:**
- Primitive types (Int, String, Boolean, etc.)
- Parcelable and Serializable objects
- Bundles
- Arrays of supported types

```
┌──────────────────────────────────────────────────────────────────────────┐
│           CONFIGURATION CHANGE vs PROCESS DEATH                          │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Configuration Change (Rotation):                                        │
│  ┌───────────┐        ┌───────────┐                                     │
│  │  Activity │        │  Activity │                                     │
│  │ Destroyed │───────►│ Recreated │                                     │
│  └───────────┘        └───────────┘                                     │
│        │                    │                                            │
│        ▼                    ▼                                            │
│  ┌───────────┐        ┌───────────┐                                     │
│  │ ViewModel │ ═══════│ ViewModel │  ← SAME instance (survives)         │
│  │ (data ✓)  │        │ (data ✓)  │                                     │
│  └───────────┘        └───────────┘                                     │
│                                                                          │
│  Process Death:                                                          │
│  ┌───────────┐        ┌───────────┐                                     │
│  │  Activity │        │  Activity │                                     │
│  │  Killed   │───────►│ Recreated │                                     │
│  └───────────┘        └───────────┘                                     │
│        │                    │                                            │
│        ▼                    ▼                                            │
│  ┌───────────┐        ┌───────────┐                                     │
│  │ ViewModel │        │ ViewModel │  ← NEW instance (data lost!)        │
│  │  GONE ✗   │        │ NEW (✗?)  │                                     │
│  └───────────┘        └───────────┘                                     │
│                                                                          │
│  With SavedStateHandle:                                                  │
│  ┌───────────┐        ┌───────────┐                                     │
│  │ ViewModel │───────►│ ViewModel │  ← NEW instance BUT data restored! │
│  │ + Handle  │        │ + Handle  │                                     │
│  │ (state ✓) │        │ (state ✓) │  ← via SavedStateHandle             │
│  └───────────┘        └───────────┘                                     │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### Basic SavedStateHandle Usage

```kotlin
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val searchRepository: SearchRepository
) : ViewModel() {
    
    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
        private const val KEY_SELECTED_FILTER = "selected_filter"
    }
    
    // Using SavedStateHandle as a property delegate
    // Automatically saved and restored
    var searchQuery: String by savedStateHandle.saveable { "" }
        private set
    
    // StateFlow backed by SavedStateHandle
    val selectedFilter: StateFlow<String> = 
        savedStateHandle.getStateFlow(KEY_SELECTED_FILTER, "all")
    
    // Search results (not saved - will be re-fetched)
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    
    init {
        // Restore search if query was saved
        savedStateHandle.get<String>(KEY_SEARCH_QUERY)?.let { query ->
            if (query.isNotEmpty()) {
                search(query)
            }
        }
    }
    
    fun search(query: String) {
        // Save query to survive process death
        savedStateHandle[KEY_SEARCH_QUERY] = query
        searchQuery = query
        
        viewModelScope.launch {
            _searchResults.value = searchRepository.search(query)
        }
    }
    
    fun setFilter(filter: String) {
        savedStateHandle[KEY_SELECTED_FILTER] = filter
    }
    
    // Get navigation arguments
    fun getPageId(): String {
        // Arguments passed via navigation are automatically available
        return savedStateHandle["pageId"] ?: ""
    }
}

// Factory with SavedStateHandle
class SearchViewModelFactory(
    private val searchRepository: SearchRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return SearchViewModel(handle, searchRepository) as T
    }
}

// Usage in Activity
class SearchActivity : AppCompatActivity() {
    
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            searchRepository = SearchRepositoryImpl(),
            owner = this,
            defaultArgs = intent.extras
        )
    }
}
```

### Advanced SavedStateHandle Patterns

```kotlin
class FormViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // Complex object - must be Parcelable
    @Parcelize
    data class FormState(
        val name: String = "",
        val email: String = "",
        val phone: String = "",
        val isValid: Boolean = false
    ) : Parcelable
    
    // StateFlow from SavedStateHandle for Parcelable
    val formState: StateFlow<FormState> = savedStateHandle.getStateFlow("form_state", FormState())
    
    // LiveData alternative (also supports SavedStateHandle)
    val formStateLiveData: MutableLiveData<FormState> = 
        savedStateHandle.getLiveData("form_state_live", FormState())
    
    fun updateName(name: String) {
        val current = formState.value
        savedStateHandle["form_state"] = current.copy(
            name = name,
            isValid = validateForm(name, current.email, current.phone)
        )
    }
    
    fun updateEmail(email: String) {
        val current = formState.value
        savedStateHandle["form_state"] = current.copy(
            email = email,
            isValid = validateForm(current.name, email, current.phone)
        )
    }
    
    private fun validateForm(name: String, email: String, phone: String): Boolean {
        return name.isNotBlank() && email.contains("@") && phone.length >= 10
    }
    
    // Save list of items
    fun saveSelectedItems(items: List<String>) {
        savedStateHandle["selected_items"] = ArrayList(items)
    }
    
    fun getSelectedItems(): List<String> {
        return savedStateHandle["selected_items"] ?: emptyList()
    }
}

// Testing SavedStateHandle
class SearchViewModelTest {
    
    @Test
    fun `search query survives process death`() {
        // Create SavedStateHandle with initial state (simulating restored state)
        val savedStateHandle = SavedStateHandle(mapOf(
            "search_query" to "kotlin"
        ))
        
        val viewModel = SearchViewModel(savedStateHandle, FakeSearchRepository())
        
        // Query should be restored from SavedStateHandle
        assertEquals("kotlin", viewModel.searchQuery)
    }
}
```

---

## Sharing ViewModels Between Fragments

**Theory:**
ViewModels can be scoped to different lifecycle owners, allowing multiple fragments to share the same ViewModel instance. This is useful for communication between fragments and sharing data without tight coupling.

### Sharing ViewModel Scoped to Activity

```kotlin
// Shared ViewModel
class SharedCartViewModel : ViewModel() {
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    
    val totalPrice: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    fun addItem(item: CartItem) {
        _cartItems.value = _cartItems.value + item
    }
    
    fun removeItem(itemId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.id == itemId }
    }
    
    fun clearCart() {
        _cartItems.value = emptyList()
    }
}

// Fragment 1: Product List
class ProductListFragment : Fragment() {
    
    // Scoped to Activity - shared with other fragments
    private val cartViewModel: SharedCartViewModel by activityViewModels()
    
    // Own ViewModel - scoped to this fragment only
    private val productViewModel: ProductListViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observe cart to show badge
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartViewModel.cartItems.collect { items ->
                    updateCartBadge(items.size)
                }
            }
        }
        
        // Add to cart button click
        addToCartButton.setOnClickListener {
            val selectedProduct = productViewModel.selectedProduct.value
            selectedProduct?.let {
                cartViewModel.addItem(CartItem(it.id, it.name, it.price, 1))
            }
        }
    }
}

// Fragment 2: Cart
class CartFragment : Fragment() {
    
    // Same ViewModel instance as ProductListFragment!
    private val cartViewModel: SharedCartViewModel by activityViewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    cartViewModel.cartItems.collect { items ->
                        adapter.submitList(items)
                    }
                }
                
                launch {
                    cartViewModel.totalPrice.collect { total ->
                        totalTextView.text = "Total: $$total"
                    }
                }
            }
        }
        
        checkoutButton.setOnClickListener {
            // Navigate to checkout
            cartViewModel.clearCart()
        }
    }
}
```

### Sharing ViewModel Scoped to Navigation Graph

```kotlin
// When using Navigation Component, you can scope to a navigation graph
class CheckoutStep1Fragment : Fragment() {
    
    // Shared within the checkout navigation graph
    private val checkoutViewModel: CheckoutViewModel by navGraphViewModels(R.id.checkout_graph)
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        nextButton.setOnClickListener {
            checkoutViewModel.setShippingAddress(addressInput.text.toString())
            findNavController().navigate(R.id.action_step1_to_step2)
        }
    }
}

class CheckoutStep2Fragment : Fragment() {
    
    // Same instance as CheckoutStep1Fragment
    private val checkoutViewModel: CheckoutViewModel by navGraphViewModels(R.id.checkout_graph)
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Access data set in step 1
        val address = checkoutViewModel.shippingAddress.value
        
        nextButton.setOnClickListener {
            checkoutViewModel.setPaymentMethod(selectedPaymentMethod)
            findNavController().navigate(R.id.action_step2_to_step3)
        }
    }
}

// With Hilt
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val checkoutRepository: CheckoutRepository
) : ViewModel() {
    
    private val _shippingAddress = MutableStateFlow("")
    val shippingAddress: StateFlow<String> = _shippingAddress.asStateFlow()
    
    private val _paymentMethod = MutableStateFlow<PaymentMethod?>(null)
    val paymentMethod: StateFlow<PaymentMethod?> = _paymentMethod.asStateFlow()
    
    fun setShippingAddress(address: String) {
        _shippingAddress.value = address
    }
    
    fun setPaymentMethod(method: PaymentMethod) {
        _paymentMethod.value = method
    }
}

// Usage with Hilt
@AndroidEntryPoint
class CheckoutStep1Fragment : Fragment() {
    
    private val checkoutViewModel: CheckoutViewModel by navGraphViewModels(R.id.checkout_graph) {
        defaultViewModelProviderFactory // Uses Hilt factory
    }
}
```

### Parent-Child Fragment Communication

```kotlin
// Parent Fragment
class ParentContainerFragment : Fragment() {
    
    private val sharedViewModel: ParentChildViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Listen for events from child
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.childEvents.collect { event ->
                    handleChildEvent(event)
                }
            }
        }
    }
}

// Child Fragment
class ChildFragment : Fragment() {
    
    // Get ViewModel from parent fragment
    private val sharedViewModel: ParentChildViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        button.setOnClickListener {
            sharedViewModel.sendEvent(ChildEvent.ButtonClicked)
        }
    }
}

class ParentChildViewModel : ViewModel() {
    
    private val _childEvents = MutableSharedFlow<ChildEvent>()
    val childEvents: SharedFlow<ChildEvent> = _childEvents.asSharedFlow()
    
    fun sendEvent(event: ChildEvent) {
        viewModelScope.launch {
            _childEvents.emit(event)
        }
    }
}

sealed class ChildEvent {
    object ButtonClicked : ChildEvent()
    data class DataSubmitted(val data: String) : ChildEvent()
}
```

---

## ViewModel with Hilt Injection

**Theory:**
Hilt simplifies dependency injection in Android apps. With `@HiltViewModel`, you can inject dependencies directly into ViewModel constructors without writing custom factories.

### Setup

```kotlin
// build.gradle.kts (app)
plugins {
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}

// Application class
@HiltAndroidApp
class MyApplication : Application()
```

### Basic Hilt ViewModel

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val analyticsService: AnalyticsService,
    private val savedStateHandle: SavedStateHandle  // Automatically provided by Hilt
) : ViewModel() {
    
    private val userId: String = savedStateHandle["userId"] ?: ""
    
    private val _userState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val userState: StateFlow<UserUiState> = _userState.asStateFlow()
    
    init {
        loadUser()
    }
    
    private fun loadUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(userId)
                _userState.value = UserUiState.Success(user)
                analyticsService.trackEvent("user_loaded")
            } catch (e: Exception) {
                _userState.value = UserUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val user: User) : UserUiState()
    data class Error(val message: String) : UserUiState()
}

// Usage in Activity
@AndroidEntryPoint
class UserActivity : AppCompatActivity() {
    
    // Hilt automatically provides the ViewModel with injected dependencies
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...
    }
}

// Usage in Fragment
@AndroidEntryPoint
class UserFragment : Fragment() {
    
    private val viewModel: UserViewModel by viewModels()
    
    // Shared with activity using Hilt
    private val sharedViewModel: SharedViewModel by activityViewModels()
}
```

### Hilt Modules for Dependencies

```kotlin
// Repository module
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        userDao: UserDao
    ): UserRepository {
        return UserRepositoryImpl(apiService, userDao)
    }
    
    @Provides
    @Singleton
    fun provideAnalyticsService(): AnalyticsService {
        return AnalyticsServiceImpl()
    }
}

// Network module
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

// Database module
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
```

### Hilt ViewModel with Assisted Injection

```kotlin
// For ViewModels that need runtime parameters beyond SavedStateHandle
@HiltViewModel(assistedFactory = ProductDetailViewModel.Factory::class)
class ProductDetailViewModel @AssistedInject constructor(
    private val productRepository: ProductRepository,
    @Assisted private val productId: String,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    @AssistedFactory
    interface Factory {
        fun create(productId: String, savedStateHandle: SavedStateHandle): ProductDetailViewModel
    }
    
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()
    
    init {
        loadProduct()
    }
    
    private fun loadProduct() {
        viewModelScope.launch {
            _product.value = productRepository.getProduct(productId)
        }
    }
}

// Usage
@AndroidEntryPoint
class ProductDetailFragment : Fragment() {
    
    private val viewModel: ProductDetailViewModel by viewModels(
        extrasProducer = {
            defaultViewModelCreationExtras
        }
    )
}
```

### Hilt with Jetpack Compose

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    
    val posts: StateFlow<List<Post>> = postRepository.getPosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

// Compose with Hilt
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    
    LazyColumn {
        items(posts) { post ->
            PostCard(post)
        }
    }
}

// Navigation with Hilt ViewModels
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onPostClick = { postId ->
                    navController.navigate("post/$postId")
                }
            )
        }
        
        composable(
            route = "post/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            // SavedStateHandle automatically gets "postId" argument
            PostDetailScreen(
                viewModel = hiltViewModel()  // postId available in SavedStateHandle
            )
        }
    }
}
```

---
-------------yaha tha para he..
# 5.3 LiveData (Legacy) vs StateFlow/SharedFlow

## Overview

**Theory:**
Both LiveData and Flow are used for observable data patterns in Android. LiveData was the standard choice before Kotlin coroutines became mainstream. Now, StateFlow and SharedFlow are preferred for new code due to their coroutine integration and flexibility.

| Feature | LiveData | StateFlow | SharedFlow |
|---------|----------|-----------|------------|
| Lifecycle awareness | Built-in | Manual (repeatOnLifecycle) | Manual |
| Initial value | Optional | Required | Not required |
| Replays to new collectors | Latest value | Latest value | Configurable (0-N) |
| Use case | UI state | UI state | Events (one-time) |
| Multiple collectors | Supported | Supported | Supported |
| Backpressure | None | Conflated | Configurable |
| Kotlin-only | No | Yes | Yes |

---

## Observers and Lifecycle Awareness

### LiveData Observation

```kotlin
class UserViewModel : ViewModel() {
    
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadUser(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _user.value = repository.getUser(userId)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// LiveData in Activity - automatically lifecycle-aware
class UserActivity : AppCompatActivity() {
    
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        
        // Observer is automatically removed when activity is destroyed
        // Updates only when activity is at least STARTED
        viewModel.user.observe(this) { user ->
            nameTextView.text = user.name
            emailTextView.text = user.email
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.isVisible = isLoading
        }
    }
}

// LiveData in Fragment - use viewLifecycleOwner
class UserFragment : Fragment() {
    
    private val viewModel: UserViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // IMPORTANT: Use viewLifecycleOwner, not 'this'
        // Fragment can outlive its view in some cases
        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.nameTextView.text = user.name
        }
    }
}
```

### StateFlow/SharedFlow Observation

```kotlin
class UserViewModel : ViewModel() {
    
    // StateFlow - always has a value
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    // SharedFlow - for one-time events (no replay by default)
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val user = repository.getUser(userId)
                _uiState.value = UiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
                _events.emit(UiEvent.ShowErrorSnackbar(e.message ?: "Error"))
            }
        }
    }
    
    fun onButtonClicked() {
        viewModelScope.launch {
            _events.emit(UiEvent.NavigateToProfile)
        }
    }
    
    sealed class UiState {
        object Loading : UiState()
        data class Success(val user: User) : UiState()
        data class Error(val message: String) : UiState()
    }
    
    sealed class UiEvent {
        data class ShowErrorSnackbar(val message: String) : UiEvent()
        object NavigateToProfile : UiEvent()
    }
}

// StateFlow in Fragment - requires repeatOnLifecycle
class UserFragment : Fragment() {
    
    private val viewModel: UserViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Collect StateFlow with lifecycle awareness
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect UI state
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Loading -> showLoading()
                            is UiState.Success -> showUser(state.user)
                            is UiState.Error -> showError(state.message)
                        }
                    }
                }
                
                // Collect one-time events
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is UiEvent.ShowErrorSnackbar -> {
                                Snackbar.make(view, event.message, Snackbar.LENGTH_SHORT).show()
                            }
                            is UiEvent.NavigateToProfile -> {
                                findNavController().navigate(R.id.profileFragment)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### One-time Events: LiveData vs SharedFlow

```kotlin
// ❌ BAD: Using LiveData for one-time events
// Problem: Event is re-delivered on configuration change
class BadEventViewModel : ViewModel() {
    private val _navigateToDetail = MutableLiveData<String>()
    val navigateToDetail: LiveData<String> = _navigateToDetail
    
    fun onItemClicked(itemId: String) {
        _navigateToDetail.value = itemId  // Will be re-observed after rotation!
    }
}

// ⚠️ WORKAROUND: Event wrapper (not ideal)
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false
    
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

class EventWrapperViewModel : ViewModel() {
    private val _navigateToDetail = MutableLiveData<Event<String>>()
    val navigateToDetail: LiveData<Event<String>> = _navigateToDetail
    
    fun onItemClicked(itemId: String) {
        _navigateToDetail.value = Event(itemId)
    }
}

// ✅ CORRECT: SharedFlow for one-time events
class GoodEventViewModel : ViewModel() {
    private val _events = MutableSharedFlow<NavigationEvent>()
    val events: SharedFlow<NavigationEvent> = _events.asSharedFlow()
    
    fun onItemClicked(itemId: String) {
        viewModelScope.launch {
            _events.emit(NavigationEvent.NavigateToDetail(itemId))
        }
    }
    
    sealed class NavigationEvent {
        data class NavigateToDetail(val itemId: String) : NavigationEvent()
        object NavigateBack : NavigationEvent()
    }
}
```

---

## Transformations: map, switchMap

**Theory:**
Transformations allow you to transform LiveData values or switch to a different LiveData source based on values.

### LiveData Transformations

```kotlin
class ProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    // Source LiveData
    private val _productId = MutableLiveData<String>()
    
    // map: Transform the value
    val productName: LiveData<String> = _productId.map { id ->
        "Product #$id"
    }
    
    // switchMap: Switch to a different LiveData based on value
    val product: LiveData<Product> = _productId.switchMap { id ->
        productRepository.getProductLiveData(id)  // Returns LiveData<Product>
    }
    
    // Transformations chain
    val productDisplayInfo: LiveData<String> = product.map { product ->
        "${product.name} - $${product.price}"
    }
    
    fun selectProduct(productId: String) {
        _productId.value = productId
    }
}

// Practical example: Search with debounce using switchMap
class SearchViewModel(
    private val searchRepository: SearchRepository
) : ViewModel() {
    
    private val _searchQuery = MutableLiveData<String>()
    
    // Each time searchQuery changes, switch to new search results LiveData
    val searchResults: LiveData<List<SearchResult>> = _searchQuery.switchMap { query ->
        if (query.isBlank()) {
            MutableLiveData(emptyList())
        } else {
            searchRepository.searchLiveData(query)
        }
    }
    
    fun search(query: String) {
        _searchQuery.value = query
    }
}
```

### Flow Transformations (StateFlow/SharedFlow)

```kotlin
class ProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    // Source StateFlow
    private val _productId = MutableStateFlow("")
    
    // map: Transform the value - creates a new Flow
    val productName: Flow<String> = _productId.map { id ->
        "Product #$id"
    }
    
    // flatMapLatest: Equivalent to switchMap - switches to new Flow
    val product: StateFlow<Product?> = _productId
        .filter { it.isNotBlank() }
        .flatMapLatest { id ->
            productRepository.getProductFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // combine: Combine multiple flows
    private val _quantity = MutableStateFlow(1)
    
    val totalPrice: StateFlow<Double> = combine(
        product,
        _quantity
    ) { product, quantity ->
        (product?.price ?: 0.0) * quantity
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    fun selectProduct(productId: String) {
        _productId.value = productId
    }
    
    fun updateQuantity(quantity: Int) {
        _quantity.value = quantity
    }
}

// Search with debounce using Flow
class SearchViewModel(
    private val searchRepository: SearchRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    
    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<SearchResult>> = _searchQuery
        .debounce(300)  // Wait 300ms after last input
        .distinctUntilChanged()  // Only emit if value changed
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                searchRepository.searchFlow(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun updateQuery(query: String) {
        _searchQuery.value = query
    }
}
```

### Transformation Comparison

```kotlin
class TransformationComparison : ViewModel() {
    
    private val repository = ProductRepository()
    
    // ============ LiveData Transformations ============
    
    private val _selectedIdLD = MutableLiveData<String>()
    
    // map
    val mappedLD: LiveData<String> = _selectedIdLD.map { "ID: $it" }
    
    // switchMap
    val switchedLD: LiveData<Product> = _selectedIdLD.switchMap { id ->
        repository.getProductLiveData(id)
    }
    
    // ============ Flow Transformations ============
    
    private val _selectedIdFlow = MutableStateFlow("")
    
    // map
    val mappedFlow: Flow<String> = _selectedIdFlow.map { "ID: $it" }
    
    // flatMapLatest (equivalent to switchMap)
    val switchedFlow: StateFlow<Product?> = _selectedIdFlow
        .flatMapLatest { id -> 
            repository.getProductFlow(id) 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // filter
    val filteredFlow: Flow<String> = _selectedIdFlow.filter { it.isNotBlank() }
    
    // debounce (Flow-only)
    @OptIn(FlowPreview::class)
    val debouncedFlow: Flow<String> = _selectedIdFlow.debounce(300)
    
    // combine multiple flows (more flexible than MediatorLiveData)
    private val _otherFlow = MutableStateFlow(0)
    
    val combinedFlow: StateFlow<Pair<String, Int>> = combine(
        _selectedIdFlow,
        _otherFlow
    ) { id, other ->
        id to other
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "" to 0)
}
```

---

## MediatorLiveData for Combining Sources

**Theory:**
`MediatorLiveData` allows you to observe multiple LiveData sources and react to changes in any of them. It's useful for combining data from different sources or implementing complex logic.

### Basic MediatorLiveData

```kotlin
class DashboardViewModel(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    // Individual sources
    private val userLiveData: LiveData<User> = userRepository.getCurrentUser()
    private val notificationsLiveData: LiveData<List<Notification>> = 
        notificationRepository.getNotifications()
    
    // Combining with MediatorLiveData
    val dashboardState: LiveData<DashboardState> = MediatorLiveData<DashboardState>().apply {
        var user: User? = null
        var notifications: List<Notification>? = null
        
        fun update() {
            val currentUser = user ?: return
            val currentNotifications = notifications ?: return
            
            value = DashboardState(
                userName = currentUser.name,
                notificationCount = currentNotifications.size,
                unreadCount = currentNotifications.count { !it.isRead }
            )
        }
        
        addSource(userLiveData) { newUser ->
            user = newUser
            update()
        }
        
        addSource(notificationsLiveData) { newNotifications ->
            notifications = newNotifications
            update()
        }
    }
    
    data class DashboardState(
        val userName: String,
        val notificationCount: Int,
        val unreadCount: Int
    )
}
```

### Advanced MediatorLiveData Patterns

```kotlin
class FormViewModel : ViewModel() {
    
    // Individual field LiveData
    val firstName = MutableLiveData("")
    val lastName = MutableLiveData("")
    val email = MutableLiveData("")
    val phone = MutableLiveData("")
    
    // Form validity - depends on all fields
    val isFormValid: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        fun validate() {
            val validFirstName = !firstName.value.isNullOrBlank()
            val validLastName = !lastName.value.isNullOrBlank()
            val validEmail = email.value?.contains("@") == true
            val validPhone = (phone.value?.length ?: 0) >= 10
            
            value = validFirstName && validLastName && validEmail && validPhone
        }
        
        addSource(firstName) { validate() }
        addSource(lastName) { validate() }
        addSource(email) { validate() }
        addSource(phone) { validate() }
    }
    
    // Full name - combines first and last
    val fullName: LiveData<String> = MediatorLiveData<String>().apply {
        fun updateFullName() {
            val first = firstName.value ?: ""
            val last = lastName.value ?: ""
            value = "$first $last".trim()
        }
        
        addSource(firstName) { updateFullName() }
        addSource(lastName) { updateFullName() }
    }
}

// Dynamic source management
class FilteredListViewModel(
    private val repository: ItemRepository
) : ViewModel() {
    
    private val _selectedCategory = MutableLiveData<String>()
    private var currentSource: LiveData<List<Item>>? = null
    
    val filteredItems = MediatorLiveData<List<Item>>()
    
    init {
        filteredItems.addSource(_selectedCategory) { category ->
            // Remove previous source if exists
            currentSource?.let { filteredItems.removeSource(it) }
            
            // Add new source based on category
            val newSource = repository.getItemsByCategory(category)
            currentSource = newSource
            
            filteredItems.addSource(newSource) { items ->
                filteredItems.value = items
            }
        }
    }
    
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
}
```

### Flow Alternative: combine

```kotlin
class FormViewModel : ViewModel() {
    
    // StateFlow for each field
    val firstName = MutableStateFlow("")
    val lastName = MutableStateFlow("")
    val email = MutableStateFlow("")
    val phone = MutableStateFlow("")
    
    // Much cleaner with combine!
    val isFormValid: StateFlow<Boolean> = combine(
        firstName, lastName, email, phone
    ) { first, last, email, phone ->
        first.isNotBlank() && 
        last.isNotBlank() && 
        email.contains("@") && 
        phone.length >= 10
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    val fullName: StateFlow<String> = combine(
        firstName, lastName
    ) { first, last ->
        "$first $last".trim()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )
    
    // Multiple sources with different transformations
    val formSummary: StateFlow<FormSummary> = combine(
        firstName, lastName, email, phone, isFormValid
    ) { first, last, email, phone, isValid ->
        FormSummary(
            fullName = "$first $last",
            email = email,
            phone = phone,
            isComplete = isValid
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FormSummary()
    )
    
    data class FormSummary(
        val fullName: String = "",
        val email: String = "",
        val phone: String = "",
        val isComplete: Boolean = false
    )
}
```

---

# 5.4 Room Database

## Overview

**Theory:**
Room is an abstraction layer over SQLite that provides compile-time verification of SQL queries and convenient annotations for database operations. It's part of Android Architecture Components and integrates seamlessly with Kotlin coroutines and Flow.

**Key Benefits:**
- Compile-time query verification
- Less boilerplate than raw SQLite
- Built-in support for LiveData and Flow
- Easy migrations
- Works great with other Architecture Components

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         ROOM ARCHITECTURE                                 │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌─────────────┐              ┌─────────────┐             ┌───────────┐│
│   │   Entity    │              │     DAO     │             │  Database ││
│   │  (@Entity)  │◄────────────►│   (@Dao)    │◄───────────►│(@Database)││
│   │             │              │             │             │           ││
│   │ Data class  │              │  Interface  │             │ Abstract  ││
│   │ = Table     │              │ = Queries   │             │  Class    ││
│   └─────────────┘              └─────────────┘             └───────────┘│
│         │                            │                           │      │
│         ▼                            ▼                           ▼      │
│    ┌─────────┐               ┌──────────────┐            ┌───────────┐  │
│    │ Columns │               │ SQL Methods  │            │  Manages  │  │
│    │ Primary │               │ @Query       │            │ DAOs and  │  │
│    │  Keys   │               │ @Insert      │            │ Entities  │  │
│    │ Foreign │               │ @Update      │            │           │  │
│    │  Keys   │               │ @Delete      │            │           │  │
│    └─────────┘               └──────────────┘            └───────────┘  │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Core Concepts

### Entity - Defining Tables

```kotlin
/**
 * @Entity - Marks a class as a database table
 * Each property becomes a column
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "first_name")
    val firstName: String,
    
    @ColumnInfo(name = "last_name")
    val lastName: String,
    
    val email: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(defaultValue = "true")
    val isActive: Boolean = true
)

/**
 * Entity with composite primary key
 */
@Entity(
    tableName = "user_favorites",
    primaryKeys = ["userId", "productId"]
)
data class UserFavorite(
    val userId: Long,
    val productId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Entity with indices for faster queries
 */
@Entity(
    tableName = "products",
    indices = [
        Index(value = ["category"]),
        Index(value = ["name"], unique = true)
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val price: Double,
    val description: String?
)

/**
 * Entity with foreign key constraint
 */
@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE  // Delete orders when user is deleted
        )
    ],
    indices = [Index(value = ["userId"])]  // Index for foreign key
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val totalAmount: Double,
    val status: String,
    @ColumnInfo(name = "order_date")
    val orderDate: Long = System.currentTimeMillis()
)

/**
 * Ignoring fields
 */
@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    
    @Ignore  // Not stored in database
    val isBookmarked: Boolean = false,
    
    @Ignore
    val readingTimeMinutes: Int = 0
)
```

### DAO - Data Access Object

```kotlin
/**
 * @Dao - Defines database operations
 * Room generates implementation at compile time
 */
@Dao
interface UserDao {
    
    // ============ INSERT ============
    
    @Insert
    suspend fun insert(user: User): Long  // Returns inserted row ID
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(user: User)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(user: User): Long  // Returns -1 if ignored
    
    @Insert
    suspend fun insertAll(users: List<User>): List<Long>
    
    // ============ UPDATE ============
    
    @Update
    suspend fun update(user: User): Int  // Returns number of rows updated
    
    @Update
    suspend fun updateAll(users: List<User>)
    
    // ============ DELETE ============
    
    @Delete
    suspend fun delete(user: User): Int  // Returns number of rows deleted
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: Long)
    
    // ============ QUERY ============
    
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getById(userId: Long): User?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE first_name LIKE :search OR last_name LIKE :search")
    suspend fun searchByName(search: String): List<User>
    
    @Query("SELECT * FROM users ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentUsers(limit: Int): List<User>
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Query("SELECT * FROM users WHERE is_active = 1")
    suspend fun getActiveUsers(): List<User>
    
    // ============ FLOW (Reactive) ============
    
    @Query("SELECT * FROM users")
    fun getAllFlow(): Flow<List<User>>  // Emits new list on any change
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getByIdFlow(userId: Long): Flow<User?>
    
    @Query("SELECT COUNT(*) FROM users")
    fun getUserCountFlow(): Flow<Int>
    
    // ============ LIVEDATA ============
    
    @Query("SELECT * FROM users")
    fun getAllLiveData(): LiveData<List<User>>
    
    // ============ TRANSACTION ============
    
    @Transaction
    suspend fun replaceUsers(users: List<User>) {
        deleteAll()
        insertAll(users)
    }
}

/**
 * Complex queries
 */
@Dao
interface ProductDao {
    
    @Query("""
        SELECT * FROM products 
        WHERE category = :category 
        AND price BETWEEN :minPrice AND :maxPrice
        ORDER BY price ASC
    """)
    suspend fun filterProducts(
        category: String,
        minPrice: Double,
        maxPrice: Double
    ): List<Product>
    
    @Query("""
        SELECT * FROM products 
        WHERE name LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchProducts(query: String, limit: Int, offset: Int): List<Product>
    
    @Query("""
        SELECT category, COUNT(*) as count, AVG(price) as avgPrice
        FROM products
        GROUP BY category
    """)
    suspend fun getCategoryStats(): List<CategoryStats>
    
    data class CategoryStats(
        val category: String,
        val count: Int,
        val avgPrice: Double
    )
}
```

### Database - Tying It Together

```kotlin
/**
 * @Database - The main database holder
 * Must be abstract and extend RoomDatabase
 */
@Database(
    entities = [User::class, Product::class, Order::class],
    version = 1,
    exportSchema = true  // Export schema to json for migration testing
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()  // Only for development!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// With Hilt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Prepopulate database on first creation
                }
            })
            .build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }
}
```

### Type Converters for Custom Types

```kotlin
/**
 * Type converters for non-primitive types
 * Room needs to know how to store these in SQLite
 */
class Converters {
    
    // Date conversion
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    // LocalDateTime (requires API 26+)
    @TypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }
    
    @TypeConverter
    fun localDateTimeToString(date: LocalDateTime?): String? {
        return date?.toString()
    }
    
    // List<String> using JSON
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }
    
    // Enum conversion
    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String = status.name
    
    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)
}

enum class OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}

// Apply to specific field or globally
@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    
    @TypeConverters(Converters::class)
    val tags: List<String>,
    
    val startDate: LocalDateTime,
    val status: OrderStatus
)
```

### Relationships: @Relation and @Embedded

```kotlin
/**
 * @Embedded - Include all columns of another object
 */
data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    
    @Embedded(prefix = "billing_")  // Adds prefix to avoid column name conflicts
    val billingAddress: Address,
    
    @Embedded(prefix = "shipping_")
    val shippingAddress: Address
)

// Results in columns: billing_street, billing_city, shipping_street, etc.

/**
 * @Relation - Define one-to-many or many-to-many relationships
 */

// One User has Many Orders (1:N)
data class UserWithOrders(
    @Embedded
    val user: User,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val orders: List<Order>
)

@Dao
interface UserOrderDao {
    
    @Transaction  // Always use @Transaction with @Relation
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithOrders(userId: Long): UserWithOrders?
    
    @Transaction
    @Query("SELECT * FROM users")
    fun getAllUsersWithOrders(): Flow<List<UserWithOrders>>
}

// Many-to-Many relationship (User <-> Product favorites)
@Entity(tableName = "user_favorites")
data class UserFavorite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val productId: Long
)

data class UserWithFavoriteProducts(
    @Embedded
    val user: User,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = UserFavorite::class,
            parentColumn = "userId",
            entityColumn = "productId"
        )
    )
    val favoriteProducts: List<Product>
)

@Dao
interface FavoriteDao {
    
    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithFavorites(userId: Long): UserWithFavoriteProducts?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: UserFavorite)
    
    @Query("DELETE FROM user_favorites WHERE userId = :userId AND productId = :productId")
    suspend fun removeFavorite(userId: Long, productId: Long)
}

// Nested relationships
data class OrderWithItems(
    @Embedded
    val order: Order,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItem>
)

data class UserWithOrdersAndItems(
    @Embedded
    val user: User,
    
    @Relation(
        entity = Order::class,  // Specify intermediate entity
        parentColumn = "id",
        entityColumn = "userId"
    )
    val ordersWithItems: List<OrderWithItems>
)
```

### Database Migrations

```kotlin
/**
 * Database migrations allow you to update the schema
 * without losing existing data
 */

// Migration from version 1 to 2: Add phone column to users
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE users ADD COLUMN phone TEXT DEFAULT NULL"
        )
    }
}

// Migration from version 2 to 3: Create new table
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId INTEGER NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                isRead INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
            )
        """)
        database.execSQL(
            "CREATE INDEX index_notifications_userId ON notifications(userId)"
        )
    }
}

// Migration from version 3 to 4: Rename column
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // SQLite doesn't support RENAME COLUMN before 3.25
        // So we need to recreate the table
        database.execSQL("""
            CREATE TABLE users_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                full_name TEXT NOT NULL,
                email TEXT NOT NULL,
                phone TEXT,
                created_at INTEGER NOT NULL,
                is_active INTEGER NOT NULL DEFAULT 1
            )
        """)
        database.execSQL("""
            INSERT INTO users_new (id, full_name, email, phone, created_at, is_active)
            SELECT id, first_name || ' ' || last_name, email, phone, created_at, is_active
            FROM users
        """)
        database.execSQL("DROP TABLE users")
        database.execSQL("ALTER TABLE users_new RENAME TO users")
    }
}

// Auto-migration (Room 2.4+)
@Database(
    entities = [User::class, Product::class],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 4, to = 5)  // Room generates migration automatically
    ]
)
abstract class AppDatabase : RoomDatabase() {
    // ...
}

// Auto-migration with spec (for complex changes)
@RenameColumn(
    tableName = "users",
    fromColumnName = "name",
    toColumnName = "full_name"
)
class Migration4To5Spec : AutoMigrationSpec

@Database(
    version = 5,
    autoMigrations = [
        AutoMigration(from = 4, to = 5, spec = Migration4To5Spec::class)
    ]
)

// Applying migrations
val database = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "app_database"
)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
    .build()
```

---

## Advanced Room

### Full-text Search with FTS

```kotlin
/**
 * FTS (Full-Text Search) for efficient text searching
 * Creates a virtual table optimized for text queries
 */
@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val id: Long,
    val title: String,
    val content: String,
    val author: String,
    val publishedAt: Long
)

// FTS table that indexes the articles table
@Fts4(contentEntity = Article::class)
@Entity(tableName = "articles_fts")
data class ArticleFts(
    val title: String,
    val content: String,
    val author: String
)

@Dao
interface ArticleDao {
    
    // Regular search (slow for large datasets)
    @Query("""
        SELECT * FROM articles 
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%'
    """)
    suspend fun searchSlow(query: String): List<Article>
    
    // FTS search (fast!)
    @Query("""
        SELECT articles.* FROM articles
        JOIN articles_fts ON articles.id = articles_fts.rowid
        WHERE articles_fts MATCH :query
    """)
    suspend fun searchFts(query: String): List<Article>
    
    // FTS with ranking
    @Query("""
        SELECT articles.*, matchinfo(articles_fts) as rank
        FROM articles
        JOIN articles_fts ON articles.id = articles_fts.rowid
        WHERE articles_fts MATCH :query
        ORDER BY rank DESC
    """)
    suspend fun searchFtsWithRanking(query: String): List<Article>
    
    // Rebuild FTS index
    @Query("INSERT INTO articles_fts(articles_fts) VALUES('rebuild')")
    suspend fun rebuildFtsIndex()
}

@Database(
    entities = [Article::class, ArticleFts::class],
    version = 1
)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
```

### Views and Complex Queries

```kotlin
/**
 * Database Views - Precomputed queries stored in database
 */
@DatabaseView("""
    SELECT 
        u.id as userId,
        u.first_name || ' ' || u.last_name as fullName,
        u.email,
        COUNT(o.id) as totalOrders,
        COALESCE(SUM(o.totalAmount), 0) as totalSpent
    FROM users u
    LEFT JOIN orders o ON u.id = o.userId
    GROUP BY u.id
""")
data class UserOrderSummary(
    val userId: Long,
    val fullName: String,
    val email: String,
    val totalOrders: Int,
    val totalSpent: Double
)

@Dao
interface ReportDao {
    
    @Query("SELECT * FROM UserOrderSummary ORDER BY totalSpent DESC")
    fun getTopCustomers(): Flow<List<UserOrderSummary>>
    
    @Query("SELECT * FROM UserOrderSummary WHERE userId = :userId")
    suspend fun getUserSummary(userId: Long): UserOrderSummary?
}

@Database(
    entities = [User::class, Order::class],
    views = [UserOrderSummary::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase()

// Raw queries for complex operations
@Dao
interface AnalyticsDao {
    
    @RawQuery
    suspend fun getCustomQuery(query: SupportSQLiteQuery): List<Any>
    
    @RawQuery(observedEntities = [Order::class])
    fun getCustomQueryFlow(query: SupportSQLiteQuery): Flow<List<Order>>
}

// Usage
suspend fun getDynamicReport(dao: AnalyticsDao, categoryFilter: String?) {
    val queryString = buildString {
        append("SELECT * FROM orders WHERE 1=1")
        if (categoryFilter != null) {
            append(" AND category = ?")
        }
        append(" ORDER BY order_date DESC")
    }
    
    val args = if (categoryFilter != null) arrayOf(categoryFilter) else emptyArray()
    val query = SimpleSQLiteQuery(queryString, args)
    
    val results = dao.getCustomQuery(query)
}
```

### Coroutines and Flow Support

```kotlin
/**
 * Room has built-in support for coroutines and Flow
 */
@Dao
interface ProductDao {
    
    // ============ SUSPEND FUNCTIONS ============
    // Execute on Dispatchers.IO automatically
    
    @Insert
    suspend fun insert(product: Product)
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): Product?
    
    // ============ FLOW ============
    // Emits new data whenever the table changes
    
    @Query("SELECT * FROM products ORDER BY name")
    fun getAllFlow(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE category = :category")
    fun getByCategoryFlow(category: String): Flow<List<Product>>
    
    // Flow with multiple tables - emits when ANY referenced table changes
    @Transaction
    @Query("""
        SELECT p.*, COUNT(oi.id) as orderCount
        FROM products p
        LEFT JOIN order_items oi ON p.id = oi.productId
        GROUP BY p.id
    """)
    fun getProductsWithOrderCountFlow(): Flow<List<ProductWithStats>>
}

// Repository using Flow
class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: ApiService
) {
    // Expose database as Flow
    val products: Flow<List<Product>> = productDao.getAllFlow()
    
    // Combine with other data sources
    fun getProductsWithPricing(): Flow<List<ProductWithPricing>> {
        return productDao.getAllFlow()
            .map { products ->
                // Could combine with in-memory cache or transform
                products.map { product ->
                    ProductWithPricing(
                        product = product,
                        formattedPrice = formatPrice(product.price)
                    )
                }
            }
    }
    
    // Refresh from network and save to database
    suspend fun refreshProducts() {
        try {
            val products = apiService.getProducts()
            productDao.insertAll(products)
            // Flow will automatically emit new data!
        } catch (e: Exception) {
            // Handle error
        }
    }
}

// ViewModel collecting Flow
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    val products: StateFlow<List<Product>> = productRepository.products
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun refresh() {
        viewModelScope.launch {
            productRepository.refreshProducts()
        }
    }
}
```

### Paging Integration

```kotlin
/**
 * Room + Paging 3 for efficient large dataset display
 */
@Dao
interface ArticleDao {
    
    // Returns PagingSource for Paging 3
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getArticlesPagingSource(): PagingSource<Int, Article>
    
    @Query("""
        SELECT * FROM articles 
        WHERE category = :category 
        ORDER BY publishedAt DESC
    """)
    fun getArticlesByCategoryPagingSource(category: String): PagingSource<Int, Article>
    
    // Also return flow for header/meta data
    @Query("SELECT COUNT(*) FROM articles")
    fun getTotalCountFlow(): Flow<Int>
}

// Repository with RemoteMediator for network + database
@OptIn(ExperimentalPagingApi::class)
class ArticleRepository(
    private val articleDao: ArticleDao,
    private val apiService: ApiService,
    private val database: AppDatabase
) {
    fun getArticlesPaged(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = ArticleRemoteMediator(database, apiService),
            pagingSourceFactory = { articleDao.getArticlesPagingSource() }
        ).flow
    }
}

// RemoteMediator for network + database caching
@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val database: AppDatabase,
    private val apiService: ApiService
) : RemoteMediator<Int, Article>() {
    
    private val articleDao = database.articleDao()
    
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Article>
    ): MediatorResult {
        
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                // Calculate next page based on last item
                (lastItem.id / state.config.pageSize + 1).toInt()
            }
        }
        
        return try {
            val response = apiService.getArticles(page, state.config.pageSize)
            
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    articleDao.deleteAll()
                }
                articleDao.insertAll(response.articles)
            }
            
            MediatorResult.Success(
                endOfPaginationReached = response.articles.isEmpty()
            )
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}

// ViewModel using paged data
@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {
    
    val articlesPaged: Flow<PagingData<Article>> = repository.getArticlesPaged()
        .cachedIn(viewModelScope)
}

// Composable displaying paged data
@Composable
fun ArticleList(viewModel: ArticleViewModel = hiltViewModel()) {
    val articles = viewModel.articlesPaged.collectAsLazyPagingItems()
    
    LazyColumn {
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.id }
        ) { index ->
            articles[index]?.let { article ->
                ArticleCard(article)
            }
        }
        
        // Loading indicator
        when (articles.loadState.append) {
            is LoadState.Loading -> {
                item { CircularProgressIndicator() }
            }
            is LoadState.Error -> {
                item { ErrorItem { articles.retry() } }
            }
            else -> {}
        }
    }
}
```

### Multi-database Support

```kotlin
/**
 * Some apps need multiple databases
 * - User data vs cached content
 * - Different encryption levels
 * - Modular features
 */

// User database (sensitive data, encrypted)
@Database(
    entities = [User::class, UserSession::class],
    version = 1
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
}

// Content database (cached content, can be cleared)
@Database(
    entities = [Article::class, Category::class],
    version = 1
)
abstract class ContentDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun categoryDao(): CategoryDao
}

// Hilt module providing both databases
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    @UserDb
    fun provideUserDatabase(@ApplicationContext context: Context): UserDatabase {
        return Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            "user_database"
        )
            .addMigrations(/* migrations */)
            .build()
    }
    
    @Provides
    @Singleton
    @ContentDb
    fun provideContentDatabase(@ApplicationContext context: Context): ContentDatabase {
        return Room.databaseBuilder(
            context,
            ContentDatabase::class.java,
            "content_database"
        )
            .fallbackToDestructiveMigration()  // OK to lose cached content
            .build()
    }
}

// Qualifiers
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserDb

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ContentDb

// Usage
class CombinedRepository @Inject constructor(
    @UserDb private val userDatabase: UserDatabase,
    @ContentDb private val contentDatabase: ContentDatabase
) {
    suspend fun getUserArticles(userId: Long): List<ArticleWithAuthor> {
        val user = userDatabase.userDao().getById(userId)
        val articles = contentDatabase.articleDao().getAll()
        // Combine data from both databases
        return articles.map { article ->
            ArticleWithAuthor(article, user)
        }
    }
}

// Clear cache database
class CacheManager @Inject constructor(
    @ContentDb private val contentDatabase: ContentDatabase
) {
    suspend fun clearCache() {
        contentDatabase.clearAllTables()
    }
}
```

### Testing Room Database

```kotlin
/**
 * Room provides testing support with in-memory database
 */
@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    
    @Before
    fun setup() {
        // In-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()  // Only for tests!
            .build()
        
        userDao = database.userDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveUser() = runTest {
        val user = User(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com"
        )
        
        val insertedId = userDao.insert(user)
        val retrieved = userDao.getById(insertedId)
        
        assertNotNull(retrieved)
        assertEquals("John", retrieved?.firstName)
        assertEquals("john@example.com", retrieved?.email)
    }
    
    @Test
    fun flowEmitsOnUpdate() = runTest {
        val user = User(firstName = "John", lastName = "Doe", email = "john@example.com")
        userDao.insert(user)
        
        val emissions = mutableListOf<List<User>>()
        
        val job = launch {
            userDao.getAllFlow().take(3).collect {
                emissions.add(it)
            }
        }
        
        // Initial emission
        advanceUntilIdle()
        assertEquals(1, emissions.size)
        assertEquals(1, emissions[0].size)
        
        // Insert another user
        userDao.insert(User(firstName = "Jane", lastName = "Doe", email = "jane@example.com"))
        advanceUntilIdle()
        assertEquals(2, emissions.size)
        assertEquals(2, emissions[1].size)
        
        job.cancel()
    }
    
    @Test
    fun migration_1_to_2() = runTest {
        // Test migration
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java
        )
        
        // Create database at version 1
        helper.createDatabase("test_db", 1).apply {
            execSQL("INSERT INTO users (id, first_name, last_name, email) VALUES (1, 'John', 'Doe', 'john@test.com')")
            close()
        }
        
        // Run migration
        helper.runMigrationsAndValidate("test_db", 2, true, MIGRATION_1_2)
        
        // Verify data
        val db = helper.createDatabase("test_db", 2)
        val cursor = db.query("SELECT * FROM users WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        // Verify new column exists and has default value
    }
}
```

---

## Summary: Choosing the Right Approach

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    JETPACK LIBRARIES DECISION GUIDE                      │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  LIFECYCLE:                                                              │
│  • Use DefaultLifecycleObserver for custom lifecycle-aware components    │
│  • Use ProcessLifecycleOwner for app-level lifecycle (foreground/bg)     │
│  • Use repeatOnLifecycle for safe Flow collection in UI                  │
│                                                                          │
│  VIEWMODEL:                                                              │
│  • Use Hilt (@HiltViewModel) for dependency injection                    │
│  • Use SavedStateHandle for surviving process death                      │
│  • Use activityViewModels() for sharing between fragments                │
│  • Use navGraphViewModels() for sharing within navigation graph          │
│                                                                          │
│  STATE MANAGEMENT:                                                       │
│  • New projects: StateFlow + SharedFlow                                  │
│  • Legacy projects: LiveData is fine, migrate gradually                  │
│  • One-time events: Always use SharedFlow, never LiveData                │
│  • Compose: collectAsStateWithLifecycle()                                │
│                                                                          │
│  ROOM:                                                                   │
│  • Use Flow for reactive queries                                         │
│  • Use suspend functions for one-shot operations                         │
│  • Use @Transaction for complex operations                               │
│  • Use migrations, avoid fallbackToDestructiveMigration in production    │
│  • Use FTS for full-text search requirements                             │
│  • Use Paging 3 for large datasets                                       │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Gradle Dependencies

```kotlin
// build.gradle.kts (app module)
dependencies {
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")  // ProcessLifecycleOwner
    
    // Compose integration
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")  // Coroutines support
    implementation("androidx.room:room-paging:2.6.1")  // Paging integration
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
    
    // Testing
    testImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
}
```
