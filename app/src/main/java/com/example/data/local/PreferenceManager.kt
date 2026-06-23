package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "expense_tracker_preferences")

class PreferenceManager(private val context: Context) {

    companion object {
        val KEY_NAME = stringPreferencesKey("username")
        val KEY_ACTIVE_SPACE_ID = stringPreferencesKey("active_space_id")
        val KEY_BASE_CURRENCY = stringPreferencesKey("base_currency")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock_enabled")
        val KEY_PIN_LOCK = stringPreferencesKey("pin_lock_code")
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_EXCHANGE_RATES_JSON = stringPreferencesKey("exchange_rates")
        val KEY_LAST_SYNC_RATES = longPreferencesKey("last_sync_rates")
    }

    val usernameFlow: Flow<String?> = context.dataStore.data.map { it[KEY_NAME] }
    val activeSpaceIdFlow: Flow<String?> = context.dataStore.data.map { it[KEY_ACTIVE_SPACE_ID] }
    val baseCurrencyFlow: Flow<String> = context.dataStore.data.map { it[KEY_BASE_CURRENCY] ?: "USD" }
    val languageFlow: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "English" }
    val themeFlow: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "SYSTEM" }
    val biometricLockFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_BIOMETRIC_LOCK] ?: false }
    val pinLockFlow: Flow<String?> = context.dataStore.data.map { it[KEY_PIN_LOCK] }
    val onboardingCompleteFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_COMPLETE] ?: false }
    val exchangeRatesFlow: Flow<String> = context.dataStore.data.map { it[KEY_EXCHANGE_RATES_JSON] ?: "{\"USD\":1.0,\"BDT\":118.0,\"EUR\":0.92,\"GBP\":0.79,\"JPY\":157.0,\"INR\":83.4}" }
    val lastSyncRatesFlow: Flow<Long> = context.dataStore.data.map { it[KEY_LAST_SYNC_RATES] ?: 0L }

    suspend fun setUsername(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NAME] = name
        }
    }

    suspend fun setActiveSpaceId(spaceId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_SPACE_ID] = spaceId
        }
    }

    suspend fun setBaseCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BASE_CURRENCY] = currency
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = lang
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_LOCK] = enabled
        }
    }

    suspend fun setPinLock(pin: String?) {
        context.dataStore.edit { preferences ->
            if (pin == null) {
                preferences.remove(KEY_PIN_LOCK)
            } else {
                preferences[KEY_PIN_LOCK] = pin
            }
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun saveExchangeRates(ratesJson: String, timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_EXCHANGE_RATES_JSON] = ratesJson
            preferences[KEY_LAST_SYNC_RATES] = timestamp
        }
    }
}
