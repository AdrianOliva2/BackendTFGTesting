package dam.adrianoliva.data.response

import dam.adrianoliva.data.item.Item
import kotlinx.serialization.Serializable

data class OrderResponse(
    val items: List<Item>,
    val total: Double,
    val completed: Boolean
)