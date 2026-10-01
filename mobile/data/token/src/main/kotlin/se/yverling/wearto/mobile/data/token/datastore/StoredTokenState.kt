package se.yverling.wearto.mobile.data.token.datastore

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface StoredTokenState {
    @Serializable
    @SerialName("unset")
    data object Unset : StoredTokenState

    @Serializable
    @SerialName("absent")
    data object Absent : StoredTokenState

    @InternalSerializationApi
    @Serializable
    @SerialName("present")
    data class Present(val token: String) : StoredTokenState
}
