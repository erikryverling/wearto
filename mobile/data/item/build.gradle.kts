plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.hilt)
    alias(libs.plugins.convention.ktor)
}

dependencies {
    implementation(projects.mobile.common.network)
    implementation(projects.mobile.data.settings)
    implementation(projects.test.utils)
}

android {
    namespace = "se.yverling.wearto.mobile.data.item"
}
