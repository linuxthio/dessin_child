package com.ptitsartistes.app.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.ptitsartistes.app.data.remote.ApiService
import com.ptitsartistes.app.data.remote.AuthInterceptor
import com.ptitsartistes.app.data.remote.DynamicBaseUrlInterceptor
import com.ptitsartistes.app.data.repository.AuthRepository
import com.ptitsartistes.app.data.repository.DessinRepository
import com.ptitsartistes.app.data.repository.EnfantRepository
import com.ptitsartistes.app.data.repository.SessionRepository
import com.ptitsartistes.app.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Conteneur d'injection de dépendances manuel (pas de Hilt/Koin) : simple,
 * suffisant pour la taille de l'app, et facile à suivre de bout en bout.
 */
class AppContainer(context: Context) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settingsRepository = SettingsRepository(context.applicationContext, appScope)
    val sessionRepository = SessionRepository(context.applicationContext, appScope)

    private val json = Json { ignoreUnknownKeys = true }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(DynamicBaseUrlInterceptor(settingsRepository))
        .addInterceptor(AuthInterceptor(sessionRepository))
        .addInterceptor(loggingInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        // Base factice : DynamicBaseUrlInterceptor réécrit hôte/port réels.
        .baseUrl("http://192.168.1.131:8000/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    val authRepository = AuthRepository(apiService, sessionRepository)
    val enfantRepository = EnfantRepository(apiService)
    val dessinRepository = DessinRepository(apiService)
}
