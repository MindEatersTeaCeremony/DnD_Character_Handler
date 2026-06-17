package com.dndcharacterhandler.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dndcharacterhandler.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class LanguagePreferencesRepository(private val context: Context) {
    private val key = stringPreferencesKey("app_language")

    val language: Flow<AppLanguage> = context.dataStore.data.map { preferences ->
        preferences[key]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() } ?: AppLanguage.ENGLISH
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[key] = language.name
        }
    }
}

