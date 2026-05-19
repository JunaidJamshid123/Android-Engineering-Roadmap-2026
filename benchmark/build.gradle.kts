import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.practiceapp.benchmark"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        minSdk = 24
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Benchmarks must run against the app's non-debuggable build.
    targetProjectPath = ":app"

    buildTypes {
        // Match the app's release build for realistic results.
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    @Suppress("UnstableApiUsage")
    testOptions.managedDevices.allDevices {
        create<ManagedVirtualDevice>("pixel6Api34") {
            device = "Pixel 6"
            apiLevel = 34
            systemImageSource = "aosp"
        }
    }
}

dependencies {
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    beforeVariants(selector().all()) {
        // Only run benchmarks against the `benchmark` variant of the app.
        it.enable = it.buildType == "benchmark"
    }
}
