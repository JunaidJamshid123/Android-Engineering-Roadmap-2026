plugins {
    id("nexusbank.android.library")
}

android {
    namespace = "com.example.nexusbank.core.testing"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.junit)
}
