package se.yverling.wearto.wear.data.items.sync

interface ItemSyncClient {
    suspend fun sendItem(name: String): Result<Unit>
}
