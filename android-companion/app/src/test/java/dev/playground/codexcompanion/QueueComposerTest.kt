package dev.playground.codexcompanion

import dev.playground.codexcompanion.data.AppSettings
import dev.playground.codexcompanion.data.QueuedPrompt
import dev.playground.codexcompanion.utils.QueueComposer
import kotlin.test.assertTrue
import org.junit.Test

class QueueComposerTest {
    @Test
    fun `compose adds prefix repo and numbered stack`() {
        val settings =
            AppSettings(
                defaultRepoLabel = "mobile-client",
                promptPrefix = "Use a safe branch + PR workflow.",
            )

        val queue =
            listOf(
                QueuedPrompt(
                    text = "Summarize the crash path.",
                    repoLabel = "mobile-client",
                    sortOrder = 0,
                ),
                QueuedPrompt(
                    text = "Then patch the root cause.",
                    sortOrder = 1,
                ),
            )

        val composed = QueueComposer.compose(queue, settings)

        assertTrue(composed.contains("Use a safe branch + PR workflow."))
        assertTrue(composed.contains("Repository focus: mobile-client"))
        assertTrue(composed.contains("1. [mobile-client] Summarize the crash path."))
        assertTrue(composed.contains("2. Then patch the root cause."))
    }
}
