apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.gradle")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
}

dependencies {
    implementation(project(":test:utils"))

    ksp(libs.hilt.android.compiler)
    implementation(libs.bundles.hilt)

    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.timber)

    testImplementation(libs.bundles.unitTest)
    testRuntimeOnly(libs.unitTest.junit.platformLauncher)
}

android {
    namespace = "se.yverling.wearto.wear.data.items"
}
