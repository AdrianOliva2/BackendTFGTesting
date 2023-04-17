package dam.adrianoliva

import dam.adrianoliva.data.employee.Employee
import dam.adrianoliva.data.employee.EmployeeMongoDTS
import dam.adrianoliva.data.item.ItemMongoDTS
import dam.adrianoliva.data.order.OrderMongoDTS
import io.ktor.server.application.*
import dam.adrianoliva.plugins.*
import dam.adrianoliva.security.hash.SHA256HashService
import dam.adrianoliva.security.token.JwtTokenService
import dam.adrianoliva.security.token.TokenConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val mongoPw = System.getenv("MONGO_PW")
    val dbName = "orders"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://aolirio:$mongoPw@cluster0.jy0zdyt.mongodb.net/$dbName?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(dbName)
    val employeeDTS = EmployeeMongoDTS(db)
    val orderDTS = OrderMongoDTS(db)
    val itemDTS = ItemMongoDTS(db)

    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 8 * 60L * 60L * 1000L,
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = SHA256HashService()

    configureSerialization()
    configureSecurity(tokenConfig)
    configureRouting(employeeDTS, orderDTS, itemDTS, hashingService, tokenService, tokenConfig)
    configureCORS()

}
