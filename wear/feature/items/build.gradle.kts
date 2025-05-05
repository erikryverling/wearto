plugins {
    alias(libs.plugins.convention.hilt)
    alias(libs.plugins.convention.compose.wear)
}

dependencies {
    implementation(projects.common.ui)
    implementation(projects.test.utils)
    implementation(projects.wear.common.designSystem)
    implementation(projects.wear.data.items)

    implementation(libs.hilt.navigation.compose)
}

android {
    namespace = "se.yverling.wearto.wear.feature.items"
}
