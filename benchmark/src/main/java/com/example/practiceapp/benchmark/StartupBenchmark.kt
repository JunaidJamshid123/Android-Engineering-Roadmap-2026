package com.example.practiceapp.benchmark

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
 * Cold / warm / hot startup macrobenchmarks.
 *
 * Run with:
 *   ./gradlew :benchmark:connectedBenchmarkAndroidTest
 *
 * Compare timings across `CompilationMode.None` (worst case), `Partial`
 * (with the generated baseline profile installed), and `Full` (AOT) to
 * quantify the win from the baseline profile.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test fun coldStartupNoCompilation()  = startup(CompilationMode.None(),    StartupMode.COLD)
    @Test fun coldStartupBaselineProfile() = startup(CompilationMode.Partial(), StartupMode.COLD)
    @Test fun coldStartupFullCompilation() = startup(CompilationMode.Full(),    StartupMode.COLD)
    @Test fun warmStartupBaselineProfile() = startup(CompilationMode.Partial(), StartupMode.WARM)
    @Test fun hotStartupBaselineProfile()  = startup(CompilationMode.Partial(), StartupMode.HOT)

    private fun startup(mode: CompilationMode, startup: StartupMode) = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = mode,
        startupMode = startup,
        iterations = 5,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
        device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), 5_000)
    }
}
