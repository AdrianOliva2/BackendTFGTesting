package dam.adrianoliva.data.request

import dam.adrianoliva.data.item.Item
import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrderRequest(
    val items: List<String>
)