package cl.cristian.bitacoraobd.data.local.entity
import androidx.room.*
import cl.cristian.bitacoraobd.domain.model.MaintenanceType
@Entity(tableName="vehicles")
data class VehicleEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val brand:String,val model:String,val year:Int,val fuelType:String,val engineDescription:String,val currentMileageKm:Int)
@Entity(tableName="diagnostic_sessions",foreignKeys=[ForeignKey(entity=VehicleEntity::class,parentColumns=["id"],childColumns=["vehicleId"],onDelete=ForeignKey.CASCADE)],indices=[Index("vehicleId"),Index(value=["captureId"],unique=true)])
data class DiagnosticSessionEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val vehicleId:Long,val timestampMillis:Long,val mileageAtScanKm:Int,@ColumnInfo(defaultValue="'legacy-migration-sentinel'") val captureId:String=java.util.UUID.randomUUID().toString())
@Entity(tableName="diagnostic_codes",primaryKeys=["sessionId","code"],foreignKeys=[ForeignKey(entity=DiagnosticSessionEntity::class,parentColumns=["id"],childColumns=["sessionId"],onDelete=ForeignKey.CASCADE)],indices=[Index("sessionId")])
data class DiagnosticCodeEntity(val sessionId:Long,val code:String)
@Entity(tableName="maintenance_records",foreignKeys=[ForeignKey(entity=VehicleEntity::class,parentColumns=["id"],childColumns=["vehicleId"],onDelete=ForeignKey.CASCADE)],indices=[Index("vehicleId"),Index("vehicleId","serviceType","performedDateEpochDay","id")])
data class MaintenanceRecordEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val vehicleId:Long,val serviceType:MaintenanceType,val performedDateEpochDay:Long,val mileageAtServiceKm:Int,val notes:String,val nextDueDateEpochDay:Long?,val nextDueMileageKm:Int?)
