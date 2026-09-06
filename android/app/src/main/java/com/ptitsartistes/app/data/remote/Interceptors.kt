package com.ptitsartistes.app.data.remote

import com.ptitsartistes.app.data.repository.SessionRepository
import com.ptitsartistes.app.data.repository.SettingsRepository
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Retrofit est configuré avec une base URL factice ("http://localhost/").
 * Cet intercepteur réécrit le schéma/hôte/port de chaque requête avec
 * l'adresse serveur actuellement enregistrée dans les Paramètres, ce qui
 * permet de la changer à tout moment sans reconstruire Retrofit/OkHttp.
 */
class DynamicBaseUrlInterceptor(private val settingsRepository: SettingsRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val configuredBaseUrl = settingsRepository.baseUrl.value.toHttpUrlOrNull()
            ?: return chain.proceed(original)

        val newUrl = original.url.newBuilder()
            .scheme(configuredBaseUrl.scheme)
            .host(configuredBaseUrl.host)
            .port(configuredBaseUrl.port)
            .build()

        return chain.proceed(original.newBuilder().url(newUrl).build())
    }
}

/**
 * Ajoute l'en-tête d'authentification adapté selon le chemin appelé :
 * - `Enfant-Token <clé>` pour les routes `/api/v1/enfant/...` (sauf login),
 * - `Token <clé>` pour les autres routes protégées,
 * - aucun en-tête pour les routes publiques (register, login, enfants/public).
 */
class AuthInterceptor(private val sessionRepository: SessionRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        val isPublic = path.endsWith("/auth/register/") ||
            path.endsWith("/auth/login/") ||
            path.endsWith("/enfants/public/") ||
            path.endsWith("/enfant/login/") ||
            path.endsWith("/dessins/top/")

        if (isPublic) {
            return chain.proceed(request)
        }

        val isEnfantRoute = path.contains("/api/v1/enfant/")

        val headerValue = if (isEnfantRoute) {
            sessionRepository.enfantToken.value?.let { "Enfant-Token $it" }
        } else {
            sessionRepository.parentToken.value?.let { "Token $it" }
        }

        val authedRequest = if (headerValue != null) {
            request.newBuilder().addHeader("Authorization", headerValue).build()
        } else {
            request
        }

        return chain.proceed(authedRequest)
    }
}
