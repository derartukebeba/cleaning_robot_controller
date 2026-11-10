package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cleanbot.data.CleanBotRepository
import com.example.cleanbot.ui.MainAppScaffold
import com.example.ui.theme.CleanBotTheme
import com.example.ui.theme.Navy900

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = CleanBotRepository.getInstance()

        setContent {
            CleanBotTheme(darkTheme = true) {
                val state by repository.appState.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Navy900
                ) {
                    MainAppScaffold(
                        state = state,
                        repository = repository
                    )
                }
            }
        }
    }
}
