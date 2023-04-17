package dam.adrianoliva.data.response

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeResponse(
    val userName: String,
    val email: String,
    val phone: String,
    val department: String
)
