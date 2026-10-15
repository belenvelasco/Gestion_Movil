package com.example.gestion_movil.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    val UsuarioViewModel: UsuarioVIewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "registro"
    ){
        composable("registro"){
            RegistroScanner(navController, usuarioViewModel)
        }

        composable("resumen"){
            ResumenScanner(usuarioViewModel)
        }
    }
}