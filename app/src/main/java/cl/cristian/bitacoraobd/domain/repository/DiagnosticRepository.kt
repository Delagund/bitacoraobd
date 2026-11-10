package cl.cristian.bitacoraobd.domain.repository

import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.model.DtcGuidance
import kotlinx.coroutines.flow.Flow

interface DiagnosticRepository {
    fun observeSessionsByVehicle(vehicleId: Long): Flow<List<DiagnosticSession>>
    suspend fun getLatestSession(vehicleId: Long): DiagnosticSession?
    suspend fun getSessionById(sessionId: Long): DiagnosticSession?

    /** Idempotently persists a capture and advances the odometer in the same database transaction. */
    suspend fun saveDiagnosticSessionAndAdvanceMileage(session: DiagnosticSession): Long

    suspend fun getSuccessfulCodesBefore(
        vehicleId: Long,
        timestampMillis: Long,
        sessionId: Long
    ): Set<String>
    suspend fun getSuccessfulCodeOccurrenceCounts(vehicleId: Long): Map<String, Int>
    fun getDtcGuidance(code: String): DtcGuidance
}
