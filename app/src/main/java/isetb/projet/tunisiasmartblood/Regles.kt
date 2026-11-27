package isetb.projet.tunisiasmartblood

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Règles métier et validations pures (sans Android), partagées par les écrans et testées en JVM.
 * Elles reproduisent celles de l'application Angular (validators, mappers, utilitaires).
 */
object Regles {

    // ----- Groupes sanguins : « O+ » (écran) <-> « O_POS » (API) -----
    private val GROUPES = mapOf(
        "A+" to "A_POS", "A-" to "A_NEG", "B+" to "B_POS", "B-" to "B_NEG",
        "AB+" to "AB_POS", "AB-" to "AB_NEG", "O+" to "O_POS", "O-" to "O_NEG"
    )

    fun groupeVersApi(groupe: String): String? = GROUPES[groupe]

    fun groupeDepuisApi(valeur: String?): String? = GROUPES.entries.firstOrNull { it.value == valeur }?.key

    // ----- Validations de formulaire -----
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun emailValide(email: String) = EMAIL.matches(email.trim())

    /** Mot de passe : 6 caractères minimum (règle du backend), pas uniquement des espaces. */
    fun motDePasseValide(mdp: String) = mdp.length >= 6 && mdp.isNotBlank()

    /** « +216 22 123 456 », « 00216 22123456 » et « 22 123 456 » donnent « 22123456 ». */
    fun normaliserTelephone(brut: String): String =
        brut.filter { it.isDigit() }.replace(Regex("^(00)?216"), "")

    fun telephoneValide(brut: String) = Regex("^\\d{8}$").matches(normaliserTelephone(brut))

    /** Dates au format ISO `yyyy-MM-dd` : comparaison lexicographique = comparaison chronologique. */
    fun dateNonFuture(iso: String, aujourdhui: LocalDate = LocalDate.now()) = iso <= aujourdhui.toString()

    fun dateNonPassee(iso: String, aujourdhui: LocalDate = LocalDate.now()) = iso >= aujourdhui.toString()

    /** L'heure de fin (HH:mm) doit suivre l'heure de début. */
    fun finApresDebut(debut: String, fin: String) = fin > debut

    // ----- Rendez-vous -----
    const val PLANIFIE = "PLANIFIE"
    const val CONFIRME = "CONFIRME"
    const val REFUSE = "REFUSE"
    const val EFFECTUE = "EFFECTUE"
    const val ANNULE = "ANNULE"

    fun libelleStatut(statut: String) = when (statut) {
        PLANIFIE -> "En attente"
        CONFIRME -> "Confirmé"
        REFUSE -> "Refusé"
        EFFECTUE -> "Effectué"
        ANNULE -> "Annulé"
        else -> statut
    }

    /** Règle du backend : seuls un rendez-vous PLANIFIE ou CONFIRME peut être annulé (donc « actif »). */
    fun peutAnnuler(statut: String) = statut == PLANIFIE || statut == CONFIRME

    fun date(iso: String): LocalDateTime? = try {
        LocalDateTime.parse(iso)
    } catch (e: Exception) {
        null
    }

    /** Jours (`yyyy-MM-dd`) où le donneur a déjà un rendez-vous en attente ou confirmé : non réservables. */
    fun joursBloques(rdvs: List<RdvApi>): Set<String> =
        rdvs.filter { peutAnnuler(it.statut) }.map { it.dateRendezVous.take(10) }.toSet()

    /** Prochain rendez-vous actif à venir. */
    fun prochainRdv(rdvs: List<RdvApi>, maintenant: LocalDateTime = LocalDateTime.now()): RdvApi? =
        rdvs.filter { peutAnnuler(it.statut) && (date(it.dateRendezVous)?.let { d -> d >= maintenant } == true) }
            .minByOrNull { date(it.dateRendezVous)!! }

    fun historique(rdvs: List<RdvApi>): List<RdvApi> =
        rdvs.sortedByDescending { date(it.dateRendezVous) ?: LocalDateTime.MIN }

    data class StatsDonneur(val effectues: Int, val planifies: Int, val dernierDon: String?)

    /** Le dernier don du dossier donneur fait foi ; sinon le dernier rendez-vous effectué. */
    fun statsDonneur(donneur: DonneurApi?, rdvs: List<RdvApi>): StatsDonneur {
        val effectues = rdvs.filter { it.statut == EFFECTUE }
        return StatsDonneur(
            effectues = effectues.size,
            planifies = rdvs.count { peutAnnuler(it.statut) },
            dernierDon = donneur?.dateDernierDon
                ?: effectues.maxByOrNull { date(it.dateRendezVous) ?: LocalDateTime.MIN }?.dateRendezVous
        )
    }

    /** Date-heure choisie -> valeur attendue par le backend (`LocalDateTime`, avec secondes). */
    fun versApiDateHeure(dateTime: LocalDateTime): String = dateTime.withSecond(0).withNano(0).toString().let {
        if (it.length == 16) "$it:00" else it
    }

    // ----- Rôles -----
    /** Rôle du backend -> rôle applicatif. */
    fun roleApplicatif(roleApi: String): String? = when (roleApi) {
        "ADMIN" -> "admin"
        "DONNEUR" -> "donneur"
        "PERSONNEL_CENTRE" -> "centre"
        else -> null
    }

    /**
     * Vérifie que le rôle réel du compte correspond à l'écran de connexion utilisé.
     * Renvoie le message (français) à afficher en cas de refus, ou null si tout est correct.
     */
    fun verifierProfil(attendu: String, reel: String?): String? = when {
        reel == attendu -> null
        attendu == "admin" && reel == "donneur" ->
            "Vous ne pouvez pas vous connecter en tant que donneur avec la connexion administrateur."
        attendu == "donneur" && reel == "admin" ->
            "Vous ne pouvez pas vous connecter en tant qu'administrateur avec la connexion donneur."
        attendu == "admin" -> "Ce compte n'est pas un compte administrateur."
        else -> "Ce compte n'est pas un compte donneur."
    }

    // ----- Messages d'erreur HTTP (même logique que HttpErrorService côté Angular) -----
    const val MSG_INJOIGNABLE = "Serveur injoignable. Vérifiez que le backend tourne sur le port 8081."
    const val MSG_SESSION_EXPIREE = "Votre session a expiré. Veuillez vous reconnecter."

    fun messageHttp(code: Int, erreur: ErreurApi?, defaut: String = "Une erreur est survenue."): String = when {
        code == 0 -> MSG_INJOIGNABLE
        code == 400 || code == 422 -> {
            val details = erreur?.erreurs?.values?.joinToString(", ").orEmpty()
            val base = erreur?.message ?: "Données invalides."
            if (details.isNotEmpty()) "$base $details" else base
        }
        code == 401 -> erreur?.message ?: "Authentification requise ou session expirée."
        code == 403 -> "Accès refusé : vous n'avez pas les droits nécessaires."
        code == 404 -> erreur?.message ?: "Ressource introuvable."
        code == 409 -> erreur?.message ?: "Cette opération est en conflit avec l'état actuel des données."
        code >= 500 -> "Erreur interne du serveur. Veuillez réessayer plus tard."
        else -> defaut
    }
}
