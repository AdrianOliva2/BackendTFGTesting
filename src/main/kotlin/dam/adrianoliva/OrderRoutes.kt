package dam.adrianoliva

import dam.adrianoliva.data.employee.EmployeeDTS
import dam.adrianoliva.data.order.Order
import dam.adrianoliva.data.order.OrderDTS
import dam.adrianoliva.data.request.CreateOrderRequest
import dam.adrianoliva.data.request.UpdateOrderRequest
import dam.adrianoliva.data.response.EmployeeResponse
import dam.adrianoliva.data.response.ErrorResponse
import dam.adrianoliva.data.response.OrderResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.getOrder(
    orderDTS: OrderDTS
) {
    get("/order") {
        val orders = orderDTS.getOrders()
        call.respond(
            status = HttpStatusCode.OK,
            message = orders.map {
                OrderResponse(
                    items = it.items,
                    total = it.total,
                    completed = it.completed
                )
            }
        )
    }
    get("/order/{id}") {
        val id = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        if (!ObjectId.isValid(id)) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    error = "The id is not valid"
                )
            )
            return@get
        }
        val order = orderDTS.getOrderById(id)
        if (order == null) {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(
                    error = "The order with id $id doesn't exist"
                )
            )
            return@get
        }
        call.respond(
            status = HttpStatusCode.OK,
            message = OrderResponse(
                items = order.items,
                total = order.total,
                completed = order.completed
            )
        )
    }
}

fun Route.getWhoServed(
    employeeDTS: EmployeeDTS,
    orderDTS: OrderDTS
) {
    get("/order/{id}/whoserved") {
        val id = call.parameters["id"] ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }
        if (!ObjectId.isValid(id)) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    error = "The id is not valid"
                )
            )
            return@get
        }
        val order = orderDTS.getOrderById(id)
        if (order == null) {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(
                    error = "The order with id $id doesn't exist"
                )
            )
            return@get
        }
        val employee = employeeDTS.getEmployeeById(order.servedby.toString())
        if (employee == null) {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(
                    error = "The employee with id ${order.servedby} doesn't exist"
                )
            )
            return@get
        }
        call.respond(
            status = HttpStatusCode.OK,
            message = EmployeeResponse(
                userName = employee.userName,
                email = employee.email,
                phone = employee.phone,
                department = employee.department
            )
        )
    }
}

fun Route.createOrder(
    employeeDTS: EmployeeDTS,
    orderDTS: OrderDTS
) {
    authenticate {
        post("/order/create") {
            val principal = call.principal<JWTPrincipal>()
            val employeeId = principal?.getClaim("_id", String::class)
            if (employeeId == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = ErrorResponse(
                        error = "The token is invalid"
                    )
                )
                return@post
            }
            val employee = employeeDTS.getEmployeeById(employeeId)
            if (employee?.department != "waiter") {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "Only waiters can create orders"
                    )
                )
                return@post
            }
            val request = call.receiveNullable<CreateOrderRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val areFieldsBlank = request.items.isEmpty()
            if (areFieldsBlank) {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = ErrorResponse(
                        error = "The items can't be empty"
                    )
                )
                return@post
            }
            val order = Order(
                items = request.items,
                total = request.items.sumOf { it.price },
                servedby = ObjectId(employeeId)
            )
            val addedOrder = orderDTS.addOrder(order)
            if (addedOrder) {
                call.respond(
                    status = HttpStatusCode.Created,
                    message = OrderResponse(
                        items = order.items,
                        total = order.total,
                        completed = order.completed
                    )
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

fun Route.updateOrder(
    employeeDTS: EmployeeDTS,
    orderDTS: OrderDTS
) {
    authenticate {
        put("/order/update/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val employeeId = principal?.getClaim("_id", String::class)
            if (employeeId == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = ErrorResponse(
                        error = "The token is invalid"
                    )
                )
                return@put
            }
            val employee = employeeDTS.getEmployeeById(employeeId)
            if (employee?.department != "waiter") {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "Only waiters can update orders"
                    )
                )
                return@put
            }
            val orderId = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val originalOrder = orderDTS.getOrderById(orderId)
            if (originalOrder == null) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = ErrorResponse(
                        error = "The order doesn't exist"
                    )
                )
                return@put
            }
            if (originalOrder.servedby != ObjectId(employeeId)) {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "You can't update orders that you didn't create"
                    )
                )
                return@put
            }
            val request = call.receiveNullable<UpdateOrderRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val areFieldsBlank = request.items.isEmpty()
            if (areFieldsBlank) {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = ErrorResponse(
                        error = "The items can't be empty"
                    )
                )
                return@put
            }
            val order = Order(
                id = ObjectId(orderId),
                items = request.items,
                total = request.items.sumOf { it.price },
                servedby = ObjectId(employeeId),
                completed = false
            )
            val updatedOrder = orderDTS.updateOrder(order)
            if (updatedOrder) {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = OrderResponse(
                        items = order.items,
                        total = order.total,
                        completed = order.completed
                    )
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

fun Route.deleteOrder(
    employeeDTS: EmployeeDTS,
    orderDTS: OrderDTS
) {
    authenticate {
        delete("/order/delete/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val employeeId = principal?.getClaim("_id", String::class)
            if (employeeId == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = ErrorResponse(
                        error = "The token is invalid"
                    )
                )
                return@delete
            }
            val employee = employeeDTS.getEmployeeById(employeeId)
            if (employee?.department != "waiter") {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "Only waiters can delete orders"
                    )
                )
                return@delete
            }
            val orderId = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val originalOrder = orderDTS.getOrderById(orderId)
            if (originalOrder == null) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = ErrorResponse(
                        error = "The order doesn't exist"
                    )
                )
                return@delete
            }
            if (originalOrder.completed) {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = ErrorResponse(
                        error = "You can't delete completed orders"
                    )
                )
                return@delete
            }
            if (originalOrder.servedby != ObjectId(employeeId)) {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "You can't delete orders that you didn't create"
                    )
                )
                return@delete
            }
            val deletedOrder = orderDTS.deleteOrder(orderId)
            if (deletedOrder) {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = OrderResponse(
                        items = originalOrder.items,
                        total = originalOrder.total,
                        completed = originalOrder.completed
                    )
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

fun Route.completeOrder(
    employeeDTS: EmployeeDTS,
    orderDTS: OrderDTS
) {
    authenticate {
        put("/order/complete/{id}") {
            val orderId = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val principal = call.principal<JWTPrincipal>()
            val employeeId = principal?.getClaim("_id", String::class)
            if (employeeId == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = ErrorResponse(
                        error = "The token is invalid"
                    )
                )
                return@put
            }
            val employee = employeeDTS.getEmployeeById(employeeId)
            if (employee?.department != "chef") {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "Only chefs can complete orders"
                    )
                )
                return@put
            }
            val order = orderDTS.getOrderById(orderId)
            if (order == null) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = ErrorResponse(
                        error = "The order doesn't exist"
                    )
                )
                return@put
            }
            order.completed = !order.completed
            val updatedOrder = orderDTS.updateOrder(order)
            if (updatedOrder) {
                call.respond(
                    status = HttpStatusCode.Accepted,
                    message = OrderResponse(
                        items = order.items,
                        total = order.total,
                        completed = order.completed
                    )
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}