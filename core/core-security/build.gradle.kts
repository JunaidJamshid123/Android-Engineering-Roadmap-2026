plugins {
    id("nexusbank.android.library")
    id("nexusbank.android.hilt")
}

android {
    namespace = "com.example.nexusbank.core.security"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-network"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.security.crypto)
    implementation(libs.androidx.biometric)
    implementation(libs.play.integrity)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation("com.google.android.gms:play-services-tasks:18.2.0")

    testImplementation(libs.junit)
}
