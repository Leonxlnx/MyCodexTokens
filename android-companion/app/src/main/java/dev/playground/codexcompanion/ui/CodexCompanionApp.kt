package dev.playground.codexcompanion.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.playground.codexcompanion.data.PinnedRepo
import dev.playground.codexcompanion.data.QueuedPrompt
import dev.playground.codexcompanion.data.SavedThread
import dev.playground.codexcompanion.ui.screens.HomeScreen
import dev.playground.codexcompanion.ui.screens.QueueScreen
import dev.playground.codexcompanion.ui.screens.SettingsScreen
import dev.playground.codexcompanion.ui.screens.ThreadsScreen
import dev.playground.codexcompanion.utils.BrowserLauncher
import dev.playground.codexcompanion.utils.QueueComposer

private enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Home("home", "Home", Icons.Rounded.Home),
    Threads("threads", "Threads", Icons.Rounded.Share),
    Queue("queue", "Queue", Icons.Rounded.CloudQueue),
    Settings("settings", "Settings", Icons.Rounded.Settings),
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CodexCompanionApp(
    viewModel: AppViewModel,
    uiState: CompanionUiState,
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val snapshot = uiState.snapshot
    val compiledPrompt = remember(snapshot.queue, snapshot.settings) {
        QueueComposer.compose(snapshot.queue, snapshot.settings)
    }

    var threadDialogState by remember { mutableStateOf<ThreadDialogState?>(null) }
    var repoDialogState by remember { mutableStateOf<RepoDialogState?>(null) }
    var queueDialogState by remember { mutableStateOf<QueueDialogState?>(null) }
    var deleteThreadTarget by remember { mutableStateOf<SavedThread?>(null) }
    var deleteRepoTarget by remember { mutableStateOf<PinnedRepo?>(null) }
    var deleteQueueTarget by remember { mutableStateOf<QueuedPrompt?>(null) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: TopLevelDestination.Home.route
    val currentDestination =
        TopLevelDestination.entries.firstOrNull { it.route == currentRoute } ?: TopLevelDestination.Home

    LaunchedEffect(Unit) {
        viewModel.snackbars.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    uiState.pendingImport?.let { incoming ->
        ThreadEditorDialog(
            dialogTitle = "Save incoming Codex link",
            initialUrl = incoming.url,
            initialTitle = incoming.suggestedTitle,
            initialRepoLabel = snapshot.settings.defaultRepoLabel,
            allowUrlEdit = false,
            confirmLabel = "Save link",
            onDismiss = viewModel::dismissPendingImport,
            onSave = { _, title, repoLabel ->
                viewModel.savePendingImport(
                    title = title,
                    repoLabel = repoLabel,
                )
            },
        )
    }

    threadDialogState?.let { current ->
        ThreadEditorDialog(
            dialogTitle = current.dialogTitle,
            initialUrl = current.initialUrl,
            initialTitle = current.initialTitle,
            initialRepoLabel = current.initialRepoLabel,
            allowUrlEdit = current.allowUrlEdit,
            confirmLabel = current.confirmLabel,
            onDismiss = { threadDialogState = null },
            onSave = { url, title, repoLabel ->
                viewModel.addOrUpdateThread(
                    existingId = current.threadId,
                    url = url,
                    title = title,
                    repoLabel = repoLabel,
                )
                threadDialogState = null
            },
        )
    }

    repoDialogState?.let { state ->
        val repo = state.repo
        RepoEditorDialog(
            initialLabel = repo?.label.orEmpty(),
            initialUrl = repo?.githubUrl.orEmpty(),
            initialNotes = repo?.notes.orEmpty(),
            onDismiss = { repoDialogState = null },
            onSave = { label, url, notes ->
                viewModel.addOrUpdateRepo(
                    existingId = repo?.id,
                    label = label,
                    githubUrl = url,
                    notes = notes,
                )
                repoDialogState = null
            },
        )
    }

    queueDialogState?.let { state ->
        val queueItem = state.item
        QueueItemEditorDialog(
            initialText = queueItem?.text.orEmpty(),
            initialRepoLabel = queueItem?.repoLabel.orEmpty(),
            defaultRepoLabel = snapshot.settings.defaultRepoLabel,
            onDismiss = { queueDialogState = null },
            onSave = { text, repoLabel ->
                viewModel.addOrUpdateQueueItem(
                    existingId = queueItem?.id,
                    text = text,
                    repoLabel = repoLabel,
                )
                queueDialogState = null
            },
        )
    }

    deleteThreadTarget?.let { thread ->
        ConfirmDeleteDialog(
            title = "Delete saved thread?",
            body = "This removes the local shortcut, not the cloud thread itself.",
            onDismiss = { deleteThreadTarget = null },
            onConfirm = {
                viewModel.deleteThread(thread.id)
                deleteThreadTarget = null
            },
        )
    }

    deleteRepoTarget?.let { repo ->
        ConfirmDeleteDialog(
            title = "Delete pinned repo?",
            body = "This only removes the local shortcut and notes.",
            onDismiss = { deleteRepoTarget = null },
            onConfirm = {
                viewModel.deleteRepo(repo.id)
                deleteRepoTarget = null
            },
        )
    }

    deleteQueueTarget?.let { item ->
        ConfirmDeleteDialog(
            title = "Delete queue item?",
            body = "This prompt will be removed from the local stack.",
            onDismiss = { deleteQueueTarget = null },
            onConfirm = {
                viewModel.deleteQueueItem(item.id)
                deleteQueueTarget = null
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentDestination.label) },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = destination == currentDestination,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Home.route,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(TopLevelDestination.Home.route) {
                HomeScreen(
                    snapshot = snapshot,
                    onOpenCodex = {
                        BrowserLauncher.openCodex(context, snapshot.settings)
                    },
                    onResumeLastThread =
                        snapshot.threads.firstOrNull { it.url == snapshot.lastOpenedThreadUrl }?.let { thread ->
                            {
                                BrowserLauncher.openUrl(context, snapshot.settings, thread.url)
                                viewModel.markThreadOpened(thread.id)
                            }
                        },
                    onSendQueueToCodex =
                        if (compiledPrompt.isBlank()) {
                            null
                        } else {
                            {
                                BrowserLauncher.copyToClipboard(context, "Codex queue", compiledPrompt)
                                BrowserLauncher.openCodex(context, snapshot.settings)
                            }
                        },
                    onJumpToThreads = {
                        navController.navigate(TopLevelDestination.Threads.route)
                    },
                    onJumpToQueue = {
                        navController.navigate(TopLevelDestination.Queue.route)
                    },
                    onAddRepo = { repoDialogState = RepoDialogState() },
                    onEditRepo = { repo -> repoDialogState = RepoDialogState(repo) },
                    onOpenRepo = { repo ->
                        if (repo.githubUrl.isNotBlank()) {
                            BrowserLauncher.openUrl(context, snapshot.settings, repo.githubUrl)
                        }
                    },
                    onOpenThread = { thread ->
                        BrowserLauncher.openUrl(context, snapshot.settings, thread.url)
                        viewModel.markThreadOpened(thread.id)
                    },
                )
            }

            composable(TopLevelDestination.Threads.route) {
                ThreadsScreen(
                    threads = snapshot.threads,
                    onAddThread = {
                        threadDialogState =
                            ThreadDialogState(
                                dialogTitle = "Save thread link",
                                initialUrl = "",
                                initialTitle = "",
                                initialRepoLabel = snapshot.settings.defaultRepoLabel,
                                allowUrlEdit = true,
                                confirmLabel = "Save thread",
                            )
                    },
                    onOpenThread = { thread ->
                        BrowserLauncher.openUrl(context, snapshot.settings, thread.url)
                        viewModel.markThreadOpened(thread.id)
                    },
                    onEditThread = { thread ->
                        threadDialogState =
                            ThreadDialogState(
                                threadId = thread.id,
                                dialogTitle = "Edit saved thread",
                                initialUrl = thread.url,
                                initialTitle = thread.title,
                                initialRepoLabel = thread.repoLabel,
                                allowUrlEdit = true,
                                confirmLabel = "Save changes",
                            )
                    },
                    onTogglePinned = { thread -> viewModel.toggleThreadPinned(thread.id) },
                    onDeleteThread = { thread -> deleteThreadTarget = thread },
                )
            }

            composable(TopLevelDestination.Queue.route) {
                QueueScreen(
                    queue = snapshot.queue,
                    settings = snapshot.settings,
                    compiledPrompt = compiledPrompt,
                    onAddQueueItem = { queueDialogState = QueueDialogState() },
                    onEditQueueItem = { queueItem -> queueDialogState = QueueDialogState(queueItem) },
                    onMoveItem = { queueItem, delta ->
                        viewModel.moveQueueItem(queueItem.id, delta)
                    },
                    onDeleteItem = { queueItem -> deleteQueueTarget = queueItem },
                    onCopyQueue =
                        if (compiledPrompt.isBlank()) {
                            null
                        } else {
                            {
                                BrowserLauncher.copyToClipboard(context, "Codex queue", compiledPrompt)
                            }
                        },
                    onCopyAndOpen =
                        if (compiledPrompt.isBlank()) {
                            null
                        } else {
                            {
                                if (snapshot.settings.copyBeforeOpen) {
                                    BrowserLauncher.copyToClipboard(context, "Codex queue", compiledPrompt)
                                }
                                BrowserLauncher.openCodex(context, snapshot.settings)
                            }
                        },
                    onClearQueue =
                        if (snapshot.queue.isEmpty()) {
                            null
                        } else {
                            { viewModel.clearQueue() }
                        },
                )
            }

            composable(TopLevelDestination.Settings.route) {
                SettingsScreen(
                    settings = snapshot.settings,
                    repos = snapshot.repos.sortedBy { it.label.lowercase() },
                    onSaveDraftFields = { defaultRepoLabel, promptPrefix ->
                        viewModel.updateSettings(
                            snapshot.settings.copy(
                                defaultRepoLabel = defaultRepoLabel.trim(),
                                promptPrefix = promptPrefix.trim(),
                            ),
                        )
                    },
                    onThemeChanged = { mode ->
                        viewModel.updateSettings(snapshot.settings.copy(theme = mode), notify = false)
                    },
                    onOpenModeChanged = { mode ->
                        viewModel.updateSettings(snapshot.settings.copy(openMode = mode), notify = false)
                    },
                    onCopyBeforeOpenChanged = { enabled ->
                        viewModel.updateSettings(
                            snapshot.settings.copy(copyBeforeOpen = enabled),
                            notify = false,
                        )
                    },
                )
            }
        }
    }
}

private data class ThreadDialogState(
    val threadId: String? = null,
    val dialogTitle: String,
    val initialUrl: String,
    val initialTitle: String,
    val initialRepoLabel: String,
    val allowUrlEdit: Boolean,
    val confirmLabel: String,
)

private data class RepoDialogState(
    val repo: PinnedRepo? = null,
)

private data class QueueDialogState(
    val item: QueuedPrompt? = null,
)
