plugins { id("com.android.library"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.smartresponsor.core.push"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies { implementation("com.squareup.okhttp3:okhttp:4.12.0"); implementation("androidx.core:core-ktx:1.12.0") }

dependencies {
    implementation(project(":client-contract"))
    implementation(project(":client-data"))
    implementation(project(":client-usecase"))
}
