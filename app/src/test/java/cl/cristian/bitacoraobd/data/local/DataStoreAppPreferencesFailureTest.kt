package cl.cristian.bitacoraobd.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import cl.cristian.bitacoraobd.data.local.datastore.DataStoreAppPreferencesRepository
import cl.cristian.bitacoraobd.domain.model.AppSettings
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertSame
import org.junit.Test

class DataStoreAppPreferencesFailureTest {
    @Test
    fun settingsReadPropagatesDataStoreFailure() = runBlocking {
        val failure = IOException("read failed")
        val repository = DataStoreAppPreferencesRepository(FailingDataStore(readFailure = failure))

        val actual = runCatching { repository.settings.first() }.exceptionOrNull()

        assertSame(failure, actual)
    }

    @Test
    fun settingsWritePropagatesDataStoreFailureWithoutSuccess() = runBlocking {
        val failure = IOException("write failed")
        val repository = DataStoreAppPreferencesRepository(FailingDataStore(writeFailure = failure))

        val actual = runCatching { repository.saveSettings(AppSettings(displayName = "Nuevo")) }.exceptionOrNull()

        assertSame(failure, actual)
    }
}

private class FailingDataStore(
    private val readFailure: IOException? = null,
    private val writeFailure: IOException? = null
) : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow {
        readFailure?.let { throw it }
        emit(emptyPreferences())
    }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        writeFailure?.let { throw it }
        return transform(emptyPreferences())
    }
}
