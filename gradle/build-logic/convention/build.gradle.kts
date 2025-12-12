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
    plugins.register("se.yverling.wearto.convention.android.library") {
        id = name
        implementationClass = "se.yverling.wearto.convention.plugins.AndroidLibraryConventionPlugin"
    }

    plugins.register("se.yverling.wearto.convention.compose") {
        id = name
        implementationClass = "se.yverling.wearto.convention.plugins.ComposeConventionPlugin"
    }

    plugins.register("se.yverling.wearto.convention.compose.mobile") {
        id = name
        implementationClass = "se.yverling.wearto.convention.plugins.ComposeMobileConventionPlugin"
    }

    plugins.register("se.yverling.wearto.convention.compose.wear") {
        id = name
        implementationClass = "se.yverling.wearto.convention.plugins.ComposeWearConventionPlugin"
    }

    plugins.register("se.yverling.wearto.convention.hilt") {
        id = name
        implementationClass = "se.yverling.wearto.convention.plugins.HiltConventionPlugin"
    }

    plugins.register("se.yverling.wearto.convention.ktor") {
        id = name
        implementationClass = "se.yverling.wearto.convention.plugins.KtorConventionPlugin"
    }

    plugins.create("se.yverling.wearto.convention.application") {
        id = name
        implementationClass = "se.yverling.wearto.convention.plugins.ApplicationConventionPlugin"
    }
}
