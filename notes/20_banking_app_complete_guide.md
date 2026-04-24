# 20. Complete Banking App — "NEXUS BANK"

> Full-stack banking app: accounts, transfers, cards, loans, KYC, UPI, biometrics.
> Kotlin + Jetpack Compose + Clean Architecture + Multi-Module.

---

## TABLE OF CONTENTS

```
1. Feature Map (All Modules)
2. Architecture Overview
3. Module Structure
4. Database Schema
5. API Contract (Endpoints)
6. Screen List & Navigation Graph
7. Security Implementation
8. Build Phases
9. Testing Strategy
10. Tech Stack
```

---

# 1. FEATURE MAP

```
╔══════════════════════════════════════════════════════════════════════════╗
║  MODULE            │  FEATURES                    │  ANDROID CONCEPTS   ║
╠══════════════════════════════════════════════════════════════════════════╣
║                    │                               │                     ║
║  AUTH &            │  Phone/Email login             │ Credential Manager  ║
║  ONBOARDING        │  OTP verification              │ SMS Retriever API   ║
║                    │  Biometric setup (finger/face) │ BiometricPrompt     ║
║                    │  MPIN setup (4/6 digit)        │ EncryptedSharedPref ║
║                    │  KYC flow (Aadhaar/PAN/selfie) │ CameraX, ML Kit    ║
║                    │  Onboarding carousel           │ HorizontalPager     ║
║                    │  Terms & consent               │ WebView             ║
║                    │                               │                     ║
║  DASHBOARD         │  Account balance (masked)      │ StateFlow, Compose  ║
║                    │  Quick actions grid            │ LazyVerticalGrid    ║
║                    │  Recent transactions (5)       │ LazyColumn          ║
║                    │  Credit score widget           │ Canvas, Animation   ║
║                    │  Offers/promotions banner      │ HorizontalPager     ║
║                    │  Pull-to-refresh               │ PullToRefresh       ║
║                    │                               │                     ║
║  ACCOUNTS          │  Savings/Current account view  │ Room, Retrofit      ║
║                    │  Account statement (paginated) │ Paging 3            ║
║                    │  Download statement (PDF)      │ FileProvider        ║
║                    │  Account details               │ Copy-to-clipboard   ║
║                    │  Fixed/Recurring deposits      │ Complex forms       ║
║                    │                               │                     ║
║  TRANSFERS &       │  Internal transfer (own accts) │ Form validation     ║
║  PAYMENTS          │  NEFT/RTGS/IMPS               │ Multi-step flow     ║
║                    │  UPI payment (VPA)             │ Deep links          ║
║                    │  QR code scan & pay            │ CameraX, ZXing      ║
║                    │  QR code generate (receive)    │ Bitmap generation   ║
║                    │  Bill payments (electricity,   │ Search + Select UI  ║
║                    │    mobile, DTH, gas)           │                     ║
║                    │  Scheduled/recurring payments  │ WorkManager         ║
║                    │  Beneficiary management        │ Room + API sync     ║
║                    │  Transaction PIN confirmation  │ Bottom sheet PIN    ║
║                    │                               │                     ║
║  CARDS             │  View debit/credit cards       │ Custom Card UI      ║
║                    │  Card controls (lock/unlock,   │ Toggle switches,    ║
║                    │    online txn, international)  │ API calls           ║
║                    │  Set card limits               │ Slider + form       ║
║                    │  View card PIN (reveal)        │ Timer auto-hide     ║
║                    │  Card statement                │ Paging 3            ║
║                    │  Block/report lost card        │ Multi-step dialog   ║
║                    │  Apply for new card            │ Multi-step form     ║
║                    │  EMI conversion                │ Calculator logic    ║
║                    │                               │                     ║
║  LOANS             │  View active loans             │ LazyColumn          ║
║                    │  EMI schedule                  │ Table composable    ║
║                    │  Loan apply (personal/home)    │ Multi-step form,    ║
║                    │                               │ Document upload     ║
║                    │  EMI calculator                │ Compose + math      ║
║                    │  Pre-approved loan offer       │ Bottom sheet        ║
║                    │  Loan closure request          │ API + confirmation  ║
║                    │                               │                     ║
║  INVESTMENTS       │  Fixed deposits (create/view)  │ Forms, Calculator   ║
║                    │  Recurring deposits            │ Scheduling logic    ║
║                    │  Mutual funds (SIP/lumpsum)    │ Charts (Canvas)     ║
║                    │  Portfolio overview             │ Pie/Bar charts      ║
║                    │                               │                     ║
║  NOTIFICATIONS     │  Transaction alerts             │ FCM                 ║
║  & ALERTS          │  Payment due reminders          │ WorkManager         ║
║                    │  Offer notifications            │ Rich notifications  ║
║                    │  In-app notification center     │ Room + Badge count  ║
║                    │  Security alerts (new login)    │ Notification channel║
║                    │                               │                     ║
║  PROFILE &         │  Personal info view/edit        │ Form + API          ║
║  SETTINGS          │  Change MPIN                    │ PIN flow            ║
║                    │  Biometric on/off               │ BiometricPrompt     ║
║                    │  Language selection             │ AppCompat locale    ║
║                    │  Theme (dark/light/system)      │ DataStore           ║
║                    │  Notification preferences       │ Toggle + DataStore  ║
║                    │  App lock timeout setting       │ DataStore           ║
║                    │  Linked devices / sessions      │ API + logout others ║
║                    │  Help & support / FAQ           │ WebView or Compose  ║
║                    │  Rate the app                   │ In-app review API   ║
║                    │  Logout                         │ Clear session       ║
║                    │                               │                     ║
║  SECURITY          │  Session timeout (auto-logout)  │ Lifecycle observer  ║
║  (Cross-cutting)   │  Root/jailbreak detection       │ SafetyNet/Play Int. ║
║                    │  SSL pinning                    │ OkHttp CertPinner   ║
║                    │  Screenshot prevention          │ FLAG_SECURE         ║
║                    │  App-level encryption            │ SQLCipher, Crypto   ║
║                    │  Tamper detection                │ Play Integrity API  ║
║                    │  Secure keyboard (PIN entry)    │ Custom composable   ║
║                    │                               │                     ║
╚══════════════════════════════════════════════════════════════════════════╝
```

---

# 2. ARCHITECTURE OVERVIEW

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     NEXUS BANK — CLEAN ARCHITECTURE                     │
│                                                                         │
│  ┌─────────────────────── PRESENTATION ───────────────────────────────┐ │
│  │                                                                     │ │
│  │  Compose Screens ──▶ ViewModels ──▶ UI State (data class)          │ │
│  │                      │                                              │ │
│  │  • Unidirectional data flow (UDF)                                  │ │
│  │  • Screen emits Events → VM processes → emits new State            │ │
│  │  • Side effects via SharedFlow (navigation, snackbar, toast)       │ │
│  │                      │                                              │ │
│  └──────────────────────┼──────────────────────────────────────────────┘ │
│                         │ Hilt injection                                │
│  ┌──────────────────────┼──── DOMAIN ─────────────────────────────────┐ │
│  │                      ▼                                              │ │
│  │  UseCases (single responsibility)                                   │ │
│  │  ├── Each UC = 1 business operation                                │ │
│  │  ├── operator fun invoke() for clean call-site                     │ │
│  │  └── Returns Flow<Resource<T>> or Resource<T>                      │ │
│  │                                                                     │ │
│  │  Domain Models (no framework dependencies)                          │ │
│  │  Repository Interfaces (abstractions only)                          │ │
│  └──────────────────────┼──────────────────────────────────────────────┘ │
│                         │                                               │
│  ┌──────────────────────┼──── DATA ───────────────────────────────────┐ │
│  │                      ▼                                              │ │
│  │  Repository Implementations                                         │ │
│  │  ├── Remote: Retrofit + OkHttp (API calls)                         │ │
│  │  ├── Local: Room (cache) + DataStore (prefs)                       │ │
│  │  └── Mappers: DTO ↔ Entity ↔ Domain Model                         │ │
│  │                                                                     │ │
│  │  NetworkBoundResource (cache-first pattern)                         │ │
│  │  TokenAuthenticator (auto-refresh JWT)                              │ │
│  │  EncryptedPreferences (session tokens)                              │ │
│  └────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Resource Wrapper

```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
```

---

# 3. MODULE STRUCTURE

```
nexus-bank/
├── app/                          ← Application module (MainActivity, App, NavHost)
├── core/
│   ├── core-ui/                  ← Shared composables, theme, design system
│   ├── core-data/                ← Base repository, NetworkBoundResource, mappers
│   ├── core-network/             ← Retrofit setup, interceptors, SSL pinning
│   ├── core-database/            ← Room DB, DAOs, type converters
│   ├── core-domain/              ← Base use case, Resource wrapper, domain models
│   ├── core-security/            ← Encryption, biometric, root detection, PIN
│   ├── core-common/              ← Extensions, utils, constants, formatters
│   └── core-testing/             ← Shared test utilities, fakes, fixtures
├── feature/
│   ├── feature-auth/             ← Login, OTP, MPIN, biometric setup, KYC
│   ├── feature-onboarding/       ← Splash, carousel, T&C
│   ├── feature-dashboard/        ← Home screen, quick actions, balance
│   ├── feature-accounts/         ← Account list, statement, details, FD/RD
│   ├── feature-transfers/        ← Fund transfer, UPI, QR, bill pay
│   ├── feature-cards/            ← Card view, controls, limits, apply
│   ├── feature-loans/            ← Loan view, apply, EMI calculator
│   ├── feature-investments/      ← FD, RD, mutual funds, portfolio
│   ├── feature-notifications/    ← Notification center, preferences
│   ├── feature-profile/          ← Profile, settings, help
│   └── feature-kyc/              ← Document upload, selfie, verification
├── gradle/
│   └── libs.versions.toml        ← Version catalog
├── build-logic/                  ← Convention plugins (shared build config)
│   └── convention/
└── settings.gradle.kts
```

### Module Dependency Rules

```
feature-* ──▶ core-domain, core-ui, core-common
core-data ──▶ core-network, core-database, core-domain
core-network ──▶ core-common
core-database ──▶ core-domain
app ──▶ all feature modules (for navigation)

RULE: feature modules NEVER depend on each other
RULE: core-domain has ZERO android dependencies (pure Kotlin)
```

---

# 4. DATABASE SCHEMA

```
┌──────────────────┐     ┌─────────────────────┐     ┌──────────────────┐
│  users (cached)   │     │   accounts           │     │  beneficiaries   │
├──────────────────┤     ├─────────────────────┤     ├──────────────────┤
│ id (PK)          │◀──┐ │ id (PK)             │     │ id (PK)          │
│ fullName         │   │ │ userId (FK) ────────┘     │ userId (FK)      │
│ email            │   │ │ accountNumber        │     │ name             │
│ phone            │   │ │ type (SAVINGS/       │     │ accountNumber    │
│ avatarUrl        │   │ │   CURRENT/FD/RD)     │     │ ifscCode         │
│ kycStatus        │   │ │ balance              │     │ bankName         │
│ createdAt        │   │ │ currency             │     │ nickname         │
│ lastSynced       │   │ │ branchName           │     │ transferLimit    │
└──────────────────┘   │ │ ifscCode             │     │ isVerified       │
                       │ │ isActive             │     │ createdAt        │
                       │ │ lastSynced           │     └──────────────────┘
                       │ └─────────────────────┘
                       │                               ┌──────────────────┐
┌──────────────────┐   │  ┌─────────────────────┐     │  notifications   │
│  transactions     │   │  │   cards              │     ├──────────────────┤
├──────────────────┤   │  ├─────────────────────┤     │ id (PK)          │
│ id (PK)          │   │  │ id (PK)             │     │ userId (FK)      │
│ accountId (FK)   │   │  │ userId (FK) ────────┘     │ title            │
│ type (CREDIT/    │   │  │ cardNumber (masked)  │     │ body             │
│   DEBIT)         │   │  │ type (DEBIT/CREDIT)  │     │ type (enum)      │
│ amount           │   │  │ network (VISA/MC/    │     │ isRead           │
│ currency         │   │  │   RUPAY)             │     │ deepLink         │
│ description      │   │  │ expiryMonth          │     │ createdAt        │
│ category         │   │  │ expiryYear           │     └──────────────────┘
│ referenceId      │   │  │ nameOnCard           │
│ status (SUCCESS/ │   │  │ isLocked             │     ┌──────────────────┐
│   PENDING/FAILED)│   │  │ isOnlineEnabled      │     │  loans            │
│ recipientName    │   │  │ isIntlEnabled        │     ├──────────────────┤
│ recipientAccount │   │  │ dailyLimit           │     │ id (PK)          │
│ mode (UPI/NEFT/  │   │  │ cardStatus           │     │ userId (FK)      │
│   IMPS/RTGS)     │   │  │ lastSynced           │     │ type (PERSONAL/  │
│ timestamp        │   │  └─────────────────────┘     │   HOME/AUTO)     │
│ lastSynced       │   │                               │ principalAmount  │
└──────────────────┘   │  ┌─────────────────────┐     │ outstandingAmount│
                       │  │  scheduled_payments  │     │ interestRate     │
┌──────────────────┐   │  ├─────────────────────┤     │ emiAmount        │
│  sessions         │   │  │ id (PK)             │     │ tenure (months)  │
├──────────────────┤   │  │ userId (FK) ────────┘     │ startDate        │
│ id (PK)          │   │  │ beneficiaryId (FK)   │     │ endDate          │
│ userId (FK)      │      │ amount               │     │ nextEmiDate      │
│ deviceId         │      │ frequency (ONCE/     │     │ status           │
│ deviceName       │      │   WEEKLY/MONTHLY)    │     │ lastSynced       │
│ ipAddress        │      │ nextDate             │     └──────────────────┘
│ isActive         │      │ isActive             │
│ createdAt        │      │ createdAt            │
│ lastActiveAt     │      └─────────────────────┘
└──────────────────┘
```

---

# 5. API CONTRACT

```
BASE URL: https://api.nexusbank.com/v1

── AUTH ─────────────────────────────────────────────────────────
POST   /auth/send-otp              { phone }                    → { otpRef }
POST   /auth/verify-otp            { phone, otp, otpRef }       → { tempToken }
POST   /auth/setup-mpin            { mpin }                     → { accessToken, refreshToken }
POST   /auth/login-mpin            { phone, mpin, deviceId }    → { accessToken, refreshToken }
POST   /auth/login-biometric       { biometricToken, deviceId } → { accessToken, refreshToken }
POST   /auth/refresh-token         { refreshToken }             → { accessToken, refreshToken }
POST   /auth/logout                { }                          → { success }

── KYC ──────────────────────────────────────────────────────────
POST   /kyc/aadhaar                { aadhaarNumber }            → { status, ref }
POST   /kyc/pan                    { panNumber }                → { status, name }
POST   /kyc/selfie                 { selfieImage (multipart) }  → { matchScore, status }
GET    /kyc/status                                              → { kycStatus, steps[] }

── USER ─────────────────────────────────────────────────────────
GET    /user/profile                                            → { user }
PUT    /user/profile               { name, email, address }     → { user }
GET    /user/devices                                            → { sessions[] }
DELETE /user/devices/{sessionId}                                → { success }

── ACCOUNTS ─────────────────────────────────────────────────────
GET    /accounts                                                → { accounts[] }
GET    /accounts/{id}                                           → { account }
GET    /accounts/{id}/statement    ?from=&to=&page=&size=       → { transactions[], pagination }
GET    /accounts/{id}/statement/pdf ?from=&to=                  → binary (PDF)
GET    /accounts/{id}/mini-statement                            → { transactions[10] }

── TRANSACTIONS ─────────────────────────────────────────────────
POST   /transfers/internal         { fromAccId, toAccId, amount, note } → { txn }
POST   /transfers/neft             { fromAccId, benefId, amount, note } → { txn }
POST   /transfers/imps             { fromAccId, benefId, amount, note } → { txn }
POST   /transfers/upi              { fromAccId, vpa, amount, note }     → { txn }
POST   /transfers/verify-pin       { txnId, pin }                       → { txn (confirmed) }
GET    /transactions/{id}                                               → { txn }

── BENEFICIARIES ────────────────────────────────────────────────
GET    /beneficiaries                                           → { beneficiaries[] }
POST   /beneficiaries              { name, accNo, ifsc, nick }  → { beneficiary }
DELETE /beneficiaries/{id}                                      → { success }
POST   /beneficiaries/{id}/verify                               → { status }

── CARDS ────────────────────────────────────────────────────────
GET    /cards                                                   → { cards[] }
GET    /cards/{id}                                              → { card }
PUT    /cards/{id}/lock            { isLocked }                 → { card }
PUT    /cards/{id}/online          { isEnabled }                → { card }
PUT    /cards/{id}/international   { isEnabled }                → { card }
PUT    /cards/{id}/limits          { daily, perTxn }            → { card }
GET    /cards/{id}/pin             { otp }                      → { pin (temp, 30s) }
POST   /cards/{id}/block           { reason }                   → { status }
GET    /cards/{id}/statement       ?month=&year=                → { transactions[] }

── LOANS ────────────────────────────────────────────────────────
GET    /loans                                                   → { loans[] }
GET    /loans/{id}                                              → { loan }
GET    /loans/{id}/emi-schedule                                 → { emis[] }
POST   /loans/apply                { type, amount, tenure, docs }→ { applicationId }
GET    /loans/pre-approved                                      → { offers[] }
POST   /loans/{id}/close                                        → { status }

── BILL PAYMENTS ────────────────────────────────────────────────
GET    /bills/categories                                        → { categories[] }
GET    /bills/operators?cat=       { category }                 → { operators[] }
POST   /bills/fetch                { operatorId, customerId }   → { billDetails }
POST   /bills/pay                  { billId, amount, accId }    → { txn }

── NOTIFICATIONS ────────────────────────────────────────────────
GET    /notifications              ?page=&size=                 → { notifications[], unread }
PUT    /notifications/{id}/read                                 → { success }
PUT    /notifications/read-all                                  → { success }
PUT    /notifications/preferences  { txnAlerts, offers, ... }   → { prefs }

── INVESTMENTS ──────────────────────────────────────────────────
GET    /investments/fd                                          → { fixedDeposits[] }
POST   /investments/fd             { amount, tenure, accId }    → { fd }
GET    /investments/portfolio                                   → { summary, holdings[] }

── MISC ─────────────────────────────────────────────────────────
GET    /offers                                                  → { offers[] }
GET    /config/app                                              → { forceUpdate, maintenance, features }
POST   /support/ticket             { subject, message, screenshots } → { ticketId }
```

---

## Auth Flow (Token Management)

```
┌──────────┐     ┌──────────┐     ┌──────────────────────────┐
│  Client   │     │  Server   │     │  OkHttp Interceptors      │
└─────┬─────┘     └─────┬─────┘     └────────────┬─────────────┘
      │                 │                         │
      │  Login (MPIN)   │                         │
      │────────────────▶│                         │
      │  accessToken +  │                         │
      │◀─refreshToken───│                         │
      │                 │                         │
      │  API call ──────┼─── AuthInterceptor ────▶│
      │                 │    adds Bearer token     │
      │                 │                         │
      │  401 Unauthorized                         │
      │◀─────────────────────────────────────────│
      │                 │                         │
      │                 │    TokenAuthenticator:   │
      │                 │    1. Call /refresh-token│
      │                 │    2. Store new tokens   │
      │                 │    3. Retry original req │
      │                 │◀────────────────────────│
      │  Success!       │                         │
      │◀─────────────────────────────────────────│
```

---

# 6. SCREEN LIST & NAVIGATION GRAPH

```
┌─────────────────────────────────────────────────────────────────────┐
│                      NAVIGATION GRAPH                                │
│                                                                      │
│  SPLASH ──▶ ┌─────────────────────────────────────────────────────┐ │
│             │ Is logged in?                                        │ │
│             │  NO ──▶ ONBOARDING ──▶ LOGIN_PHONE ──▶ OTP_VERIFY   │ │
│             │         ──▶ MPIN_SETUP ──▶ BIOMETRIC_SETUP ──▶ HOME │ │
│             │  YES ──▶ MPIN_ENTRY / BIOMETRIC_PROMPT ──▶ HOME     │ │
│             └─────────────────────────────────────────────────────┘ │
│                                                                      │
│  HOME (BottomNav) ──────────────────────────────────────────────────│
│  │                                                                   │
│  ├── Tab: Dashboard                                                  │
│  │   └── DashboardScreen                                            │
│  │       ├── → AccountDetailScreen → StatementScreen                │
│  │       ├── → TransferScreen (quick action)                        │
│  │       ├── → QRScanScreen (quick action)                          │
│  │       ├── → BillPayScreen (quick action)                         │
│  │       └── → NotificationCenterScreen                             │
│  │                                                                   │
│  ├── Tab: Accounts                                                   │
│  │   └── AccountListScreen                                          │
│  │       ├── → AccountDetailScreen                                  │
│  │       │   ├── → StatementScreen (paginated)                      │
│  │       │   └── → DownloadStatementScreen                          │
│  │       ├── → FDCreateScreen                                       │
│  │       └── → RDCreateScreen                                       │
│  │                                                                   │
│  ├── Tab: Transfers                                                  │
│  │   └── TransferHomeScreen                                         │
│  │       ├── → InternalTransferScreen (own accounts)                │
│  │       ├── → BankTransferScreen (NEFT/IMPS/RTGS)                 │
│  │       │   └── → SelectBeneficiaryScreen                         │
│  │       │       └── → AddBeneficiaryScreen                        │
│  │       ├── → UPITransferScreen                                    │
│  │       ├── → QRScanScreen                                        │
│  │       │   └── → QRPayConfirmScreen                              │
│  │       ├── → QRGenerateScreen (receive money)                     │
│  │       ├── → BillPayScreen                                       │
│  │       │   └── → SelectOperatorScreen → BillConfirmScreen        │
│  │       ├── → ScheduledPaymentsScreen                              │
│  │       └── → TransactionPinSheet (BottomSheet, reusable)          │
│  │                                                                   │
│  ├── Tab: Cards                                                      │
│  │   └── CardListScreen                                             │
│  │       ├── → CardDetailScreen                                     │
│  │       │   ├── → CardControlsScreen                              │
│  │       │   ├── → CardLimitsScreen                                │
│  │       │   ├── → ViewPinScreen (OTP-gated, auto-hide)            │
│  │       │   ├── → CardStatementScreen                             │
│  │       │   └── → BlockCardScreen                                 │
│  │       └── → ApplyCardScreen (multi-step)                        │
│  │                                                                   │
│  └── Tab: More                                                       │
│      └── MoreScreen (grid of options)                               │
│          ├── → LoansScreen                                          │
│          │   ├── → LoanDetailScreen → EMIScheduleScreen            │
│          │   ├── → LoanApplyScreen (multi-step)                    │
│          │   └── → EMICalculatorScreen                             │
│          ├── → InvestmentsScreen                                    │
│          │   ├── → PortfolioScreen                                 │
│          │   └── → CreateFDScreen                                  │
│          ├── → ProfileScreen                                        │
│          │   ├── → EditProfileScreen                               │
│          │   ├── → ChangeMPINScreen                                │
│          │   ├── → BiometricSettingScreen                          │
│          │   ├── → LinkedDevicesScreen                             │
│          │   └── → AppSettingsScreen (theme, language, notif prefs)│
│          ├── → HelpScreen                                           │
│          ├── → RateAppScreen                                        │
│          └── → Logout (confirmation dialog)                         │
│                                                                      │
│  TOTAL: ~45 screens                                                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Navigation Implementation

```kotlin
// Type-safe navigation with Kotlin Serialization
@Serializable data object Splash
@Serializable data object Onboarding
@Serializable data object LoginPhone
@Serializable data class OtpVerify(val phone: String, val otpRef: String)
@Serializable data object MpinSetup
@Serializable data object MpinEntry
@Serializable data object Home

// Nested graphs per tab
@Serializable data object DashboardGraph
@Serializable data object AccountsGraph
@Serializable data object TransfersGraph
@Serializable data object CardsGraph
@Serializable data object MoreGraph

// Screens with args
@Serializable data class AccountDetail(val accountId: String)
@Serializable data class Statement(val accountId: String)
@Serializable data class CardDetail(val cardId: String)
@Serializable data class LoanDetail(val loanId: String)
@Serializable data class TransactionDetail(val txnId: String)
@Serializable data class BankTransfer(val beneficiaryId: String? = null)
```

---

# 7. SECURITY IMPLEMENTATION

```
╔══════════════════════════════════════════════════════════════════════╗
║  LAYER          │  WHAT                         │  HOW               ║
╠══════════════════════════════════════════════════════════════════════╣
║                 │                                │                    ║
║  NETWORK        │  HTTPS only                    │  OkHttp config     ║
║                 │  Certificate pinning           │  CertificatePinner ║
║                 │  No data in logs (prod)        │  HttpLogging=NONE  ║
║                 │                                │                    ║
║  AUTH TOKENS    │  Store encrypted               │  EncryptedSharedPref║
║                 │  Auto-refresh on 401           │  TokenAuthenticator║
║                 │  Clear on logout               │  SecurityManager   ║
║                 │                                │                    ║
║  APP LOCK       │  MPIN (4-6 digits)             │  BCrypt hash stored║
║                 │  Biometric (fingerprint/face)  │  BiometricPrompt   ║
║                 │  Auto-lock on background       │  ProcessLifecycleO.║
║                 │  Max 3 wrong attempts → logout │  Counter + policy  ║
║                 │                                │                    ║
║  DATA AT REST   │  Room DB encrypted             │  SQLCipher          ║
║                 │  Preferences encrypted         │  EncryptedSharedPref║
║                 │  Sensitive fields masked in UI │  •••• 1234          ║
║                 │                                │                    ║
║  RUNTIME        │  Root/emulator detection        │  Play Integrity API║
║                 │  Debugger detection             │  isDebuggerConn.   ║
║                 │  Screenshot prevention          │  FLAG_SECURE        ║
║                 │  Tapjacking prevention          │  filterTouchEvents ║
║                 │  No data in recent apps         │  FLAG_SECURE        ║
║                 │                                │                    ║
║  BUILD          │  Code obfuscation               │  R8 / ProGuard     ║
║                 │  No secrets in code             │  BuildConfig +     ║
║                 │                                │  local.properties  ║
║                 │  API key rotation               │  Remote config     ║
║                 │                                │                    ║
║  SESSION        │  JWT short-lived (15 min)       │  Server-side       ║
║                 │  Refresh token (30 days)        │  Secure storage    ║
║                 │  Idle timeout (5 min)           │  Lifecycle tracking║
║                 │  Single device enforcement      │  Device binding    ║
║                 │                                │                    ║
╚══════════════════════════════════════════════════════════════════════╝
```

### Session Timeout Implementation

```kotlin
// In Application class
class NexusBankApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }
}

class AppLifecycleObserver @Inject constructor(
    private val sessionManager: SessionManager
) : DefaultLifecycleObserver {
    override fun onStop(owner: LifecycleOwner) {
        sessionManager.startInactivityTimer()  // 5-min countdown
    }
    override fun onStart(owner: LifecycleOwner) {
        if (sessionManager.isSessionExpired()) {
            sessionManager.requireReAuth()  // Show MPIN/Biometric
        } else {
            sessionManager.cancelInactivityTimer()
        }
    }
}
```

---

# 8. BUILD PHASES

```
╔════════════════════════════════════════════════════════════════════════╗
║  PHASE 1 — Project Foundation (Week 1-2)                              ║
║  ─────────────────────────────────────                                 ║
║  □ Multi-module project setup (app + core/* + feature/*)              ║
║  □ Convention plugins (build-logic)                                   ║
║  □ Version catalog (libs.versions.toml)                               ║
║  □ Hilt DI setup (across modules)                                     ║
║  □ Theme + Design system (colors, typography, components)             ║
║  □ Core-network: Retrofit, OkHttp, interceptors, SSL pinning         ║
║  □ Core-database: Room setup, base entities                           ║
║  □ Core-security: EncryptedPrefs, SecurityManager stub               ║
║  □ Navigation shell (NavHost + BottomNavigation)                      ║
║  □ Splash screen (API 31+ Splash Screen API)                         ║
╠════════════════════════════════════════════════════════════════════════╣
║  PHASE 2 — Auth & Onboarding (Week 3-4)                               ║
║  ─────────────────────────────────────                                 ║
║  □ Onboarding carousel (HorizontalPager)                              ║
║  □ Phone number entry + OTP screen                                    ║
║  □ SMS Retriever API (auto-read OTP)                                  ║
║  □ MPIN setup & entry (custom PIN pad composable)                     ║
║  □ BiometricPrompt integration                                        ║
║  □ Token storage (EncryptedSharedPreferences)                         ║
║  □ TokenAuthenticator (auto-refresh)                                  ║
║  □ Auth navigation flow (login ↔ authenticated)                      ║
║  □ Session timeout (ProcessLifecycleOwner)                            ║
╠════════════════════════════════════════════════════════════════════════╣
║  PHASE 3 — Dashboard & Accounts (Week 5-6)                            ║
║  ─────────────────────────────────────                                 ║
║  □ Dashboard screen (balance, quick actions, recent txns)             ║
║  □ Pull-to-refresh                                                    ║
║  □ Account list screen                                                ║
║  □ Account detail screen (balance, info, mini-statement)              ║
║  □ Account statement with Paging 3                                    ║
║  □ Statement PDF download (FileProvider)                              ║
║  □ NetworkBoundResource pattern (cache-first)                         ║
║  □ Shimmer loading effect (placeholder)                               ║
╠════════════════════════════════════════════════════════════════════════╣
║  PHASE 4 — Transfers & Payments (Week 7-9)                            ║
║  ─────────────────────────────────────                                 ║
║  □ Beneficiary management (add, delete, verify)                       ║
║  □ Internal transfer (own accounts)                                   ║
║  □ Bank transfer (NEFT/IMPS) — multi-step flow                       ║
║  □ UPI transfer (VPA input + pay)                                     ║
║  □ QR code scanner (CameraX + ZXing)                                  ║
║  □ QR code generator (receive payments)                               ║
║  □ Transaction PIN bottom sheet (reusable)                            ║
║  □ Transaction success/failure screens                                ║
║  □ Bill payments (category → operator → fetch → pay)                 ║
║  □ Scheduled/recurring payments (WorkManager)                         ║
╠════════════════════════════════════════════════════════════════════════╣
║  PHASE 5 — Cards & Loans (Week 10-11)                                  ║
║  ─────────────────────────────────────                                 ║
║  □ Card list with custom card UI composable                           ║
║  □ Card controls (lock, online, international toggles)                ║
║  □ Set card limits (slider + number input)                            ║
║  □ View card PIN (OTP gated, 30s auto-hide timer)                     ║
║  □ Card statement                                                     ║
║  □ Block card flow                                                    ║
║  □ Loan list + loan detail                                            ║
║  □ EMI schedule (table composable)                                    ║
║  □ EMI calculator (formula + interactive sliders)                     ║
║  □ Loan application (multi-step + document upload)                    ║
╠════════════════════════════════════════════════════════════════════════╣
║  PHASE 6 — Notifications, Profile & Settings (Week 12-13)              ║
║  ─────────────────────────────────────                                 ║
║  □ FCM setup + notification channels                                  ║
║  □ Notification center (in-app, paginated, mark read)                 ║
║  □ Deep links from notification → specific screen                    ║
║  □ Profile screen (view + edit)                                       ║
║  □ Change MPIN flow                                                   ║
║  □ Language selection (per-app locale)                                 ║
║  □ Theme selection (DataStore)                                        ║
║  □ Linked devices + logout others                                     ║
║  □ Help/FAQ (WebView or Compose)                                      ║
║  □ In-app review prompt                                               ║
╠════════════════════════════════════════════════════════════════════════╣
║  PHASE 7 — KYC & Investments (Week 14-15)                              ║
║  ─────────────────────────────────────                                 ║
║  □ KYC flow: Aadhaar → PAN → Selfie verification                    ║
║  □ CameraX (selfie capture)                                          ║
║  □ ML Kit face detection (liveness check)                             ║
║  □ Document upload (pick from gallery / camera)                       ║
║  □ Fixed deposit creation                                             ║
║  □ Investment portfolio (charts)                                      ║
║  □ Credit score widget (animated circular progress)                   ║
╠════════════════════════════════════════════════════════════════════════╣
║  PHASE 8 — Security Hardening (Week 16)                                ║
║  ─────────────────────────────────────                                 ║
║  □ SQLCipher encryption for Room                                      ║
║  □ Root/emulator detection (Play Integrity)                           ║
║  □ Screenshot prevention (FLAG_SECURE)                                ║
║  □ ProGuard/R8 rules for all modules                                  ║
║  □ Security audit checklist pass                                      ║
╠════════════════════════════════════════════════════════════════════════╣
║  PHASE 9 — Testing & CI/CD (Week 17-18)                                ║
║  ─────────────────────────────────────                                 ║
║  □ Unit tests (ViewModels, UseCases, Repositories, Mappers)           ║
║  □ Compose UI tests (screens, flows)                                  ║
║  □ Integration tests (Room DAOs, API with MockWebServer)              ║
║  □ Screenshot tests (Paparazzi)                                       ║
║  □ GitHub Actions CI (build → lint → test → APK)                     ║
║  □ Baseline profiles                                                  ║
║  □ Play Store internal testing release                                ║
╚════════════════════════════════════════════════════════════════════════╝
```

---

# 9. TESTING STRATEGY

```
╔═══════════════════════════════════════════════════════════════════════╗
║  LAYER          │  WHAT TO TEST             │  TOOLS                  ║
╠═══════════════════════════════════════════════════════════════════════╣
║  ViewModel      │  State transitions,        │  JUnit, Turbine,       ║
║                 │  event handling,            │  Mockk, Coroutine      ║
║                 │  error scenarios            │  test dispatcher       ║
║                 │                             │                        ║
║  UseCase        │  Business logic,            │  JUnit, Mockk          ║
║                 │  edge cases,                │                        ║
║                 │  error mapping              │                        ║
║                 │                             │                        ║
║  Repository     │  Cache logic,               │  JUnit, Mockk,        ║
║                 │  network+DB orchestration,  │  Coroutine test        ║
║                 │  mapper correctness          │                        ║
║                 │                             │                        ║
║  Room DAO       │  Queries, relations,        │  AndroidJUnit,         ║
║                 │  migrations                 │  in-memory Room DB     ║
║                 │                             │                        ║
║  API Service    │  Request/response parsing,  │  MockWebServer,        ║
║                 │  error handling             │  JUnit                 ║
║                 │                             │                        ║
║  Compose UI     │  Screen rendering,          │  Compose Testing,      ║
║                 │  user interactions,         │  createComposeRule      ║
║                 │  navigation                 │                        ║
║                 │                             │                        ║
║  Screenshot     │  Visual regression          │  Paparazzi             ║
║                 │                             │                        ║
║  E2E            │  Critical user flows        │  Espresso/UI Automator ║
║                 │  (login → transfer → verify)│                        ║
╚═══════════════════════════════════════════════════════════════════════╝

Test file structure:
  feature-auth/
    src/main/    → AuthViewModel, LoginScreen
    src/test/    → AuthViewModelTest, LoginUseCaseTest
    src/androidTest/ → LoginScreenTest
```

---

# 10. TECH STACK

```
╔════════════════════════════════════════════════════════════════════╗
║  CATEGORY          │  LIBRARY / TOOL                              ║
╠════════════════════════════════════════════════════════════════════╣
║  Language           │  Kotlin 2.x                                 ║
║  UI                 │  Jetpack Compose + Material 3               ║
║  Navigation         │  Compose Navigation (type-safe)             ║
║  DI                 │  Hilt                                       ║
║  Local DB           │  Room + SQLCipher                           ║
║  Preferences        │  DataStore + EncryptedSharedPreferences     ║
║  Networking         │  Retrofit + OkHttp + KotlinX Serialization ║
║  Async              │  Coroutines + Flow                          ║
║  Paging             │  Paging 3                                   ║
║  Image Loading      │  Coil                                       ║
║  Camera             │  CameraX                                    ║
║  QR Code            │  ZXing (scan) + custom Bitmap (generate)   ║
║  ML                 │  ML Kit (face detection, OCR)              ║
║  Charts             │  Vico or custom Canvas                      ║
║  Biometric          │  BiometricPrompt (AndroidX)                ║
║  Background         │  WorkManager                                ║
║  Push               │  Firebase Cloud Messaging (FCM)            ║
║  Analytics          │  Firebase Analytics                         ║
║  Crash Reporting    │  Firebase Crashlytics                       ║
║  Security           │  Play Integrity API                         ║
║  Splash             │  SplashScreen API (Android 12+)            ║
║  In-App Review      │  Play In-App Review                         ║
║  Testing            │  JUnit + Mockk + Turbine + Compose Testing ║
║  Screenshot Tests   │  Paparazzi                                  ║
║  API Mocking        │  MockWebServer                              ║
║  CI/CD              │  GitHub Actions                             ║
║  Code Quality       │  Detekt + Ktlint                           ║
║  Logging            │  Timber                                     ║
║  Memory Leaks       │  LeakCanary                                 ║
║  Min SDK            │  24                                         ║
║  Target SDK         │  36                                         ║
╚════════════════════════════════════════════════════════════════════╝
```

---

# KEY COMPOSABLE COMPONENTS TO BUILD

```
Reusable (core-ui):
├── NexusButton (primary, secondary, outline, text variants)
├── NexusTextField (with validation, error state, icons)
├── NexusCard (elevated, outlined)
├── NexusTopBar (with back, title, actions)
├── NexusBottomSheet (modal)
├── NexusDialog (confirmation, info, error)
├── NexusPinPad (custom 4/6 digit PIN input)
├── NexusOtpField (4-6 digit with auto-focus)
├── NexusAmountField (currency formatted input)
├── NexusLoadingOverlay (full-screen translucent)
├── NexusShimmer (placeholder loading effect)
├── NexusEmptyState (icon + message + action)
├── NexusErrorState (retry button)
├── NexusBadge (notification count)
├── NexusAvatar (initials or image)
└── NexusStatusChip (success, pending, failed)

Feature-specific:
├── BankCardComposable (3D card with number, expiry, CVV)
├── TransactionItem (icon, name, amount, date, status)
├── AccountSummaryCard (type, balance, account number)
├── QuickActionGrid (icon + label, 2-column grid)
├── CreditScoreGauge (animated circular arc)
├── EMICalculatorSlider (amount + tenure with live preview)
├── QRCodeView (generated QR bitmap)
├── BeneficiaryListItem (name, bank, account masked)
└── LoanProgressBar (paid vs remaining)
```

---

# GRADLE DEPENDENCIES (libs.versions.toml)

```toml
[versions]
kotlin = "2.1.0"
agp = "8.7.0"
compose-bom = "2025.01.00"
hilt = "2.52"
room = "2.7.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
paging = "3.3.4"
coil = "3.0.4"
camerax = "1.4.1"
datastore = "1.1.2"
work = "2.10.0"
navigation = "2.8.5"
lifecycle = "2.8.7"
coroutines = "1.9.0"
serialization = "1.7.3"
mockk = "1.13.13"
turbine = "1.2.0"
timber = "5.0.1"

[libraries]
# Compose
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-icons = { module = "androidx.compose.material:material-icons-extended" }
compose-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-testing = { module = "androidx.compose.ui:ui-test-junit4" }

# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-navigation = { module = "androidx.hilt:hilt-navigation-compose", version = "1.2.0" }

# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
room-paging = { module = "androidx.room:room-paging", version.ref = "room" }
room-testing = { module = "androidx.room:room-testing", version.ref = "room" }

# Networking
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-serialization = { module = "com.squareup.retrofit2:converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-logging = { module = "com.squareup.okhttp3:logging-interceptor", version.ref = "okhttp" }
okhttp-mockwebserver = { module = "com.squareup.okhttp3:mockwebserver", version.ref = "okhttp" }
serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }

# Other Jetpack
lifecycle-runtime = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
paging-runtime = { module = "androidx.paging:paging-runtime", version.ref = "paging" }
paging-compose = { module = "androidx.paging:paging-compose", version.ref = "paging" }
datastore = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
work-runtime = { module = "androidx.work:work-runtime-ktx", version.ref = "work" }
biometric = { module = "androidx.biometric:biometric", version = "1.2.0-alpha05" }
security-crypto = { module = "androidx.security:security-crypto", version = "1.1.0-alpha06" }
splashscreen = { module = "androidx.core:core-splashscreen", version = "1.0.1" }
camerax-core = { module = "androidx.camera:camera-core", version.ref = "camerax" }
camerax-camera2 = { module = "androidx.camera:camera-camera2", version.ref = "camerax" }
camerax-lifecycle = { module = "androidx.camera:camera-lifecycle", version.ref = "camerax" }
camerax-view = { module = "androidx.camera:camera-view", version.ref = "camerax" }

# Firebase
firebase-bom = { module = "com.google.firebase:firebase-bom", version = "33.7.0" }
firebase-messaging = { module = "com.google.firebase:firebase-messaging-ktx" }
firebase-analytics = { module = "com.google.firebase:firebase-analytics-ktx" }
firebase-crashlytics = { module = "com.google.firebase:firebase-crashlytics-ktx" }

# Image
coil = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }

# ML Kit
mlkit-face = { module = "com.google.mlkit:face-detection", version = "16.1.7" }
mlkit-barcode = { module = "com.google.mlkit:barcode-scanning", version = "17.3.0" }

# Logging & Debug
timber = { module = "com.jakewharton.timber:timber", version.ref = "timber" }
leakcanary = { module = "com.squareup.leakcanary:leakcanary-android", version = "2.14" }

# Testing
junit = { module = "junit:junit", version = "4.13.2" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.1.0-1.0.29" }
```

---

# ANDROID CONCEPTS COVERED (FINAL CHECKLIST)

```
ARCHITECTURE:
  ✓ MVVM + Clean Architecture     ✓ Multi-Module (15+ modules)
  ✓ Repository Pattern            ✓ NetworkBoundResource
  ✓ Convention Plugins            ✓ Version Catalog
  ✓ Unidirectional Data Flow      ✓ Resource sealed class

UI / COMPOSE:
  ✓ Material 3 Design System      ✓ Custom Composables (20+)
  ✓ Compose Navigation (typed)    ✓ BottomNavigation + Nested Graphs
  ✓ LazyColumn / LazyRow          ✓ LazyVerticalGrid
  ✓ HorizontalPager (carousel)    ✓ BottomSheet (Modal)
  ✓ Pull-to-Refresh               ✓ Shimmer / Placeholder
  ✓ Animations (AnimatedVisibility, AnimatedContent)
  ✓ Canvas Drawing (credit score gauge, charts)
  ✓ Custom PIN Pad                ✓ OTP Input Field
  ✓ Currency Formatting           ✓ Dark/Light Theme

DATA:
  ✓ Room (complex schema, 10+ entities, relations, FTS)
  ✓ Room Migrations               ✓ Room Paging Source
  ✓ DataStore Preferences         ✓ EncryptedSharedPreferences
  ✓ Paging 3 (remote + local)     ✓ Type Converters
  ✓ SQLCipher encryption

NETWORKING:
  ✓ Retrofit + OkHttp             ✓ KotlinX Serialization
  ✓ Interceptors (Auth, Logging)  ✓ TokenAuthenticator
  ✓ Certificate Pinning           ✓ Multipart Upload
  ✓ Error handling (sealed class) ✓ MockWebServer (testing)

DI:
  ✓ Hilt (multi-module)           ✓ @HiltViewModel
  ✓ @Binds, @Provides             ✓ Custom scopes

BACKGROUND:
  ✓ Coroutines + Flow             ✓ StateFlow + SharedFlow
  ✓ WorkManager (scheduled payments)

SECURITY:
  ✓ BiometricPrompt               ✓ MPIN with BCrypt
  ✓ EncryptedSharedPreferences    ✓ SQLCipher
  ✓ SSL Certificate Pinning       ✓ Play Integrity API
  ✓ FLAG_SECURE                   ✓ R8 / ProGuard
  ✓ Session Timeout               ✓ Root Detection

PLATFORM APIS:
  ✓ CameraX                       ✓ ML Kit (face, barcode)
  ✓ FCM Push Notifications        ✓ Notification Channels
  ✓ Deep Links                    ✓ SMS Retriever API
  ✓ FileProvider                  ✓ WebView
  ✓ Clipboard Manager             ✓ In-App Review
  ✓ SplashScreen API              ✓ Per-App Language
  ✓ ProcessLifecycleOwner

TESTING:
  ✓ JUnit                         ✓ Mockk
  ✓ Turbine (Flow testing)        ✓ Compose Testing
  ✓ MockWebServer                 ✓ Room in-memory DB
  ✓ Coroutine Test Dispatcher     ✓ Paparazzi (screenshots)

DEVOPS:
  ✓ GitHub Actions CI/CD          ✓ Baseline Profiles
  ✓ Detekt + Ktlint               ✓ Build Variants

TOTAL: 70+ Android concepts in one app
```
