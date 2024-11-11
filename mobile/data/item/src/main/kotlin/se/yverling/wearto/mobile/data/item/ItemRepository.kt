package se.yverling.wearto.mobile.data.item

interface ItemRepository {
    suspend fun addItem(itemName: String)
}
