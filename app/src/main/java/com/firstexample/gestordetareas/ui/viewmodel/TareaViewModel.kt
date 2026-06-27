package com.firstexample.gestordetareas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.firstexample.gestordetareas.domain.model.Tarea
import com.firstexample.gestordetareas.domain.repository.TareaRepository
import com.firstexample.gestordetareas.domain.repository.AuthRepository
import com.firstexample.gestordetareas.domain.model.Perfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * El ViewModel es el "puente" entre la interfaz gráfica y la base de datos.
 */
class TareaViewModel(
    private val repository: TareaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Aquí guardamos la lista de tareas. StateFlow avisa a la UI automáticamente cuando hay cambios.
    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas.asStateFlow()

    private val _perfil = MutableStateFlow<Perfil?>(null)
    val perfil: StateFlow<Perfil?> = _perfil.asStateFlow()

    init {
        // Al iniciar esta pantalla, cargamos las tareas automáticamente
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            try {
                // 1. Obtenemos quién es el usuario logueado
                val userId = authRepository.obtenerUsuarioActualId()
                if (userId != null) {
                    // 2. Buscamos su perfil en la base de datos
                    val perfilUsuario = authRepository.obtenerPerfilUsuario(userId)
                    _perfil.value = perfilUsuario
                }

                // 3. Cargamos las tareas
                val lista = repository.obtenerTodasLasTareas()
                _tareas.value = lista
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun cargarTareas() {
        viewModelScope.launch {
            try {
                val lista = repository.obtenerTodasLasTareas()
                _tareas.value = lista
            } catch (e: Exception) {
                e.printStackTrace() // Si hay error, lo imprime en la consola
            }
        }
    }

    // Función temporal para probar que podemos guardar en Supabase
    fun agregarTareaPrueba() {
        viewModelScope.launch {
            try {
                val nuevaTarea = Tarea(
                    titulo = "Nueva Tarea Familia",
                    descripcion = "Generada automáticamente desde la App a las ${System.currentTimeMillis()}",
                    tipoFrecuencia = "una_vez",
                    fechaInicio = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                )
                repository.crearTarea(nuevaTarea)
                cargarTareas() // Recargamos la lista para ver la nueva tarea
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarTarea(id: Long) {
        viewModelScope.launch {
            try {
                repository.eliminarTarea(id)
                cargarTareas() // Actualizamos la lista
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cerrarSesion(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.cerrarSesion()
            onSuccess() // Le avisa a la pantalla que ya terminó para que cambie de vista
        }
    }
}

/**
 * Como nuestro ViewModel necesita el 'TareaRepository' en su constructor,
 * necesitamos esta fábrica (Factory) para enseñarle a Android cómo crearlo.
 */
class TareaViewModelFactory(
    private val repository: TareaRepository,
    private val authRepository: AuthRepository // AÑADIMOS ESTO AQUÍ
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TareaViewModel::class.java)) {
            return TareaViewModel(repository, authRepository) as T // Y AQUÍ
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}