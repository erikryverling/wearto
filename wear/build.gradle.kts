plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.wear)

    implementation(libs.timber)

    implementation(libs.playServices.wearable)

    implementation(libs.horologist.datalayer.watch)
    implementation(libs.horologist.datalayer.phone)
}

android {
    namespace = "se.yverling.wearto.wear"

    compileSdk = Versions.compileSdk

    compileOptions {
        // KSP only supports Java 17
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = Versions.jvmTarget
    }

    defaultConfig {
        minSdk = Versions.minSdk

        applicationId = "se.yverling.wearto"

        versionCode = 10000 // Version & release number
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}
