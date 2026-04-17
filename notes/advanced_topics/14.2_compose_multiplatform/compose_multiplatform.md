# Compose Multiplatform

## Overview

Compose Multiplatform (by JetBrains) extends Jetpack Compose beyond Android to **Desktop (Windows, macOS, Linux)**, **iOS**, and **Web** — sharing not just business logic but also **UI code** across platforms.

```
┌─────────────────────────────────────────────────────────────────┐
│                 Compose Multiplatform Architecture               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│              ┌──────────────────────────┐                        │
│              │     Shared UI Layer      │                        │
│              │  (Compose @Composable)   │                        │
│              │  ┌────────────────────┐  │                        │
│              │  │  Screens           │  │                        │
│              │  │  Components        │  │                        │
│              │  │  Themes            │  │                        │
│              │  │  Navigation        │  │                        │
│              │  └────────────────────┘  │                        │
│              └────────────┬─────────────┘                        │
│                           │                                      │
│              ┌────────────▼─────────────┐                        │
│              │   Shared Business Logic  │                        │
│              │   (ViewModels, UseCases) │                        │
│              └────────────┬─────────────┘                        │
│                           │                                      │
│    ┌──────────┬───────────┼──────────┬──────────┐               │
│    ▼          ▼           ▼          ▼          ▼               │
│ ┌──────┐ ┌───────┐ ┌─────────┐ ┌───────┐ ┌───────┐            │
│ │Android│ │  iOS  │ │ Desktop │ │  Web  │ │ Web   │            │
│ │(Phone)│ │(Phone)│ │(JVM)    │ │(Wasm) │ │(JS)   │            │
│ │      │ │       │ │Win/Mac/ │ │       │ │       │            │
│ │      │ │       │ │Linux    │ │       │ │       │            │
│ └──────┘ └───────┘ └─────────┘ └───────┘ └───────┘            │
└─────────────────────────────────────────────────────────────────┘
```

---

## Project Setup

### Root `build.gradle.kts`

```kotlin
plugins {
    kotlin("multiplatform") version "1.9.22" apply false
    id("org.jetbrains.compose") version "1.6.0" apply false
    id("com.android.application") version "8.2.0" apply false
}
```

### Shared Module (`composeApp/build.gradle.kts`)

```kotlin
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.application")
}

kotlin {
    // Android
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "17" }
        }
    }

    // Desktop (JVM)
    jvm("desktop")

    // iOS
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // Web (Wasm)
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)

                // Navigation
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha03")

                // ViewModel
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

                // Image loading
                implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha01")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.8.2")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
        }

        val wasmJsMain by getting
    }
}
```

---

## Sharing UI Code Across Platforms

### Common Composables (`commonMain`)

```kotlin
// composeApp/src/commonMain/kotlin/com/example/app/App.kt

@Composable
fun App() {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography
    ) {
        val navController = rememberNavController()

        NavHost(navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    onNavigateToDetail = { id ->
                        navController.navigate("detail/$id")
                    }
                )
            }
            composable("detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                DetailScreen(
                    itemId = id,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
```

### Shared Screen Example

```kotlin
// composeApp/src/commonMain/kotlin/com/example/app/ui/HomeScreen.kt

@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = viewModel { HomeViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Multiplatform App") }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items) { item ->
                        ItemCard(
                            item = item,
                            onClick = { onNavigateToDetail(item.id) }
                        )
                    }
                }
            }
            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
            }
        }
    }
}

@Composable
fun ItemCard(item: Item, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(item.subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
```

### Shared ViewModel

```kotlin
// composeApp/src/commonMain/kotlin/com/example/app/viewmodel/HomeViewModel.kt

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Item>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Item>>> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val items = repository.getItems()
                _uiState.value = UiState.Success(items)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun retry() = loadItems()
}

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

---

## Platform Entry Points

### Android Entry Point

```kotlin
// composeApp/src/androidMain/kotlin/com/example/app/MainActivity.kt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App() // Calls the shared composable
        }
    }
}
```

### Desktop Entry Point (JVM)

```kotlin
// composeApp/src/desktopMain/kotlin/com/example/app/main.kt

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "My Multiplatform App",
        state = rememberWindowState(
            width = 1024.dp,
            height = 768.dp
        )
    ) {
        App() // Same shared composable!
    }
}
```

### iOS Entry Point

```kotlin
// composeApp/src/iosMain/kotlin/com/example/app/MainViewController.kt

fun MainViewController() = ComposeUIViewController {
    App() // Same shared composable!
}
```

```swift
// iosApp/iosApp/ContentView.swift
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}
```

### Web (Wasm) Entry Point

```kotlin
// composeApp/src/wasmJsMain/kotlin/com/example/app/main.kt

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App() // Same shared composable!
    }
}
```

```html
<!-- index.html -->
<!DOCTYPE html>
<html>
<head><title>Compose Multiplatform Web</title></head>
<body>
<canvas id="ComposeTarget"></canvas>
<script src="composeApp.js"></script>
</body>
</html>
```

---

## Desktop-Specific Features

```kotlin
// Desktop window management, menus, system tray

@Composable
fun DesktopApp() {
    var isOpen by remember { mutableStateOf(true) }

    if (isOpen) {
        Window(
            onCloseRequest = { isOpen = false },
            title = "Desktop App",
            state = rememberWindowState(
                placement = WindowPlacement.Floating,
                position = WindowPosition(300.dp, 200.dp),
                size = DpSize(1200.dp, 800.dp)
            )
        ) {
            // Menu bar (Desktop only)
            MenuBar {
                Menu("File") {
                    Item("New", onClick = { /* ... */ },
                        shortcut = KeyShortcut(Key.N, ctrl = true))
                    Item("Open", onClick = { /* ... */ },
                        shortcut = KeyShortcut(Key.O, ctrl = true))
                    Separator()
                    Item("Exit", onClick = { isOpen = false })
                }
                Menu("Edit") {
                    Item("Undo", onClick = { /* ... */ },
                        shortcut = KeyShortcut(Key.Z, ctrl = true))
                    Item("Redo", onClick = { /* ... */ },
                        shortcut = KeyShortcut(Key.Z, ctrl = true, shift = true))
                }
            }

            App() // Shared UI
        }
    }
}

// System Tray (Desktop only)
fun main() = application {
    val icon = painterResource("icon.png")

    Tray(
        icon = icon,
        menu = {
            Item("Show", onClick = { /* show window */ })
            Item("Exit", onClick = ::exitApplication)
        }
    )

    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
```

---

## Handling Platform Differences in UI

```kotlin
// composeApp/src/commonMain/kotlin/com/example/app/platform/Platform.kt

// Common expect declarations for platform-aware UI
expect val isDesktop: Boolean
expect val isMobile: Boolean

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)

// Adaptive layout based on platform
@Composable
fun AdaptiveLayout(
    listContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit
) {
    if (isDesktop) {
        // Side-by-side on desktop
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(0.35f)) { listContent() }
            VerticalDivider()
            Box(Modifier.weight(0.65f)) { detailContent() }
        }
    } else {
        // Stacked on mobile
        listContent()
    }
}
```

```kotlin
// androidMain
actual val isDesktop: Boolean = false
actual val isMobile: Boolean = true

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled, onBack)
}
```

```kotlin
// desktopMain
actual val isDesktop: Boolean = true
actual val isMobile: Boolean = false

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop: handle Escape key
    LaunchedEffect(enabled) {
        // Custom key handler
    }
}
```

---

## Platform Comparison

```
┌──────────────┬──────────┬──────────┬──────────┬──────────┐
│   Feature    │ Android  │   iOS    │ Desktop  │   Web    │
├──────────────┼──────────┼──────────┼──────────┼──────────┤
│ UI Rendering │ Skia +   │  Skia +  │  Skia +  │ Canvas   │
│              │ Android  │  Metal   │  OpenGL  │ (Wasm)   │
│              │ Canvas   │          │  /Direct │          │
├──────────────┼──────────┼──────────┼──────────┼──────────┤
│ Stability    │  Stable  │  Beta    │  Stable  │  Alpha   │
├──────────────┼──────────┼──────────┼──────────┼──────────┤
│ Navigation   │    ✅    │    ✅    │    ✅    │    ✅    │
├──────────────┼──────────┼──────────┼──────────┼──────────┤
│ Material 3   │    ✅    │    ✅    │    ✅    │    ✅    │
├──────────────┼──────────┼──────────┼──────────┼──────────┤
│ Resources    │    ✅    │    ✅    │    ✅    │    ✅    │
├──────────────┼──────────┼──────────┼──────────┼──────────┤
│ Window Mgmt  │    —     │    —     │    ✅    │    —     │
├──────────────┼──────────┼──────────┼──────────┼──────────┤
│ System Tray  │    —     │    —     │    ✅    │    —     │
├──────────────┼──────────┼──────────┼──────────┼──────────┤
│ File Dialogs │    —     │    —     │    ✅    │    —     │
└──────────────┴──────────┴──────────┴──────────┴──────────┘
```
