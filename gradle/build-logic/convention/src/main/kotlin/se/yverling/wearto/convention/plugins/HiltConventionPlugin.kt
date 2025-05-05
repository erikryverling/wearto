package se.yverling.wearto.convention.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.libs
import se.yverling.wearto.convention.implementation
import se.yverling.wearto.convention.ksp

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.convention.android.library.get().pluginId)
                apply(libs.plugins.ksp.get().pluginId)
                apply(libs.plugins.hilt.android.get().pluginId)
            }

            dependencies {
                ksp(libs.hilt.android.compiler)
                implementation(libs.hilt.android)
            }
        }
    }
}
