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

    implementation(libs.bundles.hilt)
    ksp(libs.hilt.android.compiler)

    implementation(libs.bundles.ktor)

    implementation(libs.timber)

    testImplementation(libs.bundles.unitTest)
}

android {
    namespace = "se.yverling.wearto.mobile.data.item"
}
