plugins {
    id("nexusbank.android.feature")
}

android {
    namespace = "com.example.nexusbank.feature.transfers"
    resourcePrefix = "transfers_"
}

dependencies {
    implementation(project(":core:core-network"))
}
