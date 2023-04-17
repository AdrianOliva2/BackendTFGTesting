package dam.adrianoliva.data.order

import dam.adrianoliva.data.item.Item
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Order(
    @BsonId val id: ObjectId = ObjectId(),
    val items: List<Item>,
    val total: Double,
    val servedby: ObjectId = ObjectId(),
    var completed: Boolean = false
)
