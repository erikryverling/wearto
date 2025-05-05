plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.hilt)
}

dependencies {
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization)
}


android {
    namespace = "se.yverling.wearto.mobile.data.token"
}
