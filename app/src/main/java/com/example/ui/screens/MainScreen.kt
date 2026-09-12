package com.example.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.viewmodel.TaskViewModel

@Composable
fun MainScreen(viewModel: TaskViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val items = listOf(
                    BottomNavItem("Dashboard", "dashboard", Icons.Filled.Dashboard),
                    BottomNavItem("Tareas", "tasks", Icons.Filled.List),
                    BottomNavItem("Bóveda", "vault", Icons.Filled.Folder),
                    BottomNavItem("Ajustes", "settings", Icons.Filled.Settings)
                )

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(viewModel, navController) }
            composable("tasks") { TasksScreen(viewModel) }
            composable("vault") { VaultScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("scanner") { ScannerScreen() }
            composable("collab") { CollaborationScreen(viewModel) }
            composable("global_search") { GlobalSearchScreen(navController, viewModel) }
            composable(
                route = "reader/{encodedUrl}/{title}",
                arguments = listOf(
                    navArgument("encodedUrl") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
                val title = backStackEntry.arguments?.getString("title") ?: "Documento"
                val url = java.net.URLDecoder.decode(encodedUrl, "UTF-8")
                DocumentReaderScreen(navController, url, title)
            }
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
