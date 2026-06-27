package com.firstexample.gestordetareas.di

//import com.firstexample.gestordetareas.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Módulo de Inyección de Dependencias manual para Supabase.
 * Utilizamos un 'object' para garantizar que solo exista una instancia (Singleton)
 * del cliente de Supabase en toda la aplicación.
 */
object SupabaseModule {

    // 'by lazy' significa que el cliente solo se creará la primera vez que se necesite
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://nkgkjsphisdunctjufkm.supabase.co",
            supabaseKey = "sb_publishable_rO3B07U8xv6-EdHSw-K6mA_yu1Rp0jh"
            //supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            // Instalamos el módulo de base de datos (PostgreSQL)
            install(Postgrest)
            // Instalamos el módulo de Autenticación
            install(Auth)
        }
    }
}