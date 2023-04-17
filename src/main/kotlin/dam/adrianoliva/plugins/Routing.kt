package dam.adrianoliva.plugins

import dam.adrianoliva.*
import dam.adrianoliva.data.employee.EmployeeDTS
import dam.adrianoliva.data.item.ItemDTS
import dam.adrianoliva.data.order.OrderDTS
import dam.adrianoliva.security.hash.HashService
import dam.adrianoliva.security.token.TokenConfig
import dam.adrianoliva.security.token.TokenService
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting(
    employeeDTS: EmployeeDTS,
    orderDTS: OrderDTS,
    itemDTS: ItemDTS,
    hashService: HashService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        signIn(employeeDTS, hashService, tokenService, tokenConfig)
        signUp(hashService, employeeDTS, tokenService, tokenConfig)
        getEmployee(employeeDTS)
        authenticate()
        secretInfo()
        deleteAccount(employeeDTS)
        getOrder(orderDTS)
        getWhoServed(employeeDTS, orderDTS)
        createOrder(employeeDTS, orderDTS)
        updateOrder(employeeDTS, orderDTS)
        deleteOrder(employeeDTS, orderDTS)
        completeOrder(employeeDTS, orderDTS)
        getItem(itemDTS)
        createItem(employeeDTS, itemDTS)
        updateItem(employeeDTS, itemDTS)
        deleteItem(employeeDTS, itemDTS)
    }
}
