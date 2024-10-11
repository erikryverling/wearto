apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.compose.gradle")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":mobile:common:design-system"))
    implementation(project(":mobile:data:auth"))
    implementation(project(":test:utils"))

    ksp(libs.hilt.android.compiler)
    implementation(libs.bundles.hilt)
    implementation(libs.hilt.navigation)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.mobile)

    implementation(libs.timber)

    testImplementation(libs.bundles.unitTest)
}

android {
    namespace = "se.yverling.wearto.mobile.feature.login"
}
