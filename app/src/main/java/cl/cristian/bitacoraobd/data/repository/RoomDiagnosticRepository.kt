package cl.cristian.bitacoraobd.data.repository

import androidx.room.withTransaction
import cl.cristian.bitacoraobd.data.local.database.AppDatabase
import cl.cristian.bitacoraobd.data.local.entity.DiagnosticCodeEntity
import cl.cristian.bitacoraobd.data.local.entity.toDomain
import cl.cristian.bitacoraobd.data.local.entity.toEntity
import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.model.DtcGuidance
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository
import kotlinx.coroutines.flow.map

class RoomDiagnosticRepository(private val db: AppDatabase) : DiagnosticRepository {
    private val dao get() = db.diagnosticDao()

    override fun observeSessionsByVehicle(vehicleId: Long) =
        dao.observeSessions(vehicleId).map { sessions -> sessions.map { it.toDomain() } }

    override suspend fun getLatestSession(vehicleId: Long) = dao.latestSession(vehicleId)?.toDomain()
    override suspend fun getSessionById(sessionId: Long) = dao.sessionById(sessionId)?.toDomain()

    override suspend fun saveDiagnosticSessionAndAdvanceMileage(session: DiagnosticSession): Long =
        db.withTransaction {
            val vehicle = db.vehicleDao().getById(session.vehicleId)
                ?: throw IllegalArgumentException("Vehicle does not exist")
            val insertedId = dao.insertSession(session.copy(id = 0L).toEntity())
            if (insertedId == -1L) {
                val existing = dao.sessionByCaptureId(session.captureId)
                    ?: error("Conflicting diagnostic capture was not found")
                val sameCapture = existing.session.vehicleId == session.vehicleId &&
                    existing.session.timestampMillis == session.timestampMillis &&
                    existing.session.mileageAtScanKm == session.mileageAtScanKm &&
                    existing.codes.map { it.code }.toSet() == session.codes.toSet()
                require(sameCapture) { "Capture id already belongs to different diagnostic data" }
                return@withTransaction existing.session.id
            }

            if (session.codes.isNotEmpty()) {
                dao.insertCodes(session.codes.distinct().map { DiagnosticCodeEntity(insertedId, it) })
            }
            db.vehicleDao().advanceMileage(session.vehicleId, maxOf(vehicle.currentMileageKm, session.mileageAtScanKm))
            insertedId
        }

    override suspend fun getSuccessfulCodesBefore(vehicleId: Long, timestampMillis: Long, sessionId: Long) =
        dao.codesBefore(vehicleId, timestampMillis, sessionId).toSet()

    override suspend fun getSuccessfulCodeOccurrenceCounts(vehicleId: Long) =
        dao.occurrenceCounts(vehicleId).associate { it.code to it.count }

    override fun getDtcGuidance(code: String): DtcGuidance = DtcCatalog.lookup(code)
}