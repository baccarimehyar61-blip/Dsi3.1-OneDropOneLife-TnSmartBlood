package isetb.projet.tunisiasmartblood

// ----- Authentification (mêmes structures que le backend Spring Boot et l'application Angular) -----

/**
 * @param profil écran de connexion utilisé ("ADMIN" ou "DONNEUR") : le serveur vérifie qu'il correspond
 *               au rôle réel du compte (null = pas de vérification, anciens clients).
 */
data class LoginRequest(
    val email: String,
    val motDePasse: String,
    val profil: String? = null
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

// ----- Donneurs (/donneurs) -----

data class DonneurApi(
    val id: Long,
    val nom: String,
    val prenom: String,
    val email: String?,
    val telephone: String?,
    val ville: String?,
    val groupeSanguin: String?,
    val dateDernierDon: String?,
    val disponible: Boolean?
) {
    val nomComplet: String get() = "$prenom $nom".trim()
    val actif: Boolean get() = disponible ?: true
}

/** Corps de PUT /donneurs/{id} (DonneurRequest du backend). */
data class DonneurMajRequest(
    val nom: String,
    val prenom: String,
    val groupeSanguin: String?,
    val ville: String,
    val telephone: String,
    val dateDernierDon: String?,
    val disponible: Boolean
)

// ----- Centres (/centres) -----

data class CentreApi(
    val id: Long,
    val nom: String,
    val adresse: String?,
    val ville: String?,
    val telephone: String?,
    val latitude: Double?,
    val longitude: Double?
)

// ----- Rendez-vous (/rendez-vous) -----

data class RdvApi(
    val id: Long,
    val donneurId: Long,
    val centreId: Long,
    val dateRendezVous: String,
    val statut: String
)

data class RdvRequest(
    val donneurId: Long,
    val centreId: Long,
    val dateRendezVous: String
)

/** Corps d'erreur renvoyé par le GlobalExceptionHandler du backend. */
data class ErreurApi(
    val message: String? = null,
    val erreurs: Map<String, String>? = null
)
