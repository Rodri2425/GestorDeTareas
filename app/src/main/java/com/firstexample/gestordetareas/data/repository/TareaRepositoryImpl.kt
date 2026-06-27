package com.firstexample.gestordetareas.data.repository

import com.firstexample.gestordetareas.domain.model.Tarea
import com.firstexample.gestordetareas.domain.repository.TareaRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import com.firstexample.gestordetareas.domain.model.RegistroCumplimiento

/**
 * Esta clase es la que hace el trabajo sucio. Se conecta a Supabase
 * y ejecuta las operaciones definidas en la interfaz.
 */
class TareaRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : TareaRepository {

    override suspend fun obtenerTodasLasTareas(): List<Tarea> {
        // Se conecta a la tabla 'tareas', hace un SELECT y convierte el JSON a una lista de Kotlin
        return supabaseClient.postgrest["tareas"]
            .select()
            .decodeList<Tarea>()
    }

    override suspend fun crearTarea(tarea: Tarea) {
        // Inserta el objeto tarea directamente en la base de datos
        supabaseClient.postgrest["tareas"]
            .insert(tarea)
    }

    override suspend fun eliminarTarea(id: Long) {
        // Borra la tarea que coincida con el ID proporcionado
        supabaseClient.postgrest["tareas"].delete {
            filter {
                eq("id", id) // Le decimos: "Borra donde la columna 'id' sea igual a esta variable id"
            }
        }
    }
    override suspend fun registrarCumplimiento(registro: RegistroCumplimiento) {
        supabaseClient.postgrest["registro_cumplimiento"].insert(registro)
    }


    override suspend fun obtenerTareasCompletadasHoy(usuarioId: String, fecha: String): List<Long> {
        return try {
            val registros = supabaseClient.postgrest["registro_cumplimiento"]
                .select {
                    filter {
                        eq("usuario_id", usuarioId)
                        eq("fecha", fecha)
                    }
                }.decodeList<RegistroCumplimiento>()

            // Devolvemos solo la lista de los IDs de las tareas (ej. [1, 5, 8])
            registros.map { it.tareaId }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}