plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.smartresponsor.mobile.client.navigation"
    compileSdk = 34
    defaultConfig { minSdk = 24 }
    sourceSets["main"].java.srcDirs(".")
}
