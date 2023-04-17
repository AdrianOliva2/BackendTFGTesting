package dam.adrianoliva

import dam.adrianoliva.data.employee.Employee
import dam.adrianoliva.data.employee.EmployeeDTS
import dam.adrianoliva.data.order.OrderDTS
import dam.adrianoliva.data.request.SignInRequest
import dam.adrianoliva.data.request.SignUpRequest
import dam.adrianoliva.data.response.*
import dam.adrianoliva.security.hash.HashService
import dam.adrianoliva.security.hash.SaltedHash
import dam.adrianoliva.security.token.TokenClaim
import dam.adrianoliva.security.token.TokenConfig
import dam.adrianoliva.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.getEmployee(
    employeeDTS: EmployeeDTS
) {
    get("/employee") {
        val employees = employeeDTS.getAllEmployees()
        val employeesSerialize = employees.map {
            EmployeeResponse(
                userName = it.userName,
                email = it.email,
                phone = it.phone,
                department = it.department
            )
        }
        call.respond(
            status = HttpStatusCode.OK,
            message = ListEmployeeResponse(
                employees = employeesSerialize
            )
        )
    }
    get("/employee/{id}") {
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
        val employee = employeeDTS.getEmployeeById(id)
        if (employee == null) {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(
                    error = "The employee doesn't exist"
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

fun Route.signUp(
    hashService: HashService,
    employeeDTS: EmployeeDTS,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    authenticate {
        post("/employee/signup") {
            val principal = call.principal<JWTPrincipal>()
            val adminId = principal?.getClaim("_id", String::class)
            if (adminId == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = ErrorResponse(
                        error = "The token is invalid"
                    )
                )
                return@post
            }
            val admin = employeeDTS.getEmployeeById(adminId)
            if (admin?.department != "admin") {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "Only admins can create accounts"
                    )
                )
                return@post
            }

            val request = call.receiveNullable<SignUpRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val areFieldsBlank = request.userName.isBlank() ||
                    request.email.isBlank() ||
                    request.password.isBlank() ||
                    request.phone.isBlank() ||
                    request.department.isBlank()
            val isPwTooShort = request.password.length < 8
            if (areFieldsBlank || isPwTooShort) {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = ErrorResponse(
                        error = "The fields (userName, email, password, phone, and department) can't be blank and the password must be at least 8 characters long"
                    )
                )
                return@post
            }

            val saltedHash = hashService.generateSaltedHash(request.password)
            val employee = Employee(
                userName = request.userName,
                email = request.email,
                password = saltedHash.hash,
                salt = saltedHash.salt,
                phone = request.phone,
                department = request.department
            )
            val wasAknowledged = employeeDTS.createEmployee(employee)
            if (!wasAknowledged) {
                call.respond(
                    status = HttpStatusCode.Conflict,
                    message = ErrorResponse(
                        error = "The email, phone or username is already in use"
                    )
                )
                return@post
            }

            val user = employeeDTS.getEmployeeByEmail(request.email)

            val token = tokenService.generateToken(
                config = tokenConfig,
                TokenClaim(
                    name = "_id",
                    value = user?.id.toString()
                )
            )

            call.respond(
                status = HttpStatusCode.OK,
                message = AuthResponse(
                    token = token
                )
            )
        }
    }
}

fun Route.signIn(
    employeeDTS: EmployeeDTS,
    hashService: HashService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("/employee/signin") {
        val request = call.receiveNullable<SignInRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = employeeDTS.getEmployeeByEmail(request.email)
        if (user == null) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = ErrorResponse(
                    error = "The email isn't registered"
                )
            )
            return@post
        }

        val isPasswordCorrect = hashService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isPasswordCorrect) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = ErrorResponse(
                    error = "The password is incorrect"
                )
            )
            return@post
        }

        val token = tokenService.generateToken(
            config = tokenConfig,
            TokenClaim(
                name = "_id",
                value = user.id.toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}

fun Route.deleteAccount(
    employeeDTS: EmployeeDTS,
) {
    authenticate {
        delete("/employee/{userName}") {

            val principal = call.principal<JWTPrincipal>()
            val adminId = principal?.getClaim("_id", String::class)
            if (adminId == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = ErrorResponse(
                        error = "The token is invalid"
                    )
                )
                return@delete
            }

            val admin = employeeDTS.getEmployeeById(adminId)
            if (admin?.department != "admin") {
                call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(
                        error = "Only admins can delete accounts"
                    )
                )
                return@delete
            }

            val employeeName = call.parameters["userName"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            val employee = employeeDTS.getEmployeeByUserName(employeeName)
            if (employee == null) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = ErrorResponse(
                        error = "The employee $employeeName doesn't exist"
                    )
                )
                return@delete
            }

            val deletedEmployee = employeeDTS.deleteEmployee(employee.id.toString())
            if (deletedEmployee) {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = EmployeeResponse(
                        userName = employee.userName,
                        email = employee.email,
                        phone = employee.phone,
                        department = employee.department
                    )
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

fun Route.signOut() {
    authenticate {
        post("/employee/signout") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.authenticate() {
    authenticate {
        get("/employee/authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.secretInfo() {
    authenticate {
        get("/employee/secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("_id", String::class)
            if (userId == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = ErrorResponse(
                        error = "The token is invalid"
                    )
                )
                return@get
            }
            call.respond(
                status = HttpStatusCode.OK,
                message = ObjectIDResponse(
                    _id = userId
                )
            )
            return@get
        }
    }
}