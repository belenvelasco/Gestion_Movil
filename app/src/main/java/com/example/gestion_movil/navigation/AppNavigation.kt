package com.example.gestion_movil.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestion_movil.lecturaQR.CameraPermissionHelper
import com.example.gestion_movil.ui.screen.RegistroScreen
import com.example.gestion_movil.ui.screen.ResumenScreen
import com.example.gestion_movil.ui.view.QrScannerScreen
import com.example.gestion_movil.ui.viewmodel.QrViewModel
import com.example.gestion_movil.ui.viewmodel.UsuarioViewModel

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    val context = LocalContext.current

    val UsuarioViewModel: UsuarioViewModel = viewModel()
    val QrViewModel: QrViewModel = viewModel()

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
        startDestination = "registro"
    ){
        composable("registro"){
            RegistroScreen(navController, UsuarioViewModel)
        }

        composable("resumen"){
            ResumenScreen(UsuarioViewModel)
        }

        composable("Qr"){
            QrScannerScreen(
                viewModel = QrViewModel,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            )
        }
    }
}