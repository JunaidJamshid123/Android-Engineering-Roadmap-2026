package com.example.practiceapp.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measures cold-start times with and without the generated baseline profile
 * so the speed-up is visible in CI. Run with:
 *
 *   ./gradlew :baselineprofile:connectedBenchmarkAndroidTest
 *
 * Compare `StartupTimingMetric` results for `None` vs `Partial` (baseline
 * profile installed) vs `Full` (AOT-compiled).
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupNoCompilation() = startup(CompilationMode.None())

    @Test
    fun startupBaselineProfile() = startup(CompilationMode.Partial())

    @Test
    fun startupFullCompilation() = startup(CompilationMode.Full())

    private fun startup(mode: CompilationMode) = rule.measureRepeated(
        packageName = "com.example.practiceapp",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = mode,
        startupMode = StartupMode.COLD,
        iterations = 5,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
        device.wait(Until.hasObject(By.pkg("com.example.practiceapp").depth(0)), 5_000)
    }
}
