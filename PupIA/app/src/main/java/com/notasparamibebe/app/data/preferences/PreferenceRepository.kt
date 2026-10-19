package com.notasparamibebe.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferenceRepository(private val context: Context) {

    companion object {
        val KEY_SECRET_CODE = stringPreferencesKey("secret_code")
        val KEY_SENDER_ID = stringPreferencesKey("sender_id")
    }

    val secretCodeFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_SECRET_CODE]
    }

    val senderIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_SENDER_ID]
    }

    suspend fun saveSecretCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SECRET_CODE] = code.trim()
        }
    }

    suspend fun clearSecretCode() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_SECRET_CODE)
        }
    }

    suspend fun getOrCreateSenderId(): String {
        val currentId = context.dataStore.data.map { it[KEY_SENDER_ID] }.first()
        if (!currentId.isNullOrBlank()) {
            return currentId
        }

        val newId = UUID.randomUUID().toString()
        context.dataStore.edit { preferences ->
            if (preferences[KEY_SENDER_ID].isNullOrBlank()) {
                preferences[KEY_SENDER_ID] = newId
            }
        }
        return context.dataStore.data.map { it[KEY_SENDER_ID] ?: newId }.first()
    }
}
