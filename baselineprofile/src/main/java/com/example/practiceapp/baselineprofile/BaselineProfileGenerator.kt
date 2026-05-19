package com.example.practiceapp.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a baseline profile for the NexusBank app.
 *
 * Output is collected by the Android Gradle Plugin and merged into the app's
 * `src/release/generated/baselineProfiles/baseline-prof.txt`, which the
 * `ProfileInstaller` library installs on first launch. This yields ~15-30%
 * faster cold-start and scroll performance on supported devices (API 28+
 * pre-compiles, API 24-27 falls back to a `startup-prof` partial install).
 *
 * Regenerate after large UI changes:
 *   ./gradlew :app:generateReleaseBaselineProfile
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = "com.example.practiceapp",
        includeInStartupProfile = true,
    ) {
        // Cold start
        pressHome()
        startActivityAndWait()

        // Give the first frame time to render and any initial Compose work to
        // commit before we exit. Adjust the resource id / content-description
        // below to the first stable element on the start screen so the
        // generator captures the full critical path.
        device.wait(Until.hasObject(By.pkg("com.example.practiceapp").depth(0)), 5_000)
    }
}
