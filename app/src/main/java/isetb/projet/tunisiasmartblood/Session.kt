package isetb.projet.tunisiasmartblood

import android.content.Context
import android.content.SharedPreferences

/**
 * Session de l'utilisateur, conservée après la fermeture de l'application.
 *
 * Rôles applicatifs : "admin", "donneur", "centre", "etablissement"
 * (ADMIN / DONNEUR / PERSONNEL_CENTRE du backend ; l'établissement n'existe pas encore côté serveur).
 *
 * Source :
 *  - "api"  : compte réel du backend, avec un jeton JWT ;
 *  - "demo" : compte centre / établissement SIMULÉ (pas de jeton, rien n'est enregistré côté serveur).
 *
 * SÉCURITÉ : le backend exige `Authorization: Bearer <jwt>`, le jeton est donc lisible par l'application.
 * Il est stocké dans les SharedPreferences privées de l'application (inaccessibles aux autres applications,
 * mais non chiffrées sur un téléphone rooté). Il n'est jamais écrit dans les journaux.
 */
object Session {

    const val SOURCE_API = "api"
    const val SOURCE_DEMO = "demo"

    private const val FICHIER = "tsb_session"
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) prefs = context.applicationContext.getSharedPreferences(FICHIER, Context.MODE_PRIVATE)
        // Un jeton expiré ou absent invalide la session d'un compte réel (comme dans l'application Angular).
        if (source == SOURCE_API && Jwt.estExpire(jeton)) fermer()
    }

    val jeton: String? get() = lire("jeton")

    val role: String? get() = lire("role")
    val nom: String get() = lire("nom") ?: ""
    val email: String get() = lire("email") ?: ""
    val source: String? get() = lire("source")
    val donneurId: Long? get() = lire("donneurId")?.toLongOrNull()
    val gouvernorat: String? get() = lire("gouvernorat")
    val telephone: String? get() = lire("telephone")
    val adresse: String? get() = lire("adresse")

    val connecte: Boolean get() = role != null
    val estAdmin: Boolean get() = role == "admin"

    /** Session valide : présente et, pour un compte réel, jeton non expiré. Efface la session sinon. */
    fun estValide(): Boolean {
        if (!connecte) return false
        if (source == SOURCE_API && Jwt.estExpire(jeton)) {
            fermer()
            return false
        }
        return true
    }

    fun ouvrirApi(role: String, auth: AuthResponse) {
        edit {
            clear()
            putString("jeton", auth.token)
            putString("role", role)
            putString("nom", auth.nom)
            putString("email", auth.email)
            putString("source", SOURCE_API)
            auth.donneurId?.let { putString("donneurId", it.toString()) }
        }
    }

    fun ouvrirDemo(role: String, nom: String, email: String, gouvernorat: String? = null) {
        edit {
            clear()
            putString("role", role)
            putString("nom", nom)
            putString("email", email)
            putString("source", SOURCE_DEMO)
            gouvernorat?.let { putString("gouvernorat", it) }
        }
    }

    /** Profil d'un compte simulé (jamais envoyé au serveur). */
    fun majProfilDemo(email: String, gouvernorat: String, telephone: String, adresse: String) {
        if (source != SOURCE_DEMO) return
        edit {
            putString("email", email)
            putString("gouvernorat", gouvernorat)
            putString("telephone", telephone)
            putString("adresse", adresse)
        }
    }

    fun fermer() {
        edit { clear() }
    }

    /** Vue « compte » utilisée par les espaces centre / établissement. */
    fun compte(): Compte? = role?.takeIf { it != "admin" }?.let {
        Compte(it, nom, email, gouvernorat, telephone, adresse)
    }

    private fun lire(cle: String): String? = prefs?.getString(cle, null)

    private fun edit(bloc: SharedPreferences.Editor.() -> Unit) {
        prefs?.edit()?.apply(bloc)?.apply()
    }
}
