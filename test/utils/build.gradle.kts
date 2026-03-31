plugins {
    alias(libs.plugins.convention.android.library)
}

dependencies {
    implementation(libs.bundles.unitTest)
}

android {
    namespace = "se.yverling.wearto.test.util"
}
