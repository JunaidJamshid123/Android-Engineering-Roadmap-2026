# Android Testing – Complete Guide

---

## 9. Testing

Testing is the practice of verifying that your code behaves as expected under various conditions. In Android, testing is critical because:

- **Prevents regressions** — changes in one part of the app don't silently break another.
- **Enables safe refactoring** — you can restructure code confidently knowing tests will catch mistakes.
- **Documents behavior** — tests serve as living documentation of how your code is supposed to work.
- **Catches bugs early** — a bug found during a unit test costs far less time than one found in production.
- **Speeds up development** — automated tests run in seconds vs. minutes of manual testing after every change.

Android supports multiple layers of testing — from fast unit tests running on the local JVM to full end-to-end instrumentation tests running on a real device or emulator.

### Android Testing Overview

```
 ┌──────────────────────────────────────────────────────────────────┐
 │                    Android Testing Layers                        │
 ├──────────────┬──────────┬──────────┬────────────────────────────┤
 │ Layer        │ % Tests  │ Speed    │ Tools                      │
 ├──────────────┼──────────┼──────────┼────────────────────────────┤
 │ UI / E2E     │   ~10%   │ Slowest  │ Espresso, Compose Test,    │
 │              │          │ (mins)   │ UI Automator               │
 ├──────────────┼──────────┼──────────┼────────────────────────────┤
 │ Integration  │   ~20%   │ Medium   │ Robolectric, Room          │
 │              │          │ (secs)   │ In-Memory, FragmentScenario│
 ├──────────────┼──────────┼──────────┼────────────────────────────┤
 │ Unit Tests   │   ~70%   │ Fastest  │ JUnit, MockK, Turbine,     │
 │              │          │ (ms)     │ Truth, Robolectric         │
 └──────────────┴──────────┴──────────┴────────────────────────────┘

 Rule of thumb: write MANY fast unit tests, SOME integration tests,
                and FEW slow UI tests.
```

### Android Test Source Sets

```
  app/src/
  ├── main/           ─── Production code
  │
  ├── test/           ─── Unit tests (JVM only)
  │   │                    Run: ./gradlew test
  │   └──────────────────► Local JVM  (fast, no device)
  │
  └── androidTest/    ─── Instrumented tests
      │                    Run: ./gradlew connectedAndroidTest
      └──────────────────► Device / Emulator  (slow, real Android)
```

---

## 9.1 Unit Testing

Unit tests verify the behavior of a **single class or function** in isolation. They run on the local JVM — no device or emulator is needed — making them extremely fast (milliseconds per test).

**Why unit tests matter:**
- Fastest feedback loop — run hundreds of tests in seconds.
- Force you to write decoupled, testable code (dependency injection, interfaces).
- Catch logic errors before they reach the UI layer.

**What to unit test:**
- ViewModels (state logic, transformations)
- UseCases / Interactors (business rules)
- Repositories (data mapping, caching logic)
- Utility functions (parsing, validation, formatting)

**What NOT to unit test:**
- Android framework classes directly (use Robolectric/instrumented tests)
- Trivial getters/setters with no logic
- Third-party library internals

Location: `app/src/test/java/`

---

### 9.1.1 Fundamentals

#### JUnit 4

JUnit 4 is the default testing framework in Android projects. It uses annotations to define test methods and lifecycle hooks.

**Setup (build.gradle.kts):**
```kotlin
dependencies {
    testImplementation("junit:junit:4.13.2")
}
```

**Basic Test:**
```kotlin
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculatorTest {

    private lateinit var calculator: Calculator

    @Before
    fun setUp() {
        calculator = Calculator()
    }

    @Test
    fun `addition of two positive numbers`() {
        // Arrange
        val a = 5
        val b = 3

        // Act
        val result = calculator.add(a, b)

        // Assert
        assertEquals(8, result)
    }

    @Test
    fun `division by zero throws exception`() {
        assertThrows(ArithmeticException::class.java) {
            calculator.divide(10, 0)
        }
    }
}
```

**Key Annotations:**
| Annotation | Purpose |
|---|---|
| `@Test` | Marks a test method |
| `@Before` | Runs before **each** test (setup) |
| `@After` | Runs after **each** test (cleanup) |
| `@BeforeClass` | Runs **once** before all tests (must be `static`/`@JvmStatic`) |
| `@AfterClass` | Runs **once** after all tests (must be `static`/`@JvmStatic`) |
| `@Ignore` | Skips a test (with optional reason) |
| `@Rule` | Declares a test rule (reusable setup/teardown) |

**JUnit 4 Test Lifecycle:**
```
  @BeforeClass          ← once for the entire test class
     │
     ├─── @Before       ← before EACH test method
     │      │
     │      ├── @Test   ← test method runs
     │      │
     │      └── @After  ← after EACH test method
     │
     ├─── @Before       (repeats for next @Test)
     │      │
     │      ├── @Test
     │      │
     │      └── @After
     │
  @AfterClass           ← once after all tests
```

---

#### JUnit 5 (Jupiter)

JUnit 5 is the modern version with richer features.

**Setup:**
```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

**JUnit 5 Test:**
```kotlin
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CalculatorJUnit5Test {

    private lateinit var calculator: Calculator

    @BeforeEach
    fun setUp() {
        calculator = Calculator()
    }

    @Test
    @DisplayName("Adding two numbers returns correct sum")
    fun additionTest() {
        assertEquals(8, calculator.add(5, 3))
    }

    @ParameterizedTest
    @CsvSource("1,1,2", "2,3,5", "10,20,30")
    fun `parameterized addition test`(a: Int, b: Int, expected: Int) {
        assertEquals(expected, calculator.add(a, b))
    }

    @Nested
    @DisplayName("Division tests")
    inner class DivisionTests {
        @Test
        fun `divides correctly`() {
            assertEquals(5, calculator.divide(10, 2))
        }

        @Test
        fun `throws on divide by zero`() {
            assertThrows<ArithmeticException> {
                calculator.divide(10, 0)
            }
        }
    }
}
```

**JUnit 5 Advantages over JUnit 4:**
- `@Nested` for grouping related tests
- `@DisplayName` for readable test names
- `@ParameterizedTest` for data-driven tests
- `@RepeatedTest` for repeating tests
- Better extension model (`@ExtendWith` replaces `@Rule`)

---

#### Arrange-Act-Assert (AAA) Pattern

Every test should follow this structure:

```
  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
  │   ARRANGE    │      │     ACT      │      │   ASSERT     │
  │              │ ───► │              │ ───► │              │
  │ Set up       │      │ Execute the  │      │ Verify the   │
  │ objects,     │      │ method or    │      │ outcome      │
  │ test data,   │      │ action under │      │ matches      │
  │ mocks        │      │ test         │      │ expectations │
  └──────────────┘      └──────────────┘      └──────────────┘
```

```kotlin
@Test
fun `user login with valid credentials returns success`() {
    // ARRANGE – set up the objects and data
    val repository = FakeUserRepository()
    val useCase = LoginUseCase(repository)
    val credentials = Credentials("user@mail.com", "password123")

    // ACT – perform the action being tested
    val result = useCase.login(credentials)

    // ASSERT – verify the outcome
    assertTrue(result.isSuccess)
    assertEquals("user@mail.com", result.getOrNull()?.email)
}
```

---

#### Assertions and Matchers

**JUnit Assertions:**
```kotlin
assertEquals(expected, actual)
assertNotEquals(unexpected, actual)
assertTrue(condition)
assertFalse(condition)
assertNull(value)
assertNotNull(value)
assertThrows<ExceptionType> { /* code */ }
assertAll(
    { assertEquals(1, 1) },
    { assertTrue(true) }
)
```

**Hamcrest Matchers (often used with JUnit 4):**
```kotlin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*

assertThat(result, `is`(5))
assertThat(list, hasSize(3))
assertThat(list, contains("a", "b", "c"))
assertThat(list, hasItem("a"))
assertThat(name, startsWith("And"))
assertThat(value, greaterThan(10))
assertThat(map, hasKey("name"))
```

---

### 9.1.2 Mocking and Fakes

Real classes have dependencies (network, database, other classes). In testing, you **don't want** to make real API calls or write to a real database. Instead, you replace dependencies with **test doubles** — lightweight stand-ins that let you test your class in isolation.

**Why isolation matters:**
- Tests are **deterministic** — no flaky network calls.
- Tests are **fast** — no waiting for slow I/O.
- Tests are **focused** — a failure points to exactly one class.

#### Types of Test Doubles

```
                  ┌─────────────────────────┐
                  │   System Under Test     │
                  │  (class being tested)   │
                  └────────────┬────────────┘
                               │ depends on
                               ▼
                  ┌─────────────────────────┐
                  │      Test Double         │
                  │ (replaces real dep.)     │
                  └────┬────┬────┬────┬────┬┘
                       │    │    │    │    │
         ┌───────────┘    │    │    │    └────────┐
         ▼              ▼    │    ▼              ▼
  ┌──────────┐  ┌───────┐ │ ┌───────┐  ┌────────┐
  │  Dummy   │  │ Stub  │ │ │ Fake  │  │  Spy   │
  ├──────────┤  ├───────┤ │ ├───────┤  ├────────┤
  │ Passed   │  │Returns│ ▼ │Working│  │Real obj│
  │ but not  │  │preset │┌───────┐light- │  │partial │
  │ used.    │  │values.││ Mock  │weight │  │override│
  │ Fills a  │  │       │├───────┤impl.  │  │        │
  │ param.   │  │       ││Records│       │  │        │
  └──────────┘  └───────┘│& veri-│       │  └────────┘
                        │fies   │       │
                        │calls. │       │
                        └───────┘       │
                                └───────┘
```

| Type | Description | Example |
|---|---|---|
| **Mock** | Records interactions, verifies calls were made | `verify(repo).save(user)` |
| **Stub** | Returns predefined values | `every { repo.getUser() } returns user` |
| **Fake** | A working lightweight implementation | `FakeRepository` using an in-memory list |
| **Spy** | A real object with some methods overridden | Partially mocked object |
| **Dummy** | Passed around but never used | An empty object filling a parameter |

---

#### MockK vs Mockito – When to Use

```
                Which mocking framework?
                         │
         ┌───────────────┼───────────────────┐
         │               │                   │
         ▼               ▼                   ▼
  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐
  │   MockK     │  │   Mockito    │  │  Fake Impl.   │
  │             │  │              │  │               │
  │ Pure Kotlin │  │ Java interop │  │ No mocking    │
  │ project     │  │ needed       │  │ needed        │
  │             │  │              │  │               │
  │ • Kotlin DSL│  │ • Mature     │  │ • In-memory   │
  │ • coEvery/  │  │ • Java API   │  │   data source │
  │   coVerify  │  │ • mockito-   │  │ • Realistic   │
  │ • Object    │  │   kotlin     │  │   behavior    │
  │   mocking   │  │ • Wide       │  │ • No framework│
  │ • Extension │  │   community  │  │   dependency  │
  │   functions │  │              │  │               │
  └─────────────┘  └──────────────┘  └───────────────┘
```

#### MockK (Kotlin-first Mocking)

MockK is the **preferred** mocking framework for Kotlin.

**Setup:**
```kotlin
dependencies {
    testImplementation("io.mockk:mockk:1.13.8")
}
```

**Basic Mocking:**
```kotlin
import io.mockk.*

class UserViewModelTest {

    private val repository: UserRepository = mockk()
    private lateinit var viewModel: UserViewModel

    @Before
    fun setUp() {
        viewModel = UserViewModel(repository)
    }

    @Test
    fun `getUser returns user from repository`() {
        // Stub
        val expectedUser = User("John", "john@mail.com")
        every { repository.getUser("123") } returns expectedUser

        // Act
        val result = viewModel.getUser("123")

        // Assert
        assertEquals(expectedUser, result)

        // Verify interaction
        verify(exactly = 1) { repository.getUser("123") }
    }
}
```

**MockK Features:**
```kotlin
// Relaxed mock – returns default values for unstubbed calls
val relaxedMock: UserRepository = mockk(relaxed = true)

// Capturing arguments
val slot = slot<String>()
every { repository.getUser(capture(slot)) } returns user
viewModel.getUser("123")
assertEquals("123", slot.captured)

// Mocking suspend functions
coEvery { repository.fetchUser("123") } returns user
coVerify { repository.fetchUser("123") }

// Mocking companion objects / top-level functions
mockkObject(MyCompanion)
every { MyCompanion.create() } returns fakeInstance

// Mocking static methods
mockkStatic(Log::class)
every { Log.d(any(), any()) } returns 0

// Spying on real objects
val spy = spyk(RealCalculator())
every { spy.complexMethod() } returns 42
// Other methods still work normally

// Verify ordering
verifyOrder {
    repository.getUser("123")
    repository.saveUser(any())
}

// Verify no more interactions
confirmVerified(repository)
```

---

#### Mockito (Java Interop)

Mockito is widely used, especially for Java code.

**Setup:**
```kotlin
dependencies {
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}
```

**Usage:**
```kotlin
import org.mockito.kotlin.*

class UserServiceTest {

    private val repository: UserRepository = mock()

    @Test
    fun `fetch user delegates to repository`() {
        // Stub
        val user = User("John", "john@mail.com")
        whenever(repository.getUser("123")).thenReturn(user)

        // Act
        val service = UserService(repository)
        val result = service.getUser("123")

        // Assert
        assertEquals(user, result)
        verify(repository).getUser("123")
        verifyNoMoreInteractions(repository)
    }
}
```

---

#### Fake Implementations

Fakes are often **better than mocks** for repositories and data sources because they behave like real implementations.

```kotlin
// Interface
interface NoteRepository {
    suspend fun getAll(): List<Note>
    suspend fun insert(note: Note)
    suspend fun delete(id: String)
    suspend fun getById(id: String): Note?
}

// Fake for testing
class FakeNoteRepository : NoteRepository {

    private val notes = mutableListOf<Note>()

    override suspend fun getAll(): List<Note> = notes.toList()

    override suspend fun insert(note: Note) {
        notes.add(note)
    }

    override suspend fun delete(id: String) {
        notes.removeAll { it.id == id }
    }

    override suspend fun getById(id: String): Note? = notes.find { it.id == id }
}

// Test using the fake
class NoteViewModelTest {

    private val repository = FakeNoteRepository()
    private lateinit var viewModel: NoteViewModel

    @Before
    fun setUp() {
        viewModel = NoteViewModel(repository)
    }

    @Test
    fun `adding a note updates the list`() = runTest {
        val note = Note("1", "Title", "Content")

        viewModel.addNote(note)

        val notes = viewModel.notes.first()
        assertEquals(1, notes.size)
        assertEquals("Title", notes[0].title)
    }
}
```

**When to use what:**
- **Mock**: When you only care about verifying interactions (was `save()` called?)
- **Fake**: When you need realistic behavior (in-memory database)
- **Stub**: When you just need a return value

---

### 9.1.3 Coroutines Testing

Testing coroutines is tricky because they are asynchronous by nature. In production, coroutines run on `Dispatchers.Main` or `Dispatchers.IO`, but these don't exist in a plain JVM test environment. The solution: **replace real dispatchers with test dispatchers** that give you full control over execution timing.

**Key concepts:**
- `Dispatchers.Main` doesn't exist on JVM — you must replace it with `setMain()`.
- `runTest` creates a controlled coroutine scope that skips `delay()` calls.
- `TestDispatcher` lets you decide WHEN queued coroutines execute.

```
  PRODUCTION                              TEST
  ──────────                              ────
  ViewModel                               runTest { }
    ├── Dispatchers.Main  ───replaced───►  TestDispatcher
    └── Dispatchers.IO    ───replaced───►  TestDispatcher
                                             │
                                  ┌─────────┴──────────┐
                                  │                    │
                                  ▼                    ▼
                       ┌───────────────┐   ┌────────────────┐
                       │ Standard      │   │ Unconfined     │
                       │ TestDisp.     │   │ TestDisp.      │
                       ├───────────────┤   ├────────────────┤
                       │ Queues work   │   │ Executes work  │
                       │ You call      │   │ IMMEDIATELY    │
                       │ advanceUntil  │   │ (no manual     │
                       │ Idle() to run │   │  advance)      │
                       └───────────────┘   └────────────────┘
```

**Setup:**
```kotlin
dependencies {
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

---

#### StandardTestDispatcher vs UnconfinedTestDispatcher

```kotlin
// StandardTestDispatcher – does NOT execute coroutines eagerly
// You must advance time manually
@Test
fun `standard dispatcher example`() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = MyViewModel(dispatcher)

    viewModel.loadData() // coroutine is queued but not yet executed

    advanceUntilIdle() // now it executes

    assertEquals(expected, viewModel.data.value)
}

// UnconfinedTestDispatcher – executes coroutines eagerly
@Test
fun `unconfined dispatcher example`() = runTest(UnconfinedTestDispatcher()) {
    val viewModel = MyViewModel(coroutineContext[CoroutineDispatcher]!!)

    viewModel.loadData() // executes immediately

    assertEquals(expected, viewModel.data.value)
}
```

---

#### runTest

`runTest` is the **standard** way to test coroutines. It skips delays automatically.

```kotlin
class UserRepositoryTest {

    @Test
    fun `fetchUser returns user after network call`() = runTest {
        val api = mockk<UserApi>()
        coEvery { api.getUser("123") } returns UserDto("John", "john@mail.com")

        val repository = UserRepositoryImpl(api)
        val user = repository.fetchUser("123")

        assertEquals("John", user.name)
    }
}
```

**Testing ViewModel with coroutines:**
```kotlin
class MyViewModelTest {

    // Replace Main dispatcher in tests
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeUserRepository()
    private lateinit var viewModel: UserViewModel

    @Before
    fun setUp() {
        viewModel = UserViewModel(repository)
    }

    @Test
    fun `loading users updates state`() = runTest {
        repository.addUsers(listOf(User("1", "John")))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).users.size)
    }
}

// MainDispatcherRule – a reusable rule for replacing Dispatchers.Main
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

---

#### Testing Flows with Turbine

Turbine is a small but powerful library for testing Kotlin `Flow`s. Without Turbine, testing flows requires `first()`, `toList()`, or manual collectors — which are awkward and error-prone. Turbine gives you a clean `test { }` block where you can `awaitItem()` for each emission.

```
  Test                      Flow                  Turbine
   │                         │                      │
   │── flow.test { } ─────►│                      │
   │                         │── emit(Loading) ───►│
   │◄─ awaitItem() = Loading │                      │
   │   assert: Loading ✓     │                      │
   │                         │── emit(Success) ───►│
   │◄─ awaitItem() = Success │                      │
   │   assert: Success ✓     │                      │
   │                         │                      │
   │── cancelAndIgnore... ─►│                      │
   │                         │◄─ cancel ─────────│
   ▼                         ▼                      ▼
```

**Setup:**
```kotlin
dependencies {
    testImplementation("app.cash.turbine:turbine:1.0.0")
}
```

**Usage:**
```kotlin
import app.cash.turbine.test

class NoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `notes flow emits loading then success`() = runTest {
        val repository = FakeNoteRepository()
        repository.addNote(Note("1", "Title", "Content"))

        val viewModel = NoteViewModel(repository)

        viewModel.uiState.test {
            // First emission: Loading
            assertEquals(UiState.Loading, awaitItem())

            // Second emission: Success
            val success = awaitItem()
            assertTrue(success is UiState.Success)
            assertEquals(1, (success as UiState.Success).notes.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search flow debounces and emits results`() = runTest {
        val viewModel = SearchViewModel(FakeSearchRepository())

        viewModel.searchResults.test {
            // Initial empty state
            assertEquals(emptyList<String>(), awaitItem())

            viewModel.onSearchQueryChanged("kot")
            // Turbine will wait for the next emission
            val results = awaitItem()
            assertTrue(results.isNotEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

**Turbine API:**
```kotlin
flow.test {
    awaitItem()              // Wait for next emission
    awaitComplete()          // Wait for flow completion
    awaitError()             // Wait for an error
    expectNoEvents()         // Assert nothing emitted (no item, complete, or error)
    cancelAndIgnoreRemainingEvents()  // Cancel and clean up
    cancelAndConsumeRemainingEvents() // Cancel and return remaining events
}
```

---

## 9.2 Instrumentation Testing

Instrumentation tests run on a **real device or emulator**. Unlike unit tests, they have access to the real Android framework — `Context`, `Activity`, `Fragment`, the UI thread, sensors, file system, and databases.

**When to use instrumented tests:**
- Testing UI interactions (clicking buttons, scrolling lists).
- Verifying real database operations (Room DAOs).
- Testing navigation between screens.
- Validating behavior that depends on the Android OS (permissions, intents, lifecycle).

**Trade-offs:**
- Slower than unit tests (seconds to minutes per test).
- Require a running device or emulator.
- More brittle — can fail due to device state, animations, or timing.

Location: `app/src/androidTest/java/`

**Setup:**
```kotlin
dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

---

### AndroidJUnit4

All Android instrumented tests use the `AndroidJUnit4` runner.

```kotlin
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppContextTest {

    @Test
    fun useAppContext() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.practiceapp", context.packageName)
    }
}
```

---

### ActivityScenario

`ActivityScenario` launches and controls an Activity in tests.

```kotlin
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule

class MainActivityTest {

    // Option 1: Using a Rule (auto-launches and closes)
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun activityLaunches() {
        activityRule.scenario.onActivity { activity ->
            assertNotNull(activity)
        }
    }

    // Option 2: Manual launch
    @Test
    fun manualLaunch() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            assertEquals("My App", activity.title)
        }

        // Simulate lifecycle changes
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.recreate() // simulate config change

        scenario.close()
    }

    // Option 3: Launch with intent
    @Test
    fun launchWithIntent() {
        val intent = Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            DetailActivity::class.java
        ).apply {
            putExtra("ITEM_ID", "123")
        }

        val scenario = ActivityScenario.launch<DetailActivity>(intent)
        scenario.onActivity { activity ->
            // verify activity received the intent
        }
        scenario.close()
    }
}
```

---

### FragmentScenario

`FragmentScenario` launches a Fragment in isolation.

**Setup:**
```kotlin
dependencies {
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
}
```

**Usage:**
```kotlin
import androidx.fragment.app.testing.launchFragmentInContainer

class NoteDetailFragmentTest {

    @Test
    fun fragmentDisplaysNoteTitle() {
        val bundle = bundleOf("noteId" to "123")

        val scenario = launchFragmentInContainer<NoteDetailFragment>(bundle)

        scenario.onFragment { fragment ->
            assertNotNull(fragment.view)
        }

        // Use Espresso to verify UI
        onView(withId(R.id.noteTitle))
            .check(matches(withText("My Note")))
    }

    @Test
    fun fragmentRecreation() {
        val scenario = launchFragmentInContainer<NoteDetailFragment>()
        scenario.recreate() // simulate config change
        // Verify state is preserved
    }
}
```

---

### Espresso (UI Testing)

Espresso is Android's standard UI testing framework for view-based UIs (XML layouts). It **automatically synchronizes** with the UI thread and `AsyncTask` pool — so you don't need `Thread.sleep()` hacks.

**Espresso's 3-step pattern:** Find a view → Do something → Check something.

```
  ┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
  │ onView(Matcher)  │      │ perform(Action)  │      │ check(Assertion) │
  │                  │ ───► │                  │ ───► │                  │
  │ Find a view by   │      │ click()          │      │ isDisplayed()    │
  │ ID, text, hint,  │      │ typeText()       │      │ withText()       │
  │ contentDesc, etc │      │ scrollTo()       │      │ isEnabled()      │
  │                  │      │ swipe()          │      │ doesNotExist()   │
  └──────────────────┘      └──────────────────┘      └──────────────────┘
```

**Core pattern:**
```
onView(ViewMatcher)       // Find a view
    .perform(ViewAction)  // Do something
    .check(ViewAssertion) // Verify something
```

---

#### View Matchers (Finding Views)

```kotlin
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*

// By ID
onView(withId(R.id.username))

// By text
onView(withText("Submit"))
onView(withText(R.string.submit))

// By hint
onView(withHint("Enter username"))

// By content description (accessibility)
onView(withContentDescription("Search"))

// By class
onView(isAssignableFrom(EditText::class.java))

// Combining matchers
onView(allOf(withId(R.id.title), withText("Hello")))
onView(allOf(withId(R.id.button), isDisplayed()))
onView(allOf(withId(R.id.item), not(isEnabled())))

// Parent/child relationships
onView(withParent(withId(R.id.toolbar)))
onView(isDescendantOfA(withId(R.id.container)))
onView(hasSibling(withText("Label")))

// By position in list (RecyclerView)
onView(withId(R.id.recyclerView))
    .perform(RecyclerViewActions.scrollToPosition<VH>(5))
```

---

#### View Actions (Interacting)

```kotlin
import androidx.test.espresso.action.ViewActions.*

// Click
onView(withId(R.id.button)).perform(click())
onView(withId(R.id.button)).perform(longClick())
onView(withId(R.id.button)).perform(doubleClick())

// Type text
onView(withId(R.id.editText)).perform(typeText("Hello World"))
onView(withId(R.id.editText)).perform(replaceText("New Text"))
onView(withId(R.id.editText)).perform(clearText())

// Close keyboard
onView(withId(R.id.editText)).perform(typeText("Hello"), closeSoftKeyboard())

// Scroll
onView(withId(R.id.scrollView)).perform(scrollTo())

// Swipe
onView(withId(R.id.viewPager)).perform(swipeLeft())
onView(withId(R.id.viewPager)).perform(swipeRight())
onView(withId(R.id.list)).perform(swipeUp())

// Multiple actions
onView(withId(R.id.editText))
    .perform(clearText(), typeText("New"), closeSoftKeyboard())
```

---

#### View Assertions (Verifying)

```kotlin
import androidx.test.espresso.assertion.ViewAssertions.*

// Is displayed
onView(withId(R.id.title)).check(matches(isDisplayed()))

// Has text
onView(withId(R.id.title)).check(matches(withText("Hello")))
onView(withId(R.id.title)).check(matches(withText(containsString("Hel"))))

// Is enabled/disabled
onView(withId(R.id.button)).check(matches(isEnabled()))
onView(withId(R.id.button)).check(matches(not(isEnabled())))

// Is checked (checkbox/radio)
onView(withId(R.id.checkbox)).check(matches(isChecked()))

// Does not exist
onView(withId(R.id.dialog)).check(doesNotExist())

// Is not displayed
onView(withId(R.id.loading)).check(matches(not(isDisplayed())))

// Has error text
onView(withId(R.id.editText)).check(matches(hasErrorText("Required")))

// RecyclerView item count
onView(withId(R.id.recyclerView))
    .check(matches(hasChildCount(5)))
```

---

#### Complete Espresso Example

```kotlin
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun successfulLogin_navigatesToHome() {
        // Enter username
        onView(withId(R.id.usernameEditText))
            .perform(typeText("user@example.com"), closeSoftKeyboard())

        // Enter password
        onView(withId(R.id.passwordEditText))
            .perform(typeText("password123"), closeSoftKeyboard())

        // Click login button
        onView(withId(R.id.loginButton))
            .perform(click())

        // Verify navigated to home screen
        onView(withId(R.id.welcomeText))
            .check(matches(withText("Welcome, user@example.com")))
    }

    @Test
    fun emptyUsername_showsError() {
        onView(withId(R.id.loginButton))
            .perform(click())

        onView(withId(R.id.usernameEditText))
            .check(matches(hasErrorText("Username is required")))
    }

    @Test
    fun loginButtonDisabled_whenFieldsEmpty() {
        onView(withId(R.id.loginButton))
            .check(matches(not(isEnabled())))
    }
}
```

---

#### Espresso with RecyclerView

```kotlin
dependencies {
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
}
```

```kotlin
import androidx.test.espresso.contrib.RecyclerViewActions

@Test
fun clickItemAtPosition() {
    onView(withId(R.id.recyclerView))
        .perform(RecyclerViewActions.actionOnItemAtPosition<VH>(3, click()))
}

@Test
fun scrollToItemAndClick() {
    onView(withId(R.id.recyclerView))
        .perform(
            RecyclerViewActions.scrollTo<VH>(
                hasDescendant(withText("Item 42"))
            )
        )

    onView(withText("Item 42")).perform(click())
}

@Test
fun performActionOnSpecificItem() {
    onView(withId(R.id.recyclerView))
        .perform(
            RecyclerViewActions.actionOnItem<VH>(
                hasDescendant(withText("Delete Me")),
                click()
            )
        )
}
```

---

#### Espresso Idling Resources

When your app does async work (network calls, database), Espresso needs to know when to wait.

```
  Espresso Test              IdlingResource           App (async work)
       │                          │                        │
       │── Register ─────────────►│                        │
       │                          │                        │
       │   onView().perform(      │                        │
       │     click())             │                        │
       │                          │◄── setIdle(false) ─────│ (busy)
       │                          │                        │
       │   ... ESPRESSO WAITS ... │                        │
       │                          │         Network call / │
       │                          │         DB query       │
       │                          │                        │
       │                          │◄── setIdle(true) ──────│ (done!)
       │◄── onTransitionToIdle() ─│                        │
       │                          │                        │
       │   onView().check(        │                        │
       │     matches(...))        │                        │
       │                          │                        │
       │── Unregister ───────────►│                        │
       │                          │                        │
```

```kotlin
// Step 1: Create an IdlingResource
class SimpleIdlingResource : IdlingResource {

    @Volatile
    private var isIdle = true
    private var callback: IdlingResource.ResourceCallback? = null

    override fun getName(): String = "SimpleIdlingResource"

    override fun isIdleNow(): Boolean = isIdle

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    fun setIdle(idle: Boolean) {
        isIdle = idle
        if (idle) callback?.onTransitionToIdle()
    }
}

// Step 2: Register in tests
@RunWith(AndroidJUnit4::class)
class DataLoadingTest {

    private val idlingResource = SimpleIdlingResource()

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(idlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun dataLoadsAndDisplays() {
        // Espresso will wait until idlingResource is idle
        onView(withId(R.id.dataList))
            .check(matches(isDisplayed()))
    }
}
```

---

### UI Automator (Cross-App Testing)

UI Automator tests can interact with **any app** on the device — system UI, notifications, settings, permission dialogs, or third-party apps. Unlike Espresso (which only sees your app), UI Automator works at the OS level.

**Use cases:**
- Granting runtime permissions (the system dialog is outside your app).
- Testing deep links from another app.
- Verifying notifications appear and are clickable.
- End-to-end flows that span multiple apps.

**Setup:**
```kotlin
dependencies {
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
}
```

**Usage:**
```kotlin
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

@RunWith(AndroidJUnit4::class)
class SystemInteractionTest {

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun openNotificationShade() {
        device.openNotification()

        // Wait for notification to appear
        device.wait(Until.hasObject(By.text("My Notification")), 5000)

        // Click notification
        val notification = device.findObject(UiSelector().text("My Notification"))
        notification.click()
    }

    @Test
    fun pressHomeAndReturnToApp() {
        device.pressHome()

        // Find and launch app from launcher
        val appIcon = device.findObject(
            UiSelector().description("My App")
        )
        appIcon.clickAndWaitForNewWindow()
    }

    @Test
    fun grantPermissionDialog() {
        // Trigger permission request in your app...

        // Handle system permission dialog
        val allowButton = device.findObject(
            UiSelector()
                .packageName("com.google.android.permissioncontroller")
                .text("Allow")
        )
        if (allowButton.exists()) {
            allowButton.click()
        }
    }

    @Test
    fun interactWithAnotherApp() {
        // Launch the calculator
        val intent = device.getLauncherPackageName()
        val context = InstrumentationRegistry.getInstrumentation().context
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage("com.google.android.calculator")
        context.startActivity(launchIntent)

        // Wait for the app
        device.wait(Until.hasObject(By.pkg("com.google.android.calculator")), 5000)

        // Interact
        device.findObject(By.text("5")).click()
        device.findObject(By.text("+")).click()
        device.findObject(By.text("3")).click()
        device.findObject(By.text("=")).click()
    }
}
```

---

## 9.3 Compose Testing

Jetpack Compose has its own testing framework that works fundamentally differently from Espresso. Instead of searching through a view hierarchy, Compose tests query the **Semantics Tree** — a tree of accessibility information that every composable exposes.

**Key differences from Espresso:**
| Espresso (Views)  | Compose Testing |
|---|---|
| Finds views by ID (`R.id.button`) | Finds nodes by text, tag, or semantics |
| Works with View hierarchy | Works with Semantics tree |
| `onView(withId(...))` | `onNodeWithTag(...)` |
| XML layout based | `@Composable` function based |
| `ViewMatchers` / `ViewActions` | Semantic matchers / `perform*` |

**What is the Semantics Tree?**
Every composable can expose semantic information (text, click action, toggle state, content description). Compose testing reads this tree — the same tree used by accessibility services like TalkBack.

```
  Semantics Tree (what tests see)         Visual Tree (what users see)
  ─────────────────────────────         ─────────────────────────────
  Node: Column                            ┌────────────────┐
    ├─ Node: Text("Hello")                │   Hello          │
    └─ Node: Button                       │                  │
         text = "Click me"                │  [ Click me ]    │
         onClick = { ... }                │                  │
         testTag = "my_button"            └────────────────┘
```

**Setup:**
```kotlin
dependencies {
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
}
```

---

### ComposeTestRule

The `ComposeTestRule` is the entry point for all Compose tests. It provides `setContent {}` to render composables and finder methods to query the semantics tree.

```
  ComposeTestRule
   │
   ├── setContent { }    ──►  Renders composable in a test host
   │
   ├── onNodeWith*()     ──►  Find a single node (throws if 0 or 2+)
   ├── onAllNodesWith*() ──►  Find all matching nodes
   │
   ├── waitForIdle()     ──►  Wait for all pending recompositions
   └── mainClock         ──►  Control animation/frame timing
```

```kotlin
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule

@RunWith(AndroidJUnit4::class)
class GreetingTest {

    // Option 1: Compose-only (no Activity)
    @get:Rule
    val composeTestRule = createComposeRule()

    // Option 2: With Activity
    // @get:Rule
    // val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun greetingDisplaysName() {
        composeTestRule.setContent {
            MyAppTheme {
                Greeting(name = "Android")
            }
        }

        composeTestRule
            .onNodeWithText("Hello, Android!")
            .assertIsDisplayed()
    }
}
```

---

### Semantics and Semantic Matchers (Finders)

Compose tests use the **semantics tree** (not the view hierarchy).

```kotlin
// Find by text
composeTestRule.onNodeWithText("Hello")
composeTestRule.onNodeWithText("Hello", substring = true)
composeTestRule.onNodeWithText("hello", ignoreCase = true)

// Find by content description
composeTestRule.onNodeWithContentDescription("Search icon")

// Find by tag (set via Modifier.testTag)
composeTestRule.onNodeWithTag("login_button")

// Find all matching nodes
composeTestRule.onAllNodesWithText("Item")
composeTestRule.onAllNodesWithTag("list_item")

// Semantic matchers
composeTestRule.onNode(hasText("Hello"))
composeTestRule.onNode(hasClickAction())
composeTestRule.onNode(isToggleable())
composeTestRule.onNode(isFocused())

// Combine matchers
composeTestRule.onNode(
    hasText("Submit") and hasClickAction()
)
composeTestRule.onNode(
    hasTestTag("button") and isEnabled()
)

// Hierarchy matchers
composeTestRule.onNode(
    hasParent(hasTestTag("form"))
)
composeTestRule.onNode(
    hasAnyChild(hasText("Child"))
)
```

**Setting testTag:**
```kotlin
@Composable
fun LoginScreen() {
    Button(
        onClick = { /* ... */ },
        modifier = Modifier.testTag("login_button")
    ) {
        Text("Login")
    }
}
```

---

### Testing Interactions and State

```kotlin
@Test
fun counterIncrements() {
    composeTestRule.setContent {
        CounterScreen()
    }

    // Initial state
    composeTestRule.onNodeWithText("Count: 0").assertIsDisplayed()

    // Click increment button
    composeTestRule.onNodeWithTag("increment_button").performClick()

    // Verify state updated
    composeTestRule.onNodeWithText("Count: 1").assertIsDisplayed()
}

@Test
fun textFieldInput() {
    composeTestRule.setContent {
        SearchBar()
    }

    composeTestRule
        .onNodeWithTag("search_field")
        .performTextInput("Kotlin")

    composeTestRule
        .onNodeWithText("Kotlin")
        .assertIsDisplayed()
}

@Test
fun scrollAndClick() {
    composeTestRule.setContent {
        LongList(items = (1..100).map { "Item $it" })
    }

    composeTestRule
        .onNodeWithTag("lazy_list")
        .performScrollToIndex(50)

    composeTestRule
        .onNodeWithText("Item 51")
        .assertIsDisplayed()
        .performClick()
}
```

**Full list of actions:**
```kotlin
.performClick()
.performDoubleClick()
.performLongClick()
.performTextInput("text")
.performTextClearance()
.performTextReplacement("new text")
.performScrollTo()
.performScrollToIndex(index)
.performScrollToKey(key)
.performTouchInput { swipeLeft() }
.performTouchInput { swipeUp() }
```

**Full list of assertions:**
```kotlin
.assertIsDisplayed()
.assertIsNotDisplayed()
.assertExists()
.assertDoesNotExist()
.assertIsEnabled()
.assertIsNotEnabled()
.assertIsSelected()
.assertIsNotSelected()
.assertIsOn()        // for toggles
.assertIsOff()
.assertIsFocused()
.assertTextEquals("text")
.assertTextContains("partial")
.assertHasClickAction()
.assertHasNoClickAction()
.assertContentDescriptionEquals("description")
```

---

### Complete Compose Test Example

```kotlin
@RunWith(AndroidJUnit4::class)
class TodoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addTodo_appearsInList() {
        composeTestRule.setContent {
            MyAppTheme {
                TodoScreen(viewModel = TodoViewModel(FakeTodoRepository()))
            }
        }

        // Type a new todo
        composeTestRule
            .onNodeWithTag("todo_input")
            .performTextInput("Buy groceries")

        // Click add button
        composeTestRule
            .onNodeWithTag("add_button")
            .performClick()

        // Verify it appears in the list
        composeTestRule
            .onNodeWithText("Buy groceries")
            .assertIsDisplayed()

        // Verify input is cleared
        composeTestRule
            .onNodeWithTag("todo_input")
            .assertTextEquals("")
    }

    @Test
    fun completeTodo_showsStrikethrough() {
        composeTestRule.setContent {
            MyAppTheme {
                TodoItem(
                    todo = Todo("1", "Test", false),
                    onToggle = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag("checkbox_1")
            .performClick()

        // Depending on your implementation, verify visual change
        composeTestRule
            .onNodeWithTag("checkbox_1")
            .assertIsOn()
    }
}
```

---

### Screenshot Testing

Screenshot tests capture the visual output and compare against a baseline.

**Using Roborazzi (popular choice):**
```kotlin
dependencies {
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.7.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.7.0")
}
```

```kotlin
@RunWith(ParameterizedRobolectricTestRunner::class)
class ScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_lightTheme() {
        composeTestRule.setContent {
            MyAppTheme(darkTheme = false) {
                LoginScreen()
            }
        }

        composeTestRule
            .onRoot()
            .captureRoboImage("screenshots/login_light.png")
    }

    @Test
    fun loginScreen_darkTheme() {
        composeTestRule.setContent {
            MyAppTheme(darkTheme = true) {
                LoginScreen()
            }
        }

        composeTestRule
            .onRoot()
            .captureRoboImage("screenshots/login_dark.png")
    }
}
```

---

## 9.4 Test Architecture

Test architecture defines **how** and **when** you write tests. Good architecture keeps your test suite fast, maintainable, and valuable.

### Test-Driven Development (TDD)

TDD flips the traditional workflow: you write the **test first**, then write just enough code to make it pass, then clean up. This guarantees every line of production code is backed by a test.

**TDD Cycle: Red → Green → Refactor**

```
       ┌──────────────────────────────────────────────┐
       │                                              │
       ▼                                              │
  ┌─────────┐      ┌─────────┐      ┌────────────┐   │
  │   RED   │ ───► │  GREEN  │ ───► │  REFACTOR  │ ──┘
  ├─────────┤      ├─────────┤      ├────────────┤
  │ Write a │      │ Write   │      │ Clean up   │
  │ failing │      │ minimum │      │ code while │
  │ test.   │      │ code to │      │ keeping    │
  │ It must │      │ make    │      │ tests      │
  │ NOT     │      │ the test│      │ green.     │
  │ pass.   │      │ pass.   │      │            │
  └─────────┘      └─────────┘      └────────────┘
```

**Benefits of TDD:**
- Forces you to think about the API/interface before implementation.
- Every feature has a test from day one. No "I'll write tests later" debt.
- Naturally produces small, focused, testable functions.

**When TDD works well:** Business logic, validation, parsing, algorithms.
**When TDD is overkill:** Prototyping UI, exploratory code, simple CRUD.

**Example – TDD for a password validator:**
```kotlin
// Step 1 (RED): Write the test first
class PasswordValidatorTest {

    private val validator = PasswordValidator()

    @Test
    fun `empty password is invalid`() {
        assertFalse(validator.isValid(""))
    }

    @Test
    fun `password shorter than 8 chars is invalid`() {
        assertFalse(validator.isValid("Ab1!xyz"))
    }

    @Test
    fun `password without uppercase is invalid`() {
        assertFalse(validator.isValid("abcdefg1!"))
    }

    @Test
    fun `password without digit is invalid`() {
        assertFalse(validator.isValid("Abcdefgh!"))
    }

    @Test
    fun `valid password passes all checks`() {
        assertTrue(validator.isValid("Abcdefg1!"))
    }
}

// Step 2 (GREEN): Write minimum implementation
class PasswordValidator {
    fun isValid(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isUpperCase() }) return false
        if (!password.any { it.isDigit() }) return false
        return true
    }
}

// Step 3 (REFACTOR): Improve if needed
```

---

### Behavior-Driven Development (BDD)

BDD shifts the focus from testing implementation details to testing **observable behavior**. Tests read like specifications in plain English using the **Given-When-Then** pattern.

```
  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
  │    GIVEN     │      │     WHEN     │      │     THEN     │
  ├──────────────┤ ───► ├──────────────┤ ───► ├──────────────┤
  │ Set up the   │      │ Perform the  │      │ Verify the   │
  │ initial      │      │ action or    │      │ expected     │
  │ state.       │      │ event.       │      │ outcome.     │
  │              │      │              │      │              │
  │ "Given an    │      │ "When user   │      │ "Then cart   │
  │  empty cart" │      │  adds item"  │      │  has 1 item" │
  └──────────────┘      └──────────────┘      └──────────────┘
```

**BDD vs AAA:** They're the same structure with different names. `Given = Arrange`, `When = Act`, `Then = Assert`. BDD is just more readable for non-developers and better describes user-facing behavior.
```kotlin
class ShoppingCartTest {

    @Test
    fun `given empty cart, when item added, then cart has one item`() {
        // Given
        val cart = ShoppingCart()

        // When
        cart.addItem(Product("Laptop", 999.99))

        // Then
        assertEquals(1, cart.itemCount)
        assertEquals(999.99, cart.total, 0.01)
    }

    @Test
    fun `given cart with items, when cleared, then cart is empty`() {
        // Given
        val cart = ShoppingCart()
        cart.addItem(Product("Laptop", 999.99))
        cart.addItem(Product("Mouse", 29.99))

        // When
        cart.clear()

        // Then
        assertEquals(0, cart.itemCount)
        assertEquals(0.0, cart.total, 0.01)
    }
}
```

---

### Testing Pyramid

```
           /\              ← UI / E2E Tests (~10%)
          /  \                Espresso, Compose Test, UI Automator
         / UI \               Slowest | Runs on device
        /──────\           ← Integration Tests (~20%)
       / Integ. \             Robolectric, Room In-Memory,
      /──────────\            FragmentScenario
     /            \           Medium speed
    /  Unit Tests  \       ← Unit Tests (~70%)
   /────────────────\         JUnit, MockK, Turbine, Truth
  /                  \        Fastest | Runs on JVM
 └────────────────────┘
```

| Level | Speed | Scope | Tools | Percentage |
|---|---|---|---|---|
| **Unit** | Very fast (ms) | Single class/function | JUnit, MockK, Turbine | ~70% |
| **Integration** | Medium (seconds) | Multiple components | Robolectric, Room in-memory | ~20% |
| **UI/E2E** | Slow (seconds-minutes) | Full user flow | Espresso, Compose Test, UI Automator | ~10% |

---

### Code Coverage

Code coverage measures **what percentage** of your code is executed when tests run. It answers: "Which lines/branches/methods did my tests actually touch?"

**Important:** Coverage tells you what you tested, NOT how well you tested it. A test with no assertions still counts as "covered" code.

```
  Code Coverage Pipeline:

  Run Tests ──► JaCoCo (instruments bytecode) ──► Coverage Report (HTML/XML)
                                                        │
                                          ┌─────────────┼─────────────┐
                                          ▼             ▼             ▼
                                    Line Coverage  Branch Coverage  Method Coverage
                                    % of lines     % of if/else    % of methods
                                    executed       paths taken     called
```

**How JaCoCo works:** JaCoCo instruments your compiled bytecode (`.class` files), adding probes that record which lines execute. After tests run, it generates an HTML/XML report showing green (covered) and red (missed) lines.

**Enable with JaCoCo:**
```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
}
```

**Run coverage:**
```bash
./gradlew testDebugUnitTest          # unit tests
./gradlew createDebugCoverageReport  # instrumented tests
# Reports: app/build/reports/coverage/
```

**Coverage types:**
| Type | Meaning |
|---|---|
| **Line coverage** | % of lines executed |
| **Branch coverage** | % of branches (if/else) taken |
| **Method coverage** | % of methods called |
| **Class coverage** | % of classes instantiated |

**Reasonable targets:**
- Business logic (UseCases, Repositories): 80-90%
- ViewModels: 70-80%
- UI code: 50-60%
- Overall project: 60-70%

> **Warning:** High coverage ≠ high quality. A test that executes code but doesn't assert anything gives coverage without value.

---

## 9.5 Additional Testing Tools

### Robolectric (JVM-based Android Tests)

Robolectric solves a fundamental problem: many Android classes (`Context`, `SharedPreferences`, `Resources`, `Intent`) can't be used in plain JVM unit tests because they're tied to the Android OS. Robolectric provides **shadow implementations** of these classes that run entirely on the JVM.

**Why Robolectric exists:** You want the speed of unit tests but need Android framework APIs.

```
  Without Robolectric:                   With Robolectric:
  ───────────────────                   ──────────────────
  context.getString()                    context.getString()
       │                                     │
       ▼                                     ▼
  Android OS (real)                      Shadow (JVM fake)
       │                                     │
       ▼                                     ▼
  CRASH! Not available                   Returns "My App"
  in JVM tests.                          Works! Fast!
```

**Setup:**
```kotlin
dependencies {
    testImplementation("org.robolectric:robolectric:4.11.1")
}

// In app/build.gradle.kts
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}
```

**Usage:**
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SharedPreferencesTest {

    @Test
    fun `save and read from SharedPreferences`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("test", Context.MODE_PRIVATE)

        prefs.edit().putString("key", "value").apply()

        assertEquals("value", prefs.getString("key", null))
    }
}

@RunWith(RobolectricTestRunner::class)
class ResourceTest {

    @Test
    fun `reads string resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PracticeApp", appName)
    }
}

@RunWith(RobolectricTestRunner::class)
class IntentTest {

    @Test
    fun `clicking button starts new activity`() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            activity.findViewById<Button>(R.id.detailButton).performClick()

            val shadowActivity = Shadows.shadowOf(activity)
            val intent = shadowActivity.nextStartedActivity

            assertEquals(
                DetailActivity::class.java.name,
                intent.component?.className
            )
        }
    }
}
```

**When to use Robolectric vs Instrumented tests:**
| Robolectric | Instrumented |
|---|---|
| Runs on JVM (fast) | Runs on device (slow) |
| Simulated Android | Real Android framework |
| Great for unit/integration | Needed for complex UI |
| CI-friendly | Requires emulator/device |
| Some APIs not fully supported | Full API support |

---

### Truth (Fluent Assertions)

Google's Truth library replaces JUnit's assertion methods with a **fluent, readable API** and produces much better failure messages. When a test fails, the error message tells you exactly what went wrong.

**Why Truth over JUnit assertions:**
- `assertThat(list).contains("a")` reads like English.
- Failure message: `expected to contain: a, but was: [b, c]` (vs JUnit's `expected true`).
- IDE autocomplete guides you to the right assertion method.

**Setup:**
```kotlin
dependencies {
    testImplementation("com.google.truth:truth:1.1.5")
}
```

**Usage:**
```kotlin
import com.google.truth.Truth.assertThat

class TruthExamplesTest {

    @Test
    fun `basic assertions`() {
        assertThat(4 + 4).isEqualTo(8)
        assertThat("Hello World").contains("World")
        assertThat("Hello World").startsWith("Hello")
        assertThat(true).isTrue()
        assertThat(null).isNull()
    }

    @Test
    fun `collection assertions`() {
        val list = listOf("a", "b", "c")

        assertThat(list).hasSize(3)
        assertThat(list).contains("b")
        assertThat(list).containsExactly("a", "b", "c").inOrder()
        assertThat(list).containsNoneOf("x", "y")
        assertThat(list).isNotEmpty()
    }

    @Test
    fun `map assertions`() {
        val map = mapOf("name" to "John", "age" to "30")

        assertThat(map).containsKey("name")
        assertThat(map).containsEntry("name", "John")
        assertThat(map).hasSize(2)
    }

    @Test
    fun `exception assertions`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            validateAge(-1)
        }
        assertThat(exception).hasMessageThat().contains("negative")
    }

    @Test
    fun `number comparisons`() {
        assertThat(5).isGreaterThan(3)
        assertThat(5).isAtLeast(5)
        assertThat(3.14).isWithin(0.01).of(3.14159)
    }
}
```

**Truth vs JUnit Assertions:**
```kotlin
// JUnit – less readable
assertEquals(3, list.size)
assertTrue(list.contains("a"))

// Truth – more readable, better error messages
assertThat(list).hasSize(3)
assertThat(list).contains("a")

// Error message comparison:
// JUnit:  "expected: <3> but was: <2>"
// Truth:  "expected to have size 3, but was [a, b] (size: 2)"
```

---

### Test Fixtures and Factories

As your test suite grows, you'll find yourself creating the same test objects over and over. **Test fixtures** solve this by providing reusable factory methods for test data.

**The problem without fixtures:**
```kotlin
// In test A
val user = User("1", "John", "john@mail.com", 30, true)
// In test B
val user = User("2", "John", "john@mail.com", 30, true)  // copy-paste!
// In test C
val user = User("3", "Jane", "jane@mail.com", 25, true)  // more copy-paste!
// Now the User constructor changes... fix ALL tests.
```

```
  ┌─ Test Data Patterns ─────────────────────────────────────────────┐
  │                                                                  │
  │ Object Mother          Builder Pattern      Shared Fixtures      │
  │ ─────────────          ───────────────      ────────────────     │
  │ Static factory          Fluent API for       testFixtures/        │
  │ methods with            customizing test     folder reused        │
  │ sensible defaults.      objects on a         across modules.      │
  │ Best for: simple        per-test basis.      Best for: multi-     │
  │ data objects.           Best for: complex    module projects.     │
  │                         objects with many                         │
  │ UserMother              fields.              Shared by :app,      │
  │   .createDefault()                           :feature, :data      │
  │   .createAdmin()        NoteBuilder()        modules.             │
  │   .createList(5)          .withTitle(...)                         │
  │                           .pinned()                               │
  │                           .build()                                │
  └──────────────────────────────────────────────────────────────────┘
```

#### Object Mother Pattern

```kotlin
object UserMother {

    fun createDefault(
        id: String = "user-1",
        name: String = "John Doe",
        email: String = "john@example.com",
        age: Int = 30,
        isActive: Boolean = true
    ) = User(id, name, email, age, isActive)

    fun createInactive() = createDefault(
        id = "user-2",
        name = "Inactive User",
        isActive = false
    )

    fun createAdmin() = createDefault(
        id = "admin-1",
        name = "Admin User",
        email = "admin@example.com"
    )

    fun createList(count: Int = 5) = (1..count).map {
        createDefault(
            id = "user-$it",
            name = "User $it",
            email = "user$it@example.com"
        )
    }
}

// Usage in tests
class UserViewModelTest {

    @Test
    fun `displays user list`() {
        val users = UserMother.createList(3)
        repository.setUsers(users)

        viewModel.loadUsers()

        assertEquals(3, viewModel.users.value.size)
    }
}
```

---

#### Builder Pattern for Test Data

```kotlin
class NoteBuilder {
    private var id: String = "note-1"
    private var title: String = "Default Title"
    private var content: String = "Default content"
    private var createdAt: Long = System.currentTimeMillis()
    private var isPinned: Boolean = false
    private var tags: List<String> = emptyList()

    fun withId(id: String) = apply { this.id = id }
    fun withTitle(title: String) = apply { this.title = title }
    fun withContent(content: String) = apply { this.content = content }
    fun withCreatedAt(time: Long) = apply { this.createdAt = time }
    fun pinned() = apply { this.isPinned = true }
    fun withTags(vararg tags: String) = apply { this.tags = tags.toList() }

    fun build() = Note(id, title, content, createdAt, isPinned, tags)
}

// Usage
val note = NoteBuilder()
    .withTitle("Important")
    .pinned()
    .withTags("work", "urgent")
    .build()
```

---

#### Shared Test Fixtures Module

For large projects, create a shared fixture module:

```
app/
├── src/
│   ├── main/
│   ├── test/
│   └── androidTest/
├── testFixtures/         ← Shared test data
│   └── java/
│       └── com.example.app.fixtures/
│           ├── UserFixtures.kt
│           ├── NoteFixtures.kt
│           └── FakeRepositories.kt
```

**Enable in Gradle:**
```kotlin
android {
    testFixtures {
        enable = true
    }
}

// In other modules that need fixtures:
dependencies {
    testImplementation(testFixtures(project(":app")))
    androidTestImplementation(testFixtures(project(":app")))
}
```

---

## Quick Reference – Testing Dependencies

```kotlin
dependencies {
    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")

    // Mocking
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    // Coroutines testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Flow testing
    testImplementation("app.cash.turbine:turbine:1.0.0")

    // Assertions
    testImplementation("com.google.truth:truth:1.1.5")

    // Robolectric
    testImplementation("org.robolectric:robolectric:4.11.1")

    // Android instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")

    // Fragment testing
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")

    // Compose testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")

    // Screenshot testing
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.7.0")
}
```

---

## Summary Table

| Topic | Key Tools | Test Location |
|---|---|---|
| Unit tests | JUnit, MockK, Truth | `src/test/` |
| Coroutine tests | kotlinx-coroutines-test, Turbine | `src/test/` |
| Robolectric tests | Robolectric, JUnit | `src/test/` |
| UI tests (Views) | Espresso, ActivityScenario | `src/androidTest/` |
| UI tests (Compose) | ComposeTestRule | `src/androidTest/` |
| Cross-app tests | UI Automator | `src/androidTest/` |
| Screenshot tests | Roborazzi, Paparazzi | `src/test/` |

---

## Best Practices & Common Pitfalls

### Naming Conventions

Good test names describe **what** is being tested, **under what condition**, and **what the expected result** is.

```kotlin
// BAD — says nothing about behavior
@Test fun test1() { ... }
@Test fun testLogin() { ... }

// GOOD — describes scenario and expectation
@Test fun `login with valid credentials returns success`() { ... }
@Test fun `login with empty password shows error message`() { ... }
@Test fun `fetchUsers when network error returns cached data`() { ... }
```

### What Makes a Good Test

```
  A good test is:

  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
  │    FAST      │  │ INDEPENDENT │  │ REPEATABLE  │  │ SELF-       │
  │              │  │             │  │             │  │ VALIDATING  │
  │ Runs in ms, │  │ No test     │  │ Same result │  │ Pass/fail   │
  │ not seconds. │  │ depends on  │  │ every time. │  │ without     │
  │ No network,  │  │ another     │  │ No random,  │  │ human       │
  │ no disk.     │  │ test's      │  │ no system   │  │ inspection. │
  │              │  │ state.      │  │ clock.      │  │             │
  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘
```

These are the **F.I.R.S.T** principles (Fast, Independent, Repeatable, Self-validating, Timely).

### Common Mistakes to Avoid

| Mistake | Why It's Bad | Fix |
|---|---|---|
| **Testing implementation, not behavior** | Refactoring breaks tests even though behavior is unchanged | Assert on outputs and state, not internal method calls |
| **Over-mocking** | Tests pass but real integration is broken | Prefer fakes for repositories; mock only at boundaries |
| **No assertions** | Test "passes" but verifies nothing | Every test must have at least one meaningful assertion |
| **`Thread.sleep()` in tests** | Flaky, slow, unreliable | Use `advanceUntilIdle()`, Turbine, or IdlingResources |
| **Testing Android framework code** | You're testing Google's code, not yours | Test YOUR logic; trust the framework |
| **Giant test methods** | Hard to debug when they fail | One scenario per test; clear AAA structure |
| **Copy-paste test data** | Constructor changes break 50 tests | Use Object Mother / Builder pattern |
| **Not testing error paths** | App crashes on first API failure | Test error states, empty lists, nulls, exceptions |
| **Flaky tests ignored** | Team stops trusting the test suite | Fix or delete flaky tests immediately |

### Test Organization

```
  app/src/test/java/com/example/app/
  │
  ├── data/
  │   ├── repository/
  │   │   └── UserRepositoryTest.kt          ← mirrors production structure
  │   └── local/
  │       └── UserDaoTest.kt
  │
  ├── domain/
  │   └── usecase/
  │       └── LoginUseCaseTest.kt
  │
  ├── ui/
  │   └── login/
  │       └── LoginViewModelTest.kt
  │
  └── testutil/                               ← shared test helpers
      ├── MainDispatcherRule.kt
      ├── FakeUserRepository.kt
      └── UserMother.kt
```

**Key rules:**
- Mirror production package structure in tests.
- One test class per production class.
- Keep shared test utilities in a `testutil` package.
- Name test classes as `<ClassName>Test.kt`.

### Decision Guide: Which Test Type to Use

```
  What are you testing?
         │
         ├─── Pure logic (no Android APIs)?
         │         └──► Unit test (JUnit + MockK)
         │
         ├─── Logic that needs Context/Resources?
         │         └──► Robolectric test (still in src/test/)
         │
         ├─── ViewModel + Repository together?
         │         └──► Integration test (Robolectric or Fake)
         │
         ├─── Single screen UI behavior?
         │    ├── Views (XML)?
         │    │      └──► Espresso test
         │    └── Compose?
         │           └──► ComposeTestRule test
         │
         ├─── Multi-screen user journey?
         │         └──► Espresso + Navigation test
         │
         └─── Crosses app boundaries (permissions, notifications)?
                   └──► UI Automator test
```

---
