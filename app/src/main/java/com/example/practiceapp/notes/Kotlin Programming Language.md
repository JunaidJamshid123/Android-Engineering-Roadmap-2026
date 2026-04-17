# Kotlin Interview Cheat Sheet & Quick Reference Guide

## Table of Contents
1. [Core Kotlin Fundamentals](#1-core-kotlin-fundamentals)
2. [Advanced Kotlin Concepts](#2-advanced-kotlin-concepts)

---

# 1. CORE KOTLIN FUNDAMENTALS

## 1.1 Variables, Data Types, Operators, Control Flow

### Variables
| Keyword | Description | Reassignable |
|---------|-------------|--------------|
| `val` | Immutable reference (prefer this) | No |
| `var` | Mutable reference | Yes |
| `const val` | Compile-time constant (top-level/object only) | No |

```kotlin
val name = "Android"        // Type inference
var count: Int = 0          // Explicit type
const val API_KEY = "abc"   // Compile-time constant
```

**Interview Tip**: `val` makes the *reference* immutable, not the object itself. A `val list = mutableListOf()` can still have items added.

### Data Types
| Type | Size | Example |
|------|------|---------|
| Byte | 8-bit | `127` |
| Short | 16-bit | `32767` |
| Int | 32-bit (default) | `2147483647` |
| Long | 64-bit | `9223372036854775807L` |
| Float | 32-bit decimal | `3.14f` |
| Double | 64-bit decimal (default) | `3.14159` |
| Char | Character | `'A'` |
| String | Text | `"Hello"` |
| Boolean | true/false | `true` |

**Key Point**: Unlike Java, Kotlin has no primitive types at language level - everything is an object. Compiler optimizes to JVM primitives.

### String Templates
```kotlin
val name = "John"
println("Hello, $name!")              // Simple variable
println("Length: ${name.length}")     // Expression
```

### Operators
```kotlin
// Equality
a == b    // Structural equality (content) - calls equals()
a === b   // Referential equality (same object)

// Range & Membership
5 in 1..10     // true (inclusive range)
5 !in 1..3     // true (not in range)
```

### Control Flow (All are EXPRESSIONS - return values!)

**if-else:**
```kotlin
val max = if (a > b) a else b
```

**when (replaces switch):**
```kotlin
val result = when (x) {
    1 -> "One"
    2, 3 -> "Two or Three"
    in 4..10 -> "Between 4-10"
    is String -> "It's a string"
    else -> "Unknown"
}

// Without argument (if-else chain)
val grade = when {
    score >= 90 -> "A"
    score >= 80 -> "B"
    else -> "F"
}
```

**for loops:**
```kotlin
for (i in 1..5) { }           // 1, 2, 3, 4, 5
for (i in 5 downTo 1) { }     // 5, 4, 3, 2, 1
for (i in 1 until 5) { }      // 1, 2, 3, 4 (excludes 5)
for (i in 1..10 step 2) { }   // 1, 3, 5, 7, 9
for ((index, value) in list.withIndex()) { }
```

---

## 1.2 Functions

### Function Syntax
```kotlin
// Standard function
fun add(a: Int, b: Int): Int {
    return a + b
}

// Single expression function
fun add(a: Int, b: Int): Int = a + b

// Default parameters (eliminates overloading)
fun greet(name: String, greeting: String = "Hello") = "$greeting, $name!"

// Named arguments
createUser(name = "John", email = "john@mail.com", age = 25)

// Varargs
fun printAll(vararg messages: String) { }
```

### Function Types
```kotlin
val sum: (Int, Int) -> Int = { a, b -> a + b }
val greet: (String) -> String = { "Hello, $it" }
val action: () -> Unit = { println("Hello") }
```

### Lambda Expressions
```kotlin
// Full syntax
val multiply = { a: Int, b: Int -> a * b }

// Single parameter uses 'it'
val double: (Int) -> Int = { it * 2 }

// Trailing lambda (if last param is function)
list.filter { it > 5 }
```

### Higher-Order Functions
Functions that take functions as parameters or return functions.

```kotlin
fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}

// Usage
calculate(5, 3) { x, y -> x + y }  // 8
```

### Inline Functions
```kotlin
inline fun measureTime(action: () -> Unit) {
    val start = System.currentTimeMillis()
    action()
    println("Took: ${System.currentTimeMillis() - start}ms")
}
```

**Why inline?** Prevents lambda object creation at each call site. Use for small, frequently-called higher-order functions.

| Modifier | Purpose |
|----------|---------|
| `inline` | Copy function body to call site |
| `noinline` | Don't inline specific lambda (when storing it) |
| `crossinline` | Inline but prevent non-local returns |

---

## 1.3 Object-Oriented Programming

### Classes
```kotlin
class Person(val name: String, var age: Int) {  // Primary constructor
    
    var email: String = ""
        get() = field.lowercase()    // Custom getter
        set(value) { field = value.trim() }  // Custom setter
    
    val isAdult: Boolean             // Computed property
        get() = age >= 18
    
    init {
        println("Created: $name")    // Init block
    }
    
    constructor(name: String) : this(name, 0)  // Secondary constructor
}
```

### Inheritance
```kotlin
open class Animal(val name: String) {      // 'open' allows inheritance
    open fun makeSound() { }               // 'open' allows override
}

class Dog(name: String) : Animal(name) {
    override fun makeSound() { println("Woof!") }
}
```

**Key Point**: Classes are `final` by default in Kotlin (unlike Java).

### Interfaces
```kotlin
interface Clickable {
    fun click()                         // Abstract
    fun showRipple() { println("Ripple") }  // Default implementation
    val clickCount: Int                 // Property (no backing field)
}

class Button : Clickable, Focusable {   // Multiple interfaces OK
    override val clickCount = 0
    override fun click() { }
}
```

### Abstract Classes
```kotlin
abstract class Shape(val color: String) {
    abstract fun area(): Double         // Must be implemented
    fun describe() { println("A $color shape") }  // Concrete method
}
```

### Sealed Classes
Restricted hierarchy - all subclasses known at compile time.

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

// Exhaustive when - no 'else' needed!
when (result) {
    is Result.Success -> showData(result.data)
    is Result.Error -> showError(result.message)
    is Result.Loading -> showLoading()
}
```

**Interview Tip**: Sealed vs Enum - Enum entries are singletons; Sealed subclasses can have state and multiple instances.

### Visibility Modifiers
| Modifier | Scope |
|----------|-------|
| `public` | Everywhere (default) |
| `private` | Same file/class only |
| `protected` | Class + subclasses |
| `internal` | Same module |

---

## 1.4 Data Classes

Auto-generates: `equals()`, `hashCode()`, `toString()`, `copy()`, `componentN()`

```kotlin
data class User(val id: Int, val name: String, val email: String)

val user1 = User(1, "John", "john@mail.com")
val user2 = user1.copy(name = "Jane")      // Copy with modifications
val (id, name, email) = user1              // Destructuring
```

**Requirements**: At least one `val`/`var` in primary constructor. Cannot be abstract, open, sealed, or inner.

---

## 1.5 Null Safety

### Nullable vs Non-Nullable
```kotlin
var name: String = "John"    // Cannot be null
var nullable: String? = null  // Can be null
```

### Operators
| Operator | Name | Usage |
|----------|------|-------|
| `?.` | Safe call | `name?.length` (null if name is null) |
| `?:` | Elvis | `name ?: "default"` (default if null) |
| `!!` | Not-null assertion | `name!!.length` (throws NPE if null) |
| `as?` | Safe cast | `obj as? String` (null if cast fails) |

```kotlin
// Safe call chain
val city = user?.address?.city

// Elvis with return/throw
val name = user?.name ?: return
val id = user?.id ?: throw IllegalArgumentException()

// let for null check
nullable?.let { 
    println("Not null: $it") 
}

// Smart casting
if (name != null) {
    println(name.length)  // Compiler knows it's non-null
}
```

---

## 1.6 Collections

### Types
| Read-only | Mutable |
|-----------|---------|
| `List<T>` | `MutableList<T>` |
| `Set<T>` | `MutableSet<T>` |
| `Map<K,V>` | `MutableMap<K,V>` |

```kotlin
val list = listOf(1, 2, 3)              // Immutable
val mutableList = mutableListOf(1, 2, 3)  // Mutable
val map = mapOf("a" to 1, "b" to 2)
```

### Common Operations
| Operation | Description | Example |
|-----------|-------------|---------|
| `map` | Transform each element | `list.map { it * 2 }` |
| `filter` | Keep matching elements | `list.filter { it > 2 }` |
| `reduce` | Accumulate to single value | `list.reduce { acc, n -> acc + n }` |
| `fold` | Like reduce with initial | `list.fold(0) { acc, n -> acc + n }` |
| `find` | First matching or null | `list.find { it > 2 }` |
| `any/all/none` | Check predicates | `list.any { it > 5 }` |
| `groupBy` | Group into map | `list.groupBy { it % 2 }` |
| `sortedBy` | Sort by selector | `users.sortedBy { it.name }` |
| `flatMap` | Map and flatten | `lists.flatMap { it }` |
| `distinct` | Remove duplicates | `list.distinct()` |

### Sequences (Lazy Evaluation)
```kotlin
val result = list.asSequence()
    .filter { it > 2 }
    .map { it * 2 }
    .take(3)
    .toList()  // Terminal operation triggers execution
```

**Use sequences for**: Large collections, chained operations, when you might not need all results.

---

## 1.7 Extension Functions & Properties

Add functionality to existing classes without inheritance.

```kotlin
// Extension function
fun String.addExclamation() = "$this!"

// Extension property
val String.firstChar: Char
    get() = this[0]

// Usage
"Hello".addExclamation()  // "Hello!"
"Kotlin".firstChar        // 'K'
```

**Key Points**:
- Resolved statically (not polymorphic)
- Cannot access private members
- Member functions always win over extensions

---

## 1.8 Scope Functions

| Function | Object Ref | Returns | Use Case |
|----------|-----------|---------|----------|
| `let` | `it` | Lambda result | Null checks, transform |
| `run` | `this` | Lambda result | Config + compute result |
| `with` | `this` | Lambda result | Group calls on object |
| `apply` | `this` | Object itself | Object configuration |
| `also` | `it` | Object itself | Side effects (logging) |

```kotlin
// let - null check & transform
user?.let { saveToDb(it) }

// apply - configure object
val intent = Intent().apply {
    putExtra("ID", 123)
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

// also - side effects
users.also { Log.d("TAG", "Count: ${it.size}") }
     .filter { it.isActive }

// run - config + result
val isValid = user?.run {
    name.isNotBlank() && email.contains("@")
} ?: false

// with - group operations
with(binding) {
    nameText.text = user.name
    emailText.text = user.email
}
```

---

# 2. ADVANCED KOTLIN CONCEPTS

## 2.1 Coroutines

### What are Coroutines?
Lightweight threads for asynchronous programming. Can suspend execution without blocking threads.

### suspend Functions
```kotlin
suspend fun fetchUser(id: String): User {
    delay(1000)  // Suspends, doesn't block
    return api.getUser(id)
}
```

### Coroutine Builders
| Builder | Returns | Behavior |
|---------|---------|----------|
| `launch` | `Job` | Fire-and-forget |
| `async` | `Deferred<T>` | Returns result via `await()` |
| `runBlocking` | `T` | Blocks thread (testing only) |

```kotlin
// launch - fire and forget
viewModelScope.launch {
    val user = fetchUser("123")
    _uiState.value = UiState.Success(user)
}

// async - when you need results
val user = async { fetchUser("123") }
val posts = async { fetchPosts("123") }
showProfile(user.await(), posts.await())
```

### Dispatchers
| Dispatcher | Use For |
|------------|---------|
| `Dispatchers.Main` | UI updates |
| `Dispatchers.IO` | Network, disk, database |
| `Dispatchers.Default` | CPU-intensive work |

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    val data = repository.fetchData()
    withContext(Dispatchers.Main) {
        updateUI(data)
    }
}
```

### Structured Concurrency
- Coroutines have parent-child relationships
- Parent waits for all children
- Cancelling parent cancels all children
- Error in child cancels siblings

### Exception Handling
```kotlin
viewModelScope.launch {
    try {
        val user = fetchUser("123")
    } catch (e: Exception) {
        showError(e.message)
    }
}

// Or with CoroutineExceptionHandler
val handler = CoroutineExceptionHandler { _, e ->
    Log.e("TAG", "Error: ${e.message}")
}
scope.launch(handler) { }
```

---

## 2.2 Flow API

### Cold vs Hot Flows
| Type | Behavior | Example |
|------|----------|---------|
| Cold (`Flow`) | Starts on collect, each collector gets fresh data | API calls |
| Hot (`StateFlow`) | Always active, collectors get current + updates | UI state |
| Hot (`SharedFlow`) | Broadcasts to multiple collectors | Events |

### Basic Flow
```kotlin
fun fetchUsers(): Flow<User> = flow {
    users.forEach { 
        delay(100)
        emit(it)  // Emit values
    }
}

// Collect
viewModelScope.launch {
    fetchUsers().collect { user ->
        println(user)
    }
}
```

### StateFlow (UI State)
```kotlin
class ViewModel {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun load() {
        _uiState.value = UiState.Success(data)
    }
}

// In Compose
val state by viewModel.uiState.collectAsState()
```

### SharedFlow (Events)
```kotlin
private val _events = MutableSharedFlow<Event>()
val events: SharedFlow<Event> = _events.asSharedFlow()

// Emit (from coroutine)
_events.emit(Event.ShowToast("Success"))
```

### Flow Operators
```kotlin
flow
    .map { it.toUpperCase() }
    .filter { it.isNotEmpty() }
    .onEach { log(it) }
    .catch { e -> emit("Error") }
    .onStart { emit("Loading") }
    .onCompletion { println("Done") }
    .collect { }
```

### Combining Flows
```kotlin
combine(flow1, flow2) { a, b -> a + b }
flow1.zip(flow2) { a, b -> Pair(a, b) }
merge(flow1, flow2)
flow.flatMapLatest { fetchDetails(it) }
```

---

## 2.3 Channels

Communication between coroutines - like a blocking queue.

```kotlin
val channel = Channel<Int>()

// Producer
launch {
    for (i in 1..5) channel.send(i)
    channel.close()
}

// Consumer
launch {
    for (value in channel) {
        println(value)
    }
}
```

### Channel Types
| Type | Behavior |
|------|----------|
| `RENDEZVOUS` | No buffer, sender waits |
| `BUFFERED` | Fixed buffer size |
| `UNLIMITED` | Unlimited buffer |
| `CONFLATED` | Keeps only latest value |

**Flow vs Channel**: Use Flow for streams of data; Channel for communication between coroutines.

---

## 2.4 Generics

### Type Parameters
```kotlin
class Box<T>(val item: T)
fun <T> singletonList(item: T): List<T> = listOf(item)
```

### Variance
| Modifier | Direction | Example |
|----------|-----------|---------|
| `out` (covariant) | Producer only | `List<out T>` |
| `in` (contravariant) | Consumer only | `Comparable<in T>` |
| None (invariant) | Both | `MutableList<T>` |

```kotlin
interface Producer<out T> { fun produce(): T }
interface Consumer<in T> { fun consume(item: T) }
```

### Reified Type Parameters
Access type info at runtime (inline functions only).

```kotlin
inline fun <reified T> isType(value: Any): Boolean = value is T

inline fun <reified T : Activity> Context.startActivity() {
    startActivity(Intent(this, T::class.java))
}
```

---

## 2.5 Delegated Properties

### Built-in Delegates
```kotlin
// lazy - computed once on first access
val expensiveData: Data by lazy { computeData() }

// observable - callback on change
var name: String by Delegates.observable("") { _, old, new ->
    println("Changed from $old to $new")
}

// vetoable - can reject changes
var age: Int by Delegates.vetoable(0) { _, _, new ->
    new >= 0  // Only accept non-negative
}

// map delegate
class User(map: Map<String, Any?>) {
    val name: String by map
    val age: Int by map
}
```

### Custom Delegate
```kotlin
class Preference<T>(private val key: String, private val default: T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = 
        prefs.get(key, default)
    
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
        prefs.put(key, value)
}

// Usage
var username: String by Preference("username", "")
```

---

## 2.6 Operator Overloading

Define how operators work for your classes.

| Operator | Function |
|----------|----------|
| `+` | `plus` |
| `-` | `minus` |
| `*` | `times` |
| `/` | `div` |
| `[]` | `get`/`set` |
| `in` | `contains` |
| `()` | `invoke` |
| `==` | `equals` |
| `<`, `>` | `compareTo` |

```kotlin
data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun times(scale: Int) = Point(x * scale, y * scale)
}

val p1 = Point(1, 2) + Point(3, 4)  // Point(4, 6)
val p2 = Point(2, 3) * 3             // Point(6, 9)
```

---

## 2.7 Annotations & Reflection

### Annotations
```kotlin
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MyAnnotation(val name: String)

@MyAnnotation("example")
class MyClass { }
```

### Reflection
```kotlin
val kClass = MyClass::class
val jClass = MyClass::class.java

// Property reference
val prop = MyClass::name
println(prop.get(instance))

// Function reference
val func = String::length
println(func("hello"))  // 5
```

---

## 2.8 Kotlin Multiplatform (KMP)

### expect/actual Mechanism
```kotlin
// commonMain - declare interface
expect class Platform() {
    val name: String
}

// androidMain - Android implementation
actual class Platform actual constructor() {
    actual val name: String = "Android"
}

// iosMain - iOS implementation
actual class Platform actual constructor() {
    actual val name: String = UIDevice.currentDevice.systemName()
}
```

### Source Sets
```
shared/src/
├── commonMain/     # Shared code
├── androidMain/    # Android-specific
├── iosMain/        # iOS-specific
```

### Benefits
- Share 50-80% of business logic
- Native UI per platform
- Single source of truth
- Type safety across platforms
- Gradual adoption

---

# Quick Interview Questions & Answers

**Q: val vs var?**
A: `val` is immutable reference (can't reassign), `var` is mutable. Prefer `val`.

**Q: == vs ===?**
A: `==` is structural equality (content), `===` is referential equality (same object).

**Q: What are sealed classes?**
A: Classes with restricted subclasses known at compile time. Enables exhaustive `when` without `else`.

**Q: What does `inline` do?**
A: Copies function body to call site, avoiding lambda object creation overhead.

**Q: StateFlow vs SharedFlow?**
A: StateFlow always has a value (state), SharedFlow doesn't (events). StateFlow conflates, SharedFlow can replay.

**Q: What is structured concurrency?**
A: Coroutines form parent-child hierarchies. Parent waits for children, cancellation propagates down.

**Q: out vs in variance?**
A: `out` = producer (covariant), `in` = consumer (contravariant).

**Q: Why use data class?**
A: Auto-generates `equals`, `hashCode`, `toString`, `copy`, `componentN`.

**Q: What is the Elvis operator?**
A: `?:` - provides default value when left side is null.

**Q: Difference between launch and async?**
A: `launch` returns `Job` (fire-and-forget), `async` returns `Deferred` (get result with `await()`).

---

*Generated from Android Engineering Roadmap 2026 - Kotlin Programming Language Section*
