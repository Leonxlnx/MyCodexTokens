package dev.playground.codexcompanion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.playground.codexcompanion.data.AppSettings
import dev.playground.codexcompanion.data.OpenMode
import dev.playground.codexcompanion.data.PinnedRepo
import dev.playground.codexcompanion.data.ThemeMode
import dev.playground.codexcompanion.ui.EmptyStateCard
import dev.playground.codexcompanion.ui.SectionHeader

@Composable
fun SettingsScreen(
    settings: AppSettings,
    repos: List<PinnedRepo>,
    onSaveDraftFields: (defaultRepoLabel: String, promptPrefix: String) -> Unit,
    onThemeChanged: (ThemeMode) -> Unit,
    onOpenModeChanged: (OpenMode) -> Unit,
    onCopyBeforeOpenChanged: (Boolean) -> Unit,
) {
    var draftRepoLabel by remember(settings.defaultRepoLabel) { mutableStateOf(settings.defaultRepoLabel) }
    var draftPromptPrefix by remember(settings.promptPrefix) { mutableStateOf(settings.promptPrefix) }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SectionHeader(title = "Settings")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("Theme", style = MaterialTheme.typography.titleLarge)
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.theme == mode,
                            onClick = { onThemeChanged(mode) },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("Open mode", style = MaterialTheme.typography.titleLarge)
                    OpenMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.openMode == mode,
                            onClick = { onOpenModeChanged(mode) },
                            label = {
                                Text(
                                    if (mode == OpenMode.CUSTOM_TAB) {
                                        "Chrome Custom Tab"
                                    } else {
                                        "External browser"
                                    },
                                )
                            },
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Copy queue before open", style = MaterialTheme.typography.titleLarge)
                        Switch(
                            checked = settings.copyBeforeOpen,
                            onCheckedChange = onCopyBeforeOpenChanged,
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("Prompt defaults", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = draftRepoLabel,
                        onValueChange = { draftRepoLabel = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Default repo label") },
                        singleLine = true,
                    )
                    if (repos.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(repos, key = { it.id }) { repo ->
                                FilterChip(
                                    selected = draftRepoLabel == repo.label,
                                    onClick = { draftRepoLabel = repo.label },
                                    label = { Text(repo.label) },
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = draftPromptPrefix,
                        onValueChange = { draftPromptPrefix = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Default prompt prefix") },
                        minLines = 4,
                    )
                    TextButton(
                        onClick = {
                            onSaveDraftFields(draftRepoLabel, draftPromptPrefix)
                        },
                    ) {
                        Text("Save text settings")
                    }
                }
            }
        }

        if (repos.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No pinned repos yet",
                    body = "Pinning repos gives the settings screen quick defaults for repo focus and makes mobile handoffs faster.",
                    actionLabel = "Save defaults anyway",
                    onActionClick = {
                        onSaveDraftFields(draftRepoLabel, draftPromptPrefix)
                    },
                    icon = Icons.Rounded.SettingsSuggest,
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Security model", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "The app opens Codex in Chrome Custom Tabs and stores no OpenAI or GitHub tokens locally. Authentication stays in your browser session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Git operations still happen in Codex Web, which keeps the phone client lightweight and laptop-free.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
