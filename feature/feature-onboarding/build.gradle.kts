plugins {
    id("nexusbank.android.library")
    id("nexusbank.android.library.compose")
}

android {
    namespace = "com.example.nexusbank.feature.onboarding"
    resourcePrefix = "onboarding_"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(libs.junit)
}
