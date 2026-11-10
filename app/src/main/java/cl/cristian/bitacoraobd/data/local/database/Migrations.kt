package cl.cristian.bitacoraobd.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE diagnostic_sessions ADD COLUMN captureId TEXT NOT NULL DEFAULT 'legacy-migration-sentinel'"
        )
        val sessionIds = mutableListOf<Long>()
        db.query("SELECT id FROM diagnostic_sessions").use { cursor ->
            while (cursor.moveToNext()) sessionIds += cursor.getLong(0)
        }
        sessionIds.forEach { sessionId ->
            db.execSQL(
                "UPDATE diagnostic_sessions SET captureId = ? WHERE id = ?",
                arrayOf<Any>(UUID.randomUUID().toString(), sessionId)
            )
        }
        db.execSQL(
            "CREATE UNIQUE INDEX index_diagnostic_sessions_captureId ON diagnostic_sessions(captureId)"
        )
    }
}
