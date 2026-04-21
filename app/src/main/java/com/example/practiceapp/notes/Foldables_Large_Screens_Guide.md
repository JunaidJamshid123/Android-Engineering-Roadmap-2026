# 15.3 Foldables and Large Screens

## Overview

With the growing diversity of Android devices — foldables, tablets, Chromebooks, and desktop modes — building **adaptive apps** that work beautifully across all form factors is essential. Google provides APIs and guidelines for handling different window sizes, multi-window modes, and specialized input like stylus.

```
┌─────────────────────────────────────────────────────────────────┐
│              Large Screen Device Landscape                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────┐  ┌──────────┐  ┌──────────────┐  ┌────────────┐  │
│  │  Phone  │  │ Foldable │  │   Tablet     │  │ Chromebook │  │
│  │         │  │ ┌──┬──┐  │  │              │  │            │  │
│  │ ┌─────┐ │  │ │  │  │  │  │  ┌────────┐  │  │ ┌────────┐ │  │
│  │ │     │ │  │ │  │  │  │  │  │        │  │  │ │        │ │  │
│  │ │     │ │  │ │  │  │  │  │  │        │  │  │ │        │ │  │
│  │ │     │ │  │ │  │  │  │  │  │        │  │  │ │        │ │  │
│  │ └─────┘ │  │ └──┴──┘  │  │  └────────┘  │  │ └────────┘ │  │
│  └─────────┘  └──────────┘  └──────────────┘  └────────────┘  │
│  <600dp       600-840dp      ≥840dp           ≥840dp+keyboard │
│  Compact      Medium         Expanded         Expanded        │
│                                                                  │
│  Foldable States:                                               │
│  ┌──────────┐    ┌────────────┐    ┌──────────────────┐        │
│  │ Folded   │    │ Half-open  │    │ Fully open       │        │
│  │ (phone)  │    │ (tabletop/ │    │ (tablet mode)    │        │
│  │          │    │  book mode)│    │                  │        │
│  └──────────┘    └────────────┘    └──────────────────┘        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1. Window Size Classes

### Theory

Window Size Classes are **breakpoints** that categorize the available window space into three buckets: **Compact**, **Medium**, and **Expanded**. They apply to both width and height independently.

**Width Classes:**
- **Compact** (< 600dp): Most phones in portrait
- **Medium** (600dp – 840dp): Tablets in portrait, foldables unfolded
- **Expanded** (≥ 840dp): Tablets in landscape, desktop

**Height Classes:**
- **Compact** (< 480dp): Phones in landscape
- **Medium** (480dp – 900dp): Tablets in landscape, phones in portrait
- **Expanded** (≥ 900dp): Tablets in portrait

```
┌─────────────────────────────────────────────────────────────────┐
│           Window Size Classes — Width Breakpoints                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  0dp                600dp              840dp           ∞         │
│  │                   │                  │              │         │
│  │◄── Compact ──────►│◄── Medium ──────►│◄─ Expanded ─►│         │
│  │                   │                  │              │         │
│  │  Phone portrait   │ Tablet portrait  │ Tablet land  │         │
│  │  Foldable folded  │ Foldable open    │ Desktop      │         │
│  │                   │                  │              │         │
│  │  Layout:          │  Layout:         │  Layout:     │         │
│  │  Single column    │  Two columns     │  Multi-pane  │         │
│  │  Bottom nav       │  Nav rail        │  Nav drawer  │         │
│  │  Full-width cards │  Grid cards      │  List+Detail │         │
│  │                   │                  │              │         │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Using Window Size Classes (Kotlin)

```kotlin
// build.gradle.kts
dependencies {
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")
    implementation("androidx.window:window:1.2.0")
}
```

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            AppContent(windowSizeClass)
        }
    }
}

@Composable
fun AppContent(windowSizeClass: WindowSizeClass) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Phone layout — single pane, bottom navigation
            CompactLayout()
        }
        WindowWidthSizeClass.Medium -> {
            // Tablet portrait / foldable — navigation rail
            MediumLayout()
        }
        WindowWidthSizeClass.Expanded -> {
            // Large screen — list-detail, navigation drawer
            ExpandedLayout()
        }
    }
}
```

### Code: Window Size Classes with Views (Kotlin)

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.window.layout.WindowMetricsCalculator

class AdaptiveActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adaptive)
        adaptLayout()
    }

    private fun adaptLayout() {
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this)
        val widthDp = metrics.bounds.width() /
            resources.displayMetrics.density

        when {
            widthDp < 600f -> setupCompactLayout()
            widthDp < 840f -> setupMediumLayout()
            else -> setupExpandedLayout()
        }
    }

    private fun setupCompactLayout() {
        // Single pane with bottom navigation
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, ListFragment())
            .commit()
        binding.bottomNav.visibility = View.VISIBLE
        binding.navRail?.visibility = View.GONE
    }

    private fun setupMediumLayout() {
        // Single pane with navigation rail
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, ListFragment())
            .commit()
        binding.bottomNav.visibility = View.GONE
        binding.navRail?.visibility = View.VISIBLE
    }

    private fun setupExpandedLayout() {
        // List-detail layout
        supportFragmentManager.beginTransaction()
            .replace(R.id.list_container, ListFragment())
            .replace(R.id.detail_container, DetailFragment())
            .commit()
        binding.bottomNav.visibility = View.GONE
        binding.navRail?.visibility = View.VISIBLE
    }
}
```

---

## 2. Adaptive Layouts with Compose

### Theory

Jetpack Compose makes adaptive layouts easier with built-in support for responsive UI. You can combine **Window Size Classes** with **Layout composables** to build UIs that smoothly transition between form factors.

**Common Adaptive Patterns:**
- **List-Detail**: Show list and detail side by side on large screens
- **Feed**: Adjust column count based on available width
- **Supporting Panel**: Show auxiliary content in a side panel on large screens

```
┌─────────────────────────────────────────────────────────────────┐
│             Adaptive Layout Patterns                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  LIST-DETAIL:                                                   │
│  Compact:              Expanded:                                │
│  ┌──────────────┐     ┌──────────┬───────────┐                 │
│  │    List      │     │   List   │  Detail   │                 │
│  │  ┌────────┐  │     │ ┌──────┐ │           │                 │
│  │  │ Item 1 │  │     │ │Item 1│ │  Title    │                 │
│  │  │ Item 2 │  │     │ │Item 2│ │  Content  │                 │
│  │  │ Item 3 │  │     │ │Item 3│ │  ...      │                 │
│  │  └────────┘  │     │ └──────┘ │           │                 │
│  └──────────────┘     └──────────┴───────────┘                 │
│  (tap → navigate)     (click → show in detail pane)            │
│                                                                  │
│  FEED:                                                          │
│  Compact:              Expanded:                                │
│  ┌──────────────┐     ┌──────────────────────┐                 │
│  │ ┌──────────┐ │     │ ┌──────┐ ┌──────┐   │                 │
│  │ │  Card 1  │ │     │ │Card 1│ │Card 2│   │                 │
│  │ ├──────────┤ │     │ ├──────┤ ├──────┤   │                 │
│  │ │  Card 2  │ │     │ │Card 3│ │Card 4│   │                 │
│  │ ├──────────┤ │     │ └──────┘ └──────┘   │                 │
│  │ │  Card 3  │ │     │ ┌──────┐ ┌──────┐   │                 │
│  │ └──────────┘ │     │ │Card 5│ │Card 6│   │                 │
│  └──────────────┘     └──────────────────────┘                 │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: List-Detail Layout (Kotlin)

```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class EmailItem(val id: Int, val subject: String, val body: String, val sender: String)

@Composable
fun ListDetailScreen(widthSizeClass: WindowWidthSizeClass) {
    val emails = remember {
        (1..20).map {
            EmailItem(it, "Subject $it", "Email body content for item $it...", "sender$it@email.com")
        }
    }
    var selectedEmail by remember { mutableStateOf<EmailItem?>(null) }

    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Single pane — navigate between list and detail
            if (selectedEmail != null) {
                DetailPane(
                    email = selectedEmail!!,
                    onBack = { selectedEmail = null }
                )
            } else {
                ListPane(
                    emails = emails,
                    onEmailClick = { selectedEmail = it }
                )
            }
        }
        else -> {
            // Two pane — show list and detail side by side
            Row(modifier = Modifier.fillMaxSize()) {
                ListPane(
                    emails = emails,
                    selectedEmail = selectedEmail,
                    onEmailClick = { selectedEmail = it },
                    modifier = Modifier.weight(0.4f)
                )
                selectedEmail?.let { email ->
                    DetailPane(
                        email = email,
                        modifier = Modifier.weight(0.6f)
                    )
                } ?: Box(
                    modifier = Modifier.weight(0.6f),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Select an email to read")
                }
            }
        }
    }
}

@Composable
fun ListPane(
    emails: List<EmailItem>,
    selectedEmail: EmailItem? = null,
    onEmailClick: (EmailItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(emails) { email ->
            val isSelected = email.id == selectedEmail?.id
            ListItem(
                headlineContent = { Text(email.subject) },
                supportingContent = { Text(email.sender) },
                modifier = Modifier
                    .clickable { onEmailClick(email) }
                    .then(
                        if (isSelected) Modifier.background(
                            MaterialTheme.colorScheme.secondaryContainer
                        ) else Modifier
                    ),
                tonalElevation = if (isSelected) 2.dp else 0.dp
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun DetailPane(
    email: EmailItem,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        onBack?.let {
            IconButton(onClick = it) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
        Text(email.subject, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("From: ${email.sender}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(email.body, style = MaterialTheme.typography.bodyLarge)
    }
}
```

### Code: Adaptive Grid Layout (Kotlin)

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdaptiveGridScreen(widthSizeClass: WindowWidthSizeClass) {
    val columns = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        WindowWidthSizeClass.Expanded -> 3
        else -> 1
    }

    val items = (1..30).toList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Item $item",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Description for item $item",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Or use adaptive columns that auto-fit
@Composable
fun AutoAdaptiveGrid() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),  // Auto-fit columns
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(30) { index ->
            Card(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Item ${index + 1}")
                }
            }
        }
    }
}
```

---

## 3. Multi-Resume and Multi-Window

### Theory

**Multi-window** allows apps to run side-by-side (split-screen) or in free-form windows. **Multi-resume** (Android 10+) means all visible apps in multi-window mode are in the `RESUMED` state simultaneously — not just the focused one.

**Key Concepts:**
- **Split-screen mode**: Two apps share the screen (50/50 or adjustable)
- **Free-form mode**: Apps in resizable, movable windows (Chromebooks, some tablets)
- **Picture-in-Picture (PiP)**: Floating overlay window
- **Multi-resume**: All visible activities are resumed
- **`isInMultiWindowMode`**: Check if currently in multi-window

```
┌─────────────────────────────────────────────────────────────────┐
│               Multi-Window Modes                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Split-Screen Mode:                                             │
│  ┌─────────────────────────────────────────────┐               │
│  │  ┌──────────────────┬──────────────────┐    │               │
│  │  │                  │                  │    │               │
│  │  │    App A         │    App B         │    │               │
│  │  │   (Resumed)      │   (Resumed)      │    │               │
│  │  │                  │                  │    │               │
│  │  │                  │                  │    │               │
│  │  └──────────────────┴──────────────────┘    │               │
│  │           ◄── Adjustable divider ──►        │               │
│  └─────────────────────────────────────────────┘               │
│                                                                  │
│  Picture-in-Picture:         Free-form:                         │
│  ┌───────────────────┐      ┌─────────────────────┐           │
│  │                   │      │  ┌──────┐           │           │
│  │     Main App      │      │  │ App A│  ┌──────┐ │           │
│  │                   │      │  └──────┘  │ App B│ │           │
│  │            ┌────┐ │      │            └──────┘ │           │
│  │            │PiP │ │      │       ┌──────┐      │           │
│  │            │    │ │      │       │ App C│      │           │
│  │            └────┘ │      │       └──────┘      │           │
│  └───────────────────┘      └─────────────────────┘           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Multi-Window Support (Kotlin)

```xml
<!-- AndroidManifest.xml -->
<activity
    android:name=".MultiWindowActivity"
    android:resizeableActivity="true"
    android:supportsPictureInPicture="true"
    android:configChanges="screenSize|screenLayout|smallestScreenSize|orientation">
    
    <!-- Optional: Set minimum size for free-form mode -->
    <layout
        android:defaultWidth="600dp"
        android:defaultHeight="500dp"
        android:gravity="center"
        android:minWidth="300dp"
        android:minHeight="200dp" />
</activity>
```

```kotlin
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MultiWindowActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_window)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // React to configuration changes (size, orientation)
        adaptToWindowSize()
    }

    override fun onMultiWindowModeChanged(
        isInMultiWindowMode: Boolean,
        newConfig: Configuration
    ) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)

        if (isInMultiWindowMode) {
            // Entering multi-window — simplify UI
            hideSecondaryContent()
            adjustForSmallWindow()
        } else {
            // Exiting multi-window — restore full UI
            showSecondaryContent()
            restoreFullLayout()
        }
    }

    private fun adaptToWindowSize() {
        val width = resources.configuration.screenWidthDp
        val height = resources.configuration.screenHeightDp

        when {
            width < 600 -> setupCompactLayout()
            width < 840 -> setupMediumLayout()
            else -> setupExpandedLayout()
        }
    }

    // Check multi-window state
    private fun checkMultiWindowStatus() {
        if (isInMultiWindowMode) {
            // Adapt UI for multi-window
        }
        if (isInPictureInPictureMode) {
            // Adapt UI for PiP
        }
    }

    private fun hideSecondaryContent() { /* ... */ }
    private fun adjustForSmallWindow() { /* ... */ }
    private fun showSecondaryContent() { /* ... */ }
    private fun restoreFullLayout() { /* ... */ }
    private fun setupCompactLayout() { /* ... */ }
    private fun setupMediumLayout() { /* ... */ }
    private fun setupExpandedLayout() { /* ... */ }
}
```

### Code: Picture-in-Picture (Kotlin)

```kotlin
import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.appcompat.app.AppCompatActivity

class PipActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pip)

        binding.btnEnterPip.setOnClickListener {
            enterPipMode()
        }
    }

    private fun enterPipMode() {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))  // 16:9 aspect ratio
            .apply {
                if (Build.VERSION.SDK_INT >= 31) {
                    setAutoEnterEnabled(true)  // Auto-enter PiP on home press
                    setSeamlessResizeEnabled(true)
                }
            }
            .build()

        enterPictureInPictureMode(params)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        if (isInPictureInPictureMode) {
            // Hide controls, show only video
            binding.controlsLayout.visibility = View.GONE
        } else {
            // Restore full UI
            binding.controlsLayout.visibility = View.VISIBLE
        }
    }

    // Auto-enter PiP when user navigates away (for video players)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPipMode()
    }
}
```

---

## 4. Drag and Drop

### Theory

Drag and drop enables users to **move content between apps** or within the same app by dragging items. This is especially useful on large screens and in multi-window mode.

**Key Components:**
- **DragEvent**: System event containing drag data
- **View.OnDragListener**: Listener for receiving drag events
- **ClipData**: The data being dragged
- **DragShadowBuilder**: Visual feedback during drag

```
┌─────────────────────────────────────────────────────────────────┐
│                  Drag and Drop Flow                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────┐         ┌─────────────────┐              │
│  │   Source App     │         │   Target App     │              │
│  │                  │         │                  │              │
│  │  ┌──────────┐   │   drag  │   ┌──────────┐   │              │
│  │  │  Item    │───│─────────│──>│ Drop Zone│   │              │
│  │  └──────────┘   │         │   └──────────┘   │              │
│  └─────────────────┘         └─────────────────┘              │
│                                                                  │
│  Events:                                                        │
│  ACTION_DRAG_STARTED  → Drag begins                            │
│  ACTION_DRAG_ENTERED  → Dragged item enters drop zone          │
│  ACTION_DRAG_LOCATION → Dragged item moves within drop zone    │
│  ACTION_DRAG_EXITED   → Dragged item leaves drop zone          │
│  ACTION_DROP          → User drops the item                    │
│  ACTION_DRAG_ENDED    → Drag operation completed               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Drag and Drop Implementation (Kotlin)

```kotlin
import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class DragDropActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drag_drop)

        setupDragSource()
        setupDropTarget()
    }

    private fun setupDragSource() {
        val dragSourceView = binding.dragSourceView

        dragSourceView.setOnLongClickListener { view ->
            // Create clip data with the content to drag
            val clipData = ClipData.newPlainText(
                "DraggedText",
                "Hello from drag source!"
            )

            // Create a drag shadow
            val dragShadow = View.DragShadowBuilder(view)

            // Start the drag
            view.startDragAndDrop(
                clipData,
                dragShadow,
                view,  // Local state
                View.DRAG_FLAG_GLOBAL or  // Allow cross-app drag
                    View.DRAG_FLAG_GLOBAL_URI_READ  // Allow reading URIs
            )
            true
        }
    }

    private fun setupDropTarget() {
        val dropTargetView = binding.dropTargetView

        dropTargetView.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Check if we can accept this type of data
                    event.clipDescription?.hasMimeType(
                        ClipDescription.MIMETYPE_TEXT_PLAIN
                    ) == true
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Highlight the drop zone
                    view.setBackgroundColor(getColor(R.color.drop_highlight))
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> {
                    // Track drag position if needed
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    // Remove highlight
                    view.setBackgroundColor(getColor(R.color.drop_normal))
                    true
                }

                DragEvent.ACTION_DROP -> {
                    // Handle the dropped data
                    val item = event.clipData.getItemAt(0)
                    val droppedText = item.text.toString()
                    binding.droppedTextView.text = droppedText

                    // Remove highlight
                    view.setBackgroundColor(getColor(R.color.drop_normal))
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    // Clean up
                    view.setBackgroundColor(getColor(R.color.drop_normal))
                    true
                }

                else -> false
            }
        }
    }
}
```

### Code: Drag and Drop in Compose (Kotlin)

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DragAndDropCompose() {
    var droppedText by remember { mutableStateOf("Drop here") }

    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Drag Source
        Card(
            modifier = Modifier
                .weight(1f)
                .height(200.dp)
                .padding(8.dp)
                .dragAndDropSource {
                    detectTapGestures(
                        onLong = {
                            startTransfer(
                                DragAndDropTransferData(
                                    clipData = android.content.ClipData.newPlainText(
                                        "text", "Dragged Content!"
                                    )
                                )
                            )
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Long press to drag")
            }
        }

        // Drop Target
        val dragAndDropTarget = remember {
            object : DragAndDropTarget {
                override fun onDrop(event: DragAndDropEvent): Boolean {
                    val clipData = event.toAndroidDragEvent().clipData
                    droppedText = clipData.getItemAt(0).text.toString()
                    return true
                }

                override fun onEntered(event: DragAndDropEvent) {
                    // Highlight effect
                }

                override fun onExited(event: DragAndDropEvent) {
                    // Remove highlight
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .height(200.dp)
                .padding(8.dp)
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { event ->
                        event.toAndroidDragEvent().clipDescription
                            .hasMimeType(android.content.ClipDescription.MIMETYPE_TEXT_PLAIN)
                    },
                    target = dragAndDropTarget
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(droppedText)
            }
        }
    }
}
```

---

## 5. Stylus Input

### Theory

Stylus input support is critical for tablet and foldable apps, enabling **drawing, handwriting, and precision interactions**. Android provides specialized APIs for:

- **Pressure sensitivity**: How hard the user presses
- **Tilt detection**: The angle of the stylus
- **Palm rejection**: Ignoring accidental palm touches
- **Hover detection**: Stylus near the screen but not touching
- **Low-latency rendering**: Fast ink rendering for drawing apps

```
┌─────────────────────────────────────────────────────────────────┐
│                Stylus Input Properties                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  MotionEvent Properties:                                        │
│  ┌─────────────────────────────────────────────┐               │
│  │  TOOL_TYPE_STYLUS  — Stylus is the input    │               │
│  │  TOOL_TYPE_FINGER  — Finger is the input    │               │
│  │  TOOL_TYPE_ERASER  — Eraser end of stylus   │               │
│  ├─────────────────────────────────────────────┤               │
│  │  getPressure()  — 0.0 to 1.0 (light→hard)  │               │
│  │  getSize()      — Contact area              │               │
│  │  getOrientation() — Rotation angle          │               │
│  │  getAxisValue(AXIS_TILT) — Tilt angle       │               │
│  │  getAxisValue(AXIS_DISTANCE) — Hover height │               │
│  │  getToolType()  — STYLUS/FINGER/ERASER      │               │
│  │  getButtonState() — Barrel button state     │               │
│  └─────────────────────────────────────────────┘               │
│                                                                  │
│  Events:                                                        │
│  ACTION_DOWN    → Stylus touches screen                        │
│  ACTION_MOVE    → Stylus moves on screen                       │
│  ACTION_UP      → Stylus lifts off screen                      │
│  ACTION_HOVER_ENTER → Stylus enters hover range               │
│  ACTION_HOVER_MOVE  → Stylus moves while hovering             │
│  ACTION_HOVER_EXIT  → Stylus leaves hover range               │
│  ACTION_BUTTON_PRESS → Barrel button pressed                   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Stylus Drawing Canvas (Kotlin)

```kotlin
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class StylusDrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paths = mutableListOf<DrawPath>()
    private var currentPath: DrawPath? = null

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    data class DrawPath(
        val path: Path,
        val color: Int,
        val strokeWidth: Float,
        val isEraser: Boolean = false
    )

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val pressure = event.pressure
        val toolType = event.getToolType(0)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val isEraser = toolType == MotionEvent.TOOL_TYPE_ERASER
                val strokeWidth = if (isEraser) {
                    30f  // Eraser has fixed width
                } else {
                    // Pressure-sensitive stroke width (2-20dp range)
                    2f + (pressure * 18f)
                }

                currentPath = DrawPath(
                    path = Path().apply { moveTo(x, y) },
                    color = if (isEraser) Color.WHITE else Color.BLACK,
                    strokeWidth = strokeWidth,
                    isEraser = isEraser
                )
                paths.add(currentPath!!)
            }

            MotionEvent.ACTION_MOVE -> {
                currentPath?.path?.lineTo(x, y)

                // Handle historical events for smoother lines
                for (i in 0 until event.historySize) {
                    val hx = event.getHistoricalX(i)
                    val hy = event.getHistoricalY(i)
                    currentPath?.path?.lineTo(hx, hy)
                }

                // Update stroke width based on pressure (for current stroke)
                if (toolType == MotionEvent.TOOL_TYPE_STYLUS) {
                    currentPath = currentPath?.copy(
                        strokeWidth = 2f + (pressure * 18f)
                    )
                }
            }

            MotionEvent.ACTION_UP -> {
                currentPath = null
            }
        }

        invalidate()
        return true
    }

    // Handle hover events (stylus near screen but not touching)
    override fun onHoverEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_HOVER_ENTER -> {
                // Show cursor/preview
            }
            MotionEvent.ACTION_HOVER_MOVE -> {
                // Update cursor position
                val x = event.x
                val y = event.y
                // Show hover indicator at (x, y)
            }
            MotionEvent.ACTION_HOVER_EXIT -> {
                // Hide cursor
            }
        }
        return super.onHoverEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (drawPath in paths) {
            paint.color = drawPath.color
            paint.strokeWidth = drawPath.strokeWidth
            if (drawPath.isEraser) {
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            } else {
                paint.xfermode = null
            }
            canvas.drawPath(drawPath.path, paint)
        }
    }
}
```

### Code: Stylus with Low-Latency Rendering (Kotlin)

```kotlin
import android.graphics.Canvas
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.graphics.lowlatency.CanvasFrontBufferedRenderer

class LowLatencyDrawingActivity : AppCompatActivity() {

    private lateinit var surfaceView: SurfaceView
    private var renderer: CanvasFrontBufferedRenderer<FloatArray>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        surfaceView = SurfaceView(this)
        setContentView(surfaceView)

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                setupRenderer()
            }

            override fun surfaceChanged(holder: SurfaceHolder, f: Int, w: Int, h: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                renderer?.release(true)
            }
        })

        surfaceView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val data = floatArrayOf(event.x, event.y, event.pressure)
                    renderer?.renderFrontBufferedLayer(data)
                }
                MotionEvent.ACTION_UP -> {
                    renderer?.commit()
                }
            }
            true
        }
    }

    private fun setupRenderer() {
        renderer = CanvasFrontBufferedRenderer(
            surfaceView,
            object : CanvasFrontBufferedRenderer.Callback<FloatArray> {
                override fun onDrawFrontBufferedLayer(
                    canvas: Canvas,
                    bufferWidth: Int,
                    bufferHeight: Int,
                    param: FloatArray
                ) {
                    // Draw single point on front buffer (low latency)
                    val x = param[0]
                    val y = param[1]
                    val pressure = param[2]
                    val radius = 2f + (pressure * 10f)
                    canvas.drawCircle(x, y, radius, paint)
                }

                override fun onDrawMultiBufferedLayer(
                    canvas: Canvas,
                    bufferWidth: Int,
                    bufferHeight: Int,
                    params: Collection<FloatArray>
                ) {
                    // Commit all points to the multi-buffered layer
                    for (param in params) {
                        val x = param[0]
                        val y = param[1]
                        val pressure = param[2]
                        val radius = 2f + (pressure * 10f)
                        canvas.drawCircle(x, y, radius, paint)
                    }
                }
            }
        )
    }
}
```

### Code: Handling Foldable Postures with WindowManager (Kotlin)

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.launch

class FoldableAwareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foldable)

        // Observe folding features
        lifecycleScope.launch {
            WindowInfoTracker.getOrCreate(this@FoldableAwareActivity)
                .windowLayoutInfo(this@FoldableAwareActivity)
                .collect { layoutInfo ->
                    val foldingFeature = layoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull()

                    if (foldingFeature != null) {
                        handleFoldingFeature(foldingFeature)
                    } else {
                        // No fold — single screen layout
                        setupSingleScreenLayout()
                    }
                }
        }
    }

    private fun handleFoldingFeature(foldingFeature: FoldingFeature) {
        when {
            // Tabletop mode (horizontal fold, half-open)
            foldingFeature.state == FoldingFeature.State.HALF_OPENED &&
                foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL -> {
                setupTabletopLayout(foldingFeature)
            }
            // Book mode (vertical fold, half-open)
            foldingFeature.state == FoldingFeature.State.HALF_OPENED &&
                foldingFeature.orientation == FoldingFeature.Orientation.VERTICAL -> {
                setupBookLayout(foldingFeature)
            }
            // Flat (fully open)
            foldingFeature.state == FoldingFeature.State.FLAT -> {
                setupFlatLayout()
            }
        }
    }

    private fun setupTabletopLayout(fold: FoldingFeature) {
        // Top half: content, Bottom half: controls
        // Like a laptop — screen on top, keyboard area on bottom
        val foldPosition = fold.bounds.top
        binding.topContent.layoutParams.height = foldPosition
        binding.bottomControls.layoutParams.height =
            binding.root.height - foldPosition
    }

    private fun setupBookLayout(fold: FoldingFeature) {
        // Left half: list, Right half: detail
        // Like an open book
        val foldPosition = fold.bounds.left
        binding.leftPane.layoutParams.width = foldPosition
        binding.rightPane.layoutParams.width =
            binding.root.width - foldPosition
    }

    private fun setupFlatLayout() {
        // Full tablet layout
    }

    private fun setupSingleScreenLayout() {
        // Standard phone layout
    }
}
```

---

## Summary Table

| Feature | Key API | Description |
|---------|---------|-------------|
| Window Size Classes | `WindowSizeClass` | Categorize screen sizes (Compact/Medium/Expanded) |
| Adaptive Compose | `calculateWindowSizeClass()` | Responsive Compose layouts |
| Multi-window | `isInMultiWindowMode` | Split-screen and free-form support |
| Multi-resume | Android 10+ default | All visible activities are RESUMED |
| PiP | `PictureInPictureParams` | Floating video overlay |
| Drag & Drop | `startDragAndDrop()` | Move content between/within apps |
| Stylus | `MotionEvent.TOOL_TYPE_STYLUS` | Pressure, tilt, hover, barrel button |
| Foldables | `WindowInfoTracker`, `FoldingFeature` | Detect fold state and adapt layout |

---

## Best Practices

1. **Always test on multiple screen sizes** — use Android Studio's resizable emulator
2. **Never assume a fixed screen size** — use responsive layouts
3. **Support multi-window** — set `resizeableActivity="true"` (default on API 24+)
4. **Handle configuration changes gracefully** — save/restore state properly
5. **Use `WindowSizeClass`** over raw dp values for breakpoints
6. **Test foldable postures** — use the foldable emulator or remote test lab
7. **Support keyboard and mouse** — large screen users often use external input
8. **Handle drag and drop** for productivity — especially for content-creation apps
