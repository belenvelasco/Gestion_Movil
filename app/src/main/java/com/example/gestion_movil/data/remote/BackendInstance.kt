package com.example.gestion_movil.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class OportunidadRemotaDto(
    val id: Long,
    val nombre: String?,
    val tipo: String?,
    val descripcion: String?,
    val fuente: String?,
    val prioridad: String?,
    val potencial: String?,
    val estado: String?
)

interface ApiBackend {
    @GET("api/v1/oportunidades")
    suspend fun getOportunidades(): List<OportunidadRemotaDto>
}

object BackendInstance {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    val api: ApiBackend by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiBackend::class.java)
    }
}