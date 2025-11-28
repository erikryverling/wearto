package se.yverling.wearto.convention.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.alias
import se.yverling.wearto.convention.libs
import se.yverling.wearto.convention.implementation

class KtorConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.run {
                alias(libs.plugins.serialization)
            }

            dependencies {
                implementation(libs.bundles.ktor)
            }
        }
    }
}
