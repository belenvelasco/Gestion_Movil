package com.example.gestion_movil.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LOginScreen(
    ViewModel: LoginViewModel,
    onLoginExitoso: () -> Unit
) {
    val estado by ViewModel.uiState.collectAsState()

    LaunchedEffect(estado.loginE) { }
}