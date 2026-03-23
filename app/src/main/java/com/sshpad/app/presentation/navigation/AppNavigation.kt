package com.sshpad.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sshpad.app.presentation.screens.ConnectionListScreen
import com.sshpad.app.presentation.screens.ConnectionEditScreen
import com.sshpad.app.presentation.screens.TerminalScreen

/**
 * App navigation graph
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.ConnectionList.route
    ) {
        composable(Screen.ConnectionList.route) {
            ConnectionListScreen(
                onConnectionClick = { connectionId ->
                    // Navigate to terminal screen
                    navController.navigate(Screen.Terminal.createRoute(connectionId))
                },
                onAddConnection = {
                    navController.navigate(Screen.ConnectionEdit.route)
                },
                onEditConnection = { connectionId ->
                    navController.navigate(Screen.ConnectionEdit.createRoute(connectionId))
                }
            )
        }

        composable(
            route = Screen.ConnectionEdit.routeWithArgs,
            arguments = Screen.ConnectionEdit.arguments
        ) { backStackEntry ->
            val connectionId = backStackEntry.arguments?.getString("connectionId")
            ConnectionEditScreen(
                connectionId = connectionId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Terminal.routeWithArgs,
            arguments = Screen.Terminal.arguments
        ) { backStackEntry ->
            val connectionId = backStackEntry.arguments?.getString("connectionId")
            TerminalScreen(
                connectionId = connectionId ?: "",
                onDisconnect = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Screen routes
 */
sealed class Screen(val route: String) {
    object ConnectionList : Screen("connections")
    object ConnectionEdit : Screen("connection/edit/{connectionId}") {
        val routeWithArgs = "connection/edit/{connectionId}"
        val arguments = listOf(
            androidx.navigation.namedArgument("connectionId", androidx.navigation.NavType.StringType, true)
        )
        fun createRoute(connectionId: String) = "connection/edit/$connectionId"
    }
    object Terminal : Screen("terminal/{connectionId}") {
        val routeWithArgs = "terminal/{connectionId}"
        val arguments = listOf(
            androidx.navigation.namedArgument("connectionId", androidx.navigation.NavType.StringType, false)
        )
        fun createRoute(connectionId: String) = "terminal/$connectionId"
    }
}
