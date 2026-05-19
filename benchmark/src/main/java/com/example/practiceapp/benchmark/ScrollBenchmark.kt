package com.example.practiceapp.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measures frame-timing while scrolling the transactions / dashboard list.
 *
 * Captures p50/p95/p99 frame durations via [FrameTimingMetric]. Any spikes
 * here typically point at Compose recompositions, unbounded LazyColumn
 * items, or main-thread I/O during scrolling — exactly what StrictMode
 * flags in debug builds.
 *
 * To make this useful, tag the main scrollable in the app with
 * `Modifier.testTag("scrollable_content")`. The test will skip gracefully
 * if it can't find the tag.
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollCompilationNone() = scroll(CompilationMode.None())

    @Test
    fun scrollCompilationBaselineProfile() = scroll(CompilationMode.Partial())

    private fun scroll(mode: CompilationMode) = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = mode,
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), 5_000)
        },
    ) {
        val scrollable = device.findObject(By.res(TARGET_PACKAGE, "scrollable_content"))
            ?: device.findObject(By.scrollable(true))
            ?: return@measureRepeated

        scrollable.setGestureMargin(device.displayWidth / 5)
        repeat(3) {
            scrollable.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }
}
