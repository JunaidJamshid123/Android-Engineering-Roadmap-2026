# In-App Billing (Google Play Billing)

## Overview

Google Play Billing Library lets you sell digital content (subscriptions, one-time purchases) within your Android app. All digital goods sold in apps distributed on Google Play **must** use this library.

```
┌──────────────────────────────────────────────────────────────┐
│                In-App Billing Architecture                     │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐      ┌──────────────────┐                  │
│  │   Your App   │      │  Google Play     │                  │
│  │              │◄────▶│  Billing Service │                  │
│  │  BillingClient│      │  (on device)     │                  │
│  └──────┬───────┘      └────────┬─────────┘                  │
│         │                       │                             │
│         │                       ▼                             │
│         │              ┌──────────────────┐                  │
│         │              │  Google Play     │                  │
│         │              │  Backend Servers │                  │
│         │              └────────┬─────────┘                  │
│         │                       │                             │
│         ▼                       ▼                             │
│  ┌──────────────┐      ┌──────────────────┐                  │
│  │  Your Server │◄────▶│  Google Play     │                  │
│  │  (verify     │  API │  Developer API   │                  │
│  │   purchases) │      │  (REST)          │                  │
│  └──────────────┘      └──────────────────┘                  │
│                                                               │
│  Product Types:                                               │
│  ┌────────────────────────────────────────────────────┐      │
│  │ INAPP (One-time)          │ SUBS (Subscriptions)   │      │
│  │ ┌──────────┐ ┌─────────┐ │ ┌──────────────────┐  │      │
│  │ │Consumable│ │Non-Con. │ │ │ Auto-renewing    │  │      │
│  │ │(coins,   │ │(premium │ │ │ (monthly/yearly) │  │      │
│  │ │ gems)    │ │ unlock) │ │ │                  │  │      │
│  │ │ Buy many │ │ Buy once│ │ │ Prepaid          │  │      │
│  │ │ times    │ │         │ │ │ (fixed period)   │  │      │
│  │ └──────────┘ └─────────┘ │ └──────────────────┘  │      │
│  └────────────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────────────┘
```

---

## Setup

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.android.billingclient:billing-ktx:7.0.0")
}
```

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="com.android.vending.BILLING" />
```

---

## 1. Google Play Billing Library — Complete Implementation

### BillingManager Class

```kotlin
class BillingManager(
    private val activity: Activity,
    private val onPurchaseResult: (Purchase) -> Unit,
    private val onError: (String) -> Unit
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null

    // Product IDs (must match Google Play Console)
    companion object {
        // One-time products
        const val PRODUCT_PREMIUM = "premium_upgrade"
        const val PRODUCT_COINS_100 = "coins_100"
        const val PRODUCT_COINS_500 = "coins_500"

        // Subscriptions
        const val SUB_MONTHLY = "sub_monthly"
        const val SUB_YEARLY = "sub_yearly"
    }

    // ──────────────────────────────────────────
    // 1. CONNECT to Google Play
    // ──────────────────────────────────────────
    fun startConnection() {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(this)  // PurchasesUpdatedListener
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Connection established — query products
                    queryProducts()
                    querySubscriptions()
                } else {
                    onError("Billing setup failed: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection
                startConnection()
            }
        })
    }

    // ──────────────────────────────────────────
    // 2. QUERY available products
    // ──────────────────────────────────────────
    private var productDetailsList: List<ProductDetails> = emptyList()
    private var subDetailsList: List<ProductDetails> = emptyList()

    private fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_PREMIUM)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_COINS_100)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_COINS_500)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetails ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList = productDetails
                // Update UI with product info (name, price, description)
            }
        }
    }

    private fun querySubscriptions() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SUB_MONTHLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SUB_YEARLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetails ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                subDetailsList = productDetails
            }
        }
    }

    // ──────────────────────────────────────────
    // 3. LAUNCH purchase flow
    // ──────────────────────────────────────────
    fun purchaseProduct(productId: String) {
        val productDetails = productDetailsList.find {
            it.productId == productId
        } ?: run {
            onError("Product not found: $productId")
            return
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        billingClient?.launchBillingFlow(activity, flowParams)
    }

    fun purchaseSubscription(productId: String, offerToken: String) {
        val productDetails = subDetailsList.find {
            it.productId == productId
        } ?: return

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken) // Required for subscriptions
                        .build()
                )
            )
            .build()

        billingClient?.launchBillingFlow(activity, flowParams)
    }

    // ──────────────────────────────────────────
    // 4. HANDLE purchase result
    // ──────────────────────────────────────────
    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // User cancelled — do nothing
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // Already purchased — restore it
                queryExistingPurchases()
            }
            else -> {
                onError("Purchase failed: ${result.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        // IMPORTANT: Verify purchase state
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return // PENDING purchases should wait
        }

        // IMPORTANT: Verify on your server before granting entitlement
        // verifyPurchaseOnServer(purchase.purchaseToken)

        // After verification, acknowledge the purchase
        if (!purchase.isAcknowledged) {
            acknowledgePurchase(purchase)
        }

        onPurchaseResult(purchase)
    }

    // ──────────────────────────────────────────
    // 5. ACKNOWLEDGE purchases (REQUIRED within 3 days!)
    // ──────────────────────────────────────────
    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Purchase acknowledged successfully
            }
        }
    }

    // ──────────────────────────────────────────
    // 6. CONSUME consumable purchases (coins, gems)
    // ──────────────────────────────────────────
    private fun consumePurchase(purchase: Purchase) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.consumeAsync(params) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Product consumed — user can buy again
            }
        }
    }

    // ──────────────────────────────────────────
    // 7. RESTORE / QUERY existing purchases
    // ──────────────────────────────────────────
    fun queryExistingPurchases() {
        // Query one-time purchases
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(inAppParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    // Restore entitlements
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        onPurchaseResult(purchase)
                    }
                }
            }
        }

        // Query subscriptions
        val subParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient?.queryPurchasesAsync(subParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        onPurchaseResult(purchase)
                    }
                }
            }
        }
    }

    fun endConnection() {
        billingClient?.endConnection()
    }
}
```

---

## 2. Subscriptions Management

```
┌──────────────────────────────────────────────────────────────┐
│              Subscription Lifecycle                            │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  User subscribes                                              │
│       │                                                       │
│       ▼                                                       │
│  ┌──────────────┐                                            │
│  │   ACTIVE     │◄──── Renewal (auto)                        │
│  │  (entitled)  │                                            │
│  └──────┬───────┘                                            │
│         │                                                     │
│    ┌────┴────────┬────────────┐                               │
│    ▼             ▼            ▼                               │
│  ┌──────┐  ┌─────────┐  ┌──────────┐                        │
│  │Cancel│  │ Payment │  │ Upgrade/ │                        │
│  │      │  │ Failed  │  │ Downgrade│                        │
│  └──┬───┘  └────┬────┘  └──────────┘                        │
│     │           │                                             │
│     ▼           ▼                                             │
│  ┌───────┐  ┌──────────┐                                     │
│  │ GRACE │  │ ON_HOLD  │  (Account hold — no access)         │
│  │PERIOD │  │(retry    │                                     │
│  │(still │  │ payment) │                                     │
│  │active)│  └────┬─────┘                                     │
│  └───┬───┘       │                                            │
│      │      ┌────┴────┐                                      │
│      ▼      ▼         ▼                                      │
│  ┌──────┐ ┌──────┐ ┌────────┐                               │
│  │PAUSED│ │RESUME│ │EXPIRED │                               │
│  │      │ │      │ │(no     │                               │
│  └──────┘ └──────┘ │access) │                               │
│                     └────────┘                               │
└──────────────────────────────────────────────────────────────┘
```

### Subscription Offers and Pricing

```kotlin
fun getSubscriptionOffers(productDetails: ProductDetails): List<SubscriptionOffer> {
    val offers = mutableListOf<SubscriptionOffer>()

    productDetails.subscriptionOfferDetails?.forEach { offerDetails ->
        val offer = SubscriptionOffer(
            offerId = offerDetails.offerId ?: "base",
            offerToken = offerDetails.offerToken,
            basePlanId = offerDetails.basePlanId,
            pricingPhases = offerDetails.pricingPhases.pricingPhaseList.map { phase ->
                PricingPhase(
                    formattedPrice = phase.formattedPrice,        // "$9.99"
                    priceAmountMicros = phase.priceAmountMicros,  // 9990000
                    priceCurrencyCode = phase.priceCurrencyCode,  // "USD"
                    billingPeriod = phase.billingPeriod,          // "P1M" (1 month)
                    billingCycleCount = phase.billingCycleCount,  // 0 = infinite
                    recurrenceMode = phase.recurrenceMode
                    // FINITE_RECURRING (trial/intro), INFINITE_RECURRING (base), NON_RECURRING
                )
            }
        )
        offers.add(offer)
    }

    return offers
}

data class SubscriptionOffer(
    val offerId: String,
    val offerToken: String,
    val basePlanId: String,
    val pricingPhases: List<PricingPhase>
)

data class PricingPhase(
    val formattedPrice: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val billingPeriod: String,
    val billingCycleCount: Int,
    val recurrenceMode: Int
)

// Display subscription with trial info
@Composable
fun SubscriptionCard(offer: SubscriptionOffer) {
    val phases = offer.pricingPhases

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            // Check if there's a free trial phase
            val trialPhase = phases.firstOrNull {
                it.priceAmountMicros == 0L
            }
            val basePhase = phases.lastOrNull()

            if (trialPhase != null) {
                Text(
                    "Free trial: ${parseBillingPeriod(trialPhase.billingPeriod)}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            basePhase?.let {
                Text(
                    "${it.formattedPrice} / ${parseBillingPeriod(it.billingPeriod)}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Button(onClick = { /* start purchase with offer.offerToken */ }) {
                Text("Subscribe")
            }
        }
    }
}

fun parseBillingPeriod(period: String): String = when (period) {
    "P1W" -> "week"
    "P1M" -> "month"
    "P3M" -> "3 months"
    "P6M" -> "6 months"
    "P1Y" -> "year"
    else -> period
}
```

---

## 3. Consumable vs Non-Consumable Products

```
┌──────────────────────────────────────────────────────────────┐
│         Consumable vs Non-Consumable Products                 │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  CONSUMABLE                        NON-CONSUMABLE             │
│  ┌───────────────────┐            ┌───────────────────┐      │
│  │ • Virtual coins    │            │ • Premium unlock   │      │
│  │ • Extra lives      │            │ • Ad removal       │      │
│  │ • Boost items      │            │ • Full version     │      │
│  │ • Temporary buffs  │            │ • Level pack       │      │
│  │                    │            │ • Theme pack       │      │
│  │ After purchase:    │            │                    │      │
│  │ → consumeAsync()   │            │ After purchase:    │      │
│  │ → User can buy     │            │ → acknowledgePurchase()  │
│  │   again            │            │ → User owns forever│      │
│  └───────────────────┘            └───────────────────┘      │
│                                                               │
│  Code Flow:                                                   │
│                                                               │
│  Consumable:   Purchase → Verify → Consume → Grant items     │
│  Non-consumable: Purchase → Verify → Acknowledge → Unlock    │
│  Subscription: Purchase → Verify → Acknowledge → Entitle     │
└──────────────────────────────────────────────────────────────┘
```

### Handling Both Types

```kotlin
class PurchaseHandler(private val billingManager: BillingManager) {

    // Define which products are consumable
    private val consumableProducts = setOf(
        BillingManager.PRODUCT_COINS_100,
        BillingManager.PRODUCT_COINS_500
    )

    fun handleVerifiedPurchase(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return

        if (productId in consumableProducts) {
            // Consumable: consume it so user can buy again
            billingManager.consumePurchase(purchase)

            // Grant the items
            when (productId) {
                BillingManager.PRODUCT_COINS_100 -> grantCoins(100)
                BillingManager.PRODUCT_COINS_500 -> grantCoins(500)
            }
        } else {
            // Non-consumable or subscription: acknowledge
            if (!purchase.isAcknowledged) {
                billingManager.acknowledgePurchase(purchase)
            }

            // Unlock feature
            when (productId) {
                BillingManager.PRODUCT_PREMIUM -> unlockPremium()
                BillingManager.SUB_MONTHLY,
                BillingManager.SUB_YEARLY -> activateSubscription()
            }
        }
    }

    private fun grantCoins(amount: Int) { /* Update local + server balance */ }
    private fun unlockPremium() { /* Save entitlement */ }
    private fun activateSubscription() { /* Enable sub features */ }
}
```

---

## 4. Server-Side Verification

**Never trust the client alone.** Always verify purchases on your backend server.

```
┌──────────────────────────────────────────────────────────────┐
│              Server-Side Verification Flow                     │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────┐    ┌───────────┐    ┌─────────────────┐        │
│  │  App     │    │ Your      │    │ Google Play     │        │
│  │  Client  │    │ Server    │    │ Developer API   │        │
│  └────┬─────┘    └─────┬─────┘    └────────┬────────┘        │
│       │                │                    │                 │
│  1.   │──Purchase──▶   │                    │                 │
│       │  Token sent    │                    │                 │
│       │                │                    │                 │
│  2.   │                │──Verify Token──▶   │                 │
│       │                │  GET /purchases/   │                 │
│       │                │  products/v2/...   │                 │
│       │                │                    │                 │
│  3.   │                │◀──Purchase Info──  │                 │
│       │                │  (valid/invalid,   │                 │
│       │                │   order details)   │                 │
│       │                │                    │                 │
│  4.   │◀──Grant/Deny── │                    │                 │
│       │  entitlement   │                    │                 │
│       │                │                    │                 │
│  ⚠️ WHY server verify?                                       │
│  • Prevents tampered purchase tokens                         │
│  • Prevents replay attacks                                   │
│  • Detects refunded purchases                                │
│  • Required for subscription status tracking                 │
└──────────────────────────────────────────────────────────────┘
```

### Client Side: Send Token to Server

```kotlin
private fun verifyPurchaseOnServer(purchase: Purchase) {
    val requestBody = PurchaseVerificationRequest(
        purchaseToken = purchase.purchaseToken,
        productId = purchase.products.first(),
        packageName = activity.packageName,
        orderId = purchase.orderId ?: ""
    )

    // Send to your backend
    lifecycleScope.launch {
        try {
            val response = apiService.verifyPurchase(requestBody)
            if (response.isValid) {
                // Server confirmed — grant entitlement
                handleVerifiedPurchase(purchase)
            } else {
                // Invalid purchase — don't grant
                onError("Purchase verification failed")
            }
        } catch (e: Exception) {
            // Network error — queue for later verification
            savePendingVerification(purchase)
        }
    }
}

@Serializable
data class PurchaseVerificationRequest(
    val purchaseToken: String,
    val productId: String,
    val packageName: String,
    val orderId: String
)
```

### Server Side (Kotlin/Ktor example)

```kotlin
// Server-side verification using Google Play Developer API

// Verify one-time purchase
suspend fun verifyInAppPurchase(
    packageName: String,
    productId: String,
    purchaseToken: String
): PurchaseVerificationResult {
    val url = "https://androidpublisher.googleapis.com/androidpublisher/v3" +
              "/applications/$packageName/purchases/products/$productId" +
              "/tokens/$purchaseToken"

    val response = httpClient.get(url) {
        header("Authorization", "Bearer ${getAccessToken()}")
    }

    val purchaseInfo = response.body<GooglePurchaseResponse>()

    return PurchaseVerificationResult(
        isValid = purchaseInfo.purchaseState == 0,  // 0 = Purchased
        orderId = purchaseInfo.orderId,
        purchaseTime = purchaseInfo.purchaseTimeMillis
    )
}

// Verify subscription
suspend fun verifySubscription(
    packageName: String,
    subscriptionId: String,
    purchaseToken: String
): SubscriptionVerificationResult {
    val url = "https://androidpublisher.googleapis.com/androidpublisher/v3" +
              "/applications/$packageName/purchases/subscriptions" +
              "/$subscriptionId/tokens/$purchaseToken"

    val response = httpClient.get(url) {
        header("Authorization", "Bearer ${getAccessToken()}")
    }

    val subInfo = response.body<GoogleSubscriptionResponse>()

    return SubscriptionVerificationResult(
        isActive = subInfo.expiryTimeMillis > System.currentTimeMillis(),
        expiryTime = subInfo.expiryTimeMillis,
        autoRenewing = subInfo.autoRenewing,
        cancelReason = subInfo.cancelReason
    )
}
```

### Real-Time Developer Notifications (RTDN)

```
┌──────────────────────────────────────────────────────────────┐
│  Google Play sends push notifications to your server for:    │
│                                                               │
│  • New purchases / renewals                                   │
│  • Cancellations                                              │
│  • Refunds                                                    │
│  • Account holds                                              │
│  • Grace period entries                                       │
│  • Subscription pauses/resumes                               │
│  • Price change confirmations                                │
│                                                               │
│  Setup: Pub/Sub topic → Cloud Function → Your server         │
└──────────────────────────────────────────────────────────────┘
```

---

## Billing Best Practices

```
┌──────────────────────────────────────────────────────────────┐
│              Billing Checklist                                │
├──────────────────────────────────────────────────────────────┤
│  □ Always verify purchases server-side                       │
│  □ Acknowledge purchases within 3 days (or auto-refund!)     │
│  □ Consume consumables so users can rebuy                    │
│  □ Handle PENDING purchases (wait for PURCHASED state)       │
│  □ Query existing purchases on every app start (restore)     │
│  □ Handle BillingServiceDisconnected (retry connection)      │
│  □ Show prices from ProductDetails (locale-formatted)        │
│  □ Never hardcode prices in your UI                          │
│  □ Test with license test accounts                           │
│  □ Handle subscription grace periods and account holds       │
│  □ Set up RTDN for server-side subscription tracking         │
│  □ Cache entitlements locally for offline access              │
└──────────────────────────────────────────────────────────────┘
```
