package com.sshpad.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sshpad.app.presentation.screens.ConnectionListScreen
import com.sshpad.app.presentation.screens.ConnectionEditScreen
import com.sshpad.app.presentation.screens.TerminalScreen
import com.sshpad.app.presentation.viewmodel.ConnectionListViewModel
import com.sshpad.app.presentation.viewmodel.ConnectionEditViewModel
import com.sshpad.app.presentation.viewmodel.TerminalViewModel
import org.koin.androidx.compose.getViewModel

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
            val viewModel: ConnectionListViewModel = getViewModel()
            ConnectionListScreen(
                viewModel = viewModel,
                onConnectionClick = { connectionId ->
                    // Navigate to terminal screen
                    navController.navigate(Screen.Terminal.createRoute(connectionId))
                },
                onAddConnection = {
                    // Use explicit route for add mode to avoid route collision
                    navController.navigate(Screen.ConnectionEdit.routeAdd)
                },
                onEditConnection = { connectionId ->
                    navController.navigate(Screen.ConnectionEdit.createRoute(connectionId))
                }
            )
        }

        // Add mode: no connectionId - MUST be declared BEFORE the parameterized route
        // to avoid route matching conflicts
        composable(route = Screen.ConnectionEdit.routeAdd) {
            val viewModel: ConnectionEditViewModel = getViewModel()
            viewModel.setConnectionId(null)
            ConnectionEditScreen(
                viewModel = viewModel,
                connectionId = null,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        // Edit mode: with connectionId
        composable(
            route = Screen.ConnectionEdit.routeWithArgs,
            arguments = Screen.ConnectionEdit.arguments
        ) { backStackEntry ->
            val connectionId = backStackEntry.arguments?.getString("connectionId")
            val viewModel: ConnectionEditViewModel = getViewModel()
            viewModel.setConnectionId(connectionId)
            ConnectionEditScreen(
                viewModel = viewModel,
                connectionId = connectionId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Terminal.routeWithArgs,
            arguments = Screen.Terminal.arguments
        ) { backStackEntry ->
            val connectionId = backStackEntry.arguments?.getString("connectionId") ?: ""
            val viewModel: TerminalViewModel = getViewModel()
            TerminalScreen(
                viewModel = viewModel,
                connectionId = connectionId,
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
    object ConnectionEdit : Screen("connection/edit") {
        // Add mode: no connectionId
        val routeAdd = "connection/add"
        // Edit mode: with connectionId
        val routeWithArgs = "connection/edit/{connectionId}"
        val arguments = listOf(
            navArgument("connectionId") { type = androidx.navigation.NavType.StringType; nullable = true }
        )
        fun createRoute(connectionId: String) = "connection/edit/$connectionId"
    }
    object Terminal : Screen("terminal/{connectionId}") {
        val routeWithArgs = "terminal/{connectionId}"
        val arguments = listOf(
            navArgument("connectionId") { type = androidx.navigation.NavType.StringType; nullable = false }
        )
        fun createRoute(connectionId: String) = "terminal/$connectionId"
    }
}
