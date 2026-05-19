plugins {
    id("nexusbank.android.library")
    id("nexusbank.android.hilt")
}

android {
    namespace = "com.example.nexusbank.core.data"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-network"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-common"))

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.serialization.json)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
}
