# App Links and Deep Linking

## Overview

Deep linking allows external URLs to open specific screens inside your app. Android supports three levels of deep links, from basic to fully verified.

```
┌──────────────────────────────────────────────────────────────┐
│              Deep Linking Types Comparison                     │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Type              │ Verification │ Fallback  │ Prompt       │
│  ──────────────────┼──────────────┼───────────┼──────────    │
│  Deep Links        │ None         │ None      │ Chooser      │
│  (scheme://path)   │              │           │ dialog       │
│                    │              │           │              │
│  Web Links         │ None         │ Browser   │ Chooser      │
│  (http/https)      │              │           │ dialog       │
│                    │              │           │              │
│  App Links ✅      │ Verified     │ Browser   │ NO dialog    │
│  (https +          │ (Digital     │           │ (opens app   │
│   verified)        │  Asset       │           │  directly)   │
│                    │  Links)      │           │              │
│                                                               │
│  URL Journey:                                                 │
│                                                               │
│  User clicks: https://example.com/product/123                │
│       │                                                       │
│       ▼                                                       │
│  ┌─────────────────────────────────┐                         │
│  │ Is the app installed?           │                         │
│  └──────────┬──────────────────────┘                         │
│        YES  │           NO                                    │
│       ┌─────▼─────┐  ┌─────▼──────────┐                     │
│       │App Link   │  │ Open in        │                      │
│       │verified?  │  │ browser        │                      │
│       └──┬────┬───┘  │ (web fallback) │                      │
│      YES │    │ NO   └────────────────┘                      │
│     ┌────▼┐ ┌─▼─────────┐                                   │
│     │Open │ │Show chooser│                                   │
│     │app  │ │(app or     │                                   │
│     │     │ │ browser)   │                                   │
│     └─────┘ └───────────┘                                    │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. Android App Links Verification

App Links use **HTTPS** URLs with **Digital Asset Links** verification so the system opens your app directly (no disambiguation dialog).

### Step 1: Add Intent Filters in Manifest

```xml
<!-- AndroidManifest.xml -->
<activity
    android:name=".ui.DeepLinkActivity"
    android:exported="true">

    <!-- App Link: verified HTTPS link (auto-opens app) -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:scheme="https"
            android:host="www.example.com"
            android:pathPrefix="/product" />
    </intent-filter>

    <!-- Support multiple paths -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:scheme="https"
            android:host="www.example.com"
            android:pathPrefix="/order" />
        <data
            android:scheme="https"
            android:host="www.example.com"
            android:pathPrefix="/user" />
    </intent-filter>

    <!-- Custom scheme deep link (non-verified, shows chooser) -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:scheme="myapp"
            android:host="product" />
        <!-- Handles: myapp://product/123 -->
    </intent-filter>
</activity>
```

### Step 2: Host Digital Asset Links File

Place this JSON file at: `https://www.example.com/.well-known/assetlinks.json`

```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.example.myapp",
      "sha256_cert_fingerprints": [
        "AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90"
      ]
    }
  }
]
```

```bash
# Get your app's SHA-256 fingerprint:
# Debug:
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android

# Release:
keytool -list -v -keystore your-release-key.keystore -alias your-alias
```

### Step 3: Handle Deep Links in Activity

```kotlin
class DeepLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val uri = intent.data ?: return
        val action = intent.action

        if (action == Intent.ACTION_VIEW && uri != null) {
            when {
                // https://www.example.com/product/123
                uri.pathSegments.firstOrNull() == "product" -> {
                    val productId = uri.pathSegments.getOrNull(1)
                    productId?.let { navigateToProduct(it) }
                }

                // https://www.example.com/order/456
                uri.pathSegments.firstOrNull() == "order" -> {
                    val orderId = uri.pathSegments.getOrNull(1)
                    orderId?.let { navigateToOrder(it) }
                }

                // https://www.example.com/user/profile
                uri.pathSegments.firstOrNull() == "user" -> {
                    navigateToUserProfile()
                }

                // myapp://product/789
                uri.scheme == "myapp" && uri.host == "product" -> {
                    val productId = uri.pathSegments.firstOrNull()
                    productId?.let { navigateToProduct(it) }
                }

                else -> navigateToHome()
            }
        }
    }

    private fun navigateToProduct(id: String) {
        // Navigate to product detail screen
    }

    private fun navigateToOrder(id: String) { /* ... */ }
    private fun navigateToUserProfile() { /* ... */ }
    private fun navigateToHome() { /* ... */ }
}
```

### Deep Links with Navigation Component

```kotlin
// nav_graph.xml
// <fragment
//     android:id="@+id/productFragment"
//     android:name=".ui.ProductFragment">
//     <deepLink app:uri="https://www.example.com/product/{productId}" />
//     <argument android:name="productId" app:argType="string" />
// </fragment>

// Compose Navigation with deep links
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen() }

        composable(
            route = "product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://www.example.com/product/{productId}"
                },
                navDeepLink {
                    uriPattern = "myapp://product/{productId}"
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductScreen(productId = productId)
        }

        composable(
            route = "order/{orderId}",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://www.example.com/order/{orderId}"
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            OrderScreen(orderId = orderId)
        }
    }
}
```

---

## 2. Universal Links (Cross-Platform Concept)

```
┌──────────────────────────────────────────────────────────────┐
│         Universal Links: Android vs iOS                       │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Concept         │ Android              │ iOS                 │
│  ────────────────┼──────────────────────┼──────────────       │
│  Name            │ App Links            │ Universal Links     │
│  Verification    │ assetlinks.json      │ apple-app-site-     │
│                  │ (/.well-known/)      │ association         │
│  Scheme          │ https only           │ https only          │
│  Auto-verify     │ android:autoVerify   │ Automatic on        │
│                  │ ="true"              │ install             │
│  Manifest entry  │ <intent-filter>      │ Entitlements +      │
│                  │                      │ Associated Domains  │
│                                                               │
│  Both require hosting a verification file on your server.    │
│  Both eliminate the "open with" chooser dialog.              │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Deferred Deep Linking

Deferred deep linking routes users to specific content **even if the app isn't installed yet**. The user goes: Link → Play Store → Install → App opens at the right screen.

```
┌──────────────────────────────────────────────────────────────┐
│              Deferred Deep Linking Flow                        │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  User sees link: https://example.com/product/123             │
│       │                                                       │
│       ▼                                                       │
│  ┌──────────────────┐                                        │
│  │ App installed?    │                                        │
│  └──┬──────────┬────┘                                        │
│ YES │          │ NO                                           │
│     ▼          ▼                                              │
│  Opens app   Goes to Play Store                               │
│  at product  ┌──────────────────┐                            │
│  /123        │ Install app      │                            │
│              └────────┬─────────┘                            │
│                       ▼                                       │
│              ┌──────────────────┐                            │
│              │ First launch:    │                            │
│              │ check deferred   │                            │
│              │ deep link        │                            │
│              └────────┬─────────┘                            │
│                       ▼                                       │
│              Opens app at product/123 ✅                      │
│              (link was "remembered")                          │
│                                                               │
│  HOW the link is "remembered":                               │
│  • Clipboard (deprecated/unreliable)                         │
│  • Device fingerprinting (IP + user agent)                   │
│  • Play Install Referrer API (Google Play)                   │
│  • Third-party SDKs (Branch, AppsFlyer)                     │
└──────────────────────────────────────────────────────────────┘
```

### Play Install Referrer (Google's approach)

```kotlin
// build.gradle.kts
// implementation("com.android.installreferrer:installreferrer:2.2")

class DeferredDeepLinkHandler(private val context: Context) {

    fun checkDeferredDeepLink(onResult: (String?) -> Unit) {
        val client = InstallReferrerClient.newBuilder(context).build()

        client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    val referrerDetails = client.installReferrer
                    val referrerUrl = referrerDetails.installReferrer
                    // referrerUrl might contain: "utm_source=email&deep_link=product/123"
                    val deepLink = extractDeepLink(referrerUrl)
                    onResult(deepLink)
                    client.endConnection()
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                onResult(null)
            }
        })
    }

    private fun extractDeepLink(referrer: String): String? {
        val params = Uri.parse("https://example.com?$referrer")
        return params.getQueryParameter("deep_link")
    }
}
```

---

## 4. Branch.io and Firebase Dynamic Links

### Branch.io Integration

```kotlin
// build.gradle.kts
// implementation("io.branch.sdk.android:library:5.+")

// Application class
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Branch
        Branch.getAutoInstance(this)
    }
}

// Activity — handle Branch deep links
class MainActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()

        // Initialize Branch session
        Branch.sessionBuilder(this)
            .withCallback { branchUniversalObject, linkProperties, error ->
                if (error == null && branchUniversalObject != null) {
                    // Deep link data is available
                    val productId = branchUniversalObject
                        .contentMetadata
                        .customMetadata["product_id"]

                    productId?.let {
                        navigateToProduct(it)
                    }
                }
            }
            .withData(this.intent?.data)
            .init()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        // Re-initialize with new intent data
        Branch.sessionBuilder(this)
            .withCallback { buo, lp, error -> /* handle */ }
            .reInit()
    }

    // Create a Branch link to share
    private fun createShareLink(productId: String, productName: String) {
        val buo = BranchUniversalObject()
            .setCanonicalIdentifier("product/$productId")
            .setTitle(productName)
            .setContentDescription("Check out $productName!")
            .setContentMetadata(
                ContentMetadata().addCustomMetadata("product_id", productId)
            )

        val linkProperties = LinkProperties()
            .setChannel("app_share")
            .setFeature("sharing")
            .addControlParameter("\$desktop_url", "https://example.com/product/$productId")
            .addControlParameter("\$android_url", "https://example.com/product/$productId")

        buo.generateShortUrl(this, linkProperties) { url, error ->
            if (error == null) {
                // Share the URL: https://example.app.link/abc123
                shareUrl(url)
            }
        }
    }
}
```

### Firebase Dynamic Links (Deprecated — Use App Links Instead)

```
┌──────────────────────────────────────────────────────────────┐
│  ⚠️ Firebase Dynamic Links was DEPRECATED on August 25, 2025 │
│                                                               │
│  Recommended alternatives:                                    │
│  • Android App Links (native, free, no SDK needed)           │
│  • Branch.io (full-featured deferred deep linking)           │
│  • AppsFlyer (attribution + deep linking)                    │
│  • Adjust (attribution + deep linking)                       │
└──────────────────────────────────────────────────────────────┘
```

---

## 5. Testing Deep Links

```bash
# Test via ADB
adb shell am start -a android.intent.action.VIEW \
    -d "https://www.example.com/product/123" \
    com.example.myapp

# Test custom scheme
adb shell am start -a android.intent.action.VIEW \
    -d "myapp://product/456"

# Verify App Links status
adb shell pm get-app-links com.example.myapp

# Reset App Links verification
adb shell pm set-app-links --package com.example.myapp 0 all
adb shell pm verify-app-links --re-verify com.example.myapp
```

---

## Deep Linking Best Practices

```
┌──────────────────────────────────────────────────────────────┐
│              Deep Linking Checklist                            │
├──────────────────────────────────────────────────────────────┤
│  □ Use HTTPS App Links (not custom schemes) for production   │
│  □ Host assetlinks.json with correct SHA-256 fingerprint     │
│  □ Handle both onCreate AND onNewIntent                      │
│  □ Validate/sanitize all URI parameters                      │
│  □ Provide fallback if deep link target doesn't exist        │
│  □ Log deep link events for analytics                        │
│  □ Test with ADB commands before release                     │
│  □ Test with app not installed (deferred deep links)         │
│  □ Test with app in background (onNewIntent path)            │
│  □ Support both www and non-www domains                      │
└──────────────────────────────────────────────────────────────┘
```
