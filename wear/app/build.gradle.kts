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

        versionCode = 400000000

        versionName = "2.0.1"
    }
}
