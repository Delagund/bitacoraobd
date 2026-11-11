package cl.cristian.bitacoraobd.ui.settings

import cl.cristian.bitacoraobd.domain.model.AppSettings
import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import cl.cristian.bitacoraobd.domain.model.TemperatureUnit
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionConfig
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.CompletableDeferred
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @Test fun savesOneSharedDraftAndUpdatesPersistedSettings() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val preferences = SettingsPreferences()
            val viewModel = SettingsViewModel(preferences)
            advanceUntilIdle()

            assertEquals(AppSettings(), viewModel.draft)
            viewModel.update {
                it.copy(displayName = "Mi auto", distanceUnit = DistanceUnit.MILES,
                    temperatureUnit = TemperatureUnit.FAHRENHEIT, showRpm = false)
            }
            assertTrue(viewModel.dirty)
            assertEquals(0, preferences.saveCalls)

            viewModel.save()
            advanceUntilIdle()

            assertEquals(1, preferences.saveCalls)
            assertEquals(viewModel.draft, preferences.persisted.value)
            assertFalse(viewModel.dirty)
            assertFalse(viewModel.saving)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun failedSaveKeepsDraftAndCanBeRetried() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val preferences = SettingsPreferences().apply { failSave = true }
            val viewModel = SettingsViewModel(preferences)
            advanceUntilIdle()
            viewModel.update { it.copy(displayName = "Nuevo") }

            viewModel.save()
            advanceUntilIdle()
            assertEquals("Nuevo", viewModel.draft?.displayName)
            assertEquals("disk full", viewModel.error)
            assertTrue(viewModel.dirty)

            preferences.failSave = false
            viewModel.save()
            advanceUntilIdle()
            assertNull(viewModel.error)
            assertFalse(viewModel.dirty)
            assertEquals("Nuevo", preferences.persisted.value.displayName)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun discardRestoresSavedDraftAndReturnsRequestedDestination() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val viewModel = SettingsViewModel(SettingsPreferences())
            advanceUntilIdle()
            viewModel.update { it.copy(displayName = "Draft") }
            viewModel.requestLeave("vehicles")

            assertTrue(viewModel.pendingLeave)
            assertEquals("vehicles", viewModel.discard())
            assertNull(viewModel.leaveTarget)
            assertEquals(AppSettings(), viewModel.draft)
            assertFalse(viewModel.dirty)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun discardingWhileSaveIsInFlightDoesNotNavigateBeforeTheSaveSettles() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val preferences = SettingsPreferences().apply {
                saveGate = CompletableDeferred()
                saveStarted = CompletableDeferred()
            }
            val viewModel = SettingsViewModel(preferences)
            advanceUntilIdle()
            viewModel.update { it.copy(displayName = "Saving") }
            viewModel.save()
            runCurrent()
            assertTrue(preferences.saveStarted!!.isCompleted)

            viewModel.requestLeave("home")
            val leaveTarget = viewModel.discard()

            assertNull("A route must not be released while its write is unresolved", leaveTarget)
            preferences.saveGate!!.complete(Unit)
            advanceUntilIdle()
            assertEquals("home", viewModel.takeSuccessfulLeaveTarget())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun failedSaveKeepsRequestedRouteUntilRetrySucceeds() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val preferences = SettingsPreferences().apply { failSave = true }
            val viewModel = SettingsViewModel(preferences)
            advanceUntilIdle()
            viewModel.update { it.copy(displayName = "Retry") }
            viewModel.save()
            viewModel.requestLeave("vehicles")
            advanceUntilIdle()

            assertEquals("disk full", viewModel.error)
            assertNull(viewModel.takeSuccessfulLeaveTarget())

            preferences.failSave = false
            viewModel.save()
            advanceUntilIdle()

            assertEquals("vehicles", viewModel.takeSuccessfulLeaveTarget())
            assertNull(viewModel.leaveTarget)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun initialReadFailureDoesNotPretendDefaultsWereLoaded() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val viewModel = SettingsViewModel(SettingsPreferences().apply { failRead = true })
            advanceUntilIdle()

            assertNull(viewModel.draft)
            assertEquals("read failed", viewModel.error)
            assertFalse(viewModel.loading)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun keepEditingCancelsPendingLeaveWithoutLosingDraft() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val viewModel = SettingsViewModel(SettingsPreferences())
            advanceUntilIdle()
            viewModel.update { it.copy(showSpeed = false) }
            viewModel.requestLeave("home")
            viewModel.keepEditing()

            assertFalse(viewModel.pendingLeave)
            assertNull(viewModel.leaveTarget)
            assertFalse(viewModel.draft!!.showSpeed)
            assertTrue(viewModel.dirty)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class SettingsPreferences : AppPreferencesRepository {
    val persisted = MutableStateFlow(AppSettings())
    var saveCalls = 0
    var failSave = false
    var failRead = false
    var saveStarted: CompletableDeferred<Unit>? = null
    var saveGate: CompletableDeferred<Unit>? = null
    override val activeVehicleId: Flow<Long?> = MutableStateFlow(1L)
    override val obdConnectionConfig: Flow<ObdConnectionConfig> = MutableStateFlow(ObdConnectionConfig())
    override val settings: Flow<AppSettings> = kotlinx.coroutines.flow.flow {
        if (failRead) throw IOException("read failed")
        emitAll(persisted)
    }
    override suspend fun setActiveVehicleId(vehicleId: Long?) = Unit
    override suspend fun saveObdConnectionConfig(config: ObdConnectionConfig) = Unit
    override suspend fun saveSettings(settings: AppSettings) {
        saveCalls++
        check(!failSave) { "disk full" }
        saveStarted?.complete(Unit)
        saveGate?.await()
        persisted.value = settings
    }
}
