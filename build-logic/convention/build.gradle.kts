plugins {
    `kotlin-dsl`
}

group = "com.example.nexusbank.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "nexusbank.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "nexusbank.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "nexusbank.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "nexusbank.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "nexusbank.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "nexusbank.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidFeature") {
            id = "nexusbank.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = "nexusbank.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("codeQuality") {
            id = "nexusbank.code.quality"
            implementationClass = "CodeQualityConventionPlugin"
        }
    }
}
