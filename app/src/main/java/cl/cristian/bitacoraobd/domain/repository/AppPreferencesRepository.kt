package cl.cristian.bitacoraobd.domain.repository

import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    val activeVehicleId: Flow<Long?>
    val obdConnectionConfig: Flow<ObdConnectionConfig>

    suspend fun setActiveVehicleId(vehicleId: Long?)
    suspend fun saveObdConnectionConfig(config: ObdConnectionConfig)
}
