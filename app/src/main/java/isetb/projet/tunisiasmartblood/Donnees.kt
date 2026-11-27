package isetb.projet.tunisiasmartblood

// ===========================================================================================
// DONNÉES SIMULÉES (en mémoire, perdues à la fermeture de l'application).
// Le backend n'expose pas encore d'endpoint pour : campagnes, annonces, créneaux, rendez-vous
// côté centre, validation des comptes. Même situation que dans l'application Angular.
// Tout ce qui existe côté serveur (connexion, donneurs, centres, rendez-vous) passe par l'API.
// ===========================================================================================

data class Campagne(
    val id: Int, val titre: String, val organisateur: String,
    val lieu: String, val gouvernorat: String, val date: String, val horaire: String,
    val groupes: String? = null
)

data class Annonce(
    val id: Int, val groupe: String, var urgent: Boolean, val etablissement: String,
    val gouvernorat: String, val description: String, val publie: String
)

data class Compte(
    val role: String, val nom: String, val email: String,
    val gouvernorat: String? = null, val telephone: String? = null, val adresse: String? = null
)

data class CompteAttente(val id: Int, val nom: String, val type: String, val gouvernorat: String, val email: String)

/** Donneur actif connu de la plateforme, SIMULÉ : un établissement n'a pas de compte serveur pour lire /donneurs. */
data class DonneurDemo(val id: Int, val nom: String, val groupe: String, val gouvernorat: String, var statut: String)

data class Creneau(val id: Int, val date: String, val horaire: String, val places: Int, val reservees: Int)

/** Rendez-vous reçus par un centre (SIMULÉ : les centres ne peuvent pas encore s'authentifier côté serveur). */
data class RendezVous(val id: Int, val donneur: String, val groupe: String, val creneau: String, var statut: String)

private var compteurId = 1000
fun nouvelId() = ++compteurId

val listeGouvernorats = listOf(
    "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan", "Bizerte", "Béja",
    "Jendouba", "Kef", "Siliana", "Sousse", "Monastir", "Mahdia", "Sfax", "Kairouan",
    "Kasserine", "Sidi Bouzid", "Gabès", "Medenine", "Tataouine", "Gafsa", "Tozeur", "Kebili"
)

val listeGroupes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

val comptesEnAttente = mutableListOf(
    CompteAttente(1, "Centre de transfusion de Gabès", "Centre de collecte", "Gabès", "contact@ctsgabes.tn"),
    CompteAttente(2, "Clinique El Manar", "Établissement de santé", "Tunis", "contact@elmanar.tn"),
    CompteAttente(3, "Hôpital régional de Gafsa", "Établissement de santé", "Gafsa", "contact@hrgafsa.tn")
)

val donneursDemo = mutableListOf(
    DonneurDemo(1, "Ben Salah Amine", "O+", "Tunis", "Actif"),
    DonneurDemo(2, "Trabelsi Sarra", "A+", "Sousse", "Actif"),
    DonneurDemo(3, "Gharbi Walid", "B-", "Sfax", "Actif"),
    DonneurDemo(4, "Jlassi Nour", "O-", "Béja", "Actif"),
    DonneurDemo(5, "Mansouri Khaled", "AB+", "Ariana", "Suspendu"),
    DonneurDemo(6, "Hamdi Ines", "A+", "Nabeul", "Actif")
)

val creneaux = mutableListOf(
    Creneau(1, "12 décembre 2026", "08h30 – 10h00", 20, 12),
    Creneau(2, "12 décembre 2026", "10h00 – 12h00", 20, 5),
    Creneau(3, "12 décembre 2026", "13h00 – 15h00", 15, 3)
)

val rendezVous = mutableListOf(
    RendezVous(1, "Ben Salah Amine", "O+", "12 décembre 2026 · 08h30", "En attente"),
    RendezVous(2, "Trabelsi Sarra", "A+", "12 décembre 2026 · 09h00", "Confirmé"),
    RendezVous(3, "Jlassi Nour", "O-", "12 décembre 2026 · 10h00", "En attente"),
    RendezVous(4, "Hamdi Ines", "A+", "12 décembre 2026 · 10h30", "Confirmé"),
    RendezVous(5, "Gharbi Walid", "B-", "12 décembre 2026 · 13h00", "Participation enregistrée")
)

fun campagnesCentre() = campagnes.filter { it.organisateur == Session.nom }
fun mesAnnonces() = annonces.filter { it.etablissement == Session.nom }
fun rdvEnAttente() = rendezVous.count { it.statut == "En attente" }

val campagnes = mutableListOf(
    Campagne(1, "Grande collecte de fin d'année", "Centre National de Transfusion Sanguine", "Avenue Jean Jaurès, Tunis", "Tunis", "12 décembre 2026", "08h30 – 15h00", "O+, O-, A+"),
    Campagne(2, "Collecte universitaire", "Association Jeunes Solidaires", "Campus universitaire, Ariana", "Ariana", "15 décembre 2026", "09h00 – 14h00"),
    Campagne(3, "Don de sang au cœur de Sousse", "Centre régional de Sousse", "Place de la Médina, Sousse", "Sousse", "19 décembre 2026", "08h00 – 13h00"),
    Campagne(4, "Journée solidaire de Sfax", "Hôpital Habib Bourguiba", "Route de l'Aéroport, Sfax", "Sfax", "22 décembre 2026", "08h30 – 16h00"),
    Campagne(5, "Caravane du don – Nabeul", "Croissant-Rouge Tunisien", "Centre-ville, Nabeul", "Nabeul", "28 décembre 2026", "09h00 – 15h00"),
    Campagne(6, "Collecte du Nouvel An", "Centre de Bizerte", "Avenue Habib Bourguiba, Bizerte", "Bizerte", "3 janvier 2027", "09h00 – 14h00")
)

val annonces = mutableListOf(
    Annonce(1, "O-", true, "Hôpital Charles Nicolle", "Tunis", "Besoin urgent de donneurs O- pour des interventions chirurgicales programmées.", "Il y a 2 heures"),
    Annonce(2, "A+", false, "Hôpital Farhat Hached", "Sousse", "Recherche de donneurs A+ pour renouveler le stock du service d'hématologie.", "Il y a 5 heures"),
    Annonce(3, "B-", true, "Hôpital Hédi Chaker", "Sfax", "Patient en réanimation : donneurs B- recherchés en priorité.", "Hier"),
    Annonce(4, "AB+", false, "Clinique Les Jasmins", "Ariana", "Stock bas en AB+. Merci de prendre rendez-vous dans les prochains jours.", "Il y a 2 jours"),
    Annonce(5, "O+", false, "Centre de transfusion de Nabeul", "Nabeul", "Les donneurs O+ sont les bienvenus toute la semaine, de 8h à 14h.", "Il y a 3 jours"),
    Annonce(6, "A-", true, "Hôpital de Bizerte", "Bizerte", "Besoin urgent de donneurs A- pour une maternité.", "Il y a 3 jours"),
    Annonce(7, "B+", false, "Hôpital régional de Ben Arous", "Ben Arous", "Recherche de donneurs B+ pour reconstituer le stock du service des urgences.", "Il y a 4 heures"),
    Annonce(8, "AB-", true, "Hôpital de la Manouba", "Manouba", "Groupe rare : besoin urgent de donneurs AB- pour un patient en soins intensifs.", "Il y a 6 heures"),
    Annonce(9, "O+", false, "Hôpital régional de Zaghouan", "Zaghouan", "Collecte ouverte aux donneurs O+ pour les besoins du bloc opératoire.", "Hier"),
    Annonce(10, "B+", false, "Hôpital régional de Béja", "Béja", "Stock en baisse : les donneurs B+ sont attendus cette semaine.", "Il y a 2 jours"),
    Annonce(11, "AB-", true, "Hôpital de Jendouba", "Jendouba", "Besoin urgent de donneurs AB- pour une intervention cardiaque.", "Il y a 5 heures"),
    Annonce(12, "A+", false, "Hôpital régional du Kef", "Kef", "Recherche de donneurs A+ pour le service de médecine interne.", "Il y a 3 jours"),
    Annonce(13, "O-", true, "Hôpital de Siliana", "Siliana", "Donneurs O- recherchés en urgence : donneur universel, stock presque épuisé.", "Il y a 1 heure"),
    Annonce(14, "B-", false, "Hôpital Fattouma Bourguiba", "Monastir", "Appel aux donneurs B- pour le service d'oncologie.", "Hier"),
    Annonce(15, "AB+", false, "Hôpital Tahar Sfar", "Mahdia", "Les donneurs AB+ sont invités à se présenter au centre de transfusion.", "Il y a 2 jours"),
    Annonce(16, "B+", true, "Hôpital Ibn El Jazzar", "Kairouan", "Besoin urgent de donneurs B+ pour des patients dialysés.", "Il y a 3 heures"),
    Annonce(17, "AB-", false, "Hôpital régional de Kasserine", "Kasserine", "Recherche de donneurs AB- pour constituer une réserve de sécurité.", "Il y a 4 jours"),
    Annonce(18, "A-", false, "Hôpital régional de Sidi Bouzid", "Sidi Bouzid", "Donneurs A- bienvenus tous les matins, de 8h à 13h.", "Il y a 2 jours"),
    Annonce(19, "O+", true, "Hôpital de Gabès", "Gabès", "Besoin urgent de donneurs O+ suite à plusieurs admissions aux urgences.", "Il y a 7 heures"),
    Annonce(20, "O-", false, "Hôpital régional de Medenine", "Medenine", "Recherche de donneurs O- pour renouveler le stock du centre de transfusion.", "Hier"),
    Annonce(21, "A+", false, "Hôpital de Tataouine", "Tataouine", "Appel aux donneurs A+ pour le service de chirurgie.", "Il y a 3 jours"),
    Annonce(22, "B-", true, "Hôpital Ahmed Gasmi", "Gafsa", "Besoin urgent de donneurs B- pour une patiente en maternité.", "Il y a 2 heures"),
    Annonce(23, "AB+", false, "Hôpital régional de Tozeur", "Tozeur", "Les donneurs AB+ sont attendus pour la collecte de la semaine.", "Il y a 5 jours"),
    Annonce(24, "A-", true, "Hôpital régional de Kebili", "Kebili", "Besoin urgent de donneurs A- pour un patient en réanimation.", "Il y a 6 heures")
)
