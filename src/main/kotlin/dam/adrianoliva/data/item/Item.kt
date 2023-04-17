package dam.adrianoliva.data.item

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Item(
    @BsonId val id: ObjectId = ObjectId(),
    val name: String,
    val description: String,
    val price: Double,
    val image: String
)
