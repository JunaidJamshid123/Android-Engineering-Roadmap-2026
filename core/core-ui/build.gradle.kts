plugins {
    id("nexusbank.android.library")
    id("nexusbank.android.library.compose")
}

android {
    namespace = "com.example.nexusbank.core.ui"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
}
