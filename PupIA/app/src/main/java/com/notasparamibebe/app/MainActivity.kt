package com.notasparamibebe.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.notasparamibebe.app.service.NotificationHelper
import com.notasparamibebe.app.theme.NotasParaMiBebeTheme
import com.notasparamibebe.app.ui.navigation.AppNavigation
import com.notasparamibebe.app.ui.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NotesViewModel by viewModels {
        NotesViewModel.provideFactory(application)
    }

    private var initialRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleNotificationIntent(intent)

        setContent {
            NotasParaMiBebeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        viewModel = viewModel,
                        initialRoute = initialRoute
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val route = intent?.getStringExtra(NotificationHelper.EXTRA_OPEN_ROUTE)
        if (!route.isNullOrBlank()) {
            initialRoute = route
        }
    }
}
