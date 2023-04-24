package dam.adrianoliva.data.response

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class ItemResponse(
    @BsonId val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val image: String
)