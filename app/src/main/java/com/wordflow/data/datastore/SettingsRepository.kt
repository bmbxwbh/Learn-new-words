
package com.wordflow.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DAILY_NEW_WORDS = intPreferencesKey("daily_new_words")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    // 读取每日新词数量
    val dailyNewWords: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DAILY_NEW_WORDS] ?: 20 // 默认20
        }

    // 读取深色模式设置
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] ?: false // 默认跟随系统
        }

    // 保存每日新词数量
    suspend fun saveDailyNewWords(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_NEW_WORDS] = count
        }
    }

    // 保存深色模式设置
    suspend fun saveDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }
}

