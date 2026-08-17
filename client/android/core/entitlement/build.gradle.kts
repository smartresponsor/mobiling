plugins { id("com.android.library"); id("com.google.devtools.ksp") }
android { namespace = "app.mobiling.core.entitlement"; compileSdk = 34; defaultConfig { minSdk = 24 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
 }
dependencies {
  implementation("androidx.room:room-runtime:2.8.4"); implementation("androidx.room:room-ktx:2.8.4"); ksp("androidx.room:room-compiler:2.8.4")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

dependencies {
    implementation(project(":client-contract"))
    implementation(project(":client-data"))
    implementation(project(":client-usecase"))
}




