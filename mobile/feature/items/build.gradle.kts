apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.compose.gradle")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(project(":mobile:common:design-system"))
    implementation(project(":mobile:common:network"))
    implementation(project(":mobile:common:ui"))
    implementation(project(":mobile:data:items"))
    implementation(project(":mobile:data:token"))
    implementation(project(":test:utils"))
    implementation(project(":mobile:feature:settings"))

    ksp(libs.hilt.android.compiler)
    implementation(libs.bundles.hilt)
    implementation(libs.hilt.navigation)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.mobile)

    implementation(libs.kotlinx.serialization)

    implementation(libs.timber)

    testImplementation(libs.bundles.unitTest)
}

android {
    namespace = "se.yverling.wearto.mobile.feature.items"
}
