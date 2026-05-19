plugins {
    id("nexusbank.android.feature")
}

android {
    namespace = "com.example.nexusbank.feature.transactions"
    resourcePrefix = "transactions_"
}

dependencies {
    implementation(project(":core:core-network"))
}
