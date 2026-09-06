package com.ptitsartistes.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Adresse du serveur Django, modifiable dans les Paramètres.
 * Sur l'émulateur Android, `10.0.2.2` pointe vers `localhost` de la machine
 * hôte. Sur un appareil physique, utilisez l'IP locale du poste qui héberge
 * `python manage.py runserver 0.0.0.0:8000` (les deux doivent être sur le
 * même réseau Wi-Fi).
 */
class SettingsRepository(private val context: Context, private val scope: CoroutineScope) {

    private object Keys {
        val BASE_URL = stringPreferencesKey("base_url")
    }

    private val baseUrlFlowFromDisk = context.dataStore.data.map { prefs ->
        prefs[Keys.BASE_URL] ?: DEFAULT_BASE_URL
    }

    /** Toujours à jour de façon synchrone, utilisé par l'intercepteur réseau. */
    val baseUrl: StateFlow<String> = baseUrlFlowFromDisk.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = DEFAULT_BASE_URL,
    )

    fun setBaseUrl(url: String) {
        val cleaned = if (url.endsWith("/")) url else "$url/"
        scope.launch(Dispatchers.IO) {
            context.dataStore.edit { it[Keys.BASE_URL] = cleaned }
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
    }
}
