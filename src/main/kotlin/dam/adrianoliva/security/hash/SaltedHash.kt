package dam.adrianoliva.security.hash

data class SaltedHash(
    val hash: String,
    val salt: String
)
