package dev.playground.codexcompanion.data

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
enum class OpenMode {
    CUSTOM_TAB,
    EXTERNAL_BROWSER,
}

@Serializable
enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
}

@Serializable
data class SavedThread(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val repoLabel: String = "",
    val pinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Serializable
data class PinnedRepo(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val githubUrl: String = "",
    val notes: String = "",
)

@Serializable
data class QueuedPrompt(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val repoLabel: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
)

@Serializable
data class AppSettings(
    val defaultRepoLabel: String = "",
    val openMode: OpenMode = OpenMode.CUSTOM_TAB,
    val copyBeforeOpen: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val promptPrefix: String =
        "Use a safe branch + PR workflow by default. Summarize the intended change briefly, then implement carefully.",
)

@Serializable
data class CompanionState(
    val threads: List<SavedThread> = emptyList(),
    val repos: List<PinnedRepo> = emptyList(),
    val queue: List<QueuedPrompt> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val lastOpenedThreadUrl: String? = null,
)
