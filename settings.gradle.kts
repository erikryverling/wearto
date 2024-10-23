rootProject.name = "WearTo"
rootProject.buildFileName = "build.gradle.kts"

include(
    ":test:utils",
    ":mobile:app",
    ":mobile:common:design-system",
    ":mobile:common:network",
    ":mobile:data:items",
    ":mobile:data:settings",
    ":mobile:data:token",
    ":mobile:feature:login",
    ":mobile:feature:settings",
    ":wear",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
