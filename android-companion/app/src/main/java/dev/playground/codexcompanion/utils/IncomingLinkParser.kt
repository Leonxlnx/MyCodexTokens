package dev.playground.codexcompanion.utils

import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class IncomingThreadLink(
    val url: String,
    val suggestedTitle: String,
)

object IncomingLinkParser {
    private val urlRegex = Regex("""https?://[^\s]+""")

    fun parse(intent: Intent?): IncomingThreadLink? {
        intent ?: return null

        return when (intent.action) {
            Intent.ACTION_SEND -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
                val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                parseText(sharedText, sharedSubject)
            }

            Intent.ACTION_VIEW -> parseUrl(intent.dataString, null)
            else -> null
        }
    }

    fun parseText(raw: String, subject: String? = null): IncomingThreadLink? {
        val candidate =
            urlRegex.findAll(raw)
                .map { it.value.trimEnd('.', ',', ';', ')', ']') }
                .firstOrNull(::looksLikeOpenAiLink)

        return parseUrl(candidate, subject)
    }

    fun parseUrl(raw: String?, subject: String?): IncomingThreadLink? {
        val url = raw?.trim()?.takeIf(::looksLikeOpenAiLink) ?: return null
        return IncomingThreadLink(
            url = url,
            suggestedTitle = buildSuggestedTitle(url, subject),
        )
    }

    private fun buildSuggestedTitle(url: String, subject: String?): String {
        if (!subject.isNullOrBlank()) {
            return subject.trim()
        }

        val uri = Uri.parse(url)
        val lastPathSegment = uri.lastPathSegment?.replace('-', ' ')?.takeIf { it.isNotBlank() }
        if (lastPathSegment != null) {
            return lastPathSegment.replaceFirstChar { it.uppercase() }
        }

        val stamp = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date())
        return "Codex thread $stamp"
    }

    private fun looksLikeOpenAiLink(url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        val host = uri.host?.lowercase(Locale.US) ?: return false
        val path = uri.path.orEmpty()
        return uri.scheme == "https" &&
            (host == "chatgpt.com" || host == "chat.openai.com") &&
            path.isNotBlank()
    }
}
