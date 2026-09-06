package com.ptitsartistes.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ptitsartistes.app.LocalAppContainer
import com.ptitsartistes.app.ui.components.ErrorBanner
import com.ptitsartistes.app.ui.components.ImagePickerBox
import com.ptitsartistes.app.ui.components.PrimaryButton
import com.ptitsartistes.app.ui.components.rememberImagePickerLauncher
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnfantFormScreen(enfantId: Int?, onSaved: () -> Unit, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel: EnfantFormViewModel = viewModel(
        factory = viewModelFactory { initializer { EnfantFormViewModel(container.enfantRepository, enfantId) } },
    )

    var showDatePicker by remember { mutableStateOf(false) }
    val pickImage = rememberImagePickerLauncher { uri -> viewModel.avatarUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Modifier le profil" else "Ajouter un enfant") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ImagePickerBox(
                previewModel = viewModel.avatarUri ?: viewModel.existingAvatarUrl,
                onClick = pickImage,
                placeholderEmoji = "🧒",
                placeholderText = "Choisir un avatar (optionnel)",
            )

            OutlinedTextField(
                value = viewModel.prenom,
                onValueChange = { viewModel.prenom = it },
                label = { Text("Prénom") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = viewModel.nom,
                onValueChange = { viewModel.nom = it },
                label = { Text("Nom (optionnel)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(if (viewModel.dateNaissance.isBlank()) "Date de naissance (optionnel)" else viewModel.dateNaissance)
            }

            if (viewModel.isEditing && viewModel.pinCode != null) {
                Text(
                    text = "Code PIN actuel : ${viewModel.pinCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (!viewModel.isEditing) {
                Text(
                    text = "Un code PIN à 4 chiffres sera généré automatiquement.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            viewModel.errorMessage?.let { ErrorBanner(message = it) }

            PrimaryButton(
                text = if (viewModel.isEditing) "Enregistrer" else "Créer le profil",
                loading = viewModel.isSaving,
                onClick = { viewModel.save(context, onSaved) },
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.dateNaissance = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
