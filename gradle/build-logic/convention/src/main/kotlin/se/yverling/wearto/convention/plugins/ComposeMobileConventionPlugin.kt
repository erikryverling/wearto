package se.yverling.wearto.convention.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.implementation
import se.yverling.wearto.convention.libs

class ComposeMobileConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.convention.android.library.get().pluginId)
                apply(libs.plugins.convention.compose.common.get().pluginId)
            }

            dependencies {
                implementation(libs.bundles.compose.mobile)
            }
        }
    }
}
