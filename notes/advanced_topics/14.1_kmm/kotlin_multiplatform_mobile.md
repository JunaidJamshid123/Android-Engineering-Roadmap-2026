# Kotlin Multiplatform Mobile (KMM)

## Overview

Kotlin Multiplatform Mobile (KMM) allows you to **share business logic** between Android and iOS while keeping platform-specific UI and APIs native. It's not "write once, run anywhere" — it's **write once, adapt where needed**.

```
┌─────────────────────────────────────────────────────────────┐
│                    KMM Project Structure                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐    │
│  │  Android App  │   │  Shared Code │   │   iOS App    │    │
│  │  (Kotlin/    │   │   (Common)   │   │  (Swift/     │    │
│  │   Compose)   │   │              │   │   SwiftUI)   │    │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘    │
│         │                  │                   │             │
│         │    ┌─────────────┴────────────┐      │             │
│         │    │      commonMain          │      │             │
│         │    │  ┌─────────────────────┐ │      │             │
│         │    │  │  Business Logic     │ │      │             │
│         │    │  │  Data Models        │ │      │             │
│         │    │  │  Use Cases          │ │      │             │
│         │    │  │  Repository (API)   │ │      │             │
│         │    │  │  Networking (Ktor)  │ │      │             │
│         │    │  │  Local DB (SQLDel.) │ │      │             │
│         │    │  └─────────────────────┘ │      │             │
│         │    └──────────────────────────┘      │             │
│         │         │              │              │             │
│    ┌────▼─────────▼──┐    ┌─────▼──────────────▼────┐       │
│    │   androidMain   │    │       iosMain            │       │
│    │  (JVM/Android   │    │   (Native/iOS impl.)     │       │
│    │   impl.)        │    │                          │       │
│    └─────────────────┘    └──────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

---

## Project Setup

### 1. Gradle Configuration (Root `build.gradle.kts`)

```kotlin
plugins {
    kotlin("multiplatform") version "1.9.22" apply false
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
}
```

### 2. Shared Module (`shared/build.gradle.kts`)

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
}

kotlin {
    // Android target
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    // iOS targets
    listOf(
        iosX64(),       // Simulator (Intel)
        iosArm64(),     // Device
        iosSimulatorArm64() // Simulator (Apple Silicon)
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Networking
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

                // Date/Time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:2.3.7")
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.3.7")
            }
        }

        // Link all iOS targets to iosMain
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

android {
    namespace = "com.example.shared"
    compileSdk = 34
    defaultConfig { minSdk = 24 }
}
```

---

## Shared Business Logic

### Data Models (commonMain)

```kotlin
// shared/src/commonMain/kotlin/com/example/shared/model/User.kt

@Serializable
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val avatarUrl: String? = null
)

@Serializable
data class ApiResponse<T>(
    val data: T,
    val success: Boolean,
    val message: String? = null
)
```

### Repository Layer (commonMain)

```kotlin
// shared/src/commonMain/kotlin/com/example/shared/repository/UserRepository.kt

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: Long): User
    suspend fun createUser(name: String, email: String): User
}

class UserRepositoryImpl(
    private val apiService: ApiService,
    private val localDb: UserDatabase
) : UserRepository {

    override suspend fun getUsers(): List<User> {
        return try {
            val users = apiService.fetchUsers()
            localDb.insertUsers(users) // Cache locally
            users
        } catch (e: Exception) {
            localDb.getAllUsers() // Return cached on failure
        }
    }

    override suspend fun getUserById(id: Long): User {
        return apiService.fetchUser(id)
    }

    override suspend fun createUser(name: String, email: String): User {
        return apiService.createUser(name, email)
    }
}
```

### Networking (commonMain using Ktor)

```kotlin
// shared/src/commonMain/kotlin/com/example/shared/network/ApiService.kt

class ApiService(engine: HttpClientEngine) {

    private val client = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(Logging) {
            level = LogLevel.BODY
        }
        defaultRequest {
            url("https://api.example.com/")
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun fetchUsers(): List<User> {
        return client.get("users").body()
    }

    suspend fun fetchUser(id: Long): User {
        return client.get("users/$id").body()
    }

    suspend fun createUser(name: String, email: String): User {
        return client.post("users") {
            setBody(mapOf("name" to name, "email" to email))
        }.body()
    }
}
```

### Use Cases / Business Logic (commonMain)

```kotlin
// shared/src/commonMain/kotlin/com/example/shared/usecase/GetUsersUseCase.kt

class GetUsersUseCase(private val repository: UserRepository) {

    suspend fun execute(): Result<List<User>> {
        return try {
            val users = repository.getUsers()
            if (users.isEmpty()) {
                Result.failure(NoUsersException())
            } else {
                Result.success(users)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class NoUsersException : Exception("No users found")
```

---

## expect/actual Declarations

The `expect`/`actual` mechanism lets you **declare** an API in common code and **implement** it differently per platform.

```
┌──────────────────────────────────────────────────────────┐
│                  expect/actual Flow                       │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  commonMain:                                              │
│  ┌──────────────────────────────────────┐                │
│  │  expect class PlatformLogger {       │                │
│  │      fun log(message: String)        │                │
│  │  }                                   │                │
│  │                                      │                │
│  │  expect fun getPlatformName(): String│                │
│  │                                      │                │
│  │  expect fun createUUID(): String     │                │
│  └──────────┬──────────────┬────────────┘                │
│             │              │                              │
│     ┌───────▼──────┐  ┌───▼──────────┐                   │
│     │ androidMain  │  │   iosMain    │                   │
│     │              │  │              │                   │
│     │ actual class │  │ actual class │                   │
│     │ PlatformLog..│  │ PlatformLog..│                   │
│     │ { Log.d(..) }│  │ { NSLog(..) }│                   │
│     │              │  │              │                   │
│     │ actual fun   │  │ actual fun   │                   │
│     │ getPlatform..│  │ getPlatform..│                   │
│     │ = "Android"  │  │ = "iOS"      │                   │
│     └──────────────┘  └──────────────┘                   │
└──────────────────────────────────────────────────────────┘
```

### Common Declaration (`commonMain`)

```kotlin
// shared/src/commonMain/kotlin/com/example/shared/platform/Platform.kt

// Expected function — no body, just signature
expect fun getPlatformName(): String

// Expected class
expect class PlatformLogger() {
    fun log(tag: String, message: String)
}

// Expected UUID generator
expect fun randomUUID(): String

// Expected key-value storage
expect class SecureStorage {
    fun putString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
    fun clear()
}
```

### Android Implementation (`androidMain`)

```kotlin
// shared/src/androidMain/kotlin/com/example/shared/platform/Platform.android.kt

import android.util.Log
import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

actual fun getPlatformName(): String = "Android ${android.os.Build.VERSION.SDK_INT}"

actual class PlatformLogger actual constructor() {
    actual fun log(tag: String, message: String) {
        Log.d(tag, message)
    }
}

actual fun randomUUID(): String = UUID.randomUUID().toString()

actual class SecureStorage(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    actual fun clear() {
        prefs.edit().clear().apply()
    }
}
```

### iOS Implementation (`iosMain`)

```kotlin
// shared/src/iosMain/kotlin/com/example/shared/platform/Platform.ios.kt

import platform.UIKit.UIDevice
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSLog

actual fun getPlatformName(): String =
    "${UIDevice.currentDevice.systemName()} ${UIDevice.currentDevice.systemVersion}"

actual class PlatformLogger actual constructor() {
    actual fun log(tag: String, message: String) {
        NSLog("[$tag] $message")
    }
}

actual fun randomUUID(): String = NSUUID().UUIDString()

actual class SecureStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    actual fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    actual fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }

    actual fun clear() {
        val dict = defaults.dictionaryRepresentation()
        dict.keys.forEach { defaults.removeObjectForKey(it as String) }
    }
}
```

---

## Platform-Specific Implementations

### Providing Platform Dependencies via DI

```kotlin
// shared/src/commonMain/kotlin/com/example/shared/di/CommonModule.kt

// Using a simple service locator pattern (or Koin)
class CommonModule(
    private val platformModule: PlatformModule
) {
    val apiService: ApiService by lazy {
        ApiService(platformModule.httpEngine)
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(apiService, platformModule.userDatabase)
    }

    val getUsersUseCase: GetUsersUseCase by lazy {
        GetUsersUseCase(userRepository)
    }
}

// Platform-specific module interface
expect class PlatformModule {
    val httpEngine: HttpClientEngine
    val userDatabase: UserDatabase
}
```

```kotlin
// shared/src/androidMain/kotlin/com/example/shared/di/PlatformModule.android.kt

actual class PlatformModule(private val context: Context) {
    actual val httpEngine: HttpClientEngine = Android.create()
    actual val userDatabase: UserDatabase = AndroidUserDatabase(context)
}
```

```kotlin
// shared/src/iosMain/kotlin/com/example/shared/di/PlatformModule.ios.kt

actual class PlatformModule {
    actual val httpEngine: HttpClientEngine = Darwin.create()
    actual val userDatabase: UserDatabase = IosUserDatabase()
}
```

---

## Integration with Existing Apps

### Android Integration

```kotlin
// androidApp/build.gradle.kts
dependencies {
    implementation(project(":shared"))
}

// androidApp/src/main/kotlin/.../MainActivity.kt
class MainActivity : ComponentActivity() {

    private val commonModule by lazy {
        CommonModule(PlatformModule(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val users by commonModule.getUsersUseCase
                .execute()
                .collectAsState(initial = emptyList())

            UserListScreen(users = users)
        }
    }
}
```

### iOS Integration (Swift)

```swift
// iosApp/iosApp/ContentView.swift
import SwiftUI
import shared  // The KMM framework

struct ContentView: View {
    @StateObject private var viewModel = UsersViewModel()

    var body: some View {
        NavigationView {
            List(viewModel.users, id: \.id) { user in
                VStack(alignment: .leading) {
                    Text(user.name)
                        .font(.headline)
                    Text(user.email)
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }
            }
            .navigationTitle("Users")
            .onAppear { viewModel.loadUsers() }
        }
    }
}

class UsersViewModel: ObservableObject {
    @Published var users: [User] = []

    private let commonModule = CommonModule(
        platformModule: PlatformModule()
    )

    func loadUsers() {
        commonModule.getUsersUseCase.execute { result, error in
            DispatchQueue.main.async {
                if let users = result {
                    self.users = users as! [User]
                }
            }
        }
    }
}
```

---

## What to Share vs. Keep Native

```
┌────────────────────────────────────────────────────────┐
│              SHARE in commonMain                        │
├────────────────────────────────────────────────────────┤
│  ✅ Data models / DTOs                                 │
│  ✅ Business logic / Use cases                         │
│  ✅ Repository interfaces                              │
│  ✅ Networking (Ktor)                                  │
│  ✅ Local database (SQLDelight)                        │
│  ✅ Validation logic                                   │
│  ✅ State management                                   │
│  ✅ Utility functions                                  │
│  ✅ Constants / Configuration                          │
├────────────────────────────────────────────────────────┤
│              KEEP NATIVE (platform-specific)            │
├────────────────────────────────────────────────────────┤
│  ❌ UI (Compose / SwiftUI)                             │
│  ❌ Navigation                                         │
│  ❌ Platform APIs (Camera, GPS, Bluetooth)             │
│  ❌ Notifications                                      │
│  ❌ Animations & Gestures                              │
│  ❌ Platform-specific libraries                        │
└────────────────────────────────────────────────────────┘
```

---

## Key Libraries for KMM

| Library | Purpose | commonMain Support |
|---------|---------|-------------------|
| **Ktor** | HTTP networking | ✅ Full |
| **SQLDelight** | Local SQL database | ✅ Full |
| **Koin** | Dependency Injection | ✅ Full |
| **kotlinx.serialization** | JSON parsing | ✅ Full |
| **kotlinx.coroutines** | Async operations | ✅ Full |
| **kotlinx.datetime** | Date/time handling | ✅ Full |
| **Napier** | Logging | ✅ Full |
| **Multiplatform Settings** | Key-value storage | ✅ Full |
