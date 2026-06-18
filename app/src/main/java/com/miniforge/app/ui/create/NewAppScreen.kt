package com.miniforge.app.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun NewAppScreen(
    onGenerate: (name: String, description: String, prompt: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }

    val padding = 16.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
    ) {
        Text(
            text = "Create New App",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("App Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        // Description field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            minLines = 3,
            maxLines = 5
        )

        // Prompt field
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("System Prompt") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            minLines = 4,
            maxLines = 8
        )

        // Generate button
        Button(
            onClick = {
                onGenerate(name.trim(), description.trim(), prompt.trim())
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            enabled = name.isNotBlank() && prompt.isNotBlank()
        ) {
            Text("Generate")
        }
    }
}

private fun buildPrompt(name: String, description: String, prompt: String): String {
    return buildString {
        append("App Name: $name\n")
        if (description.isNotBlank()) {
            append("Description: $description\n")
        }
        append("\nSystem Prompt:\n$prompt")
    }
}
