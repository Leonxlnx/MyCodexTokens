package dev.playground.codexcompanion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.playground.codexcompanion.data.AppSettings
import dev.playground.codexcompanion.data.QueuedPrompt
import dev.playground.codexcompanion.ui.EmptyStateCard
import dev.playground.codexcompanion.ui.MetaChip
import dev.playground.codexcompanion.ui.SectionHeader
import dev.playground.codexcompanion.ui.formatDate

@Composable
fun QueueScreen(
    queue: List<QueuedPrompt>,
    settings: AppSettings,
    compiledPrompt: String,
    onAddQueueItem: () -> Unit,
    onEditQueueItem: (QueuedPrompt) -> Unit,
    onMoveItem: (QueuedPrompt, Int) -> Unit,
    onDeleteItem: (QueuedPrompt) -> Unit,
    onCopyQueue: (() -> Unit)?,
    onCopyAndOpen: (() -> Unit)?,
    onClearQueue: (() -> Unit)?,
) {
    val sorted = queue.sortedBy { it.sortOrder }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionHeader(
                title = "Queue studio",
                actionLabel = "Add prompt",
                onActionClick = onAddQueueItem,
            )
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
                    Text("Compiled output", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text =
                            if (compiledPrompt.isBlank()) {
                                "Your queue is empty, so only the default prefix would be copied."
                            } else {
                                compiledPrompt
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetaChip(
                            text =
                                if (settings.copyBeforeOpen) {
                                    "Copy before open: on"
                                } else {
                                    "Copy before open: off"
                                },
                        )
                        if (settings.defaultRepoLabel.isNotBlank()) {
                            MetaChip(text = settings.defaultRepoLabel)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onCopyQueue != null) {
                            AssistChip(
                                onClick = onCopyQueue,
                                label = { Text("Copy") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                        if (onCopyAndOpen != null) {
                            AssistChip(
                                onClick = onCopyAndOpen,
                                label = { Text("Copy + open Codex") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.OpenInBrowser,
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                        if (onClearQueue != null) {
                            AssistChip(
                                onClick = onClearQueue,
                                label = { Text("Clear") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        if (sorted.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Stack your prompts here",
                    body = "Build up several requests over time, then copy the merged prompt into Codex when you are ready.",
                    actionLabel = "Add prompt",
                    onActionClick = onAddQueueItem,
                    icon = Icons.Rounded.CloudQueue,
                )
            }
        } else {
            items(sorted, key = { it.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MetaChip(text = "Step ${item.sortOrder + 1}", icon = Icons.Rounded.PostAdd)
                                    if (item.repoLabel.isNotBlank()) {
                                        MetaChip(text = item.repoLabel)
                                    }
                                    MetaChip(text = formatDate(item.createdAt))
                                }
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Column {
                                IconButton(onClick = { onMoveItem(item, -1) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowUpward,
                                        contentDescription = "Move up",
                                    )
                                }
                                IconButton(onClick = { onMoveItem(item, 1) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDownward,
                                        contentDescription = "Move down",
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            IconButton(onClick = { onEditQueueItem(item) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Edit queued prompt",
                                )
                            }
                            IconButton(onClick = { onDeleteItem(item) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete queued prompt",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
