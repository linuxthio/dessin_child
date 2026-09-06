package com.ptitsartistes.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.ptitsartistes.app.data.remote.dto.DessinDto

/**
 * Visionneuse plein écran d'un dessin. La notation et le like ne sont
 * actifs que si les callbacks correspondants sont fournis (sur l'écran
 * d'accueil, avant connexion, [onRate]/[onToggleLike]/[onDelete] valent
 * `null` et l'affichage reste en lecture seule).
 * La suppression est toujours protégée par une confirmation.
 */
@Composable
fun LightboxDialog(
    dessin: DessinDto,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onRate: ((Int) -> Unit)? = null,
    onToggleLike: (() -> Unit)? = null,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f)),
        ) {
            AsyncImage(
                model = dessin.image,
                contentDescription = dessin.titre,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentScale = ContentScale.Fit,
            )

            Row(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                IconButton(onClick = { onToggleLike?.invoke() }, enabled = onToggleLike != null) {
                    Icon(
                        imageVector = if (dessin.aime) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (dessin.aime) "Coup de cœur" else "Ajouter en coup de cœur",
                        tint = if (dessin.aime) Color(0xFFFF6B81) else Color.White,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = dessin.titre,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                dessin.enfantPrenom?.let {
                    Text(text = it, color = Color.White.copy(alpha = 0.8f))
                }

                Spacer(modifier = Modifier.height(10.dp))
                StarRating(
                    note = dessin.note,
                    onRate = onRate,
                )

                if (onDelete != null) {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF8A8A))
                        Text(" Supprimer", color = Color(0xFFFF8A8A))
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        ConfirmDialog(
            title = "Supprimer ce dessin ?",
            message = "\"${dessin.titre}\" sera définitivement supprimé. Cette action est irréversible.",
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}
