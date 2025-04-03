apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.compose.gradle")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":common:ui"))
    implementation(project(":test:utils"))
    implementation(project(":wear:common:design-system"))
    implementation(project(":wear:data:items"))

    ksp(libs.hilt.android.compiler)
    implementation(libs.bundles.hilt)
    implementation(libs.hilt.navigation.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.wear)

    implementation(libs.timber)

    testImplementation(libs.bundles.unitTest)
    testRuntimeOnly(libs.unitTest.junit.platformLauncher)
}

android {
    namespace = "se.yverling.wearto.wear.feature.items"
}
