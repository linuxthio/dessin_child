package com.ptitsartistes.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ptitsartistes.app.LocalAppContainer
import com.ptitsartistes.app.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val currentUrl by container.settingsRepository.baseUrl.collectAsState()
    var urlInput by remember(currentUrl) { mutableStateOf(currentUrl) }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adresse du serveur") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Adresse du serveur Django (avec le port). Sur l'émulateur Android, " +
                    "utilisez 10.0.2.2 pour joindre le localhost de votre ordinateur. " +
                    "Sur un appareil physique, indiquez l'IP locale de votre poste.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = urlInput,
                onValueChange = {
                    urlInput = it
                    saved = false
                },
                label = { Text("URL de base") },
                placeholder = { Text("http://10.0.2.2:8000/") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (saved) {
                Text(
                    text = "Adresse enregistrée !",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            PrimaryButton(
                text = "Enregistrer",
                onClick = {
                    container.settingsRepository.setBaseUrl(urlInput.trim())
                    saved = true
                },
            )
        }
    }
}
