package com.dmarts.learndocker.data.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dmarts.learndocker.domain.model.SimulatorState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.simulatorDataStore: DataStore<Preferences> by preferencesDataStore(name = "simulator_state")

private val SIMULATOR_KEY = stringPreferencesKey("simulator_state_json")

class SimulatorStateDataStore(
    private val context: Context,
    private val json: Json
) {
    val stateFlow: Flow<SimulatorState?> = context.simulatorDataStore.data.map { prefs ->
        val raw = prefs[SIMULATOR_KEY] ?: return@map null
        runCatching { json.decodeFromString<SimulatorState>(raw) }.getOrNull()
    }

    suspend fun save(state: SimulatorState) {
        context.simulatorDataStore.edit { prefs ->
            prefs[SIMULATOR_KEY] = json.encodeToString(state)
        }
    }

    suspend fun clear() {
        context.simulatorDataStore.edit { prefs ->
            prefs.remove(SIMULATOR_KEY)
        }
    }
}
