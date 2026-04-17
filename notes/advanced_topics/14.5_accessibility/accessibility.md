# Accessibility in Android

## Overview

Accessibility ensures your app is usable by **everyone**, including people with visual, motor, hearing, or cognitive impairments. It's not optional — it's a legal requirement in many countries and affects **~15% of the global population**.

```
┌──────────────────────────────────────────────────────────────┐
│              Android Accessibility System                      │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────────────────┐                            │
│  │         Your App UI          │                            │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ │                            │
│  │  │Button│ │ Text │ │Image │ │                            │
│  │  └──┬───┘ └──┬───┘ └──┬───┘ │                            │
│  └─────┼────────┼────────┼─────┘                            │
│        │        │        │                                    │
│        ▼        ▼        ▼                                    │
│  ┌──────────────────────────────┐                            │
│  │   AccessibilityNodeInfo      │  ← Describes each UI      │
│  │   (content descriptions,     │     element to services    │
│  │    roles, states, actions)    │                            │
│  └──────────────┬───────────────┘                            │
│                 │                                             │
│                 ▼                                             │
│  ┌──────────────────────────────┐                            │
│  │   Accessibility Framework    │                            │
│  │   (AccessibilityManager)     │                            │
│  └──────────────┬───────────────┘                            │
│                 │                                             │
│       ┌─────────┼─────────┐                                  │
│       ▼         ▼         ▼                                  │
│  ┌────────┐ ┌───────┐ ┌──────────┐                          │
│  │TalkBack│ │Switch │ │BrailleBack│                          │
│  │(Screen │ │Access │ │(Braille  │                          │
│  │Reader) │ │       │ │Display)  │                          │
│  └────────┘ └───────┘ └──────────┘                          │
│                                                               │
│  Other services: Voice Access, Select to Speak, Magnifier    │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. TalkBack Support

TalkBack is Android's built-in screen reader. It reads aloud what's on screen and lets users navigate via gestures or keyboard.

### How TalkBack Navigates

```
┌──────────────────────────────────────────────────────────┐
│                TalkBack Gesture Navigation                 │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  Swipe Right  →  Move to next element                    │
│  Swipe Left   ←  Move to previous element                │
│  Double Tap      Activate (click) focused element        │
│  Swipe Up+Down   Open local context menu                 │
│  Two-Finger Scroll  Scroll content                       │
│                                                           │
│  Focus Order (default):                                   │
│  ┌─────┐     ┌─────┐     ┌─────┐     ┌─────┐            │
│  │  1  │ ──▶ │  2  │ ──▶ │  3  │ ──▶ │  4  │            │
│  │Title│     │Input│     │ Btn │     │Image│            │
│  └─────┘     └─────┘     └─────┘     └─────┘            │
│                                                           │
│  What TalkBack reads for each element:                    │
│  "[Content description], [Role], [State]"                 │
│  e.g. "Submit, Button, Double tap to activate"            │
│  e.g. "Email, Edit text, Enter your email"                │
│  e.g. "Profile photo, Image"                              │
└──────────────────────────────────────────────────────────┘
```

### Making Views TalkBack-Compatible (XML)

```xml
<!-- ✅ GOOD: Accessible layout -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <!-- Text views are read automatically -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Welcome to our app" />

    <!-- EditText: use hint for context -->
    <EditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Enter your email address"
        android:inputType="textEmailAddress"
        android:importantForAccessibility="yes" />

    <!-- Button: text is read automatically -->
    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Submit" />

    <!-- ImageButton: MUST have contentDescription -->
    <ImageButton
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:src="@drawable/ic_delete"
        android:contentDescription="Delete item"
        android:background="?attr/selectableItemBackground" />

    <!-- Decorative image: mark as not important -->
    <ImageView
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:src="@drawable/decorative_banner"
        android:importantForAccessibility="no" />
</LinearLayout>
```

### Making TalkBack Work in Compose

```kotlin
@Composable
fun AccessibleScreen() {
    Column(modifier = Modifier.padding(16.dp)) {

        // ✅ Text is automatically accessible
        Text("Welcome to our app", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        // ✅ TextField with label
        var email by remember { mutableStateOf("") }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address") },  // Read by TalkBack
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // ✅ Button text is read automatically
        Button(onClick = { }) {
            Text("Submit")
        }

        Spacer(Modifier.height(16.dp))

        // ✅ IconButton with contentDescription
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete item"  // REQUIRED for TalkBack
            )
        }

        // ✅ Decorative image — null contentDescription hides from TalkBack
        Image(
            painter = painterResource(R.drawable.banner),
            contentDescription = null  // Decorative, skip in TalkBack
        )
    }
}
```

---

## 2. Content Descriptions

Content descriptions are the **most important** accessibility feature. They provide text alternatives for non-text elements.

```
┌──────────────────────────────────────────────────────────────┐
│              Content Description Rules                        │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ✅ DO:                                                       │
│  • Describe the ACTION or MEANING, not the appearance         │
│  • Keep descriptions concise (2-4 words)                      │
│  • Be specific to context                                     │
│  • Update dynamically when state changes                      │
│                                                               │
│  ❌ DON'T:                                                    │
│  • Start with "Image of..." or "Button for..."               │
│    (TalkBack already announces the role)                      │
│  • Use redundant text (same as visible label)                 │
│  • Leave interactive elements without descriptions            │
│  • Use descriptions for decorative elements                   │
│                                                               │
│  EXAMPLES:                                                    │
│  ┌──────────────────┬──────────────────────────────────┐     │
│  │  Element          │  Content Description              │     │
│  ├──────────────────┼──────────────────────────────────┤     │
│  │  🔍 (Search icon) │  "Search"                         │     │
│  │  ❤️ (Like button) │  "Like" / "Unlike" (toggle)       │     │
│  │  🗑️ (Delete icon) │  "Delete message"                 │     │
│  │  📷 (User avatar) │  "John's profile photo"           │     │
│  │  ⭐⭐⭐⭐☆         │  "Rating: 4 out of 5 stars"       │     │
│  │  ✕ (Close)       │  "Close dialog"                   │     │
│  │  🎨 (Decorative)  │  null (hide from TalkBack)        │     │
│  └──────────────────┴──────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
```

### Dynamic Content Descriptions

```kotlin
@Composable
fun LikeButton(isLiked: Boolean, likeCount: Int, onToggle: () -> Unit) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier.semantics {
            // Dynamic description based on state
            contentDescription = if (isLiked) {
                "Unlike. Currently liked by $likeCount people"
            } else {
                "Like. Currently liked by $likeCount people"
            }
            // Announce role
            role = Role.Button
        }
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null, // Handled by parent semantics
            tint = if (isLiked) Color.Red else Color.Gray
        )
    }
}

// Star Rating with accessibility
@Composable
fun StarRating(rating: Int, maxRating: Int = 5, onRatingChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "Rating: $rating out of $maxRating stars"
            role = Role.RadioButton
        }
    ) {
        for (i in 1..maxRating) {
            IconButton(
                onClick = { onRatingChange(i) },
                modifier = Modifier.semantics {
                    contentDescription = "$i star${if (i > 1) "s" else ""}"
                    selected = i == rating
                }
            ) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null
                )
            }
        }
    }
}
```

### Grouping Related Elements

```kotlin
@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* navigate */ }
            .semantics(mergeDescendants = true) {
                // TalkBack reads everything as ONE unit:
                // "Running Shoes, $89.99, 4.5 stars, In stock"
                contentDescription = buildString {
                    append(product.name)
                    append(", ${product.formattedPrice}")
                    append(", ${product.rating} stars")
                    append(", ${if (product.inStock) "In stock" else "Out of stock"}")
                }
            }
    ) {
        Row(Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(product.imageUrl),
                contentDescription = null, // Merged into parent
                modifier = Modifier.size(80.dp)
            )
            Column(Modifier.padding(start = 16.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(product.formattedPrice)
                Text("${product.rating} ⭐")
            }
        }
    }
}
```

---

## 3. Accessibility Services

### Custom Accessibility Actions

```kotlin
@Composable
fun SwipeableListItem(
    item: Item,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onEdit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                // Add custom actions for accessibility users
                // (These replace swipe gestures which are hard for some users)
                customActions = listOf(
                    CustomAccessibilityAction("Delete") {
                        onDelete()
                        true
                    },
                    CustomAccessibilityAction("Archive") {
                        onArchive()
                        true
                    },
                    CustomAccessibilityAction("Edit") {
                        onEdit()
                        true
                    }
                )
            }
    ) {
        ListItemContent(item)
    }
}
```

### Live Regions (Announce Dynamic Updates)

```kotlin
@Composable
fun CounterWithAnnouncement() {
    var count by remember { mutableIntStateOf(0) }

    Column {
        Text(
            text = "Count: $count",
            modifier = Modifier.semantics {
                // Announce changes to TalkBack automatically
                liveRegion = LiveRegionMode.Polite  // or Assertive
            }
        )

        Row {
            Button(onClick = { count-- }) { Text("-") }
            Spacer(Modifier.width(16.dp))
            Button(onClick = { count++ }) { Text("+") }
        }
    }
}

// XML equivalent
// android:accessibilityLiveRegion="polite"
// android:accessibilityLiveRegion="assertive"
```

### Traversal Order

```kotlin
@Composable
fun CustomTraversalOrder() {
    // Control the reading order for TalkBack
    val (first, second, third) = remember { List(3) { FocusRequester() } }

    Column {
        // This will be read SECOND
        Text(
            "Subtitle",
            modifier = Modifier.semantics {
                traversalIndex = 2f
            }
        )

        // This will be read FIRST
        Text(
            "Title",
            modifier = Modifier.semantics {
                traversalIndex = 1f
            }
        )

        // This will be read THIRD
        Button(
            onClick = { },
            modifier = Modifier.semantics {
                traversalIndex = 3f
            }
        ) {
            Text("Action")
        }
    }
}
```

### Headings for Screen Structure

```kotlin
@Composable
fun AccessibleForm() {
    Column {
        // Mark as heading — TalkBack users can jump between headings
        Text(
            "Personal Information",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )

        OutlinedTextField(value = "", onValueChange = {}, label = { Text("First name") })
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Last name") })

        // Another heading section
        Text(
            "Contact Details",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )

        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Email") })
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Phone") })
    }
}

// XML equivalent: android:accessibilityHeading="true"
```

### Building a Custom Accessibility Service

```kotlin
// Custom accessibility service (e.g., for enterprise apps)
class MyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                         AccessibilityEvent.TYPE_VIEW_FOCUSED or
                         AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                val source = event.source
                val text = source?.text?.toString() ?: "Unknown"
                val desc = source?.contentDescription?.toString()
                // Log or handle the event
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // New screen opened
                val packageName = event.packageName?.toString()
                val className = event.className?.toString()
            }
        }
    }

    override fun onInterrupt() {
        // Cleanup
    }
}
```

```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".MyAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
    android:exported="false">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

---

## 4. Touch Target Sizes

All interactive elements must have a **minimum touch target of 48dp x 48dp** (Google recommends this; WCAG recommends 44px).

```
┌──────────────────────────────────────────────────────────────┐
│                Touch Target Size Guidelines                   │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ❌ TOO SMALL (24x24dp)       ✅ CORRECT (48x48dp)           │
│  ┌──────┐                     ┌──────────────────┐           │
│  │  ✕   │ ← Hard to tap      │                  │           │
│  └──────┘                     │     ✕            │           │
│                                │                  │           │
│  ❌ Icons too close            │   48dp x 48dp    │           │
│  ┌──┐┌──┐┌──┐                 └──────────────────┘           │
│  │❤ ││📤││💬│ ← Accidental taps                              │
│  └──┘└──┘└──┘                 ✅ Enough spacing              │
│                                ┌──────┐  ┌──────┐  ┌──────┐ │
│                                │  ❤   │  │  📤  │  │  💬  │ │
│                                │48x48 │  │48x48 │  │48x48 │ │
│                                └──────┘  └──────┘  └──────┘ │
│                                   8dp      8dp               │
│                                 spacing   spacing            │
└──────────────────────────────────────────────────────────────┘
```

### Touch Targets in Compose

```kotlin
@Composable
fun AccessibleIconRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ✅ IconButton already enforces 48dp minimum
        IconButton(onClick = { }) {
            Icon(Icons.Default.Favorite, contentDescription = "Like")
        }

        IconButton(onClick = { }) {
            Icon(Icons.Default.Share, contentDescription = "Share")
        }

        IconButton(onClick = { }) {
            Icon(Icons.Default.Comment, contentDescription = "Comment")
        }
    }
}

// For custom small elements, use sizeIn to enforce minimum touch target
@Composable
fun SmallChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp), // ✅ Minimum touch target
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

// Using Modifier for existing composables
@Composable
fun AccessibleSmallButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp) // ✅
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            modifier = Modifier.size(24.dp) // Visual size can be smaller
        )
    }
}
```

### Touch Targets in XML

```xml
<!-- ✅ GOOD: 48dp minimum touch target -->
<ImageButton
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:src="@drawable/ic_close"
    android:padding="12dp"
    android:contentDescription="Close"
    android:background="?attr/selectableItemBackgroundBorderless" />

<!-- ❌ BAD: Too small -->
<ImageButton
    android:layout_width="24dp"
    android:layout_height="24dp"
    android:src="@drawable/ic_close"
    android:contentDescription="Close" />

<!-- ✅ GOOD: Use padding to increase touch target without changing visual size -->
<ImageButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:minWidth="48dp"
    android:minHeight="48dp"
    android:src="@drawable/ic_small_icon"
    android:padding="12dp"
    android:contentDescription="Settings" />
```

---

## 5. Color Contrast

Sufficient contrast ensures text and UI elements are readable for users with low vision or color blindness.

```
┌──────────────────────────────────────────────────────────────┐
│              WCAG Color Contrast Requirements                 │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Level AA (Minimum):                                          │
│  ┌────────────────────────────────────────────────┐          │
│  │  Normal text (< 18sp):     4.5:1 contrast ratio │          │
│  │  Large text  (≥ 18sp bold  3.0:1 contrast ratio │          │
│  │              or ≥ 24sp):                         │          │
│  │  UI Components & icons:    3.0:1 contrast ratio │          │
│  └────────────────────────────────────────────────┘          │
│                                                               │
│  Level AAA (Enhanced):                                        │
│  ┌────────────────────────────────────────────────┐          │
│  │  Normal text:               7.0:1 ratio         │          │
│  │  Large text:                4.5:1 ratio          │          │
│  └────────────────────────────────────────────────┘          │
│                                                               │
│  EXAMPLES:                                                    │
│                                                               │
│  ✅ GOOD CONTRAST              ❌ POOR CONTRAST               │
│  ┌─────────────────┐          ┌─────────────────┐           │
│  │ ██████████████  │          │ ░░░░░░░░░░░░░░  │           │
│  │ Black on White  │          │ Light gray on   │           │
│  │ Ratio: 21:1     │          │ White           │           │
│  └─────────────────┘          │ Ratio: 1.5:1    │           │
│                                └─────────────────┘           │
│  ┌─────────────────┐          ┌─────────────────┐           │
│  │ White on Dark   │          │ Yellow on White │           │
│  │ Blue: #1A237E   │          │ #FFEB3B on      │           │
│  │ Ratio: 12.7:1   │          │ #FFFFFF          │           │
│  └─────────────────┘          │ Ratio: 1.1:1    │           │
│                                └─────────────────┘           │
└──────────────────────────────────────────────────────────────┘
```

### Implementing Accessible Colors

```kotlin
// Define accessible color schemes
val AccessibleLightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),          // Dark blue on white: 7.5:1 ✅
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1), // Dark on light: 8.2:1 ✅

    secondary = Color(0xFF2E7D32),         // Dark green: 5.1:1 ✅
    onSecondary = Color.White,

    error = Color(0xFFC62828),             // Dark red: 5.6:1 ✅
    onError = Color.White,

    background = Color.White,
    onBackground = Color(0xFF1C1B1F),      // Near black: 16:1 ✅

    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),         // 16:1 ✅

    // ❌ AVOID: light colors on white backgrounds
    // Color(0xFFFFEB3B) on white = 1.1:1 (FAIL)
    // Color(0xFF90CAF9) on white = 2.1:1 (FAIL)
)

// Dark theme (also needs contrast!)
val AccessibleDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),           // Light blue on dark: 8.1:1 ✅
    onPrimary = Color(0xFF0D47A1),
    
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),      // Light gray on dark: 13:1 ✅

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),         // 11.5:1 ✅
)
```

### Handling Color Blindness

```kotlin
// Don't rely on color ALONE to convey information
@Composable
fun StatusIndicator(status: Status) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // ✅ Use icon + color + text (triple encoding)
        Icon(
            imageVector = when (status) {
                Status.SUCCESS -> Icons.Default.CheckCircle
                Status.WARNING -> Icons.Default.Warning
                Status.ERROR -> Icons.Default.Error
            },
            contentDescription = null,
            tint = when (status) {
                Status.SUCCESS -> Color(0xFF2E7D32)
                Status.WARNING -> Color(0xFFE65100)
                Status.ERROR -> Color(0xFFC62828)
            }
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = when (status) {
                Status.SUCCESS -> "Success"
                Status.WARNING -> "Warning"
                Status.ERROR -> "Error"
            }
        )
    }

    // ❌ BAD: Color only — colorblind users can't distinguish
    // Box(modifier = Modifier
    //     .size(16.dp)
    //     .background(if (valid) Color.Green else Color.Red))
}

// Form validation: use icons + text, not just red borders
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = error != null,
            trailingIcon = {
                if (error != null) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error", // ✅ Announced by TalkBack
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            supportingText = {
                if (error != null) {
                    Text(
                        text = error, // ✅ Error message in text
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Assertive // ✅ Announced immediately
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

---

## 6. Accessibility Testing Checklist

```
┌──────────────────────────────────────────────────────────────┐
│              Accessibility Testing Checklist                  │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  AUTOMATED TOOLS:                                             │
│  □ Android Lint (accessibility checks)                       │
│  □ Accessibility Scanner app (by Google)                     │
│  □ Espresso accessibility checks                             │
│  □ Compose UI test semantics assertions                      │
│                                                               │
│  MANUAL TESTING:                                              │
│  □ Navigate entire app with TalkBack enabled                 │
│  □ Complete all user flows eyes-closed with TalkBack         │
│  □ Test with Switch Access                                   │
│  □ Test with keyboard-only navigation                        │
│  □ Test with 200% font size (Display > Font size)            │
│  □ Test with Display > Bold text                             │
│  □ Test with Color correction (Settings > Accessibility)     │
│  □ Test with High contrast text                               │
│                                                               │
│  CHECK FOR:                                                   │
│  □ All images have content descriptions (or null)            │
│  □ All buttons/icons have labels                             │
│  □ Touch targets ≥ 48dp                                      │
│  □ Color contrast ≥ 4.5:1 for text                          │
│  □ Information not conveyed by color alone                   │
│  □ Error messages are in text (not just color)               │
│  □ Dynamic content announced via live regions                │
│  □ Headings mark page structure                               │
│  □ Custom views expose proper semantics                      │
│  □ Focus order is logical                                     │
│  □ Modal dialogs trap focus                                   │
│  □ Time-limited actions can be extended                       │
└──────────────────────────────────────────────────────────────┘
```

### Automated Testing with Compose

```kotlin
@Test
fun testAccessibility() {
    composeTestRule.setContent {
        MyScreen()
    }

    // Verify content descriptions exist
    composeTestRule
        .onNodeWithContentDescription("Delete item")
        .assertExists()
        .assertIsDisplayed()

    // Verify touch target size
    composeTestRule
        .onNodeWithContentDescription("Settings")
        .assertTouchHeightIsAtLeast(48.dp)
        .assertTouchWidthIsAtLeast(48.dp)

    // Verify semantics
    composeTestRule
        .onNodeWithText("Submit")
        .assertHasClickAction()
        .assert(hasRole(Role.Button))

    // Check heading semantics
    composeTestRule
        .onNodeWithText("Personal Information")
        .assert(isHeading())

    // Verify labels on text fields
    composeTestRule
        .onNodeWithText("Email address")
        .assertExists()
}

// Enable Espresso accessibility checks
@Before
fun setUp() {
    AccessibilityChecks.enable().setRunChecksFromRootView(true)
}
```
