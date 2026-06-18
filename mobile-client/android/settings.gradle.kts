pluginManagement {
    repositories {
        google()
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

rootProject.name = "Mobiling"

include(
    ":app",
    ":client-contract",
    ":client-data",
    ":client-usecase",
    ":client-navigation",
    ":client-ui",
    ":core:config",
    ":core:entitlement",
    ":core:billing",
    ":core:analytic",
    ":core:push",
    ":core:security",
)

project(":client-contract").projectDir = file("Contract")
project(":client-data").projectDir = file("Data")
project(":client-usecase").projectDir = file("UseCase")
project(":client-navigation").projectDir = file("Navigation")
project(":client-ui").projectDir = file("UI")
