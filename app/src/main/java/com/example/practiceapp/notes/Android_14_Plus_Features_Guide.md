# 15.1 Android 14+ Features

## Overview

Android 14 (API 34) and beyond introduce significant platform improvements focused on **user privacy**, **customization**, **accessibility**, and **modern gesture navigation**. These features help developers build more intuitive, personalized, and secure applications.

```
┌─────────────────────────────────────────────────────────────────┐
│                   Android 14+ Feature Categories                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐                     │
│  │  Predictive Back │  │  Grammatical     │                     │
│  │  Gestures        │  │  Inflection API  │                     │
│  │  ─────────────── │  │  ─────────────── │                     │
│  │  • Back preview  │  │  • Gender-aware  │                     │
│  │  • Animations    │  │    translations  │                     │
│  │  • Custom logic  │  │  • Per-language  │                     │
│  └──────────────────┘  └──────────────────┘                     │
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐                     │
│  │  Photo Picker    │  │  Per-App         │                     │
│  │  Improvements    │  │  Language Prefs  │                     │
│  │  ─────────────── │  │  ─────────────── │                     │
│  │  • Embedded mode │  │  • In-app locale │                     │
│  │  • Cloud photos  │  │  • System UI     │                     │
│  │  • Ordered sel.  │  │  • Auto restart  │                     │
│  └──────────────────┘  └──────────────────┘                     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1. Predictive Back Gestures

### Theory

Predictive back gestures allow the system to **show a preview of the destination** before the user completes the back gesture. Instead of immediately navigating back, the system shows an animation that gives the user a glimpse of where they'll go — the previous screen, the home screen, or a custom destination.

**Key Concepts:**
- **Back Preview**: System shows what's behind the current screen as the user swipes
- **OnBackPressedCallback**: The programmatic way to intercept and handle back gestures
- **Ahead-of-time model**: Apps must declare their back behavior in advance so the system can animate
- **System animations**: Cross-activity, cross-task, and return-to-home animations

```
┌─────────────────────────────────────────────────────────────────┐
│               Predictive Back Gesture Flow                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  User starts back swipe                                          │
│       │                                                          │
│       ▼                                                          │
│  ┌─────────────────────┐                                        │
│  │  System checks      │                                        │
│  │  registered callbacks│                                       │
│  └──────────┬──────────┘                                        │
│             │                                                    │
│     ┌───────┴───────┐                                           │
│     │               │                                            │
│     ▼               ▼                                            │
│  Callback         No Callback                                   │
│  Enabled          or Disabled                                    │
│     │               │                                            │
│     ▼               ▼                                            │
│  ┌──────────┐  ┌──────────────┐                                 │
│  │ Custom   │  │ System shows │                                 │
│  │ preview  │  │ default back │                                 │
│  │ animation│  │ animation    │                                 │
│  └────┬─────┘  └──────┬───────┘                                 │
│       │               │                                          │
│       ▼               ▼                                          │
│  User completes or cancels the gesture                          │
│       │                                                          │
│  ┌────┴──────┐   ┌──────────┐                                   │
│  │ Complete  │   │  Cancel  │                                    │
│  │ → Execute │   │  → Stay  │                                    │
│  │   back    │   │    here  │                                    │
│  └───────────┘   └──────────┘                                   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Enabling Predictive Back in Manifest

```xml
<!-- AndroidManifest.xml -->
<application
    android:enableOnBackInvokedCallback="true"
    ... >
    <!-- This enables the predictive back gesture system-wide for your app -->
</application>
```

### Code: Basic OnBackPressedCallback (Kotlin)

```kotlin
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class PredictiveBackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predictive_back)

        // Register a callback for handling back press
        val callback = object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                // Custom back logic
                if (hasUnsavedChanges()) {
                    showSaveDialog()
                } else {
                    // Disable this callback and let the system handle it
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        // Add callback to the dispatcher
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun hasUnsavedChanges(): Boolean {
        // Check if user has unsaved work
        return false
    }

    private fun showSaveDialog() {
        // Show a dialog asking user to save
    }
}
```

### Code: Predictive Back with Custom Animation (Kotlin)

```kotlin
import android.os.Bundle
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class CustomPredictiveBackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_back)

        // For API 34+ — use OnBackInvokedCallback for predictive animations
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                // Handle back invocation
                handleCustomBack()
            }
        }

        // For backwards compatibility — use OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleCustomBack()
                }
            }
        )
    }

    private fun handleCustomBack() {
        // Perform custom back navigation
        supportFragmentManager.let { fm ->
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
            } else {
                finish()
            }
        }
    }
}
```

### Code: Predictive Back in Jetpack Compose (Kotlin)

```kotlin
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.material3.*

@Composable
fun PredictiveBackScreen(
    onNavigateBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }

    // BackHandler integrates with the OnBackPressedDispatcher
    BackHandler(enabled = true) {
        if (hasChanges) {
            showDialog = true
        } else {
            onNavigateBack()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("Do you want to save your changes?") },
            confirmButton = {
                TextButton(onClick = {
                    // Save changes and navigate back
                    showDialog = false
                    onNavigateBack()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onNavigateBack()
                }) {
                    Text("Discard")
                }
            }
        )
    }

    // Screen content
    Column {
        TextField(
            value = "",
            onValueChange = { hasChanges = true },
            label = { Text("Enter something") }
        )
    }
}
```

### Code: Predictive Back with Progress Animation (API 34+)

```kotlin
import android.os.Build
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity

class AnimatedBackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animated_back)

        if (Build.VERSION.SDK_INT >= 34) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                object : OnBackAnimationCallback {
                    override fun onBackStarted(backEvent: BackEvent) {
                        // Called when the back gesture starts
                        // backEvent.touchX, backEvent.touchY — finger position
                        // backEvent.progress — 0.0 to 1.0
                        // backEvent.swipeEdge — LEFT or RIGHT
                    }

                    override fun onBackProgressed(backEvent: BackEvent) {
                        // Called as user swipes — animate based on progress
                        val progress = backEvent.progress
                        // Scale down the view as user swipes
                        val scale = 1f - (0.1f * progress)
                        binding.contentView.scaleX = scale
                        binding.contentView.scaleY = scale
                    }

                    override fun onBackInvoked() {
                        // User completed the back gesture
                        finish()
                    }

                    override fun onBackCancelled() {
                        // User cancelled — reset animations
                        binding.contentView.scaleX = 1f
                        binding.contentView.scaleY = 1f
                    }
                }
            )
        }
    }
}
```

---

## 2. Grammatical Inflection API

### Theory

The Grammatical Inflection API (Android 14+) allows apps to **address users with the correct grammatical gender** in gendered languages (like French, German, Arabic, Polish, etc.). Without this API, many languages default to masculine forms, which can feel impersonal for non-male users.

**Key Concepts:**
- **Grammatical Gender**: Languages have gendered nouns, adjectives, and verb forms
- **GrammaticalInflectionManager**: System service to set the user's grammatical gender preference
- **Inflected Resources**: Provide gender-specific string variants in resource files
- **Gender Values**: `NEUTER`, `FEMININE`, `MASCULINE`, `NOT_SPECIFIED`

```
┌─────────────────────────────────────────────────────────────────┐
│              Grammatical Inflection Flow                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   User selects gender preference                                │
│          │                                                       │
│          ▼                                                       │
│   ┌──────────────────────────┐                                  │
│   │ GrammaticalInflection    │                                  │
│   │ Manager                  │                                  │
│   │ .setRequestedGrammatical │                                  │
│   │  Gender(FEMININE)        │                                  │
│   └────────────┬─────────────┘                                  │
│                │                                                 │
│                ▼                                                 │
│   ┌──────────────────────────┐                                  │
│   │ System restarts activity │                                  │
│   │ with new configuration   │                                  │
│   └────────────┬─────────────┘                                  │
│                │                                                 │
│                ▼                                                 │
│   ┌──────────────────────────┐                                  │
│   │ Resource system loads    │                                  │
│   │ gender-specific strings  │                                  │
│   │ ─────────────────────    │                                  │
│   │ "Bienvenue" →            │                                  │
│   │   "Bienvenue" (masc.)    │                                  │
│   │   "Bienvenue" (fem.)     │                                  │
│   └──────────────────────────┘                                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Setting Grammatical Gender (Kotlin)

```kotlin
import android.app.GrammaticalInflectionManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class GrammaticalInflectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grammatical)

        setupGenderSelection()
    }

    private fun setupGenderSelection() {
        if (Build.VERSION.SDK_INT >= 34) {
            val inflectionManager = getSystemService(
                GrammaticalInflectionManager::class.java
            )

            // Set gender based on user preference
            binding.radioGroupGender.setOnCheckedChangeListener { _, checkedId ->
                val gender = when (checkedId) {
                    R.id.radioFeminine ->
                        Configuration.GRAMMATICAL_GENDER_FEMININE
                    R.id.radioMasculine ->
                        Configuration.GRAMMATICAL_GENDER_MASCULINE
                    R.id.radioNeutral ->
                        Configuration.GRAMMATICAL_GENDER_NEUTRAL
                    else ->
                        Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED
                }
                inflectionManager.setRequestedGrammaticalGender(gender)
            }
        }
    }
}
```

### Gender-Specific String Resources

```xml
<!-- res/values-fr/strings.xml (French - Default/Masculine) -->
<resources>
    <string name="welcome_message">Bienvenu sur notre app</string>
    <string name="you_are_invited">Vous êtes invité</string>
</resources>

<!-- res/values-fr-feminine/strings.xml (French - Feminine) -->
<resources>
    <string name="welcome_message">Bienvenue sur notre app</string>
    <string name="you_are_invited">Vous êtes invitée</string>
</resources>

<!-- res/values-fr-neutral/strings.xml (French - Neutral) -->
<resources>
    <string name="welcome_message">Content de vous voir sur notre app</string>
    <string name="you_are_invited">Vous avez une invitation</string>
</resources>
```

---

## 3. Photo Picker Improvements

### Theory

Android 14 enhances the **system photo picker** introduced in Android 13. The photo picker provides a **safe, privacy-friendly** way for users to grant access to specific photos/videos without giving the app full media library access.

**Android 14+ Improvements:**
- **Embedded Photo Picker**: Can be embedded directly inside your app's UI (not just as a separate activity)
- **Cloud Media Provider**: Access photos from cloud storage (Google Photos) alongside local media
- **Ordered Selection**: Users can select photos in a specific order
- **Search Functionality**: Built-in search within the picker
- **Partial Media Access**: Grant access to selected items only (READ_MEDIA_VISUAL_USER_SELECTED)

```
┌─────────────────────────────────────────────────────────────────┐
│                  Photo Picker Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐      ┌─────────────────────────┐              │
│  │  Your App   │      │     System Photo Picker  │              │
│  │             │─────>│  ┌───────────────────┐   │              │
│  │  Launch     │      │  │  Local Photos     │   │              │
│  │  Picker     │      │  ├───────────────────┤   │              │
│  │             │      │  │  Cloud Photos     │   │              │
│  │             │      │  │  (Google Photos)  │   │              │
│  │             │      │  ├───────────────────┤   │              │
│  │  Receive    │<─────│  │  Screenshots      │   │              │
│  │  URIs       │      │  ├───────────────────┤   │              │
│  │             │      │  │  Downloads        │   │              │
│  └─────────────┘      │  └───────────────────┘   │              │
│                        │                          │              │
│  No permissions        │  User selects specific   │              │
│  required!             │  photos only             │              │
│                        └─────────────────────────┘              │
│                                                                  │
│  Privacy: App only sees the photos user explicitly selects      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Using Photo Picker (Kotlin)

```kotlin
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PhotoPickerActivity : AppCompatActivity() {

    // Single photo picker
    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // User selected a photo
            binding.imageView.setImageURI(uri)

            // Take persistable permission for later access
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } else {
            // User cancelled
        }
    }

    // Multiple photo picker
    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            // User selected photos
            uris.forEach { uri ->
                // Process each selected URI
                processSelectedMedia(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_picker)

        // Launch picker for images only
        binding.btnPickImage.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        // Launch picker for videos only
        binding.btnPickVideo.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.VideoOnly
                )
            )
        }

        // Launch picker for both images and videos
        binding.btnPickBoth.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageAndVideo
                )
            )
        }

        // Launch picker for specific MIME type (e.g., GIFs)
        binding.btnPickGif.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.SingleMimeType("image/gif")
                )
            )
        }

        // Launch multiple photo picker
        binding.btnPickMultiple.setOnClickListener {
            pickMultipleMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageAndVideo
                )
            )
        }
    }

    private fun processSelectedMedia(uri: Uri) {
        // Process the selected media URI
    }
}
```

### Code: Photo Picker in Jetpack Compose (Kotlin)

```kotlin
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import coil.compose.rememberAsyncImagePainter

@Composable
fun PhotoPickerScreen() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Single photo picker launcher
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    // Multiple photo picker launcher
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        selectedImageUris = uris
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = {
                singlePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        ) {
            Text("Pick Single Photo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                multiplePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    )
                )
            }
        ) {
            Text("Pick Multiple Photos")
        }

        // Display selected image
        selectedImageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Selected photo",
                modifier = Modifier.size(200.dp)
            )
        }
    }
}
```

### Code: Partial Media Access (Android 14+)

```kotlin
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PartialMediaAccessActivity : AppCompatActivity() {

    // New Android 14 permission for partial media access
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true -> {
                // Full access to images
                loadAllImages()
            }
            permissions[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true -> {
                // Partial access — only user-selected photos
                loadSelectedImages()
            }
            else -> {
                // No access
                showPermissionDeniedMessage()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 34) {
            requestPermissions.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            )
        } else if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions.launch(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            )
        } else {
            requestPermissions.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }
    }

    // Re-request on each app launch for partial access
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 34) {
            val partialAccess = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == PackageManager.PERMISSION_GRANTED

            val fullAccess = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED

            if (partialAccess && !fullAccess) {
                // User granted partial access — show option to select more
                showSelectMorePhotosButton()
            }
        }
    }

    private fun loadAllImages() { /* Load all images */ }
    private fun loadSelectedImages() { /* Load only selected images */ }
    private fun showPermissionDeniedMessage() { /* Show denied UI */ }
    private fun showSelectMorePhotosButton() { /* Show "Select more" button */ }
}
```

---

## 4. Per-App Language Preferences

### Theory

Per-app language preferences (introduced in Android 13, enhanced in Android 14) allow users to set **different languages for different apps** independently of the system language. This is crucial for multilingual users.

**Key Concepts:**
- **AppCompatDelegate.setApplicationLocales()**: Jetpack API for changing app locale
- **LocaleManager**: Platform API (API 33+) for per-app locale
- **android:localeConfig**: Manifest attribute to declare supported locales
- **System Settings Integration**: Users can change app language from system settings
- **Auto-restart**: App activities are automatically recreated when locale changes

```
┌─────────────────────────────────────────────────────────────────┐
│                Per-App Language Flow                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐        │
│  │  System      │   │  App A       │   │  App B       │        │
│  │  Language:   │   │  Language:   │   │  Language:   │        │
│  │  English     │   │  French      │   │  Arabic      │        │
│  └──────────────┘   └──────────────┘   └──────────────┘        │
│                                                                  │
│  How it works:                                                  │
│  ─────────────                                                  │
│  1. App declares supported locales in locale_config.xml         │
│  2. System shows languages in Settings → Apps → Language        │
│  3. User OR app sets locale via API                             │
│  4. Configuration change triggers activity recreation           │
│  5. Resources loaded for the selected locale                    │
│                                                                  │
│  ┌─────────────────────────────────┐                            │
│  │      locale_config.xml          │                            │
│  │  ┌───────────────────────────┐  │                            │
│  │  │ <locale android:name="en"/>│  │                            │
│  │  │ <locale android:name="fr"/>│  │                            │
│  │  │ <locale android:name="ar"/>│  │                            │
│  │  │ <locale android:name="de"/>│  │                            │
│  │  │ <locale android:name="ja"/>│  │                            │
│  │  └───────────────────────────┘  │                            │
│  └─────────────────────────────────┘                            │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Code: Declaring Supported Locales

```xml
<!-- res/xml/locale_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
    <locale android:name="en" />       <!-- English -->
    <locale android:name="fr" />       <!-- French -->
    <locale android:name="de" />       <!-- German -->
    <locale android:name="ar" />       <!-- Arabic -->
    <locale android:name="ja" />       <!-- Japanese -->
    <locale android:name="es" />       <!-- Spanish -->
    <locale android:name="hi" />       <!-- Hindi -->
</locale-config>
```

```xml
<!-- AndroidManifest.xml -->
<application
    android:localeConfig="@xml/locale_config"
    ... >
</application>
```

### Code: Implementing Language Switcher (Kotlin)

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class LanguageSettingsActivity : AppCompatActivity() {

    private val supportedLanguages = listOf(
        "en" to "English",
        "fr" to "Français",
        "de" to "Deutsch",
        "ar" to "العربية",
        "ja" to "日本語",
        "es" to "Español"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        setupLanguageList()
        showCurrentLanguage()
    }

    private fun setupLanguageList() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_single_choice,
            supportedLanguages.map { it.second }
        )
        binding.languageListView.adapter = adapter
        binding.languageListView.choiceMode = ListView.CHOICE_MODE_SINGLE

        binding.languageListView.setOnItemClickListener { _, _, position, _ ->
            val languageTag = supportedLanguages[position].first
            setAppLanguage(languageTag)
        }
    }

    private fun setAppLanguage(languageTag: String) {
        // Using AppCompat — works on API 21+
        val appLocale = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(appLocale)
        // Activity will be automatically recreated
    }

    private fun showCurrentLanguage() {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (!currentLocales.isEmpty) {
            val currentLang = currentLocales[0]?.toLanguageTag()
            binding.currentLanguageText.text = "Current: $currentLang"
        }
    }

    // Using Platform API (API 33+) directly
    private fun setAppLanguagePlatform(languageTag: String) {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val localeManager = getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList.forLanguageTags(languageTag)
        }
    }
}
```

### Code: Per-App Language in Compose (Kotlin)

```kotlin
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat

data class Language(val code: String, val name: String, val nativeName: String)

@Composable
fun LanguagePickerScreen() {
    val languages = remember {
        listOf(
            Language("en", "English", "English"),
            Language("fr", "French", "Français"),
            Language("de", "German", "Deutsch"),
            Language("ar", "Arabic", "العربية"),
            Language("ja", "Japanese", "日本語"),
            Language("es", "Spanish", "Español")
        )
    }

    val currentLocale = AppCompatDelegate.getApplicationLocales()
    val currentLangCode = if (!currentLocale.isEmpty) {
        currentLocale[0]?.language ?: "en"
    } else "en"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Select Language",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(languages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = language.code == currentLangCode,
                    onSelect = {
                        val appLocale = LocaleListCompat.forLanguageTags(language.code)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    ListItem(
        headlineContent = { Text(language.nativeName) },
        supportingContent = { Text(language.name) },
        trailingContent = {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected"
                )
            }
        },
        modifier = Modifier.clickable { onSelect() }
    )
}
```

---

## Summary Table

| Feature | Min API | Key Class/API | Use Case |
|---------|---------|---------------|----------|
| Predictive Back | 34 | `OnBackAnimationCallback` | Smooth back navigation with preview |
| Grammatical Inflection | 34 | `GrammaticalInflectionManager` | Gender-aware UI text |
| Photo Picker | 33+ (enhanced 34) | `PickVisualMedia` | Privacy-safe media selection |
| Per-App Language | 33+ (Jetpack: 21+) | `AppCompatDelegate.setApplicationLocales()` | Multi-language apps |

---

## Best Practices

1. **Predictive Back**: Always test with `adb shell settings put global enable_back_preview 1`
2. **Grammatical Inflection**: Provide fallback strings for languages without gendered forms
3. **Photo Picker**: Prefer the system picker over requesting broad storage permissions
4. **Per-App Language**: Use `AppCompatDelegate` for backward compatibility instead of platform API
5. **Migration**: Use `android:enableOnBackInvokedCallback="true"` and migrate away from `onBackPressed()`
