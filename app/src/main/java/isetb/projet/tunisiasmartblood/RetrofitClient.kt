package isetb.projet.tunisiasmartblood

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 10.0.2.2 = votre PC, vu depuis l'émulateur Android.
    // Sur un vrai téléphone, remplacez par l'IP de votre PC (commande ipconfig).
    // Le « / » à la fin est obligatoire.
    private const val BASE_URL = "http://10.0.2.2:8081/api/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}