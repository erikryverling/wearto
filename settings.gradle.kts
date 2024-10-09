rootProject.name = "WearTo"
rootProject.buildFileName = "build.gradle.kts"

include(
    ":mobile:app",
    ":mobile:common:design-system",
    ":mobile:feature:login",
    ":wear",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
