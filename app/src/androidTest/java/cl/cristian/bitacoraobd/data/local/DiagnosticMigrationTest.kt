package cl.cristian.bitacoraobd.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.cristian.bitacoraobd.data.local.database.AppDatabase
import cl.cristian.bitacoraobd.data.local.database.MIGRATION_1_2
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiagnosticMigrationTest {
    @Test
    fun migrationFromVersionOneAssignsUniqueCaptureIdsAndPreservesData() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-v1-" + System.nanoTime() + ".db"
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        createVersionOneDatabase(file)

        val database = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2)
            .build()

        try {
            val vehicle = database.vehicleDao().getById(1L)
            val sessions = database.diagnosticDao().observeSessions(1L).first()
            val maintenance = database.maintenanceDao().observeByVehicle(1L).first()

            assertEquals("Toyota", vehicle?.brand)
            assertEquals(listOf(8L, 7L), sessions.map { it.session.id })
            assertEquals(listOf(listOf("P0123"), emptyList<String>()), sessions.map { it.codes.map { code -> code.code } })
            assertNotEquals(sessions[0].session.captureId, sessions[1].session.captureId)
            assertTrue(sessions.all { it.session.captureId.isNotBlank() && it.session.captureId != "legacy-migration-sentinel" })
            assertEquals(1, maintenance.size)
            assertEquals(1234L, sessions[0].session.timestampMillis)
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }

    private fun createVersionOneDatabase(file: File) {
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            db.execSQL("CREATE TABLE vehicles (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, brand TEXT NOT NULL, model TEXT NOT NULL, year INTEGER NOT NULL, fuelType TEXT NOT NULL, engineDescription TEXT NOT NULL, currentMileageKm INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE diagnostic_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, vehicleId INTEGER NOT NULL, timestampMillis INTEGER NOT NULL, mileageAtScanKm INTEGER NOT NULL, FOREIGN KEY(vehicleId) REFERENCES vehicles(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            db.execSQL("CREATE INDEX index_diagnostic_sessions_vehicleId ON diagnostic_sessions(vehicleId)")
            db.execSQL("CREATE TABLE diagnostic_codes (sessionId INTEGER NOT NULL, code TEXT NOT NULL, PRIMARY KEY(sessionId, code), FOREIGN KEY(sessionId) REFERENCES diagnostic_sessions(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            db.execSQL("CREATE INDEX index_diagnostic_codes_sessionId ON diagnostic_codes(sessionId)")
            db.execSQL("CREATE TABLE maintenance_records (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, vehicleId INTEGER NOT NULL, serviceType TEXT NOT NULL, performedDateEpochDay INTEGER NOT NULL, mileageAtServiceKm INTEGER NOT NULL, notes TEXT NOT NULL, nextDueDateEpochDay INTEGER, nextDueMileageKm INTEGER, FOREIGN KEY(vehicleId) REFERENCES vehicles(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            db.execSQL("CREATE INDEX index_maintenance_records_vehicleId ON maintenance_records(vehicleId)")
            db.execSQL("CREATE INDEX index_maintenance_records_vehicleId_serviceType_performedDateEpochDay_id ON maintenance_records(vehicleId, serviceType, performedDateEpochDay, id)")
            db.execSQL("INSERT INTO vehicles VALUES (1, 'Toyota', 'Corolla', 2020, 'Gasoline', '1.8', 50000)")
            db.execSQL("INSERT INTO diagnostic_sessions VALUES (7, 1, 1000, 40000)")
            db.execSQL("INSERT INTO diagnostic_sessions VALUES (8, 1, 1234, 50000)")
            db.execSQL("INSERT INTO diagnostic_codes VALUES (8, 'P0123')")
            db.execSQL("INSERT INTO maintenance_records VALUES (9, 1, 'OIL_ONLY', 20000, 45000, 'service', NULL, NULL)")
            db.version = 1
        } finally {
            db.close()
        }
    }
}
