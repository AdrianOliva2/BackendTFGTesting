package dam.adrianoliva.data.item

import dam.adrianoliva.data.order.Order

interface ItemDTS {
    suspend fun getItems(): List<Item>
    suspend fun getItemById(id: String): Item?
    suspend fun addItem(item: Item): Boolean
    suspend fun updateItem(item: Item): Boolean
    suspend fun deleteItem(id: String): Boolean
}