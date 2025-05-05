plugins {
    alias(libs.plugins.kotlin.dsl)
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.android.tools.gradle)
    compileOnly(libs.kotlin.gradle)

    // Expose the generated version catalog API to the plugins.
    implementation(files(libs::class.java.superclass.protectionDomain.codeSource.location))
}

kotlin {
    sourceSets["main"].kotlin.srcDir("../../../buildSrc/src/main/kotlin")
}

gradlePlugin {
    plugins {
        plugins {
            create("AndroidLibraryConventionPlugin") {
                id = "se.yverling.wearto.convention.android.library"
                implementationClass = "se.yverling.wearto.convention.plugins.AndroidLibraryConventionPlugin"
            }

            create("ComposeConventionPlugin") {
                id = "se.yverling.wearto.convention.compose"
                implementationClass = "se.yverling.wearto.convention.plugins.ComposeConventionPlugin"
            }

            create("ComposeMobileConventionPlugin") {
                id = "se.yverling.wearto.convention.compose.mobile"
                implementationClass = "se.yverling.wearto.convention.plugins.ComposeMobileConventionPlugin"
            }

            create("ComposeWearConventionPlugin") {
                id = "se.yverling.wearto.convention.compose.wear"
                implementationClass = "se.yverling.wearto.convention.plugins.ComposeWearConventionPlugin"
            }

            create("HiltConventionPlugin") {
                id = "se.yverling.wearto.convention.hilt"
                implementationClass = "se.yverling.wearto.convention.plugins.HiltConventionPlugin"
            }

            create("KtorConventionPlugin") {
                id = "se.yverling.wearto.convention.ktor"
                implementationClass = "se.yverling.wearto.convention.plugins.KtorConventionPlugin"
            }

            create("ApplicationConventionPlugin") {
                id = "se.yverling.wearto.convention.application"
                implementationClass = "se.yverling.wearto.convention.plugins.ApplicationConventionPlugin"
            }
        }
    }
}
