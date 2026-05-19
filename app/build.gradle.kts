plugins {
    id("nexusbank.android.application")
    id("nexusbank.android.application.compose")
    id("nexusbank.android.hilt")
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.example.practiceapp"

    defaultConfig {
        applicationId = "com.example.practiceapp"
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // A non-debuggable build type is required for the baseline-profile
            // plugin to generate the `release` benchmark variant.
        }
    }
}

dependencies {
    // Core modules
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-network"))
    implementation(project(":core:core-security"))
    implementation(project(":core:core-ui"))

    // Feature modules
    implementation(project(":feature:feature-auth"))
    implementation(project(":feature:feature-onboarding"))
    implementation(project(":feature:feature-dashboard"))
    implementation(project(":feature:feature-transfers"))
    implementation(project(":feature:feature-transactions"))
    implementation(project(":feature:feature-profile"))
    implementation(project(":feature:feature-about"))
    implementation(project(":feature:feature-statement"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.navigation.compose)

    // Installs the precompiled baseline profile produced by :baselineprofile
    // into the APK on first launch — significant startup win on cold start.
    implementation(libs.androidx.profileinstaller)

    // Memory-leak detection (debug builds only — never shipped to release).
    debugImplementation(libs.leakcanary.android)

    // Wires the consumer side of the baseline-profile plugin.
    "baselineProfile"(project(":baselineprofile"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}