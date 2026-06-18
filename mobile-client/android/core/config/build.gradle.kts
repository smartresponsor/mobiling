plugins { id("com.android.library"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.smartresponsor.core.config"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies {
  implementation("androidx.datastore:datastore-preferences:1.0.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("androidx.work:work-runtime-ktx:2.9.0")
  implementation("androidx.core:core-ktx:1.12.0")
}

dependencies {
    implementation(project(":client-contract"))
    implementation(project(":client-data"))
    implementation(project(":client-usecase"))
}
