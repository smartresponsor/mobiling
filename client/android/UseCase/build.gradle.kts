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
    namespace = "app.mobiling.client.usecase"
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
            implementation(project(":client-data"))
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
        }




