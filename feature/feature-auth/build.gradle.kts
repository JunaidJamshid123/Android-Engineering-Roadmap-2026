plugins {
    id("nexusbank.android.feature")
}

android {
    namespace = "com.example.nexusbank.feature.auth"
    resourcePrefix = "auth_"
}

dependencies {
    implementation(project(":core:core-network"))
    implementation(project(":core:core-security"))
    implementation(project(":core:core-database"))
}
