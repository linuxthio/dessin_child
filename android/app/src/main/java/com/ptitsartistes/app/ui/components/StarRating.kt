package com.ptitsartistes.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Note de 1 à 5 étoiles. En lecture seule si [onRate] vaut `null` (utilisé
 * par exemple sur l'écran d'accueil, avant toute connexion).
 */
@Composable
fun StarRating(
    note: Int?,
    modifier: Modifier = Modifier,
    starSize: androidx.compose.ui.unit.Dp = 28.dp,
    color: Color = Color(0xFFFFC107),
    onRate: ((Int) -> Unit)? = null,
) {
    Row(modifier = modifier) {
        for (star in 1..5) {
            val filled = note != null && star <= note
            Icon(
                imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "$star étoile(s)",
                tint = color,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onRate != null) Modifier.clickable { onRate(star) } else Modifier,
                    ),
            )
        }
    }
}
