package se.yverling.wearto.convention

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal fun Project.commonAndroidConfig() {
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
            resources.excludes += "META-INF/**"
        }

        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }

        buildFeatures.buildConfig = true
    }
}

internal fun Project.android(action: BaseExtension.() -> Unit) = extensions.configure<BaseExtension>(action)
