import org.gradle.kotlin.dsl.libs

apply(from = "${rootProject.projectDir}/buildSrc/build.module.android.gradle")

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.serialization)
    alias(libs.plugins.protobuf)
}

dependencies {
    implementation(project(":mobile:common:network"))

    ksp(libs.hilt.android.compiler)
    implementation(libs.bundles.hilt)

    implementation(libs.bundles.ktor)

    implementation(libs.datastore)
    implementation(libs.protobuf)

    implementation(libs.timber)

    testImplementation(libs.bundles.unitTest)
    testRuntimeOnly(libs.unitTest.junit.platformLauncher)
}


android {
    namespace = "se.yverling.wearto.mobile.data.settings"
}

protobuf {
    protoc {
        artifact = libs.versions.protobufCompilerArtifact.get()
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
