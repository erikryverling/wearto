plugins {
    alias(libs.plugins.convention.hilt)
    alias(libs.plugins.convention.compose.mobile)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(projects.common.ui)
    implementation(projects.mobile.common.designSystem)
    implementation(projects.mobile.common.network)
    implementation(projects.mobile.data.items)
    implementation(projects.mobile.data.settings)
    implementation(projects.mobile.data.token)
    implementation(projects.test.utils)

    implementation(libs.hilt.navigation.compose)

    implementation(libs.kotlinx.serialization)
}

android {
    namespace = "se.yverling.wearto.mobile.feature.settings"
}
