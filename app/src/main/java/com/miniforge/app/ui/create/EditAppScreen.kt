package com.miniforge.app.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppScreen(
    appId: String,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditAppViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var refinementPrompt by remember { mutableStateOf("") }

    LaunchedEffect(appId) {
        viewModel.loadApp(appId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit: ${uiState.appName}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (val genState = uiState.generationState) {
                        is GeneratorUiState.Streaming -> {
                            StreamingView(accumulated = genState.accumulated)
                        }
                        is GeneratorUiState.Ready -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                // Safety check: never display file paths as HTML
                                val isFilePath = (genState.html.startsWith("/") && genState.html.endsWith(".html") && genState.html.length < 300) ||
                                        (genState.html.contains("/data/") && genState.html.contains(".html") && !genState.html.contains("<html"))

                                if (isFilePath) {
                                    Text(
                                        "Error: Failed to load HTML content",
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (genState.html.isBlank() || genState.html.length < 20) {
                                    Text(
                                        "Error: Empty HTML",
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    HtmlWebView(html = genState.html, modifier = Modifier.fillMaxSize())
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "AI Model",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(uiState.providers) { provider ->
                                        FilterChip(
                                            selected = uiState.selectedProviderId == provider.id,
                                            onClick = { viewModel.selectProvider(provider.id) },
                                            label = { Text(provider.name) }
                                        )
                                    }
                                }

                                if (uiState.selectedProviderId != null) {
                                    Spacer(Modifier.height(8.dp))
                                    if (uiState.loadingModels.contains(uiState.selectedProviderId)) {
                                        CircularProgressIndicator(modifier = Modifier.height(32.dp))
                                    } else {
                                        val models = uiState.availableModels[uiState.selectedProviderId] ?: emptyList()
                                        if (models.isNotEmpty()) {
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                contentPadding = PaddingValues(0.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                items(models) { model ->
                                                    FilterChip(
                                                        selected = uiState.selectedModelId == model,
                                                        onClick = { viewModel.selectModel(model) },
                                                        label = { Text(model, maxLines = 1) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = refinementPrompt,
                                    onValueChange = { refinementPrompt = it },
                                    label = { Text("What would you like to change?") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    maxLines = 3
                                )
                                Spacer(Modifier.height(12.dp))
                                Row {
                                    OutlinedButton(
                                        onClick = onBack,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancel")
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.refine(refinementPrompt); refinementPrompt = "" },
                                        modifier = Modifier.weight(1f),
                                        enabled = refinementPrompt.isNotBlank() && uiState.selectedModelId != null
                                    ) {
                                        Text("Update")
                                    }
                                }
                            }
                        }
                        is GeneratorUiState.Idle -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is GeneratorUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(genState.message, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        is GeneratorUiState.Saved -> {
                            LaunchedEffect(Unit) { onSaved() }
                        }
                    }
                }
            }
        }
    }
}
