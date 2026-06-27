package com.firstexample.gestordetareas.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.unit.dp
import com.firstexample.gestordetareas.ui.viewmodel.AuthState
import com.firstexample.gestordetareas.ui.viewmodel.AuthViewModel

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.ui.unit.dp


@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit // Callback que ejecutaremos cuando el login sea exitoso
) {
    // Herramientas para guardar datos localmente
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE) }
    // Variables de estado para los campos de texto
    var isLoginMode by remember { mutableStateOf(true) }
    // Inicializamos el email con el que esté guardado en el celular
    var email by remember { mutableStateOf(sharedPreferences.getString("email_guardado", "") ?: "") }
    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // <- Nueva variable para el ojito
    var nombre by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("usuario") } // Por defecto es usuario

    // Observamos el estado que viene del ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Si el estado cambia a Success, disparamos la navegación a la siguiente pantalla
    LaunchedEffect(uiState) {
        if (uiState is AuthState.Success) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLoginMode) "Iniciar Sesión" else "Crear Cuenta",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Si estamos en modo registro, mostramos los campos extra (Nombre y Rol)
        if (!isLoginMode) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Selección de Rol
            Text("Selecciona tu rol:", modifier = Modifier.align(Alignment.Start))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = rol == "admin", onClick = { rol = "admin" })
                Text("Mamá (Admin)")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = rol == "usuario", onClick = { rol = "usuario" })
                Text("Hijo (Usuario)")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            // Cambiamos dinámicamente si se ve o no
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            // Agregamos el ícono al final del campo de texto
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Si hay un error, lo mostramos en rojo
        if (uiState is AuthState.Error) {
            Text(
                text = (uiState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón principal
        Button(
            onClick = {
                // 1. Limpiamos los espacios en blanco invisibles al inicio y al final
                val emailLimpio = email.trim()
                val contrasenaLimpia = contrasena.trim()
                val nombreLimpio = nombre.trim()

                // 2. Guardamos el email limpio en las preferencias
                sharedPreferences.edit().putString("email_guardado", emailLimpio).apply()

                if (isLoginMode) {
                    viewModel.login(emailLimpio, contrasenaLimpia)
                } else {
                    viewModel.registrar(emailLimpio, contrasenaLimpia, nombreLimpio, rol)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = uiState !is AuthState.Loading // Deshabilita el botón si está cargando
        ) {
            if (uiState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isLoginMode) "Entrar" else "Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para alternar entre Login y Registro
        TextButton(
            onClick = {
                isLoginMode = !isLoginMode
                viewModel.resetState() // Limpiamos errores si cambia de modo
            }
        ) {
            Text(if (isLoginMode) "¿No tienes cuenta? Regístrate aquí" else "¿Ya tienes cuenta? Inicia sesión")
        }
    }
}