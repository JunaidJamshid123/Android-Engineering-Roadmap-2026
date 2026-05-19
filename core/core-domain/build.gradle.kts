plugins {
    id("nexusbank.android.library")
}

android {
    namespace = "com.example.nexusbank.core.domain"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
}
