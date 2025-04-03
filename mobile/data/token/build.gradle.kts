apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.gradle")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
}

dependencies {
    ksp(libs.hilt.android.compiler)
    implementation(libs.bundles.hilt)

    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization)

    implementation(libs.timber)

    testImplementation(libs.bundles.unitTest)
    testRuntimeOnly(libs.unitTest.junit.platformLauncher)
}


android {
    namespace = "se.yverling.wearto.mobile.data.token"
}
