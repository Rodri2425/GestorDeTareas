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

data class DetalleHistorial(
    val nombreUsuario: String,
    val tituloTarea: String,
    val fecha: String
)

class TareaViewModel(
    private val repository: TareaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas.asStateFlow()

    private val _perfil = MutableStateFlow<Perfil?>(null)
    val perfil: StateFlow<Perfil?> = _perfil.asStateFlow()

    // LA VARIABLE MAGICA: Guarda los IDs de las tareas que NO se deben mostrar al usuario hoy
    private val _tareasOcultas = MutableStateFlow<List<Long>>(emptyList())
    val tareasOcultas: StateFlow<List<Long>> = _tareasOcultas.asStateFlow()

    private val _historial = MutableStateFlow<List<DetalleHistorial>>(emptyList())
    val historial: StateFlow<List<DetalleHistorial>> = _historial.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            try {
                val userId = authRepository.obtenerUsuarioActualId()
                if (userId != null) {
                    val perfilUsuario = authRepository.obtenerPerfilUsuario(userId)
                    _perfil.value = perfilUsuario

                    // Calculamos qué ocultarle al entrar
                    actualizarTareasOcultas(userId)
                }

                val lista = repository.obtenerTodasLasTareas()
                _tareas.value = lista
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // EL MOTOR DE TIEMPO Y FRECUENCIAS
    private fun actualizarTareasOcultas(userId: String) {
        viewModelScope.launch {
            try {
                // 1. Traemos TODAS las veces que este usuario ha hecho tareas
                val historial = repository.obtenerHistorialUsuario(userId)
                val tareasActivas = repository.obtenerTodasLasTareas()

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaHoy = Date()
                val hoyStr = sdf.format(fechaHoy)

                // 2. Buscamos cuál fue la ÚLTIMA VEZ (fecha más reciente) que hizo cada tarea
                val ultimasCompletadas = mutableMapOf<Long, Date>()
                for (reg in historial) {
                    try {
                        val fechaReg = sdf.parse(reg.fecha)
                        val existente = ultimasCompletadas[reg.tareaId]
                        if (fechaReg != null && (existente == null || fechaReg.after(existente))) {
                            ultimasCompletadas[reg.tareaId] = fechaReg
                        }
                    } catch (e: Exception) {}
                }

                // 3. Evaluamos las reglas de frecuencia
                val ocultas = mutableListOf<Long>()
                for (tarea in tareasActivas) {
                    val ultimaVez = ultimasCompletadas[tarea.id]

                    if (ultimaVez != null) {
                        when (tarea.tipoFrecuencia) {
                            "una_vez" -> {
                                // Si ya la hizo en la vida, se oculta para siempre
                                ocultas.add(tarea.id)
                            }
                            "diaria" -> {
                                // Se oculta solo si la hizo HOY
                                if (sdf.format(ultimaVez) == hoyStr) {
                                    ocultas.add(tarea.id)
                                }
                            }
                            "semanal" -> {
                                // Se oculta si han pasado menos de 7 días
                                val diffInMillies = fechaHoy.time - ultimaVez.time
                                val diffInDays = diffInMillies / (1000 * 60 * 60 * 24)
                                if (diffInDays < 7) {
                                    ocultas.add(tarea.id)
                                }
                            }
                        }
                    }
                }

                // 4. Actualizamos la lista de tareas que no se deben mostrar
                _tareasOcultas.value = ocultas
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
                e.printStackTrace()
            }
        }
    }

    fun cargarHistorialCompleto() {
        viewModelScope.launch {
            try {
                val registros = repository.obtenerTodoElHistorial()
                val perfiles = authRepository.obtenerTodosLosPerfiles()
                val tareas = repository.obtenerTodasLasTareas()

                val detalles = registros.map { registro ->
                    val nombre = perfiles.find { it.id == registro.usuarioId }?.nombre ?: "Usuario Borrado"
                    val titulo = tareas.find { it.id == registro.tareaId }?.titulo ?: "Tarea Eliminada"
                    DetalleHistorial(nombre, titulo, registro.fecha)
                }.reversed()

                _historial.value = detalles
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun crearNuevaTarea(titulo: String, descripcion: String, tipoFrecuencia: String) {
        viewModelScope.launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaHoy = sdf.format(Date())

                val nuevaTarea = Tarea(
                    titulo = titulo,
                    descripcion = descripcion.ifBlank { null },
                    tipoFrecuencia = tipoFrecuencia,
                    fechaInicio = fechaHoy
                )

                repository.crearTarea(nuevaTarea)
                cargarTareas()

                val userId = _perfil.value?.id
                if (userId != null) actualizarTareasOcultas(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarTareaExistente(tarea: Tarea, nuevoTitulo: String, nuevaDescripcion: String, nuevaFrecuencia: String) {
        viewModelScope.launch {
            try {
                val tareaActualizada = tarea.copy(
                    titulo = nuevoTitulo,
                    descripcion = nuevaDescripcion.ifBlank { null },
                    tipoFrecuencia = nuevaFrecuencia
                )
                repository.actualizarTarea(tareaActualizada)
                cargarTareas()

                val userId = _perfil.value?.id
                if (userId != null) actualizarTareasOcultas(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarTarea(id: Long) {
        viewModelScope.launch {
            try {
                repository.eliminarTarea(id)
                cargarTareas()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun marcarTareaComoCompletada(tareaId: Long) {
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

                repository.registrarCumplimiento(registro)

                // Actualizamos las tareas ocultas aplicando nuestra nueva lógica de tiempo
                actualizarTareasOcultas(userId)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cerrarSesion(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.cerrarSesion()
            onSuccess()
        }
    }
}

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































/*
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

data class DetalleHistorial(
    val nombreUsuario: String,
    val tituloTarea: String,
    val fecha: String
)
class TareaViewModel(
    private val repository: TareaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas.asStateFlow()

    private val _perfil = MutableStateFlow<Perfil?>(null)
    val perfil: StateFlow<Perfil?> = _perfil.asStateFlow()

    private val _tareasCompletadasHoy = MutableStateFlow<List<Long>>(emptyList())
    val tareasCompletadasHoy: StateFlow<List<Long>> = _tareasCompletadasHoy.asStateFlow()

    private val _historial = MutableStateFlow<List<DetalleHistorial>>(emptyList())
    val historial: StateFlow<List<DetalleHistorial>> = _historial.asStateFlow()


    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            try {
                val userId = authRepository.obtenerUsuarioActualId()
                if (userId != null) {
                    val perfilUsuario = authRepository.obtenerPerfilUsuario(userId)
                    _perfil.value = perfilUsuario
                    cargarTareasCompletadas(userId)
                }
                cargarTareas()
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
                e.printStackTrace()
            }
        }
    }

    fun cargarHistorialCompleto() {
        viewModelScope.launch {
            try {
                val registros = repository.obtenerTodoElHistorial()
                val perfiles = authRepository.obtenerTodosLosPerfiles()
                val tareas = repository.obtenerTodasLasTareas()

                // Armamos los detalles legibles
                val detalles = registros.map { registro ->
                    val nombre = perfiles.find { it.id == registro.usuarioId }?.nombre ?: "Usuario Borrado"
                    val titulo = tareas.find { it.id == registro.tareaId }?.titulo ?: "Tarea Eliminada"
                    DetalleHistorial(nombre, titulo, registro.fecha)
                }.reversed() // .reversed() para que las más recientes salgan arriba

                _historial.value = detalles
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // CREAR TAREA (Aseguramos que guarde la frecuencia correcta)
    fun crearNuevaTarea(titulo: String, descripcion: String, tipoFrecuencia: String) {
        viewModelScope.launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaHoy = sdf.format(Date())

                val nuevaTarea = Tarea(
                    titulo = titulo,
                    descripcion = descripcion.ifBlank { null },
                    tipoFrecuencia = tipoFrecuencia, // <-- Aquí guardamos la frecuencia seleccionada
                    fechaInicio = fechaHoy
                )

                repository.crearTarea(nuevaTarea)
                cargarTareas()
            } catch (e: Exception) {
                android.util.Log.e("ERROR_TAREAS", "Fallo al crear: ${e.message}")
            }
        }
    }

    // EDITAR TAREA (Aseguramos que guarde los cambios)
    fun actualizarTareaExistente(tarea: Tarea, nuevoTitulo: String, nuevaDescripcion: String, nuevaFrecuencia: String) {
        viewModelScope.launch {
            try {
                val tareaActualizada = tarea.copy(
                    titulo = nuevoTitulo,
                    descripcion = nuevaDescripcion.ifBlank { null },
                    tipoFrecuencia = nuevaFrecuencia // <-- Aquí actualizamos la frecuencia
                )
                repository.actualizarTarea(tareaActualizada)
                cargarTareas()
            } catch (e: Exception) {
                android.util.Log.e("ERROR_TAREAS", "Fallo al actualizar: ${e.message}")
            }
        }
    }

    fun eliminarTarea(id: Long) {
        viewModelScope.launch {
            try {
                repository.eliminarTarea(id)
                cargarTareas()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun marcarTareaComoCompletada(tareaId: Long) {
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

                repository.registrarCumplimiento(registro)
                cargarTareasCompletadas(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cerrarSesion(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.cerrarSesion()
            onSuccess()
        }
    }
}

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


 */
