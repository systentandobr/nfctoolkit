package com.systentando.nfc.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")

@Singleton
class OnboardingRepository @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.onboardingDataStore

    suspend fun setOnboardingSeen() {
        dataStore.edit { prefs ->
            prefs[SEEN_KEY] = true
        }
    }

    suspend fun isOnboardingSeen(): Boolean {
        return dataStore.data.map { it[SEEN_KEY] ?: false }.first()
    }

    private companion object {
        val SEEN_KEY = booleanPreferencesKey("onboarding_seen")
    }
}
