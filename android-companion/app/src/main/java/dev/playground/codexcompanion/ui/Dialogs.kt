package dev.playground.codexcompanion.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThreadEditorDialog(
    dialogTitle: String,
    initialUrl: String,
    initialTitle: String,
    initialRepoLabel: String,
    allowUrlEdit: Boolean,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onSave: (url: String, title: String, repoLabel: String) -> Unit,
) {
    var url by remember(initialUrl) { mutableStateOf(initialUrl) }
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var repoLabel by remember(initialRepoLabel) { mutableStateOf(initialRepoLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Thread title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    enabled = allowUrlEdit,
                    label = { Text("Thread URL") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                OutlinedTextField(
                    value = repoLabel,
                    onValueChange = { repoLabel = it },
                    label = { Text("Repo label") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(url, title, repoLabel) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun RepoEditorDialog(
    initialLabel: String,
    initialUrl: String,
    initialNotes: String,
    onDismiss: () -> Unit,
    onSave: (label: String, url: String, notes: String) -> Unit,
) {
    var label by remember(initialLabel) { mutableStateOf(initialLabel) }
    var url by remember(initialUrl) { mutableStateOf(initialUrl) }
    var notes by remember(initialNotes) { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pinned repo") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("GitHub URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(label, url, notes) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun QueueItemEditorDialog(
    initialText: String,
    initialRepoLabel: String,
    defaultRepoLabel: String,
    onDismiss: () -> Unit,
    onSave: (text: String, repoLabel: String) -> Unit,
) {
    var text by remember(initialText) { mutableStateOf(initialText) }
    var repoLabel by remember(initialRepoLabel.ifBlank { defaultRepoLabel }) {
        mutableStateOf(initialRepoLabel.ifBlank { defaultRepoLabel })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Queue item") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (defaultRepoLabel.isNotBlank()) {
                    Text(
                        text = "Default repo: $defaultRepoLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                OutlinedTextField(
                    value = repoLabel,
                    onValueChange = { repoLabel = it },
                    label = { Text("Repo label") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Stacked prompt") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text, repoLabel) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
