package dam.adrianoliva.data.item

import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class ItemMongoDTS(
    db: CoroutineDatabase
): ItemDTS {

    private val items = db.getCollection<Item>()

    override suspend fun getItems(): List<Item> {
        return items.find().toList()
    }

    override suspend fun getItemById(id: String): Item? {
        return items.findOne(Item::id eq ObjectId(id))
    }

    override suspend fun addItem(item: Item): Boolean {
        return items.insertOne(item).wasAcknowledged()
    }

    override suspend fun updateItem(item: Item): Boolean {
        return items.updateOne(Item::id eq item.id, item).wasAcknowledged()
    }

    override suspend fun deleteItem(id: String): Boolean {
        return items.deleteOne(Item::id eq ObjectId(id)).wasAcknowledged()
    }

}