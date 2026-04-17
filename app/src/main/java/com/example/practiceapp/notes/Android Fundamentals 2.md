# Android Languages & Build Systems - Complete Interview Guide

> A comprehensive guide covering Java legacy support, Kotlin interoperability, XML resources, Gradle DSL, and NDK for Android developer interviews.

---

## Table of Contents

1. [Java Legacy Support](#1-java-legacy-support)
   - [Understanding Existing Codebases](#11-understanding-existing-codebases)
   - [Java vs Kotlin Syntax Comparison](#12-java-vs-kotlin-syntax-comparison)
   - [Interoperability with Kotlin](#13-interoperability-with-kotlin)
   - [Migration Strategies](#14-migration-strategies-from-java-to-kotlin)
2. [XML for Layouts and Resources](#2-xml-for-layouts-and-resources)
3. [Gradle with Kotlin DSL](#3-gradle-with-kotlin-dsl)
4. [C/C++ and NDK](#4-cc-and-ndk-native-development-kit)
5. [Interview Questions & Answers](#5-interview-questions--answers)

---

## 1. Java Legacy Support

### 1.1 Understanding Existing Codebases

#### Why Java Knowledge is Still Essential

Despite Kotlin being Google's preferred language since 2019, Java remains critical because:

- **70%+ of existing Android apps** have Java code
- Many **enterprise applications** are entirely in Java
- **Third-party libraries** often have Java APIs
- **Stack Overflow answers** frequently show Java solutions
- **Interview coding tests** may require Java

#### Common Java Patterns in Legacy Android Code

**1. Activity Lifecycle Implementation**

```java
public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private TextView titleTextView;
    private Button submitButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // View binding the old way
        titleTextView = findViewById(R.id.titleTextView);
        submitButton = findViewById(R.id.submitButton);
        
        setupClickListeners();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Activity started");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity becomes visible
        loadData();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Save state before activity loses focus
        saveCurrentState();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Release resources
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up
    }
    
    private void setupClickListeners() {
        // Anonymous inner class pattern (pre-Java 8)
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });
    }
    
    // With Java 8 lambdas (minSdk 24+)
    private void setupClickListenersModern() {
        submitButton.setOnClickListener(v -> handleSubmit());
    }
    
    private void handleSubmit() {
        // Handle button click
    }
    
    private void loadData() {
        // Load data from repository
    }
    
    private void saveCurrentState() {
        // Save state to SharedPreferences or Bundle
    }
}
```

**2. AsyncTask Pattern (Deprecated but still seen in legacy code)**

```java
// DO NOT USE IN NEW CODE - For understanding legacy code only
public class FetchDataTask extends AsyncTask<String, Integer, List<User>> {
    
    private WeakReference<MainActivity> activityRef;
    
    public FetchDataTask(MainActivity activity) {
        this.activityRef = new WeakReference<>(activity);
    }
    
    @Override
    protected void onPreExecute() {
        // Runs on UI thread before background work
        MainActivity activity = activityRef.get();
        if (activity != null) {
            activity.showLoading();
        }
    }
    
    @Override
    protected List<User> doInBackground(String... params) {
        // Runs on background thread
        String url = params[0];
        List<User> users = new ArrayList<>();
        
        try {
            // Simulate network call
            Thread.sleep(2000);
            // Parse response into users list
            
            // Report progress
            publishProgress(50);
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    @Override
    protected void onProgressUpdate(Integer... values) {
        // Runs on UI thread
        MainActivity activity = activityRef.get();
        if (activity != null) {
            activity.updateProgress(values[0]);
        }
    }
    
    @Override
    protected void onPostExecute(List<User> users) {
        // Runs on UI thread after background work
        MainActivity activity = activityRef.get();
        if (activity != null && !activity.isFinishing()) {
            activity.hideLoading();
            activity.displayUsers(users);
        }
    }
}

// Usage
new FetchDataTask(this).execute("https://api.example.com/users");
```

**Modern Replacement with Kotlin Coroutines:**

```kotlin
// Modern approach
class MainActivity : AppCompatActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            viewModel.users.collect { users ->
                displayUsers(users)
            }
        }
        
        viewModel.fetchUsers()
    }
}

class MainViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    fun fetchUsers() {
        viewModelScope.launch {
            _users.value = repository.getUsers() // Suspend function
        }
    }
}
```

**3. Java POJO (Plain Old Java Object)**

```java
// Typical Java model class - lots of boilerplate!
public class User {
    private int id;
    private String name;
    private String email;
    private boolean isActive;
    
    // Empty constructor
    public User() {
    }
    
    // Full constructor
    public User(int id, String name, String email, boolean isActive) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    // equals()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
               isActive == user.isActive &&
               Objects.equals(name, user.name) &&
               Objects.equals(email, user.email);
    }
    
    // hashCode()
    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, isActive);
    }
    
    // toString()
    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", isActive=" + isActive +
               '}';
    }
    
    // copy() method if needed
    public User copy() {
        return new User(id, name, email, isActive);
    }
}
```

**Kotlin Equivalent - Just ONE line!**

```kotlin
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val isActive: Boolean = true  // Default parameter
)

// Automatically generates: equals(), hashCode(), toString(), copy(), componentN()
```

---

### 1.2 Java vs Kotlin Syntax Comparison

| Feature | Java | Kotlin |
|---------|------|--------|
| **Variable Declaration** | `String name = "John";` | `val name = "John"` |
| **Mutable Variable** | `String name = "John";` | `var name = "John"` |
| **Null Declaration** | `String name = null;` | `var name: String? = null` |
| **Type Inference** | Limited | Full |
| **String Templates** | `"Hello " + name` | `"Hello $name"` |
| **Class Declaration** | `public class User { }` | `class User { }` |
| **Constructor** | Separate method | Primary constructor |
| **Singleton** | Manual implementation | `object Singleton { }` |
| **Static Methods** | `static` keyword | `companion object` |
| **Null Check** | `if (x != null) { }` | `x?.let { }` or `x!!` |
| **When/Switch** | `switch(x) { case: }` | `when(x) { }` |
| **List Creation** | `Arrays.asList(1,2,3)` | `listOf(1, 2, 3)` |
| **Lambda** | `(a, b) -> a + b` | `{ a, b -> a + b }` |
| **Extension Functions** | Not supported | `fun String.addHello() = "Hello $this"` |
| **Coroutines** | Not native | Native support |
| **Data Classes** | Manual boilerplate | `data class` |

---

### 1.3 Interoperability with Kotlin

#### Calling Java from Kotlin

**Scenario 1: Using Java Libraries**

```java
// Java utility class
public class StringUtils {
    public static String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
    
    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}
```

```kotlin
// Calling from Kotlin
fun main() {
    val result = StringUtils.capitalize("hello")  // "Hello"
    val isValid = StringUtils.isValidEmail("test@example.com")  // true
}
```

**Scenario 2: Java Getters/Setters Become Properties**

```java
// Java class
public class Person {
    private String name;
    private int age;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    // Boolean getter with "is" prefix
    public boolean isAdult() { return age >= 18; }
}
```

```kotlin
// Kotlin usage - getters/setters become properties!
fun main() {
    val person = Person()
    
    // setName() becomes assignment
    person.name = "John"  // Calls setName("John")
    
    // getName() becomes property access
    println(person.name)  // Calls getName()
    
    person.age = 25       // Calls setAge(25)
    
    // isAdult() becomes isAdult property
    if (person.isAdult) {  // Calls isAdult()
        println("Adult")
    }
}
```

**Scenario 3: Platform Types (Dangerous!)**

```java
// Java class with no nullability annotations
public class UserRepository {
    public User getUser(int id) {
        // May return null!
        return database.findById(id);
    }
    
    public List<User> getAllUsers() {
        // List might be null, items might be null
        return database.findAll();
    }
}
```

```kotlin
// Kotlin sees these as "platform types" shown as User! and List<User!>!
fun displayUser(repo: UserRepository) {
    // DANGEROUS: Could crash if getUser returns null
    val user = repo.getUser(1)
    println(user.name)  // NullPointerException risk!
    
    // SAFE: Treat as nullable
    val safeUser: User? = repo.getUser(1)
    println(safeUser?.name ?: "Unknown")
    
    // SAFE: Check before use
    repo.getUser(1)?.let { user ->
        println(user.name)
    }
}
```

**Best Practice: Add Nullability Annotations to Java Code**

```java
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserRepository {
    @Nullable
    public User getUser(int id) {
        return database.findById(id);
    }
    
    @NonNull
    public List<@NonNull User> getAllUsers() {
        List<User> users = database.findAll();
        return users != null ? users : Collections.emptyList();
    }
}
```

#### Calling Kotlin from Java

**Problem 1: Companion Object Access**

```kotlin
// Kotlin class with companion object
class NetworkConfig {
    companion object {
        const val BASE_URL = "https://api.example.com"
        const val TIMEOUT = 30_000L
        
        fun createClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .build()
        }
    }
}
```

```java
// Java access - Awkward without annotations!
public class JavaClass {
    public void example() {
        // Accessing const values works
        String url = NetworkConfig.BASE_URL;
        long timeout = NetworkConfig.TIMEOUT;
        
        // But methods require .Companion
        OkHttpClient client = NetworkConfig.Companion.createClient();  // Ugly!
    }
}
```

**Solution: Use @JvmStatic**

```kotlin
class NetworkConfig {
    companion object {
        const val BASE_URL = "https://api.example.com"
        
        @JvmStatic
        fun createClient(): OkHttpClient {
            return OkHttpClient.Builder().build()
        }
    }
}
```

```java
// Now Java can call directly
OkHttpClient client = NetworkConfig.createClient();  // Clean!
```

**Problem 2: Default Parameters Not Available in Java**

```kotlin
// Kotlin function with default parameters
fun createUser(
    name: String,
    email: String,
    age: Int = 0,
    isActive: Boolean = true
): User {
    return User(name, email, age, isActive)
}
```

```java
// Java can only call with ALL parameters
User user = MainKt.createUser("John", "john@email.com", 25, true);

// This WON'T work - Java doesn't see default parameters
// User user = MainKt.createUser("John", "john@email.com");  // Error!
```

**Solution: Use @JvmOverloads**

```kotlin
@JvmOverloads
fun createUser(
    name: String,
    email: String,
    age: Int = 0,
    isActive: Boolean = true
): User {
    return User(name, email, age, isActive)
}
```

```java
// Now Java sees multiple overloaded methods
User user1 = MainKt.createUser("John", "john@email.com");
User user2 = MainKt.createUser("John", "john@email.com", 25);
User user3 = MainKt.createUser("John", "john@email.com", 25, false);
```

**Problem 3: Kotlin Properties Not Exposed as Fields**

```kotlin
class Config {
    val apiKey = "secret_key_123"
    var debugMode = false
}
```

```java
// Java must use getter/setter methods
Config config = new Config();
String key = config.getApiKey();      // Not config.apiKey
config.setDebugMode(true);            // Not config.debugMode = true
```

**Solution: Use @JvmField**

```kotlin
class Config {
    @JvmField
    val apiKey = "secret_key_123"
    
    @JvmField
    var debugMode = false
}
```

```java
// Now Java can access directly
Config config = new Config();
String key = config.apiKey;           // Direct access!
config.debugMode = true;              // Direct access!
```

**Complete Interop Example:**

```kotlin
// Kotlin file: UserManager.kt
class UserManager private constructor() {
    
    private val users = mutableListOf<User>()
    
    @JvmOverloads
    fun addUser(
        name: String,
        email: String,
        role: String = "user"
    ): User {
        val user = User(users.size + 1, name, email, role)
        users.add(user)
        return user
    }
    
    fun getUsers(): List<User> = users.toList()
    
    companion object {
        @JvmField
        val MAX_USERS = 1000
        
        @Volatile
        private var instance: UserManager? = null
        
        @JvmStatic
        fun getInstance(): UserManager {
            return instance ?: synchronized(this) {
                instance ?: UserManager().also { instance = it }
            }
        }
    }
}

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)
```

```java
// Java file: JavaConsumer.java
public class JavaConsumer {
    
    public void demonstrateInterop() {
        // Access singleton via @JvmStatic
        UserManager manager = UserManager.getInstance();
        
        // Access constant via @JvmField
        int maxUsers = UserManager.MAX_USERS;
        
        // Use overloaded methods via @JvmOverloads
        User user1 = manager.addUser("John", "john@email.com");  // Default role
        User user2 = manager.addUser("Jane", "jane@email.com", "admin");
        
        // Access data class properties
        System.out.println(user1.getName());
        System.out.println(user1.getEmail());
        
        // Data class copy - need to use copy method
        // But copy() with named params doesn't translate well
        User user3 = user1.copy(
            user1.getId(),
            "John Updated",
            user1.getEmail(),
            user1.getRole()
        );
    }
}
```

---

### 1.4 Migration Strategies from Java to Kotlin

#### Strategy 1: Big Bang (Not Recommended)

Convert everything at once. **Avoid this approach** - high risk, difficult to debug.

#### Strategy 2: Gradual Migration (Recommended)

```
Phase 1: Setup & Preparation
├── Add Kotlin support to build.gradle
├── Add nullability annotations to Java code
├── Write tests for existing Java code
└── Train team on Kotlin basics

Phase 2: Start with Low-Risk Components
├── Utility classes
├── Data/Model classes (POJOs → data classes)
├── Extension functions for existing Java classes
└── New features written in Kotlin

Phase 3: Convert Business Logic
├── Repository classes
├── Use cases / Interactors
├── ViewModels
└── Mappers and transformers

Phase 4: Convert UI Layer (Last)
├── Activities
├── Fragments
├── Custom Views
└── Adapters
```

#### Automatic Conversion in Android Studio

**Step 1:** Open Java file
**Step 2:** Menu → Code → Convert Java File to Kotlin (Ctrl+Alt+Shift+K)
**Step 3:** Review and refine the converted code

**Example - Before (Java):**

```java
public class UserValidator {
    
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    
    public ValidationResult validateUser(User user) {
        List<String> errors = new ArrayList<>();
        
        if (user == null) {
            return new ValidationResult(false, Arrays.asList("User cannot be null"));
        }
        
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            errors.add("Name is required");
        }
        
        if (user.getEmail() == null || !user.getEmail().matches(EMAIL_PATTERN)) {
            errors.add("Valid email is required");
        }
        
        if (user.getPassword() == null || 
            user.getPassword().length() < MIN_PASSWORD_LENGTH) {
            errors.add("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
}

public class ValidationResult {
    private final boolean isValid;
    private final List<String> errors;
    
    public ValidationResult(boolean isValid, List<String> errors) {
        this.isValid = isValid;
        this.errors = errors;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
}
```

**After Automatic Conversion (needs refinement):**

```kotlin
class UserValidator {
    
    fun validateUser(user: User?): ValidationResult {
        val errors: MutableList<String> = ArrayList()
        
        if (user == null) {
            return ValidationResult(false, listOf("User cannot be null"))
        }
        
        if (user.name == null || user.name.trim().isEmpty()) {
            errors.add("Name is required")
        }
        
        if (user.email == null || !user.email.matches(EMAIL_PATTERN.toRegex())) {
            errors.add("Valid email is required")
        }
        
        if (user.password == null || user.password.length < MIN_PASSWORD_LENGTH) {
            errors.add("Password must be at least $MIN_PASSWORD_LENGTH characters")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)
```

**After Manual Refinement (Idiomatic Kotlin):**

```kotlin
class UserValidator {
    
    fun validateUser(user: User?): ValidationResult {
        if (user == null) {
            return ValidationResult(false, listOf("User cannot be null"))
        }
        
        val errors = buildList {
            if (user.name.isNullOrBlank()) {
                add("Name is required")
            }
            
            if (!user.email.isValidEmail()) {
                add("Valid email is required")
            }
            
            if ((user.password?.length ?: 0) < MIN_PASSWORD_LENGTH) {
                add("Password must be at least $MIN_PASSWORD_LENGTH characters")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    // Extension function for email validation
    private fun String?.isValidEmail(): Boolean {
        return this != null && EMAIL_REGEX.matches(this)
    }
    
    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    }
}

// Sealed class for better result handling
sealed class ValidationResult {
    data class Success(val user: User) : ValidationResult()
    data class Failure(val errors: List<String>) : ValidationResult()
    
    val isValid: Boolean get() = this is Success
}
```

---

## 2. XML for Layouts and Resources

### 2.1 Layout XML Structure

**Basic Layout Example:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- ConstraintLayout - Most flexible and performant -->
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp"
    tools:context=".MainActivity">

    <!-- ImageView with constraints -->
    <ImageView
        android:id="@+id/profileImage"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:contentDescription="@string/profile_image_desc"
        android:src="@drawable/ic_profile_placeholder"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- TextView below ImageView -->
    <TextView
        android:id="@+id/nameText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="@string/default_name"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="@color/text_primary"
        app:layout_constraintTop_toBottomOf="@id/profileImage"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- EditText for input -->
    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/emailInputLayout"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:hint="@string/email_hint"
        app:layout_constraintTop_toBottomOf="@id/nameText"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/emailEditText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="textEmailAddress" />

    </com.google.android.material.textfield.TextInputLayout>

    <!-- Button at bottom -->
    <com.google.android.material.button.MaterialButton
        android:id="@+id/submitButton"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="@string/submit"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### 2.2 Resource Types and Organization

```
res/
├── drawable/                    # Images, shapes, vector drawables
│   ├── ic_launcher_background.xml
│   ├── ic_profile_placeholder.xml
│   ├── button_background.xml   # Shape drawable
│   └── gradient_background.xml
│
├── drawable-night/              # Dark mode drawables
│   └── ic_profile_placeholder.xml
│
├── layout/                      # Portrait layouts
│   ├── activity_main.xml
│   ├── fragment_home.xml
│   └── item_user.xml           # RecyclerView item
│
├── layout-land/                 # Landscape layouts
│   └── activity_main.xml
│
├── layout-sw600dp/              # Tablet layouts (7"+)
│   └── activity_main.xml
│
├── values/                      # Default values
│   ├── colors.xml
│   ├── strings.xml
│   ├── dimens.xml
│   ├── themes.xml
│   └── styles.xml
│
├── values-night/                # Dark mode colors
│   └── colors.xml
│
├── values-es/                   # Spanish translations
│   └── strings.xml
│
├── values-sw600dp/              # Tablet dimensions
│   └── dimens.xml
│
├── mipmap-mdpi/                 # Launcher icons (medium density)
├── mipmap-hdpi/                 # High density
├── mipmap-xhdpi/                # Extra high density
├── mipmap-xxhdpi/               # Extra extra high density
├── mipmap-xxxhdpi/              # Extra extra extra high density
│
├── menu/                        # Menu definitions
│   ├── main_menu.xml
│   └── bottom_navigation.xml
│
├── navigation/                  # Navigation graphs
│   └── nav_graph.xml
│
├── anim/                        # Tween animations
│   ├── fade_in.xml
│   └── slide_up.xml
│
├── animator/                    # Property animations
│   └── rotate.xml
│
├── xml/                         # Arbitrary XML files
│   ├── backup_rules.xml
│   ├── network_security_config.xml
│   └── file_paths.xml
│
└── raw/                         # Raw files (audio, video, etc.)
    └── notification_sound.mp3
```

**colors.xml:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Brand Colors -->
    <color name="primary">#6200EE</color>
    <color name="primary_variant">#3700B3</color>
    <color name="secondary">#03DAC6</color>
    <color name="secondary_variant">#018786</color>
    
    <!-- Background Colors -->
    <color name="background">#FFFFFF</color>
    <color name="surface">#FFFFFF</color>
    <color name="error">#B00020</color>
    
    <!-- Text Colors -->
    <color name="text_primary">#212121</color>
    <color name="text_secondary">#757575</color>
    <color name="text_on_primary">#FFFFFF</color>
    <color name="text_on_secondary">#000000</color>
    
    <!-- State Colors -->
    <color name="disabled">#BDBDBD</color>
    <color name="divider">#E0E0E0</color>
</resources>
```

**values-night/colors.xml (Dark Theme):**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Dark theme overrides -->
    <color name="background">#121212</color>
    <color name="surface">#1E1E1E</color>
    
    <color name="text_primary">#FFFFFF</color>
    <color name="text_secondary">#B0B0B0</color>
    
    <color name="divider">#2C2C2C</color>
</resources>
```

**strings.xml:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">My App</string>
    
    <!-- Screen Titles -->
    <string name="title_home">Home</string>
    <string name="title_profile">Profile</string>
    <string name="title_settings">Settings</string>
    
    <!-- Labels -->
    <string name="email_hint">Enter your email</string>
    <string name="password_hint">Enter your password</string>
    <string name="default_name">John Doe</string>
    
    <!-- Buttons -->
    <string name="submit">Submit</string>
    <string name="cancel">Cancel</string>
    <string name="retry">Retry</string>
    
    <!-- Messages -->
    <string name="error_network">Network error. Please try again.</string>
    <string name="error_invalid_email">Please enter a valid email</string>
    
    <!-- Plurals -->
    <plurals name="items_count">
        <item quantity="zero">No items</item>
        <item quantity="one">%d item</item>
        <item quantity="other">%d items</item>
    </plurals>
    
    <!-- Formatted strings -->
    <string name="welcome_message">Welcome, %1$s!</string>
    <string name="items_in_cart">You have %1$d items in your cart totaling %2$s</string>
    
    <!-- Accessibility -->
    <string name="profile_image_desc">User profile picture</string>
    <string name="btn_submit_desc">Submit form</string>
</resources>
```

**Using String Resources in Kotlin:**

```kotlin
// Simple string
val appName = getString(R.string.app_name)

// Formatted string
val welcome = getString(R.string.welcome_message, "John")
// Output: "Welcome, John!"

// Multiple parameters
val cartMessage = getString(R.string.items_in_cart, 5, "$49.99")
// Output: "You have 5 items in your cart totaling $49.99"

// Plurals
val itemsText = resources.getQuantityString(R.plurals.items_count, 0, 0)  // "No items"
val itemsText1 = resources.getQuantityString(R.plurals.items_count, 1, 1) // "1 item"
val itemsText5 = resources.getQuantityString(R.plurals.items_count, 5, 5) // "5 items"
```

### 2.3 Shape Drawables

**button_background.xml:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    
    <!-- Solid fill color -->
    <solid android:color="@color/primary" />
    
    <!-- Rounded corners -->
    <corners android:radius="8dp" />
    
    <!-- Padding -->
    <padding
        android:left="16dp"
        android:top="12dp"
        android:right="16dp"
        android:bottom="12dp" />
    
    <!-- Optional: Add stroke (border) -->
    <stroke
        android:width="1dp"
        android:color="@color/primary_variant" />
    
</shape>
```

**gradient_background.xml:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    
    <gradient
        android:type="linear"
        android:angle="135"
        android:startColor="#6200EE"
        android:centerColor="#9D46FF"
        android:endColor="#BB86FC" />
    
</shape>
```

**Selector Drawable (State-based):**

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- button_selector.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Pressed state -->
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/primary_variant" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    
    <!-- Disabled state -->
    <item android:state_enabled="false">
        <shape android:shape="rectangle">
            <solid android:color="@color/disabled" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    
    <!-- Default state (must be last) -->
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/primary" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    
</selector>
```

### 2.4 View Binding vs Data Binding

**View Binding (Simpler, Recommended for most cases):**

```kotlin
// build.gradle.kts
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
        
        // Type-safe view access
        binding.nameText.text = "John Doe"
        binding.submitButton.setOnClickListener {
            handleSubmit()
        }
    }
}

// Fragment
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Important: Avoid memory leaks
    }
}
```

**Data Binding (Two-way binding, expressions in XML):**

```kotlin
// build.gradle.kts
android {
    buildFeatures {
        dataBinding = true
    }
}
```

```xml
<!-- activity_main.xml with Data Binding -->
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <!-- Data variables -->
    <data>
        <import type="android.view.View" />
        <variable
            name="viewModel"
            type="com.example.app.MainViewModel" />
    </data>

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <!-- One-way binding -->
        <TextView
            android:id="@+id/nameText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@{viewModel.userName}"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintStart_toStartOf="parent" />

        <!-- Two-way binding (note the @={} syntax) -->
        <EditText
            android:id="@+id/emailInput"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="@={viewModel.email}"
            app:layout_constraintTop_toBottomOf="@id/nameText" />

        <!-- Conditional visibility -->
        <ProgressBar
            android:id="@+id/loadingSpinner"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:visibility="@{viewModel.isLoading ? View.VISIBLE : View.GONE}"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />

        <!-- Click binding -->
        <Button
            android:id="@+id/submitButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Submit"
            android:onClick="@{() -> viewModel.onSubmitClicked()}"
            android:enabled="@{!viewModel.isLoading}"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent" />

    </androidx.constraintlayout.widget.ConstraintLayout>
</layout>
```

```kotlin
// Activity with Data Binding
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

// ViewModel
class MainViewModel : ViewModel() {
    
    private val _userName = MutableLiveData("John Doe")
    val userName: LiveData<String> = _userName
    
    val email = MutableLiveData("")  // Two-way binding
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun onSubmitClicked() {
        _isLoading.value = true
        // Perform submission
    }
}
```

---

## 3. Gradle with Kotlin DSL

### 3.1 Project Structure

```
project-root/
├── build.gradle.kts           # Root build script
├── settings.gradle.kts        # Project settings
├── gradle.properties          # Gradle properties
├── local.properties           # Local SDK path (gitignored)
├── gradle/
│   ├── wrapper/
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml     # Version catalog
├── app/
│   ├── build.gradle.kts       # App module build script
│   └── proguard-rules.pro
└── feature-module/
    └── build.gradle.kts       # Feature module build script
```

### 3.2 Version Catalog (libs.versions.toml)

```toml
[versions]
# Android SDK
compileSdk = "34"
minSdk = "24"
targetSdk = "34"

# Kotlin & Compose
kotlin = "1.9.22"
compose-bom = "2024.02.00"
compose-compiler = "1.5.8"

# AndroidX
core-ktx = "1.12.0"
lifecycle = "2.7.0"
activity-compose = "1.8.2"
navigation = "2.7.7"

# Dependency Injection
hilt = "2.50"
hilt-navigation-compose = "1.1.0"

# Networking
retrofit = "2.9.0"
okhttp = "4.12.0"
moshi = "1.15.0"

# Database
room = "2.6.1"

# Testing
junit = "4.13.2"
androidx-test = "1.5.0"
espresso = "3.5.1"

# Plugins
android-gradle-plugin = "8.2.2"
ksp = "1.9.22-1.0.17"

[libraries]
# Core
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hilt-navigation-compose" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
moshi = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshi" }
moshi-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshi" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-test-core = { group = "androidx.test", name = "core", version.ref = "androidx-test" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }

[bundles]
compose = [
    "compose-ui",
    "compose-ui-graphics",
    "compose-ui-tooling-preview",
    "compose-material3",
]
networking = [
    "retrofit",
    "retrofit-moshi",
    "okhttp",
    "okhttp-logging",
    "moshi",
]
room = [
    "room-runtime",
    "room-ktx",
]

[plugins]
android-application = { id = "com.android.application", version.ref = "android-gradle-plugin" }
android-library = { id = "com.android.library", version.ref = "android-gradle-plugin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

### 3.3 Root build.gradle.kts

```kotlin
// Top-level build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

// Clean task
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
```

### 3.4 App build.gradle.kts (Complete Example)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.myapp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }

        // Build config fields
        buildConfigField("String", "API_BASE_URL", "\"https://api.example.com\"")
    }

    // Signing configurations
    signingConfigs {
        create("release") {
            storeFile = file("keystore/release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.example.com\"")
        }
        
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Product flavors for different app versions
    flavorDimensions += "version"
    
    productFlavors {
        create("free") {
            dimension = "version"
            applicationIdSuffix = ".free"
            versionNameSuffix = "-free"
            
            buildConfigField("Boolean", "IS_PREMIUM", "false")
            resValue("string", "app_name", "MyApp Free")
        }
        
        create("premium") {
            dimension = "version"
            applicationIdSuffix = ".premium"
            
            buildConfigField("Boolean", "IS_PREMIUM", "true")
            resValue("string", "app_name", "MyApp Premium")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true  // If using Views alongside Compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Networking
    implementation(libs.bundles.networking)
    ksp(libs.moshi.codegen)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
}
```

### 3.5 Custom Gradle Tasks

```kotlin
// Custom task example in build.gradle.kts

// Task to print version info
tasks.register("printVersion") {
    group = "info"
    description = "Prints the current version"
    
    doLast {
        val versionName = android.defaultConfig.versionName
        val versionCode = android.defaultConfig.versionCode
        println("Version: $versionName ($versionCode)")
    }
}

// Task to copy APK to specific location
tasks.register<Copy>("copyReleaseApk") {
    group = "distribution"
    description = "Copies release APK to distribution folder"
    
    dependsOn("assembleRelease")
    
    from("build/outputs/apk/premium/release/")
    into("../distribution/")
    include("*.apk")
    
    rename { filename ->
        val versionName = android.defaultConfig.versionName
        "myapp-v$versionName.apk"
    }
}

// Task to run all checks
tasks.register("checks") {
    group = "verification"
    description = "Runs all checks"
    
    dependsOn("lint", "test", "ktlintCheck")
}
```

---

## 4. C/C++ and NDK (Native Development Kit)

### 4.1 When to Use NDK

| Use Case | Example | Why NDK? |
|----------|---------|----------|
| **Performance-critical computation** | Image filters, video encoding | CPU-bound operations 10-100x faster |
| **Existing C/C++ libraries** | OpenCV, FFmpeg, TensorFlow Lite | Reuse instead of rewrite |
| **Game development** | Physics engines, rendering | Direct hardware access |
| **Security-sensitive code** | Encryption, DRM | Harder to reverse-engineer |
| **Cross-platform code** | Core business logic | Share code with iOS/desktop |
| **Hardware access** | Sensors, USB devices | Low-level APIs |

### 4.2 NDK Project Structure

```
app/
├── build.gradle.kts
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/app/
│       │       └── NativeLib.kt
│       ├── cpp/                    # Native source files
│       │   ├── CMakeLists.txt      # CMake configuration
│       │   ├── native-lib.cpp      # Main C++ file
│       │   ├── image_processor.cpp
│       │   ├── image_processor.h
│       │   └── utils/
│       │       ├── math_utils.cpp
│       │       └── math_utils.h
│       └── jniLibs/                # Pre-built libraries (optional)
│           ├── arm64-v8a/
│           │   └── libthirdparty.so
│           ├── armeabi-v7a/
│           │   └── libthirdparty.so
│           └── x86_64/
│               └── libthirdparty.so
```

### 4.3 Gradle Configuration for NDK

```kotlin
// app/build.gradle.kts
android {
    namespace = "com.example.ndkapp"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34

        // NDK configuration
        ndk {
            // Specify which ABIs to build for
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
        
        externalNativeBuild {
            cmake {
                // CMake arguments
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_TOOLCHAIN=clang"
                )
                // C++ flags
                cppFlags += listOf("-std=c++17", "-O2")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        debug {
            externalNativeBuild {
                cmake {
                    cppFlags += "-DDEBUG"
                }
            }
        }
        release {
            externalNativeBuild {
                cmake {
                    cppFlags += listOf("-DNDEBUG", "-O3")
                }
            }
        }
    }

    // Split APKs by ABI for smaller download size
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true  // Also build universal APK
        }
    }
}
```

### 4.4 CMakeLists.txt

```cmake
# CMakeLists.txt
cmake_minimum_required(VERSION 3.22.1)

project("myapp" VERSION 1.0.0 LANGUAGES CXX)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find required packages
find_library(log-lib log)
find_library(android-lib android)

# Add your source files
add_library(
    native-lib
    SHARED
    
    # Main JNI file
    native-lib.cpp
    
    # Additional source files
    image_processor.cpp
    utils/math_utils.cpp
)

# Include directories
target_include_directories(
    native-lib
    PRIVATE
    ${CMAKE_SOURCE_DIR}
    ${CMAKE_SOURCE_DIR}/utils
)

# Link libraries
target_link_libraries(
    native-lib
    ${log-lib}
    ${android-lib}
)

# Optional: Link to a pre-built library
# add_library(thirdparty SHARED IMPORTED)
# set_target_properties(
#     thirdparty
#     PROPERTIES IMPORTED_LOCATION
#     ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libthirdparty.so
# )
# target_link_libraries(native-lib thirdparty)
```

### 4.5 JNI (Java Native Interface) Code

**Kotlin Side:**

```kotlin
// NativeLib.kt
package com.example.ndkapp

class NativeLib {
    
    // Load native library when class is loaded
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    
    // Declare external functions (implemented in C++)
    
    // Simple function
    external fun getVersionString(): String
    
    // Function with primitive parameters
    external fun addNumbers(a: Int, b: Int): Int
    
    // Function with array parameter
    external fun processIntArray(input: IntArray): IntArray
    
    // Function with object parameter
    external fun processUser(user: User): String
    
    // Function with callback
    external fun performAsyncOperation(callback: NativeCallback)
    
    // Function that can throw
    @Throws(NativeException::class)
    external fun riskyOperation(): Boolean
}

// Data class passed to native code
data class User(
    val id: Int,
    val name: String,
    val email: String
)

// Callback interface for native code
interface NativeCallback {
    fun onProgress(progress: Int)
    fun onComplete(result: String)
    fun onError(errorCode: Int, message: String)
}

// Custom exception from native code
class NativeException(message: String) : Exception(message)
```

**C++ Side (native-lib.cpp):**

```cpp
#include <jni.h>
#include <string>
#include <android/log.h>

// Logging macros
#define LOG_TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// JNI function naming convention:
// Java_<package>_<class>_<method>
// Package separators (.) become underscores (_)
// Underscores in names become _1

extern "C" {

// Simple function returning a string
JNIEXPORT jstring JNICALL
Java_com_example_ndkapp_NativeLib_getVersionString(
        JNIEnv *env,
        jobject /* this */) {
    
    std::string version = "Native Library v1.0.0";
    return env->NewStringUTF(version.c_str());
}

// Function with primitive parameters
JNIEXPORT jint JNICALL
Java_com_example_ndkapp_NativeLib_addNumbers(
        JNIEnv *env,
        jobject /* this */,
        jint a,
        jint b) {
    
    LOGI("Adding %d + %d", a, b);
    return a + b;
}

// Function processing an array
JNIEXPORT jintArray JNICALL
Java_com_example_ndkapp_NativeLib_processIntArray(
        JNIEnv *env,
        jobject /* this */,
        jintArray input) {
    
    // Get array length
    jsize length = env->GetArrayLength(input);
    
    // Get pointer to array elements
    jint *inputElements = env->GetIntArrayElements(input, nullptr);
    
    // Create output array
    jintArray output = env->NewIntArray(length);
    jint *outputElements = env->GetIntArrayElements(output, nullptr);
    
    // Process: double each element
    for (int i = 0; i < length; i++) {
        outputElements[i] = inputElements[i] * 2;
    }
    
    // Release arrays (important for memory management!)
    // 0 = copy back and free the buffer
    env->ReleaseIntArrayElements(input, inputElements, JNI_ABORT);  // Don't copy back
    env->ReleaseIntArrayElements(output, outputElements, 0);  // Copy back changes
    
    return output;
}

// Function processing an object
JNIEXPORT jstring JNICALL
Java_com_example_ndkapp_NativeLib_processUser(
        JNIEnv *env,
        jobject /* this */,
        jobject user) {
    
    // Get the User class
    jclass userClass = env->GetObjectClass(user);
    
    // Get field IDs
    jfieldID idField = env->GetFieldID(userClass, "id", "I");  // I = int
    jfieldID nameField = env->GetFieldID(userClass, "name", "Ljava/lang/String;");
    jfieldID emailField = env->GetFieldID(userClass, "email", "Ljava/lang/String;");
    
    // Get field values
    jint id = env->GetIntField(user, idField);
    jstring jName = (jstring) env->GetObjectField(user, nameField);
    jstring jEmail = (jstring) env->GetObjectField(user, emailField);
    
    // Convert Java strings to C++ strings
    const char *name = env->GetStringUTFChars(jName, nullptr);
    const char *email = env->GetStringUTFChars(jEmail, nullptr);
    
    // Process
    std::string result = "Processed user: " + std::to_string(id) + 
                        ", " + name + ", " + email;
    
    // Release strings (important!)
    env->ReleaseStringUTFChars(jName, name);
    env->ReleaseStringUTFChars(jEmail, email);
    
    // Clean up local reference
    env->DeleteLocalRef(userClass);
    
    return env->NewStringUTF(result.c_str());
}

// Function with callback
JNIEXPORT void JNICALL
Java_com_example_ndkapp_NativeLib_performAsyncOperation(
        JNIEnv *env,
        jobject /* this */,
        jobject callback) {
    
    // Get callback class
    jclass callbackClass = env->GetObjectClass(callback);
    
    // Get method IDs
    jmethodID onProgress = env->GetMethodID(callbackClass, "onProgress", "(I)V");
    jmethodID onComplete = env->GetMethodID(callbackClass, "onComplete", "(Ljava/lang/String;)V");
    jmethodID onError = env->GetMethodID(callbackClass, "onError", "(ILjava/lang/String;)V");
    
    // Simulate work with progress updates
    for (int i = 0; i <= 100; i += 10) {
        // Call onProgress
        env->CallVoidMethod(callback, onProgress, i);
        
        // Simulate work
        // Note: In real code, don't block the JNI thread like this
    }
    
    // Call onComplete
    jstring result = env->NewStringUTF("Operation completed successfully");
    env->CallVoidMethod(callback, onComplete, result);
    
    // Clean up
    env->DeleteLocalRef(callbackClass);
    env->DeleteLocalRef(result);
}

// Function that throws exception
JNIEXPORT jboolean JNICALL
Java_com_example_ndkapp_NativeLib_riskyOperation(
        JNIEnv *env,
        jobject /* this */) {
    
    // Simulate an error condition
    bool errorOccurred = true;
    
    if (errorOccurred) {
        // Find the exception class
        jclass exceptionClass = env->FindClass("com/example/ndkapp/NativeException");
        
        if (exceptionClass != nullptr) {
            // Throw the exception
            env->ThrowNew(exceptionClass, "Native operation failed: error code 42");
            env->DeleteLocalRef(exceptionClass);
        }
        
        return JNI_FALSE;
    }
    
    return JNI_TRUE;
}

} // extern "C"
```

### 4.6 JNI Type Signatures

| Java Type | JNI Type | Signature |
|-----------|----------|-----------|
| `void` | `void` | `V` |
| `boolean` | `jboolean` | `Z` |
| `byte` | `jbyte` | `B` |
| `char` | `jchar` | `C` |
| `short` | `jshort` | `S` |
| `int` | `jint` | `I` |
| `long` | `jlong` | `J` |
| `float` | `jfloat` | `F` |
| `double` | `jdouble` | `D` |
| `Object` | `jobject` | `L<classname>;` |
| `String` | `jstring` | `Ljava/lang/String;` |
| `int[]` | `jintArray` | `[I` |
| `Object[]` | `jobjectArray` | `[L<classname>;` |

**Method Signature Examples:**

```cpp
// void method()
"()V"

// int method(int, int)
"(II)I"

// String method(String)
"(Ljava/lang/String;)Ljava/lang/String;"

// void method(int, String, boolean)
"(ILjava/lang/String;Z)V"

// int[] method(int[])
"([I)[I"
```

### 4.7 Memory Management in JNI

```cpp
// CRITICAL: Memory management rules

// 1. Always release array elements
jint *elements = env->GetIntArrayElements(array, nullptr);
// ... use elements ...
env->ReleaseIntArrayElements(array, elements, 0);

// 2. Always release string characters
const char *str = env->GetStringUTFChars(jstr, nullptr);
// ... use str ...
env->ReleaseStringUTFChars(jstr, str);

// 3. Delete local references when done (optional for small objects)
jobject localRef = env->NewObject(...);
// ... use localRef ...
env->DeleteLocalRef(localRef);

// 4. Create global references for objects that persist
jobject globalRef = env->NewGlobalRef(localRef);
// ... use globalRef across JNI calls ...
env->DeleteGlobalRef(globalRef);  // When no longer needed

// 5. Handle exceptions
if (env->ExceptionCheck()) {
    env->ExceptionDescribe();  // Print to logcat
    env->ExceptionClear();     // Clear the exception
    // Handle error...
}
```

### 4.8 Using Native Code from Kotlin

```kotlin
class ImageProcessor {
    
    private val nativeLib = NativeLib()
    
    fun processImage(bitmap: Bitmap): Bitmap {
        // Get pixels from bitmap
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // Process in native code
        val processedPixels = nativeLib.processIntArray(pixels)
        
        // Create new bitmap with processed pixels
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(processedPixels, 0, width, 0, 0, width, height)
        
        return result
    }
    
    fun performBackgroundWork() {
        nativeLib.performAsyncOperation(object : NativeCallback {
            override fun onProgress(progress: Int) {
                Log.d("ImageProcessor", "Progress: $progress%")
            }
            
            override fun onComplete(result: String) {
                Log.d("ImageProcessor", "Complete: $result")
            }
            
            override fun onError(errorCode: Int, message: String) {
                Log.e("ImageProcessor", "Error $errorCode: $message")
            }
        })
    }
}
```

---

## 5. Interview Questions & Answers

### Java & Kotlin Interoperability

**Q1: How do you call a Kotlin extension function from Java?**

Extension functions compile to static methods with the receiver as the first parameter:

```kotlin
// Kotlin
fun String.addExclamation() = "$this!"
```

```java
// Java
String result = StringKt.addExclamation("Hello");  // "Hello!"
```

**Q2: What is a platform type in Kotlin and why is it dangerous?**

Platform types (shown as `Type!`) represent types from Java where nullability is unknown. They're dangerous because:
- Kotlin can't enforce null safety
- Can cause NullPointerException at runtime
- Should be treated as nullable or validated

**Q3: How do you expose a Kotlin property as a Java field?**

Use `@JvmField` annotation:
```kotlin
@JvmField
val MAX_SIZE = 100
```

---

### XML & Resources

**Q4: What's the difference between `dp`, `sp`, `px`, and `pt`?**

| Unit | Description | Use Case |
|------|-------------|----------|
| `dp` | Density-independent pixels | Sizes, margins, padding |
| `sp` | Scale-independent pixels | Text sizes (respects user font settings) |
| `px` | Actual pixels | Avoid (not density-aware) |
| `pt` | Points (1/72 inch) | Rarely used |

**Q5: How does Android choose between different resource qualifiers?**

Android follows a priority order:
1. MCC/MNC (Mobile Country/Network Code)
2. Language and locale
3. Layout direction (ldrtl/ldltr)
4. Smallest width (sw600dp)
5. Available width/height
6. Screen size (small/normal/large/xlarge)
7. Screen aspect
8. Screen orientation
9. UI mode (car, desk, watch)
10. Night mode
11. Screen pixel density (hdpi/xhdpi/xxhdpi)

**Q6: What's the difference between View Binding and Data Binding?**

| Feature | View Binding | Data Binding |
|---------|--------------|--------------|
| Null Safety | Yes | Yes |
| Type Safety | Yes | Yes |
| Build Speed | Faster | Slower |
| XML Expressions | No | Yes |
| Two-way Binding | No | Yes |
| Observable Data | No | Yes |
| Complexity | Simple | More complex |

---

### Gradle

**Q7: What are build variants and how are they created?**

Build variants = Build Types × Product Flavors

```
Build Types: debug, release
Flavors: free, premium

Variants:
- freeDebug
- freeRelease
- premiumDebug
- premiumRelease
```

**Q8: How do you share dependencies across multiple modules?**

Use a version catalog (`libs.versions.toml`) and reference in each module:
```kotlin
implementation(libs.retrofit)
```

Or use a convention plugin for common configurations.

---

### NDK

**Q9: When would you choose NDK over pure Kotlin?**

- CPU-intensive operations (10x+ performance gain expected)
- Using existing C/C++ libraries
- Cross-platform code sharing (iOS/Android)
- Security-sensitive code (harder to reverse-engineer)
- Direct hardware access requirements

**Q10: What are the performance implications of JNI calls?**

JNI calls have overhead:
- ~100ns per simple call
- String/array conversions are expensive
- Memory allocation across boundaries is costly

**Best Practice:** Batch operations in native code, minimize boundary crossings:
```cpp
// Bad: Call JNI for each pixel
// Good: Pass entire array, process all pixels, return result
```

**Q11: How do you handle threading with JNI?**

- JNIEnv* is thread-local (can't share between threads)
- Use `AttachCurrentThread()` for native threads
- Callbacks to Java must happen on attached threads
- Consider using `JavaVM*` for multi-threaded native code

```cpp
JavaVM* jvm;
env->GetJavaVM(&jvm);

// In native thread:
JNIEnv* threadEnv;
jvm->AttachCurrentThread(&threadEnv, nullptr);
// ... use threadEnv ...
jvm->DetachCurrentThread();
```

---

## Quick Reference Card

```
┌──────────────────────────────────────────────────────────────┐
│                    JAVA ↔ KOTLIN INTEROP                     │
├──────────────────────────────────────────────────────────────┤
│  Kotlin → Java Annotations:                                  │
│    @JvmStatic    - Static method access                     │
│    @JvmField     - Direct field access                      │
│    @JvmOverloads - Generate overloaded methods              │
│    @JvmName      - Custom function name                     │
│    @Throws       - Declare checked exceptions               │
├──────────────────────────────────────────────────────────────┤
│  Java → Kotlin:                                             │
│    getX()/setX() → .x property                             │
│    isX()         → .isX property                           │
│    Type!         → Platform type (careful!)                │
├──────────────────────────────────────────────────────────────┤
│                       XML RESOURCES                          │
├──────────────────────────────────────────────────────────────┤
│  Qualifiers:  -night, -land, -sw600dp, -es, -xxhdpi        │
│  References:  @string/name, @color/primary, @dimen/margin  │
│  Themes:      ?attr/colorPrimary                           │
├──────────────────────────────────────────────────────────────┤
│                      GRADLE KOTLIN DSL                       │
├──────────────────────────────────────────────────────────────┤
│  libs.versions.toml → Central dependency management         │
│  libs.plugins.x     → Plugin references                    │
│  libs.bundles.x     → Dependency bundles                   │
├──────────────────────────────────────────────────────────────┤
│                      JNI TYPE SIGNATURES                     │
├──────────────────────────────────────────────────────────────┤
│  V=void  Z=boolean  I=int  J=long  F=float  D=double       │
│  L<class>;=Object  [I=int[]  Ljava/lang/String;=String     │
└──────────────────────────────────────────────────────────────┘
```

---

*Document created for Android Developer Interview Preparation*
*Last updated: February 2026*
