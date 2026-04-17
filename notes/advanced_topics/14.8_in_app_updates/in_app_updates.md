# In-App Updates

## Overview

The Google Play In-App Updates API lets you prompt users to update your app **without leaving it**. It supports two update flows: **Flexible** (background download) and **Immediate** (blocking full-screen).

```
┌──────────────────────────────────────────────────────────────┐
│              In-App Update Types                              │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  FLEXIBLE UPDATE                  IMMEDIATE UPDATE            │
│  ┌─────────────────────┐         ┌─────────────────────┐     │
│  │ ┌─────────────────┐ │         │                     │     │
│  │ │  Your App UI    │ │         │   ┌─────────────┐   │     │
│  │ │  (running       │ │         │   │  UPDATING   │   │     │
│  │ │   normally)     │ │         │   │             │   │     │
│  │ └─────────────────┘ │         │   │  ████░░░░   │   │     │
│  │ ┌─────────────────┐ │         │   │  65%        │   │     │
│  │ │ ⬇ Downloading.. │ │         │   │             │   │     │
│  │ │ ████████░░ 80%  │ │         │   │ Please wait │   │     │
│  │ └─────────────────┘ │         │   └─────────────┘   │     │
│  └─────────────────────┘         └─────────────────────┘     │
│                                                               │
│  • User can continue            • Full-screen blocking       │
│    using the app                • User MUST update            │
│  • Snackbar prompts             • App restarts after          │
│    restart when ready             update completes            │
│  • Good for: minor updates      • Good for: critical         │
│                                   security/breaking fixes     │
│                                                               │
│  FLOW COMPARISON:                                             │
│                                                               │
│  Flexible:                                                    │
│  Check → Prompt → Download (background) → Snackbar → Restart │
│                                                               │
│  Immediate:                                                   │
│  Check → Full-screen UI → Download → Install → App resumes   │
└──────────────────────────────────────────────────────────────┘
```

---

## Setup

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
}
```

---

## 1. Flexible Updates

The user can continue using the app while the update downloads in the background. When ready, a snackbar prompts them to restart.

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.FLEXIBLE

    // Activity result launcher for update flow
    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            // User declined or update failed
            Log.d("Update", "Update flow failed: ${result.resultCode}")
            // Optionally re-prompt later
        }
    }

    // Listener for download state changes
    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                // Update downloaded — prompt user to restart
                showUpdateSnackbar()
            }
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytes = state.totalBytesToDownload()
                val progress = (bytesDownloaded * 100 / totalBytes).toInt()
                // Optionally show progress
                updateProgressBar(progress)
            }
            InstallStatus.FAILED -> {
                Log.e("Update", "Download failed")
            }
            InstallStatus.INSTALLED -> {
                // Clean up listener
                appUpdateManager.unregisterListener(installStateListener)
            }
            else -> { /* PENDING, INSTALLING, etc. */ }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdate()
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            val isUpdateAvailable =
                updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

            val isUpdateAllowed = updateInfo.isUpdateTypeAllowed(updateType)

            if (isUpdateAvailable && isUpdateAllowed) {
                // Register listener for flexible update progress
                appUpdateManager.registerListener(installStateListener)

                // Start the update
                appUpdateManager.startUpdateFlowForResult(
                    updateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
            }
        }
    }

    private fun showUpdateSnackbar() {
        Snackbar.make(
            findViewById(R.id.root_layout),
            "Update downloaded. Restart to apply.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") {
                appUpdateManager.completeUpdate() // Triggers app restart
            }
            show()
        }
    }

    // Handle case: user returns to app after download completed
    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                // Update was already downloaded while app was in background
                showUpdateSnackbar()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateListener)
    }
}
```

---

## 2. Immediate Updates

The update takes over the full screen. The user cannot use the app until the update is installed.

```kotlin
class CriticalUpdateActivity : AppCompatActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            // For immediate updates, you may want to force close
            // or re-prompt because this update is critical
            Log.e("Update", "Immediate update failed or cancelled")
            checkForUpdate() // Re-prompt
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdate()
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            val isUpdateAvailable =
                updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

            val isUpdateAllowed = updateInfo.isUpdateTypeAllowed(updateType)

            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(
                    updateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
            }
        }
    }

    // CRITICAL: Resume immediate update if user navigated away
    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                // An immediate update was started but didn't complete
                // Re-start the update flow
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
            }
        }
    }
}
```

---

## 3. Update Flow Handling — Complete Manager

```kotlin
/**
 * Reusable update manager that handles both update types
 * with staleness checks and priority-based decisions.
 */
class InAppUpdateManager(
    private val activity: AppCompatActivity,
    private val rootView: View
) {
    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private var installStateListener: InstallStateUpdatedListener? = null

    private val updateLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != AppCompatActivity.RESULT_OK) {
            handleUpdateFailure()
        }
    }

    /**
     * Check for updates and decide which flow to use based on priority.
     *
     * Google Play Console update priority (0-5):
     *   0 = default (no urgency)
     *   1-2 = low priority → flexible update
     *   3-4 = medium priority → flexible with staleness check
     *   5 = critical → immediate update
     */
    fun checkForUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) return@addOnSuccessListener

            val priority = info.updatePriority()  // Set in Play Console
            val staleDays = info.clientVersionStalenessDays() ?: 0

            when {
                // Critical update → force immediate
                priority >= 5 -> startImmediateUpdate(info)

                // High priority OR user ignored for 7+ days → immediate
                priority >= 3 && staleDays >= 7 -> startImmediateUpdate(info)

                // Medium priority → flexible
                priority >= 3 -> startFlexibleUpdate(info)

                // Low priority but stale for 30+ days → flexible
                staleDays >= 30 -> startFlexibleUpdate(info)

                // Low priority, recent → do nothing (or show subtle hint)
                else -> { /* Optional: show "Update available" badge */ }
            }
        }
    }

    private fun startFlexibleUpdate(info: AppUpdateInfo) {
        if (!info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) return

        installStateListener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> showRestartSnackbar()
                InstallStatus.FAILED -> handleUpdateFailure()
                InstallStatus.INSTALLED -> cleanup()
                else -> {}
            }
        }
        appUpdateManager.registerListener(installStateListener!!)

        appUpdateManager.startUpdateFlowForResult(
            info,
            updateLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
        )
    }

    private fun startImmediateUpdate(info: AppUpdateInfo) {
        if (!info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) return

        appUpdateManager.startUpdateFlowForResult(
            info,
            updateLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        )
    }

    private fun showRestartSnackbar() {
        Snackbar.make(rootView, "Update ready to install", Snackbar.LENGTH_INDEFINITE)
            .setAction("RESTART") { appUpdateManager.completeUpdate() }
            .show()
    }

    private fun handleUpdateFailure() {
        // Log analytics, optionally retry
    }

    fun resumeUpdateIfNeeded() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.installStatus() == InstallStatus.DOWNLOADED -> {
                    showRestartSnackbar()
                }
                info.updateAvailability() ==
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    startImmediateUpdate(info)
                }
            }
        }
    }

    fun cleanup() {
        installStateListener?.let {
            appUpdateManager.unregisterListener(it)
        }
    }
}

// Usage in Activity:
class MainActivity : AppCompatActivity() {
    private lateinit var updateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateManager = InAppUpdateManager(this, findViewById(R.id.root_layout))
        updateManager.checkForUpdates()
    }

    override fun onResume() {
        super.onResume()
        updateManager.resumeUpdateIfNeeded()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateManager.cleanup()
    }
}
```

---

## Decision Flowchart

```
┌──────────────────────────────────────────────────────────────┐
│              Which Update Type to Use?                         │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Is this a critical security or breaking change?              │
│       │                                                       │
│   ┌───▼───┐    ┌───────────────────────────┐                 │
│   │  YES  │───▶│  IMMEDIATE UPDATE          │                 │
│   └───────┘    │  • Full-screen blocking    │                 │
│                │  • User must update        │                 │
│   ┌───▼───┐   │  • Priority 5              │                 │
│   │  NO   │    └───────────────────────────┘                 │
│   └───┬───┘                                                   │
│       │                                                       │
│       ▼                                                       │
│  Has user ignored for 7+ days?                                │
│       │                                                       │
│   ┌───▼───┐    ┌───────────────────────────┐                 │
│   │  YES  │───▶│  IMMEDIATE UPDATE          │                 │
│   └───────┘    │  (escalate from flexible)  │                 │
│                └───────────────────────────┘                 │
│   ┌───▼───┐                                                  │
│   │  NO   │                                                   │
│   └───┬───┘                                                   │
│       │                                                       │
│       ▼                                                       │
│  ┌───────────────────────────┐                               │
│  │  FLEXIBLE UPDATE          │                               │
│  │  • Background download    │                               │
│  │  • User continues working │                               │
│  │  • Snackbar when ready    │                               │
│  └───────────────────────────┘                               │
└──────────────────────────────────────────────────────────────┘
```

---

## Testing In-App Updates

```kotlin
// Use FakeAppUpdateManager for testing
class UpdateTest {
    @Test
    fun testFlexibleUpdate() {
        val fakeUpdateManager = FakeAppUpdateManager(context).apply {
            setUpdateAvailable(2)  // Version code 2 is available
            setUpdatePriority(3)   // Medium priority
        }

        // Simulate download
        fakeUpdateManager.downloadStarts()
        fakeUpdateManager.downloadCompletes()

        // Verify state
        assertTrue(fakeUpdateManager.isConfirmationDialogVisible)
        assertEquals(InstallStatus.DOWNLOADED,
            fakeUpdateManager.fakeInstallState?.installStatus())
    }
}
```
