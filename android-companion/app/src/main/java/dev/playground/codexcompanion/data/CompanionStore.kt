package dev.playground.codexcompanion.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val STORE_NAME = "codex_companion_store"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = STORE_NAME)

class CompanionStore(
    private val context: Context,
) {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val stateKey = stringPreferencesKey("companion_state")

    val state: Flow<CompanionState> =
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { prefs -> decodeState(prefs[stateKey]) }
            .distinctUntilChanged()

    suspend fun update(transform: (CompanionState) -> CompanionState) {
        context.dataStore.edit { prefs ->
            val current = decodeState(prefs[stateKey])
            prefs[stateKey] = json.encodeToString(transform(current))
        }
    }

    private fun decodeState(raw: String?): CompanionState {
        if (raw.isNullOrBlank()) {
            return CompanionState()
        }

        return runCatching { json.decodeFromString<CompanionState>(raw) }
            .getOrElse { CompanionState() }
    }
}
