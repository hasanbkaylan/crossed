package com.example.domain

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    
    companion object {
        val USERNAME = stringPreferencesKey("username")
        val RADIUS_METERS = intPreferencesKey("radius_meters")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    val usernameFlow: Flow<String> = context.dataStore.data.map { it[USERNAME] ?: "" }
    val radiusFlow: Flow<Int> = context.dataStore.data.map { it[RADIUS_METERS] ?: 100 }
    val hasSeenOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { it[HAS_SEEN_ONBOARDING] ?: false }

    suspend fun setUsername(name: String) {
        context.dataStore.edit { it[USERNAME] = name }
    }

    suspend fun setRadius(meters: Int) {
        context.dataStore.edit { it[RADIUS_METERS] = meters }
    }

    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.dataStore.edit { it[HAS_SEEN_ONBOARDING] = seen }
    }
}
