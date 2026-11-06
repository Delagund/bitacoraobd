package cl.cristian.bitacoraobd.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import cl.cristian.bitacoraobd.data.local.datastore.DataStoreAppPreferencesRepository
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class DataStoreAppPreferencesRepositoryTest {
    @Test
    fun preferencesPersistVehicleAndConnectionConfig() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.filesDir, "preferences-test.preferences_pb").apply { delete() }
        val store: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = { file }
        )
        val repository = DataStoreAppPreferencesRepository(store)
        val config = ObdConnectionConfig("10.0.0.7", 35000)

        assertEquals(null, repository.activeVehicleId.first())
        assertEquals(ObdConnectionConfig(), repository.obdConnectionConfig.first())

        repository.setActiveVehicleId(11L)
        repository.saveObdConnectionConfig(config)

        assertEquals(11L, repository.activeVehicleId.first())
        assertEquals(config, repository.obdConnectionConfig.first())
        file.delete()
        Unit
    }
}
