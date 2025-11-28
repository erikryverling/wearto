package se.yverling.wearto.convention.plugins

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.alias
import se.yverling.wearto.convention.implementation
import se.yverling.wearto.convention.libs

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.run {
                alias(libs.plugins.convention.android.library)
                alias(libs.plugins.kotlin.compose)
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
