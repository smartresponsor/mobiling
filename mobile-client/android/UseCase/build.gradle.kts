plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "app.mobiling.client.client.usecase"
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
            implementation(project(":client-data"))
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
        }




