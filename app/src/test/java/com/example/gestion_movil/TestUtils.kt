package com.example.gestion_movil

import com.example.gestion_movil.data.local.OportunidadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Reemplaza Dispatchers.Main por uno de pruebas. Los ViewModels usan viewModelScope,
 * que necesita un Main; en una prueba JVM no existe, así que se lo damos nosotros.
 * Con UnconfinedTestDispatcher las corrutinas se ejecutan de inmediato.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/**
 * Los StateFlow creados con stateIn(WhileSubscribed) solo se actualizan si alguien los observa.
 * Esta función "se suscribe" durante la prueba, como lo haría la pantalla con collectAsState().
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.recolectar(flow: Flow<*>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect { } }
}

/** Oportunidad de ejemplo (datos ficticios) para no repetir el constructor en cada prueba. */
fun oportunidadDemo(
    id: Int = 1,
    nombre: String = "Oportunidad $id",
    tipo: String = "Feria empresarial",
    estado: String = "Evento identificado",
    prioridad: String = "Media",
    comuna: String = "",
    responsable: String = "",
    organizacionId: Int? = null
) = OportunidadEntity(
    id = id,
    nombre = nombre,
    tipo = tipo,
    descripcion = "Descripción de $nombre",
    fuente = "Fuente demo",
    prioridad = prioridad,
    potencial = "Media",
    estado = estado,
    observaciones = "",
    comuna = comuna,
    responsable = responsable,
    organizacionId = organizacionId
)
