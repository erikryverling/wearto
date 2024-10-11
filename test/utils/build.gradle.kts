apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.gradle")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    implementation(libs.bundles.unitTest)
}

android {
    namespace = "se.yverling.wearto.test.util"
}
