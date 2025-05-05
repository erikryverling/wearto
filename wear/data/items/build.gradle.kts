plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.hilt)
}

dependencies {
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
}

android {
    namespace = "se.yverling.wearto.wear.data.items"
}
