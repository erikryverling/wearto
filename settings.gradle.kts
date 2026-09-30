rootProject.name = "wearto"

pluginManagement {
    includeBuild("gradle/build-logic")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    ":common:ui",
    ":test:utils",
    ":mobile:app",
    ":mobile:common:design-system",
    ":mobile:common:network",
    ":mobile:data:item",
    ":mobile:data:items",
    ":mobile:data:settings",
    ":mobile:data:token",
    ":mobile:feature:items",
    ":mobile:feature:login",
    ":mobile:feature:settings",
    ":wear:app",
    ":wear:common:design-system",
    ":wear:data:items",
    ":wear:feature:items",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
