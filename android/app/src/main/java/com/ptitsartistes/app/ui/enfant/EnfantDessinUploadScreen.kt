package com.ptitsartistes.app.ui.enfant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ptitsartistes.app.LocalAppContainer
import com.ptitsartistes.app.ui.components.ErrorBanner
import com.ptitsartistes.app.ui.components.ImagePickerBox
import com.ptitsartistes.app.ui.components.PrimaryButton
import com.ptitsartistes.app.ui.components.rememberImagePickerLauncher
import androidx.compose.ui.platform.LocalContext

@Composable
fun EnfantDessinUploadScreen(onSaved: () -> Unit, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel: EnfantDessinUploadViewModel = viewModel(
        factory = viewModelFactory { initializer { EnfantDessinUploadViewModel(container.dessinRepository) } },
    )
    val pickImage = rememberImagePickerLauncher { uri -> viewModel.imageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "Ajoute ton dessin ! 🖍️",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )

        OutlinedTextField(
            value = viewModel.titre,
            onValueChange = { viewModel.titre = it },
            placeholder = { Text("Donne un titre à ton dessin !") },
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = MaterialTheme.shapes.large,
        )

        ImagePickerBox(
            previewModel = viewModel.imageUri,
            onClick = pickImage,
            placeholderEmoji = "📷",
            placeholderText = "Choisis ta photo ou ton scan",
        )

        viewModel.errorMessage?.let { ErrorBanner(message = it) }

        PrimaryButton(
            text = "✅ Envoyer mon dessin",
            loading = viewModel.isSaving,
            containerColor = MaterialTheme.colorScheme.secondary,
            onClick = { viewModel.save(context, onSaved) },
        )

        TextButton(onClick = onBack) {
            Text("Retour à ma galerie")
        }
    }
}
