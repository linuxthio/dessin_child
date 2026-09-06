package com.ptitsartistes.app.ui.enfant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.ptitsartistes.app.ui.components.ErrorBanner
import com.ptitsartistes.app.ui.components.PinDots
import com.ptitsartistes.app.ui.components.PinKeypad

@Composable
fun EnfantPinScreen(
    enfantId: Int,
    prenom: String,
    onLoggedIn: () -> Unit,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: EnfantPinViewModel = viewModel(
        factory = viewModelFactory { initializer { EnfantPinViewModel(container.authRepository) } },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = "Salut $prenom !",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Entre ton code secret à 4 chiffres 🔑",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            PinDots(pinLength = viewModel.pin.length)
        }

        viewModel.errorMessage?.let { ErrorBanner(message = it) }

        PinKeypad(
            onDigit = { digit -> viewModel.addDigit(digit, enfantId, onLoggedIn) },
            onBackspace = { viewModel.backspace() },
            modifier = Modifier.fillMaxWidth(),
        )

        TextButton(onClick = onBack) {
            Text("Ce n'est pas moi")
        }
    }
}
