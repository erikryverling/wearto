package se.yverling.wearto.convention.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.libs
import se.yverling.wearto.convention.implementation

class KtorConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.serialization.get().pluginId)
            }

            dependencies {
                implementation(libs.bundles.ktor)
            }
        }
    }
}
