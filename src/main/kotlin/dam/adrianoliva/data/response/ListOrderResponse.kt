package dam.adrianoliva.data.response

import kotlinx.serialization.Serializable

@Serializable
data class ListOrderResponse(
    val orders: List<OrderResponse>
)
