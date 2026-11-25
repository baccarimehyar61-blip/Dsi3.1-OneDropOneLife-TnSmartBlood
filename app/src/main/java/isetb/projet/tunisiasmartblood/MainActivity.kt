package isetb.projet.tunisiasmartblood

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Patterns
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import isetb.projet.tunisiasmartblood.databinding.ActivityMainBinding
import isetb.projet.tunisiasmartblood.databinding.ItemAnnonceBinding
import isetb.projet.tunisiasmartblood.databinding.ItemCampagneBinding
import isetb.projet.tunisiasmartblood.databinding.ItemCarteBinding
import isetb.projet.tunisiasmartblood.databinding.ItemGestionBinding
import isetb.projet.tunisiasmartblood.databinding.ViewEspaceBinding
import isetb.projet.tunisiasmartblood.databinding.ItemRdvBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
private const val TYPE_EMAIL = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    // Handler unique, annulé dans onDestroy (évite un crash après fermeture)
    private val handler = Handler(Looper.getMainLooper())

    private var filtreGouvCamp = ""
    private var filtreGroupe = ""
    private var filtreGouvAnn = ""

    private var modeAdmin = false
    private var roleInscription = "donneur"
    private var jeton: String? = null

    private var ongletAdmin = "comptes"
    private var ongletCentre = "rendezvous"
    private var ongletEtab = "besoins"
    private var retourAdmin = ""
    private var msgDonneur = ""
    private var erreurDonneur = ""
    private var rdvChoisi: Calendar? = null
    private var centresDispo = listOf<Centre>()
    private var retourEspace = ""
    private val rolesConnexion = listOf("donneur", "etablissement", "centre")

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
    private val apropos = listOf(
        "Pour les donneurs" to "Trouver une collecte, réserver un créneau et recevoir un rappel avant le jour J.",
        "Pour les centres" to "Publier des campagnes, gérer les rendez-vous et mobiliser plus rapidement.",
        "Pour les établissements" to "Signaler un besoin en sang avec le groupe recherché et le lieu concerné."
    )
    private val contactInfos = listOf(
        "Email" to "TunisiaSmartBlood@gmail.tn",
        "Téléphone" to "+216 70 000 000",
        "Adresse" to "Tunis, Tunisie"
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

        b.etDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                b.etDate.setText(String.format("%02d/%02d/%04d", d, m + 1, y))
            }, 2000, 0, 1).show()
        }

        b.logo.setOnClickListener { afficher(b.viewAccueil) }

        b.btnTopConnexion.setOnClickListener {
            if (adminConnecte || compteConnecte != null) deconnexion() else afficher(b.viewConnexion)
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

        // CORRECTION 1 : l'espace donneur est configuré une seule fois, dès le démarrage
        configurerDonneur()

        b.btnModeUser.setOnClickListener { choisirMode(false) }
        b.btnModeAdmin.setOnClickListener { choisirMode(true) }
        b.btnSeConnecter.setOnClickListener { seConnecter() }
        b.btnInscrire.setOnClickListener { sInscrire() }

        choisirMode(false)
        majSession()
        majOnglets(b.viewAccueil)
    }

    // CORRECTION 5 : on annule les actions différées à la fermeture de l'activité
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    // ----- Navigation -----
    private fun afficher(vue: View) {
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
        val connecte = adminConnecte || compteConnecte != null
        b.btnTopConnexion.text = if (connecte) "Déconnexion" else "Se connecter"
        b.tabAdmin.visibility = if (adminConnecte) View.VISIBLE else View.GONE
        b.tabEspace.visibility = if (compteConnecte != null) View.VISIBLE else View.GONE
    }

    // CORRECTION 4 : réinitialisation complète de l'état du donneur
    private fun deconnexion() {
        handler.removeCallbacksAndMessages(null)
        adminConnecte = false
        compteConnecte = null
        rdvChoisi = null
        msgDonneur = ""
        erreurDonneur = ""
        retourEspace = ""
        retourAdmin = ""
        b.viewEspaceDonneur.etDateRdv.text.clear()
        majSession()
        afficher(b.viewAccueil)
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
            carte.btnReserver.setOnClickListener { afficher(b.viewConnexion) }
            b.llCampagnes.addView(carte.root)
        }
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

            carte.btnDonner.setOnClickListener { choisirProfil("donneur", true) }
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

    // CORRECTION 2 : bloc dupliqué supprimé
    private fun seConnecter() {
        if (!(requis(b.etEmailLogin, b.etPasswordLogin) && emailValide(b.etEmailLogin))) return
        val email = b.etEmailLogin.text.toString().trim()
        val mdp = b.etPasswordLogin.text.toString()

        // Mode administrateur : inchangé (identifiants locaux)
        if (modeAdmin) {
            if (email.lowercase() == ADMIN_EMAIL && mdp == ADMIN_MDP) {
                adminConnecte = true
                compteConnecte = null
                majSession()
                allerAdmin()
            } else {
                b.tvMessageLogin.text = "Accès refusé : identifiants administrateur incorrects."
            }
            return
        }

        b.tvMessageLogin.text = "Connexion en cours..."
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.login(LoginRequest(email, mdp))
                val auth = res.body()
                if (res.isSuccessful && auth != null) {
                    jeton = auth.token
                    adminConnecte = false

                    val r = auth.role.lowercase()
                    val role = when {
                        r.contains("donneur") -> "donneur"
                        r.contains("centre") -> "centre"
                        else -> rolesConnexion[b.spProfilConnexion.selectedItemPosition]
                    }
                    compteConnecte = Compte(role, auth.nom, auth.email)

                    b.tvMessageLogin.text = "Connexion réussie !"
                    majSession()
                    ouvrirEspaceApres()
                } else if (res.code() == 400 || res.code() == 401 || res.code() == 403) {
                    b.tvMessageLogin.text = "E-mail ou mot de passe incorrect."
                } else {
                    b.tvMessageLogin.text = "Erreur du serveur (${res.code()})."
                }
            } catch (e: Exception) {
                b.tvMessageLogin.text = "Serveur injoignable. Vérifiez que le backend est lancé."
            }
        }
    }

    // CORRECTION 3 : message d'erreur pour les listes déroulantes non choisies
    private fun sInscrire() {
        b.tvMessageInsc.text = ""
        var ok = requis(b.etEmailInsc, b.etPasswordInsc)
        if (b.llDonneur.visibility == View.VISIBLE) {
            ok = requis(b.etNomDonneur, b.etPrenom, b.etDate, b.etTel) && ok
            if (b.spGroupe.selectedItemPosition == 0 || b.spGouvernorat.selectedItemPosition == 0) {
                ok = false
                b.tvMessageInsc.text = "Veuillez sélectionner le groupe sanguin et le gouvernorat."
            }
        } else {
            ok = requis(b.etNomEtab) && ok
        }
        if (!(ok && emailValide(b.etEmailInsc))) return

        val email = b.etEmailInsc.texte()
        val mdp = b.etPasswordInsc.text.toString()
        val estDonneur = roleInscription == "donneur"

        val demande = if (estDonneur) {
            RegisterRequest(
                nom = b.etNomDonneur.texte(),
                email = email,
                motDePasse = mdp,
                prenom = b.etPrenom.texte(),
                groupeSanguin = groupeVersApi(b.spGroupe.selectedItem.toString()),
                ville = b.spGouvernorat.selectedItem.toString(),
                telephone = b.etTel.texte().filter { it.isDigit() }.takeLast(8)
            )
        } else {
            RegisterRequest(nom = b.etNomEtab.texte(), email = email, motDePasse = mdp)
        }

        b.tvMessageInsc.text = "Création du compte..."
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.register(demande)
                val auth = res.body()
                if (res.isSuccessful && auth != null) {
                    jeton = auth.token
                    adminConnecte = false
                    b.tvMessageInsc.text = "Compte créé avec succès !"

                    if (estDonneur) {
                        val nom = b.etNomDonneur.texte()
                        val prenom = b.etPrenom.texte()
                        val groupe = b.spGroupe.selectedItem.toString()
                        val gouv = b.spGouvernorat.selectedItem.toString()
                        compteConnecte = Compte("donneur", nom, email, gouv, b.etTel.texte(),
                            prenom = prenom, groupe = groupe)
                        donneurs.add(Donneur(nouvelId(), "$nom $prenom", groupe, gouv, "Actif"))
                        rdvDonneur.clear()
                    } else {
                        compteConnecte = Compte(roleInscription, b.etNomEtab.texte(), email)
                    }
                    majSession()
                    ouvrirEspaceApres()
                } else if (res.code() == 409) {
                    b.tvMessageInsc.text = "Cet e-mail est déjà utilisé."
                } else if (res.code() == 400) {
                    b.tvMessageInsc.text = "Données invalides (mot de passe de 6 caractères minimum, téléphone de 8 chiffres)."
                } else {
                    b.tvMessageInsc.text = "Erreur du serveur (${res.code()})."
                }
            } catch (e: Exception) {
                b.tvMessageInsc.text = "Serveur injoignable. Vérifiez que le backend est lancé."
            }
        }
    }

    // Convertit « O+ » en valeur de l'enum du backend (À VÉRIFIER, voir plus bas)
    private fun groupeVersApi(g: String): String =
        g.replace("+", "_POSITIF").replace("-", "_NEGATIF")

    // CORRECTION 5 : utilise le handler unique
    private fun ouvrirEspaceApres() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (compteConnecte != null &&
                (b.viewConnexion.visibility == View.VISIBLE || b.viewInscription.visibility == View.VISIBLE)
            ) allerEspace()
        }, 1200)
    }

    private fun envoyerContact() {
        val champs = arrayOf(b.etNomContact, b.etEmailContact, b.etSujet, b.etMessageContact)
        if (requis(*champs) && emailValide(b.etEmailContact)) {
            b.tvMessageContact.text = "Merci ! Votre message a bien été envoyé."
            champs.forEach { it.text.clear() }
        }
    }

    // ----- Espace administrateur -----
    private fun allerAdmin() {
        if (!adminConnecte) return
        ongletAdmin = "comptes"
        retourAdmin = ""
        rendreAdmin()
        afficher(b.viewAdmin.root)
    }

    private fun rendreAdmin() {
        val v = b.viewAdmin
        v.tvEtiquette.text = "ADMINISTRATION"
        v.tvTitreEspace.text = "Tableau de bord administrateur"
        v.tvSousTitre.text = "Validez les comptes, supervisez les campagnes et modérez les annonces."
        dessinerOnglets(
            v, listOf(
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
            "comptes" -> {
                if (comptesEnAttente.isEmpty()) vide(l, "Aucun compte en attente de validation.")
                comptesEnAttente.toList().forEach { c ->
                    carteGestion(l, c.nom, badge = c.type, texte = c.email, meta = "📍 ${c.gouvernorat}",
                        actionA = "Valider" to {
                            comptesEnAttente.removeAll { it.id == c.id }
                            centres.add(Centre(nouvelId(), c.nom, c.type, c.gouvernorat, "Actif"))
                            retourAdmin = "Compte « ${c.nom} » validé."
                            rendreAdmin()
                        },
                        actionB = "Refuser" to {
                            comptesEnAttente.removeAll { it.id == c.id }
                            retourAdmin = "Compte refusé."
                            rendreAdmin()
                        })
                }
            }
            "donneurs" -> {
                if (donneurs.isEmpty()) vide(l, "Aucun donneur.")
                donneurs.toList().forEach { d ->
                    carteGestion(l, d.nom, groupe = d.groupe, badge = d.statut, badgeUrgent = d.statut == "Suspendu",
                        meta = "📍 ${d.gouvernorat}",
                        actionA = (if (d.statut == "Actif") "Suspendre" else "Réactiver") to {
                            d.statut = if (d.statut == "Actif") "Suspendu" else "Actif"
                            rendreAdmin()
                        },
                        actionB = "Supprimer" to {
                            donneurs.removeAll { it.id == d.id }
                            retourAdmin = "Donneur supprimé."
                            rendreAdmin()
                        })
                }
            }
            "centres" -> {
                if (centres.isEmpty()) vide(l, "Aucun centre.")
                centres.toList().forEach { c ->
                    carteGestion(l, c.nom, badge = "${c.type} · ${c.statut}", badgeUrgent = c.statut == "Suspendu",
                        meta = "📍 ${c.gouvernorat}",
                        actionA = (if (c.statut == "Actif") "Suspendre" else "Réactiver") to {
                            c.statut = if (c.statut == "Actif") "Suspendu" else "Actif"
                            rendreAdmin()
                        },
                        actionB = "Supprimer" to {
                            centres.removeAll { it.id == c.id }
                            retourAdmin = "Centre supprimé."
                            rendreAdmin()
                        })
                }
            }
            "campagnes" -> {
                if (campagnes.isEmpty()) vide(l, "Aucune campagne à superviser.")
                campagnes.toList().forEach { c ->
                    carteGestion(l, c.titre, badge = c.date, texte = c.organisateur,
                        meta = "📍 ${c.lieu}\n🕒 ${c.horaire}",
                        actionA = "Annuler la campagne" to {
                            campagnes.removeAll { it.id == c.id }
                            retourAdmin = "Campagne annulée."
                            rendreAdmin()
                        })
                }
            }
            "annonces" -> {
                if (annonces.isEmpty()) vide(l, "Aucune annonce à modérer.")
                annonces.toList().forEach { a ->
                    carteGestion(l, a.etablissement, groupe = a.groupe,
                        badge = if (a.urgent) "URGENT" else null, badgeUrgent = true, texte = a.description,
                        actionA = (if (a.urgent) "Retirer l'urgence" else "Marquer urgent") to {
                            a.urgent = !a.urgent
                            rendreAdmin()
                        },
                        actionB = "Retirer" to {
                            annonces.removeAll { it.id == a.id }
                            retourAdmin = "Annonce retirée."
                            rendreAdmin()
                        })
                }
            }
            else -> {
                remplir(l, listOf(
                    "${donneurs.size}" to "Donneurs inscrits",
                    "${centres.size}" to "Centres et établissements",
                    "${comptesEnAttente.size}" to "Comptes en attente",
                    "${campagnes.size}" to "Campagnes",
                    "${annonces.size}" to "Annonces publiées",
                    "${annonces.count { it.urgent }}" to "Annonces urgentes"
                ), gros = true)
                l.addView(texteCentre("Donneurs par groupe : " + groupesSanguins.joinToString("   ·   ") { g ->
                    "$g : ${donneurs.count { it.groupe == g }}"
                }))
                l.addView(bouton("Générer un rapport") { genererRapport() })
            }
        }
    }

    private fun genererRapport() {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
        val texte = buildString {
            appendLine("Rapport Tunisia Smart Blood - $date")
            appendLine()
            appendLine("Donneurs inscrits : ${donneurs.size}")
            appendLine("Centres et établissements : ${centres.size}")
            appendLine("Comptes en attente de validation : ${comptesEnAttente.size}")
            appendLine("Campagnes : ${campagnes.size}")
            appendLine("Annonces publiées : ${annonces.size}")
            appendLine("Annonces urgentes : ${annonces.count { it.urgent }}")
            appendLine()
            appendLine("Donneurs par groupe sanguin :")
            groupesSanguins.forEach { g -> appendLine(" - $g : ${donneurs.count { it.groupe == g }}") }
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
        val cpt = compteConnecte ?: return
        retourEspace = ""
        when (cpt.role) {
            "donneur" -> {
                msgDonneur = ""
                erreurDonneur = ""
                rendreDonneur()
                afficher(b.viewEspaceDonneur.root)
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
        when (compteConnecte?.role) {
            "centre" -> rendreCentre()
            "donneur" -> rendreDonneur()
            else -> rendreEtab()
        }
    }

    // ----- Espace centre de collecte -----
    private fun rendreCentre() {
        val v = b.viewEspaceCentre
        val cpt = compteConnecte ?: return
        v.tvEtiquette.text = "ESPACE CENTRE DE COLLECTE"
        v.tvTitreEspace.text = cpt.nom
        v.tvSousTitre.text = "Gérez vos campagnes, vos créneaux et vos rendez-vous."
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
                    if (fin.iso() <= debut.iso()) {
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
                        meta = "📍 ${c.lieu}\n🕒 ${c.horaire}\n🩸 ${c.groupes ?: ""}",
                        actionA = "Annuler la campagne" to {
                            campagnes.removeAll { it.id == c.id }
                            retourEspace = "Campagne annulée."
                            rendreCentre()
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
                    if (fin.iso() <= debut.iso()) {
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
                val valides = rendezVous.count { it.statut != "Annulé" }
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

    // ----- Espace établissement de santé -----
    private fun rendreEtab() {
        val v = b.viewEspaceEtab
        val cpt = compteConnecte ?: return
        v.tvEtiquette.text = "ESPACE ÉTABLISSEMENT DE SANTÉ"
        v.tvTitreEspace.text = cpt.nom
        v.tvSousTitre.text = "Signalez vos besoins en sang avec le groupe recherché et le lieu concerné."
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
                    "${donneurs.count { it.statut == "Actif" }}" to "Donneurs actifs"
                ), gros = true)
                v.llListe.addView(texteCentre("Donneurs actifs par groupe : " + groupesSanguins.joinToString("   ·   ") { g ->
                    "$g : ${donneurs.count { it.statut == "Actif" && it.groupe == g }}"
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
                compteConnecte?.nom ?: "", gouv.valeur(), desc.texte(), "À l'instant"
            ))
            retourEspace = if (centre) "Message d'urgence publié." else "Besoin publié."
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
                annonces.removeAll { it.id == a.id }
                retourEspace = "Annonce retirée."
                rafraichirEspace()
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
        val cpt = compteConnecte ?: return
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
            if (!Patterns.EMAIL_ADDRESS.matcher(email.texte()).matches()) {
                v.tvRetour.text = "Veuillez saisir un email valide."
                return@bouton
            }
            compteConnecte = cpt.copy(
                email = email.texte(), gouvernorat = gouv.valeur(),
                telephone = tel.texte(), adresse = adr.texte()
            )
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
            DatePickerDialog(this, { _, y, m, d ->
                e.tag = String.format("%04d-%02d-%02d", y, m + 1, d)
                e.setText(String.format("%02d/%02d/%04d", d, m + 1, y))
            }, c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DAY_OF_MONTH]).show()
        }
        return e
    }

    private fun champHeure(parent: LinearLayout, libelle: String): EditText {
        val e = champTexte(parent, libelle, "Choisir l'heure")
        e.isFocusable = false
        e.isCursorVisible = false
        e.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                e.tag = String.format("%02d:%02d", h, m)
                e.setText(heure(String.format("%02d:%02d", h, m)))
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

    // ----- Espace donneur -----
    private fun fmt(motif: String, ms: Long) =
        SimpleDateFormat(motif, Locale.FRANCE).format(Date(ms))

    private fun libelleStatut(s: String) = when (s) {
        "PLANIFIE" -> "Planifié"
        "EFFECTUE" -> "Effectué"
        else -> "Annulé"
    }

    private fun couleursStatut(s: String) = when (s) {
        "PLANIFIE" -> R.color.statut_planifie_fond to R.color.statut_planifie_texte
        "EFFECTUE" -> R.color.statut_effectue_fond to R.color.statut_effectue_texte
        else -> R.color.statut_annule_fond to R.color.statut_annule_texte
    }

    private fun configurerDonneur() {
        val v = b.viewEspaceDonneur
        v.btnSortir.setOnClickListener { deconnexion() }
        v.btnReserver.setOnClickListener { reserverRdv() }
        v.etDateRdv.setOnClickListener { choisirDateHeure(v.etDateRdv) }
    }

    // Android n'a pas de "datetime-local" : on enchaîne deux dialogues
    private fun choisirDateHeure(champ: EditText) {
        val now = Calendar.getInstance()
        val dialogue = DatePickerDialog(this, { _, y, m, d ->
            TimePickerDialog(this, { _, h, min ->
                val c = Calendar.getInstance().apply {
                    set(y, m, d, h, min, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                rdvChoisi = c
                champ.setText(fmt("dd/MM/yyyy HH:mm", c.timeInMillis))
            }, 9, 30, true).show()
        }, now[Calendar.YEAR], now[Calendar.MONTH], now[Calendar.DAY_OF_MONTH])
        dialogue.datePicker.minDate = System.currentTimeMillis() - 1000 // équivalent de [min]="minDate"
        dialogue.show()
    }

    private fun rendreDonneur() {
        val v = b.viewEspaceDonneur
        val cpt = compteConnecte ?: return
        val maintenant = System.currentTimeMillis()

        // En-tête
        val prenom = cpt.prenom ?: ""
        v.tvInitiales.text = (prenom.take(1) + cpt.nom.take(1)).uppercase()
        v.tvNomDonneur.text = "$prenom ${cpt.nom}".trim()
        v.tvContact.text = listOfNotNull(cpt.gouvernorat, cpt.telephone)
            .filter { it.isNotBlank() }.joinToString(" · ")
        v.llGroupe.visibility = if (cpt.groupe.isNullOrEmpty()) View.GONE else View.VISIBLE
        v.tvGroupeDonneur.text = cpt.groupe ?: ""

        // Statistiques
        val effectues = rdvDonneur.filter { it.statut == "EFFECTUE" }
        val planifies = rdvDonneur.filter { it.statut == "PLANIFIE" }
        v.tvNbEffectues.text = "${effectues.size}"
        v.tvNbPlanifies.text = "${planifies.size}"
        val dernier = effectues.maxOfOrNull { it.dateHeure }
        v.tvDernierDon.text = if (dernier != null) fmt("dd/MM/yyyy", dernier) else "—"

        // Prochain rendez-vous : le plus proche parmi les planifiés à venir
        val prochain = planifies.filter { it.dateHeure >= maintenant }.minByOrNull { it.dateHeure }
        v.llProchain.visibility = if (prochain != null) View.VISIBLE else View.GONE
        v.tvVideProchain.visibility = if (prochain == null) View.VISIBLE else View.GONE
        if (prochain != null) {
            v.tvProchainJour.text = fmt("dd", prochain.dateHeure)
            v.tvProchainMois.text = fmt("MM/yyyy", prochain.dateHeure)
            v.tvProchainCentre.text = prochain.centreNom
            v.tvProchainHeure.text = "à " + fmt("HH:mm", prochain.dateHeure)
            v.btnProchainAnnuler.setOnClickListener { annulerRdv(prochain.id) }
        }

        // Historique (du plus récent au plus ancien)
        v.llHistorique.removeAllViews()
        val tous = rdvDonneur.sortedByDescending { it.dateHeure }
        v.tvVideHistorique.visibility = if (tous.isEmpty()) View.VISIBLE else View.GONE
        tous.forEach { r ->
            val c = ItemRdvBinding.inflate(layoutInflater, v.llHistorique, false)
            val (fond, texte) = couleursStatut(r.statut)
            c.tvCentre.text = r.centreNom
            c.tvDate.text = fmt("dd/MM/yyyy HH:mm", r.dateHeure)
            c.tvStatut.text = libelleStatut(r.statut)
            c.tvStatut.backgroundTintList = ColorStateList.valueOf(getColor(fond))
            c.tvStatut.setTextColor(getColor(texte))
            c.btnAnnuler.visibility = if (r.statut == "PLANIFIE") View.VISIBLE else View.GONE
            c.btnAnnuler.setOnClickListener { annulerRdv(r.id) }
            v.llHistorique.addView(c.root)
        }

        // Messages
        v.tvMessageDonneur.text = msgDonneur
        v.tvMessageDonneur.visibility = if (msgDonneur.isEmpty()) View.GONE else View.VISIBLE
        v.tvErreurDonneur.text = erreurDonneur
        v.tvErreurDonneur.visibility = if (erreurDonneur.isEmpty()) View.GONE else View.VISIBLE

        // Liste des centres (on garde le choix en cours)
        centresDispo = centres.filter { it.statut == "Actif" }
        val choix = v.spCentre.selectedItemPosition
        v.spCentre.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            centresDispo.map { "${it.nom} (${it.gouvernorat})" }
        )
        if (choix in centresDispo.indices) v.spCentre.setSelection(choix)
    }

    private fun annulerRdv(id: Int) {
        val r = rdvDonneur.find { it.id == id } ?: return
        r.statut = "ANNULE"
        // le centre voit aussi l'annulation
        rendezVous.find { it.id == r.idRdvCentre }?.statut = "Annulé"
        msgDonneur = "Rendez-vous annulé."
        erreurDonneur = ""
        rendreDonneur()
    }

    private fun reserverRdv() {
        msgDonneur = ""
        erreurDonneur = ""
        val cpt = compteConnecte ?: return
        val centre = centresDispo.getOrNull(b.viewEspaceDonneur.spCentre.selectedItemPosition)
        val date = rdvChoisi
        when {
            centre == null -> erreurDonneur = "Aucun centre disponible."
            date == null -> erreurDonneur = "Veuillez choisir la date et l'heure."
            date.timeInMillis < System.currentTimeMillis() -> erreurDonneur = "La date doit être dans le futur."
            else -> {
                val ms = date.timeInMillis
                val idCentre = nouvelId()
                // Le centre reçoit la demande "En attente" dans son espace
                rendezVous.add(RendezVous(
                    idCentre, "${cpt.prenom ?: ""} ${cpt.nom}".trim(), cpt.groupe ?: "",
                    fmt("d MMMM yyyy · HH'h'mm", ms), "En attente"
                ))
                rdvDonneur.add(RdvDonneur(nouvelId(), centre.nom, ms, "PLANIFIE", idCentre))
                msgDonneur = "Rendez-vous réservé. Le centre le confirmera."
                rdvChoisi = null
                b.viewEspaceDonneur.etDateRdv.text.clear()
            }
        }
        rendreDonneur()
    }
}