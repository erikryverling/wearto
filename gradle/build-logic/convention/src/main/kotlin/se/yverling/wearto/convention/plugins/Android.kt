package se.yverling.wearto.convention.plugins

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureAndroidBase() {
    android {
        compileSdkVersion(Versions.compileSdk)

        defaultConfig {
            minSdk = Versions.minSdk
        }

        compileOptions {
            // KSP only supports Java 17
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        testOptions {
            unitTests.all {
                it.useJUnitPlatform()
            }
        }

        packagingOptions {
            resources.excludes += listOf("META-INF/**")
        }

        tasks.withType<KotlinCompile>().configureEach {
            @Suppress("DEPRECATION")
            kotlinOptions {
                jvmTarget = Versions.jvmTarget
            }
        }

        buildFeatures.buildConfig = true
    }
}

internal fun Project.android(action: BaseExtension.() -> Unit) = extensions.configure<BaseExtension>(action)
