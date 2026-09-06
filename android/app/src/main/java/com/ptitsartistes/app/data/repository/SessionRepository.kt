package com.ptitsartistes.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ptitsartistes.app.data.remote.dto.EnfantDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Gère les jetons de session (parent et enfant) et sait qui est
 * actuellement connecté. Les deux jetons sont indépendants : un seul rôle
 * est actif à la fois côté UI, mais rien n'empêche techniquement d'avoir
 * les deux stockés (ex: un parent qui laisse l'app ouverte pour un enfant).
 */
class SessionRepository(private val context: Context, private val scope: CoroutineScope) {

    private object Keys {
        val PARENT_TOKEN = stringPreferencesKey("parent_token")
        val ENFANT_TOKEN = stringPreferencesKey("enfant_token")
        val ENFANT_PROFILE = stringPreferencesKey("enfant_profile")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val parentToken: StateFlow<String?> = context.dataStore.data
        .map { it[Keys.PARENT_TOKEN] }
        .stateIn(scope, SharingStarted.Eagerly, null)

    val enfantToken: StateFlow<String?> = context.dataStore.data
        .map { it[Keys.ENFANT_TOKEN] }
        .stateIn(scope, SharingStarted.Eagerly, null)

    val enfantProfile: StateFlow<EnfantDto?> = context.dataStore.data
        .map { prefs ->
            prefs[Keys.ENFANT_PROFILE]?.let { raw ->
                runCatching { json.decodeFromString<EnfantDto>(raw) }.getOrNull()
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    fun saveParentToken(token: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.edit { it[Keys.PARENT_TOKEN] = token }
        }
    }

    fun clearParentSession() {
        scope.launch(Dispatchers.IO) {
            context.dataStore.edit { it.remove(Keys.PARENT_TOKEN) }
        }
    }

    fun saveEnfantSession(token: String, enfant: EnfantDto) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.edit {
                it[Keys.ENFANT_TOKEN] = token
                it[Keys.ENFANT_PROFILE] = json.encodeToString(enfant)
            }
        }
    }

    fun clearEnfantSession() {
        scope.launch(Dispatchers.IO) {
            context.dataStore.edit {
                it.remove(Keys.ENFANT_TOKEN)
                it.remove(Keys.ENFANT_PROFILE)
            }
        }
    }
}
