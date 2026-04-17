# Android Dependency Injection Complete Guide

## Table of Contents
1. [What is Dependency Injection?](#1-what-is-dependency-injection)
2. [Core Concepts & Theory](#2-core-concepts--theory)
3. [DI Benefits and SOLID Principles](#3-di-benefits-and-solid-principles)
4. [Types of Dependency Injection](#4-types-of-dependency-injection)
5. [Manual Dependency Injection](#5-manual-dependency-injection)
6. [Dagger 2](#6-dagger-2)
7. [Hilt](#7-hilt)
8. [Koin (Bonus)](#8-koin-bonus)
9. [Advanced Topics](#9-advanced-topics)
10. [Testing with DI](#10-testing-with-di)
11. [Comparison and Best Practices](#11-comparison-and-best-practices)

---

## 1. What is Dependency Injection?

### Definition

**Dependency Injection (DI)** is a design pattern where an object receives (is injected with) the objects it depends on, rather than creating them internally. This is a specific form of **Inversion of Control (IoC)**.

```
┌─────────────────────────────────────────────────────────────────────┐
│                      DEPENDENCY INJECTION CONCEPT                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   "Don't call us, we'll call you" (Hollywood Principle)            │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │                                                             │  │
│   │   Traditional Way:     Class A creates Class B              │  │
│   │                        Class A ──creates──► Class B         │  │
│   │                                                             │  │
│   │   DI Way:              Container gives B to A               │  │
│   │                        Container ──injects B──► Class A     │  │
│   │                                                             │  │
│   └─────────────────────────────────────────────────────────────┘  │
│                                                                     │
│   KEY INSIGHT: The control of creating dependencies is INVERTED    │
│   from the class itself to an external entity (the container)      │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### The Problem: Tight Coupling

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         WITHOUT DI (Bad Design)                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌─────────────────┐         ┌─────────────────┐                      │
│   │   UserViewModel │────────▶│  UserRepository │                      │
│   │                 │ creates │                 │                      │
│   │  val repo =     │         │  val api =      │                      │
│   │  UserRepository()         │  ApiService()   │                      │
│   └─────────────────┘         └────────┬────────┘                      │
│                                        │ creates                        │
│                                        ▼                                │
│                               ┌─────────────────┐                      │
│                               │   ApiService    │                      │
│                               │                 │                      │
│                               │  val client =   │                      │
│                               │  OkHttpClient() │                      │
│                               └─────────────────┘                      │
│                                                                         │
│   ┌───────────────────────────────────────────────────────────────┐    │
│   │                    PROBLEMS WITH THIS APPROACH                │    │
│   ├───────────────────────────────────────────────────────────────┤    │
│   │ ✗ Classes create their own dependencies (tight coupling)     │    │
│   │ ✗ Hard to test - can't substitute mocks/fakes               │    │
│   │ ✗ Hard to change implementations without modifying code     │    │
│   │ ✗ No centralized object lifecycle management                │    │
│   │ ✗ Difficult to share instances (e.g., singleton OkHttp)     │    │
│   │ ✗ Changes ripple through the codebase                       │    │
│   └───────────────────────────────────────────────────────────────┘    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Code Example - Without DI (Anti-pattern):**
```kotlin
// ❌ BAD: Classes create their own dependencies
class ApiService {
    // Hardcoded - every ApiService creates its own client
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com/")
        .client(client)
        .build()
}

class UserRepository {
    // Hardcoded dependency - can't change or mock
    private val apiService = ApiService()  // Creates new instance!
    private val database = AppDatabase.getInstance()
    
    fun getUsers() = apiService.getUsers()
}

class UserViewModel {
    // Hardcoded - can't inject fake repository for testing
    private val repository = UserRepository()  // Creates new instance!
    
    fun loadUsers() = repository.getUsers()
}

// PROBLEMS:
// 1. Each ViewModel creates its own Repository chain
// 2. Multiple OkHttpClient instances (memory waste)
// 3. Can't test UserViewModel with a fake Repository
// 4. Can't switch to different API endpoint for testing
```

### The Solution: Dependency Injection

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          WITH DI (Good Design)                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│                        ┌─────────────────────┐                         │
│                        │    DI Container     │                         │
│                        │     (Injector)      │                         │
│                        │                     │                         │
│                        │  ┌───────────────┐  │                         │
│                        │  │ Object Graph  │  │                         │
│                        │  │ & Lifecycle   │  │                         │
│                        │  └───────────────┘  │                         │
│                        └──────────┬──────────┘                         │
│                                   │                                     │
│                ┌──────────────────┼──────────────────┐                 │
│                │                  │                  │                 │
│                ▼                  ▼                  ▼                 │
│        ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
│        │ OkHttpClient│    │   Retrofit  │    │  Database   │          │
│        │  (Singleton)│    │ (Singleton) │    │ (Singleton) │          │
│        └──────┬──────┘    └──────┬──────┘    └──────┬──────┘          │
│               │                  │                  │                  │
│               └─────────┬────────┘                  │                  │
│                         ▼                           │                  │
│                 ┌─────────────┐                     │                  │
│                 │ ApiService  │                     │                  │
│                 │ (Singleton) │                     │                  │
│                 └──────┬──────┘                     │                  │
│                        │                            │                  │
│                        └──────────┬─────────────────┘                  │
│                                   ▼                                    │
│                          ┌─────────────┐                               │
│                          │ Repository  │                               │
│                          │ (Singleton) │                               │
│                          └──────┬──────┘                               │
│                                 │                                      │
│                                 ▼                                      │
│                         ┌─────────────┐                                │
│                         │  ViewModel  │                                │
│                         │ (Per-Screen)│                                │
│                         └─────────────┘                                │
│                                                                         │
│   ┌───────────────────────────────────────────────────────────────┐    │
│   │                   BENEFITS OF DI APPROACH                     │    │
│   ├───────────────────────────────────────────────────────────────┤    │
│   │ ✓ Dependencies provided from outside (loose coupling)        │    │
│   │ ✓ Easy to test with mocks/fakes                             │    │
│   │ ✓ Swap implementations via configuration                    │    │
│   │ ✓ Centralized object creation and lifecycle                 │    │
│   │ ✓ Efficient sharing of singleton instances                  │    │
│   │ ✓ Clear dependency graph - easy to understand               │    │
│   └───────────────────────────────────────────────────────────────┘    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Code Example - With DI (Correct Pattern):**
```kotlin
// ✅ GOOD: Dependencies are injected via constructor

// Interface definitions (abstractions)
interface ApiService {
    suspend fun getUsers(): List<User>
}

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun refreshUsers()
}

// Concrete implementations receive their dependencies
class ApiServiceImpl(
    private val retrofit: Retrofit  // Injected - shared instance
) : ApiService {
    
    private val api = retrofit.create(UserApi::class.java)
    
    override suspend fun getUsers(): List<User> {
        return api.fetchUsers()
    }
}

class UserRepositoryImpl(
    private val apiService: ApiService,  // Injected
    private val userDao: UserDao,         // Injected
    private val dispatcher: CoroutineDispatcher  // Injected
) : UserRepository {
    
    override suspend fun getUsers(): List<User> = withContext(dispatcher) {
        userDao.getAllUsers()
    }
    
    override suspend fun refreshUsers() = withContext(dispatcher) {
        val remoteUsers = apiService.getUsers()
        userDao.insertAll(remoteUsers)
    }
}

class UserViewModel(
    private val repository: UserRepository  // Injected
) : ViewModel() {
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _users.value = repository.getUsers()
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// TESTING IS NOW EASY
// ═══════════════════════════════════════════════════════════════

class UserViewModelTest {
    @Test
    fun `loadUsers should update state with repository data`() = runTest {
        // Create fake repository
        val fakeRepository = object : UserRepository {
            override suspend fun getUsers() = listOf(
                User(1, "John", "john@email.com"),
                User(2, "Jane", "jane@email.com")
            )
            override suspend fun refreshUsers() {}
        }
        
        // Inject fake into ViewModel
        val viewModel = UserViewModel(fakeRepository)
        
        // Test
        viewModel.loadUsers()
        
        assertEquals(2, viewModel.users.value.size)
        assertEquals("John", viewModel.users.value[0].name)
    }
}
```

---

## 2. Core Concepts & Theory

### Inversion of Control (IoC)

Dependency Injection is a specific implementation of the broader **Inversion of Control** principle.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     INVERSION OF CONTROL (IoC)                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   IoC is a principle where the control flow of a program is inverted:  │
│   instead of the application code controlling the flow, the framework  │
│   or container takes control.                                          │
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │              TRADITIONAL CONTROL FLOW                           │  │
│   │                                                                 │  │
│   │   Your Code ──controls──▶ Library/Framework                    │  │
│   │                                                                 │  │
│   │   // Your code decides when to call the library                │  │
│   │   val result = library.process(data)                           │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │              INVERTED CONTROL FLOW (IoC)                        │  │
│   │                                                                 │  │
│   │   Framework ──controls──▶ Your Code                            │  │
│   │                                                                 │  │
│   │   // Framework decides when to call your code                  │  │
│   │   class MyActivity : AppCompatActivity() {                     │  │
│   │       override fun onCreate() { }  // Called BY the framework  │  │
│   │   }                                                            │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │              IoC IMPLEMENTATIONS                                │  │
│   ├─────────────────────────────────────────────────────────────────┤  │
│   │                                                                 │  │
│   │   1. Dependency Injection (DI)                                 │  │
│   │      └── Container injects dependencies into objects          │  │
│   │                                                                 │  │
│   │   2. Service Locator Pattern                                   │  │
│   │      └── Objects request dependencies from a registry          │  │
│   │                                                                 │  │
│   │   3. Template Method Pattern                                   │  │
│   │      └── Superclass controls flow, subclass provides details  │  │
│   │                                                                 │  │
│   │   4. Strategy Pattern                                          │  │
│   │      └── Behavior is injected via pluggable strategies        │  │
│   │                                                                 │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Dependency Injection vs Service Locator

Both patterns solve the problem of obtaining dependencies, but in different ways:

```
┌─────────────────────────────────────────────────────────────────────────┐
│              DEPENDENCY INJECTION vs SERVICE LOCATOR                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────┬─────────────────────────────────┐ │
│  │     DEPENDENCY INJECTION        │        SERVICE LOCATOR          │ │
│  ├─────────────────────────────────┼─────────────────────────────────┤ │
│  │                                 │                                 │ │
│  │  Dependencies are PUSHED        │  Dependencies are PULLED        │ │
│  │  to the object                  │  by the object                  │ │
│  │                                 │                                 │ │
│  │  ┌─────────┐    ┌─────────┐    │    ┌─────────┐    ┌─────────┐  │ │
│  │  │Container│───▶│ Object  │    │    │ Object  │───▶│ Locator │  │ │
│  │  └─────────┘    └─────────┘    │    └─────────┘    └─────────┘  │ │
│  │   "Here's your     │           │         │       "Give me the   │ │
│  │   dependency"      │           │         │        dependency"   │ │
│  │                    ▼           │         ▼                      │ │
│  │             Dependencies       │    Dependencies                │ │
│  │             are injected       │    are requested               │ │
│  │                                 │                                 │ │
│  ├─────────────────────────────────┼─────────────────────────────────┤ │
│  │  PROS:                          │  PROS:                          │ │
│  │  ✓ Clear dependencies          │  ✓ Simpler setup               │ │
│  │  ✓ Easy to test                │  ✓ Flexible                    │ │
│  │  ✓ Compile-time safety         │  ✓ Objects control timing      │ │
│  │  ✓ No hidden dependencies      │                                 │ │
│  │                                 │                                 │ │
│  │  CONS:                          │  CONS:                          │ │
│  │  ✗ More infrastructure         │  ✗ Hidden dependencies         │ │
│  │  ✗ Steeper learning curve      │  ✗ Harder to test              │ │
│  │                                 │  ✗ Runtime errors possible     │ │
│  │                                 │  ✗ Global state dependency     │ │
│  └─────────────────────────────────┴─────────────────────────────────┘ │
│                                                                         │
│  RECOMMENDATION: Prefer Dependency Injection over Service Locator      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Code Comparison:**

```kotlin
// ═══════════════════════════════════════════════════════════════
// SERVICE LOCATOR PATTERN (Less preferred)
// ═══════════════════════════════════════════════════════════════

// Global registry
object ServiceLocator {
    private val services = mutableMapOf<Class<*>, Any>()
    
    fun <T : Any> register(clazz: Class<T>, service: T) {
        services[clazz] = service
    }
    
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(clazz: Class<T>): T {
        return services[clazz] as? T 
            ?: throw IllegalStateException("Service not registered: ${clazz.simpleName}")
    }
}

// Class PULLS its dependencies (anti-pattern aspects)
class UserViewModel : ViewModel() {
    // ❌ Hidden dependency - not visible in constructor
    private val repository: UserRepository = ServiceLocator.get(UserRepository::class.java)
    
    // Hard to know what this class needs without reading the code
}

// ═══════════════════════════════════════════════════════════════
// DEPENDENCY INJECTION PATTERN (Preferred)
// ═══════════════════════════════════════════════════════════════

// Dependencies are PUSHED via constructor
class UserViewModel(
    private val repository: UserRepository  // ✅ Visible, explicit dependency
) : ViewModel() {
    // Easy to see what this class needs just from the constructor
}
```

### Understanding the Dependency Graph

A **Dependency Graph** is a directed graph showing how objects depend on each other:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       DEPENDENCY GRAPH VISUALIZATION                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   Arrows point FROM dependent TO dependency (A depends on B: A → B)    │
│                                                                         │
│   ┌──────────────────────────────────────────────────────────────────┐ │
│   │                                                                  │ │
│   │                      ┌─────────────────┐                        │ │
│   │                      │   Application   │                        │ │
│   │                      │    (Root)       │                        │ │
│   │                      └────────┬────────┘                        │ │
│   │                               │                                  │ │
│   │               ┌───────────────┼───────────────┐                 │ │
│   │               ▼               ▼               ▼                 │ │
│   │        ┌──────────┐    ┌──────────┐    ┌──────────┐            │ │
│   │        │ Activity │    │ Service  │    │WorkManager│            │ │
│   │        └─────┬────┘    └────┬─────┘    └─────┬────┘            │ │
│   │              │              │                │                  │ │
│   │              ▼              │                │                  │ │
│   │        ┌──────────┐         │                │                  │ │
│   │        │ ViewModel│         │                │                  │ │
│   │        └─────┬────┘         │                │                  │ │
│   │              │              │                │                  │ │
│   │              └──────────────┼────────────────┘                  │ │
│   │                             ▼                                    │ │
│   │                      ┌─────────────┐                            │ │
│   │                      │ Repository  │                            │ │
│   │                      └──────┬──────┘                            │ │
│   │                             │                                    │ │
│   │               ┌─────────────┼─────────────┐                     │ │
│   │               ▼             ▼             ▼                     │ │
│   │        ┌──────────┐  ┌──────────┐  ┌──────────┐                │ │
│   │        │ApiService│  │ Database │  │Dispatcher│                │ │
│   │        └─────┬────┘  └─────┬────┘  └──────────┘                │ │
│   │              │             │                                    │ │
│   │              ▼             │                                    │ │
│   │        ┌──────────┐        │                                    │ │
│   │        │ Retrofit │        │                                    │ │
│   │        └─────┬────┘        │                                    │ │
│   │              │             │                                    │ │
│   │              ▼             ▼                                    │ │
│   │        ┌──────────┐  ┌──────────┐                              │ │
│   │        │OkHttpCli.│  │   Room   │                              │ │
│   │        └──────────┘  └──────────┘                              │ │
│   │                                                                  │ │
│   └──────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│   KEY CONCEPTS:                                                         │
│   • Root: Entry point (Application class in Android)                   │
│   • Leaf nodes: Dependencies with no further dependencies              │
│   • Transitive dependencies: A→B→C means A transitively depends on C   │
│   • DI Container manages this graph automatically                      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Object Lifecycle and Scoping

Understanding when objects are created and destroyed is crucial:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    OBJECT LIFECYCLE IN ANDROID DI                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   TIME ────────────────────────────────────────────────────────────▶   │
│                                                                         │
│   APP START                         CONFIG CHANGE              APP END  │
│       │                                  │                        │     │
│       ▼                                  ▼                        ▼     │
│   ═══════════════════════════════════════════════════════════════════   │
│   │             @Singleton (Application Scope)                  │       │
│   │ ─────────────────────────────────────────────────────────── │       │
│   │ OkHttpClient, Retrofit, Database, Repository                │       │
│   │ Created ONCE when app starts, destroyed when app dies       │       │
│   ═══════════════════════════════════════════════════════════════════   │
│                                                                         │
│       ┌─────────────────────────────────────────────────────────┐       │
│       │        @ActivityRetainedScoped                          │       │
│       │ ───────────────────────────────────────────────         │       │
│       │ Survives configuration changes (rotation)               │       │
│       │ Tied to ViewModel lifecycle                             │       │
│       └─────────────────────────────────────────────────────────┘       │
│                                                                         │
│       ┌───────────────┐              ┌───────────────┐                 │
│       │ @ActivityScoped│              │ @ActivityScoped│                 │
│       │ ───────────── │              │ ───────────── │                 │
│       │ Activity A    │              │ Activity A    │                 │
│       │ (destroyed on │              │ (new instance)│                 │
│       │  rotation)    │              │               │                 │
│       └───────────────┘              └───────────────┘                 │
│                                                                         │
│       ┌───────┐┌───────┐             ┌───────┐                         │
│       │Fragment││Fragment│             │Fragment│                         │
│       │   A   ││   B   │             │   A   │                         │
│       └───────┘└───────┘             └───────┘                         │
│                                                                         │
│   ═══════════════════════════════════════════════════════════════════   │
│                                                                         │
│   SCOPE SELECTION GUIDE:                                               │
│   ┌─────────────────────┬───────────────────────────────────────────┐  │
│   │ Scope               │ Use For                                    │  │
│   ├─────────────────────┼───────────────────────────────────────────┤  │
│   │ @Singleton          │ Network clients, databases, repositories  │  │
│   │ @ActivityRetained   │ Data that survives rotation               │  │
│   │ @ViewModelScoped    │ ViewModel-specific helpers                │  │
│   │ @ActivityScoped     │ Activity-specific dependencies           │  │
│   │ @FragmentScoped     │ Fragment-specific dependencies           │  │
│   │ Unscoped            │ Cheap objects, new instance each time    │  │
│   └─────────────────────┴───────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. DI Benefits and SOLID Principles

### How DI Enables SOLID

```
┌─────────────────────────────────────────────────────────────────────────┐
│                  DEPENDENCY INJECTION AND SOLID PRINCIPLES              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ S - SINGLE RESPONSIBILITY PRINCIPLE                               │ │
│  │━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━│ │
│  │                                                                   │ │
│  │ "A class should have only one reason to change"                  │ │
│  │                                                                   │ │
│  │ WITHOUT DI:                           WITH DI:                   │ │
│  │ ┌─────────────────────────┐         ┌─────────────────────────┐ │ │
│  │ │    UserRepository       │         │    UserRepository       │ │ │
│  │ │ ─────────────────────── │         │ ─────────────────────── │ │ │
│  │ │ • Creates OkHttpClient  │         │ • Business logic only   │ │ │
│  │ │ • Configures Retrofit   │         │ • No object creation    │ │ │
│  │ │ • Manages Database      │         │ • Single responsibility │ │ │
│  │ │ • Business logic        │         └─────────────────────────┘ │ │
│  │ │ (Too many reasons to    │                                     │ │
│  │ │  change!)               │         DI Container handles all   │ │
│  │ └─────────────────────────┘         object creation separately  │ │
│  │                                                                   │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ O - OPEN/CLOSED PRINCIPLE                                         │ │
│  │━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━│ │
│  │                                                                   │ │
│  │ "Open for extension, closed for modification"                    │ │
│  │                                                                   │ │
│  │   ┌─────────────────┐                                            │ │
│  │   │  UserViewModel  │                                            │ │
│  │   │ ─────────────── │                                            │ │
│  │   │ Depends on      │                                            │ │
│  │   │ UserRepository  │◄── Interface                               │ │
│  │   │ (interface)     │                                            │ │
│  │   └─────────────────┘                                            │ │
│  │            │                                                      │ │
│  │            │ DI can inject different implementations:            │ │
│  │            │                                                      │ │
│  │   ┌────────┴────────┬──────────────────┬────────────────────┐   │ │
│  │   ▼                 ▼                  ▼                    ▼   │ │
│  │ ┌───────────┐  ┌───────────┐  ┌────────────────┐  ┌──────────┐│ │
│  │ │RemoteRepo │  │ CacheRepo │  │OfflineFirstRepo│  │ FakeRepo ││ │
│  │ │(API only) │  │(Cache only│  │ (API + Cache)  │  │ (Tests)  ││ │
│  │ └───────────┘  └───────────┘  └────────────────┘  └──────────┘│ │
│  │                                                                   │ │
│  │ ✓ Add new implementations without changing ViewModel            │ │
│  │ ✓ Swap implementations via configuration                         │ │
│  │                                                                   │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ L - LISKOV SUBSTITUTION PRINCIPLE                                 │ │
│  │━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━│ │
│  │                                                                   │ │
│  │ "Objects should be replaceable by subtypes without breaking app" │ │
│  │                                                                   │ │
│  │ DI makes this possible by allowing any implementation            │ │
│  │ of an interface to be injected:                                  │ │
│  │                                                                   │ │
│  │   interface Logger {                                             │ │
│  │       fun log(message: String)                                   │ │
│  │   }                                                               │ │
│  │                                                                   │ │
│  │   class ConsoleLogger : Logger     // Can substitute             │ │
│  │   class FileLogger : Logger        // Can substitute             │ │
│  │   class RemoteLogger : Logger      // Can substitute             │ │
│  │   class NoOpLogger : Logger        // Can substitute             │ │
│  │                                                                   │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ I - INTERFACE SEGREGATION PRINCIPLE                               │ │
│  │━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━│ │
│  │                                                                   │ │
│  │ "Clients should not be forced to depend on unused interfaces"    │ │
│  │                                                                   │ │
│  │ DI encourages smaller, focused interfaces:                       │ │
│  │                                                                   │ │
│  │   ❌ FAT INTERFACE:                 ✅ SEGREGATED:                │ │
│  │   ┌─────────────────┐              ┌─────────────────┐           │ │
│  │   │ UserRepository  │              │ UserReader      │           │ │
│  │   │ ─────────────── │              │ ─────────────── │           │ │
│  │   │ getUsers()      │              │ getUsers()      │           │ │
│  │   │ saveUser()      │              │ getUser(id)     │           │ │
│  │   │ deleteUser()    │              └─────────────────┘           │ │
│  │   │ syncUsers()     │              ┌─────────────────┐           │ │
│  │   │ exportUsers()   │              │ UserWriter      │           │ │
│  │   │ importUsers()   │              │ ─────────────── │           │ │
│  │   └─────────────────┘              │ saveUser()      │           │ │
│  │                                    │ deleteUser()    │           │ │
│  │                                    └─────────────────┘           │ │
│  │                                                                   │ │
│  │   Inject only what each class needs                              │ │
│  │                                                                   │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │ D - DEPENDENCY INVERSION PRINCIPLE                                │ │
│  │━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━│ │
│  │                                                                   │ │
│  │ "High-level modules should not depend on low-level modules.      │ │
│  │  Both should depend on abstractions."                            │ │
│  │                                                                   │ │
│  │   ❌ DIRECT DEPENDENCY:            ✅ INVERTED DEPENDENCY:       │ │
│  │                                                                   │ │
│  │   HIGH LEVEL                       HIGH LEVEL                    │ │
│  │   ┌──────────────┐                ┌──────────────┐               │ │
│  │   │ UserViewModel│                │ UserViewModel│               │ │
│  │   └──────┬───────┘                └──────┬───────┘               │ │
│  │          │ depends                       │ depends               │ │
│  │          │ directly                      ▼                       │ │
│  │          │                        ┌──────────────┐               │ │
│  │          │                        │<<interface>> │               │ │
│  │          │                        │UserRepository│ ◄── ABSTRACTION│ │
│  │          │                        └──────────────┘               │ │
│  │          │                               ▲                       │ │
│  │          │                               │ implements            │ │
│  │          ▼                               │                       │ │
│  │   LOW LEVEL                       LOW LEVEL                      │ │
│  │   ┌──────────────┐                ┌──────────────┐               │ │
│  │   │UserRepoImpl  │                │UserRepoImpl  │               │ │
│  │   │ (concrete)   │                │ (concrete)   │               │ │
│  │   └──────────────┘                └──────────────┘               │ │
│  │                                                                   │ │
│  │   DI Framework injects the concrete implementation               │ │
│  │   ViewModel only knows about the interface                       │ │
│  │                                                                   │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Complete Code Example - SOLID with DI:**

```kotlin
// ═══════════════════════════════════════════════════════════════
// APPLYING ALL SOLID PRINCIPLES WITH DI
// ═══════════════════════════════════════════════════════════════

// INTERFACE SEGREGATION - Small, focused interfaces
interface UserReader {
    suspend fun getUsers(): List<User>
    suspend fun getUser(id: String): User?
}

interface UserWriter {
    suspend fun saveUser(user: User)
    suspend fun deleteUser(id: String)
}

interface UserSync {
    suspend fun syncWithServer()
}

// DEPENDENCY INVERSION - Depend on abstractions
interface UserRepository : UserReader, UserWriter

// LISKOV SUBSTITUTION - Any implementation can be substituted
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,      // Abstraction
    private val userDao: UserDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UserRepository {
    
    // SINGLE RESPONSIBILITY - Only handles data coordination
    // No object creation, no configuration
    
    override suspend fun getUsers(): List<User> = withContext(dispatcher) {
        userDao.getAllUsers()
    }
    
    override suspend fun getUser(id: String): User? = withContext(dispatcher) {
        userDao.getUser(id)
    }
    
    override suspend fun saveUser(user: User) = withContext(dispatcher) {
        userDao.insert(user)
        try {
            apiService.updateUser(user)
        } catch (e: Exception) {
            // Handle sync failure
        }
    }
    
    override suspend fun deleteUser(id: String) = withContext(dispatcher) {
        userDao.delete(id)
        apiService.deleteUser(id)
    }
}

// OPEN/CLOSED - Add new implementations without modification
class CachedUserRepository @Inject constructor(
    private val delegate: UserRepositoryImpl,
    private val cache: UserCache
) : UserRepository {
    
    private var cachedUsers: List<User>? = null
    
    override suspend fun getUsers(): List<User> {
        return cachedUsers ?: delegate.getUsers().also { cachedUsers = it }
    }
    
    override suspend fun getUser(id: String): User? {
        return cache.get(id) ?: delegate.getUser(id)?.also { cache.put(id, it) }
    }
    
    override suspend fun saveUser(user: User) {
        cache.put(user.id, user)
        cachedUsers = cachedUsers?.map { if (it.id == user.id) user else it }
        delegate.saveUser(user)
    }
    
    override suspend fun deleteUser(id: String) {
        cache.remove(id)
        cachedUsers = cachedUsers?.filter { it.id != id }
        delegate.deleteUser(id)
    }
}

// ViewModel only depends on abstraction (interface)
class UserViewModel @Inject constructor(
    private val userReader: UserReader  // Can inject any implementation
) : ViewModel() {
    // Uses only the reading functionality it needs
}
```

---

## 4. Types of Dependency Injection

There are three main ways to inject dependencies into an object:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    THREE TYPES OF DEPENDENCY INJECTION                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ 1. CONSTRUCTOR INJECTION (Highly Preferred)                       ║ │
│  ╠═══════════════════════════════════════════════════════════════════╣ │
│  ║                                                                   ║ │
│  ║   Dependencies provided via class constructor                     ║ │
│  ║                                                                   ║ │
│  ║   ┌─────────────────────────────────────────────────────────────┐ ║ │
│  ║   │  class UserViewModel(                                       │ ║ │
│  ║   │      private val repository: UserRepository,  ◀── Injected │ ║ │
│  ║   │      private val analytics: Analytics          ◀── Injected │ ║ │
│  ║   │  ) : ViewModel()                                            │ ║ │
│  ║   └─────────────────────────────────────────────────────────────┘ ║ │
│  ║                                                                   ║ │
│  ║   ✅ ADVANTAGES:                                                  ║ │
│  ║   • Immutable dependencies (val) - thread safe                   ║ │
│  ║   • All dependencies visible in constructor                      ║ │
│  ║   • Object always fully initialized                              ║ │
│  ║   • Easy to test - just pass mocks to constructor                ║ │
│  ║   • Compile-time checking of dependencies                        ║ │
│  ║   • Required dependencies are explicit and enforced              ║ │
│  ║                                                                   ║ │
│  ║   ❌ DISADVANTAGES:                                               ║ │
│  ║   • Not possible when you don't control construction             ║ │
│  ║     (Activities, Fragments, Services)                            ║ │
│  ║   • Large constructors may indicate design issues                ║ │
│  ║                                                                   ║ │
│  ║   ANDROID CONTEXT:                                               ║ │
│  ║   Use for: ViewModels, Repositories, UseCases, Services          ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ 2. FIELD/PROPERTY INJECTION (When Constructor Not Possible)      ║ │
│  ╠═══════════════════════════════════════════════════════════════════╣ │
│  ║                                                                   ║ │
│  ║   Dependencies set directly on class properties/fields           ║ │
│  ║                                                                   ║ │
│  ║   ┌─────────────────────────────────────────────────────────────┐ ║ │
│  ║   │  @AndroidEntryPoint                                         │ ║ │
│  ║   │  class MainActivity : AppCompatActivity() {                 │ ║ │
│  ║   │                                                             │ ║ │
│  ║   │      @Inject                                                │ ║ │
│  ║   │      lateinit var analytics: Analytics   ◀── Injected      │ ║ │
│  ║   │                                                             │ ║ │
│  ║   │      @Inject                                                │ ║ │
│  ║   │      lateinit var logger: Logger         ◀── Injected      │ ║ │
│  ║   │                                                             │ ║ │
│  ║   │      override fun onCreate(savedInstanceState: Bundle?) {   │ ║ │
│  ║   │          super.onCreate()                                   │ ║ │
│  ║   │          // analytics and logger are ready to use           │ ║ │
│  ║   │      }                                                      │ ║ │
│  ║   │  }                                                          │ ║ │
│  ║   └─────────────────────────────────────────────────────────────┘ ║ │
│  ║                                                                   ║ │
│  ║   ✅ ADVANTAGES:                                                  ║ │
│  ║   • Works when you don't control constructor                     ║ │
│  ║   • Required for Android framework classes                       ║ │
│  ║                                                                   ║ │
│  ║   ❌ DISADVANTAGES:                                               ║ │
│  ║   • Mutable dependencies (var/lateinit) - less safe             ║ │
│  ║   • Dependencies not visible in signature                       ║ │
│  ║   • Object may be in partially initialized state                ║ │
│  ║   • lateinit can throw exceptions if accessed too early         ║ │
│  ║   • Harder to test (need reflection or DI framework)            ║ │
│  ║                                                                   ║ │
│  ║   ANDROID CONTEXT:                                               ║ │
│  ║   Use for: Activities, Fragments, Services, BroadcastReceivers  ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ 3. METHOD INJECTION (Specialized Use Cases)                      ║ │
│  ╠═══════════════════════════════════════════════════════════════════╣ │
│  ║                                                                   ║ │
│  ║   Dependencies provided via method parameters                     ║ │
│  ║                                                                   ║ │
│  ║   ┌─────────────────────────────────────────────────────────────┐ ║ │
│  ║   │  class DataProcessor {                                      │ ║ │
│  ║   │                                                             │ ║ │
│  ║   │      // Setter injection                                    │ ║ │
│  ║   │      @Inject                                                │ ║ │
│  ║   │      fun setLogger(logger: Logger) {                        │ ║ │
│  ║   │          this.logger = logger                               │ ║ │
│  ║   │      }                                                      │ ║ │
│  ║   │                                                             │ ║ │
│  ║   │      // Assisted injection (parameters at runtime)         │ ║ │
│  ║   │      fun process(data: Data, helper: Helper) {             │ ║ │
│  ║   │          // helper provided at call time                    │ ║ │
│  ║   │      }                                                      │ ║ │
│  ║   │  }                                                          │ ║ │
│  ║   └─────────────────────────────────────────────────────────────┘ ║ │
│  ║                                                                   ║ │
│  ║   ✅ ADVANTAGES:                                                  ║ │
│  ║   • Allows changing dependencies at runtime                     ║ │
│  ║   • Useful for optional dependencies                            ║ │
│  ║   • Good for mix of injected + runtime parameters               ║ │
│  ║                                                                   ║ │
│  ║   ❌ DISADVANTAGES:                                               ║ │
│  ║   • Complex lifecycle management                                 ║ │
│  ║   • Object may be used before method is called                  ║ │
│  ║   • Easy to forget to call the method                           ║ │
│  ║                                                                   ║ │
│  ║   ANDROID CONTEXT:                                               ║ │
│  ║   Use for: Assisted injection, optional dependencies            ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │                    INJECTION TYPE DECISION TREE                   │ │
│  ├───────────────────────────────────────────────────────────────────┤ │
│  │                                                                   │ │
│  │   Can you control the constructor?                               │ │
│  │          │                                                        │ │
│  │    ┌─────┴─────┐                                                 │ │
│  │    │           │                                                 │ │
│  │   YES         NO                                                  │ │
│  │    │           │                                                 │ │
│  │    ▼           ▼                                                 │ │
│  │  ┌─────────┐ ┌─────────────────────┐                             │ │
│  │  │CONSTRUCT│ │Is it an Android     │                             │ │
│  │  │   OR    │ │framework class?     │                             │ │
│  │  │INJECTION│ │(Activity, Fragment) │                             │ │
│  │  └─────────┘ └──────────┬──────────┘                             │ │
│  │                         │                                         │ │
│  │                   ┌─────┴─────┐                                  │ │
│  │                   │           │                                  │ │
│  │                  YES         NO                                   │ │
│  │                   │           │                                  │ │
│  │                   ▼           ▼                                  │ │
│  │             ┌──────────┐ ┌───────────┐                           │ │
│  │             │  FIELD   │ │  METHOD   │                           │ │
│  │             │INJECTION │ │ INJECTION │                           │ │
│  │             └──────────┘ └───────────┘                           │ │
│  │                                                                   │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Complete Code Examples:**

```kotlin
// ═══════════════════════════════════════════════════════════════
// 1. CONSTRUCTOR INJECTION - Preferred for most cases
// ═══════════════════════════════════════════════════════════════

// With Hilt
@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // All dependencies are:
    // ✓ Immutable (val)
    // ✓ Private (encapsulated)
    // ✓ Non-null (required)
    // ✓ Available immediately
    
    private val userId: String = savedStateHandle.get<String>("userId") ?: ""
}

// With Dagger
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    // Same benefits as above
}

// ═══════════════════════════════════════════════════════════════
// 2. FIELD INJECTION - For Android framework classes
// ═══════════════════════════════════════════════════════════════

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    // Field injection with Hilt
    @Inject
    lateinit var analytics: AnalyticsService
    
    @Inject
    lateinit var navigator: Navigator
    
    // ViewModel injection (special case - uses field but works like constructor)
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // After super.onCreate(), all @Inject fields are initialized
        
        analytics.logScreenView("MainActivity")
    }
}

@AndroidEntryPoint
class UserFragment : Fragment(R.layout.fragment_user) {
    
    @Inject
    lateinit var imageLoader: ImageLoader
    
    // Get ViewModel scoped to Activity
    private val sharedViewModel: SharedViewModel by activityViewModels()
    
    // Get ViewModel scoped to this Fragment
    private val viewModel: UserViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // All dependencies are ready
    }
}

// ═══════════════════════════════════════════════════════════════
// 3. METHOD INJECTION - Special cases
// ═══════════════════════════════════════════════════════════════

// Setter injection (rare, for optional dependencies)
class AnalyticsTracker @Inject constructor(
    private val baseUrl: String
) {
    private var debugLogger: DebugLogger? = null
    
    @Inject  // Called after constructor
    fun setDebugLogger(logger: DebugLogger) {
        this.debugLogger = logger
    }
    
    fun track(event: String) {
        debugLogger?.log("Tracking: $event")  // Optional
        // ... actual tracking
    }
}

// Assisted Injection (mix of DI and runtime parameters)
// Very useful when some parameters are known at compile time
// and others only at runtime

// Define the factory interface
@AssistedFactory
interface UserDetailViewModelFactory {
    fun create(userId: String): UserDetailViewModel
}

// The ViewModel with both injected and runtime parameters
class UserDetailViewModel @AssistedInject constructor(
    private val repository: UserRepository,                // Injected by DI
    private val analytics: Analytics,                      // Injected by DI
    @Assisted private val userId: String                   // Provided at runtime
) : ViewModel() {
    
    init {
        loadUser(userId)
    }
}

// Usage in Fragment
@AndroidEntryPoint
class UserDetailFragment : Fragment() {
    
    @Inject
    lateinit var viewModelFactory: UserDetailViewModelFactory
    
    private val viewModel: UserDetailViewModel by viewModels {
        val userId = arguments?.getString("userId") ?: ""
        // Create factory that provides the runtime parameter
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return viewModelFactory.create(userId) as T
            }
        }
    }
}
```

---

## 5. Manual Dependency Injection

Before using frameworks, it's important to understand manual DI - this helps you appreciate what frameworks do for you.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     MANUAL DI ARCHITECTURE                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌───────────────────────────────────────────────────────────────────┐│
│   │                         Application                               ││
│   │                             │                                     ││
│   │                             │ creates & holds                     ││
│   │                             ▼                                     ││
│   │    ┌─────────────────────────────────────────────────────────┐   ││
│   │    │                    AppContainer                          │   ││
│   │    │  ┌───────────────────────────────────────────────────┐  │   ││
│   │    │  │          SINGLETON DEPENDENCIES                    │  │   ││
│   │    │  │                                                    │  │   ││
│   │    │  │  • OkHttpClient      (lazy, created once)         │  │   ││
│   │    │  │  • Retrofit          (lazy, needs OkHttpClient)   │  │   ││
│   │    │  │  • ApiService        (lazy, needs Retrofit)       │  │   ││
│   │    │  │  • AppDatabase       (lazy, needs Context)        │  │   ││
│   │    │  │  • UserRepository    (lazy, needs Api + DB)       │  │   ││
│   │    │  │                                                    │  │   ││
│   │    │  └───────────────────────────────────────────────────┘  │   ││
│   │    │                                                          │   ││
│   │    │  ┌───────────────────────────────────────────────────┐  │   ││
│   │    │  │         FACTORY METHODS (new each time)           │  │   ││
│   │    │  │                                                    │  │   ││
│   │    │  │  • createUserViewModel()                          │  │   ││
│   │    │  │  • createUserListAdapter()                        │  │   ││
│   │    │  │                                                    │  │   ││
│   │    │  └───────────────────────────────────────────────────┘  │   ││
│   │    └─────────────────────────────────────────────────────────┘   ││
│   │                             ▲                                     ││
│   │                             │ obtains container                   ││
│   │                             │                                     ││
│   │    ┌───────────────────────┴────────────────────────────────┐   ││
│   │    │                                                         │   ││
│   │  ┌─┴───────────┐    ┌───────────┐    ┌───────────┐          │   ││
│   │  │  Activity   │    │  Fragment │    │  Service  │          │   ││
│   │  │             │    │           │    │           │          │   ││
│   │  │ viewModel = │    │ viewModel │    │ repository│          │   ││
│   │  │ container   │    │ = ...     │    │ = ...     │          │   ││
│   │  │ .create..() │    │           │    │           │          │   ││
│   │  └─────────────┘    └───────────┘    └───────────┘          │   ││
│   │                                                               │   ││
│   └───────────────────────────────────────────────────────────────────┘│
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Step-by-Step Manual DI Implementation

```kotlin
// ═══════════════════════════════════════════════════════════════
// STEP 1: Define Interfaces (Abstractions)
// ═══════════════════════════════════════════════════════════════

// Data model
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?
)

// Network abstraction
interface UserApi {
    @GET("users")
    suspend fun getUsers(): List<UserDto>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto
    
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: UserDto): UserDto
}

// Repository abstraction
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUser(id: String): Result<User>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun refreshUsers(): Result<Unit>
}

// ═══════════════════════════════════════════════════════════════
// STEP 2: Create Concrete Implementations
// ═══════════════════════════════════════════════════════════════

class UserRepositoryImpl(
    private val api: UserApi,
    private val userDao: UserDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {
    
    override suspend fun getUsers(): Result<List<User>> = withContext(dispatcher) {
        runCatching {
            userDao.getAllUsers().map { it.toUser() }
        }
    }
    
    override suspend fun getUser(id: String): Result<User> = withContext(dispatcher) {
        runCatching {
            userDao.getUserById(id)?.toUser()
                ?: api.getUser(id).toUser().also { 
                    userDao.insert(UserEntity.fromUser(it)) 
                }
        }
    }
    
    override suspend fun saveUser(user: User): Result<Unit> = withContext(dispatcher) {
        runCatching {
            userDao.insert(UserEntity.fromUser(user))
            api.updateUser(user.id, UserDto.fromUser(user))
            Unit
        }
    }
    
    override suspend fun refreshUsers(): Result<Unit> = withContext(dispatcher) {
        runCatching {
            val remoteUsers = api.getUsers()
            userDao.deleteAllAndInsert(remoteUsers.map { UserEntity.fromDto(it) })
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// STEP 3: Create the DI Container
// ═══════════════════════════════════════════════════════════════

/**
 * Manual DI Container - manages all dependencies
 * 
 * This is what Dagger/Hilt generates automatically for you!
 */
class AppContainer(private val context: Context) {
    
    // ─────────────────────────────────────────────────────────────
    // Network Layer (Singletons)
    // ─────────────────────────────────────────────────────────────
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.example.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .create()
            ))
            .build()
    }
    
    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }
    
    // ─────────────────────────────────────────────────────────────
    // Database Layer (Singleton)
    // ─────────────────────────────────────────────────────────────
    
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    private val userDao: UserDao by lazy {
        database.userDao()
    }
    
    // ─────────────────────────────────────────────────────────────
    // Repository Layer (Singleton)
    // ─────────────────────────────────────────────────────────────
    
    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(
            api = userApi,
            userDao = userDao,
            dispatcher = Dispatchers.IO
        )
    }
    
    // ─────────────────────────────────────────────────────────────
    // ViewModel Factories (New instance each time)
    // ─────────────────────────────────────────────────────────────
    
    fun userViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UserViewModel(userRepository) as T
            }
        }
    }
    
    fun userDetailViewModelFactory(userId: String): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UserDetailViewModel(userId, userRepository) as T
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// STEP 4: Application Class Setup
// ═══════════════════════════════════════════════════════════════

class MyApplication : Application() {
    
    // The container holding all dependencies
    lateinit var container: AppContainer
        private set
    
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

// Extension property for easy access
val Context.appContainer: AppContainer
    get() = (applicationContext as MyApplication).container

// ═══════════════════════════════════════════════════════════════
// STEP 5: Usage in Activity/Fragment
// ═══════════════════════════════════════════════════════════════

class UserListActivity : AppCompatActivity() {
    
    private lateinit var viewModel: UserViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        
        // Get ViewModel using the factory from container
        viewModel = ViewModelProvider(
            this,
            appContainer.userViewModelFactory()
        )[UserViewModel::class.java]
        
        observeViewModel()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.users.collect { users ->
                // Update UI
            }
        }
    }
}

class UserDetailActivity : AppCompatActivity() {
    
    private lateinit var viewModel: UserDetailViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        
        val userId = intent.getStringExtra("USER_ID") ?: return
        
        // Factory needs runtime parameter
        viewModel = ViewModelProvider(
            this,
            appContainer.userDetailViewModelFactory(userId)
        )[UserDetailViewModel::class.java]
    }
}
```

### Problems with Manual DI

```
┌─────────────────────────────────────────────────────────────────────────┐
│                 PROBLEMS WITH MANUAL DEPENDENCY INJECTION               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. LOTS OF BOILERPLATE CODE                                           │
│     ─────────────────────────                                          │
│     • Every dependency needs manual wiring                             │
│     • ViewModel factories for every ViewModel                          │
│     • Repetitive lazy initialization code                              │
│                                                                         │
│  2. ERROR-PRONE LIFECYCLE MANAGEMENT                                   │
│     ─────────────────────────────────                                  │
│     • Easy to forget to clean up resources                            │
│     • Scope management is manual and complex                          │
│     • Memory leaks from holding wrong references                       │
│                                                                         │
│  3. NO COMPILE-TIME VERIFICATION                                       │
│     ─────────────────────────────                                      │
│     • Missing dependencies found at runtime                            │
│     • Circular dependencies not detected                               │
│     • Crashes happen in production, not development                    │
│                                                                         │
│  4. HARD TO MAINTAIN IN LARGE CODEBASES                               │
│     ──────────────────────────────────                                 │
│     • Container grows very large                                       │
│     • Dependencies become tangled                                      │
│     • Refactoring is painful                                           │
│                                                                         │
│  5. TESTING REQUIRES CUSTOM SETUP                                      │
│     ────────────────────────────                                       │
│     • Need separate test containers                                    │
│     • Manual mock injection                                            │
│     • No standardized testing patterns                                 │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ THIS IS WHY WE USE DI FRAMEWORKS LIKE DAGGER/HILT/KOIN!        │   │
│  │ They solve all these problems with code generation and DSLs    │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 6. Dagger 2

Dagger is Google's compile-time dependency injection framework that generates highly optimized code. Understanding Dagger helps you understand what Hilt does under the hood.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       DAGGER 2 OVERVIEW                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   Dagger = Directed Acyclic Graph (DAG) + -ger (generator)             │
│                                                                         │
│   KEY FEATURES:                                                         │
│   • Compile-time code generation (no reflection)                       │
│   • Zero runtime overhead                                              │
│   • Compile-time error detection                                       │
│   • Generated code is human-readable and debuggable                    │
│   • JSR-330 standard annotations                                       │
│                                                                         │
│   HOW IT WORKS:                                                         │
│   1. You define how to create objects (@Module, @Inject)               │
│   2. You define injection points (@Component)                          │
│   3. Dagger generates factory classes at compile time                  │
│   4. Generated code creates and wires up all dependencies              │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Dagger Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    DAGGER 2 ARCHITECTURE DIAGRAM                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                         @MODULE                                 │  │
│   │   "I know HOW to create these dependencies"                    │  │
│   │                                                                 │  │
│   │   ┌─────────────────┐  ┌─────────────────┐  ┌───────────────┐  │  │
│   │   │  NetworkModule  │  │ DatabaseModule  │  │ RepositoryMod │  │  │
│   │   │                 │  │                 │  │               │  │  │
│   │   │ @Provides       │  │ @Provides       │  │ @Binds        │  │  │
│   │   │ OkHttpClient    │  │ AppDatabase     │  │ UserRepository│  │  │
│   │   │ Retrofit        │  │ UserDao         │  │ (interface to │  │  │
│   │   │ ApiService      │  │                 │  │  impl binding)│  │  │
│   │   └─────────────────┘  └─────────────────┘  └───────────────┘  │  │
│   │                                                                 │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                              │                                          │
│                              │ modules = [...]                          │
│                              ▼                                          │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                        @COMPONENT                               │  │
│   │   "I am the bridge that connects everything"                   │  │
│   │                                                                 │  │
│   │   @Singleton                                                    │  │
│   │   @Component(modules = [NetworkModule, DatabaseModule, ...])    │  │
│   │   interface AppComponent {                                      │  │
│   │                                                                 │  │
│   │       // Provision method - expose dependency                  │  │
│   │       fun repository(): UserRepository                         │  │
│   │                                                                 │  │
│   │       // Injection method - inject into target                 │  │
│   │       fun inject(activity: MainActivity)                       │  │
│   │                                                                 │  │
│   │       // Subcomponent factory                                   │  │
│   │       fun activityComponent(): ActivityComponent.Factory       │  │
│   │   }                                                             │  │
│   │                                                                 │  │
│   │         Generated: DaggerAppComponent                          │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                              │                                          │
│                              │ inject()                                 │
│                              ▼                                          │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                        @INJECT                                  │  │
│   │   "I NEED these dependencies"                                  │  │
│   │                                                                 │  │
│   │   ┌─────────────────────────────────────────────────────┐      │  │
│   │   │ class MainActivity : AppCompatActivity() {          │      │  │
│   │   │                                                     │      │  │
│   │   │     @Inject                                         │      │  │
│   │   │     lateinit var repository: UserRepository ◀────── │      │  │
│   │   │                                    Injected by      │      │  │
│   │   │     @Inject                        Component        │      │  │
│   │   │     lateinit var analytics: Analytics               │      │  │
│   │   │ }                                                   │      │  │
│   │   └─────────────────────────────────────────────────────┘      │  │
│   │                                                                 │  │
│   │   ┌─────────────────────────────────────────────────────┐      │  │
│   │   │ class UserRepository @Inject constructor(           │      │  │
│   │   │     private val api: ApiService,     ◀───────────── │      │  │
│   │   │     private val dao: UserDao         Dagger sees    │      │  │
│   │   │ )                                    constructor    │      │  │
│   │   │                                      and knows how  │      │  │
│   │   │ // Dagger can create this class!    to create it   │      │  │
│   │   └─────────────────────────────────────────────────────┘      │  │
│   │                                                                 │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Setup

```kotlin
// build.gradle.kts (app)
plugins {
    id("kotlin-kapt")  // Kotlin annotation processing
}

android {
    // ...
}

dependencies {
    // Dagger 2
    implementation("com.google.dagger:dagger:2.51")
    kapt("com.google.dagger:dagger-compiler:2.51")
}
```

### Core Annotations Explained

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    DAGGER 2 ANNOTATIONS REFERENCE                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ @Inject                                                           ║ │
│  ║━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━║ │
│  ║                                                                   ║ │
│  ║ CONSTRUCTOR: "Dagger, you can create this class!"                ║ │
│  ║ ┌─────────────────────────────────────────────────────────────┐  ║ │
│  ║ │ class Repository @Inject constructor(                       │  ║ │
│  ║ │     private val api: ApiService  // Dagger will provide     │  ║ │
│  ║ │ )                                                           │  ║ │
│  ║ └─────────────────────────────────────────────────────────────┘  ║ │
│  ║                                                                   ║ │
│  ║ FIELD: "Dagger, please set this field!"                          ║ │
│  ║ ┌─────────────────────────────────────────────────────────────┐  ║ │
│  ║ │ class MainActivity : AppCompatActivity() {                  │  ║ │
│  ║ │     @Inject lateinit var repository: Repository             │  ║ │
│  ║ │ }                                                           │  ║ │
│  ║ └─────────────────────────────────────────────────────────────┘  ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ @Module / @Provides / @Binds                                      ║ │
│  ║━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━║ │
│  ║                                                                   ║ │
│  ║ @Module: Container class for provision methods                    ║ │
│  ║ @Provides: Method that returns a dependency instance              ║ │
│  ║ @Binds: Efficient way to bind interface to implementation        ║ │
│  ║                                                                   ║ │
│  ║ ┌─────────────────────────────────────────────────────────────┐  ║ │
│  ║ │ @Module                                                     │  ║ │
│  ║ │ object NetworkModule {                                      │  ║ │
│  ║ │                                                             │  ║ │
│  ║ │     @Provides                                               │  ║ │
│  ║ │     @Singleton                                              │  ║ │
│  ║ │     fun provideOkHttp(): OkHttpClient = OkHttpClient()      │  ║ │
│  ║ │                                                             │  ║ │
│  ║ │     @Provides                                               │  ║ │
│  ║ │     @Singleton                                              │  ║ │
│  ║ │     fun provideRetrofit(client: OkHttpClient): Retrofit {  │  ║ │
│  ║ │         // client is injected by Dagger                    │  ║ │
│  ║ │         return Retrofit.Builder().client(client).build()   │  ║ │
│  ║ │     }                                                       │  ║ │
│  ║ │ }                                                           │  ║ │
│  ║ └─────────────────────────────────────────────────────────────┘  ║ │
│  ║                                                                   ║ │
│  ║ @Binds (More efficient - no method body needed):                  ║ │
│  ║ ┌─────────────────────────────────────────────────────────────┐  ║ │
│  ║ │ @Module                                                     │  ║ │
│  ║ │ abstract class RepositoryModule {                           │  ║ │
│  ║ │                                                             │  ║ │
│  ║ │     @Binds                                                  │  ║ │
│  ║ │     @Singleton                                              │  ║ │
│  ║ │     abstract fun bindUserRepo(                              │  ║ │
│  ║ │         impl: UserRepositoryImpl                            │  ║ │
│  ║ │     ): UserRepository  // Interface                         │  ║ │
│  ║ │ }                                                           │  ║ │
│  ║ └─────────────────────────────────────────────────────────────┘  ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ @Component                                                        ║ │
│  ║━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━║ │
│  ║                                                                   ║ │
│  ║ The glue that connects modules to injection points               ║ │
│  ║                                                                   ║ │
│  ║ ┌─────────────────────────────────────────────────────────────┐  ║ │
│  ║ │ @Singleton                                                  │  ║ │
│  ║ │ @Component(modules = [NetworkModule::class, RepoModule::class])│║ │
│  ║ │ interface AppComponent {                                    │  ║ │
│  ║ │                                                             │  ║ │
│  ║ │     // Provision method: Get dependency directly            │  ║ │
│  ║ │     fun userRepository(): UserRepository                    │  ║ │
│  ║ │                                                             │  ║ │
│  ║ │     // Member injection: Inject fields                      │  ║ │
│  ║ │     fun inject(activity: MainActivity)                      │  ║ │
│  ║ │                                                             │  ║ │
│  ║ │     // Factory pattern                                      │  ║ │
│  ║ │     @Component.Factory                                      │  ║ │
│  ║ │     interface Factory {                                     │  ║ │
│  ║ │         fun create(@BindsInstance context: Context): AppComp│  ║ │
│  ║ │     }                                                       │  ║ │
│  ║ │ }                                                           │  ║ │
│  ║ └─────────────────────────────────────────────────────────────┘  ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ @Scope / @Singleton                                               ║ │
│  ║━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━║ │
│  ║                                                                   ║ │
│  ║ Control instance lifecycle within a component                     ║ │
│  ║                                                                   ║ │
│  ║ @Singleton: One instance per component lifetime                   ║ │
│  ║                                                                   ║ │
│  ║ Custom scopes:                                                    ║ │
│  ║ ┌─────────────────────────────────────────────────────────────┐  ║ │
│  ║ │ @Scope                                                      │  ║ │
│  ║ │ @Retention(AnnotationRetention.RUNTIME)                     │  ║ │
│  ║ │ annotation class ActivityScope  // One per Activity         │  ║ │
│  ║ │                                                             │  ║ │
│  ║ │ @Scope                                                      │  ║ │
│  ║ │ @Retention(AnnotationRetention.RUNTIME)                     │  ║ │
│  ║ │ annotation class FragmentScope  // One per Fragment         │  ║ │
│  ║ └─────────────────────────────────────────────────────────────┘  ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ @Qualifier / @Named                                               ║ │
│  ║━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━║ │
│  ║                                                                   ║ │
│  ║ Distinguish between multiple bindings of the same type           ║ │
│  ║                                                                   ║ │
│  ║ ┌─────────────────────────────────────────────────────────────┐  ║ │
│  ║ │ // Custom qualifier                                         │  ║ │
│  ║ │ @Qualifier                                                  │  ║ │
│  ║ │ @Retention(AnnotationRetention.BINARY)                      │  ║ │
│  ║ │ annotation class IoDispatcher                               │  ║ │
│  ║ │                                                             │  ║ │
│  ║ │ @Qualifier                                                  │  ║ │
│  ║ │ @Retention(AnnotationRetention.BINARY)                      │  ║ │
│  ║ │ annotation class MainDispatcher                             │  ║ │
│  ║ │                                                             │  ║ │
│  ║ │ // Or use built-in @Named                                   │  ║ │
│  ║ │ @Provides @Named("api_url")                                 │  ║ │
│  ║ │ fun provideApiUrl(): String = "https://api.example.com"     │  ║ │
│  ║ └─────────────────────────────────────────────────────────────┘  ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Complete Dagger 2 Example

```kotlin
// ═══════════════════════════════════════════════════════════════
// STEP 1: Define Domain Models and Interfaces
// ═══════════════════════════════════════════════════════════════

data class User(
    val id: String,
    val name: String,
    val email: String
)

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<User>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): User
}

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUser(id: String): User
}

// ═══════════════════════════════════════════════════════════════
// STEP 2: Create Implementations with @Inject Constructor
// ═══════════════════════════════════════════════════════════════

// Dagger sees @Inject constructor and knows how to create this class
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UserRepository {
    
    override suspend fun getUsers(): List<User> = withContext(dispatcher) {
        try {
            val remoteUsers = apiService.getUsers()
            userDao.insertAll(remoteUsers)
            remoteUsers
        } catch (e: Exception) {
            userDao.getAllUsers()  // Return cached on error
        }
    }
    
    override suspend fun getUser(id: String): User = withContext(dispatcher) {
        userDao.getUser(id) ?: apiService.getUser(id).also {
            userDao.insert(it)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// STEP 3: Create Modules - Define How to Create Dependencies
// ═══════════════════════════════════════════════════════════════

// Object module - all methods are static (more efficient)
@Module
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,  // Dagger provides this
        gson: Gson                    // Dagger provides this
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

// Abstract module with @Binds - more efficient for interface binding
@Module
abstract class RepositoryModule {
    
    // @Binds tells Dagger: when someone asks for UserRepository,
    // give them UserRepositoryImpl
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}

@Module
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}

@Module
object DispatcherModule {
    
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    
    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

// ═══════════════════════════════════════════════════════════════
// STEP 4: Create Component - The Bridge
// ═══════════════════════════════════════════════════════════════

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        RepositoryModule::class,
        DatabaseModule::class,
        DispatcherModule::class,
        SubcomponentsModule::class
    ]
)
interface AppComponent {
    
    // ─────────────────────────────────────────────────────────────
    // Provision Methods: Expose dependencies to outside world
    // ─────────────────────────────────────────────────────────────
    fun userRepository(): UserRepository
    fun apiService(): ApiService
    
    // ─────────────────────────────────────────────────────────────
    // Member Injection Methods: Inject fields marked with @Inject
    // ─────────────────────────────────────────────────────────────
    fun inject(activity: MainActivity)
    fun inject(fragment: UserListFragment)
    fun inject(service: SyncService)
    
    // ─────────────────────────────────────────────────────────────
    // Subcomponent Factories: For scoped components
    // ─────────────────────────────────────────────────────────────
    fun activityComponentFactory(): ActivityComponent.Factory
    
    // ─────────────────────────────────────────────────────────────
    // Component Factory: How to create the component
    // ─────────────────────────────────────────────────────────────
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance @ApplicationContext context: Context
        ): AppComponent
    }
}

// Subcomponents module - registers all subcomponents
@Module(subcomponents = [ActivityComponent::class])
interface SubcomponentsModule

// ═══════════════════════════════════════════════════════════════
// STEP 5: Initialize in Application
// ═══════════════════════════════════════════════════════════════

class MyApplication : Application() {
    
    // Dagger generates DaggerAppComponent
    lateinit var appComponent: AppComponent
        private set
    
    override fun onCreate() {
        super.onCreate()
        // DaggerAppComponent is generated by Dagger
        appComponent = DaggerAppComponent.factory().create(this)
    }
}

// Extension for easy access from any Context
val Context.appComponent: AppComponent
    get() = (applicationContext as MyApplication).appComponent

// ═══════════════════════════════════════════════════════════════
// STEP 6: Use in Activity/Fragment
// ═══════════════════════════════════════════════════════════════

class MainActivity : AppCompatActivity() {
    
    // Fields to be injected
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var analytics: AnalyticsService
    
    // For ViewModel, use ViewModelProvider with factory
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // IMPORTANT: Inject BEFORE super.onCreate()
        // So that injected fields are available in onCreate
        appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Create ViewModel (or inject via subcomponent)
        viewModel = ViewModelProvider(this, MainViewModelFactory(userRepository))
            .get(MainViewModel::class.java)
        
        // Now dependencies are available
        lifecycleScope.launch {
            val users = userRepository.getUsers()
            updateUI(users)
        }
    }
}

class UserListFragment : Fragment() {
    
    @Inject lateinit var imageLoader: ImageLoader
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Inject in onAttach for Fragments
        context.appComponent.inject(this)
    }
}
```

### Subcomponents (Scoped Hierarchies)

Subcomponents allow you to create scoped hierarchies for better lifecycle management:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    DAGGER SUBCOMPONENT HIERARCHY                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌───────────────────────────────────────────────────────────────────┐│
│   │                      @Singleton                                   ││
│   │                     AppComponent                                  ││
│   │  ┌─────────────────────────────────────────────────────────────┐ ││
│   │  │  LIVES FOR: Entire app lifetime                             │ ││
│   │  │                                                              │ ││
│   │  │  PROVIDES: OkHttpClient, Retrofit, Database, Repository    │ ││
│   │  │            (Expensive, shared resources)                    │ ││
│   │  └─────────────────────────────────────────────────────────────┘ ││
│   └───────────────────────────────┬───────────────────────────────────┘│
│                                   │                                     │
│            ┌──────────────────────┼──────────────────────┐             │
│            ▼                      ▼                      ▼             │
│   ┌────────────────────┐ ┌────────────────────┐ ┌────────────────────┐│
│   │  @ActivityScope    │ │  @ActivityScope    │ │  @ActivityScope    ││
│   │  MainComponent     │ │  UserComponent     │ │  SettingsComponent ││
│   │                    │ │                    │ │                    ││
│   │  LIVES FOR:        │ │  LIVES FOR:        │ │  LIVES FOR:        ││
│   │  MainActivity life │ │  UserActivity life │ │  SettingsAct life ││
│   │                    │ │                    │ │                    ││
│   │  PROVIDES:         │ │  PROVIDES:         │ │  PROVIDES:         ││
│   │  MainPresenter,    │ │  UserPresenter,    │ │  SettingsPresenter││
│   │  MainNavigator     │ │  UserUseCase       │ │  ThemeManager     ││
│   └─────────┬──────────┘ └────────────────────┘ └────────────────────┘│
│             │                                                          │
│             ▼                                                          │
│   ┌────────────────────┐                                              │
│   │  @FragmentScope    │                                              │
│   │  ListComponent     │                                              │
│   │                    │                                              │
│   │  LIVES FOR:        │                                              │
│   │  ListFragment life │                                              │
│   │                    │                                              │
│   │  PROVIDES:         │                                              │
│   │  ListAdapter,      │                                              │
│   │  ItemClickHandler  │                                              │
│   └────────────────────┘                                              │
│                                                                         │
│   MEMORY MANAGEMENT:                                                   │
│   • When Activity dies, ActivityScope dependencies are garbage         │
│     collected (not held by AppComponent)                               │
│   • When Fragment dies, FragmentScope dependencies are released        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Subcomponent Implementation:**

```kotlin
// ═══════════════════════════════════════════════════════════════
// Custom Scopes
// ═══════════════════════════════════════════════════════════════

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FragmentScope

// ═══════════════════════════════════════════════════════════════
// Subcomponent Definition
// ═══════════════════════════════════════════════════════════════

@ActivityScope
@Subcomponent(modules = [MainActivityModule::class])
interface MainActivityComponent {
    
    // Inject into MainActivity
    fun inject(activity: MainActivity)
    
    // Expose ViewModel to the activity
    fun mainViewModel(): MainViewModel
    
    // Factory to create this subcomponent
    @Subcomponent.Factory
    interface Factory {
        fun create(): MainActivityComponent
    }
}

@Module
class MainActivityModule {
    
    @Provides
    @ActivityScope
    fun provideMainPresenter(
        repository: UserRepository  // From parent component
    ): MainPresenter {
        return MainPresenter(repository)
    }
    
    @Provides
    @ActivityScope
    fun provideMainViewModel(
        repository: UserRepository,
        analytics: Analytics
    ): MainViewModel {
        return MainViewModel(repository, analytics)
    }
    
    @Provides
    fun provideNavigator(activity: MainActivity): Navigator {
        return NavigatorImpl(activity)
    }
}

// ═══════════════════════════════════════════════════════════════
// Register Subcomponent in Parent
// ═══════════════════════════════════════════════════════════════

// Register subcomponents
@Module(subcomponents = [MainActivityComponent::class])
interface SubcomponentsModule

// Parent component exposes factory
@Singleton
@Component(modules = [NetworkModule::class, SubcomponentsModule::class])
interface AppComponent {
    fun mainActivityComponentFactory(): MainActivityComponent.Factory
}

// ═══════════════════════════════════════════════════════════════
// Usage in Activity
// ═══════════════════════════════════════════════════════════════

class MainActivity : AppCompatActivity() {
    
    // Hold reference to scoped component
    lateinit var component: MainActivityComponent
    
    @Inject lateinit var presenter: MainPresenter
    @Inject lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Create scoped component from parent
        component = appComponent.mainActivityComponentFactory().create()
        component.inject(this)
        
        super.onCreate(savedInstanceState)
        
        // Dependencies are ready
        presenter.onStart()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // When Activity is destroyed, the component and all its
        // @ActivityScope dependencies become eligible for GC
    }
}
```

### Qualifiers - Multiple Bindings of Same Type

```kotlin
// ═══════════════════════════════════════════════════════════════
// Define Custom Qualifiers
// ═══════════════════════════════════════════════════════════════

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CdnUrl

// ═══════════════════════════════════════════════════════════════
// Provide Multiple Same-Type Dependencies
// ═══════════════════════════════════════════════════════════════

@Module
object DispatcherModule {
    
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    
    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

@Module
object UrlModule {
    
    @Provides
    @ApiUrl
    fun provideApiUrl(): String = "https://api.example.com/v1/"
    
    @Provides
    @CdnUrl
    fun provideCdnUrl(): String = "https://cdn.example.com/"
}

// ═══════════════════════════════════════════════════════════════
// Inject with Qualifiers
// ═══════════════════════════════════════════════════════════════

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : UserRepository {
    
    override suspend fun getUsers(): List<User> = withContext(ioDispatcher) {
        val users = apiService.getUsers()
        withContext(mainDispatcher) {
            // Update UI-related cache on main thread
        }
        users
    }
}

class ImageLoader @Inject constructor(
    @CdnUrl private val baseUrl: String,
    private val okHttpClient: OkHttpClient
) {
    fun loadImage(path: String): String = "$baseUrl$path"
}
```
│   │ ListComponent │                                            │
│   │               │                                            │
│   │ • Adapter     │                                            │
│   └───────────────┘                                            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

```kotlin
// Custom scopes
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FragmentScope

// Subcomponent definition
@ActivityScope
@Subcomponent(modules = [MainActivityModule::class])
interface MainActivityComponent {
    
    fun inject(activity: MainActivity)
    
    @Subcomponent.Factory
    interface Factory {
        fun create(): MainActivityComponent
    }
}

@Module
class MainActivityModule {
    
    @Provides
    @ActivityScope
    fun provideMainViewModel(repository: UserRepository): MainViewModel {
        return MainViewModel(repository)
    }
}

// Register in parent component
@Singleton
@Component(modules = [NetworkModule::class, SubcomponentsModule::class])
interface AppComponent {
    fun mainActivityComponentFactory(): MainActivityComponent.Factory
}

@Module(subcomponents = [MainActivityComponent::class])
interface SubcomponentsModule

// Usage
class MainActivity : AppCompatActivity() {
    
    @Inject lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.mainActivityComponentFactory().create().inject(this)
        super.onCreate(savedInstanceState)
    }
}
```

### Qualifiers

```kotlin
// Define qualifiers
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

// Provide with qualifiers
@Module
class DispatcherModule {
    
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

@Module
class ConfigModule {
    
    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = "https://api.example.com/"
    
    @Provides
    @Named("debug")  // Built-in qualifier
    fun provideDebugUrl(): String = "https://debug.api.example.com/"
}

// Inject with qualifiers
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UserRepository {
    
    override suspend fun getUsers(): List<User> = withContext(dispatcher) {
        apiService.getUsers()
    }
}
```

---

## 7. Hilt

Hilt is Google's recommended DI solution for Android. Built on top of Dagger, it provides a standardized way to incorporate DI into Android apps with much less boilerplate.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          HILT OVERVIEW                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   WHY HILT?                                                            │
│   • Simplifies Dagger setup for Android                                │
│   • Predefined components and scopes (no manual setup)                 │
│   • First-class ViewModel support                                      │
│   • Integration with Jetpack libraries                                 │
│   • Compile-time safety (like Dagger)                                  │
│   • Google-recommended and maintained                                  │
│                                                                         │
│   WHAT HILT PROVIDES:                                                  │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ • Pre-built component hierarchy tied to Android lifecycles     │  │
│   │ • @HiltAndroidApp - auto-generates app component               │  │
│   │ • @AndroidEntryPoint - enables injection in activities/frags   │  │
│   │ • @HiltViewModel - easy ViewModel injection                    │  │
│   │ • @InstallIn - declares which component a module belongs to    │  │
│   │ • Built-in qualifiers (@ApplicationContext, @ActivityContext)  │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Hilt vs Dagger Comparison

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        HILT vs DAGGER COMPARISON                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌────────────────────────────────┬────────────────────────────────┐  │
│   │           DAGGER               │            HILT                │  │
│   ├────────────────────────────────┼────────────────────────────────┤  │
│   │                                │                                │  │
│   │  // Manual component          │  // Just one annotation!       │  │
│   │  @Singleton                   │  @HiltAndroidApp               │  │
│   │  @Component(modules = [...])   │  class MyApp : Application()   │  │
│   │  interface AppComponent {      │                                │  │
│   │    fun inject(activity: ...)  │                                │  │
│   │    @Component.Factory         │                                │  │
│   │    interface Factory { ... }  │                                │  │
│   │  }                            │                                │  │
│   │                                │                                │  │
│   │  // Manual Application setup  │  // Automatic!                 │  │
│   │  class MyApp : Application() {│                                │  │
│   │    lateinit var component     │                                │  │
│   │    override fun onCreate() {  │                                │  │
│   │      component = Dagger...    │                                │  │
│   │        .factory()             │                                │  │
│   │        .create(this)          │                                │  │
│   │    }                          │                                │  │
│   │  }                            │                                │  │
│   │                                │                                │  │
│   ├────────────────────────────────┼────────────────────────────────┤  │
│   │                                │                                │  │
│   │  // Manual Activity inject    │  // Just one annotation!       │  │
│   │  class MainActivity {          │  @AndroidEntryPoint            │  │
│   │    override fun onCreate() {  │  class MainActivity {          │  │
│   │      (app as MyApp)           │    @Inject                     │  │
│   │        .component             │    lateinit var repo: Repo     │  │
│   │        .inject(this)          │                                │  │
│   │      super.onCreate()         │    // Injection is automatic!  │  │
│   │    }                          │  }                             │  │
│   │  }                            │                                │  │
│   │                                │                                │  │
│   ├────────────────────────────────┼────────────────────────────────┤  │
│   │                                │                                │  │
│   │  // ViewModel factory needed  │  // Built-in support!          │  │
│   │  class VMFactory(            │  @HiltViewModel                │  │
│   │    val repo: Repo            │  class MyViewModel @Inject     │  │
│   │  ) : ViewModelProvider.Fac.. {│    constructor(...)            │  │
│   │    override fun create()...  │                                │  │
│   │  }                            │  // In Activity/Fragment:      │  │
│   │                                │  val vm: MyViewModel           │  │
│   │  // In Activity:              │    by viewModels()            │  │
│   │  ViewModelProvider(this,     │                                │  │
│   │    VMFactory(repo)).get(..) │                                │  │
│   │                                │                                │  │
│   └────────────────────────────────┴────────────────────────────────┘  │
│                                                                         │
│   BOTTOM LINE: Hilt removes ~50% of Dagger boilerplate                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Setup

```kotlin
// build.gradle.kts (project level)
plugins {
    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false  // For KSP
}

// build.gradle.kts (app level)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")  // Recommended over kapt
    // OR use kapt for older setup:
    // kotlin("kapt")
}

android {
    // ...
}

dependencies {
    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-android-compiler:2.51")
    // OR with kapt: kapt("com.google.dagger:hilt-android-compiler:2.51")
    
    // Hilt ViewModel integration
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")  // For Compose
    
    // Hilt Work integration (for WorkManager)
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    
    // Testing
    testImplementation("com.google.dagger:hilt-android-testing:2.51")
    kspTest("com.google.dagger:hilt-android-compiler:2.51")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.51")
}

// If using kapt (deprecated but still works)
// kapt {
//     correctErrorTypes = true
// }
```

### Hilt Component Hierarchy

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    HILT COMPONENT HIERARCHY                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   AUTOMATIC COMPONENT GENERATION - Hilt creates these for you!        │
│                                                                         │
│   ┌───────────────────────────────────────────────────────────────────┐│
│   │                    SingletonComponent                             ││
│   │                      @Singleton                                   ││
│   │                 (Application lifetime)                            ││
│   │                                                                   ││
│   │  BINDINGS AVAILABLE:                                              ││
│   │  • @ApplicationContext Application                                ││
│   │  • @ApplicationContext Context                                    ││
│   │  • Application                                                    ││
│   └───────────────────────────────┬───────────────────────────────────┘│
│                                   │                                     │
│         ┌─────────────────────────┼─────────────────────────┐          │
│         │                         │                         │          │
│         ▼                         ▼                         ▼          │
│ ┌───────────────────┐   ┌───────────────────┐   ┌───────────────────┐ │
│ │ActivityRetained   │   │ ViewModelComponent │   │ ServiceComponent  │ │
│ │   Component       │   │  @ViewModelScoped  │   │  @ServiceScoped   │ │
│ │@ActivityRetained  │   │                    │   │                    │ │
│ │   Scoped          │   │ BINDINGS:          │   │ BINDINGS:          │ │
│ │                   │   │ • SavedStateHandle │   │ • Service          │ │
│ │ Survives config   │   │ • ViewModelLifec.. │   │                    │ │
│ │ changes!          │   │                    │   │                    │ │
│ └─────────┬─────────┘   └────────────────────┘   └────────────────────┘ │
│           │                                                             │
│           ▼                                                             │
│ ┌───────────────────┐                                                  │
│ │ActivityComponent  │                                                  │
│ │ @ActivityScoped   │                                                  │
│ │                   │                                                  │
│ │ BINDINGS:          │                                                  │
│ │ • Activity         │                                                  │
│ │ • @ActivityContext│                                                  │
│ │ • FragmentManager │                                                  │
│ └─────────┬─────────┘                                                  │
│           │                                                             │
│           ▼                                                             │
│ ┌───────────────────┐                                                  │
│ │FragmentComponent  │                                                  │
│ │ @FragmentScoped   │                                                  │
│ │                   │                                                  │
│ │ BINDINGS:          │                                                  │
│ │ • Fragment         │                                                  │
│ └─────────┬─────────┘                                                  │
│           │                                                             │
│           ▼                                                             │
│ ┌───────────────────┐                                                  │
│ │  ViewComponent    │                                                  │
│ │   @ViewScoped     │                                                  │
│ │                   │                                                  │
│ │ BINDINGS:          │                                                  │
│ │ • View             │                                                  │
│ └───────────────────┘                                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    SCOPE SELECTION GUIDE                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌────────────────────────┬─────────────────────┬────────────────────┐│
│   │ Scope                  │ Component           │ Use For            ││
│   ├────────────────────────┼─────────────────────┼────────────────────┤│
│   │ @Singleton             │ SingletonComponent  │ Network, Database, ││
│   │                        │ (App lifetime)      │ Shared repositories││
│   ├────────────────────────┼─────────────────────┼────────────────────┤│
│   │ @ActivityRetainedScoped│ ActivityRetained    │ Data surviving     ││
│   │                        │ Component           │ rotation           ││
│   ├────────────────────────┼─────────────────────┼────────────────────┤│
│   │ @ViewModelScoped       │ ViewModelComponent  │ ViewModel-specific ││
│   │                        │ (ViewModel life)    │ helpers            ││
│   ├────────────────────────┼─────────────────────┼────────────────────┤│
│   │ @ActivityScoped        │ ActivityComponent   │ Activity-specific  ││
│   │                        │ (Activity life)     │ presenters, navs   ││
│   ├────────────────────────┼─────────────────────┼────────────────────┤│
│   │ @FragmentScoped        │ FragmentComponent   │ Fragment-specific  ││
│   │                        │ (Fragment life)     │ adapters, handlers ││
│   ├────────────────────────┼─────────────────────┼────────────────────┤│
│   │ No scope (Unscoped)    │ New each injection  │ Cheap objects,     ││
│   │                        │                     │ stateless helpers  ││
│   └────────────────────────┴─────────────────────┴────────────────────┘│
│                                                                         │
│   RULE OF THUMB:                                                        │
│   • If expensive to create or needs sharing → Add a scope             │
│   • If cheap and stateless → Leave unscoped (new instance each time)  │
│   • When in doubt, start unscoped and add scope if needed             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Complete Hilt Example

```kotlin
// ═══════════════════════════════════════════════════════════════
// STEP 1: Application Class (@HiltAndroidApp)
// ═══════════════════════════════════════════════════════════════

// This single annotation replaces all the manual Dagger component setup!
@HiltAndroidApp
class MyApplication : Application() {
    // Hilt generates the component automatically
    // No need for lateinit var component
}

// ═══════════════════════════════════════════════════════════════
// STEP 2: Create Modules
// ═══════════════════════════════════════════════════════════════

@Module
@InstallIn(SingletonComponent::class)  // Lives for entire app
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context  // Built-in qualifier
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        // Not scoped - UserDao is cheap to get
        return database.userDao()
    }
}

// Abstract module with @Binds for interface binding
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}

// Dispatcher module for coroutines
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    
    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

// ═══════════════════════════════════════════════════════════════
// STEP 3: Create Repository
// ═══════════════════════════════════════════════════════════════

// Repository with @Inject constructor
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {
    
    override suspend fun getUsers(): Result<List<User>> = withContext(ioDispatcher) {
        runCatching {
            try {
                // Try network first
                val users = apiService.getUsers()
                userDao.insertAll(users)
                users
            } catch (e: IOException) {
                // Fall back to cache on network error
                userDao.getAllUsers()
            }
        }
    }
    
    override suspend fun getUser(id: String): Result<User> = withContext(ioDispatcher) {
        runCatching {
            userDao.getUser(id) ?: apiService.getUser(id).also {
                userDao.insert(it)
            }
        }
    }
    
    override suspend fun refreshUsers(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val users = apiService.getUsers()
            userDao.deleteAllAndInsert(users)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// STEP 4: Create ViewModel
// ═══════════════════════════════════════════════════════════════

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
    private val savedStateHandle: SavedStateHandle  // Automatically injected
) : ViewModel() {
    
    // State
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    // Get navigation argument
    private val userId: String? = savedStateHandle.get<String>("userId")
    
    init {
        loadUsers()
    }
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.getUsers()
                .onSuccess { users ->
                    _uiState.update { 
                        it.copy(isLoading = false, users = users) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = error.message) 
                    }
                }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            repository.refreshUsers()
                .onSuccess {
                    loadUsers()
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(isRefreshing = false, error = error.message) 
                    }
                }
        }
    }
}

data class UserUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null
)

// ═══════════════════════════════════════════════════════════════
// STEP 5: Use in Activities and Fragments
// ═══════════════════════════════════════════════════════════════

@AndroidEntryPoint  // Enables injection for this Activity
class MainActivity : AppCompatActivity() {
    
    // ViewModel injection - so simple with Hilt!
    private val viewModel: UserViewModel by viewModels()
    
    // Field injection for other dependencies
    @Inject
    lateinit var analytics: AnalyticsService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // analytics is injected and ready
        analytics.logScreenView("MainActivity")
        
        // Observe ViewModel state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }
    
    private fun updateUI(state: UserUiState) {
        // Update your UI based on state
    }
}

@AndroidEntryPoint
class UserFragment : Fragment(R.layout.fragment_user) {
    
    // Fragment-scoped ViewModel
    private val viewModel: UserDetailViewModel by viewModels()
    
    // Share ViewModel with Activity
    private val sharedViewModel: SharedViewModel by activityViewModels()
    
    // Field injection
    @Inject
    lateinit var imageLoader: ImageLoader
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { user ->
                        // Update UI
                    }
                }
                launch {
                    sharedViewModel.sharedData.collect { data ->
                        // React to shared data
                    }
                }
            }
        }
    }
}
```

### Hilt with Jetpack Compose

```kotlin
// ═══════════════════════════════════════════════════════════════
// COMPOSE ENTRY POINT
// ═══════════════════════════════════════════════════════════════

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme {
                AppNavigation()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// NAVIGATION WITH HILT
// ═══════════════════════════════════════════════════════════════

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "users"
    ) {
        composable("users") {
            UserListScreen(
                onUserClick = { userId ->
                    navController.navigate("user/$userId")
                }
            )
        }
        
        composable(
            route = "user/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            UserDetailScreen()
            // userId is automatically passed to ViewModel via SavedStateHandle
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// COMPOSE SCREENS WITH HILT
// ═══════════════════════════════════════════════════════════════

@Composable
fun UserListScreen(
    onUserClick: (String) -> Unit,
    viewModel: UserViewModel = hiltViewModel()  // Hilt Compose integration
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    UserListContent(
        uiState = uiState,
        onUserClick = onUserClick,
        onRefresh = viewModel::refresh
    )
}

@Composable
fun UserListContent(
    uiState: UserUiState,
    onUserClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(uiState.isRefreshing),
        onRefresh = onRefresh
    ) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorMessage(message = uiState.error, onRetry = onRefresh)
            }
            else -> {
                LazyColumn {
                    items(uiState.users, key = { it.id }) { user ->
                        UserItem(
                            user = user,
                            onClick = { onUserClick(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserDetailScreen(
    viewModel: UserDetailViewModel = hiltViewModel()
    // savedStateHandle automatically gets userId from navigation argument
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    
    user?.let {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = it.avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = it.email,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
```

### Entry Points - Inject into Non-Hilt Classes

For classes that Hilt doesn't support directly (ContentProvider, third-party libraries):

```kotlin
// ═══════════════════════════════════════════════════════════════
// ENTRY POINT DEFINITION
// ═══════════════════════════════════════════════════════════════

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AnalyticsEntryPoint {
    fun analyticsService(): AnalyticsService
    fun userRepository(): UserRepository
}

// ═══════════════════════════════════════════════════════════════
// USAGE IN CONTENT PROVIDER
// ═══════════════════════════════════════════════════════════════

class MyContentProvider : ContentProvider() {
    
    override fun onCreate(): Boolean {
        val entryPoint = EntryPointAccessors.fromApplication(
            context!!.applicationContext,
            AnalyticsEntryPoint::class.java
        )
        val analyticsService = entryPoint.analyticsService()
        analyticsService.logEvent("ContentProvider_Created")
        return true
    }
    
    // ... other ContentProvider methods
}

// ═══════════════════════════════════════════════════════════════
// HILT WORKER FOR WORKMANAGER
// ═══════════════════════════════════════════════════════════════

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userRepository: UserRepository,  // Injected
    private val analytics: AnalyticsService       // Injected
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            userRepository.refreshUsers()
            analytics.logEvent("sync_success")
            Result.success()
        } catch (e: Exception) {
            analytics.logError("sync_failed", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}

// Don't forget to configure WorkManager with HiltWorkerFactory
@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

### Assisted Injection

When you need to combine DI-provided dependencies with runtime parameters:

```kotlin
// ═══════════════════════════════════════════════════════════════
// ASSISTED INJECTION
// ═══════════════════════════════════════════════════════════════

// Define factory interface
@AssistedFactory
interface UserDetailViewModelFactory {
    fun create(userId: String): UserDetailViewModel
}

// ViewModel with assisted injection
class UserDetailViewModel @AssistedInject constructor(
    private val repository: UserRepository,           // Injected by Hilt
    private val analytics: AnalyticsService,          // Injected by Hilt
    @Assisted private val userId: String              // Provided at runtime
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    init {
        loadUser()
    }
    
    private fun loadUser() {
        viewModelScope.launch {
            repository.getUser(userId)
                .onSuccess { _user.value = it }
        }
    }
}

// Usage in Fragment
@AndroidEntryPoint
class UserDetailFragment : Fragment() {
    
    @Inject
    lateinit var factory: UserDetailViewModelFactory
    
    private val viewModel: UserDetailViewModel by viewModels {
        val userId = arguments?.getString("userId") ?: ""
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(userId) as T
            }
        }
    }
}
```

---

## 8. Koin (Bonus)

Koin is a lightweight DI framework using pure Kotlin DSL - no annotation processing or code generation.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          KOIN OVERVIEW                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   KEY CHARACTERISTICS:                                                  │
│   • Pure Kotlin DSL - no annotations needed                           │
│   • No code generation - faster build times                            │
│   • Runtime resolution - errors detected at runtime                    │
│   • Lightweight - small library footprint                              │
│   • Kotlin Multiplatform (KMM) support                                │
│   • Simple to learn and use                                            │
│                                                                         │
│   TRADE-OFFS:                                                           │
│   ✓ Faster builds (no code generation)                                 │
│   ✓ Easier to debug (plain Kotlin)                                     │
│   ✓ Quick setup and iteration                                          │
│   ✗ Runtime errors instead of compile-time                            │
│   ✗ Slightly slower resolution at runtime                             │
│   ✗ Harder to trace dependency issues                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Setup

```kotlin
// build.gradle.kts (app)
dependencies {
    // Core Koin
    implementation("io.insert-koin:koin-android:3.5.3")
    
    // Koin for Jetpack Compose
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")
    
    // Koin for Jetpack Compose Navigation
    implementation("io.insert-koin:koin-androidx-compose-navigation:3.5.3")
    
    // Koin for WorkManager
    implementation("io.insert-koin:koin-androidx-workmanager:3.5.3")
    
    // Testing
    testImplementation("io.insert-koin:koin-test:3.5.3")
    testImplementation("io.insert-koin:koin-test-junit4:3.5.3")
}
```

### Complete Koin Example

```kotlin
// ═══════════════════════════════════════════════════════════════
// MODULE DEFINITIONS - Pure Kotlin DSL
// ═══════════════════════════════════════════════════════════════

// Network Module
val networkModule = module {
    // single = singleton (one instance)
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    single {
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create()
    }
    
    single {
        Retrofit.Builder()
            .baseUrl("https://api.example.com/v1/")
            .client(get())  // get() resolves OkHttpClient
            .addConverterFactory(GsonConverterFactory.create(get()))  // get() resolves Gson
            .build()
    }
    
    single<ApiService> { 
        get<Retrofit>().create(ApiService::class.java) 
    }
}

// Database Module
val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),  // Built-in Koin function
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    // factory = new instance each time
    factory { get<AppDatabase>().userDao() }
}

// Repository Module
val repositoryModule = module {
    // bind interface to implementation
    single<UserRepository> { 
        UserRepositoryImpl(
            api = get(),
            dao = get(),
            dispatcher = Dispatchers.IO
        ) 
    }
    
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}

// ViewModel Module
val viewModelModule = module {
    // viewModel delegate - scoped to ViewModelStore
    viewModel { UserListViewModel(get()) }
    
    // With parameters - passed at runtime
    viewModel { params -> 
        UserDetailViewModel(
            userId = params.get<String>(),
            repository = get()
        )
    }
    
    viewModel { LoginViewModel(get(), get()) }
}

// All modules combined
val appModules = listOf(
    networkModule,
    databaseModule,
    repositoryModule,
    viewModelModule
)

// ═══════════════════════════════════════════════════════════════
// APPLICATION SETUP
// ═══════════════════════════════════════════════════════════════

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            // Provide Android context
            androidContext(this@MyApplication)
            
            // Log Koin activity
            androidLogger(Level.DEBUG)
            
            // Load modules
            modules(appModules)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// USAGE IN ACTIVITIES AND FRAGMENTS
// ═══════════════════════════════════════════════════════════════

class MainActivity : AppCompatActivity() {
    
    // Lazy inject ViewModel
    private val viewModel: UserListViewModel by viewModel()
    
    // Direct injection
    private val repository: UserRepository by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        lifecycleScope.launch {
            viewModel.users.collect { users ->
                // Update UI
            }
        }
    }
}

class UserDetailFragment : Fragment() {
    
    // Pass parameter to ViewModel
    private val viewModel: UserDetailViewModel by viewModel {
        val userId = arguments?.getString("userId") ?: ""
        parametersOf(userId)
    }
    
    // Share ViewModel with Activity
    private val sharedViewModel: SharedViewModel by sharedViewModel()
}

// ═══════════════════════════════════════════════════════════════
// USAGE IN COMPOSE
// ═══════════════════════════════════════════════════════════════

@Composable
fun UserListScreen(
    onUserClick: (String) -> Unit,
    viewModel: UserListViewModel = koinViewModel()
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    
    LazyColumn {
        items(users, key = { it.id }) { user ->
            UserItem(user = user, onClick = { onUserClick(user.id) })
        }
    }
}

@Composable
fun UserDetailScreen(
    userId: String,
    viewModel: UserDetailViewModel = koinViewModel { parametersOf(userId) }
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    
    user?.let {
        UserDetailContent(user = it)
    }
}

// ═══════════════════════════════════════════════════════════════
// SCOPES IN KOIN
// ═══════════════════════════════════════════════════════════════

// Define a custom scope
val activityScopedModule = module {
    scope<MainActivity> {
        scoped { MainPresenter(get()) }
        scoped { MainNavigator(get()) }
    }
}

// Use in Activity
class MainActivity : AppCompatActivity(), KoinScopeComponent {
    
    // Create scope tied to this Activity
    override val scope: Scope by activityScope()
    
    // Gets from this Activity's scope
    private val presenter: MainPresenter by inject()
    
    override fun onDestroy() {
        super.onDestroy()
        // Scope is automatically closed
    }
}
```

---

## 9. Advanced Topics

### Multi-Module Project Setup with Hilt

```
┌─────────────────────────────────────────────────────────────────────────┐
│               MULTI-MODULE PROJECT WITH HILT                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                          :app                                   │  │
│   │                      @HiltAndroidApp                            │  │
│   │                                                                 │  │
│   │  • Entry point for Hilt                                        │  │
│   │  • Can install modules from all dependencies                   │  │
│   └──────────────────────────┬──────────────────────────────────────┘  │
│                              │                                          │
│              ┌───────────────┼───────────────┐                         │
│              │               │               │                         │
│              ▼               ▼               ▼                         │
│   ┌───────────────┐ ┌───────────────┐ ┌───────────────┐               │
│   │:feature:users │ │:feature:auth  │ │:feature:home  │               │
│   │               │ │               │ │               │               │
│   │ UserModule    │ │ AuthModule    │ │ HomeModule    │               │
│   │ UserViewModel │ │ LoginVM       │ │ HomeViewModel │               │
│   │ UserRepo      │ │ AuthRepo      │ │               │               │
│   └───────┬───────┘ └───────┬───────┘ └───────┬───────┘               │
│           │                 │                 │                        │
│           └────────────┬────┴─────────────────┘                        │
│                        │                                                │
│                        ▼                                                │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                         :core                                   │  │
│   │                                                                 │  │
│   │  NetworkModule, DatabaseModule, CommonModule                   │  │
│   │  Base classes, utilities, shared dependencies                  │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// :core/build.gradle.kts
plugins {
    id("com.android.library")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

// :core/di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit { ... }
}

// :feature:users/build.gradle.kts
plugins {
    id("com.android.library")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":core"))
}

// :feature:users/di/UserModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {
    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}

// :app/build.gradle.kts
dependencies {
    implementation(project(":core"))
    implementation(project(":feature:users"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:home"))
}
```

### Custom Scopes

```kotlin
// ═══════════════════════════════════════════════════════════════
// CUSTOM SCOPE - PER USER SESSION
// ═══════════════════════════════════════════════════════════════

// Define the scope annotation
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class UserSessionScope

// Define the component that will have this scope
@UserSessionScope
@DefineComponent(parent = SingletonComponent::class)
interface UserSessionComponent

// Define interface for building the component
@DefineComponent.Builder
interface UserSessionComponentBuilder {
    fun setUser(@BindsInstance user: User): UserSessionComponentBuilder
    fun build(): UserSessionComponent
}

// Module scoped to the user session
@Module
@InstallIn(UserSessionComponent::class)
object UserSessionModule {
    
    @Provides
    @UserSessionScope
    fun provideUserPreferences(user: User): UserPreferences {
        return UserPreferences(user.id)
    }
    
    @Provides
    @UserSessionScope
    fun provideUserAnalytics(user: User): UserAnalytics {
        return UserAnalytics(userId = user.id)
    }
}

// Manager to control the scope lifecycle
@Singleton
class UserSessionManager @Inject constructor(
    private val componentBuilder: Provider<UserSessionComponentBuilder>
) {
    private var userSessionComponent: UserSessionComponent? = null
    
    fun startSession(user: User) {
        userSessionComponent = componentBuilder.get()
            .setUser(user)
            .build()
    }
    
    fun endSession() {
        userSessionComponent = null
    }
    
    fun getComponent(): UserSessionComponent? = userSessionComponent
}

// Entry point to access scoped dependencies
@EntryPoint
@InstallIn(UserSessionComponent::class)
interface UserSessionEntryPoint {
    fun userPreferences(): UserPreferences
    fun userAnalytics(): UserAnalytics
}
```

---

## 10. Testing with DI

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      TESTING STRATEGIES WITH DI                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ UNIT TESTING (No DI Framework Needed)                           │  │
│   │                                                                 │  │
│   │ • Test classes in isolation                                     │  │
│   │ • Pass fake/mock dependencies through constructor              │  │
│   │ • Fast execution                                                │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ INTEGRATION TESTING (With DI Framework)                         │  │
│   │                                                                 │  │
│   │ • Test multiple components working together                    │  │
│   │ • Replace specific modules with test modules                   │  │
│   │ • @UninstallModules + @BindValue (Hilt)                        │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │ END-TO-END TESTING                                              │  │
│   │                                                                 │  │
│   │ • Test full app flow                                           │  │
│   │ • May use mock server                                          │  │
│   │ • @HiltAndroidTest                                             │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Unit Testing (Constructor Injection)

```kotlin
// ═══════════════════════════════════════════════════════════════
// UNIT TEST - No DI framework needed!
// ═══════════════════════════════════════════════════════════════

class UserViewModelTest {
    
    // Create fakes manually
    private lateinit var fakeRepository: FakeUserRepository
    private lateinit var viewModel: UserViewModel
    
    @Before
    fun setup() {
        fakeRepository = FakeUserRepository()
        // Simply inject the fake through constructor
        viewModel = UserViewModel(fakeRepository, SavedStateHandle())
    }
    
    @Test
    fun `loadUsers should update state with users`() = runTest {
        // Given
        val expectedUsers = listOf(
            User("1", "John", "john@email.com"),
            User("2", "Jane", "jane@email.com")
        )
        fakeRepository.setUsers(expectedUsers)
        
        // When
        viewModel.loadUsers()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(expectedUsers, state.users)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `loadUsers should handle error`() = runTest {
        // Given
        fakeRepository.setShouldFail(true)
        
        // When
        viewModel.loadUsers()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.users.isEmpty())
        assertNotNull(state.error)
    }
}

// Fake implementation for testing
class FakeUserRepository : UserRepository {
    
    private var users = emptyList<User>()
    private var shouldFail = false
    
    fun setUsers(users: List<User>) {
        this.users = users
    }
    
    fun setShouldFail(shouldFail: Boolean) {
        this.shouldFail = shouldFail
    }
    
    override suspend fun getUsers(): Result<List<User>> {
        return if (shouldFail) {
            Result.failure(Exception("Network error"))
        } else {
            Result.success(users)
        }
    }
    
    override suspend fun getUser(id: String): Result<User> {
        return users.find { it.id == id }
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("User not found"))
    }
    
    override suspend fun refreshUsers(): Result<Unit> {
        return if (shouldFail) {
            Result.failure(Exception("Network error"))
        } else {
            Result.success(Unit)
        }
    }
}
```

### Integration Testing with Hilt

```kotlin
// ═══════════════════════════════════════════════════════════════
// HILT TESTING SETUP
// ═══════════════════════════════════════════════════════════════

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UserRepositoryIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var repository: UserRepository
    
    @BindValue
    val fakeApiService: ApiService = FakeApiService()
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun `getUsers should return cached data when api fails`() = runTest {
        // Given - API will fail
        (fakeApiService as FakeApiService).setShouldFail(true)
        
        // When
        val result = repository.getUsers()
        
        // Then - Should return cached data
        assertTrue(result.isSuccess)
    }
}

// ═══════════════════════════════════════════════════════════════
// REPLACE ENTIRE MODULE IN TESTS
// ═══════════════════════════════════════════════════════════════

@UninstallModules(NetworkModule::class)  // Remove production module
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FullIntegrationTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun `user list should display users from mock api`() {
        // UI test with mocked backend
        onView(withId(R.id.user_list))
            .check(matches(isDisplayed()))
        
        onView(withText("John Doe"))
            .check(matches(isDisplayed()))
    }
}

// Test module replacing production module
@Module
@InstallIn(SingletonComponent::class)
object TestNetworkModule {
    
    @Provides
    @Singleton
    fun provideFakeApiService(): ApiService = FakeApiService().apply {
        setUsers(listOf(
            User("1", "John Doe", "john@test.com")
        ))
    }
}

// ═══════════════════════════════════════════════════════════════
// COMPOSE UI TESTING WITH HILT
// ═══════════════════════════════════════════════════════════════

@HiltAndroidTest
class UserListScreenTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()
    
    @BindValue
    val fakeRepository: UserRepository = FakeUserRepository().apply {
        setUsers(listOf(
            User("1", "Test User", "test@email.com")
        ))
    }
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun userListDisplaysUsers() {
        composeRule.setContent {
            UserListScreen(onUserClick = {})
        }
        
        composeRule.onNodeWithText("Test User")
            .assertIsDisplayed()
    }
}
```

---

## 11. Comparison and Best Practices

### Framework Comparison

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    DI FRAMEWORK COMPARISON                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌────────────────────┬─────────────┬─────────────┬─────────────────┐  │
│  │ Feature            │  Dagger 2   │    Hilt     │      Koin       │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ Type Safety        │ Compile-time│ Compile-time│ Runtime         │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ Error Detection    │ At compile  │ At compile  │ At runtime      │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ Learning Curve     │ Steep       │ Moderate    │ Easy            │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ Boilerplate        │ High        │ Medium      │ Low             │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ Build Time Impact  │ Moderate    │ Moderate    │ None            │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ Runtime Performance│ Excellent   │ Excellent   │ Good            │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ Android Integration│ Manual      │ Automatic   │ Manual          │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ ViewModel Support  │ Manual      │ Built-in    │ Built-in        │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ KMM Support        │ Limited     │ No          │ Full            │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ Testing Support    │ Good        │ Excellent   │ Good            │  │
│  ├────────────────────┼─────────────┼─────────────┼─────────────────┤  │
│  │ Google Support     │ Yes         │ Yes         │ Community       │  │
│  └────────────────────┴─────────────┴─────────────┴─────────────────┘  │
│                                                                         │
│  RECOMMENDATIONS:                                                       │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ • New Android projects     → HILT (Google recommended)         │   │
│  │ • Legacy Dagger projects   → Continue with Dagger or migrate   │   │
│  │ • Rapid prototyping        → Koin (fastest setup)              │   │
│  │ • Kotlin Multiplatform     → Koin (KMM support)                │   │
│  │ • Complex enterprise apps  → Dagger 2 (most flexible)          │   │
│  │ • Learning DI concepts     → Manual DI or Koin first           │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Best Practices

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         DI BEST PRACTICES                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ 1. CONSTRUCTOR INJECTION FIRST                                    ║ │
│  ╠═══════════════════════════════════════════════════════════════════╣ │
│  ║                                                                   ║ │
│  ║   ✅ PREFERRED:                                                   ║ │
│  ║   class UserRepository @Inject constructor(                      ║ │
│  ║       private val api: ApiService,                               ║ │
│  ║       private val dao: UserDao                                   ║ │
│  ║   )                                                               ║ │
│  ║                                                                   ║ │
│  ║   ❌ AVOID (unless necessary):                                   ║ │
│  ║   class UserRepository {                                         ║ │
│  ║       @Inject lateinit var api: ApiService                       ║ │
│  ║   }                                                               ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ 2. DEPEND ON ABSTRACTIONS (INTERFACES)                           ║ │
│  ╠═══════════════════════════════════════════════════════════════════╣ │
│  ║                                                                   ║ │
│  ║   ✅ interface UserRepository { }                                 ║ │
│  ║      class UserRepositoryImpl @Inject constructor() : UserRepo   ║ │
│  ║      class ViewModel(val repo: UserRepository) // Interface!     ║ │
│  ║                                                                   ║ │
│  ║   ❌ class ViewModel(val repo: UserRepositoryImpl) // Concrete! ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ 3. SCOPE APPROPRIATELY                                           ║ │
│  ╠═══════════════════════════════════════════════════════════════════╣ │
│  ║                                                                   ║ │
│  ║   @Singleton   → Expensive to create (OkHttp, Room, Retrofit)    ║ │
│  ║   Unscoped     → Cheap, stateless (mappers, validators)         ║ │
│  ║                                                                   ║ │
│  ║   ⚠️ Don't make everything @Singleton!                          ║ │
│  ║      Over-scoping leads to memory issues                         ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ 4. USE @Binds OVER @Provides FOR INTERFACES                      ║ │
│  ╠═══════════════════════════════════════════════════════════════════╣ │
│  ║                                                                   ║ │
│  ║   ✅ EFFICIENT:                                                   ║ │
│  ║   @Binds abstract fun bind(impl: RepoImpl): Repository          ║ │
│  ║                                                                   ║ │
│  ║   ❌ LESS EFFICIENT:                                             ║ │
│  ║   @Provides fun provide(impl: RepoImpl): Repository = impl      ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ 5. ORGANIZE MODULES BY LAYER/FEATURE                             ║ │
│  ╠═══════════════════════════════════════════════════════════════════╣ │
│  ║                                                                   ║ │
│  ║   BY LAYER:                BY FEATURE:                           ║ │
│  ║   NetworkModule            UserModule                            ║ │
│  ║   DatabaseModule           AuthModule                            ║ │
│  ║   RepositoryModule         SettingsModule                        ║ │
│  ║   ViewModelModule          HomeModule                            ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
│  ╔═══════════════════════════════════════════════════════════════════╗ │
│  ║ 6. AVOID CONTEXT LEAKS                                           ║ │
│  ╠═══════════════════════════════════════════════════════════════════╣ │
│  ║                                                                   ║ │
│  ║   ✅ @ApplicationContext context: Context  // Safe in Singleton  ║ │
│  ║   ✅ @ActivityContext context: Context     // For Activity scope ║ │
│  ║                                                                   ║ │
│  ║   ❌ Don't inject Activity/Fragment directly into Singletons    ║ │
│  ║                                                                   ║ │
│  ╚═══════════════════════════════════════════════════════════════════╝ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Quick Reference Cheatsheet

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      HILT QUICK REFERENCE                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ANNOTATIONS:                                                           │
│  ─────────────────────────────────────────────────────────────────────  │
│  @HiltAndroidApp      → Application class (required, once)             │
│  @AndroidEntryPoint   → Activity, Fragment, Service, View              │
│  @HiltViewModel       → ViewModel class                                │
│  @Inject constructor  → Request/enable DI for a class                  │
│  @Inject field        → Field injection (lateinit var)                 │
│  @Module              → Container for @Provides/@Binds                 │
│  @InstallIn(...)      → Which component the module belongs to          │
│  @Provides            → Return instance of a type                      │
│  @Binds               → Bind interface to impl (abstract method)       │
│  @Singleton           → One instance per app                           │
│                                                                         │
│  BUILT-IN QUALIFIERS:                                                   │
│  ─────────────────────────────────────────────────────────────────────  │
│  @ApplicationContext  → Application Context                            │
│  @ActivityContext     → Activity Context                               │
│                                                                         │
│  COMPONENTS & SCOPES:                                                   │
│  ─────────────────────────────────────────────────────────────────────  │
│  SingletonComponent          │ @Singleton          │ Application       │
│  ActivityRetainedComponent   │ @ActivityRetained   │ ViewModel         │
│  ViewModelComponent          │ @ViewModelScoped    │ ViewModel         │
│  ActivityComponent           │ @ActivityScoped     │ Activity          │
│  FragmentComponent           │ @FragmentScoped     │ Fragment          │
│  ViewComponent               │ @ViewScoped         │ View              │
│  ServiceComponent            │ @ServiceScoped      │ Service           │
│                                                                         │
│  COMMON PATTERNS:                                                       │
│  ─────────────────────────────────────────────────────────────────────  │
│                                                                         │
│  // ViewModel in Compose                                                │
│  viewModel: MyViewModel = hiltViewModel()                               │
│                                                                         │
│  // ViewModel in Fragment/Activity                                      │
│  private val viewModel: MyViewModel by viewModels()                     │
│  private val sharedVM: SharedVM by activityViewModels()                │
│                                                                         │
│  // Module structure                                                    │
│  @Module                                                                │
│  @InstallIn(SingletonComponent::class)                                 │
│  object NetworkModule {                                                 │
│      @Provides @Singleton                                               │
│      fun provideOkHttp(): OkHttpClient = OkHttpClient()                │
│  }                                                                      │
│                                                                         │
│  @Module                                                                │
│  @InstallIn(SingletonComponent::class)                                 │
│  abstract class RepoModule {                                            │
│      @Binds @Singleton                                                  │
│      abstract fun bind(impl: RepoImpl): Repository                     │
│  }                                                                      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

**End of Android Dependency Injection Guide**

Created for learning purposes. For the latest updates and best practices, refer to:
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Dagger Documentation](https://dagger.dev/)
- [Android Developer Guide - DI](https://developer.android.com/training/dependency-injection)
│  3. USE APPROPRIATE SCOPES                                      │
│     @Singleton  → Network clients, databases                    │
│     @ViewModelScoped → ViewModel dependencies                   │
│     No scope → Lightweight objects, new instance each time      │
│                                                                 │
│  4. AVOID SCOPE CREEP                                           │
│     ❌ Don't make everything @Singleton                         │
│     ✅ Only scope what needs to survive configuration changes   │
│                                                                 │
│  5. ORGANIZE MODULES BY FEATURE                                 │
│     NetworkModule, DatabaseModule, UserModule, AuthModule       │
│                                                                 │
│  6. USE @Binds OVER @Provides FOR INTERFACES                    │
│     @Binds is more efficient (no method call overhead)          │
│                                                                 │
│  7. AVOID INJECTING CONTEXT DIRECTLY                            │
│     ✅ Use @ApplicationContext or @ActivityContext              │
│     ❌ Don't pass raw Context through constructors              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Testing with DI

```kotlin
// Test with Hilt
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UserViewModelTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var repository: UserRepository
    
    @BindValue
    val fakeApiService: ApiService = FakeApiService()
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun loadUsers_returnsUsers() = runTest {
        val viewModel = UserViewModel(repository)
        
        viewModel.loadUsers()
        
        assertEquals(3, viewModel.users.value.size)
    }
}

// Replace module in tests
@Module
@InstallIn(SingletonComponent::class)
object TestNetworkModule {
    
    @Provides
    @Singleton
    fun provideFakeApiService(): ApiService = FakeApiService()
}

// Uninstall real module
@UninstallModules(NetworkModule::class)
@HiltAndroidTest
class UserRepositoryTest {
    // Uses TestNetworkModule instead
}

// Unit test without Hilt
class UserViewModelTest {
    
    private lateinit var viewModel: UserViewModel
    private val fakeRepository = FakeUserRepository()
---

**End of Android Dependency Injection Guide**

Created for learning purposes. For the latest updates and best practices, refer to:
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Dagger Documentation](https://dagger.dev/)
- [Android Developer Guide - DI](https://developer.android.com/training/dependency-injection)
