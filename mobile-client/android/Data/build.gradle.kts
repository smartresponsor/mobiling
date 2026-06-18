plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.smartresponsor.mobile.client.data"
    compileSdk = 34
    defaultConfig { minSdk = 24 }
    sourceSets["main"].java.srcDirs(".")
}


        dependencies {
            implementation(project(":client-contract"))
            implementation("androidx.core:core-ktx:1.12.0")
            implementation("androidx.datastore:datastore-preferences:1.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
        }
