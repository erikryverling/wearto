package se.yverling.wearto.convention.plugins

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.implementation
import se.yverling.wearto.convention.libs

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.convention.android.library.get().pluginId)
                apply(libs.plugins.kotlin.compose.get().pluginId)
            }

           compose {
               buildFeatures.compose = true
           }

            dependencies {
                implementation(platform(libs.compose.bom))
                implementation(libs.bundles.compose)
            }
        }
    }
}

private fun Project.compose(action: BaseExtension.() -> Unit) = extensions.configure<BaseExtension>(action)
