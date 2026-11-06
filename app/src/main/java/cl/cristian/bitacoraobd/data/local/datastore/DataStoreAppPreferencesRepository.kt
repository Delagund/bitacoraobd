package cl.cristian.bitacoraobd.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreAppPreferencesRepository(private val dataStore: DataStore<Preferences>) : AppPreferencesRepository {
    override val activeVehicleId: Flow<Long?> =
        dataStore.data.map { it[ACTIVE_VEHICLE_ID] }

    override val obdConnectionConfig: Flow<ObdConnectionConfig> = dataStore.data.map { preferences ->
        ObdConnectionConfig(
            preferences[OBD_IP] ?: ObdConnectionConfig.DEFAULT_IP,
            preferences[OBD_PORT] ?: ObdConnectionConfig.DEFAULT_PORT
        )
    }

    override suspend fun setActiveVehicleId(vehicleId: Long?) {
        require(vehicleId == null || vehicleId > 0L)
        dataStore.edit {
            if (vehicleId == null) it.remove(ACTIVE_VEHICLE_ID) else it[ACTIVE_VEHICLE_ID] = vehicleId
        }
    }

    override suspend fun saveObdConnectionConfig(config: ObdConnectionConfig) {
        dataStore.edit {
            it[OBD_IP] = config.ipAddress
            it[OBD_PORT] = config.port
        }
    }

    private companion object {
        val ACTIVE_VEHICLE_ID = longPreferencesKey("active_vehicle_id")
        val OBD_IP = stringPreferencesKey("obd_ip")
        val OBD_PORT = intPreferencesKey("obd_port")
    }
}