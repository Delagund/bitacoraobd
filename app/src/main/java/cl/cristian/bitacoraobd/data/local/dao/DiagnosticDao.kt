package cl.cristian.bitacoraobd.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import cl.cristian.bitacoraobd.data.local.entity.DiagnosticCodeEntity
import cl.cristian.bitacoraobd.data.local.entity.DiagnosticSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticDao {
    @Transaction
    @Query("SELECT * FROM diagnostic_sessions WHERE vehicleId = :vehicleId ORDER BY timestampMillis DESC, id DESC")
    fun observeSessions(vehicleId: Long): Flow<List<DiagnosticSessionWithCodes>>

    @Transaction
    @Query("SELECT * FROM diagnostic_sessions WHERE vehicleId = :vehicleId ORDER BY timestampMillis DESC, id DESC LIMIT 1")
    suspend fun latestSession(vehicleId: Long): DiagnosticSessionWithCodes?

    @Transaction
    @Query("SELECT * FROM diagnostic_sessions WHERE id = :sessionId")
    suspend fun sessionById(sessionId: Long): DiagnosticSessionWithCodes?

    @Query("SELECT code FROM diagnostic_codes WHERE sessionId IN (SELECT id FROM diagnostic_sessions WHERE vehicleId = :vehicleId AND (timestampMillis < :timestamp OR (timestampMillis = :timestamp AND id < :sessionId)))")
    suspend fun codesBefore(vehicleId: Long, timestamp: Long, sessionId: Long): List<String>

    @Query("SELECT code, COUNT(DISTINCT sessionId) AS count FROM diagnostic_codes WHERE sessionId IN (SELECT id FROM diagnostic_sessions WHERE vehicleId = :vehicleId) GROUP BY code")
    suspend fun occurrenceCounts(vehicleId: Long): List<CodeCount>

    @Insert
    suspend fun insertSession(session: DiagnosticSessionEntity): Long

    @Insert
    suspend fun insertCodes(codes: List<DiagnosticCodeEntity>)

    @Query("SELECT * FROM diagnostic_sessions WHERE vehicleId = :vehicleId ORDER BY timestampMillis DESC, id DESC")
    suspend fun sessions(vehicleId: Long): List<DiagnosticSessionEntity>
}

data class DiagnosticSessionWithCodes(
    @Embedded val session: DiagnosticSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val codes: List<DiagnosticCodeEntity>
)

data class CodeCount(val code: String, val count: Int)
