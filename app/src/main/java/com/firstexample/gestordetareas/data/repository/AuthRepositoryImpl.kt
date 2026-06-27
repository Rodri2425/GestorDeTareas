package com.firstexample.gestordetareas.data.repository

import com.firstexample.gestordetareas.domain.model.Perfil
import com.firstexample.gestordetareas.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override suspend fun login(email: String, contrasena: String): Boolean {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                password = contrasena
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("ERROR_SUPABASE", "Fallo al iniciar sesion: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun registrar(email: String, contrasena: String, nombre: String, rol: String): Boolean {
        return try {
            // 1. Registramos en Supabase Auth
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                password = contrasena
            }

            // 2. Obtenemos el ID real del usuario que se acaba de crear y loguear automáticamente
            val uid = supabaseClient.auth.currentUserOrNull()?.id

            if (uid != null) {
                // 3. Guardamos el perfil en la tabla
                val nuevoPerfil = Perfil(id = uid, nombre = nombre, rol = rol)
                supabaseClient.postgrest["perfiles"].insert(nuevoPerfil)
                android.util.Log.d("EXITO_SUPABASE", "Perfil guardado correctamente para: $nombre")
            } else {
                android.util.Log.e("ERROR_SUPABASE", "No se encontró el UID después del registro.")
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("ERROR_SUPABASE", "Fallo al registrar: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun cerrarSesion() {
        try {
            supabaseClient.auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun obtenerUsuarioActualId(): String? {
        // Devuelve el ID del usuario si hay una sesión activa, o null si no la hay
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    override suspend fun obtenerPerfilUsuario(userId: String): Perfil? {
        return try {
            // Usamos decodeList().firstOrNull() que es mucho más seguro para evitar que la app falle
            val listaPerfiles = supabaseClient.postgrest["perfiles"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeList<Perfil>()

            val perfilEncontrado = listaPerfiles.firstOrNull()

            if (perfilEncontrado == null) {
                android.util.Log.e("ERROR_SUPABASE", "El usuario existe, pero NO tiene perfil en la tabla 'perfiles'.")
            }

            perfilEncontrado
        } catch (e: Exception) {
            android.util.Log.e("ERROR_SUPABASE", "Fallo al obtener perfil: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}