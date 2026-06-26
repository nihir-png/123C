package com.smart.aicalculator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_calculator_settings")

class AppSettingsStore(private val context: Context) {

    companion object {
        // First-launch setup flag. Key string kept as "onboarding_completed"
        // so existing installs aren't reset; exposed as setupCompleted below.
        val SETUP_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"
        val APP_LANGUAGE = stringPreferencesKey("app_language") // "system", "en", "hi", "es", "fr", "de", "pt"
        val CURRENCY_DEFAULT = stringPreferencesKey("currency_default") // "USD", "INR", "EUR", etc.
        val DECIMAL_PRECISION = intPreferencesKey("decimal_precision")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val CURRENCY_RATES = stringPreferencesKey("currency_rates") // "USD=1.0;EUR=0.92;..."
        val CURRENCY_RATES_TIME = longPreferencesKey("currency_rates_time")
    }

    val isSetupCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SETUP_COMPLETED] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_LANGUAGE] ?: "system"
    }

    val currencyDefault: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_DEFAULT] ?: "INR"
    }

    val decimalPrecision: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DECIMAL_PRECISION] ?: 4
    }

    val isVibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    val isSoundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SOUND_ENABLED] ?: false
    }

    suspend fun setSetupCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SETUP_COMPLETED] = completed
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = language
        }
    }

    suspend fun setCurrencyDefault(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_DEFAULT] = currency
        }
    }

    suspend fun setDecimalPrecision(precision: Int) {
        context.dataStore.edit { preferences ->
            preferences[DECIMAL_PRECISION] = precision
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    // Cached currency exchange rates (last successful fetch), so the converter
    // keeps working offline.
    val cachedCurrencyRates: Flow<String> = context.dataStore.data.map { it[CURRENCY_RATES] ?: "" }
    val currencyRatesUpdatedAt: Flow<Long> = context.dataStore.data.map { it[CURRENCY_RATES_TIME] ?: 0L }

    suspend fun setCurrencyRates(serialized: String, updatedAt: Long) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_RATES] = serialized
            preferences[CURRENCY_RATES_TIME] = updatedAt
        }
    }
}
