plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.hilt)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(libs.datastore)
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.tink)
    implementation(libs.tink.android)
    implementation(libs.kotlinx.serialization)
}

android {
    namespace = "se.yverling.wearto.mobile.data.token"
}
