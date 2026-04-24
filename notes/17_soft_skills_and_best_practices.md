# 17. Soft Skills and Best Practices

---

## 17.1 Code Organization

### Package Structure by Feature

Instead of organizing packages by **layer** (e.g., `activities/`, `fragments/`, `adapters/`), organize by **feature**. This keeps related code together and improves maintainability.

**Bad — Package by Layer:**
```
com.myapp/
├── activities/
│   ├── LoginActivity.kt
│   ├── HomeActivity.kt
│   └── ProfileActivity.kt
├── fragments/
│   ├── LoginFragment.kt
│   ├── HomeFragment.kt
│   └── ProfileFragment.kt
├── viewmodels/
│   ├── LoginViewModel.kt
│   ├── HomeViewModel.kt
│   └── ProfileViewModel.kt
├── repositories/
│   ├── LoginRepository.kt
│   ├── HomeRepository.kt
│   └── ProfileRepository.kt
├── models/
│   ├── User.kt
│   ├── Post.kt
│   └── Comment.kt
└── adapters/
    ├── PostAdapter.kt
    └── CommentAdapter.kt
```

**Good — Package by Feature:**
```
com.myapp/
├── core/
│   ├── network/
│   │   ├── ApiService.kt
│   │   └── NetworkModule.kt
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   └── DatabaseModule.kt
│   ├── di/
│   │   └── AppModule.kt
│   └── utils/
│       ├── Extensions.kt
│       └── Constants.kt
├── feature/
│   ├── auth/
│   │   ├── data/
│   │   │   ├── AuthRepository.kt
│   │   │   ├── AuthApi.kt
│   │   │   └── AuthDto.kt
│   │   ├── domain/
│   │   │   ├── LoginUseCase.kt
│   │   │   └── User.kt
│   │   └── presentation/
│   │       ├── LoginScreen.kt
│   │       ├── LoginViewModel.kt
│   │       └── LoginUiState.kt
│   ├── home/
│   │   ├── data/
│   │   ├── domain/
│   │   └── presentation/
│   └── profile/
│       ├── data/
│       ├── domain/
│       └── presentation/
└── App.kt
```

**Why feature packaging is better:**
- **High cohesion** — Everything related to one feature lives together
- **Easy navigation** — Find all auth-related code in `feature/auth/`
- **Better encapsulation** — Features can have internal visibility
- **Scalable** — Adding a new feature = adding a new package
- **Team-friendly** — Teams can own entire feature packages

---

### Clean Code Principles

#### 1. Single Responsibility Principle (SRP)
Each class/function should do **one thing** and do it well.

```kotlin
// BAD — ViewModel doing too much
class UserViewModel : ViewModel() {
    fun login(email: String, password: String) { /* ... */ }
    fun validateEmail(email: String): Boolean { /* ... */ }
    fun formatDate(date: Long): String { /* ... */ }
    fun saveToDatabase(user: User) { /* ... */ }
    fun sendAnalytics(event: String) { /* ... */ }
}

// GOOD — Each class has a single responsibility
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val inputValidator: InputValidator
) : ViewModel() {
    
    fun login(email: String, password: String) {
        if (inputValidator.isValidEmail(email)) {
            viewModelScope.launch {
                loginUseCase(email, password)
            }
        }
    }
}

class InputValidator {
    fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    
    fun isValidPassword(password: String): Boolean =
        password.length >= 8
}
```

#### 2. DRY (Don't Repeat Yourself)
Extract common logic into reusable functions/classes.

```kotlin
// BAD — Repeated error handling
class UserRepository {
    suspend fun getUser(): Result<User> {
        return try {
            val response = api.getUser()
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPosts(): Result<List<Post>> {
        return try {
            val response = api.getPosts()
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// GOOD — Extracted common logic
class UserRepository {
    suspend fun getUser(): Result<User> = safeApiCall { api.getUser() }
    suspend fun getPosts(): Result<List<Post>> = safeApiCall { api.getPosts() }
}

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

#### 3. KISS (Keep It Simple, Stupid)
Avoid unnecessary complexity.

```kotlin
// BAD — Over-engineered
class StringUtils {
    companion object {
        fun processAndValidateAndFormatUserInput(
            input: String,
            shouldTrim: Boolean = true,
            shouldLowercase: Boolean = false,
            maxLength: Int = Int.MAX_VALUE,
            validator: ((String) -> Boolean)? = null,
            transformer: ((String) -> String)? = null
        ): ProcessedResult<String> { /* 50 lines of code */ }
    }
}

// GOOD — Simple and clear
fun String.trimAndValidate(maxLength: Int): String? {
    val trimmed = this.trim()
    return if (trimmed.length <= maxLength) trimmed else null
}
```

#### 4. YAGNI (You Aren't Gonna Need It)
Don't build things until you actually need them.

```kotlin
// BAD — Building for imagined future requirements
interface DataExporter {
    fun exportToCsv()
    fun exportToJson()
    fun exportToXml()
    fun exportToPdf()
    fun exportToExcel()  // "We might need this later"
}

// GOOD — Build only what's needed now
interface DataExporter {
    fun exportToCsv()
}
```

#### 5. Favor Composition Over Inheritance

```kotlin
// BAD — Deep inheritance chain
open class BaseActivity : AppCompatActivity() { /* common logic */ }
open class BaseToolbarActivity : BaseActivity() { /* toolbar logic */ }
open class BaseDrawerActivity : BaseToolbarActivity() { /* drawer logic */ }
class HomeActivity : BaseDrawerActivity() { /* actual screen logic */ }

// GOOD — Composition
class HomeActivity : AppCompatActivity() {
    private val toolbarDelegate = ToolbarDelegate()
    private val drawerDelegate = DrawerDelegate()
    private val analyticsDelegate = AnalyticsDelegate()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarDelegate.setup(this)
        drawerDelegate.setup(this)
        analyticsDelegate.trackScreenView("home")
    }
}
```

---

### Naming Conventions

#### General Rules

| Element | Convention | Example |
|---|---|---|
| Package | lowercase, no underscores | `com.myapp.feature.auth` |
| Class/Object | PascalCase (nouns) | `UserRepository`, `LoginScreen` |
| Function | camelCase (verbs) | `getUserById()`, `calculateTotal()` |
| Variable/Property | camelCase (nouns) | `userName`, `isLoggedIn` |
| Constant | SCREAMING_SNAKE_CASE | `MAX_RETRY_COUNT`, `BASE_URL` |
| Enum values | SCREAMING_SNAKE_CASE | `LOADING`, `SUCCESS`, `ERROR` |
| Type parameter | Single uppercase letter | `T`, `K`, `V` |

#### Android-Specific Naming

```kotlin
// Activities & Fragments
class LoginActivity : AppCompatActivity()
class UserProfileFragment : Fragment()

// ViewModels
class LoginViewModel : ViewModel()
class HomeViewModel : ViewModel()

// Composables — PascalCase (like components)
@Composable
fun LoginScreen() { }

@Composable
fun UserProfileCard(user: User) { }

// Use Cases — verb phrase
class GetUserByIdUseCase
class ValidateEmailUseCase
class SyncDataUseCase

// Repositories
class UserRepository
class AuthRepository

// Data classes — describe what they hold
data class User(val id: String, val name: String)
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val user: User)

// Sealed classes for states
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// Boolean properties — use is/has/should prefixes
val isLoading: Boolean
val hasError: Boolean
val shouldRetry: Boolean
val canSubmit: Boolean

// Collections — use plural nouns
val users: List<User>
val selectedItems: Set<Item>
val userCache: Map<String, User>

// Callbacks & Lambdas
val onClick: () -> Unit
val onUserSelected: (User) -> Unit
val onValueChanged: (String) -> Unit
```

#### Resource Naming (XML)

```
<!-- Layouts -->
activity_login.xml
fragment_user_profile.xml
item_user_list.xml
dialog_confirmation.xml
view_custom_toolbar.xml

<!-- Drawables -->
ic_arrow_back_24.xml        (icons)
bg_rounded_button.xml       (backgrounds)
shape_circle_red.xml        (shapes)
selector_button_primary.xml (selectors)

<!-- Strings -->
<string name="login_title">Login</string>
<string name="login_button_submit">Submit</string>
<string name="error_network">Network error</string>

<!-- Dimensions -->
<dimen name="spacing_small">8dp</dimen>
<dimen name="text_size_title">20sp</dimen>

<!-- Colors -->
<color name="primary">#6200EE</color>
<color name="text_primary">#000000</color>
```

---

### Documentation and KDoc

KDoc is Kotlin's documentation format (equivalent of Javadoc).

#### Basic KDoc Syntax

```kotlin
/**
 * Authenticates a user with the provided credentials.
 *
 * This function performs network authentication and stores the resulting
 * token in the local keystore. If the user has 2FA enabled, it will
 * trigger the two-factor authentication flow.
 *
 * @param email The user's email address. Must be a valid email format.
 * @param password The user's password. Must be at least 8 characters.
 * @return [AuthResult] containing the authentication token on success,
 *         or an error message on failure.
 * @throws NetworkException if there is no internet connection.
 * @throws AuthenticationException if the credentials are invalid.
 * @see [LogoutUseCase] for the logout flow.
 * @since 2.1.0
 * @sample com.myapp.samples.loginSample
 */
suspend fun login(email: String, password: String): AuthResult
```

#### KDoc Tags Reference

| Tag | Purpose | Example |
|---|---|---|
| `@param` | Document a parameter | `@param id The user ID` |
| `@return` | Document the return value | `@return The user object` |
| `@throws` | Document exceptions | `@throws IOException on failure` |
| `@see` | Reference related elements | `@see [OtherClass]` |
| `@since` | Version when added | `@since 1.2.0` |
| `@sample` | Link to code sample | `@sample com.myapp.Sample.test` |
| `@property` | Document a class property | `@property name The user name` |
| `@constructor` | Document primary constructor | `@constructor Creates a User` |
| `@receiver` | Document extension receiver | `@receiver The string to parse` |
| `@suppress` | Suppress in generated docs | `@suppress` |

#### What to Document (and What Not To)

```kotlin
// DO document: Public APIs, complex logic, non-obvious behavior

/**
 * Calculates the shipping cost based on weight, distance, and delivery speed.
 *
 * Uses the formula: baseCost + (weight * weightMultiplier) + (distance * distanceFee)
 * with a minimum charge of $5.99.
 *
 * @param weightKg Package weight in kilograms. Must be positive.
 * @param distanceKm Delivery distance in kilometers.
 * @param express Whether to use express delivery (2x cost).
 * @return Shipping cost in USD, never less than 5.99.
 */
fun calculateShipping(weightKg: Double, distanceKm: Double, express: Boolean): Double

// DON'T document: Self-explanatory code
// BAD — This adds no value
/** Returns the user's name. */
fun getUserName(): String = user.name

// BAD — Comment restates the code
/** Sets the loading state to true. */
fun startLoading() { _isLoading.value = true }
```

#### Class Documentation

```kotlin
/**
 * Repository that manages user data from multiple sources.
 *
 * This repository follows the single source of truth pattern:
 * - Network data is fetched and cached locally
 * - Local database serves as the single source of truth
 * - Stale data is refreshed based on [CachePolicy]
 *
 * Usage:
 * ```
 * val repo = UserRepository(api, dao, cachePolicy)
 * val users = repo.getUsers().first()
 * ```
 *
 * @property api The remote API service for fetching user data.
 * @property dao The local database DAO for caching.
 * @property cachePolicy Defines when cached data is considered stale.
 */
class UserRepository(
    private val api: UserApi,
    private val dao: UserDao,
    private val cachePolicy: CachePolicy
)
```

---

## 17.2 Git Workflow

### Commit Message Conventions

The most widely used convention is **Conventional Commits**.

#### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Types

| Type | When to Use | Example |
|---|---|---|
| `feat` | New feature | `feat(auth): add biometric login` |
| `fix` | Bug fix | `fix(cart): correct price calculation` |
| `docs` | Documentation only | `docs(readme): update setup steps` |
| `style` | Formatting, no code change | `style: fix indentation in LoginScreen` |
| `refactor` | Restructure without behavior change | `refactor(db): simplify query logic` |
| `perf` | Performance improvement | `perf(images): add lazy loading` |
| `test` | Adding or fixing tests | `test(auth): add login unit tests` |
| `chore` | Build, tooling, config | `chore: update Gradle to 8.5` |
| `ci` | CI/CD changes | `ci: add Play Store deployment step` |
| `build` | Build system changes | `build: migrate to version catalogs` |
| `revert` | Revert a previous commit | `revert: revert feat(auth): add biometric` |

#### Rules for Good Commit Messages

```
# GOOD examples
feat(profile): add avatar upload with image cropping
fix(notifications): prevent duplicate push notifications on app restart
refactor(networking): migrate from Retrofit callbacks to coroutines
docs(api): add KDoc for all public repository methods

# BAD examples
fixed stuff                    # Too vague
WIP                            # Don't commit work-in-progress
updated files                  # Which files? What changed?
bug fix                        # Which bug?
feat: added login and signup and forgot password and profile
# ^ Too many changes in one commit — split them
```

#### Commit Message Body & Footer

```
feat(checkout): implement Stripe payment integration

Integrate Stripe SDK for handling credit card payments in the checkout
flow. This replaces the previous PayPal-only implementation.

Changes:
- Add StripePaymentProcessor implementation
- Create PaymentMethodSelector composable
- Add card validation with Luhn algorithm
- Store payment tokens securely in EncryptedSharedPreferences

BREAKING CHANGE: PaymentProcessor interface now requires 
`processAsync()` instead of `process()`.

Closes #245
```

---

### Feature Branches

#### Git Flow Model

```
main (production)
 │
 ├── develop (integration branch)
 │    │
 │    ├── feature/login-screen
 │    ├── feature/user-profile
 │    ├── feature/push-notifications
 │    │
 │    ├── release/1.2.0
 │    │
 │    └── hotfix/crash-on-launch
 │
 └── hotfix/critical-security-patch
```

#### Branch Naming Convention

```bash
# Features
feature/login-screen
feature/JIRA-123-add-biometric-auth
feature/user-profile-redesign

# Bug fixes
bugfix/fix-crash-on-rotation
bugfix/JIRA-456-wrong-total

# Hotfixes (production emergencies)
hotfix/critical-payment-bug
hotfix/security-vulnerability

# Releases
release/1.2.0
release/2.0.0-beta

# Experiments / spikes
spike/evaluate-compose-navigation
experiment/new-image-loader
```

#### Typical Feature Branch Workflow

```bash
# 1. Start from develop
git checkout develop
git pull origin develop

# 2. Create feature branch
git checkout -b feature/login-screen

# 3. Make commits (small, focused)
git add .
git commit -m "feat(auth): add login screen UI layout"

git add .
git commit -m "feat(auth): implement login form validation"

git add .
git commit -m "feat(auth): connect login to API endpoint"

git add .
git commit -m "test(auth): add unit tests for LoginViewModel"

# 4. Keep branch up to date with develop
git checkout develop
git pull origin develop
git checkout feature/login-screen
git rebase develop    # or merge, depending on team preference

# 5. Push and create Pull Request
git push origin feature/login-screen
# Create PR: feature/login-screen → develop

# 6. After PR approval and merge
git checkout develop
git pull origin develop
git branch -d feature/login-screen
```

#### When to Use Rebase vs Merge

| Scenario | Use | Why |
|---|---|---|
| Updating feature branch from develop | `rebase` | Keeps linear history |
| Merging feature into develop | `merge --no-ff` | Preserves feature context |
| Multiple people on same branch | `merge` | Avoids rewriting shared history |
| Cleaning up local commits before PR | `rebase -i` | Squash/reorder commits |

---

### Semantic Versioning

Format: **MAJOR.MINOR.PATCH** (e.g., `2.4.1`)

#### Version Components

| Component | When to Increment | Example |
|---|---|---|
| **MAJOR** | Breaking/incompatible changes | `1.x.x → 2.0.0` |
| **MINOR** | New features, backward compatible | `2.3.x → 2.4.0` |
| **PATCH** | Bug fixes, backward compatible | `2.4.0 → 2.4.1` |

#### Pre-release Versions

```
1.0.0-alpha.1     → Early testing, unstable
1.0.0-beta.1      → Feature complete, testing
1.0.0-rc.1        → Release candidate, final testing
1.0.0              → Stable release
```

#### Android Version Codes

```kotlin
// build.gradle.kts
android {
    defaultConfig {
        // User-facing version
        versionName = "2.4.1"
        
        // Internal version (must always increase for Play Store)
        // Formula: MAJOR * 10000 + MINOR * 100 + PATCH
        versionCode = 20401
    }
}
```

#### Practical Example Timeline

```
1.0.0    → Initial release
1.0.1    → Fix: crash on Android 12
1.0.2    → Fix: login button not responding
1.1.0    → Feature: dark mode support
1.1.1    → Fix: dark mode colors in settings
1.2.0    → Feature: push notifications
2.0.0    → Breaking: complete UI redesign, min SDK bumped to 26
2.0.1    → Fix: notification sound not playing
2.1.0    → Feature: biometric authentication
```

---

## 17.3 Agile Methodologies

### Scrum Framework

Scrum is an iterative framework for managing work in **time-boxed sprints** (usually 2 weeks).

#### Scrum Roles

| Role | Responsibility |
|---|---|
| **Product Owner (PO)** | Defines what to build, prioritizes backlog, represents stakeholders |
| **Scrum Master** | Facilitates process, removes blockers, protects the team |
| **Development Team** | Self-organizing team (3-9 people) that builds the product |

#### Scrum Artifacts

| Artifact | What It Contains |
|---|---|
| **Product Backlog** | Ordered list of all features, bugs, tech debt (owned by PO) |
| **Sprint Backlog** | Items selected for the current sprint + plan to deliver them |
| **Increment** | The working software delivered at the end of each sprint |

#### Scrum Events (Ceremonies)

```
Sprint (2 weeks)
├── Sprint Planning (Day 1, 2-4 hours)
├── Daily Standup (Every day, 15 min)
├── Sprint Review (Last day, 1-2 hours)
└── Sprint Retrospective (Last day, 1-1.5 hours)
```

#### User Stories

```
As a [type of user],
I want [some goal],
So that [some reason/benefit].
```

**Examples:**
```
As a new user,
I want to sign up with my Google account,
So that I can start using the app without creating a new password.

As a frequent shopper,
I want to save my payment method,
So that I can check out faster next time.
```

**Acceptance Criteria (Given/When/Then):**
```
Feature: Google Sign-Up

Scenario: Successful signup
  Given the user is on the login screen
  When they tap "Sign up with Google"
  And they select their Google account
  Then they should be redirected to the home screen
  And their profile should show their Google name and avatar

Scenario: Cancelled signup
  Given the user is on the login screen
  When they tap "Sign up with Google"
  And they cancel the Google account picker
  Then they should remain on the login screen
  And no account should be created
```

---

### Sprint Planning and Estimation

#### Story Points

Story points measure **relative effort**, not time. Use the Fibonacci sequence: **1, 2, 3, 5, 8, 13, 21**.

| Points | Effort Level | Example (Android) |
|---|---|---|
| **1** | Trivial | Change a button color, fix a typo |
| **2** | Small | Add a new string resource, simple UI tweak |
| **3** | Medium | Add form validation, create a simple Composable |
| **5** | Large | Implement a new screen with ViewModel |
| **8** | Very Large | Integrate a third-party SDK (Stripe, Maps) |
| **13** | Huge | Build offline-first sync with conflict resolution |
| **21** | Epic-sized | Should be broken down into smaller stories |

#### Planning Poker

1. PO presents a user story
2. Team discusses requirements and asks questions
3. Everyone privately selects a story point card
4. All cards revealed simultaneously
5. If estimates differ widely → discuss and re-vote
6. Agree on a consensus estimate

#### Sprint Capacity Planning

```
Team: 4 developers
Sprint: 2 weeks (10 working days)
Average velocity: 40 story points / sprint

Available capacity this sprint:
- Dev A: 10 days (full)
- Dev B: 8 days (2 days PTO)
- Dev C: 10 days (full)
- Dev D: 5 days (half-sprint, conference)

Adjusted capacity: 33/40 = ~82%
Target this sprint: 40 × 0.82 ≈ 33 story points
```

#### Definition of Done (DoD)

A story is DONE when:
- [ ] Code written and self-reviewed
- [ ] Unit tests written and passing
- [ ] UI tests written for critical paths
- [ ] Code reviewed and approved by at least 1 peer
- [ ] No lint warnings or compiler warnings
- [ ] Tested on minimum SDK device
- [ ] Tested on latest Android version
- [ ] Documentation updated (if public API)
- [ ] PR merged to develop branch
- [ ] PO has accepted the story

---

### Daily Standups

#### Format (15 minutes max)

Each team member answers three questions:

1. **What did I do yesterday?**
2. **What will I do today?**
3. **Are there any blockers?**

#### Example

```
Dev A:
  Yesterday: Finished the login screen UI and form validation.
  Today: Connecting the login form to the API, writing unit tests.
  Blockers: None.

Dev B:
  Yesterday: Worked on push notification setup with Firebase.
  Today: Testing notifications on different Android versions.
  Blockers: Need the production Firebase config from DevOps.

Dev C:
  Yesterday: Code review for profile feature, fixed 3 bugs.
  Today: Starting the settings screen.
  Blockers: Waiting on design mockups for the settings page.
```

#### Standup Best Practices

| Do | Don't |
|---|---|
| Keep it under 15 minutes | Turn it into a problem-solving session |
| Focus on sprint goal progress | Report to the manager — talk to the team |
| Flag blockers immediately | Give vague updates ("worked on stuff") |
| Take detailed discussions offline | Discuss code-level implementation details |
| Stand up (if in person) | Sit comfortably and ramble |

---

### Retrospectives

Retrospectives happen at the **end of each sprint** to improve the team's process.

#### Format: Start, Stop, Continue

| Category | Question | Example |
|---|---|---|
| **Start** | What should we begin doing? | "Start writing integration tests" |
| **Stop** | What should we stop doing? | "Stop skipping code reviews for 'small' PRs" |
| **Continue** | What's working well? | "Continue pair programming on complex features" |

#### Format: 4Ls

- **Liked** — What went well?
- **Learned** — What did we learn?
- **Lacked** — What was missing?
- **Longed for** — What do we wish we had?

#### Format: Sailboat

```
    🏝️ Island = Sprint Goal
    
    ⛵ Sailboat = The Team
    
    💨 Wind (pushing forward):
       - Great collaboration on payment feature
       - New CI pipeline saved time
    
    ⚓ Anchor (holding back):
       - Flaky UI tests slowing down merges
       - Unclear requirements on notification feature
    
    🪨 Rocks (risks ahead):
       - Major Android version update coming
       - Key team member leaving next month
```

#### Retrospective Action Items

The most important part — **concrete, actionable improvements**:

```
Action Items from Sprint 12 Retro:
1. [Owner: Dev B] Set up automated UI test retry (max 3) in CI
   → Due: Sprint 13, Day 3
2. [Owner: Scrum Master] Schedule requirements review meeting 
   before each sprint planning
   → Due: Before Sprint 13 planning
3. [Owner: Dev A] Create shared code review checklist in wiki
   → Due: Sprint 13, Day 5
```

---

## 17.4 Code Review Skills

### Giving Constructive Feedback

#### The Right Tone

```
# BAD — Harsh, confrontational
"This code is terrible. Why would you do it this way?"
"This is wrong."
"You clearly don't understand how coroutines work."

# GOOD — Constructive, educational
"What do you think about using `StateFlow` here instead of `LiveData`? 
It plays more naturally with Compose and gives us thread safety."

"Nit: Could we rename this to `fetchUserProfile()` for clarity? 
The current name `getData()` doesn't tell us what data."

"I noticed this coroutine is launched in `GlobalScope`. This could 
cause a memory leak if the Activity is destroyed. Consider using 
`viewModelScope` instead — it auto-cancels when the ViewModel clears. 
Here's the docs: [link]"
```

#### Comment Prefixes

Use prefixes to clarify the **severity** of your feedback:

| Prefix | Meaning | Action Required? |
|---|---|---|
| `blocker:` | Must fix before merge | Yes — blocks approval |
| `bug:` | This will cause a bug | Yes |
| `security:` | Security vulnerability | Yes — critical |
| `suggestion:` | Improvement idea | Optional |
| `nit:` | Nitpick (style, naming) | Optional |
| `question:` | Seeking understanding | Clarification needed |
| `praise:` | Something done well | None — encouragement |
| `thought:` | Thinking out loud | Discussion |

#### Examples

```
blocker: This SQL query is vulnerable to injection. Use parameterized 
queries instead of string concatenation.

bug: `collectAsState()` is called outside of a Composable function. 
This will crash at runtime.

security: The API key is hardcoded here. Move it to local.properties 
or use BuildConfig.

suggestion: This nested `when` could be simplified using a sealed 
class hierarchy. Want me to sketch it out?

nit: Minor — the parameter order here is (context, id, name) but our 
convention is (id, name, context). Not a big deal.

praise: Really clean separation of concerns here. The use case 
pattern makes this super testable. 👍

question: What's the reasoning behind using `Channel` instead of 
`SharedFlow` here? I'm curious about the trade-off.
```

---

### Receiving Feedback Gracefully

#### Mindset Principles

1. **The code is not you** — Feedback on code is not personal criticism
2. **Everyone's code can improve** — Even senior developers get feedback
3. **Reviewers are investing time to help** — Appreciate their effort
4. **Disagreement is normal** — Discuss respectfully, then decide

#### Responding to Reviews

```
# GOOD responses
"Good catch! Fixed in the latest commit."

"Thanks for the suggestion. I went with this approach because [reason], 
but I see how your suggestion would be cleaner. Updated!"

"I'm not sure I agree here — can we discuss? My reasoning is [X]. 
What do you think?"

"I didn't know about that API! Thanks for pointing it out. Updated."

# BAD responses
"It works, so it's fine."          # Defensive
"That's just your opinion."         # Dismissive
"I'll fix it later."                # Procrastinating
[No response, just pushes changes]  # Ignoring feedback
```

#### How to Handle Disagreements

```
1. Understand their perspective fully before responding
2. Explain your reasoning with evidence (docs, benchmarks, examples)
3. If still disagreeing, involve a third team member
4. If no consensus, the code owner makes the final call
5. Document the decision for future reference
```

---

### Review Checklist

#### Functionality
- [ ] Does the code do what the PR description says?
- [ ] Are edge cases handled (null, empty, boundary values)?
- [ ] Does it work on different screen sizes / API levels?
- [ ] Are error states handled and shown to the user?

#### Code Quality
- [ ] Is the code readable and self-documenting?
- [ ] Are names meaningful and consistent with project conventions?
- [ ] Is there any duplicated logic that should be extracted?
- [ ] Are classes and functions appropriately sized (SRP)?
- [ ] Is the complexity reasonable?

#### Architecture
- [ ] Does it follow the project's architecture (MVVM, Clean)?
- [ ] Are dependencies injected, not created directly?
- [ ] Is business logic in the right layer (not in UI)?
- [ ] Are data models separate from domain models?

#### Performance
- [ ] Any work being done on the main thread that shouldn't be?
- [ ] Are database queries optimized (indexes, pagination)?
- [ ] Are images loaded efficiently (proper sizing, caching)?
- [ ] Any potential memory leaks (context references, listeners)?

#### Security
- [ ] No hardcoded secrets, API keys, or passwords?
- [ ] User input is validated and sanitized?
- [ ] Sensitive data encrypted at rest and in transit?
- [ ] Proper authentication/authorization checks?

#### Testing
- [ ] Are there unit tests for new business logic?
- [ ] Are edge cases tested?
- [ ] Do existing tests still pass?
- [ ] Are test names descriptive (`should_returnError_when_networkUnavailable`)?

#### Android-Specific
- [ ] Handles configuration changes (rotation)?
- [ ] Handles process death and restoration?
- [ ] Respects the activity/fragment lifecycle?
- [ ] Permissions requested at runtime where needed?
- [ ] Works offline or shows appropriate offline state?
- [ ] Accessibility — content descriptions, touch targets?

---

## 17.5 Communication

### Technical Documentation

#### README Structure

```markdown
# Project Name

Brief description of the project (1-2 sentences).

## Screenshots / Demo
[Include screenshots or GIF of the app]

## Tech Stack
- Kotlin, Jetpack Compose
- MVVM + Clean Architecture
- Hilt for DI
- Retrofit + Coroutines
- Room Database

## Architecture
[Include architecture diagram]
```
com.myapp/
├── core/        # Shared utilities, networking, DI
├── feature/     # Feature modules
│   ├── auth/
│   ├── home/
│   └── profile/
└── App.kt
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34

### Setup
1. Clone the repo: `git clone https://github.com/org/project.git`
2. Copy `local.properties.example` to `local.properties`
3. Add your API keys to `local.properties`
4. Open in Android Studio and sync Gradle
5. Run on emulator or device

### Environment Variables
| Key | Description | Required |
|-----|-------------|----------|
| `API_BASE_URL` | Backend API URL | Yes |
| `MAPS_API_KEY` | Google Maps key | Yes |
| `SENTRY_DSN` | Error tracking | No |

## Running Tests
```bash
./gradlew testDebugUnitTest          # Unit tests
./gradlew connectedDebugAndroidTest  # UI tests
./gradlew koverHtmlReport            # Coverage report
```

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md)

## License
MIT License — see [LICENSE](LICENSE)
```

#### Architecture Decision Records (ADRs)

Document **why** decisions were made, not just what was decided.

```markdown
# ADR-003: Use Room over SQLDelight for Local Database

## Status
Accepted (2024-03-15)

## Context
We need a local database for offline caching. The main options are:
- Room (Google's official solution)
- SQLDelight (Square's multiplatform solution)

## Decision
We will use Room.

## Reasons
- Team has more experience with Room
- Better integration with Android Jetpack (LiveData, Paging)
- Official Google support and documentation
- We don't need multiplatform support yet (YAGNI)

## Consequences
- Positive: Faster development, more community resources
- Negative: Migration needed if we go multiplatform later
- Neutral: Both have similar performance characteristics

## Alternatives Considered
- SQLDelight: Better for KMM, but adds complexity we don't need
- Realm: Being deprecated for new projects
- Raw SQLite: Too much boilerplate
```

---

### API Documentation

#### REST API Documentation

```markdown
# User API

## Get User Profile

`GET /api/v1/users/{id}`

### Parameters

| Name | Type | In | Description |
|------|------|----|-------------|
| `id` | string | path | User ID (UUID format) |
| `include` | string | query | Comma-separated: `posts,followers,settings` |

### Headers

| Name | Required | Description |
|------|----------|-------------|
| `Authorization` | Yes | Bearer token: `Bearer <access_token>` |
| `Accept-Language` | No | Locale code (default: `en`) |

### Response 200 (Success)
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Jane Doe",
  "email": "jane@example.com",
  "avatar_url": "https://cdn.example.com/avatars/jane.jpg",
  "created_at": "2024-01-15T10:30:00Z",
  "posts_count": 42
}
```

### Response 401 (Unauthorized)
```json
{
  "error": "unauthorized",
  "message": "Invalid or expired access token"
}
```

### Response 404 (Not Found)
```json
{
  "error": "not_found",
  "message": "User with id '550e...' not found"
}
```
```

#### Documenting Your Android API Client

```kotlin
/**
 * API service for user-related endpoints.
 *
 * All endpoints require authentication via Bearer token,
 * which is automatically attached by [AuthInterceptor].
 *
 * Base URL: `https://api.example.com/v1/`
 *
 * Rate limits:
 * - 100 requests per minute per user
 * - 429 status code when exceeded (retry after header provided)
 */
interface UserApi {

    /**
     * Fetches a user's public profile.
     *
     * @param userId UUID of the user to fetch.
     * @return [UserDto] with profile data.
     * @throws HttpException 404 if user doesn't exist.
     */
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): UserDto

    /**
     * Updates the current user's profile.
     *
     * Only provided fields will be updated (PATCH semantics).
     *
     * @param request Fields to update. Null fields are ignored.
     * @return Updated [UserDto].
     * @throws HttpException 400 if validation fails.
     * @throws HttpException 413 if avatar exceeds 5MB.
     */
    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserDto
}
```

---

### Stakeholder Communication

#### Communicating with Non-Technical Stakeholders

| Technical Term | Business-Friendly Translation |
|---|---|
| "We need to refactor the codebase" | "We need to reorganize the code to ship features faster and with fewer bugs" |
| "Technical debt is slowing us down" | "Past shortcuts are now costing us extra time on every new feature" |
| "We should migrate to Compose" | "We want to switch to a modern UI toolkit that lets us build screens 30% faster" |
| "There's a race condition" | "Two things are happening at the same time and sometimes causing incorrect data" |
| "We need to bump the min SDK" | "We want to drop support for very old phones (less than 3% of our users) to unlock better features" |
| "CI/CD pipeline is broken" | "Our automated testing and deployment system is down — we can't release updates until it's fixed" |

#### Status Update Template

```markdown
## Sprint 14 Status Update — Mobile Team

### Summary
On track for the 2.5.0 release. Payment integration is complete,
push notifications are in testing.

### Completed This Sprint
✅ Stripe payment integration (checkout flow)
✅ Order confirmation screen
✅ Push notification setup (Firebase)

### In Progress
🔄 Push notification testing across Android versions
🔄 Payment error handling edge cases

### Blocked / Risks
⚠️ Waiting on production API keys from backend team (ETA: Monday)
⚠️ New Android 15 behavior change may affect notification permissions

### Metrics
- Sprint velocity: 38 points (target: 40)
- Bug count: 3 open, 5 closed this sprint
- Test coverage: 78% (+3% from last sprint)

### Next Sprint Preview
- Push notification customization
- Order history screen
- Performance optimization for image loading
```

#### Escalation Framework

```
Level 1 — Team-level
  "I'm blocked on X. Can someone help during standup?"
  → Resolve within the team, same day

Level 2 — Scrum Master
  "I've been blocked for 2+ days. Need SM help removing this blocker."
  → SM escalates to relevant party, 1-2 day resolution

Level 3 — Engineering Manager
  "This is a cross-team dependency. Backend team hasn't delivered 
   the API we need and it's affecting our sprint goal."
  → EM coordinates across teams, scheduled meeting

Level 4 — Director / VP
  "This is a project-level risk. If we don't resolve the 
   infrastructure issue, the release date is at risk."
  → Leadership decision required
```

---

### Team Collaboration

#### Pair Programming

| Style | How It Works | Best For |
|---|---|---|
| **Driver-Navigator** | Driver types, Navigator reviews and guides | Complex features, mentoring |
| **Ping-Pong** | A writes test, B makes it pass, B writes next test | TDD, learning |
| **Strong Style** | To get an idea into the code, it must go through someone else's hands | Knowledge sharing |

```
Tips for Effective Pairing:
- Switch roles every 25-30 minutes (Pomodoro)
- The navigator should think big picture, not dictate keystrokes
- Take breaks — pairing is mentally intense
- Use it strategically, not for everything
- Great for onboarding new team members
```

#### Knowledge Sharing

```
Practices:
1. Tech Talks (30 min, bi-weekly)
   - Team members present on topics they've learned
   - "How I implemented offline-first sync"
   - "Deep dive into Compose animations"

2. Lunch & Learn (informal, weekly)
   - Casual knowledge sharing over lunch
   - Demo new tools, libraries, or techniques

3. Internal Wiki / Confluence
   - Architecture decisions (ADRs)
   - Onboarding guides
   - Troubleshooting runbooks
   - "How we do X" guides

4. Code Walkthroughs
   - Author walks through complex PR before review
   - Explains the why, not just the what
   - Recorded for async team members

5. Mob Programming (whole team, 1 feature)
   - Everyone works on the same thing
   - Rotate driver every 10-15 minutes
   - Best for complex, cross-cutting features
```

#### Communication Channels Guide

| Channel | Use For | Response Time |
|---|---|---|
| **Slack/Teams DM** | Quick questions, informal chat | Minutes to hours |
| **Slack/Teams Channel** | Team announcements, discussions | Hours |
| **PR Comments** | Code feedback, technical discussion | Same day |
| **Email** | External communication, formal updates | 24 hours |
| **Meeting** | Complex discussions, brainstorming, decisions | Scheduled |
| **Wiki/Docs** | Persistent knowledge, guides, runbooks | Async reference |
| **JIRA/Tickets** | Work tracking, bug reports, requirements | Varies |

#### Conflict Resolution in Technical Decisions

```
1. DATA over opinions
   → "Let's benchmark both approaches and compare"
   → "Let's check the official docs/guidelines"

2. PROTOTYPE over debate
   → "Let's spend 2 hours spiking both approaches"
   → "Build a small proof of concept and compare"

3. TIMEBOX the discussion
   → "Let's discuss for 15 minutes, then decide"
   → If no consensus, tech lead makes the call

4. DOCUMENT the decision
   → Write an ADR explaining what was decided and why
   → Future team members will understand the reasoning

5. REVISIT if needed
   → "Let's try this for 2 sprints and reassess"
   → Decisions aren't permanent — adapt based on evidence
```

---

## Quick Reference Card

```
╔═══════════════════════════════════════════════════════════════╗
║                SOFT SKILLS CHEAT SHEET                        ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  CODE ORGANIZATION                                            ║
║  ✓ Package by feature, not by layer                           ║
║  ✓ Follow SRP, DRY, KISS, YAGNI                              ║
║  ✓ Meaningful names everywhere                                ║
║  ✓ Document the WHY, not the WHAT                             ║
║                                                               ║
║  GIT                                                          ║
║  ✓ type(scope): subject                                       ║
║  ✓ Feature branches from develop                              ║
║  ✓ Semantic versioning: MAJOR.MINOR.PATCH                     ║
║  ✓ Small, focused commits                                     ║
║                                                               ║
║  AGILE                                                        ║
║  ✓ Sprint = 2 weeks, Standup = 15 min                         ║
║  ✓ Estimate in story points (Fibonacci)                       ║
║  ✓ Retro → concrete action items with owners                  ║
║  ✓ Definition of Done before marking complete                 ║
║                                                               ║
║  CODE REVIEWS                                                 ║
║  ✓ Use prefixes: blocker/bug/nit/suggestion/praise            ║
║  ✓ Critique code, not people                                  ║
║  ✓ Explain the WHY behind your suggestion                     ║
║  ✓ Accept feedback gracefully — code ≠ you                    ║
║                                                               ║
║  COMMUNICATION                                                ║
║  ✓ Translate tech to business language                        ║
║  ✓ Status updates: done / in progress / blocked               ║
║  ✓ Data > opinions for technical decisions                    ║
║  ✓ Document decisions in ADRs                                 ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```
