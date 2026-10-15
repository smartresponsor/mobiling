plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "app.mobiling.widget"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.mobiling.client.onetasker.widget"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "PRODUCT_PROFILE", "\"one_tasker_widget\"")
        buildConfigField("String", "BRAND_PROFILE", "\"one_tasker\"")
        buildConfigField("String", "ENVIRONMENT_PROFILE", "\"local\"")
        buildConfigField("String", "MOBILE_EDGE_BASE_URL", "\"http://10.0.2.2:8080\"")
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"https://app.smartresponsor.com/page/privacy-policy\"")
        buildConfigField("String", "TERMS_OF_SERVICE_URL", "\"https://app.smartresponsor.com/page/terms-of-service\"")
        buildConfigField("String", "ABOUT_URL", "\"https://app.smartresponsor.com/page/about\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":client-contract"))
    implementation(project(":client-data"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(platform("androidx.compose:compose-bom:2024.02.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
}
