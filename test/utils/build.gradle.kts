plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    implementation(libs.bundles.unitTest)
}

android {
    namespace = "se.yverling.wearto.test.util"
}
