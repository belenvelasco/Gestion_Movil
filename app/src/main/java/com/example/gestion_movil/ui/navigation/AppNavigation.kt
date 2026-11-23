package com.example.gestion_movil.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.gestion_movil.camara.CamaraLibreScreen
import com.example.gestion_movil.ui.panel.PanelSupervisorScreen
import com.example.gestion_movil.ui.panel.PanelSupervisorViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gestion_movil.camara.CamaraEvidenciaScreen
import com.example.gestion_movil.lecturaQR.CameraPermissionHelper
import com.example.gestion_movil.ui.eventos.EventosExternosScreen
import com.example.gestion_movil.ui.eventos.EventosExternosViewModel
import com.example.gestion_movil.ui.evidencias.EvidenciaViewModel
import com.example.gestion_movil.ui.qr.QrScannerScreen
import com.example.gestion_movil.ui.qr.QrViewModel
import com.example.gestion_movil.ui.login.LoginScreen
import com.example.gestion_movil.ui.login.LoginViewModel
import com.example.gestion_movil.ui.login.Rol
import com.example.gestion_movil.ui.menu.MenuScreen
import com.example.gestion_movil.ui.oportunidad.DetalleOportunidadScreen
import com.example.gestion_movil.ui.oportunidad.DetalleOportunidadViewModel
import com.example.gestion_movil.ui.oportunidad.ListaOportunidadesScreen
import com.example.gestion_movil.ui.oportunidad.OportunidadFormScreen
import com.example.gestion_movil.ui.oportunidad.OportunidadViewModel
import com.example.gestion_movil.ui.servidor.ServidorScreen
import com.example.gestion_movil.ui.servidor.ServidorViewModel
import com.example.gestion_movil.ui.activacion.ActivacionScreen
import com.example.gestion_movil.ui.activacion.ActivacionViewModel

object Rutas{
    const val  LOGIN = "login"

    const val  MENU = "menu"

    const val  LISTA = "lista"

    const val DETALLE = "detalle/{id}"

    const val FORM = "form/{id}"

    const val  Qr = "Qr"

    const val EVENTOS = "eventos"

    const val CAMARA = "camara/{id}"

    const val CAMERA_LIBRE = "camara_libre"

    const val PANEL = "panel"

    const val SERVIDOR = "servidor"

    const val ACTIVACION = "activacion/{id}"

    fun activacion(id: Int) = "activacion/$id"
    fun detalle(id: Int) = "detalle/$id"

    fun camara(id: Int) = "camara/$id"
    fun form(id: Int) = "form/$id"
}

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    val context = LocalContext.current

    //
    val loginViewModel: LoginViewModel = viewModel()
    val oportunidadViewModel: OportunidadViewModel = viewModel()
    val qrViewModel : QrViewModel = viewModel()

    //
    var hasCameraPermission by remember {
        mutableStateOf(CameraPermissionHelper.hasCameraPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
    ) {
        composable(Rutas.LOGIN){
            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = {
                    navController.navigate(Rutas.MENU){
                        popUpTo(Rutas.LOGIN){inclusive = true}
                    }
                }
            )
        }

        composable(Rutas.MENU){
            val sesion by loginViewModel.uiState.collectAsState()
            MenuScreen(
                esSupervisor = sesion.rol == Rol.SUPERVISOR,
                onVerPanel = { navController.navigate(Rutas.PANEL)},
                onVerOportunidades = {navController.navigate(Rutas.LISTA)},
                onNuevaOportunidades = { navController.navigate(Rutas.form(0))},
                onEscanerQr = { navController.navigate(Rutas.Qr)},
                onCamera = {navController.navigate(Rutas.CAMERA_LIBRE)},
                onVerCatalogo = {navController.navigate(Rutas.EVENTOS)},
                onVerServidor = {navController.navigate(Rutas.SERVIDOR)},
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN){
                        popUpTo(Rutas.MENU) {inclusive = true}
                    }
                }
            )
        }

        composable(Rutas.LISTA){
            ListaOportunidadesScreen(
                viewModel = oportunidadViewModel,
                onNueva = {navController.navigate(Rutas.form(0))},
                onVerDetalle = {id -> navController.navigate(Rutas.detalle(id))}
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument("id") {type = NavType.IntType})
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            val detalleViewModel: DetalleOportunidadViewModel = viewModel()
            val evidenciaViewModel: EvidenciaViewModel = viewModel()
            val sesion by loginViewModel.uiState.collectAsState()
            DetalleOportunidadScreen(
                id = id,
                esSupervisor = sesion.rol == Rol.SUPERVISOR,
                viewModel = detalleViewModel,
                evidenciaViewModel = evidenciaViewModel,
                onEditar = { navController.navigate(Rutas.form(it)) },
                onAbrirCamara = { navController.navigate(Rutas.camara(it)) },
                onAbrirActivacion = {navController.navigate(Rutas.activacion(it))}
            )
        }
        composable(
            route = Rutas.CAMARA,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            val evidenciaViewModel: EvidenciaViewModel = viewModel()
            CamaraEvidenciaScreen(
                oportunidadId = id,
                viewModel = evidenciaViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Rutas.ACTIVACION,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            val activacionViewModel: ActivacionViewModel = viewModel()
            val evidenciaViewModel: EvidenciaViewModel = viewModel()
            ActivacionScreen(
                id = id,
                viewModel = activacionViewModel,
                evidenciaViewModel = evidenciaViewModel,
                onAbrirCamara = { navController.navigate(Rutas.camara(it)) }
            )
        }

        composable (Rutas.CAMERA_LIBRE) {
            CamaraLibreScreen(onVolver = {navController.popBackStack()})
        }

        composable(Rutas.PANEL) {
            val panelViewModel: PanelSupervisorViewModel = viewModel()
            PanelSupervisorScreen(
                viewModel = panelViewModel,
                onVerDetalle = { id -> navController.navigate(Rutas.detalle(id)) }
            )
        }

        composable(Rutas.EVENTOS) {
            val eventosViewModel: EventosExternosViewModel = viewModel()
            EventosExternosScreen(viewModel = eventosViewModel)
        }

        composable(
            route = Rutas.FORM,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            OportunidadFormScreen(
                id = id,
                viewModel = oportunidadViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.Qr) {
            // Mantiene la lista suscrita para poder buscar por código
            oportunidadViewModel.lista.collectAsState()
            LaunchedEffect(Unit) { qrViewModel.clearResult() }

            QrScannerScreen(
                viewModel = qrViewModel,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                },
                buscarOportunidad = { codigo -> oportunidadViewModel.buscarPorCodigo(codigo) },
                onAbrirOportunidad = { id -> navController.navigate(Rutas.detalle(id)) }
            )
        }

        composable(Rutas.SERVIDOR) {
            val servidorViewModel: ServidorViewModel = viewModel()
            ServidorScreen(servidorViewModel)
        }
    }
}

