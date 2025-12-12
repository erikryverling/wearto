package se.yverling.wearto.convention.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.alias
import se.yverling.wearto.convention.commonAndroidConfig
import se.yverling.wearto.convention.implementation
import se.yverling.wearto.convention.libs
import se.yverling.wearto.convention.testImplementation
import se.yverling.wearto.convention.testRuntimeOnly

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.run {
                alias(libs.plugins.android.library)
                alias(libs.plugins.kotlin.android)
            }
            commonAndroidConfig()

            dependencies {
                implementation(libs.timber)

                testImplementation(libs.bundles.unitTest)
                testImplementation(project(":test:utils"))
                testRuntimeOnly(libs.unitTest.junit.platformLauncher)
            }
        }
    }
}
