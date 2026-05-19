plugins {
    id("nexusbank.android.library")
    id("nexusbank.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.nexusbank.core.network"

    defaultConfig {
        // Fallback only — actual values come from buildTypes below.
        buildConfigField("String", "BASE_URL", "\"https://api.nexusbank.com/api/\"")
    }

    buildTypes {
        debug {
            // Android emulator loopback → host machine's localhost.
            // Cleartext is permitted for this host via network_security_config.
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:5000/api/\"")
        }
        release {
            // Production API. HTTPS-only; cert pinning enforced by OkHttp.
            buildConfigField("String", "BASE_URL", "\"https://api.nexusbank.com/api/\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:core-common"))

    api(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.serialization.json)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
}
