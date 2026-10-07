package com.example.data.supabase

import com.example.BuildConfig

object SupabaseConfig {
    /**
     * Resolves the configured Supabase Project URL from BuildConfig (.env / Secrets panel).
     */
    val url: String
        get() {
            return try {
                val value = BuildConfig.SUPABASE_URL
                if (value.isBlank() || value.contains("placeholder") || value.contains("your-project")) {
                    ""
                } else {
                    value.trim()
                }
            } catch (e: Throwable) {
                ""
            }
        }

    /**
     * Resolves the configured Supabase Anon Key from BuildConfig (.env / Secrets panel).
     */
    val anonKey: String
        get() {
            return try {
                val value = BuildConfig.SUPABASE_ANON_KEY
                if (value.isBlank() || value.contains("placeholder")) {
                    ""
                } else {
                    value.trim()
                }
            } catch (e: Throwable) {
                ""
            }
        }

    /**
     * Returns true if both the URL and Anon Key are configured and non-placeholder.
     */
    val isConfigured: Boolean
        get() = url.isNotBlank() && anonKey.isNotBlank()
}
