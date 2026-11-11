package cl.cristian.bitacoraobd.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.appPreferencesDataStore by preferencesDataStore(name = "bitacora_preferences")
