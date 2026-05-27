package com.harekrishna.data.remote

import com.harekrishna.data.local.SessionPrefs
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object ApiClient {
    // Local-dev base URL. To make this work on a physical phone, set up an
    // ADB port-forward once:  `adb reverse tcp:3001 tcp:3001`
    // For production, swap to "https://hare-krishna-tournament.vercel.app/api/".
    private const val BASE_URL = "http://localhost:3001/api/"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls     = false
    }

    fun create(sessionPrefs: SessionPrefs): ApiService {
        val client = OkHttpClient.Builder()
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
