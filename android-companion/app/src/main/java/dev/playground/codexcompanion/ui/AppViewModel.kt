package dev.playground.codexcompanion.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.playground.codexcompanion.data.AppSettings
import dev.playground.codexcompanion.data.CompanionState
import dev.playground.codexcompanion.data.CompanionStore
import dev.playground.codexcompanion.data.PinnedRepo
import dev.playground.codexcompanion.data.QueuedPrompt
import dev.playground.codexcompanion.data.SavedThread
import dev.playground.codexcompanion.utils.IncomingLinkParser
import dev.playground.codexcompanion.utils.IncomingThreadLink
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CompanionUiState(
    val snapshot: CompanionState = CompanionState(),
    val pendingImport: IncomingThreadLink? = null,
)

class AppViewModel(
    private val store: CompanionStore,
) : ViewModel() {
    private val pendingImport = MutableStateFlow<IncomingThreadLink?>(null)
    private val snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)

    val uiState: StateFlow<CompanionUiState> =
        combine(store.state, pendingImport) { snapshot, incoming ->
            CompanionUiState(snapshot = snapshot, pendingImport = incoming)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CompanionUiState(),
        )

    val snackbars = snackbarMessages.asSharedFlow()

    fun handleIncomingIntent(intent: Intent?) {
        val parsed = IncomingLinkParser.parse(intent) ?: return
        if (pendingImport.value?.url == parsed.url) {
            return
        }
        pendingImport.value = parsed
    }

    fun dismissPendingImport() {
        pendingImport.value = null
    }

    fun savePendingImport(title: String, repoLabel: String) {
        val incoming = pendingImport.value ?: return
        addOrUpdateThread(
            existingId = null,
            url = incoming.url,
            title = title.ifBlank { incoming.suggestedTitle },
            repoLabel = repoLabel,
        )
        pendingImport.value = null
    }

    fun addOrUpdateThread(
        existingId: String?,
        url: String,
        title: String,
        repoLabel: String,
    ) {
        val cleanUrl = url.trim()
        val cleanTitle = title.trim()
        if (cleanUrl.isBlank() || cleanTitle.isBlank()) {
            snackbarMessages.tryEmit("Thread URL and title are required.")
            return
        }

        viewModelScope.launch {
            store.update { state ->
                val now = System.currentTimeMillis()
                val existing =
                    state.threads.firstOrNull { it.id == existingId }
                        ?: state.threads.firstOrNull { it.url == cleanUrl }

                val updated =
                    if (existing != null) {
                        existing.copy(
                            url = cleanUrl,
                            title = cleanTitle,
                            repoLabel = repoLabel.trim(),
                            updatedAt = now,
                        )
                    } else {
                        SavedThread(
                            url = cleanUrl,
                            title = cleanTitle,
                            repoLabel = repoLabel.trim(),
                            updatedAt = now,
                        )
                    }

                state.copy(
                    threads = state.threads.filterNot { it.id == updated.id || it.url == cleanUrl } + updated,
                )
            }
            snackbarMessages.emit("Saved thread.")
        }
    }

    fun deleteThread(threadId: String) {
        viewModelScope.launch {
            store.update { state ->
                state.copy(threads = state.threads.filterNot { it.id == threadId })
            }
            snackbarMessages.emit("Thread removed.")
        }
    }

    fun toggleThreadPinned(threadId: String) {
        viewModelScope.launch {
            store.update { state ->
                state.copy(
                    threads =
                        state.threads.map { thread ->
                            if (thread.id == threadId) {
                                thread.copy(pinned = !thread.pinned)
                            } else {
                                thread
                            }
                        },
                )
            }
        }
    }

    fun markThreadOpened(threadId: String) {
        viewModelScope.launch {
            store.update { state ->
                val updatedThreads =
                    state.threads.map { thread ->
                        if (thread.id == threadId) {
                            thread.copy(updatedAt = System.currentTimeMillis())
                        } else {
                            thread
                        }
                    }

                val lastOpenedUrl = updatedThreads.firstOrNull { it.id == threadId }?.url
                state.copy(
                    threads = updatedThreads,
                    lastOpenedThreadUrl = lastOpenedUrl ?: state.lastOpenedThreadUrl,
                )
            }
        }
    }

    fun addOrUpdateRepo(
        existingId: String?,
        label: String,
        githubUrl: String,
        notes: String,
    ) {
        val cleanLabel = label.trim()
        if (cleanLabel.isBlank()) {
            snackbarMessages.tryEmit("Repo label is required.")
            return
        }

        viewModelScope.launch {
            store.update { state ->
                val existing = state.repos.firstOrNull { it.id == existingId }
                val updated =
                    if (existing != null) {
                        existing.copy(
                            label = cleanLabel,
                            githubUrl = githubUrl.trim(),
                            notes = notes.trim(),
                        )
                    } else {
                        PinnedRepo(
                            label = cleanLabel,
                            githubUrl = githubUrl.trim(),
                            notes = notes.trim(),
                        )
                    }

                val nextSettings =
                    if (state.settings.defaultRepoLabel == existing?.label) {
                        state.settings.copy(defaultRepoLabel = updated.label)
                    } else {
                        state.settings
                    }

                state.copy(
                    repos = state.repos.filterNot { it.id == updated.id } + updated,
                    settings = nextSettings,
                )
            }
            snackbarMessages.emit("Pinned repo saved.")
        }
    }

    fun deleteRepo(repoId: String) {
        viewModelScope.launch {
            store.update { state ->
                val repo = state.repos.firstOrNull { it.id == repoId }
                val nextDefault =
                    if (state.settings.defaultRepoLabel == repo?.label) {
                        ""
                    } else {
                        state.settings.defaultRepoLabel
                    }

                state.copy(
                    repos = state.repos.filterNot { it.id == repoId },
                    settings = state.settings.copy(defaultRepoLabel = nextDefault),
                )
            }
            snackbarMessages.emit("Pinned repo removed.")
        }
    }

    fun addOrUpdateQueueItem(
        existingId: String?,
        text: String,
        repoLabel: String,
    ) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) {
            snackbarMessages.tryEmit("Queue text cannot be empty.")
            return
        }

        viewModelScope.launch {
            store.update { state ->
                val sorted = state.queue.sortedBy { it.sortOrder }
                val existing = sorted.firstOrNull { it.id == existingId }
                val nextOrder = sorted.maxOfOrNull { it.sortOrder }?.plus(1) ?: 0

                val updated =
                    if (existing != null) {
                        existing.copy(text = cleanText, repoLabel = repoLabel.trim())
                    } else {
                        QueuedPrompt(
                            text = cleanText,
                            repoLabel = repoLabel.trim(),
                            sortOrder = nextOrder,
                        )
                    }

                state.copy(
                    queue = normalizeQueue(state.queue.filterNot { it.id == updated.id } + updated),
                )
            }
            snackbarMessages.emit("Queue updated.")
        }
    }

    fun deleteQueueItem(itemId: String) {
        viewModelScope.launch {
            store.update { state ->
                state.copy(queue = normalizeQueue(state.queue.filterNot { it.id == itemId }))
            }
        }
    }

    fun moveQueueItem(itemId: String, delta: Int) {
        viewModelScope.launch {
            store.update { state ->
                val sorted = state.queue.sortedBy { it.sortOrder }.toMutableList()
                val currentIndex = sorted.indexOfFirst { it.id == itemId }
                val targetIndex = currentIndex + delta

                if (currentIndex == -1 || targetIndex !in sorted.indices) {
                    return@update state
                }

                val current = sorted[currentIndex]
                sorted[currentIndex] = sorted[targetIndex]
                sorted[targetIndex] = current
                state.copy(queue = normalizeQueue(sorted))
            }
        }
    }

    fun clearQueue() {
        viewModelScope.launch {
            store.update { state -> state.copy(queue = emptyList()) }
            snackbarMessages.emit("Queue cleared.")
        }
    }

    fun updateSettings(
        settings: AppSettings,
        notify: Boolean = true,
    ) {
        viewModelScope.launch {
            store.update { state -> state.copy(settings = settings) }
            if (notify) {
                snackbarMessages.emit("Settings saved.")
            }
        }
    }

    private fun normalizeQueue(queue: List<QueuedPrompt>): List<QueuedPrompt> =
        queue.sortedBy { it.sortOrder }
            .mapIndexed { index, queuedPrompt -> queuedPrompt.copy(sortOrder = index) }
}

class AppViewModelFactory(
    private val store: CompanionStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AppViewModel::class.java))
        return AppViewModel(store) as T
    }
}
