package se.yverling.wearto.convention.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import se.yverling.wearto.convention.alias
import se.yverling.wearto.convention.implementation
import se.yverling.wearto.convention.ksp
import se.yverling.wearto.convention.libs
import se.yverling.wearto.convention.testImplementation
import se.yverling.wearto.convention.testRuntimeOnly

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.run {
                alias(libs.plugins.android.application)
                alias(libs.plugins.kotlin.android)
                alias(libs.plugins.ksp)
                alias(libs.plugins.hilt.android)
                alias(libs.plugins.kotlin.compose)
            }

            configureAndroidBase()

            android {
                buildTypes {
                    getByName("debug") {
                        applicationIdSuffix = ".debug"
                    }

                    val releaseSigningConfigName = "release"
                    signingConfigs {
                        register(releaseSigningConfigName) {
                            val storeFilePath = System.getenv("SIGNING_STORE_FILE")
                            if (storeFilePath != null) {
                                storeFile = file(storeFilePath)
                            }
                            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
                        }
                    }

                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                        signingConfig = signingConfigs.getByName(releaseSigningConfigName)
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
