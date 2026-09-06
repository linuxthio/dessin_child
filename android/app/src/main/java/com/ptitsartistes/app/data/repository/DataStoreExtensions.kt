package com.ptitsartistes.app.data.repository

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "ptits_artistes_prefs")
