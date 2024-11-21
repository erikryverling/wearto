apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.compose.gradle")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":mobile:common:design-system"))
    implementation(project(":mobile:data:item"))
    implementation(project(":mobile:data:items"))
    implementation(project(":mobile:feature:items"))
    implementation(project(":mobile:feature:login"))
    implementation(project(":mobile:feature:settings"))
    implementation(project(":test:utils"))

    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.mobile)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.playServices.wearable)

    implementation(libs.timber)

    testImplementation(libs.bundles.unitTest)
}

android {
    namespace = "se.yverling.wearto.mobile.app"

    defaultConfig {
        applicationId = "se.yverling.wearto"

        targetSdk = Versions.targetSdkMobile

        // Target SDK, version, build number, multi-apk number (00 = mobile, 01 = wear)
        versionCode = 352000000

        versionName = "2.0.0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}
