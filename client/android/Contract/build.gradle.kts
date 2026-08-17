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
    namespace = "app.mobiling.client.contract"
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

