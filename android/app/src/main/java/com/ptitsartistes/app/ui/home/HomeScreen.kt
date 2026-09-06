package com.ptitsartistes.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.compose.AsyncImage
import com.ptitsartistes.app.LocalAppContainer
import com.ptitsartistes.app.data.remote.dto.DessinDto
import com.ptitsartistes.app.ui.components.LightboxDialog
import com.ptitsartistes.app.ui.components.PrimaryButton
import com.ptitsartistes.app.ui.components.StarRating
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun formatPublicationDate(isoDateTime: String): String = runCatching {
    OffsetDateTime.parse(isoDateTime).format(displayDateFormatter)
}.getOrDefault(isoDateTime)

@Composable
fun HomeScreen(
    onEnfantClick: () -> Unit,
    onParentClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { initializer { HomeViewModel(container.dessinRepository) } },
    )
    var selectedDessinId by remember { mutableStateOf<Int?>(null) }
    val selectedDessin = viewModel.topDessins.find { it.id == selectedDessinId }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Paramètres du serveur")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "🎨", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        text = "P'tits Artistes",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Toute la créativité de vos enfants, au même endroit",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                    )

                    PrimaryButton(
                        text = "👧🧒 Je suis un enfant",
                        onClick = onEnfantClick,
                        containerColor = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(
                        text = "👨‍👩‍👧 Je suis un parent",
                        onClick = onParentClick,
                    )
                }
            }

            if (viewModel.topDessins.isNotEmpty()) {
                item {
                    Text(
                        text = "🏆 Les 5 meilleurs dessins",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 40.dp, bottom = 14.dp),
                    )
                }
                items(viewModel.topDessins, key = { it.id }) { dessin ->
                    TopDessinCard(
                        dessin = dessin,
                        onClick = { selectedDessinId = dessin.id },
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
            }
        }
    }

    selectedDessin?.let { dessin ->
        LightboxDialog(dessin = dessin, onDismiss = { selectedDessinId = null })
    }
}

@Composable
private fun TopDessinCard(dessin: DessinDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = dessin.image,
            contentDescription = dessin.titre,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = dessin.titre,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            dessin.enfantPrenom?.let {
                Text(
                    text = "Par $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Publié le ${formatPublicationDate(dessin.createdAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StarRating(note = dessin.note, starSize = 16.dp, modifier = Modifier.padding(top = 4.dp))
        }
        if (dessin.aime) {
            Icon(Icons.Default.Favorite, contentDescription = "Coup de cœur", tint = Color(0xFFFF6B81))
        }
    }
}
