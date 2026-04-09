package dev.playground.codexcompanion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.playground.codexcompanion.ui.AppViewModel
import dev.playground.codexcompanion.ui.AppViewModelFactory
import dev.playground.codexcompanion.ui.CodexCompanionApp
import dev.playground.codexcompanion.ui.theme.CodexCompanionTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels {
        AppViewModelFactory((application as CompanionApplication).appContainer.store)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            CodexCompanionTheme(themeMode = uiState.value.snapshot.settings.theme) {
                CodexCompanionApp(
                    viewModel = viewModel,
                    uiState = uiState.value,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        viewModel.handleIncomingIntent(intent)
    }
}
