plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.smartresponsor.mobile.client.usecase"
    compileSdk = 34
    defaultConfig { minSdk = 24 }
    sourceSets["main"].java.srcDirs(".")
}


        dependencies {
            implementation(project(":client-contract"))
            implementation(project(":client-data"))
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
        }
