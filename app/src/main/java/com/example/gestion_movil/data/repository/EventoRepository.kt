package com.example.gestion_movil.data.repository

import com.example.gestion_movil.data.model.EventoExterno
import com.example.gestion_movil.remote.ApiService
import com.example.gestion_movil.remote.RetrofitInstance


class EventoRepository(
    private val api: ApiService = RetrofitInstance.api
) {

    companion object {
        private const val DIA_EN_MILIS = 86_400_000L
    }

    // posts = eventos; users = organización (empresa) y comuna (ciudad) de cada evento.
    // El servicio de prueba no trae fechas: se arma una fecha de ejemplo, un poco más lejos por evento.
    suspend fun obtenerEventos(hoy: Long = System.currentTimeMillis()): List<EventoExterno> {
        val posts = api.getPosts()
        val usuarios = api.getUsers().associateBy { it.id }

        return posts.map { post ->
            val usuario = usuarios[post.userId]
            EventoExterno(
                id = post.id,
                nombre = (post.title ?: "Evento ${post.id}").trim()
                    .replaceFirstChar { it.uppercase() },
                descripcion = (post.body ?: "").replace("\n", " ").trim(),
                fecha = fechaDeEjemplo(hoy, post.id),
                comuna = usuario?.address?.city ?: "Sin comuna",
                organizacion = usuario?.company?.name ?: "Sin organización"
            )
        }
    }

    // Medianoche UTC de (hoy + 7 días + 3 días por cada id)
    private fun fechaDeEjemplo(hoy: Long, id: Int): Long =
        ((hoy / DIA_EN_MILIS) + 7 + id * 3L) * DIA_EN_MILIS
}
