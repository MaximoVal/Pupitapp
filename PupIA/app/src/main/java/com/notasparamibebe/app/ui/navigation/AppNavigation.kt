package com.notasparamibebe.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notasparamibebe.app.ui.screens.CreateNoteScreen
import com.notasparamibebe.app.ui.screens.HomeScreen
import com.notasparamibebe.app.ui.screens.NoteListScreen
import com.notasparamibebe.app.ui.screens.SetupScreen
import com.notasparamibebe.app.ui.viewmodel.NotesViewModel

@Composable
fun AppNavigation(
    viewModel: NotesViewModel,
    initialRoute: String? = null
) {
    val navController = rememberNavController()
    val secretCode by viewModel.secretCode.collectAsState()

    // Manejar apertura directa desde notificación (ej. list/today)
    LaunchedEffect(initialRoute) {
        if (!initialRoute.isNullOrBlank() && !secretCode.isNullOrBlank()) {
            navController.navigate(initialRoute) {
                launchSingleTop = true
            }
        }
    }

    // La pantalla de inicio depende de si ya tenemos el código guardado
    val startDestination = if (secretCode.isNullOrBlank()) "setup" else "home"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("setup") {
            SetupScreen(
                viewModel = viewModel,
                onConnected = {
                    navController.navigate("home") {
                        popUpTo("setup") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToCreate = {
                    navController.navigate("create")
                },
                onNavigateToList = { filterType ->
                    navController.navigate("list/$filterType")
                },
                onDisconnected = {
                    navController.navigate("setup") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("create") {
            CreateNoteScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "list/{filterType}",
            arguments = listOf(navArgument("filterType") { type = NavType.StringType })
        ) { backStackEntry ->
            val filterType = backStackEntry.arguments?.getString("filterType") ?: "today"
            NoteListScreen(
                filterType = filterType,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
