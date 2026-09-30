package se.yverling.wearto.convention

import Versions
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal fun Project.commonAndroidConfig() {
    extensions.configure<CommonExtension> {
        compileSdk = Versions.compileSdk

        defaultConfig.apply {
            minSdk = Versions.minSdk
        }

        compileOptions.apply {
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

    kotlin {
        jvmToolchain(Versions.jvm.toInt())
    }
}

internal fun Project.android(action: ApplicationExtension.() -> Unit) =
    extensions.configure<ApplicationExtension>(action)

internal fun Project.kotlin(action: KotlinAndroidProjectExtension.() -> Unit) {
    extensions.configure(KotlinAndroidProjectExtension::class.java, action)
}
