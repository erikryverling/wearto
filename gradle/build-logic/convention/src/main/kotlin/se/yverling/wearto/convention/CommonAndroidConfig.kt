package se.yverling.wearto.convention

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal fun Project.commonAndroidConfig() {
    extensions.configure<CommonExtension> {
        compileSdk = Versions.compileSdk

        defaultConfig.apply {
            minSdk = Versions.minSdk
        }

        compileOptions.apply {
            // KSP only supports Java 17
            sourceCompatibility = JavaVersion.toVersion(Versions.jvm)
            targetCompatibility = JavaVersion.toVersion(Versions.jvm)
        }

        testOptions.apply {
            unitTests.all {
                it.useJUnitPlatform()
            }
        }

        lint.apply {
            disable += "NewerVersionAvailable"
            disable += "AndroidGradlePluginVersion"
            disable += "GradleDependency"
        }

        packaging.apply {
            resources.excludes += "META-INF/**"
        }

        buildFeatures.buildConfig = true
    }

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

internal fun Project.android(action: ApplicationExtension.() -> Unit) =
    extensions.configure<ApplicationExtension>(action)
