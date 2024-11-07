rootProject.name = "WearTo"
rootProject.buildFileName = "build.gradle.kts"

include(
    ":test:utils",
    ":mobile:app",
    ":mobile:common:design-system",
    ":mobile:common:network",
    ":mobile:common:ui",
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

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
