package cl.cristian.bitacoraobd.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import cl.cristian.bitacoraobd.data.local.database.AppDatabase
import cl.cristian.bitacoraobd.data.local.entity.DiagnosticCodeEntity
import cl.cristian.bitacoraobd.data.local.entity.DiagnosticSessionEntity
import cl.cristian.bitacoraobd.data.local.entity.MaintenanceRecordEntity
import cl.cristian.bitacoraobd.data.local.entity.VehicleEntity
import cl.cristian.bitacoraobd.domain.model.MaintenanceType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun deletingVehicleCascadesToSessionsCodesAndMaintenance() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        val sessionId = database.diagnosticDao().insertSession(
            DiagnosticSessionEntity(vehicleId = vehicleId, timestampMillis = 1L, mileageAtScanKm = 10)
        )
        database.diagnosticDao().insertCodes(listOf(DiagnosticCodeEntity(sessionId, "P0123")))
        database.maintenanceDao().insert(
            MaintenanceRecordEntity(
                vehicleId = vehicleId,
                serviceType = MaintenanceType.OIL_ONLY,
                performedDateEpochDay = 1L,
                mileageAtServiceKm = 10,
                notes = "",
                nextDueDateEpochDay = null,
                nextDueMileageKm = null
            )
        )

        database.vehicleDao().deleteById(vehicleId)

        assertNull(database.vehicleDao().getById(vehicleId))
        assertEquals(emptyList<DiagnosticSessionEntity>(), database.diagnosticDao().sessions(vehicleId))
        assertEquals(emptyList<MaintenanceRecordEntity>(), database.maintenanceDao().observeByVehicle(vehicleId).first())
    }

    @Test
    fun updatingVehicleDoesNotDeleteRelatedRecords() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        val sessionId = database.diagnosticDao().insertSession(
            DiagnosticSessionEntity(vehicleId = vehicleId, timestampMillis = 1L, mileageAtScanKm = 10)
        )
        database.vehicleDao().update(vehicle().copy(id = vehicleId, currentMileageKm = 20))

        assertEquals(sessionId, database.diagnosticDao().sessions(vehicleId).single().id)
    }

    @Test
    fun failedOdometerAdvanceRollsBackDiagnosticAndMaintenanceWrites() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_vehicle_mileage BEFORE UPDATE OF currentMileageKm ON vehicles " +
                "BEGIN SELECT RAISE(ABORT, 'forced failure'); END"
        )
        val diagnostics = cl.cristian.bitacoraobd.data.repository.RoomDiagnosticRepository(database)
        val maintenance = cl.cristian.bitacoraobd.data.repository.RoomMaintenanceRepository(database)

        runCatching {
            diagnostics.saveDiagnosticSessionAndAdvanceMileage(
                cl.cristian.bitacoraobd.domain.model.DiagnosticSession(
                    vehicleId = vehicleId, timestampMillis = 1L, mileageAtScanKm = 20, codes = listOf("P0123")
                )
            )
        }
        runCatching {
            maintenance.saveMaintenanceRecord(
                cl.cristian.bitacoraobd.domain.model.MaintenanceRecord(
                    vehicleId = vehicleId, serviceType = MaintenanceType.OIL_ONLY,
                    performedDate = java.time.LocalDate.of(2025, 1, 1),
                    mileageAtServiceKm = 20
                )
            )
        }

        assertEquals(emptyList<DiagnosticSessionEntity>(), database.diagnosticDao().sessions(vehicleId))
        assertEquals(emptyList<MaintenanceRecordEntity>(), database.maintenanceDao().observeByVehicle(vehicleId).first())
        assertEquals(10, database.vehicleDao().getById(vehicleId)?.currentMileageKm)
    }

    private fun vehicle() = VehicleEntity(
        brand = "A", model = "B", year = 2020, fuelType = "Gasoline",
        engineDescription = "1.6", currentMileageKm = 10
    )
}
