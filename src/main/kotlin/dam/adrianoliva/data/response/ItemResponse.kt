package dam.adrianoliva.data.response

import dam.adrianoliva.data.item.Item
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class ItemResponse(
    @BsonId val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val image: String
)