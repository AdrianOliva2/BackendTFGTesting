package dam.adrianoliva.data.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val items: List<String>
)