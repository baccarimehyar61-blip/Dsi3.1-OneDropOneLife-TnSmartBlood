package isetb.projet.tunisiasmartblood

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import isetb.projet.tunisiasmartblood.databinding.ActivityMainBinding
import isetb.projet.tunisiasmartblood.databinding.ItemCarteBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private val gouvernorats = listOf(
        "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan", "Bizerte", "Béja",
        "Jendouba", "Kef", "Siliana", "Sousse", "Monastir", "Mahdia", "Sfax", "Kairouan",
        "Kasserine", "Sidi Bouzid", "Gabès", "Medenine", "Tataouine", "Gafsa", "Tozeur", "Kebili"
    )
    private val groupesSanguins = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    private val pourquoi = listOf(
        "Sauver des vies" to "Un simple geste peut faire toute la différence.",
        "Une communauté solidaire" to "Des donneurs, des patients, un même objectif.",
        "Des centres proches" to "Trouvez facilement les centres de collecte près de chez vous."
    )
    private val chiffres = listOf(
        "4" to "acteurs réunis : donneurs, centres, établissements de santé et associations",
        "24h/24" to "un accès permanent aux campagnes, aux besoins et aux rappels",
        "1" to "seul endroit pour trouver où donner et où l'on a besoin de vous"
    )
    private val presentation = listOf(
        "Besoins en sang" to "Les centres et les établissements de santé publient leurs besoins, avec le groupe sanguin recherché et le lieu.",
        "Campagnes" to "Les centres et les associations organisent des collectes que chacun peut consulter et rejoindre.",
        "Rendez-vous" to "Les donneurs réservent un créneau, les centres le confirment, et un rappel est envoyé avant le jour J."
    )
    private val etapes = listOf(
        "1. Créez votre compte" to "Choisissez votre profil : donneur, centre de collecte, établissement de santé ou association.",
        "2. Publiez ou trouvez" to "Les centres publient leurs campagnes et leurs besoins. Les donneurs cherchent selon leur groupe sanguin et leur ville.",
        "3. Réservez et soyez rappelé" to "Le donneur réserve un créneau, le centre le confirme, et un rappel est envoyé avant le rendez-vous."
    )
    private val confiance = listOf(
        "Des comptes vérifiés" to "Les comptes des centres et des établissements sont validés avant de pouvoir publier. Les annonces sont modérées.",
        "Vos données protégées" to "Nous demandons seulement le nécessaire, dans le respect de la loi tunisienne sur les données personnelles.",
        "Un don volontaire" to "Le don de sang est un geste libre et solidaire. La plateforme est là pour le faciliter, jamais pour l'imposer.",
        "Un complément, pas un remplaçant" to "Elle aide à mieux s'organiser, aux côtés des professionnels de santé et des centres de transfusion."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(b.root) { v, insets ->
            val s = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(s.left, s.top, s.right, s.bottom)
            insets
        }

        // Contenu de l'accueil
        remplir(b.llPourquoi, pourquoi)
        remplir(b.llChiffres, chiffres, gros = true)
        remplir(b.llPresentation, presentation)
        remplir(b.llEtapes, etapes)
        remplir(b.llConfiance, confiance)

        // Profils (page « choix du profil »)
        ajouterProfil("Donneur", "Je souhaite donner mon sang et sauver des vies.", "donneur")
        ajouterProfil("Établissement de santé", "Je représente un hôpital ou une clinique.", "etablissement")
        ajouterProfil("Centre de collecte", "J'organise des collectes de sang.", "centre")

        // Listes déroulantes
        remplirSpinner(b.spGroupe, "Sélectionnez votre groupe", groupesSanguins)
        remplirSpinner(b.spGouvernorat, "Sélectionnez votre gouvernorat", gouvernorats)

        // Date de naissance
        b.etDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                b.etDate.setText(String.format("%02d/%02d/%04d", d, m + 1, y))
            }, 2000, 0, 1).show()
        }

        // Navigation (équivalent de allerAccueil / allerConnexion / allerInscription ...)
        b.logo.setOnClickListener { afficher(b.viewAccueil) }
        b.btnTopConnexion.setOnClickListener { afficher(b.viewConnexion) }
        b.btnCreerCompte.setOnClickListener { afficher(b.viewChoix) }
        b.btnSInscrire.setOnClickListener { afficher(b.viewChoix) }
        b.btnDejaCompte.setOnClickListener { afficher(b.viewConnexion) }
        b.btnAllerConnexion.setOnClickListener { afficher(b.viewConnexion) }
        b.btnRetour.setOnClickListener { afficher(b.viewChoix) }

        b.btnSeConnecter.setOnClickListener { seConnecter() }
        b.btnInscrire.setOnClickListener { sInscrire() }
    }

    // ---------- Navigation ----------
    private fun afficher(vue: ScrollView) {
        listOf(b.viewAccueil, b.viewConnexion, b.viewChoix, b.viewInscription).forEach {
            it.visibility = if (it == vue) View.VISIBLE else View.GONE
        }
        vue.scrollTo(0, 0)
        b.tvMessageLogin.text = ""
        b.tvMessageInsc.text = ""
    }

    private fun choisirProfil(role: String) {
        val donneur = role == "donneur"
        b.llDonneur.visibility = if (donneur) View.VISIBLE else View.GONE
        b.llEtab.visibility = if (donneur) View.GONE else View.VISIBLE

        b.tvRole.text = when (role) {
            "donneur" -> "DONNEUR"
            "etablissement" -> "ÉTABLISSEMENT DE SANTÉ"
            else -> "CENTRE DE COLLECTE"
        }
        val nom = if (role == "centre") "Nom du centre" else "Nom de l'établissement"
        b.tvLabelNom.text = nom
        b.etNomEtab.hint = nom
        b.etEmailInsc.hint = if (donneur) "votre@email.com" else "contact@etablissement.tn"
        afficher(b.viewInscription)
    }

    // ---------- Actions ----------
    private fun seConnecter() {
        if (requis(b.etEmailLogin, b.etPasswordLogin) && emailValide(b.etEmailLogin)) {
            b.tvMessageLogin.text = "Connexion réussie !"
        }
    }

    private fun sInscrire() {
        var ok = requis(b.etEmailInsc, b.etPasswordInsc)
        if (b.llDonneur.visibility == View.VISIBLE) {
            ok = requis(b.etNomDonneur, b.etPrenom, b.etDate, b.etTel) && ok
            if (b.spGroupe.selectedItemPosition == 0 || b.spGouvernorat.selectedItemPosition == 0) ok = false
        } else {
            ok = requis(b.etNomEtab) && ok
        }
        if (ok && emailValide(b.etEmailInsc)) {
            b.tvMessageInsc.text = "Compte créé avec succès !"
        }
    }

    // ---------- Utilitaires ----------
    private fun requis(vararg champs: EditText): Boolean {
        var ok = true
        champs.forEach {
            if (it.text.isNullOrBlank()) {
                it.error = "Champ requis"
                ok = false
            }
        }
        return ok
    }

    private fun emailValide(champ: EditText): Boolean {
        val ok = Patterns.EMAIL_ADDRESS.matcher(champ.text.toString().trim()).matches()
        if (!ok) champ.error = "Email invalide"
        return ok
    }

    private fun remplirSpinner(sp: Spinner, indication: String, items: List<String>) {
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf(indication) + items)
    }

    private fun remplir(conteneur: LinearLayout, items: List<Pair<String, String>>, gros: Boolean = false) {
        items.forEach { (titre, texte) ->
            val c = ItemCarteBinding.inflate(layoutInflater, conteneur, false)
            c.tvTitre.text = titre
            c.tvTexte.text = texte
            if (gros) {
                c.tvTitre.textSize = 38f
                c.tvTitre.setTextColor(getColor(R.color.rouge))
            }
            conteneur.addView(c.root)
        }
    }

    private fun ajouterProfil(titre: String, texte: String, role: String) {
        val c = ItemCarteBinding.inflate(layoutInflater, b.llProfils, false)
        c.tvTitre.text = titre
        c.tvTexte.text = texte
        c.root.setOnClickListener { choisirProfil(role) }
        b.llProfils.addView(c.root)
    }
}