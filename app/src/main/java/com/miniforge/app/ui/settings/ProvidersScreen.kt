package com.miniforge.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.miniforge.app.data.model.AiProvider
import com.miniforge.app.data.model.ApiFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(vm: ProvidersViewModel = hiltViewModel()) {
    val providers by vm.providers.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var confirmDeleteProvider by remember { mutableStateOf<AiProvider?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AI Providers") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add provider")
            }
        }
    ) { padding ->
        if (providers.isEmpty()) {
            EmptyProvidersState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(providers, key = { it.id }) { provider ->
                    ProviderCard(
                        provider = provider,
                        onSetDefault = { vm.setDefault(provider) },
                        onDeleteRequest = { confirmDeleteProvider = provider }
                    )
                }
            }
        }
    }

    confirmDeleteProvider?.let { provider ->
        AlertDialog(
            onDismissRequest = { confirmDeleteProvider = null },
            title = { Text("Remove provider?") },
            text = { Text("\"${provider.name}\" and its API key will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(provider); confirmDeleteProvider = null }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteProvider = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddSheet) {
        AddProviderSheet(
            onSave = { form -> vm.save(form); showAddSheet = false },
            onDismiss = { showAddSheet = false }
        )
    }
}

@Composable
private fun EmptyProvidersState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No AI providers yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap + to add your API key from Claude, OpenAI, or any compatible provider.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProviderCard(
    provider: AiProvider,
    onSetDefault: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (provider.isDefault)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = provider.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = provider.model,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (provider.isDefault) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Default", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = provider.baseUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(provider.apiFormat.name.lowercase(), style = MaterialTheme.typography.labelSmall) }
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSetDefault, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (provider.isDefault) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Set as default",
                        tint = if (provider.isDefault) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDeleteRequest, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProviderSheet(onSave: (ProviderFormState) -> Unit, onDismiss: () -> Unit) {
    var form by remember { mutableStateOf(ProviderFormState()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add AI Provider", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = form.name,
                onValueChange = { form = form.copy(name = it) },
                label = { Text("Provider name") },
                placeholder = { Text("e.g. My Claude") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.baseUrl,
                onValueChange = { form = form.copy(baseUrl = it) },
                label = { Text("Base URL") },
                placeholder = { Text("https://api.anthropic.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.model,
                onValueChange = { form = form.copy(model = it) },
                label = { Text("Model ID") },
                placeholder = { Text("claude-sonnet-4-6") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.apiKey,
                onValueChange = { form = form.copy(apiKey = it) },
                label = { Text("API Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Stored securely on-device only") }
            )

            Text("API Format", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ApiFormat.entries.forEach { fmt ->
                    FilterChip(
                        selected = form.apiFormat == fmt,
                        onClick = { form = form.copy(apiFormat = fmt) },
                        label = { Text(fmt.name.lowercase()) }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = form.isDefault,
                    onCheckedChange = { form = form.copy(isDefault = it) }
                )
                Text("Set as default provider")
            }

            Button(
                onClick = { onSave(form) },
                enabled = form.name.isNotBlank() && form.baseUrl.isNotBlank()
                        && form.model.isNotBlank() && form.apiKey.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Provider")
            }
        }
    }
}
