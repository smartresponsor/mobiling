plugins {
    id("com.android.library")
}

val moduleSourceDirs = file(".")
    .listFiles()
    ?.filter { candidate ->
        candidate.isDirectory &&
            candidate.name != "build" &&
            candidate.name != ".gradle" &&
            candidate.walkTopDown().any { source -> source.isFile && source.extension == "kt" }
    }
    ?.map { candidate -> candidate.path }
    ?: emptyList()

android {
    namespace = "app.mobiling.client.data"
    compileSdk = 34
    defaultConfig { minSdk = 24 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

androidComponents {
    onVariants { variant ->
        moduleSourceDirs.forEach { directory ->
            variant.sources.kotlin?.addStaticSourceDirectory(directory)
        }
    }
}

dependencies {
            implementation(project(":client-contract"))
            implementation("androidx.core:core-ktx:1.12.0")
            implementation("androidx.datastore:datastore-preferences:1.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
            api("com.squareup.okhttp3:okhttp:4.12.0")
        }




