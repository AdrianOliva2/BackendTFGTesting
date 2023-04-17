package dam.adrianoliva.data.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String
)
