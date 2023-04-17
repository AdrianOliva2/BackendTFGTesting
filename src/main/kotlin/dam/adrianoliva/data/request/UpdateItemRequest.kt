package dam.adrianoliva.data.request

import dam.adrianoliva.data.item.Item
import kotlinx.serialization.Serializable

@Serializable
data class UpdateItemRequest(
    val name: String,
    val description: String,
    val price: Double,
    val image: String
)