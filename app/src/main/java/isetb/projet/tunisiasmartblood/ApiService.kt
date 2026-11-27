package isetb.projet.tunisiasmartblood

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints du backend Spring Boot (identiques à ceux de l'application Angular). */
interface ApiService {

    // --- Authentification (publique) ---
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    // --- Donneurs ---
    @GET("donneurs")
    suspend fun donneurs(): Response<List<DonneurApi>>

    @GET("donneurs/moi")
    suspend fun donneurMoi(): Response<DonneurApi>

    @PUT("donneurs/{id}")
    suspend fun majDonneur(@Path("id") id: Long, @Body body: DonneurMajRequest): Response<DonneurApi>

    @DELETE("donneurs/{id}")
    suspend fun supprimerDonneur(@Path("id") id: Long): Response<Unit>

    // --- Centres de collecte ---
    @GET("centres")
    suspend fun centres(): Response<List<CentreApi>>

    @DELETE("centres/{id}")
    suspend fun supprimerCentre(@Path("id") id: Long): Response<Unit>

    // --- Rendez-vous ---
    @GET("rendez-vous")
    suspend fun rendezVous(@Query("donneurId") donneurId: Long? = null): Response<List<RdvApi>>

    @POST("rendez-vous")
    suspend fun creerRdv(@Body body: RdvRequest): Response<RdvApi>

    @PATCH("rendez-vous/{id}/annuler")
    suspend fun annulerRdv(@Path("id") id: Long): Response<RdvApi>

    @PATCH("rendez-vous/{id}/confirmer")
    suspend fun confirmerRdv(@Path("id") id: Long): Response<RdvApi>

    @PATCH("rendez-vous/{id}/refuser")
    suspend fun refuserRdv(@Path("id") id: Long): Response<RdvApi>

    @PATCH("rendez-vous/{id}/effectuer")
    suspend fun effectuerRdv(@Path("id") id: Long): Response<RdvApi>
}
