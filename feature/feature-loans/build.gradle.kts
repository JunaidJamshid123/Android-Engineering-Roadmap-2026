plugins {
    id("nexusbank.android.feature")
}

android {
    namespace = "com.example.nexusbank.feature.loans"
    resourcePrefix = "loans_"
}

dependencies {
    implementation(project(":core:core-database"))
    implementation(project(":core:core-security"))
}
