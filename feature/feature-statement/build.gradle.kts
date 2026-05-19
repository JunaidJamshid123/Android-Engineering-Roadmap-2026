plugins {
    id("nexusbank.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.nexusbank.feature.statement"
    resourcePrefix = "statement_"
}

dependencies {
    implementation(project(":core:core-network"))

    implementation(libs.serialization.json)
    implementation(libs.retrofit)
}
