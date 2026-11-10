package cl.cristian.bitacoraobd.domain.repository

import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    val activeVehicleId: Flow<Long?>
    val obdConnectionConfig: Flow<ObdConnectionConfig>
    val settings: Flow<AppSettings>

    suspend fun setActiveVehicleId(vehicleId: Long?)
    suspend fun saveObdConnectionConfig(config: ObdConnectionConfig)
    suspend fun saveSettings(settings: AppSettings)
}
