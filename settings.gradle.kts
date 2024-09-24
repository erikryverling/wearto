rootProject.name = "WearTo"
rootProject.buildFileName = "build.gradle.kts"

include(
    ":mobile",
    ":wear",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
