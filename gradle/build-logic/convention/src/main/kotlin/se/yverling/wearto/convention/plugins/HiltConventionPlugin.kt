package se.yverling.wearto.convention.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.alias
import se.yverling.wearto.convention.libs
import se.yverling.wearto.convention.implementation
import se.yverling.wearto.convention.ksp

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.run {
                alias(libs.plugins.convention.android.library)
                alias(libs.plugins.ksp)
                alias(libs.plugins.hilt.android)
            }

            dependencies {
                ksp(libs.hilt.android.compiler)
                implementation(libs.hilt.android)
            }
        }
    }
}
