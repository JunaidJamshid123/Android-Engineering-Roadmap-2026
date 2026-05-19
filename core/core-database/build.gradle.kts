plugins {
    id("nexusbank.android.library")
    id("nexusbank.android.hilt")
    id("nexusbank.android.room")
}

android {
    namespace = "com.example.nexusbank.core.database"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
}
