package com.firstexample.gestordetareas.domain.repository

import com.firstexample.gestordetareas.domain.model.Tarea

/**
 * Esta interfaz define las reglas. Le dice a la aplicación qué operaciones
 * existen para las Tareas, sin importar si vienen de Supabase, Firebase o de internet.
 */
interface TareaRepository {
    // Usamos 'suspend' porque estas funciones se conectan a internet
    // y deben ejecutarse en un hilo secundario (Corrutinas) para no trabar la pantalla.

    suspend fun obtenerTodasLasTareas(): List<Tarea>

    suspend fun crearTarea(tarea: Tarea)

    suspend fun eliminarTarea(id: Long)

    suspend fun actualizarTarea(tarea: Tarea)

    suspend fun registrarCumplimiento(registro: com.firstexample.gestordetareas.domain.model.RegistroCumplimiento)

    //suspend fun obtenerTareasCompletadasHoy(usuarioId: String, fecha: String): List<Long>
    suspend fun obtenerHistorialUsuario(usuarioId: String): List<com.firstexample.gestordetareas.domain.model.RegistroCumplimiento>

    // NUEVA FUNCIÓN: Traer todo el historial de la casa

    suspend fun obtenerTodoElHistorial(): List<com.firstexample.gestordetareas.domain.model.RegistroCumplimiento>
}