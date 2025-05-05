plugins {
    alias(libs.plugins.convention.application)
}

dependencies {
    implementation(projects.wear.common.designSystem)
    implementation(projects.wear.data.items)
    implementation(projects.wear.feature.items)
    implementation(projects.test.utils)

    implementation(libs.bundles.compose.wear)
}

android {
    namespace = "se.yverling.wearto.wear.app"

    defaultConfig {
        applicationId = "se.yverling.wearto"

        targetSdk = Versions.targetSdkWear

        // Target SDK, version, build number, multi-apk number (00 = mobile, 01 = wear)
        versionCode = 352000201

        versionName = "2.0.0"
    }
}
