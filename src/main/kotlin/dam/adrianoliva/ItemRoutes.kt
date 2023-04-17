package dam.adrianoliva

import dam.adrianoliva.data.employee.EmployeeDTS
import dam.adrianoliva.data.item.Item
import dam.adrianoliva.data.item.ItemDTS
import dam.adrianoliva.data.request.CreateItemRequest
import dam.adrianoliva.data.request.UpdateItemRequest
import dam.adrianoliva.data.response.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.getItem(
    itemDTS: ItemDTS
) {
    get("/item") {
        val items = itemDTS.getItems()
        call.respond(
            status = HttpStatusCode.OK,
            message = items.map {
                ItemResponse(
                    id = it.id.toString(),
                    name = it.name,
                    description = it.description,
                    price = it.price,
                    image = it.image
                )
            }
        )
    }
    get("/item/{id}") {
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
        val item = itemDTS.getItemById(id)
        if (item == null) {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(
                    error = "The item with id $id doesn't exist"
                )
            )
            return@get
        }
        call.respond(
            status = HttpStatusCode.OK,
            message = ItemResponse(
                id = item.id.toString(),
                name = item.name,
                description = item.description,
                price = item.price,
                image = item.image
            )
        )
    }
}

fun Route.createItem(
    employeeDTS: EmployeeDTS,
    itemDTS: ItemDTS
) {
    authenticate {
        post("/item/create") {
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
            if (employee?.department != "admin") {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "Only admins can create items"
                    )
                )
                return@post
            }
            val request = call.receiveNullable<CreateItemRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val areFieldsBlank = request.name.isBlank() && request.description.isBlank() && request.price >= 0 && request.image.isBlank()
            if (areFieldsBlank) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorResponse(
                        error = "The request is not valid"
                    )
                )
                return@post
            }
            val item = Item(
                name = request.name,
                description = request.description,
                price = request.price,
                image = request.image
            )
            val addedItem = itemDTS.addItem(item)
            if (addedItem) {
                call.respond(
                    status = HttpStatusCode.Created,
                    message = ItemResponse(
                        id = item.id.toString(),
                        name = item.name,
                        description = item.description,
                        price = item.price,
                        image = item.image
                    )
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

fun Route.updateItem(
    employeeDTS: EmployeeDTS,
    itemDTS: ItemDTS
) {
    authenticate {
        put("/item/update/{id}") {
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
            if (employee?.department != "admin") {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "Only admins can update items"
                    )
                )
                return@put
            }
            val itemId = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val originalItem = itemDTS.getItemById(itemId)
            if (originalItem == null) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = ErrorResponse(
                        error = "The item doesn't exist"
                    )
                )
                return@put
            }

            val request = call.receiveNullable<UpdateItemRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val areFieldsBlank = request.name.isEmpty() && request.description.isEmpty() && request.price >= 0 && request.image.isEmpty()
            if (areFieldsBlank) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorResponse(
                        error = "The request is not valid"
                    )
                )
                return@put
            }
            val item = Item(
                name = request.name,
                description = request.description,
                price = request.price,
                image = request.image
            )
            val updatedItem = itemDTS.updateItem(item)
            if (updatedItem) {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = ItemResponse(
                        id = item.id.toString(),
                        name = item.name,
                        description = item.description,
                        price = item.price,
                        image = item.image
                    )
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

fun Route.deleteItem(
    employeeDTS: EmployeeDTS,
    itemDTS: ItemDTS
) {
    authenticate {
        delete("/item/delete/{id}") {
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
            if (employee?.department != "admin") {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "Only admins can delete items"
                    )
                )
                return@delete
            }
            val itemId = call.parameters["id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val originalItem = itemDTS.getItemById(itemId)
            if (originalItem == null) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = ErrorResponse(
                        error = "The item doesn't exist"
                    )
                )
                return@delete
            }

            val deletedItem = itemDTS.deleteItem(itemId)
            if (deletedItem) {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = ItemResponse(
                        id = originalItem.id.toString(),
                        name = originalItem.name,
                        description = originalItem.description,
                        price = originalItem.price,
                        image = originalItem.image
                    )
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
