apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.compose.gradle")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":mobile:common:design-system"))
    implementation(project(":mobile:data:item"))
    implementation(project(":mobile:data:items"))
    implementation(project(":mobile:feature:items"))
    implementation(project(":mobile:feature:login"))
    implementation(project(":mobile:feature:settings"))
    implementation(project(":test:utils"))

    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.mobile)
    implementation(libs.hilt.navigation)

    implementation(libs.timber)
    implementation(libs.playServices.wearable)
    implementation(libs.horologist.datalayer.phone)

    testImplementation(libs.bundles.unitTest)
}

android {
    namespace = "se.yverling.wearto.mobile.app"

    defaultConfig {
        applicationId = "se.yverling.wearto"

        versionCode = 10000 // Version & release number
        versionName = "1.0.0"
    }
}
