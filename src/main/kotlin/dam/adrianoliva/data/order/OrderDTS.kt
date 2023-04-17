package dam.adrianoliva.data.order

interface OrderDTS {
    suspend fun getOrders(): List<Order>
    suspend fun getOrderById(id: String): Order?
    suspend fun addOrder(order: Order): Boolean
    suspend fun updateOrder(order: Order): Boolean
    suspend fun deleteOrder(id: String): Boolean
}