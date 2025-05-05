import org.gradle.kotlin.dsl.libs

plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.hilt)
    alias(libs.plugins.convention.ktor)
    alias(libs.plugins.protobuf)
}

dependencies {
    implementation(projects.mobile.common.network)

    implementation(libs.datastore)
    implementation(libs.protobuf)
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
