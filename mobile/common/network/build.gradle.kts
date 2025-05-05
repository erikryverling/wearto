plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.hilt)
    alias(libs.plugins.convention.ktor)
}

dependencies {
    implementation(projects.mobile.data.token)
}

android {
    namespace = "se.yverling.wearto.mobile.common.network"
}
