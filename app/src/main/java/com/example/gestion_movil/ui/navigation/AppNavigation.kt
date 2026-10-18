package com.example.gestion_movil.ui.navigation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.gestion_movil.ui.login.LOginScreen
import com.example.gestion_movil.ui.login.LoginViewModel
import com.example.gestion_movil.ui.menu.MenuScreen
import com.example.gestion_movil.ui.screen.RegistroScreen
import com.example.gestion_movil.ui.screen.ResumenScreen
import com.example.gestion_movil.ui.view.QrScannerScreen
import com.example.gestion_movil.ui.viewmodel.QrViewModel
import com.example.gestion_movil.ui.viewmodel.UsuarioViewModel


object Rutas{
    const val  LOGIN = "login"

    const val  MENU = " menu"

    const val  LISTA = " lista"

    const val FORM = "form/{id}"

    const val  Qr = "Qr"

    fun form(id: Int) = "form/$id"
}

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    val context = LocalContext.current

    val loginViewModel: LoginViewModel = viewModel()
    val oprtunidadViewModel: OportunidadViewModel = viewModel()
    val QrViewModel : QrViewModel = viewModel()

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
            LOginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = {
                    navController.navigate(Rutas.MENU){
                        popUpToId(Rutas.LOGIN){inclosive = true}
                    }
                }
            )
        }

        composable(Rutas.MENU){
            MenuScreen(
                onVerOportunidades = {navController.navigate(Rutas.LISTA)},
                onnuevaOportunidades = { navController.navigate(Rutas.form(0))},
                onEscanerQr = { navController.navigate(Rutas.Qr)},
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN){
                        popUpTo(Rutas.MENU) {inclusive = true}
                    }
                }
            )
        }

        composable(Rutas.LISTA){
            ListaOportunidadScreen(
                viewModel = oprtunidadViewModel,
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
                ViewModel = oprtunidadViewModel,
                onVolver = {navController.popBackStack()}
            )
        }

        composable (Rutas.Qr){
            oprtunidadViewModel.lista.collectAsState()
            LaunchedEffect(Unit) {QrViewModel.clearResult() }

            QrScannerScreen(
                ViewModel = QrViewModel,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = {
                    permissionLauncher.launch(androidx.Manifest.permission.CAMARA)
                },
                buscarportunidad = { codigo -> oportunidadViewModel.buscarPorCodigo(codigo)},
                onAbrirOportunidad = {id -> navController.navigate(Rutas,form(id))}
            )
        }
    }
}