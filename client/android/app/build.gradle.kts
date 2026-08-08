import java.util.Properties

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun quotedBuildConfigValue(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
android {
    namespace = "app.mobiling.client"
    compileSdk = 34
    defaultConfig {
        applicationId = "app.mobiling.client"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.2.0-access-entry-materialized"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        flavorDimensions += "brand"
        buildConfigField("String", "PRODUCT_PROFILE", quotedBuildConfigValue("one_tasker"))
        buildConfigField("String", "ENVIRONMENT_PROFILE", quotedBuildConfigValue("local"))
        buildConfigField("String", "MOBILE_EDGE_BASE_URL", quotedBuildConfigValue("http://10.0.2.2:8080"))
        buildConfigField("String", "INITIAL_DESTINATION", quotedBuildConfigValue("vendor/project"))
        buildConfigField("String", "PRIMARY_CATALOG", quotedBuildConfigValue("services"))
        buildConfigField("String", "ENABLED_CATALOGS", quotedBuildConfigValue("services,products,projects"))
        buildConfigField("String", "AVAILABLE_RETAIL_KINDS", quotedBuildConfigValue("service,goods,project"))
    }
    productFlavors {
        create("oneTasker") {
            dimension = "brand"
            applicationIdSuffix = ".onetasker"
            buildConfigField("String", "BRAND_PROFILE", quotedBuildConfigValue("one_tasker"))
            buildConfigField("String", "PRODUCT_PROFILE", quotedBuildConfigValue("one_tasker"))
            buildConfigField("String", "PRIMARY_CATALOG", quotedBuildConfigValue("services"))
            buildConfigField("String", "AVAILABLE_RETAIL_KINDS", quotedBuildConfigValue("task,service"))
        }
        create("smartResponsor") {
            dimension = "brand"
            applicationIdSuffix = ".smartresponsor"
            buildConfigField("String", "BRAND_PROFILE", quotedBuildConfigValue("smart_responsor"))
            buildConfigField("String", "PRODUCT_PROFILE", quotedBuildConfigValue("platform"))
            buildConfigField("String", "AVAILABLE_RETAIL_KINDS", quotedBuildConfigValue("service,goods,project"))
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            buildConfigField("String", "DEBUG_LOGIN", quotedBuildConfigValue(""))
            buildConfigField("String", "DEBUG_PASSWORD", quotedBuildConfigValue(""))
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = false
            buildConfigField("String", "DEBUG_LOGIN", quotedBuildConfigValue(localProperties.getProperty("mobiling.debug.login", "")))
            buildConfigField("String", "DEBUG_PASSWORD", quotedBuildConfigValue(localProperties.getProperty("mobiling.debug.password", "")))
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.3" }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
dependencies {
    implementation(project(":client-contract")); implementation(project(":client-data")); implementation(project(":client-usecase")); implementation(project(":client-navigation")); implementation(project(":client-ui"))
    implementation(project(":core:config")); implementation(project(":core:entitlement")); implementation(project(":core:billing")); implementation(project(":core:analytic")); implementation(project(":core:push")); implementation(project(":core:security"))
    implementation("androidx.core:core-ktx:1.12.0"); implementation("androidx.activity:activity-compose:1.8.2"); implementation("androidx.appcompat:appcompat:1.7.1")
    implementation(platform("androidx.compose:compose-bom:2024.02.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-core")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.github.yalantis:ucrop:2.2.11")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-web:3.5.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")
    testImplementation("junit:junit:4.13.2")
    androidTestUtil("androidx.test:orchestrator:1.4.2")
}
