# Android Architecture and Design Patterns - Complete Guide

## Table of Contents
1. [MVVM (Model-View-ViewModel)](#41-mvvm-model-view-viewmodel)
2. [MVI (Model-View-Intent)](#42-mvi-model-view-intent)
3. [Clean Architecture](#43-clean-architecture)
4. [Repository Pattern](#44-repository-pattern)
5. [Use Cases (Interactors)](#45-use-cases-interactors)

---

# 4. Architecture and Design Patterns

Good architecture is essential for building maintainable, testable, and scalable Android applications. This guide covers the most important architectural patterns used in modern Android development.

---

# 4.1 MVVM (Model-View-ViewModel)

## Overview

**Theory:**
MVVM (Model-View-ViewModel) is the recommended architecture pattern by Google for Android development. It provides a clear separation between UI logic and business logic, making code more maintainable and testable.

**The Three Components:**

| Component | Responsibility | Examples |
|-----------|---------------|----------|
| **Model** | Data and business logic | Repository, Database, API, Domain models |
| **View** | UI display and user input | Activity, Fragment, Composable |
| **ViewModel** | UI state and presentation logic | AndroidX ViewModel |

```
┌─────────────┐     observes      ┌─────────────┐     requests     ┌─────────────┐
│    View     │ ───────────────► │  ViewModel  │ ───────────────► │    Model    │
│  (Activity/ │                   │             │                   │ (Repository)│
│  Fragment)  │ ◄─────────────── │             │ ◄─────────────── │             │
└─────────────┘   UI state/events └─────────────┘      data        └─────────────┘
```

---

## Separation of Concerns

**Theory:**
The key principle of MVVM is that each component has a single, well-defined responsibility. This makes the code easier to understand, test, and modify.

**View Responsibilities:**
- Display UI based on state from ViewModel
- Capture user input and forward to ViewModel
- Observe state changes and update UI accordingly
- NO business logic, NO data operations

**ViewModel Responsibilities:**
- Hold and expose UI state
- Handle user interactions (events)
- Call business logic/repository methods
- Transform data for UI consumption
- Survive configuration changes

**Model Responsibilities:**
- Manage data operations (fetch, save, cache)
- Implement business rules
- Abstract data sources
- Handle API calls and database operations

```kotlin
// ❌ BAD: Business logic in View
class BadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // DON'T do this - business logic in View
        val users = database.getUsers()
        val filteredUsers = users.filter { it.isActive }
        val sortedUsers = filteredUsers.sortedBy { it.name }
        displayUsers(sortedUsers)
    }
}

// ✅ GOOD: Proper separation of concerns
class GoodActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // View only observes and displays
        viewModel.users.observe(this) { users ->
            displayUsers(users)
        }
        
        // View forwards user actions to ViewModel
        searchButton.setOnClickListener {
            viewModel.searchUsers(searchInput.text.toString())
        }
    }
}

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    init {
        loadUsers()
    }
    
    fun searchUsers(query: String) {
        viewModelScope.launch {
            val result = userRepository.searchUsers(query)
            _users.value = result
        }
    }
    
    private fun loadUsers() {
        viewModelScope.launch {
            val result = userRepository.getActiveUsersSorted()
            _users.value = result
        }
    }
}
```

---

## ViewModel Lifecycle Awareness

**Theory:**
`ViewModel` is designed to store and manage UI-related data in a lifecycle-conscious way. It survives configuration changes (like screen rotation), preventing data loss and unnecessary re-fetching.

**Key Characteristics:**
1. **Outlives Views:** ViewModel survives Activity/Fragment recreation
2. **Scoped Lifecycle:** Tied to Activity or Fragment lifecycle scope
3. **No View References:** Never hold Activity/Fragment/View references (causes memory leaks)
4. **Automatic Cleanup:** `onCleared()` called when owner is finished

```
Activity Created (first time)
        │
        ▼
   ┌─────────────┐
   │  ViewModel  │ ◄─── Created
   │   Created   │
   └─────────────┘
        │
Configuration Change (rotation)
        │
        ▼
   Activity Destroyed & Recreated
        │
        ▼
   ┌─────────────┐
   │  ViewModel  │ ◄─── SAME instance, data preserved!
   │  Still Alive│
   └─────────────┘
        │
User finishes Activity (back button)
        │
        ▼
   ┌─────────────┐
   │  ViewModel  │ ◄─── onCleared() called, destroyed
   │  Destroyed  │
   └─────────────┘
```

```kotlin
class UserProfileViewModel(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle  // Survives process death
) : ViewModel() {
    
    // This data survives configuration changes automatically
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    // This survives even process death (using SavedStateHandle)
    private val userId: String = savedStateHandle["userId"] ?: ""
    
    private var loadJob: Job? = null
    
    init {
        loadUserProfile()
    }
    
    fun loadUserProfile() {
        loadJob?.cancel()  // Cancel previous load if any
        loadJob = viewModelScope.launch {
            try {
                val userData = userRepository.getUser(userId)
                _user.value = userData
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Called when ViewModel is about to be destroyed permanently
    override fun onCleared() {
        super.onCleared()
        // Clean up resources
        Log.d("ViewModel", "ViewModel cleared - cleanup resources")
    }
}

// Fragment-scoped ViewModel
class ProfileFragment : Fragment() {
    // Scoped to this fragment's lifecycle
    private val viewModel: UserProfileViewModel by viewModels()
    
    // Shared with Activity (useful for communication between fragments)
    private val sharedViewModel: SharedViewModel by activityViewModels()
}

// Compose with ViewModel
@Composable
fun ProfileScreen(
    viewModel: UserProfileViewModel = viewModel()  // hiltViewModel() with Hilt
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    
    user?.let { UserProfileContent(it) }
}
```

---

## LiveData vs StateFlow for UI State

**Theory:**
Both LiveData and StateFlow are used to hold observable state in ViewModels. Each has its strengths and the choice depends on your project requirements.

### LiveData

**Characteristics:**
- Android-specific (part of Android Architecture Components)
- Lifecycle-aware by default (automatically stops observing when View is destroyed)
- Simple API, easy to use
- No initial value required (can be null initially)
- Single active observer pattern

```kotlin
class UserViewModel : ViewModel() {
    
    // Private mutable, public immutable (encapsulation)
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error handling
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadUsers() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getUsers()
                _users.value = result
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// Observing in Activity/Fragment
class UserActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Lifecycle-aware observation - automatically handles lifecycle
        viewModel.users.observe(this) { users ->
            adapter.submitList(users)
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.isVisible = isLoading
        }
        
        viewModel.error.observe(this) { error ->
            error?.let { showErrorSnackbar(it) }
        }
    }
}
```

### StateFlow

**Characteristics:**
- Part of Kotlin Coroutines (platform-independent)
- Always has a value (requires initial value)
- Hot flow - always active regardless of collectors
- Better for Jetpack Compose
- More powerful operators from Flow API
- Thread-safe by default

```kotlin
class UserViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    // Alternative: expose as Flow with operators
    private val _searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<User>> = _searchQuery
        .debounce(300.milliseconds)
        .flatMapLatest { query ->
            repository.searchUsers(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val users = repository.getUsers()
                _uiState.update { 
                    it.copy(users = users, isLoading = false, error = null) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = e.message) 
                }
            }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}

// UI State data class (recommended pattern)
data class UserUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Collecting in Activity/Fragment
class UserActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Must use lifecycle-aware collection!
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }
}

// Collecting in Compose (recommended approach)
@Composable
fun UserScreen(viewModel: UserViewModel = viewModel()) {
    // collectAsStateWithLifecycle handles lifecycle automatically
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(uiState.error!!)
        else -> UserList(uiState.users)
    }
}
```

### Comparison Table

| Feature | LiveData | StateFlow |
|---------|----------|-----------|
| Platform | Android only | Kotlin Multiplatform |
| Initial value | Not required | Required |
| Lifecycle awareness | Built-in | Need `repeatOnLifecycle` or `collectAsStateWithLifecycle` |
| Compose support | Works but StateFlow preferred | Native support |
| Operators | Transformations API | Full Flow operators |
| Null safety | Can be null | Can't be null (use nullable type) |
| Thread safety | Must use `postValue` for background | Always thread-safe |
| Testing | Android testing required | Pure Kotlin tests |

**Interview Tip:** Google now recommends StateFlow for new projects, especially those using Compose. Use LiveData for legacy projects or when you need simpler lifecycle handling.

---

## Data Binding with ViewModel

**Theory:**
Data Binding allows you to bind UI components directly to data sources declaratively, reducing boilerplate code. Combined with ViewModel, it creates a seamless connection between UI and state.

### Setup

```kotlin
// build.gradle.kts (Module level)
android {
    buildFeatures {
        dataBinding = true
    }
}
```

### Two-Way Data Binding with ViewModel

```xml
<!-- activity_login.xml -->
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    
    <data>
        <variable
            name="viewModel"
            type="com.example.app.LoginViewModel" />
    </data>
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- Two-way binding with @={} -->
        <EditText
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="@={viewModel.email}"
            android:hint="Email" />
        
        <EditText
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="@={viewModel.password}"
            android:inputType="textPassword"
            android:hint="Password" />
        
        <!-- One-way binding and click handler -->
        <Button
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Login"
            android:enabled="@{viewModel.isFormValid}"
            android:onClick="@{() -> viewModel.login()}" />
        
        <!-- Visibility binding -->
        <ProgressBar
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:visibility="@{viewModel.isLoading ? View.VISIBLE : View.GONE}" />
        
        <!-- Error message binding -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="@{viewModel.errorMessage}"
            android:textColor="@color/error"
            android:visibility="@{viewModel.errorMessage != null ? View.VISIBLE : View.GONE}" />
    </LinearLayout>
</layout>
```

```kotlin
class LoginViewModel : ViewModel() {
    
    // Two-way bindable properties using MutableLiveData
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    
    // Derived property for validation
    val isFormValid: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(email) { value = validateForm() }
        addSource(password) { value = validateForm() }
    }
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _loginSuccess = MutableLiveData<Event<Unit>>()
    val loginSuccess: LiveData<Event<Unit>> = _loginSuccess
    
    private fun validateForm(): Boolean {
        return email.value?.isNotEmpty() == true && 
               password.value?.length ?: 0 >= 6
    }
    
    fun login() {
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                authRepository.login(email.value!!, password.value!!)
                _loginSuccess.value = Event(Unit)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// Activity setup
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this  // Important for LiveData observation
        
        // One-time events still need manual observation
        viewModel.loginSuccess.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToHome()
            }
        }
    }
}
```

### Binding Adapters for Custom Behavior

```kotlin
// Custom Binding Adapters
object BindingAdapters {
    
    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadImage(imageView: ImageView, url: String?) {
        url?.let {
            Glide.with(imageView.context)
                .load(url)
                .into(imageView)
        }
    }
    
    @JvmStatic
    @BindingAdapter("visibleIf")
    fun setVisibleIf(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }
    
    @JvmStatic
    @BindingAdapter("errorText")
    fun setErrorText(textInputLayout: TextInputLayout, error: String?) {
        textInputLayout.error = error
    }
}

// Usage in XML:
// <ImageView app:imageUrl="@{viewModel.user.avatarUrl}" />
// <View app:visibleIf="@{viewModel.isLoading}" />
```

---

# 4.2 MVI (Model-View-Intent)

## Overview

**Theory:**
MVI (Model-View-Intent) is an architecture pattern that emphasizes **unidirectional data flow** and **immutable state**. It provides a more predictable way to manage UI state compared to MVVM.

**The Three Components:**

| Component | Description | Direction |
|-----------|-------------|-----------|
| **Model** | Single immutable state representing the entire UI | Output from ViewModel |
| **View** | Renders the current state and captures user intents | Input/Output |
| **Intent** | User actions/events that can change the state | Input to ViewModel |

```
┌─────────────────────────────────────────────────────────┐
│                        MVI Flow                         │
│                                                         │
│   ┌────────┐ Intent   ┌────────────┐ State  ┌────────┐  │
│   │  View  │ ───────► │  ViewModel │ ──────►│  View  │  │
│   │(Render)│          │  (Reduce)  │        │(Update)│ │
│   └────────┘          └────────────┘        └────────┘ │
│       ▲                     │                    │     │
│       │                     ▼                    │     │
│       │              ┌────────────┐              │     │
│       │              │   State    │              │     │
│       └──────────────│ (Immutable)│◄─────────────┘     │
│                      └────────────┘                    │
└─────────────────────────────────────────────────────────┘
```

**Key Principles:**
1. **Single Source of Truth:** One state object represents the entire screen
2. **Unidirectional Flow:** Data flows in one direction (Intent → State → UI)
3. **Immutable State:** State cannot be modified, only replaced
4. **Pure Reducers:** State transitions are deterministic functions

---

## Unidirectional Data Flow

**Theory:**
Unidirectional Data Flow (UDF) means data flows in a single direction through your app. This makes state changes predictable and easier to debug.

**The Flow:**
1. **User interacts** with UI → generates **Intent**
2. **Intent** is processed by ViewModel (reducer)
3. ViewModel produces new **State**
4. **View** renders new State
5. Repeat

```kotlin
// State - represents the complete UI state (immutable)
data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Intent - user actions that can change state
sealed interface CounterIntent {
    data object Increment : CounterIntent
    data object Decrement : CounterIntent
    data object Reset : CounterIntent
    data class SetValue(val value: Int) : CounterIntent
}

// ViewModel - processes intents and produces new state
class CounterViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(CounterState())
    val state: StateFlow<CounterState> = _state.asStateFlow()
    
    // Single entry point for all user actions
    fun processIntent(intent: CounterIntent) {
        when (intent) {
            is CounterIntent.Increment -> {
                _state.update { it.copy(count = it.count + 1) }
            }
            is CounterIntent.Decrement -> {
                _state.update { it.copy(count = it.count - 1) }
            }
            is CounterIntent.Reset -> {
                _state.update { it.copy(count = 0) }
            }
            is CounterIntent.SetValue -> {
                _state.update { it.copy(count = intent.value) }
            }
        }
    }
}

// View - renders state and sends intents
@Composable
fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Count: ${state.count}",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.processIntent(CounterIntent.Decrement) }) {
                Text("-")
            }
            Button(onClick = { viewModel.processIntent(CounterIntent.Reset) }) {
                Text("Reset")
            }
            Button(onClick = { viewModel.processIntent(CounterIntent.Increment) }) {
                Text("+")
            }
        }
    }
}
```

---

## State Management with Sealed Classes

**Theory:**
Sealed classes/interfaces are perfect for modeling UI states because they provide exhaustive when expressions and type safety. The compiler ensures you handle all possible states.

### Complete MVI Example with Sealed Classes

```kotlin
// ========== State Definition ==========

// Main UI State - single source of truth
data class ProductListState(
    val products: List<Product> = emptyList(),
    val screenState: ScreenState = ScreenState.Loading,
    val selectedProduct: Product? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NAME_ASC
)

// Screen state variations using sealed class
sealed class ScreenState {
    data object Loading : ScreenState()
    data object Success : ScreenState()
    data class Error(val message: String, val retry: Boolean = true) : ScreenState()
    data object Empty : ScreenState()
}

// Sort options
enum class SortOrder {
    NAME_ASC, NAME_DESC, PRICE_ASC, PRICE_DESC
}

// ========== Intent/Event Definition ==========

sealed interface ProductIntent {
    // Load actions
    data object LoadProducts : ProductIntent
    data object RefreshProducts : ProductIntent
    
    // Search actions
    data class SearchProducts(val query: String) : ProductIntent
    data object ClearSearch : ProductIntent
    
    // Sort actions
    data class ChangeSortOrder(val sortOrder: SortOrder) : ProductIntent
    
    // Selection actions
    data class SelectProduct(val product: Product) : ProductIntent
    data object ClearSelection : ProductIntent
    
    // Error handling
    data object DismissError : ProductIntent
    data object RetryLastAction : ProductIntent
}

// ========== Side Effects (One-time events) ==========

sealed interface ProductEffect {
    data class ShowToast(val message: String) : ProductEffect
    data class NavigateToDetail(val productId: String) : ProductEffect
    data object ScrollToTop : ProductEffect
}

// ========== ViewModel Implementation ==========

class ProductListViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProductListState())
    val state: StateFlow<ProductListState> = _state.asStateFlow()
    
    // Channel for one-time effects
    private val _effect = Channel<ProductEffect>()
    val effect: Flow<ProductEffect> = _effect.receiveAsFlow()
    
    init {
        processIntent(ProductIntent.LoadProducts)
    }
    
    fun processIntent(intent: ProductIntent) {
        when (intent) {
            is ProductIntent.LoadProducts -> loadProducts()
            is ProductIntent.RefreshProducts -> refreshProducts()
            is ProductIntent.SearchProducts -> searchProducts(intent.query)
            is ProductIntent.ClearSearch -> clearSearch()
            is ProductIntent.ChangeSortOrder -> changeSortOrder(intent.sortOrder)
            is ProductIntent.SelectProduct -> selectProduct(intent.product)
            is ProductIntent.ClearSelection -> clearSelection()
            is ProductIntent.DismissError -> dismissError()
            is ProductIntent.RetryLastAction -> retryLastAction()
        }
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(screenState = ScreenState.Loading) }
            
            productRepository.getProducts()
                .onSuccess { products ->
                    _state.update {
                        it.copy(
                            products = sortProducts(products, it.sortOrder),
                            screenState = if (products.isEmpty()) 
                                ScreenState.Empty else ScreenState.Success
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(screenState = ScreenState.Error(error.message ?: "Unknown error"))
                    }
                }
        }
    }
    
    private fun refreshProducts() {
        viewModelScope.launch {
            // Don't show loading for refresh, just update in background
            productRepository.getProducts()
                .onSuccess { products ->
                    _state.update {
                        it.copy(
                            products = sortProducts(products, it.sortOrder),
                            screenState = if (products.isEmpty()) 
                                ScreenState.Empty else ScreenState.Success
                        )
                    }
                    _effect.send(ProductEffect.ShowToast("Refreshed!"))
                }
                .onFailure { error ->
                    _effect.send(ProductEffect.ShowToast("Refresh failed: ${error.message}"))
                }
        }
    }
    
    private fun searchProducts(query: String) {
        _state.update { currentState ->
            val filteredProducts = currentState.products.filter {
                it.name.contains(query, ignoreCase = true)
            }
            currentState.copy(
                searchQuery = query,
                products = filteredProducts
            )
        }
    }
    
    private fun clearSearch() {
        _state.update { it.copy(searchQuery = "") }
        processIntent(ProductIntent.LoadProducts)
    }
    
    private fun changeSortOrder(sortOrder: SortOrder) {
        _state.update { currentState ->
            currentState.copy(
                sortOrder = sortOrder,
                products = sortProducts(currentState.products, sortOrder)
            )
        }
        viewModelScope.launch {
            _effect.send(ProductEffect.ScrollToTop)
        }
    }
    
    private fun selectProduct(product: Product) {
        _state.update { it.copy(selectedProduct = product) }
        viewModelScope.launch {
            _effect.send(ProductEffect.NavigateToDetail(product.id))
        }
    }
    
    private fun clearSelection() {
        _state.update { it.copy(selectedProduct = null) }
    }
    
    private fun dismissError() {
        _state.update { it.copy(screenState = ScreenState.Success) }
    }
    
    private fun retryLastAction() {
        processIntent(ProductIntent.LoadProducts)
    }
    
    private fun sortProducts(products: List<Product>, sortOrder: SortOrder): List<Product> {
        return when (sortOrder) {
            SortOrder.NAME_ASC -> products.sortedBy { it.name }
            SortOrder.NAME_DESC -> products.sortedByDescending { it.name }
            SortOrder.PRICE_ASC -> products.sortedBy { it.price }
            SortOrder.PRICE_DESC -> products.sortedByDescending { it.price }
        }
    }
}
```

### View Implementation with Exhaustive State Handling

```kotlin
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel = viewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Handle one-time effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProductEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is ProductEffect.NavigateToDetail -> {
                    onNavigateToDetail(effect.productId)
                }
                is ProductEffect.ScrollToTop -> {
                    // Scroll to top logic
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            SearchTopBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.processIntent(ProductIntent.SearchProducts(it)) },
                onClear = { viewModel.processIntent(ProductIntent.ClearSearch) }
            )
        }
    ) { padding ->
        // Exhaustive when - compiler ensures all states handled
        when (val screenState = state.screenState) {
            is ScreenState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is ScreenState.Success -> {
                ProductList(
                    products = state.products,
                    onProductClick = { viewModel.processIntent(ProductIntent.SelectProduct(it)) },
                    onRefresh = { viewModel.processIntent(ProductIntent.RefreshProducts) },
                    modifier = Modifier.padding(padding)
                )
            }
            
            is ScreenState.Empty -> {
                EmptyState(
                    message = "No products found",
                    onRetry = { viewModel.processIntent(ProductIntent.LoadProducts) }
                )
            }
            
            is ScreenState.Error -> {
                ErrorState(
                    message = screenState.message,
                    showRetry = screenState.retry,
                    onRetry = { viewModel.processIntent(ProductIntent.RetryLastAction) },
                    onDismiss = { viewModel.processIntent(ProductIntent.DismissError) }
                )
            }
        }
    }
}
```

---

## Intent Handling and State Reduction

**Theory:**
State reduction is the process of creating a new state based on the current state and an intent. The reducer function is pure - it always produces the same output for the same inputs.

### Reducer Pattern

```kotlin
// Pure reducer function approach
class ProductReducer {
    
    fun reduce(currentState: ProductListState, intent: ProductIntent): ProductListState {
        return when (intent) {
            is ProductIntent.LoadProducts -> {
                currentState.copy(screenState = ScreenState.Loading)
            }
            
            is ProductIntent.SearchProducts -> {
                val filteredProducts = currentState.products.filter {
                    it.name.contains(intent.query, ignoreCase = true)
                }
                currentState.copy(
                    searchQuery = intent.query,
                    products = filteredProducts
                )
            }
            
            is ProductIntent.ChangeSortOrder -> {
                val sortedProducts = sortProducts(currentState.products, intent.sortOrder)
                currentState.copy(
                    sortOrder = intent.sortOrder,
                    products = sortedProducts
                )
            }
            
            // ... handle other intents
            else -> currentState
        }
    }
}

// ViewModel using reducer
class ProductListViewModel(
    private val reducer: ProductReducer,
    private val productRepository: ProductRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProductListState())
    val state: StateFlow<ProductListState> = _state.asStateFlow()
    
    // Intent channel for processing
    private val intentChannel = Channel<ProductIntent>(Channel.UNLIMITED)
    
    init {
        // Process intents sequentially
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                val newState = reducer.reduce(_state.value, intent)
                _state.value = newState
                
                // Handle side effects after state update
                handleSideEffects(intent)
            }
        }
    }
    
    fun processIntent(intent: ProductIntent) {
        viewModelScope.launch {
            intentChannel.send(intent)
        }
    }
    
    private suspend fun handleSideEffects(intent: ProductIntent) {
        when (intent) {
            is ProductIntent.LoadProducts -> {
                loadProductsFromRepository()
            }
            // ... other side effects
            else -> { /* No side effect */ }
        }
    }
}
```

### Middleware Pattern for Complex Logic

```kotlin
// Middleware for logging, analytics, etc.
interface Middleware<S, I> {
    suspend fun process(state: S, intent: I, next: suspend (I) -> Unit)
}

class LoggingMiddleware : Middleware<ProductListState, ProductIntent> {
    override suspend fun process(
        state: ProductListState,
        intent: ProductIntent,
        next: suspend (ProductIntent) -> Unit
    ) {
        Log.d("MVI", "Processing intent: $intent")
        Log.d("MVI", "Current state: $state")
        next(intent)
    }
}

class AnalyticsMiddleware(
    private val analytics: Analytics
) : Middleware<ProductListState, ProductIntent> {
    override suspend fun process(
        state: ProductListState,
        intent: ProductIntent,
        next: suspend (ProductIntent) -> Unit
    ) {
        // Track certain intents
        when (intent) {
            is ProductIntent.SelectProduct -> {
                analytics.trackEvent("product_viewed", mapOf("id" to intent.product.id))
            }
            is ProductIntent.SearchProducts -> {
                analytics.trackEvent("search", mapOf("query" to intent.query))
            }
            else -> { /* Not tracked */ }
        }
        next(intent)
    }
}
```

---

# 4.3 Clean Architecture

## Overview

**Theory:**
Clean Architecture, proposed by Robert C. Martin (Uncle Bob), is a software design philosophy that separates code into layers with clear boundaries. The goal is to create systems that are:
- **Independent of frameworks**
- **Testable** without UI, database, or external services
- **Independent of UI** - UI can change without affecting business rules
- **Independent of database** - Business rules don't know about storage
- **Independent of external agencies** - Business rules don't know about the outside world

```
┌─────────────────────────────────────────────────────────────────┐
│                     CLEAN ARCHITECTURE                          │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    PRESENTATION LAYER                     │  │
│  │    ┌─────────────┐          ┌─────────────┐              │  │
│  │    │   Activity  │          │  ViewModel  │              │  │
│  │    │  Fragment   │◄────────►│   State     │              │  │
│  │    │  Compose    │          │   Mapper    │              │  │
│  │    └─────────────┘          └──────┬──────┘              │  │
│  └─────────────────────────────────────┼────────────────────┘  │
│                                        │ depends on            │
│  ┌─────────────────────────────────────▼────────────────────┐  │
│  │                      DOMAIN LAYER                         │  │
│  │    ┌─────────────┐          ┌─────────────┐              │  │
│  │    │  Use Cases  │          │   Domain    │              │  │
│  │    │(Interactors)│◄────────►│   Models    │              │  │
│  │    └──────┬──────┘          └─────────────┘              │  │
│  │           │                                              │  │
│  │    ┌──────▼───────────────────────────────┐              │  │
│  │    │      Repository Interfaces           │              │  │
│  │    └──────────────────────────────────────┘              │  │
│  └─────────────────────────────────────┬────────────────────┘  │
│                                        │ depends on            │
│  ┌─────────────────────────────────────▼────────────────────┐  │
│  │                       DATA LAYER                          │  │
│  │    ┌─────────────┐          ┌─────────────┐              │  │
│  │    │ Repository  │          │   Mappers   │              │  │
│  │    │   Impl      │◄────────►│   DTOs      │              │  │
│  │    └──────┬──────┘          └─────────────┘              │  │
│  │           │                                              │  │
│  │    ┌──────▼──────┐          ┌─────────────┐              │  │
│  │    │   Remote    │          │    Local    │              │  │
│  │    │ Data Source │          │ Data Source │              │  │
│  │    │   (API)     │          │    (DB)     │              │  │
│  │    └─────────────┘          └─────────────┘              │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

              DEPENDENCY DIRECTION: Always points INWARD
                   Outer layers depend on inner layers
                   Inner layers know nothing about outer
```

---

## Layers

### Presentation Layer (UI Layer)

**Responsibility:** Display data to the user and handle user input.

**Contains:**
- Activities, Fragments, Composables
- ViewModels
- UI State classes
- UI Mappers (domain → UI models)
- Adapters

```kotlin
// ========== Presentation Layer ==========

// UI State
data class UserProfileUiState(
    val userName: String = "",
    val userEmail: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// UI Model (simpler than domain model, formatted for display)
data class UserProfileUi(
    val displayName: String,
    val emailDisplay: String,
    val avatarUrl: String?,
    val memberSinceText: String  // Pre-formatted: "Member since Jan 2024"
)

// Mapper: Domain → UI
class UserProfileUiMapper {
    fun mapToUi(domainUser: User): UserProfileUi {
        return UserProfileUi(
            displayName = "${domainUser.firstName} ${domainUser.lastName}",
            emailDisplay = domainUser.email,
            avatarUrl = domainUser.avatarUrl,
            memberSinceText = formatMemberSince(domainUser.createdAt)
        )
    }
    
    private fun formatMemberSince(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
        return "Member since ${date.format(formatter)}"
    }
}

// ViewModel
class UserProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val mapper: UserProfileUiMapper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()
    
    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            getUserProfileUseCase(userId)
                .onSuccess { user ->
                    val uiModel = mapper.mapToUi(user)
                    _uiState.update {
                        it.copy(
                            userName = uiModel.displayName,
                            userEmail = uiModel.emailDisplay,
                            avatarUrl = uiModel.avatarUrl,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
        }
    }
}

// Composable UI
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Pure rendering based on state
    UserProfileContent(
        state = state,
        onRetry = { viewModel.loadProfile(userId) }
    )
}
```

### Domain Layer (Business Logic Layer)

**Responsibility:** Contains all business logic and rules. This is the heart of the application.

**Contains:**
- Use Cases (Interactors)
- Domain Models (pure business entities)
- Repository Interfaces (not implementations!)
- Business rules and validation

**Key Rule:** Domain layer has NO dependencies on outer layers. It's pure Kotlin with no Android framework dependencies.

```kotlin
// ========== Domain Layer ==========

// Domain Model - pure business entity
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val avatarUrl: String?,
    val createdAt: LocalDate,
    val isPremium: Boolean,
    val settings: UserSettings
) {
    // Business logic can live in the model
    val fullName: String get() = "$firstName $lastName"
    
    fun canAccessPremiumFeatures(): Boolean = isPremium
}

data class UserSettings(
    val notificationsEnabled: Boolean,
    val darkModeEnabled: Boolean,
    val language: String
)

// Repository Interface (contract - NOT implementation)
interface UserRepository {
    suspend fun getUser(userId: String): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun searchUsers(query: String): Result<List<User>>
}

// Use Case - single business operation
class GetUserProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return userRepository.getUser(userId)
    }
}

class UpdateUserProfileUseCase(
    private val userRepository: UserRepository,
    private val validator: UserValidator
) {
    suspend operator fun invoke(user: User): Result<User> {
        // Business validation
        val validationResult = validator.validate(user)
        if (!validationResult.isValid) {
            return Result.failure(ValidationException(validationResult.errors))
        }
        
        return userRepository.updateUser(user)
    }
}

// Domain-level validation
class UserValidator {
    fun validate(user: User): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (user.firstName.isBlank()) {
            errors.add("First name cannot be empty")
        }
        
        if (!user.email.contains("@")) {
            errors.add("Invalid email format")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

class ValidationException(val errors: List<String>) : Exception(errors.joinToString())
```

### Data Layer

**Responsibility:** Manage data operations and provide data to the domain layer.

**Contains:**
- Repository Implementations
- Data Sources (Remote API, Local Database)
- Data Transfer Objects (DTOs)
- Mappers (DTO ↔ Domain)
- Caching logic

```kotlin
// ========== Data Layer ==========

// Data Transfer Objects (DTOs) - match API/Database schema
@Serializable
data class UserDto(
    @SerialName("user_id")
    val userId: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val email: String,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("created_at")
    val createdAt: String,  // String from API
    @SerialName("is_premium")
    val isPremium: Boolean,
    val settings: UserSettingsDto?
)

@Serializable
data class UserSettingsDto(
    @SerialName("notifications_enabled")
    val notificationsEnabled: Boolean,
    @SerialName("dark_mode")
    val darkMode: Boolean,
    val language: String
)

// Database Entity (Room)
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val avatarUrl: String?,
    val createdAt: Long,  // Store as timestamp
    val isPremium: Boolean,
    val notificationsEnabled: Boolean,
    val darkModeEnabled: Boolean,
    val language: String
)

// Mapper: DTO → Domain, Domain → DTO
class UserMapper {
    
    fun mapToDomain(dto: UserDto): User {
        return User(
            id = dto.userId,
            firstName = dto.firstName,
            lastName = dto.lastName,
            email = dto.email,
            avatarUrl = dto.avatarUrl,
            createdAt = LocalDate.parse(dto.createdAt),
            isPremium = dto.isPremium,
            settings = dto.settings?.let { mapSettingsToDomain(it) } ?: defaultSettings()
        )
    }
    
    fun mapToDto(domain: User): UserDto {
        return UserDto(
            userId = domain.id,
            firstName = domain.firstName,
            lastName = domain.lastName,
            email = domain.email,
            avatarUrl = domain.avatarUrl,
            createdAt = domain.createdAt.toString(),
            isPremium = domain.isPremium,
            settings = mapSettingsToDto(domain.settings)
        )
    }
    
    fun mapEntityToDomain(entity: UserEntity): User {
        return User(
            id = entity.id,
            firstName = entity.firstName,
            lastName = entity.lastName,
            email = entity.email,
            avatarUrl = entity.avatarUrl,
            createdAt = LocalDate.ofEpochDay(entity.createdAt),
            isPremium = entity.isPremium,
            settings = UserSettings(
                notificationsEnabled = entity.notificationsEnabled,
                darkModeEnabled = entity.darkModeEnabled,
                language = entity.language
            )
        )
    }
    
    fun mapDomainToEntity(domain: User): UserEntity {
        return UserEntity(
            id = domain.id,
            firstName = domain.firstName,
            lastName = domain.lastName,
            email = domain.email,
            avatarUrl = domain.avatarUrl,
            createdAt = domain.createdAt.toEpochDay(),
            isPremium = domain.isPremium,
            notificationsEnabled = domain.settings.notificationsEnabled,
            darkModeEnabled = domain.settings.darkModeEnabled,
            language = domain.settings.language
        )
    }
    
    private fun mapSettingsToDomain(dto: UserSettingsDto): UserSettings {
        return UserSettings(
            notificationsEnabled = dto.notificationsEnabled,
            darkModeEnabled = dto.darkMode,
            language = dto.language
        )
    }
    
    private fun mapSettingsToDto(settings: UserSettings): UserSettingsDto {
        return UserSettingsDto(
            notificationsEnabled = settings.notificationsEnabled,
            darkMode = settings.darkModeEnabled,
            language = settings.language
        )
    }
    
    private fun defaultSettings() = UserSettings(
        notificationsEnabled = true,
        darkModeEnabled = false,
        language = "en"
    )
}

// Remote Data Source
interface UserRemoteDataSource {
    suspend fun getUser(userId: String): UserDto
    suspend fun updateUser(userDto: UserDto): UserDto
}

class UserRemoteDataSourceImpl(
    private val apiService: UserApiService
) : UserRemoteDataSource {
    
    override suspend fun getUser(userId: String): UserDto {
        return apiService.getUser(userId)
    }
    
    override suspend fun updateUser(userDto: UserDto): UserDto {
        return apiService.updateUser(userDto.userId, userDto)
    }
}

// Local Data Source
interface UserLocalDataSource {
    suspend fun getUser(userId: String): UserEntity?
    suspend fun saveUser(user: UserEntity)
    suspend fun deleteUser(userId: String)
}

class UserLocalDataSourceImpl(
    private val userDao: UserDao
) : UserLocalDataSource {
    
    override suspend fun getUser(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }
    
    override suspend fun saveUser(user: UserEntity) {
        userDao.insertOrUpdate(user)
    }
    
    override suspend fun deleteUser(userId: String) {
        userDao.deleteById(userId)
    }
}

// Repository Implementation
class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource,
    private val mapper: UserMapper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {
    
    override suspend fun getUser(userId: String): Result<User> = withContext(dispatcher) {
        try {
            // Try remote first
            val remoteUser = remoteDataSource.getUser(userId)
            val domainUser = mapper.mapToDomain(remoteUser)
            
            // Cache locally
            localDataSource.saveUser(mapper.mapDomainToEntity(domainUser))
            
            Result.success(domainUser)
        } catch (e: Exception) {
            // Fallback to local
            val localUser = localDataSource.getUser(userId)
            if (localUser != null) {
                Result.success(mapper.mapEntityToDomain(localUser))
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun updateUser(user: User): Result<User> = withContext(dispatcher) {
        try {
            val dto = mapper.mapToDto(user)
            val updatedDto = remoteDataSource.updateUser(dto)
            val updatedUser = mapper.mapToDomain(updatedDto)
            
            // Update local cache
            localDataSource.saveUser(mapper.mapDomainToEntity(updatedUser))
            
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteUser(userId: String): Result<Unit> = withContext(dispatcher) {
        // Implementation
        Result.success(Unit)
    }
    
    override suspend fun searchUsers(query: String): Result<List<User>> {
        // Implementation
        return Result.success(emptyList())
    }
}
```

---

## Principles

### Dependency Rule

**Theory:**
Dependencies must point inward. Outer layers can depend on inner layers, but inner layers must know nothing about outer layers.

```
Presentation depends on → Domain
Data depends on → Domain
Domain depends on → Nothing (it's the core)
```

```kotlin
// ❌ WRONG: Domain knows about Data layer
// domain/UserRepository.kt
interface UserRepository {
    suspend fun getUser(userId: String): UserDto  // DON'T use DTO in domain!
}

// ✅ CORRECT: Domain is pure, uses its own models
// domain/UserRepository.kt
interface UserRepository {
    suspend fun getUser(userId: String): Result<User>  // User is domain model
}

// data/UserRepositoryImpl.kt
class UserRepositoryImpl : UserRepository {
    // Data layer handles conversion between DTO and Domain
    override suspend fun getUser(userId: String): Result<User> {
        val dto = api.getUser(userId)
        return Result.success(mapper.mapToDomain(dto))
    }
}
```

### Separation of Concerns

Each layer has distinct responsibilities and knowledge:

```kotlin
// Presentation Layer knows about:
// - UI frameworks (Compose, Views)
// - Android lifecycle
// - Navigation
// - UI state management

// Domain Layer knows about:
// - Business rules
// - Domain models
// - Business validation
// - Use case orchestration

// Data Layer knows about:
// - APIs, databases
// - Caching strategies
// - Data transformation
// - Network handling
```

### Dependency Inversion

**Theory:**
High-level modules (domain) should not depend on low-level modules (data). Both should depend on abstractions (interfaces).

```kotlin
// Domain defines the contract (interface)
// domain/repository/UserRepository.kt
interface UserRepository {
    suspend fun getUser(userId: String): Result<User>
}

// Data provides the implementation
// data/repository/UserRepositoryImpl.kt
class UserRepositoryImpl(
    private val api: ApiService,
    private val db: UserDatabase
) : UserRepository {
    override suspend fun getUser(userId: String): Result<User> {
        // Implementation details hidden from domain
    }
}

// Dependency injection wires them together
// di/DataModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideUserRepository(
        api: ApiService,
        db: UserDatabase,
        mapper: UserMapper
    ): UserRepository {  // Return interface type
        return UserRepositoryImpl(api, db, mapper)  // Provide implementation
    }
}
```

### Single Responsibility Principle

Each class/function should have one reason to change.

```kotlin
// ❌ BAD: ViewModel doing too much
class BadUserViewModel : ViewModel() {
    fun loadUser(id: String) {
        // Making API call
        // Parsing JSON
        // Validating data
        // Caching
        // Updating UI state
        // All in one method!
    }
}

// ✅ GOOD: Single responsibility per class
class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String) = repository.getUser(id)
}

class UserValidator {
    fun validate(user: User): ValidationResult { /* validation only */ }
}

class UserMapper {
    fun mapToDomain(dto: UserDto): User { /* mapping only */ }
}

class UserViewModel(private val getUserUseCase: GetUserUseCase) : ViewModel() {
    // Only handles UI state
    fun loadUser(id: String) {
        viewModelScope.launch {
            _state.value = getUserUseCase(id).fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message) }
            )
        }
    }
}
```

---

# 4.4 Repository Pattern

## Overview

**Theory:**
The Repository Pattern acts as an abstraction layer between the domain/business logic and data sources. It provides a clean API for data access while hiding the details of where the data comes from.

**Benefits:**
1. **Single Source of Truth:** One place to get data
2. **Abstraction:** Business logic doesn't know about APIs or databases
3. **Testability:** Easy to mock for testing
4. **Flexibility:** Can change data sources without affecting business logic
5. **Caching:** Central place to implement caching strategies

```
┌─────────────────────────────────────────────────────────────┐
│                     Repository Pattern                       │
│                                                             │
│  ┌─────────────┐          ┌─────────────────────┐          │
│  │  Use Case   │          │     Repository      │          │
│  │             │─────────►│                     │          │
│  │             │          │  - Abstract data    │          │
│  └─────────────┘          │  - Coordinate       │          │
│                           │  - Cache            │          │
│                           │  - Single API       │          │
│                           └──────────┬──────────┘          │
│                                      │                     │
│                    ┌─────────────────┼─────────────────┐   │
│                    │                 │                 │   │
│                    ▼                 ▼                 ▼   │
│           ┌────────────┐    ┌────────────┐    ┌────────────┐
│           │   Remote   │    │   Local    │    │   Memory   │
│           │   (API)    │    │    (DB)    │    │   Cache    │
│           └────────────┘    └────────────┘    └────────────┘
└─────────────────────────────────────────────────────────────┘
```

---

## Abstraction Over Data Sources

```kotlin
// Repository hides all data source complexity
interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getProduct(id: String): Result<Product>
    suspend fun searchProducts(query: String): Result<List<Product>>
    fun observeProducts(): Flow<List<Product>>
    suspend fun refreshProducts(): Result<Unit>
}

class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
    private val localDataSource: ProductLocalDataSource,
    private val mapper: ProductMapper
) : ProductRepository {
    
    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            // Try remote
            val remoteDtos = remoteDataSource.getProducts()
            val products = remoteDtos.map { mapper.mapToDomain(it) }
            
            // Save to local
            localDataSource.saveProducts(products.map { mapper.mapToEntity(it) })
            
            Result.success(products)
        } catch (e: Exception) {
            // Fallback to local
            val localEntities = localDataSource.getProducts()
            if (localEntities.isNotEmpty()) {
                Result.success(localEntities.map { mapper.mapEntityToDomain(it) })
            } else {
                Result.failure(e)
            }
        }
    }
    
    override fun observeProducts(): Flow<List<Product>> {
        return localDataSource.observeProducts()
            .map { entities -> entities.map { mapper.mapEntityToDomain(it) } }
    }
    
    override suspend fun refreshProducts(): Result<Unit> {
        return try {
            val remoteDtos = remoteDataSource.getProducts()
            val entities = remoteDtos.map { mapper.mapToEntity(mapper.mapToDomain(it)) }
            localDataSource.clearAndSaveProducts(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Other methods...
}
```

---

## Single Source of Truth (SSOT)

**Theory:**
The Single Source of Truth principle states that there should be one authoritative source of data that the UI observes. Typically, this is the local database, with remote data synced into it.

```kotlin
class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
    private val localDataSource: ProductLocalDataSource,
    private val mapper: ProductMapper
) : ProductRepository {
    
    // Single Source of Truth: Local database is the source
    // UI always observes local, never directly from remote
    override fun getProductsStream(): Flow<List<Product>> {
        return localDataSource.observeProducts()
            .map { entities -> entities.map { mapper.mapEntityToDomain(it) } }
    }
    
    // Sync remote → local
    override suspend fun syncProducts(): Result<Unit> {
        return try {
            val remoteProducts = remoteDataSource.getProducts()
            val entities = remoteProducts.map { mapper.mapDtoToEntity(it) }
            localDataSource.clearAndSaveProducts(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get product - always from local (SSOT)
    override suspend fun getProduct(id: String): Result<Product> {
        return try {
            val entity = localDataSource.getProduct(id)
            if (entity != null) {
                Result.success(mapper.mapEntityToDomain(entity))
            } else {
                // Product not in local, try fetching
                val remote = remoteDataSource.getProduct(id)
                val product = mapper.mapToDomain(remote)
                localDataSource.saveProduct(mapper.mapToEntity(product))
                Result.success(product)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ViewModel observes the single source
class ProductListViewModel(
    private val repository: ProductRepository
) : ViewModel() {
    
    private val _isRefreshing = MutableStateFlow(false)
    
    // Products flow from SSOT (local database)
    val products: StateFlow<List<Product>> = repository.getProductsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        // Initial sync
        refreshProducts()
    }
    
    fun refreshProducts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.syncProducts()  // Updates local DB
            _isRefreshing.value = false
            // UI automatically updates via products flow
        }
    }
}
```

---

## Offline-First Architecture

**Theory:**
Offline-first means the app works fully offline, with data synced when connectivity is available. The local database is always the source of truth.

```kotlin
class OfflineFirstRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val syncManager: SyncManager,
    private val connectivityObserver: ConnectivityObserver
) : Repository {
    
    // Always return local data immediately
    override fun getItems(): Flow<List<Item>> {
        return localDataSource.observeItems()
            .onStart {
                // Trigger background sync if online
                syncIfOnline()
            }
    }
    
    private fun syncIfOnline() {
        if (connectivityObserver.isOnline()) {
            syncManager.scheduleSync()
        }
    }
    
    // Optimistic update: update local immediately, sync later
    override suspend fun createItem(item: Item): Result<Item> {
        // 1. Save locally immediately (user sees instant feedback)
        val localItem = item.copy(syncStatus = SyncStatus.PENDING)
        localDataSource.saveItem(localItem)
        
        // 2. Try to sync to remote
        return try {
            if (connectivityObserver.isOnline()) {
                val remoteItem = remoteDataSource.createItem(item)
                val syncedItem = remoteItem.copy(syncStatus = SyncStatus.SYNCED)
                localDataSource.saveItem(syncedItem)
                Result.success(syncedItem)
            } else {
                // Queue for later sync
                syncManager.queueForSync(localItem)
                Result.success(localItem)
            }
        } catch (e: Exception) {
            // Keep local item, mark for retry
            localDataSource.saveItem(localItem.copy(syncStatus = SyncStatus.FAILED))
            syncManager.queueForSync(localItem)
            Result.success(localItem)  // Still return success for optimistic update
        }
    }
    
    // Delete: mark as deleted locally, sync later
    override suspend fun deleteItem(itemId: String): Result<Unit> {
        localDataSource.markAsDeleted(itemId)
        syncManager.queueForDeletion(itemId)
        return Result.success(Unit)
    }
}

// Sync status for tracking
enum class SyncStatus {
    SYNCED,     // Synchronized with server
    PENDING,    // Waiting to be synced
    FAILED      // Sync failed, will retry
}

// Sync Manager using WorkManager
class SyncManager(
    private val workManager: WorkManager
) {
    fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWork = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniqueWork(
            "sync_work",
            ExistingWorkPolicy.KEEP,
            syncWork
        )
    }
    
    fun queueForSync(item: Item) {
        // Add to sync queue and schedule work
        scheduleSync()
    }
}
```

---

## Cache Strategies

### Time-Based Cache

```kotlin
class CachedRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val cacheExpirationMs: Long = 5 * 60 * 1000  // 5 minutes
) : Repository {
    
    override suspend fun getItems(): Result<List<Item>> {
        // Check if cache is valid
        val cacheMetadata = localDataSource.getCacheMetadata("items")
        val isCacheValid = cacheMetadata?.let {
            System.currentTimeMillis() - it.lastUpdated < cacheExpirationMs
        } ?: false
        
        return if (isCacheValid) {
            // Return cached data
            val items = localDataSource.getItems()
            Result.success(items)
        } else {
            // Fetch fresh data
            fetchAndCacheItems()
        }
    }
    
    private suspend fun fetchAndCacheItems(): Result<List<Item>> {
        return try {
            val items = remoteDataSource.getItems()
            localDataSource.saveItems(items)
            localDataSource.updateCacheMetadata("items", System.currentTimeMillis())
            Result.success(items)
        } catch (e: Exception) {
            // Fallback to stale cache
            val cachedItems = localDataSource.getItems()
            if (cachedItems.isNotEmpty()) {
                Result.success(cachedItems)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun forceRefresh(): Result<List<Item>> {
        localDataSource.invalidateCache("items")
        return fetchAndCacheItems()
    }
}
```

### Stale-While-Revalidate

```kotlin
class StaleWhileRevalidateRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : Repository {
    
    // Return stale data immediately, then update
    override fun getItems(): Flow<List<Item>> = flow {
        // 1. Emit cached data immediately (stale)
        val cachedItems = localDataSource.getItems()
        emit(cachedItems)
        
        // 2. Fetch fresh data in background (revalidate)
        try {
            val freshItems = remoteDataSource.getItems()
            localDataSource.saveItems(freshItems)
            
            // 3. Emit fresh data if different
            if (freshItems != cachedItems) {
                emit(freshItems)
            }
        } catch (e: Exception) {
            // Silently fail - stale data already emitted
            Log.e("Repository", "Failed to refresh", e)
        }
    }
}
```

### LRU Memory Cache

```kotlin
class MemoryCachedRepository(
    private val repository: Repository,
    private val maxCacheSize: Int = 100
) : Repository {
    
    // LRU cache for items
    private val cache = object : LinkedHashMap<String, Item>(
        maxCacheSize, 0.75f, true  // accessOrder = true for LRU
    ) {
        override fun removeEldestEntry(eldest: Map.Entry<String, Item>): Boolean {
            return size > maxCacheSize
        }
    }
    
    override suspend fun getItem(id: String): Result<Item> {
        // Check memory cache first
        cache[id]?.let { return Result.success(it) }
        
        // Fetch from underlying repository
        return repository.getItem(id).onSuccess { item ->
            cache[id] = item
        }
    }
    
    override suspend fun getItems(): Result<List<Item>> {
        return repository.getItems().onSuccess { items ->
            items.forEach { cache[it.id] = it }
        }
    }
    
    fun invalidate() {
        cache.clear()
    }
    
    fun invalidateItem(id: String) {
        cache.remove(id)
    }
}
```

---

# 4.5 Use Cases (Interactors)

## Overview

**Theory:**
Use Cases (also called Interactors) represent individual business operations. They encapsulate business logic and orchestrate the flow of data between the presentation and data layers.

**Benefits:**
1. **Single Responsibility:** Each use case does one thing
2. **Reusability:** Same use case can be used across multiple ViewModels
3. **Testability:** Pure business logic, easy to unit test
4. **Readability:** Code reads like business requirements

```
┌─────────────────────────────────────────────────────────────┐
│                        Use Cases                             │
│                                                             │
│  ┌─────────────┐                                            │
│  │  ViewModel  │                                            │
│  └──────┬──────┘                                            │
│         │                                                   │
│         ▼                                                   │
│  ┌─────────────────────────────────────────────┐            │
│  │              Use Cases                       │            │
│  │  ┌───────────────┐  ┌───────────────┐       │            │
│  │  │ GetUserUseCase│  │UpdateUserCase │ ...   │            │
│  │  └───────┬───────┘  └───────┬───────┘       │            │
│  └──────────┼──────────────────┼───────────────┘            │
│             │                  │                            │
│             ▼                  ▼                            │
│  ┌─────────────┐        ┌─────────────┐                     │
│  │    User     │        │  Validation │                     │
│  │ Repository  │        │   Service   │                     │
│  └─────────────┘        └─────────────┘                     │
└─────────────────────────────────────────────────────────────┘
```

---

## Single Responsibility Per Use Case

**Rule:** Each use case should do exactly ONE thing.

```kotlin
// ❌ BAD: Use case doing multiple things
class UserUseCase(private val repository: UserRepository) {
    suspend fun getUser(id: String): User = repository.getUser(id)
    suspend fun updateUser(user: User): User = repository.updateUser(user)
    suspend fun deleteUser(id: String) = repository.deleteUser(id)
    suspend fun searchUsers(query: String): List<User> = repository.search(query)
}

// ✅ GOOD: One use case per operation
class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(userId: String): Result<User> {
        return repository.getUser(userId)
    }
}

class UpdateUserUseCase(
    private val repository: UserRepository,
    private val validator: UserValidator
) {
    suspend operator fun invoke(user: User): Result<User> {
        val validationResult = validator.validate(user)
        if (!validationResult.isValid) {
            return Result.failure(ValidationException(validationResult.errors))
        }
        return repository.updateUser(user)
    }
}

class DeleteUserUseCase(
    private val repository: UserRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        // Business rule: can only delete your own account
        val currentUser = authRepository.getCurrentUser()
        if (currentUser?.id != userId) {
            return Result.failure(UnauthorizedException("Cannot delete other users"))
        }
        return repository.deleteUser(userId)
    }
}

class SearchUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(query: String): Result<List<User>> {
        if (query.length < 2) {
            return Result.success(emptyList())
        }
        return repository.searchUsers(query)
    }
}
```

---

## Business Logic Encapsulation

Use cases encapsulate business rules that don't belong in the UI or data layer.

```kotlin
class PlaceOrderUseCase(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val paymentService: PaymentService,
    private val notificationService: NotificationService
) {
    suspend operator fun invoke(orderRequest: OrderRequest): Result<Order> {
        // Business Rule 1: Validate user
        val user = userRepository.getUser(orderRequest.userId).getOrElse {
            return Result.failure(UserNotFoundException())
        }
        
        if (!user.isVerified) {
            return Result.failure(UnverifiedUserException())
        }
        
        // Business Rule 2: Check inventory
        val products = mutableListOf<Product>()
        for (item in orderRequest.items) {
            val product = productRepository.getProduct(item.productId).getOrElse {
                return Result.failure(ProductNotFoundException(item.productId))
            }
            
            if (product.stock < item.quantity) {
                return Result.failure(InsufficientStockException(product.name))
            }
            
            products.add(product)
        }
        
        // Business Rule 3: Calculate total with discounts
        val subtotal = calculateSubtotal(orderRequest.items, products)
        val discount = calculateDiscount(user, subtotal)
        val total = subtotal - discount
        
        // Business Rule 4: Process payment
        val paymentResult = paymentService.processPayment(
            userId = user.id,
            amount = total,
            paymentMethod = orderRequest.paymentMethod
        )
        
        if (!paymentResult.isSuccessful) {
            return Result.failure(PaymentFailedException(paymentResult.error))
        }
        
        // Business Rule 5: Create order
        val order = Order(
            id = generateOrderId(),
            userId = user.id,
            items = orderRequest.items,
            subtotal = subtotal,
            discount = discount,
            total = total,
            paymentId = paymentResult.transactionId,
            status = OrderStatus.CONFIRMED,
            createdAt = Instant.now()
        )
        
        val savedOrder = orderRepository.createOrder(order).getOrElse {
            // Rollback payment
            paymentService.refund(paymentResult.transactionId)
            return Result.failure(it)
        }
        
        // Business Rule 6: Update inventory
        for ((index, item) in orderRequest.items.withIndex()) {
            productRepository.decrementStock(item.productId, item.quantity)
        }
        
        // Business Rule 7: Send confirmation
        notificationService.sendOrderConfirmation(user, savedOrder)
        
        return Result.success(savedOrder)
    }
    
    private fun calculateSubtotal(
        items: List<OrderItem>,
        products: List<Product>
    ): BigDecimal {
        return items.zip(products).sumOf { (item, product) ->
            product.price * item.quantity.toBigDecimal()
        }
    }
    
    private fun calculateDiscount(user: User, subtotal: BigDecimal): BigDecimal {
        return when {
            user.isPremium -> subtotal * "0.15".toBigDecimal()  // 15% for premium
            subtotal > "100".toBigDecimal() -> subtotal * "0.05".toBigDecimal()  // 5% over $100
            else -> BigDecimal.ZERO
        }
    }
    
    private fun generateOrderId(): String = UUID.randomUUID().toString()
}
```

---

## Testability Improvement

Use cases are easy to test because they're pure business logic with injected dependencies.

```kotlin
// Use Case
class GetDiscountedPriceUseCase(
    private val userRepository: UserRepository,
    private val discountService: DiscountService
) {
    suspend operator fun invoke(
        userId: String,
        originalPrice: BigDecimal
    ): Result<PricingResult> {
        val user = userRepository.getUser(userId).getOrElse {
            return Result.failure(it)
        }
        
        val discountPercentage = discountService.getDiscountForUser(user)
        val discountAmount = originalPrice * discountPercentage
        val finalPrice = originalPrice - discountAmount
        
        return Result.success(
            PricingResult(
                originalPrice = originalPrice,
                discountPercentage = discountPercentage,
                discountAmount = discountAmount,
                finalPrice = finalPrice
            )
        )
    }
}

data class PricingResult(
    val originalPrice: BigDecimal,
    val discountPercentage: BigDecimal,
    val discountAmount: BigDecimal,
    val finalPrice: BigDecimal
)

// Unit Tests
class GetDiscountedPriceUseCaseTest {
    
    private lateinit var useCase: GetDiscountedPriceUseCase
    private val userRepository: UserRepository = mockk()
    private val discountService: DiscountService = mockk()
    
    @Before
    fun setup() {
        useCase = GetDiscountedPriceUseCase(userRepository, discountService)
    }
    
    @Test
    fun `premium user gets 15% discount`() = runTest {
        // Arrange
        val userId = "user123"
        val user = User(id = userId, isPremium = true, name = "John")
        val originalPrice = BigDecimal("100.00")
        
        coEvery { userRepository.getUser(userId) } returns Result.success(user)
        coEvery { discountService.getDiscountForUser(user) } returns BigDecimal("0.15")
        
        // Act
        val result = useCase(userId, originalPrice)
        
        // Assert
        assertTrue(result.isSuccess)
        val pricing = result.getOrNull()!!
        assertEquals(BigDecimal("15.00"), pricing.discountAmount)
        assertEquals(BigDecimal("85.00"), pricing.finalPrice)
    }
    
    @Test
    fun `regular user gets no discount`() = runTest {
        // Arrange
        val userId = "user456"
        val user = User(id = userId, isPremium = false, name = "Jane")
        val originalPrice = BigDecimal("50.00")
        
        coEvery { userRepository.getUser(userId) } returns Result.success(user)
        coEvery { discountService.getDiscountForUser(user) } returns BigDecimal.ZERO
        
        // Act
        val result = useCase(userId, originalPrice)
        
        // Assert
        assertTrue(result.isSuccess)
        val pricing = result.getOrNull()!!
        assertEquals(BigDecimal.ZERO, pricing.discountAmount)
        assertEquals(BigDecimal("50.00"), pricing.finalPrice)
    }
    
    @Test
    fun `returns failure when user not found`() = runTest {
        // Arrange
        val userId = "nonexistent"
        coEvery { userRepository.getUser(userId) } returns 
            Result.failure(UserNotFoundException())
        
        // Act
        val result = useCase(userId, BigDecimal("100.00"))
        
        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UserNotFoundException)
    }
}
```

---

## Use Case Patterns

### Parameterized Use Case

```kotlin
// Use case with parameters wrapped in a data class
class GetFilteredProductsUseCase(
    private val repository: ProductRepository
) {
    data class Params(
        val category: String? = null,
        val minPrice: Double? = null,
        val maxPrice: Double? = null,
        val sortBy: SortOption = SortOption.NAME,
        val limit: Int = 50
    )
    
    suspend operator fun invoke(params: Params): Result<List<Product>> {
        return repository.getProducts().map { products ->
            products
                .filter { product ->
                    (params.category == null || product.category == params.category) &&
                    (params.minPrice == null || product.price >= params.minPrice) &&
                    (params.maxPrice == null || product.price <= params.maxPrice)
                }
                .let { filtered ->
                    when (params.sortBy) {
                        SortOption.NAME -> filtered.sortedBy { it.name }
                        SortOption.PRICE_ASC -> filtered.sortedBy { it.price }
                        SortOption.PRICE_DESC -> filtered.sortedByDescending { it.price }
                    }
                }
                .take(params.limit)
        }
    }
}

// Usage
val products = getFilteredProductsUseCase(
    GetFilteredProductsUseCase.Params(
        category = "Electronics",
        maxPrice = 500.0,
        sortBy = SortOption.PRICE_ASC
    )
)
```

### Flow-Returning Use Case

```kotlin
// Use case that returns a Flow for reactive data
class ObserveUserUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<User> {
        return userRepository.observeUser(userId)
            .distinctUntilChanged()
            .catch { e ->
                // Log error and emit nothing
                Log.e("ObserveUser", "Error observing user", e)
            }
    }
}

// Usage in ViewModel
class ProfileViewModel(
    private val observeUserUseCase: ObserveUserUseCase
) : ViewModel() {
    
    val user: StateFlow<User?> = observeUserUseCase(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
```

### Composing Use Cases

```kotlin
// Higher-level use case that composes other use cases
class CheckoutUseCase(
    private val validateCartUseCase: ValidateCartUseCase,
    private val calculateTotalUseCase: CalculateTotalUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val sendConfirmationUseCase: SendConfirmationUseCase
) {
    suspend operator fun invoke(cart: Cart, paymentMethod: PaymentMethod): Result<Order> {
        // Step 1: Validate
        validateCartUseCase(cart).getOrElse { 
            return Result.failure(it) 
        }
        
        // Step 2: Calculate
        val total = calculateTotalUseCase(cart).getOrElse { 
            return Result.failure(it) 
        }
        
        // Step 3: Payment
        val paymentResult = processPaymentUseCase(total, paymentMethod).getOrElse { 
            return Result.failure(it) 
        }
        
        // Step 4: Create order
        val order = createOrderUseCase(cart, paymentResult).getOrElse { 
            return Result.failure(it) 
        }
        
        // Step 5: Notification (non-blocking)
        sendConfirmationUseCase(order) // Fire and forget
        
        return Result.success(order)
    }
}
```

---

## Complete Example: Feature Module Structure

```
feature-products/
├── domain/
│   ├── model/
│   │   ├── Product.kt
│   │   └── Category.kt
│   ├── repository/
│   │   └── ProductRepository.kt
│   └── usecase/
│       ├── GetProductsUseCase.kt
│       ├── GetProductByIdUseCase.kt
│       ├── SearchProductsUseCase.kt
│       └── FilterProductsUseCase.kt
├── data/
│   ├── dto/
│   │   └── ProductDto.kt
│   ├── entity/
│   │   └── ProductEntity.kt
│   ├── mapper/
│   │   └── ProductMapper.kt
│   ├── datasource/
│   │   ├── ProductRemoteDataSource.kt
│   │   └── ProductLocalDataSource.kt
│   └── repository/
│       └── ProductRepositoryImpl.kt
└── presentation/
    ├── model/
    │   └── ProductUiModel.kt
    ├── mapper/
    │   └── ProductUiMapper.kt
    ├── viewmodel/
    │   └── ProductListViewModel.kt
    └── ui/
        ├── ProductListScreen.kt
        └── ProductDetailScreen.kt
```

---

## Summary: Choosing the Right Pattern

| Scenario | Recommended Pattern |
|----------|---------------------|
| Simple CRUD app | MVVM with Repository |
| Complex UI state | MVI with sealed classes |
| Large enterprise app | Clean Architecture + MVVM/MVI |
| Need offline support | Repository with offline-first |
| Complex business logic | Use Cases (Interactors) |
| Multiple data sources | Repository Pattern |
| Sharing logic across features | Use Cases |

**Interview Tips:**
1. **MVVM** is Google's recommended pattern - know it inside out
2. **MVI** is great for complex state management - understand unidirectional flow
3. **Clean Architecture** separates concerns - remember dependency rule
4. **Repository** abstracts data - know caching strategies
5. **Use Cases** encapsulate business logic - one responsibility each
6. Always explain **why** you'd choose a pattern, not just **what** it is
