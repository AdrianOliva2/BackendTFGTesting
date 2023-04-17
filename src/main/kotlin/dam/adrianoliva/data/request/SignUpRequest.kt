package dam.adrianoliva.data.request

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val userName: String,
    val email: String,
    val password: String,
    val phone: String,
    val department: String,
)