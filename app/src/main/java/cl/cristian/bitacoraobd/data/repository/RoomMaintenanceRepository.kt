package cl.cristian.bitacoraobd.data.repository

import androidx.room.withTransaction
import cl.cristian.bitacoraobd.data.local.database.AppDatabase
import cl.cristian.bitacoraobd.data.local.entity.toDomain
import cl.cristian.bitacoraobd.data.local.entity.toEntity
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.map

class RoomMaintenanceRepository(private val db: AppDatabase) : MaintenanceRepository {
    private val dao get() = db.maintenanceDao()

    override fun observeMaintenanceByVehicle(vehicleId: Long) =
        dao.observeByVehicle(vehicleId).map { it.map { row -> row.toDomain() } }

    override suspend fun getMaintenanceById(recordId: Long) = dao.getById(recordId)?.toDomain()

    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord): Long = db.withTransaction {
        val vehicle = db.vehicleDao().getById(record.vehicleId)
            ?: throw IllegalArgumentException("Vehicle does not exist")
        val id = dao.insert(record.copy(id = 0L).toEntity())
        db.vehicleDao().advanceMileage(record.vehicleId, maxOf(vehicle.currentMileageKm, record.mileageAtServiceKm))
        id
    }

    override suspend fun getLatestRecordsPerAlertableType(vehicleId: Long) =
        dao.latestAlertable(vehicleId).map { it.toDomain() }
}