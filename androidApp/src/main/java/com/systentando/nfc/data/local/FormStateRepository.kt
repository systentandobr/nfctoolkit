package com.systentando.nfc.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.systentando.nfc.domain.model.WritePayload
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.formDataStore: DataStore<Preferences> by preferencesDataStore(name = "form_state")

@Singleton
class FormStateRepository @Inject constructor(
    private val context: Context
) {
    private val gson = Gson()
    private val dataStore = context.formDataStore

    suspend fun save(type: WritePayload.Type, payload: WritePayload) {
        dataStore.edit { prefs ->
            prefs[KEY(type)] = gson.toJson(payload, payload.javaClass)
        }
    }

    suspend fun load(type: WritePayload.Type): WritePayload? {
        val json = dataStore.data.map { it[KEY(type)] }.first() ?: return null
        val targetClass = when (type) {
            WritePayload.Type.TEXT -> WritePayload.Text::class.java
            WritePayload.Type.URL -> WritePayload.Url::class.java
            WritePayload.Type.EMAIL -> WritePayload.Email::class.java
            WritePayload.Type.LOCATION -> WritePayload.Location::class.java
            WritePayload.Type.WIFI -> WritePayload.WiFi::class.java
            WritePayload.Type.PHONE -> WritePayload.Phone::class.java
            WritePayload.Type.GOOGLE_BUSINESS -> WritePayload.GoogleBusiness::class.java
            WritePayload.Type.RAW -> WritePayload.Raw::class.java
        }
        return try {
            gson.fromJson(json, targetClass)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clear(type: WritePayload.Type) {
        dataStore.edit { it.remove(KEY(type)) }
    }

    private companion object {
        private fun KEY(type: WritePayload.Type) = stringPreferencesKey("payload_${type.name}")
    }
}
