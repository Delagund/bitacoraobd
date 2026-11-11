package cl.cristian.bitacoraobd.data.local.database

import android.content.Context
import androidx.room.Room

internal fun buildAppDatabase(context: Context, name: String): AppDatabase =
    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
        .addMigrations(MIGRATION_1_2)
        .build()
