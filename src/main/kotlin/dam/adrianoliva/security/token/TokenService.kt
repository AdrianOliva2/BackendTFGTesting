package dam.adrianoliva.security.token

interface TokenService {
    fun generateToken(
        config: TokenConfig,
        vararg claims: TokenClaim
    ): String
}