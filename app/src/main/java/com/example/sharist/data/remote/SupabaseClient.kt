package com.example.sharist.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import com.example.sharist.BuildConfig
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

        val isConfigured: Boolean
            get() = BuildConfig.SUPABASE_URL.isNotBlank() &&
                    BuildConfig.SUPABASE_KEY.isNotBlank()

        val client by lazy {
            if (!isConfigured) {
                null
            } else {
                createSupabaseClient(
                    supabaseUrl = BuildConfig.SUPABASE_URL,
                    supabaseKey = BuildConfig.SUPABASE_KEY
                ) {
                    install(Postgrest)
                    install(Auth)
                    install(Storage)
                }
            }
        }
}