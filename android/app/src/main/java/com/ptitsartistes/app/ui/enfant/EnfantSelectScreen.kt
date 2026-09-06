package com.ptitsartistes.app.ui.enfant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.ptitsartistes.app.ui.components.EmptyState
import com.ptitsartistes.app.ui.components.EnfantAvatar
import com.ptitsartistes.app.ui.components.ErrorBanner

@Composable
fun EnfantSelectScreen(
    onEnfantChosen: (id: Int, prenom: String) -> Unit,
    onNavigateToParentLogin: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: EnfantSelectViewModel = viewModel(
        factory = viewModelFactory { initializer { EnfantSelectViewModel(container.enfantRepository) } },
    )

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = "Qui est là ? 👋",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Touche ton prénom pour te connecter !",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp).fillMaxWidth(),
        )

        viewModel.errorMessage?.let { ErrorBanner(message = it) }

        Box(modifier = Modifier.weight(1f)) {
            when {
                viewModel.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                viewModel.enfants.isEmpty() -> EmptyState(
                    emoji = "🙈",
                    message = "Aucun profil enfant n'a encore été créé. Demande à un parent de t'ajouter !",
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(viewModel.enfants, key = { it.id }) { enfant ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onEnfantChosen(enfant.id, enfant.prenom) },
                        ) {
                            EnfantAvatar(prenom = enfant.prenom, avatarUrl = enfant.avatar, size = 88.dp)
                            Text(
                                text = enfant.prenom,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        TextButton(onClick = onNavigateToParentLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Tu es un parent ? Connecte-toi ici")
        }
    }
}
