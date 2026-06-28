package com.firstexample.gestordetareas.domain.repository

interface AuthRepository {
    suspend fun login(email: String, contrasena: String): Boolean
    suspend fun registrar(email: String, contrasena: String, nombre: String, rol: String): Boolean
    suspend fun cerrarSesion()
    fun obtenerUsuarioActualId(): String?


    suspend fun obtenerPerfilUsuario(userId: String): com.firstexample.gestordetareas.domain.model.Perfil?

    suspend fun obtenerTodosLosPerfiles(): List<com.firstexample.gestordetareas.domain.model.Perfil>
}
