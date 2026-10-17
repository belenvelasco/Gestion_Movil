package com.example.gestion_movil.ui.login

import android.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.w3c.dom.Text
import java.nio.file.WatchEvent

@Composable
fun LOginScreen(
    ViewModel: LoginViewModel,
    onLoginExitoso: () -> Unit
) {
    val estado by ViewModel.uiState.collectAsState()

    LaunchedEffect(estado.loginExitoso) {
        if (estado.loginExitoso) {
            ViewModel.consumirLogin()
            onLoginExitoso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = androidx.compose.ui.Alignment.CenterVertically)

    ) {
        Text("Gestion de oportunidades", stayle = MaterialTheme.typography.headlineMedium )
        Text("Iniciar sesion con la cuenta ejecutiva", stayle = MaterialTheme.typography.bodyMedium)

        OutlinedTextField(
            value = estado.Usuario,
            onValueChange = ViewModel::onUsuario,
            label = { Text("usuario")},
            singleLine = true,
            isError = estado.usuarioError!=null,
            supportingText = { Text(estado.usuarioError ?: "")},
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value =  estado.Clave,
            onValueChange = ViewModel:: onClave,
            label = { Text("clave")},
            singleLine = true,
            VisualTransformation = PasswordVisualTransformation(),
            isError = estado.ClaveError != null,
            SupportingText = { Text(estado.ClaveError?: "")},
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {ViewModel.iniciarSesion()},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesion")
        }

        Text(
            "Cuenta de prube: ${LOginViewModel.USUARIO_PRUEBA} / ${LoginViewModel.CLAVE_PRUEBA}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}