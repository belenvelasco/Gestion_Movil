package com.example.gestion_movil.Ubicasion

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

object UbicacionHelper {

    val PERMISOS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private const val EDAD_MAXIMA_MS = 2 * 60 * 1000L
    private const val TIEMPO_ESPERA_MS = 20_000L

    fun tienePermiso(context: Context): Boolean = PERMISOS.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun tienePermisoPreciso(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    // Entrega la ubicación actual o no se pudo obtener.
    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun obtenerUbicacion(context: Context, onResultado: (Location?) -> Unit) {
        if (!tienePermiso(context)) {
            onResultado(null)
            return
        }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Con permiso "aproximado" solo se puede usar el proveedor de red
        val candidatos = if (tienePermisoPreciso(context)) {
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        } else {
            listOf(LocationManager.NETWORK_PROVIDER)
        }

        try {
            val proveedores = candidatos.filter { manager.isProviderEnabled(it) }
            if (proveedores.isEmpty()) {
                onResultado(null)
                return
            }

            // 1) Si hay una ubicación reciente guardada, se usa directamente
            val reciente = proveedores
                .mapNotNull { manager.getLastKnownLocation(it) }
                .filter { System.currentTimeMillis() - it.time < EDAD_MAXIMA_MS }
                .maxByOrNull { it.time }
            if (reciente != null) {
                onResultado(reciente)
                return
            }

            // 2) Si no, se pide una ubicación nueva
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pedirActual(manager, context, proveedores, onResultado)
            } else {
                pedirUnaVez(manager, proveedores, onResultado)
            }
        } catch (e: SecurityException) {
            onResultado(null)
        }
    }

    // Android 11 o superior: getCurrentLocation, probando proveedor por proveedor
    @SuppressLint("MissingPermission")
    private fun pedirActual(
        manager: LocationManager,
        context: Context,
        proveedores: List<String>,
        onResultado: (Location?) -> Unit
    ) {
        if (proveedores.isEmpty()) {
            onResultado(null)
            return
        }
        try {
            manager.getCurrentLocation(
                proveedores.first(),
                null,
                ContextCompat.getMainExecutor(context)
            ) { ubicacion ->
                if (ubicacion != null) {
                    onResultado(ubicacion)
                } else {
                    pedirActual(manager, context, proveedores.drop(1), onResultado)
                }
            }
        } catch (e: SecurityException) {
            onResultado(null)
        }
    }

    // Android 10 o inferior: una sola actualización con tiempo máximo de espera
    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun pedirUnaVez(
        manager: LocationManager,
        proveedores: List<String>,
        onResultado: (Location?) -> Unit
    ) {
        val handler = Handler(Looper.getMainLooper())
        val escuchas = mutableListOf<LocationListener>()
        var entregado = false

        fun terminar(ubicacion: Location?) {
            if (entregado) return
            entregado = true
            handler.removeCallbacksAndMessages(null)
            escuchas.forEach { manager.removeUpdates(it) }
            onResultado(ubicacion)
        }

        try {
            proveedores.forEach { proveedor ->
                val escucha = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        terminar(location)
                    }

                    override fun onProviderEnabled(provider: String) {}

                    override fun onProviderDisabled(provider: String) {}

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }
                escuchas.add(escucha)
                manager.requestSingleUpdate(proveedor, escucha, Looper.getMainLooper())
            }
            handler.postDelayed({ terminar(null) }, TIEMPO_ESPERA_MS)
        } catch (e: SecurityException) {
            terminar(null)
        }
    }
}