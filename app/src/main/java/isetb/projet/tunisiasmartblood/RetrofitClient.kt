package isetb.projet.tunisiasmartblood

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object RetrofitClient {

    // 10.0.2.2 = votre PC, vu depuis l'émulateur Android.
    // Sur un vrai téléphone, remplacez par l'IP de votre PC (commande ipconfig).
    // Le « / » à la fin est obligatoire.
    const val BASE_URL = "http://10.0.2.2:8081/api/"

    /**
     * Ajoute le jeton JWT aux requêtes protégées (jamais aux endpoints publics (auth),
     * ni avec un jeton expiré) : équivalent de l'intercepteur HTTP d'Angular.
     */
    private val jetonInterceptor = Interceptor { chain ->
        val requete = chain.request()
        val jeton = Session.jeton
        val publique = requete.url.encodedPath.contains("/auth/")
        if (jeton == null || publique || Jwt.estExpire(jeton)) {
            chain.proceed(requete)
        } else {
            chain.proceed(requete.newBuilder().header("Authorization", "Bearer $jeton").build())
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(jetonInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            // Les en-têtes (donc le jeton) ne sont jamais journalisés.
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}

/** Résultat d'un appel réseau : valeur ou message d'erreur prêt à afficher. */
sealed class Resultat<out T> {
    data class Ok<T>(val valeur: T) : Resultat<T>()

    /** @param sessionExpiree le serveur a refusé le jeton : la session a été fermée. */
    data class Echec(
        val code: Int,
        val message: String,
        val sessionExpiree: Boolean = false,
        /** Message brut renvoyé par le serveur (ex. refus de profil de connexion), sans reformulation. */
        val serveur: String? = null
    ) : Resultat<Nothing>()
}

private val gson = Gson()

/**
 * Exécute un appel Retrofit et traduit toute erreur (réseau, 4xx, 5xx) en message français.
 * @param protege true pour un endpoint authentifié : un 401 ferme la session (comme l'intercepteur Angular).
 */
suspend fun <T> appeler(
    defaut: String = "Une erreur est survenue.",
    protege: Boolean = true,
    bloc: suspend () -> Response<T>
): Resultat<T> {
    return try {
        val reponse = bloc()
        if (reponse.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            Resultat.Ok(reponse.body() as T)
        } else {
            val erreur = try {
                reponse.errorBody()?.string()?.let { gson.fromJson(it, ErreurApi::class.java) }
            } catch (e: Exception) {
                null
            }
            if (reponse.code() == 401 && protege && Session.source == Session.SOURCE_API) {
                Session.fermer()
                Resultat.Echec(401, Regles.MSG_SESSION_EXPIREE, sessionExpiree = true)
            } else {
                Resultat.Echec(reponse.code(), Regles.messageHttp(reponse.code(), erreur, defaut), serveur = erreur?.message)
            }
        }
    } catch (e: IOException) {
        Resultat.Echec(0, Regles.MSG_INJOIGNABLE)
    }
}
