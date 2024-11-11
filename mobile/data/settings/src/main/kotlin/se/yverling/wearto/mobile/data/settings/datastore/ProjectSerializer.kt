package se.yverling.wearto.mobile.data.settings.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import se.yverling.wearto.Project
import java.io.InputStream
import java.io.OutputStream

internal const val DATASTORE_FILE_NAME = "project.pb"

internal object ProjectSerializer : Serializer<Project> {
    override val defaultValue: Project = Project.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Project {
        try {
            return Project.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto", exception)
        }
    }

    override suspend fun writeTo(t: Project, output: OutputStream) = t.writeTo(output)
}
