plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.smartresponsor.mobile.client.ui"
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


        dependencies { implementation(project(":client-contract")) }




