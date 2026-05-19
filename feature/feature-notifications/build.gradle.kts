plugins {
    id("nexusbank.android.feature")
}

android {
    namespace = "com.example.nexusbank.feature.notifications"
    resourcePrefix = "notifications_"
}

dependencies {
    implementation(project(":core:core-database"))
    implementation(project(":core:core-security"))
}
