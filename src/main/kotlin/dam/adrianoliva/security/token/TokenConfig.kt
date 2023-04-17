package dam.adrianoliva.security.token

data class TokenConfig (
    val issuer: String,
    val audience: String,
    val expiresIn: Long,
    val secret: String
)