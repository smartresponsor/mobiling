plugins { id("com.android.library"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }
android { namespace = "com.smartresponsor.core.entitlement"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies {
  implementation("androidx.room:room-runtime:2.6.1"); implementation("androidx.room:room-ktx:2.6.1"); kapt("androidx.room:room-compiler:2.6.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
