package com.example.gestion_movil.ui.login


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado.loginExitoso) {
        if (estado.loginExitoso) {
            viewModel.consumirLogin()
            onLoginExitoso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment =  androidx.compose.ui.Alignment.CenterVertically)

    ) {
        Text("Gestión de oportunidades", style = MaterialTheme.typography.headlineMedium )
        Text("Iniciar sesión con la cuenta ejecutiva", style = MaterialTheme.typography.bodyMedium)

        OutlinedTextField(
            value = estado.usuario,
            onValueChange = viewModel::onUsuario,
            label = { Text("Usuario") },
            singleLine = true,
            isError = estado.usuarioError != null,
            supportingText = { Text(estado.usuarioError ?: "") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.clave,
            onValueChange = viewModel::onClave,
            label = { Text("Clave") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = estado.claveError != null,
            supportingText = { Text(estado.claveError ?: "") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {viewModel.iniciarSesion()},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Text(
            "Cuenta de prueba: ${LoginViewModel.USUARIO_PRUEBA} / ${LoginViewModel.CLAVE_PRUEBA}" +
                    ". ${LoginViewModel.USUARIO_SUPERVISOR} / ${LoginViewModel.CLAVE_SUPERVISOR}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
