package isetb.projet.tunisiasmartblood

import com.google.gson.JsonParser
import java.util.Base64

/** Lecture (sans vérification de signature : le serveur reste l'autorité) de la date d'expiration d'un JWT. */
object Jwt {

    /** Instant d'expiration (millisecondes) d'après la revendication `exp`, ou null si absente/illisible. */
    fun expiration(token: String?): Long? {
        val charge = decoderCharge(token) ?: return null
        val exp = charge.get("exp") ?: return null
        return try {
            exp.asLong * 1000
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Un jeton absent ou illisible est considéré comme expiré ;
     * un jeton sans `exp` n'expire jamais localement.
     */
    fun estExpire(token: String?, maintenant: Long = System.currentTimeMillis()): Boolean {
        if (token.isNullOrBlank() || decoderCharge(token) == null) return true
        val fin = expiration(token) ?: return false
        return fin <= maintenant
    }

    private fun decoderCharge(token: String?): com.google.gson.JsonObject? {
        val segment = token?.split('.')?.getOrNull(1)?.takeIf { it.isNotEmpty() } ?: return null
        return try {
            val json = String(Base64.getUrlDecoder().decode(segment), Charsets.UTF_8)
            JsonParser.parseString(json).takeIf { it.isJsonObject }?.asJsonObject
        } catch (e: Exception) {
            null
        }
    }
}
