package dam.adrianoliva.data.request

import dam.adrianoliva.data.item.Item
import kotlinx.serialization.Serializable

data class CreateOrderRequest(
    val items: List<Item>
)