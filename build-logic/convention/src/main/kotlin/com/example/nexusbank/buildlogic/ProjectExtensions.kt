package com.example.nexusbank.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal fun Project.libs(): VersionCatalog =
    extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = 36

        defaultConfig {
            minSdk = 24
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        configureLint(this@configureKotlinAndroid)
    }

    extensions.configure(KotlinAndroidProjectExtension::class.java) {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

/**
 * Centralized Android Lint configuration applied to every module.
 *
 * - `checkDependencies = true` runs lint transitively across module deps so
 *   issues in :core surface when linting :app, not only when the leaf module
 *   is built in isolation.
 * - `abortOnError = true` fails any local build with a lint error — keeps
 *   the main branch clean.
 * - `warningsAsErrors` is toggled on in CI (set `-Pci=true` or the `CI`
 *   environment variable) so local dev stays fast while CI is strict.
 */
private fun CommonExtension<*, *, *, *, *, *>.configureLint(project: Project) {
    val isCi = project.providers.gradleProperty("ci").orNull == "true" ||
        System.getenv("CI")?.equals("true", ignoreCase = true) == true

    lint {
        checkDependencies = true
        abortOnError = true
        warningsAsErrors = isCi

        htmlReport = true
        xmlReport = true
        sarifReport = true

        checkTestSources = false
        checkGeneratedSources = false
    }
}

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }
    }

    // Strong-skipping mode is the default in the Kotlin 2.0.20+ Compose
    // compiler — no explicit opt-in needed. Here we enable the compiler
    // metrics + reports output (`<module>/build/compose_metrics`) so we can
    // audit recomposition stability after large UI changes:
    //   ./gradlew :feature:feature-dashboard:assembleRelease
    //   open feature/feature-dashboard/build/compose_metrics
    extensions.findByName("composeCompiler")?.let { ext ->
        val composeExt =
            ext as org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
        val metricsDir = layout.buildDirectory.dir("compose_metrics")
        composeExt.metricsDestination.set(metricsDir)
        composeExt.reportsDestination.set(metricsDir)
    }

    val bom = libs().findLibrary("androidx-compose-bom").get()
    dependencies.apply {
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))
        add("implementation", libs().findLibrary("androidx-compose-ui").get())
        add("implementation", libs().findLibrary("androidx-compose-ui-graphics").get())
        add("implementation", libs().findLibrary("androidx-compose-ui-tooling-preview").get())
        add("implementation", libs().findLibrary("androidx-compose-material3").get())
        add("implementation", libs().findLibrary("androidx-compose-material-icons-extended").get())
        add("debugImplementation", libs().findLibrary("androidx-compose-ui-tooling").get())
        add("debugImplementation", libs().findLibrary("androidx-compose-ui-test-manifest").get())
    }
}
