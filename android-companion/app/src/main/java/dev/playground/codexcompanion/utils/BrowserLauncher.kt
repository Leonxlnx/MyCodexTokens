package dev.playground.codexcompanion.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import dev.playground.codexcompanion.data.AppSettings
import dev.playground.codexcompanion.data.OpenMode

object BrowserLauncher {
    const val CodexHomeUrl: String = "https://chatgpt.com/codex"

    private val preferredPackages = listOf(
        "com.android.chrome",
        "com.chrome.beta",
        "com.chrome.dev",
        "com.google.android.apps.chrome",
    )

    fun openCodex(context: Context, settings: AppSettings, url: String = CodexHomeUrl) {
        openUrl(context, settings, url)
    }

    fun openUrl(context: Context, settings: AppSettings, url: String) {
        val uri = Uri.parse(url)
        if (settings.openMode == OpenMode.EXTERNAL_BROWSER) {
            launchBrowserIntent(context, uri)
            return
        }

        val customTabsPackage = resolveCustomTabsPackage(context)
        if (customTabsPackage == null) {
            launchBrowserIntent(context, uri)
            return
        }

        val intent =
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .build()

        intent.intent.setPackage(customTabsPackage)
        intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.launchUrl(context, uri)
    }

    fun copyToClipboard(context: Context, label: String, value: String) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    }

    private fun resolveCustomTabsPackage(context: Context): String? {
        preferredPackages.firstOrNull { packageName ->
            runCatching {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            }.getOrDefault(false)
        }?.let { return it }

        return CustomTabsClient.getPackageName(context, null)
    }

    private fun launchBrowserIntent(context: Context, uri: Uri) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
