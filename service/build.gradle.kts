plugins {
    id("com.android.library")
}

android {
    namespace = "io.github.libxposed.service"
    compileSdk = 33
    buildToolsVersion = "33.0.1"

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }

    buildFeatures {
        androidResources = false
        buildConfig = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":interface"))
    compileOnly("androidx.annotation:annotation:1.5.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.2.2")
}
