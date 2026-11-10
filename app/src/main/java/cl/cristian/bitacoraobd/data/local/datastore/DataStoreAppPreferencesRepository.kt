package cl.cristian.bitacoraobd.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.cristian.bitacoraobd.domain.model.AppSettings
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import cl.cristian.bitacoraobd.domain.model.TemperatureUnit
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

    override val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            displayName = preferences[DISPLAY_NAME],
            distanceUnit = preferences[DISTANCE_UNIT]?.let { runCatching { DistanceUnit.valueOf(it) }.getOrNull() }
                ?: DistanceUnit.KILOMETERS,
            temperatureUnit = preferences[TEMPERATURE_UNIT]?.let { runCatching { TemperatureUnit.valueOf(it) }.getOrNull() }
                ?: TemperatureUnit.CELSIUS,
            showRpm = preferences[SHOW_RPM] ?: true,
            showCoolant = preferences[SHOW_COOLANT] ?: true,
            showSpeed = preferences[SHOW_SPEED] ?: true
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

    override suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit {
            settings.displayName?.let { name -> it[DISPLAY_NAME] = name } ?: it.remove(DISPLAY_NAME)
            it[DISTANCE_UNIT] = settings.distanceUnit.name
            it[TEMPERATURE_UNIT] = settings.temperatureUnit.name
            it[SHOW_RPM] = settings.showRpm
            it[SHOW_COOLANT] = settings.showCoolant
            it[SHOW_SPEED] = settings.showSpeed
        }
    }

    private companion object {
        val ACTIVE_VEHICLE_ID = longPreferencesKey("active_vehicle_id")
        val OBD_IP = stringPreferencesKey("obd_ip")
        val OBD_PORT = intPreferencesKey("obd_port")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val DISTANCE_UNIT = stringPreferencesKey("distance_unit")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val SHOW_RPM = booleanPreferencesKey("show_rpm")
        val SHOW_COOLANT = booleanPreferencesKey("show_coolant")
        val SHOW_SPEED = booleanPreferencesKey("show_speed")
    }
}
