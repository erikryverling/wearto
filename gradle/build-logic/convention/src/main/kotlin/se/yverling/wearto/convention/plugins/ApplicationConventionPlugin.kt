package se.yverling.wearto.convention.plugins

import groovyjarjarantlr4.v4.runtime.misc.RuleDependencyChecker.checkDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.implementation
import se.yverling.wearto.convention.ksp
import se.yverling.wearto.convention.libs
import se.yverling.wearto.convention.testImplementation
import se.yverling.wearto.convention.testRuntimeOnly

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.android.application.get().pluginId)
                apply(libs.plugins.kotlin.android.get().pluginId)
                apply(libs.plugins.ksp.get().pluginId)
                apply(libs.plugins.hilt.android.get().pluginId)
                apply(libs.plugins.kotlin.compose.get().pluginId)
            }

            configureAndroidBase()

            android {
                buildTypes {
                    getByName("debug") {
                        applicationIdSuffix = ".debug"
                    }

                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                }

                lintOptions {
                    // This will generate a single report for all dependent modules
                    isCheckDependencies = true
                    isWarningsAsErrors = true
                    xmlReport = false
                    htmlReport = true
                    htmlOutput = file("${project.rootDir}/build/reports/android-lint.html")
                }
            }

            dependencies {
                ksp(libs.hilt.android.compiler)
                implementation(libs.hilt.android)

                implementation(libs.timber)

                implementation(platform(libs.compose.bom))
                implementation(libs.bundles.compose)

                implementation(libs.playServices.wearable)

                testImplementation(libs.bundles.unitTest)
                testImplementation(project(":test:utils"))
                testRuntimeOnly(libs.unitTest.junit.platformLauncher)
            }
        }
    }
}
