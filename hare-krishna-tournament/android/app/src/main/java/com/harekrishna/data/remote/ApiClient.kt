package com.harekrishna.data.remote

import com.harekrishna.data.local.SessionPrefs
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {
    // Production API. For local dev, swap to "http://localhost:3001/api/" and
    // bridge a physical phone with `adb reverse tcp:3001 tcp:3001`.
    private const val BASE_URL = "https://hare-krishna-tournament.vercel.app/api/"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls     = false
    }

    fun create(sessionPrefs: SessionPrefs): ApiService {
        val client = OkHttpClient.Builder()
            // Vercel cold-starts + a long sync payload can take ~10–20s; the
            // default 10s socket timeout was bailing on every /api/naam call.
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout   (30, TimeUnit.SECONDS)
            .writeTimeout  (30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor(sessionPrefs))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    // Injects `Authorization: Bearer <token>` when we have one, and clears the
    // stored session on a 401 so the navigation observer kicks the user back
    // to the login screen.
    private fun authInterceptor(sessionPrefs: SessionPrefs) = Interceptor { chain ->
        val token = sessionPrefs.read()?.token
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        val response = chain.proceed(request)
        if (response.code == 401 && token != null) {
            sessionPrefs.clear()
        }
        response
    }
}
