apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.gradle")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(project(":mobile:common:network"))
    implementation(project(":mobile:data:settings"))
    implementation(project(":test:utils"))

    ksp(libs.hilt.android.compiler)
    implementation(libs.bundles.hilt)

    implementation(libs.bundles.ktor)

    implementation(libs.timber)

    testImplementation(libs.bundles.unitTest)
    testRuntimeOnly(libs.unitTest.junit.platformLauncher)
}

android {
    namespace = "se.yverling.wearto.mobile.data.item"
}
