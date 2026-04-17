# Android User Interface Development Guide

## 3. User Interface Development

---

# 3.1 Jetpack Compose (Modern UI - Primary Focus)

## Compose Fundamentals

### Declarative UI Paradigm

**Theory:**
Jetpack Compose introduces a **declarative programming model** for building Android UIs, fundamentally different from the traditional imperative approach.

**Imperative vs Declarative:**
- **Imperative (Traditional Views):** You write step-by-step instructions telling the system *how* to update the UI. You manually find views, update their properties, add/remove them from the hierarchy.
- **Declarative (Compose):** You describe *what* the UI should look like for a given state. When the state changes, the framework automatically figures out what needs to update.

**Key Benefits:**
1. **Less Code:** No need for findViewById, XML layouts, or manual view updates
2. **Intuitive:** UI naturally reflects the current state - no synchronization bugs
3. **Predictable:** Same input always produces the same output
4. **Composable:** Small, reusable UI components that combine together

**Mental Model:**
Think of it as: `UI = f(state)` - Your UI is a pure function of your application state. When state changes, the UI automatically re-renders to reflect that change.

```kotlin
// Declarative: Describe the UI state
@Composable
fun Greeting(name: String) {
    Text(text = "Hello, $name!")
}

// vs Imperative (Traditional):
// textView.setText("Hello, $name!")
```

---

### Composable Functions and @Composable Annotation

**Theory:**
Composable functions are the building blocks of Compose UI. They are regular Kotlin functions marked with the `@Composable` annotation.

**Key Characteristics:**
1. **Don't return UI:** Unlike traditional functions, composables don't return a View. They *emit* UI elements into the composition tree.
2. **Can call other composables:** A composable can only be called from within another composable function.
3. **May be called frequently:** The Compose runtime may call your composable function multiple times (recomposition).
4. **Should be side-effect free:** The function body should not modify external state; use side-effect APIs instead.
5. **Order independent within siblings:** Compose may execute sibling composables in any order or in parallel.

**Naming Convention:**
- Composable functions should be named using **PascalCase** (like classes)
- They typically describe *what* they display, not *how* they behave
- Example: `UserProfile`, `MessageList`, `SettingsScreen`

**The @Composable Annotation:**
This annotation tells the Compose compiler to transform this function. The compiler generates special code to track the composition, enable smart recomposition, and manage the UI tree.

```kotlin
@Composable
fun UserProfile(user: User) {
    Column {
        Text(user.name)
        Text(user.email)
    }
}
```

---

### State Management

**Theory:**
State in Compose refers to any value that can change over time and affects what the UI displays. Proper state management is crucial for building reactive, efficient UIs.

**Core Concepts:**

**1. `remember`:**
- Stores a value in the Composition (the UI tree)
- The value survives **recomposition** (UI rebuilds)
- Gets reset when the composable leaves the composition (e.g., navigating away)
- Use for UI state that doesn't need to survive configuration changes

**2. `mutableStateOf`:**
- Creates an observable state holder
- When the value changes, all composables reading it will recompose
- Works with Compose's snapshot system for thread-safe state access

**3. `rememberSaveable`:**
- Like `remember`, but survives **configuration changes** (rotation, theme change)
- Uses the saved instance state mechanism
- Only works with types that can be saved to a Bundle (primitives, Parcelable, etc.)

**Property Delegation (`by` keyword):**
Using `by` with `remember { mutableStateOf() }` allows direct access to the value without `.value`:
- Without `by`: `val count = remember { mutableStateOf(0) }` → access via `count.value`
- With `by`: `var count by remember { mutableStateOf(0) }` → access directly as `count`

```kotlin
// remember - survives recomposition
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}

// rememberSaveable - survives configuration changes
@Composable
fun InputField() {
    var text by rememberSaveable { mutableStateOf("") }
    TextField(value = text, onValueChange = { text = it })
}

// mutableStateOf types
val intState = mutableIntStateOf(0)
val floatState = mutableFloatStateOf(0f)
val listState = mutableStateListOf<String>()
val mapState = mutableStateMapOf<String, Int>()
```

---

### Recomposition

**Theory:**
Recomposition is the process of calling composable functions again when state changes. It's how Compose updates the UI.

**How It Works:**
1. When you read a `State` value inside a composable, Compose automatically tracks this dependency
2. When that state value changes, Compose schedules a recomposition
3. During recomposition, Compose re-executes the composable function
4. Compose compares the new UI tree with the old one and only updates what changed

**Smart Recomposition:**
Compose is intelligent about what it recomposes:
- Only composables that read the changed state will recompose
- Sibling composables that don't read the changed state are skipped
- This is why it's important to read state values as late as possible (called "state reads at the leaf")

**Recomposition Scope:**
The smallest unit that can be recomposed is typically a lambda or composable function call. Compose tries to minimize the scope of recomposition.

**Things to Avoid:**
- Expensive computations in the composable body (use `remember` or `derivedStateOf`)
- Side effects in the composable body (use `LaunchedEffect`, `SideEffect`)
- Creating new objects unnecessarily (lambdas, lists) - can cause unnecessary recompositions

```kotlin
@Composable
fun ParentComposable() {
    var count by remember { mutableStateOf(0) }
    
    // Only Button recomposes when count changes
    Text("Static text") // Skipped during recomposition
    Button(onClick = { count++ }) {
        Text("Count: $count") // Recomposes
    }
}
```

---

### CompositionLocal

**Theory:**
CompositionLocal provides a way to pass data implicitly through the composition tree, without explicitly passing it as parameters through every composable.

**Use Cases:**
- Theming (colors, typography, shapes)
- Context-like data (locale, configuration)
- Dependencies that many composables need but shouldn't clutter parameter lists

**Types of CompositionLocal:**
1. **`compositionLocalOf`:** Creates a CompositionLocal that triggers recomposition when the value changes. Most common choice.
2. **`staticCompositionLocalOf`:** Creates a CompositionLocal that does NOT trigger recomposition on change. Use for values that rarely/never change (better performance).

**How It Works:**
1. **Define:** Create a CompositionLocal with a default value (or error)
2. **Provide:** Use `CompositionLocalProvider` to supply a value for a subtree
3. **Consume:** Access the value via `.current` property

**Built-in CompositionLocals:**
- `LocalContext` - Android Context
- `LocalLifecycleOwner` - Current lifecycle owner
- `LocalConfiguration` - Device configuration
- `LocalDensity` - Screen density for dp/px conversions

```kotlin
// Define
val LocalUserSession = compositionLocalOf<UserSession> { 
    error("No UserSession provided") 
}

// Provide
CompositionLocalProvider(LocalUserSession provides userSession) {
    ChildComposable()
}

// Consume
@Composable
fun ChildComposable() {
    val session = LocalUserSession.current
    Text("User: ${session.userName}")
}
```

---

### Side Effects

**Theory:**
Side effects are operations that escape the scope of a composable function - things like network calls, database operations, logging, or subscribing to external data sources. Since composables can recompose frequently and unpredictably, side effects need special handling.

**Why Special Handling?**
- Composables should be pure functions (same input → same output)
- Composables may be called multiple times, in parallel, or skipped
- Some operations should only run once, or when specific values change

**Side Effect APIs:**

**1. `LaunchedEffect(key)`:**
- Runs a suspend function when entering composition
- Cancels and restarts when key(s) change
- Auto-cancels when leaving composition
- Use for: API calls, animations, one-time events

**2. `DisposableEffect(key)`:**
- For effects that need cleanup (subscribe/unsubscribe pattern)
- Must provide an `onDispose` block
- Use for: Listeners, observers, callbacks

**3. `SideEffect`:**
- Runs after every successful recomposition
- No coroutine scope, no keys
- Use for: Logging, analytics, sharing state with non-Compose code

**4. `derivedStateOf`:**
- Creates a state that's computed from other states
- Only recomputes when dependencies change
- Reduces unnecessary recompositions
- Use for: Filtered lists, computed values

**5. `rememberCoroutineScope`:**
- Provides a coroutine scope tied to the composition
- Use for: Launching coroutines from callbacks (onClick, etc.)

**6. `rememberUpdatedState`:**
- Captures a value that should always be current in a long-running lambda
- Use for: Callbacks in LaunchedEffect that shouldn't restart on change

```kotlin
// LaunchedEffect - runs suspend functions, restarts on key change
@Composable
fun FetchData(userId: String) {
    var user by remember { mutableStateOf<User?>(null) }
    
    LaunchedEffect(userId) {
        user = api.fetchUser(userId)
    }
}

// DisposableEffect - cleanup when leaving composition
@Composable
fun LifecycleObserver(lifecycle: Lifecycle) {
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

// SideEffect - runs after every successful recomposition
@Composable
fun Analytics(screenName: String) {
    SideEffect {
        analytics.logScreenView(screenName)
    }
}

// derivedStateOf - compute value from other states
@Composable
fun FilteredList(items: List<String>, query: String) {
    val filteredItems by remember(items, query) {
        derivedStateOf { items.filter { it.contains(query) } }
    }
}
```

---

## Compose Layouts

### Column, Row, Box

**Theory:**
These are the three fundamental layout composables in Compose. They determine how child elements are arranged on screen.

**Column:**
- Arranges children **vertically** (top to bottom)
- Similar to LinearLayout with vertical orientation
- Children are placed one below another by default
- **`verticalArrangement`:** How to distribute vertical space (Top, Bottom, Center, SpaceEvenly, SpaceBetween, SpaceAround)
- **`horizontalAlignment`:** How to align children horizontally (Start, End, CenterHorizontally)

**Row:**
- Arranges children **horizontally** (left to right)
- Similar to LinearLayout with horizontal orientation
- Children are placed side by side by default
- **`horizontalArrangement`:** How to distribute horizontal space
- **`verticalAlignment`:** How to align children vertically (Top, Bottom, CenterVertically)

**Box:**
- **Stacks** children on top of each other (z-axis)
- Similar to FrameLayout
- Last child is drawn on top (painter's algorithm)
- **`contentAlignment`:** Default alignment for all children
- Children can override alignment with `Modifier.align()`

**When to Use:**
- **Column:** Vertical lists, forms, stacked content
- **Row:** Horizontal buttons, toolbars, inline elements
- **Box:** Overlays, badges, floating elements, custom positioning

```kotlin
// Column - vertical arrangement
Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceEvenly,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text("Item 1")
    Text("Item 2")
}

// Row - horizontal arrangement
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Text("Left")
    Text("Right")
}

// Box - stack children
Box(
    modifier = Modifier.size(200.dp),
    contentAlignment = Alignment.Center
) {
    Image(painter = painterResource(R.drawable.bg), contentDescription = null)
    Text("Overlay") // On top of image
}
```

---

### Modifier System

**Theory:**
Modifiers are Compose's way of decorating or augmenting composables. They form a chain that wraps around the composable, adding behavior and appearance.

**Key Concepts:**

**1. Chaining:**
Modifiers are chained using dot notation. Each modifier wraps the previous one, creating layers of decoration.

**2. Order Matters (Critical!):**
Modifiers are applied **outside-in** (from first to last in the chain):
- The first modifier is the outermost layer
- Each subsequent modifier wraps inside the previous one
- This affects how padding, background, clickable areas, etc. interact

**3. Common Modifier Categories:**
- **Size:** `size()`, `width()`, `height()`, `fillMaxWidth()`, `fillMaxSize()`, `wrapContentSize()`
- **Padding/Margin:** `padding()` - Compose doesn't have margins; use padding on parent
- **Background/Border:** `background()`, `border()`, `clip()`
- **Interaction:** `clickable()`, `draggable()`, `scrollable()`
- **Drawing:** `drawBehind()`, `drawWithContent()`, `alpha()`, `rotate()`, `scale()`
- **Layout:** `offset()`, `align()`, `weight()` (in Row/Column)

**4. Modifier.then():**
Used to conditionally chain modifiers or combine modifier variables.

**Best Practices:**
- Always accept `modifier` as a parameter in reusable composables
- Apply the passed modifier first, then add your own
- Keep modifier chains readable - extract complex chains to variables

```kotlin
// Different results based on order
Text(
    text = "Hello",
    modifier = Modifier
        .padding(16.dp)      // Add padding first
        .background(Color.Red) // Then background (padding area not colored)
)

Text(
    text = "Hello",
    modifier = Modifier
        .background(Color.Red) // Background first
        .padding(16.dp)        // Padding inside background (all colored)
)

// Common modifiers
Modifier
    .size(100.dp)
    .width(50.dp)
    .height(80.dp)
    .fillMaxWidth()
    .fillMaxHeight()
    .fillMaxSize()
    .padding(8.dp)
    .padding(horizontal = 16.dp, vertical = 8.dp)
    .background(Color.Blue, shape = RoundedCornerShape(8.dp))
    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
    .clickable { /* action */ }
    .clip(CircleShape)
    .alpha(0.5f)
    .rotate(45f)
    .scale(1.5f)
```

---

### Spacer and Weight

**Theory:**

**Spacer:**
A blank composable that takes up space. It has no visual representation but affects layout. Essential for creating gaps between elements or pushing elements apart.

**Weight Modifier:**
Available only inside `Row` and `Column`. Distributes remaining space among weighted children proportionally.

**How Weight Works:**
1. Non-weighted children are measured first and take their natural size
2. Remaining space is divided among weighted children based on their weight values
3. A child with `weight(2f)` gets twice as much space as one with `weight(1f)`

**Common Patterns:**
- `Spacer(Modifier.weight(1f))` - Push elements to opposite ends
- Equal distribution - Give same weight to all children
- Proportional - Different weights for different proportions

```kotlin
Row(modifier = Modifier.fillMaxWidth()) {
    Text("Start")
    Spacer(modifier = Modifier.weight(1f)) // Takes remaining space
    Text("End")
}

Row(modifier = Modifier.fillMaxWidth()) {
    Text("1", modifier = Modifier.weight(1f)) // 1/3 of width
    Text("2", modifier = Modifier.weight(2f)) // 2/3 of width
}
```

---

### ConstraintLayout in Compose

**Theory:**
ConstraintLayout allows you to create complex layouts with flat view hierarchies by defining constraints between elements.

**Why Use ConstraintLayout?**
- Complex positioning that's hard with Row/Column/Box
- Aligning elements relative to each other
- Percentage-based positioning
- Chains for distributing elements evenly
- Barriers for dynamic boundaries

**Key Concepts:**

**1. References:**
Create references using `createRefs()` or `createRef()`. These identify elements for constraints.

**2. Constraints:**
Use `constrainAs()` modifier to define constraints. Link edges using `linkTo()`.

**3. Chains:**
Group elements to distribute space. Chain styles:
- **Spread:** Elements distributed evenly
- **SpreadInside:** First and last elements at edges
- **Packed:** Elements packed together

**4. Guidelines:**
Invisible lines at fixed positions or percentages for aligning elements.

**5. Barriers:**
Invisible lines that move based on the edges of referenced elements. Useful when you don't know which element will be larger.

```kotlin
@Composable
fun ConstraintLayoutExample() {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (button, text, image) = createRefs()
        
        Button(
            onClick = {},
            modifier = Modifier.constrainAs(button) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Text("Button")
        }
        
        Text(
            text = "Below Button",
            modifier = Modifier.constrainAs(text) {
                top.linkTo(button.bottom, margin = 8.dp)
                centerHorizontallyTo(parent)
            }
        )
        
        // Chains
        val (item1, item2, item3) = createRefs()
        createHorizontalChain(item1, item2, item3, chainStyle = ChainStyle.Spread)
        
        // Guidelines
        val guideline = createGuidelineFromStart(0.3f)
        
        // Barriers
        val barrier = createEndBarrier(button, text)
    }
}
```

---

### Custom Layouts

**Theory:**
When Column, Row, and Box don't meet your needs, you can create custom layouts using the `Layout` composable.

**The Layout Composable:**
Takes content and a measure policy. You control exactly how children are measured and placed.

**Measurement Process:**
1. **Measure Phase:** Measure each child with constraints, get Placeables
2. **Layout Phase:** Decide the layout size and position each Placeable

**Constraints:**
Define min/max width and height. Children must fit within constraints.

**Placeables:**
Measured children. Have fixed width and height. Can be placed at any position.

**Key Rules:**
- Measure each child exactly once
- Children can be measured in any order
- Place children within the layout method's scope

```kotlin
@Composable
fun CustomLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        // Measure children
        val placeables = measurables.map { it.measure(constraints) }
        
        // Calculate layout size
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.sumOf { it.height }
        
        layout(width, height) {
            var yPosition = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x = 0, y = yPosition)
                yPosition += placeable.height
            }
        }
    }
}
```

---

### SubcomposeLayout

**Theory:**
SubcomposeLayout allows you to compose children during the layout phase, after you know the sizes of other children. This enables layouts where one element's size depends on another's measured size.

**Use Cases:**
- Measure one item, then size another based on it
- Create items dynamically based on available space
- Deferred composition for performance (only compose visible items)

**How It Works:**
1. Use `subcompose(slotId)` to compose children with a unique key
2. Measure the composed content
3. Use measured sizes to compose/measure dependent content
4. Place all Placeables

**Performance Note:**
SubcomposeLayout has overhead compared to regular Layout. Only use when necessary (when children truly depend on each other's sizes).

```kotlin
@Composable
fun SubcomposeLayoutExample() {
    SubcomposeLayout { constraints ->
        // Measure dependent content based on other content's size
        val mainPlaceables = subcompose("main") {
            Text("Main Content")
        }.map { it.measure(constraints) }
        
        val mainHeight = mainPlaceables.maxOfOrNull { it.height } ?: 0
        
        val dependentPlaceables = subcompose("dependent") {
            Box(modifier = Modifier.height(mainHeight.toDp()))
        }.map { it.measure(constraints) }
        
        layout(constraints.maxWidth, mainHeight) {
            mainPlaceables.forEach { it.placeRelative(0, 0) }
        }
    }
}
```

---

## Compose UI Components

### Text Components

**Theory:**
Text is fundamental to any UI. Compose provides rich text capabilities through the `Text` composable and related components.

**Text Composable:**
Displays static or dynamic text with extensive styling options.

**Key Properties:**
- **text:** The string to display (can be String or AnnotatedString)
- **style:** Predefined style from MaterialTheme.typography
- **color, fontSize, fontWeight, fontStyle:** Override individual style properties
- **textAlign:** Alignment within the Text's bounds
- **overflow:** How to handle text that doesn't fit (Clip, Ellipsis, Visible)
- **maxLines/minLines:** Constrain the number of lines

**AnnotatedString:**
For styled text with multiple styles in one string. Use `buildAnnotatedString` to create rich text with different colors, styles, or clickable sections.

**TextField vs OutlinedTextField:**
- **TextField:** Material Design filled text field with background
- **OutlinedTextField:** Text field with outline border
- Both are stateless - you must provide value and onValueChange

**Keyboard Options:**
Control the keyboard type (email, number, password) and IME action (done, next, search).

```kotlin
// Basic Text
Text(
    text = "Hello World",
    color = Color.Blue,
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Italic,
    textAlign = TextAlign.Center,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
    style = MaterialTheme.typography.headlineMedium
)

// Annotated String (styled text)
Text(
    buildAnnotatedString {
        withStyle(SpanStyle(color = Color.Red)) {
            append("Red ")
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append("Bold")
        }
    }
)

// TextField
var text by remember { mutableStateOf("") }
TextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Enter name") },
    placeholder = { Text("Placeholder") },
    leadingIcon = { Icon(Icons.Default.Person, null) },
    trailingIcon = { Icon(Icons.Default.Clear, null) },
    isError = text.isEmpty(),
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Done
    ),
    keyboardActions = KeyboardActions(
        onDone = { /* Handle done */ }
    ),
    singleLine = true
)

// OutlinedTextField
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Email") },
    shape = RoundedCornerShape(12.dp)
)
```

---

### Buttons

**Theory:**
Buttons are interactive elements that trigger actions when clicked. Material Design 3 provides several button variants for different emphasis levels.

**Button Hierarchy (by emphasis):**
1. **Filled Button (Button):** Highest emphasis, primary actions
2. **FilledTonalButton:** Medium-high emphasis, secondary important actions
3. **ElevatedButton:** Medium emphasis, needs slight elevation
4. **OutlinedButton:** Medium emphasis, alternative to filled
5. **TextButton:** Lowest emphasis, tertiary actions

**IconButton:**
A button that displays only an icon. Used for toolbars, compact actions.

**FloatingActionButton (FAB):**
A prominent button for the primary action of a screen. Floats above content.
- **FAB:** Circular or rounded square
- **ExtendedFloatingActionButton:** Includes icon and text label

**Button Content:**
Buttons use a Row internally, so you can add icons alongside text using `Spacer` for spacing.

```kotlin
// Button
Button(
    onClick = { /* action */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = Color.Blue,
        contentColor = Color.White
    ),
    shape = RoundedCornerShape(8.dp),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
) {
    Icon(Icons.Default.Add, contentDescription = null)
    Spacer(Modifier.width(8.dp))
    Text("Add Item")
}

// Other button variants
OutlinedButton(onClick = {}) { Text("Outlined") }
TextButton(onClick = {}) { Text("Text Button") }
ElevatedButton(onClick = {}) { Text("Elevated") }
FilledTonalButton(onClick = {}) { Text("Tonal") }

// IconButton
IconButton(onClick = {}) {
    Icon(Icons.Default.Favorite, contentDescription = "Like")
}

// FloatingActionButton
FloatingActionButton(onClick = {}) {
    Icon(Icons.Default.Add, contentDescription = "Add")
}

ExtendedFloatingActionButton(
    onClick = {},
    icon = { Icon(Icons.Default.Add, null) },
    text = { Text("Create") }
)
```

---

### Images

**Theory:**
Images are loaded and displayed using the `Image` composable for local resources or `AsyncImage` (from Coil) for remote URLs.

**Image Composable:**
For local drawable resources. Uses a `Painter` to draw the image.

**ContentScale:**
How the image fits within its bounds:
- **Crop:** Scale to fill bounds, cropping if necessary
- **Fit:** Scale to fit within bounds, may have empty space
- **FillBounds:** Stretch to fill bounds exactly (may distort)
- **Inside:** Scale down only if larger than bounds
- **None:** No scaling

**contentDescription:**
Accessibility description for screen readers. Use `null` for decorative images.

**AsyncImage (Coil):**
For loading images from URLs. Handles:
- Asynchronous loading
- Memory and disk caching
- Placeholder and error states
- Crossfade animations

```kotlin
// Local image resource
Image(
    painter = painterResource(id = R.drawable.image),
    contentDescription = "Description",
    contentScale = ContentScale.Crop,
    modifier = Modifier
        .size(100.dp)
        .clip(CircleShape)
)

// AsyncImage (Coil library)
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data("https://example.com/image.jpg")
        .crossfade(true)
        .build(),
    contentDescription = "Profile",
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error),
    contentScale = ContentScale.Crop,
    modifier = Modifier.size(128.dp)
)
```

---

### Card and Surface

**Theory:**
Card and Surface are container composables that provide Material Design elevation and theming.

**Surface:**
The foundation for Material components. Provides:
- Background color from theme
- Shape clipping
- Elevation (shadow) and tonal elevation (color shift)
- Content color propagation to children

**Card:**
A Surface with predefined styling for content containers. Material Design guidance:
- Use for related, grouped content
- Should be elevated above the surface behind it
- Can be interactive (clickable)

**Card Variants:**
- **Card:** Basic card with default elevation
- **ElevatedCard:** Card with more prominent shadow
- **OutlinedCard:** Card with outline instead of elevation

**Tonal Elevation:**
In Material 3, elevation can also affect color (higher elevation = lighter tone in light theme). This is separate from shadow elevation.

```kotlin
Card(
    modifier = Modifier.padding(8.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Card Title", style = MaterialTheme.typography.titleLarge)
        Text("Card content goes here")
    }
}

// Clickable card
ElevatedCard(onClick = { /* action */ }) {
    Text("Clickable Card")
}

Surface(
    modifier = Modifier.size(200.dp),
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    tonalElevation = 4.dp
) {
    // Content
}
```

---

### Lists (Lazy Components)

**Theory:**
Lazy components only compose and lay out items that are currently visible on screen. Essential for displaying large datasets efficiently.

**LazyColumn vs Column:**
- **Column:** Composes ALL children immediately - bad for long lists
- **LazyColumn:** Only composes visible items + a small buffer
- LazyColumn is like RecyclerView for Compose

**LazyRow:**
Horizontal equivalent of LazyColumn.

**LazyVerticalGrid / LazyHorizontalGrid:**
Grid layouts with lazy composition.

**LazyVerticalStaggeredGrid:**
Pinterest-style grid where items can have different heights.

**Key Concepts:**

**1. DSL Functions:**
- `item { }` - Single item
- `items(list) { }` - Multiple items from a list
- `itemsIndexed(list) { index, item -> }` - Items with index

**2. Keys:**
Providing stable keys helps Compose:
- Maintain scroll position
- Preserve item state
- Efficiently animate changes
- Always provide keys for items that can change order or be modified

**3. Content Padding:**
Padding around the entire list content. Items scroll behind this padding (edge-to-edge scrolling).

**4. Arrangement:**
Spacing between items using `Arrangement.spacedBy()`.

**5. Sticky Headers:**
Headers that stick to the top while their section is visible.

```kotlin
// LazyColumn - vertical scrolling list
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    // Single item
    item { Text("Header") }
    
    // Multiple items
    items(itemsList) { item ->
        ListItem(item)
    }
    
    // Items with keys for better performance
    items(
        items = itemsList,
        key = { it.id }
    ) { item ->
        ListItem(item)
    }
    
    // Indexed items
    itemsIndexed(itemsList) { index, item ->
        Text("$index: ${item.name}")
    }
}

// LazyRow - horizontal scrolling
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(images) { image ->
        ImageCard(image)
    }
}

// LazyVerticalGrid
LazyVerticalGrid(
    columns = GridCells.Fixed(2), // or GridCells.Adaptive(minSize = 128.dp)
    contentPadding = PaddingValues(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(products) { product ->
        ProductCard(product)
    }
}

// LazyVerticalStaggeredGrid
LazyVerticalStaggeredGrid(
    columns = StaggeredGridCells.Fixed(2),
    verticalItemSpacing = 8.dp,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(images) { ImageCard(it) }
}

// Sticky headers
LazyColumn {
    groupedItems.forEach { (category, items) ->
        stickyHeader {
            Text(category, modifier = Modifier.background(Color.LightGray))
        }
        items(items) { item -> ListItem(item) }
    }
}
```

---

### Scaffold

**Theory:**
Scaffold is a layout composable that implements the basic Material Design visual layout structure. It provides slots for common app components.

**Scaffold Slots:**
- **topBar:** Top app bar area
- **bottomBar:** Bottom navigation or bottom app bar
- **floatingActionButton:** FAB, typically bottom-right
- **floatingActionButtonPosition:** FAB position (Center, End)
- **snackbarHost:** Container for snackbars
- **content:** Main content area (receives PaddingValues)

**Important: PaddingValues**
Scaffold provides `paddingValues` to the content lambda. You MUST apply this padding to your content to avoid overlap with top/bottom bars.

**TopAppBar Variants:**
- **TopAppBar:** Standard height, fixed
- **CenterAlignedTopAppBar:** Title centered
- **MediumTopAppBar:** Larger title, collapses on scroll
- **LargeTopAppBar:** Even larger title

**Scroll Behavior:**
TopAppBars can react to scrolling:
- `pinnedScrollBehavior()` - Always visible
- `enterAlwaysScrollBehavior()` - Hides on scroll down, shows on scroll up
- `exitUntilCollapsedScrollBehavior()` - Collapses to pinned state

```kotlin
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My App") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, "Add")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        // Main content
        Box(modifier = Modifier.padding(paddingValues)) {
            Text("Content")
        }
    }
}

// CenterAlignedTopAppBar
CenterAlignedTopAppBar(title = { Text("Centered Title") })

// LargeTopAppBar with scroll behavior
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
LargeTopAppBar(
    title = { Text("Large Title") },
    scrollBehavior = scrollBehavior
)
```

---

### Navigation Drawer

**Theory:**
Navigation drawers provide access to destinations and app functionality from the side of the screen.

**Types:**
- **ModalNavigationDrawer:** Slides over content, dims background (most common)
- **PermanentNavigationDrawer:** Always visible (tablets, desktops)
- **DismissibleNavigationDrawer:** Pushes content aside

**Components:**
- **DrawerState:** Controls open/closed state programmatically
- **ModalDrawerSheet:** The container for drawer content
- **NavigationDrawerItem:** Individual menu items

**Gesture Handling:**
Modal drawers support swipe-to-open gesture by default. Can be disabled with `gesturesEnabled = false`.

```kotlin
@Composable
fun DrawerExample() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Header", modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {}
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("App") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) { Text("Content") }
        }
    }
}
```

---

### Dialogs

**Theory:**
Dialogs interrupt the user with urgent information, details, or actions. They appear above all other content.

**Types:**

**AlertDialog:**
Pre-built dialog for simple alerts with title, text, and buttons. Use for:
- Confirmations ("Delete this item?")
- Simple choices
- Important messages

**Dialog:**
Custom dialog container. Use when AlertDialog's structure doesn't fit your needs.

**Popup:**
Lightweight overlay that appears relative to another element. Unlike dialogs:
- Doesn't dim background
- Doesn't capture focus
- For tooltips, dropdowns, context menus

**Dialog Behavior:**
- `onDismissRequest`: Called when user tries to dismiss (back press, outside click)
- Modal: Blocks interaction with content behind
- Should be used sparingly - too many dialogs disrupt user flow

```kotlin
// AlertDialog
var showDialog by remember { mutableStateOf(false) }

if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Delete Item?") },
        text = { Text("This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = { 
                // Delete action
                showDialog = false 
            }) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("Cancel")
            }
        }
    )
}

// Custom Dialog
Dialog(onDismissRequest = { showDialog = false }) {
    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Custom Dialog")
            // Custom content
        }
    }
}

// Popup
var showPopup by remember { mutableStateOf(false) }
Box {
    Button(onClick = { showPopup = true }) { Text("Show Popup") }
    
    if (showPopup) {
        Popup(
            alignment = Alignment.TopStart,
            offset = IntOffset(0, 50),
            onDismissRequest = { showPopup = false }
        ) {
            Card { Text("Popup content", Modifier.padding(8.dp)) }
        }
    }
}
```

---

### Tabs and HorizontalPager

**Theory:**
Tabs organize content across different screens, data sets, or functional aspects. HorizontalPager provides swipeable pages.

**TabRow:**
Container for tabs. Shows all tabs in a row with an indicator for the selected tab.
- **TabRow:** Fixed tabs - all visible, equal width
- **ScrollableTabRow:** Scrollable tabs - for many tabs or long labels

**Tab:**
Individual tab item. Can contain icon, text, or both.

**HorizontalPager:**
Swipeable container that shows one page at a time. The Compose equivalent of ViewPager.

**Integration Pattern:**
Tabs and Pager often work together:
1. TabRow shows tab buttons
2. HorizontalPager shows page content
3. Selecting a tab scrolls the pager
4. Swiping the pager updates the selected tab

**PagerState:**
Holds pager state including current page. Use `rememberPagerState()` to create.

```kotlin
@Composable
fun TabsExample() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tab 1", "Tab 2", "Tab 3")
    
    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content
        when (selectedTab) {
            0 -> Tab1Content()
            1 -> Tab2Content()
            2 -> Tab3Content()
        }
    }
}

// HorizontalPager (ViewPager alternative)
@Composable
fun PagerExample() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    Column {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
        ) {
            // tabs
        }
        
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> Page1()
                1 -> Page2()
                2 -> Page3()
            }
        }
    }
    
    // Sync tabs with pager
    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab)
    }
}
```

---

## Material Design 3 in Compose

### Theme Customization

**Theory:**
Material Design 3 (Material You) brings dynamic color, new components, and updated design guidance. Compose implements M3 through the `MaterialTheme` composable.

**Theme Components:**

**1. Color Scheme:**
M3 uses a comprehensive color system with semantic roles:
- **Primary, Secondary, Tertiary:** Brand colors at different emphasis levels
- **Surface, Background:** Container colors
- **Error:** Error states
- **On* colors:** Content colors (text/icons) for each container

**2. Dynamic Colors (Material You):**
On Android 12+, colors can be derived from the user's wallpaper. Creates a personalized, cohesive experience.

**3. Typography:**
Predefined text styles organized by use case:
- Display (large), Headline (medium), Title (smaller)
- Body (reading), Label (buttons/captions)

**4. Shapes:**
Corner rounding for components at different sizes.

**Theme Function Pattern:**
Create a composable theme function that wraps your app. It sets up color scheme, typography, and shapes based on system settings (dark mode, dynamic colors).

```kotlin
// Color.kt
val md_theme_light_primary = Color(0xFF6200EE)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_secondary = Color(0xFF03DAC6)
// ... more colors

val md_theme_dark_primary = Color(0xFFBB86FC)
// ... dark colors

// Define color schemes
private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    secondary = md_theme_light_secondary,
    // ... all scheme colors
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    // ...
)

// Theme.kt
@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Material You
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

---

### Typography System

**Theory:**
Typography in Material 3 organizes text styles by their semantic purpose rather than just size.

**Type Scale Categories:**
1. **Display:** Largest, reserved for short, important text
2. **Headline:** Section headers, prominent text
3. **Title:** Smaller headers, list item titles
4. **Body:** Long-form reading, default text
5. **Label:** Buttons, captions, small text

**Size Variants:**
Each category has Large, Medium, Small variants (e.g., `headlineLarge`, `headlineMedium`, `headlineSmall`).

**Custom Fonts:**
Create a `FontFamily` with your font files and use it in your Typography definition.

**Usage:**
Access styles via `MaterialTheme.typography.headlineLarge`, etc.

```kotlin
// Type.kt
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)

// Usage
Text(
    text = "Headline",
    style = MaterialTheme.typography.headlineLarge
)
```

---

### Shapes Configuration

**Theory:**
Shapes define the corner rounding of Material components. M3 uses a consistent shape scale.

**Shape Scale:**
- **extraSmall:** 4dp - Chips, small badges
- **small:** 8dp - Buttons, text fields
- **medium:** 12dp - Cards, dialogs
- **large:** 16dp - FABs, large cards
- **extraLarge:** 24dp - Sheets, large surfaces

**Shape Types:**
- `RoundedCornerShape` - Rounded corners
- `CutCornerShape` - Cut (chamfered) corners
- `CircleShape` - Fully rounded (circle/pill)
- `RectangleShape` - No rounding

**Usage:**
Components automatically use appropriate shapes from the theme. Override with explicit shape parameter when needed.

```kotlin
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Usage
Card(shape = MaterialTheme.shapes.medium) { }
```

---

## State Management in Compose

### Unidirectional Data Flow

**Theory:**
Unidirectional Data Flow (UDF) is an architectural pattern where state flows in one direction and events flow in the opposite direction.

**The Pattern:**
```
[State Source (ViewModel)] → [State] → [UI (Composables)]
                          ↑                    ↓
                          └──── [Events] ←────┘
```

**Benefits:**
1. **Predictability:** State changes are traceable; you always know where state comes from
2. **Debugging:** Easy to follow the flow of data and events
3. **Testing:** State logic is isolated and testable
4. **Single Source of Truth:** One place owns the state, reducing inconsistencies

**Implementation:**
- State holder (ViewModel) exposes immutable state
- UI observes and displays state
- User interactions become events sent to state holder
- State holder processes events and emits new state

```kotlin
// State flows down, events flow up
@Composable
fun ParentScreen(viewModel: MyViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    ChildScreen(
        state = uiState,
        onAction = { action -> viewModel.handleAction(action) }
    )
}

@Composable
fun ChildScreen(
    state: UiState,
    onAction: (Action) -> Unit
) {
    // UI based on state
    Button(onClick = { onAction(Action.Submit) }) {
        Text("Submit")
    }
}
```

---

### State Hoisting

**Theory:**
State hoisting is a pattern of moving state up to make a composable stateless or to share state between composables.

**Why Hoist State?**
1. **Reusability:** Stateless composables work with any data source
2. **Testability:** Easy to test with predetermined state
3. **Separation of Concerns:** UI logic separate from state management
4. **Flexibility:** Parent controls the state behavior

**The Pattern:**
Move state variables out of a composable and replace with:
- **State parameter:** The current value
- **Event callbacks:** Functions to request state changes

**When to Hoist:**
- When multiple composables need the same state
- When a parent needs to know about state changes
- When you want a reusable, testable composable

**When NOT to Hoist:**
- Internal UI state that no other composable needs
- Temporary state during user interaction

```kotlin
// Stateless composable (hoisted state)
@Composable
fun Counter(
    count: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(onClick = onIncrement, modifier = modifier) {
        Text("Count: $count")
    }
}

// Stateful wrapper
@Composable
fun StatefulCounter() {
    var count by remember { mutableStateOf(0) }
    Counter(count = count, onIncrement = { count++ })
}
```

---

### ViewModel Integration

**Theory:**
ViewModel is the recommended state holder for screen-level composables. It survives configuration changes and scopes business logic.

**StateFlow vs LiveData:**
- **StateFlow:** Kotlin-first, null-safe, requires initial value, better for Compose
- **LiveData:** Lifecycle-aware, returns nullable, older approach

**Collecting State:**
- **`collectAsState()`:** Basic collection, doesn't pause when app is backgrounded
- **`collectAsStateWithLifecycle()`:** Lifecycle-aware, pauses collection when not active (recommended)

**One-Time Events:**
- Use `SharedFlow` for events that should only be handled once
- Navigate, show snackbar, play sound
- Collect in `LaunchedEffect`

**UiState Pattern:**
Encapsulate all screen state in a single data class for atomic updates and easier management.

```kotlin
class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getData()
            _uiState.update { it.copy(isLoading = false, data = result) }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    // Collect StateFlow with lifecycle awareness
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Collect SharedFlow for one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> { /* show snackbar */ }
                is UiEvent.Navigate -> { /* navigate */ }
            }
        }
    }
    
    if (uiState.isLoading) {
        CircularProgressIndicator()
    } else {
        DataContent(uiState.data)
    }
}
```

---

## Compose Animation

**Theory Overview:**
Compose provides a rich animation system designed for the declarative paradigm. Animations are state-driven: when state changes, animations automatically run.

**Animation Levels:**
1. **High-level APIs:** `animate*AsState`, `AnimatedVisibility` - easiest to use
2. **Mid-level APIs:** `Transition`, `AnimatedContent` - coordinated animations
3. **Low-level APIs:** `Animatable`, `Animation` - full control

### animate*AsState APIs

**Theory:**
The simplest animation API. Automatically animates between values when the target changes.

**Available Types:**
- `animateDpAsState` - Dp values (size, padding)
- `animateColorAsState` - Colors
- `animateFloatAsState` - Float values
- `animateIntAsState` - Integer values
- `animateOffsetAsState` - Offsets
- `animateSizeAsState` - Sizes

**How It Works:**
1. You provide a target value
2. When target changes, animation runs automatically
3. Returns the current animated value
4. Recomposition happens as value changes

**The `label` parameter:**
Used for debugging in Android Studio's composition traces.

```kotlin
// Animate simple values
var expanded by remember { mutableStateOf(false) }

val size by animateDpAsState(
    targetValue = if (expanded) 200.dp else 100.dp,
    animationSpec = tween(durationMillis = 300),
    label = "size"
)

val color by animateColorAsState(
    targetValue = if (expanded) Color.Red else Color.Blue,
    label = "color"
)

Box(
    modifier = Modifier
        .size(size)
        .background(color)
        .clickable { expanded = !expanded }
)
```

---

### AnimatedVisibility

**Theory:**
Animates the appearance and disappearance of content. Best for show/hide scenarios.

**Enter Transitions:**
- `fadeIn()` - Fade in
- `slideIn()` / `slideInVertically()` / `slideInHorizontally()` - Slide in
- `expandIn()` / `expandVertically()` / `expandHorizontally()` - Expand from size zero
- `scaleIn()` - Scale up from a smaller size

**Exit Transitions:**
Corresponding exit versions: `fadeOut()`, `slideOut()`, `shrinkOut()`, `scaleOut()`

**Combining Transitions:**
Use `+` to combine: `fadeIn() + slideInVertically()`

**Customization:**
- `initialOffset` / `targetOffset` for slide direction
- `expandFrom` / `shrinkTowards` for expand/shrink origin
- `animationSpec` for timing

```kotlin
var visible by remember { mutableStateOf(true) }

AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + slideInVertically(),
    exit = fadeOut() + slideOutVertically()
) {
    Text("Animated Content")
}

// Custom enter/exit
AnimatedVisibility(
    visible = visible,
    enter = expandVertically(
        expandFrom = Alignment.Top,
        animationSpec = tween(300)
    ) + fadeIn(),
    exit = shrinkVertically() + fadeOut()
) {
    Card { Text("Expandable Card") }
}
```

---

### AnimatedContent

**Theory:**
Animates between different content based on state. Unlike AnimatedVisibility, it's for when content changes (not just appears/disappears).

**Use Cases:**
- Counters incrementing/decrementing
- Switching between screens
- Content that morphs based on state

**TransitionSpec:**
Define how old content exits and new content enters. Access `initialState` and `targetState` to create directional animations.

**ContentKey:**
By default, content is keyed by target state. For complex state, you might need a custom key.

```kotlin
var count by remember { mutableIntStateOf(0) }

AnimatedContent(
    targetState = count,
    transitionSpec = {
        if (targetState > initialState) {
            slideInVertically { -it } + fadeIn() togetherWith
                slideOutVertically { it } + fadeOut()
        } else {
            slideInVertically { it } + fadeIn() togetherWith
                slideOutVertically { -it } + fadeOut()
        }
    },
    label = "counter"
) { target ->
    Text("Count: $target", style = MaterialTheme.typography.headlineLarge)
}
```

---

### Transition API

**Theory:**
For animating multiple values together when state changes. All animations are coordinated under a single transition.

**Benefits:**
- Multiple properties animate in sync
- Easy to add/remove animated properties
- Can have different specs per property
- Good for component-level animations

**updateTransition:**
Creates a transition that tracks a state value. Child animations automatically run when state changes.

```kotlin
var selected by remember { mutableStateOf(false) }

val transition = updateTransition(targetState = selected, label = "box")

val borderColor by transition.animateColor(label = "border") { state ->
    if (state) Color.Green else Color.Gray
}

val size by transition.animateDp(
    transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
    label = "size"
) { state ->
    if (state) 150.dp else 100.dp
}

Box(
    modifier = Modifier
        .size(size)
        .border(2.dp, borderColor)
        .clickable { selected = !selected }
)
```

---

### Animatable

**Theory:**
Low-level animation API for full control. Use when you need:
- Imperative animation triggering
- Snap to values
- Animation with velocity
- Complex choreographed sequences

**Key Methods:**
- `animateTo()` - Animate to a target value
- `snapTo()` - Immediately set value (no animation)
- `animateDecay()` - Animate with initial velocity, decaying to stop
- `stop()` - Cancel ongoing animation

**Coroutine-based:**
All Animatable methods are suspend functions. Run them in `LaunchedEffect` or a coroutine scope.

```kotlin
@Composable
fun ShakeAnimation() {
    val offset = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        offset.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 500
                0f at 0
                -10f at 100
                10f at 200
                -10f at 300
                10f at 400
                0f at 500
            }
        )
    }
    
    Box(modifier = Modifier.offset(x = offset.value.dp)) {
        Text("Shake me!")
    }
}
```

---

### Animation Specs

**Theory:**
Animation specs define the timing and physics of animations.

**Types:**

**1. Tween:**
Duration-based animation with easing. Predictable timing.
- `durationMillis`: How long
- `delayMillis`: Wait before starting
- `easing`: Curve (FastOutSlowIn, Linear, etc.)

**2. Spring:**
Physics-based animation. Natural, responsive feel.
- `dampingRatio`: How quickly oscillation settles (0 = no damping, 1 = critical damping)
- `stiffness`: Spring stiffness (higher = faster)

**3. Keyframes:**
Define specific values at specific times. Good for complex, precise sequences.

**4. Repeatable:**
Repeat a finite number of times.
- `iterations`: Number of repeats
- `repeatMode`: Restart or Reverse

**5. InfiniteRepeatable:**
Repeat forever (loading indicators, pulsing effects).

```kotlin
// Tween - duration-based
animationSpec = tween(
    durationMillis = 300,
    delayMillis = 50,
    easing = FastOutSlowInEasing
)

// Spring - physics-based
animationSpec = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

// Keyframes
animationSpec = keyframes {
    durationMillis = 1000
    0.5f at 200 using LinearEasing
    0.8f at 500
    1f at 1000
}

// Repeatable
animationSpec = repeatable(
    iterations = 3,
    animation = tween(500),
    repeatMode = RepeatMode.Reverse
)

// Infinite
animationSpec = infiniteRepeatable(
    animation = tween(1000),
    repeatMode = RepeatMode.Restart
)
```

---

## Compose Navigation

**Theory Overview:**
Navigation in Compose is handled by the Navigation component library with Compose-specific APIs. It manages the back stack, arguments, and screen transitions.

**Key Components:**
- **NavController:** Manages navigation state and back stack
- **NavHost:** Container composable that displays destinations
- **NavGraph:** Collection of destinations and routes

### Basic Navigation

**Theory:**
Routes are string paths that identify destinations. Arguments can be embedded in routes like URL path parameters.

**NavController:**
Central navigation coordinator. Create with `rememberNavController()`. Never pass NavController to composables that don't need it - instead, pass lambdas.

**NavHost:**
Composable container. Defines:
- Which NavController to use
- Start destination
- All navigation destinations via `composable()` calls

**Arguments:**
- Path arguments: `"details/{itemId}"` - Required, in the path
- Query arguments: `"details?itemId={itemId}"` - Optional, like URL query params

```kotlin
// NavHost setup
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToDetails = { id ->
                    navController.navigate("details/$id")
                }
            )
        }
        
        composable(
            route = "details/{itemId}",
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            DetailsScreen(itemId = itemId)
        }
    }
}
```

---

### Type-Safe Navigation (Compose Navigation 2.8+)

**Theory:**
Type-safe navigation uses Kotlin serialization to define routes as classes/objects instead of strings. Benefits:
- Compile-time route checking
- Type-safe arguments
- No string concatenation errors
- IDE autocomplete

**Route Types:**
- **Object routes:** For screens with no arguments (`object Home`)
- **Data class routes:** For screens with arguments (`data class Details(val id: Int)`)

**How It Works:**
1. Define routes as `@Serializable` classes/objects
2. Use `composable<RouteType>` in NavHost
3. Navigate with `navController.navigate(RouteInstance)`
4. Extract route with `backStackEntry.toRoute<RouteType>()`

```kotlin
// Define routes as serializable objects
@Serializable
object Home

@Serializable
data class Details(val itemId: Int)

@Serializable
object Settings

// NavHost with type-safe routes
NavHost(
    navController = navController,
    startDestination = Home
) {
    composable<Home> {
        HomeScreen(onNavigate = { navController.navigate(Details(itemId = 123)) })
    }
    
    composable<Details> { backStackEntry ->
        val details: Details = backStackEntry.toRoute()
        DetailsScreen(itemId = details.itemId)
    }
}
```

---

### Bottom Navigation Integration

**Theory:**
Bottom navigation is a common pattern for top-level destinations. Integration with Navigation requires:
- Tracking current destination for selection state
- Using proper navigation flags to avoid back stack buildup

**Navigation Flags:**
- `popUpTo()`: Clear screens up to (and optionally including) a destination
- `saveState`: Save the state of popped destinations
- `restoreState`: Restore state when returning
- `launchSingleTop`: Don't create duplicate if already at top

**Common Pattern:**
Pop up to start destination (saving state) and restore state. This creates the illusion of switching between persistent tabs.

```kotlin
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("search", "Search", Icons.Default.Search),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen() }
            composable("search") { SearchScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}
```

---

### Nested Navigation Graphs

**Theory:**
Nested graphs group related destinations together. Benefits:
- Encapsulation: Auth flow, onboarding, settings as separate graphs
- Reusability: Graphs can be defined separately and included
- Scoped ViewModels: Share ViewModel within a nested graph

**Navigation to Nested Graphs:**
Navigate to the nested graph's *route*, which automatically goes to its start destination.

**Scoped ViewModel:**
Use `hiltViewModel()` or `viewModel()` with the NavBackStackEntry of the parent graph to share ViewModel across nested screens.

```kotlin
NavHost(navController, startDestination = "main") {
    // Main graph
    navigation(startDestination = "home", route = "main") {
        composable("home") { HomeScreen() }
        composable("list") { ListScreen() }
    }
    
    // Auth graph
    navigation(startDestination = "login", route = "auth") {
        composable("login") { LoginScreen() }
        composable("register") { RegisterScreen() }
    }
    
    // Settings graph
    navigation(startDestination = "settings_main", route = "settings") {
        composable("settings_main") { SettingsScreen() }
        composable("settings_account") { AccountSettingsScreen() }
    }
}

// Navigate to nested graph
navController.navigate("auth") // Goes to login (start destination of auth)
```

---

## Advanced Compose

### Custom Modifiers

**Theory:**
Custom modifiers let you package reusable behavior and styling. Two approaches:

**1. Extension Function Modifier:**
Use `Modifier.then()` to chain with other modifiers. Stateless, simple.

**2. Composed Modifier:**
Use `Modifier.composed { }` when you need:
- State (`remember`)
- Side effects (`LaunchedEffect`)
- Composition locals

**composed vs then:**
- `then()` is more efficient but can't access composition
- `composed` creates state per usage site

```kotlin
// Extension function modifier
fun Modifier.customShadow(
    color: Color = Color.Black,
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp
) = this.then(
    Modifier.drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                this.color = color
                asFrameworkPaint().apply {
                    maskFilter = BlurMaskFilter(
                        blurRadius.toPx(),
                        BlurMaskFilter.Blur.NORMAL
                    )
                }
            }
            canvas.drawRoundRect(
                left = offsetX.toPx(),
                top = offsetY.toPx(),
                right = size.width + offsetX.toPx(),
                bottom = size.height + offsetY.toPx(),
                radiusX = borderRadius.toPx(),
                radiusY = borderRadius.toPx(),
                paint = paint
            )
        }
    }
)

// Composed modifier (creates state per modifier instance)
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    this.alpha(alpha)
}
```

---

### Custom Drawing with Canvas

**Theory:**
The `Canvas` composable provides a `DrawScope` for custom drawing. You have full control over pixels.

**DrawScope Functions:**
- `drawRect()`, `drawCircle()`, `drawOval()`
- `drawLine()`, `drawArc()`, `drawPath()`
- `drawText()` - Requires TextMeasurer
- `drawImage()` - Draw bitmaps

**Coordinate System:**
- Origin (0,0) is top-left
- X increases rightward
- Y increases downward
- Use `size.width` and `size.height` for bounds

**Colors and Paint:**
- Use Color for simple fills
- Use Brush for gradients
- Use drawBehind/drawWithContent for adding drawing to existing composables

```kotlin
@Composable
fun CustomChart(data: List<Float>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxValue = data.maxOrNull() ?: 1f
        val barWidth = size.width / data.size
        
        data.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height
            drawRect(
                color = Color.Blue,
                topLeft = Offset(index * barWidth, size.height - barHeight),
                size = Size(barWidth - 4.dp.toPx(), barHeight)
            )
        }
        
        // Draw line
        drawLine(
            color = Color.Red,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 2.dp.toPx()
        )
        
        // Draw circle
        drawCircle(
            color = Color.Green,
            radius = 20.dp.toPx(),
            center = center
        )
        
        // Draw arc
        drawArc(
            color = Color.Yellow,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(50.dp.toPx(), 50.dp.toPx()),
            size = Size(100.dp.toPx(), 100.dp.toPx())
        )
    }
}
```

---

### Gesture Handling

**Theory:**
Compose provides gesture detection at multiple levels:

**High-Level Modifiers:**
- `clickable` - Tap handling with ripple
- `combinedClickable` - Tap, double-tap, long-press
- `draggable` - Single-axis drag
- `scrollable` - Scroll gestures

**Mid-Level:**
- `swipeable` / `anchoredDraggable` - Drag with snap points
- `transformable` - Pinch, pan, rotate simultaneously

**Low-Level:**
- `pointerInput` - Raw pointer events, custom gestures
- `detectTapGestures`, `detectDragGestures`, etc.

**Interaction States:**
`InteractionSource` tracks interaction state (pressed, focused, dragged) for visual feedback.

```kotlin
// Clickable with ripple
Modifier.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = rememberRipple(bounded = true)
) { /* click action */ }

// Draggable
var offsetX by remember { mutableFloatStateOf(0f) }
Box(
    modifier = Modifier
        .offset { IntOffset(offsetX.roundToInt(), 0) }
        .draggable(
            orientation = Orientation.Horizontal,
            state = rememberDraggableState { delta ->
                offsetX += delta
            }
        )
)

// Swipeable (for swipe-to-dismiss, etc.)
val swipeableState = rememberSwipeableState(initialValue = 0)
Box(
    modifier = Modifier.swipeable(
        state = swipeableState,
        anchors = mapOf(0f to 0, 300f to 1),
        thresholds = { _, _ -> FractionalThreshold(0.5f) },
        orientation = Orientation.Horizontal
    )
)

// Transformable (pinch, pan, rotate)
var scale by remember { mutableFloatStateOf(1f) }
var rotation by remember { mutableFloatStateOf(0f) }
var offset by remember { mutableStateOf(Offset.Zero) }

Box(
    modifier = Modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            rotationZ = rotation
            translationX = offset.x
            translationY = offset.y
        }
        .transformable(
            state = rememberTransformableState { zoomChange, panChange, rotationChange ->
                scale *= zoomChange
                rotation += rotationChange
                offset += panChange
            }
        )
)

// Pointer input for custom gestures
Modifier.pointerInput(Unit) {
    detectTapGestures(
        onTap = { /* single tap */ },
        onDoubleTap = { /* double tap */ },
        onLongPress = { /* long press */ },
        onPress = { /* press started */ }
    )
}
```

---

### Performance Optimization

**Theory:**
Compose performance centers on minimizing unnecessary recomposition. Key principles:

**1. Stability:**
Compose skips recomposition for composables whose parameters haven't changed. Parameters must be *stable*.

**Stable Types:**
- Primitives (Int, String, Boolean)
- `@Stable` or `@Immutable` annotated classes
- Functions (lambdas)

**Unstable Types (cause recomposition):**
- `List`, `Map` (use `ImmutableList` from Kotlinx collections)
- Classes with `var` properties
- Classes Compose can't verify are stable

**2. Lambda Stability:**
Inline lambdas may cause recomposition issues. Solutions:
- Use `remember` to stabilize
- Use method references (`viewModel::action`)

**3. Keys in Lists:**
Always provide stable keys for `LazyColumn`/`LazyRow` items.

**4. Read State Late:**
Defer state reads to smaller scopes. Don't read state in parent if only child needs it.

**Tools:**
- Layout Inspector's recomposition counts
- Compose compiler metrics
- `@Stable` and `@Immutable` annotations

```kotlin
// Use remember for expensive calculations
val sortedList = remember(items) {
    items.sortedBy { it.name }
}

// Use key for LazyColumn items
LazyColumn {
    items(items, key = { it.id }) { item ->
        ItemRow(item)
    }
}

// Use derivedStateOf for computed values
val showButton by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}

// Stable classes for skipping recomposition
@Stable
data class UiState(
    val items: List<Item>,
    val isLoading: Boolean
)

// Immutable collections
@Immutable
data class ImmutableUiState(
    val items: ImmutableList<Item>
)

// Lambda stability - avoid inline lambdas
val onClick = remember { { viewModel.doAction() } }
Button(onClick = onClick) { }

// or use method reference
Button(onClick = viewModel::doAction) { }
```

---

### Testing Compose UI

**Theory:**
Compose provides a testing library built on semantics - accessibility-like properties that describe UI elements.

**Test Setup:**
Use `createComposeRule()` to get a test rule. This sets up the Compose testing environment.

**Finding Nodes:**
- `onNodeWithText("text")` - Find by displayed text
- `onNodeWithTag("tag")` - Find by test tag (set with `Modifier.testTag()`)
- `onNodeWithContentDescription("desc")` - Find by accessibility description
- `onAllNodesWithText()` - Find multiple matches

**Assertions:**
- `assertIsDisplayed()` - Visible on screen
- `assertExists()` - In the tree (may not be visible)
- `assertTextEquals("text")` - Text content
- `assertIsEnabled()` / `assertIsNotEnabled()`

**Actions:**
- `performClick()`
- `performTextInput("text")`
- `performScrollTo()` / `performScrollToIndex()`
- `performTouchInput { swipeLeft() }`

**Semantics Tree:**
Tests operate on the semantics tree, not the actual UI tree. Use `printToLog()` for debugging.

```kotlin
class MyComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testButtonClick() {
        composeTestRule.setContent {
            var count by remember { mutableStateOf(0) }
            Button(onClick = { count++ }) {
                Text("Count: $count")
            }
        }
        
        // Find and assert
        composeTestRule.onNodeWithText("Count: 0").assertIsDisplayed()
        
        // Perform action
        composeTestRule.onNodeWithText("Count: 0").performClick()
        
        // Assert after action
        composeTestRule.onNodeWithText("Count: 1").assertIsDisplayed()
    }
    
    @Test
    fun testTextField() {
        composeTestRule.setContent {
            var text by remember { mutableStateOf("") }
            TextField(value = text, onValueChange = { text = it })
        }
        
        composeTestRule
            .onNodeWithTag("textField")
            .performTextInput("Hello")
        
        composeTestRule
            .onNodeWithText("Hello")
            .assertIsDisplayed()
    }
    
    @Test
    fun testList() {
        composeTestRule.setContent {
            LazyColumn {
                items(100) { Text("Item $it") }
            }
        }
        
        composeTestRule
            .onNodeWithText("Item 0")
            .assertIsDisplayed()
        
        // Scroll to item
        composeTestRule
            .onNodeWithTag("list")
            .performScrollToIndex(50)
    }
}
```

---

# 3.2 Traditional Views (Legacy Support)

**Theory Overview:**
Traditional Views are the original Android UI system based on XML layouts and View classes. While Jetpack Compose is the modern approach, Views are still important for:
- Maintaining legacy codebases
- Interoperability with existing libraries
- Understanding Android's UI fundamentals

**Imperative vs Declarative:**
Traditional Views use an **imperative** approach - you explicitly tell the system *how* to change the UI step by step. This contrasts with Compose's *declarative* approach.

---

## XML Layouts

**Theory:**
XML layouts define the structure and appearance of your UI. The Android framework inflates XML into View objects at runtime.

**Inflation Process:**
1. XML is parsed at runtime
2. View objects are instantiated
3. View tree is constructed
4. Views are measured, laid out, and drawn

### LinearLayout

**Theory:**
LinearLayout arranges children in a single direction (horizontal or vertical). Simple and predictable, but can cause performance issues with deep nesting.

**Key Attributes:**
- **orientation:** `vertical` or `horizontal`
- **gravity:** Alignment of children within the layout
- **layout_gravity:** Alignment of the layout within its parent
- **layout_weight:** Proportional space distribution

**Performance Note:**
Nested LinearLayouts with weights trigger multiple measure passes. For complex layouts, prefer ConstraintLayout.

```xml
<!-- Vertical LinearLayout -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="16dp">
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Item 1" />
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Item 2"
        android:layout_marginTop="8dp" />
    
    <!-- Weight distribution -->
    <View
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
</LinearLayout>
```

---

### RelativeLayout

**Theory:**
RelativeLayout positions children relative to each other or to the parent. Allows complex layouts without nesting, but can be verbose.

**Positioning Types:**
1. **Relative to Parent:** `alignParentTop`, `alignParentStart`, `centerInParent`
2. **Relative to Siblings:** `above`, `below`, `toStartOf`, `toEndOf`
3. **Alignment:** `alignTop`, `alignBaseline`

**Deprecation Note:**
While still functional, ConstraintLayout is preferred for new development as it offers more features with similar or better performance.

```xml
<RelativeLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <TextView
        android:id="@+id/title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true" />
    
    <Button
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/title"
        android:layout_alignParentEnd="true" />
</RelativeLayout>
```

---

### ConstraintLayout

**Theory:**
ConstraintLayout is a powerful, flexible layout that allows flat view hierarchies for complex layouts. It's the recommended layout for most use cases.

**Key Concepts:**

**1. Constraints:**
Every view needs at least one horizontal and one vertical constraint. Constraints connect a view's edge to another view or the parent.

**2. Bias:**
When constrained on both sides, bias (0.0 to 1.0) determines positioning. Default is 0.5 (centered).

**3. Chains:**
Group views for collective positioning:
- **Spread:** Even distribution
- **Spread inside:** First/last at edges
- **Packed:** Views clustered together

**4. Guidelines:**
Invisible lines for positioning, defined by percentage or fixed position.

**5. Barriers:**
Invisible lines that move based on referenced views' sizes.

**6. Match Constraints (0dp):**
Special size that means "expand to match constraints" (similar to match_parent but respects constraints).

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <TextView
        android:id="@+id/title"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_margin="16dp" />
    
    <Button
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_constraintTop_toBottomOf="@id/title"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
    
    <!-- Chains -->
    <TextView
        android:id="@+id/item1"
        app:layout_constraintHorizontal_chainStyle="spread"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toStartOf="@id/item2" />
    
    <!-- Guidelines -->
    <androidx.constraintlayout.widget.Guideline
        android:id="@+id/guideline"
        android:orientation="vertical"
        app:layout_constraintGuide_percent="0.5" />
    
    <!-- Barriers -->
    <androidx.constraintlayout.widget.Barrier
        android:id="@+id/barrier"
        app:barrierDirection="end"
        app:constraint_referenced_ids="item1,item2" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

---

### ViewBinding

**Theory:**
ViewBinding generates a binding class for each XML layout, providing type-safe, null-safe access to views. It replaces `findViewById()`.

**Benefits over findViewById:**
1. **Null Safety:** Binding only includes views that exist in the layout
2. **Type Safety:** Views are already cast to the correct type
3. **Build-time Verification:** Errors caught at compile time
4. **Performance:** Single inflation, no runtime reflection

**Fragment Lifecycle Note:**
In Fragments, views can outlive the Fragment's view lifecycle. You must null out the binding in `onDestroyView()` to prevent memory leaks.

```kotlin
// build.gradle
android {
    buildFeatures {
        viewBinding = true
    }
}

// Activity
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.titleText.text = "Hello"
        binding.submitButton.setOnClickListener { /* action */ }
    }
}

// Fragment
class MyFragment : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

### DataBinding

**Theory:**
DataBinding extends ViewBinding with expression language support in XML. It allows binding UI components directly to data sources.

**Features:**
- **Expression Language:** Logic in XML (`@{user.firstName}`)
- **Two-way Binding:** `@={}` for input fields
- **Observable Data:** Automatic UI updates when data changes
- **Event Binding:** `@{() -> viewModel.onClick()}`

**Comparison with ViewBinding:**
- DataBinding is more powerful but adds complexity
- DataBinding increases build times more significantly
- ViewBinding is sufficient for most cases
- DataBinding enables MVVM pattern directly in XML

**Best Practice:**
Keep XML expressions simple. Complex logic belongs in ViewModel, not XML.

```kotlin
// build.gradle
android {
    buildFeatures {
        dataBinding = true
    }
}
```

```xml
<!-- layout.xml -->
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <data>
        <variable
            name="viewModel"
            type="com.example.MainViewModel" />
    </data>
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        <TextView
            android:text="@{viewModel.userName}"
            android:visibility="@{viewModel.isVisible ? View.VISIBLE : View.GONE}" />
        
        <Button
            android:onClick="@{() -> viewModel.onButtonClick()}" />
    </LinearLayout>
</layout>
```

```kotlin
// Activity
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }
}
```

---

### Include, Merge, ViewStub

**Theory:**
These tags help organize and optimize layouts.

**`<include>`:**
Reuse layouts in multiple places. The included layout becomes part of the parent hierarchy.

**`<merge>`:**
Eliminates redundant ViewGroups when including layouts. The merge tag's children are added directly to the parent.

**`<ViewStub>`:**
A zero-sized, invisible view that lazily inflates a layout. Use for rarely shown views (error states, empty states).

**ViewStub Benefits:**
- Zero cost until inflated
- Layout only parsed when needed
- Reduces initial layout complexity

```xml
<!-- reusable_header.xml -->
<merge xmlns:android="http://schemas.android.com/apk/res/android">
    <ImageView android:id="@+id/logo" />
    <TextView android:id="@+id/title" />
</merge>

<!-- main_layout.xml -->
<LinearLayout>
    <include layout="@layout/reusable_header" />
    
    <!-- ViewStub - lazy inflation -->
    <ViewStub
        android:id="@+id/stub_import"
        android:inflatedId="@+id/panel_import"
        android:layout="@layout/progress_overlay" />
</LinearLayout>
```

```kotlin
// Inflate ViewStub when needed
val stub = findViewById<ViewStub>(R.id.stub_import)
stub.inflate()
// or
stub.visibility = View.VISIBLE
```

---

## Views and ViewGroups

### RecyclerView with ViewHolder

**Theory:**
RecyclerView is the standard for displaying large lists efficiently. It recycles view holders to minimize object creation and layout inflation.

**Key Components:**

**1. RecyclerView:**
The container view. Handles scrolling and recycling.

**2. LayoutManager:**
Determines item arrangement:
- `LinearLayoutManager` - Vertical or horizontal list
- `GridLayoutManager` - Grid with fixed columns
- `StaggeredGridLayoutManager` - Grid with varying row heights

**3. Adapter:**
Bridges data to views. Creates ViewHolders and binds data.

**4. ViewHolder:**
Holds references to item views. Avoids repeated `findViewById()` calls.

**ViewHolder Pattern Benefits:**
- Views looked up once per ViewHolder creation
- ViewHolders are recycled as items scroll
- Only visible items + a few buffers are in memory

**Item Decorations:**
Add dividers, spacing, or custom decorations between items.

```kotlin
// Adapter
class ItemAdapter(
    private var items: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    
    class ItemViewHolder(
        private val binding: ItemLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: Item, onItemClick: (Item) -> Unit) {
            binding.titleText.text = item.title
            binding.descText.text = item.description
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }
    
    override fun getItemCount() = items.size
    
    fun updateItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}

// Activity/Fragment setup
recyclerView.apply {
    layoutManager = LinearLayoutManager(context)
    adapter = ItemAdapter(items) { item -> /* handle click */ }
    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
}
```

---

### ListAdapter with DiffUtil

**Theory:**
ListAdapter is an improved RecyclerView.Adapter that uses DiffUtil to calculate changes between lists automatically.

**DiffUtil:**
Calculates the minimal set of changes needed to transform one list into another. Runs on a background thread.

**Benefits:**
1. **Efficient Updates:** Only changed items are updated
2. **Animations:** Item animations happen automatically
3. **Background Processing:** Diff calculation doesn't block UI
4. **Simpler Code:** No manual `notifyItemChanged()` calls

**DiffUtil.ItemCallback:**
You must implement two methods:
- `areItemsTheSame()`: Do items represent the same object? (Compare IDs)
- `areContentsTheSame()`: Is the displayed content identical? (Compare all fields)

```kotlin
class ItemListAdapter(
    private val onItemClick: (Item) -> Unit
) : ListAdapter<Item, ItemListAdapter.ViewHolder>(ItemDiffCallback()) {
    
    class ViewHolder(
        private val binding: ItemLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item, onItemClick: (Item) -> Unit) {
            binding.titleText.text = item.title
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}

// Usage
val adapter = ItemListAdapter { item -> /* click */ }
recyclerView.adapter = adapter
adapter.submitList(newItems) // Automatically calculates diff
```

---

### Multiple View Types

**Theory:**
RecyclerView can display different layouts for different items. Common uses:
- Headers and items
- Different card layouts
- Ads interspersed with content

**Implementation:**
1. Override `getItemViewType()` to return different types
2. Create different ViewHolders for each type
3. Handle each type in `onCreateViewHolder()` and `onBindViewHolder()`

**Sealed Classes:**
Using sealed classes for list items provides type safety and exhaustive when expressions.

```kotlin
sealed class ListItem {
    data class Header(val title: String) : ListItem()
    data class Content(val item: Item) : ListItem()
}

class MultiTypeAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(DiffCallback()) {
    
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_CONTENT = 1
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.Content -> TYPE_CONTENT
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(/* inflate */)
            TYPE_CONTENT -> ContentViewHolder(/* inflate */)
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ListItem.Content -> (holder as ContentViewHolder).bind(item)
        }
    }
}
```

---

## Material Design Components

### MaterialButton and Chips

**Theory:**
Material Components for Android provide pre-built, customizable UI components following Material Design guidelines.

**MaterialButton Styles:**
- **Filled:** Default, highest emphasis
- **Outlined:** Medium emphasis
- **Text:** Lowest emphasis

**Chip:**
Compact elements representing attributes, actions, or filters.

**Chip Types:**
- **Input Chips:** Represent user input (tags)
- **Choice Chips:** Single selection from options
- **Filter Chips:** Multiple selection filters
- **Action Chips:** Trigger actions

**ChipGroup:**
Container for chips with single or multiple selection support.

```xml
<!-- MaterialButton -->
<com.google.android.material.button.MaterialButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Button"
    app:icon="@drawable/ic_add"
    app:iconGravity="start"
    app:cornerRadius="8dp"
    style="@style/Widget.Material3.Button" />

<!-- Outlined Button -->
<com.google.android.material.button.MaterialButton
    style="@style/Widget.Material3.Button.OutlinedButton" />

<!-- ChipGroup -->
<com.google.android.material.chip.ChipGroup
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:singleSelection="true">
    
    <com.google.android.material.chip.Chip
        android:text="Chip 1"
        style="@style/Widget.Material3.Chip.Filter" />
    
    <com.google.android.material.chip.Chip
        android:text="Chip 2"
        style="@style/Widget.Material3.Chip.Filter" />
</com.google.android.material.chip.ChipGroup>
```

---

### BottomSheet

**Theory:**
BottomSheets slide up from the bottom of the screen. Two types:

**1. Standard (Persistent) Bottom Sheet:**
- Part of the layout, always present
- Can be collapsed, expanded, or hidden
- Good for complementary content

**2. Modal Bottom Sheet:**
- Appears as a dialog
- Dims background
- Must be dismissed to interact with content below
- Good for choices, forms

**BottomSheetBehavior States:**
- `STATE_EXPANDED` - Fully open
- `STATE_COLLAPSED` - Partially visible (peek height)
- `STATE_HIDDEN` - Completely hidden
- `STATE_DRAGGING` - User is dragging
- `STATE_SETTLING` - Animating to a state

```xml
<!-- Layout with BottomSheet -->
<androidx.coordinatorlayout.widget.CoordinatorLayout>
    
    <!-- Main content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <!-- content -->
    </LinearLayout>
    
    <!-- Bottom Sheet -->
    <LinearLayout
        android:id="@+id/bottomSheet"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_behavior="com.google.android.material.bottomsheet.BottomSheetBehavior">
        <!-- sheet content -->
    </LinearLayout>
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

```kotlin
// Control BottomSheet
val bottomSheet = findViewById<LinearLayout>(R.id.bottomSheet)
val behavior = BottomSheetBehavior.from(bottomSheet)

behavior.state = BottomSheetBehavior.STATE_EXPANDED
behavior.state = BottomSheetBehavior.STATE_COLLAPSED
behavior.state = BottomSheetBehavior.STATE_HIDDEN

behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> { }
            BottomSheetBehavior.STATE_COLLAPSED -> { }
        }
    }
    
    override fun onSlide(bottomSheet: View, slideOffset: Float) { }
})

// Modal Bottom Sheet
class MyBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }
}
```

---

### TabLayout with ViewPager2

**Theory:**
TabLayout and ViewPager2 work together for tabbed navigation with swipeable content.

**ViewPager2 vs ViewPager:**
- ViewPager2 is built on RecyclerView
- Supports both horizontal and vertical scrolling
- Better performance and RTL support
- Uses FragmentStateAdapter instead of FragmentPagerAdapter

**TabLayoutMediator:**
Connects TabLayout with ViewPager2:
- Syncs tab selection with page
- Creates tabs automatically
- Handles tab configuration callback

**Adapter:**
`FragmentStateAdapter` creates fragments on demand. Fragments are destroyed when far from current position.

```xml
<LinearLayout
    android:orientation="vertical">
    
    <com.google.android.material.tabs.TabLayout
        android:id="@+id/tabLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:tabMode="fixed"
        app:tabGravity="fill" />
    
    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
</LinearLayout>
```

```kotlin
// Adapter
class PagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = 3
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FirstFragment()
            1 -> SecondFragment()
            2 -> ThirdFragment()
            else -> throw IllegalArgumentException()
        }
    }
}

// Setup
viewPager.adapter = PagerAdapter(this)

TabLayoutMediator(tabLayout, viewPager) { tab, position ->
    tab.text = when (position) {
        0 -> "Tab 1"
        1 -> "Tab 2"
        2 -> "Tab 3"
        else -> ""
    }
}.attach()
```

---

### Snackbar and Dialogs

**Theory:**

**Snackbar:**
Brief messages at the bottom of the screen. Better than Toast because:
- Can include actions
- Dismissible by swiping
- Respects CoordinatorLayout (moves with FAB)

**Material Dialogs:**
`MaterialAlertDialogBuilder` creates Material-styled dialogs. Types:
- **Alert:** Confirmation or information
- **Simple:** List of choices
- **Single/Multi Choice:** Selection from options

**When to Use:**
- Snackbar: Brief, non-critical feedback
- Dialog: Critical decisions requiring user action

```kotlin
// Snackbar
Snackbar.make(view, "Message", Snackbar.LENGTH_LONG)
    .setAction("Undo") { /* undo action */ }
    .setActionTextColor(Color.YELLOW)
    .show()

// Material Dialog
MaterialAlertDialogBuilder(context)
    .setTitle("Delete?")
    .setMessage("This action cannot be undone.")
    .setPositiveButton("Delete") { dialog, _ ->
        // delete
        dialog.dismiss()
    }
    .setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
    }
    .show()
```

---

## Animations in Views

**Theory Overview:**
Android provides several animation systems for Views, each suited to different use cases. Understanding when to use each is crucial for creating smooth, performant animations.

**Animation Types:**
1. **View Animations (Tween):** Legacy, XML-based, doesn't change actual view properties
2. **Property Animations:** Modern, changes actual object properties
3. **Transitions Framework:** Scene-based layout changes
4. **MotionLayout:** Declarative complex animations

---

### Property Animations

**Theory:**
Property Animations animate the actual properties of objects. When you animate `translationX`, the view actually moves - not just its drawing.

**Key Classes:**

**1. ValueAnimator:**
Base class that animates values over time. You must apply the values yourself.

**2. ObjectAnimator:**
Extends ValueAnimator to automatically apply values to object properties via reflection or property setters.

**3. AnimatorSet:**
Chains multiple animators with timing relationships (sequential, together, delays).

**4. ViewPropertyAnimator:**
Optimized, fluent API for common View property animations. Best performance.

**Interpolators:**
Control the rate of change:
- **LinearInterpolator:** Constant rate
- **AccelerateInterpolator:** Starts slow, speeds up
- **DecelerateInterpolator:** Starts fast, slows down
- **OvershootInterpolator:** Goes past target, springs back
- **BounceInterpolator:** Bounces at the end
- **AnticipateInterpolator:** Pulls back before moving forward

**Performance:**
ViewPropertyAnimator is most efficient because it batches property changes and uses hardware layers automatically.

```kotlin
// ObjectAnimator
val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
    duration = 300
    interpolator = AccelerateDecelerateInterpolator()
}
animator.start()

// Multiple properties
ObjectAnimator.ofPropertyValuesHolder(
    view,
    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.5f),
    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.5f),
    PropertyValuesHolder.ofFloat("alpha", 1f, 0.5f)
).apply {
    duration = 500
    start()
}

// AnimatorSet
AnimatorSet().apply {
    playTogether(
        ObjectAnimator.ofFloat(view, "translationX", 0f, 100f),
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
    )
    duration = 300
    start()
}

// ViewPropertyAnimator (simpler API - RECOMMENDED)
view.animate()
    .alpha(0.5f)
    .scaleX(1.2f)
    .scaleY(1.2f)
    .translationY(100f)
    .setDuration(300)
    .setInterpolator(OvershootInterpolator())
    .withEndAction { /* completion */ }
    .start()
```

---

### MotionLayout

**Theory:**
MotionLayout is a subclass of ConstraintLayout that enables complex motion and widget animation. It bridges layout transitions and motion handling.

**Key Concepts:**

**1. MotionScene:**
XML file defining animation states and transitions. Contains:
- ConstraintSets (start/end states)
- Transitions (how to animate between states)
- KeyFrames (intermediate states during animation)

**2. ConstraintSet:**
Defines complete layout state - positions, sizes, visibility of all views.

**3. Transition:**
Defines how to move between ConstraintSets:
- Duration
- Interpolator
- Touch handlers (OnSwipe, OnClick)
- KeyFrames

**4. KeyFrames:**
Modify properties at specific points (0-100%) during transition:
- **KeyPosition:** Change path (arc, delta, screen-relative)
- **KeyAttribute:** Change view attributes (alpha, rotation, scale)
- **KeyCycle:** Add oscillations

**Advantages over Property Animations:**
- Declarative (defined in XML)
- Scrubbing (drag to any point in animation)
- Touch-driven animations
- Complex choreography without code

**Use Cases:**
- Collapsing toolbars
- Swipe-to-reveal
- Complex onboarding animations
- Coordinated motion of multiple views

```xml
<!-- activity_main.xml -->
<androidx.constraintlayout.motion.widget.MotionLayout
    android:id="@+id/motionLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:layoutDescription="@xml/scene">
    
    <ImageView
        android:id="@+id/image"
        android:layout_width="100dp"
        android:layout_height="100dp" />
</androidx.constraintlayout.motion.widget.MotionLayout>

<!-- res/xml/scene.xml -->
<MotionScene xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:motion="http://schemas.android.com/apk/res-auto">
    
    <Transition
        motion:constraintSetStart="@+id/start"
        motion:constraintSetEnd="@+id/end"
        motion:duration="1000">
        
        <OnSwipe
            motion:touchAnchorId="@id/image"
            motion:dragDirection="dragUp" />
    </Transition>
    
    <ConstraintSet android:id="@+id/start">
        <Constraint
            android:id="@id/image"
            android:layout_width="100dp"
            android:layout_height="100dp"
            motion:layout_constraintBottom_toBottomOf="parent"
            motion:layout_constraintStart_toStartOf="parent" />
    </ConstraintSet>
    
    <ConstraintSet android:id="@+id/end">
        <Constraint
            android:id="@id/image"
            android:layout_width="200dp"
            android:layout_height="200dp"
            motion:layout_constraintTop_toTopOf="parent"
            motion:layout_constraintEnd_toEndOf="parent" />
    </ConstraintSet>
</MotionScene>
```

---

### Transitions Framework

**Theory:**
The Transitions Framework automatically animates layout changes. You define "before" and "after" states; the framework figures out how to animate between them.

**Key Concepts:**

**1. Scene:**
Represents a layout state. Can be created from:
- Layout resource
- ViewGroup hierarchy

**2. Transition:**
Defines how to animate between scenes:
- **Fade:** Animate alpha
- **ChangeBounds:** Animate position/size
- **ChangeTransform:** Animate scale/rotation
- **AutoTransition:** Combination of fade and bounds

**3. TransitionManager:**
Orchestrates the animation:
- `go()` - Animate to a specific scene
- `beginDelayedTransition()` - Animate next layout changes

**beginDelayedTransition:**
This is the most powerful feature - you call it before making layout changes, and all subsequent changes to that ViewGroup are animated automatically.

```kotlin
// Scene transitions
val scene1 = Scene.getSceneForLayout(sceneRoot, R.layout.scene1, context)
val scene2 = Scene.getSceneForLayout(sceneRoot, R.layout.scene2, context)

TransitionManager.go(scene2, ChangeBounds())

// Auto transitions (MOST COMMON USAGE)
TransitionManager.beginDelayedTransition(viewGroup, AutoTransition())
view.visibility = View.GONE // Animates automatically!

// Custom transition set
val transition = TransitionSet().apply {
    addTransition(Fade())
    addTransition(ChangeBounds())
    duration = 300
}
TransitionManager.beginDelayedTransition(viewGroup, transition)
```

---

### Shared Element Transitions

**Theory:**
Shared element transitions create visual continuity between screens by animating a view from one Activity/Fragment to another.

**How It Works:**
1. Mark views with `transitionName` in both layouts
2. Pass shared elements when starting new Activity/Fragment
3. Framework finds matching views and animates between their positions/sizes

**Matching:**
Views are matched by `transitionName` attribute - not view ID. The same name must appear in both source and destination layouts.

**Important Considerations:**
- Views must be visible and measured before transition starts
- For images loaded asynchronously, use `postponeEnterTransition()` and `startPostponedEnterTransition()`
- Order views with `setReorderingAllowed(true)` for Fragments

**Types of Shared Element Transitions:**
- **changeImageTransform:** Animates ImageView's matrix (scale type changes)
- **changeBounds:** Animates position and size
- **changeClipBounds:** Animates clip bounds
- **changeTransform:** Animates scale and rotation

```kotlin
// Activity A - Starting the transition
val intent = Intent(this, DetailActivity::class.java)
val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
    this,
    Pair(imageView, "shared_image"),
    Pair(titleView, "shared_title")
)
startActivity(intent, options.toBundle())

// Activity B - styles.xml
<style name="AppTheme">
    <item name="android:windowSharedElementEnterTransition">@transition/shared_element</item>
    <item name="android:windowSharedElementExitTransition">@transition/shared_element</item>
</style>

// Activity B layout - must have matching transitionNames
<ImageView
    android:transitionName="shared_image" />
<TextView
    android:transitionName="shared_title" />

// res/transition/shared_element.xml
<transitionSet>
    <changeImageTransform />
    <changeBounds />
</transitionSet>
```

```kotlin
// Fragment shared elements
parentFragmentManager.commit {
    setReorderingAllowed(true) // Important for shared elements!
    addSharedElement(imageView, "shared_image")
    replace(R.id.container, DetailFragment())
    addToBackStack(null)
}

// In destination fragment - postpone if loading images
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    sharedElementEnterTransition = TransitionInflater.from(context)
        .inflateTransition(R.transition.shared_element)
    
    // If image needs to load:
    postponeEnterTransition()
}

// After image loaded:
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    // Load image, then:
    startPostponedEnterTransition()
}
```

---

## Quick Reference: Compose vs Views

**Theory:**
Choosing between Compose and Views depends on your project's requirements, team expertise, and existing codebase.

**When to Choose Compose:**
- New projects starting fresh
- Teams wanting modern, concise code
- Need for complex, dynamic UIs
- Rapid iteration and previews
- Full Kotlin codebase

**When to Use Views:**
- Maintaining legacy codebases
- Using third-party libraries that require Views
- Complex animations that need MotionLayout
- Team more familiar with XML

**Interoperability:**
Compose and Views can coexist:
- `AndroidView` - Embed Views in Compose
- `ComposeView` - Embed Compose in Views/Fragments

| Feature | Compose | Views |
|---------|---------|-------|
| UI Definition | Kotlin functions | XML layouts |
| State | `remember`, `mutableStateOf` | `LiveData`, `StateFlow` |
| Lists | `LazyColumn`, `LazyRow` | `RecyclerView` |
| Themes | `MaterialTheme` | `styles.xml`, `themes.xml` |
| Navigation | Navigation Compose | Navigation Component |
| Animation | `animate*AsState`, `AnimatedVisibility` | `ObjectAnimator`, `MotionLayout` |
| Testing | `ComposeTestRule` | `Espresso` |
| Preview | `@Preview` | Layout Preview |
| Learning Curve | Kotlin required | XML + Java/Kotlin |
| Build Time | Slightly longer (Compose compiler) | Faster |
| Runtime Performance | Comparable (optimized) | Mature optimization |

---

## Best Practices Summary

### Compose Best Practices

**1. State Hoisting:**
Keep state in parent composables for reusability and testability.
```kotlin
// Bad - internal state
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) { Text("$count") }
}

// Good - hoisted state
@Composable
fun Counter(count: Int, onCountChange: (Int) -> Unit) {
    Button(onClick = { onCountChange(count + 1) }) { Text("$count") }
}
```

**2. Use Keys in Lists:**
Provide stable keys for efficient recomposition.
```kotlin
LazyColumn {
    items(items, key = { it.id }) { item ->
        ItemRow(item)
    }
}
```

**3. Avoid Side Effects in Composable Body:**
Use effect handlers: `LaunchedEffect`, `DisposableEffect`, `SideEffect`.

**4. Minimize Recomposition:**
- Use `derivedStateOf` for computed values
- Mark classes as `@Stable` or `@Immutable`
- Use `remember` with appropriate keys

**5. Test with Semantics:**
Use `testTag` and semantic matchers for reliable tests.

### Views Best Practices

**1. Use ViewBinding:**
Type-safe, null-safe view access without reflection.

**2. Use ListAdapter with DiffUtil:**
Efficient RecyclerView updates with automatic item animations.

**3. Avoid Deep Nesting:**
Prefer ConstraintLayout for flat hierarchies. Each nesting level adds measure/layout overhead.

**4. Fragment Lifecycle Awareness:**
Clear binding references in `onDestroyView()` to prevent memory leaks.

**5. Background Operations:**
Never block UI thread. Use coroutines, WorkManager, or other async mechanisms.

### General Performance Tips

**1. Layout Inspection:**
Use Android Studio's Layout Inspector to identify:
- Overdraw (layers drawing on top of each other)
- Deep hierarchies
- Expensive layouts

**2. Profile First:**
Don't optimize prematurely. Use profilers to identify actual bottlenecks.

**3. Image Loading:**
Use libraries like Coil (Compose) or Glide (Views) with proper caching and sizing.

**4. Animations:**
- Use hardware acceleration
- Prefer property animations over view animations
- Keep animating views simple

**5. RecyclerView Optimization (Views):**
- Use `setHasFixedSize(true)` if item size is constant
- Use `setItemViewCacheSize()` for scroll performance
- Implement `onViewRecycled()` to release resources
