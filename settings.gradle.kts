pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PracticeApp"
include(":app")

// Core modules
include(":core:core-ui")
include(":core:core-data")
include(":core:core-network")
include(":core:core-database")
include(":core:core-domain")
include(":core:core-security")
include(":core:core-common")
include(":core:core-testing")

// Feature modules
include(":feature:feature-auth")
include(":feature:feature-onboarding")
include(":feature:feature-dashboard")
