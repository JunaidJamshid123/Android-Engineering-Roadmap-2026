plugins {
    id("nexusbank.android.feature")
}

android {
    namespace = "com.example.nexusbank.feature.profile"
    resourcePrefix = "profile_"
}

dependencies {
    implementation(project(":core:core-network"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-security"))

    implementation(libs.androidx.activity.compose)
}
