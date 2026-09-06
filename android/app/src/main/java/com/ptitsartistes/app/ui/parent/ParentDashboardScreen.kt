package com.ptitsartistes.app.ui.parent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ptitsartistes.app.LocalAppContainer
import com.ptitsartistes.app.ui.components.DessinGrid
import com.ptitsartistes.app.ui.components.EmptyState
import com.ptitsartistes.app.ui.components.EnfantAvatar
import com.ptitsartistes.app.ui.components.ErrorBanner
import com.ptitsartistes.app.ui.components.LightboxDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onAddEnfant: () -> Unit,
    onEditEnfant: (Int) -> Unit,
    onAddDessin: (enfantId: Int?) -> Unit,
    onLoggedOut: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: ParentDashboardViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ParentDashboardViewModel(
                    container.enfantRepository,
                    container.dessinRepository,
                    container.authRepository,
                )
            }
        },
    )

    var menuExpanded by remember { mutableStateOf(false) }
    var selectedDessinId by remember { mutableStateOf<Int?>(null) }
    val selectedDessin = viewModel.dessins.find { it.id == selectedDessinId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon espace") },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLoggedOut) }) {
                        Icon(Icons.Default.Logout, contentDescription = "Déconnexion")
                    }
                },
            )
        },
        floatingActionButton = {
            Box {
                ExtendedFloatingActionButton(
                    text = { Text("Ajouter") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { menuExpanded = true },
                )
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Un enfant") },
                        onClick = { menuExpanded = false; onAddEnfant() },
                    )
                    DropdownMenuItem(
                        text = { Text("Un dessin") },
                        onClick = { menuExpanded = false; onAddDessin(viewModel.selectedEnfantId) },
                    )
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            viewModel.errorMessage?.let {
                ErrorBanner(message = it, modifier = Modifier.padding(12.dp))
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    EnfantFilterChip(
                        label = "Tous",
                        selected = viewModel.selectedEnfantId == null,
                        onClick = { viewModel.selectEnfant(null) },
                    )
                }
                items(viewModel.enfants, key = { it.id }) { enfant ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            EnfantAvatar(
                                prenom = enfant.prenom,
                                avatarUrl = enfant.avatar,
                                size = 56.dp,
                                borderColor = if (viewModel.selectedEnfantId == enfant.id) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                },
                                modifier = Modifier.clickable { viewModel.selectEnfant(enfant.id) },
                            )
                            IconButton(
                                onClick = { onEditEnfant(enfant.id) },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(22.dp),
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Modifier ${enfant.prenom}",
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Text(enfant.prenom, style = MaterialTheme.typography.bodyMedium)
                        enfant.pinCode?.let {
                            Text("PIN $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    viewModel.isLoading && viewModel.dessins.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    viewModel.dessins.isEmpty() -> {
                        EmptyState(emoji = "🖼️", message = "Aucun dessin dans cette galerie pour l'instant.")
                    }
                    else -> {
                        DessinGrid(
                            dessins = viewModel.dessins,
                            onDessinClick = { selectedDessinId = it.id },
                            modifier = Modifier.padding(horizontal = 12.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnfantFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
