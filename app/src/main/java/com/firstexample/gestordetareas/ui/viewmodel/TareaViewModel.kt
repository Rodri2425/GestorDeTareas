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
import com.firstexample.gestordetareas.domain.model.RegistroCumplimiento

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

    private val _tareasCompletadasHoy = MutableStateFlow<List<Long>>(emptyList())
    val tareasCompletadasHoy: StateFlow<List<Long>> = _tareasCompletadasHoy.asStateFlow()

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
                    cargarTareasCompletadas(userId)
                }

                // 3. Cargamos las tareas
                val lista = repository.obtenerTodasLasTareas()
                _tareas.value = lista
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cargarTareasCompletadas(userId: String) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaHoy = sdf.format(Date())
            val completadas = repository.obtenerTareasCompletadasHoy(userId, fechaHoy)
            _tareasCompletadasHoy.value = completadas
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

    fun crearNuevaTarea(titulo: String, descripcion: String) {
        viewModelScope.launch {
            try {
                // Generamos la fecha de hoy automáticamente
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaHoy = sdf.format(Date())

                val nuevaTarea = Tarea(
                    titulo = titulo,
                    descripcion = descripcion.ifBlank { null }, // Si la dejan vacía, guardamos null
                    tipoFrecuencia = "una_vez", // Por defecto por ahora
                    fechaInicio = fechaHoy
                )

                repository.crearTarea(nuevaTarea)
                cargarTareas() // Recargamos la lista para ver la tarea nueva
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

    fun marcarTareaComoCompletada(tareaId: Long) {
        // Necesitamos saber quién es el usuario actual
        val userId = _perfil.value?.id ?: return

        viewModelScope.launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaHoy = sdf.format(Date())

                val registro = RegistroCumplimiento(
                    tareaId = tareaId,
                    usuarioId = userId,
                    fecha = fechaHoy,
                    completada = true
                )

                // 1. Guardamos el recibo en la base de datos
                repository.registrarCumplimiento(registro)

                // 2. AÑADIMOS ESTO: Recargamos la lista de completadas para que desaparezca la tarea
                cargarTareasCompletadas(userId)

                android.util.Log.d("EXITO", "¡Tarea $tareaId completada por $userId!")

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
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TareaViewModel::class.java)) {
            return TareaViewModel(repository, authRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}