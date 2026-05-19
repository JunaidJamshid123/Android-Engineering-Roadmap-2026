plugins {
    id("nexusbank.android.feature")
}

android {
    namespace = "com.example.nexusbank.feature.cards"
    resourcePrefix = "cards_"
}

dependencies {
    implementation(project(":core:core-network"))
    implementation(project(":core:core-database"))
}
