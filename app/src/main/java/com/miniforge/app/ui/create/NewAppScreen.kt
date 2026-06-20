package com.miniforge.app.ui.create

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NewAppScreen(
    onGenerate: (name: String, description: String, prompt: String, providerId: String?, modelId: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewAppViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }

    val selectedProvider = uiState.effectiveProvider
    val hasProviders = uiState.providers.isNotEmpty()
    val providerModels = uiState.providerModels
    val isLoadingModels = selectedProvider?.id?.let { uiState.loadingModels.contains(it) } == true

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Create a Mini App", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Describe what you want to build — the AI generates a self-contained app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        // Provider selector
        Text(
            text = "Provider",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        if (!hasProviders) {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No providers added yet. Go to Settings → AI Providers to add one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.providers.forEach { provider ->
                    val isSelected = provider.id == selectedProvider?.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectProvider(provider.id) },
                        label = { Text(provider.name) }
                    )
                }
            }
        }

        // Model selector
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Model",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        when {
            isLoadingModels -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text(
                        text = "Fetching models from ${selectedProvider?.name}…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            !providerModels.isNullOrEmpty() -> {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    providerModels.forEach { model ->
                        val isSelected = model == (uiState.selectedModel ?: uiState.selectedModelOrDefault)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectModel(model) },
                            label = { Text(model, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
            selectedProvider != null -> {
                Text(
                    text = selectedProvider.model,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Form fields
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("App name") },
            placeholder = { Text("e.g. Tip Calculator") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Short description (optional)") },
            placeholder = { Text("e.g. Calculates tips and splits bills between friends") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("What should it do?") },
            placeholder = { Text("Describe the features, layout, and behavior in detail…") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 10,
            supportingText = { Text("The more detail you provide, the better the result.") }
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onGenerate(
                    name.trim(),
                    description.trim(),
                    prompt.trim(),
                    selectedProvider?.id,
                    uiState.selectedModelOrDefault
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && prompt.isNotBlank() && hasProviders,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Generate App")
        }
    }
}
