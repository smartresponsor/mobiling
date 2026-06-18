plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }
android {
    namespace = "com.smartresponsor.mobile"
    compileSdk = 34
    defaultConfig { applicationId = "com.smartresponsor.mobile"; minSdk = 24; targetSdk = 34; versionCode = 1; versionName = "0.1.0-materialized" }
    buildTypes {
        getByName("release") { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") }
        getByName("debug") { isMinifyEnabled = false }
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.3" }
}
dependencies {
    implementation(project(":client-contract")); implementation(project(":client-data")); implementation(project(":client-usecase")); implementation(project(":client-navigation")); implementation(project(":client-ui"))
    implementation(project(":core:config")); implementation(project(":core:entitlement")); implementation(project(":core:billing")); implementation(project(":core:analytic")); implementation(project(":core:push")); implementation(project(":core:security"))
    implementation("androidx.core:core-ktx:1.12.0"); implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.6.0"); implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
