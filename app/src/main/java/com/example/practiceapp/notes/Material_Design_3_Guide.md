# 15.2 Material Design 3 (Material You)

## Overview

Material Design 3 (also called **Material You**) is Google's latest design system that emphasizes **personalization**, **adaptability**, and **accessibility**. It introduces dynamic color theming based on the user's wallpaper, updated components, adaptive layouts, and expressive motion.

```
┌─────────────────────────────────────────────────────────────────┐
│                Material Design 3 Pillars                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐       │
│  │   Dynamic     │  │   Adaptive    │  │   Updated     │       │
│  │   Theming     │  │   Layouts     │  │   Components  │       │
│  │               │  │               │  │               │       │
│  │  • Wallpaper  │  │  • Window     │  │  • New FAB    │       │
│  │    colors     │  │    sizes      │  │  • Cards      │       │
│  │  • Color      │  │  • Responsive │  │  • TopAppBar  │       │
│  │    schemes    │  │  • Panes      │  │  • Navigation │       │
│  │  • Tokens     │  │  • Grids      │  │  • Dialogs    │       │
│  └───────────────┘  └───────────────┘  └───────────────┘       │
│                                                                  │
│  ┌───────────────────────────────────────────────────┐          │
│  │              Motion & Transitions                  │          │
│  │                                                    │          │
│  │  • Shared element transitions                     │          │
│  │  • Container transforms                           │          │
│  │  • Predictive back animations                     │          │
│  │  • Emphasis and hierarchy through motion          │          │
│  └───────────────────────────────────────────────────┘          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1. Dynamic Theming

### Theory

Dynamic theming extracts **colors from the user's wallpaper** and generates a complete color scheme that is applied across the entire app. This makes every app feel personal and unique to each user.

**Key Concepts:**
- **Color Extraction**: System extracts seed colors from the wallpaper
- **Tonal Palettes**: 5 tonal palettes generated (Primary, Secondary, Tertiary, Neutral, Neutral Variant)
- **Color Roles**: 29 color roles mapped from tonal palettes (primary, onPrimary, primaryContainer, etc.)
- **Light/Dark**: Automatic light and dark theme variants
- **Fallback**: Custom color scheme for devices that don't support dynamic color (pre-Android 12)

```
┌─────────────────────────────────────────────────────────────────┐
│                  Dynamic Color Pipeline                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐                                               │
│  │  User's      │                                               │
│  │  Wallpaper   │                                               │
│  └──────┬───────┘                                               │
│         │  Color extraction                                      │
│         ▼                                                        │
│  ┌──────────────┐     ┌────────────────────────────────────┐    │
│  │  Seed Color  │────>│  Tonal Palettes (5)                │    │
│  │  (Source)    │     │  ┌────────────────────────────────┐ │    │
│  └──────────────┘     │  │ Primary     [0..100] tones    │ │    │
│                        │  │ Secondary   [0..100] tones    │ │    │
│                        │  │ Tertiary    [0..100] tones    │ │    │
│                        │  │ Neutral     [0..100] tones    │ │    │
│                        │  │ Neutral Var [0..100] tones    │ │    │
│                        │  └────────────────────────────────┘ │    │
│                        └──────────────┬─────────────────────┘    │
│                                       │                          │
│              ┌────────────────────────┴──────────────────┐      │
│              │                                           │      │
│              ▼                                           ▼      │
│  ┌───────────────────┐              ┌───────────────────┐      │
│  │  Light Scheme     │              │  Dark Scheme      │      │
│  │  ──────────────── │              │  ──────────────── │      │
│  │  primary          │              │  primary          │      │
│  │  onPrimary        │              │  onPrimary        │      │
│  │  primaryContainer │              │  primaryContainer │      │
│  │  secondary        │              │  secondary        │      │
│  │  surface          │              │  surface          │      │
│  │  background       │              │  background       │      │
│  │  error            │              │  error            │      │
│  │  ...29 roles      │              │  ...29 roles      │      │
│  └───────────────────┘              └───────────────────┘      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Setting Up Material 3 Theme in Compose (Kotlin)

```kotlin
// build.gradle.kts (app)
dependencies {
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")
}
```

```kotlin
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Define custom color scheme as fallback
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)

@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // Enable dynamic colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic color available on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,   // Custom typography
        shapes = AppShapes,           // Custom shapes
        content = content
    )
}
```

### Code: Custom Typography & Shapes (Kotlin)

```kotlin
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Material 3 Typography Scale
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Material 3 Shapes
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
```

### Code: Using Color Roles in Compose (Kotlin)

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColorRolesDemo() {
    Column(modifier = Modifier.padding(16.dp)) {
        // Primary colors
        Surface(
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("Primary / onPrimary", modifier = Modifier.padding(16.dp))
        }

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("PrimaryContainer / onPrimaryContainer",
                modifier = Modifier.padding(16.dp))
        }

        // Secondary colors
        Surface(
            color = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("Secondary / onSecondary", modifier = Modifier.padding(16.dp))
        }

        // Tertiary colors
        Surface(
            color = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("Tertiary / onTertiary", modifier = Modifier.padding(16.dp))
        }

        // Surface colors
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("SurfaceVariant / onSurfaceVariant",
                modifier = Modifier.padding(16.dp))
        }

        // Error colors
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("ErrorContainer / onErrorContainer",
                modifier = Modifier.padding(16.dp))
        }
    }
}
```

---

## 2. Adaptive Layouts

### Theory

Adaptive layouts ensure your app looks great on **all screen sizes** — from phones to tablets, foldables, and desktops. Material 3 uses **Window Size Classes** to categorize screen sizes and adapt UI accordingly.

**Window Size Classes:**
- **Compact**: Width < 600dp (phones in portrait)
- **Medium**: 600dp ≤ Width < 840dp (tablets in portrait, foldables)
- **Expanded**: Width ≥ 840dp (tablets in landscape, desktops)

```
┌─────────────────────────────────────────────────────────────────┐
│                  Adaptive Layout Strategy                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  COMPACT (<600dp)      MEDIUM (600-840dp)    EXPANDED (≥840dp) │
│  ┌───────────┐        ┌──────────────┐      ┌────────────────┐ │
│  │           │        │    │         │      │   │            │ │
│  │  Single   │        │ Nav│  Main   │      │Nav│    Main    │ │
│  │  Column   │        │Rail│  Content│      │   │   Content  │ │
│  │           │        │    │         │      │   │            │ │
│  │           │        │    │         │      │   │     ┌────┐ │ │
│  │           │        │    │         │      │   │     │Side│ │ │
│  │           │        │    │         │      │   │     │Pane│ │ │
│  │           │        │    │         │      │   │     └────┘ │ │
│  ├───────────┤        │    │         │      │   │            │ │
│  │Bottom Nav │        └──────────────┘      └────────────────┘ │
│  └───────────┘                                                  │
│                                                                  │
│  Navigation:           Navigation:          Navigation:         │
│  Bottom Bar            Navigation Rail      Permanent Drawer   │
│                                              or Nav Rail        │
│                                                                  │
│  Content:              Content:             Content:            │
│  Single pane           Single pane          List-detail or     │
│                        with more space      two-pane layout    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Adaptive Layout with Window Size Classes (Kotlin)

```kotlin
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent

class AdaptiveActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            MyAppTheme {
                AdaptiveApp(windowSizeClass)
            }
        }
    }
}

@Composable
fun AdaptiveApp(windowSizeClass: WindowSizeClass) {
    val navigationType = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> NavigationType.BOTTOM_NAVIGATION
        WindowWidthSizeClass.Medium -> NavigationType.NAVIGATION_RAIL
        WindowWidthSizeClass.Expanded -> NavigationType.PERMANENT_NAVIGATION_DRAWER
        else -> NavigationType.BOTTOM_NAVIGATION
    }

    val contentType = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> ContentType.SINGLE_PANE
        WindowWidthSizeClass.Medium -> ContentType.SINGLE_PANE
        WindowWidthSizeClass.Expanded -> ContentType.DUAL_PANE
        else -> ContentType.SINGLE_PANE
    }

    AdaptiveAppContent(
        navigationType = navigationType,
        contentType = contentType
    )
}

enum class NavigationType {
    BOTTOM_NAVIGATION,
    NAVIGATION_RAIL,
    PERMANENT_NAVIGATION_DRAWER
}

enum class ContentType {
    SINGLE_PANE,
    DUAL_PANE
}
```

### Code: Adaptive Navigation (Kotlin)

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AdaptiveAppContent(
    navigationType: NavigationType,
    contentType: ContentType
) {
    var selectedDestination by remember { mutableIntStateOf(0) }

    val destinations = listOf(
        NavigationItem(Icons.Default.Home, "Home"),
        NavigationItem(Icons.Default.Search, "Search"),
        NavigationItem(Icons.Default.Settings, "Settings")
    )

    when (navigationType) {
        NavigationType.BOTTOM_NAVIGATION -> {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        destinations.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = selectedDestination == index,
                                onClick = { selectedDestination = index }
                            )
                        }
                    }
                }
            ) { padding ->
                MainContent(
                    selectedDestination = selectedDestination,
                    contentType = contentType,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        NavigationType.NAVIGATION_RAIL -> {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail {
                    destinations.forEachIndexed { index, item ->
                        NavigationRailItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selectedDestination == index,
                            onClick = { selectedDestination = index }
                        )
                    }
                }
                MainContent(
                    selectedDestination = selectedDestination,
                    contentType = contentType,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        NavigationType.PERMANENT_NAVIGATION_DRAWER -> {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(modifier = Modifier.width(240.dp)) {
                        Spacer(Modifier.height(12.dp))
                        destinations.forEachIndexed { index, item ->
                            NavigationDrawerItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = selectedDestination == index,
                                onClick = { selectedDestination = index },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            ) {
                MainContent(
                    selectedDestination = selectedDestination,
                    contentType = contentType
                )
            }
        }
    }
}

data class NavigationItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
```

---

## 3. Updated Components

### Theory

Material 3 introduces redesigned components with new visual styles, updated elevation system (tonal elevation instead of shadow), and improved accessibility defaults.

```
┌─────────────────────────────────────────────────────────────────┐
│              Material 3 Component Changes                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  M2 Component          →    M3 Component                        │
│  ──────────────────         ──────────────────                  │
│  TopAppBar             →    CenterAlignedTopAppBar              │
│                              MediumTopAppBar                    │
│                              LargeTopAppBar                     │
│  FloatingActionButton  →    FloatingActionButton (rounded)      │
│                              SmallFloatingActionButton          │
│                              LargeFloatingActionButton          │
│                              ExtendedFloatingActionButton       │
│  BottomNavigation      →    NavigationBar                       │
│  Card                  →    Card / ElevatedCard / OutlinedCard  │
│  TextField             →    TextField / OutlinedTextField       │
│  Button                →    Button / FilledTonalButton /        │
│                              OutlinedButton / TextButton /      │
│                              ElevatedButton                     │
│  AlertDialog           →    AlertDialog (updated)               │
│  Checkbox              →    Checkbox / TriStateCheckbox         │
│  Slider                →    Slider / RangeSlider                │
│  Switch                →    Switch (with thumb icon)            │
│  Chip                  →    AssistChip / FilterChip /           │
│                              InputChip / SuggestionChip         │
│                                                                  │
│  Elevation:                                                     │
│  M2: Shadow-based elevation (0dp, 1dp, 2dp... 24dp)           │
│  M3: Tonal elevation (surface color shifts, subtle shadows)    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Material 3 Component Showcase (Kotlin)

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ComponentShowcase() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // Large collapsing top app bar
            LargeTopAppBar(
                title = { Text("Material You") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Create") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Button variants
            Text("Buttons", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { }) { Text("Filled") }
                FilledTonalButton(onClick = { }) { Text("Tonal") }
                OutlinedButton(onClick = { }) { Text("Outlined") }
                ElevatedButton(onClick = { }) { Text("Elevated") }
                TextButton(onClick = { }) { Text("Text") }
            }

            // Card variants
            Text("Cards", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f)) {
                    Text("Filled Card", modifier = Modifier.padding(16.dp))
                }
                ElevatedCard(modifier = Modifier.weight(1f)) {
                    Text("Elevated Card", modifier = Modifier.padding(16.dp))
                }
                OutlinedCard(modifier = Modifier.weight(1f)) {
                    Text("Outlined Card", modifier = Modifier.padding(16.dp))
                }
            }

            // Chips
            Text("Chips", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { },
                    label = { Text("Assist") },
                    leadingIcon = {
                        Icon(Icons.Default.Star, contentDescription = null)
                    }
                )
                var filterSelected by remember { mutableStateOf(false) }
                FilterChip(
                    selected = filterSelected,
                    onClick = { filterSelected = !filterSelected },
                    label = { Text("Filter") },
                    leadingIcon = if (filterSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
                InputChip(
                    selected = false,
                    onClick = { },
                    label = { Text("Input") },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                )
                SuggestionChip(
                    onClick = { },
                    label = { Text("Suggestion") }
                )
            }

            // Switch with thumb icon
            Text("Switch", style = MaterialTheme.typography.titleMedium)
            var checked by remember { mutableStateOf(true) }
            Switch(
                checked = checked,
                onCheckedChange = { checked = it },
                thumbContent = {
                    if (checked) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                }
            )

            // Segmented Button
            Text("Segmented Buttons", style = MaterialTheme.typography.titleMedium)
            var selectedIndex by remember { mutableIntStateOf(0) }
            val options = listOf("Day", "Week", "Month")
            SingleChoiceSegmentedButtonRow {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = options.size
                        ),
                        onClick = { selectedIndex = index },
                        selected = index == selectedIndex
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}
```

### Code: Material 3 Dialogs (Kotlin)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3Dialogs() {
    var showBasicDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Basic Alert Dialog
    if (showBasicDialog) {
        AlertDialog(
            onDismissRequest = { showBasicDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text("Dialog Title") },
            text = { Text("This is a Material 3 dialog with an icon.") },
            confirmButton = {
                TextButton(onClick = { showBasicDialog = false }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBasicDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}
```

---

## 4. Motion and Transitions

### Theory

Material 3 motion emphasizes **purposeful, smooth, and expressive** animations. Key principles include:

- **Informative**: Motion tells users where they came from and where they're going
- **Focused**: Motion draws attention to what matters
- **Expressive**: Motion adds personality without slowing users down

```
┌─────────────────────────────────────────────────────────────────┐
│               Material 3 Motion Patterns                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Container Transform                                         │
│  ┌──────┐                    ┌─────────────────┐               │
│  │ Card │  ───────────────>  │                 │               │
│  │      │    expand           │   Detail Page   │               │
│  └──────┘                    │                 │               │
│                               └─────────────────┘               │
│                                                                  │
│  2. Shared Axis (Forward/Backward)                              │
│  ┌─────────┐    slide      ┌─────────┐                         │
│  │  Page A  │ ──────────>  │  Page B  │                         │
│  └─────────┘   + fade      └─────────┘                         │
│                                                                  │
│  3. Fade Through                                                │
│  ┌─────────┐    fade out   ┌─────────┐                         │
│  │  View A  │ ──────────>  │  View B  │                         │
│  └─────────┘    fade in    └─────────┘                         │
│                                                                  │
│  4. Fade                                                        │
│  ┌─────────┐                                                    │
│  │  View   │ ──── appears/disappears with opacity               │
│  └─────────┘                                                    │
│                                                                  │
│  Easing Curves (M3):                                            │
│  • EmphasizedDecelerate: Items arriving on screen              │
│  • EmphasizedAccelerate: Items leaving the screen              │
│  • Emphasized:           Primary transitions                    │
│  • Standard:             Utility transitions                    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Animated Visibility with M3 Motion (Kotlin)

```kotlin
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun M3MotionExamples() {
    var isVisible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.padding(16.dp)) {
        // 1. Fade Through Transition (Tab switching)
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Tab 1", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Tab 2", modifier = Modifier.padding(16.dp))
            }
        }

        // Fade through animation for tab content
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                // Fade through = fade out + scale down, then fade in + scale up
                (fadeIn(
                    animationSpec = tween(220, delayMillis = 90)
                ) + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(220, delayMillis = 90)
                )).togetherWith(
                    fadeOut(animationSpec = tween(90))
                )
            },
            label = "FadeThrough"
        ) { targetTab ->
            when (targetTab) {
                0 -> TabContent("Content for Tab 1")
                1 -> TabContent("Content for Tab 2")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Shared Axis Transition (Forward/Back navigation)
        Button(onClick = { isVisible = !isVisible }) {
            Text(if (isVisible) "Hide" else "Show")
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Animated Content",
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

@Composable
private fun TabContent(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
```

### Code: Shared Element Transition (Navigation Compose) (Kotlin)

```kotlin
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*

@Composable
fun SharedElementTransitionDemo() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable(
            "list",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            ItemListScreen(
                onItemClick = { itemId ->
                    navController.navigate("detail/$itemId")
                }
            )
        }

        composable(
            "detail/{itemId}",
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }) +
                    fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { it }) +
                    fadeOut(tween(300))
            }
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            ItemDetailScreen(itemId = itemId)
        }
    }
}

@Composable
fun ItemListScreen(onItemClick: (String) -> Unit) {
    val items = remember {
        (1..20).map { "Item $it" }
    }

    LazyColumn {
        items(items) { item ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onItemClick(item) }
            ) {
                ListItem(
                    headlineContent = { Text(item) },
                    supportingContent = { Text("Tap to see details") }
                )
            }
        }
    }
}

@Composable
fun ItemDetailScreen(itemId: String) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = itemId,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Detail content for $itemId",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

---

## 5. Dynamic Color with XML (Views System)

### Code: Material 3 Theme for XML-based Apps

```xml
<!-- res/values/themes.xml -->
<resources>
    <style name="Theme.MyApp" parent="Theme.Material3.DayNight">
        <!-- Primary brand color -->
        <item name="colorPrimary">@color/md_theme_primary</item>
        <item name="colorOnPrimary">@color/md_theme_onPrimary</item>
        <item name="colorPrimaryContainer">@color/md_theme_primaryContainer</item>
        <item name="colorOnPrimaryContainer">@color/md_theme_onPrimaryContainer</item>
        <!-- Secondary -->
        <item name="colorSecondary">@color/md_theme_secondary</item>
        <item name="colorOnSecondary">@color/md_theme_onSecondary</item>
        <item name="colorSecondaryContainer">@color/md_theme_secondaryContainer</item>
        <item name="colorOnSecondaryContainer">@color/md_theme_onSecondaryContainer</item>
        <!-- Tertiary -->
        <item name="colorTertiary">@color/md_theme_tertiary</item>
        <item name="colorOnTertiary">@color/md_theme_onTertiary</item>
        <!-- Error -->
        <item name="colorError">@color/md_theme_error</item>
        <item name="colorOnError">@color/md_theme_onError</item>
        <!-- Surface -->
        <item name="colorSurface">@color/md_theme_surface</item>
        <item name="colorOnSurface">@color/md_theme_onSurface</item>
    </style>

    <!-- Dynamic Color Theme (Android 12+) -->
    <style name="Theme.MyApp.DynamicColors" parent="Theme.Material3.DynamicColors.DayNight">
    </style>
</resources>
```

### Code: Applying Dynamic Colors in Activity

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors

class DynamicColorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply dynamic colors from user's wallpaper
        DynamicColors.applyToActivityIfAvailable(this)

        setContentView(R.layout.activity_dynamic_color)
    }
}

// Or apply globally in Application class
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply dynamic colors to all activities
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
```

---

## Summary

| Feature | Key API / Tool | Purpose |
|---------|---------------|---------|
| Dynamic Theming | `dynamicLightColorScheme()` / `DynamicColors` | Personalized colors from wallpaper |
| Adaptive Layouts | `WindowSizeClass` | Responsive UI across screen sizes |
| Updated Components | `material3` composables | Modern, accessible UI elements |
| Motion & Transitions | `AnimatedContent`, `AnimatedVisibility` | Expressive, purposeful animations |
| Color Roles | `MaterialTheme.colorScheme` | Semantic color usage (29 roles) |
| Typography | `MaterialTheme.typography` | 15 text styles (display→label) |
| Shapes | `MaterialTheme.shapes` | 5 corner radius levels |

---

## Best Practices

1. **Always provide a fallback** color scheme for devices without dynamic color support
2. **Use semantic color roles** (`primary`, `secondary`) instead of hardcoded colors
3. **Test with different wallpapers** to ensure contrast and readability
4. **Use `WindowSizeClass`** to adapt navigation and layout for different screen sizes
5. **Follow the tonal elevation** model — use `tonalElevation` instead of shadow elevation
6. **Use Material Theme Builder** (material-foundation.github.io) to generate color schemes
7. **Migrate incrementally** — Material 3 Compose and Material 2 can coexist temporarily
