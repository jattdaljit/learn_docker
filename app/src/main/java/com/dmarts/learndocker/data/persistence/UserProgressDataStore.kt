package com.dmarts.learndocker.data.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dmarts.learndocker.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_progress")

private val PROGRESS_KEY = stringPreferencesKey("progress_json")

class UserProgressDataStore(
    private val context: Context,
    private val json: Json
) {
    val progressFlow: Flow<UserProgress> = context.dataStore.data.map { prefs ->
        val raw = prefs[PROGRESS_KEY] ?: return@map UserProgress()
        runCatching { json.decodeFromString<UserProgress>(raw) }.getOrDefault(UserProgress())
    }

    suspend fun save(progress: UserProgress) {
        context.dataStore.edit { prefs ->
            prefs[PROGRESS_KEY] = json.encodeToString(progress)
        }
    }
}
