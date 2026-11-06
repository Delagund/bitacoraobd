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
            val sessionId = dao.insertSession(session.copy(id = 0L).toEntity())
            if (session.codes.isNotEmpty()) {
                dao.insertCodes(session.codes.distinct().map { DiagnosticCodeEntity(sessionId, it) })
            }
            db.vehicleDao().advanceMileage(session.vehicleId, maxOf(vehicle.currentMileageKm, session.mileageAtScanKm))
            sessionId
        }

    override suspend fun getSuccessfulCodesBefore(vehicleId: Long, timestampMillis: Long, sessionId: Long) =
        dao.codesBefore(vehicleId, timestampMillis, sessionId).toSet()

    override suspend fun getSuccessfulCodeOccurrenceCounts(vehicleId: Long) =
        dao.occurrenceCounts(vehicleId).associate { it.code to it.count }

    override fun getDtcGuidance(code: String): DtcGuidance = DtcCatalog.lookup(code)
}