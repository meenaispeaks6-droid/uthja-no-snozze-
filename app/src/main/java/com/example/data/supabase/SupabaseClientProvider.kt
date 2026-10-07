package com.example.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime

object SupabaseClientProvider {
    @Volatile
    private var clientInstance: SupabaseClient? = null

    val isConfigured: Boolean
        get() = SupabaseConfig.isConfigured

    fun getClient(): SupabaseClient? {
        if (!isConfigured) return null
        return clientInstance ?: synchronized(this) {
            clientInstance ?: try {
                createSupabaseClient(
                    supabaseUrl = SupabaseConfig.url,
                    supabaseKey = SupabaseConfig.anonKey
                ) {
                    install(Auth)
                    install(Postgrest)
                    install(Realtime)
                }.also { clientInstance = it }
            } catch (e: Throwable) {
                null
            }
        }
    }

    val auth: Auth?
        get() = getClient()?.auth

    val postgrest: Postgrest?
        get() = getClient()?.postgrest

    val realtime: Realtime?
        get() = getClient()?.realtime
}
