plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "uz.mebelar"
    compileSdk = 35

    defaultConfig {
        applicationId = "uz.mebelar"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Sinov uchun debug kalit bilan imzolanadi (har qanday telefonga o'rnatiladi)
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
   
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    // .glb fayllar siqilmasin
    androidResources { noCompress += listOf("glb", "gltf", "filamat", "ktx") }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("io.github.sceneview:arsceneview:2.3.3")
configurations.configureEach {
       resolutionStrategy {
           force("androidx.core:core:1.15.0", "androidx.core:core-ktx:1.15.0")
       }
   }
}
