package com.miniforge.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.miniforge.app.data.model.AiProvider
import com.miniforge.app.data.model.ApiFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(vm: ProvidersViewModel = hiltViewModel()) {
    val providers by vm.providers.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AI Providers") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, "Add provider")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), modifier = Modifier.padding(padding)) {
            items(providers, key = { it.id }) { provider ->
                ProviderRow(
                    provider = provider,
                    onSetDefault = { vm.setDefault(provider) },
                    onDelete = { vm.delete(provider) }
                )
            }
        }
    }

    if (showAddSheet) {
        AddProviderSheet(
            onSave = { form -> vm.save(form); showAddSheet = false },
            onDismiss = { showAddSheet = false }
        )
    }
}

@Composable
private fun ProviderRow(provider: AiProvider, onSetDefault: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(provider.name) },
        supportingContent = { Text("${provider.model} · ${provider.apiFormat.name.lowercase()}") },
        trailingContent = {
            Row {
                IconButton(onClick = onSetDefault) {
                    Icon(
                        if (provider.isDefault) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Set default"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProviderSheet(onSave: (ProviderFormState) -> Unit, onDismiss: () -> Unit) {
    var form by remember { mutableStateOf(ProviderFormState()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Add AI Provider", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = form.name, onValueChange = { form = form.copy(name = it) }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.baseUrl, onValueChange = { form = form.copy(baseUrl = it) }, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.model, onValueChange = { form = form.copy(model = it) }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.apiKey, onValueChange = { form = form.copy(apiKey = it) }, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Format:", modifier = Modifier.padding(end = 8.dp))
                ApiFormat.values().forEach { fmt ->
                    FilterChip(
                        selected = form.apiFormat == fmt,
                        onClick = { form = form.copy(apiFormat = fmt) },
                        label = { Text(fmt.name.lowercase()) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = form.isDefault, onCheckedChange = { form = form.copy(isDefault = it) })
                Text("Set as default")
            }
            Button(
                onClick = { onSave(form) },
                enabled = form.name.isNotBlank() && form.baseUrl.isNotBlank() && form.model.isNotBlank() && form.apiKey.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }
}
