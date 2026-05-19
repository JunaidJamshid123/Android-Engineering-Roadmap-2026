plugins {
    id("nexusbank.android.feature")
}

android {
    namespace = "com.example.nexusbank.feature.dashboard"
    resourcePrefix = "dashboard_"
}

dependencies {
    implementation(project(":core:core-network"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-security"))
    implementation(project(":feature:feature-auth"))

    // QR Code generation
    implementation(libs.zxing.core)
}
