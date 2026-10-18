package com.example.gestion_movil.ui.navigation

import android.Manifest
import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gestion_movil.lecturaQR.CameraPermissionHelper
import com.example.gestion_movil.ui.Qr.QrScannerScreen
import com.example.gestion_movil.ui.Qr.QrViewModel
import com.example.gestion_movil.ui.login.LoginScreen
import com.example.gestion_movil.ui.login.LoginViewModel
import com.example.gestion_movil.ui.menu.MenuScreen
import com.example.gestion_movil.ui.oportunidad.ListaOportunidadesScreen
import com.example.gestion_movil.ui.oportunidad.OportunidadFormScreen
import com.example.gestion_movil.ui.oportunidad.OportunidadViewModel

object Rutas{
    const val  LOGIN = "login"

    const val  MENU = "menu"

    const val  LISTA = "lista"

    const val FORM = "form/{id}"

    const val  Qr = "Qr"

    fun form(id: Int) = "form/$id"
}

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    val context = LocalContext.current

    val loginViewModel: LoginViewModel = viewModel()
    val oportunidadViewModel: OportunidadViewModel = viewModel()
    val qrViewModel : QrViewModel = viewModel()

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
        startDestination = Rutas.LOGIN
    ){
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
            MenuScreen(
                onVerOportunidades = {navController.navigate(Rutas.LISTA)},
                onNuevaOportunidades = { navController.navigate(Rutas.form(0))},
                onEscanerQr = { navController.navigate(Rutas.Qr)},
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
                onEditar = {id -> navController.navigate(Rutas.form(id))}
            )
        }

        composable(
            route = Rutas.FORM,
            arguments = listOf(navArgument("id") {type = NavType.IntType})
        ) {entrada ->
            val id = entrada.arguments?.getInt("id")?: 0
            OportunidadFormScreen(
                id = id,
                viewModel = oportunidadViewModel,
                onVolver = {navController.popBackStack()}
            )
        }

        composable (Rutas.Qr){
            oportunidadViewModel.lista.collectAsState()
            LaunchedEffect(Unit) {qrViewModel.clearResult() }

            QrScannerScreen(

                viewModel = qrViewModel,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                buscarOportunidad = { codigo -> oportunidadViewModel.buscarPorCodigo(codigo)},
                onAbrirOportunidad = { id -> navController.navigate(Rutas.form(id)) }
            )
        }
    }
}