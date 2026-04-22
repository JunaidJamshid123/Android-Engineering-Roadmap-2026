# 16. Development Tools — Complete Guide

## Overview

Mastering development tools is as important as knowing the language and framework. This guide covers **Android Studio**, **version control**, **code quality**, **debugging**, and **design tools** — the complete toolkit for professional Android development.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Android Development Toolchain                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│                          ┌──────────────┐                               │
│                          │  Android     │                               │
│                          │  Studio IDE  │                               │
│                          └──────┬───────┘                               │
│                                 │                                        │
│            ┌────────────────────┼────────────────────┐                  │
│            │                    │                    │                   │
│            ▼                    ▼                    ▼                   │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐              │
│  │ Code Editing │    │ Build System │    │ Profiling &  │              │
│  │ ──────────── │    │ ──────────── │    │ Debugging    │              │
│  │ • IntelliJ   │    │ • Gradle     │    │ ──────────── │              │
│  │ • Refactoring│    │ • Build      │    │ • CPU/Memory │              │
│  │ • Navigation │    │   Variants   │    │ • Network    │              │
│  │ • Templates  │    │ • Signing    │    │ • ADB        │              │
│  │ • Compose    │    │ • ProGuard   │    │ • Breakpoints│              │
│  │   Preview    │    │ • KSP/KAPT  │    │ • Inspectors │              │
│  └──────────────┘    └──────────────┘    └──────────────┘              │
│            │                    │                    │                   │
│            └────────────────────┼────────────────────┘                  │
│                                 │                                        │
│            ┌────────────────────┼────────────────────┐                  │
│            │                    │                    │                   │
│            ▼                    ▼                    ▼                   │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐              │
│  │ Version      │    │ Code Quality │    │ Design       │              │
│  │ Control      │    │ Tools        │    │ Tools        │              │
│  │ ──────────── │    │ ──────────── │    │ ──────────── │              │
│  │ • Git        │    │ • Lint       │    │ • Figma      │              │
│  │ • Branching  │    │ • Detekt     │    │ • Theme Bldr │              │
│  │ • PRs/MRs    │    │ • SonarQube  │    │ • Asset Stdo │              │
│  │ • CI/CD      │    │ • ktlint     │    │ • Lottie     │              │
│  │ • Git Hooks  │    │ • JaCoCo     │    │ • Screenshot │              │
│  └──────────────┘    └──────────────┘    └──────────────┘              │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

# 16.1 Android Studio

## Code Editor Features

### Theory

Android Studio (built on IntelliJ IDEA) is the **official IDE** for Android development. It provides:

- **Intelligent code completion** using type inference, context analysis, and data flow
- **Deep Kotlin support** with null safety checks, coroutine debugging, and Compose integration
- **Live Templates** that expand abbreviations into full code blocks
- **Structural Search & Replace (SSR)** for pattern-based refactoring across codebases
- **Postfix completion** — transforms expressions by appending `.let`, `.if`, `.for`, `.try`, etc.
- **Inlay hints** — shows parameter names, type hints, and return types inline
- **Code folding** — collapse regions, functions, imports, comments for readability
- **Scratch files** — disposable files (Ctrl+Alt+Shift+Insert) for quick experiments

```
┌─────────────────────────────────────────────────────────────────────────┐
│                  How Code Completion Works                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Source Code                                                            │
│      │                                                                   │
│      ▼                                                                   │
│  ┌──────────────────┐                                                   │
│  │ PSI Tree (AST)   │  ← IntelliJ parses code into a syntax tree      │
│  │ Program Structure │    in real-time as you type                      │
│  │ Interface         │                                                   │
│  └────────┬─────────┘                                                   │
│           │                                                              │
│    ┌──────┴──────┐                                                      │
│    │             │                                                       │
│    ▼             ▼                                                       │
│  ┌──────────┐ ┌──────────────┐                                         │
│  │ Type     │ │ Data Flow    │                                         │
│  │ Resolver │ │ Analysis     │                                         │
│  │          │ │              │                                         │
│  │ "What    │ │ "What values │                                         │
│  │  type is │ │  can this    │                                         │
│  │  this?"  │ │  variable    │                                         │
│  │          │ │  hold?"      │                                         │
│  └────┬─────┘ └──────┬───────┘                                         │
│       │              │                                                   │
│       └──────┬───────┘                                                   │
│              ▼                                                           │
│  ┌───────────────────┐                                                  │
│  │ Completion Engine │  → Ranks suggestions by:                        │
│  │                   │    • Type compatibility                          │
│  │                   │    • Usage frequency                             │
│  │                   │    • Statistical patterns                        │
│  │                   │    • ML-based prediction                         │
│  └───────────────────┘                                                  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Complete Keyboard Shortcuts Reference

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Code Editor Key Shortcuts                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ══ NAVIGATION ══                                                       │
│  Ctrl+B / Ctrl+Click     → Go to definition                            │
│  Ctrl+Alt+B              → Go to implementation                         │
│  Ctrl+U                  → Go to super method/class                     │
│  Alt+F7                  → Find usages                                  │
│  Ctrl+F12                → File structure (methods/fields)              │
│  Ctrl+Shift+F            → Find in files (global search)               │
│  Ctrl+Shift+R            → Replace in files                             │
│  Ctrl+E                  → Recent files                                 │
│  Ctrl+Shift+E            → Recent locations (with code context)         │
│  Ctrl+G                  → Go to line number                            │
│  Ctrl+H                  → Type hierarchy                               │
│  Ctrl+Alt+H              → Call hierarchy                               │
│  Double Shift             → Search everywhere                            │
│  Ctrl+N                  → Find class by name                           │
│  Ctrl+Shift+N            → Find file by name                            │
│  Ctrl+Alt+Shift+N        → Find symbol (method/field) by name          │
│  Alt+Left/Right          → Navigate to previous/next tab                │
│  Ctrl+Alt+Left/Right     → Navigate back/forward in history            │
│  F2 / Shift+F2           → Next/previous error in file                  │
│                                                                          │
│  ══ EDITING ══                                                          │
│  Alt+Enter               → Quick fix / intention actions                │
│  Ctrl+Space              → Basic code completion                        │
│  Ctrl+Shift+Space        → Smart completion (type-aware)                │
│  Ctrl+Alt+L              → Reformat code                                │
│  Ctrl+Alt+O              → Optimize imports                             │
│  Ctrl+D                  → Duplicate line/selection                     │
│  Ctrl+Y                  → Delete line                                  │
│  Ctrl+Shift+Up/Down      → Move statement up/down                       │
│  Alt+Shift+Up/Down       → Move line up/down                            │
│  Alt+J                   → Add next occurrence (multi-cursor)           │
│  Ctrl+Alt+Shift+J        → Select all occurrences                       │
│  Ctrl+W                  → Expand selection (word→expr→statement→block) │
│  Ctrl+Shift+W            → Shrink selection                             │
│  Ctrl+Shift+Enter        → Complete current statement                   │
│  Ctrl+/                  → Line comment toggle                          │
│  Ctrl+Shift+/            → Block comment toggle                        │
│  Ctrl+Shift+J            → Join lines                                   │
│  Ctrl+P                  → Show parameter info for method call          │
│  Ctrl+Q                  → Quick documentation                          │
│                                                                          │
│  ══ REFACTORING ══                                                      │
│  Shift+F6                → Rename                                       │
│  Ctrl+Alt+M              → Extract method                               │
│  Ctrl+Alt+V              → Extract variable                             │
│  Ctrl+Alt+C              → Extract constant                             │
│  Ctrl+Alt+F              → Extract field                                │
│  Ctrl+Alt+P              → Extract parameter                            │
│  Ctrl+Alt+N              → Inline                                       │
│  F5                      → Copy class                                   │
│  F6                      → Move class/file                              │
│  Ctrl+F6                 → Change signature                             │
│  Ctrl+Alt+Shift+T        → Refactoring menu (all refactorings)         │
│                                                                          │
│  ══ CODE GENERATION ══                                                  │
│  Alt+Insert              → Generate (constructor, getter, etc.)         │
│  Ctrl+O                  → Override methods                             │
│  Ctrl+I                  → Implement interface methods                  │
│  Ctrl+Alt+Shift+Insert   → New scratch file                             │
│                                                                          │
│  ══ DEBUGGING ══                                                        │
│  Shift+F9               → Debug (run with debugger)                     │
│  Shift+F10              → Run (without debugger)                        │
│  F8                      → Step over                                    │
│  F7                      → Step into                                    │
│  Alt+Shift+F7            → Force step into (including library code)     │
│  Shift+F8               → Step out                                      │
│  Alt+F8                  → Evaluate expression                          │
│  Alt+F9                  → Run to cursor                                │
│  Ctrl+F8                → Toggle breakpoint                             │
│  Ctrl+Shift+F8          → View all breakpoints                          │
│  F9                      → Resume program                               │
│                                                                          │
│  ══ BUILD ══                                                            │
│  Ctrl+F9                → Build (make project)                          │
│  Ctrl+Shift+F9          → Recompile current file                        │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Live Templates (Built-in & Custom)

```
Built-in Live Templates:
─────────────────────────────────────────────────────────────
Abbreviation     Expands to
─────────────────────────────────────────────────────────────
logd          →  Log.d(TAG, "$msg$")
loge          →  Log.e(TAG, "$msg$", $exception$)
logi          →  Log.i(TAG, "$msg$")
logw          →  Log.w(TAG, "$msg$")
toast         →  Toast.makeText(context, "$msg$", Toast.LENGTH_SHORT).show()
foreach       →  for ($item$ in $collection$) { }
fori          →  for (i in 0 until $count$) { }
ifn           →  if ($var$ == null) { }
inn           →  if ($var$ != null) { }
sout          →  println($expr$)
comp          →  @Composable fun $name$() { }
key           →  const val $NAME$ = "$value$"
todo          →  // TODO: $text$
fixme         →  // FIXME: $text$
─────────────────────────────────────────────────────────────

Postfix Completion (type after an expression):
─────────────────────────────────────────────────────────────
expr.let      →  expr.let { $it$ }
expr.if       →  if (expr) { }
expr.else     →  if (!expr) { }
expr.for      →  for (item in expr) { }
expr.null     →  if (expr == null) { }
expr.notnull  →  if (expr != null) { }
expr.try      →  try { expr } catch(e: Exception) { }
expr.return   →  return expr
expr.val      →  val name = expr
expr.var      →  var name = expr
expr.assert   →  assert(expr)
─────────────────────────────────────────────────────────────
```

### Custom Live Template Examples

```kotlin
// Create via: Settings → Editor → Live Templates → + Template Group

// ═══ Template: "vm" ═══
// Abbreviation: vm
// Description: ViewModel with StateFlow
// Template text:
class $NAME$ViewModel @Inject constructor(
    private val $REPO$: $REPO_TYPE$
) : ViewModel() {

    private val _uiState = MutableStateFlow($STATE$())
    val uiState: StateFlow<$STATE$> = _uiState.asStateFlow()

    private val _events = Channel<$EVENT$>()
    val events = _events.receiveAsFlow()

    fun onAction(action: $ACTION$) {
        when (action) {
            $END$
        }
    }
}

// ═══ Template: "hiltvm" ═══
// Abbreviation: hiltvm
// Description: Hilt ViewModel
@HiltViewModel
class $NAME$ViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    $END$
) : ViewModel()

// ═══ Template: "compscreen" ═══
// Abbreviation: compscreen
// Description: Full Compose screen with ViewModel
@Composable
fun $NAME$Screen(
    viewModel: $NAME$ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    $NAME$Content(
        uiState = uiState,
        onAction = viewModel::onAction
    )
}

@Composable
private fun $NAME$Content(
    uiState: $STATE$,
    onAction: ($ACTION$) -> Unit,
    modifier: Modifier = Modifier
) {
    $END$
}

// ═══ Template: "usecase" ═══
// Abbreviation: usecase
class $NAME$UseCase @Inject constructor(
    private val repository: $REPO$
) {
    suspend operator fun invoke($PARAMS$): Result<$RETURN$> {
        return try {
            $END$
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## Gradle Build System

### Theory

**Gradle** is the build system for Android projects. It automates compilation, dependency management, testing, signing, and packaging. Android uses the **Android Gradle Plugin (AGP)** which adds Android-specific build tasks.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Gradle Build System Architecture                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Project Structure:                                                     │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  MyProject/                                                      │    │
│  │  ├── build.gradle.kts         ← Root build config               │    │
│  │  ├── settings.gradle.kts      ← Module inclusion                │    │
│  │  ├── gradle.properties        ← Build properties                │    │
│  │  ├── gradle/                                                     │    │
│  │  │   ├── wrapper/                                                │    │
│  │  │   │   └── gradle-wrapper.properties  ← Gradle version       │    │
│  │  │   └── libs.versions.toml   ← Version catalog               │    │
│  │  ├── app/                                                        │    │
│  │  │   └── build.gradle.kts     ← App module config              │    │
│  │  ├── core/                                                       │    │
│  │  │   └── build.gradle.kts     ← Library module config          │    │
│  │  └── feature/                                                    │    │
│  │      └── build.gradle.kts     ← Feature module config          │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
│  Build Lifecycle:                                                       │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐              │
│  │ Initialization│──>│ Configuration│──>│  Execution   │              │
│  │              │    │              │    │              │              │
│  │ Which modules│    │ Build task   │    │ Run selected │              │
│  │ to include?  │    │ graph setup  │    │ tasks        │              │
│  │              │    │              │    │              │              │
│  │ settings     │    │ build.gradle │    │ compile,     │              │
│  │ .gradle.kts  │    │ .kts files   │    │ test, assemble│             │
│  └──────────────┘    └──────────────┘    └──────────────┘              │
│                                                                          │
│  Dependency Configurations:                                             │
│  ┌──────────────────────────────────────────────────────────────┐      │
│  │ implementation    → Compile + runtime, NOT transitive        │      │
│  │ api               → Compile + runtime, IS transitive         │      │
│  │ compileOnly       → Compile only, not in APK                 │      │
│  │ runtimeOnly       → Runtime only, not visible at compile     │      │
│  │ testImplementation→ Test compile + runtime                   │      │
│  │ androidTestImpl   → Instrumented test dependencies           │      │
│  │ ksp               → Kotlin Symbol Processing annotation      │      │
│  │ debugImplementation → Only for debug builds                  │      │
│  └──────────────────────────────────────────────────────────────┘      │
│                                                                          │
│  implementation vs api:                                                 │
│  ┌─────────┐ depends on ┌──────────┐ depends on ┌────────────┐        │
│  │ Module A │───────────>│ Module B │───────────>│ Library C  │        │
│  └─────────┘            └──────────┘            └────────────┘        │
│                                                                          │
│  If B uses "implementation" for C:                                      │
│    A CANNOT see C's classes (encapsulation ✓, faster builds ✓)         │
│  If B uses "api" for C:                                                 │
│    A CAN see C's classes (C is exposed through B)                      │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Version Catalog (libs.versions.toml)

```toml
# gradle/libs.versions.toml — centralized dependency management

[versions]
agp = "8.5.0"
kotlin = "2.0.0"
compose-bom = "2024.06.00"
hilt = "2.51.1"
room = "2.6.1"
retrofit = "2.9.0"
coroutines = "1.8.1"
lifecycle = "2.8.3"
navigation = "2.7.7"
ksp = "2.0.0-1.0.22"
junit = "4.13.2"
mockk = "1.13.11"

[libraries]
# AndroidX
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.13.1" }
androidx-lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

# Network
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version = "4.12.0" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

[bundles]
compose = ["compose-ui", "compose-material3", "compose-ui-tooling"]
room = ["room-runtime", "room-ktx"]
retrofit = ["retrofit", "retrofit-gson", "okhttp-logging"]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### Build Variants and Product Flavors

```kotlin
// app/build.gradle.kts
android {
    namespace = "com.example.myapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    // ═══ Build Types (how to build) ═══
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"     // Separate app on device
            versionNameSuffix = "-debug"
            buildConfigField("String", "BASE_URL", "\"https://dev-api.example.com\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true             // Enable R8/ProGuard
            isShrinkResources = true           // Remove unused resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://api.example.com\"")
            buildConfigField("Boolean", "ENABLE_LOGGING", "false")
        }
        create("staging") {                    // Custom build type
            initWith(getByName("release"))     // Copy from release
            applicationIdSuffix = ".staging"
            buildConfigField("String", "BASE_URL", "\"https://staging-api.example.com\"")
            matchingFallbacks += listOf("release")
        }
    }

    // ═══ Product Flavors (what to build) ═══
    flavorDimensions += listOf("environment", "store")

    productFlavors {
        // Environment dimension
        create("free") {
            dimension = "environment"
            applicationIdSuffix = ".free"
            buildConfigField("Boolean", "IS_PREMIUM", "false")
            buildConfigField("Int", "MAX_ITEMS", "10")
        }
        create("premium") {
            dimension = "environment"
            applicationIdSuffix = ".premium"
            buildConfigField("Boolean", "IS_PREMIUM", "true")
            buildConfigField("Int", "MAX_ITEMS", "999999")
        }

        // Store dimension
        create("google") {
            dimension = "store"
            buildConfigField("String", "STORE", "\"google\"")
        }
        create("huawei") {
            dimension = "store"
            buildConfigField("String", "STORE", "\"huawei\"")
        }
    }

    // Generated variants: freeGoogleDebug, freeGoogleRelease,
    //                     freeHuaweiDebug, premiumGoogleRelease, etc.

    // ═══ Signing Configs ═══
    signingConfigs {
        create("release") {
            // NEVER hardcode these — use gradle.properties or env vars
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true                     // Enable BuildConfig generation
    }
}

// Using version catalog references
dependencies {
    implementation(libs.bundles.compose)
    implementation(libs.bundles.room)
    implementation(libs.bundles.retrofit)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)
    implementation(libs.hilt.navigation.compose)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Build Variants Matrix                                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Build Variant = Product Flavor(s) + Build Type                         │
│                                                                          │
│  Flavor Dimensions:    environment     ×     store     ×    Build Type  │
│                       ┌───────────┐     ┌──────────┐    ┌──────────┐   │
│                       │   free    │     │  google  │    │  debug   │   │
│                       │  premium  │     │  huawei  │    │ staging  │   │
│                       └───────────┘     └──────────┘    │ release  │   │
│                                                          └──────────┘   │
│  Generated variants (12 total):                                         │
│  ├── freeGoogleDebug         (com.example.myapp.free.debug)            │
│  ├── freeGoogleStaging       (com.example.myapp.free.staging)          │
│  ├── freeGoogleRelease       (com.example.myapp.free)                  │
│  ├── freeHuaweiDebug         (com.example.myapp.free.debug)            │
│  ├── freeHuaweiStaging       ...                                        │
│  ├── freeHuaweiRelease       ...                                        │
│  ├── premiumGoogleDebug      (com.example.myapp.premium.debug)         │
│  ├── premiumGoogleStaging    ...                                        │
│  ├── premiumGoogleRelease    ...                                        │
│  ├── premiumHuaweiDebug      ...                                        │
│  ├── premiumHuaweiStaging    ...                                        │
│  └── premiumHuaweiRelease    (com.example.myapp.premium)               │
│                                                                          │
│  Source Set Hierarchy (higher priority overrides lower):                 │
│  ┌──────────────────────────────────────────────────┐                  │
│  │  src/freeGoogleDebug/  ← Most specific           │                  │
│  │  src/freeGoogle/       ← Flavor combination      │                  │
│  │  src/free/             ← Flavor                   │                  │
│  │  src/google/           ← Flavor                   │                  │
│  │  src/debug/            ← Build type               │                  │
│  │  src/main/             ← Base (always included)   │                  │
│  └──────────────────────────────────────────────────┘                  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Gradle Performance & Build Analyzer

```kotlin
// gradle.properties — optimize build performance
org.gradle.jvmargs=-Xmx4096m -XX:+UseParallelGC
org.gradle.parallel=true              // Parallel module compilation
org.gradle.caching=true               // Build cache
org.gradle.configuration-cache=true   // Configuration cache (Gradle 8+)
org.gradle.daemon=true                // Keep daemon running

// Kotlin-specific
kotlin.incremental=true
kotlin.code.style=official

// Android-specific
android.useAndroidX=true
android.nonTransitiveRClass=true      // Faster R class generation
```

```bash
# Analyze build performance
./gradlew assembleDebug --scan        # Upload build scan to scans.gradle.com
./gradlew assembleDebug --profile     # Generate local HTML profile

# Build Analyzer in Android Studio:
# Build → Build Analyzer (after a build completes)
# Shows: task execution time, configuration issues, dependency download time

# Useful Gradle commands
./gradlew tasks                       # List all tasks
./gradlew dependencies                # Show dependency tree
./gradlew app:dependencies --configuration debugRuntimeClasspath
./gradlew assembleDebug               # Build debug APK
./gradlew assembleRelease             # Build release APK
./gradlew bundleRelease               # Build App Bundle (AAB)
./gradlew clean                       # Clean build outputs
./gradlew --stop                      # Stop Gradle daemon
```

---

## Layout Editor and Design Tools

### Theory

The Layout Editor provides a visual way to build XML layouts with drag-and-drop. It includes:

- **Design View**: Visual drag-and-drop editor with rendered preview
- **Blueprint View**: Wire-frame view showing constraints without rendering
- **Split View**: Code + preview side by side for rapid iteration
- **Component Tree**: Hierarchical view showing view nesting (detect deep nesting!)
- **Attributes Panel**: Edit all view properties visually with search
- **Constraint helpers**: Guidelines, Barriers, Chains, Groups, Flow
- **Compose Preview**: `@Preview` annotation renders Compose UI in editor
- **Animation Preview**: Preview Compose animations and transitions

```
┌─────────────────────────────────────────────────────────────────────────┐
│                Layout Editor Views                                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Toolbar: [Code] [Split] [Design]  | Device ▾ | Theme ▾ | API│    │
│  ├─────────────────────────────────────────────────────────────────┤    │
│  │                        │                                        │    │
│  │  Component Tree        │        Design Surface                  │    │
│  │  ┌──────────────────┐  │  ┌────────────────────────────────┐   │    │
│  │  │ConstraintLayout  │  │  │                                │   │    │
│  │  │ ├─ Guideline(H)  │  │  │  ┌────────────────────────┐   │   │    │
│  │  │ ├─ Toolbar       │  │  │  │     App Toolbar        │   │   │    │
│  │  │ │  └─ TextView   │  │  │  └────────────────────────┘   │   │    │
│  │  │ ├─ EditText      │  │  │  ┌───────────────────┐        │   │    │
│  │  │ ├─ Button        │  │  │  │ [Enter email    ] │        │   │    │
│  │  │ ├─ RecyclerView  │  │  │  └───────────────────┘        │   │    │
│  │  │ └─ FAB           │  │  │  ┌───────────────────┐        │   │    │
│  │  │   ⚠ 5 levels deep│  │  │  │   [ Login ]      │        │   │    │
│  │  └──────────────────┘  │  │  └───────────────────┘        │   │    │
│  │                        │  │         ┌──┐                   │   │    │
│  │  Attributes Panel      │  │         │+│  FAB               │   │    │
│  │  ┌──────────────────┐  │  │         └──┘                   │   │    │
│  │  │ 🔍 Search attrs  │  │  └────────────────────────────────┘   │    │
│  │  │ id: btnLogin     │  │                                        │    │
│  │  │ text: "Login"    │  │  Multi-Preview:                        │    │
│  │  │ width: 0dp(match)│  │  ┌──────┐ ┌──────┐ ┌──────┐          │    │
│  │  │ height: wrap     │  │  │Phone │ │Tablet│ │Fold  │          │    │
│  │  │ layout_constraint│  │  │      │ │      │ │      │          │    │
│  │  │  Start→parent    │  │  │      │ │      │ │      │          │    │
│  │  │  End→parent      │  │  └──────┘ └──────┘ └──────┘          │    │
│  │  │  Top→editText    │  │                                        │    │
│  │  │ style: @style/...│  │                                        │    │
│  │  └──────────────────┘  │                                        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Compose Preview — All Annotations & Features

```kotlin
// ═══ Basic Previews ═══
@Preview(
    name = "Light Mode",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun MyScreenPreview() {
    MyAppTheme {
        MyScreen()
    }
}

// ═══ Device Previews ═══
@Preview(name = "Phone", device = Devices.PIXEL_7)
@Preview(name = "Tablet", device = Devices.PIXEL_TABLET, showSystemUi = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Wear", device = Devices.WEAR_OS_LARGE_ROUND)
@Preview(name = "TV", device = Devices.TV_1080p)
@Composable
fun DevicePreview() { MyScreen() }

// ═══ Preview Parameters — Test with multiple data sets ═══
class UserPreviewProvider : PreviewParameterProvider<User> {
    override val values = sequenceOf(
        User(id = 1, name = "Alice", isPremium = true),
        User(id = 2, name = "Bob", isPremium = false),
        User(id = 3, name = "مستخدم", isPremium = true),   // RTL text
    )
}

@Preview(showBackground = true)
@Composable
fun UserCardPreview(
    @PreviewParameter(UserPreviewProvider::class) user: User
) {
    UserCard(user = user)
}

// ═══ Multi-Preview Annotations (reusable) ═══
@Preview(name = "Phone", device = Devices.PIXEL_7, showBackground = true)
@Preview(name = "Phone - Landscape", widthDp = 840, heightDp = 400)
@Preview(name = "Tablet", device = Devices.PIXEL_TABLET)
@Preview(name = "Font 1.5x", fontScale = 1.5f, showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
annotation class DevicesPreviews

// Now use on any composable:
@DevicesPreviews
@Composable
fun LoginScreenPreview() {
    MyAppTheme { LoginScreen() }
}

// ═══ Interactive Preview & Animation Preview ═══
// Interactive: Click buttons and interact in preview pane
// Deploy Preview: Push preview to device/emulator for testing
// Animation Preview: Inspect AnimatedVisibility, animate*AsState, etc.
// Compose Preview includes: start, seek, loop animations

// ═══ Wallpaper preview for dynamic color testing ═══
@Preview(wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Preview(wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE)
@Preview(wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE)
@Composable
fun DynamicColorPreview() {
    MyAppTheme(dynamicColor = true) {
        MyScreen()
    }
}
```

---

## Profilers: CPU, Memory, Network, Energy

### Theory

Android Studio Profilers help identify performance bottlenecks in real-time. Access via **View → Tool Windows → Profiler** or the profiler icon in the toolbar.

**When to use each profiler:**
- **CPU Profiler** — App feels slow, UI jank, frames dropping, ANR dialogs
- **Memory Profiler** — App crashes with OOM, excessive garbage collection pauses
- **Network Profiler** — Slow data loading, large payloads, too many API calls
- **Energy Profiler** — Users report battery drain, wake locks held too long

```
┌─────────────────────────────────────────────────────────────────────────┐
│               Android Studio Profilers                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  CPU Profiler                                                      │  │
│  │  ─────────────                                                     │  │
│  │  Recording modes:                                                  │  │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐   │  │
│  │  │ Sample Java/     │  │ Trace Java/      │  │ System Trace  │   │  │
│  │  │ Kotlin Methods   │  │ Kotlin Methods   │  │ (Perfetto)    │   │  │
│  │  │ ─────────────    │  │ ─────────────    │  │ ─────────     │   │  │
│  │  │ Low overhead     │  │ High overhead    │  │ Thread sched. │   │  │
│  │  │ Statistical      │  │ Every call       │  │ Frame render  │   │  │
│  │  │ Good for finding │  │ Exact timing     │  │ Kernel events │   │  │
│  │  │ hotspots         │  │ of each method   │  │ System calls  │   │  │
│  │  └──────────────────┘  └──────────────────┘  └───────────────┘   │  │
│  │                                                                    │  │
│  │  Visualization:                                                    │  │
│  │  ┌─ Flame Chart ──────────────────────────────────────────────┐   │  │
│  │  │ ██████████████████████████████████████████  main()         │   │  │
│  │  │ ████████████████████████  doWork()                         │   │  │
│  │  │ ████████████  parseJson()  ███████ saveDb()               │   │  │
│  │  │ ██████ decode()    ████ transform()  ██ insert()          │   │  │
│  │  └────────────────────────────────────────────────────────────┘   │  │
│  │  Width = time spent in method. Wider = more time = hotspot        │  │
│  │                                                                    │  │
│  │  ┌─ Top-Down ─────────────┐  ┌─ Bottom-Up ──────────────────┐   │  │
│  │  │ main() ──── 100%       │  │ decode() ──── 45% total      │   │  │
│  │  │  ├ doWork() ─── 80%    │  │  ← parseJson() ─── 45%      │   │  │
│  │  │  │  ├ parseJson() 50%  │  │     ← doWork() ─── 45%      │   │  │
│  │  │  │  │  └ decode() 45%  │  │        ← main() ─── 45%     │   │  │
│  │  │  │  └ saveDb() ─── 30% │  │ (finds which callers used   │   │  │
│  │  │  └ init() ──── 20%     │  │  the most time in a method) │   │  │
│  │  └────────────────────────┘  └──────────────────────────────┘   │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  Memory Profiler                                                   │  │
│  │  ───────────────                                                   │  │
│  │                                                                    │  │
│  │  Memory categories:                                                │  │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌──────────┐ ┌─────────┐  │  │
│  │  │  Java   │ │ Native  │ │Graphics │ │  Stack   │ │  Code   │  │  │
│  │  │  Heap   │ │  Heap   │ │ Buffers │ │          │ │         │  │  │
│  │  │  ──────  │ │  ──────  │ │  ──────  │ │  ──────  │ │  ──────  │  │  │
│  │  │ Kotlin  │ │ C/C++   │ │ GL tex  │ │ Thread  │ │ DEX +   │  │  │
│  │  │ Java    │ │ Bitmap  │ │ Surfaces│ │ stacks  │ │ SO libs │  │  │
│  │  │ objects │ │ pixel   │ │ frame   │ │         │ │ OAT     │  │  │
│  │  │         │ │ data    │ │ buffers │ │         │ │         │  │  │
│  │  └─────────┘ └─────────┘ └─────────┘ └──────────┘ └─────────┘  │  │
│  │                                                                    │  │
│  │  Heap Dump Analysis:                                               │  │
│  │  ┌───────────────────────────────────────────────────────────┐    │  │
│  │  │ Class Name          │ Allocations │ Native Size │ Shallow│    │  │
│  │  │ String              │    5,234    │       0     │  125KB │    │  │
│  │  │ byte[]              │    3,102    │       0     │  890KB │    │  │
│  │  │ Bitmap              │       42    │   12.4MB    │    2KB │    │  │
│  │  │ ViewModel           │        3    │       0     │   120B │    │  │
│  │  │ Activity (LEAKED!)  │        2    │       0     │    8KB │    │  │
│  │  └───────────────────────────────────────────────────────────┘    │  │
│  │  → Click on leaked Activity to see reference chain (GC root path)│  │
│  │                                                                    │  │
│  │  Detect leaks: Retained Size much larger than Shallow Size       │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  Network Profiler                                                  │  │
│  │  ────────────────                                                  │  │
│  │  • Real-time network activity graph (sent/received bytes)         │  │
│  │  • Request/response inspection (headers, body, status code)       │  │
│  │  • Connection timeline with threading info                        │  │
│  │  • Works with OkHttp, HttpURLConnection, Volley                   │  │
│  │  • Shows which thread initiated each request                      │  │
│  │                                                                    │  │
│  │  Common issues to detect:                                          │  │
│  │  • Duplicate requests (same endpoint called multiple times)       │  │
│  │  • Large payloads (downloading unnecessary data)                  │  │
│  │  • Missing compression (no gzip/br encoding)                      │  │
│  │  • Sequential requests (should be parallel)                       │  │
│  │  • Requests on main thread (network on UI thread)                 │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  Energy Profiler                                                   │  │
│  │  ───────────────                                                   │  │
│  │  • Estimates energy consumption from CPU, Network, GPS, Sensors   │  │
│  │  • Shows wake locks, jobs, alarms, location requests timeline     │  │
│  │  • Correlates energy with system events                           │  │
│  │                                                                    │  │
│  │  Common battery drain causes:                                      │  │
│  │  • Wake locks held too long (forgot to release)                   │  │
│  │  • GPS polling at high frequency unnecessarily                    │  │
│  │  • Network requests when app is in background                     │  │
│  │  • Alarms firing too frequently                                   │  │
│  │  • Animations running when screen is off                          │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Programmatic Profiling with Custom Trace Sections

```kotlin
import android.os.Trace
import androidx.tracing.trace

// ═══ Method 1: android.os.Trace (manual) ═══
fun loadData() {
    Trace.beginSection("loadData")          // Start trace section
    try {
        Trace.beginSection("fetchFromNetwork")
        val data = api.fetchUsers()
        Trace.endSection()                   // End fetchFromNetwork

        Trace.beginSection("parseAndSave")
        val parsed = parseResponse(data)
        database.saveAll(parsed)
        Trace.endSection()                   // End parseAndSave
    } finally {
        Trace.endSection()                   // End loadData
    }
}

// ═══ Method 2: androidx.tracing (Kotlin-friendly) ═══
// implementation("androidx.tracing:tracing-ktx:1.2.0")
fun loadDataBetter() {
    trace("loadData") {                      // Auto-closes section
        val data = trace("fetchFromNetwork") {
            api.fetchUsers()
        }
        trace("parseAndSave") {
            val parsed = parseResponse(data)
            database.saveAll(parsed)
        }
    }
}

// ═══ Compose Performance: Stability & Recomposition ═══
// In Compose compiler report, check stability:
// Add to build.gradle.kts:
// kotlinOptions {
//     freeCompilerArgs += listOf(
//         "-P", "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
//             project.buildDir.absolutePath + "/compose_reports"
//     )
// }

// Stable: primitives, String, enums, @Immutable/@Stable classes
@Immutable
data class UserUiState(
    val name: String,
    val email: String,
    val avatarUrl: String
)

// Unstable (causes recomposition): List, Map, var properties, non-data classes
// Fix: Use kotlinx.collections.immutable
// implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
@Immutable
data class FeedUiState(
    val posts: ImmutableList<Post>,    // ✓ Stable
    // val posts: List<Post>,          // ✗ Unstable — causes unnecessary recomposition
)
```

### Profiling Commands

```bash
# Record a method trace from command line
adb shell am profile start <process> /data/local/tmp/trace.trace
adb shell am profile stop <process>
adb pull /data/local/tmp/trace.trace

# Capture a heap dump
adb shell am dumpheap <process> /data/local/tmp/heap.hprof
adb pull /data/local/tmp/heap.hprof

# System trace (Perfetto) — best for jank analysis
adb shell perfetto -o /data/misc/perfetto-traces/trace.perfetto-trace \
  -t 10s sched freq idle am wm gfx view input
adb pull /data/misc/perfetto-traces/trace.perfetto-trace
# Open at ui.perfetto.dev

# Macro-benchmark (startup, scroll, animation timing)
# Runs instrumented tests that measure real performance
# ./gradlew :benchmark:connectedBenchmarkAndroidTest
```

---

## Logcat and Debugging

### Theory

**Logcat** displays system messages including stack traces, log messages from your app, and system events. Android Studio's Logcat v2 provides powerful filtering, formatting, and search.

**Log levels** form a hierarchy — each level includes all levels above it:
- **V (Verbose)** — Most detailed, everything (dev only)
- **D (Debug)** — Debugging info (dev only, stripped in release)
- **I (Info)** — General operational events
- **W (Warn)** — Something unexpected but recoverable
- **E (Error)** — Errors that need attention
- **A (Assert)** — Critical failures (should never happen)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                  Logcat Window (v2)                                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Filter bar: [package:mine level:debug tag:MyTag message:error]         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ 12:30:01.123 D/MyTag    : User clicked login button              │  │
│  │ 12:30:01.456 I/MyTag    : Starting authentication...             │  │
│  │ 12:30:02.100 D/OkHttp   : --> POST https://api.example.com/auth  │  │
│  │ 12:30:02.243 D/OkHttp   : <-- 200 OK (143ms, 1.2kB body)       │  │
│  │ 12:30:02.300 I/MyTag    : Authentication successful              │  │
│  │ 12:30:03.010 W/MyTag    : Cache expired after 24h, refreshing    │  │
│  │ 12:30:05.500 E/MyTag    : Failed to load profile image           │  │
│  │              java.io.IOException: Connection reset                │  │
│  │                  at com.example.app.ImageLoader.load(IL:42)      │  │
│  │                  at com.example.app.ProfileVM.init(PVM:18)       │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│  Logcat v2 Filter Syntax (combinable):                                  │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │ package:mine            — Only your app's process               │    │
│  │ package:com.other.app   — Specific package                      │    │
│  │ tag:MyTag               — Filter by log tag                     │    │
│  │ tag~:My.*               — Tag regex                             │    │
│  │ level:warn              — Warn and above (W, E, A)              │    │
│  │ message:error           — Search in message text                │    │
│  │ message~:user \d+       — Message regex                         │    │
│  │ age:5m                  — Logs from last 5 minutes              │    │
│  │ age:2h                  — Logs from last 2 hours                │    │
│  │ -tag:Choreographer      — Exclude tag                           │    │
│  │ -tag:ViewRootImpl       — Exclude noisy system tags             │    │
│  │ process:pid             — Filter by process ID                  │    │
│  │ is:crash                — Show only crashes                     │    │
│  │ is:stacktrace           — Show only stack traces                │    │
│  │                                                                  │    │
│  │ Combine: package:mine level:debug -tag:Choreographer            │    │
│  │ Favorites: Save frequently used filters with ★                 │    │
│  └────────────────────────────────────────────────────────────────┘    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Logging Best Practices — android.util.Log vs Timber

```kotlin
// ═══ BAD: Using android.util.Log directly ═══
// Problems: TAG boilerplate, logs in production, no formatting, no crash reporting

class MyRepository {
    companion object {
        private const val TAG = "MyRepository"    // Boilerplate in every class
    }

    fun fetchData() {
        Log.d(TAG, "fetchData() called")
        Log.i(TAG, "Fetching data from server")
        Log.w(TAG, "Cache miss, fetching remote")
        Log.e(TAG, "Network request failed", exception)
    }
}

// ═══ GOOD: Using Timber ═══
// implementation("com.jakewharton.timber:timber:5.0.1")
// Benefits: auto TAG, format strings, no-op in release, pluggable trees

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())     // Logs to Logcat with auto TAG
        } else {
            Timber.plant(CrashReportingTree())   // Sends to Crashlytics
        }
    }
}

// Custom tree for production crash reporting
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return          // Only log W, E, A in prod

        // Send to crash reporting service
        FirebaseCrashlytics.getInstance().log("$tag: $message")
        t?.let { FirebaseCrashlytics.getInstance().recordException(it) }
    }
}

// Usage (no TAG needed — Timber auto-detects class name)
class UserRepository @Inject constructor(private val api: ApiService) {

    suspend fun getUser(id: String): User {
        Timber.d("Fetching user %s", id)              // Format string (safer than concat)
        return try {
            val user = api.getUser(id)
            Timber.i("User loaded: %s", user.name)
            user
        } catch (e: HttpException) {
            Timber.e(e, "Failed to fetch user %s (HTTP %d)", id, e.code())
            throw e
        }
    }
}

// ═══ Advanced: Timber with coroutine context ═══
class TimberMDCTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val threadName = Thread.currentThread().name
        super.log(priority, tag, "[$threadName] $message", t)
    }
}
```

---

## Emulator and AVD Manager

### Theory

The **Android Virtual Device (AVD) Manager** creates and manages emulated Android devices. The Android Emulator is the most feature-rich testing tool — it simulates hardware sensors, network conditions, GPS, battery states, and more.

**Architecture**: The emulator uses QEMU with hardware acceleration (KVM on Linux, HAXM/Hyper-V on Windows/Mac) to run x86/x86_64 system images at near-native speed.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              AVD Manager & Emulator                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  AVD Creation Flow:                                                     │
│  ┌──────────────┐   ┌───────────────┐   ┌──────────────┐   ┌────────┐ │
│  │ 1. Hardware  │──>│ 2. System     │──>│ 3. Configure │──>│4.Launch│ │
│  │  Profile     │   │  Image        │   │  AVD         │   │        │ │
│  │              │   │               │   │              │   │        │ │
│  │ Pixel 8      │   │ API 34        │   │ RAM: 4GB     │   │ 🔄     │ │
│  │ Pixel Fold   │   │ x86_64        │   │ Storage: 8GB │   │        │ │
│  │ Pixel Tablet │   │ Google APIs   │   │ Orientation  │   │        │ │
│  │ Wear OS      │   │ Google Play   │   │ Network speed│   │        │ │
│  │ TV           │   │ AOSP          │   │ GPU mode     │   │        │ │
│  │ Automotive   │   │               │   │ Skin         │   │        │ │
│  └──────────────┘   └───────────────┘   └──────────────┘   └────────┘ │
│                                                                          │
│  Emulator Extended Controls:                                            │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐              │
│  │  📍 Location   │ │  📶 Cellular   │ │  🔋 Battery    │              │
│  │  • GPS coords  │ │  • Network type│ │  • Level %     │              │
│  │  • Routes      │ │  • 4G/3G/Edge │ │  • Charging    │              │
│  │  • GPX/KML     │ │  • Signal str. │ │  • Health      │              │
│  │  • Play routes │ │  • Data limit  │ │  • AC/USB/Qi   │              │
│  └────────────────┘ └────────────────┘ └────────────────┘              │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐              │
│  │  📷 Camera     │ │  🔊 Microphone │ │  🧭 Sensors    │              │
│  │  • Virtual     │ │  • Virtual mic │ │  • Accelero.   │              │
│  │  • Webcam      │ │  • Audio file  │ │  • Gyroscope   │              │
│  │  • Image file  │ │  • Noise types │ │  • Magnetometer│              │
│  │  • VirtualScene│ │                │ │  • Proximity   │              │
│  └────────────────┘ └────────────────┘ │  • Light       │              │
│  ┌────────────────┐ ┌────────────────┐ │  • Pressure    │              │
│  │  🖥 Display     │ │  📸 Snapshots  │ │  • Fingerprint │              │
│  │  • Multi-display│ │  • Quick boot  │ └────────────────┘              │
│  │  • Foldable    │ │  • Save state  │                                 │
│  │  • Resize      │ │  • Load state  │ ┌────────────────┐              │
│  │  • Rotation    │ │  • Name & org  │ │  🐛 Bugs       │              │
│  │  • Cutout      │ │                │ │  • Bug report   │              │
│  └────────────────┘ └────────────────┘ │  • Screenshot   │              │
│                                          │  • Screen record│             │
│  Emulator Shortcuts:                     └────────────────┘              │
│  Ctrl+Up/Down/Left/Right → Rotate device                               │
│  Ctrl+Shift+S            → Screenshot                                  │
│  Ctrl+Shift+R            → Screen record                               │
│  Ctrl+M                  → Simulate multi-window                       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Emulator Commands

```bash
# List available AVDs
emulator -list-avds

# Start emulator with options
emulator -avd Pixel_8_API_34                        # Standard start
emulator -avd Pixel_8_API_34 -no-snapshot -wipe-data # Fresh start
emulator -avd Pixel_8_API_34 -gpu host               # Host GPU rendering
emulator -avd Pixel_8_API_34 -no-snapshot-load        # Cold boot
emulator -avd Pixel_8_API_34 -memory 4096             # 4GB RAM
emulator -avd Pixel_8_API_34 -no-audio -no-boot-anim  # Headless/CI mode
emulator -avd Pixel_8_API_34 -dns-server 8.8.8.8      # Custom DNS
emulator -avd Pixel_8_API_34 -http-proxy http://proxy:8080  # Proxy

# Emulator console (telnet)
# macOS/Linux: nc localhost 5554   |   Windows: telnet localhost 5554
# Commands in console:
# geo fix <longitude> <latitude>   → Set GPS
# network speed edge               → Simulate EDGE
# network delay gprs                → Simulate latency
# power status not-charging         → Battery discharge
# sms send 5551234567 "Hello"       → Send SMS to emulator
```

---

## Device File Explorer

### Theory

The **Device File Explorer** (View → Tool Windows → Device File Explorer) lets you browse, download, upload, and delete files on a connected device or emulator. Essential for debugging database contents, SharedPreferences, cached files, and logs.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Device File Explorer                                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Common paths to inspect:                                               │
│                                                                          │
│  /data/data/<package>/                ← App-private internal storage    │
│  ├── databases/                       ← SQLite & Room databases         │
│  │   ├── app.db                       ← Main database file              │
│  │   ├── app.db-shm                   ← Shared memory (WAL mode)       │
│  │   └── app.db-wal                   ← Write-ahead log                │
│  ├── shared_prefs/                    ← SharedPreferences XML files     │
│  │   ├── settings.xml                                                    │
│  │   └── com.example_preferences.xml                                    │
│  ├── files/                           ← Context.filesDir                │
│  │   ├── datastore/                   ← DataStore proto/preferences     │
│  │   │   └── user_preferences.pb                                        │
│  │   └── custom_data.json                                                │
│  ├── cache/                           ← Context.cacheDir               │
│  │   ├── image_cache/                 ← Coil/Glide image cache         │
│  │   └── http_cache/                  ← OkHttp response cache          │
│  ├── no_backup/                       ← Excluded from backup           │
│  ├── code_cache/                      ← JIT/AOT compiled code          │
│  └── app_webview/                     ← WebView data                   │
│                                                                          │
│  /sdcard/Android/data/<package>/      ← App-specific external storage  │
│  /sdcard/Android/media/<package>/     ← App media files                │
│  /sdcard/Download/                    ← User downloads                 │
│  /sdcard/DCIM/                        ← Camera photos                  │
│  /sdcard/Pictures/                    ← Saved images                   │
│                                                                          │
│  /data/local/tmp/                     ← ADB push/pull temp directory   │
│                                                                          │
│  Actions:                                                               │
│  • Right-click → Save As (download to PC)                              │
│  • Right-click → Upload (push file to device)                          │
│  • Right-click → Delete                                                 │
│  • Double-click → Open in editor (databases open in DB Inspector)      │
│  • Synchronize → Refresh file list                                     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Android Studio Plugins

### Useful Plugins for Productivity

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Recommended Android Studio Plugins                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ═══ Code Quality ═══                                                   │
│  • Detekt              — Kotlin static analysis integration             │
│  • SonarLint           — Real-time SonarQube rules in IDE              │
│  • String Manipulation — Case conversion, escaping, encoding           │
│                                                                          │
│  ═══ UI & Design ═══                                                    │
│  • Material Design Icon Generator — Import material icons quickly      │
│  • Color Manager       — Visual color editing in XML/Compose           │
│  • Compose Multipreview— Enhanced multi-device previews                │
│  • Shape Shifter       — SVG/Vector animation path morphing            │
│                                                                          │
│  ═══ Productivity ═══                                                   │
│  • Key Promoter X      — Shows shortcut for every mouse action         │
│  • Rainbow Brackets    — Color-coded matching brackets                 │
│  • .ignore             — .gitignore file generation and management     │
│  • ADB Idea            — Quick ADB commands from IDE (restart, clear)  │
│  • JSON to Kotlin Class— Paste JSON, generate data classes             │
│  • Database Navigator  — Enhanced SQLite browser                       │
│                                                                          │
│  ═══ Architecture ═══                                                   │
│  • Dagger/Hilt Navigator— Navigate between DI components              │
│  • ArchGuard            — Architecture fitness function                │
│                                                                          │
│  ═══ Version Control ═══                                                │
│  • GitToolBox          — Inline blame, status bar info, auto-fetch     │
│  • Conventional Commit — Structured commit message templates           │
│                                                                          │
│  Install: Settings → Plugins → Marketplace → Search & Install         │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

# 16.2 Version Control

## Git Fundamentals

### Theory

Git is a **distributed version control system (DVCS)** — every developer has a full copy of the entire repository history. This makes it fast, reliable, and supports offline work. Android Studio has built-in Git support via the **Git tool window** and **VCS menu**.

**Why Git is distributed (vs centralized like SVN):**
- Each clone is a full backup of the entire project history
- Commits, branching, and merging are local (fast, offline-capable)
- Push/pull to synchronize with remotes
- No single point of failure

```
┌─────────────────────────────────────────────────────────────────────────┐
│                  Git Architecture — Distributed Model                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────── Developer A's Machine ───────────────────┐        │
│  │                                                              │        │
│  │  Working     ──git add──>   Staging    ──git commit──>  Local│        │
│  │  Directory                  Area (Index)                Repo │        │
│  │  (files on disk)            (.git/index)                (.git)│       │
│  │                                                              │        │
│  └──────────────────────────────┬───────────────────────────────┘        │
│                                 │                                        │
│                          git push ↑↓ git pull/fetch                     │
│                                 │                                        │
│  ┌──────────────────── Remote Repository ──────────────────────┐        │
│  │              (GitHub / GitLab / Bitbucket)                    │        │
│  │                                                              │        │
│  │  Branches:  main ──●──●──●──●                               │        │
│  │             develop ──●──●──●──●──●                         │        │
│  │             feature/login ──●──●                             │        │
│  │                                                              │        │
│  │  Tags:      v1.0.0, v1.1.0, v2.0.0                         │        │
│  │  PRs:       #42 (open), #41 (merged), #40 (closed)          │        │
│  │                                                              │        │
│  └──────────────────────────────┬───────────────────────────────┘        │
│                                 │                                        │
│                          git clone / git fetch / git pull               │
│                                 │                                        │
│  ┌─────────────────── Developer B's Machine ───────────────────┐        │
│  │  Working Dir  →  Staging  →  Local Repo  (independent copy) │        │
│  └──────────────────────────────────────────────────────────────┘        │
│                                                                          │
│  File State Lifecycle:                                                  │
│  ┌──────────┐  git add  ┌──────────┐  git commit  ┌────────────┐      │
│  │Untracked │─────────>│  Staged  │────────────>│ Committed  │      │
│  └──────────┘          └──────────┘              └────────────┘      │
│  ┌──────────┐  git add  ┌──────────┐                                   │
│  │ Modified │─────────>│  Staged  │                                   │
│  └──────────┘          └──────────┘                                   │
│  ┌──────────┐                                                          │
│  │Unmodified│  (clean — matches last commit)                          │
│  └──────────┘                                                          │
│                                                                          │
│  Key Concepts:                                                          │
│  • commit   — Immutable snapshot with SHA-1 hash, author, message     │
│  • branch   — Movable pointer to a commit (lightweight, instant)      │
│  • merge    — Combine two branches (3-way merge with common ancestor) │
│  • rebase   — Replay commits on top of another branch (linear history)│
│  • stash    — Temporarily shelve changes (stack-based)                │
│  • tag      — Named, permanent reference to a specific commit         │
│  • HEAD     — Pointer to current branch tip (or detached commit)      │
│  • remote   — Reference to a remote repository (origin, upstream)     │
│  • reflog   — Log of all HEAD movements (safety net for recovery)     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Essential Git Commands

```bash
# ═══ SETUP ═══
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
git config --global core.autocrlf true    # Windows: convert CRLF→LF on commit
git config --global pull.rebase true      # Rebase on pull instead of merge
git config --global init.defaultBranch main
git config --list                          # Show all config
git init                                   # Initialize new repo
git clone <url>                            # Clone remote repo
git clone --depth 1 <url>                  # Shallow clone (faster, CI)

# ═══ DAILY WORKFLOW ═══
git status                                 # What's changed?
git status -s                              # Short format
git add .                                  # Stage all changes
git add <file>                             # Stage specific file
git add -p                                 # Stage interactively (hunks)
git commit -m "feat: add login"            # Commit with message
git commit --amend                         # Edit last commit message + add staged
git push origin main                       # Push to remote
git pull origin main                       # Pull + merge latest
git pull --rebase origin main              # Pull + rebase (cleaner history)
git fetch origin                           # Download without merging

# ═══ BRANCHING ═══
git branch                                 # List local branches
git branch -a                              # List all (local + remote)
git branch -d feature/login               # Delete merged branch
git branch -D feature/login               # Force delete unmerged branch
git switch feature/login                   # Switch branch (modern)
git switch -c feature/login               # Create + switch (modern)
git switch -                               # Switch to previous branch
git push origin feature/login              # Push branch to remote
git push origin --delete feature/login     # Delete remote branch

# ═══ MERGING ═══
git merge feature/login                    # Merge branch into current
git merge --no-ff feature/login            # Force merge commit (no fast-forward)
git merge --squash feature/login           # Squash all commits, then commit

# ═══ REBASING ═══
git rebase main                            # Rebase current branch onto main
git rebase -i HEAD~5                       # Interactive rebase last 5 commits
                                           # (squash, edit, reorder, drop commits)
git rebase --abort                         # Cancel in-progress rebase

# ═══ VIEWING HISTORY ═══
git log --oneline --graph --all            # Visual commit graph
git log --oneline -20                      # Last 20 commits
git log --author="Alice"                   # Filter by author
git log --since="2024-01-01"               # Filter by date
git log -- path/to/file                    # History of specific file
git log -p -- path/to/file                 # History with diffs
git diff                                   # Unstaged changes
git diff --staged                          # Staged changes
git diff main..feature/login              # Diff between branches
git diff HEAD~3                            # Diff against 3 commits ago
git blame <file>                           # Who changed each line
git show <commit>                          # Show commit details + diff

# ═══ UNDOING ═══
git restore <file>                         # Discard unstaged changes
git restore --staged <file>                # Unstage file (keep changes)
git reset --soft HEAD~1                    # Undo commit, keep staged
git reset --mixed HEAD~1                   # Undo commit, keep unstaged (default)
git reset --hard HEAD~1                    # ⚠️ Undo commit + discard changes
git revert <commit>                        # New commit that undoes a commit (safe)
git reflog                                 # Show all HEAD movements
git reset --hard <reflog-hash>             # Recover "lost" commits

# ═══ STASHING ═══
git stash                                  # Stash changes
git stash push -m "WIP: login"            # Stash with message
git stash push -- path/to/file            # Stash specific file
git stash pop                              # Apply + remove latest stash
git stash apply stash@{2}                  # Apply specific stash (keep in list)
git stash list                             # List all stashes
git stash drop stash@{0}                   # Delete a stash
git stash clear                            # Delete all stashes

# ═══ CHERRY-PICK ═══
git cherry-pick <commit-hash>              # Apply specific commit to current branch
git cherry-pick abc123 def456              # Cherry-pick multiple commits
git cherry-pick --no-commit <commit>       # Apply without committing

# ═══ TAGGING ═══
git tag v1.0.0                             # Lightweight tag
git tag -a v1.0.0 -m "Release 1.0.0"      # Annotated tag (recommended)
git tag -a v1.0.0 <commit-hash>            # Tag a past commit
git push origin v1.0.0                     # Push specific tag
git push origin --tags                     # Push all tags
git tag -d v1.0.0                          # Delete local tag
git push origin --delete v1.0.0            # Delete remote tag

# ═══ CLEANUP ═══
git clean -fd                              # Remove untracked files/dirs
git gc                                     # Garbage collection (compress objects)
git prune                                  # Remove unreachable objects
git remote prune origin                    # Remove stale remote tracking branches
```

---

## .gitignore for Android Projects

### Theory

The `.gitignore` file specifies files and directories that Git should **never track**. For Android projects, this includes build outputs, IDE-specific files, local configs, and secrets.

```gitignore
# ═══ .gitignore for Android (Kotlin + Compose) ═══

# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/
!**/src/androidTest/**/build/

# IDE - Android Studio / IntelliJ
*.iml
.idea/
.idea/caches/
.idea/libraries/
.idea/modules.xml
.idea/workspace.xml
.idea/navEditor.xml
.idea/assetWizardSettings.xml
.idea/codeStyles/
.idea/dictionaries/
.idea/inspectionProfiles/
# Keep .idea/vcs.xml for VCS config sharing (optional)

# Local configuration (contains SDK path)
local.properties

# Keystore files (NEVER commit signing keys!)
*.jks
*.keystore
keystore.properties

# ProGuard
proguard/

# Android-specific
*.apk
*.aab
*.ap_
*.dex

# Google Services (contains API keys)
# google-services.json        # Uncomment if sensitive

# Environment files
.env
*.env.local

# OS-specific
.DS_Store           # macOS
Thumbs.db           # Windows
Desktop.ini         # Windows
*.swp               # Vim
*~                  # Emacs backup

# Logs
*.log

# Kotlin
*.class

# Test outputs
/test-results/
/test-reports/

# Coverage reports
/coverage/
jacoco.exec
```

---

## Conventional Commits

### Theory

**Conventional Commits** is a specification for writing structured commit messages that are human-readable and machine-parseable. It enables automatic changelog generation and semantic versioning.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Conventional Commit Format                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Format:                                                                │
│  <type>(<scope>): <description>                                        │
│                                                                          │
│  [optional body]                                                        │
│                                                                          │
│  [optional footer(s)]                                                   │
│                                                                          │
│  ═══ Types ═══                                                          │
│  feat:     — New feature                    → MINOR version bump       │
│  fix:      — Bug fix                        → PATCH version bump       │
│  docs:     — Documentation only                                        │
│  style:    — Formatting, no logic change                               │
│  refactor: — Code restructuring, no behavior change                    │
│  perf:     — Performance improvement                                   │
│  test:     — Adding/fixing tests                                       │
│  build:    — Build system or dependencies                              │
│  ci:       — CI/CD configuration                                       │
│  chore:    — Maintenance tasks                                         │
│  revert:   — Reverts a previous commit                                 │
│                                                                          │
│  BREAKING CHANGE: in footer     → MAJOR version bump                   │
│                                                                          │
│  ═══ Examples ═══                                                       │
│  feat(auth): add biometric login                                       │
│  fix(profile): prevent crash on null avatar URL                        │
│  docs(readme): add setup instructions                                  │
│  refactor(network): extract API client to separate module              │
│  perf(list): use paging for user list                                  │
│  test(login): add unit tests for LoginViewModel                        │
│  build(deps): bump Compose BOM to 2024.06.00                          │
│  ci(actions): add instrumented test workflow                           │
│                                                                          │
│  feat(auth)!: replace login with OAuth 2.0                             │
│                                                                          │
│  BREAKING CHANGE: Legacy username/password login removed.              │
│  Migrate to OAuth using the new AuthManager class.                     │
│                                                                          │
│  ═══ Semantic Versioning (SemVer) ═══                                  │
│  MAJOR.MINOR.PATCH  →  1.4.2                                          │
│  MAJOR = breaking change (feat! or BREAKING CHANGE)                    │
│  MINOR = new feature (feat)                                            │
│  PATCH = bug fix (fix)                                                 │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Branching Strategies

### Theory

Branching strategies define how teams organize their Git branches for development, testing, and releases. The right strategy depends on team size, release cadence, and project complexity.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              GitFlow Branching Strategy                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  main ────●──────────────────────●────────────────●──── (releases)      │
│           │                      ▲                ▲                      │
│           │                      │                │                      │
│  release  │               ●──●──●          ●──●──●    (stabilize)      │
│           │               ▲                ▲                             │
│           │               │                │                             │
│  develop ─●──●──●──●──●──●──●──●──●──●──●──●──●──●── (integration)    │
│              │  ▲     │  ▲     │  ▲                                     │
│              │  │     │  │     │  │                                      │
│  feature/    ●──●     │  │     ●──●       (feature branches)           │
│  login              │  │                                                │
│                      │  │                                                │
│  feature/            ●──●                                               │
│  profile                                                                 │
│                                                                          │
│  hotfix ─────────────────────────────●──● → main + develop             │
│                                      ▲                                   │
│                                      │  (emergency production fix)      │
│                                    main                                  │
│                                                                          │
│  When to use: Apps with scheduled releases, QA stages, support          │
│  for multiple release versions                                          │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│              Trunk-Based Development                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  main ──●──●──●──●──●──●──●──●──●──●──●──●──●── (single branch)       │
│         │  ▲  │  ▲  │  ▲                                               │
│         │  │  │  │  │  │    Short-lived feature branches               │
│         ●──●  ●──●  ●──●   (merged within 1-2 days MAX)              │
│                                                                          │
│  Principles:                                                            │
│  • Everyone commits to main (or branches that live < 2 days)           │
│  • Feature flags to hide incomplete features in production             │
│  • CI/CD runs on every push — fast feedback                            │
│  • Releases via tags or very short-lived release branches              │
│  • Simpler than GitFlow — less ceremony, faster delivery               │
│                                                                          │
│  When to use: Teams practicing continuous delivery, SaaS apps,         │
│  high-frequency deployments, experienced teams                         │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│              GitHub Flow (Simplified)                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  main ──●──────────●──────────●──────────●──── (always deployable)    │
│         │          ▲          ▲          ▲                               │
│         │          │          │          │                               │
│  feat/  ●──●──●──●─┘         │          │    (branch → PR → merge)    │
│  login                       │          │                               │
│  fix/                  ●──●──┘          │                               │
│  crash                                  │                               │
│  feat/                          ●──●──●─┘                               │
│  search                                                                  │
│                                                                          │
│  Rules:                                                                 │
│  1. main is always deployable                                           │
│  2. Branch off main for any change                                     │
│  3. Open PR when ready for review                                      │
│  4. Merge to main after approval + CI                                  │
│  5. Deploy immediately after merge                                     │
│                                                                          │
│  When to use: Small-medium teams, web/mobile apps, startups            │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Pull Requests and Code Review

### Theory

Pull Requests (PRs) / Merge Requests (MRs) are the gateway for code to enter the main branch. They enable code review, discussion, automated checks, and knowledge sharing across the team.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                Pull Request Lifecycle                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Developer                         Reviewer(s)                          │
│  ─────────                         ───────────                          │
│  1. Create feature branch                                               │
│  2. Make atomic commits (conventional)                                  │
│  3. Push to remote                                                      │
│  4. Open Pull Request ────────────> 5. Review code                     │
│     • Clear title (conventional)    • Read diff line-by-line           │
│     • Description with context      • Check logic, edge cases          │
│     • Screenshots (if UI change)    • Check test coverage              │
│     • Link to Jira/Linear issue     • Comment with suggestions         │
│     • Add reviewers + labels        • Approve or Request Changes       │
│     • CI checks auto-run            • Use "Suggest changes" feature    │
│                                                                          │
│  6. Address feedback  <──────────── (if changes requested)             │
│     • Push new commits              Review each change                 │
│     • Respond to all comments                                          │
│     • Request re-review                                                │
│                                                                          │
│  7. All CI checks pass ✓                                               │
│  8. Required reviewers approve ✓                                       │
│  9. Merge strategy:                                                    │
│     ┌─────────────────────────────────────────────────────────────┐    │
│     │ Squash & Merge   — All commits → 1 clean commit (default)  │    │
│     │                   Best for feature branches                 │    │
│     │                                                             │    │
│     │ Create Merge Commit — Preserves all commits + merge node   │    │
│     │                       Best when history matters             │    │
│     │                                                             │    │
│     │ Rebase & Merge   — Replays commits on main (linear)        │    │
│     │                   Best for clean linear history             │    │
│     └─────────────────────────────────────────────────────────────┘    │
│  10. Delete feature branch (auto-deletable)                            │
│                                                                          │
│  PR Template (.github/pull_request_template.md):                        │
│  ┌──────────────────────────────────────────────────────────────┐      │
│  │  ## Description                                               │      │
│  │  Brief description of changes.                                │      │
│  │                                                                │      │
│  │  ## Type of Change                                            │      │
│  │  - [ ] Bug fix                                                 │      │
│  │  - [ ] New feature                                             │      │
│  │  - [ ] Breaking change                                         │      │
│  │  - [ ] Refactor                                                │      │
│  │                                                                │      │
│  │  ## Testing                                                    │      │
│  │  - [ ] Unit tests added/updated                                │      │
│  │  - [ ] Manual testing done                                     │      │
│  │                                                                │      │
│  │  ## Screenshots (if UI change)                                │      │
│  │  | Before | After |                                            │      │
│  │  |--------|-------|                                            │      │
│  │  | img    | img   |                                            │      │
│  │                                                                │      │
│  │  ## Checklist                                                  │      │
│  │  - [ ] Code follows project conventions                       │      │
│  │  - [ ] Self-reviewed                                           │      │
│  │  - [ ] No new warnings                                        │      │
│  │  Fixes #(issue number)                                        │      │
│  └──────────────────────────────────────────────────────────────┘      │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Merge Conflict Resolution

### Theory

Merge conflicts occur when two branches modify the **same lines** in the same file, and Git can't automatically decide which version to keep. Understanding the **3-way merge** algorithm is key to resolving conflicts correctly.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              3-Way Merge Algorithm                                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Git compares THREE versions of the file:                               │
│                                                                          │
│               Common Ancestor (base)                                    │
│              /                      \                                    │
│         Yours (HEAD)            Theirs (incoming)                       │
│                                                                          │
│  ┌───────────────────────────────────────────────────────────────┐      │
│  │  Base           Yours             Theirs          Result     │      │
│  │  ─────          ─────             ──────          ──────     │      │
│  │  line A         line A            line A          line A     │      │
│  │  line B         line B-modified   line B          B-modified │      │
│  │  line C         line C            line C-changed  C-changed  │      │
│  │  line D         line D-yours      line D-theirs   CONFLICT!  │      │
│  │  line E         line E            line E          line E     │      │
│  └───────────────────────────────────────────────────────────────┘      │
│                                                                          │
│  Rules:                                                                 │
│  • If only one side changed → take that change (auto-merge)           │
│  • If both sides changed the SAME way → take either (auto-merge)      │
│  • If both sides changed DIFFERENTLY → CONFLICT (manual resolution)   │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Conflict Markers in File:                                              │
│  ┌──────────────────────────────────────────────────┐                  │
│  │  fun getUserName(): String {                     │                  │
│  │  <<<<<<< HEAD (your current branch)              │                  │
│  │      return user.displayName                     │                  │
│  │  ||||||| merged common ancestor                  │                  │
│  │      return user.name                            │  ← diff3 format │
│  │  =======                                         │                  │
│  │      return user.fullName                        │                  │
│  │  >>>>>>> feature/profile (incoming branch)       │                  │
│  │  }                                               │                  │
│  └──────────────────────────────────────────────────┘                  │
│                                                                          │
│  Enable diff3 for better conflict context:                              │
│  git config --global merge.conflictstyle diff3                         │
│                                                                          │
│  Android Studio 3-Way Merge Tool:                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                 │
│  │  Yours (left) │  │ Result (mid) │  │ Theirs (right)│                │
│  │              │  │              │  │              │                 │
│  │ return user  │  │              │  │ return user  │                 │
│  │ .displayName │>>│   Choose     │<<│ .fullName    │                 │
│  │              │  │   or edit    │  │              │                 │
│  │ [Accept ✓]   │  │  manually   │  │  [Accept ✓]  │                 │
│  └──────────────┘  └──────────────┘  └──────────────┘                 │
│                                                                          │
│  Access: Git → Resolve Conflicts (or click notification)               │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Conflict Resolution Commands

```bash
# ═══ During merge conflict ═══
git merge feature/profile              # Starts merge, conflicts appear
git status                             # See conflicted files (both modified)

# After manually resolving each conflicted file:
git add <resolved-file>                # Mark as resolved
git commit                             # Complete the merge

# Abort if overwhelmed:
git merge --abort                      # Cancel merge, go back to before

# ═══ During rebase conflict ═══
git rebase main                        # Start rebase
# Fix conflicts in each commit, then:
git add <resolved-file>
git rebase --continue                  # Process next commit
git rebase --skip                      # Skip this commit entirely
git rebase --abort                     # Cancel entire rebase

# ═══ Prefer a side for all conflicts ═══
git merge -X ours feature/profile      # Auto-prefer our changes
git merge -X theirs feature/profile    # Auto-prefer their changes

# ═══ Check which files will conflict before merging ═══
git merge --no-commit --no-ff feature/profile
git diff --name-only --diff-filter=U   # List conflicted files
git merge --abort                      # Abort the test merge

# ═══ Android Studio ═══
# VCS → Git → Resolve Conflicts → 3-way merge tool
# Or: Click the "Resolve" notification that appears
```

---

## Git Hooks for Android

### Theory

**Git hooks** are scripts that run automatically at specific points in the Git workflow. They enforce quality standards before code reaches the repository.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Git Hooks for Android Development                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Hook Location: .git/hooks/ (local, not committed)                      │
│  Shared hooks: Use a tool like Lefthook, Husky, or commit scripts      │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Client-Side Hooks (local machine)                              │    │
│  │  ─────────────────                                              │    │
│  │  pre-commit    → Before commit is created                      │    │
│  │                  Run: ktlint, detekt, lint                     │    │
│  │                                                                 │    │
│  │  commit-msg    → Validate commit message format                │    │
│  │                  Enforce: conventional commits                 │    │
│  │                                                                 │    │
│  │  pre-push      → Before push to remote                        │    │
│  │                  Run: unit tests, full lint check              │    │
│  │                                                                 │    │
│  │  prepare-commit-msg → Pre-fill commit message template        │    │
│  │                                                                 │    │
│  │  post-checkout → After switching branches                     │    │
│  │                  Run: dependency check, clean if needed        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  Server-Side Hooks (GitHub/GitLab)                              │    │
│  │  ─────────────────                                              │    │
│  │  pre-receive   → Reject pushes that violate rules              │    │
│  │  post-receive  → Notify, trigger CI/CD                        │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```bash
#!/bin/bash
# .git/hooks/pre-commit (make executable: chmod +x)
# Runs ktlint and detekt before each commit

echo "🔍 Running ktlint check..."
./gradlew ktlintCheck --daemon --quiet
KTLINT_EXIT=$?

echo "🔍 Running detekt..."
./gradlew detekt --daemon --quiet
DETEKT_EXIT=$?

if [ $KTLINT_EXIT -ne 0 ] || [ $DETEKT_EXIT -ne 0 ]; then
    echo "❌ Pre-commit checks failed!"
    echo "Run './gradlew ktlintFormat' to auto-fix formatting."
    exit 1
fi

echo "✅ Pre-commit checks passed!"
exit 0
```

```bash
#!/bin/bash
# .git/hooks/commit-msg — Enforce conventional commits
# Validates commit message format

commit_msg=$(cat "$1")
pattern="^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\(.+\))?(!)?: .{1,72}"

if ! echo "$commit_msg" | grep -qE "$pattern"; then
    echo "❌ Invalid commit message format!"
    echo ""
    echo "Expected: <type>(<scope>): <description>"
    echo "Example:  feat(auth): add biometric login"
    echo "          fix(profile): handle null avatar URL"
    echo ""
    echo "Types: feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert"
    exit 1
fi

echo "✅ Commit message format valid."
exit 0
```

```kotlin
// Alternative: Use Lefthook (cross-platform, YAML config, committed to repo)
// Install: brew install lefthook  OR  npm install lefthook
```

```yaml
# lefthook.yml (committed to repo — shared with team)
pre-commit:
  parallel: true
  commands:
    ktlint:
      run: ./gradlew ktlintCheck --daemon --quiet
      glob: "*.{kt,kts}"
    detekt:
      run: ./gradlew detekt --daemon --quiet
      glob: "*.{kt,kts}"

commit-msg:
  commands:
    conventional:
      run: |
        echo "$1" | grep -qE "^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\(.+\))?(!)?: .{1,72}" || exit 1

pre-push:
  commands:
    tests:
      run: ./gradlew testDebugUnitTest --daemon --quiet
```

---

# 16.3 Code Quality Tools

## Static Analysis

### Theory

Static analysis examines code **without executing it** to find bugs, vulnerabilities, style violations, and code smells. Android projects should use multiple complementary tools — each catches different issues.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Static Analysis Tool Comparison                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Tool          Focus             Language     Integration               │
│  ──────────    ─────────         ──────────   ───────────               │
│  Android Lint  Android-specific  Java/Kotlin  Built into AGP           │
│                bugs, APIs,       XML          Runs with ./gradlew lint │
│                resources, a11y                                          │
│                                                                          │
│  Detekt        Kotlin code       Kotlin       Gradle plugin             │
│                smells, style,    only         ./gradlew detekt          │
│                complexity                                               │
│                                                                          │
│  ktlint        Kotlin code       Kotlin       Gradle plugin             │
│                formatting        only         ./gradlew ktlintCheck    │
│                (auto-fixable)                                           │
│                                                                          │
│  SonarQube     Enterprise code   Multi-lang   Server + scanner         │
│                quality platform  Java/Kotlin  CI/CD integration        │
│                                                                          │
│  SonarLint     Real-time IDE     Multi-lang   IntelliJ plugin          │
│                (SonarQube rules)                                        │
│                                                                          │
│  Spotless       Multi-tool       Multi-lang   Gradle plugin             │
│                 formatting       +formats     ./gradlew spotlessCheck  │
│                 orchestrator                                            │
│                                                                          │
│  Recommended combo:                                                     │
│  Android Lint (always) + Detekt (Kotlin quality) + ktlint (formatting) │
│  + SonarQube (enterprise / CI) + JaCoCo (coverage)                     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Android Lint

**Lint** is Android's built-in static analysis tool. It checks for correctness, security, performance, usability, accessibility, and internationalization issues across Java/Kotlin code and XML resources.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Android Lint Categories                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────────┐ ┌─────────────────────┐                       │
│  │  Correctness        │ │  Performance         │                       │
│  │  • Missing perms    │ │  • Overdraw           │                       │
│  │  • Wrong API level  │ │  • Unused resources   │                       │
│  │  • Null issues      │ │  • Nested weights     │                       │
│  │  • Wrong locale     │ │  • Obsolete views     │                       │
│  │  • Invalid resource │ │  • Inefficient layout │                       │
│  │  • Thread safety    │ │  • View stubs needed  │                       │
│  │  • Parcelable impl │ │  • Wakelock issues     │                       │
│  └─────────────────────┘ └─────────────────────┘                       │
│  ┌─────────────────────┐ ┌─────────────────────┐                       │
│  │  Security           │ │  Usability           │                       │
│  │  • Hardcoded keys   │ │  • Missing translate │                       │
│  │  • Exported comps   │ │  • Small touch target│                       │
│  │  • Insecure TLS     │ │  • Icon issues       │                       │
│  │  • SQL injection    │ │  • Typography        │                       │
│  │  • World-readable   │ │  • Button order      │                       │
│  │  • Intent redirect  │ │  • Locale formatting │                       │
│  └─────────────────────┘ └─────────────────────┘                       │
│  ┌─────────────────────┐ ┌─────────────────────┐                       │
│  │  Accessibility      │ │  Interoperability    │                       │
│  │  • Missing content  │ │  • Kotlin/Java       │                       │
│  │    Description      │ │    interop issues    │                       │
│  │  • Low contrast     │ │  • Gradle config     │                       │
│  │  • Touch target < 48│ │  • API compatibility │                       │
│  │  • Missing labels   │ │  • Library conflicts │                       │
│  │  • Focus navigation │ │  • Deprecated APIs   │                       │
│  └─────────────────────┘ └─────────────────────┘                       │
│                                                                          │
│  Severity levels: Fatal > Error > Warning > Informational > Ignore     │
│                                                                          │
│  Run: Analyze → Inspect Code                                           │
│  CLI: ./gradlew lint  |  ./gradlew lintDebug  |  ./gradlew lintRelease│
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```xml
<!-- lint.xml — Configure Lint rules at project level -->
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- Treat hardcoded text as error (force string resources) -->
    <issue id="HardcodedText" severity="error" />

    <!-- Ignore specific warning -->
    <issue id="ObsoleteLintCustomCheck" severity="ignore" />

    <!-- Security: treat as error -->
    <issue id="HardcodedDebugMode" severity="error" />
    <issue id="ExportedContentProvider" severity="error" />
    <issue id="ExportedReceiver" severity="error" />
    <issue id="ExportedService" severity="error" />

    <!-- Performance -->
    <issue id="UnusedResources" severity="warning" />
    <issue id="Overdraw" severity="warning" />

    <!-- Accessibility -->
    <issue id="ContentDescription" severity="error" />

    <!-- Exclude generated code -->
    <issue id="all">
        <ignore path="**/build/**" />
        <ignore path="**/generated/**" />
    </issue>
</lint>
```

```kotlin
// build.gradle.kts — Lint configuration
android {
    lint {
        abortOnError = true                // Fail build on errors
        warningsAsErrors = false
        checkReleaseBuilds = true          // Run on release builds
        checkDependencies = true           // Check library dependencies
        htmlReport = true
        htmlOutput = file("${project.buildDir}/reports/lint-results.html")
        xmlReport = true                   // For CI tools
        sarifReport = true                 // For GitHub Code Scanning

        // Baselines: "freeze" existing issues, only report new ones
        baseline = file("lint-baseline.xml")

        // Disable specific checks
        disable += setOf("MissingTranslation", "ExtraTranslation")

        // Enable specific checks
        enable += setOf("RtlHardcoded", "RtlCompat", "RtlEnabled")

        // Treat issues as fatal
        fatal += setOf("NewApi", "InlinedApi", "HardcodedDebugMode")
    }
}
```

```kotlin
// Suppress lint warnings in code
@SuppressLint("NewApi")
fun useNewApi() {
    // Suppressed — but prefer SDK_INT check instead:
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Safe to use API 33+ features here
    }
}

// Suppress in XML
// tools:ignore="HardcodedText,ContentDescription"
```

```bash
# Run lint from command line
./gradlew lint                         # All variants
./gradlew lintDebug                    # Debug variant
./gradlew lintRelease                  # Release variant

# Generate baseline (snapshot of existing issues)
./gradlew lintDebug -Dlint.baselines.continue=true
# First run creates lint-baseline.xml, future runs only report NEW issues

# Output locations:
# app/build/reports/lint-results-debug.html   (human readable)
# app/build/reports/lint-results-debug.xml    (machine readable)
# app/build/reports/lint-results-debug.sarif  (GitHub integration)
```

### Writing Custom Lint Rules

```kotlin
// Custom lint rules catch project-specific issues
// Create a separate module: lint-rules/

// build.gradle.kts (lint-rules module)
plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    compileOnly("com.android.tools.lint:lint-api:31.4.0")
    compileOnly("com.android.tools.lint:lint-checks:31.4.0")
    testImplementation("com.android.tools.lint:lint-tests:31.4.0")
}

// ═══ Example: Detect direct Log usage (should use Timber) ═══
class TimberLogDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> {
        return listOf("v", "d", "i", "w", "e", "wtf")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val evaluator = context.evaluator
        if (evaluator.isMemberInClass(method, "android.util.Log")) {
            context.report(
                issue = ISSUE,
                scope = node,
                location = context.getLocation(node),
                message = "Use Timber instead of android.util.Log"
            )
        }
    }

    companion object {
        val ISSUE = Issue.create(
            id = "DirectLogUsage",
            briefDescription = "Direct android.util.Log usage",
            explanation = "Use Timber for logging. It provides automatic TAG, " +
                "format strings, and can be configured per build type.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                TimberLogDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}

// Register in IssueRegistry
class CustomIssueRegistry : IssueRegistry() {
    override val issues = listOf(TimberLogDetector.ISSUE)
    override val api = CURRENT_API
}

// In app/build.gradle.kts:
// dependencies {
//     lintChecks(project(":lint-rules"))
// }
```

---

### Detekt for Kotlin

**Detekt** is a static code analysis tool specifically for Kotlin. It finds code smells, complexity issues, naming violations, and potential bugs that Android Lint doesn't catch.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Detekt Rule Sets & Examples                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Rule Set         What It Catches                    Example            │
│  ────────         ──────────────                     ───────            │
│  complexity       Long methods (>60 lines)           fun doAll() {...}  │
│                   Complex methods (>15 branches)     when(x) { 20 cas} │
│                   Large classes (>20 functions)      God classes        │
│                   Long parameter lists (>6 params)   fun f(a,b,c,d,e,f)│
│                   Nested blocks (>4 levels)          if{if{if{if{}}}}  │
│                                                                          │
│  coroutines       GlobalScope usage                  GlobalScope.launch │
│                   Redundant suspend modifier         suspend fun f() {} │
│                   Missing delay in infinite loop                        │
│                                                                          │
│  empty-blocks     Empty catch blocks                 catch(e: Ex) { }  │
│                   Empty if/else bodies               if (x) { }        │
│                   Empty functions                    fun init() { }    │
│                                                                          │
│  exceptions       Swallowed exceptions               catch { return }  │
│                   Too generic exception caught        catch(e: Exception)│
│                   Throwing generic exceptions         throw Exception() │
│                   Rethrow caught exception            catch { throw it }│
│                                                                          │
│  naming           Function naming convention         fun MyFunction()  │
│                   Variable naming                    val MyVar = 1     │
│                   Class naming                       class my_class    │
│                   Package naming                     com.Example       │
│                   Enum entry naming                  enum { my_entry } │
│                                                                          │
│  performance      Spread operator in func calls      foo(*array)       │
│                   For-each on range                  (0..n).forEach    │
│                   Array primitives boxing            Array<Int> vs IntA │
│                                                                          │
│  potential-bugs    Implicit default locale            String.format()   │
│                   Unsafe cast                        val x = y as Str  │
│                   Unused variable                    val unused = calc()│
│                   Unreachable code                   return; doMore()  │
│                   Equals with hash code              equals() no hash  │
│                                                                          │
│  style            Magic numbers                      if (status == 42) │
│                   Wildcard imports                   import java.util.*│
│                   Max line length                    120+ chars        │
│                   Return count (>2 returns)          multiple returns  │
│                   Unnecessary parentheses            (x + 1).let       │
│                   Redundant visibility modifier      public class (def)│
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// build.gradle.kts (project-level)
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

// build.gradle.kts (app-level)
detekt {
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true      // Use default rules + overrides
    allRules = false                   // Only enable default-active rules
    parallel = true                    // Run in parallel
    autoCorrect = true                 // Auto-fix where possible

    // Source paths
    source.setFrom(
        "src/main/java",
        "src/main/kotlin"
    )

    // Baseline: freeze existing issues
    baseline = file("detekt-baseline.xml")

    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)       // GitHub Code Scanning
    }
}

// Add formatting rules (wraps ktlint)
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
    // Custom rule sets:
    // detektPlugins("com.twitter.compose.rules:detekt:0.0.26")  // Compose rules
}
```

```yaml
# config/detekt/detekt.yml — Full configuration

complexity:
  ComplexMethod:
    active: true
    threshold: 15                      # Max cognitive complexity
  LongMethod:
    active: true
    threshold: 60                      # Max lines per method
  LongParameterList:
    active: true
    functionThreshold: 6
    constructorThreshold: 7
    ignoreDefaultParameters: true      # Don't count params with defaults
    ignoreDataClasses: true            # Data classes often have many params
  TooManyFunctions:
    active: true
    thresholdInFiles: 15
    thresholdInClasses: 15
    thresholdInInterfaces: 10
  NestedBlockDepth:
    active: true
    threshold: 4
  CyclomaticComplexMethod:
    active: true
    threshold: 15

coroutines:
  GlobalCoroutineUsage:
    active: true                       # Ban GlobalScope
  RedundantSuspendModifier:
    active: true
  SuspendFunWithFlowReturnType:
    active: true                       # suspend fun shouldn't return Flow

style:
  MagicNumber:
    active: true
    ignoreNumbers:
      - '-1'
      - '0'
      - '1'
      - '2'
    ignoreHashCodeFunction: true
    ignorePropertyDeclaration: true    # val MAX_RETRY = 3 is OK
    ignoreAnnotation: true             # @Preview(widthDp = 320)
    ignoreEnums: true
    ignoreCompanionObjectPropertyDeclaration: true
  MaxLineLength:
    active: true
    maxLineLength: 120
    excludeCommentStatements: true
  WildcardImport:
    active: true
    excludeImports:                    # Allow Compose wildcard imports
      - 'kotlinx.coroutines.*'
      - 'androidx.compose.ui.*'
      - 'androidx.compose.foundation.*'
      - 'androidx.compose.material3.*'
  ReturnCount:
    active: true
    max: 3
    excludeGuardClauses: true          # Early returns don't count
  ForbiddenComment:
    active: true
    values:
      - 'TODO'                         # Force resolution of TODOs
      - 'FIXME'
      - 'HACK'
    allowedPatterns: 'TODO\\(\\w+\\)'  # Allow TODO(username)

naming:
  FunctionNaming:
    active: true
    functionPattern: '[a-z][a-zA-Z0-9]*'
    excludes: ['**/test/**']           # Test methods can use backtick names
    ignoreAnnotated: ['Composable']    # Composable functions are PascalCase
  VariableNaming:
    active: true
    variablePattern: '[a-z][a-zA-Z0-9]*'
    privateVariablePattern: '_?[a-z][a-zA-Z0-9]*'

exceptions:
  SwallowedException:
    active: true
    ignoredExceptionTypes:
      - 'InterruptedException'
      - 'CancellationException'
  TooGenericExceptionCaught:
    active: true
    exceptionNames:
      - 'Exception'
      - 'RuntimeException'
      - 'Throwable'
  TooGenericExceptionThrown:
    active: true

empty-blocks:
  EmptyCatchBlock:
    active: true
    allowedExceptionNameRegex: '_|(ignore|expected).*'
  EmptyFunctionBlock:
    active: true
    ignoreOverridden: true             # OK for empty overrides
```

```bash
# Run detekt
./gradlew detekt                       # Full analysis
./gradlew detektDebug                  # Debug source set only
./gradlew detektBaseline               # Generate baseline file

# Output: app/build/reports/detekt/detekt.html
```

---

### SonarQube

**SonarQube** is an enterprise-grade code quality platform that provides continuous inspection of code quality. It tracks metrics over time and enforces **quality gates** that can block merges.

```
┌─────────────────────────────────────────────────────────────────────────┐
│               SonarQube Architecture & Metrics                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────┐     ┌──────────────┐     ┌───────────────────────┐       │
│  │ Developer│────>│  CI/CD       │────>│  SonarQube Server     │       │
│  │ pushes   │     │  Pipeline    │     │                       │       │
│  │ code     │     │  runs sonar  │     │  ┌─────────────────┐  │       │
│  └──────────┘     │  scanner     │     │  │   Dashboard     │  │       │
│                    └──────────────┘     │  │                 │  │       │
│                                         │  │ Reliability  A  │  │       │
│  Metrics tracked:                       │  │ Security     A  │  │       │
│  ┌──────────────────────────────────┐  │  │ Maintain.    B  │  │       │
│  │ Bugs         — Logic errors      │  │  │ Coverage   82%  │  │       │
│  │ Vulnerab.    — Security issues   │  │  │ Duplicat.  3.2% │  │       │
│  │ Code Smells  — Maintainability   │  │  │ Lines     12.4K │  │       │
│  │ Coverage     — Test coverage %   │  │  │ Tech Debt  2d   │  │       │
│  │ Duplications — Copy-paste code   │  │  └─────────────────┘  │       │
│  │ Tech Debt    — Estimated fix time│  │                       │       │
│  │ Complexity   — Cyclomatic/cognit.│  │  Quality Gate:        │       │
│  └──────────────────────────────────┘  │  ┌─────────────────┐  │       │
│                                         │  │ ✓ Coverage >80% │  │       │
│  SQALE Rating (maintainability):       │  │ ✓ No new bugs   │  │       │
│  A = 0-5% tech debt ratio             │  │ ✓ No new vulns  │  │       │
│  B = 6-10%                              │  │ ✗ Smells < 20   │  │       │
│  C = 11-20%                             │  │   GATE: FAILED  │  │       │
│  D = 21-50%                             │  └─────────────────┘  │       │
│  E = 51%+                               │                       │       │
│                                         └───────────────────────┘       │
│                                                                          │
│  New Code Period:                                                       │
│  SonarQube focuses on NEW code quality (since last version/branch)     │
│  → Prevents "big bang" cleanups, ensures ongoing quality               │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// build.gradle.kts — SonarQube plugin
plugins {
    id("org.sonarqube") version "5.0.0.4638"
}

sonarqube {
    properties {
        property("sonar.projectKey", "com.example:my-app")
        property("sonar.organization", "my-org")
        property("sonar.host.url", "https://sonarqube.example.com")

        // Source paths
        property("sonar.sources", "src/main/java,src/main/kotlin")
        property("sonar.tests", "src/test/java,src/test/kotlin,src/androidTest")

        // Integration with other tools
        property("sonar.android.lint.report", "build/reports/lint-results-debug.xml")
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")
        property("sonar.coverage.jacoco.xmlReportPaths",
            "build/reports/jacoco/testDebugUnitTestCoverage.xml")

        // Exclusions
        property("sonar.exclusions", listOf(
            "**/build/**",
            "**/generated/**",
            "**/*Test*/**",
            "**/R.class",
            "**/BuildConfig.*"
        ).joinToString(","))
    }
}
```

```bash
# Run SonarQube analysis (typically in CI)
./gradlew sonarqube -Dsonar.token=$SONAR_TOKEN

# Full CI sequence:
./gradlew clean testDebugUnitTest lintDebug detekt jacocoTestReport sonarqube
```

---

### ktlint for Code Formatting

**ktlint** enforces a consistent Kotlin coding style based on the official Kotlin coding conventions and the Android Kotlin style guide. It can **auto-fix** most formatting issues.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              ktlint — What It Checks                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │  Indentation      — 4 spaces, no tabs                          │     │
│  │  Imports           — No wildcard imports, sorted                │     │
│  │  Spacing           — Around operators, after commas, etc.      │     │
│  │  Trailing comma    — Enforce or disallow trailing commas       │     │
│  │  Max line length   — 120 characters default                    │     │
│  │  Blank lines       — Between functions, classes                │     │
│  │  Braces            — Consistent brace placement                │     │
│  │  String templates  — No unnecessary braces in $var             │     │
│  │  Final newline     — File ends with newline                    │     │
│  │  No semi-colons    — Remove unnecessary semicolons             │     │
│  │  Parameter wrapping— Consistent multiline parameter lists     │     │
│  │  Annotation order  — @Composable before @Preview              │     │
│  └────────────────────────────────────────────────────────────────┘     │
│                                                                          │
│  Auto-fix: Most issues are automatically fixable with ktlintFormat     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// build.gradle.kts — ktlint via Gradle plugin
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

ktlint {
    android.set(true)                  // Use Android style guide
    verbose.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)          // Fail build on violations
    enableExperimentalRules.set(true)

    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}
```

```ini
# .editorconfig — ktlint configuration (place in project root)
[*.{kt,kts}]
indent_size = 4
indent_style = space
max_line_length = 120
insert_final_newline = true
end_of_line = lf

# ktlint specific rules
ktlint_standard_no-wildcard-imports = enabled
ktlint_standard_trailing-comma-on-call-site = enabled
ktlint_standard_trailing-comma-on-declaration-site = enabled
ktlint_standard_function-naming = disabled          # For @Composable PascalCase
ktlint_standard_filename = disabled                 # Compose files often differ
ktlint_standard_package-name = enabled
ktlint_standard_import-ordering = enabled
```

```bash
# Run ktlint
./gradlew ktlintCheck                  # Check formatting (fails on violations)
./gradlew ktlintFormat                 # Auto-fix formatting issues

# Generate baseline
./gradlew ktlintGenerateBaseline       # Freeze existing violations
```

---

## Test Coverage with JaCoCo

### Theory

**JaCoCo** (Java Code Coverage) measures how much of your code is exercised by tests. It reports line coverage, branch coverage, and method coverage. Useful for enforcing minimum coverage thresholds in CI.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              JaCoCo Coverage Metrics                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Coverage Types:                                                        │
│  ┌───────────────────────────────────────────────────────────────┐      │
│  │  Line Coverage      — Was this line executed?                  │      │
│  │                      ✓ Green = executed, ✗ Red = missed       │      │
│  │                                                                │      │
│  │  Branch Coverage    — Were both sides of if/when tested?      │      │
│  │                      if (condition) { ✓ } else { ✗ }         │      │
│  │                      Yellow = partially covered               │      │
│  │                                                                │      │
│  │  Method Coverage    — Was this method called at all?          │      │
│  │                                                                │      │
│  │  Class Coverage     — Was any method in this class executed?  │      │
│  │                                                                │      │
│  │  Instruction Coverage — Bytecode instruction level            │      │
│  └───────────────────────────────────────────────────────────────┘      │
│                                                                          │
│  What to aim for:                                                       │
│  • 80%+ line coverage on business logic / ViewModels / UseCases        │
│  • Don't chase 100% — diminishing returns on trivial code              │
│  • Focus on: critical paths, error handling, edge cases                 │
│  • Exclude: generated code, DI modules, UI-only code, data classes     │
│                                                                          │
│  Report output:                                                         │
│  ┌──────────────────────────────────────────────────────────┐          │
│  │  Package               │  Line %  │  Branch %  │  Missed │          │
│  │  com.example.domain    │   92%    │    85%     │    12   │          │
│  │  com.example.data      │   78%    │    70%     │    45   │          │
│  │  com.example.ui        │   45%    │    30%     │   120   │          │
│  │  ─────────────────────────────────────────────────────── │          │
│  │  Total                 │   72%    │    62%     │   177   │          │
│  └──────────────────────────────────────────────────────────┘          │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// build.gradle.kts — JaCoCo setup for Android
plugins {
    id("jacoco")
}

android {
    buildTypes {
        debug {
            enableUnitTestCoverage = true       // Enable coverage for unit tests
            enableAndroidTestCoverage = true    // Enable for instrumented tests
        }
    }
}

// Custom JaCoCo report task
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)                  // For SonarQube/CI
        html.required.set(true)                 // For human review
        csv.required.set(false)
    }

    val fileFilter = listOf(
        // Exclude generated code
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*_Factory.*",                       // Dagger/Hilt generated
        "**/*_HiltModules*.*",
        "**/*Module_*.*",
        "**/*Directions*.*",                    // Navigation generated
        "**/*Args*.*",
        "**/hilt_aggregated_deps/**",
        "**/*_Impl*.*",                         // Room generated
        // Exclude UI-only code
        "**/*Activity*.*",
        "**/*Fragment*.*",
        "**/*Adapter*.*",
        // Exclude data classes
        "**/model/**",
        "**/dto/**",
        "**/entity/**"
    )

    val debugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(buildDir) {
        include("jacoco/testDebugUnitTest.exec")
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    })
}

// Enforce minimum coverage (CI gate)
tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    dependsOn("jacocoTestReport")

    violationRules {
        rule {
            limit {
                minimum = 0.80.toBigDecimal()   // 80% minimum coverage
            }
        }
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = 0.70.toBigDecimal()   // 70% per class minimum
            }
            excludes = listOf(
                "*.BuildConfig",
                "*.R",
                "*.*Module*",
                "*.di.*"
            )
        }
    }
}
```

```bash
# Generate coverage report
./gradlew testDebugUnitTest jacocoTestReport

# Output: app/build/reports/jacoco/jacocoTestReport/html/index.html

# Verify coverage thresholds (fails if below minimum)
./gradlew jacocoCoverageVerification
```

---

## Code Review Best Practices

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Code Review Checklist for Android                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ═══ As a Reviewer ═══                                                  │
│  Functionality:                                                         │
│  ✓ Does the code do what the PR description says?                      │
│  ✓ Are edge cases handled? (null, empty, network error, rotation)      │
│  ✓ Is the logic correct and complete?                                  │
│  ✓ Are lifecycle concerns addressed? (config change, process death)    │
│                                                                          │
│  Code Quality:                                                          │
│  ✓ Is the code readable? Could you maintain it in 6 months?           │
│  ✓ Does it follow project conventions and architecture?                │
│  ✓ Are there tests for new functionality?                              │
│  ✓ Do tests verify behavior, not implementation?                       │
│  ✓ Are error messages helpful for debugging?                           │
│                                                                          │
│  Android-Specific:                                                      │
│  ✓ No heavy work on main thread? (disk I/O, network, parsing)         │
│  ✓ Memory leaks? (context references, unregistered callbacks)          │
│  ✓ Compose: unnecessary recompositions? (stability, derivedStateOf)    │
│  ✓ ProGuard/R8 rules needed for new reflection usage?                  │
│  ✓ Backward compatibility? (minSdk API check)                         │
│  ✓ Security? (no hardcoded secrets, proper encryption)                 │
│                                                                          │
│  ═══ As an Author ═══                                                   │
│  ✓ Self-review diff before requesting review                           │
│  ✓ Keep PRs small and focused (< 400 lines, single concern)           │
│  ✓ Write clear description with context ("why", not just "what")      │
│  ✓ Add screenshots/recordings for UI changes                          │
│  ✓ Respond to ALL review comments (even if just "Done")               │
│  ✓ Don't take feedback personally — reviews improve everyone          │
│                                                                          │
│  ═══ Automated Review Tools (add to CI) ═══                            │
│  • Danger       — Automate PR hygiene (size, labels, description)     │
│  • Reviewdog    — Post lint/detekt results as PR comments             │
│  • SonarQube    — Quality gate blocks merge                           │
│  • Codecov      — Coverage diff on each PR                            │
│  • GitHub Actions — Run lint/test/detekt/ktlint on every PR          │
│  • Renovate/Dependabot — Auto-update dependencies                    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### GitHub Actions CI Pipeline — Complete Example

```yaml
# .github/workflows/android-ci.yml
name: Android CI

on:
  pull_request:
    branches: [ main, develop ]
  push:
    branches: [ main ]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true       # Cancel outdated runs

jobs:
  # ═══ Stage 1: Quick checks (fast feedback) ═══
  lint-and-format:
    runs-on: ubuntu-latest
    timeout-minutes: 15
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/main' }}

      - name: Run ktlint
        run: ./gradlew ktlintCheck

      - name: Run Detekt
        run: ./gradlew detekt

      - name: Run Android Lint
        run: ./gradlew lintDebug

      - name: Upload Lint Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: lint-report
          path: app/build/reports/lint-results-debug.html

  # ═══ Stage 2: Unit tests + coverage ═══
  unit-tests:
    runs-on: ubuntu-latest
    timeout-minutes: 20
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest

      - name: Generate Coverage Report
        run: ./gradlew jacocoTestReport

      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          file: app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
          flags: unittests

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: app/build/reports/tests/testDebugUnitTest/

  # ═══ Stage 3: Build APK ═══
  build:
    needs: [lint-and-format, unit-tests]
    runs-on: ubuntu-latest
    timeout-minutes: 20
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Build Debug APK
        run: ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk

  # ═══ Stage 4: Instrumented tests (optional, slower) ═══
  instrumented-tests:
    needs: build
    runs-on: ubuntu-latest
    timeout-minutes: 30
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Enable KVM
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm

      - name: Run Instrumented Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          target: google_apis
          arch: x86_64
          profile: pixel_6
          script: ./gradlew connectedDebugAndroidTest
```

---

# 16.4 Debugging Tools

## Android Debug Bridge (ADB)

### Theory

ADB is a versatile command-line tool that communicates with connected Android devices/emulators. It consists of three components:

- **Client** — Runs on your dev machine (the `adb` command)
- **Server** — Background process on your machine (port 5037) managing connections
- **Daemon (adbd)** — Runs on each device/emulator, executes commands

```
┌─────────────────────────────────────────────────────────────────────────┐
│                   ADB Architecture                                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Developer Machine                     Device / Emulator                │
│  ┌──────────────────────────────┐     ┌──────────────────────┐         │
│  │                              │     │                      │         │
│  │  ┌──────────┐  ┌──────────┐ │     │  ┌──────────────┐   │         │
│  │  │  adb     │  │  adb     │ │ USB │  │    adbd      │   │         │
│  │  │  client  │─>│  server  │─┼─────┼─>│   (daemon)   │   │         │
│  │  │ (CLI)    │  │ (port    │ │ or  │  │  (port 5555) │   │         │
│  │  │          │  │  5037)   │ │ WiFi│  │              │   │         │
│  │  └──────────┘  └──────────┘ │     │  └──────────────┘   │         │
│  │                     │        │     │         │            │         │
│  │  ┌──────────┐       │        │     │  ┌──────┴───────┐   │         │
│  │  │ Android  │───────┘        │     │  │    Android   │   │         │
│  │  │ Studio   │                │     │  │      OS      │   │         │
│  │  └──────────┘                │     │  └──────────────┘   │         │
│  └──────────────────────────────┘     └──────────────────────┘         │
│                                                                          │
│  Connection types:                                                      │
│  • USB — Default, most reliable                                        │
│  • Wi-Fi (Android 11+) — Wireless debugging with pairing              │
│  • Wi-Fi (legacy) — adb tcpip 5555 + adb connect                      │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Essential ADB Commands — Complete Reference

```bash
# ═══ DEVICE MANAGEMENT ═══
adb devices                             # List connected devices
adb devices -l                          # List with model, transport details
adb -s <serial> <command>               # Target specific device
adb kill-server                         # Kill ADB server
adb start-server                        # Start ADB server
adb version                             # Show ADB version

# ═══ WIRELESS DEBUGGING ═══
# Android 11+ (Developer Options → Wireless Debugging)
adb pair 192.168.1.100:37099            # Pair with pairing code
adb connect 192.168.1.100:43999         # Connect after pairing

# Legacy wireless (requires USB first)
adb tcpip 5555                          # Switch device to TCP mode
adb connect 192.168.1.100:5555          # Connect over WiFi
adb disconnect                          # Disconnect all wireless

# ═══ APP MANAGEMENT ═══
adb install app.apk                     # Install APK
adb install -r app.apk                  # Reinstall keeping data
adb install -t app.apk                  # Allow test APKs
adb install-multiple base.apk split.apk # Install split APKs
adb uninstall com.example.app           # Uninstall
adb uninstall -k com.example.app        # Uninstall but keep data

adb shell pm list packages              # List ALL packages
adb shell pm list packages -3           # List third-party only
adb shell pm list packages | grep example  # Filter by name
adb shell pm clear com.example.app      # Clear ALL app data
adb shell pm grant com.example.app android.permission.CAMERA  # Grant perm
adb shell pm revoke com.example.app android.permission.CAMERA # Revoke perm

# ═══ FILE TRANSFER ═══
adb push local.txt /sdcard/             # Push file to device
adb push folder/ /sdcard/              # Push directory
adb pull /sdcard/file.txt ./            # Pull file from device
adb pull /sdcard/folder/ ./             # Pull directory
adb shell ls /sdcard/                   # List files on device

# ═══ SHELL ACCESS ═══
adb shell                               # Interactive shell
adb shell whoami                        # Run single command
adb shell id                            # Show user/group info

# ═══ INPUT SIMULATION ═══
adb shell input text "hello"            # Type text
adb shell input tap 500 500             # Tap at coordinates
adb shell input swipe 100 500 400 500 300  # Swipe (with duration ms)
adb shell input keyevent 4              # Back button
adb shell input keyevent 3              # Home button
adb shell input keyevent 26             # Power button
adb shell input keyevent 82             # Menu button
adb shell input keyevent 24             # Volume up
adb shell input keyevent 25             # Volume down
adb shell input keyevent 187            # Recent apps

# ═══ LOGGING ═══
adb logcat                              # Stream all logs
adb logcat -s MyTag                     # Filter by tag
adb logcat *:E                          # Errors only
adb logcat -c                           # Clear log buffer
adb logcat -d > log.txt                 # Dump to file and exit
adb logcat -b crash                     # Show crash buffer only
adb logcat --pid=<pid>                  # Filter by process ID
adb logcat -v time                      # Show timestamps
adb logcat -v threadtime                # Show thread + time
adb logcat -e "Exception|Error"         # Regex filter

# ═══ SCREENSHOTS & RECORDING ═══
adb shell screencap /sdcard/screen.png
adb pull /sdcard/screen.png
adb exec-out screencap -p > screen.png  # Direct to PC (faster)

adb shell screenrecord /sdcard/video.mp4       # Record (max 3 min)
adb shell screenrecord --time-limit 30 /sdcard/short.mp4  # 30 sec
adb shell screenrecord --size 720x1280 /sdcard/vid.mp4    # Resolution
adb pull /sdcard/video.mp4

# ═══ SYSTEM INFO ═══
adb shell dumpsys battery               # Battery status
adb shell dumpsys meminfo com.example.app  # Memory usage
adb shell dumpsys activity activities    # Activity stack
adb shell dumpsys activity services      # Running services
adb shell dumpsys window displays       # Display info
adb shell dumpsys package com.example.app  # App info (perms, etc.)
adb shell cat /proc/cpuinfo             # CPU info
adb shell df                            # Disk space
adb shell getprop                       # All system properties
adb shell getprop ro.build.version.sdk  # API level

# ═══ INTENT LAUNCHING ═══
adb shell am start -n com.example.app/.MainActivity
adb shell am start -a android.intent.action.VIEW \
  -d "https://example.com"                           # Open URL
adb shell am start -a android.intent.action.SEND \
  --es android.intent.extra.TEXT "Hello"              # Share text
adb shell am broadcast -a com.example.CUSTOM_ACTION   # Send broadcast
adb shell am force-stop com.example.app               # Force stop app
adb shell am kill com.example.app                     # Kill background

# ═══ SIMULATING CONDITIONS ═══
adb shell svc wifi disable              # Disable WiFi
adb shell svc wifi enable               # Enable WiFi
adb shell svc data disable              # Disable mobile data
adb emu geo fix -122.084 37.422         # Set GPS (emulator only)
adb emu geo fix -122.084 37.422 100     # With altitude

# ═══ PERFORMANCE ═══
adb shell dumpsys gfxinfo com.example.app  # Frame rendering stats
adb shell dumpsys gfxinfo com.example.app reset  # Reset stats
adb shell cmd package compile -m speed-profile -f com.example.app  # AOT compile

# ═══ APP BUNDLE TESTING ═══
# Install from AAB using bundletool
# java -jar bundletool.jar build-apks --bundle=app.aab --output=app.apks
# java -jar bundletool.jar install-apks --apks=app.apks
```

---

## StrictMode

### Theory

**StrictMode** is a developer tool that detects accidental disk or network access on the main thread, resource leaks, and other coding mistakes during development. It should only be enabled in debug builds.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              StrictMode Policies                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Thread Policy (detects main thread violations):                        │
│  ┌───────────────────────────────────────────────────────────────┐      │
│  │  detectDiskReads()         — File read on main thread         │      │
│  │  detectDiskWrites()        — File write on main thread        │      │
│  │  detectNetwork()           — Network call on main thread      │      │
│  │  detectCustomSlowCalls()   — Custom Trace sections > 100ms   │      │
│  │  detectResourceMismatches()— Resource type mismatches         │      │
│  │  detectUnbufferedIo()      — Unbuffered I/O operations        │      │
│  │  detectAll()               — All of the above                 │      │
│  └───────────────────────────────────────────────────────────────┘      │
│                                                                          │
│  VM Policy (detects process-wide violations):                           │
│  ┌───────────────────────────────────────────────────────────────┐      │
│  │  detectLeakedClosableObjects()  — Unclosed streams/cursors   │      │
│  │  detectLeakedSqlLiteObjects()   — Unclosed SQLite objects     │      │
│  │  detectActivityLeaks()          — Leaked Activity instances   │      │
│  │  detectCleartextNetwork()       — HTTP without TLS            │      │
│  │  detectContentUriWithoutPermission() — Missing URI perms     │      │
│  │  detectCredentialProtectedWhileLocked()                       │      │
│  │  detectFileUriExposure()        — file:// URI shared          │      │
│  │  detectAll()                    — All of the above            │      │
│  └───────────────────────────────────────────────────────────────┘      │
│                                                                          │
│  Penalties (what happens on violation):                                  │
│  • penaltyLog()         — Log to Logcat (recommended)                  │
│  • penaltyDeath()       — Crash the app (aggressive)                   │
│  • penaltyFlashScreen() — Flash screen red                             │
│  • penaltyDialog()      — Show dialog                                  │
│  • penaltyDropBox()     — Write to DropBox (system diagnostic store)   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // Thread policy: detect main thread violations
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .detectCustomSlowCalls()
                    .penaltyLog()                // Log violations
                    .penaltyFlashScreen()         // Visual indicator
                    // .penaltyDeath()            // Crash on violation (aggressive)
                    .build()
            )

            // VM policy: detect process-wide issues
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .detectLeakedSqlLiteObjects()
                    .detectActivityLeaks()
                    .detectCleartextNetwork()
                    .detectFileUriExposure()
                    .penaltyLog()
                    .build()
            )
        }
    }
}

// StrictMode violations appear in Logcat with tag "StrictMode":
// D/StrictMode: StrictMode policy violation: android.os.StrictMode$StrictModeDiskReadViolation
//     at com.example.app.MainActivity.onCreate(MainActivity.kt:42)
```

---

## LeakCanary — Memory Leak Detection

### Theory

**LeakCanary** automatically detects memory leaks in Android apps. It watches for retained objects that should have been garbage collected (Activities, Fragments, ViewModels, Views) and provides a clear reference chain showing what's keeping the object alive.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              LeakCanary — How It Works                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────┐                                                     │
│  │  Object gets   │  Activity.onDestroy(), Fragment.onDestroyView()     │
│  │  destroyed     │  ViewModel.onCleared(), View.onDetach()            │
│  └───────┬────────┘                                                     │
│          │                                                               │
│          ▼                                                               │
│  ┌────────────────┐                                                     │
│  │  LeakCanary     │  Adds a WeakReference to the object               │
│  │  watches it     │  Waits 5 seconds for GC                           │
│  └───────┬────────┘                                                     │
│          │                                                               │
│          ▼                                                               │
│  ┌────────────────┐                                                     │
│  │  Object still   │  YES → Triggers heap dump (.hprof)                │
│  │  in memory?     │  NO  → Object was properly GC'd (no leak)        │
│  └───────┬────────┘                                                     │
│          │ YES                                                           │
│          ▼                                                               │
│  ┌────────────────┐                                                     │
│  │  Analyze heap   │  Uses Shark library to find GC root path          │
│  │  dump           │  to the leaked object                             │
│  └───────┬────────┘                                                     │
│          │                                                               │
│          ▼                                                               │
│  ┌────────────────┐                                                     │
│  │  Show leak      │  Notification + UI with reference chain:          │
│  │  trace          │                                                    │
│  │                  │  GCRoot (Thread)                                  │
│  │                  │   ↓                                               │
│  │                  │  Singleton.callback                               │
│  │                  │   ↓                                               │
│  │                  │  LoginCallback.activity    ← LEAK!               │
│  │                  │   ↓                                               │
│  │                  │  LoginActivity (DESTROYED) ← should be GC'd     │
│  └────────────────┘                                                     │
│                                                                          │
│  Common leak patterns:                                                  │
│  • Static reference to Activity/Context                                │
│  • Inner class holding reference to outer Activity                     │
│  • Unregistered listeners/callbacks                                    │
│  • Handler with delayed message referencing Activity                   │
│  • Singleton holding View reference                                    │
│  • ViewModel surviving config change but holding View ref              │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// build.gradle.kts — LeakCanary setup (debug only!)
dependencies {
    // Only in debug builds — no impact on release APK
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}

// That's it! No code needed — LeakCanary auto-initializes via ContentProvider

// ═══ Optional: Watch custom objects for leaks ═══
class MyManager {
    fun destroy() {
        // After cleanup, tell LeakCanary to watch this object
        AppWatcher.objectWatcher.expectWeaklyReachable(
            watchedObject = this,
            description = "MyManager was destroyed"
        )
    }
}

// ═══ Optional: Configure what to watch ═══
// In Application class:
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // LeakCanary auto-watches:
        // ✓ Activities (after onDestroy)
        // ✓ Fragments (after onDestroyView)
        // ✓ Fragment Views (after onDestroyView)
        // ✓ ViewModels (after onCleared)
        // ✓ Services (after onDestroy)
        // ✓ RootViews (after removeView)
    }
}
```

---

## Chucker — HTTP Inspector (In-App)

### Theory

**Chucker** is an in-app HTTP inspector that shows all network requests/responses in a notification and dedicated UI. It works as an OkHttp interceptor — no external tools needed.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Chucker Architecture                                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  App                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐      │
│  │                                                               │      │
│  │  Retrofit / API call                                          │      │
│  │       │                                                       │      │
│  │       ▼                                                       │      │
│  │  OkHttpClient                                                 │      │
│  │  ┌───────────────────────────────────────────────────┐       │      │
│  │  │  ChuckerInterceptor  ← Captures request/response │       │      │
│  │  │       │                                           │       │      │
│  │  │       ├──> Log to local DB                        │       │      │
│  │  │       ├──> Show notification (tap to open UI)     │       │      │
│  │  │       │                                           │       │      │
│  │  │  Other interceptors (logging, auth, etc.)         │       │      │
│  │  └───────────────────────────────────────────────────┘       │      │
│  │       │                                                       │      │
│  │       ▼                                                       │      │
│  │  Network request → Server                                    │      │
│  │                                                               │      │
│  │  Chucker UI shows:                                            │      │
│  │  ┌────────────────────────────────────────────────┐          │      │
│  │  │ ✓ 200 GET  /api/users           143ms  24KB   │          │      │
│  │  │ ✓ 201 POST /api/login           230ms   1KB   │          │      │
│  │  │ ✗ 404 GET  /api/avatar           89ms   0B   │          │      │
│  │  │ ✗ 500 PUT  /api/profile         502ms   0B   │          │      │
│  │  │                                                │          │      │
│  │  │ Request headers, body (formatted JSON)         │          │      │
│  │  │ Response headers, body, timing, size           │          │      │
│  │  │ Search, share, clear history                   │          │      │
│  │  └────────────────────────────────────────────────┘          │      │
│  │                                                               │      │
│  └──────────────────────────────────────────────────────────────┘      │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// build.gradle.kts
dependencies {
    debugImplementation("com.github.chuckerteam.chucker:library:4.0.0")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:4.0.0")
}

// Network module setup
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                ChuckerInterceptor.Builder(context)
                    .collector(ChuckerCollector(
                        context = context,
                        showNotification = true,
                        retentionPeriod = RetentionManager.Period.ONE_HOUR
                    ))
                    .maxContentLength(250_000L)
                    .redactHeaders("Authorization", "Cookie")
                    .alwaysReadResponseBody(true)
                    .build()
            )
            .build()
    }
}
```

---

## Firebase Crashlytics

### Theory

**Firebase Crashlytics** is the industry-standard crash reporting tool for Android. It captures crashes, non-fatal exceptions, and ANRs in production and provides detailed reports with stack traces, device info, and breadcrumbs.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Crashlytics Architecture                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Production App                     Firebase Console                    │
│  ┌──────────────────────┐          ┌──────────────────────────┐        │
│  │                      │          │                          │        │
│  │  App crashes or      │  Upload  │  Dashboard               │        │
│  │  logs exception  ────┼─────────>│  ┌────────────────────┐  │        │
│  │                      │  (batch, │  │ Crash-free: 99.2%  │  │        │
│  │  Crashlytics SDK     │  WiFi)   │  │ Users affected: 42 │  │        │
│  │  captures:           │          │  │ Sessions: 1,234    │  │        │
│  │  • Stack trace       │          │  └────────────────────┘  │        │
│  │  • Device model      │          │                          │        │
│  │  • OS version        │          │  Issues (grouped):       │        │
│  │  • App version       │          │  ┌────────────────────┐  │        │
│  │  • RAM/disk state    │          │  │ #1 NullPointerExc  │  │        │
│  │  • Orientation       │          │  │    ProfileVM:42    │  │        │
│  │  • Custom keys       │          │  │    234 events     │  │        │
│  │  • Custom logs       │          │  │                    │  │        │
│  │  • Breadcrumbs       │          │  │ #2 IOException     │  │        │
│  │                      │          │  │    ApiClient:108   │  │        │
│  │  Symbolication:      │          │  │    89 events      │  │        │
│  │  Upload mapping.txt  │          │  └────────────────────┘  │        │
│  │  for obfuscated code │          │                          │        │
│  └──────────────────────┘          │  Velocity alerts:        │        │
│                                     │  (spike in new crashes)  │        │
│                                     └──────────────────────────┘        │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// build.gradle.kts (project)
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}

// build.gradle.kts (app)
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
}

// Enable mapping file upload for release builds
android {
    buildTypes {
        release {
            firebaseCrashlytics {
                mappingFileUploadEnabled = true
            }
        }
    }
}
```

```kotlin
// ═══ Crashlytics API Usage ═══
class UserRepository @Inject constructor(private val api: ApiService) {

    suspend fun getUser(id: String): User {
        Firebase.crashlytics.setCustomKey("user_id", id)
        Firebase.crashlytics.setCustomKey("screen", "profile")
        Firebase.crashlytics.setUserId(id)

        return try {
            api.getUser(id)
        } catch (e: HttpException) {
            Firebase.crashlytics.recordException(e)
            Firebase.crashlytics.log("Failed to fetch user $id: HTTP ${e.code()}")
            throw e
        }
    }
}

// ═══ Timber + Crashlytics integration ═══
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return

        Firebase.crashlytics.log("${priorityChar(priority)}/$tag: $message")
        t?.let { Firebase.crashlytics.recordException(it) }
    }

    private fun priorityChar(priority: Int) = when (priority) {
        Log.WARN -> 'W'
        Log.ERROR -> 'E'
        Log.ASSERT -> 'A'
        else -> '?'
    }
}

// ═══ Disable in debug / respect user privacy ═══
Firebase.crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
```

---

## Layout Inspector

### Theory

The **Layout Inspector** lets you examine your app's view hierarchy in real-time, supporting both traditional Views and Jetpack Compose. Essential for debugging UI issues like overlapping views, incorrect constraints, and unnecessary recompositions.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Layout Inspector — Views & Compose                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Access: View → Tool Windows → Layout Inspector                        │
│  (or: Running Devices panel → Layout Inspector tab)                    │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │ Component Tree      │  Live View / 3D    │  Attributes        │     │
│  │ ┌────────────────┐  │  ┌──────────────┐  │  ┌──────────────┐  │     │
│  │ │DecorView       │  │  │              │  │  │ id: tvTitle  │  │     │
│  │ │ ├─LinearLayout │  │  │ ╔══════════╗ │  │  │ text: "Hi"   │  │     │
│  │ │ │ ├─Toolbar    │  │  │ ║  Title   ║ │  │  │ size: wrap   │  │     │
│  │ │ │ ├─FrameLayout│  │  │ ╠══════════╣ │  │  │ margin: 16   │  │     │
│  │ │ │ │ ├─TextView │  │  │ ║  Body    ║ │  │  │ padding: 8   │  │     │
│  │ │ │ │ ├─Button   │  │  │ ╠══════════╣ │  │  │ visibility   │  │     │
│  │ │ │ │ └─ImageView│  │  │ ║  Button  ║ │  │  │ constraints  │  │     │
│  │ │ └─NavigationBar│  │  │ ╚══════════╝ │  │  │ enabled: true│  │     │
│  │ └────────────────┘  │  └──────────────┘  │  └──────────────┘  │     │
│  └────────────────────────────────────────────────────────────────┘     │
│                                                                          │
│  ═══ View Mode Features ═══                                            │
│  • Live updates — changes reflect in real-time                         │
│  • 3D rotation — visualize overlapping layers                          │
│  • Click to select — click any element to see properties               │
│  • Snapshot mode — capture for offline analysis                        │
│                                                                          │
│  ═══ Compose Mode Features ═══                                         │
│  • Semantics tree — see accessibility properties                       │
│  • Recomposition counts — find unnecessary recompositions              │
│  │  ┌─────────────────────────────────────────────┐                    │
│  │  │ Composable       │ Recomp │ Skipped │ Issue │                    │
│  │  │ UserCard()       │   42   │    0    │  ⚠    │ ← too many       │
│  │  │ ProfileImage()   │   42   │   38    │  OK   │ ← 38 skipped     │
│  │  │ UserName()       │    3   │    1    │  OK   │                    │
│  │  │ ActionButton()   │    1   │    0    │  OK   │                    │
│  │  └─────────────────────────────────────────────┘                    │
│  • State values — inspect remember'd state in composables              │
│  • Modifier chain — see how modifiers are applied                      │
│  • Layout bounds — overlay showing padding, margin, etc.              │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Network Inspector

### Theory

The **Network Inspector** (View → Tool Windows → App Inspection → Network Inspector) intercepts and displays all network traffic from your app in real time. Works with OkHttp, HttpURLConnection, and Volley.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Network Inspector                                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │  Timeline: ──△──△△──────▽▽▽▽──────△──▽▽▽──────               │     │
│  │  ▲ Sent: 5.2KB    ▼ Received: 342KB                           │     │
│  ├────────────────────────────────────────────────────────────────┤     │
│  │  Requests:                                                     │     │
│  │  ┌────────┬────────┬──────────────────┬──────┬──────┬──────┐  │     │
│  │  │ Status │ Method │ URL              │ Size │ Time │Thread│  │     │
│  │  ├────────┼────────┼──────────────────┼──────┼──────┼──────┤  │     │
│  │  │  200   │ GET    │ /api/users       │ 24KB │ 143ms│ IO-2 │  │     │
│  │  │  201   │ POST   │ /api/login       │  1KB │ 230ms│ IO-1 │  │     │
│  │  │  304   │ GET    │ /api/config      │  0B  │  45ms│ IO-3 │  │     │
│  │  │  404   │ GET    │ /api/avatar      │  0B  │  89ms│ IO-2 │  │     │
│  │  │  500   │ PUT    │ /api/profile     │  0B  │ 502ms│ IO-1 │  │     │
│  │  └────────┴────────┴──────────────────┴──────┴──────┴──────┘  │     │
│  │                                                                │     │
│  │  Selected: GET /api/users                                      │     │
│  │  ┌──────────────────────┬──────────────────────┐              │     │
│  │  │  Request              │  Response             │              │     │
│  │  │  Headers:              │  Headers:              │              │     │
│  │  │   Accept: app/json    │   Content-Type: json  │              │     │
│  │  │   Auth: Bearer...     │   Cache-Control: 60s  │              │     │
│  │  │   Accept-Encoding: gz │   Content-Encoding: gz│              │     │
│  │  │                       │                       │              │     │
│  │  │  Body: (none)         │  Body:                │              │     │
│  │  │                       │  [{                   │              │     │
│  │  │  Rules:               │    "id": 1,           │              │     │
│  │  │  ┌─────────────────┐  │    "name": "Alice"    │              │     │
│  │  │  │Create rule to   │  │  }, ...]              │              │     │
│  │  │  │modify request   │  │                       │              │     │
│  │  │  │or response for  │  │  ┌─────────────────┐  │              │     │
│  │  │  │testing edge     │  │  │Timing breakdown │  │              │     │
│  │  │  │cases            │  │  │DNS:    12ms     │  │              │     │
│  │  │  └─────────────────┘  │  │Connect: 45ms   │  │              │     │
│  │  │                       │  │TLS:     28ms   │  │              │     │
│  │  │                       │  │Request:  5ms   │  │              │     │
│  │  │                       │  │Response: 53ms  │  │              │     │
│  │  │                       │  └─────────────────┘  │              │     │
│  │  └──────────────────────┴──────────────────────┘              │     │
│  │                                                                │     │
│  │  Network Rules (mock responses for testing):                  │     │
│  │  • Change status code (200 → 500)                             │     │
│  │  • Modify response body                                       │     │
│  │  • Add latency                                                │     │
│  │  • Block specific URLs                                        │     │
│  └────────────────────────────────────────────────────────────────┘     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Database Inspector

### Theory

The **Database Inspector** (View → Tool Windows → App Inspection → Database Inspector) lets you inspect, query, and modify your app's Room/SQLite databases in real-time while the app is running.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Database Inspector                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │  Databases:  [app_database ▾]  Tables:  users │ posts │ tags  │     │
│  ├────────────────────────────────────────────────────────────────┤     │
│  │                                                                │     │
│  │  Table: users                          [🔄 Live updates: ON]  │     │
│  │  ┌─────┬──────────┬─────────────────┬──────────┬─────────┐   │     │
│  │  │ id  │  name    │  email          │ created  │ premium │   │     │
│  │  ├─────┼──────────┼─────────────────┼──────────┼─────────┤   │     │
│  │  │  1  │  Alice   │  alice@mail.com │ 2024-01  │  true   │   │     │
│  │  │  2  │  Bob     │  bob@mail.com   │ 2024-01  │  false  │   │     │
│  │  │  3  │  Carol   │  carol@mail.com │ 2024-02  │  true   │   │     │
│  │  └─────┴──────────┴─────────────────┴──────────┴─────────┘   │     │
│  │                                                                │     │
│  │  ┌── Query Console ───────────────────────────────────────┐   │     │
│  │  │ SELECT u.name, COUNT(p.id) as post_count               │   │     │
│  │  │ FROM users u                                            │   │     │
│  │  │ LEFT JOIN posts p ON u.id = p.user_id                   │   │     │
│  │  │ GROUP BY u.id                                           │   │     │
│  │  │ HAVING post_count > 5                                   │  [Run] │
│  │  └─────────────────────────────────────────────────────────┘   │     │
│  │                                                                │     │
│  │  Features:                                                     │     │
│  │  • Live updates — table refreshes as data changes             │     │
│  │  • Run custom SQL queries (SELECT, INSERT, UPDATE, DELETE)    │     │
│  │  • Edit cells directly (double-click a cell)                  │     │
│  │  • Export table data (CSV, SQL)                                │     │
│  │  • View Room DAO queries and their SQL                        │     │
│  │  • Multiple database support (Room, raw SQLite)               │     │
│  │  • Query history with favorites                               │     │
│  │                                                                │     │
│  │  Useful queries for debugging:                                │     │
│  │  • SELECT * FROM sqlite_master WHERE type='table'  — Schema  │     │
│  │  • EXPLAIN QUERY PLAN SELECT ... — Query optimization        │     │
│  │  • PRAGMA table_info(users)     — Column details              │     │
│  └────────────────────────────────────────────────────────────────┘     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Background Task Inspector

### Theory

The **Background Task Inspector** (App Inspection → Background Task Inspector) monitors WorkManager workers, jobs, alarms, and other background tasks. Essential for debugging work that runs when the app is not in the foreground.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Background Task Inspector                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │  Workers:                                                      │     │
│  │  ┌───────────────────┬──────────┬────────┬──────────────────┐ │     │
│  │  │ Class             │ Status   │ Tags   │ Constraints      │ │     │
│  │  ├───────────────────┼──────────┼────────┼──────────────────┤ │     │
│  │  │ SyncWorker        │ RUNNING  │ sync   │ Network: CONN.   │ │     │
│  │  │ UploadWorker      │ ENQUEUED │ upload │ Network: UNMET.  │ │     │
│  │  │ CleanupWorker     │ SUCCEEDED│ maint  │ Battery: NOT_LOW │ │     │
│  │  │ AnalyticsWorker   │ FAILED   │ analyt │ None             │ │     │
│  │  └───────────────────┴──────────┴────────┴──────────────────┘ │     │
│  │                                                                │     │
│  │  Worker Details (click to expand):                             │     │
│  │  ┌──────────────────────────────────────────────────────┐     │     │
│  │  │ SyncWorker                                            │     │     │
│  │  │ UUID: abc-123-def                                     │     │     │
│  │  │ State: RUNNING → progress: 45%                       │     │     │
│  │  │ Input: {"userId": "42"}                               │     │     │
│  │  │ Output: (pending)                                     │     │     │
│  │  │ Constraints: RequiresNetwork(CONNECTED)               │     │     │
│  │  │ Retry policy: LINEAR (30s backoff)                    │     │     │
│  │  │ Run attempt: 2                                        │     │     │
│  │  │ Tags: ["sync", "periodic"]                            │     │     │
│  │  │                                                       │     │     │
│  │  │ [Cancel] [Force Run] [View in Graph]                  │     │     │
│  │  └──────────────────────────────────────────────────────┘     │     │
│  │                                                                │     │
│  │  Work Chain Graph:                                             │     │
│  │  DownloadWorker → ParseWorker → SaveWorker                   │     │
│  │       ✓              ✓          (running)                    │     │
│  │                                                                │     │
│  └────────────────────────────────────────────────────────────────┘     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Breakpoints and Conditional Debugging

### Theory

Breakpoints pause execution at a specific line so you can inspect variables, call stack, and program state. Android Studio supports advanced breakpoint types beyond simple line breaks.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Breakpoint Types — Complete Reference                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  1. Line Breakpoint (click in gutter — red dot ●)                      │
│     Pauses execution when the line is reached                           │
│                                                                          │
│  2. Conditional Breakpoint (right-click breakpoint → Condition)         │
│     Only pauses when condition evaluates to true                        │
│     ┌──────────────────────────────────────────────────────┐            │
│     │ Examples:                                             │            │
│     │   userId == "abc123"                                  │            │
│     │   list.size > 100                                     │            │
│     │   user.name.startsWith("A")                           │            │
│     │   counter % 10 == 0    (break every 10th iteration)  │            │
│     │   exception.message?.contains("timeout") == true     │            │
│     └──────────────────────────────────────────────────────┘            │
│                                                                          │
│  3. Logging Breakpoint (non-suspending — uncheck "Suspend")             │
│     Logs a message without stopping execution                           │
│     ┌──────────────────────────────────────────────────────┐            │
│     │ "Evaluate and log" expression:                       │            │
│     │   "User: " + user.name + ", items: " + list.size    │            │
│     │   "Thread: " + Thread.currentThread().name           │            │
│     │   "State: " + _uiState.value                         │            │
│     │                                                       │            │
│     │ Output appears in Debugger Console (not Logcat)      │            │
│     │ Great for adding traces without modifying code!       │            │
│     └──────────────────────────────────────────────────────┘            │
│                                                                          │
│  4. Exception Breakpoint (Run → View Breakpoints → + Exception)         │
│     Pauses when a specific exception is thrown                          │
│     ┌──────────────────────────────────────────────────────┐            │
│     │ • Break on NullPointerException                       │            │
│     │ • Break on IllegalStateException                      │            │
│     │ • Break on all uncaught exceptions                   │            │
│     │ • Break on caught + uncaught exceptions              │            │
│     │ • Class filter: com.example.app.*                    │            │
│     └──────────────────────────────────────────────────────┘            │
│                                                                          │
│  5. Method Breakpoint (set on method signature — diamond ◆)            │
│     Pauses on method entry AND/OR exit                                  │
│     Shows return value on exit                                          │
│     ⚠ Slower than line breakpoints (uses JVM agent)                    │
│                                                                          │
│  6. Field Watchpoint (set on field declaration — eye)                   │
│     Pauses when a field is read AND/OR modified                         │
│     Essential for tracking unexpected state changes                     │
│     ┌──────────────────────────────────────────────────────┐            │
│     │ Example: Set on `private var isLoggedIn: Boolean`    │            │
│     │ → Breaks whenever any code reads or modifies it     │            │
│     │ → Shows the old value and new value                  │            │
│     └──────────────────────────────────────────────────────┘            │
│                                                                          │
│  7. Kotlin Coroutine Breakpoint                                         │
│     In the Variables panel, coroutine debugger shows:                   │
│     • Coroutine name, state (RUNNING, SUSPENDED, etc.)                 │
│     • Full call stack across suspension points                          │
│     • Which dispatcher it's running on                                  │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Debugger Windows:                                                      │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │  Variables         │  Call Stack        │  Watches             │     │
│  │  ┌──────────────┐  │  ┌──────────────┐  │  ┌──────────────┐   │     │
│  │  │ user = User( │  │  │ onClick()    │  │  │ user.name    │   │     │
│  │  │  name="Alice"│  │  │ doLogin()    │  │  │  = "Alice"   │   │     │
│  │  │  age=25      │  │  │ validate()   │  │  │ list.size    │   │     │
│  │  │  email=null  │  │  │ Repository   │  │  │  = 42        │   │     │
│  │  │ )            │  │  │  .fetch()    │  │  │ isValid      │   │     │
│  │  │ list = [3 el]│  │  │ CoroutineSc. │  │  │  = false     │   │     │
│  │  │  [0] = "A"   │  │  │ main()       │  │  └──────────────┘   │     │
│  │  │  [1] = "B"   │  │  └──────────────┘  │                     │     │
│  │  └──────────────┘  │                     │  + Add watch expr.  │     │
│  └────────────────────────────────────────────────────────────────┘     │
│                                                                          │
│  Key Debugger Actions:                                                  │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │  F8            → Step Over (next line, skip method internals) │     │
│  │  F7            → Step Into (enter the called method)          │     │
│  │  Alt+Shift+F7  → Force Step Into (even library code)        │     │
│  │  Shift+F8      → Step Out (finish current method, return)    │     │
│  │  F9            → Resume (run to next breakpoint)              │     │
│  │  Alt+F8        → Evaluate Expression (run code mid-debug)   │     │
│  │  Alt+F9        → Run to Cursor (temporary breakpoint)        │     │
│  │  Ctrl+F8       → Toggle Breakpoint                           │     │
│  │  Ctrl+Shift+F8 → View All Breakpoints (manage, enable/disable)│    │
│  │  Ctrl+F5       → Rerun                                       │     │
│  │                                                                │     │
│  │  Right-click variable:                                        │     │
│  │  → Set Value (modify variable at runtime)                    │     │
│  │  → Mark Object (label for tracking across frames)            │     │
│  │  → Jump to Source                                             │     │
│  │  → Add to Watches                                            │     │
│  └────────────────────────────────────────────────────────────────┘     │
│                                                                          │
│  Evaluate Expression (Alt+F8) — run arbitrary code:                    │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │  user.copy(name = "Test")                     [Evaluate]      │     │
│  │  repository.getCount()                                        │     │
│  │  listOf(1,2,3).filter { it > 1 }.sum()                      │     │
│  │  _uiState.value = UiState(loading = true)    // Modify state!│     │
│  │  Result: 5                                                    │     │
│  └────────────────────────────────────────────────────────────────┘     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

# 16.5 Design Tools

## Figma Integration

### Theory

**Figma** is the industry-standard design tool for Android apps. Developers use Figma files to extract design specs, assets, and translate designs to code. Understanding the Figma → Android pipeline is essential for developer-designer collaboration.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Figma → Android Pipeline                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────────┐     │
│  │ Designer │───>│  Figma   │───>│ Dev Mode │───>│  Developer   │     │
│  │ creates  │    │  File    │    │ inspect  │    │  implements  │     │
│  │ mockups  │    │ (shared) │    │  specs   │    │  in Compose  │     │
│  └──────────┘    └──────────┘    └──────────┘    └──────────────┘     │
│                                                                          │
│  What developers extract from Figma:                                    │
│  ┌───────────────────────────────────────────────────────────────┐      │
│  │                                                               │      │
│  │  Colors         → colorScheme / Design Tokens                │      │
│  │  #6750A4        → primary = Color(0xFF6750A4)                │      │
│  │                                                               │      │
│  │  Typography     → Typography object / TextStyle               │      │
│  │  Roboto 16sp    → bodyLarge = TextStyle(...)                  │      │
│  │                                                               │      │
│  │  Spacing        → padding / margin values                    │      │
│  │  16px           → 16.dp                                      │      │
│  │  8px grid       → consistent spacing system                  │      │
│  │                                                               │      │
│  │  Corner Radius  → RoundedCornerShape                         │      │
│  │  12px           → RoundedCornerShape(12.dp)                  │      │
│  │                                                               │      │
│  │  Elevation      → tonalElevation / shadowElevation           │      │
│  │  2dp            → Card(elevation = CardDefaults.cardElevation │      │
│  │                      (defaultElevation = 2.dp))              │      │
│  │                                                               │      │
│  │  Icons/Images   → Export as SVG → Vector Asset Studio        │      │
│  │                 → Export as WebP (lossy or lossless)          │      │
│  │                                                               │      │
│  │  Components     → Composable functions                       │      │
│  │  Auto layout    → Row/Column with Arrangement.spacedBy()    │      │
│  │  Constraints    → Modifier.fillMaxWidth(), weight()          │      │
│  │  Frame          → Box / Surface with specific size           │      │
│  │                                                               │      │
│  └───────────────────────────────────────────────────────────────┘      │
│                                                                          │
│  Integration Tools:                                                     │
│  ┌───────────────────────────────────────────────────────────────┐      │
│  │ Tool                │ What it does                            │      │
│  │─────────────────────┼────────────────────────────────────────│      │
│  │ Figma Dev Mode      │ Inspect spacing, colors, CSS/Android  │      │
│  │                     │ values, redlines, copy code snippets   │      │
│  │                     │                                        │      │
│  │ Relay for Figma     │ Google's official Figma-to-Compose    │      │
│  │ (Google)            │ pipeline. Annotate in Figma →          │      │
│  │                     │ generates Compose code in AS           │      │
│  │                     │                                        │      │
│  │ Figma REST API      │ Programmatic access to design files   │      │
│  │                     │ Automate token extraction              │      │
│  │                     │                                        │      │
│  │ Figma Tokens plugin │ Export design tokens as JSON for      │      │
│  │                     │ automated theme generation             │      │
│  └───────────────────────────────────────────────────────────────┘      │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Design Tokens — Bridging Design ↔ Code

```kotlin
// Design Tokens: Single source of truth for design values
// Extracted from Figma, used consistently in code

object DesignTokens {
    // ═══ Spacing System (8dp grid) ═══
    object Spacing {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 16.dp
        val lg = 24.dp
        val xl = 32.dp
        val xxl = 48.dp
    }

    // ═══ Corner Radius ═══
    object Radius {
        val sm = 4.dp
        val md = 8.dp
        val lg = 12.dp
        val xl = 16.dp
        val full = 100.dp  // pill shape
    }

    // ═══ Elevation ═══
    object Elevation {
        val none = 0.dp
        val sm = 1.dp
        val md = 3.dp
        val lg = 6.dp
        val xl = 8.dp
    }
}

// Usage in composables:
@Composable
fun UserCard(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.md),
        shape = RoundedCornerShape(DesignTokens.Radius.lg),
        elevation = CardDefaults.cardElevation(
            defaultElevation = DesignTokens.Elevation.md
        )
    ) {
        Column(modifier = Modifier.padding(DesignTokens.Spacing.md)) {
            Text(user.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(DesignTokens.Spacing.sm))
            Text(user.email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
```

### Relay — Figma to Compose Pipeline

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Relay Workflow                                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Step 1: Designer annotates in Figma                                    │
│  ┌─────────────────────────────────────────────┐                        │
│  │ Figma Component: "UserCard"                  │                        │
│  │ ┌─────────────────────────────────────┐     │                        │
│  │ │ [Avatar]  [Name]  [Email]           │     │                        │
│  │ │                                     │     │                        │
│  │ │ Relay annotations:                  │     │                        │
│  │ │  • name → Text param (String)       │     │                        │
│  │ │  • email → Text param (String)      │     │                        │
│  │ │  • avatar → Image param (Painter)   │     │                        │
│  │ │  • onTap → Interaction (callback)   │     │                        │
│  │ └─────────────────────────────────────┘     │                        │
│  └─────────────────────────────────────────────┘                        │
│                       │                                                  │
│                       ▼                                                  │
│  Step 2: Import in Android Studio (Relay plugin)                        │
│  ┌─────────────────────────────────────────────┐                        │
│  │ File → New → Import UI Packages             │                        │
│  │ Select Figma file → Import components       │                        │
│  └─────────────────────────────────────────────┘                        │
│                       │                                                  │
│                       ▼                                                  │
│  Step 3: Auto-generated Compose code                                    │
│  ┌─────────────────────────────────────────────┐                        │
│  │ @Composable                                  │                        │
│  │ fun UserCard(                                │                        │
│  │     name: String,                            │                        │
│  │     email: String,                           │                        │
│  │     avatar: Painter,                         │                        │
│  │     onTap: () -> Unit                        │                        │
│  │ ) { /* generated layout code */ }            │                        │
│  └─────────────────────────────────────────────┘                        │
│                                                                          │
│  Benefits:                                                              │
│  • Design changes automatically reflected in code                      │
│  • Pixel-perfect implementation                                        │
│  • Reduces designer-developer handoff friction                         │
│  • Supports theming and dynamic content                                │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Material Theme Builder

### Theory

The **Material Theme Builder** generates a complete Material 3 color scheme from a single seed color using the HCT (Hue, Chroma, Tone) color space. It creates 29 color roles for both Light and Dark schemes, ensuring proper contrast ratios for accessibility.

```
┌─────────────────────────────────────────────────────────────────────────┐
│           Material Theme Builder — Color System                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Input: Seed Color #6750A4                                              │
│           │                                                              │
│           ▼                                                              │
│  ┌─────────────────────────────────────────────────────────┐            │
│  │  HCT Algorithm (Hue, Chroma, Tone)                      │            │
│  │  Generates tonal palettes from T0 (black) to T100 (white)│            │
│  │                                                           │            │
│  │  Tonal Palette:                                           │            │
│  │  T0  T10  T20  T30  T40  T50  T60  T70  T80  T90  T100  │            │
│  │  ██   ██   ██   ██   ██   ██   ██   ██   ██   ██   ██   │            │
│  │  ← darker ─────────────────────────────── lighter →      │            │
│  └─────────────────────────────────────────────────────────┘            │
│           │                                                              │
│           ▼                                                              │
│  ┌─────────────────────────────────────────────────────────┐            │
│  │  Light Scheme        │  Dark Scheme                      │            │
│  │  ─────────────        │  ───────────                      │            │
│  │  primary: T40        │  primary: T80                     │            │
│  │  onPrimary: T100     │  onPrimary: T20                   │            │
│  │  primaryContainer:   │  primaryContainer:                │            │
│  │    T90               │    T30                             │            │
│  │  onPrimaryContainer: │  onPrimaryContainer:              │            │
│  │    T10               │    T90                             │            │
│  │  surface: T99        │  surface: T10                     │            │
│  │  onSurface: T10      │  onSurface: T90                   │            │
│  │  surfaceVariant: T90 │  surfaceVariant: T30              │            │
│  │  outline: T50        │  outline: T60                     │            │
│  │  error: T40          │  error: T80                       │            │
│  └─────────────────────────────────────────────────────────┘            │
│                                                                          │
│  Color Role Purpose:                                                    │
│  ┌────────────────────────┬────────────────────────────────┐            │
│  │ Role                   │ Used for                       │            │
│  │────────────────────────┼────────────────────────────────│            │
│  │ primary                │ Key components (FAB, buttons)  │            │
│  │ onPrimary              │ Text/icons on primary          │            │
│  │ primaryContainer       │ Less prominent components      │            │
│  │ secondary              │ Filters, chips, toggles        │            │
│  │ tertiary               │ Accent, complementary          │            │
│  │ surface                │ Background of sheets, dialogs  │            │
│  │ surfaceVariant         │ Cards, navigation rail         │            │
│  │ error                  │ Error states                   │            │
│  │ outline                │ Borders, dividers              │            │
│  │ inverseSurface         │ Snackbars                      │            │
│  └────────────────────────┴────────────────────────────────┘            │
│                                                                          │
│  Dynamic Color (Android 12+):                                           │
│  User's wallpaper → Extract seed → Generate custom theme               │
│  val colorScheme = if (Build.VERSION.SDK_INT >= 31)                    │
│      dynamicDarkColorScheme(context) else darkColorScheme()            │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// ═══ Complete Material 3 Theme setup ═══
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // Android 12+ wallpaper colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF4F378B),
            secondary = Color(0xFFCCC2DC),
            tertiary = Color(0xFFEFB8C8),
            background = Color(0xFF1C1B1F),
            surface = Color(0xFF1C1B1F),
            error = Color(0xFFF2B8B5),
        )
        else -> lightColorScheme(
            primary = Color(0xFF6750A4),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEADDFF),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260),
            background = Color(0xFFFFFBFE),
            surface = Color(0xFFFFFBFE),
            error = Color(0xFFB3261E),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,    // From Material Theme Builder export
        content = content
    )
}
```

---

## Lottie Animations

### Theory

**Lottie** renders After Effects animations in real-time on Android. Designers create animations in After Effects, export as JSON via the Bodymovin plugin, and developers play them with minimal code. Far more efficient than GIF/video alternatives.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Lottie Architecture                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Designer                          Developer                            │
│  ┌────────────────────┐           ┌────────────────────────┐           │
│  │ After Effects      │           │ Android App             │           │
│  │ ┌───────────────┐  │  Export   │ ┌────────────────────┐ │           │
│  │ │ Animation     │──┼──────────>│ │ animation.json     │ │           │
│  │ │ (keyframes,   │  │  JSON     │ │ (res/raw/ or URL)  │ │           │
│  │ │  shapes,      │  │  via      │ └────────┬───────────┘ │           │
│  │ │  masks)       │  │ Bodymovin │          │              │           │
│  │ └───────────────┘  │           │          ▼              │           │
│  └────────────────────┘           │ ┌────────────────────┐ │           │
│                                    │ │ Lottie Library     │ │           │
│  Alternative sources:              │ │ • Parse JSON       │ │           │
│  • LottieFiles.com                │ │ • Render shapes    │ │           │
│  • IconScout                      │ │ • Animate frames   │ │           │
│  • Custom designs                 │ │ • Hardware accel.  │ │           │
│                                    │ └────────────────────┘ │           │
│                                    └────────────────────────┘           │
│                                                                          │
│  Lottie vs Alternatives:                                                │
│  ┌────────────┬──────────┬──────────┬──────────┬──────────┐            │
│  │ Format     │ File Size│ Quality  │ Interact │ Perform. │            │
│  │────────────┼──────────┼──────────┼──────────┼──────────│            │
│  │ Lottie JSON│ Tiny     │ Vector   │ Yes      │ Great    │            │
│  │ GIF        │ Large    │ Pixel    │ No       │ Poor     │            │
│  │ Video (MP4)│ Large    │ Good     │ Limited  │ Medium   │            │
│  │ Animated   │ Medium   │ Pixel    │ No       │ Medium   │            │
│  │ WebP/PNG   │          │          │          │          │            │
│  │ AGSL Shader│ None     │ Vector   │ Yes      │ Great    │            │
│  │ (code-only)│          │          │          │          │            │
│  └────────────┴──────────┴──────────┴──────────┴──────────┘            │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.airbnb.android:lottie-compose:6.4.0")
}

// ═══ Basic Lottie Animation in Compose ═══
@Composable
fun LoadingAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading_animation)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever  // Loop forever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(200.dp)
    )
}

// ═══ Controlled Animation (play/pause/progress) ═══
@Composable
fun ControlledAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.success_animation)
    )
    var isPlaying by remember { mutableStateOf(false) }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = true,
        iterations = 1
    )

    // Play animation once when triggered
    LaunchedEffect(Unit) {
        delay(500)
        isPlaying = true
    }

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(150.dp)
    )
}

// ═══ Load from URL (for dynamic animations) ═══
@Composable
fun RemoteAnimation(url: String) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url(url)
    )
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.size(100.dp)
    )
}

// ═══ Common use cases ═══
// • Empty states:        No data → show friendly animation
// • Loading indicators:  Replace circular progress with branded animation
// • Success feedback:    Checkmark animation after form submit
// • Onboarding:          Animated illustrations for intro screens
// • Pull-to-refresh:     Custom refresh indicator
// • Like/favorite:       Heart animation (like Twitter)
```

---

## Screenshot Testing (Paparazzi)

### Theory

**Paparazzi** (by Cash App) renders Compose/View screenshots on JVM without a device or emulator, enabling fast visual regression testing in CI. It catches unintended UI changes by comparing screenshots against a golden (baseline) image.

```
┌─────────────────────────────────────────────────────────────────────────┐
│              Screenshot Testing with Paparazzi                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Workflow:                                                              │
│  ┌──────────┐    ┌──────────────┐    ┌──────────────────┐              │
│  │ Record   │───>│ Golden images│───>│ Compare on CI    │              │
│  │ baseline │    │ (committed)  │    │ ./gradlew verify │              │
│  └──────────┘    └──────────────┘    └────────┬─────────┘              │
│                                                │                        │
│                                      ┌────────┴─────────┐              │
│                                      │   Match?          │              │
│                                      ├───────┬───────────┤              │
│                                      │  YES  │    NO     │              │
│                                      │  ✓    │ ✗ FAIL   │              │
│                                      │ Pass  │ Show diff │              │
│                                      └───────┴───────────┘              │
│                                                                          │
│  Diff output (pixel-by-pixel comparison):                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                              │
│  │ Expected │  │  Actual  │  │   Diff   │                              │
│  │ (golden) │  │ (current)│  │ (red =   │                              │
│  │          │  │          │  │  changed) │                              │
│  └──────────┘  └──────────┘  └──────────┘                              │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```kotlin
// build.gradle.kts (app)
plugins {
    id("app.cash.paparazzi") version "1.3.4"
}

// Test class
class UserCardScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material3.Light.NoActionBar"
    )

    @Test
    fun userCard_default() {
        paparazzi.snapshot {
            AppTheme {
                UserCard(
                    user = User(name = "Alice", email = "alice@mail.com")
                )
            }
        }
    }

    @Test
    fun userCard_longName() {
        paparazzi.snapshot {
            AppTheme {
                UserCard(
                    user = User(
                        name = "Very Long Username That Might Overflow",
                        email = "long@mail.com"
                    )
                )
            }
        }
    }

    @Test
    fun userCard_darkTheme() {
        paparazzi.snapshot {
            AppTheme(darkTheme = true) {
                UserCard(
                    user = User(name = "Alice", email = "alice@mail.com")
                )
            }
        }
    }
}

// Commands:
// ./gradlew :app:recordPaparazziDebug     — Record golden screenshots
// ./gradlew :app:verifyPaparazziDebug     — Verify against golden
// Golden images saved in: src/test/snapshots/
```

---

## Icon Design with Asset Studio

### Theory

**Asset Studio** in Android Studio generates app icons, launcher icons, action bar icons, and notification icons in all required sizes and densities.

```
┌─────────────────────────────────────────────────────────────────┐
│              Asset Studio Tools                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Access: Right-click res → New → Image Asset / Vector Asset    │
│                                                                  │
│  1. Image Asset (Launcher Icons):                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Source:    [Image] [Clip Art] [Text]                     │  │
│  │  Shape:     [Circle] [Square] [Rounded Square]            │  │
│  │  Layers:    Foreground + Background (Adaptive Icons)      │  │
│  │                                                           │  │
│  │  Generates:                                               │  │
│  │  ├── mipmap-mdpi/        (48×48)                         │  │
│  │  ├── mipmap-hdpi/        (72×72)                         │  │
│  │  ├── mipmap-xhdpi/       (96×96)                         │  │
│  │  ├── mipmap-xxhdpi/      (144×144)                       │  │
│  │  ├── mipmap-xxxhdpi/     (192×192)                       │  │
│  │  └── mipmap-anydpi-v26/  (Adaptive icon XML)            │  │
│  │      ├── ic_launcher.xml                                 │  │
│  │      └── ic_launcher_round.xml                           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  2. Vector Asset:                                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Source:    [Material Icon Library] [Local SVG/PSD]       │  │
│  │                                                           │  │
│  │  • Search 2500+ Material Design icons                    │  │
│  │  • Import SVG/PSD and convert to VectorDrawable          │  │
│  │  • Single XML file → scales to any density               │  │
│  │  • Smaller APK than multiple PNG densities               │  │
│  │                                                           │  │
│  │  Generates:                                               │  │
│  │  └── drawable/                                           │  │
│  │      └── ic_icon_name.xml  (VectorDrawable)              │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  3. Notification Icons:                                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  • Must be white + transparent (monochrome)              │  │
│  │  • System tints with app color                           │  │
│  │  • Keep it simple — visible at 24×24dp                   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Adaptive Icons (API 26+):                                      │
│  ┌────────────────────────────┐                                │
│  │  ┌──────────────────────┐  │                                │
│  │  │     Background       │  │  Background layer             │
│  │  │  ┌──────────────┐    │  │  (108×108dp, color/image)     │
│  │  │  │  Foreground  │    │  │                                │
│  │  │  │    Icon      │    │  │  Foreground layer             │
│  │  │  │              │    │  │  (108×108dp, 72dp safe zone)  │
│  │  │  └──────────────┘    │  │                                │
│  │  └──────────────────────┘  │  System applies mask:         │
│  │         Masked to:         │  Circle, Squircle, Square...  │
│  │        ┌────────┐          │                                │
│  │        │(      )│          │                                │
│  │        │( Icon )│          │                                │
│  │        │(      )│          │                                │
│  │        └────────┘          │                                │
│  └────────────────────────────┘                                │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Adaptive Icon Example

```xml
<!-- res/mipmap-anydpi-v26/ic_launcher.xml -->
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
    <!-- monochrome: API 33+ themed icon support -->
</adaptive-icon>
```

```xml
<!-- res/drawable/ic_launcher_background.xml -->
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#6750A4"
        android:pathData="M0,0h108v108h-108z" />
</vector>
```

---

# Summary

| Tool Category | Key Tools | Purpose |
|---------------|-----------|---------|
| **IDE** | Android Studio, Layout Editor, Compose Preview, Live Edit | Write, design, and preview code |
| **Build** | Gradle, Version Catalog, Build Variants, Product Flavors | Configure, build, and sign apps |
| **Profilers** | CPU, Memory, Network, Energy Profiler, Compose Tracing | Find performance bottlenecks |
| **Logging** | Logcat, Timber, CrashReportingTree | Runtime debugging output |
| **Emulator** | AVD Manager, Emulator, Device Mirroring | Test without physical device |
| **Version Control** | Git, GitHub Flow, Conventional Commits, Lefthook | Track and collaborate on code |
| **Static Analysis** | Lint (custom rules), Detekt, ktlint, SonarQube | Catch issues before runtime |
| **Code Coverage** | JaCoCo, test verification tasks | Measure test completeness |
| **CI/CD** | GitHub Actions (lint, test, build, instrumented tests) | Automate quality gates |
| **Leak Detection** | LeakCanary, StrictMode | Find memory leaks and violations |
| **Network Debug** | Chucker, Network Inspector | Inspect HTTP traffic |
| **Crash Reporting** | Firebase Crashlytics, Timber integration | Monitor production crashes |
| **Inspection** | Layout Inspector, DB Inspector, Background Task Inspector | Diagnose runtime UI/data/work issues |
| **Debugging** | ADB (80+ commands), Breakpoints (7 types), Evaluate Expression | Deep runtime debugging |
| **Design** | Figma (Relay, Dev Mode), Material Theme Builder, Design Tokens | Design system and handoff |
| **Animation** | Lottie (Compose), After Effects → JSON pipeline | Rich animations with tiny files |
| **Visual Testing** | Paparazzi screenshot tests | Catch UI regressions |
| **Assets** | Asset Studio (Image, Vector), Adaptive Icons | Icon and asset generation |

---

# Tool Selection Quick Reference

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Problem                         →  Tool to Use                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ═══ PERFORMANCE ═══                                                    │
│  App is slow/janky               →  CPU Profiler + System Trace         │
│  App uses too much memory        →  Memory Profiler + Heap Dump        │
│  Network calls are slow          →  Network Inspector + Chucker        │
│  Battery drain                   →  Energy Profiler                     │
│  Unnecessary recompositions      →  Layout Inspector (Compose mode)    │
│  Compose stability issues        →  Compose Compiler Metrics           │
│                                                                          │
│  ═══ DEBUGGING ═══                                                      │
│  UI doesn't look right           →  Layout Inspector (3D mode)         │
│  Database issues                 →  Database Inspector + query console │
│  HTTP request/response problems  →  Chucker (in-app) or Network Insp. │
│  WorkManager not running         →  Background Task Inspector          │
│  Memory leak                     →  LeakCanary (auto-detected!)        │
│  Main thread violations          →  StrictMode                          │
│  Need to check device files      →  Device File Explorer               │
│  Shell access to device          →  ADB shell                          │
│  Install/manage APK              →  ADB install/uninstall              │
│  Production crashes              →  Firebase Crashlytics               │
│                                                                          │
│  ═══ CODE QUALITY ═══                                                   │
│  Code style inconsistent         →  ktlint + Detekt                    │
│  Potential bugs/anti-patterns    →  Android Lint + Detekt custom rules │
│  Security vulnerabilities        →  Lint + SonarQube                   │
│  Test coverage too low           →  JaCoCo coverage reports            │
│  Visual regression               →  Paparazzi screenshot tests         │
│                                                                          │
│  ═══ DESIGN & ASSETS ═══                                                │
│  Need app icon                   →  Asset Studio (Image Asset)         │
│  Need vector icon                →  Asset Studio (Vector Asset)        │
│  Need color theme                →  Material Theme Builder             │
│  Design specs from Figma         →  Figma Dev Mode                     │
│  Auto-generate Compose from      →  Relay for Figma                    │
│    Figma designs                                                        │
│  Need rich animations            →  Lottie (LottieFiles.com)          │
│  Test on many devices            →  AVD Manager + Firebase Test Lab   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```
