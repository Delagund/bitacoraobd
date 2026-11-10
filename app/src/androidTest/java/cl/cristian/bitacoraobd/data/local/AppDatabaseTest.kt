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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
        assertNull(database.diagnosticDao().sessionById(sessionId))
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM diagnostic_codes WHERE sessionId = $sessionId").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
        assertEquals(emptyList<MaintenanceRecordEntity>(), database.maintenanceDao().observeByVehicle(vehicleId).first())
    }

    @Test
    fun editingMaintenanceKeepsIdAndRecomputesLatest() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle().copy(year = 0))
        val repository = cl.cristian.bitacoraobd.data.repository.RoomMaintenanceRepository(database)
        val first = cl.cristian.bitacoraobd.domain.model.MaintenanceRecord(
            vehicleId = vehicleId, serviceType = MaintenanceType.BRAKES,
            performedDate = java.time.LocalDate.of(2025, 1, 1), mileageAtServiceKm = 10, notes = "original"
        )
        val firstId = repository.saveMaintenanceRecord(first)
        val secondId = repository.saveMaintenanceRecord(first.copy(performedDate = java.time.LocalDate.of(2025, 2, 1)))
        assertEquals(firstId, repository.saveMaintenanceRecord(first.copy(id = firstId, performedDate = java.time.LocalDate.of(2025, 3, 1), notes = "edited")))
        assertEquals(2, repository.observeMaintenanceByVehicle(vehicleId).first().size)
        assertEquals(firstId, repository.getLatestRecordsPerAlertableType(vehicleId).single().id)
        assertEquals("edited", repository.getMaintenanceById(firstId)?.notes)
        assertEquals(0, database.vehicleDao().getById(vehicleId)?.year)
        assertEquals(true, runCatching { repository.saveMaintenanceRecord(first.copy(id = secondId + 100)) }.isFailure)
    }

    @Test
    fun editingNotesDoesNotUndoManualOdometerCorrection() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle().copy(currentMileageKm = 100))
        val repository = cl.cristian.bitacoraobd.data.repository.RoomMaintenanceRepository(database)
        val record = cl.cristian.bitacoraobd.domain.model.MaintenanceRecord(
            vehicleId = vehicleId, serviceType = MaintenanceType.BRAKES,
            performedDate = java.time.LocalDate.of(2025, 1, 1), mileageAtServiceKm = 100
        )
        val id = repository.saveMaintenanceRecord(record)
        database.vehicleDao().update(vehicle().copy(id = vehicleId, currentMileageKm = 50))
        repository.saveMaintenanceRecord(record.copy(id = id, notes = "corregido"))
        assertEquals(50, database.vehicleDao().getById(vehicleId)?.currentMileageKm)
        assertEquals("corregido", repository.getMaintenanceById(id)?.notes)
    }

    @Test
    fun newMaintenanceTypesAreIndependentlyAlertable() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        val types = listOf(MaintenanceType.BRAKES, MaintenanceType.TIRES, MaintenanceType.BATTERY, MaintenanceType.COOLANT)
        types.forEach { database.maintenanceDao().insert(maintenance(vehicleId, it, 1L)) }
        database.maintenanceDao().insert(maintenance(vehicleId, MaintenanceType.OTHER, 1L))
        assertEquals(types.toSet(), database.maintenanceDao().latestAlertable(vehicleId).map { it.serviceType }.toSet())
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

        val diagnosticFailure = runCatching {
            diagnostics.saveDiagnosticSessionAndAdvanceMileage(
                cl.cristian.bitacoraobd.domain.model.DiagnosticSession(
                    vehicleId = vehicleId, timestampMillis = 1L, mileageAtScanKm = 20, codes = listOf("P0123")
                )
            )
        }
        val maintenanceFailure = runCatching {
            maintenance.saveMaintenanceRecord(
                cl.cristian.bitacoraobd.domain.model.MaintenanceRecord(
                    vehicleId = vehicleId, serviceType = MaintenanceType.OIL_ONLY,
                    performedDate = java.time.LocalDate.of(2025, 1, 1),
                    mileageAtServiceKm = 20
                )
            )
        }

        assertEquals(true, diagnosticFailure.isFailure)
        assertEquals(true, maintenanceFailure.isFailure)
        assertEquals(emptyList<DiagnosticSessionEntity>(), database.diagnosticDao().sessions(vehicleId))
        assertEquals(emptyList<MaintenanceRecordEntity>(), database.maintenanceDao().observeByVehicle(vehicleId).first())
        assertEquals(10, database.vehicleDao().getById(vehicleId)?.currentMileageKm)
    }

    @Test
    fun diagnosticQueriesKeepSessionsCodesOrderingAndVehicleIsolation() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        val otherVehicleId = database.vehicleDao().insert(vehicle().copy(brand = "Other"))
        val oldId = database.diagnosticDao().insertSession(
            DiagnosticSessionEntity(vehicleId = vehicleId, timestampMillis = 1L, mileageAtScanKm = 10)
        )
        val emptyId = database.diagnosticDao().insertSession(
            DiagnosticSessionEntity(vehicleId = vehicleId, timestampMillis = 0L, mileageAtScanKm = 10)
        )
        val firstId = database.diagnosticDao().insertSession(
            DiagnosticSessionEntity(vehicleId = vehicleId, timestampMillis = 2L, mileageAtScanKm = 10)
        )
        val latestId = database.diagnosticDao().insertSession(
            DiagnosticSessionEntity(vehicleId = vehicleId, timestampMillis = 2L, mileageAtScanKm = 10)
        )
        val otherVehicleSessionId = database.diagnosticDao().insertSession(
            DiagnosticSessionEntity(vehicleId = otherVehicleId, timestampMillis = 3L, mileageAtScanKm = 10)
        )
        database.diagnosticDao().insertCodes(
            listOf(
                DiagnosticCodeEntity(oldId, "P0123"),
                DiagnosticCodeEntity(firstId, "P0123"),
                DiagnosticCodeEntity(firstId, "P0789"),
                DiagnosticCodeEntity(latestId, "P0456"),
                DiagnosticCodeEntity(otherVehicleSessionId, "P0123")
            )
        )

        val sessions = database.diagnosticDao().observeSessions(vehicleId).first()
        assertEquals(listOf(latestId, firstId, oldId, emptyId), sessions.map { it.session.id })
        assertEquals(listOf("P0456"), sessions.first().codes.map { it.code })
        assertEquals(emptyList<DiagnosticCodeEntity>(), sessions.last().codes)
        assertEquals(
            setOf("P0123", "P0789"),
            database.diagnosticDao().codesBefore(vehicleId, 2L, latestId).toSet()
        )
        assertEquals(
            setOf("P0123"),
            database.diagnosticDao().codesBefore(vehicleId, 2L, firstId).toSet()
        )
        assertEquals(2, database.diagnosticDao().occurrenceCounts(vehicleId).single { it.code == "P0123" }.count)
    }

    @Test
    fun maintenanceQueriesChooseLatestPerTypeByDateThenIdAndExcludeOther() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        database.maintenanceDao().insert(maintenance(vehicleId, MaintenanceType.OIL_ONLY, 10L))
        database.maintenanceDao().insert(maintenance(vehicleId, MaintenanceType.OIL_ONLY, 20L))
        val tiedNewerId = database.maintenanceDao().insert(maintenance(vehicleId, MaintenanceType.OIL_ONLY, 20L))
        val filterId = database.maintenanceDao().insert(maintenance(vehicleId, MaintenanceType.FILTER_ONLY, 15L))
        database.maintenanceDao().insert(maintenance(vehicleId, MaintenanceType.OTHER, 30L))

        val latest = database.maintenanceDao().latestAlertable(vehicleId)

        assertEquals(setOf(tiedNewerId, filterId), latest.map { it.id }.toSet())
        assertEquals(tiedNewerId, latest.first { it.serviceType == MaintenanceType.OIL_ONLY }.id)
    }

    @Test
    fun maintenanceMileageHistoryDoesNotLowerOdometerButHigherMileageAdvancesIt() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle().copy(currentMileageKm = 50_000))
        val maintenance = cl.cristian.bitacoraobd.data.repository.RoomMaintenanceRepository(database)

        maintenance.saveMaintenanceRecord(
            cl.cristian.bitacoraobd.domain.model.MaintenanceRecord(
                vehicleId = vehicleId, serviceType = MaintenanceType.OIL_ONLY,
                performedDate = java.time.LocalDate.of(2025, 1, 1), mileageAtServiceKm = 40_000
            )
        )
        assertEquals(50_000, database.vehicleDao().getById(vehicleId)?.currentMileageKm)

        maintenance.saveMaintenanceRecord(
            cl.cristian.bitacoraobd.domain.model.MaintenanceRecord(
                vehicleId = vehicleId, serviceType = MaintenanceType.FILTER_ONLY,
                performedDate = java.time.LocalDate.of(2025, 2, 1), mileageAtServiceKm = 60_000
            )
        )
        assertEquals(60_000, database.vehicleDao().getById(vehicleId)?.currentMileageKm)

        database.vehicleDao().update(vehicle().copy(id = vehicleId, currentMileageKm = 35_000))
        assertEquals(35_000, database.vehicleDao().getById(vehicleId)?.currentMileageKm)
    }

    @Test
    fun sqliteEnforcesForeignKeysAndUniqueDiagnosticCodes() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        val sessionId = database.diagnosticDao().insertSession(
            DiagnosticSessionEntity(vehicleId = vehicleId, timestampMillis = 1L, mileageAtScanKm = 10)
        )
        database.diagnosticDao().insertCodes(listOf(DiagnosticCodeEntity(sessionId, "P0123")))

        val duplicateRejected = runCatching {
            database.diagnosticDao().insertCodes(listOf(DiagnosticCodeEntity(sessionId, "P0123")))
        }.isFailure
        val orphanRejected = runCatching {
            database.diagnosticDao().insertSession(
                DiagnosticSessionEntity(vehicleId = Long.MAX_VALUE, timestampMillis = 2L, mileageAtScanKm = 10)
            )
        }.isFailure

        assertEquals(true, duplicateRejected)
        assertEquals(true, orphanRejected)
    }

    @Test
    fun sqliteMetadataMatchesForeignKeysPrimaryKeysAndRoomIndexes() = runBlocking {
        val sqlite = database.openHelper.writableDatabase

        fun rows(query: String, columns: Int): List<List<String?>> =
            sqlite.query(query).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add((0 until columns).map { index ->
                            if (cursor.isNull(index)) null else cursor.getString(index)
                        })
                    }
                }
            }

        val sessionForeignKey = rows("PRAGMA foreign_key_list(diagnostic_sessions)", 8).single()
        assertEquals("vehicles", sessionForeignKey[2])
        assertEquals("vehicleId", sessionForeignKey[3])
        assertEquals("id", sessionForeignKey[4])
        assertEquals("CASCADE", sessionForeignKey[6])

        val codeForeignKey = rows("PRAGMA foreign_key_list(diagnostic_codes)", 8).single()
        assertEquals("diagnostic_sessions", codeForeignKey[2])
        assertEquals("sessionId", codeForeignKey[3])
        assertEquals("id", codeForeignKey[4])
        assertEquals("CASCADE", codeForeignKey[6])

        val maintenanceForeignKey = rows("PRAGMA foreign_key_list(maintenance_records)", 8).single()
        assertEquals("vehicles", maintenanceForeignKey[2])
        assertEquals("vehicleId", maintenanceForeignKey[3])
        assertEquals("id", maintenanceForeignKey[4])
        assertEquals("CASCADE", maintenanceForeignKey[6])

        val codePrimaryKey = rows("PRAGMA table_info(diagnostic_codes)", 6)
            .filter { it[5] != "0" }
            .sortedBy { it[5]!!.toInt() }
        assertEquals(listOf("sessionId", "code"), codePrimaryKey.map { it[1] })

        fun indexColumns(table: String): Set<List<String>> {
            val names = rows("PRAGMA index_list($table)", 5).map { it[1] }
            return names.map { indexName ->
                rows("PRAGMA index_info($indexName)", 5).sortedBy { it[0]!!.toInt() }.mapNotNull { it[2] }
            }.toSet()
        }

        assertEquals(setOf(listOf("vehicleId"), listOf("captureId")), indexColumns("diagnostic_sessions"))
        assertEquals(
            setOf(listOf("sessionId"), listOf("sessionId", "code")),
            indexColumns("diagnostic_codes")
        )
        assertEquals(
            setOf(
                listOf("vehicleId"),
                listOf("vehicleId", "serviceType", "performedDateEpochDay", "id")
            ),
            indexColumns("maintenance_records")
        )
    }

    @Test
    fun diagnosticCaptureSaveIsIdempotentAndRejectsDifferentPayload() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        val diagnostics = cl.cristian.bitacoraobd.data.repository.RoomDiagnosticRepository(database)
        val capture = cl.cristian.bitacoraobd.domain.model.DiagnosticSession(
            vehicleId = vehicleId,
            timestampMillis = 10L,
            mileageAtScanKm = 20,
            codes = listOf("P0123"),
            captureId = "capture-1"
        )

        val firstId = diagnostics.saveDiagnosticSessionAndAdvanceMileage(capture)
        val retryId = diagnostics.saveDiagnosticSessionAndAdvanceMileage(capture)

        assertEquals(firstId, retryId)
        assertEquals(1, database.diagnosticDao().sessions(vehicleId).size)
        assertEquals(listOf("P0123"), diagnostics.getSessionById(firstId)?.codes)
        assertEquals(20, database.vehicleDao().getById(vehicleId)?.currentMileageKm)

        val conflict = runCatching {
            diagnostics.saveDiagnosticSessionAndAdvanceMileage(capture.copy(codes = listOf("P0456")))
        }
        assertEquals(true, conflict.isFailure)
        assertEquals(listOf("P0123"), diagnostics.getSessionById(firstId)?.codes)
        assertEquals(1, database.diagnosticDao().sessions(vehicleId).size)
    }

    @Test
    fun concurrentSavesOfSameDiagnosticCaptureReturnOneSessionId() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        val diagnostics = cl.cristian.bitacoraobd.data.repository.RoomDiagnosticRepository(database)
        val capture = cl.cristian.bitacoraobd.domain.model.DiagnosticSession(
            vehicleId = vehicleId,
            timestampMillis = 10L,
            mileageAtScanKm = 20,
            codes = listOf("P0123", "P0123"),
            captureId = "capture-concurrent"
        )

        val ids = coroutineScope {
            (1..8).map {
                async(Dispatchers.IO) {
                    diagnostics.saveDiagnosticSessionAndAdvanceMileage(capture)
                }
            }.awaitAll()
        }

        assertEquals(1, ids.toSet().size)
        assertEquals(1, database.diagnosticDao().sessions(vehicleId).size)
        assertEquals(listOf("P0123"), diagnostics.getSessionById(ids.first())?.codes)
    }

    @Test
    fun differentCaptureIdsWithIdenticalPayloadCreateDifferentSessions() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        val diagnostics = cl.cristian.bitacoraobd.data.repository.RoomDiagnosticRepository(database)
        val payload = cl.cristian.bitacoraobd.domain.model.DiagnosticSession(
            vehicleId = vehicleId, timestampMillis = 12L, mileageAtScanKm = 10, codes = listOf("P0123")
        )

        val firstId = diagnostics.saveDiagnosticSessionAndAdvanceMileage(payload.copy(captureId = "capture-a"))
        val secondId = diagnostics.saveDiagnosticSessionAndAdvanceMileage(payload.copy(captureId = "capture-b"))

        assertEquals(false, firstId == secondId)
        assertEquals(2, database.diagnosticDao().sessions(vehicleId).size)
    }

    @Test
    fun diagnosticRollbackCanRetrySameCapture() = runBlocking {
        val vehicleId = database.vehicleDao().insert(vehicle())
        val diagnostics = cl.cristian.bitacoraobd.data.repository.RoomDiagnosticRepository(database)
        val capture = cl.cristian.bitacoraobd.domain.model.DiagnosticSession(
            vehicleId = vehicleId, timestampMillis = 15L, mileageAtScanKm = 20,
            codes = listOf("P0123"), captureId = "capture-retry"
        )
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_vehicle_mileage BEFORE UPDATE OF currentMileageKm ON vehicles " +
                "BEGIN SELECT RAISE(ABORT, 'forced failure'); END"
        )

        assertEquals(true, runCatching {
            diagnostics.saveDiagnosticSessionAndAdvanceMileage(capture)
        }.isFailure)
        assertEquals(emptyList<DiagnosticSessionEntity>(), database.diagnosticDao().sessions(vehicleId))
        assertEquals(10, database.vehicleDao().getById(vehicleId)?.currentMileageKm)

        database.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_vehicle_mileage")
        val id = diagnostics.saveDiagnosticSessionAndAdvanceMileage(capture)

        assertEquals(id, diagnostics.saveDiagnosticSessionAndAdvanceMileage(capture))
        assertEquals(1, database.diagnosticDao().sessions(vehicleId).size)
        assertEquals(listOf("P0123"), diagnostics.getSessionById(id)?.codes)
        assertEquals(20, database.vehicleDao().getById(vehicleId)?.currentMileageKm)
    }

    private fun maintenance(vehicleId: Long, type: MaintenanceType, day: Long) =
        MaintenanceRecordEntity(
            vehicleId = vehicleId,
            serviceType = type,
            performedDateEpochDay = day,
            mileageAtServiceKm = 10,
            notes = "",
            nextDueDateEpochDay = null,
            nextDueMileageKm = null
        )

    private fun vehicle() = VehicleEntity(
        brand = "A", model = "B", year = 2020, fuelType = "Gasoline",
        engineDescription = "1.6", currentMileageKm = 10
    )
}
