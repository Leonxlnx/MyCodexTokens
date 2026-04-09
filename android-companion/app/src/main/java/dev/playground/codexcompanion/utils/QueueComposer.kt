package dev.playground.codexcompanion.utils

import dev.playground.codexcompanion.data.AppSettings
import dev.playground.codexcompanion.data.QueuedPrompt

object QueueComposer {
    fun compose(
        queue: List<QueuedPrompt>,
        settings: AppSettings,
    ): String {
        val parts = mutableListOf<String>()

        settings.promptPrefix.trim().takeIf { it.isNotBlank() }?.let(parts::add)

        val sorted = queue.sortedBy { it.sortOrder }
        val repoLabel =
            sorted.firstOrNull { it.repoLabel.isNotBlank() }?.repoLabel
                ?: settings.defaultRepoLabel

        repoLabel.takeIf { it.isNotBlank() }?.let { parts += "Repository focus: $it" }

        if (sorted.isEmpty()) {
            return parts.joinToString("\n\n").trim()
        }

        if (sorted.size == 1) {
            parts += sorted.first().text.trim()
            return parts.joinToString("\n\n").trim()
        }

        parts += buildString {
            append("Stacked requests:")
            sorted.forEachIndexed { index, prompt ->
                append("\n\n")
                append(index + 1)
                append(". ")
                if (prompt.repoLabel.isNotBlank()) {
                    append("[")
                    append(prompt.repoLabel)
                    append("] ")
                }
                append(prompt.text.trim())
            }
        }

        return parts.joinToString("\n\n").trim()
    }
}
