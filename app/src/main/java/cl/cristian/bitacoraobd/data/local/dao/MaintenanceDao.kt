package cl.cristian.bitacoraobd.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.cristian.bitacoraobd.data.local.entity.MaintenanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_records WHERE vehicleId = :vehicleId ORDER BY performedDateEpochDay DESC, id DESC")
    fun observeByVehicle(vehicleId: Long): Flow<List<MaintenanceRecordEntity>>

    @Query("SELECT * FROM maintenance_records WHERE id = :id")
    suspend fun getById(id: Long): MaintenanceRecordEntity?

    @Query("SELECT * FROM maintenance_records AS m WHERE m.vehicleId = :vehicleId AND m.serviceType != 'OTHER' AND NOT EXISTS (SELECT 1 FROM maintenance_records AS newer WHERE newer.vehicleId = m.vehicleId AND newer.serviceType = m.serviceType AND (newer.performedDateEpochDay > m.performedDateEpochDay OR (newer.performedDateEpochDay = m.performedDateEpochDay AND newer.id > m.id)))")
    suspend fun latestAlertable(vehicleId: Long): List<MaintenanceRecordEntity>

    @Insert
    suspend fun insert(record: MaintenanceRecordEntity): Long

    @Update
    suspend fun update(record: MaintenanceRecordEntity): Int
}
