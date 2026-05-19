plugins {
    id("nexusbank.android.library")
    id("nexusbank.android.hilt")
}

android {
    namespace = "com.example.nexusbank.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    testImplementation(libs.junit)
}
