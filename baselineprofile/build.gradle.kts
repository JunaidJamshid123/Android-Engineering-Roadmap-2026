import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.example.practiceapp.baselineprofile"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        minSdk = 28 // Baseline profiles require API 28+
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // A non-debuggable target build type is required to collect profiles.
    targetProjectPath = ":app"

    // Headless Gradle Managed Device used to generate the profile on CI/dev.
    // Run: `./gradlew :app:generateReleaseBaselineProfile`
    @Suppress("UnstableApiUsage")
    testOptions.managedDevices.allDevices {
        create<ManagedVirtualDevice>("pixel6Api34") {
            device = "Pixel 6"
            apiLevel = 34
            systemImageSource = "aosp"
        }
    }
}

// Use the managed device above and run benchmarks against the release build.
baselineProfile {
    managedDevices += "pixel6Api34"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
