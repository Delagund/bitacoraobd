package cl.cristian.bitacoraobd.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import cl.cristian.bitacoraobd.data.local.dao.DiagnosticDao
import cl.cristian.bitacoraobd.data.local.dao.MaintenanceDao
import cl.cristian.bitacoraobd.data.local.dao.VehicleDao
import cl.cristian.bitacoraobd.data.local.entity.DiagnosticCodeEntity
import cl.cristian.bitacoraobd.data.local.entity.DiagnosticSessionEntity
import cl.cristian.bitacoraobd.data.local.entity.MaintenanceRecordEntity
import cl.cristian.bitacoraobd.data.local.entity.VehicleEntity
import cl.cristian.bitacoraobd.domain.model.MaintenanceType

@Database(
    entities = [VehicleEntity::class, DiagnosticSessionEntity::class, DiagnosticCodeEntity::class, MaintenanceRecordEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun diagnosticDao(): DiagnosticDao
    abstract fun maintenanceDao(): MaintenanceDao
}

class DatabaseConverters {
    @TypeConverter fun maintenanceTypeToString(value: MaintenanceType) = value.name
    @TypeConverter fun stringToMaintenanceType(value: String) = MaintenanceType.valueOf(value)
}