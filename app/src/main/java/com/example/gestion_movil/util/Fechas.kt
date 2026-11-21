package com.example.gestion_movil.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Fechas{

    private val LOCALE = Locale("es","CL")

     //Se muetra el UTC para que no se muestre un dia antes
    fun formatearEvento(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy", LOCALE)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millis))

    // Las fechas de registro en el momento exacto que se hizo algo con la hora del telefono
    fun formatear(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy", LOCALE).format(Date(millis))

    fun formatearConHora(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy  HH:mm", LOCALE).format(Date(millis))
}