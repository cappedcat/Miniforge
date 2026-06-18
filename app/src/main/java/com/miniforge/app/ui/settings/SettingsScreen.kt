package com.miniforge.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "settings_home") {
        composable("settings_home") {
            Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
                ListItem(
                    headlineContent = { Text("AI Providers") },
                    supportingContent = { Text("Configure API keys and models") },
                    modifier = Modifier.padding(padding).clickable { nav.navigate("providers") }
                )
            }
        }
        composable("providers") { ProvidersScreen() }
    }
}
