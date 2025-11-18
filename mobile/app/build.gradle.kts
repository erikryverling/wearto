plugins {
    alias(libs.plugins.convention.application)
}

dependencies {
    implementation(projects.mobile.common.designSystem)
    implementation(projects.mobile.data.item)
    implementation(projects.mobile.data.items)
    implementation(projects.mobile.feature.items)
    implementation(projects.mobile.feature.login)
    implementation(projects.mobile.feature.settings)
    implementation(projects.test.utils)

    implementation(libs.bundles.compose.mobile)

    implementation(libs.hilt.navigation.compose)
}

android {
    namespace = "se.yverling.wearto.mobile.app"

    defaultConfig {
        applicationId = "se.yverling.wearto"

        targetSdk = Versions.targetSdkMobile

        // Target SDK, version, build number, multi-apk number (00 = mobile, 01 = wear)
        versionCode = 362110400

        versionName = "2.1.1"
    }
}
