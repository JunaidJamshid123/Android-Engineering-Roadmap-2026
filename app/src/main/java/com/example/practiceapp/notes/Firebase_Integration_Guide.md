# 12. Firebase Integration — Complete Guide

## Table of Contents
- [12.1 Firebase Authentication](#121-firebase-authentication)
- [12.2 Firestore](#122-firestore)
- [12.3 Realtime Database](#123-realtime-database)
- [12.4 Cloud Functions](#124-cloud-functions)
- [12.5 Firebase Cloud Messaging (FCM)](#125-firebase-cloud-messaging-fcm)
- [12.6 Firebase Analytics](#126-firebase-analytics)
- [12.7 Crashlytics](#127-crashlytics)
- [12.8 Remote Config](#128-remote-config)
- [12.9 Performance Monitoring](#129-performance-monitoring)
- [12.10 Cloud Storage](#1210-cloud-storage)

---

## Overview: What is Firebase?

Firebase is Google's Backend-as-a-Service (BaaS) platform that provides a suite of cloud-based services to accelerate Android, iOS, and web app development. It eliminates the need to build and manage your own backend servers for common features like authentication, databases, messaging, analytics, and more.

### History & Evolution
- **2011**: Firebase founded as a startup (originally "Envolve" — a chat API)
- **2012**: Pivoted to a real-time database platform
- **2014**: Acquired by Google
- **2016**: Major expansion — Analytics, Crashlytics, Cloud Functions, Storage added
- **2017**: Cloud Firestore released (next-gen database)
- **2019+**: Continued GA4 integration, Extensions marketplace, Emulator Suite
- **Today**: Full-stack serverless platform with 20+ products

### Why Firebase?
| Benefit | Description |
|---------|-------------|
| **Speed** | Go from idea to production in hours, not weeks |
| **No Server Management** | Google manages infrastructure, scaling, and uptime |
| **Real-time** | Built-in real-time sync across all connected clients |
| **Offline-First** | SDKs cache data locally — apps work without internet |
| **Unified Platform** | Auth, DB, storage, analytics, crash reports — one console |
| **Free Tier** | Generous Spark plan (free) for prototypes and small apps |
| **Cross-Platform** | Same backend for Android, iOS, Web, Flutter, Unity |

### Pricing Model (Spark vs Blaze)
```
┌──────────────────────────────────────────────────────────────┐
│                  FIREBASE PRICING PLANS                      │
│                                                              │
│  SPARK (Free)                 BLAZE (Pay-as-you-go)          │
│  ┌──────────────────────┐     ┌──────────────────────────┐   │
│  │ • Auth: Unlimited     │     │ • Everything in Spark    │   │
│  │   (most providers)    │     │ • Cloud Functions        │   │
│  │ • Firestore:          │     │ • Unlimited Firestore    │   │
│  │   1 GiB storage       │     │ • Multiple databases     │   │
│  │   50K reads/day       │     │ • Extensions             │   │
│  │   20K writes/day      │     │ • Cloud Run integration  │   │
│  │ • Storage: 5 GB       │     │ • BigQuery export        │   │
│  │ • Hosting: 10 GB      │     │ • Pay only for usage     │   │
│  │ • FCM: Unlimited      │     │   beyond free quota      │   │
│  │ • Analytics: Unlimited│     │ • Budget alerts          │   │
│  │ • Crashlytics: Free   │     │                          │   │
│  │ • NO Cloud Functions  │     │                          │   │
│  └──────────────────────┘     └──────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

### Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                       ANDROID APPLICATION                            │
│                                                                      │
│  ┌──────────┐   ┌───────────┐   ┌────────────┐   ┌──────────────┐   │
│  │   UI     │   │ ViewModel │   │ Repository │   │  Firebase    │   │
│  │  Layer   │──→│   Layer   │──→│   Layer    │──→│  SDK Layer   │   │
│  │(Compose/ │   │(StateFlow/│   │ (suspend   │   │(Auth, DB,   │   │
│  │  XML)    │   │ LiveData) │   │  functions)│   │ Storage...) │   │
│  └──────────┘   └───────────┘   └────────────┘   └──────┬───────┘   │
│                                                          │           │
│                           Local Cache (SQLite/LevelDB)   │           │
│                           ┌──────────────────────────┐   │           │
│                           │ Offline data persisted   │◄──┘           │
│                           │ automatically by SDK     │               │
│                           └──────────┬───────────────┘               │
└──────────────────────────────────────┼───────────────────────────────┘
                                       │ HTTPS / WebSocket
                                       │ (TLS encrypted)
                                       ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     FIREBASE CLOUD SERVICES                          │
│                                                                      │
│  ┌─────── BUILD ────────┐  ┌──── RELEASE & MONITOR ────┐            │
│  │                      │  │                           │            │
│  │ ┌──────────────────┐ │  │ ┌───────────────────────┐ │            │
│  │ │  Authentication  │ │  │ │    Crashlytics        │ │            │
│  │ │  (Identity)      │ │  │ │    (Crash reports)    │ │            │
│  │ └──────────────────┘ │  │ └───────────────────────┘ │            │
│  │ ┌──────────────────┐ │  │ ┌───────────────────────┐ │            │
│  │ │  Cloud Firestore │ │  │ │    Analytics / GA4    │ │            │
│  │ │  (NoSQL DB)      │ │  │ │    (Event tracking)   │ │            │
│  │ └──────────────────┘ │  │ └───────────────────────┘ │            │
│  │ ┌──────────────────┐ │  │ ┌───────────────────────┐ │            │
│  │ │ Realtime Database│ │  │ │  Performance Monitor  │ │            │
│  │ │  (JSON tree)     │ │  │ │  (Latency/traces)     │ │            │
│  │ └──────────────────┘ │  │ └───────────────────────┘ │            │
│  │ ┌──────────────────┐ │  │ ┌───────────────────────┐ │            │
│  │ │  Cloud Storage   │ │  │ │   Remote Config       │ │            │
│  │ │  (Files/Blobs)   │ │  │ │   (Feature flags)     │ │            │
│  │ └──────────────────┘ │  │ └───────────────────────┘ │            │
│  │ ┌──────────────────┐ │  └───────────────────────────┘            │
│  │ │ Cloud Functions  │ │                                           │
│  │ │ (Serverless)     │ │  ┌──── ENGAGE ───────────────┐            │
│  │ └──────────────────┘ │  │ ┌───────────────────────┐ │            │
│  │ ┌──────────────────┐ │  │ │    FCM (Push          │ │            │
│  │ │  Hosting         │ │  │ │    Notifications)     │ │            │
│  │ │  (Web apps)      │ │  │ └───────────────────────┘ │            │
│  │ └──────────────────┘ │  │ ┌───────────────────────┐ │            │
│  └──────────────────────┘  │ │  In-App Messaging     │ │            │
│                            │ │  Dynamic Links        │ │            │
│                            │ └───────────────────────┘ │            │
│                            └───────────────────────────┘            │
│                                                                      │
│  ┌────────── INFRASTRUCTURE ─────────────────────────────────────┐   │
│  │  Google Cloud Platform • Auto-scaling • Global CDN •          │   │
│  │  Multi-region replication • 99.95% SLA (Blaze) • IAM          │   │
│  └───────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

### Firebase Emulator Suite (Local Development)
```
┌──────────────────────────────────────────────────────────────┐
│              FIREBASE EMULATOR SUITE                         │
│                                                              │
│  Run Firebase services LOCALLY for development & testing     │
│                                                              │
│  $ firebase emulators:start                                  │
│                                                              │
│  ┌────────────────┐  ┌────────────────┐  ┌───────────────┐  │
│  │ Auth Emulator  │  │ Firestore      │  │ Realtime DB   │  │
│  │ :9099          │  │ Emulator :8080 │  │ Emulator :9000│  │
│  └────────────────┘  └────────────────┘  └───────────────┘  │
│  ┌────────────────┐  ┌────────────────┐  ┌───────────────┐  │
│  │ Functions      │  │ Storage        │  │ Hosting       │  │
│  │ Emulator :5001 │  │ Emulator :9199 │  │ Emulator :5000│  │
│  └────────────────┘  └────────────────┘  └───────────────┘  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │              Emulator UI → http://localhost:4000       │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  Benefits:                                                   │
│  • No cloud charges during development                       │
│  • Test security rules locally                               │
│  • Reproducible test data                                    │
│  • Works offline                                             │
│  • CI/CD integration                                         │
└──────────────────────────────────────────────────────────────┘
```

### Initial Project Setup

**Step 1: Add Firebase to your Android project**

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use an existing Google Cloud project)
3. Click "Add app" → select Android icon
4. Enter your package name: `com.example.practiceapp`
5. (Optional) Enter SHA-1 certificate fingerprint (required for Google Sign-In, Phone Auth)
   ```bash
   # Get SHA-1 from debug keystore:
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android
   ```
6. Download `google-services.json` and place it in the `app/` directory (NOT project root)
7. The `google-services.json` contains your project's Firebase configuration — **never commit it to public repos**

```
Project Structure:
PracticeApp2/
├── app/
│   ├── google-services.json    ← HERE (app-level)
│   ├── build.gradle.kts
│   └── src/
├── build.gradle.kts
└── settings.gradle.kts
```

**Step 2: Project-level `build.gradle.kts`**

```kotlin
// settings.gradle.kts (or project-level build.gradle.kts)
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
}
```

**Step 3: App-level `build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

dependencies {
    // Firebase BoM (Bill of Materials) - manages all Firebase library versions
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Individual Firebase libraries (no version needed when using BoM)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-functions-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
}
```

---

## 12.1 Firebase Authentication

### Theory

Firebase Authentication provides backend services, easy-to-use SDKs, and ready-made UI libraries to authenticate users. It supports authentication using passwords, phone numbers, federated identity providers like Google, Facebook, Twitter, and more.

### Authentication Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                   AUTHENTICATION FLOW                        │
│                                                              │
│  ┌────────┐    ┌──────────┐    ┌──────────┐    ┌─────────┐  │
│  │  User  │───→│  Login   │───→│ Firebase │───→│ Success │  │
│  │  Input │    │  Screen  │    │   Auth   │    │ /Failure│  │
│  └────────┘    └──────────┘    └────┬─────┘    └─────────┘  │
│                                     │                        │
│                    ┌────────────────┼────────────────┐        │
│                    │               │                │        │
│               ┌────▼────┐    ┌────▼────┐     ┌────▼────┐   │
│               │ Email/  │    │ Social  │     │  Phone  │   │
│               │Password │    │ Login   │     │  Auth   │   │
│               └─────────┘    └─────────┘     └─────────┘   │
│                                    │                        │
│                    ┌───────────────┼───────────────┐        │
│                    │               │               │        │
│               ┌────▼────┐    ┌────▼────┐    ┌────▼─────┐   │
│               │ Google  │    │Facebook │    │ Twitter  │   │
│               └─────────┘    └─────────┘    └──────────┘   │
└──────────────────────────────────────────────────────────────┘

Auth State Lifecycle:
┌────────────┐     ┌──────────┐     ┌────────────┐
│ Signed Out │────→│ Signing  │────→│ Signed In  │
│            │     │   In     │     │            │
└────────────┘     └──────────┘     └──────┬─────┘
       ▲                                    │
       │          ┌──────────┐              │
       └──────────│ Signing  │◄─────────────┘
                  │   Out    │
                  └──────────┘
```

### 12.1.1 Email/Password Authentication

**How it works internally:**
1. SDK sends email + password hash to Firebase servers over HTTPS
2. Firebase verifies credentials against its identity store
3. On success, returns an ID token (JWT) + refresh token
4. SDK caches the refresh token in secure device storage
5. Subsequent launches re-authenticate silently using the refresh token

**Security considerations:**
- Firebase enforces minimum 6-character passwords by default
- Enable email enumeration protection in Firebase Console → Authentication → Settings
- Always send verification email after sign-up before granting full access
- Re-authenticate the user before sensitive operations (password change, account deletion)

```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.tasks.await

class EmailAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // ── SIGN UP (Register new user) ──
    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                // Send email verification
                it.sendEmailVerification().await()
                Result.success(it)
            } ?: Result.failure(Exception("User creation failed"))
        } catch (e: FirebaseAuthException) {
            // Handle specific auth errors for better UX
            val message = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
                "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters"
                "ERROR_INVALID_EMAIL" -> "Please enter a valid email address"
                else -> e.message ?: "Sign-up failed"
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── SIGN IN ──
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                // Check if email is verified before allowing access
                if (!it.isEmailVerified) {
                    return Result.failure(Exception("Please verify your email first"))
                }
                Result.success(it)
            } ?: Result.failure(Exception("Sign-in failed"))
        } catch (e: FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                "ERROR_USER_DISABLED" -> "This account has been disabled"
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again later"
                else -> e.message ?: "Sign-in failed"
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── PASSWORD RESET ──
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── RE-AUTHENTICATE (required before sensitive operations) ──
    suspend fun reAuthenticate(email: String, password: String): Result<Unit> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser?.reauthenticate(credential)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── UPDATE PASSWORD (call reAuthenticate first!) ──
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            auth.currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── UPDATE PROFILE ──
    suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<Unit> {
        return try {
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .apply {
                    displayName?.let { setDisplayName(it) }
                    photoUrl?.let { setPhotoUri(android.net.Uri.parse(it)) }
                }
                .build()
            auth.currentUser?.updateProfile(profileUpdates)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── DELETE ACCOUNT (call reAuthenticate first!) ──
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── SIGN OUT ──
    fun signOut() {
        auth.signOut()
    }

    // ── CURRENT USER ──
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // ── GET ID TOKEN (for authenticating with your own backend) ──
    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        return try {
            auth.currentUser?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    // ── AUTH STATE LISTENER ──
    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }
}
```

### 12.1.2 Social Logins: Google Sign-In

**Two approaches exist — Legacy (GoogleSignInClient) and Modern (Credential Manager):**

```
┌──────────────────────────────────────────────────────────────────┐
│                  GOOGLE SIGN-IN APPROACHES                       │
│                                                                  │
│  LEGACY (GoogleSignInClient)    MODERN (Credential Manager)      │
│  ┌──────────────────────────┐   ┌──────────────────────────────┐ │
│  │ • Uses Play Services     │   │ • Jetpack library (API 21+)  │ │
│  │ • Activity result flow   │   │ • One Tap + passkey support  │ │
│  │ • Deprecated in 2023     │   │ • Single unified API         │ │
│  │ • Still works but no     │   │ • Recommended by Google      │ │
│  │   new features           │   │ • Better UX (bottom sheet)   │ │
│  └──────────────────────────┘   └──────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

**Legacy Approach (still widely used):**

```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.tasks.await

class GoogleAuthManager(private val activity: Activity) {
    private val auth = FirebaseAuth.getInstance()

    // Configure Google Sign In
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(activity.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(activity, gso)

    // Step 1: Get sign-in intent
    fun getSignInIntent() = googleSignInClient.signInIntent

    // Step 2: Handle the result (called from onActivityResult or ActivityResultLauncher)
    suspend fun firebaseAuthWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Google sign-in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ── Using with ActivityResultLauncher (Modern Approach) ──
class LoginActivity : AppCompatActivity() {

    private lateinit var googleAuthManager: GoogleAuthManager

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    lifecycleScope.launch {
                        val authResult = googleAuthManager.firebaseAuthWithGoogle(token)
                        authResult.onSuccess { user ->
                            // Navigate to home screen
                            navigateToHome()
                        }.onFailure { error ->
                            showError(error.message)
                        }
                    }
                }
            } catch (e: ApiException) {
                showError("Google sign-in failed: ${e.statusCode}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleAuthManager = GoogleAuthManager(this)

        binding.googleSignInButton.setOnClickListener {
            googleSignInLauncher.launch(googleAuthManager.getSignInIntent())
        }
    }
}
```

**Modern Approach: Credential Manager API (Recommended for new projects):**

```kotlin
// Additional dependency:
// implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
// implementation("androidx.credentials:credentials:1.3.0")
// implementation("androidx.credentials:credentials-play-services-auth:1.3.0")

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth

class CredentialManagerAuthHelper(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val auth = FirebaseAuth.getInstance()

    suspend fun signInWithGoogle(activityContext: Activity): Result<FirebaseUser> {
        return try {
            // Build Google ID option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false) // Show all accounts
                .setAutoSelectEnabled(true)           // Auto-select if only one account
                .build()

            // Build credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Launch Credential Manager (shows bottom sheet UI)
            val result = credentialManager.getCredential(activityContext, request)
            val googleIdToken = GoogleIdTokenCredential
                .createFrom(result.credential.data)
                .idToken

            // Firebase auth with the token
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()

            authResult.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Google sign-in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 12.1.3 Facebook Login

```kotlin
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FacebookAuthManager {
    private val auth = FirebaseAuth.getInstance()
    val callbackManager = CallbackManager.Factory.create()

    fun setupFacebookLogin(
        activity: Activity,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    // Got Facebook access token, now sign in with Firebase
                    val credential = FacebookAuthProvider.getCredential(
                        result.accessToken.token
                    )
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { authResult ->
                            authResult.user?.let { onSuccess(it) }
                        }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "Facebook auth failed")
                        }
                }

                override fun onCancel() {
                    onError("Facebook login cancelled")
                }

                override fun onError(error: FacebookException) {
                    onError(error.message ?: "Facebook login error")
                }
            }
        )
    }

    fun login(activity: Activity) {
        LoginManager.getInstance().logInWithReadPermissions(
            activity,
            listOf("email", "public_profile")
        )
    }
}
```

### 12.1.4 Twitter Login

```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await

class TwitterAuthManager(private val activity: Activity) {
    private val auth = FirebaseAuth.getInstance()

    fun signInWithTwitter(
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val provider = OAuthProvider.newBuilder("twitter.com")

        // Optional: Add custom parameters
        provider.addCustomParameter("lang", "en")

        auth.startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener { authResult ->
                authResult.user?.let { onSuccess(it) }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Twitter sign-in failed")
            }
    }
}
```

### 12.1.5 Phone Authentication

```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.tasks.await

class PhoneAuthManager(private val activity: Activity) {
    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // ── STEP 1: Send verification code ──
    fun sendVerificationCode(
        phoneNumber: String,
        onCodeSent: () -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onError: (String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // Auto-verification (instant verification on some devices)
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onVerificationCompleted(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                vId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = vId
                resendToken = token
                onCodeSent()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)          // e.g., "+1234567890"
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ── STEP 2: Verify code entered by user ──
    suspend fun verifyCode(code: String): Result<FirebaseUser> {
        return try {
            val vId = verificationId ?: return Result.failure(
                Exception("Verification ID not found")
            )
            val credential = PhoneAuthProvider.getCredential(vId, code)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Phone auth failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── RESEND CODE ──
    fun resendCode(
        phoneNumber: String,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        val token = resendToken ?: run {
            onError("No resend token available")
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.message ?: "Resend failed")
            }
            override fun onCodeSent(vId: String, newToken: PhoneAuthProvider.ForceResendingToken) {
                verificationId = vId
                resendToken = newToken
                onCodeSent()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
```

### 12.1.6 Anonymous Authentication

```kotlin
class AnonymousAuthManager {
    private val auth = FirebaseAuth.getInstance()

    // ── Sign in anonymously ──
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Anonymous sign-in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Convert anonymous account to permanent account ──
    suspend fun linkWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val result = auth.currentUser?.linkWithCredential(credential)?.await()
            result?.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Account linking failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Check if current user is anonymous ──
    fun isAnonymous(): Boolean = auth.currentUser?.isAnonymous == true
}
```

### 12.1.7 Custom Authentication

```kotlin
class CustomAuthManager {
    private val auth = FirebaseAuth.getInstance()

    /**
     * Custom auth uses a token generated by YOUR server.
     * Your server uses Firebase Admin SDK to create a custom token.
     *
     * Flow:
     * 1. Client authenticates with your server (any method)
     * 2. Server verifies user and creates a Firebase custom token
     * 3. Client uses the custom token to sign in with Firebase
     */
    suspend fun signInWithCustomToken(customToken: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCustomToken(customToken).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Custom auth failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ── Server-side (Node.js example for reference) ──
// const admin = require('firebase-admin');
// admin.initializeApp();
//
// async function createCustomToken(uid) {
//     const customToken = await admin.auth().createCustomToken(uid, {
//         premiumUser: true,
//         role: 'admin'
//     });
//     return customToken;
// }
```

### Complete Auth ViewModel (Production-Ready with StateFlow)

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val emailAuthManager = EmailAuthManager()

    // ── Auth state as a Flow (reactive, lifecycle-aware) ──
    val authState: StateFlow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(
                if (user != null) AuthState.Authenticated(user)
                else AuthState.Unauthenticated
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Loading
    )

    // ── UI events (one-shot messages like errors, navigation) ──
    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    sealed class AuthState {
        object Loading : AuthState()
        data class Authenticated(val user: FirebaseUser) : AuthState()
        object Unauthenticated : AuthState()
    }

    sealed class AuthEvent {
        data class ShowError(val message: String) : AuthEvent()
        object NavigateToHome : AuthEvent()
        object NavigateToLogin : AuthEvent()
        data class ShowMessage(val message: String) : AuthEvent()
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val result = emailAuthManager.signInWithEmail(email, password)
            result.onFailure { e ->
                _events.emit(AuthEvent.ShowError(e.message ?: "Sign-in failed"))
            }
            // Success is automatically handled by authState Flow
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            val result = emailAuthManager.signUpWithEmail(email, password)
            result.onSuccess {
                _events.emit(AuthEvent.ShowMessage("Verification email sent. Please check your inbox."))
            }.onFailure { e ->
                _events.emit(AuthEvent.ShowError(e.message ?: "Sign-up failed"))
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            val result = emailAuthManager.sendPasswordResetEmail(email)
            result.onSuccess {
                _events.emit(AuthEvent.ShowMessage("Password reset email sent"))
            }.onFailure { e ->
                _events.emit(AuthEvent.ShowError(e.message ?: "Failed to send reset email"))
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // ── Convenience properties ──
    val isLoggedIn: Boolean get() = auth.currentUser != null
    val currentUser: FirebaseUser? get() = auth.currentUser
}

// ── Usage in Activity/Fragment ──
// lifecycleScope.launch {
//     viewModel.authState.collect { state ->
//         when (state) {
//             is AuthState.Loading -> showLoading()
//             is AuthState.Authenticated -> navigateToHome()
//             is AuthState.Unauthenticated -> showLoginScreen()
//         }
//     }
// }
// lifecycleScope.launch {
//     viewModel.events.collect { event ->
//         when (event) {
//             is AuthEvent.ShowError -> showSnackbar(event.message)
//             is AuthEvent.ShowMessage -> showToast(event.message)
//             ...
//         }
//     }
// }
```

---

## 12.2 Firestore

### Theory

Cloud Firestore is a flexible, scalable NoSQL cloud database from Firebase and Google Cloud. It keeps data in sync across client apps through real-time listeners and offers offline support for mobile and web, so you can build responsive apps that work regardless of network latency or internet connectivity.

**Key Concepts:**
- **Document**: A lightweight record containing fields (key-value pairs). Like a JSON object. Max 1 MB.
- **Collection**: A group of documents. Collections cannot contain other collections directly — only documents.
- **Subcollection**: A collection nested inside a document, enabling hierarchical data.
- **Reference**: A pointer to a document or collection location (doesn't contain the data itself).
- **Snapshot**: An immutable copy of data at a point in time, returned from reads.
- **ListenerRegistration**: Handle returned by `addSnapshotListener()` — must be removed to prevent leaks.

**Data Types Supported:**
| Type | Example | Notes |
|------|---------|-------|
| String | `"hello"` | UTF-8, max ~1 MB |
| Number | `42`, `3.14` | 64-bit integer or floating-point |
| Boolean | `true` | |
| Map | `{"name": "Junaid"}` | Nested objects, max 20 levels deep |
| Array | `[1, 2, 3]` | Cannot contain arrays, no native query on index |
| Null | `null` | |
| Timestamp | `Timestamp(seconds, nanos)` | Server-generated or client-side |
| GeoPoint | `GeoPoint(33.6, 73.0)` | Latitude/longitude pair |
| Reference | `/users/user_001` | Pointer to another document |
| Bytes | `Blob(...)` | Max 1 MB, for binary data |

**Read/Write Cost Model (Blaze plan):**
```
┌──────────────────────────────────────────────────────────────┐
│               FIRESTORE PRICING (Blaze)                      │
│                                                              │
│  Operation           │ Free/Day │ Cost Beyond Free Tier      │
│  ────────────────────┼──────────┼──────────────────────────  │
│  Document Read       │ 50,000   │ $0.06 per 100K reads       │
│  Document Write      │ 20,000   │ $0.18 per 100K writes      │
│  Document Delete     │ 20,000   │ $0.02 per 100K deletes     │
│  Storage             │ 1 GiB    │ $0.18 per GiB/month        │
│  Network Egress      │ 10 GiB   │ Varies by region           │
│                                                              │
│  Cost-saving tips:                                           │
│  • Use .select() to return only needed fields                │
│  • Cache results and use offline persistence                 │
│  • Avoid reading entire collections — use queries            │
│  • listener on 100-doc collection = 100 reads on attach      │
│  • Subsequent changes count as 1 read per changed doc        │
│  • Existence checks (get) still cost 1 read even if doc      │
│    doesn't exist                                             │
└──────────────────────────────────────────────────────────────┘
```

### Data Model Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                 FIRESTORE DATA MODEL                        │
│                                                             │
│  Firestore                                                  │
│  └── Collection: "users"                                    │
│      ├── Document: "user_001"                               │
│      │   ├── name: "Junaid"                                 │
│      │   ├── email: "junaid@example.com"                    │
│      │   ├── age: 25                                        │
│      │   └── Subcollection: "orders"                        │
│      │       ├── Document: "order_001"                      │
│      │       │   ├── item: "Laptop"                         │
│      │       │   ├── price: 999.99                          │
│      │       │   └── date: Timestamp                        │
│      │       └── Document: "order_002"                      │
│      │           ├── item: "Phone"                          │
│      │           └── price: 699.99                          │
│      └── Document: "user_002"                               │
│          ├── name: "Ahmad"                                   │
│          └── email: "ahmad@example.com"                      │
│                                                             │
│  └── Collection: "products"                                 │
│      ├── Document: "prod_001"                               │
│      │   ├── name: "Widget"                                 │
│      │   ├── price: 29.99                                   │
│      │   └── tags: ["electronics", "gadget"]                │
│      └── ...                                                │
│                                                             │
│  KEY CONCEPTS:                                              │
│  • Collections contain Documents                            │
│  • Documents contain Fields + Subcollections                │
│  • Documents cannot exceed 1MB                              │
│  • Max depth: alternating Collection/Document               │
│  • Document IDs are unique within a collection              │
└─────────────────────────────────────────────────────────────┘

Firestore vs SQL Comparison:
┌──────────────────┬────────────────────┐
│    SQL            │    Firestore       │
├──────────────────┼────────────────────┤
│ Database          │ Project            │
│ Table             │ Collection         │
│ Row               │ Document           │
│ Column            │ Field              │
│ Primary Key       │ Document ID        │
│ Foreign Key       │ Reference field    │
│ JOIN              │ Subcollection/     │
│                   │ Denormalization    │
└──────────────────┴────────────────────┘
```

### 12.2.1 CRUD Operations

```kotlin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

// ── Data Model ──
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val createdAt: com.google.firebase.Timestamp? = null
)

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // ── CREATE: Add a document with auto-generated ID ──
    suspend fun addUser(user: User): Result<String> {
        return try {
            val documentRef = usersCollection.add(user).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── CREATE: Set a document with specific ID ──
    suspend fun setUser(userId: String, user: User): Result<Unit> {
        return try {
            usersCollection.document(userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── READ: Get a single document ──
    suspend fun getUser(userId: String): Result<User> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val user = snapshot.toObject<User>()
            user?.let {
                Result.success(it.copy(id = snapshot.id))
            } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── READ: Get all documents from a collection ──
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject<User>()?.copy(id = doc.id)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── UPDATE: Update specific fields ──
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── DELETE: Remove a document ──
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 12.2.2 Real-time Listeners

```kotlin
class FirestoreRealtimeManager {
    private val db = FirebaseFirestore.getInstance()

    // ── Listen to a single document ──
    fun listenToUser(
        userId: String,
        onUpdate: (User?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject<User>()?.copy(id = snapshot.id)
                onUpdate(user)
            }
    }

    // ── Listen to a collection ──
    fun listenToAllUsers(
        onUpdate: (List<User>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<User>()?.copy(id = doc.id)
                } ?: emptyList()
                onUpdate(users)
            }
    }

    // ── Listen to a query ──
    fun listenToAdultUsers(
        onUpdate: (List<User>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("users")
            .whereGreaterThanOrEqualTo("age", 18)
            .orderBy("age")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documentChanges?.map { change ->
                    when (change.type) {
                        DocumentChange.Type.ADDED -> { /* New document */ }
                        DocumentChange.Type.MODIFIED -> { /* Updated document */ }
                        DocumentChange.Type.REMOVED -> { /* Deleted document */ }
                    }
                    change.document.toObject<User>().copy(id = change.document.id)
                } ?: emptyList()
                onUpdate(users)
            }
    }
}

// ── Usage in ViewModel with proper cleanup (modern Flow approach) ──
class UsersViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // Convert Firestore snapshots to Kotlin Flow (reactive stream)
    val users: StateFlow<List<User>> = callbackFlow {
        val registration = db.collection("users")
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close flow on error
                    return@addSnapshotListener
                }
                val userList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<User>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(userList)
            }
        awaitClose { registration.remove() } // Auto-cleanup when ViewModel cleared
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Track document changes granularly
    val userChanges: Flow<List<Pair<DocumentChange.Type, User>>> = callbackFlow {
        val registration = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val changes = snapshot?.documentChanges?.map { change ->
                    change.type to change.document.toObject<User>().copy(id = change.document.id)
                } ?: emptyList()
                trySend(changes)
            }
        awaitClose { registration.remove() }
    }
}
```

### 12.2.3 Offline Persistence

```kotlin
// Firestore offline persistence is ENABLED by default on Android.
// You can configure it:

class FirestoreConfig {
    fun configureFirestore() {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)        // default: true
            .setCacheSizeBytes(               // default: 100MB
                FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
            )
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings
    }

    // ── Control cache behavior per query ──
    suspend fun getUsersFromCache(): List<User> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("users")
            .get(Source.CACHE)    // Force read from cache
            .await()
        return snapshot.toObjects(User::class.java)
    }

    suspend fun getUsersFromServer(): List<User> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("users")
            .get(Source.SERVER)   // Force read from server
            .await()
        return snapshot.toObjects(User::class.java)
    }

    // Source.DEFAULT → reads from server, falls back to cache
    // Source.CACHE   → reads only from cache
    // Source.SERVER  → reads only from server
}
```

### 12.2.4 Security Rules

```javascript
// firestore.rules
rules_version = '2';
service cloud.firestore {
    match /databases/{database}/documents {

        // ── Allow read/write only to authenticated users ──
        match /users/{userId} {
            allow read: if request.auth != null;
            allow write: if request.auth != null && request.auth.uid == userId;

            // ── Subcollection: orders ──
            match /orders/{orderId} {
                allow read, write: if request.auth != null
                    && request.auth.uid == userId;
            }
        }

        // ── Public read, admin write ──
        match /products/{productId} {
            allow read: if true;
            allow write: if request.auth != null
                && request.auth.token.admin == true;
        }

        // ── Data validation ──
        match /posts/{postId} {
            allow create: if request.auth != null
                && request.resource.data.title is string
                && request.resource.data.title.size() > 0
                && request.resource.data.title.size() <= 100
                && request.resource.data.content is string
                && request.resource.data.authorId == request.auth.uid;

            allow update: if request.auth != null
                && resource.data.authorId == request.auth.uid;

            allow delete: if request.auth != null
                && resource.data.authorId == request.auth.uid;
        }

        // ── Rate limiting with timestamps ──
        match /messages/{messageId} {
            allow create: if request.auth != null
                && request.time > resource.data.lastPost + duration.value(1, 's');
        }
    }
}
```

### 12.2.5 Queries and Indexes

```kotlin
class FirestoreQueryExamples {
    private val db = FirebaseFirestore.getInstance()

    // ── Simple queries ──
    suspend fun queryByField() {
        // Equality
        val adults = db.collection("users")
            .whereEqualTo("age", 25)
            .get().await()

        // Comparison
        val olderUsers = db.collection("users")
            .whereGreaterThan("age", 30)
            .get().await()

        // Range
        val ageRange = db.collection("users")
            .whereGreaterThanOrEqualTo("age", 18)
            .whereLessThan("age", 65)
            .get().await()

        // In operator (up to 30 values)
        val specificCities = db.collection("users")
            .whereIn("city", listOf("Lahore", "Karachi", "Islamabad"))
            .get().await()

        // Array contains
        val electronicsProducts = db.collection("products")
            .whereArrayContains("tags", "electronics")
            .get().await()

        // Array contains any (up to 30 values)
        val taggedProducts = db.collection("products")
            .whereArrayContainsAny("tags", listOf("electronics", "gadget"))
            .get().await()
    }

    // ── Compound queries (require composite indexes) ──
    suspend fun compoundQueries() {
        // This requires a composite index on (city, age)
        val result = db.collection("users")
            .whereEqualTo("city", "Lahore")
            .whereGreaterThan("age", 25)
            .orderBy("age")
            .get().await()
    }

    // ── Ordering and Limiting ──
    suspend fun orderAndLimit() {
        val topUsers = db.collection("users")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get().await()
    }

    // ── Pagination with cursors ──
    suspend fun paginateResults() {
        // First page
        val firstPage = db.collection("users")
            .orderBy("name")
            .limit(25)
            .get().await()

        // Get the last document
        val lastDocument = firstPage.documents.lastOrNull()

        // Next page
        if (lastDocument != null) {
            val nextPage = db.collection("users")
                .orderBy("name")
                .startAfter(lastDocument)
                .limit(25)
                .get().await()
        }
    }

    // ── Collection Group Query ──
    // Query across all subcollections with the same name
    suspend fun collectionGroupQuery() {
        // Find all orders across ALL users
        val allOrders = db.collectionGroup("orders")
            .whereGreaterThan("price", 100)
            .get().await()
    }
}
```

**Index Types:**
```
┌─────────────────────────────────────────────────────┐
│                FIRESTORE INDEXES                    │
│                                                     │
│ 1. Single-field indexes (automatic)                 │
│    • Created automatically for each field           │
│    • Supports simple queries                        │
│                                                     │
│ 2. Composite indexes (manual)                       │
│    • Required for compound queries                  │
│    • Must be created in Firebase Console or          │
│      firestore.indexes.json                         │
│                                                     │
│ Example firestore.indexes.json:                     │
│ {                                                   │
│   "indexes": [                                      │
│     {                                               │
│       "collectionGroup": "users",                   │
│       "queryScope": "COLLECTION",                   │
│       "fields": [                                   │
│         { "fieldPath": "city", "order": "ASC" },    │
│         { "fieldPath": "age", "order": "ASC" }      │
│       ]                                             │
│     }                                               │
│   ]                                                 │
│ }                                                   │
└─────────────────────────────────────────────────────┘
```

### 12.2.6 Batch Writes and Transactions

```kotlin
class FirestoreBatchAndTransactions {
    private val db = FirebaseFirestore.getInstance()

    // ── Batch Write: Multiple writes as a single atomic operation ──
    // Use when you don't need to read data before writing
    suspend fun batchWrite() {
        val batch = db.batch()

        // Create
        val newUserRef = db.collection("users").document()
        batch.set(newUserRef, User(name = "Junaid", email = "junaid@test.com"))

        // Update
        val existingUserRef = db.collection("users").document("user_001")
        batch.update(existingUserRef, "age", 26)

        // Delete
        val deleteRef = db.collection("users").document("user_old")
        batch.delete(deleteRef)

        // Commit (all succeed or all fail)
        try {
            batch.commit().await()
            // All writes succeeded
        } catch (e: Exception) {
            // All writes failed (atomically)
        }
    }

    // ── Transaction: Read + Write atomically ──
    // Use when writes depend on current data (e.g., incrementing a counter)
    suspend fun transferCredits(fromUserId: String, toUserId: String, amount: Int) {
        try {
            db.runTransaction { transaction ->
                // Read both documents
                val fromRef = db.collection("users").document(fromUserId)
                val toRef = db.collection("users").document(toUserId)

                val fromSnapshot = transaction.get(fromRef)
                val toSnapshot = transaction.get(toRef)

                val fromCredits = fromSnapshot.getLong("credits") ?: 0
                val toCredits = toSnapshot.getLong("credits") ?: 0

                // Validate
                if (fromCredits < amount) {
                    throw FirebaseFirestoreException(
                        "Insufficient credits",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }

                // Write (after all reads)
                transaction.update(fromRef, "credits", fromCredits - amount)
                transaction.update(toRef, "credits", toCredits + amount)

                // Return value from transaction (optional)
                null
            }.await()
        } catch (e: Exception) {
            // Transaction failed after max retries
        }
    }

    // ── Atomic increment (no transaction needed) ──
    suspend fun incrementLikes(postId: String) {
        db.collection("posts").document(postId)
            .update("likes", FieldValue.increment(1))
            .await()
    }

    // ── Atomic array operations ──
    suspend fun arrayOperations(userId: String) {
        val userRef = db.collection("users").document(userId)

        // Add to array (only if not already present)
        userRef.update("favoriteColors", FieldValue.arrayUnion("blue")).await()

        // Remove from array
        userRef.update("favoriteColors", FieldValue.arrayRemove("red")).await()
    }

    // ── Server timestamp ──
    suspend fun setServerTimestamp(userId: String) {
        db.collection("users").document(userId)
            .update("lastUpdated", FieldValue.serverTimestamp())
            .await()
    }
}
```

---

## 12.3 Realtime Database

### Theory

Firebase Realtime Database is a cloud-hosted JSON database. Data is stored as JSON and synchronized in real-time to every connected client. Unlike Firestore which uses documents and collections, Realtime Database stores everything as one large JSON tree.

**Key Differences from Firestore:**
- Data model is a **single JSON tree** (not documents/collections)
- Synchronization is **event-driven** at the node level via WebSocket
- Queries are **limited** — you can only filter/sort on one child key at a time
- **Cheaper for high-frequency writes** (charged by bandwidth, not operation count)
- **Best for**: chat, presence, multiplayer game state, IoT sensor data
- **Avoid for**: complex queries, deeply structured data, apps needing multi-region

**Data Structure Best Practices:**
```
✗ BAD (deeply nested — downloading /users also downloads ALL messages):
{
  "users": {
    "user_001": {
      "name": "Junaid",
      "messages": { ... thousands of messages ... }
    }
  }
}

✓ GOOD (denormalized/flat — each top-level node is independently queryable):
{
  "users":    { "user_001": { "name": "Junaid" } },
  "messages": { "room_001": { "msg_001": { ... } } },
  "userRooms": { "user_001": { "room_001": true } }
}
```

**Scaling Limits:**
- Max depth: 32 levels
- Max key size: 768 bytes
- Max data per write: 16 MB
- Max nodes per child: unlimited (but performance degrades)
- Max concurrent connections: 200,000 (per database)
- A single database instance is limited to one region

### Structure Diagram

```
┌──────────────────────────────────────────────────────────────┐
│              REALTIME DATABASE JSON TREE                      │
│                                                              │
│  {                                                           │
│    "users": {                                                │
│      "user_001": {                                           │
│        "name": "Junaid",                                     │
│        "email": "junaid@example.com",                        │
│        "online": true                                        │
│      },                                                      │
│      "user_002": {                                           │
│        "name": "Ahmad",                                      │
│        "email": "ahmad@example.com",                         │
│        "online": false                                       │
│      }                                                       │
│    },                                                        │
│    "messages": {                                             │
│      "room_001": {                                           │
│        "msg_001": {                                          │
│          "text": "Hello!",                                   │
│          "senderId": "user_001",                             │
│          "timestamp": 1703001234567                           │
│        }                                                     │
│      }                                                       │
│    },                                                        │
│    "presence": {                                             │
│      "user_001": { "status": "online", "lastSeen": ... }    │
│    }                                                         │
│  }                                                           │
│                                                              │
│  Firestore vs Realtime Database:                             │
│  ┌────────────────────┬──────────────────────────┐           │
│  │ Realtime Database   │ Firestore                │           │
│  ├────────────────────┼──────────────────────────┤           │
│  │ JSON tree           │ Documents/Collections    │           │
│  │ Single region       │ Multi-region             │           │
│  │ Charged by          │ Charged by reads/        │           │
│  │ bandwidth + storage │ writes + storage         │           │
│  │ Simple queries      │ Complex queries          │           │
│  │ No offline on web   │ Offline everywhere       │           │
│  │ 200K concurrent     │ 1M concurrent            │           │
│  │ Better for presence │ Better for complex data  │           │
│  └────────────────────┴──────────────────────────┘           │
└──────────────────────────────────────────────────────────────┘
```

### 12.3.1 CRUD Operations

```kotlin
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ChildEventListener
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0
)

class RealtimeDatabaseRepository {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val messagesRef = database.getReference("messages")

    // ── CREATE: Write data ──
    suspend fun createUser(userId: String, user: User) {
        usersRef.child(userId).setValue(user).await()
    }

    // ── CREATE: Push (auto-generate key) ──
    suspend fun sendMessage(roomId: String, message: ChatMessage): String {
        val newRef = messagesRef.child(roomId).push()
        newRef.setValue(message).await()
        return newRef.key ?: ""
    }

    // ── READ: Single value ──
    suspend fun getUser(userId: String): User? {
        val snapshot = usersRef.child(userId).get().await()
        return snapshot.getValue(User::class.java)
    }

    // ── UPDATE: Update specific fields ──
    suspend fun updateUserFields(userId: String, updates: Map<String, Any>) {
        usersRef.child(userId).updateChildren(updates).await()
    }

    // ── UPDATE: Multi-path update (atomic across multiple locations) ──
    suspend fun multiPathUpdate() {
        val updates = hashMapOf<String, Any>(
            "/users/user_001/name" to "Updated Name",
            "/users/user_001/age" to 30,
            "/metadata/lastModified" to ServerValue.TIMESTAMP
        )
        database.reference.updateChildren(updates).await()
    }

    // ── DELETE ──
    suspend fun deleteUser(userId: String) {
        usersRef.child(userId).removeValue().await()
    }
}
```

### 12.3.2 Real-time Synchronization

```kotlin
class RealtimeListeners {
    private val database = FirebaseDatabase.getInstance()

    // ── ValueEventListener: Fires for initial data AND every change ──
    fun listenToUser(userId: String, onUpdate: (User?) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                onUpdate(user)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        database.getReference("users/$userId").addValueEventListener(listener)
        return listener
    }

    // ── ChildEventListener: Fires for individual child changes ──
    fun listenToMessages(roomId: String): ChildEventListener {
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatMessage::class.java)
                // New message added
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Existing message was modified
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Message was deleted
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Child's sort position changed
            }

            override fun onCancelled(error: DatabaseError) {
                // Error occurred
            }
        }
        database.getReference("messages/$roomId").addChildEventListener(listener)
        return listener
    }

    // ── Listen once (single read, no continuous updates) ──
    fun listenOnce(path: String, onResult: (DataSnapshot) -> Unit) {
        database.getReference(path)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ── Remove listeners ──
    fun removeListener(path: String, listener: ValueEventListener) {
        database.getReference(path).removeEventListener(listener)
    }

    // ── Presence system (online/offline status) ──
    fun setupPresence(userId: String) {
        val connectedRef = database.getReference(".info/connected")
        val userStatusRef = database.getReference("presence/$userId")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    // When disconnected, set status to offline
                    userStatusRef.onDisconnect().setValue(
                        mapOf(
                            "status" to "offline",
                            "lastSeen" to ServerValue.TIMESTAMP
                        )
                    )
                    // Set status to online
                    userStatusRef.setValue(
                        mapOf(
                            "status" to "online",
                            "lastSeen" to ServerValue.TIMESTAMP
                        )
                    )
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
```

### 12.3.3 Offline Capabilities

```kotlin
class RealtimeDbOffline {
    private val database = FirebaseDatabase.getInstance()

    fun enableOffline() {
        // Enable disk persistence (call before any other Database usage)
        database.setPersistenceEnabled(true)

        // Keep specific data synced (even when no listeners are active)
        database.getReference("users").keepSynced(true)
        database.getReference("messages").keepSynced(true)
    }

    // Manual connection management
    fun goOffline() {
        database.goOffline()
    }

    fun goOnline() {
        database.goOnline()
    }
}
```

### 12.3.4 Security Rules

```json
// database.rules.json
{
    "rules": {
        "users": {
            "$userId": {
                // Only the user themselves can read/write their own data
                ".read": "auth != null && auth.uid === $userId",
                ".write": "auth != null && auth.uid === $userId",

                "name": {
                    ".validate": "newData.isString() && newData.val().length <= 100"
                },
                "email": {
                    ".validate": "newData.isString() && newData.val().matches(/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$/)"
                },
                "age": {
                    ".validate": "newData.isNumber() && newData.val() >= 0 && newData.val() <= 150"
                }
            }
        },
        "messages": {
            "$roomId": {
                ".read": "auth != null",
                "$messageId": {
                    ".write": "auth != null",
                    ".validate": "newData.hasChildren(['text', 'senderId', 'timestamp'])",
                    "senderId": {
                        ".validate": "newData.val() === auth.uid"
                    }
                }
            }
        },
        "presence": {
            "$userId": {
                ".read": "auth != null",
                ".write": "auth != null && auth.uid === $userId"
            }
        }
    }
}
```

---

## 12.4 Cloud Functions

### Theory

Cloud Functions for Firebase lets you automatically run backend code in response to events triggered by Firebase features and HTTPS requests. Your code is stored in Google's cloud and runs in a managed environment — no need to manage and scale your own servers.

**Key Concepts:**
- **Serverless**: You write functions, Google manages the infrastructure (servers, scaling, patches)
- **Event-driven**: Functions are triggered by events (Firestore write, Auth signup, HTTP request, etc.)
- **Stateless**: Each function invocation is independent — no shared memory between calls
- **Cold Start**: First invocation after idle period takes longer (~1-5s) because the runtime must initialize
- **Warm Instance**: Subsequent calls reuse the same container — much faster (~100ms)
- **Idempotent**: Functions may be retried — design them to produce the same result if run multiple times
- **Timeout**: Default 60s, max 540s (9 minutes) for HTTP functions

**Supported Runtimes**: Node.js (recommended), Python, Go, Java, .NET, Ruby, PHP

**When to use Cloud Functions vs your own backend:**
| Cloud Functions | Own Backend Server |
|----------------|-------------------|
| Event-driven tasks | Complex business logic |
| < 1M invocations/month | High-throughput APIs |
| Simple request/response | WebSocket connections |
| Auto-scaling needed | Predictable traffic |
| Pay-per-use budget | Fixed server costs OK |

**Function Types:**
```
┌──────────────────────────────────────────────────────────────┐
│                CLOUD FUNCTION TYPES                          │
│                                                              │
│  1. HTTPS Functions                                          │
│     onRequest()  → Raw HTTP (REST APIs, webhooks)            │
│     onCall()     → Client SDK callable (auto-auth, typing)   │
│                                                              │
│  2. Background Trigger Functions                             │
│     Firestore   → onCreate, onUpdate, onDelete, onWrite     │
│     Auth        → onCreate, onDelete                         │
│     Storage     → onFinalize, onDelete, onArchive            │
│     Pub/Sub     → onPublish (message queue)                  │
│     Scheduler   → onRun (cron jobs)                          │
│     RTDB        → onWrite, onCreate, onUpdate, onDelete      │
│                                                              │
│  3. V2 Functions (2nd gen — built on Cloud Run)              │
│     → Longer timeouts (up to 60 minutes)                     │
│     → Larger instances (up to 32 GB RAM)                     │
│     → Concurrency (handle multiple requests per instance)    │
│     → Traffic splitting (gradual rollouts)                   │
└──────────────────────────────────────────────────────────────┘
```

### Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                    CLOUD FUNCTIONS ARCHITECTURE                   │
│                                                                  │
│   TRIGGERS                    FUNCTION                 ACTIONS   │
│                                                                  │
│  ┌──────────┐              ┌──────────────┐         ┌─────────┐ │
│  │  HTTP    │─────────────→│              │────────→│ Send    │ │
│  │ Request  │              │              │         │ Email   │ │
│  └──────────┘              │              │         └─────────┘ │
│                            │              │                     │
│  ┌──────────┐              │   Cloud      │         ┌─────────┐ │
│  │Firestore │─────────────→│  Function    │────────→│ Update  │ │
│  │ Write    │              │  (Node.js)   │         │   DB    │ │
│  └──────────┘              │              │         └─────────┘ │
│                            │              │                     │
│  ┌──────────┐              │              │         ┌─────────┐ │
│  │  Auth    │─────────────→│              │────────→│  Send   │ │
│  │ Signup   │              │              │         │  FCM    │ │
│  └──────────┘              │              │         └─────────┘ │
│                            │              │                     │
│  ┌──────────┐              │              │         ┌─────────┐ │
│  │ Storage  │─────────────→│              │────────→│ Process │ │
│  │ Upload   │              │              │         │ Image   │ │
│  └──────────┘              └──────────────┘         └─────────┘ │
│                                                                  │
│  Function Lifecycle:                                            │
│  ┌────────┐   ┌────────┐   ┌─────────┐   ┌─────────┐           │
│  │ Event  │──→│Trigger │──→│ Execute │──→│  Cold/  │           │
│  │Happens │   │ Fires  │   │Function │   │ Warm    │           │
│  └────────┘   └────────┘   └─────────┘   │Shutdown │           │
│                                          └─────────┘           │
└──────────────────────────────────────────────────────────────────┘
```

### 12.4.1 Cloud Functions (Server-side - TypeScript/Node.js)

```typescript
// functions/src/index.ts
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

// ── HTTP Trigger ──
export const helloWorld = functions.https.onRequest((req, res) => {
    res.json({ message: "Hello from Firebase Cloud Functions!" });
});

// ── HTTP Callable Function (designed for client SDKs) ──
export const addMessage = functions.https.onCall(async (data, context) => {
    // Authentication check
    if (!context.auth) {
        throw new functions.https.HttpsError(
            "unauthenticated",
            "User must be authenticated"
        );
    }

    const text = data.text;
    const userId = context.auth.uid;

    // Write to Firestore
    const result = await admin.firestore().collection("messages").add({
        text: text,
        authorId: userId,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { messageId: result.id };
});

// ── Firestore Trigger: On document create ──
export const onUserCreated = functions.firestore
    .document("users/{userId}")
    .onCreate(async (snapshot, context) => {
        const userData = snapshot.data();
        const userId = context.params.userId;

        // Send welcome email
        await admin.firestore().collection("mail").add({
            to: userData.email,
            message: {
                subject: "Welcome!",
                text: `Hello ${userData.name}, welcome to our app!`,
            },
        });

        // Initialize user data
        await admin.firestore().collection("userStats").doc(userId).set({
            totalPosts: 0,
            totalLikes: 0,
            joinedAt: admin.firestore.FieldValue.serverTimestamp(),
        });
    });

// ── Firestore Trigger: On document update ──
export const onPostUpdated = functions.firestore
    .document("posts/{postId}")
    .onUpdate(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();

        // Detect changes
        if (before.title !== after.title) {
            console.log(`Post ${context.params.postId} title changed`);
        }
    });

// ── Auth Trigger: On user creation ──
export const onAuthUserCreated = functions.auth.user().onCreate(async (user) => {
    // Create user profile in Firestore
    await admin.firestore().collection("users").doc(user.uid).set({
        email: user.email,
        displayName: user.displayName || "",
        photoURL: user.photoURL || "",
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
});

// ── Auth Trigger: On user deletion ──
export const onAuthUserDeleted = functions.auth.user().onDelete(async (user) => {
    // Clean up user data
    const batch = admin.firestore().batch();
    batch.delete(admin.firestore().collection("users").doc(user.uid));
    batch.delete(admin.firestore().collection("userStats").doc(user.uid));
    await batch.commit();
});

// ── Scheduled Function (runs on a schedule) ──
export const dailyCleanup = functions.pubsub
    .schedule("every 24 hours")
    .onRun(async (context) => {
        const cutoff = new Date();
        cutoff.setDate(cutoff.getDate() - 30);

        const oldDocs = await admin.firestore()
            .collection("tempData")
            .where("createdAt", "<", cutoff)
            .get();

        const batch = admin.firestore().batch();
        oldDocs.docs.forEach((doc) => batch.delete(doc.ref));
        await batch.commit();

        console.log(`Deleted ${oldDocs.size} old documents`);
    });
```

### 12.4.2 Calling Cloud Functions from Android

```kotlin
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class CloudFunctionsManager {
    private val functions = FirebaseFunctions.getInstance()

    // ── Call an HTTPS Callable Function ──
    suspend fun addMessage(text: String): Result<String> {
        return try {
            val data = hashMapOf("text" to text)
            val result = functions
                .getHttpsCallable("addMessage")
                .call(data)
                .await()

            val messageId = (result.data as Map<*, *>)["messageId"] as String
            Result.success(messageId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Call with typed response ──
    suspend fun getUserStats(userId: String): Result<Map<String, Any>> {
        return try {
            val data = hashMapOf("userId" to userId)
            val result = functions
                .getHttpsCallable("getUserStats")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val stats = result.data as Map<String, Any>
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Use a specific region ──
    fun getFunctionsForRegion(region: String): FirebaseFunctions {
        return FirebaseFunctions.getInstance(region)
    }

    // ── Use emulator for testing ──
    fun useEmulator() {
        functions.useEmulator("10.0.2.2", 5001) // Android emulator localhost
    }
}
```

---

## 12.5 Firebase Cloud Messaging (FCM)

### Theory

Firebase Cloud Messaging (FCM) is a cross-platform messaging solution that lets you reliably send messages (push notifications) at no cost. You can send notification messages (displayed by the system), data messages (handled by the app), or both.

**Key Concepts:**
- **FCM Token**: A unique device+app identifier generated by the SDK. Changes when app is reinstalled, data is cleared, or device is restored. Must be sent to your backend for targeting.
- **Notification Message**: Contains `title` and `body`. System tray handles display when app is in background; `onMessageReceived()` only fires in foreground.
- **Data Message**: Contains only custom key-value pairs. Always delivered to `onMessageReceived()` regardless of app state. Max payload: 4 KB.
- **Topic**: A named channel users can subscribe to (e.g., "news", "deals"). Server sends to topic → all subscribers receive it. Max 2000 topics per app instance.
- **Condition**: Logical expression combining topics (e.g., `'TopicA' in topics && !('TopicB' in topics)`).
- **Upstream Messages**: Rarely used — sending from client to server. Deprecated in favor of HTTPS from client.

**Critical Behavior by App State:**
```
┌──────────────────────────────────────────────────────────────────┐
│          FCM MESSAGE BEHAVIOR BY APP STATE                       │
│                                                                  │
│  Message Type        App in          App in          App         │
│                    Foreground      Background     Terminated     │
│  ─────────────────────────────────────────────────────────────   │
│  Notification      onMessage-      System tray    System tray   │
│  only              Received()      (auto-display) (auto-display)│
│                                                                  │
│  Data only         onMessage-      onMessage-     onMessage-    │
│                    Received()      Received()     Received()    │
│                                                                  │
│  Notification      onMessage-      System tray    System tray   │
│  + Data            Received()      (data in       (data in      │
│                    (both avail)    intent extras)  intent extras)│
│                                                                  │
│  BEST PRACTICE: Use data-only messages for full control.        │
│  Build the notification yourself in onMessageReceived().        │
└──────────────────────────────────────────────────────────────────┘
```

**Android 13+ (API 33) Runtime Permission:**
Starting from Android 13, you must request `POST_NOTIFICATIONS` permission at runtime before showing notifications. Without it, notifications are silently dropped.

### FCM Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                     FCM MESSAGE FLOW                             │
│                                                                  │
│  ┌──────────┐       ┌───────────┐       ┌──────────────────┐    │
│  │  Your    │──────→│   FCM     │──────→│  Client Device   │    │
│  │ Server/  │ HTTP  │  Backend  │       │  (Android App)   │    │
│  │ Console  │       │  (Google) │       │                  │    │
│  └──────────┘       └───────────┘       └──────────────────┘    │
│                                                                  │
│  Message Types:                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                                                          │   │
│  │  1. NOTIFICATION MESSAGE        2. DATA MESSAGE          │   │
│  │  ┌─────────────────────┐       ┌─────────────────────┐  │   │
│  │  │ {                   │       │ {                   │  │   │
│  │  │  "notification": {  │       │  "data": {          │  │   │
│  │  │    "title": "Hi",   │       │    "key1": "val1",  │  │   │
│  │  │    "body": "Hello"  │       │    "key2": "val2"   │  │   │
│  │  │  }                  │       │  }                  │  │   │
│  │  │ }                   │       │ }                   │  │   │
│  │  └─────────┬───────────┘       └─────────┬───────────┘  │   │
│  │            │                             │              │   │
│  │  App in    │ App in       App in    │ App in            │   │
│  │  FG: App   │ BG: System   FG: App   │ BG: App          │   │
│  │  handles   │ tray         handles   │ handles           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  Targeting Methods:                                              │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐    │
│  │ Single Device  │  │    Topic       │  │  Device Group  │    │
│  │ (Token)        │  │ (Subscribe)    │  │  (Group Key)   │    │
│  └────────────────┘  └────────────────┘  └────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
```

### 12.5.1 FCM Setup and Service

```xml
<!-- AndroidManifest.xml additions -->

<!-- Required for Android 13+ (API 33+) runtime notification permission -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<application ...>
    <service
        android:name=".MyFirebaseMessagingService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>

    <meta-data
        android:name="com.google.firebase.messaging.default_notification_icon"
        android:resource="@drawable/ic_notification" />
    <meta-data
        android:name="com.google.firebase.messaging.default_notification_channel_id"
        android:value="default_channel" />
</application>
```

**Request notification permission (Android 13+):**
```kotlin
// In your Activity/Fragment
private val notificationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        // Permission granted — FCM notifications will display
    } else {
        // Permission denied — show rationale or guide user to settings
    }
}

fun requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
```

**Messaging Service:**

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // ── Called when a new token is generated ──
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your server
        sendTokenToServer(token)
    }

    // ── Called when a message is received ──
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Check if message contains a notification payload
        message.notification?.let { notification ->
            val title = notification.title ?: "Notification"
            val body = notification.body ?: ""
            showNotification(title, body)
        }

        // Check if message contains a data payload
        if (message.data.isNotEmpty()) {
            handleDataMessage(message.data)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        val content = data["content"]

        when (type) {
            "chat" -> {
                val senderId = data["senderId"] ?: return
                val messageText = data["message"] ?: return
                showChatNotification(senderId, messageText)
            }
            "update" -> {
                // Trigger a background data sync
                syncData()
            }
            "silent" -> {
                // Process silently without showing notification
                processInBackground(data)
            }
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "default_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // Create intent for notification tap
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android 8+ (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Default notification channel"
                enableVibration(true)
                enableLights(true)
            }
            manager.createNotificationChannel(channel)
        }

        manager.notify(notificationId, notification)
    }

    private fun sendTokenToServer(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .update("fcmToken", token)
    }

    private fun syncData() { /* ... */ }
    private fun processInBackground(data: Map<String, String>) { /* ... */ }
    private fun showChatNotification(senderId: String, message: String) { /* ... */ }
}
```

### 12.5.2 Topics and Subscriptions

```kotlin
class FCMTopicManager {
    private val messaging = FirebaseMessaging.getInstance()

    // ── Subscribe to a topic ──
    fun subscribeToTopic(topic: String) {
        messaging.subscribeToTopic(topic)
            .addOnSuccessListener {
                Log.d("FCM", "Subscribed to $topic")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Subscribe failed", e)
            }
    }

    // ── Unsubscribe from a topic ──
    fun unsubscribeFromTopic(topic: String) {
        messaging.unsubscribeFromTopic(topic)
            .addOnSuccessListener {
                Log.d("FCM", "Unsubscribed from $topic")
            }
    }

    // ── Get current token ──
    suspend fun getToken(): String? {
        return try {
            messaging.token.await()
        } catch (e: Exception) {
            null
        }
    }

    // Example topic subscriptions
    fun setupDefaultTopics() {
        subscribeToTopic("news")
        subscribeToTopic("updates")
        // User-specific topics
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { subscribeToTopic("user_$it") }
    }
}
```

### 12.5.3 Notification Channels (Android 8+)

```kotlin
class NotificationChannelManager(private val context: Context) {

    companion object {
        const val CHANNEL_GENERAL = "general"
        const val CHANNEL_CHAT = "chat_messages"
        const val CHANNEL_UPDATES = "app_updates"
        const val CHANNEL_PROMOTIONS = "promotions"
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)

        val channels = listOf(
            NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications"
            },
            NotificationChannel(
                CHANNEL_CHAT,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat message notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                enableLights(true)
                lightColor = Color.BLUE
                setShowBadge(true)
            },
            NotificationChannel(
                CHANNEL_UPDATES,
                "App Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "App update notifications"
            },
            NotificationChannel(
                CHANNEL_PROMOTIONS,
                "Promotions",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Promotional notifications"
                setShowBadge(false)
            }
        )

        // Create a channel group
        manager.createNotificationChannelGroup(
            NotificationChannelGroup("social", "Social")
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }
}

// ── Initialize in Application class ──
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannelManager(this).createNotificationChannels()
    }
}
```

### 12.5.4 Sending Messages (Server-side)

```typescript
// Cloud Function to send notifications
import * as admin from "firebase-admin";

// Send to a specific device
async function sendToDevice(token: string, title: string, body: string) {
    await admin.messaging().send({
        token: token,
        notification: { title, body },
        android: {
            priority: "high",
            notification: {
                channelId: "chat_messages",
                sound: "default",
                clickAction: "OPEN_CHAT",
            },
        },
    });
}

// Send to a topic
async function sendToTopic(topic: string, title: string, body: string) {
    await admin.messaging().send({
        topic: topic,
        notification: { title, body },
        data: {
            type: "news",
            articleId: "12345",
        },
    });
}

// Send data-only message (always handled by app, even in background)
async function sendDataMessage(token: string, data: Record<string, string>) {
    await admin.messaging().send({
        token: token,
        data: data,
        android: {
            priority: "high",
        },
    });
}
```

---

## 12.6 Firebase Analytics

### Theory

Firebase Analytics (now integrated with Google Analytics 4) is a free and unlimited analytics solution. It provides insight on app usage and user engagement. It automatically captures certain events and user properties, and you can define your own custom events.

**Key Concepts:**
- **Event**: A discrete action a user performs (e.g., `screen_view`, `purchase`, `button_click`). Each event can have up to **25 parameters**.
- **User Property**: An attribute of the user (e.g., `subscription_type`, `user_level`). Up to **25 custom** user properties per project.
- **Session**: A period of user engagement. A session starts when the app comes to foreground and ends after 30 minutes of inactivity.
- **Audience**: A group of users defined by conditions (e.g., "Users who purchased in the last 7 days"). Used for targeted messaging and A/B tests.
- **Funnel**: A sequence of events that defines a user path (e.g., `open_app → view_product → add_to_cart → purchase`). Funnels show where users drop off.
- **Conversion**: A marked event that represents a key business goal (e.g., `purchase`, `sign_up`). Up to 30 conversion events.

**What is Collected Automatically (zero code):**
| Event | Description |
|-------|-------------|
| `first_open` | First time user opens the app after install |
| `session_start` | When a new session begins |
| `screen_view` | Each time a screen is displayed (Activity-based) |
| `app_update` | App version changes |
| `app_remove` | App is uninstalled (Android only) |
| `os_update` | Device OS version changes |
| `app_exception` | Uncaught exception occurs |
| `user_engagement` | When app is in foreground |
| `in_app_purchase` | In-app purchase via Google Play |

**Analytics Data Pipeline:**
```
┌──────────────────────────────────────────────────────────────────┐
│                 ANALYTICS DATA PIPELINE                          │
│                                                                  │
│  App Event → SDK Batches → Upload on:         → Firebase Console │
│              (locally)      • Wi-Fi available    (within ~1 hr)  │
│                            • Every 1 hour                         │
│                            • App goes to BG                       │
│                            • SDK has 1000+                        │
│                              events queued                        │
│                                                                  │
│  Firebase Console                                                │
│  └─→ Google Analytics 4 Dashboard                                │
│      └─→ BigQuery Export (raw event data, Blaze plan only)       │
│          └─→ Data Studio / Looker (custom reports)               │
│                                                                  │
│  Debug Mode (see events in real-time):                           │
│  $ adb shell setprop debug.firebase.analytics.app PACKAGE_NAME   │
│  Then open: Firebase Console → DebugView                         │
└──────────────────────────────────────────────────────────────────┘
```

**Parameter Limits:**
- Max 500 distinct event types per app
- Max 25 parameters per event
- Event name: max 40 chars, must start with letter
- Parameter name: max 40 chars
- Parameter string value: max 100 chars

### Analytics Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                  FIREBASE ANALYTICS FLOW                     │
│                                                              │
│  ┌─────────────┐    ┌──────────────┐    ┌────────────────┐  │
│  │ User Action │───→│ Log Event    │───→│  Firebase      │  │
│  │ in App      │    │ (SDK)        │    │  Analytics     │  │
│  └─────────────┘    └──────────────┘    │  Dashboard     │  │
│                                         └───────┬────────┘  │
│                                                 │            │
│                                         ┌───────▼────────┐  │
│                                         │  Google        │  │
│                                         │  Analytics 4   │  │
│                                         │  (GA4)         │  │
│                                         └────────────────┘  │
│                                                              │
│  Automatic Events:        Custom Events:                     │
│  • first_open              • purchase                        │
│  • session_start           • level_complete                  │
│  • app_update              • share_content                   │
│  • in_app_purchase         • search                          │
│  • screen_view             • sign_up_method                  │
│  • app_exception           • (your custom events)            │
│                                                              │
│  User Properties:                                            │
│  • Auto: age, gender, interests, country, device             │
│  • Custom: subscription_type, user_level, theme_preference   │
└──────────────────────────────────────────────────────────────┘
```

### 12.6.1 Event Tracking

```kotlin
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class AnalyticsManager(private val context: Context) {
    private val analytics: FirebaseAnalytics = Firebase.analytics

    // ── Log screen view ──
    fun logScreenView(screenName: String, screenClass: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
    }

    // ── Log predefined events ──
    fun logLogin(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }

    fun logSignUp(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }

    fun logPurchase(itemId: String, itemName: String, price: Double) {
        analytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.ITEM_ID, itemId)
            param(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            param(FirebaseAnalytics.Param.VALUE, price)
            param(FirebaseAnalytics.Param.CURRENCY, "USD")
        }
    }

    fun logSearch(searchTerm: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SEARCH) {
            param(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm)
        }
    }

    fun logShare(contentType: String, itemId: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SHARE) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            param(FirebaseAnalytics.Param.ITEM_ID, itemId)
        }
    }

    fun logSelectContent(contentType: String, itemId: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            param(FirebaseAnalytics.Param.ITEM_ID, itemId)
        }
    }
}
```

### 12.6.2 Custom Events

```kotlin
class CustomAnalytics(context: Context) {
    private val analytics = Firebase.analytics

    // ── Custom events ──
    fun logLevelCompleted(level: Int, score: Int, timeSpent: Long) {
        analytics.logEvent("level_completed") {
            param("level_number", level.toLong())
            param("score", score.toLong())
            param("time_spent_seconds", timeSpent)
            param("difficulty", "hard")
        }
    }

    fun logFeatureUsed(featureName: String) {
        analytics.logEvent("feature_used") {
            param("feature_name", featureName)
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logArticleRead(articleId: String, category: String, readTime: Long) {
        analytics.logEvent("article_read") {
            param("article_id", articleId)
            param("category", category)
            param("read_time_seconds", readTime)
        }
    }

    fun logButtonClick(buttonName: String, screenName: String) {
        analytics.logEvent("button_click") {
            param("button_name", buttonName)
            param("screen_name", screenName)
        }
    }

    fun logError(errorType: String, errorMessage: String, screen: String) {
        analytics.logEvent("app_error") {
            param("error_type", errorType)
            param("error_message", errorMessage.take(100)) // Max 100 chars
            param("screen", screen)
        }
    }
}
```

### 12.6.3 User Properties

```kotlin
class UserPropertyManager(context: Context) {
    private val analytics = Firebase.analytics

    // ── Set user properties ──
    fun setUserProperties(
        subscriptionType: String,
        userLevel: Int,
        favoriteCategory: String
    ) {
        analytics.setUserProperty("subscription_type", subscriptionType)
        analytics.setUserProperty("user_level", userLevel.toString())
        analytics.setUserProperty("favorite_category", favoriteCategory)
    }

    // ── Set user ID (cross-device tracking) ──
    fun setUserId(userId: String) {
        analytics.setUserId(userId)
    }

    // ── Set default event parameters ──
    fun setDefaultParameters() {
        val defaults = Bundle().apply {
            putString("app_version", BuildConfig.VERSION_NAME)
            putString("platform", "android")
        }
        analytics.setDefaultEventParameters(defaults)
    }

    // ── Reset (on sign out) ──
    fun clearUserData() {
        analytics.setUserId(null)
        analytics.setUserProperty("subscription_type", null)
        analytics.setUserProperty("user_level", null)
    }

    // ── Consent management ──
    fun setAnalyticsConsent(granted: Boolean) {
        analytics.setAnalyticsCollectionEnabled(granted)
    }
}
```

---

## 12.7 Crashlytics

### Theory

Firebase Crashlytics is a lightweight, real-time crash reporter that helps you track, prioritize, and fix stability issues that erode your app quality. It groups crashes intelligently and highlights the circumstances leading to them.

**Key Concepts:**
- **Issue**: A group of crashes with the same root cause (same stack trace signature). Crashlytics automatically clusters crashes into issues.
- **Session**: A single app session from launch to termination. Crash-free session rate is the primary stability metric.
- **Crash-Free Users**: Percentage of users who did NOT experience a crash. Target: **99.5%+** for a healthy app.
- **Velocity Alert**: Automatic notification when a new crash suddenly affects many users (regression detection).
- **Breadcrumbs**: Chronological log entries that show what the user was doing before the crash.
- **Custom Keys**: Metadata key-value pairs (screen name, feature flag values, user state) attached to every crash for debugging context.
- **Non-Fatal**: Exceptions that you explicitly report — app doesn't crash, but something went wrong (API failures, parsing errors, etc.).
- **ANR (Application Not Responding)**: Detected when the main thread is blocked for 5+ seconds. Crashlytics captures ANR stack traces.

**How Crashlytics Works Internally:**
```
┌──────────────────────────────────────────────────────────────────┐
│               CRASHLYTICS INTERNAL PIPELINE                      │
│                                                                  │
│  1. SDK installs an UncaughtExceptionHandler                     │
│  2. On crash: captures stack trace + device info + custom logs   │
│  3. Writes crash report to LOCAL DISK (persist across restart)   │
│  4. On NEXT app launch: uploads crash report to Firebase servers │
│  5. Server deobfuscates stack traces using uploaded mapping.txt  │
│  6. Groups crash into an Issue based on stack trace signature     │
│  7. Appears in Firebase Console (usually within 5 minutes)       │
│                                                                  │
│  ProGuard/R8 Deobfuscation:                                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ The Crashlytics Gradle plugin automatically uploads      │   │
│  │ mapping.txt on each build. Without it, stack traces      │   │
│  │ show obfuscated names like a.b.c() instead of            │   │
│  │ com.example.MyClass.myMethod()                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  Key Metrics (Firebase Console → Crashlytics Dashboard):        │
│  • Crash-free users (%)      • Crash-free sessions (%)          │
│  • Total crashes             • Affected users per issue          │
│  • Issues by priority        • Trends over time                  │
│  • Velocity alerts           • Breadcrumb timeline               │
└──────────────────────────────────────────────────────────────────┘
```

### Crashlytics Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                    CRASHLYTICS FLOW                          │
│                                                              │
│  ┌───────────┐    ┌──────────────┐    ┌────────────────┐    │
│  │  App      │───→│  Crashlytics │───→│   Firebase     │    │
│  │  Crash/   │    │     SDK      │    │   Console      │    │
│  │  Error    │    │              │    │   Dashboard    │    │
│  └───────────┘    └──────┬───────┘    └────────┬───────┘    │
│                          │                     │            │
│                    ┌─────▼─────┐         ┌─────▼─────┐     │
│                    │ Capture   │         │ Grouped   │     │
│                    │ Stack     │         │ by Issue  │     │
│                    │ Trace     │         │ Signature │     │
│                    │ + Logs    │         └───────────┘     │
│                    │ + Keys    │                           │
│                    │ + User ID │                           │
│                    └───────────┘                           │
│                                                              │
│  What Crashlytics Captures:                                  │
│  • Stack traces (deobfuscated with mapping files)            │
│  • Device info (model, OS version, orientation)              │
│  • Memory state (free/used RAM)                              │
│  • Custom logs you add                                       │
│  • Custom keys for context                                   │
│  • User identifiers                                          │
│  • Non-fatal exceptions (manually reported)                  │
│  • ANRs (Application Not Responding)                         │
└──────────────────────────────────────────────────────────────┘
```

### 12.7.1 Crash Reporting Setup

```kotlin
import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashlyticsManager {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    // ── Set user identifier ──
    fun setUser(userId: String) {
        crashlytics.setUserId(userId)
    }

    // ── Custom logs (appear in crash report timeline) ──
    fun log(message: String) {
        crashlytics.log(message)
    }

    // Max 64 key-value pairs; keys up to 1024 chars, values up to 1024 chars
    // ── Custom keys (appear as metadata in crash report) ──
    fun setCustomKeys() {
        crashlytics.setCustomKey("current_screen", "HomeScreen")
        crashlytics.setCustomKey("experiment_variant", "B")
        crashlytics.setCustomKey("items_in_cart", 3)
        crashlytics.setCustomKey("is_premium_user", true)
        crashlytics.setCustomKey("last_action", "clicked_buy")
    }
}
```

### 12.7.2 Non-Fatal Exception Tracking

```kotlin
class NonFatalExceptionTracker {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    // ── Record non-fatal exceptions ──
    fun trackNonFatal(exception: Exception) {
        crashlytics.recordException(exception)
    }

    // ── Practical usage in a repository ──
    suspend fun fetchData(): Result<Data> {
        return try {
            val response = apiService.getData()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                // Log non-fatal: API returned error
                val exception = Exception("API error: ${response.code()} - ${response.message()}")
                crashlytics.log("Fetching data from API endpoint /data")
                crashlytics.setCustomKey("http_status_code", response.code())
                crashlytics.recordException(exception)
                Result.failure(exception)
            }
        } catch (e: Exception) {
            // Log non-fatal: Network error
            crashlytics.log("Network error while fetching data")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    // ── Custom exception class for better grouping ──
    class PaymentException(
        message: String,
        val errorCode: String,
        cause: Throwable? = null
    ) : Exception(message, cause)

    fun trackPaymentError(errorCode: String, message: String) {
        crashlytics.setCustomKey("payment_error_code", errorCode)
        crashlytics.log("Payment failed with code: $errorCode")
        crashlytics.recordException(
            PaymentException(message, errorCode)
        )
    }
}
```

### 12.7.3 Advanced Crashlytics Configuration

```kotlin
class CrashlyticsConfig {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    // ── Enable/Disable crash collection (opt-in/opt-out) ──
    fun setCrashlyticsEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    // ── Global uncaught exception handler with Crashlytics context ──
    fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Add last-minute context before crash
            crashlytics.setCustomKey("crash_thread", thread.name)
            crashlytics.setCustomKey("free_memory_mb",
                Runtime.getRuntime().freeMemory() / (1024 * 1024)
            )
            crashlytics.log("Uncaught exception on thread: ${thread.name}")

            // Let Crashlytics handle it
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    // ── Force a test crash ──
    fun forceCrash() {
        throw RuntimeException("Test Crash - Crashlytics Integration")
    }

    // ── Check if crash collection is enabled ──
    fun isCrashlyticsEnabled(): Boolean {
        return crashlytics.isCrashlyticsCollectionEnabled
    }

    // ── Timber integration for automatic Crashlytics logging ──
    class CrashlyticsTree : Timber.Tree() {
        private val crashlytics = FirebaseCrashlytics.getInstance()

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority >= Log.WARN) {
                crashlytics.log("$tag: $message")
                t?.let { crashlytics.recordException(it) }
            }
        }
    }
}
```

---

## 12.8 Remote Config

### Theory

Firebase Remote Config lets you change the behavior and appearance of your app without publishing an app update. You define default values in the app, then override them from the Firebase Console. You can use it for A/B testing, feature flags, and dynamic configuration.

**Key Concepts:**
- **Parameter**: A key-value pair (e.g., `"show_new_feature" → true`). Max 2000 parameters per project.
- **Default Value**: Hardcoded in the app — used when no fetched value is available.
- **Fetched Value**: Downloaded from the server but NOT yet active. Sits in a staging area.
- **Active Value**: Applied after calling `activate()`. This is the value your app actually reads.
- **Condition**: A rule that targets specific users (by app version, platform, user %, country, language, audience, etc.).
- **Minimum Fetch Interval**: Throttle to prevent excessive server requests. Default 12 hours in production.
- **Real-time Updates**: Push-based listener that notifies the app when values change on the server — no polling needed.

**Value Resolution Order (highest priority wins):**
```
┌──────────────────────────────────────────────────────────────┐
│       REMOTE CONFIG VALUE RESOLUTION                         │
│                                                              │
│  Priority 1: Server value with matching CONDITION            │
│   └── If user matches multiple conditions, the              │
│       one with the HIGHEST priority (set in Console) wins   │
│                                                              │
│  Priority 2: Server DEFAULT value (no condition)             │
│                                                              │
│  Priority 3: In-app default value (setDefaultsAsync)         │
│                                                              │
│  Priority 4: Static type default (0 for Long, "" for String) │
│                                                              │
│  IMPORTANT: Values are only visible to the app after         │
│  fetch() + activate() (or fetchAndActivate())                │
└──────────────────────────────────────────────────────────────┘
```

**Common Use Cases:**
| Use Case | Example |
|----------|---------|
| **Feature flags** | Enable a feature for 10% of users before full rollout |
| **Kill switch** | Disable a broken API endpoint remotely without app update |
| **A/B testing** | Show variant A (blue button) to 50%, variant B (green) to 50% |
| **Force update** | Compare `minimum_app_version` against installed version |
| **Seasonal content** | Change banner images, colors, promotions remotely |
| **Dynamic limits** | Change max upload size, pagination count, retry limits |
| **Maintenance mode** | Show maintenance screen without deploying |

### Remote Config Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                 REMOTE CONFIG FLOW                           │
│                                                              │
│  ┌─────────────┐                  ┌────────────────────┐    │
│  │  Firebase    │                  │   Android App      │    │
│  │  Console     │                  │                    │    │
│  │             │    fetch()        │  ┌──────────────┐  │    │
│  │  Set        │◄─────────────────│  │ In-App       │  │    │
│  │  Values     │                  │  │ Default      │  │    │
│  │             │────────────────→ │  │ Values       │  │    │
│  │             │   response       │  └──────┬───────┘  │    │
│  └─────────────┘                  │         │          │    │
│                                   │  ┌──────▼───────┐  │    │
│                                   │  │  Fetched     │  │    │
│                                   │  │  Values      │  │    │
│                                   │  │  (Pending)   │  │    │
│                                   │  └──────┬───────┘  │    │
│                                   │         │activate()│    │
│                                   │  ┌──────▼───────┐  │    │
│                                   │  │  Active      │  │    │
│                                   │  │  Values      │  │    │
│                                   │  │  (In Use)    │  │    │
│                                   │  └──────────────┘  │    │
│                                   └────────────────────┘    │
│                                                              │
│  Value Resolution Order:                                     │
│  1. Active (fetched + activated) → highest priority          │
│  2. In-app default values → fallback                         │
│  3. Static type default (0, false, "") → last resort         │
│                                                              │
│  Use Cases:                                                  │
│  • Feature flags (enable/disable features)                   │
│  • A/B testing (show different UIs)                          │
│  • Seasonal themes / promotions                              │
│  • Gradual rollouts                                          │
│  • Kill switches for broken features                         │
└──────────────────────────────────────────────────────────────┘
```

### 12.8.1 Remote Config Setup

```kotlin
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class RemoteConfigManager {
    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    // ── Initialize ──
    fun initialize() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) {
                0  // Fetch every time in debug
            } else {
                3600  // 1 hour in production
            }
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set in-app default values
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        // OR set defaults from a map
        remoteConfig.setDefaultsAsync(
            mapOf(
                "welcome_message" to "Welcome to our app!",
                "show_new_feature" to false,
                "max_items_per_page" to 20L,
                "discount_percentage" to 10.0,
                "maintenance_mode" to false,
                "minimum_app_version" to "1.0.0",
                "onboarding_flow" to "default",
                "banner_color" to "#FF5722"
            )
        )
    }

    // ── Fetch and activate ──
    suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            false
        }
    }

    // ── Fetch only (activate later) ──
    suspend fun fetchOnly() {
        try {
            remoteConfig.fetch().await()
        } catch (e: Exception) {
            // Use cached/default values
        }
    }

    // ── Activate fetched values ──
    suspend fun activate(): Boolean {
        return remoteConfig.activate().await()
    }

    // ── Get values ──
    fun getWelcomeMessage(): String = remoteConfig.getString("welcome_message")
    fun isNewFeatureEnabled(): Boolean = remoteConfig.getBoolean("show_new_feature")
    fun getMaxItemsPerPage(): Long = remoteConfig.getLong("max_items_per_page")
    fun getDiscountPercentage(): Double = remoteConfig.getDouble("discount_percentage")
    fun isMaintenanceMode(): Boolean = remoteConfig.getBoolean("maintenance_mode")
    fun getBannerColor(): String = remoteConfig.getString("banner_color")

    // ── Real-time config updates (listen for changes) ──
    fun enableRealTimeUpdates() {
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                // Activate the updated values
                remoteConfig.activate().addOnCompleteListener {
                    // Apply new config values to UI
                    val updatedKeys = configUpdate.updatedKeys
                    // Handle updated keys
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.e("RemoteConfig", "Config update error", error)
            }
        })
    }
}
```

### 12.8.2 Feature Flags

```kotlin
class FeatureFlagManager {
    private val remoteConfig = Firebase.remoteConfig

    // ── Feature flag checks ──
    fun isFeatureEnabled(featureKey: String): Boolean {
        return remoteConfig.getBoolean(featureKey)
    }

    // ── Conditional UI based on flags ──
    fun getUIConfig(): UIConfig {
        return UIConfig(
            showBottomNav = remoteConfig.getBoolean("show_bottom_nav"),
            showSearchBar = remoteConfig.getBoolean("show_search_bar"),
            showAds = remoteConfig.getBoolean("show_ads"),
            maxFreeArticles = remoteConfig.getLong("max_free_articles").toInt(),
            theme = remoteConfig.getString("app_theme")
        )
    }

    data class UIConfig(
        val showBottomNav: Boolean,
        val showSearchBar: Boolean,
        val showAds: Boolean,
        val maxFreeArticles: Int,
        val theme: String
    )

    // ── A/B Testing variant ──
    fun getOnboardingVariant(): String {
        return remoteConfig.getString("onboarding_variant")
        // Returns "control", "variant_a", or "variant_b"
    }

    // ── Force update check ──
    fun shouldForceUpdate(currentVersion: String): Boolean {
        val minVersion = remoteConfig.getString("minimum_app_version")
        return compareVersions(currentVersion, minVersion) < 0
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}
```

### 12.8.3 Usage in ViewModel

```kotlin
class MainViewModel(
    private val remoteConfigManager: RemoteConfigManager,
    private val featureFlagManager: FeatureFlagManager
) : ViewModel() {

    private val _uiConfig = MutableLiveData<FeatureFlagManager.UIConfig>()
    val uiConfig: LiveData<FeatureFlagManager.UIConfig> = _uiConfig

    init {
        loadRemoteConfig()
    }

    private fun loadRemoteConfig() {
        viewModelScope.launch {
            val activated = remoteConfigManager.fetchAndActivate()
            if (activated) {
                _uiConfig.value = featureFlagManager.getUIConfig()
            }

            // Check for maintenance mode
            if (remoteConfigManager.isMaintenanceMode()) {
                // Show maintenance screen
            }

            // Check for force update
            val currentVersion = BuildConfig.VERSION_NAME
            if (featureFlagManager.shouldForceUpdate(currentVersion)) {
                // Show force update dialog
            }
        }
    }
}
```

---

## 12.9 Performance Monitoring

### Theory

Firebase Performance Monitoring helps you gain insight into the performance characteristics of your app. It uses automatic and custom instrumentation to measure app startup time, HTTP network requests, and other key metrics.

**Key Concepts:**
- **Trace**: A measurement of time between two points in your code (start → stop), with optional metrics and attributes.
- **Metric**: A numeric value attached to a trace (e.g., `items_loaded: 42`, `cache_hits: 5`). Up to 32 metrics per trace.
- **Attribute**: A string key-value pair for filtering (e.g., `screen: "Home"`, `user_type: "premium"`). Up to 5 per trace.
- **Automatic Trace**: Built-in measurements captured without any code (app start, screen rendering, foreground/background).
- **Custom Trace**: Manually instrumented code sections you want to measure.
- **Network Request Metric**: Automatic monitoring of HTTP/S requests — response time, payload size, success rate.

**What Gets Measured Automatically (zero code):**
| Metric | What It Measures | Why It Matters |
|--------|-----------------|----------------|
| `_app_start` | Time from process start to first Activity's `onResume()` | Users abandon apps with >3s cold start |
| `_app_in_foreground` | Duration app is visible | Engagement metric |
| `_app_in_background` | Duration app is backgrounded | Resource usage insight |
| Screen rendering | Slow frames (>16ms) and frozen frames (>700ms) | Jank = bad UX |
| HTTP requests | Response time, size, status code | API performance |

**Performance Budgets (recommended targets):**
```
┌──────────────────────────────────────────────────────────────┐
│           PERFORMANCE BUDGETS                                │
│                                                              │
│  Cold Start:           < 2 seconds     (aim for < 1s)        │
│  Warm Start:           < 1 second                            │
│  Screen Transition:    < 300ms                               │
│  API Response (P95):   < 1 second                            │
│  Slow Rendering Rate:  < 5% of frames                        │
│  Frozen Frame Rate:    < 1% of frames                        │
│  App Size (APK):       < 50 MB (ideal < 20 MB)               │
│                                                              │
│  Firebase Performance shows percentiles (P50, P90, P95)      │
│  to help you understand real-world user experience.          │
└──────────────────────────────────────────────────────────────┘
```

### Performance Monitoring Diagram

```
┌──────────────────────────────────────────────────────────────┐
│              PERFORMANCE MONITORING                          │
│                                                              │
│  AUTOMATIC TRACES:                                           │
│  ┌──────────────────────────────────────────────────┐       │
│  │ • App start trace (_app_start)                    │       │
│  │   - Cold start time                               │       │
│  │   - Warm start time                               │       │
│  │                                                    │       │
│  │ • App in foreground trace (_app_in_foreground)     │       │
│  │   - Time app is visible                           │       │
│  │                                                    │       │
│  │ • App in background trace (_app_in_background)     │       │
│  │   - Time app is in background                     │       │
│  │                                                    │       │
│  │ • Screen rendering traces (per Activity/Fragment)  │       │
│  │   - Slow rendering frames                         │       │
│  │   - Frozen frames                                 │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  NETWORK MONITORING (Automatic):                             │
│  ┌──────────────────────────────────────────────────┐       │
│  │ For each HTTP/S request:                          │       │
│  │ • Response time                                   │       │
│  │ • Payload size (request & response)               │       │
│  │ • Success rate                                    │       │
│  │ • URL pattern aggregation                         │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  CUSTOM TRACES:                                              │
│  ┌──────────────────────────────────────────────────┐       │
│  │ trace("my_trace") {                               │       │
│  │   start ─────────────────────────── stop           │       │
│  │           └─ metrics & attributes ─┘               │       │
│  │           └─ counters ─────────────┘               │       │
│  │ }                                                  │       │
│  └──────────────────────────────────────────────────┘       │
└──────────────────────────────────────────────────────────────┘
```

### 12.9.1 Automatic Traces

```kotlin
// Automatic traces require NO CODE changes!
// Just add the Gradle plugin and dependency:

// build.gradle.kts (app-level)
// plugins {
//     id("com.google.firebase.firebase-perf")
// }
// dependencies {
//     implementation("com.google.firebase:firebase-perf-ktx")
// }

// Firebase Performance automatically monitors:
// 1. App startup time
// 2. Screen rendering (slow & frozen frames)
// 3. HTTP/HTTPS network requests (using OkHttp, HttpURLConnection)
// 4. App foreground/background time
```

### 12.9.2 Custom Traces

```kotlin
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.perf.ktx.trace

class PerformanceManager {
    private val performance = FirebasePerformance.getInstance()

    // ── Method 1: Manual trace management ──
    fun manualTrace() {
        val trace = performance.newTrace("data_processing")
        trace.start()

        // Add custom metrics
        trace.putMetric("items_processed", 0)
        trace.putAttribute("data_source", "api")
        trace.putAttribute("user_type", "premium")

        // Do work...
        for (i in 1..100) {
            processItem(i)
            trace.incrementMetric("items_processed", 1)
        }

        trace.stop()
    }

    // ── Method 2: Using Kotlin extension (recommended) ──
    suspend fun loadDataWithTrace() {
        // trace() extension function handles start/stop automatically
        val result = trace("load_user_data") {
            putAttribute("source", "firestore")

            val users = fetchUsersFromFirestore()

            putMetric("user_count", users.size.toLong())
            putAttribute("cache_hit", "false")

            users
        }
    }

    // ── Trace database operations ──
    fun traceDatabaseQuery(queryName: String, block: () -> Unit) {
        val trace = performance.newTrace("db_query_$queryName")
        trace.start()
        trace.putAttribute("query_name", queryName)

        try {
            block()
            trace.putAttribute("success", "true")
        } catch (e: Exception) {
            trace.putAttribute("success", "false")
            trace.putAttribute("error", e.message?.take(100) ?: "unknown")
        } finally {
            trace.stop()
        }
    }

    // ── Trace image loading ──
    fun traceImageLoad(imageUrl: String): Trace {
        val trace = performance.newTrace("image_load")
        trace.putAttribute("image_url_pattern", extractUrlPattern(imageUrl))
        trace.start()
        return trace
        // Caller calls trace.stop() when image is loaded
    }

    private fun extractUrlPattern(url: String): String {
        // Extract domain for grouping
        return try {
            java.net.URI(url).host ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun processItem(i: Int) { /* ... */ }
    private suspend fun fetchUsersFromFirestore(): List<User> = emptyList()
}
```

### 12.9.3 Network Request Monitoring

```kotlin
import com.google.firebase.perf.metrics.HttpMetric
import com.google.firebase.perf.FirebasePerformance

class NetworkPerformanceMonitor {

    // ── Custom HTTP metric (for non-standard HTTP clients) ──
    fun monitorCustomRequest(url: String, method: String) {
        val metric = FirebasePerformance.getInstance()
            .newHttpMetric(url, method) // "GET", "POST", etc.

        metric.start()

        try {
            // Make your HTTP request
            val response = makeHttpRequest(url, method)

            metric.setResponseContentType(response.contentType)
            metric.setHttpResponseCode(response.statusCode)
            metric.setResponsePayloadSize(response.bodySize)
            metric.setRequestPayloadSize(response.requestSize)

            // Custom attributes
            metric.putAttribute("api_version", "v2")
            metric.putAttribute("endpoint", extractEndpoint(url))

        } catch (e: Exception) {
            metric.putAttribute("error", e.javaClass.simpleName)
        } finally {
            metric.stop()
        }
    }

    // ── OkHttp Interceptor for automatic monitoring ──
    // Firebase automatically monitors OkHttp requests.
    // But you can add custom attributes via an interceptor:
    /*
    class PerformanceInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            // Firebase Perf automatically picks up OkHttp requests
            return chain.proceed(request)
        }
    }
    */

    // ── Disable performance monitoring (e.g., for debug builds) ──
    fun disablePerformanceMonitoring() {
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = false
    }

    private fun makeHttpRequest(url: String, method: String): HttpResponse {
        // placeholder
        return HttpResponse(200, "application/json", 1024, 256)
    }

    private fun extractEndpoint(url: String): String = url.substringAfterLast("/")

    data class HttpResponse(
        val statusCode: Int,
        val contentType: String,
        val bodySize: Long,
        val requestSize: Long
    )
}
```

---

## 12.10 Cloud Storage

### Theory

Firebase Cloud Storage is built for app developers who need to store and serve user-generated content, such as photos, videos, or other files. It is backed by Google Cloud Storage, providing secure file uploads and downloads with robust operations regardless of network quality. It supports resumable uploads, and you can set security rules to control access.

**Key Concepts:**
- **Bucket**: The top-level container for all your files (like a hard drive). Default bucket: `gs://your-project.appspot.com`. Blaze plan allows multiple buckets.
- **Reference**: A pointer to a location in the bucket — does NOT contain data. Think of it like a file path.
- **Upload Task**: An asynchronous, observable, controllable upload operation. Can be paused, resumed, or cancelled.
- **Resumable Upload**: If a network interruption occurs, the SDK resumes from where it stopped — doesn't re-upload the entire file.
- **Download URL**: A long-lived, publicly accessible HTTPS URL for a file. Contains an auth token in the URL. Revoked only when the file is deleted or security rules change.
- **Metadata**: Information about a file (size, content type, custom key-value pairs). Stored alongside the file.

**Storage vs Other Solutions:**
| Feature | Firebase Storage | Firebase RTDB | Firestore |
|---------|-----------------|--------------|-----------|
| Best for | Files (images, video, PDFs) | Small JSON data | Structured data |
| Max item | 5 TB per file | 16 MB per write | 1 MB per document |
| Pricing | $0.026/GB/month | Bandwidth-based | Read/write-based |
| CDN | Yes (via Google Cloud) | No | No |
| Offline | Queues ops | Full sync | Full sync |

**Upload Flow (resume-capable):**
```
┌──────────────────────────────────────────────────────────────┐
│                  UPLOAD LIFECYCLE                             │
│                                                              │
│  putFile(uri)                                                │
│    │                                                         │
│    ├─→ IN_PROGRESS ──→ onProgress(bytes / total)             │
│    │     │                                                   │
│    │     ├─→ pause() ──→ PAUSED                              │
│    │     │                 │                                  │
│    │     │                 └─→ resume() ──→ IN_PROGRESS       │
│    │     │                                                   │
│    │     ├─→ cancel() ──→ CANCELLED (cannot resume)          │
│    │     │                                                   │
│    │     ├─→ Network lost ──→ SDK retries automatically      │
│    │     │                                                   │
│    │     └─→ SUCCESS ──→ getDownloadUrl() ──→ HTTPS URL      │
│    │                                                         │
│    └─→ FAILURE ──→ StorageException with error code          │
│                                                              │
│  Error Codes:                                                │
│  • RETRY_LIMIT_EXCEEDED → Poor network, too many retries     │
│  • OBJECT_NOT_FOUND → File doesn't exist (wrong path)        │
│  • NOT_AUTHORIZED → Security rules rejection                 │
│  • QUOTA_EXCEEDED → Free plan quota used up                  │
│  • CANCELLED → cancel() was called                           │
└──────────────────────────────────────────────────────────────┘
```

### Cloud Storage Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                  CLOUD STORAGE ARCHITECTURE                   │
│                                                              │
│  ┌────────────┐     ┌────────────┐     ┌────────────────┐   │
│  │ Android    │     │ Firebase   │     │ Google Cloud   │   │
│  │ App        │────→│ Storage    │────→│ Storage        │   │
│  │            │     │ SDK        │     │ Bucket         │   │
│  └────────────┘     └────────────┘     └────────────────┘   │
│                                                              │
│  Storage Structure (like a file system):                     │
│  gs://your-app.appspot.com/                                  │
│  ├── users/                                                  │
│  │   ├── user_001/                                           │
│  │   │   ├── profile.jpg                                     │
│  │   │   └── documents/                                      │
│  │   │       ├── resume.pdf                                  │
│  │   │       └── cover_letter.pdf                            │
│  │   └── user_002/                                           │
│  │       └── profile.jpg                                     │
│  ├── posts/                                                  │
│  │   ├── post_001/                                           │
│  │   │   ├── image_1.jpg                                     │
│  │   │   └── image_2.jpg                                     │
│  │   └── post_002/                                           │
│  │       └── video.mp4                                       │
│  └── public/                                                 │
│      ├── app_icon.png                                        │
│      └── terms.pdf                                           │
│                                                              │
│  Key Features:                                               │
│  • Resumable uploads/downloads                               │
│  • Pause, resume, cancel operations                          │
│  • File metadata management                                  │
│  • Security rules for access control                         │
│  • Works offline (queues operations)                         │
│  • Scales automatically                                      │
└──────────────────────────────────────────────────────────────┘
```

### 12.10.1 File Upload

```kotlin
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.tasks.await

class CloudStorageManager {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    // ── Upload from file URI ──
    suspend fun uploadFile(
        localFileUri: Uri,
        storagePath: String
    ): Result<String> {
        return try {
            val fileRef = storageRef.child(storagePath)
            fileRef.putFile(localFileUri).await()
            val downloadUrl = fileRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Upload with progress tracking ──
    fun uploadWithProgress(
        localFileUri: Uri,
        storagePath: String,
        onProgress: (Int) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ): UploadTask {
        val fileRef = storageRef.child(storagePath)
        val uploadTask = fileRef.putFile(localFileUri)

        uploadTask
            .addOnProgressListener { snapshot ->
                val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                onProgress(progress)
            }
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }

        return uploadTask // Can be used to pause/resume/cancel
    }

    // ── Upload from byte array ──
    suspend fun uploadBytes(
        data: ByteArray,
        storagePath: String
    ): Result<String> {
        return try {
            val fileRef = storageRef.child(storagePath)
            fileRef.putBytes(data).await()
            val downloadUrl = fileRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Upload with metadata ──
    suspend fun uploadWithMetadata(
        localFileUri: Uri,
        storagePath: String,
        contentType: String
    ): Result<String> {
        return try {
            val metadata = storageMetadata {
                this.contentType = contentType
                setCustomMetadata("uploadedBy", FirebaseAuth.getInstance().currentUser?.uid ?: "")
                setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
            }

            val fileRef = storageRef.child(storagePath)
            fileRef.putFile(localFileUri, metadata).await()
            val downloadUrl = fileRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Pause, Resume, Cancel upload ──
    fun controlUpload(uploadTask: UploadTask) {
        uploadTask.pause()   // Pause the upload
        uploadTask.resume()  // Resume the upload
        uploadTask.cancel()  // Cancel the upload
    }
}
```

### 12.10.2 File Download

```kotlin
class CloudStorageDownloader {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    // ── Get download URL ──
    suspend fun getDownloadUrl(storagePath: String): Result<Uri> {
        return try {
            val url = storageRef.child(storagePath).downloadUrl.await()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Download to local file ──
    suspend fun downloadToFile(
        storagePath: String,
        localFile: File
    ): Result<File> {
        return try {
            storageRef.child(storagePath).getFile(localFile).await()
            Result.success(localFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Download as byte array (max 10MB recommended) ──
    suspend fun downloadAsBytes(
        storagePath: String,
        maxSize: Long = 10 * 1024 * 1024  // 10MB
    ): Result<ByteArray> {
        return try {
            val data = storageRef.child(storagePath).getBytes(maxSize).await()
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Download with progress ──
    fun downloadWithProgress(
        storagePath: String,
        localFile: File,
        onProgress: (Int) -> Unit,
        onSuccess: (File) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        storageRef.child(storagePath).getFile(localFile)
            .addOnProgressListener { snapshot ->
                val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                onProgress(progress)
            }
            .addOnSuccessListener {
                onSuccess(localFile)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    // ── Delete a file ──
    suspend fun deleteFile(storagePath: String): Result<Unit> {
        return try {
            storageRef.child(storagePath).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── List files in a directory ──
    suspend fun listFiles(directoryPath: String): Result<List<StorageReference>> {
        return try {
            val result = storageRef.child(directoryPath).listAll().await()
            Result.success(result.items) // Files
            // result.prefixes → Subdirectories
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 12.10.3 Security Rules

```javascript
// storage.rules
rules_version = '2';
service firebase.storage {
    match /b/{bucket}/o {

        // ── User profile images ──
        match /users/{userId}/profile.jpg {
            // Anyone can read profile images
            allow read: if true;
            // Only the user can upload their own profile image
            allow write: if request.auth != null
                && request.auth.uid == userId
                && request.resource.size < 5 * 1024 * 1024  // Max 5MB
                && request.resource.contentType.matches('image/.*');
        }

        // ── User documents (private) ──
        match /users/{userId}/documents/{document} {
            allow read, write: if request.auth != null
                && request.auth.uid == userId;
        }

        // ── Post images ──
        match /posts/{postId}/{imageFile} {
            // Authenticated users can read
            allow read: if request.auth != null;
            // Only post author can write (check via custom claim or Firestore)
            allow write: if request.auth != null
                && request.resource.size < 10 * 1024 * 1024  // Max 10MB
                && request.resource.contentType.matches('image/(png|jpeg|gif|webp)');
        }

        // ── Public files (read-only) ──
        match /public/{allPaths=**} {
            allow read: if true;
            allow write: if false; // Only admin via Admin SDK
        }
    }
}
```

### 12.10.4 Metadata Management

```kotlin
class StorageMetadataManager {
    private val storageRef = FirebaseStorage.getInstance().reference

    // ── Get file metadata ──
    suspend fun getMetadata(storagePath: String): Result<StorageMetadata> {
        return try {
            val metadata = storageRef.child(storagePath).metadata.await()
            // Access metadata properties:
            // metadata.name         → File name
            // metadata.path         → Full path
            // metadata.sizeBytes    → File size in bytes
            // metadata.contentType  → MIME type
            // metadata.creationTimeMillis → Creation time
            // metadata.updatedTimeMillis  → Last updated time
            // metadata.md5Hash     → MD5 hash
            // metadata.getCustomMetadata("key") → Custom metadata
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Update file metadata ──
    suspend fun updateMetadata(
        storagePath: String,
        contentType: String? = null,
        customMetadata: Map<String, String>? = null
    ): Result<StorageMetadata> {
        return try {
            val metadata = storageMetadata {
                contentType?.let { this.contentType = it }
                customMetadata?.forEach { (key, value) ->
                    setCustomMetadata(key, value)
                }
            }
            val updated = storageRef.child(storagePath).updateMetadata(metadata).await()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Complete Storage ViewModel Example

```kotlin
class StorageViewModel : ViewModel() {
    private val storageManager = CloudStorageManager()
    private val downloader = CloudStorageDownloader()

    private val _uploadProgress = MutableLiveData<Int>()
    val uploadProgress: LiveData<Int> = _uploadProgress

    private val _uploadResult = MutableLiveData<Result<String>>()
    val uploadResult: LiveData<Result<String>> = _uploadResult

    private var currentUploadTask: UploadTask? = null

    fun uploadProfileImage(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val path = "users/$userId/profile.jpg"

        currentUploadTask = storageManager.uploadWithProgress(
            localFileUri = imageUri,
            storagePath = path,
            onProgress = { progress ->
                _uploadProgress.postValue(progress)
            },
            onSuccess = { downloadUrl ->
                _uploadResult.postValue(Result.success(downloadUrl))
                // Update user profile with new image URL
                viewModelScope.launch {
                    FirebaseFirestore.getInstance()
                        .collection("users").document(userId)
                        .update("photoUrl", downloadUrl)
                }
            },
            onFailure = { e ->
                _uploadResult.postValue(Result.failure(e))
            }
        )
    }

    fun cancelUpload() {
        currentUploadTask?.cancel()
    }

    fun pauseUpload() {
        currentUploadTask?.pause()
    }

    fun resumeUpload() {
        currentUploadTask?.resume()
    }

    override fun onCleared() {
        super.onCleared()
        currentUploadTask?.cancel()
    }
}
```

---

## Summary: Firebase Services Quick Reference

```
┌──────────────────────┬─────────────────────────────────────────────────┬────────────┐
│ Service              │ Key Use Case                                    │ Free Tier  │
├──────────────────────┼─────────────────────────────────────────────────┼────────────┤
│ Authentication       │ User sign-in/sign-up (email, Google, phone)    │ Unlimited* │
│ Firestore            │ Structured data, complex queries, real-time    │ 50K reads  │
│ Realtime Database    │ Simple data, presence, chat, low latency       │ 100 conns  │
│ Cloud Functions      │ Serverless backend, event triggers             │ Blaze only │
│ FCM                  │ Push notifications, topic messaging            │ Unlimited  │
│ Analytics            │ Event tracking, user behavior, funnels         │ Unlimited  │
│ Crashlytics          │ Crash/error reporting, stability monitoring    │ Unlimited  │
│ Remote Config        │ Feature flags, A/B testing, dynamic config     │ Unlimited  │
│ Performance Monitor  │ App performance, network latency tracking      │ Unlimited  │
│ Cloud Storage        │ File uploads/downloads (images, videos, docs)  │ 5 GB       │
└──────────────────────┴─────────────────────────────────────────────────┴────────────┘
* Phone auth: Spark plan limited to 10K verifications/month
```

### When to Use Which Database?

```
┌──────────────────────────────────────────────────────────────────┐
│              DECISION GUIDE: FIRESTORE vs REALTIME DB            │
│                                                                  │
│  Use FIRESTORE when:              Use REALTIME DB when:         │
│  ─────────────────────            ──────────────────────        │
│  • Complex queries needed         • Simple key-value data        │
│  • Rich data structures           • Online presence/typing       │
│  • Multi-region required          • Ultra-low latency (< 10ms)   │
│  • Offline-first mobile app       • High-frequency small writes  │
│  • Collection group queries       • Broadcasting game state      │
│  • Strong consistency needed      • Budget: bandwidth-based      │
│                                                                  │
│  Use BOTH together:                                              │
│  • Firestore = main data (users, posts, orders)                 │
│  • Realtime DB = presence, typing indicators, live cursors       │
└──────────────────────────────────────────────────────────────────┘
```

---

## Key Best Practices

### Setup & Configuration
1. **Always use Firebase BoM** to manage library versions consistently — prevents dependency conflicts
2. **Never commit `google-services.json`** to public repos — add it to `.gitignore`
3. **Use Firebase Emulator Suite** for local development — no cloud charges, reproducible tests
4. **Initialize Firebase in `Application.onCreate()`** — ensures SDKs are ready before any Activity

### Security
5. **Never ship with open security rules** — `allow read, write: if true` is for development only
6. **Validate data in security rules** — check types, sizes, required fields, `request.auth.uid`
7. **Use custom claims** for role-based access (admin, editor) instead of storing roles in Firestore
8. **Re-authenticate users** before sensitive operations (password change, account deletion)
9. **Follow the principle of least privilege** — grant minimum permissions needed

### Performance & Cost
10. **Remove listeners** in `onCleared()` / `onDestroy()` to prevent memory leaks and unnecessary reads
11. **Use `kotlinx.coroutines.tasks.await()`** to convert Firebase Tasks to clean coroutine code
12. **Prefer Kotlin Flow** (via `callbackFlow`) over raw listeners for reactive, lifecycle-aware data
13. **Use `.select()` queries** to return only needed fields — reduces bandwidth and read costs
14. **Enable offline persistence** (Firestore: default on, RTDB: call `setPersistenceEnabled(true)`)
15. **Set cache limits** to prevent unbounded local storage growth

### Monitoring & Quality
16. **Set up Crashlytics** with custom keys and logs for meaningful crash context
17. **Track non-fatal exceptions** — API failures, parsing errors, validation failures
18. **Use Remote Config** for feature flags instead of hard-coded booleans
19. **Set performance budgets** — monitor cold start < 2s, API P95 < 1s
20. **Use Analytics DebugView** during development: `adb shell setprop debug.firebase.analytics.app PACKAGE_NAME`

### Architecture
```
┌──────────────────────────────────────────────────────────────────┐
│        RECOMMENDED ARCHITECTURE WITH FIREBASE                    │
│                                                                  │
│  UI Layer (Activity/Fragment/Compose)                            │
│     │   observes StateFlow/LiveData                             │
│     ▼                                                            │
│  ViewModel Layer                                                 │
│     │   calls suspend functions                                 │
│     ▼                                                            │
│  Repository Layer (abstracts data sources)                       │
│     │                                                            │
│     ├── FirebaseAuthRepository                                   │
│     ├── FirestoreRepository                                      │
│     ├── StorageRepository                                        │
│     └── RemoteConfigRepository                                   │
│          │                                                       │
│          ▼                                                       │
│  Firebase SDK Layer (direct SDK calls)                           │
│     │                                                            │
│     ├── Local Cache (automatic offline persistence)              │
│     └── Cloud Services (Firebase servers)                        │
│                                                                  │
│  Dependency Injection (Hilt/Koin):                               │
│  • Provide Firebase instances as singletons                     │
│  • Swap to emulator instances in tests                          │
│  • Mock repositories for unit tests                             │
└──────────────────────────────────────────────────────────────────┘
```
