rootProject.name = "WearTo"
rootProject.buildFileName = "build.gradle.kts"

include(
    ":test:utils",
    ":mobile:app",
    ":mobile:common:design-system",
    ":mobile:data:auth",
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
