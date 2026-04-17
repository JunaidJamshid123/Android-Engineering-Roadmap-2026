# 11. Build Tools and CI/CD — Complete Android Guide

---

## Table of Contents
1. [11.1 Gradle](#111-gradle)
   - [Fundamentals](#fundamentals) — Lifecycle, Build Types, Flavors, Configurations
   - [Advanced Gradle](#advanced-gradle) — Kotlin DSL, Custom Tasks, Caching, Composite Builds
2. [11.2 CI/CD Pipelines](#112-cicd-pipelines) — GitHub Actions, GitLab, Bitrise, CircleCI, Jenkins
3. [11.3 Automated Testing in CI](#113-automated-testing-in-ci) — Unit/Instrumentation, Firebase Test Lab, Reporting
4. [11.4 Release Management](#114-release-management) — Signing, Play Console, Staged Rollouts, Automation
5. [Interview Questions & Answers](#interview-questions--answers)

---

# 11.1 Gradle

## Fundamentals

### What is Gradle?

Gradle is the **official build automation tool** for Android, chosen by Google over Maven and Ant for
its flexibility, performance, and incremental build capabilities. Unlike Maven (XML-based, rigid
convention-over-configuration) or Ant (imperative scripts), Gradle combines the best of both:
**declarative build configuration** with **programmatic extensibility**.

**Why Gradle for Android (not Maven/Ant)?**

```
┌──────────────────────────────────────────────────────────────────────────┐
│                  BUILD TOOL EVOLUTION FOR ANDROID                        │
│                                                                          │
│  2005              2012              2013              Today             │
│  ┌────────┐       ┌────────┐       ┌────────────┐    ┌──────────────┐   │
│  │  Ant   │──────▶│ Maven  │──────▶│  Gradle    │───▶│ Gradle with  │   │
│  │        │       │        │       │            │    │ Kotlin DSL + │   │
│  │ XML    │       │ XML    │       │ Groovy DSL │    │ Version      │   │
│  │ Scripts│       │ POM    │       │ + Android  │    │ Catalogs     │   │
│  │        │       │ Rigid  │       │   Plugin   │    │              │   │
│  └────────┘       └────────┘       └────────────┘    └──────────────┘   │
│                                                                          │
│  Problems:         Problems:        Solved:           Improved:          │
│  • No dependency   • No variant     • Build variants  • Type-safe DSL   │
│    management        support        • Flavor system   • Central deps    │
│  • Verbose XML     • Inflexible     • Incremental     • Convention      │
│  • Manual build    • Slow builds      builds            plugins         │
└──────────────────────────────────────────────────────────────────────────┘
```

Gradle handles the **entire Android build pipeline**:

| Responsibility | What It Does | Key Gradle Component |
|---|---|---|
| **Compilation** | Compiles Kotlin/Java to bytecode, then to DEX | `compileDebugKotlin`, `dexBuilderDebug` |
| **Resource Processing** | Merges, compiles XML resources (layouts, drawables) | `mergeDebugResources`, `processDebugResources` |
| **Packaging** | Zips DEX + resources + assets into APK/AAB | `packageDebug`, `bundleRelease` |
| **Dependency Management** | Downloads & resolves transitive library graphs | Version catalogs, `dependencies {}` |
| **Code Shrinking** | R8 removes unused code + obfuscates (release) | ProGuard rules, `isMinifyEnabled` |
| **Testing** | Runs unit tests (JVM) + instrumentation tests (device) | `testDebugUnitTest`, `connectedCheck` |
| **Signing** | Signs APK/AAB with debug/release keystores | `signingConfigs {}` |
| **Publishing** | Uploads to Google Play Store via API | Gradle Play Publisher plugin |

---

### Gradle Build Lifecycle

Every Gradle build goes through **3 distinct phases**—understanding this is critical because
**performance problems and subtle bugs** almost always come from doing work in the wrong phase.

```
┌────────────────────────────────────────────────────────────────────────────┐
│                       GRADLE BUILD LIFECYCLE                               │
│                                                                            │
│  ./gradlew assembleDebug                                                   │
│       │                                                                    │
│       ▼                                                                    │
│  ╔═══════════════════╗   ╔═══════════════════╗   ╔═══════════════════╗     │
│  ║  1. INITIALIZATION║──▶║  2. CONFIGURATION ║──▶║  3. EXECUTION    ║     │
│  ║                   ║   ║                   ║   ║                   ║     │
│  ║ • Reads           ║   ║ • Evaluates ALL   ║   ║ • Runs ONLY the  ║     │
│  ║   settings.gradle ║   ║   build.gradle    ║   ║   tasks that     ║     │
│  ║ • Finds all       ║   ║   files of EVERY  ║   ║   were requested ║     │
│  ║   included        ║   ║   module          ║   ║ • Follows task   ║     │
│  ║   projects        ║   ║ • Creates Task    ║   ║   dependency DAG ║     │
│  ║ • Creates         ║   ║   Graph (DAG)     ║   ║ • Checks up-to- ║     │
│  ║   Project objects ║   ║ • Configures all  ║   ║   date / cached  ║     │
│  ║ • Runs            ║   ║   tasks (even     ║   ║ • Executes       ║     │
│  ║   pluginManagement║   ║   ones NOT run)   ║   ║   doFirst {}     ║     │
│  ║                   ║   ║ • Resolves deps   ║   ║   doLast {}      ║     │
│  ╚═══════════════════╝   ╚═══════════════════╝   ╚═══════════════════╝     │
│         │                        │                        │                │
│         ▼                        ▼                        ▼                │
│  settings.gradle.kts      build.gradle.kts         Task Actions            │
│  (1 file, runs once)     (1 per module, ALL run)   (only needed ones)      │
│                                                                            │
│  ┌─────────────────────────────────────────────────────────────────┐       │
│  │  COMMON MISTAKE: Doing I/O or expensive work at configuration   │       │
│  │  time. It runs on EVERY build even if the task is never needed! │       │
│  │                                                                 │       │
│  │  ✗ BAD:  val data = file("big.json").readText()  // in config   │       │
│  │  ✓ GOOD: doLast { val data = file("big.json").readText() }      │       │
│  └─────────────────────────────────────────────────────────────────┘       │
└────────────────────────────────────────────────────────────────────────────┘
```

**Phase Details:**

| Phase | What Happens | Files Involved | Performance Impact |
|-------|-------------|----------------|-------------------|
| **Initialization** | Determines which projects participate in the build | `settings.gradle.kts` | Fast (single file) |
| **Configuration** | Evaluates ALL build scripts, creates task DAG | `build.gradle.kts` (ALL modules) | Slow if modules are many; cached with config cache |
| **Execution** | Runs ONLY the selected tasks in dependency order | Task action closures (`doLast`, `doFirst`) | Depends on task; skipped if up-to-date |

**Task DAG (Directed Acyclic Graph) — How Gradle Plans Execution:**

```
┌──────────────────────────────────────────────────────────────────┐
│           TASK DAG for "assembleDebug"                            │
│                                                                  │
│  assembleDebug                                                   │
│       │                                                          │
│       ├── packageDebug                                           │
│       │       ├── mergeDebugResources                            │
│       │       │       └── generateDebugResValues                 │
│       │       ├── compileDebugKotlin                             │
│       │       │       └── generateDebugBuildConfig               │
│       │       ├── mergeDebugAssets                               │
│       │       ├── processDebugManifest                           │
│       │       │       └── mergeDebugManifest                     │
│       │       └── dexBuilderDebug                                │
│       │               └── compileDebugKotlin (already done)      │
│       └── mergeDebugJniLibFolders                                │
│                                                                  │
│  Gradle walks this graph bottom-up, running tasks that have      │
│  changed inputs. Tasks with unchanged inputs are SKIPPED         │
│  (marked UP-TO-DATE or FROM-CACHE).                              │
└──────────────────────────────────────────────────────────────────┘
```

```kotlin
// settings.gradle.kts — INITIALIZATION PHASE
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PracticeApp"
include(":app")
include(":core")
include(":feature:home")
include(":feature:profile")
```

```kotlin
// build.gradle.kts — CONFIGURATION PHASE
// Everything here runs during configuration, NOT execution
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    // This block is CONFIGURED, not executed yet
    compileSdk { version = release(36) }
}

// EXECUTION PHASE — only runs when this task is invoked
tasks.register("printBuildInfo") {
    // This closure runs during CONFIGURATION
    val appName = "PracticeApp"

    doLast {
        // This closure runs during EXECUTION
        println("Building: $appName")
    }
}
```

---

### Build Types: Debug & Release

Build types define **HOW** your app is compiled and packaged. They answer: "Is this a development
build or a production build?" Every Android project gets `debug` and `release` automatically,
but you can create **custom build types** for staging, benchmarking, QA, etc.

**Key Concept:** Build types affect the **same code** — they don't add/remove features (that's
what product flavors do). Instead, they control compilation flags, optimization, signing, and
environment configuration.

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           BUILD TYPES — DEEP DIVE                           │
│                                                                              │
│  ┌─────────────────────────────┐      ┌─────────────────────────────────┐   │
│  │          DEBUG               │      │           RELEASE                │   │
│  │ ┌─────────────────────────┐ │      │ ┌─────────────────────────────┐ │   │
│  │ │ debuggable = true       │ │      │ │ debuggable = false          │ │   │
│  │ │ isMinifyEnabled = false │ │      │ │ isMinifyEnabled = true      │ │   │
│  │ │ isShrinkResources = NO  │ │      │ │ isShrinkResources = true    │ │   │
│  │ │ Uses debug.keystore     │ │      │ │ Uses release.keystore       │ │   │
│  │ │ applicationIdSuffix:    │ │      │ │ ProGuard/R8 optimization    │ │   │
│  │ │   ".debug" (optional)   │ │      │ │ Code obfuscation            │ │   │
│  │ │ Fast incremental builds │ │      │ │ Slower but optimized builds │ │   │
│  │ │ Attach debugger         │ │      │ │ Cannot attach debugger      │ │   │
│  │ │ StrictMode enabled      │ │      │ │ StrictMode disabled         │ │   │
│  │ └─────────────────────────┘ │      │ └─────────────────────────────┘ │   │
│  └─────────────────────────────┘      └─────────────────────────────────┘   │
│                                                                              │
│  ┌─────────────────────────────┐      ┌─────────────────────────────────┐   │
│  │     STAGING (Custom)         │      │     BENCHMARK (Custom)          │   │
│  │ ┌─────────────────────────┐ │      │ ┌─────────────────────────────┐ │   │
│  │ │ initWith(release)       │ │      │ │ initWith(release)           │ │   │
│  │ │ debuggable = false      │ │      │ │ debuggable = false          │ │   │
│  │ │ Points to staging API   │ │      │ │ profileable = true          │ │   │
│  │ │ Uses debug signing key  │ │      │ │ matchingFallbacks: release  │ │   │
│  │ │ QA team installs this   │ │      │ │ For Macrobenchmark tests    │ │   │
│  │ └─────────────────────────┘ │      │ └─────────────────────────────┘ │   │
│  └─────────────────────────────┘      └─────────────────────────────────┘   │
│                                                                              │
│  How R8/ProGuard Works in Release:                                           │
│  ┌──────────────────────────────────────────────────────────────────────┐    │
│  │  Source Code ──▶ Compile ──▶ R8 Shrinks/Optimizes/Obfuscates ──▶ DEX│    │
│  │                                                                      │    │
│  │  [1] Tree Shaking: Removes unused classes, methods, fields          │    │
│  │  [2] Code Shrinking: Removes dead code paths                        │    │
│  │  [3] Resource Shrinking: Removes unused drawables, layouts, etc.    │    │
│  │  [4] Obfuscation: Renames classes/methods to a, b, c...            │    │
│  │  [5] Optimization: Inlines methods, removes logging, etc.          │    │
│  │                                                                      │    │
│  │  Typical APK size reduction: 30-60%                                  │    │
│  └──────────────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        // Debug — auto-created, customizable
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"       // com.example.app.debug
            versionNameSuffix = "-DEBUG"
            buildConfigField("String", "API_URL", "\"https://api-dev.example.com\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
        }

        // Release — auto-created, customizable
        release {
            isDebuggable = false
            isMinifyEnabled = true                // Enable R8 code shrinking
            isShrinkResources = true              // Remove unused resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_URL", "\"https://api.example.com\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "false")
        }

        // Staging — custom build type
        create("staging") {
            initWith(getByName("release"))        // Copy from release
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-STAGING"
            buildConfigField("String", "API_URL", "\"https://api-staging.example.com\"")
            // Use debug signing for staging
            signingConfig = signingConfigs.getByName("debug")
        }

        // Benchmark — custom build type
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
}
```

---

### Product Flavors for Variants

Product flavors create **different versions** of your app from the **same codebase**. While build
types control HOW the app is compiled (debug vs release), flavors control WHAT gets compiled — they
let you swap out features, branding, API endpoints, or monetization models.

**Real-World Use Cases:**
- **White-label apps**: Same app, different branding for different clients
- **Free vs Paid**: Limit features in free version
- **Regional variants**: Different payment methods, languages, compliance rules
- **Internal vs External**: Employee app vs customer-facing app

**Flavor Dimensions** — The key to understanding variants. Each dimension represents an independent
axis of variation. Gradle creates the **Cartesian product** of all dimensions × build types.

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                   PRODUCT FLAVORS & VARIANTS — DEEP DIVE                     │
│                                                                              │
│  Flavor Dimensions are INDEPENDENT axes:                                     │
│                                                                              │
│  Dimension 1: ENVIRONMENT       Dimension 2: TIER                            │
│  (Which server to talk to?)     (What features are enabled?)                 │
│                                                                              │
│  ┌──────────────────┐          ┌──────────────────┐                          │
│  │  ┌────────────┐  │          │  ┌────────────┐  │                          │
│  │  │    dev     │  │          │  │    free    │  │                          │
│  │  │ dev-api.com│  │    ×     │  │ Ads, Limit │  │                          │
│  │  ├────────────┤  │          │  ├────────────┤  │                          │
│  │  │  staging   │  │          │  │    paid    │  │                          │
│  │  │ staging.com│  │          │  │ No ads     │  │                          │
│  │  ├────────────┤  │          │  └────────────┘  │                          │
│  │  │   prod     │  │          └──────────────────┘                          │
│  │  │ api.com    │  │                                                        │
│  │  └────────────┘  │                                                        │
│  └──────────────────┘                                                        │
│                                                                              │
│  Build Variants = Dim1 × Dim2 × BuildType = 3 × 2 × 2 = 12 variants        │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐     │
│  │             DEBUG                    │         RELEASE              │     │
│  │  ┌──────────────┬──────────────┐    │ ┌──────────────┬───────────┐ │     │
│  │  │ devFreeDebug │ devPaidDebug │    │ │devFreeRelease│devPaidRel.│ │     │
│  │  ├──────────────┼──────────────┤    │ ├──────────────┼───────────┤ │     │
│  │  │stagFreeDebug │stagPaidDebug │    │ │stagFreeRel.  │stagPaidR. │ │     │
│  │  ├──────────────┼──────────────┤    │ ├──────────────┼───────────┤ │     │
│  │  │prodFreeDebug │prodPaidDebug │    │ │prodFreeRel.  │prodPaidR. │ │     │
│  │  └──────────────┴──────────────┘    │ └──────────────┴───────────┘ │     │
│  └─────────────────────────────────────┴──────────────────────────────┘     │
│                                                                              │
│  Naming Convention: [flavor1][flavor2][BuildType]                             │
│  Example:           dev      Paid      Debug    → devPaidDebug               │
│  (Dimension order matters — defined by flavorDimensions list order)           │
└──────────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// app/build.gradle.kts
android {
    // Step 1: Define flavor dimensions (order = priority)
    flavorDimensions += listOf("environment", "tier")

    productFlavors {
        // ── Environment Dimension ──
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"https://dev-api.example.com\"")
            // Use a different app name for dev
            resValue("string", "app_name", "MyApp Dev")
        }

        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "BASE_URL", "\"https://staging-api.example.com\"")
            resValue("string", "app_name", "MyApp Staging")
        }

        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://api.example.com\"")
            resValue("string", "app_name", "MyApp")
        }

        // ── Tier Dimension ──
        create("free") {
            dimension = "tier"
            buildConfigField("Boolean", "IS_PREMIUM", "false")
            buildConfigField("Int", "MAX_ITEMS", "10")
        }

        create("paid") {
            dimension = "tier"
            applicationIdSuffix = ".premium"
            buildConfigField("Boolean", "IS_PREMIUM", "true")
            buildConfigField("Int", "MAX_ITEMS", "Integer.MAX_VALUE")
        }
    }

    // Optional: Filter out unnecessary variants
    androidComponents {
        beforeVariants { variantBuilder ->
            // Don't build devPaid variants — not needed
            if (variantBuilder.productFlavors.containsAll(
                    listOf("environment" to "dev", "tier" to "paid")
                )
            ) {
                variantBuilder.enable = false
            }
        }
    }
}
```

**Source Set Structure for Flavors:**

Understanding source sets is crucial — Gradle **merges** code and resources from multiple source
set directories based on the active variant. More specific source sets override less specific ones.

```
app/src/
├── main/            ← Base code (ALL variants always include this)
│   ├── java/        ← Shared Kotlin/Java code
│   ├── res/         ← Shared resources (layouts, drawables, strings)
│   └── AndroidManifest.xml
│
├── debug/           ← Debug build type overlay
│   └── java/        ← Debug-only code (e.g., LeakCanary init)
├── release/         ← Release build type overlay
│   └── res/values/  ← Release-only string overrides
│
├── dev/             ← Dev flavor overlay
│   └── java/        ← Dev-specific implementations
├── staging/         ← Staging flavor overlay
├── prod/            ← Prod flavor overlay
│
├── free/            ← Free tier overlay
│   └── java/        ← Free features (e.g., AdsManager)
├── paid/            ← Paid tier overlay
│   └── java/        ← Premium features
│
├── devFree/         ← Combined flavor overlay (dev + free)
└── devFreeDebug/    ← Full variant overlay (most specific)
```

**Source Set Merge Priority (highest wins):**

```
┌────────────────────────────────────────────────────────────────────┐
│  MERGE PRIORITY — How Gradle resolves conflicts                    │
│                                                                    │
│  For variant "devFreeDebug":                                       │
│                                                                    │
│  HIGHEST PRIORITY                                                  │
│     │                                                              │
│     │  1. src/devFreeDebug/   ← Full variant-specific override     │
│     │  2. src/devFree/        ← Multi-flavor combination           │
│     │  3. src/devDebug/       ← Flavor + build type combo          │
│     │  4. src/dev/            ← First dimension flavor             │
│     │  5. src/free/           ← Second dimension flavor            │
│     │  6. src/debug/          ← Build type                         │
│     │  7. src/main/           ← Base (always included)             │
│     ▼                                                              │
│  LOWEST PRIORITY                                                   │
│                                                                    │
│  Merge Rules:                                                      │
│  • Java/Kotlin: Files are merged (same class = compile error!)     │
│  • Resources: Higher-priority overrides lower (string, drawable)   │
│  • Manifests: Merged with manifest merger tool                     │
│  • Assets: Higher-priority wins for same filename                  │
└────────────────────────────────────────────────────────────────────┘
```

**Practical Example — Different implementations per flavor:**

```kotlin
// src/main/java/com/example/app/analytics/AnalyticsTracker.kt
// INTERFACE defined in main (shared)
interface AnalyticsTracker {
    fun trackEvent(name: String, params: Map<String, Any> = emptyMap())
    fun trackScreen(screenName: String)
}

// src/dev/java/com/example/app/analytics/AnalyticsTrackerImpl.kt
// DEV flavor — just prints to logcat (no real analytics in dev)
class AnalyticsTrackerImpl : AnalyticsTracker {
    override fun trackEvent(name: String, params: Map<String, Any>) {
        Log.d("Analytics-DEV", "Event: $name, params: $params")
    }
    override fun trackScreen(screenName: String) {
        Log.d("Analytics-DEV", "Screen: $screenName")
    }
}

// src/prod/java/com/example/app/analytics/AnalyticsTrackerImpl.kt
// PROD flavor — sends to Firebase Analytics
class AnalyticsTrackerImpl(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsTracker {
    override fun trackEvent(name: String, params: Map<String, Any>) {
        firebaseAnalytics.logEvent(name, bundleOf(*params.entries.map { 
            it.key to it.value 
        }.toTypedArray()))
    }
    override fun trackScreen(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName
        ))
    }
}
```

---

### Build Configurations

```kotlin
// app/build.gradle.kts — Complete build configuration
android {
    namespace = "com.example.practiceapp"
    compileSdk { version = release(36) }

    defaultConfig {
        applicationId = "com.example.practiceapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Vector drawable support
        vectorDrawables.useSupportLibrary = true

        // Multi-dex for large apps
        multiDexEnabled = true

        // NDK filters
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }

        // Room schema export directory
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true      // Generate BuildConfig class
        viewBinding = true      // Enable View Binding
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true  // Java 8+ APIs on older Android
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = false
        warningsAsErrors = true
        checkDependencies = true
        xmlReport = true
        htmlReport = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}
```

---

## Advanced Gradle

### Gradle Kotlin DSL

The Kotlin DSL (`.gradle.kts`) replaces the Groovy DSL (`.gradle`) with type-safe Kotlin syntax.
Google made it the **default for new projects** in 2023. The key advantage isn't just syntax — it's
that **Gradle generates Kotlin accessor types** for your build model, giving you compile-time safety
and IDE auto-completion that Groovy can never match.

**How it works under the hood:** When you write `android { compileSdk = 34 }`, Gradle generates
Kotlin extension functions tied to the Android Gradle Plugin's `ApplicationExtension` class. This
means typos are caught at build script compile time, not at runtime.

```
┌──────────────────────────────────────────────────────────────────────────┐
│               GROOVY DSL  vs  KOTLIN DSL — DETAILED COMPARISON           │
│                                                                          │
│  Feature          │ Groovy (.gradle)        │ Kotlin (.gradle.kts)       │
│  ─────────────────┼─────────────────────────┼──────────────────────────  │
│  Type Safety      │ ✗ Runtime errors        │ ✓ Compile-time errors      │
│  IDE Support      │ ✗ Limited completion    │ ✓ Full auto-completion     │
│  Refactoring      │ ✗ No safe refactoring   │ ✓ IDE rename/refactor      │
│  Debugging        │ ✗ Hard to debug         │ ✓ Set breakpoints in IDE   │
│  Learning Curve   │ ✓ Easier for beginners  │ ✗ Kotlin knowledge needed  │
│  String Quotes    │ Single or double        │ Always double              │
│  Property Access  │ compileSdkVersion 34    │ compileSdk = 34            │
│  Task Creation    │ task myTask { }         │ tasks.register("myTask")   │
│  Delegation       │ Implicit               │ Explicit (by lazy, etc.)   │
│  Build Speed      │ ✓ Slightly faster parse │ ✗ First compile is slower  │
│  Documentation    │ ✓ More legacy examples  │ ✓ Google's new default     │
│                   │                         │                            │
│  apply plugin:    │ apply plugin:           │ plugins {                  │
│                   │   'com.android.app'     │   id("com.android.app")    │
│                   │                         │ }                          │
│                   │                         │                            │
│  dependencies     │ implementation          │ implementation(            │
│                   │   'group:name:1.0'      │   "group:name:1.0"         │
│                   │                         │ )                          │
│                   │                         │                            │
│  lists            │ ['a', 'b']              │ listOf("a", "b")           │
│  maps             │ [key: 'value']          │ mapOf("key" to "value")    │
│  file access      │ file('path')            │ file("path")               │
│  boolean props    │ minifyEnabled true      │ isMinifyEnabled = true     │
└──────────────────────────────────────────────────────────────────────────┘
```

**Migration Tips (Groovy → Kotlin DSL):**

```kotlin
// ── 1. File extension change ──
// Rename: build.gradle → build.gradle.kts
// Rename: settings.gradle → settings.gradle.kts

// ── 2. All strings must use double quotes ──
// Groovy: implementation 'lib:1.0'
// Kotlin: implementation("lib:1.0")

// ── 3. Assignment operator required for properties ──
// Groovy: compileSdkVersion 34      (function call style)
// Kotlin: compileSdk = 34           (property assignment)

// ── 4. Boolean properties use "is" prefix ──
// Groovy: minifyEnabled true
// Kotlin: isMinifyEnabled = true

// ── 5. Task creation uses register() ──
// Groovy: task cleanAll(type: Delete) { delete rootProject.buildDir }
// Kotlin:
tasks.register<Delete>("cleanAll") {
    delete(rootProject.layout.buildDirectory)
}

// ── 6. Extra properties ──
// Groovy: ext.myVersion = "1.0"
// Kotlin: val myVersion by extra("1.0")   // or:
//         extra["myVersion"] = "1.0"

// ── 7. Build script dependencies ──
// Groovy: classpath 'com.android.tools.build:gradle:8.2.0'
// Kotlin: classpath("com.android.tools.build:gradle:8.2.0")

// ── 8. Accessing project properties ──
// Groovy: project.hasProperty('prop') ? project.prop : 'default'
// Kotlin: project.findProperty("prop")?.toString() ?: "default"
```

---

### Custom Tasks

```kotlin
// app/build.gradle.kts

// ── Simple custom task ──
tasks.register("cleanBuildReports") {
    group = "cleanup"
    description = "Deletes all build report files"

    doLast {
        delete(fileTree("build/reports"))
        println("Build reports cleaned!")
    }
}

// ── Task with inputs/outputs (enables caching) ──
tasks.register("generateVersionFile") {
    group = "generation"
    description = "Generates a version info file"

    val outputFile = file("$buildDir/generated/version.txt")

    // Declare inputs/outputs for up-to-date checking
    inputs.property("versionName", android.defaultConfig.versionName)
    inputs.property("versionCode", android.defaultConfig.versionCode)
    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()
        outputFile.writeText("""
            Version: ${android.defaultConfig.versionName}
            Code: ${android.defaultConfig.versionCode}
            Built: ${java.time.LocalDateTime.now()}
        """.trimIndent())
    }
}

// ── Task dependencies ──
tasks.register("fullClean") {
    group = "cleanup"
    dependsOn("clean", "cleanBuildReports")
    doLast {
        println("Full clean completed!")
    }
}

// ── Typed task (Copy) ──
tasks.register<Copy>("copyApk") {
    from("build/outputs/apk/release/")
    into("$rootDir/artifacts/")
    include("*.apk")
    rename { "app-release-${android.defaultConfig.versionName}.apk" }
}

// ── Task that runs after assembleRelease ──
tasks.whenTaskAdded {
    if (name == "assembleRelease") {
        finalizedBy("copyApk")
    }
}

// ── Custom task class (reusable) ──
abstract class IncrementVersionTask : DefaultTask() {
    @get:Input
    abstract val versionType: Property<String>

    @get:InputFile
    abstract val gradleFile: RegularFileProperty

    @TaskAction
    fun increment() {
        val file = gradleFile.get().asFile
        val content = file.readText()

        val regex = Regex("""versionCode\s*=\s*(\d+)""")
        val match = regex.find(content) ?: return

        val currentCode = match.groupValues[1].toInt()
        val newContent = content.replace(
            "versionCode = $currentCode",
            "versionCode = ${currentCode + 1}"
        )
        file.writeText(newContent)
        println("Version code: $currentCode → ${currentCode + 1}")
    }
}

tasks.register<IncrementVersionTask>("incrementVersion") {
    group = "versioning"
    versionType.set("patch")
    gradleFile.set(file("build.gradle.kts"))
}
```

---

### Build Variants Matrix

```
┌──────────────────────────────────────────────────────────────────────────┐
│                     BUILD VARIANTS MATRIX                                │
│                                                                          │
│  Formula: Variants = Flavor1 × Flavor2 × ... × BuildTypes               │
│                                                                          │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐                   │
│  │ Dimension 1 │   │ Dimension 2 │   │ Build Types │                   │
│  │ environment  │ × │ tier        │ × │             │                   │
│  ├─────────────┤   ├─────────────┤   ├─────────────┤                   │
│  │ dev         │   │ free        │   │ debug       │                   │
│  │ prod        │   │ paid        │   │ release     │                   │
│  └─────────────┘   └─────────────┘   └─────────────┘                   │
│                                                                          │
│  Resulting Variant Names:                                                │
│  ┌──────────────────┬────────────────────┬───────────────────────┐       │
│  │ devFreeDebug     │ devFreeRelease     │ devPaidDebug          │       │
│  │ devPaidRelease   │ prodFreeDebug      │ prodFreeRelease       │       │
│  │ prodPaidDebug    │ prodPaidRelease    │                       │       │
│  └──────────────────┴────────────────────┴───────────────────────┘       │
│                                                                          │
│  Source Set Priority (highest to lowest):                                │
│  ┌────────────────────────────────────────────────────────┐              │
│  │  1. src/devFreeDebug/   (full variant)                 │              │
│  │  2. src/devFree/        (multi-flavor combo)           │              │
│  │  3. src/devDebug/       (flavor + build type)          │              │
│  │  4. src/dev/            (flavor)                       │              │
│  │  5. src/free/           (flavor)                       │              │
│  │  6. src/debug/          (build type)                   │              │
│  │  7. src/main/           (base — always included)       │              │
│  └────────────────────────────────────────────────────────┘              │
└──────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// Accessing variant-specific info in code
// BuildConfig fields are generated per variant
class AppConfig {
    companion object {
        val baseUrl: String = BuildConfig.BASE_URL
        val isPremium: Boolean = BuildConfig.IS_PREMIUM
        val isDebug: Boolean = BuildConfig.DEBUG
    }
}

// Use variant-specific dependencies
dependencies {
    // Only for free tier
    "freeImplementation"("com.google.android.gms:play-services-ads:22.6.0")

    // Only for paid tier
    "paidImplementation"("com.example:premium-features:1.0.0")

    // Only for debug build type
    debugImplementation("com.facebook.flipper:flipper:0.250.0")

    // Only for devDebug variant
    "devDebugImplementation"("com.squareup.leakcanary:leakcanary-android:2.13")
}
```

---

### Dependency Management and Version Catalogs

```
┌──────────────────────────────────────────────────────────────────────┐
│                   VERSION CATALOG SYSTEM                              │
│                                                                      │
│  gradle/libs.versions.toml (Single source of truth)                  │
│                                                                      │
│  ┌────────────────┐                                                  │
│  │  [versions]    │── Defines version numbers                        │
│  ├────────────────┤                                                  │
│  │  [libraries]   │── Defines library coordinates + version refs     │
│  ├────────────────┤                                                  │
│  │  [bundles]     │── Groups libraries together                      │
│  ├────────────────┤                                                  │
│  │  [plugins]     │── Defines Gradle plugin IDs + versions           │
│  └────────────────┘                                                  │
│          │                                                           │
│          ▼                                                           │
│  ┌────────────────────────────────────────────┐                      │
│  │  build.gradle.kts                          │                      │
│  │                                            │                      │
│  │  libs.versions.someVersion                 │                      │
│  │  libs.someLibrary                          │                      │
│  │  libs.bundles.someBundle                   │                      │
│  │  libs.plugins.somePlugin                   │                      │
│  └────────────────────────────────────────────┘                      │
└──────────────────────────────────────────────────────────────────────┘
```

```toml
# gradle/libs.versions.toml — Complete Example

[versions]
agp = "8.13.2"
kotlin = "2.0.21"
compose-bom = "2024.09.00"
coroutines = "1.8.1"
room = "2.6.1"
hilt = "2.51"
retrofit = "2.9.0"
okhttp = "4.12.0"
coil = "2.6.0"
navigation = "2.7.7"
lifecycle = "2.8.0"
ksp = "2.0.21-1.0.25"
junit = "4.13.2"
mockk = "1.13.10"

[libraries]
# AndroidX Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.12.0" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version = "1.6.1" }

# Compose (uses BOM for consistent versions)
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }

# Lifecycle
lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Room Database
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Hilt DI
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

# Image Loading
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }

[bundles]
# Group related libraries together
compose = ["compose-ui", "compose-material3", "compose-ui-tooling-preview"]
lifecycle = ["lifecycle-runtime-ktx", "lifecycle-viewmodel-compose"]
room = ["room-runtime", "room-ktx"]
networking = ["retrofit", "retrofit-gson", "okhttp-logging"]
coroutines = ["coroutines-core", "coroutines-android"]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

```kotlin
// app/build.gradle.kts — Using Version Catalog
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

dependencies {
    // Individual libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)

    // Bundles — add multiple related deps at once
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.room)
    implementation(libs.bundles.networking)
    implementation(libs.bundles.coroutines)

    // Compose BOM (manages compose versions)
    implementation(platform(libs.compose.bom))
    debugImplementation(libs.compose.ui.tooling)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
```

---

### Gradle Plugins Development

```kotlin
// buildSrc/src/main/kotlin/AndroidLibraryConventionPlugin.kt
// Convention plugin — reuse build logic across modules

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply common plugins
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")

            // Configure android block
            extensions.configure<LibraryExtension> {
                compileSdk = 34

                defaultConfig {
                    minSdk = 24
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }

                buildFeatures {
                    buildConfig = false
                }
            }
        }
    }
}

// buildSrc/build.gradle.kts
plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly("com.android.tools.build:gradle:8.2.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "myapp.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}
```

```kotlin
// feature/home/build.gradle.kts — Using convention plugin
plugins {
    id("myapp.android.library")   // All common config applied!
}

// Only module-specific config needed
dependencies {
    implementation(project(":core"))
    implementation(libs.bundles.compose)
}
```

---

### Build Cache and Configuration Cache

These are two **different** caching mechanisms that solve different performance problems. Understanding
the distinction is a common interview topic.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       GRADLE CACHING SYSTEM — DEEP DIVE                     │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────┐          │
│  │              1. BUILD CACHE (Task Output Cache)               │          │
│  │                                                               │          │
│  │  What it caches: The OUTPUT of individual tasks               │          │
│  │  How it works:   Hash(task inputs) → stored output            │          │
│  │  When it helps:  When you re-run a task with same inputs      │          │
│  │                                                               │          │
│  │  Example: compileDebugKotlin                                  │          │
│  │  Inputs: source files, compiler version, JVM args             │          │
│  │  Output: compiled .class files                                │          │
│  │                                                               │          │
│  │  If inputs haven't changed → skip execution, reuse output     │          │
│  │                                                               │          │
│  │  ┌──────────────┐      ┌──────────────────────────┐          │          │
│  │  │ Local Cache  │      │  Remote Cache (CI share) │          │          │
│  │  │ ~/.gradle/   │      │  HTTP server / GCS / S3  │          │          │
│  │  │ caches/      │◀────▶│                          │          │          │
│  │  │              │      │  CI pushes, devs pull     │          │          │
│  │  │ Per-machine  │      │  Shared across machines   │          │          │
│  │  └──────────────┘      └──────────────────────────┘          │          │
│  │                                                               │          │
│  │  Task States:                                                 │          │
│  │  • EXECUTED     — Ran the task (first time or inputs changed) │          │
│  │  • UP-TO-DATE   — Inputs unchanged since last local run       │          │
│  │  • FROM-CACHE   — Retrieved output from build cache           │          │
│  │  • SKIPPED      — Task disabled or condition not met          │          │
│  │  • NO-SOURCE    — Task has no input files                     │          │
│  └───────────────────────────────────────────────────────────────┘          │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────┐          │
│  │           2. CONFIGURATION CACHE (Task Graph Cache)           │          │
│  │                                                               │          │
│  │  What it caches: The entire TASK GRAPH from configuration     │          │
│  │  How it works:   Serializes the configured task graph to disk │          │
│  │  When it helps:  EVERY build (skips configuration phase)      │          │
│  │                                                               │          │
│  │  Without config cache:                                        │          │
│  │  ┌───────────────────────────────────────────────────┐       │          │
│  │  │ Init → Configure ALL modules → Build Task Graph → │       │          │
│  │  │ Execute (2-10 seconds for configuration alone)     │       │          │
│  │  └───────────────────────────────────────────────────┘       │          │
│  │                                                               │          │
│  │  With config cache (subsequent runs):                         │          │
│  │  ┌───────────────────────────────────────────────────┐       │          │
│  │  │ Load cached Task Graph → Execute                   │       │          │
│  │  │ (Skip configuration entirely! 0ms)                 │       │          │
│  │  └───────────────────────────────────────────────────┘       │          │
│  │                                                               │          │
│  │  Restrictions: Build scripts must be "configuration-safe":    │          │
│  │  • Cannot read System properties at configuration time        │          │
│  │  • Cannot use Task.project reference in task actions           │          │
│  │  • Must use Provider API for lazy evaluation                  │          │
│  └───────────────────────────────────────────────────────────────┘          │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────┐          │
│  │           3. INCREMENTAL COMPILATION                          │          │
│  │                                                               │          │
│  │  What it does: Recompiles ONLY changed files + dependents     │          │
│  │  Example: Change 1 file → recompile 3 files (not 500)        │          │
│  │  Works for: Kotlin, Java, Android resources                   │          │
│  │                                                               │          │
│  │  Kotlin incremental compilation:                              │          │
│  │  ┌─────────────────────────────────────┐                     │          │
│  │  │ Change A.kt                         │                     │          │
│  │  │   ↓ B.kt depends on A.kt           │                     │          │
│  │  │   ↓ Recompile: A.kt + B.kt only    │                     │          │
│  │  │   ↓ C.kt, D.kt, E.kt = SKIPPED    │                     │          │
│  │  └─────────────────────────────────────┘                     │          │
│  └───────────────────────────────────────────────────────────────┘          │
│                                                                             │
│  Combined Impact on Build Speed:                                            │
│  ┌──────────────┬──────────────┬────────────┬─────────────────┐            │
│  │  Feature      │ First Build │ No Changes │ Small Change    │            │
│  ├──────────────┼──────────────┼────────────┼─────────────────┤            │
│  │ None          │ 60s          │ 20s        │ 25s             │            │
│  │ + Build Cache │ 60s          │ 5s         │ 10s             │            │
│  │ + Config Cache│ 60s          │ 2s         │ 7s              │            │
│  │ + Incremental │ 60s          │ 1s         │ 4s              │            │
│  │ + Parallel    │ 40s          │ 1s         │ 3s              │            │
│  └──────────────┴──────────────┴────────────┴─────────────────┘            │
└─────────────────────────────────────────────────────────────────────────────┘
```

```properties
# gradle.properties — Enable caching

# Build cache — cache task outputs
org.gradle.caching=true

# Configuration cache — cache task graph
org.gradle.configuration-cache=true

# Parallel execution — run independent tasks in parallel
org.gradle.parallel=true

# Daemon — keep Gradle JVM warm between builds
org.gradle.daemon=true

# JVM memory settings
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC -XX:MaxMetaspaceSize=1g

# File system watching — detect changes efficiently
org.gradle.vfs.watch=true
```

```kotlin
// settings.gradle.kts — Remote build cache setup
buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, ".gradle/build-cache")
        removeUnusedEntriesAfterDays = 7
    }
    remote<HttpBuildCache> {
        url = uri("https://your-cache-server.com/cache/")
        isEnabled = true
        isPush = System.getenv("CI") != null  // Only push from CI
        credentials {
            username = System.getenv("CACHE_USER") ?: ""
            password = System.getenv("CACHE_PASS") ?: ""
        }
    }
}
```

---

### Composite Builds

```
┌──────────────────────────────────────────────────────────────────────┐
│                    COMPOSITE BUILDS                                  │
│                                                                      │
│  Combine multiple independent Gradle projects into one build         │
│                                                                      │
│  ┌─────────────┐  includeBuild  ┌──────────────────┐               │
│  │  Main App   │◀──────────────│  Shared Library   │               │
│  │  Project    │                │  (separate repo)  │               │
│  │             │  includeBuild  ├──────────────────┤               │
│  │             │◀──────────────│  Build Plugins    │               │
│  │             │                │  (build-logic)    │               │
│  └─────────────┘                └──────────────────┘               │
│                                                                      │
│  Benefits:                                                           │
│  • Develop library + app simultaneously                              │
│  • No need to publish library to test changes                        │
│  • Substitute published dependency with local source                 │
│  • Share build logic via includeBuild("build-logic")                 │
└──────────────────────────────────────────────────────────────────────┘
```

```kotlin
// settings.gradle.kts — Composite build setup

pluginManagement {
    // Include build-logic as a composite build for convention plugins
    includeBuild("build-logic")
}

// Include an external library project for local development
includeBuild("../shared-networking-lib") {
    dependencySubstitution {
        // When the app depends on the published artifact,
        // substitute it with the local project
        substitute(module("com.example:networking-lib"))
            .using(project(":networking"))
    }
}

rootProject.name = "PracticeApp"
include(":app")
include(":core")
include(":feature:home")
```

```kotlin
// build-logic/settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "build-logic"
include(":convention")

// build-logic/convention/build.gradle.kts
plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApp") {
            id = "myapp.android.application"
            implementationClass = "AndroidAppConventionPlugin"
        }
        register("androidLib") {
            id = "myapp.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "myapp.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
    }
}
```

---

# 11.2 CI/CD Pipelines

## What is CI/CD?

**CI (Continuous Integration):** Automatically build and test code every time a developer pushes
changes. The goal is to catch bugs EARLY — within minutes of introduction, not days later.

**CD (Continuous Delivery):** Automatically package, sign, and prepare releases for deployment.
The release to production may still require manual approval ("delivery") or be fully automated
("deployment").

**Why CI/CD for Android?**
- Android builds are **slow** (2-15 minutes) — developers shouldn't wait manually
- Testing on multiple API levels and devices is impractical locally
- Signing with release keys should happen in a secure environment (not developer machines)
- Play Store uploads can be automated, eliminating human error
- Enforces code quality gates (lint, tests must pass before merge)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                    CI/CD PIPELINE — DETAILED FLOW                            │
│                                                                              │
│  Developer                                                                   │
│     │                                                                        │
│     │ git push / Pull Request                                                │
│     ▼                                                                        │
│  ┌──────────┐    ┌───────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │  SOURCE  │───▶│    BUILD      │───▶│    TEST      │───▶│   DEPLOY     │  │
│  │  CONTROL │    │               │    │              │    │              │  │
│  │          │    │ • Compile     │    │ • Lint       │    │ • Sign       │  │
│  │ Git Push │    │ • Resolve     │    │ • Unit Tests │    │ • Upload AAB │  │
│  │ or PR    │    │   deps        │    │ • UI Tests   │    │ • Release    │  │
│  │ Webhook  │    │ • Generate    │    │ • Coverage   │    │   notes      │  │
│  │ triggers │    │   BuildConfig │    │ • Security   │    │ • Staged     │  │
│  │ pipeline │    │ • Package     │    │   scan       │    │   rollout    │  │
│  └──────────┘    │   APK/AAB    │    │ • Firebase   │    │ • Notify     │  │
│                  └───────────────┘    │   Test Lab   │    │   team       │  │
│                                      └──────────────┘    └──────────────┘  │
│       │               │                    │                    │           │
│       ▼               ▼                    ▼                    ▼           │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      FEEDBACK & QUALITY GATES                       │    │
│  │                                                                     │    │
│  │  ┌─────────┐  ┌──────────┐  ┌───────────┐  ┌─────────────────┐   │    │
│  │  │ PR      │  │ Slack /  │  │ Test      │  │ Build Artifacts │   │    │
│  │  │ Status  │  │ Teams    │  │ Reports   │  │ (APK download)  │   │    │
│  │  │ Check   │  │ Notif.   │  │ HTML /    │  │                 │   │    │
│  │  │ ✓ or ✗  │  │          │  │ Coverage  │  │                 │   │    │
│  │  └─────────┘  └──────────┘  └───────────┘  └─────────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  Quality Gates (block merge if ANY fails):                                   │
│  ┌──────────────────────────────────────────────────────────────────────┐    │
│  │ Gate 1: Build succeeds                     (no compile errors)       │    │
│  │ Gate 2: Lint passes                        (no critical warnings)    │    │
│  │ Gate 3: Unit tests pass                    (100% pass rate)          │    │
│  │ Gate 4: Code coverage ≥ threshold          (e.g., ≥ 80%)            │    │
│  │ Gate 5: No new security vulnerabilities    (dependency scan)         │    │
│  │ Gate 6: Code review approved               (at least 1 approval)    │    │
│  └──────────────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────────────┘
```

**Key CI/CD Concepts:**

| Concept | Definition | Android Example |
|---------|-----------|-----------------|
| **Pipeline** | Sequence of automated stages | Build → Test → Deploy |
| **Job** | A unit of work in a stage | `unit-tests`, `lint`, `build-apk` |
| **Step** | A single command within a job | `./gradlew testDebugUnitTest` |
| **Artifact** | A file produced by a job | Debug APK, test reports |
| **Secret** | Encrypted env variable | Keystore password, API keys |
| **Matrix** | Run same job with different params | Test on API 28, 31, 34 |
| **Cache** | Persisted files between runs | Gradle caches, downloaded deps |
| **Trigger** | Event that starts a pipeline | Push, PR, tag, schedule, manual |

---

### GitHub Actions for Android

```yaml
# .github/workflows/android-ci.yml
name: Android CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

# Cancel in-progress runs for the same branch
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

env:
  JAVA_VERSION: '17'
  GRADLE_OPTS: "-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true"

jobs:
  # ── Job 1: Lint & Build ──
  build:
    name: Build & Lint
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/main' }}

      - name: Run Lint
        run: ./gradlew lintDebug

      - name: Build Debug APK
        run: ./gradlew assembleDebug

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 7

      - name: Upload Lint results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: lint-results
          path: app/build/reports/lint-results-debug.html

  # ── Job 2: Unit Tests ──
  unit-tests:
    name: Unit Tests
    runs-on: ubuntu-latest
    needs: build
    timeout-minutes: 20

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - uses: gradle/actions/setup-gradle@v3

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: unit-test-results
          path: app/build/reports/tests/

  # ── Job 3: Instrumentation Tests ──
  instrumentation-tests:
    name: Instrumentation Tests
    runs-on: ubuntu-latest
    needs: build
    timeout-minutes: 45

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Enable KVM (for Android emulator)
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm

      - name: Run instrumentation tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 31
          arch: x86_64
          profile: Nexus 6
          emulator-options: -no-snapshot-save -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim
          script: ./gradlew connectedDebugAndroidTest

      - name: Upload instrumentation results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: instrumentation-test-results
          path: app/build/reports/androidTests/

  # ── Job 4: Release (only on main) ──
  release:
    name: Build Release
    runs-on: ubuntu-latest
    needs: [unit-tests, instrumentation-tests]
    if: github.ref == 'refs/heads/main'

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - uses: gradle/actions/setup-gradle@v3

      - name: Decode Keystore
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: echo "$KEYSTORE_BASE64" | base64 -d > app/release-keystore.jks

      - name: Build Release Bundle
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew bundleRelease

      - name: Upload AAB
        uses: actions/upload-artifact@v4
        with:
          name: release-aab
          path: app/build/outputs/bundle/release/*.aab
```

---

### GitLab CI/CD

```yaml
# .gitlab-ci.yml
image: "eclipse-temurin:17-jdk"

variables:
  ANDROID_SDK_ROOT: "/opt/android-sdk"
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"

stages:
  - build
  - test
  - deploy

cache:
  key: ${CI_COMMIT_REF_SLUG}
  paths:
    - .gradle/caches/
    - .gradle/wrapper/
  policy: pull-push

before_script:
  - chmod +x ./gradlew

# ── Build Stage ──
build:debug:
  stage: build
  script:
    - ./gradlew assembleDebug
  artifacts:
    paths:
      - app/build/outputs/apk/debug/
    expire_in: 1 week

lint:
  stage: build
  script:
    - ./gradlew lintDebug
  artifacts:
    paths:
      - app/build/reports/lint-results-debug.html
    expire_in: 1 week
  allow_failure: true

# ── Test Stage ──
unit-tests:
  stage: test
  script:
    - ./gradlew testDebugUnitTest
  artifacts:
    when: always
    reports:
      junit: app/build/test-results/testDebugUnitTest/*.xml
    paths:
      - app/build/reports/tests/
    expire_in: 1 week

# ── Deploy Stage ──
deploy:internal:
  stage: deploy
  only:
    - main
  script:
    - echo $KEYSTORE_FILE | base64 -d > keystore.jks
    - ./gradlew bundleRelease
    - ./gradlew publishBundle  # Using Gradle Play Publisher
  artifacts:
    paths:
      - app/build/outputs/bundle/release/
    expire_in: 1 month
  when: manual  # Require manual trigger for deploy
```

---

### Bitrise

```yaml
# bitrise.yml
format_version: "11"
default_step_lib_source: https://github.com/bitrise-io/bitrise-steplib.git

app:
  envs:
    - GRADLE_BUILD_FILE_PATH: build.gradle.kts
    - GRADLEW_PATH: ./gradlew
    - MODULE: app
    - VARIANT: debug

workflows:
  primary:
    steps:
      - activate-ssh-key@4: {}
      - git-clone@8: {}
      - install-missing-android-tools@3:
          inputs:
            - gradlew_path: $GRADLEW_PATH
      - android-lint@0:
          inputs:
            - module: $MODULE
            - variant: $VARIANT
      - android-unit-test@1:
          inputs:
            - module: $MODULE
            - variant: $VARIANT
      - android-build@1:
          inputs:
            - module: $MODULE
            - variant: $VARIANT
      - deploy-to-bitrise-io@2: {}

  deploy:
    steps:
      - activate-ssh-key@4: {}
      - git-clone@8: {}
      - android-build@1:
          inputs:
            - module: $MODULE
            - variant: release
            - build_type: aab
      - sign-apk@1: {}
      - google-play-deploy@3:
          inputs:
            - service_account_json_key_path: $GOOGLE_PLAY_KEY
            - package_name: com.example.practiceapp
            - track: internal
```

---

### CircleCI

```yaml
# .circleci/config.yml
version: 2.1

orbs:
  android: circleci/android@2.5.0

executors:
  android-executor:
    docker:
      - image: cimg/android:2024.01
    resource_class: large
    environment:
      GRADLE_OPTS: "-Dorg.gradle.daemon=false -Dorg.gradle.workers.max=2"
      TERM: dumb

jobs:
  build-and-test:
    executor: android-executor
    steps:
      - checkout

      - restore_cache:
          keys:
            - gradle-v2-{{ checksum "build.gradle.kts" }}-{{ checksum "app/build.gradle.kts" }}
            - gradle-v2-

      - run:
          name: Build Debug
          command: ./gradlew assembleDebug

      - run:
          name: Run Lint
          command: ./gradlew lintDebug

      - run:
          name: Run Unit Tests
          command: ./gradlew testDebugUnitTest

      - save_cache:
          paths:
            - ~/.gradle/caches
            - ~/.gradle/wrapper
          key: gradle-v2-{{ checksum "build.gradle.kts" }}-{{ checksum "app/build.gradle.kts" }}

      - store_test_results:
          path: app/build/test-results

      - store_artifacts:
          path: app/build/outputs/apk/debug
          destination: apk

      - store_artifacts:
          path: app/build/reports
          destination: reports

  deploy:
    executor: android-executor
    steps:
      - checkout
      - run:
          name: Build Release Bundle
          command: |
            echo $KEYSTORE_BASE64 | base64 -d > app/keystore.jks
            ./gradlew bundleRelease
      - store_artifacts:
          path: app/build/outputs/bundle/release
          destination: aab

workflows:
  build-test-deploy:
    jobs:
      - build-and-test
      - deploy:
          requires:
            - build-and-test
          filters:
            branches:
              only: main
```

---

### Jenkins for Android

```groovy
// Jenkinsfile (Declarative Pipeline)
pipeline {
    agent {
        docker {
            image 'eclipse-temurin:17-jdk'
            args '-v $HOME/.gradle:/root/.gradle'
        }
    }

    environment {
        ANDROID_SDK_ROOT = '/opt/android-sdk'
        GRADLE_OPTS = '-Dorg.gradle.daemon=false'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'chmod +x gradlew'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew assembleDebug'
            }
        }

        stage('Lint') {
            steps {
                sh './gradlew lintDebug'
            }
            post {
                always {
                    publishHTML(target: [
                        reportDir: 'app/build/reports/',
                        reportFiles: 'lint-results-debug.html',
                        reportName: 'Lint Report'
                    ])
                }
            }
        }

        stage('Unit Tests') {
            steps {
                sh './gradlew testDebugUnitTest'
            }
            post {
                always {
                    junit 'app/build/test-results/**/*.xml'
                }
            }
        }

        stage('Build Release') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([
                    file(credentialsId: 'android-keystore', variable: 'KEYSTORE_FILE'),
                    string(credentialsId: 'keystore-password', variable: 'KEYSTORE_PASSWORD'),
                    string(credentialsId: 'key-alias', variable: 'KEY_ALIAS'),
                    string(credentialsId: 'key-password', variable: 'KEY_PASSWORD')
                ]) {
                    sh '''
                        cp $KEYSTORE_FILE app/release-keystore.jks
                        ./gradlew bundleRelease
                    '''
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'app/build/outputs/bundle/release/*.aab'
                }
            }
        }
    }

    post {
        failure {
            // Notify team on failure (e.g., Slack, email)
            echo 'Build failed! Sending notifications...'
        }
        cleanup {
            cleanWs()
        }
    }
}
```

---

### CI/CD Platform Comparison

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                     CI/CD PLATFORM COMPARISON — DETAILED                       │
│                                                                                │
│  Feature         │ GitHub    │ GitLab   │ Bitrise  │ CircleCI  │ Jenkins      │
│                  │ Actions   │ CI/CD    │          │           │              │
│  ────────────────┼───────────┼──────────┼──────────┼───────────┼───────────── │
│  Hosting         │ Cloud     │ Cloud/   │ Cloud    │ Cloud     │ Self-hosted  │
│                  │           │ Self-host│          │           │              │
│  Free Tier       │ 2000min/  │ 400min/  │ Limited  │ 6000min/  │ Free (OSS)   │
│                  │ month     │ month    │          │ month     │ pay infra    │
│  Android Focus   │ Good      │ Good     │ Best     │ Good      │ DIY          │
│  Config File     │ YAML      │ YAML     │ YAML     │ YAML      │ Groovy       │
│  Config Location │ .github/  │ .gitlab- │ bitrise  │ .circleci/│ Jenkinsfile  │
│                  │ workflows/│ ci.yml   │ .yml     │ config.yml│              │
│  Emulator        │ Plugin    │ Docker   │ Built-in │ Orb       │ Plugin       │
│  Setup Effort    │ Low       │ Low      │ Low      │ Medium    │ High         │
│  Flexibility     │ High      │ High     │ Medium   │ High      │ Highest      │
│  Play Store      │ Plugin    │ Script   │ Step     │ Orb       │ Plugin       │
│  Secrets Mgmt    │ Built-in  │ Built-in │ Built-in │ Built-in  │ Credentials  │
│  Marketplace     │ 20K+      │ 500+     │ 300+     │ 3K+       │ 1800+        │
│                  │ actions   │ templates│ steps    │ orbs      │ plugins      │
│  macOS Runners   │ ✓ (paid)  │ ✗        │ ✓        │ ✓ (paid)  │ Self-host    │
│  PR Integration  │ Native    │ Native   │ Manual   │ Good      │ Plugin       │
│                  │                                                             │
│  BEST FOR:                                                                     │
│  ─────────────────────────────────────────────────────────────────────────────  │
│  GitHub Actions  → Teams already using GitHub (most common choice)             │
│  GitLab CI/CD    → Teams using GitLab for full DevOps platform                 │
│  Bitrise         → Mobile-first teams wanting drag-and-drop setup              │
│  CircleCI        → Teams needing fast build times and advanced caching          │
│  Jenkins         → Enterprise teams needing full control & custom infra        │
└────────────────────────────────────────────────────────────────────────────────┘
```

**Decision Flowchart:**

```
┌──────────────────────────────────────────────────────────────────┐
│  WHICH CI/CD SHOULD YOU CHOOSE?                                  │
│                                                                  │
│  Using GitHub for source code?                                   │
│    ├── YES → GitHub Actions (easiest integration)                │
│    └── NO                                                        │
│         ├── Using GitLab? → GitLab CI/CD                         │
│         └── NO                                                   │
│              ├── Need mobile-specific features? → Bitrise        │
│              ├── Need enterprise/on-prem? → Jenkins              │
│              └── Need fastest builds? → CircleCI                 │
└──────────────────────────────────────────────────────────────────┘
```

---

# 11.3 Automated Testing in CI

## Why Automate Tests in CI?

Running tests manually is unreliable — developers skip them, forget to run on the right variants,
or test on limited device configurations. CI ensures **every code change** is tested automatically
before merging, catching regressions in minutes instead of days.

**The Testing Tax:** Without CI, a team of 5 developers spending 10 minutes each running tests
before every PR = 250 minutes/day wasted (4+ hours). CI runs tests in parallel, freeing developers
to code while tests execute.

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    TESTING PYRAMID IN CI/CD                              │
│                                                                          │
│                           ╱╲                                            │
│                          ╱  ╲        E2E / UI Tests                     │
│                         ╱    ╲       • Slowest (5-30 min)               │
│                        ╱ E2E  ╲      • Most expensive                   │
│                       ╱────────╲     • Firebase Test Lab                │
│                      ╱          ╲    • Real devices                     │
│                     ╱ Integration╲   • Run on main/release only         │
│                    ╱    Tests     ╲  • Catch: real user flow bugs       │
│                   ╱────────────────╲                                     │
│                  ╱                  ╲                                    │
│                 ╱    Unit Tests      ╲  • Fastest (seconds)             │
│                ╱    (Foundation)      ╲ • Run on EVERY commit           │
│               ╱    70-80% of tests    ╲ • JVM only (no device)         │
│              ╱────────────────────────╲ • Catch: logic bugs            │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐      │
│  │ TYPE            │ WHERE IT RUNS │ SPEED    │ WHEN TO RUN       │      │
│  ├─────────────────┼───────────────┼──────────┼───────────────────┤      │
│  │ Unit Tests      │ JVM (no device│ Seconds  │ Every push/PR     │      │
│  │                 │  needed)      │          │                   │      │
│  │ Integration     │ JVM or        │ Minutes  │ Every PR          │      │
│  │ Tests           │ Emulator      │          │                   │      │
│  │ Instrumentation │ Emulator /    │ 5-20 min │ Merge to main     │      │
│  │ Tests           │ Real device   │          │                   │      │
│  │ E2E / UI Tests  │ Firebase Test │ 10-30min │ Before release    │      │
│  │                 │ Lab           │          │                   │      │
│  └────────────────────────────────────────────────────────────────┘      │
│                                                                          │
│  Cost Comparison:                                                        │
│  ┌────────────────────────────────────────────────────────────────┐      │
│  │  1000 Unit Tests on JVM:        ~30 seconds,  $0.001/run      │      │
│  │  100 Instrumentation on emulator: ~10 minutes, $0.05/run      │      │
│  │  50 E2E on Firebase Test Lab:    ~20 minutes, $5.00/run       │      │
│  │                                                                │      │
│  │  Strategy: Run cheap tests often, expensive tests selectively  │      │
│  └────────────────────────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────────────────────────┘
```

**Practical CI Testing Strategy:**

```
┌──────────────────────────────────────────────────────────────────────┐
│           WHEN TO RUN WHAT — PRACTICAL STRATEGY                      │
│                                                                      │
│  Event              │ What to Run                                    │
│  ───────────────────┼────────────────────────────────────────────    │
│  Every push         │ Lint + Unit Tests (fast feedback)              │
│  Pull Request       │ Lint + Unit + Build APK + Coverage check       │
│  Merge to develop   │ Above + Instrumentation Tests on emulator     │
│  Merge to main      │ Above + Firebase Test Lab (multi-device)       │
│  Tag (release)      │ Full test suite + Build AAB + Sign + Deploy   │
│  Nightly (cron)     │ Full test suite + Security scan + Upgrade deps│
│                                                                      │
│  PR opened                                                           │
│    │                                                                 │
│    ├──▶ [Lint]     ──┐                                               │
│    ├──▶ [Unit Test] ─┤── All run in PARALLEL                         │
│    ├──▶ [Build APK] ─┘                                               │
│    │        │                                                        │
│    │        ▼ (all pass?)                                             │
│    │   ┌────────────────┐                                            │
│    │   │ Instrumentation│  (runs only if fast checks pass)           │
│    │   │ Tests           │                                            │
│    │   └────────┬───────┘                                            │
│    │            ▼                                                     │
│    │   ┌────────────────┐                                            │
│    │   │ PR Status: ✓   │  → Ready for code review                   │
│    │   └────────────────┘                                            │
│    │                                                                 │
│    └── If ANY fails → PR Status: ✗ (block merge)                     │
└──────────────────────────────────────────────────────────────────────┘
```

---

### Running Unit and Instrumentation Tests

```yaml
# GitHub Actions — Complete test workflow
name: Android Tests

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: gradle/actions/setup-gradle@v3

      # Run unit tests for all modules
      - name: Unit Tests
        run: ./gradlew testDebugUnitTest

      # Generate combined coverage report
      - name: Generate Coverage Report
        run: ./gradlew jacocoTestReport

      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          files: app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml

      - name: Publish Test Results
        uses: EnricoMi/publish-unit-test-result-action@v2
        if: always()
        with:
          files: '**/build/test-results/**/*.xml'
          comment_mode: 'create new'

  instrumentation-tests:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        api-level: [28, 31, 34]    # Test on multiple API levels
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Enable KVM
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm

      - name: Run Instrumentation Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          arch: x86_64
          emulator-options: -no-snapshot-save -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim
          disable-animations: true
          script: ./gradlew connectedDebugAndroidTest
```

**Gradle Test Configuration:**

```kotlin
// app/build.gradle.kts
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true  // For Robolectric
            isReturnDefaultValues = true

            all {
                it.maxHeapSize = "1024m"
                it.jvmArgs("-noverify")

                // Retry flaky tests
                it.retry {
                    maxRetries = 2
                    maxFailures = 5
                    failOnPassedAfterRetry = false
                }

                // Test logging
                it.testLogging {
                    events("passed", "skipped", "failed", "standardOut", "standardError")
                    showExceptions = true
                    showStackTraces = true
                }
            }
        }

        // Instrumentation test options
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

// For test orchestrator (isolates each test in its own Instrumentation)
dependencies {
    androidTestUtil("androidx.test:orchestrator:1.4.2")
}

// JaCoCo code coverage
plugins {
    jacoco
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*",
        "**/Manifest*.*", "**/*Test*.*", "**/Hilt_*.*"
    )

    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    classDirectories.setFrom(
        fileTree("build/tmp/kotlin-classes/debug") { exclude(fileFilter) }
    )
    executionData.setFrom(
        fileTree("build") { include("jacoco/testDebugUnitTest.exec") }
    )
}
```

---

### Firebase Test Lab Integration

Firebase Test Lab is Google's cloud-based infrastructure for testing Android apps on **real physical
devices and virtual devices** hosted in Google data centers. It solves the "works on my phone"
problem by running tests across dozens of device/OS combinations simultaneously.

**Why Firebase Test Lab over local emulators?**

| Aspect | Local Emulator | Firebase Test Lab |
|--------|---------------|-------------------|
| Device variety | 1-2 configs | 100+ real devices |
| API levels | What you install | 21-34+ always available |
| Speed | Sequential | Parallel (all devices at once) |
| Flakiness | Emulator quirks | Real device behavior |
| Screenshots | Manual | Automatic per test |
| Video recording | No | Yes, per test session |
| Cost | Free | Pay per device-minute |
| Setup in CI | Complex (KVM, emulator boot) | Simple (upload APK, run) |

```
┌──────────────────────────────────────────────────────────────────────────┐
│                   FIREBASE TEST LAB — ARCHITECTURE                       │
│                                                                          │
│  ┌──────────┐   Upload APK    ┌────────────────────────────────┐        │
│  │ CI/CD    │────────────────▶│      Firebase Test Lab          │        │
│  │ Pipeline │   + Test APK    │                                │        │
│  └──────────┘                 │  ┌──────────────────────────┐  │        │
│                               │  │   Device Farm             │  │        │
│                               │  │                          │  │        │
│                               │  │  ┌──────┐ ┌──────┐      │  │        │
│                               │  │  │Pixel │ │Pixel │      │  │        │
│                               │  │  │  6   │ │  7   │ ...  │  │        │
│                               │  │  │API 33│ │API 34│      │  │        │
│                               │  │  └──────┘ └──────┘      │  │        │
│                               │  │  ┌──────┐ ┌──────┐      │  │        │
│                               │  │  │Samsung│ │Galaxy│      │  │        │
│                               │  │  │S23   │ │A54  │ ...  │  │        │
│                               │  │  │API 33│ │API 33│      │  │        │
│                               │  │  └──────┘ └──────┘      │  │        │
│                               │  └──────────────────────────┘  │        │
│                               │                                │        │
│                               │  Runs ALL devices in PARALLEL  │        │
│                               └────────────┬───────────────────┘        │
│                                            │                            │
│                               ┌────────────▼───────────────────┐        │
│                               │      Test Results               │        │
│                               │                                │        │
│                               │  Per Device:                   │        │
│                               │  ├── Pass/Fail summary         │        │
│                               │  ├── Video recording           │        │
│                               │  ├── Screenshots               │        │
│                               │  ├── Logcat output             │        │
│                               │  ├── Performance metrics       │        │
│                               │  └── Crash stack traces        │        │
│                               │                                │        │
│                               │  Stored in Google Cloud Storage│        │
│                               └────────────────────────────────┘        │
│                                                                          │
│  Three Test Types:                                                       │
│  ┌────────────────────────────────────────────────────────────────┐      │
│  │  1. INSTRUMENTATION — Your Espresso/Compose tests             │      │
│  │     You wrote the tests. FTL runs them on real devices.       │      │
│  │     Requires: app APK + test APK                              │      │
│  │                                                               │      │
│  │  2. ROBO TEST — AI-driven automatic UI exploration            │      │
│  │     No test code needed! A bot crawls your app, tapping       │      │
│  │     buttons, entering text, and looking for crashes.          │      │
│  │     Great for: smoke testing, finding crash paths             │      │
│  │     Requires: app APK only                                    │      │
│  │                                                               │      │
│  │  3. GAME LOOP — For game apps using Unity/Unreal              │      │
│  │     Runs predefined game scenarios via intents                │      │
│  └────────────────────────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────────────────────────┘
```

```yaml
# GitHub Actions — Firebase Test Lab integration
name: Firebase Test Lab

on:
  push:
    branches: [main]

jobs:
  firebase-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: gradle/actions/setup-gradle@v3

      # Build both the app APK and test APK
      - name: Build APKs
        run: |
          ./gradlew assembleDebug
          ./gradlew assembleDebugAndroidTest

      # Authenticate with Google Cloud
      - name: Authenticate to Google Cloud
        uses: google-github-actions/auth@v2
        with:
          credentials_json: ${{ secrets.GCP_SERVICE_ACCOUNT_KEY }}

      - name: Set up Cloud SDK
        uses: google-github-actions/setup-gcloud@v2

      # Run tests on Firebase Test Lab
      - name: Run Instrumentation Tests on Firebase Test Lab
        run: |
          gcloud firebase test android run \
            --type instrumentation \
            --app app/build/outputs/apk/debug/app-debug.apk \
            --test app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
            --device model=Pixel6,version=33,locale=en,orientation=portrait \
            --device model=Pixel7,version=34,locale=en,orientation=portrait \
            --timeout 10m \
            --results-bucket your-ftl-results-bucket \
            --results-dir "results-${{ github.run_id }}" \
            --num-flaky-test-attempts 2 \
            --environment-variables clearPackageData=true

      # Run Robo Test (no test code needed)
      - name: Run Robo Test
        run: |
          gcloud firebase test android run \
            --type robo \
            --app app/build/outputs/apk/debug/app-debug.apk \
            --device model=Pixel6,version=33 \
            --timeout 5m \
            --robo-directives "text:username_field=testuser,text:password_field=testpass123"
```

**Firebase Test Lab with Gradle Plugin:**

```kotlin
// app/build.gradle.kts
plugins {
    id("com.google.firebase.testlab") version "0.0.1-alpha05"
}

firebaseTestLab {
    // Service account for authentication
    serviceAccountCredentials.set(file("firebase-service-account.json"))

    devices {
        create("pixel6api33") {
            device = "oriole"
            apiLevel = 33
        }
        create("pixel7api34") {
            device = "cheetah"
            apiLevel = 34
        }
    }
}
```

---

### Test Reporting and Analytics

```kotlin
// app/build.gradle.kts — Test reporting plugins

// JUnit5 with detailed reporting
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()

    // HTML & XML reports
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }

    // Console output formatting
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    // Fail build if tests fail
    ignoreFailures = false

    // Run tests in parallel
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
}

// Aggregate test reports across all modules
tasks.register<TestReport>("allTestReport") {
    destinationDirectory.set(file("$buildDir/reports/allTests"))
    testResults.from(
        subprojects.map { it.tasks.withType<Test>().map { t -> t.binaryResultsDirectory } }
    )
}
```

```yaml
# GitHub Actions — Test reporting with annotations
- name: Publish Test Results
  uses: EnricoMi/publish-unit-test-result-action@v2
  if: always()
  with:
    files: |
      **/build/test-results/**/*.xml
      **/build/outputs/androidTest-results/**/*.xml
    comment_mode: 'create new'
    check_name: 'Test Results'

# Danger for PR test analysis
- name: Run Danger
  uses: danger/danger-js@v12
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

```
┌──────────────────────────────────────────────────────────────────────────┐
│                      TEST REPORTING FLOW — DETAILED                      │
│                                                                          │
│  Test Execution                                                          │
│       │                                                                  │
│       ▼                                                                  │
│  ┌──────────────────────┐                                                │
│  │ JUnit XML Output     │  Machine-readable test results                 │
│  │ build/test-results/  │  Standard format all CI systems understand     │
│  └────────┬─────────────┘                                                │
│           │                                                              │
│     ┌─────┼──────────┬──────────────┬───────────────┐                    │
│     ▼     ▼          ▼              ▼               ▼                    │
│  ┌──────┐ ┌────────┐ ┌──────────┐ ┌──────────┐ ┌────────────────┐       │
│  │ HTML │ │ PR     │ │ Coverage │ │ Flaky    │ │ Analytics      │       │
│  │Report│ │ Comment│ │ (JaCoCo) │ │ Test     │ │ Dashboard      │       │
│  │      │ │        │ │          │ │ Detection│ │                │       │
│  │Browse│ │ GitHub │ │ Codecov  │ │          │ │ Grafana /      │       │
│  │local │ │ check  │ │ Coveralls│ │ Auto-    │ │ DataDog        │       │
│  │      │ │ annot. │ │ Sonar    │ │ retry    │ │                │       │
│  └──────┘ └────────┘ └──────────┘ └──────────┘ └────────────────┘       │
│                                                                          │
│  Key Metrics to Track Over Time:                                         │
│  ┌────────────────────────────────────────────────────────────────┐      │
│  │  Metric              │ Target            │ Alert If             │      │
│  │  ────────────────────┼───────────────────┼──────────────────── │      │
│  │  Pass Rate           │ 100%              │ < 95%                │      │
│  │  Code Coverage       │ ≥ 80%             │ Drops > 5% in PR     │      │
│  │  Test Duration       │ < 10 min (unit)   │ Increases > 20%      │      │
│  │  Flaky Test Count    │ 0                 │ > 3 flaky tests      │      │
│  │  Test Count Trend    │ Growing           │ Decreasing           │      │
│  │  Build Success Rate  │ ≥ 95%             │ < 90%                │      │
│  └────────────────────────────────────────────────────────────────┘      │
│                                                                          │
│  Handling Flaky Tests (tests that sometimes pass, sometimes fail):       │
│  ┌────────────────────────────────────────────────────────────────┐      │
│  │  1. Detect: Use retry mechanism — if test fails then passes   │      │
│  │     on retry, flag it as flaky (don't fail the build)         │      │
│  │  2. Quarantine: Move flaky tests to a separate suite          │      │
│  │  3. Fix: Prioritize fixing flaky tests (they erode trust)     │      │
│  │  4. Prevent: Use idling resources, disable animations in CI   │      │
│  │  5. Monitor: Track flaky test trends in analytics dashboard   │      │
│  └────────────────────────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────────────────────────┘
```

---

# 11.4 Release Management

## Overview

Release management is the process of getting your app from source code to users' devices securely
and reliably. In Android, this involves **signing** (proving you own the app), **uploading** to
Google Play, and **rolling out** to users in a controlled way.

**The Release Pipeline at a Glance:**

```
┌──────────────────────────────────────────────────────────────────────────┐
│                  ANDROID RELEASE PIPELINE                                │
│                                                                          │
│  Source Code                                                             │
│     │                                                                    │
│     ▼                                                                    │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐            │
│  │ Version  │──▶│  Build   │──▶│  Sign    │──▶│ Upload   │            │
│  │ Bump     │   │  AAB     │   │  with    │   │ to Play  │            │
│  │          │   │  (not    │   │  Upload  │   │ Console  │            │
│  │ vCode++  │   │   APK!)  │   │  Key     │   │          │            │
│  │ vName    │   │          │   │          │   │          │            │
│  └──────────┘   └──────────┘   └──────────┘   └─────┬────┘            │
│                                                      │                  │
│                                                      ▼                  │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                    GOOGLE PLAY TRACKS                            │    │
│  │                                                                 │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │    │
│  │  │ Internal │─▶│  Closed  │─▶│   Open   │─▶│ Production   │   │    │
│  │  │ Testing  │  │  Testing │  │  Testing │  │              │   │    │
│  │  │          │  │  (Alpha) │  │  (Beta)  │  │  Staged      │   │    │
│  │  │ • 100    │  │          │  │          │  │  Rollout     │   │    │
│  │  │   testers│  │ • By     │  │ • Public │  │              │   │    │
│  │  │ • No     │  │   email  │  │   opt-in │  │  1% → 5% →  │   │    │
│  │  │   review │  │   invite │  │ • Play   │  │  25% → 100% │   │    │
│  │  │ • Instant│  │ • Review │  │   Store  │  │              │   │    │
│  │  │          │  │   needed │  │   listing│  │ • Monitored  │   │    │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────────┘   │    │
│  │       │              │             │              │             │    │
│  │       └──────────────┴─────────────┴──────────────┘             │    │
│  │            "Promote" — move build between tracks                │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
│  APK vs AAB:                                                             │
│  ┌──────────────────────────────────────────────────────────────────┐    │
│  │  APK: Single file containing ALL code + resources for ALL       │    │
│  │       devices. User downloads everything (large file).          │    │
│  │                                                                  │    │
│  │  AAB: Container format. Google Play generates optimized APKs     │    │
│  │       per device (screen density, CPU architecture, language).   │    │
│  │       Users download 30-50% less data.                           │    │
│  │                                                                  │    │
│  │  Google Play REQUIRES AAB since August 2021 for new apps.        │    │
│  └──────────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────────┘
```

### Signing Configurations

**Why signing matters:** Android uses cryptographic signatures to verify the **identity of the app
developer** and ensure the app hasn't been tampered with. Every APK/AAB must be signed before it
can be installed. A user can only update an app if the new version is signed with the **same key**.

**Critical Rule:** If you lose your signing key, you can NEVER update your app. Users must
uninstall and reinstall (losing all data). This is why Google Play App Signing exists.

```
┌──────────────────────────────────────────────────────────────────────────┐
│                   APP SIGNING — COMPLETE FLOW                            │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │               TWO-KEY SYSTEM (Google Play App Signing)          │    │
│  │                                                                 │    │
│  │  YOU manage:                    GOOGLE manages:                 │    │
│  │  ┌────────────────┐            ┌────────────────────────┐      │    │
│  │  │   Upload Key   │            │   App Signing Key      │      │    │
│  │  │                │            │                        │      │    │
│  │  │ • Used to sign │            │ • Used to sign the     │      │    │
│  │  │   before upload│            │   FINAL APK that       │      │    │
│  │  │ • Can be reset │            │   users download       │      │    │
│  │  │   if compromised│           │ • Stored in Google's   │      │    │
│  │  │ • Stored in CI │            │   secure infrastructure│      │    │
│  │  │   secrets      │            │ • CANNOT be extracted  │      │    │
│  │  └───────┬────────┘            └───────────┬────────────┘      │    │
│  │          │                                 │                   │    │
│  │          ▼                                 ▼                   │    │
│  │  ┌──────────────────────────────────────────────────────┐      │    │
│  │  │ Developer signs AAB ──▶ Uploads to Play ──▶ Google   │      │    │
│  │  │ with Upload Key         Console              re-signs│      │    │
│  │  │                                              with App│      │    │
│  │  │                                              Signing │      │    │
│  │  │                                              Key     │      │    │
│  │  └──────────────────────────────────────────────────────┘      │    │
│  │                                                                 │    │
│  │  Benefits:                                                      │    │
│  │  • If Upload Key is compromised → reset it (app continues)     │    │
│  │  • If Upload Key is lost → request reset from Google            │    │
│  │  • Google optimizes APK signing per device (split APKs)         │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
│  Debug vs Release Signing:                                               │
│  ┌──────────────────────────────────────────────────────────────────┐    │
│  │  DEBUG:                                                          │    │
│  │  • Auto-generated at ~/.android/debug.keystore                   │    │
│  │  • Same on all developer machines (shared password: "android")   │    │
│  │  • Valid for 30 years, no security                               │    │
│  │  • Cannot upload to Play Store                                   │    │
│  │                                                                  │    │
│  │  RELEASE:                                                        │    │
│  │  • YOU generate with keytool command                             │    │
│  │  • Must be kept SECRET and BACKED UP                             │    │
│  │  • Validity: 25+ years recommended                               │    │
│  │  • Required for Play Store uploads                               │    │
│  │                                                                  │    │
│  │  Keystore file (.jks) contains:                                  │    │
│  │  ┌─────────────────────────────────┐                             │    │
│  │  │  Keystore Password (outer lock) │                             │    │
│  │  │  ┌──────────────────────────┐   │                             │    │
│  │  │  │  Key Alias: "my-app-key" │   │                             │    │
│  │  │  │  Key Password (inner)    │   │                             │    │
│  │  │  │  Private Key             │   │                             │    │
│  │  │  │  Certificate Chain       │   │                             │    │
│  │  │  └──────────────────────────┘   │                             │    │
│  │  └─────────────────────────────────┘                             │    │
│  └──────────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// app/build.gradle.kts — Signing configurations

android {
    signingConfigs {
        // Debug signing — auto-generated, used for development
        getByName("debug") {
            // Uses ~/.android/debug.keystore by default
            // Pre-configured, no changes needed
        }

        // Release signing — for production builds
        create("release") {
            // ✅ SECURE: Read from environment variables or local.properties
            // ❌ NEVER hardcode passwords in build files!

            val keystorePropsFile = rootProject.file("keystore.properties")
            if (keystorePropsFile.exists()) {
                val keystoreProps = java.util.Properties().apply {
                    load(keystorePropsFile.inputStream())
                }
                storeFile = file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            } else {
                // Fallback to environment variables (CI/CD)
                storeFile = file(System.getenv("KEYSTORE_PATH") ?: "release.keystore")
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

```properties
# keystore.properties (DO NOT commit to version control!)
# Add to .gitignore: keystore.properties
storeFile=../release-keystore.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

```bash
# Generate a release keystore
keytool -genkey -v \
  -keystore release-keystore.jks \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -alias my-app-key \
  -storepass your_store_password \
  -keypass your_key_password
```

---

### Google Play Console Integration

Google Play Console is the web dashboard where you manage your app's listing, releases, analytics,
and user feedback. For CI/CD, we interact with it programmatically via the **Google Play Developer
API** using a **service account**.

**Setting Up Service Account for CI/CD:**

```
┌──────────────────────────────────────────────────────────────────────────┐
│             GOOGLE PLAY API ACCESS SETUP                                │
│                                                                          │
│  Step 1: Google Cloud Console                                            │
│  ┌──────────────────────────────────────────────────────────┐            │
│  │ • Create a Google Cloud Project                          │            │
│  │ • Enable "Google Play Android Developer API"             │            │
│  │ • Create Service Account (IAM & Admin)                   │            │
│  │ • Download JSON key file                                  │            │
│  └──────────────────────────────────────────────────────────┘            │
│                    │                                                     │
│                    ▼                                                     │
│  Step 2: Google Play Console                                             │
│  ┌──────────────────────────────────────────────────────────┐            │
│  │ • Settings → API Access → Link Cloud Project            │            │
│  │ • Grant Service Account permissions:                     │            │
│  │   - "Release manager" (for publishing)                   │            │
│  │   - "View app information" (for reading)                 │            │
│  │ • Invite service account email to your app               │            │
│  └──────────────────────────────────────────────────────────┘            │
│                    │                                                     │
│                    ▼                                                     │
│  Step 3: CI/CD Secret                                                    │
│  ┌──────────────────────────────────────────────────────────┐            │
│  │ • Store JSON key as CI/CD secret (never commit to git!)  │            │
│  │ • Reference in Gradle Play Publisher plugin               │            │
│  └──────────────────────────────────────────────────────────┘            │
└──────────────────────────────────────────────────────────────────────────┘
```

**Release Tracks Explained:**

```
┌──────────────────────────────────────────────────────────────────────────┐
│             GOOGLE PLAY RELEASE TRACKS — DETAILED                        │
│                                                                          │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌─────────┐  │
│  │   Internal   │──▶│   Closed     │──▶│    Open      │──▶│  Prod   │  │
│  │   Testing    │   │   Testing    │   │   Testing    │   │         │  │
│  │              │   │   (Alpha)    │   │   (Beta)     │   │         │  │
│  ├──────────────┤   ├──────────────┤   ├──────────────┤   ├─────────┤  │
│  │ Testers: 100 │   │ Testers:     │   │ Testers:     │   │ All     │  │
│  │ max (email)  │   │ Unlimited    │   │ Anyone can   │   │ users   │  │
│  │              │   │ by email     │   │ opt in via   │   │         │  │
│  │ Review: NONE │   │ or Google    │   │ Play Store   │   │ Staged  │  │
│  │ (instant)    │   │ Groups       │   │ link         │   │ rollout │  │
│  │              │   │              │   │              │   │ support │  │
│  │ Use for:     │   │ Use for:     │   │ Use for:     │   │         │  │
│  │ Dev team     │   │ QA team      │   │ Public beta  │   │ 1%→100% │  │
│  │ Quick shares │   │ Stakeholders │   │ Early access │   │         │  │
│  └──────────────┘   └──────────────┘   └──────────────┘   └─────────┘  │
│                                                                          │
│  Version Code Rules:                                                     │
│  ┌──────────────────────────────────────────────────────────────────┐    │
│  │ • versionCode must ALWAYS increase across ALL tracks            │    │
│  │ • You cannot upload versionCode=5 if versionCode=6 exists      │    │
│  │   on ANY track (even internal)                                  │    │
│  │ • Tip: Use git commit count as versionCode for auto-increment   │    │
│  │ • versionName is for humans ("1.2.3"), versionCode for Play     │    │
│  └──────────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// app/build.gradle.kts — Google Play publishing with Gradle Play Publisher
plugins {
    id("com.github.triplet.play") version "3.9.0"
}

play {
    // Service account JSON for API access
    serviceAccountCredentials.set(file("play-service-account.json"))

    // Default track for publishing
    track.set("internal")    // internal, alpha, beta, production

    // Default release status
    defaultToAppBundles.set(true)  // Upload AAB instead of APK

    // Release name format
    releaseName.set("Build ${android.defaultConfig.versionCode}")

    // Update priority (0-5, how urgently users should update)
    updatePriority.set(3)

    // Artifact directory (if not using default)
    // artifactDir.set(file("custom/output/dir"))
}

// Available Gradle tasks after applying the plugin:
// ./gradlew publishBundle              — Upload AAB to configured track
// ./gradlew publishApk                 — Upload APK to configured track
// ./gradlew promoteArtifact            — Promote from one track to another
// ./gradlew publishListing             — Update Play Store listing
// ./gradlew publishReleaseNotes        — Update release notes
// ./gradlew bootstrap                  — Download existing listing data
```

---

### Internal App Sharing

```
┌──────────────────────────────────────────────────────────────────────┐
│              INTERNAL APP SHARING                                     │
│                                                                      │
│  Purpose: Instantly share APK/AAB with team — NO review required     │
│                                                                      │
│  ┌──────────┐   Upload   ┌──────────────┐  Share Link  ┌─────────┐ │
│  │ CI/CD    │──────────▶│ Google Play  │─────────────▶│ Tester  │ │
│  │ Pipeline │   AAB/APK  │ Internal     │  Install URL │ Device  │ │
│  └──────────┘            │ App Sharing  │              └─────────┘ │
│                          └──────────────┘                           │
│                                                                      │
│  Benefits:                                                           │
│  • No review process — instant availability                          │
│  • Each upload gets a unique link                                    │
│  • Can share debug & release builds                                  │
│  • Works with any Google account (no tester groups needed)           │
│  • Perfect for PR review builds                                      │
└──────────────────────────────────────────────────────────────────────┘
```

```yaml
# GitHub Actions — Upload to Internal App Sharing
- name: Upload to Internal App Sharing
  run: |
    # Using Google Play Developer API v3
    gcloud auth activate-service-account --key-file=service-account.json

    # Upload via bundletool or API
    curl -X POST \
      "https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/com.example.app/internalappsharing/bundle" \
      -H "Authorization: Bearer $(gcloud auth print-access-token)" \
      -H "Content-Type: application/octet-stream" \
      --data-binary @app/build/outputs/bundle/release/app-release.aab

  # The response contains a download URL to share with testers
```

```kotlin
// Gradle Play Publisher — Internal sharing tasks
// ./gradlew uploadReleasePrivateBundle  — Upload to internal app sharing
// Returns a direct install link for testers

// Automate posting the link to Slack/Teams
tasks.register("shareInternalBuild") {
    dependsOn("uploadReleasePrivateBundle")
    doLast {
        // Read the upload result and post to team channel
        println("Internal sharing link generated — check Gradle Play Publisher output")
    }
}
```

---

### Staged Rollouts and A/B Testing

Staged rollouts are the **safety net** of release management. Instead of pushing to 100% of users
immediately (risky — a crashing release affects millions), you gradually increase the percentage
while monitoring crash rates, ANRs, and user feedback.

**Why this matters:** A 1% rollout to an app with 10M users = 100,000 users. If the crash rate
spikes, you've only affected 1% instead of everyone. You halt, fix, and restart.

```
┌──────────────────────────────────────────────────────────────────────────┐
│                   STAGED ROLLOUT — DETAILED STRATEGY                     │
│                                                                          │
│  Gradually release to increasing % of users, monitoring at each stage    │
│                                                                          │
│  Day 1       Day 2       Day 4       Day 7       Day 10     Day 14      │
│  ┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐   ┌──────────┐  │
│  │  1%  │──▶│  5%  │──▶│ 10%  │──▶│ 25%  │──▶│ 50%  │──▶│  100%    │  │
│  │      │   │      │   │      │   │      │   │      │   │ (Full)   │  │
│  └──┬───┘   └──┬───┘   └──┬───┘   └──┬───┘   └──┬───┘   └──────────┘  │
│     │          │          │          │          │                        │
│     ▼          ▼          ▼          ▼          ▼                        │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                    MONITORING CHECKLIST                           │   │
│  │                                                                  │   │
│  │  At EACH stage, check before increasing rollout:                │   │
│  │                                                                  │   │
│  │  ✓ Crash-free rate ≥ 99.5%     (Firebase Crashlytics)           │   │
│  │  ✓ ANR rate ≤ 0.47%            (Play Console Vitals)            │   │
│  │  ✓ No new crash clusters       (unique crash signatures)        │   │
│  │  ✓ Battery & wake lock normal  (Vitals)                         │   │
│  │  ✓ App startup time normal     (no regression)                  │   │
│  │  ✓ User reviews ≥ 4.0 stars    (no sudden drop)                │   │
│  │  ✓ Uninstall rate normal       (not spiking)                    │   │
│  │  ✓ Revenue not declining       (if applicable)                  │   │
│  │                                                                  │   │
│  │  IF ANY metric is bad:                                          │   │
│  │  ┌────────────────────────────────────────────────────────┐     │   │
│  │  │  HALT rollout → Investigate → Fix → New build →        │     │   │
│  │  │  Start rollout from 1% again                           │     │   │
│  │  └────────────────────────────────────────────────────────┘     │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  Release Status Options:                                                 │
│  ┌──────────────────────────────────────────────────────────────────┐    │
│  │  DRAFT       — Saved but not published. Can be edited.           │    │
│  │  IN_PROGRESS — Staged rollout active. Specify userFraction.     │    │
│  │  HALTED      — Rollout paused. No new users get this version.   │    │
│  │  COMPLETED   — Full rollout to 100%. Cannot be undone.          │    │
│  └──────────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// app/build.gradle.kts — Staged rollout with Gradle Play Publisher
play {
    track.set("production")
    userFraction.set(0.01)   // Start with 1% of users

    // Release status options:
    // "completed"    — Full rollout (100%)
    // "inProgress"   — Staged rollout (use userFraction)
    // "halted"       — Paused rollout
    // "draft"        — Draft release (not published)
    releaseStatus.set(com.github.triplet.gradle.androidpublisher.ReleaseStatus.IN_PROGRESS)
}

// To increase rollout percentage:
// 1. Change userFraction to 0.05 (5%), 0.20 (20%), etc.
// 2. Run: ./gradlew publishBundle

// To complete rollout (100%):
// Set releaseStatus to COMPLETED
// Run: ./gradlew publishBundle

// To halt a bad rollout:
// Run: ./gradlew publishBundle with releaseStatus = HALTED
```

**A/B Testing with Firebase Remote Config:**

```kotlin
// Use Firebase Remote Config for A/B testing features
// This works alongside staged rollouts

class FeatureFlagManager(
    private val remoteConfig: FirebaseRemoteConfig
) {
    // A/B test: New checkout flow
    val useNewCheckoutFlow: Boolean
        get() = remoteConfig.getBoolean("use_new_checkout_flow")

    // A/B test: Button color experiment
    val ctaButtonColor: String
        get() = remoteConfig.getString("cta_button_color")  // "blue" or "green"

    // A/B test: Pricing tier
    val showAnnualPricing: Boolean
        get() = remoteConfig.getBoolean("show_annual_pricing")

    suspend fun fetchAndActivate() {
        remoteConfig.fetchAndActivate().await()
    }
}

// In your Composable
@Composable
fun CheckoutScreen(featureFlags: FeatureFlagManager) {
    if (featureFlags.useNewCheckoutFlow) {
        NewCheckoutFlow()    // Variant B
    } else {
        ClassicCheckoutFlow() // Variant A (control)
    }
}
```

---

### Release Automation with Gradle Play Publisher

```
┌──────────────────────────────────────────────────────────────────────┐
│           COMPLETE RELEASE AUTOMATION PIPELINE                       │
│                                                                      │
│  Tag Push                                                            │
│  (v1.2.0)                                                            │
│     │                                                                │
│     ▼                                                                │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │ 1. Parse version from tag                                    │    │
│  │ 2. Update versionCode & versionName                          │    │
│  │ 3. Run full test suite                                       │    │
│  │ 4. Build release AAB                                         │    │
│  │ 5. Sign with upload key                                      │    │
│  │ 6. Upload to Play Store (internal track)                     │    │
│  │ 7. Auto-promote to production with staged rollout (1%)       │    │
│  │ 8. Create GitHub Release with changelog                      │    │
│  │ 9. Notify team on Slack                                      │    │
│  └──────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
```

```kotlin
// app/build.gradle.kts — Full release automation setup
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.github.triplet.play") version "3.9.0"
}

// Auto-calculate version from git tags
val gitVersionCode: Int by lazy {
    val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
        .redirectErrorStream(true)
        .start()
    process.inputStream.bufferedReader().readText().trim().toIntOrNull() ?: 1
}

val gitVersionName: String by lazy {
    val process = ProcessBuilder("git", "describe", "--tags", "--always")
        .redirectErrorStream(true)
        .start()
    process.inputStream.bufferedReader().readText().trim().ifEmpty { "0.0.1" }
}

android {
    defaultConfig {
        versionCode = gitVersionCode
        versionName = gitVersionName
    }
}

play {
    serviceAccountCredentials.set(file("play-service-account.json"))
    track.set("internal")
    defaultToAppBundles.set(true)
    releaseStatus.set(
        com.github.triplet.gradle.androidpublisher.ReleaseStatus.COMPLETED
    )

    // Auto-resolve version conflicts
    resolutionStrategy.set(
        com.github.triplet.gradle.androidpublisher.ResolutionStrategy.AUTO
    )
}
```

```yaml
# .github/workflows/release.yml — Full release workflow
name: Release

on:
  push:
    tags:
      - 'v*'  # Triggered by version tags (v1.0.0, v1.2.3, etc.)

permissions:
  contents: write  # For creating GitHub releases

jobs:
  release:
    name: Build & Publish Release
    runs-on: ubuntu-latest
    timeout-minutes: 45

    steps:
      # ── Setup ──
      - name: Checkout
        uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Full history for version calculation

      - name: Extract version from tag
        id: version
        run: |
          TAG=${GITHUB_REF#refs/tags/v}
          echo "version=$TAG" >> $GITHUB_OUTPUT
          echo "Releasing version: $TAG"

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - uses: gradle/actions/setup-gradle@v3

      # ── Test ──
      - name: Run Tests
        run: ./gradlew testReleaseUnitTest

      # ── Build ──
      - name: Decode Keystore
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: echo "$KEYSTORE_BASE64" | base64 -d > app/release-keystore.jks

      - name: Build Release Bundle
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew bundleRelease

      # ── Publish to Play Store ──
      - name: Decode Play Store service account
        env:
          PLAY_STORE_KEY: ${{ secrets.PLAY_STORE_SERVICE_ACCOUNT }}
        run: echo "$PLAY_STORE_KEY" > app/play-service-account.json

      - name: Publish to Internal Track
        run: ./gradlew publishBundle

      # ── Promote to Production (1% staged rollout) ──
      - name: Promote to Production
        run: |
          ./gradlew promoteArtifact \
            --from-track internal \
            --promote-track production \
            --user-fraction 0.01 \
            --release-status inProgress

      # ── Create GitHub Release ──
      - name: Generate Changelog
        id: changelog
        run: |
          # Get commits since last tag
          PREV_TAG=$(git describe --tags --abbrev=0 HEAD~1 2>/dev/null || echo "")
          if [ -n "$PREV_TAG" ]; then
            CHANGES=$(git log --pretty=format:"- %s (%h)" $PREV_TAG..HEAD)
          else
            CHANGES=$(git log --pretty=format:"- %s (%h)" --max-count=20)
          fi
          echo "changes<<EOF" >> $GITHUB_OUTPUT
          echo "$CHANGES" >> $GITHUB_OUTPUT
          echo "EOF" >> $GITHUB_OUTPUT

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ github.ref_name }}
          name: "Release ${{ steps.version.outputs.version }}"
          body: |
            ## What's Changed
            ${{ steps.changelog.outputs.changes }}

            ## Artifacts
            - AAB uploaded to Google Play Internal Testing track
            - Staged rollout to 1% of production users
          files: |
            app/build/outputs/bundle/release/*.aab
          draft: false
          prerelease: false

      # ── Notify Team ──
      - name: Notify Slack
        if: success()
        uses: slackapi/slack-github-action@v1
        with:
          payload: |
            {
              "text": "🚀 *${{ github.event.repository.name }}* v${{ steps.version.outputs.version }} released!\n• Published to Google Play (1% staged rollout)\n• <${{ github.server_url }}/${{ github.repository }}/releases/tag/${{ github.ref_name }}|View Release>"
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

---

## Quick Reference: Common Gradle Commands

```bash
# ── Build ──
./gradlew assembleDebug              # Build debug APK
./gradlew assembleRelease            # Build release APK
./gradlew bundleRelease              # Build release AAB (for Play Store)
./gradlew installDebug               # Build & install on connected device

# ── Test ──
./gradlew testDebugUnitTest          # Run unit tests
./gradlew connectedDebugAndroidTest  # Run instrumentation tests
./gradlew jacocoTestReport           # Generate coverage report

# ── Analyze ──
./gradlew lintDebug                  # Run lint checks
./gradlew dependencies               # Show dependency tree
./gradlew app:dependencies --configuration releaseRuntimeClasspath

# ── Clean ──
./gradlew clean                      # Delete build directory

# ── Info ──
./gradlew tasks                      # List all available tasks
./gradlew tasks --group publishing   # List tasks in a group
./gradlew --scan                     # Generate build scan (performance analysis)
./gradlew --profile                  # Generate local performance report

# ── Publish ──
./gradlew publishBundle              # Upload to Play Store
./gradlew promoteArtifact            # Promote between tracks
```

---

## Summary Diagram: Full CI/CD Architecture

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                    COMPLETE ANDROID CI/CD ARCHITECTURE                       │
│                                                                              │
│  Developer                                                                   │
│     │                                                                        │
│     │ git push / PR                                                          │
│     ▼                                                                        │
│  ┌──────────┐                                                                │
│  │  GitHub   │──── Webhook ────┐                                             │
│  │  Repo     │                 │                                             │
│  └──────────┘                  ▼                                             │
│                          ┌──────────────┐                                    │
│                          │  CI/CD       │                                    │
│                          │  (GitHub     │                                    │
│                          │   Actions)   │                                    │
│                          └──────┬───────┘                                    │
│                                 │                                            │
│                    ┌────────────┼────────────┐                               │
│                    ▼            ▼            ▼                               │
│              ┌──────────┐ ┌──────────┐ ┌──────────┐                         │
│              │  Lint &  │ │  Unit    │ │  Build   │                         │
│              │  Static  │ │  Tests   │ │  APK/AAB │                         │
│              │  Analysis│ │  (JVM)   │ │          │                         │
│              └────┬─────┘ └────┬─────┘ └────┬─────┘                         │
│                   │            │            │                                │
│                   ▼            ▼            ▼                                │
│              ┌──────────────────────────────────┐                            │
│              │     Firebase Test Lab            │                            │
│              │     (Instrumentation Tests)      │                            │
│              │     (Multiple devices & APIs)    │                            │
│              └───────────────┬──────────────────┘                            │
│                              │                                               │
│                    ┌─────────┴──────────┐                                    │
│                    ▼                    ▼                                     │
│              ┌──────────┐        ┌───────────────┐                           │
│              │  Test     │        │  Google Play  │                           │
│              │  Reports  │        │  Console      │                           │
│              │  Coverage │        │               │                           │
│              │  Codecov  │        │ Internal ──▶ Alpha ──▶ Beta ──▶ Prod    │
│              └──────────┘        └───────────────┘                           │
│                                         │                                    │
│                                         ▼                                    │
│                                  ┌──────────────┐                            │
│                                  │  Monitoring   │                           │
│                                  │  • Crashlytics│                           │
│                                  │  • Analytics  │                           │
│                                  │  • Vitals     │                           │
│                                  └──────────────┘                            │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

> **Key Takeaways:**
> 1. **Gradle** is the backbone — master build types, flavors, and the Kotlin DSL
> 2. **Version Catalogs** (`libs.versions.toml`) centralize dependency management
> 3. **Convention Plugins** eliminate build script duplication across modules
> 4. **CI/CD** automates build → test → deploy on every push
> 5. **Firebase Test Lab** runs tests on real devices in the cloud
> 6. **Staged Rollouts** reduce risk — start at 1%, monitor, then expand
> 7. **Gradle Play Publisher** automates the entire release pipeline
> 8. **Never hardcode secrets** — use environment variables or encrypted files

---

# Interview Questions & Answers

## Gradle Fundamentals

**Q1: What are the three phases of the Gradle build lifecycle? What happens in each?**

**A:** **(1) Initialization** — Gradle reads `settings.gradle.kts` to determine which projects are
included in the build (`:app`, `:core`, etc.) and creates `Project` objects.
**(2) Configuration** — Gradle evaluates ALL `build.gradle.kts` files of EVERY module, creating
the task graph (DAG). This runs even for tasks that won't be executed.
**(3) Execution** — Gradle walks the DAG and executes only the requested tasks and their
dependencies. Tasks with unchanged inputs are skipped (UP-TO-DATE) or loaded FROM-CACHE.

**Key insight:** A common performance mistake is doing expensive work (file I/O, network calls)
during the configuration phase, which runs on every single build regardless of which task you invoke.

---

**Q2: What is the difference between `buildTypes` and `productFlavors`?**

**A:** `buildTypes` control **HOW** the app is built (debug vs release — optimization, signing,
ProGuard), while `productFlavors` control **WHAT** is built (different feature sets, API endpoints,
branding from the same codebase). Every variant is a combination of one flavor from each dimension
plus one build type: `devFreeDebug` = dev flavor + free flavor + debug build type.

Build types answer: "Is this optimized for development or production?"
Flavors answer: "Is this the free or paid version? Dev or prod environment?"

---

**Q3: What is a version catalog and why use it over `ext` variables?**

**A:** A version catalog (`gradle/libs.versions.toml`) is a centralized, type-safe TOML file that
declares all dependency versions, library coordinates, and plugin IDs.

**Advantages over `ext` variables:**
- **Type-safe accessors**: `libs.retrofit` instead of `"$retrofit_version"` (typos caught at
  compile time)
- **IDE auto-completion**: Full IntelliSense support for all libraries
- **Bundling**: Group related libraries with `[bundles]` section
- **Multi-module**: Automatically shared across all modules without extra setup
- **Standardized format**: TOML is readable and tool-friendly (Dependabot, Renovate)

---

**Q4: Explain the difference between build cache and configuration cache.**

**A:**
- **Build Cache** caches individual **task outputs** keyed by the hash of task inputs. If you run
  `compileDebugKotlin` and the source files haven't changed, the compiled classes are loaded from
  cache instead of re-compiling. Can be local (disk) or remote (shared HTTP server for CI).

- **Configuration Cache** caches the entire **task graph** generated during the configuration
  phase. On subsequent builds, it skips evaluating all `build.gradle.kts` files entirely, loading
  the serialized task graph from disk.

Build cache = skip task execution. Configuration cache = skip task graph construction.

---

## CI/CD

**Q5: How would you set up a CI/CD pipeline for an Android project?**

**A:** A typical pipeline has 4 stages:

1. **Build** — Compile code, run lint checks, generate APK/AAB
2. **Unit Tests** — Run JVM-based tests (fast, no device needed)
3. **Instrumentation Tests** — Run on emulator (in CI) or Firebase Test Lab (for real devices)
4. **Deploy** — Sign with release key, upload to Play Store internal track

Key considerations:
- Use **Gradle caching** (setup-gradle action) to speed up builds
- Run lint and unit tests in **parallel** (they're independent)
- Only run expensive instrumentation tests after cheap tests pass
- Store keystore as a **Base64-encoded secret**, decode in CI
- Use **staged rollouts** for production (1% → 5% → 25% → 100%)

---

**Q6: How do you securely handle signing keys in CI/CD?**

**A:** Never commit keystores or passwords to version control. Instead:

1. **Encode keystore** as Base64: `base64 release.keystore > keystore.b64`
2. **Store as CI secret**: GitHub Secrets, GitLab CI Variables, etc.
3. **Decode in pipeline**: `echo "$KEYSTORE_BASE64" | base64 -d > keystore.jks`
4. **Pass passwords as environment variables** from secrets
5. **Delete keystore after build** (CI runners are ephemeral anyway)
6. Use **Google Play App Signing** so even if the upload key is leaked, the real signing key
   (managed by Google) is safe

---

**Q7: What is Firebase Test Lab and when would you use it over local emulators?**

**A:** Firebase Test Lab is Google's cloud-based device farm. Use it when:
- You need to test on **real physical devices** (not emulators)
- You need to test across **many API levels simultaneously** (e.g., API 28, 31, 33, 34)
- You want **Robo testing** — an AI bot that crawls your app without any test code
- You need **video recordings** and **screenshots** of test runs
- Local emulators in CI are slow (5-10 min boot time) and flaky

It supports three test types: Instrumentation (your Espresso/Compose tests), Robo (automated
exploration), and Game Loop (for games).

---

## Release Management

**Q8: What is the difference between APK and AAB? Why does Google require AAB?**

**A:**
- **APK**: A single file containing ALL code and resources for ALL device configurations.
  Users download everything, even resources they don't need.
- **AAB (Android App Bundle)**: A container format. Google Play generates **optimized APKs**
  per device — only the right screen density, CPU architecture, and language are included.

AAB reduces download size by 30-50%. Google requires AAB for all new apps since August 2021.
The trade-off: you must enroll in Google Play App Signing (Google manages the final signing key).

---

**Q9: Explain staged rollouts. When would you halt a rollout?**

**A:** Staged rollout releases a new version to an increasing percentage of users (1% → 5% →
25% → 50% → 100%), monitoring metrics at each stage before expanding.

**Halt the rollout if:**
- Crash-free rate drops below 99.5%
- ANR (App Not Responding) rate exceeds 0.47%
- New crash clusters appear in Crashlytics
- User ratings suddenly drop
- Uninstall rate spikes
- Revenue declines unexpectedly

After halting: diagnose, fix, create a new build, and restart from 1%.

---

**Q10: What is the difference between Internal Testing, Closed Testing, and Open Testing tracks?**

**A:**
- **Internal Testing**: Up to 100 testers by email. No review by Google. Available instantly.
  Best for: developer team, quick PR review builds.
- **Closed Testing (Alpha)**: Unlimited testers via email lists or Google Groups. Requires Google
  review. Best for: QA teams, stakeholder previews.
- **Open Testing (Beta)**: Anyone can opt in via Play Store link. Requires Google review. Best
  for: public beta programs, early access.
- **Production**: All users. Supports staged rollouts. Requires Google review.

You can **promote** a build from one track to the next without re-uploading.

---

**Q11: How does `Gradle Play Publisher` automate releases?**

**A:** Gradle Play Publisher is a Gradle plugin (`com.github.triplet.play`) that automates
interaction with the Google Play Developer API. Key tasks:

- `publishBundle` — Upload AAB to configured track (internal, alpha, beta, production)
- `promoteArtifact` — Move a build from one track to another
- `publishListing` — Update Play Store listing (description, screenshots)
- `publishReleaseNotes` — Update what's-new text

Combined with CI/CD (triggered by git tags), it enables fully automated releases:
Tag `v1.2.3` → CI builds AAB → signs → uploads to internal → auto-promotes to production at 1%.

---

**Q12: What are convention plugins and why use them in multi-module projects?**

**A:** Convention plugins are custom Gradle plugins (in `buildSrc/` or `build-logic/` composite
build) that encapsulate shared build configuration. Instead of duplicating the same `android {}`,
`compileOptions {}`, and `dependencies {}` blocks across 20 modules, you write it once in a
plugin and apply it:

```kotlin
// Every feature module just needs:
plugins {
    id("myapp.android.library")   // All shared config applied
    id("myapp.android.compose")   // Compose setup applied
}
```

This is the approach used by **Now in Android** (Google's reference project) and is considered
best practice for multi-module Android projects with 5+ modules.
