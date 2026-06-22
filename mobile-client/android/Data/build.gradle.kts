plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "app.mobiling.client.client.data"
    compileSdk = 34
    defaultConfig { minSdk = 24 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
        val moduleRoot = file(".")
    val moduleSourceDir = moduleRoot
        .listFiles()
        ?.filter { candidate ->
            candidate.isDirectory &&
                candidate.name != "build" &&
                candidate.name != ".gradle" &&
                candidate.walkTopDown().any { source -> source.isFile && source.extension == "kt" }
        }
        ?.map { candidate -> candidate.path }
        ?: emptyList()

    sourceSets["main"].java.setSrcDirs(moduleSourceDir)
}


        dependencies {
            implementation(project(":client-contract"))
            implementation("androidx.core:core-ktx:1.12.0")
            implementation("androidx.datastore:datastore-preferences:1.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
        }




