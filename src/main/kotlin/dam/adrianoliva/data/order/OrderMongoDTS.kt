package dam.adrianoliva.data.order

import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class OrderMongoDTS(
    db: CoroutineDatabase
): OrderDTS {

    private val orders = db.getCollection<Order>()

    override suspend fun getOrders(): List<Order> {
        return orders.find().toList()
    }

    override suspend fun getOrderById(id: String): Order? {
        return orders.findOne(Order::id eq ObjectId(id))
    }

    override suspend fun addOrder(order: Order): Boolean {
        return orders.insertOne(order).wasAcknowledged()
    }

    override suspend fun updateOrder(order: Order): Boolean {
        return orders.updateOne(Order::id eq order.id, order).wasAcknowledged()
    }

    override suspend fun deleteOrder(id: String): Boolean {
        return orders.deleteOne(Order::id eq ObjectId(id)).wasAcknowledged()
    }

}