plugins {
    id("nexusbank.android.feature")
}

android {
    namespace = "com.example.nexusbank.feature.accounts"
    resourcePrefix = "accounts_"
}

dependencies {
    implementation(project(":core:core-network"))
    implementation(project(":core:core-database"))
}
