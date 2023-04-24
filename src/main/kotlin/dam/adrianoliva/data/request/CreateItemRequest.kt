package dam.adrianoliva.data.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    val name: String,
    val description: String,
    val price: Double,
    val image: String
)