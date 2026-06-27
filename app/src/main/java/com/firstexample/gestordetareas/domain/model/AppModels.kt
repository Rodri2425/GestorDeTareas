package com.firstexample.gestordetareas.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representa la tabla 'perfiles' en Supabase
 */
@Serializable
data class Perfil(
    @SerialName("id") val id: String, // Supabase usa UUID, en Kotlin lo manejamos como String
    @SerialName("nombre") val nombre: String,
    @SerialName("rol") val rol: String, // 'admin' o 'usuario'
    @SerialName("creado_en") val creadoEn: String? = null
)

/**
 * Representa la tabla 'tareas' en Supabase (La definición de la actividad)
 */
@Serializable
data class Tarea(
    @SerialName("id") val id: Long = 0, // 0 por defecto para cuando creemos nuevas tareas
    @SerialName("titulo") val titulo: String,
    @SerialName("descripcion") val descripcion: String? = null,
    @SerialName("tipo_frecuencia") val tipoFrecuencia: String, // 'una_vez', 'diaria', 'cada_x_dias'
    @SerialName("intervalo_dias") val intervaloDias: Int = 1,
    @SerialName("fecha_inicio") val fechaInicio: String, // Formato "YYYY-MM-DD"
    @SerialName("creado_por") val creadoPor: String? = null,
    @SerialName("creado_en") val creadoEn: String? = null
)

/**
 * Representa la tabla 'asignaciones' (Qué usuario hace qué tarea)
 */
@Serializable
data class Asignacion(
    @SerialName("id") val id: Long = 0,
    @SerialName("tarea_id") val tareaId: Long,
    @SerialName("usuario_id") val usuarioId: String
)

/**
 * Representa la tabla 'registro_cumplimiento' (El estado real día a día)
 */
@Serializable
data class RegistroCumplimiento(
    @SerialName("id") val id: Long = 0,
    @SerialName("tarea_id") val tareaId: Long,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("fecha") val fecha: String, // Formato "YYYY-MM-DD"
    @SerialName("completada") val completada: Boolean = false,
    @SerialName("actualizado_en") val actualizadoEn: String? = null
)