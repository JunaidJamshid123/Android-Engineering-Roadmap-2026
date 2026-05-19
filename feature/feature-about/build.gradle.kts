plugins {
    id("nexusbank.android.library")
    id("nexusbank.android.library.compose")
}

android {
    namespace = "com.example.nexusbank.feature.about"
    resourcePrefix = "about_"
}

dependencies {
    implementation(project(":core:core-ui"))

    implementation(libs.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
}
