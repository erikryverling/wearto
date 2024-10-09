plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "se.yverling.wearto.mobile.common.design"

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
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.mobile)

    implementation(libs.timber)
}
