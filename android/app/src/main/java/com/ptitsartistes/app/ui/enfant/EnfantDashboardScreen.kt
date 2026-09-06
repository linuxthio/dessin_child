package com.ptitsartistes.app.ui.enfant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ptitsartistes.app.LocalAppContainer
import com.ptitsartistes.app.ui.components.DessinGrid
import com.ptitsartistes.app.ui.components.EmptyState
import com.ptitsartistes.app.ui.components.ErrorBanner
import com.ptitsartistes.app.ui.components.LightboxDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnfantDashboardScreen(
    onAddDessin: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: EnfantDashboardViewModel = viewModel(
        factory = viewModelFactory {
            initializer { EnfantDashboardViewModel(container.dessinRepository, container.authRepository) }
        },
    )
    val enfantProfile by container.authRepository.enfantProfile.collectAsState()
    var selectedDessinId by remember { mutableStateOf<Int?>(null) }
    val selectedDessin = viewModel.dessins.find { it.id == selectedDessinId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎨 Les dessins de ${enfantProfile?.prenom ?: ""}",
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLoggedOut) }) {
                        Icon(Icons.Default.Logout, contentDescription = "Quitter")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nouveau dessin") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onAddDessin,
                containerColor = MaterialTheme.colorScheme.secondary,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            viewModel.errorMessage?.let { ErrorBanner(message = it, modifier = Modifier.padding(12.dp)) }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    viewModel.isLoading && viewModel.dessins.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    viewModel.dessins.isEmpty() -> {
                        EmptyState(
                            emoji = "🖍️",
                            message = "Tu n'as pas encore ajouté de dessin. Vas-y, montre-nous ton talent !",
                        )
                    }
                    else -> {
                        DessinGrid(
                            dessins = viewModel.dessins,
                            onDessinClick = { selectedDessinId = it.id },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }

    selectedDessin?.let { dessin ->
        LightboxDialog(
            dessin = dessin,
            onDismiss = { selectedDessinId = null },
            onDelete = {
                viewModel.deleteDessin(dessin.id)
                selectedDessinId = null
            },
            onRate = { note -> viewModel.rateDessin(dessin.id, note) },
            onToggleLike = { viewModel.toggleLike(dessin.id) },
        )
    }
}
