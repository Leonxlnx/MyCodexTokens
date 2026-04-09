package dev.playground.codexcompanion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Source
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
import dev.playground.codexcompanion.data.CompanionState
import dev.playground.codexcompanion.data.PinnedRepo
import dev.playground.codexcompanion.data.SavedThread
import dev.playground.codexcompanion.ui.EmptyStateCard
import dev.playground.codexcompanion.ui.MetaChip
import dev.playground.codexcompanion.ui.SectionHeader
import dev.playground.codexcompanion.ui.formatTimestamp

@Composable
fun HomeScreen(
    snapshot: CompanionState,
    onOpenCodex: () -> Unit,
    onResumeLastThread: (() -> Unit)?,
    onSendQueueToCodex: (() -> Unit)?,
    onJumpToThreads: () -> Unit,
    onJumpToQueue: () -> Unit,
    onAddRepo: () -> Unit,
    onEditRepo: (PinnedRepo) -> Unit,
    onOpenRepo: (PinnedRepo) -> Unit,
    onOpenThread: (SavedThread) -> Unit,
) {
    val recentThreads =
        snapshot.threads.sortedWith(
            compareByDescending<SavedThread> { it.pinned }.thenByDescending { it.updatedAt },
        )
    val pinnedRepos = snapshot.repos.sortedBy { it.label.lowercase() }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Codex on your phone, cloud-first.",
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = "Launch Codex fast, keep your cloud thread links close, and stack prompts before you jump back into chatgpt.com.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeActionCard(
                    title = "Open Codex",
                    body = "Jump straight into Codex Web with your existing ChatGPT Pro session and GitHub connection.",
                    icon = Icons.Rounded.Code,
                    onClick = onOpenCodex,
                )

                if (onResumeLastThread != null) {
                    HomeActionCard(
                        title = "Resume last thread",
                        body = "Reopen the most recent cloud thread you used from the app.",
                        icon = Icons.Rounded.History,
                        onClick = onResumeLastThread,
                    )
                }

                HomeActionCard(
                    title = "Queue studio",
                    body =
                        if (snapshot.queue.isEmpty()) {
                            "Stack prompts, copy them, and keep them ready for the next Codex session."
                        } else {
                            "${snapshot.queue.size} queued prompt(s) ready to send."
                        },
                    icon = Icons.Rounded.CloudQueue,
                    onClick = onJumpToQueue,
                    secondaryActionLabel = onSendQueueToCodex?.let { "Copy + open Codex" },
                    onSecondaryClick = onSendQueueToCodex,
                )
            }
        }

        item {
            SectionHeader(
                title = "Recent threads",
                actionLabel = "All threads",
                onActionClick = onJumpToThreads,
            )
        }

        if (recentThreads.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No saved cloud threads yet",
                    body = "Share a Codex thread link into the app or paste one manually in the Threads tab.",
                    actionLabel = "Open threads",
                    onActionClick = onJumpToThreads,
                    icon = Icons.Rounded.History,
                )
            }
        } else {
            items(recentThreads.take(4), key = { it.id }) { thread ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    onClick = { onOpenThread(thread) },
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = thread.title,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = thread.url,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            thread.repoLabel.takeIf { it.isNotBlank() }?.let {
                                MetaChip(text = it, icon = Icons.Rounded.Source)
                            }
                            MetaChip(text = formatTimestamp(thread.updatedAt))
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Pinned repos",
                actionLabel = "Add repo",
                onActionClick = onAddRepo,
            )
        }

        if (pinnedRepos.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Pin the repos you touch most",
                    body = "Keep quick GitHub links and repo notes nearby so you can jump back into the right Codex context faster.",
                    actionLabel = "Add repo",
                    onActionClick = onAddRepo,
                    icon = Icons.Rounded.FolderOpen,
                )
            }
        } else {
            items(pinnedRepos.take(4), key = { it.id }) { repo ->
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(repo.label, style = MaterialTheme.typography.titleLarge)
                                if (repo.notes.isNotBlank()) {
                                    Text(
                                        text = repo.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Row {
                                if (repo.githubUrl.isNotBlank()) {
                                    IconButton(onClick = { onOpenRepo(repo) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.OpenInBrowser,
                                            contentDescription = "Open repo",
                                        )
                                    }
                                }
                                IconButton(onClick = { onEditRepo(repo) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit repo",
                                    )
                                }
                            }
                        }

                        if (repo.githubUrl.isNotBlank()) {
                            Text(
                                text = repo.githubUrl,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Branch + PR by default", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "This companion stays out of local git on the phone. Do the editing in Codex Web and keep write actions in branches plus pull requests.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (secondaryActionLabel != null && onSecondaryClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    AssistChip(
                        onClick = onSecondaryClick,
                        label = { Text(secondaryActionLabel) },
                    )
                }
            }
        }
    }
}
