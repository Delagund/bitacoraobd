package cl.cristian.bitacoraobd.domain.repository

import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    fun observeMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>>
    suspend fun getMaintenanceById(recordId: Long): MaintenanceRecord?
    suspend fun saveMaintenanceRecord(record: MaintenanceRecord): Long
    suspend fun getLatestRecordsPerAlertableType(vehicleId: Long): List<MaintenanceRecord>
}
