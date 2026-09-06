package com.ptitsartistes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.ptitsartistes.app.di.AppContainer
import com.ptitsartistes.app.ui.navigation.PtitsArtistesNavGraph
import com.ptitsartistes.app.ui.theme.PtitsArtistesTheme

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer non fourni : LocalAppContainer doit être fourni depuis MainActivity.")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as PtitsArtistesApp).container

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                PtitsArtistesTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        PtitsArtistesNavGraph()
                    }
                }
            }
        }
    }
}
