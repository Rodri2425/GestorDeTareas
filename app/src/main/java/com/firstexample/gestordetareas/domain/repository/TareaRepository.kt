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
}