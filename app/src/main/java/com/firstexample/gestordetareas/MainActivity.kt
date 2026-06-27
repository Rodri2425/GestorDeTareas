package com.firstexample.gestordetareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.firstexample.gestordetareas.data.repository.AuthRepositoryImpl
import com.firstexample.gestordetareas.data.repository.TareaRepositoryImpl
import com.firstexample.gestordetareas.di.SupabaseModule
import com.firstexample.gestordetareas.ui.screens.admin.AdminScreen
import com.firstexample.gestordetareas.ui.screens.auth.AuthScreen
import com.firstexample.gestordetareas.ui.viewmodel.AuthViewModel
import com.firstexample.gestordetareas.ui.viewmodel.AuthViewModelFactory
import com.firstexample.gestordetareas.ui.viewmodel.TareaViewModel
import com.firstexample.gestordetareas.ui.viewmodel.TareaViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Instanciamos los repositorios pasándoles nuestra conexión a Supabase
        val authRepository = AuthRepositoryImpl(SupabaseModule.client)
        val tareaRepository = TareaRepositoryImpl(SupabaseModule.client)

        // 2. Preparamos las fábricas para los ViewModels
        val authFactory = AuthViewModelFactory(authRepository)
        val tareaFactory = TareaViewModelFactory(tareaRepository, authRepository)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 3. Creamos el controlador de navegación
                    val navController = rememberNavController()

                    // 4. Definimos el "Mapa" de nuestras pantallas (NavHost)
                    // startDestination indica con cuál pantalla arranca la app
                    NavHost(navController = navController, startDestination = "login") {

                        composable("login") {
                            val authViewModel: AuthViewModel = viewModel(factory = authFactory)
                            AuthScreen(
                                viewModel = authViewModel,
                                onAuthSuccess = {
                                    // Cuando el login es exitoso, viajamos al inicio
                                    navController.navigate("admin") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("admin") {
                            val tareaViewModel: TareaViewModel = viewModel(factory = tareaFactory)
                            AdminScreen(
                                viewModel = tareaViewModel,
                                onLogout = {
                                    // Cuando cerramos sesión, volvems al login
                                    navController.navigate("login") {
                                        popUpTo("admin") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}