package com.miniforge.app.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miniforge.app.ui.create.GeneratorScreen
import com.miniforge.app.ui.create.NewAppScreen
import com.miniforge.app.ui.myapps.MyAppsScreen
import com.miniforge.app.ui.runner.AppRunnerScreen
import com.miniforge.app.ui.settings.SettingsScreen

private object Routes {
    const val MY_APPS = "my_apps"
    const val CREATE = "create"
    const val SETTINGS = "settings"
    const val RUNNER = "runner/{appId}"

    fun runner(appId: String) = "runner/$appId"
    fun generator(name: String, description: String, prompt: String) =
        "generator?name=${Uri.encode(name)}&description=${Uri.encode(description)}&prompt=${Uri.encode(prompt)}"
}

private val TAB_ROUTES = setOf(Routes.MY_APPS, Routes.CREATE, Routes.SETTINGS)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in TAB_ROUTES

    Scaffold(
        bottomBar = { if (showBottomBar) BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.MY_APPS,
        ) {
            composable(Routes.MY_APPS) {
                MyAppsScreen(
                    onOpenApp = { app -> navController.navigate(Routes.runner(app.id)) }
                )
            }
            composable(Routes.CREATE) {
                NewAppScreen(
                    onGenerate = { name, description, prompt ->
                        navController.navigate(Routes.generator(name, description, prompt))
                    }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
            composable(
                route = "generator?name={name}&description={description}&prompt={prompt}",
                arguments = listOf(
                    navArgument("name") { type = NavType.StringType; defaultValue = "" },
                    navArgument("description") { type = NavType.StringType; defaultValue = "" },
                    navArgument("prompt") { type = NavType.StringType; defaultValue = "" }
                )
            ) { entry ->
                GeneratorScreen(
                    appName = entry.arguments?.getString("name") ?: "",
                    appDescription = entry.arguments?.getString("description") ?: "",
                    initialPrompt = entry.arguments?.getString("prompt") ?: "",
                    onSaved = {
                        navController.navigate(Routes.MY_APPS) {
                            popUpTo(Routes.MY_APPS) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = Routes.RUNNER,
                arguments = listOf(navArgument("appId") { type = NavType.StringType })
            ) { entry ->
                val appId = entry.arguments?.getString("appId") ?: return@composable
                AppRunnerScreen(
                    appId = appId,
                    onBack = { navController.popBackStack() },
                    onRefine = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Apps, contentDescription = null) },
            label = { Text("My Apps") },
            selected = currentDestination?.hierarchy?.any { it.route == Routes.MY_APPS } == true,
            onClick = {
                navController.navigate(Routes.MY_APPS) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            label = { Text("Create") },
            selected = currentDestination?.hierarchy?.any { it.route == Routes.CREATE } == true,
            onClick = {
                navController.navigate(Routes.CREATE) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = currentDestination?.hierarchy?.any { it.route == Routes.SETTINGS } == true,
            onClick = {
                navController.navigate(Routes.SETTINGS) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}
