apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.compose.gradle")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.wear)

    implementation(libs.timber)
}

android {
    namespace = "se.yverling.wearto.wear.common.design"
}
