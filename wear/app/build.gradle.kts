apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.compose.gradle")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":wear:common:design-system"))
    implementation(project(":wear:data:items"))
    implementation(project(":wear:feature:items"))

    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.wear)

    implementation(libs.timber)

    implementation(libs.playServices.wearable)

    implementation(libs.horologist.datalayer.watch)
    implementation(libs.horologist.datalayer.phone)
}

android {
    namespace = "se.yverling.wearto.wear.app"

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
        targetSdk = Versions.targetSdk

        applicationId = "se.yverling.wearto"

        versionCode = 10000 // Version & release number
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}
