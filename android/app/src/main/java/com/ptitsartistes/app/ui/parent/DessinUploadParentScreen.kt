package com.ptitsartistes.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DessinUploadParentScreen(preselectedEnfantId: Int?, onSaved: () -> Unit, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel: DessinUploadParentViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                DessinUploadParentViewModel(container.enfantRepository, container.dessinRepository, preselectedEnfantId)
            }
        },
    )

    var enfantMenuExpanded by remember { mutableStateOf(false) }
    val pickImage = rememberImagePickerLauncher { uri -> viewModel.imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un dessin") },
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
            ImagePickerBox(previewModel = viewModel.imageUri, onClick = pickImage)

            val selectedEnfant = viewModel.enfants.find { it.id == viewModel.selectedEnfantId }
            ExposedDropdownMenuBox(
                expanded = enfantMenuExpanded,
                onExpandedChange = { enfantMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = selectedEnfant?.prenom ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Enfant") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = enfantMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = enfantMenuExpanded,
                    onDismissRequest = { enfantMenuExpanded = false },
                ) {
                    viewModel.enfants.forEach { enfant ->
                        DropdownMenuItem(
                            text = { Text(enfant.prenom) },
                            onClick = {
                                viewModel.selectedEnfantId = enfant.id
                                enfantMenuExpanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.titre,
                onValueChange = { viewModel.titre = it },
                label = { Text("Titre du dessin") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = viewModel.dateCreation,
                onValueChange = { viewModel.dateCreation = it },
                label = { Text("Date (AAAA-MM-JJ)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text("Description (optionnel)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            viewModel.errorMessage?.let { ErrorBanner(message = it) }

            PrimaryButton(
                text = "Ajouter à la galerie",
                loading = viewModel.isSaving,
                onClick = { viewModel.save(context, onSaved) },
            )
        }
    }
}
