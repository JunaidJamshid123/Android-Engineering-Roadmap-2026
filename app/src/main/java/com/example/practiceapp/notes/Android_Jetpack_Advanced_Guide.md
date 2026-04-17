# Android Jetpack Advanced Libraries Guide

## 5.5 WorkManager

WorkManager is the recommended solution for persistent, deferrable, and guaranteed background work that must run even if the app exits or device restarts.

### Core Concepts

**When to use WorkManager:**
- Uploading logs or analytics
- Syncing data with server
- Periodic data backup
- Processing images/files

**Work Types:**
- **OneTimeWorkRequest**: Executes once
- **PeriodicWorkRequest**: Repeats at intervals (minimum 15 minutes)

---

### OneTimeWorkRequest and PeriodicWorkRequest

```kotlin
// Step 1: Create a Worker class
class UploadWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            // Get input data
            val imageUri = inputData.getString("IMAGE_URI") ?: return Result.failure()
            
            // Perform upload
            uploadImage(imageUri)
            
            // Return output data
            val outputData = workDataOf("UPLOAD_URL" to "https://example.com/uploaded.jpg")
            Result.success(outputData)
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private fun uploadImage(uri: String) {
        // Upload logic
    }
}

// CoroutineWorker for suspend functions
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
    
    private suspend fun syncData() {
        // Suspend function for sync
    }
}
```

```kotlin
// OneTimeWorkRequest
val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
    .setInputData(workDataOf("IMAGE_URI" to "content://image.jpg"))
    .addTag("upload")
    .build()

WorkManager.getInstance(context).enqueue(uploadRequest)

// PeriodicWorkRequest (minimum 15 minutes)
val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    repeatInterval = 1,
    repeatIntervalTimeUnit = TimeUnit.HOURS,
    flexTimeInterval = 15,
    flexTimeIntervalUnit = TimeUnit.MINUTES
).build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "sync_work",
    ExistingPeriodicWorkPolicy.KEEP,
    syncRequest
)
```

---

### Constraints

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)      // Network required
    .setRequiresBatteryNotLow(true)                     // Battery > 20%
    .setRequiresStorageNotLow(true)                     // Storage available
    .setRequiresCharging(true)                          // Device charging
    .setRequiresDeviceIdle(true)                        // Device idle (API 23+)
    .build()

val constrainedWork = OneTimeWorkRequestBuilder<UploadWorker>()
    .setConstraints(constraints)
    .setInitialDelay(10, TimeUnit.MINUTES)
    .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
        TimeUnit.MILLISECONDS
    )
    .build()
```

---

### Work Chaining and Unique Work

```kotlin
// Sequential chaining
val downloadWork = OneTimeWorkRequestBuilder<DownloadWorker>().build()
val filterWork = OneTimeWorkRequestBuilder<FilterWorker>().build()
val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>().build()

WorkManager.getInstance(context)
    .beginWith(downloadWork)
    .then(filterWork)
    .then(uploadWork)
    .enqueue()

// Parallel then sequential
val compress1 = OneTimeWorkRequestBuilder<CompressWorker>()
    .setInputData(workDataOf("FILE" to "file1.jpg"))
    .build()
val compress2 = OneTimeWorkRequestBuilder<CompressWorker>()
    .setInputData(workDataOf("FILE" to "file2.jpg"))
    .build()
val uploadAll = OneTimeWorkRequestBuilder<UploadAllWorker>().build()

WorkManager.getInstance(context)
    .beginWith(listOf(compress1, compress2))  // Parallel
    .then(uploadAll)                           // After both complete
    .enqueue()

// Unique work - prevent duplicates
WorkManager.getInstance(context).enqueueUniqueWork(
    "unique_sync",
    ExistingWorkPolicy.REPLACE,  // KEEP, APPEND, APPEND_OR_REPLACE
    syncRequest
)
```

---

### Observing Work Status

```kotlin
// In ViewModel
class WorkViewModel(application: Application) : AndroidViewModel(application) {
    
    private val workManager = WorkManager.getInstance(application)
    
    fun startUpload(imageUri: String): LiveData<WorkInfo> {
        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(workDataOf("IMAGE_URI" to imageUri))
            .build()
        
        workManager.enqueue(request)
        
        return workManager.getWorkInfoByIdLiveData(request.id)
    }
    
    // Observe by tag
    fun getUploadStatus(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosByTagLiveData("upload")
    }
}

// In Activity/Fragment
viewModel.startUpload("content://image.jpg").observe(this) { workInfo ->
    when (workInfo?.state) {
        WorkInfo.State.ENQUEUED -> showProgress("Queued")
        WorkInfo.State.RUNNING -> showProgress("Uploading...")
        WorkInfo.State.SUCCEEDED -> {
            val url = workInfo.outputData.getString("UPLOAD_URL")
            showSuccess("Uploaded: $url")
        }
        WorkInfo.State.FAILED -> showError("Upload failed")
        WorkInfo.State.CANCELLED -> showError("Cancelled")
        else -> {}
    }
}

// Cancel work
workManager.cancelWorkById(request.id)
workManager.cancelAllWorkByTag("upload")
workManager.cancelUniqueWork("unique_sync")
```

---

## 5.6 Navigation Component

Navigation Component simplifies implementing navigation with a visual editor, type-safe arguments, and support for deep links.

### Setup

```kotlin
// build.gradle.kts (app)
plugins {
    id("androidx.navigation.safeargs.kotlin")
}

dependencies {
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.0")
}

// build.gradle.kts (project)
plugins {
    id("androidx.navigation.safeargs.kotlin") version "2.7.0" apply false
}
```

---

### Navigation Graphs and Destinations

```xml
<!-- res/navigation/nav_graph.xml -->
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/homeFragment">

    <fragment
        android:id="@+id/homeFragment"
        android:name="com.example.app.HomeFragment"
        android:label="Home">
        
        <action
            android:id="@+id/action_home_to_detail"
            app:destination="@id/detailFragment"
            app:enterAnim="@anim/slide_in_right"
            app:exitAnim="@anim/slide_out_left"
            app:popEnterAnim="@anim/slide_in_left"
            app:popExitAnim="@anim/slide_out_right" />
    </fragment>

    <fragment
        android:id="@+id/detailFragment"
        android:name="com.example.app.DetailFragment"
        android:label="Detail">
        
        <!-- Arguments -->
        <argument
            android:name="itemId"
            app:argType="integer" />
        <argument
            android:name="itemName"
            app:argType="string"
            android:defaultValue="Unknown" />
        <argument
            android:name="item"
            app:argType="com.example.app.Item"
            app:nullable="true" />
    </fragment>
</navigation>
```

```xml
<!-- activity_main.xml -->
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/nav_host_fragment"
    android:name="androidx.navigation.fragment.NavHostFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:defaultNavHost="true"
    app:navGraph="@navigation/nav_graph" />
```

```kotlin
// MainActivity.kt
class MainActivity : AppCompatActivity() {
    
    private lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Setup ActionBar with NavController
        setupActionBarWithNavController(navController)
        
        // Setup BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
```

---

### Safe Args Plugin

```kotlin
// HomeFragment.kt - Navigate with type-safe args
class HomeFragment : Fragment() {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.itemButton.setOnClickListener {
            // Generated directions class
            val action = HomeFragmentDirections.actionHomeToDetail(
                itemId = 42,
                itemName = "My Item"
            )
            findNavController().navigate(action)
        }
    }
}

// DetailFragment.kt - Receive args
class DetailFragment : Fragment() {
    
    // Generated args class with navArgs delegate
    private val args: DetailFragmentArgs by navArgs()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val itemId = args.itemId
        val itemName = args.itemName
        
        binding.titleText.text = "$itemName (ID: $itemId)"
    }
}

// Custom Parcelable for complex objects
@Parcelize
data class Item(
    val id: Int,
    val name: String,
    val price: Double
) : Parcelable

// Pass Parcelable
val action = HomeFragmentDirections.actionHomeToDetail(
    itemId = item.id,
    itemName = item.name,
    item = item  // Parcelable object
)
```

---

### Deep Linking

```xml
<!-- In nav_graph.xml -->
<fragment
    android:id="@+id/detailFragment"
    android:name="com.example.app.DetailFragment">
    
    <!-- Explicit deep link -->
    <deepLink
        android:id="@+id/deepLink"
        app:uri="https://example.com/item/{itemId}"
        app:action="android.intent.action.VIEW"
        app:mimeType="text/plain" />
    
    <argument
        android:name="itemId"
        app:argType="integer" />
</fragment>
```

```xml
<!-- AndroidManifest.xml -->
<activity android:name=".MainActivity">
    <nav-graph android:value="@navigation/nav_graph" />
</activity>
```

```kotlin
// Programmatic deep link
val pendingIntent = NavDeepLinkBuilder(context)
    .setGraph(R.navigation.nav_graph)
    .setDestination(R.id.detailFragment)
    .setArguments(bundleOf("itemId" to 42))
    .createPendingIntent()

// Use in notification
val notification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setContentTitle("New Item")
    .setContentIntent(pendingIntent)
    .build()

// Navigate with URI
findNavController().navigate(
    Uri.parse("https://example.com/item/42")
)
```

---

### Conditional Navigation

```kotlin
// Login flow example
class SplashFragment : Fragment() {
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        lifecycleScope.launch {
            delay(1000)
            
            val isLoggedIn = checkLoginStatus()
            
            if (isLoggedIn) {
                findNavController().navigate(R.id.action_splash_to_home)
            } else {
                findNavController().navigate(R.id.action_splash_to_login)
            }
        }
    }
}

// Global action for login required
// In nav_graph.xml
<action
    android:id="@+id/action_global_login"
    app:destination="@id/loginFragment"
    app:popUpTo="@id/nav_graph"
    app:popUpToInclusive="true" />

// Navigate with pop behavior
findNavController().navigate(R.id.homeFragment) {
    popUpTo(R.id.loginFragment) { inclusive = true }
    launchSingleTop = true
}
```

---

### Nested Graphs

```xml
<!-- nav_graph.xml -->
<navigation
    android:id="@+id/nav_graph"
    app:startDestination="@id/homeFragment">
    
    <fragment android:id="@+id/homeFragment" ... />
    
    <!-- Nested graph for settings flow -->
    <navigation
        android:id="@+id/settings_graph"
        app:startDestination="@id/settingsMainFragment">
        
        <fragment
            android:id="@+id/settingsMainFragment"
            android:name="com.example.app.SettingsMainFragment">
            <action
                android:id="@+id/action_to_account"
                app:destination="@id/accountSettingsFragment" />
        </fragment>
        
        <fragment
            android:id="@+id/accountSettingsFragment"
            android:name="com.example.app.AccountSettingsFragment" />
    </navigation>
    
    <!-- Navigate to nested graph -->
    <action
        android:id="@+id/action_home_to_settings"
        app:destination="@id/settings_graph" />
</navigation>
```

---

## 5.7 DataStore

DataStore is a modern data storage solution that replaces SharedPreferences with Kotlin coroutines and Flow.

### Setup

```kotlin
dependencies {
    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Proto DataStore
    implementation("androidx.datastore:datastore:1.0.0")
    implementation("com.google.protobuf:protobuf-javalite:3.21.0")
}
```

---

### Preferences DataStore

```kotlin
// Create DataStore
val Context.dataStore by preferencesDataStore(name = "settings")

// Keys
object PreferencesKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val USERNAME = stringPreferencesKey("username")
    val NOTIFICATIONS = booleanPreferencesKey("notifications")
    val FONT_SIZE = intPreferencesKey("font_size")
}

// Repository
class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    
    // Read as Flow
    val darkModeFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DARK_MODE] ?: false
        }
    
    // Read all settings
    val settingsFlow: Flow<UserSettings> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            UserSettings(
                darkMode = prefs[PreferencesKeys.DARK_MODE] ?: false,
                username = prefs[PreferencesKeys.USERNAME] ?: "",
                notifications = prefs[PreferencesKeys.NOTIFICATIONS] ?: true,
                fontSize = prefs[PreferencesKeys.FONT_SIZE] ?: 14
            )
        }
    
    // Write
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }
    
    suspend fun updateSettings(settings: UserSettings) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DARK_MODE] = settings.darkMode
            prefs[PreferencesKeys.USERNAME] = settings.username
            prefs[PreferencesKeys.NOTIFICATIONS] = settings.notifications
            prefs[PreferencesKeys.FONT_SIZE] = settings.fontSize
        }
    }
    
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}

data class UserSettings(
    val darkMode: Boolean,
    val username: String,
    val notifications: Boolean,
    val fontSize: Int
)

// ViewModel usage
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    
    val settings = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, UserSettings(false, "", true, 14))
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            repository.setDarkMode(!settings.value.darkMode)
        }
    }
}
```

---

### Proto DataStore

```protobuf
// app/src/main/proto/user_prefs.proto
syntax = "proto3";

option java_package = "com.example.app";
option java_multiple_files = true;

message UserPreferences {
    bool dark_mode = 1;
    string username = 2;
    int32 font_size = 3;
    
    enum Theme {
        THEME_UNSPECIFIED = 0;
        LIGHT = 1;
        DARK = 2;
        SYSTEM = 3;
    }
    Theme theme = 4;
}
```

```kotlin
// Serializer
object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences = UserPreferences.getDefaultInstance()
    
    override suspend fun readFrom(input: InputStream): UserPreferences {
        try {
            return UserPreferences.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }
    
    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        t.writeTo(output)
    }
}

// Create Proto DataStore
val Context.userPrefsDataStore by dataStore(
    fileName = "user_prefs.pb",
    serializer = UserPreferencesSerializer
)

// Repository
class UserPrefsRepository(
    private val dataStore: DataStore<UserPreferences>
) {
    val userPrefsFlow: Flow<UserPreferences> = dataStore.data
        .catch { if (it is IOException) emit(UserPreferences.getDefaultInstance()) else throw it }
    
    suspend fun updateTheme(theme: UserPreferences.Theme) {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setTheme(theme)
                .build()
        }
    }
    
    suspend fun updateUsername(name: String) {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setUsername(name)
                .build()
        }
    }
}
```

---

### Migration from SharedPreferences

```kotlin
val Context.dataStore by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "old_shared_prefs"))
    }
)

// Custom migration
class CustomMigration : DataMigration<Preferences> {
    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        return currentData[PreferencesKeys.MIGRATED] != true
    }
    
    override suspend fun migrate(currentData: Preferences): Preferences {
        return currentData.toMutablePreferences().apply {
            // Perform migration logic
            this[PreferencesKeys.MIGRATED] = true
        }.toPreferences()
    }
    
    override suspend fun cleanUp() {
        // Clean up old data source
    }
}
```

---

## 5.8 Paging 3

Paging 3 library helps load and display pages of data from a larger dataset efficiently.

### Setup

```kotlin
dependencies {
    implementation("androidx.paging:paging-runtime-ktx:3.2.0")
    implementation("androidx.paging:paging-compose:3.2.0")  // For Compose
}
```

---

### PagingSource

```kotlin
// Data model
data class Article(
    val id: Int,
    val title: String,
    val content: String
)

// API response
data class ArticleResponse(
    val articles: List<Article>,
    val totalPages: Int,
    val currentPage: Int
)

// PagingSource for network-only data
class ArticlePagingSource(
    private val apiService: ApiService,
    private val query: String
) : PagingSource<Int, Article>() {
    
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        
        return try {
            val response = apiService.searchArticles(
                query = query,
                page = page,
                pageSize = params.loadSize
            )
            
            LoadResult.Page(
                data = response.articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.articles.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}
```

---

### RemoteMediator (Network + Database)

```kotlin
@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val query: String
) : RemoteMediator<Int, ArticleEntity>() {
    
    private val articleDao = database.articleDao()
    private val remoteKeyDao = database.remoteKeyDao()
    
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKey = remoteKeyDao.getRemoteKey(query)
                remoteKey?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }
        
        return try {
            val response = apiService.searchArticles(query, page, state.config.pageSize)
            val articles = response.articles.map { it.toEntity() }
            val endReached = articles.isEmpty()
            
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    articleDao.clearAll()
                    remoteKeyDao.clearAll()
                }
                
                val nextKey = if (endReached) null else page + 1
                remoteKeyDao.insert(RemoteKey(query, nextKey))
                articleDao.insertAll(articles)
            }
            
            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
```

---

### Repository and ViewModel

```kotlin
class ArticleRepository(
    private val apiService: ApiService,
    private val database: AppDatabase
) {
    // Network only
    fun searchArticles(query: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { ArticlePagingSource(apiService, query) }
        ).flow
    }
    
    // Network + Database
    @OptIn(ExperimentalPagingApi::class)
    fun getArticlesWithCache(query: String): Flow<PagingData<ArticleEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = ArticleRemoteMediator(database, apiService, query),
            pagingSourceFactory = { database.articleDao().getArticles(query) }
        ).flow
    }
}

class ArticleViewModel(private val repository: ArticleRepository) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    
    val articles: Flow<PagingData<Article>> = _searchQuery
        .debounce(300)
        .filter { it.isNotBlank() }
        .flatMapLatest { query ->
            repository.searchArticles(query)
        }
        .cachedIn(viewModelScope)
    
    fun search(query: String) {
        _searchQuery.value = query
    }
}
```

---

### PagingDataAdapter for RecyclerView

```kotlin
class ArticleAdapter(
    private val onClick: (Article) -> Unit
) : PagingDataAdapter<Article, ArticleAdapter.ViewHolder>(ArticleDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemArticleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
    
    inner class ViewHolder(
        private val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(article: Article) {
            binding.titleText.text = article.title
            binding.root.setOnClickListener { onClick(article) }
        }
    }
}

class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Article, newItem: Article) = oldItem == newItem
}

// Load state adapter for header/footer
class LoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<LoadStateAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val binding = ItemLoadStateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
    
    inner class ViewHolder(private val binding: ItemLoadStateBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.retryButton.setOnClickListener { retry() }
        }
        
        fun bind(loadState: LoadState) {
            binding.progressBar.isVisible = loadState is LoadState.Loading
            binding.retryButton.isVisible = loadState is LoadState.Error
            binding.errorText.isVisible = loadState is LoadState.Error
            
            if (loadState is LoadState.Error) {
                binding.errorText.text = loadState.error.localizedMessage
            }
        }
    }
}
```

---

### Load States and Error Handling

```kotlin
// In Fragment
private fun setupRecyclerView() {
    val adapter = ArticleAdapter { article -> navigateToDetail(article) }
    
    binding.recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
        header = LoadStateAdapter { adapter.retry() },
        footer = LoadStateAdapter { adapter.retry() }
    )
    
    // Observe load states
    lifecycleScope.launch {
        adapter.loadStateFlow.collectLatest { loadStates ->
            binding.progressBar.isVisible = loadStates.refresh is LoadState.Loading
            binding.emptyView.isVisible = loadStates.refresh is LoadState.NotLoading 
                && adapter.itemCount == 0
            
            val errorState = loadStates.refresh as? LoadState.Error
                ?: loadStates.append as? LoadState.Error
                ?: loadStates.prepend as? LoadState.Error
            
            errorState?.let {
                showError(it.error.message ?: "Unknown error")
            }
        }
    }
    
    // Submit data
    lifecycleScope.launch {
        viewModel.articles.collectLatest { pagingData ->
            adapter.submitData(pagingData)
        }
    }
}
```

---

### Compose Support with LazyPagingItems

```kotlin
@Composable
fun ArticleListScreen(viewModel: ArticleViewModel = viewModel()) {
    val articles = viewModel.articles.collectAsLazyPagingItems()
    
    LazyColumn {
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.id }
        ) { index ->
            articles[index]?.let { article ->
                ArticleItem(article = article)
            }
        }
        
        // Load state handling
        when (articles.loadState.refresh) {
            is LoadState.Loading -> {
                item { LoadingIndicator() }
            }
            is LoadState.Error -> {
                item {
                    ErrorItem(
                        message = (articles.loadState.refresh as LoadState.Error).error.message,
                        onRetry = { articles.retry() }
                    )
                }
            }
            else -> {}
        }
        
        // Append loading
        when (articles.loadState.append) {
            is LoadState.Loading -> {
                item { LoadingItem() }
            }
            is LoadState.Error -> {
                item {
                    ErrorItem(
                        message = (articles.loadState.append as LoadState.Error).error.message,
                        onRetry = { articles.retry() }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ArticleItem(article: Article) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = article.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = article.content, maxLines = 2)
        }
    }
}
```

---

## 5.9 CameraX

CameraX is a Jetpack library that makes camera app development easier with a consistent API across devices.

### Setup

```kotlin
dependencies {
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")
}
```

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.any" />
```

---

### Use Cases Overview

| Use Case | Purpose |
|----------|---------|
| **Preview** | Display camera feed on screen |
| **ImageCapture** | Capture high-quality photos |
| **ImageAnalysis** | Process frames in real-time (ML, QR codes) |
| **VideoCapture** | Record video |

---

### Basic Camera Setup

```kotlin
class CameraFragment : Fragment() {
    
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var imageCapture: ImageCapture
    private lateinit var imageAnalyzer: ImageAnalysis
    
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
        
        binding.captureButton.setOnClickListener { takePhoto() }
        binding.switchButton.setOnClickListener { switchCamera() }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    
    private fun bindUseCases() {
        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
            .also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
        
        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            .build()
        
        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, QRCodeAnalyzer { qrCode ->
                    // Handle QR code
                })
            }
        
        try {
            cameraProvider.unbindAll()
            
            val camera = cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer
            )
            
            // Camera controls
            setupCameraControls(camera)
            
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }
    
    private fun setupCameraControls(camera: Camera) {
        val cameraControl = camera.cameraControl
        val cameraInfo = camera.cameraInfo
        
        // Zoom
        binding.zoomSlider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                cameraControl.setLinearZoom(progress / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        
        // Observe zoom state
        cameraInfo.zoomState.observe(viewLifecycleOwner) { state ->
            binding.zoomText.text = "%.1fx".format(state.zoomRatio)
        }
        
        // Tap to focus
        binding.previewView.setOnTouchListener { _, event ->
            val factory = binding.previewView.meteringPointFactory
            val point = factory.createPoint(event.x, event.y)
            val action = FocusMeteringAction.Builder(point).build()
            cameraControl.startFocusAndMetering(action)
            true
        }
        
        // Toggle torch
        binding.flashButton.setOnClickListener {
            cameraControl.enableTorch(cameraInfo.torchState.value != TorchState.ON)
        }
    }
}
```

---

### ImageCapture - Taking Photos

```kotlin
private fun takePhoto() {
    val imageCapture = imageCapture ?: return
    
    // Create file
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
    )
    
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
        .setMetadata(ImageCapture.Metadata().apply {
            isReversedHorizontal = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
        })
        .build()
    
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(requireContext()),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                showToast("Photo saved: $savedUri")
            }
            
            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
            }
        }
    )
}

// Capture to memory (for processing)
imageCapture.takePicture(
    ContextCompat.getMainExecutor(requireContext()),
    object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val bitmap = image.toBitmap()
            // Process bitmap
            image.close()
        }
        
        override fun onError(exception: ImageCaptureException) {
            Log.e(TAG, "Capture failed", exception)
        }
    }
)
```

---

### ImageAnalysis - Frame Processing

```kotlin
class QRCodeAnalyzer(
    private val onQRCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )
    
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { qrCode ->
                    onQRCodeDetected(qrCode)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

// Face detection analyzer
class FaceAnalyzer(
    private val onFacesDetected: (List<Face>) -> Unit
) : ImageAnalysis.Analyzer {
    
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()
    )
    
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        
        val inputImage = InputImage.fromMediaImage(
            mediaImage, imageProxy.imageInfo.rotationDegrees
        )
        
        detector.process(inputImage)
            .addOnSuccessListener { faces -> onFacesDetected(faces) }
            .addOnCompleteListener { imageProxy.close() }
    }
}
```

---

### VideoCapture

```kotlin
private var recording: Recording? = null

private fun setupVideoCapture() {
    val recorder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.HD))
        .build()
    
    videoCapture = VideoCapture.withOutput(recorder)
    
    // Bind with other use cases
    cameraProvider.bindToLifecycle(
        viewLifecycleOwner,
        cameraSelector,
        preview,
        videoCapture
    )
}

private fun startRecording() {
    val videoCapture = videoCapture ?: return
    
    val videoFile = File(
        outputDirectory,
        "video_${System.currentTimeMillis()}.mp4"
    )
    
    val outputOptions = FileOutputOptions.Builder(videoFile).build()
    
    recording = videoCapture.output
        .prepareRecording(requireContext(), outputOptions)
        .withAudioEnabled()
        .start(ContextCompat.getMainExecutor(requireContext())) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    binding.recordButton.text = "Stop"
                }
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        Log.e(TAG, "Video recording error: ${event.error}")
                    } else {
                        showToast("Video saved: ${event.outputResults.outputUri}")
                    }
                    binding.recordButton.text = "Record"
                }
            }
        }
}

private fun stopRecording() {
    recording?.stop()
    recording = null
}
```

---

### Camera Selector and Configurations

```kotlin
// Select camera
val backCamera = CameraSelector.DEFAULT_BACK_CAMERA
val frontCamera = CameraSelector.DEFAULT_FRONT_CAMERA

// Custom selector
val wideAngleSelector = CameraSelector.Builder()
    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
    .addCameraFilter { cameras ->
        cameras.filter { cameraInfo ->
            val focalLength = cameraInfo.intrinsicZoomRatio
            focalLength < 1.0f  // Wide angle has zoom ratio < 1
        }
    }
    .build()

// Check camera availability
fun hasBackCamera(): Boolean {
    return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
}

fun hasFrontCamera(): Boolean {
    return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
}

// Switch camera
private fun switchCamera() {
    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
        CameraSelector.DEFAULT_FRONT_CAMERA
    } else {
        CameraSelector.DEFAULT_BACK_CAMERA
    }
    bindUseCases()
}

// Camera extensions (HDR, Night mode, etc.)
private fun setupExtensions() {
    val extensionsManager = ExtensionsManager.getInstanceAsync(
        requireContext(), cameraProvider
    ).get()
    
    if (extensionsManager.isExtensionAvailable(cameraSelector, ExtensionMode.NIGHT)) {
        val nightSelector = extensionsManager.getExtensionEnabledCameraSelector(
            cameraSelector, ExtensionMode.NIGHT
        )
        cameraProvider.bindToLifecycle(viewLifecycleOwner, nightSelector, preview, imageCapture)
    }
}
```

---

### Complete CameraX Activity Example

```kotlin
class CameraActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        
        binding.captureButton.setOnClickListener { takePhoto() }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera() else finish()
    }
    
    private fun startCamera() {
        ProcessCameraProvider.getInstance(this).addListener({
            val cameraProvider = ProcessCameraProvider.getInstance(this).get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        
        val photoFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "${System.currentTimeMillis()}.jpg"
        )
        
        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(photoFile).build(),
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@CameraActivity, "Saved: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()
                }
                override fun onError(e: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
```

---

## Quick Reference Summary

| Library | Key Classes | Primary Use |
|---------|-------------|-------------|
| **WorkManager** | `Worker`, `OneTimeWorkRequest`, `PeriodicWorkRequest`, `Constraints` | Background tasks |
| **Navigation** | `NavController`, `NavHostFragment`, `NavGraph`, Safe Args | App navigation |
| **DataStore** | `PreferencesDataStore`, `ProtoDataStore`, `DataMigration` | Key-value storage |
| **Paging 3** | `PagingSource`, `RemoteMediator`, `PagingDataAdapter`, `LazyPagingItems` | Large data sets |
| **CameraX** | `Preview`, `ImageCapture`, `ImageAnalysis`, `VideoCapture` | Camera features |

---
