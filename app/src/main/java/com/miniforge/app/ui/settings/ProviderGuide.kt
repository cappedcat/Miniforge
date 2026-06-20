package com.miniforge.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ProviderInfo(
    val name: String,
    val description: String,
    val getKeyUrl: String,
    val steps: List<String>
)

val PROVIDER_GUIDES = mapOf(
    "Claude" to ProviderInfo(
        name = "Anthropic Claude",
        description = "Access Claude API from Anthropic",
        getKeyUrl = "https://console.anthropic.com/account/keys",
        steps = listOf(
            "1. Go to console.anthropic.com/account/keys",
            "2. Sign up or log in to your Anthropic account",
            "3. Click 'Create Key' to generate a new API key",
            "4. Copy the key (starts with sk-ant-)",
            "5. Paste it in MiniForge under Settings → AI Providers"
        )
    ),
    "OpenAI" to ProviderInfo(
        name = "OpenAI GPT",
        description = "Use GPT-4, GPT-4 Turbo, and other OpenAI models",
        getKeyUrl = "https://platform.openai.com/account/api-keys",
        steps = listOf(
            "1. Go to platform.openai.com/account/api-keys",
            "2. Sign up or log in to your OpenAI account",
            "3. Click 'Create new secret key'",
            "4. Copy the key (starts with sk-)",
            "5. Add $5+ credits to your account for API usage",
            "6. Paste it in MiniForge under Settings → AI Providers"
        )
    ),
    "Mistral" to ProviderInfo(
        name = "Mistral AI",
        description = "Use Mistral models for fast and efficient generation",
        getKeyUrl = "https://console.mistral.ai/api-keys/",
        steps = listOf(
            "1. Go to console.mistral.ai/api-keys/",
            "2. Sign up or log in to your Mistral account",
            "3. Click 'Create API key'",
            "4. Copy the key",
            "5. Paste it in MiniForge under Settings → AI Providers"
        )
    ),
    "Groq" to ProviderInfo(
        name = "Groq",
        description = "Ultra-fast inference with Groq",
        getKeyUrl = "https://console.groq.com/keys",
        steps = listOf(
            "1. Go to console.groq.com/keys",
            "2. Sign up or log in to your Groq account",
            "3. Click 'Create API Key'",
            "4. Copy the key (starts with gsk_)",
            "5. Paste it in MiniForge under Settings → AI Providers"
        )
    ),
    "OpenRouter" to ProviderInfo(
        name = "OpenRouter",
        description = "Access multiple models through one API",
        getKeyUrl = "https://openrouter.ai/keys",
        steps = listOf(
            "1. Go to openrouter.ai/keys",
            "2. Sign up or log in to your OpenRouter account",
            "3. Copy your API key from the dashboard",
            "4. Add $5+ credits for API usage",
            "5. Paste it in MiniForge under Settings → AI Providers"
        )
    ),
    "Gemini" to ProviderInfo(
        name = "Google Gemini",
        description = "Use Google's Gemini AI models",
        getKeyUrl = "https://aistudio.google.com/app/apikey",
        steps = listOf(
            "1. Go to aistudio.google.com/app/apikey",
            "2. Sign in with your Google account",
            "3. Click 'Create API key'",
            "4. Select a project (or create new)",
            "5. Copy the key",
            "6. Paste it in MiniForge under Settings → AI Providers"
        )
    )
)

@Composable
fun ProviderGuideDialog(providerName: String, onDismiss: () -> Unit) {
    val guide = PROVIDER_GUIDES[providerName]
    val uriHandler = LocalUriHandler.current

    if (guide != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("${guide.name} Setup") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = guide.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Steps to get your API key:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    guide.steps.forEach { step ->
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Get API key",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { uriHandler.openUri(guide.getKeyUrl) }) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Open in browser")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Done") }
            }
        )
    }
}
