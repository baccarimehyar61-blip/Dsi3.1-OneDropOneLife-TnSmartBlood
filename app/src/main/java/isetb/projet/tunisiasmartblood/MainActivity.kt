package isetb.projet.tunisiasmartblood

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import isetb.projet.tunisiasmartblood.databinding.ActivityMainBinding
import isetb.projet.tunisiasmartblood.databinding.ItemAnnonceBinding
import isetb.projet.tunisiasmartblood.databinding.ItemCampagneBinding
import isetb.projet.tunisiasmartblood.databinding.ItemCarteBinding
import isetb.projet.tunisiasmartblood.databinding.ItemGestionBinding
import isetb.projet.tunisiasmartblood.databinding.ItemRdvBinding
import isetb.projet.tunisiasmartblood.databinding.ViewEspaceBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
private const val TYPE_EMAIL = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

/** Centre affiché par l'administration : réel (id > 0) ou issu d'un compte validé en simulation (id < 0). */
private data class CentreAdmin(val id: Long, val nom: String, val ville: String, var actif: Boolean)

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private var filtreGouvCamp = ""
    private var filtreGroupe = ""
    private var filtreGouvAnn = ""

    private var modeAdmin = false
    private var roleInscription = "donneur"
    private var connexionEnCours = false
    private var inscriptionEnCours = false

    private var ongletAdmin = "rendezvous"
    private var ongletCentre = "rendezvous"
    private var ongletEtab = "besoins"
    private var retourAdmin = ""
    private var retourEspace = ""

    // Espace donneur (données réelles de l'API)
    private var msgDonneur = ""
    private var erreurDonneur = ""
    private var rdvChoisi: Calendar? = null
    private var donneurMoi: DonneurApi? = null
    private var centresApi = listOf<CentreApi>()
    private var rdvDonneur = listOf<RdvApi>()
    private var reservationEnCours = false

    // Espace administrateur (données réelles de l'API)
    private var adminDonneurs = listOf<DonneurApi>()
    private var adminCentres = mutableListOf<CentreAdmin>()
    private var adminRdv = listOf<RdvApi>()

    private val rolesConnexion = listOf("donneur", "etablissement", "centre")

    private val gouvernorats = listeGouvernorats
    private val groupesSanguins = listeGroupes

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
    private val apropos = listOf(
        "Pour les donneurs" to "Trouver une collecte, réserver un créneau et recevoir un rappel avant le jour J.",
        "Pour les centres" to "Publier des campagnes, gérer les rendez-vous et mobiliser plus rapidement.",
        "Pour les établissements" to "Signaler un besoin en sang avec le groupe recherché et le lieu concerné."
    )
    private val contactInfos = listOf(
        "Email" to "TunisiaSmartBlood@gmail.tn",
        "Téléphone" to "+216 70 234 000",
        "Adresse" to "Tunis, Tunisie"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Session.init(this)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(b.root) { v, insets ->
            val s = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(s.left, s.top, s.right, s.bottom)
            insets
        }

        remplir(b.llPourquoi, pourquoi)
        remplir(b.llChiffres, chiffres, gros = true)
        remplir(b.llPresentation, presentation)
        remplir(b.llEtapes, etapes)
        remplir(b.llConfiance, confiance)
        remplir(b.llApropos, apropos)
        remplir(b.llContactInfos, contactInfos)

        ajouterProfil("Donneur", "Je souhaite donner mon sang et sauver des vies.", "donneur")
        ajouterProfil("Établissement de santé", "Je représente un hôpital ou une clinique.", "etablissement")
        ajouterProfil("Centre de collecte", "J'organise des collectes de sang.", "centre")

        remplirSpinner(b.spGroupe, "Sélectionnez votre groupe", groupesSanguins)
        remplirSpinner(b.spGouvernorat, "Sélectionnez votre gouvernorat", gouvernorats)

        // Profil choisi dans le formulaire de connexion
        b.spProfilConnexion.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Donneur", "Établissement de santé", "Centre de collecte")
        )

        configurerFiltre(b.spFiltreGouvCamp, "Tous les gouvernorats", gouvernorats) {
            filtreGouvCamp = it
            afficherCampagnes()
        }
        configurerFiltre(b.spFiltreGroupe, "Tous les groupes", groupesSanguins) {
            filtreGroupe = it
            afficherAnnonces()
        }
        configurerFiltre(b.spFiltreGouvAnn, "Tous les gouvernorats", gouvernorats) {
            filtreGouvAnn = it
            afficherAnnonces()
        }

        // Date de naissance : jamais dans le futur
        b.etDate.setOnClickListener {
            val c = Calendar.getInstance()
            val dialogue = DatePickerDialog(this, { _, y, m, d ->
                b.etDate.tag = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                b.etDate.setText(String.format(Locale.US, "%02d/%02d/%04d", d, m + 1, y))
                b.etDate.error = null
            }, 2000, 0, 1)
            dialogue.datePicker.maxDate = c.timeInMillis
            dialogue.show()
        }

        b.logo.setOnClickListener { afficher(b.viewAccueil) }

        b.btnTopConnexion.setOnClickListener {
            if (Session.connecte) deconnexion() else afficher(b.viewConnexion)
        }
        b.btnCreerCompte.setOnClickListener { afficher(b.viewChoix) }
        b.btnSInscrire.setOnClickListener { afficher(b.viewChoix) }
        b.btnDejaCompte.setOnClickListener { afficher(b.viewConnexion) }
        b.btnAllerConnexion.setOnClickListener { afficher(b.viewConnexion) }
        b.btnRetour.setOnClickListener { afficher(b.viewChoix) }

        b.tabAccueil.setOnClickListener { afficher(b.viewAccueil) }
        b.tabContact.setOnClickListener { afficher(b.viewContact) }
        b.tabAnnonces.setOnClickListener { allerAnnonces() }
        b.tabApropos.setOnClickListener { afficher(b.viewApropos) }
        b.tabAdmin.setOnClickListener { allerAdmin() }
        b.tabEspace.setOnClickListener { allerEspace() }

        b.footAccueil.setOnClickListener { afficher(b.viewAccueil) }
        b.footContact.setOnClickListener { afficher(b.viewContact) }
        b.footAnnonces.setOnClickListener { allerAnnonces() }
        b.footApropos.setOnClickListener { afficher(b.viewApropos) }

        b.btnDecouvrir.setOnClickListener { allerCampagnes() }
        b.tvVoirCampagnes.setOnClickListener { allerCampagnes() }
        b.btnRejoindre.setOnClickListener { afficher(b.viewChoix) }
        b.btnEnvoyer.setOnClickListener { envoyerContact() }

        configurerDonneur()

        b.btnModeUser.setOnClickListener { choisirMode(false) }
        b.btnModeAdmin.setOnClickListener { choisirMode(true) }
        b.btnSeConnecter.setOnClickListener { seConnecter() }
        b.btnInscrire.setOnClickListener { sInscrire() }

        // Bouton « Retour » du téléphone : revient à l'accueil avant de quitter l'application
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (b.viewAccueil.visibility != View.VISIBLE) {
                    afficher(b.viewAccueil)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        choisirMode(false)
        majSession()
        majOnglets(b.viewAccueil)

        // La session survit à la fermeture de l'application (jeton encore valide)
        if (Session.estValide()) {
            majSession()
            if (Session.estAdmin) allerAdmin() else allerEspace()
        } else {
            majSession()
        }
    }

    // ----- Navigation -----
    private fun afficher(vue: View) {
        // L'écran de connexion s'ouvre toujours sur l'onglet « Utilisateur » (comme sur Angular)
        if (vue == b.viewConnexion && b.viewConnexion.visibility != View.VISIBLE) choisirMode(false)
        listOf(
            b.viewAccueil, b.viewConnexion, b.viewChoix, b.viewInscription,
            b.viewCampagnes, b.viewAnnonces, b.viewApropos, b.viewContact,
            b.viewAdmin.root, b.viewEspaceCentre.root, b.viewEspaceEtab.root,
            b.viewEspaceDonneur.root
        ).forEach {
            it.visibility = if (it == vue) View.VISIBLE else View.GONE
        }
        (vue as? android.widget.ScrollView)?.scrollTo(0, 0)
        b.tvMessageLogin.text = ""
        b.tvMessageInsc.text = ""
        b.tvMessageContact.text = ""
        majOnglets(vue)
    }

    private fun majOnglets(vue: View) {
        val onglets = listOf(
            b.tabAccueil to listOf<View>(b.viewAccueil),
            b.tabContact to listOf<View>(b.viewContact),
            b.tabAnnonces to listOf<View>(b.viewAnnonces),
            b.tabApropos to listOf<View>(b.viewApropos),
            b.tabAdmin to listOf<View>(b.viewAdmin.root),
            b.tabEspace to listOf<View>(b.viewEspaceCentre.root, b.viewEspaceEtab.root, b.viewEspaceDonneur.root)
        )
        onglets.forEach { (onglet, vues) ->
            val actif = vue in vues
            onglet.setTextColor(if (actif) getColor(R.color.texte) else Color.parseColor("#65575B"))
            onglet.setTypeface(null, if (actif) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    // ----- Session -----
    private fun majSession() {
        val connecte = Session.connecte
        b.btnTopConnexion.text = if (connecte) "Déconnexion" else "Se connecter"
        b.tabAdmin.visibility = if (Session.estAdmin) View.VISIBLE else View.GONE
        b.tabEspace.visibility = if (connecte && !Session.estAdmin) View.VISIBLE else View.GONE
    }

    /** Efface la session (jeton compris) et toutes les données de l'utilisateur en mémoire. */
    private fun deconnexion() {
        Session.fermer()
        rdvChoisi = null
        msgDonneur = ""
        erreurDonneur = ""
        retourEspace = ""
        retourAdmin = ""
        donneurMoi = null
        centresApi = listOf()
        rdvDonneur = listOf()
        adminDonneurs = listOf()
        adminCentres = mutableListOf()
        adminRdv = listOf()
        b.viewEspaceDonneur.etDateRdv.text.clear()
        majSession()
        afficher(b.viewAccueil)
    }

    /** Le serveur a refusé le jeton : retour à la connexion, comme l'intercepteur 401 d'Angular. */
    private fun sessionExpiree() {
        deconnexion()
        afficher(b.viewConnexion)
        b.tvMessageLogin.text = Regles.MSG_SESSION_EXPIREE
    }

    private fun choisirProfil(role: String, verrou: Boolean = false) {
        roleInscription = role
        val donneur = role == "donneur"
        b.llDonneur.visibility = if (donneur) View.VISIBLE else View.GONE
        b.llEtab.visibility = if (donneur) View.GONE else View.VISIBLE

        b.btnRetour.visibility = if (verrou) View.GONE else View.VISIBLE

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

    /** « Réserver un créneau » / « Je peux donner » : espace donneur si connecté, sinon connexion. */
    private fun versReservation() {
        if (Session.estValide() && Session.role == "donneur") allerEspace() else afficher(b.viewConnexion)
    }

    // ----- Campagnes et annonces publiques -----
    private fun allerCampagnes() {
        b.spFiltreGouvCamp.setSelection(0)
        filtreGouvCamp = ""
        afficherCampagnes()
        afficher(b.viewCampagnes)
    }

    private fun allerAnnonces() {
        b.spFiltreGroupe.setSelection(0)
        b.spFiltreGouvAnn.setSelection(0)
        filtreGroupe = ""
        filtreGouvAnn = ""
        afficherAnnonces()
        afficher(b.viewAnnonces)
    }

    private fun afficherCampagnes() {
        b.llCampagnes.removeAllViews()
        val liste = campagnes.filter { filtreGouvCamp.isEmpty() || it.gouvernorat == filtreGouvCamp }
        b.tvVideCampagnes.visibility = if (liste.isEmpty()) View.VISIBLE else View.GONE

        liste.forEach { c ->
            val carte = ItemCampagneBinding.inflate(layoutInflater, b.llCampagnes, false)
            carte.tvDate.text = c.date
            carte.tvTitre.text = c.titre
            carte.tvOrganisateur.text = c.organisateur
            carte.tvLieu.text = "📍 ${c.lieu}"
            carte.tvHoraire.text = "🕒 ${c.horaire}"
            carte.btnReserver.setOnClickListener { versReservation() }
            // Détail de la campagne (page /campaigns/:id dans l'application Angular)
            carte.tvTitre.setOnClickListener { detailCampagne(c) }
            b.llCampagnes.addView(carte.root)
        }
    }

    private fun detailCampagne(c: Campagne) {
        MaterialAlertDialogBuilder(this)
            .setTitle(c.titre)
            .setMessage(
                "Organisée par ${c.organisateur}\n\n📅 ${c.date}\n🕒 ${c.horaire}\n" +
                        "📍 ${c.lieu} (${c.gouvernorat})\n🩸 ${c.groupes ?: "Tous les groupes"}"
            )
            .setPositiveButton("Réserver un créneau") { _, _ -> versReservation() }
            .setNegativeButton("Fermer", null)
            .show()
    }

    private fun afficherAnnonces() {
        b.llAnnonces.removeAllViews()
        val liste = annonces.filter {
            (filtreGroupe.isEmpty() || it.groupe == filtreGroupe) &&
                    (filtreGouvAnn.isEmpty() || it.gouvernorat == filtreGouvAnn)
        }
        b.tvVideAnnonces.visibility = if (liste.isEmpty()) View.VISIBLE else View.GONE

        liste.forEach { a ->
            val carte = ItemAnnonceBinding.inflate(layoutInflater, b.llAnnonces, false)
            carte.tvGroupe.text = a.groupe
            carte.tvUrgent.visibility = if (a.urgent) View.VISIBLE else View.GONE
            carte.tvEtablissement.text = a.etablissement
            carte.tvDescription.text = a.description
            carte.tvGouvernorat.text = "📍 ${a.gouvernorat}"
            carte.tvPublie.text = "🕒 ${a.publie}"

            carte.btnDonner.setOnClickListener { versReservation() }
            b.llAnnonces.addView(carte.root)
        }
    }

    private fun configurerFiltre(sp: Spinner, indication: String, items: List<String>, onChoix: (String) -> Unit) {
        val liste = listOf(indication) + items
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, liste)
        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                onChoix(if (pos == 0) "" else liste[pos])
            }

            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    // ----- Connexion / inscription -----
    private fun choisirMode(admin: Boolean) {
        modeAdmin = admin
        b.llProfilConnexion.visibility = if (admin) View.GONE else View.VISIBLE
        b.tvInfoAdmin.visibility = if (admin) View.VISIBLE else View.GONE
        styleMode(b.btnModeUser, !admin)
        styleMode(b.btnModeAdmin, admin)
        b.tvMessageLogin.text = ""
    }

    private fun styleMode(btn: MaterialButton, actif: Boolean) {
        btn.backgroundTintList = ColorStateList.valueOf(getColor(if (actif) R.color.rouge else R.color.white))
        btn.setTextColor(getColor(if (actif) R.color.white else R.color.rouge))
        btn.strokeWidth = dp(2)
        btn.strokeColor = ColorStateList.valueOf(getColor(R.color.rouge))
    }

    /**
     * Connexion. Administrateur et donneur passent par l'API (JWT) ; centre / établissement sont SIMULÉS
     * (comme dans l'application Angular). Le profil choisi est envoyé au serveur ET vérifié ici :
     * un compte dont le rôle réel ne correspond pas à l'écran est refusé, sans aucune session ni jeton.
     */
    private fun seConnecter() {
        if (connexionEnCours) return
        if (!(requis(b.etEmailLogin, b.etPasswordLogin) && emailValide(b.etEmailLogin))) return
        val email = b.etEmailLogin.text.toString().trim()
        val mdp = b.etPasswordLogin.text.toString()

        val profilChoisi = rolesConnexion[b.spProfilConnexion.selectedItemPosition]
        if (!modeAdmin && profilChoisi != "donneur") {
            val nom = if (profilChoisi == "centre") "Centre National de Transfusion Sanguine" else "Hôpital Charles Nicolle"
            Session.ouvrirDemo(profilChoisi, nom, email, "Tunis")
            majSession()
            allerEspace()
            return
        }

        val attendu = if (modeAdmin) "admin" else "donneur"
        connexionEnCours = true
        b.btnSeConnecter.isEnabled = false
        b.tvMessageLogin.text = "Connexion en cours..."
        lifecycleScope.launch {
            val res = appeler(protege = false) {
                RetrofitClient.api.login(LoginRequest(email, mdp, if (modeAdmin) "ADMIN" else "DONNEUR"))
            }
            connexionEnCours = false
            b.btnSeConnecter.isEnabled = true
            when (res) {
                is Resultat.Ok -> {
                    val auth = res.valeur
                    val reel = Regles.roleApplicatif(auth.role)
                    val refus = Regles.verifierProfil(attendu, reel)
                    if (refus != null || reel == null) {
                        b.tvMessageLogin.text = refus ?: "Ce compte ne peut pas se connecter."
                        return@launch
                    }
                    Session.ouvrirApi(reel, auth)
                    b.tvMessageLogin.text = ""
                    b.etPasswordLogin.text.clear()
                    majSession()
                    if (reel == "admin") allerAdmin() else allerEspace()
                }
                is Resultat.Echec -> b.tvMessageLogin.text = when (res.code) {
                    401 -> if (modeAdmin) "Accès refusé : identifiants administrateur incorrects."
                    else "Email ou mot de passe incorrect."
                    403 -> res.serveur ?: res.message // refus de profil renvoyé par le serveur
                    else -> res.message
                }
            }
        }
    }

    /** Inscription : donneur via l'API (compte + fiche), centre / établissement simulés. */
    private fun sInscrire() {
        if (inscriptionEnCours) return
        b.tvMessageInsc.text = ""
        val estDonneur = roleInscription == "donneur"
        var ok = requis(b.etEmailInsc, b.etPasswordInsc, b.etConfirmInsc)
        if (estDonneur) {
            ok = requis(b.etNomDonneur, b.etPrenom, b.etDate, b.etTel) && ok
            if (b.spGroupe.selectedItemPosition == 0 || b.spGouvernorat.selectedItemPosition == 0) {
                ok = false
                b.tvMessageInsc.text = "Veuillez sélectionner le groupe sanguin et le gouvernorat."
            }
        } else {
            ok = requis(b.etNomEtab) && ok
        }
        if (!ok || !emailValide(b.etEmailInsc)) return

        val email = b.etEmailInsc.texte()
        val mdp = b.etPasswordInsc.text.toString()
        if (!Regles.motDePasseValide(mdp)) {
            b.etPasswordInsc.error = "6 caractères minimum"
            return
        }
        if (mdp != b.etConfirmInsc.text.toString()) {
            b.etConfirmInsc.error = "Les mots de passe ne correspondent pas"
            return
        }

        if (!estDonneur) {
            // Centre / établissement : démonstration (pas encore dans le backend)
            Session.ouvrirDemo(roleInscription, b.etNomEtab.texte(), email)
            majSession()
            allerEspace()
            retourEspace = "Compte de démonstration créé : il n'est pas enregistré côté serveur."
            rafraichirEspace()
            return
        }

        if (!Regles.telephoneValide(b.etTel.texte())) {
            b.etTel.error = "8 chiffres (ex. 22 123 456)"
            return
        }
        val naissance = b.etDate.iso()
        if (naissance.isNotEmpty() && !Regles.dateNonFuture(naissance)) {
            b.etDate.error = "La date ne peut pas être dans le futur."
            return
        }

        val demande = RegisterRequest(
            nom = b.etNomDonneur.texte(),
            email = email,
            motDePasse = mdp,
            prenom = b.etPrenom.texte(),
            groupeSanguin = Regles.groupeVersApi(b.spGroupe.selectedItem.toString()),
            ville = b.spGouvernorat.selectedItem.toString(),
            telephone = Regles.normaliserTelephone(b.etTel.texte())
        )

        inscriptionEnCours = true
        b.btnInscrire.isEnabled = false
        b.tvMessageInsc.text = "Création du compte..."
        lifecycleScope.launch {
            val res = appeler("Inscription impossible.", protege = false) { RetrofitClient.api.register(demande) }
            inscriptionEnCours = false
            b.btnInscrire.isEnabled = true
            when (res) {
                is Resultat.Ok -> {
                    // Aucune session n'est ouverte : le donneur se connecte ensuite (comme sur Angular).
                    listOf(b.etNomDonneur, b.etPrenom, b.etDate, b.etTel, b.etEmailInsc, b.etPasswordInsc, b.etConfirmInsc)
                        .forEach { it.text.clear(); it.tag = null }
                    b.spGroupe.setSelection(0)
                    b.spGouvernorat.setSelection(0)
                    afficher(b.viewConnexion)
                    b.tvMessageLogin.text = "Compte créé avec succès ! Vous pouvez maintenant vous connecter."
                }
                is Resultat.Echec -> b.tvMessageInsc.text = res.message
            }
        }
    }

    private fun envoyerContact() {
        val champs = arrayOf(b.etNomContact, b.etEmailContact, b.etSujet, b.etMessageContact)
        if (requis(*champs) && emailValide(b.etEmailContact)) {
            // SIMULÉ : le backend n'a pas d'endpoint de contact, rien n'est envoyé.
            b.tvMessageContact.text =
                "Merci ! Votre message a été pris en compte (démonstration : aucun email n'est envoyé pour le moment)."
            champs.forEach { it.text.clear() }
        }
    }

    // ----- Espace administrateur -----
    private fun allerAdmin() {
        if (!Session.estAdmin || !Session.estValide()) return
        ongletAdmin = "rendezvous"
        retourAdmin = ""
        chargerAdmin()
        rendreAdmin()
        afficher(b.viewAdmin.root)
    }

    /** Charge donneurs, centres et rendez-vous depuis l'API. */
    private fun chargerAdmin() {
        lifecycleScope.launch {
            when (val r = appeler("Impossible de charger les donneurs.") { RetrofitClient.api.donneurs() }) {
                is Resultat.Ok -> adminDonneurs = r.valeur
                is Resultat.Echec -> { if (r.sessionExpiree) return@launch sessionExpiree(); retourAdmin = r.message }
            }
            when (val r = appeler("Impossible de charger les centres.") { RetrofitClient.api.centres() }) {
                is Resultat.Ok -> {
                    val locaux = adminCentres.filter { it.id < 0 }
                    adminCentres = (r.valeur.map { CentreAdmin(it.id, it.nom, it.ville ?: "", true) } + locaux).toMutableList()
                }
                is Resultat.Echec -> { if (r.sessionExpiree) return@launch sessionExpiree(); retourAdmin = r.message }
            }
            when (val r = appeler("Impossible de charger les rendez-vous.") { RetrofitClient.api.rendezVous() }) {
                is Resultat.Ok -> adminRdv = r.valeur
                is Resultat.Echec -> { if (r.sessionExpiree) return@launch sessionExpiree(); retourAdmin = r.message }
            }
            if (b.viewAdmin.root.visibility == View.VISIBLE) rendreAdmin()
        }
    }

    private fun nomDonneur(id: Long) = adminDonneurs.find { it.id == id }?.nomComplet ?: "Donneur n°$id"
    private fun nomCentre(id: Long) = adminCentres.find { it.id == id }?.nom ?: "Centre n°$id"

    private fun dateRdv(iso: String): String {
        val d = Regles.date(iso) ?: return iso
        return SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.FRANCE)
            .format(Date.from(d.atZone(ZoneId.systemDefault()).toInstant()))
    }

    private fun rendreAdmin() {
        val v = b.viewAdmin
        v.tvEtiquette.text = "ADMINISTRATION"
        v.tvTitreEspace.text = "Tableau de bord administrateur"
        v.tvSousTitre.text = "Validez les comptes, traitez les rendez-vous, supervisez les campagnes et modérez les annonces."
        v.tvDemo.visibility = View.GONE
        dessinerOnglets(
            v, listOf(
                "rendezvous" to "Rendez-vous (${adminRdv.count { it.statut == Regles.PLANIFIE }})",
                "comptes" to "Comptes à valider (${comptesEnAttente.size})",
                "donneurs" to "Donneurs",
                "centres" to "Centres",
                "campagnes" to "Campagnes",
                "annonces" to "Annonces et urgences",
                "stats" to "Statistiques"
            ), ongletAdmin
        ) { ongletAdmin = it; retourAdmin = ""; rendreAdmin() }
        v.tvRetour.text = retourAdmin
        v.llForm.removeAllViews()
        v.llListe.removeAllViews()
        val l = v.llListe

        when (ongletAdmin) {
            "rendezvous" -> {
                if (adminRdv.isEmpty()) vide(l, "Aucun rendez-vous pour le moment.")
                adminRdv.forEach { r ->
                    val a: Pair<String, () -> Unit>? = when (r.statut) {
                        Regles.PLANIFIE -> "Accepter" to { transitionRdv(r.id, "Rendez-vous accepté.") { RetrofitClient.api.confirmerRdv(r.id) } }
                        Regles.CONFIRME -> "Marquer effectué" to { transitionRdv(r.id, "Don enregistré : dernier don mis à jour.") { RetrofitClient.api.effectuerRdv(r.id) } }
                        else -> null
                    }
                    val refuser: Pair<String, () -> Unit>? =
                        if (r.statut == Regles.PLANIFIE)
                            "Refuser" to { transitionRdv(r.id, "Rendez-vous refusé.") { RetrofitClient.api.refuserRdv(r.id) } }
                        else null
                    carteGestion(l, nomDonneur(r.donneurId), badge = Regles.libelleStatut(r.statut),
                        badgeCouleurs = couleursStatut(r.statut),
                        texte = nomCentre(r.centreId), meta = "🕒 ${dateRdv(r.dateRendezVous)}",
                        actionA = a, actionB = refuser)
                }
            }
            "comptes" -> {
                noteSimulation(l, "Fonction simulée : le serveur ne gère pas encore la validation des comptes centres/établissements. Les décisions ne sont pas enregistrées.")
                if (comptesEnAttente.isEmpty()) vide(l, "Aucun compte en attente de validation.")
                comptesEnAttente.toList().forEach { c ->
                    carteGestion(l, c.nom, badge = c.type, texte = c.email, meta = "📍 ${c.gouvernorat}",
                        actionA = "Valider" to {
                            comptesEnAttente.removeAll { it.id == c.id }
                            // id négatif = entrée qui n'existe pas côté serveur
                            adminCentres.add(CentreAdmin(-c.id.toLong(), c.nom, c.gouvernorat, true))
                            retourAdmin = "Compte « ${c.nom} » validé (simulation : non enregistré côté serveur)."
                            rendreAdmin()
                        },
                        actionB = "Refuser" to {
                            comptesEnAttente.removeAll { it.id == c.id }
                            retourAdmin = "Compte refusé (simulation : non enregistré côté serveur)."
                            rendreAdmin()
                        })
                }
            }
            "donneurs" -> {
                if (adminDonneurs.isEmpty()) vide(l, "Aucun donneur.")
                adminDonneurs.forEach { d ->
                    carteGestion(l, d.nomComplet, groupe = Regles.groupeDepuisApi(d.groupeSanguin) ?: "?",
                        badge = if (d.actif) "Actif" else "Suspendu",
                        badgeCouleurs = if (d.actif) R.color.statut_effectue_fond to R.color.statut_effectue_texte
                        else R.color.statut_annule_fond to R.color.statut_annule_texte,
                        meta = "📍 ${d.ville ?: "—"}",
                        actionA = (if (d.actif) "Suspendre" else "Réactiver") to { basculerDonneur(d) },
                        actionB = "Supprimer" to {
                            confirmer("Supprimer le donneur", "${d.nomComplet} sera définitivement supprimé. Cette action est irréversible.", "Supprimer") {
                                supprimerDonneur(d)
                            }
                        })
                }
            }
            "centres" -> {
                if (adminCentres.isEmpty()) vide(l, "Aucun centre.")
                adminCentres.toList().forEach { c ->
                    carteGestion(l, c.nom, badge = "Centre de collecte · ${if (c.actif) "Actif" else "Suspendu"}",
                        badgeUrgent = !c.actif, meta = "📍 ${c.ville}",
                        actionA = (if (c.actif) "Suspendre" else "Réactiver") to {
                            // Le backend n'a pas de statut pour un centre : reste local
                            c.actif = !c.actif
                            retourAdmin = "Statut modifié localement : il n'est pas enregistré côté serveur."
                            rendreAdmin()
                        },
                        actionB = "Supprimer" to {
                            confirmer("Supprimer le centre", "${c.nom} sera définitivement supprimé. Cette action est irréversible.", "Supprimer") {
                                supprimerCentre(c)
                            }
                        })
                }
            }
            "campagnes" -> {
                noteSimulation(l, "Données simulées : le serveur n'expose pas encore d'endpoint pour les campagnes.")
                if (campagnes.isEmpty()) vide(l, "Aucune campagne à superviser.")
                campagnes.toList().forEach { c ->
                    carteGestion(l, c.titre, badge = c.date, texte = c.organisateur,
                        meta = "📍 ${c.lieu}\n🕒 ${c.horaire}",
                        actionA = "Annuler la campagne" to {
                            confirmer("Annuler la campagne", "La campagne « ${c.titre} » sera annulée.", "Annuler la campagne") {
                                campagnes.removeAll { it.id == c.id }
                                retourAdmin = "Campagne annulée."
                                rendreAdmin()
                            }
                        })
                }
            }
            "annonces" -> {
                noteSimulation(l, "Données simulées : le serveur n'expose pas encore d'endpoint pour les annonces.")
                if (annonces.isEmpty()) vide(l, "Aucune annonce à modérer.")
                annonces.toList().forEach { a ->
                    carteGestion(l, a.etablissement, groupe = a.groupe,
                        badge = if (a.urgent) "URGENT" else null, badgeUrgent = true, texte = a.description,
                        actionA = (if (a.urgent) "Retirer l'urgence" else "Marquer urgent") to {
                            a.urgent = !a.urgent
                            rendreAdmin()
                        },
                        actionB = "Retirer" to {
                            confirmer("Retirer l'annonce", "L'annonce de ${a.etablissement} sera retirée.", "Retirer") {
                                annonces.removeAll { it.id == a.id }
                                retourAdmin = "Annonce retirée."
                                rendreAdmin()
                            }
                        })
                }
            }
            else -> {
                remplir(l, listOf(
                    "${adminDonneurs.size}" to "Donneurs inscrits",
                    "${adminCentres.size}" to "Centres et établissements",
                    "${comptesEnAttente.size}" to "Comptes en attente",
                    "${campagnes.size}" to "Campagnes",
                    "${annonces.size}" to "Annonces publiées",
                    "${annonces.count { it.urgent }}" to "Annonces urgentes"
                ), gros = true)
                l.addView(texteCentre("Donneurs par groupe : " + groupesSanguins.joinToString("   ·   ") { g ->
                    "$g : ${adminDonneurs.count { Regles.groupeDepuisApi(it.groupeSanguin) == g }}"
                }))
                l.addView(bouton("Générer un rapport") { genererRapport() })
            }
        }
    }

    /** Applique une transition de rendez-vous via l'API (confirmer / refuser / effectuer). */
    private fun transitionRdv(id: Long, succes: String, appel: suspend () -> retrofit2.Response<RdvApi>) {
        lifecycleScope.launch {
            when (val r = appeler("Action impossible.") { appel() }) {
                is Resultat.Ok -> {
                    adminRdv = adminRdv.map { if (it.id == id) r.valeur else it }
                    retourAdmin = succes
                }
                is Resultat.Echec -> { if (r.sessionExpiree) return@launch sessionExpiree(); retourAdmin = r.message }
            }
            rendreAdmin()
        }
    }

    /** Suspendre / réactiver un donneur : PUT /donneurs/{id} (champ disponible). */
    private fun basculerDonneur(d: DonneurApi) {
        val corps = DonneurMajRequest(
            nom = d.nom, prenom = d.prenom, groupeSanguin = d.groupeSanguin, ville = d.ville ?: "",
            telephone = Regles.normaliserTelephone(d.telephone ?: ""), dateDernierDon = d.dateDernierDon,
            disponible = !d.actif
        )
        lifecycleScope.launch {
            when (val r = appeler("Modification impossible.") { RetrofitClient.api.majDonneur(d.id, corps) }) {
                is Resultat.Ok -> adminDonneurs = adminDonneurs.map { if (it.id == d.id) r.valeur else it }
                is Resultat.Echec -> { if (r.sessionExpiree) return@launch sessionExpiree(); retourAdmin = r.message }
            }
            rendreAdmin()
        }
    }

    private fun supprimerDonneur(d: DonneurApi) {
        lifecycleScope.launch {
            when (val r = appeler("Suppression impossible.") { RetrofitClient.api.supprimerDonneur(d.id) }) {
                is Resultat.Ok -> {
                    adminDonneurs = adminDonneurs.filter { it.id != d.id }
                    retourAdmin = "Donneur supprimé."
                }
                is Resultat.Echec -> { if (r.sessionExpiree) return@launch sessionExpiree(); retourAdmin = r.message }
            }
            rendreAdmin()
        }
    }

    private fun supprimerCentre(c: CentreAdmin) {
        if (c.id < 0) {
            // Créé localement à partir d'un compte validé (simulation) : rien n'existe côté serveur
            adminCentres.removeAll { it.id == c.id }
            retourAdmin = "Centre supprimé."
            rendreAdmin()
            return
        }
        lifecycleScope.launch {
            when (val r = appeler("Suppression impossible.") { RetrofitClient.api.supprimerCentre(c.id) }) {
                is Resultat.Ok -> {
                    adminCentres.removeAll { it.id == c.id }
                    retourAdmin = "Centre supprimé."
                }
                is Resultat.Echec -> { if (r.sessionExpiree) return@launch sessionExpiree(); retourAdmin = r.message }
            }
            rendreAdmin()
        }
    }

    private fun genererRapport() {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
        val texte = buildString {
            appendLine("Rapport Tunisia Smart Blood - $date")
            appendLine()
            appendLine("Donneurs inscrits : ${adminDonneurs.size}")
            appendLine("Centres et établissements : ${adminCentres.size}")
            appendLine("Comptes en attente de validation : ${comptesEnAttente.size}")
            appendLine("Campagnes : ${campagnes.size}")
            appendLine("Annonces publiées : ${annonces.size}")
            appendLine("Annonces urgentes : ${annonces.count { it.urgent }}")
            appendLine()
            appendLine("Donneurs par groupe sanguin :")
            groupesSanguins.forEach { g ->
                appendLine(" - $g : ${adminDonneurs.count { Regles.groupeDepuisApi(it.groupeSanguin) == g }}")
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Rapport Tunisia Smart Blood")
            putExtra(Intent.EXTRA_TEXT, texte)
        }
        startActivity(Intent.createChooser(intent, "Enregistrer ou partager le rapport"))
        retourAdmin = "Rapport généré."
        rendreAdmin()
    }

    // ----- Espaces connectés -----
    private fun allerEspace() {
        if (!Session.estValide()) return
        val cpt = Session.compte() ?: return
        retourEspace = ""
        when (cpt.role) {
            "donneur" -> {
                msgDonneur = ""
                erreurDonneur = ""
                afficher(b.viewEspaceDonneur.root)
                chargerDonneur()
            }
            "centre" -> {
                ongletCentre = "rendezvous"
                rendreCentre()
                afficher(b.viewEspaceCentre.root)
            }
            else -> {
                ongletEtab = "besoins"
                rendreEtab()
                afficher(b.viewEspaceEtab.root)
            }
        }
    }

    private fun rafraichirEspace() {
        when (Session.role) {
            "centre" -> rendreCentre()
            "donneur" -> rendreDonneur()
            else -> rendreEtab()
        }
    }

    private fun noteDemo(v: ViewEspaceBinding, texte: String) {
        v.tvDemo.text = texte
        v.tvDemo.visibility = View.VISIBLE
    }

    private fun noteSimulation(parent: LinearLayout, texte: String) {
        parent.addView(TextView(this, null, 0, R.style.Gris).apply {
            text = texte
            textSize = 13f
            gravity = Gravity.CENTER
            layoutParams = lp(bottom = 12)
        })
    }

    // ----- Espace centre de collecte (SIMULÉ) -----
    private fun rendreCentre() {
        val v = b.viewEspaceCentre
        val cpt = Session.compte() ?: return
        v.tvEtiquette.text = "ESPACE CENTRE DE COLLECTE"
        v.tvTitreEspace.text = cpt.nom
        v.tvSousTitre.text = "Gérez vos campagnes, vos créneaux et vos rendez-vous."
        noteDemo(v, "Mode démonstration : les centres n'ont pas encore de compte côté serveur. Les rendez-vous, campagnes, créneaux et urgences ci-dessous sont simulés et ne sont pas enregistrés.")
        dessinerOnglets(
            v, listOf(
                "rendezvous" to "Rendez-vous (${rdvEnAttente()} en attente)",
                "campagnes" to "Campagnes",
                "creneaux" to "Créneaux",
                "urgences" to "Urgences",
                "stats" to "Statistiques",
                "profil" to "Mon profil"
            ), ongletCentre
        ) { ongletCentre = it; retourEspace = ""; rendreCentre() }
        v.tvRetour.text = retourEspace
        v.llForm.removeAllViews()
        v.llListe.removeAllViews()
        val f = v.llForm
        val l = v.llListe

        when (ongletCentre) {
            "rendezvous" -> {
                if (rendezVous.isEmpty()) vide(l, "Aucun rendez-vous pour le moment.")
                rendezVous.toList().forEach { x ->
                    val a: Pair<String, () -> Unit>? = when (x.statut) {
                        "En attente" -> "Confirmer" to { changerStatut(x.id, "Confirmé") }
                        "Confirmé" -> "Enregistrer la participation" to { changerStatut(x.id, "Participation enregistrée") }
                        else -> null
                    }
                    val annuler: Pair<String, () -> Unit>? =
                        if (x.statut == "En attente" || x.statut == "Confirmé")
                            "Annuler" to { changerStatut(x.id, "Annulé") } else null
                    carteGestion(l, x.donneur, groupe = x.groupe, badge = x.statut, badgeUrgent = x.statut == "Annulé",
                        meta = "🕒 ${x.creneau}", actionA = a, actionB = annuler)
                }
            }
            "campagnes" -> {
                titreForm(f, "Publier une campagne")
                val titre = champTexte(f, "Titre", "Ex. : Collecte de printemps")
                val date = champDate(f, "Date")
                val gouv = listeDeroulante(f, "Gouvernorat", "Sélectionnez", gouvernorats)
                val debut = champHeure(f, "Heure de début")
                val fin = champHeure(f, "Heure de fin")
                val lieu = champTexte(f, "Lieu", "Adresse de la collecte")
                label(f, "Groupes sanguins recherchés (aucun = tous)")
                val cases = groupesSanguins.map { g -> CheckBox(this).apply { text = g } }
                cases.chunked(4).forEach { ligne ->
                    f.addView(LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        ligne.forEach { addView(it, LinearLayout.LayoutParams(0, WRAP, 1f)) }
                    })
                }
                f.addView(bouton("Publier la campagne") {
                    if (titre.texte().isEmpty() || date.iso().isEmpty() || gouv.valeur().isEmpty() ||
                        debut.iso().isEmpty() || fin.iso().isEmpty() || lieu.texte().isEmpty()
                    ) {
                        v.tvRetour.text = "Veuillez remplir tous les champs."
                        return@bouton
                    }
                    if (!Regles.dateNonPassee(date.iso())) {
                        v.tvRetour.text = "La date ne peut pas être dans le passé."
                        return@bouton
                    }
                    if (!Regles.finApresDebut(debut.iso(), fin.iso())) {
                        v.tvRetour.text = "L'heure de fin doit être après l'heure de début."
                        return@bouton
                    }
                    val groupes = cases.filter { it.isChecked }.joinToString(", ") { it.text.toString() }
                    campagnes.add(Campagne(
                        nouvelId(), titre.texte(), cpt.nom, lieu.texte(), gouv.valeur(),
                        dateFr(date.iso()), heure(debut.iso()) + " – " + heure(fin.iso()),
                        groupes.ifEmpty { "Tous les groupes" }
                    ))
                    retourEspace = "Campagne publiée."
                    rendreCentre()
                })
                titreListe(l, "Mes campagnes")
                val mes = campagnesCentre()
                if (mes.isEmpty()) vide(l, "Vous n'avez pas encore publié de campagne.")
                mes.forEach { c ->
                    carteGestion(l, c.titre, badge = c.date,
                        meta = "📍 ${c.lieu}\n🕒 ${c.horaire}\n🩸 ${c.groupes ?: "Tous les groupes"}",
                        actionA = "Annuler la campagne" to {
                            confirmer("Annuler la campagne", "Cette campagne ne sera plus visible. Continuer ?", "Annuler la campagne") {
                                campagnes.removeAll { it.id == c.id }
                                retourEspace = "Campagne annulée."
                                rendreCentre()
                            }
                        })
                }
            }
            "creneaux" -> {
                titreForm(f, "Ajouter un créneau")
                val date = champDate(f, "Date")
                val places = champTexte(f, "Places disponibles", "20", InputType.TYPE_CLASS_NUMBER)
                val debut = champHeure(f, "Heure de début")
                val fin = champHeure(f, "Heure de fin")
                f.addView(bouton("Ajouter le créneau") {
                    val n = places.texte().toIntOrNull()
                    if (date.iso().isEmpty() || debut.iso().isEmpty() || fin.iso().isEmpty() || n == null || n < 1) {
                        v.tvRetour.text = "Veuillez remplir tous les champs."
                        return@bouton
                    }
                    if (!Regles.dateNonPassee(date.iso())) {
                        v.tvRetour.text = "La date ne peut pas être dans le passé."
                        return@bouton
                    }
                    if (!Regles.finApresDebut(debut.iso(), fin.iso())) {
                        v.tvRetour.text = "L'heure de fin doit être après l'heure de début."
                        return@bouton
                    }
                    creneaux.add(Creneau(nouvelId(), dateFr(date.iso()),
                        heure(debut.iso()) + " – " + heure(fin.iso()), n, 0))
                    retourEspace = "Créneau ajouté."
                    rendreCentre()
                })
                titreListe(l, "Créneaux ouverts")
                if (creneaux.isEmpty()) vide(l, "Aucun créneau ouvert.")
                creneaux.toList().forEach { c ->
                    carteGestion(l, c.horaire, badge = c.date, texte = "${c.reservees} / ${c.places} places réservées",
                        actionA = "Supprimer" to {
                            creneaux.removeAll { it.id == c.id }
                            retourEspace = "Créneau supprimé."
                            rendreCentre()
                        })
                }
            }
            "urgences" -> {
                formBesoin(v, true)
                listeBesoins(v, false, "Mes messages publiés", "Aucun message publié.")
            }
            "stats" -> {
                val participations = rendezVous.count { it.statut == "Participation enregistrée" }
                val valides = rendezVous.count { it.statut != "Annulé" && it.statut != "Refusé" }
                val taux = if (valides > 0) Math.round(participations * 100f / valides) else 0
                remplir(l, listOf(
                    "${campagnesCentre().size}" to "Campagnes publiées",
                    "${rendezVous.size}" to "Rendez-vous reçus",
                    "${rdvEnAttente()}" to "En attente",
                    "${rendezVous.count { it.statut == "Confirmé" }}" to "Confirmés",
                    "$participations" to "Participations enregistrées",
                    "$taux %" to "Taux de participation"
                ), gros = true)
            }
            else -> formProfil(v)
        }
    }

    private fun changerStatut(id: Int, statut: String) {
        rendezVous.find { it.id == id }?.statut = statut
        retourEspace = when (statut) {
            "Confirmé" -> "Rendez-vous confirmé."
            "Annulé" -> "Rendez-vous annulé."
            else -> "Participation enregistrée."
        }
        rendreCentre()
    }

    // ----- Espace établissement de santé (SIMULÉ) -----
    private fun rendreEtab() {
        val v = b.viewEspaceEtab
        val cpt = Session.compte() ?: return
        v.tvEtiquette.text = "ESPACE ÉTABLISSEMENT DE SANTÉ"
        v.tvTitreEspace.text = cpt.nom
        v.tvSousTitre.text = "Signalez vos besoins en sang avec le groupe recherché et le lieu concerné."
        noteDemo(v, "Mode démonstration : les établissements n'ont pas encore de compte côté serveur. Les besoins publiés et les statistiques ci-dessous sont simulés et ne sont pas enregistrés.")
        dessinerOnglets(
            v, listOf(
                "besoins" to "Besoins en sang",
                "stats" to "Statistiques",
                "profil" to "Mon profil"
            ), ongletEtab
        ) { ongletEtab = it; retourEspace = ""; rendreEtab() }
        v.tvRetour.text = retourEspace
        v.llForm.removeAllViews()
        v.llListe.removeAllViews()

        when (ongletEtab) {
            "besoins" -> {
                formBesoin(v, false)
                listeBesoins(v, true, "Mes besoins publiés", "Vous n'avez pas encore publié de besoin.")
            }
            "stats" -> {
                val a = mesAnnonces()
                remplir(v.llListe, listOf(
                    "${a.size}" to "Besoins publiés",
                    "${a.count { it.urgent }}" to "Besoins urgents",
                    "${donneursDemo.count { it.statut == "Actif" }}" to "Donneurs actifs"
                ), gros = true)
                v.llListe.addView(texteCentre("Donneurs actifs par groupe : " + groupesSanguins.joinToString("   ·   ") { g ->
                    "$g : ${donneursDemo.count { it.statut == "Actif" && it.groupe == g }}"
                }))
            }
            else -> formProfil(v)
        }
    }

    // ----- Parties communes aux deux espaces -----
    private fun formBesoin(v: ViewEspaceBinding, centre: Boolean) {
        val f = v.llForm
        titreForm(f, if (centre) "Publier un message d'urgence" else "Publier un besoin en sang")
        val groupe = listeDeroulante(f, if (centre) "Groupe sanguin" else "Groupe sanguin recherché", "Sélectionnez", groupesSanguins)
        val gouv = listeDeroulante(f, "Gouvernorat", "Sélectionnez", gouvernorats)
        val desc = champTexte(
            f, if (centre) "Message" else "Description",
            if (centre) "Décrivez le besoin en poches de sang..." else "Service concerné, contexte, délai...",
            lignes = 4
        )
        val urgent = CheckBox(this).apply { text = "Marquer comme urgent" }
        if (!centre) f.addView(urgent)
        f.addView(bouton(if (centre) "Publier l'urgence" else "Publier le besoin") {
            if (groupe.valeur().isEmpty() || gouv.valeur().isEmpty() || desc.texte().isEmpty()) {
                v.tvRetour.text = "Veuillez remplir tous les champs."
                return@bouton
            }
            annonces.add(0, Annonce(
                nouvelId(), groupe.valeur(), centre || urgent.isChecked,
                Session.nom, gouv.valeur(), desc.texte(), "À l'instant"
            ))
            retourEspace = "Besoin publié."
            rafraichirEspace()
        })
    }

    private fun listeBesoins(v: ViewEspaceBinding, avecUrgence: Boolean, titre: String, msgVide: String) {
        val l = v.llListe
        titreListe(l, titre)
        val data = mesAnnonces()
        if (data.isEmpty()) vide(l, msgVide)
        data.forEach { a ->
            val retirer: Pair<String, () -> Unit> = "Retirer" to {
                val retirerAnnonce = {
                    annonces.removeAll { it.id == a.id }
                    retourEspace = "Annonce retirée."
                    rafraichirEspace()
                }
                if (avecUrgence) {
                    confirmer("Retirer le besoin", "Ce besoin ne sera plus visible par les donneurs. Continuer ?", "Retirer") { retirerAnnonce() }
                } else retirerAnnonce()
            }
            if (avecUrgence) {
                carteGestion(l, a.gouvernorat, groupe = a.groupe, badge = if (a.urgent) "URGENT" else null,
                    badgeUrgent = true, texte = a.description, meta = "🕒 ${a.publie}",
                    actionA = (if (a.urgent) "Retirer l'urgence" else "Marquer urgent") to {
                        a.urgent = !a.urgent
                        rafraichirEspace()
                    },
                    actionB = retirer)
            } else {
                carteGestion(l, a.gouvernorat, groupe = a.groupe, badge = if (a.urgent) "URGENT" else null,
                    badgeUrgent = true, texte = a.description, meta = "🕒 ${a.publie}", actionA = retirer)
            }
        }
    }

    private fun formProfil(v: ViewEspaceBinding) {
        val cpt = Session.compte() ?: return
        val f = v.llForm
        titreForm(f, "Mon profil")
        champTexte(f, if (cpt.role == "centre") "Nom du centre" else "Nom de l'établissement", valeur = cpt.nom, actif = false)
        val email = champTexte(f, "Email", type = TYPE_EMAIL, valeur = cpt.email)
        val gouv = listeDeroulante(f, "Gouvernorat", "Sélectionnez", gouvernorats, cpt.gouvernorat)
        val tel = champTexte(f, "Téléphone", "+216 XX XXX XXX", InputType.TYPE_CLASS_PHONE, valeur = cpt.telephone ?: "")
        val adr = champTexte(
            f, "Adresse",
            if (cpt.role == "centre") "Adresse du centre" else "Adresse de l'établissement",
            valeur = cpt.adresse ?: ""
        )
        f.addView(bouton("Enregistrer") {
            if (!Regles.emailValide(email.texte())) {
                v.tvRetour.text = "Veuillez saisir un email valide."
                return@bouton
            }
            if (tel.texte().isNotEmpty() && !Regles.telephoneValide(tel.texte())) {
                v.tvRetour.text = "Le téléphone doit contenir 8 chiffres (ex. 22 123 456)."
                return@bouton
            }
            Session.majProfilDemo(email.texte(), gouv.valeur(), tel.texte(), adr.texte())
            retourEspace = "Profil mis à jour."
            rafraichirEspace()
        })
    }

    // ----- Utilitaires d'interface -----
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun lp(top: Int = 0, bottom: Int = 0) =
        LinearLayout.LayoutParams(MATCH, WRAP).apply { setMargins(0, dp(top), 0, dp(bottom)) }

    private fun EditText.texte() = text.toString().trim()
    private fun EditText.iso() = (tag as? String) ?: ""
    private fun Spinner.valeur() = if (selectedItemPosition <= 0) "" else selectedItem.toString()

    private fun dateFr(iso: String): String = try {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)!!
        SimpleDateFormat("d MMMM yyyy", Locale.FRANCE).format(d)
    } catch (e: Exception) { iso }

    private fun heure(h: String) = h.replace(":", "h")

    /** Boîte de confirmation avant une action destructive (équivalent de ConfirmationService côté Angular). */
    private fun confirmer(titre: String, message: String, libelleOk: String, action: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(titre)
            .setMessage(message)
            .setNegativeButton("Annuler", null)
            .setPositiveButton(libelleOk) { _, _ -> action() }
            .show()
    }

    private fun dessinerOnglets(
        v: ViewEspaceBinding, items: List<Pair<String, String>>, actif: String, choisir: (String) -> Unit
    ) {
        v.llOnglets.removeAllViews()
        items.forEach { (cle, lib) ->
            v.llOnglets.addView(TextView(this).apply {
                text = lib
                textSize = 13.5f
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(16), dp(10), dp(16), dp(10))
                setTextColor(getColor(if (cle == actif) R.color.white else R.color.texte))
                background = GradientDrawable().apply {
                    cornerRadius = dp(50).toFloat()
                    setColor(getColor(if (cle == actif) R.color.bordeaux else R.color.white))
                    if (cle != actif) setStroke(dp(1), getColor(R.color.bordure))
                }
                layoutParams = LinearLayout.LayoutParams(WRAP, WRAP).apply { marginEnd = dp(8) }
                setOnClickListener { choisir(cle) }
            })
        }
    }

    private fun carteGestion(
        parent: LinearLayout, titre: String, groupe: String? = null, badge: String? = null,
        badgeUrgent: Boolean = false, texte: String? = null, meta: String? = null,
        badgeCouleurs: Pair<Int, Int>? = null,
        actionA: Pair<String, () -> Unit>? = null, actionB: Pair<String, () -> Unit>? = null
    ) {
        val c = ItemGestionBinding.inflate(layoutInflater, parent, false)
        fun TextView.maj(t: String?) {
            visibility = if (t == null) View.GONE else View.VISIBLE
            if (t != null) text = t
        }
        c.tvTitre.text = titre
        c.tvGroupe.maj(groupe)
        c.tvBadge.maj(badge)
        c.tvBadge.setBackgroundResource(if (badgeUrgent) R.drawable.bg_urgent else R.drawable.bg_groupe)
        c.tvBadge.setTextColor(getColor(if (badgeUrgent) R.color.white else R.color.rouge))
        if (badgeCouleurs != null) {
            // couleur propre au statut (en attente / confirmé / effectué / refusé…), comme les badges d'Angular
            c.tvBadge.backgroundTintList = ColorStateList.valueOf(getColor(badgeCouleurs.first))
            c.tvBadge.setTextColor(getColor(badgeCouleurs.second))
        } else {
            c.tvBadge.backgroundTintList = null
        }
        c.rowHaut.visibility = if (groupe != null || badge != null) View.VISIBLE else View.GONE
        c.tvTexte.maj(texte)
        c.tvMeta.maj(meta)
        c.llActions.visibility = if (actionA != null || actionB != null) View.VISIBLE else View.GONE
        if (actionA != null) {
            c.btnA.visibility = View.VISIBLE
            c.btnA.text = actionA.first
            c.btnA.setOnClickListener { actionA.second() }
        } else c.btnA.visibility = View.GONE
        if (actionB != null) {
            c.btnB.visibility = View.VISIBLE
            c.btnB.text = actionB.first
            c.btnB.setOnClickListener { actionB.second() }
        } else c.btnB.visibility = View.GONE
        parent.addView(c.root)
    }

    private fun vide(parent: LinearLayout, t: String) = parent.addView(texteCentre(t))

    private fun texteCentre(t: String) = TextView(this, null, 0, R.style.Gris).apply {
        text = t
        gravity = Gravity.CENTER
        layoutParams = lp(top = 8, bottom = 16)
    }

    private fun titreListe(parent: LinearLayout, t: String) {
        parent.addView(TextView(this, null, 0, R.style.Titre).apply {
            text = t
            textSize = 22f
            gravity = Gravity.CENTER
            layoutParams = lp(top = 8, bottom = 16)
        })
    }

    private fun titreForm(parent: LinearLayout, t: String) {
        parent.addView(TextView(this, null, 0, R.style.Titre).apply {
            text = t
            textSize = 21f
            layoutParams = lp(bottom = 16)
        })
    }

    private fun label(parent: LinearLayout, t: String) {
        parent.addView(TextView(this, null, 0, R.style.Label).apply {
            text = t
            layoutParams = lp(bottom = 8)
        })
    }

    private fun champTexte(
        parent: LinearLayout, libelle: String, indice: String = "",
        type: Int = InputType.TYPE_CLASS_TEXT, lignes: Int = 1, valeur: String = "", actif: Boolean = true
    ): EditText {
        label(parent, libelle)
        val e = EditText(this, null, 0, R.style.Champ).apply {
            hint = indice
            inputType = if (lignes > 1) type or InputType.TYPE_TEXT_FLAG_MULTI_LINE else type
            if (lignes > 1) { minLines = lignes; gravity = Gravity.TOP }
            setText(valeur)
            isEnabled = actif
            layoutParams = lp(bottom = 20)
        }
        parent.addView(e)
        return e
    }

    private fun champDate(parent: LinearLayout, libelle: String): EditText {
        val e = champTexte(parent, libelle, "Choisir une date")
        e.isFocusable = false
        e.isCursorVisible = false
        e.setOnClickListener {
            val c = Calendar.getInstance()
            val dialogue = DatePickerDialog(this, { _, y, m, d ->
                e.tag = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                e.setText(String.format(Locale.US, "%02d/%02d/%04d", d, m + 1, y))
            }, c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DAY_OF_MONTH])
            dialogue.datePicker.minDate = c.timeInMillis - 1000
            dialogue.show()
        }
        return e
    }

    private fun champHeure(parent: LinearLayout, libelle: String): EditText {
        val e = champTexte(parent, libelle, "Choisir l'heure")
        e.isFocusable = false
        e.isCursorVisible = false
        e.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                e.tag = String.format(Locale.US, "%02d:%02d", h, m)
                e.setText(heure(String.format(Locale.US, "%02d:%02d", h, m)))
            }, 8, 0, true).show()
        }
        return e
    }

    private fun listeDeroulante(
        parent: LinearLayout, libelle: String, indication: String, items: List<String>, selection: String? = null
    ): Spinner {
        label(parent, libelle)
        val sp = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf(indication) + items
            )
            minimumHeight = dp(48)
            if (!selection.isNullOrEmpty()) setSelection(items.indexOf(selection) + 1)
        }
        parent.addView(FrameLayout(this).apply {
            setBackgroundResource(R.drawable.bg_champ)
            layoutParams = lp(bottom = 20)
            addView(sp)
        })
        return sp
    }

    private fun bouton(texte: String, action: () -> Unit) = MaterialButton(this).apply {
        text = texte
        isAllCaps = false
        cornerRadius = dp(50)
        backgroundTintList = ColorStateList.valueOf(getColor(R.color.rouge))
        setTextColor(getColor(R.color.white))
        layoutParams = lp(top = 8, bottom = 8)
        setOnClickListener { action() }
    }

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
        val ok = Regles.emailValide(champ.text.toString())
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

    // ----- Espace donneur (API réelle : /donneurs/moi, /centres, /rendez-vous) -----
    private fun fmt(motif: String, ms: Long) =
        SimpleDateFormat(motif, Locale.FRANCE).format(Date(ms))

    private fun msDe(iso: String): Long =
        (Regles.date(iso) ?: LocalDateTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun couleursStatut(s: String) = when (s) {
        Regles.PLANIFIE -> R.color.statut_attente_fond to R.color.statut_attente_texte
        Regles.CONFIRME -> R.color.statut_planifie_fond to R.color.statut_planifie_texte
        Regles.EFFECTUE -> R.color.statut_effectue_fond to R.color.statut_effectue_texte
        else -> R.color.statut_annule_fond to R.color.statut_annule_texte
    }

    private fun configurerDonneur() {
        val v = b.viewEspaceDonneur
        v.btnSortir.setOnClickListener { deconnexion() }
        v.btnReserver.setOnClickListener { reserverRdv() }
        v.btnReessayer.setOnClickListener { chargerDonneur() }
        v.etDateRdv.setOnClickListener { choisirDateHeure(v.etDateRdv) }
    }

    /** Affiche soit l'état (chargement / erreur), soit le tableau de bord. */
    private fun montrerDonneur(contenu: Boolean, etat: String = "", reessayer: Boolean = false) {
        val v = b.viewEspaceDonneur
        val racine = v.llRacineDonneur
        for (i in 0 until racine.childCount) {
            val enfant = racine.getChildAt(i)
            enfant.visibility = if (enfant == v.llEtatDonneur) {
                if (contenu) View.GONE else View.VISIBLE
            } else {
                if (contenu) View.VISIBLE else View.GONE
            }
        }
        v.tvEtatDonneur.text = etat
        v.btnReessayer.visibility = if (reessayer) View.VISIBLE else View.GONE
    }

    private fun chargerDonneur() {
        montrerDonneur(false, "Chargement de votre espace…")
        lifecycleScope.launch {
            val moi = appeler("Impossible de charger votre espace donneur.") { RetrofitClient.api.donneurMoi() }
            if (moi is Resultat.Echec) {
                if (moi.sessionExpiree) return@launch sessionExpiree()
                // Jamais de fausses données : on affiche la vraie erreur
                montrerDonneur(
                    false,
                    if (moi.code == 404) "Aucune fiche donneur liée à ce compte." else moi.message,
                    reessayer = true
                )
                return@launch
            }
            val d = (moi as Resultat.Ok).valeur
            donneurMoi = d

            when (val c = appeler("Impossible de charger les centres.") { RetrofitClient.api.centres() }) {
                is Resultat.Ok -> centresApi = c.valeur
                is Resultat.Echec -> { if (c.sessionExpiree) return@launch sessionExpiree(); erreurDonneur = c.message; centresApi = listOf() }
            }
            when (val r = appeler("Impossible de charger les rendez-vous.") { RetrofitClient.api.rendezVous(d.id) }) {
                is Resultat.Ok -> rdvDonneur = r.valeur
                is Resultat.Echec -> { if (r.sessionExpiree) return@launch sessionExpiree(); erreurDonneur = r.message; rdvDonneur = listOf() }
            }
            rendreDonneur()
        }
    }

    // Android n'a pas de "datetime-local" : on enchaîne deux dialogues
    private fun choisirDateHeure(champ: EditText) {
        val now = Calendar.getInstance()
        val dialogue = DatePickerDialog(this, { _, y, m, d ->
            // Un jour qui a déjà un rendez-vous en attente ou confirmé n'est pas réservable
            val jour = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
            if (jour in Regles.joursBloques(rdvDonneur)) {
                rdvChoisi = null
                champ.text.clear()
                erreurDonneur = "Vous avez déjà un rendez-vous ce jour-là : choisissez une autre date."
                msgDonneur = ""
                rendreDonneur()
                return@DatePickerDialog
            }
            TimePickerDialog(this, { _, h, min ->
                val c = Calendar.getInstance().apply {
                    set(y, m, d, h, min, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                rdvChoisi = c
                erreurDonneur = ""
                champ.setText(fmt("dd/MM/yyyy HH:mm", c.timeInMillis))
                rendreDonneur()
            }, 9, 30, true).show()
        }, now[Calendar.YEAR], now[Calendar.MONTH], now[Calendar.DAY_OF_MONTH])
        dialogue.datePicker.minDate = System.currentTimeMillis() - 1000 // équivalent de [min]="minDate"
        dialogue.show()
    }

    private fun rendreDonneur() {
        val v = b.viewEspaceDonneur
        val d = donneurMoi ?: return
        montrerDonneur(true)
        val maintenant = LocalDateTime.now()

        // En-tête
        v.tvInitiales.text = (d.prenom.take(1) + d.nom.take(1)).uppercase()
        v.tvNomDonneur.text = d.nomComplet
        v.tvContact.text = listOfNotNull(d.ville, d.telephone).filter { it.isNotBlank() }.joinToString(" · ")
        val groupe = Regles.groupeDepuisApi(d.groupeSanguin)
        v.llGroupe.visibility = if (groupe.isNullOrEmpty()) View.GONE else View.VISIBLE
        v.tvGroupeDonneur.text = groupe ?: ""

        // Statistiques
        val stats = Regles.statsDonneur(d, rdvDonneur)
        v.tvNbEffectues.text = "${stats.effectues}"
        v.tvNbPlanifies.text = "${stats.planifies}"
        v.tvDernierDon.text = stats.dernierDon?.let { fmt("dd/MM/yyyy", msDe(it)) } ?: "—"

        // Prochain rendez-vous : le plus proche parmi les actifs à venir
        val prochain = Regles.prochainRdv(rdvDonneur, maintenant)
        v.llProchain.visibility = if (prochain != null) View.VISIBLE else View.GONE
        v.tvVideProchain.visibility = if (prochain == null) View.VISIBLE else View.GONE
        if (prochain != null) {
            val ms = msDe(prochain.dateRendezVous)
            v.tvProchainJour.text = fmt("dd", ms)
            v.tvProchainMois.text = fmt("MM/yyyy", ms)
            v.tvProchainCentre.text = nomCentreDonneur(prochain.centreId)
            v.tvProchainHeure.text = "à " + fmt("HH:mm", ms)
            v.btnProchainAnnuler.setOnClickListener { annulerRdv(prochain.id) }
        }

        // Historique (du plus récent au plus ancien)
        v.llHistorique.removeAllViews()
        val tous = Regles.historique(rdvDonneur)
        v.tvVideHistorique.visibility = if (tous.isEmpty()) View.VISIBLE else View.GONE
        tous.forEach { r ->
            val c = ItemRdvBinding.inflate(layoutInflater, v.llHistorique, false)
            val (fond, texte) = couleursStatut(r.statut)
            c.tvCentre.text = nomCentreDonneur(r.centreId)
            c.tvDate.text = fmt("dd/MM/yyyy HH:mm", msDe(r.dateRendezVous))
            c.tvStatut.text = Regles.libelleStatut(r.statut)
            c.tvStatut.backgroundTintList = ColorStateList.valueOf(getColor(fond))
            c.tvStatut.setTextColor(getColor(texte))
            c.btnAnnuler.visibility = if (Regles.peutAnnuler(r.statut)) View.VISIBLE else View.GONE
            c.btnAnnuler.setOnClickListener { annulerRdv(r.id) }
            v.llHistorique.addView(c.root)
        }

        // Messages
        v.tvMessageDonneur.text = msgDonneur
        v.tvMessageDonneur.visibility = if (msgDonneur.isEmpty()) View.GONE else View.VISIBLE
        v.tvErreurDonneur.text = erreurDonneur
        v.tvErreurDonneur.visibility = if (erreurDonneur.isEmpty()) View.GONE else View.VISIBLE

        // Liste des centres (on garde le choix en cours)
        val choix = v.spCentre.selectedItemPosition
        v.spCentre.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            centresApi.map { "${it.nom} (${it.ville ?: ""})" }
        )
        if (choix in centresApi.indices) v.spCentre.setSelection(choix)
        v.btnReserver.isEnabled = !reservationEnCours
    }

    private fun nomCentreDonneur(id: Long) = centresApi.find { it.id == id }?.nom ?: "Centre de collecte"

    private fun annulerRdv(id: Long) {
        confirmer("Annuler le rendez-vous", "Voulez-vous vraiment annuler ce rendez-vous ?", "Annuler le rendez-vous") {
            msgDonneur = ""
            erreurDonneur = ""
            lifecycleScope.launch {
                when (val r = appeler("Erreur lors de l'annulation.") { RetrofitClient.api.annulerRdv(id) }) {
                    is Resultat.Ok -> {
                        rdvDonneur = rdvDonneur.map { if (it.id == id) r.valeur else it }
                        msgDonneur = "Rendez-vous annulé."
                    }
                    is Resultat.Echec -> { if (r.sessionExpiree) return@launch sessionExpiree(); erreurDonneur = r.message }
                }
                rendreDonneur()
            }
        }
    }

    private fun reserverRdv() {
        if (reservationEnCours) return
        msgDonneur = ""
        erreurDonneur = ""
        val d = donneurMoi ?: return
        val centre = centresApi.getOrNull(b.viewEspaceDonneur.spCentre.selectedItemPosition)
        val date = rdvChoisi
        when {
            centre == null -> erreurDonneur = "Aucun centre disponible."
            date == null -> erreurDonneur = "Veuillez choisir la date et l'heure."
            date.timeInMillis < System.currentTimeMillis() -> erreurDonneur = "La date doit être dans le futur."
            else -> {
                val local = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
                if (local.toLocalDate().toString() in Regles.joursBloques(rdvDonneur)) {
                    erreurDonneur = "Vous avez déjà un rendez-vous ce jour-là : choisissez une autre date."
                } else {
                    reservationEnCours = true
                    b.viewEspaceDonneur.btnReserver.isEnabled = false
                    lifecycleScope.launch {
                        val corps = RdvRequest(d.id, centre.id, Regles.versApiDateHeure(local))
                        when (val r = appeler("Erreur lors de la réservation.") { RetrofitClient.api.creerRdv(corps) }) {
                            is Resultat.Ok -> {
                                rdvDonneur = rdvDonneur + r.valeur
                                msgDonneur = "Rendez-vous réservé avec succès."
                                rdvChoisi = null
                                b.viewEspaceDonneur.etDateRdv.text.clear()
                            }
                            // Règles du serveur (90 jours après le dernier don, un seul rendez-vous par jour…)
                            is Resultat.Echec -> {
                                if (r.sessionExpiree) { reservationEnCours = false; return@launch sessionExpiree() }
                                erreurDonneur = r.message
                            }
                        }
                        reservationEnCours = false
                        rendreDonneur()
                    }
                }
            }
        }
        rendreDonneur()
    }
}
