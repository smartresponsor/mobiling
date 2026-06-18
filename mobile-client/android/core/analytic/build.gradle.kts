plugins { id("com.android.library"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.smartresponsor.core.analytic"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies { implementation("androidx.core:core-ktx:1.12.0"); implementation("com.squareup.okhttp3:okhttp:4.12.0") }
