package se.yverling.wearto.convention.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.alias
import se.yverling.wearto.convention.implementation
import se.yverling.wearto.convention.libs

class ComposeWearConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.run {
                alias(libs.plugins.convention.android.library)
                alias(libs.plugins.convention.compose.common)
            }

            dependencies {
                implementation(libs.bundles.compose.wear)
            }
        }
    }
}
