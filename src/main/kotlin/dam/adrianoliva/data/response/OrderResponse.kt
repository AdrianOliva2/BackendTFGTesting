package dam.adrianoliva.data.response

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class OrderResponse(
    val id: String,
    val items: List<String>,
    val total: Double,
    val completed: Boolean
)