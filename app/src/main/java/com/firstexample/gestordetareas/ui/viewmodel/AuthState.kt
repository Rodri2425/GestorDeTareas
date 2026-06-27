package com.firstexample.gestordetareas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.firstexample.gestordetareas.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Definimos los posibles estados de nuestra pantalla de Login
sealed class AuthState {
    object Idle : AuthState() // Estado inicial (sin hacer nada)
    object Loading : AuthState() // Cargando (mostramos un spinner)
    object Success : AuthState() // ¡Login o registro exitoso!
    data class Error(val message: String) : AuthState() // Ocurrió un error
}

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    fun login(email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            val exito = repository.login(email, contrasena)
            if (exito) {
                _uiState.value = AuthState.Success
            } else {
                _uiState.value = AuthState.Error("Error al iniciar sesión. Revisa tus credenciales.")
            }
        }
    }

    fun registrar(email: String, contrasena: String, nombre: String, rol: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            val exito = repository.registrar(email, contrasena, nombre, rol)
            if (exito) {
                _uiState.value = AuthState.Success
            } else {
                _uiState.value = AuthState.Error("Error al registrar la cuenta.")
            }
        }
    }

    // Función útil para reiniciar el estado si el usuario se equivoca y vuelve a intentar
    fun resetState() {
        _uiState.value = AuthState.Idle
    }
}

// Fábrica para enseñarle a Android cómo crear el AuthViewModel
class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}