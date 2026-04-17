# Internationalization (i18n) & Localization (l10n)

## Overview

Internationalization (i18n) is designing your app to support multiple languages and regions **without code changes**. Localization (l10n) is the actual translation and adaptation for a specific locale.

```
┌──────────────────────────────────────────────────────────────┐
│              i18n / l10n Architecture                          │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  App Code (Locale-independent)                                │
│  ┌──────────────────────────────────────────┐                │
│  │  getString(R.string.welcome_message)     │                │
│  │  // Never hardcode strings!              │                │
│  └────────────────────┬─────────────────────┘                │
│                       │                                       │
│                       ▼                                       │
│  Android Resource System (picks best match)                   │
│  ┌──────────────────────────────────────────┐                │
│  │  Device Locale: fr-FR                    │                │
│  │                                          │                │
│  │  Search Order:                           │                │
│  │  1. values-fr-rFR/  (exact match)        │                │
│  │  2. values-fr/      (language match)     │                │
│  │  3. values/         (default fallback)   │                │
│  └──────────────────────────────────────────┘                │
│                                                               │
│  Resource Folders:                                            │
│  res/                                                         │
│  ├── values/                 ← Default (English)             │
│  │   └── strings.xml                                         │
│  ├── values-fr/              ← French                        │
│  │   └── strings.xml                                         │
│  ├── values-de/              ← German                        │
│  │   └── strings.xml                                         │
│  ├── values-ar/              ← Arabic (RTL)                  │
│  │   └── strings.xml                                         │
│  ├── values-ja/              ← Japanese                      │
│  │   └── strings.xml                                         │
│  ├── values-zh-rCN/          ← Chinese (Simplified)         │
│  │   └── strings.xml                                         │
│  ├── values-es-rMX/          ← Spanish (Mexico)             │
│  │   └── strings.xml                                         │
│  └── values-pt-rBR/          ← Portuguese (Brazil)          │
│      └── strings.xml                                         │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. String Resources and Localization

### Default Strings (`res/values/strings.xml`)

```xml
<!-- res/values/strings.xml (English — Default) -->
<resources>
    <string name="app_name">My Shop</string>
    <string name="welcome_message">Welcome back, %1$s!</string>
    <string name="item_count">You have %1$d items in your cart</string>
    <string name="price_label">Price: %1$s</string>
    <string name="login_button">Log In</string>
    <string name="logout_button">Log Out</string>
    <string name="search_hint">Search products…</string>
    <string name="error_network">Unable to connect. Please check your internet.</string>
    <string name="empty_cart">Your cart is empty</string>
    <string name="confirm_delete">Are you sure you want to delete \"%1$s\"?</string>

    <!-- String arrays -->
    <string-array name="sort_options">
        <item>Price: Low to High</item>
        <item>Price: High to Low</item>
        <item>Newest First</item>
        <item>Best Rating</item>
    </string-array>
</resources>
```

### French Translation (`res/values-fr/strings.xml`)

```xml
<!-- res/values-fr/strings.xml -->
<resources>
    <string name="app_name">Ma Boutique</string>
    <string name="welcome_message">Bon retour, %1$s !</string>
    <string name="item_count">Vous avez %1$d articles dans votre panier</string>
    <string name="price_label">Prix : %1$s</string>
    <string name="login_button">Se connecter</string>
    <string name="logout_button">Se déconnecter</string>
    <string name="search_hint">Rechercher des produits…</string>
    <string name="error_network">Connexion impossible. Vérifiez votre connexion internet.</string>
    <string name="empty_cart">Votre panier est vide</string>
    <string name="confirm_delete">Voulez-vous vraiment supprimer « %1$s » ?</string>

    <string-array name="sort_options">
        <item>Prix : croissant</item>
        <item>Prix : décroissant</item>
        <item>Plus récent</item>
        <item>Meilleure note</item>
    </string-array>
</resources>
```

### Arabic Translation (`res/values-ar/strings.xml`)

```xml
<!-- res/values-ar/strings.xml -->
<resources>
    <string name="app_name">متجري</string>
    <string name="welcome_message">مرحباً بعودتك، %1$s!</string>
    <string name="item_count">لديك %1$d عناصر في سلتك</string>
    <string name="login_button">تسجيل الدخول</string>
    <string name="logout_button">تسجيل الخروج</string>
    <string name="search_hint">البحث عن المنتجات…</string>
    <string name="error_network">تعذر الاتصال. يرجى التحقق من اتصالك بالإنترنت.</string>
    <string name="empty_cart">سلة التسوق فارغة</string>
</resources>
```

### Using Strings in Kotlin

```kotlin
// In Activity/Fragment
val welcome = getString(R.string.welcome_message, userName)
val itemCount = getString(R.string.item_count, cart.size)

// In Jetpack Compose
@Composable
fun WelcomeScreen(userName: String, cartSize: Int) {
    Text(text = stringResource(R.string.welcome_message, userName))
    Text(text = stringResource(R.string.item_count, cartSize))

    // String arrays
    val sortOptions = stringArrayResource(R.array.sort_options)
}
```

### String Formatting Placeholders

```xml
<!-- Positional arguments (%1$s, %2$d, etc.) -->
<string name="order_summary">Order #%1$d by %2$s on %3$s</string>
<!-- Result: "Order #1234 by John on Jan 5, 2026" -->

<!-- ALWAYS use positional args (%1$s not %s) because
     word order differs across languages:
     English: "Sent by John"     → %1$s by %2$s
     Japanese: "Johnが送信しました" → %2$s が %1$s しました
-->
```

---

## 2. RTL (Right-to-Left) Support

RTL languages include Arabic, Hebrew, Persian (Farsi), and Urdu. Android can mirror your entire layout automatically.

```
┌──────────────────────────────────────────────────────────────┐
│                 LTR vs RTL Layout Mirroring                   │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  LTR (English):                RTL (Arabic):                  │
│  ┌──────────────────────┐     ┌──────────────────────┐       │
│  │ ☰  My Shop    🔍 🛒  │     │ 🛒 🔍    متجري   ☰ │       │
│  ├──────────────────────┤     ├──────────────────────┤       │
│  │ ┌────┐               │     │               ┌────┐ │       │
│  │ │IMG │ Product Name  │     │  اسم المنتج  │IMG │ │       │
│  │ │    │ $29.99        │     │       ٢٩.٩٩$ │    │ │       │
│  │ └────┘               │     │               └────┘ │       │
│  ├──────────────────────┤     ├──────────────────────┤       │
│  │ ┌────┐               │     │               ┌────┐ │       │
│  │ │IMG │ Another Item  │     │   عنصر آخر   │IMG │ │       │
│  │ │    │ $15.00        │     │       ١٥.٠٠$ │    │ │       │
│  │ └────┘               │     │               └────┘ │       │
│  └──────────────────────┘     └──────────────────────┘       │
│                                                               │
│  Key: Horizontal layouts, padding, margins, drawables        │
│  all MIRROR automatically when using Start/End instead       │
│  of Left/Right.                                              │
└──────────────────────────────────────────────────────────────┘
```

### Enable RTL in Manifest

```xml
<!-- AndroidManifest.xml -->
<application
    android:supportsRtl="true"
    ... >
</application>
```

### Use Start/End Instead of Left/Right

```xml
<!-- ❌ BAD: Won't mirror for RTL -->
<TextView
    android:layout_marginLeft="16dp"
    android:paddingLeft="8dp"
    android:drawableLeft="@drawable/icon"
    android:gravity="left" />

<!-- ✅ GOOD: Mirrors automatically for RTL -->
<TextView
    android:layout_marginStart="16dp"
    android:paddingStart="8dp"
    android:drawableStart="@drawable/icon"
    android:gravity="start" />

<!-- Full mapping:
     Left    → Start
     Right   → End
     layout_marginLeft → layout_marginStart
     paddingRight      → paddingEnd
     drawableLeft      → drawableStart
     layout_alignParentLeft → layout_alignParentStart
     layout_toRightOf  → layout_toEndOf
-->
```

### RTL in Compose

```kotlin
@Composable
fun ProductRow(product: Product) {
    // Compose handles RTL automatically with Arrangement.Start/End
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), // Mirrors automatically
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(product.imageUrl),
            contentDescription = product.name,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.width(12.dp)) // Mirrors in RTL
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name)
            Text(product.price)
        }
    }
}

// Force LTR for specific elements (phone numbers, codes, etc.)
@Composable
fun PhoneNumber(number: String) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Text(text = number) // Always LTR regardless of locale
    }
}
```

### RTL-Aware Drawables

```xml
<!-- Drawables that should mirror (e.g., arrows, back button) -->
<!-- res/drawable/ic_arrow_forward.xml -->
<vector
    android:autoMirrored="true"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M12,4l-1.41,1.41L16.17,11H4v2h12.17l-5.58,5.59L12,20l8,-8z" />
</vector>

<!-- Drawables that should NOT mirror:
     ✗ Checkmarks, play buttons, clocks
     ✓ Back arrows, forward arrows, reply icons -->
```

### Provide RTL-Specific Layouts (if needed)

```
res/
├── layout/           ← Default (LTR)
│   └── activity_main.xml
├── layout-ldrtl/     ← RTL-specific layout
│   └── activity_main.xml
```

---

## 3. Plurals and Quantities

Different languages have different plural rules. English has 2 forms (1 item / 2 items). Arabic has 6 forms. Russian has 3.

```
┌──────────────────────────────────────────────────────────────┐
│              Plural Categories by Language                     │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Category   │ English  │ Arabic        │ Russian              │
│  ───────────┼──────────┼───────────────┼─────────────────     │
│  zero       │ (unused) │ 0             │ (unused)             │
│  one        │ 1        │ 1             │ 1, 21, 31...         │
│  two        │ (unused) │ 2             │ (unused)             │
│  few        │ (unused) │ 3-10          │ 2-4, 22-24...        │
│  many       │ (unused) │ 11-99         │ 5-20, 25-30...       │
│  other      │ 0, 2+    │ 100, 1000... │ 1.5, other...        │
│                                                               │
│  Android Plural Quantities:                                   │
│  zero, one, two, few, many, other                            │
│                                                               │
│  Rule: Always include "other" — it's the fallback!           │
└──────────────────────────────────────────────────────────────┘
```

### Define Plurals in XML

```xml
<!-- res/values/strings.xml (English) -->
<resources>
    <plurals name="items_in_cart">
        <item quantity="one">%1$d item in your cart</item>
        <item quantity="other">%1$d items in your cart</item>
    </plurals>

    <plurals name="minutes_ago">
        <item quantity="one">%1$d minute ago</item>
        <item quantity="other">%1$d minutes ago</item>
    </plurals>

    <plurals name="unread_messages">
        <item quantity="one">%1$d unread message</item>
        <item quantity="other">%1$d unread messages</item>
    </plurals>
</resources>

<!-- res/values-ar/strings.xml (Arabic — uses all 6 forms!) -->
<resources>
    <plurals name="items_in_cart">
        <item quantity="zero">لا توجد عناصر في سلتك</item>
        <item quantity="one">عنصر واحد في سلتك</item>
        <item quantity="two">عنصران في سلتك</item>
        <item quantity="few">%1$d عناصر في سلتك</item>
        <item quantity="many">%1$d عنصرًا في سلتك</item>
        <item quantity="other">%1$d عنصر في سلتك</item>
    </plurals>
</resources>

<!-- res/values-ru/strings.xml (Russian — uses 3 forms) -->
<resources>
    <plurals name="items_in_cart">
        <item quantity="one">%1$d товар в корзине</item>
        <item quantity="few">%1$d товара в корзине</item>
        <item quantity="many">%1$d товаров в корзине</item>
        <item quantity="other">%1$d товаров в корзине</item>
    </plurals>
</resources>
```

### Use Plurals in Code

```kotlin
// In Activity/Fragment
val cartMessage = resources.getQuantityString(
    R.plurals.items_in_cart,
    cartSize,       // Used to pick the plural form
    cartSize        // Used as %1$d format argument
)
// cartSize = 1 → "1 item in your cart"
// cartSize = 5 → "5 items in your cart"

// In Compose
@Composable
fun CartBadge(count: Int) {
    val message = pluralStringResource(
        R.plurals.items_in_cart,
        count,
        count
    )
    Text(text = message)
}
```

---

## 4. Date, Time, and Currency Formatting

**Never format dates/currency manually.** Always use locale-aware formatters.

```
┌──────────────────────────────────────────────────────────────┐
│         Date/Currency Formats by Locale                       │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Locale     │  Date           │  Number    │  Currency        │
│  ───────────┼─────────────────┼────────────┼───────────       │
│  en-US      │  Jan 5, 2026    │  1,234.56  │  $1,234.56      │
│  en-GB      │  5 Jan 2026     │  1,234.56  │  £1,234.56      │
│  de-DE      │  5. Jan. 2026   │  1.234,56  │  1.234,56 €     │
│  fr-FR      │  5 janv. 2026   │  1 234,56  │  1 234,56 €     │
│  ja-JP      │  2026年1月5日    │  1,234.56  │  ¥1,235         │
│  ar-SA      │  ٥ يناير ٢٠٢٦  │  ١٬٢٣٤٫٥٦ │  ١٬٢٣٤٫٥٦ ر.س  │
│                                                               │
│  ⚠️ Note: Decimal separator, thousands separator, currency    │
│  symbol position ALL vary by locale!                          │
└──────────────────────────────────────────────────────────────┘
```

### Date and Time Formatting

```kotlin
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.text.DateFormat
import android.text.format.DateUtils

// ---- java.time (recommended) ----

val now = LocalDateTime.now()
val date = LocalDate.of(2026, 1, 5)

// Locale-aware formatting
val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    .withLocale(Locale.getDefault())
val formatted = date.format(formatter)
// en-US: "Jan 5, 2026"
// de-DE: "05.01.2026"
// ja-JP: "2026/01/05"

// Date + Time
val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    .withLocale(Locale.getDefault())
val formattedDateTime = now.format(dateTimeFormatter)
// en-US: "Jan 5, 2026, 3:30:00 PM"

// Relative time ("5 minutes ago", "Yesterday")
val relativeTime = DateUtils.getRelativeTimeSpanString(
    timestamp,
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE
)

// Custom pattern (use sparingly — prefer FormatStyle)
val customFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
// en-US: "Monday, January 5, 2026"
// fr-FR: "lundi, janvier 5, 2026"
```

### Number and Currency Formatting

```kotlin
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// ---- Number formatting ----
val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
val formattedNumber = numberFormat.format(1234567.89)
// en-US: "1,234,567.89"
// de-DE: "1.234.567,89"
// fr-FR: "1 234 567,89"

// ---- Percentage ----
val percentFormat = NumberFormat.getPercentInstance(Locale.getDefault())
percentFormat.maximumFractionDigits = 1
val formattedPercent = percentFormat.format(0.856)
// en-US: "85.6%"
// tr-TR: "%85,6"

// ---- Currency formatting ----
val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
val formattedPrice = currencyFormat.format(29.99)
// en-US: "$29.99"
// de-DE: "29,99 €"
// ja-JP: "¥30"

// Specific currency (e.g., always show EUR)
val eurFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
eurFormat.currency = Currency.getInstance("EUR")
val euroPrice = eurFormat.format(29.99)
// en-US: "€29.99"
// de-DE: "29,99 €"

// ---- In Compose ----
@Composable
fun PriceTag(amount: Double, currencyCode: String = "USD") {
    val locale = LocalConfiguration.current.locales[0]
    val formatted = remember(amount, currencyCode, locale) {
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = Currency.getInstance(currencyCode)
        format.format(amount)
    }
    Text(text = formatted, style = MaterialTheme.typography.titleLarge)
}
```

### Locale-Aware Compose Utilities

```kotlin
@Composable
fun LocalizedOrderSummary(order: Order) {
    val locale = LocalConfiguration.current.locales[0]

    val dateStr = remember(order.date, locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(locale)
            .format(order.date)
    }

    val priceStr = remember(order.total, locale) {
        NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(order.currencyCode)
        }.format(order.total)
    }

    val itemCount = pluralStringResource(
        R.plurals.items_in_cart, order.itemCount, order.itemCount
    )

    Column {
        Text(text = stringResource(R.string.order_date, dateStr))
        Text(text = itemCount)
        Text(text = stringResource(R.string.order_total, priceStr))
    }
}
```

---

## 5. Changing Locale Programmatically

```kotlin
// In-app language picker (Android 13+ Per-App Language Preferences)
// AndroidManifest.xml
// <application android:localeConfig="@xml/locales_config" ...>

// res/xml/locales_config.xml
// <locale-config xmlns:android="http://schemas.android.com/apk/res/android">
//     <locale android:name="en" />
//     <locale android:name="fr" />
//     <locale android:name="ar" />
//     <locale android:name="de" />
//     <locale android:name="ja" />
// </locale-config>

// Change app locale (API 33+)
val appLocale = LocaleListCompat.forLanguageTags("fr")
AppCompatDelegate.setApplicationLocales(appLocale)

// Get current app locale
val currentLocale = AppCompatDelegate.getApplicationLocales()[0]
```

---

## i18n Checklist

```
┌──────────────────────────────────────────────────────────────┐
│                 i18n Checklist                                │
├──────────────────────────────────────────────────────────────┤
│  □ No hardcoded strings — all in strings.xml                 │
│  □ Use positional format args (%1$s, not %s)                 │
│  □ Use plurals for quantity-dependent strings                 │
│  □ Use Start/End instead of Left/Right                       │
│  □ android:supportsRtl="true" in manifest                    │
│  □ autoMirrored="true" on directional drawables              │
│  □ Test with pseudolocales (en-XA, ar-XB)                    │
│  □ Use locale-aware date/number/currency formatters          │
│  □ Allow text expansion (~30-40% for German/French)          │
│  □ Don't concatenate translated strings                      │
│  □ Don't embed text in images                                │
│  □ Provide locales_config.xml for per-app language           │
└──────────────────────────────────────────────────────────────┘
```
