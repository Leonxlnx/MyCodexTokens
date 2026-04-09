package dev.playground.codexcompanion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.playground.codexcompanion.data.SavedThread
import dev.playground.codexcompanion.ui.EmptyStateCard
import dev.playground.codexcompanion.ui.MetaChip
import dev.playground.codexcompanion.ui.SectionHeader
import dev.playground.codexcompanion.ui.formatTimestamp

@Composable
fun ThreadsScreen(
    threads: List<SavedThread>,
    onAddThread: () -> Unit,
    onOpenThread: (SavedThread) -> Unit,
    onEditThread: (SavedThread) -> Unit,
    onTogglePinned: (SavedThread) -> Unit,
    onDeleteThread: (SavedThread) -> Unit,
) {
    val sorted =
        threads.sortedWith(
            compareByDescending<SavedThread> { it.pinned }.thenByDescending { it.updatedAt },
        )

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionHeader(
                title = "Saved cloud threads",
                actionLabel = "Add link",
                onActionClick = onAddThread,
            )
        }

        item {
            Text(
                text = "Share a Codex URL into the app or save one manually here. These are just cloud thread shortcuts, so your laptop can stay off.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (sorted.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Nothing saved yet",
                    body = "Start a Codex cloud task on desktop or mobile, then share the thread link into this app.",
                    actionLabel = "Add link",
                    onActionClick = onAddThread,
                    icon = Icons.Rounded.Share,
                )
            }
        } else {
            items(sorted, key = { it.id }) { thread ->
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
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(text = thread.title, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = thread.url,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Row {
                                IconButton(onClick = { onOpenThread(thread) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.OpenInBrowser,
                                        contentDescription = "Open thread",
                                    )
                                }
                                IconButton(onClick = { onEditThread(thread) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit thread",
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (thread.repoLabel.isNotBlank()) {
                                MetaChip(text = thread.repoLabel, icon = Icons.Rounded.History)
                            }
                            MetaChip(text = formatTimestamp(thread.updatedAt))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AssistChip(
                                onClick = { onOpenThread(thread) },
                                label = { Text("Open in Custom Tab") },
                            )
                            Row {
                                IconButton(onClick = { onTogglePinned(thread) }) {
                                    Icon(
                                        imageVector =
                                            if (thread.pinned) {
                                                Icons.Rounded.Bookmark
                                            } else {
                                                Icons.Rounded.BookmarkBorder
                                            },
                                        contentDescription = "Pin thread",
                                    )
                                }
                                IconButton(onClick = { onDeleteThread(thread) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete thread",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
