package isetb.projet.tunisiasmartblood

data class LoginRequest(
    val email: String,
    val motDePasse: String
)

data class RegisterRequest(
    val nom: String,
    val email: String,
    val motDePasse: String,
    val prenom: String? = null,
    val groupeSanguin: String? = null,
    val ville: String? = null,
    val telephone: String? = null
)

data class AuthResponse(
    val token: String,
    val email: String,
    val nom: String,
    val role: String,
    val donneurId: Long?
)