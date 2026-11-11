package cl.cristian.bitacoraobd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cl.cristian.bitacoraobd.ui.ExitAction
import cl.cristian.bitacoraobd.ui.PendingChoice
import cl.cristian.bitacoraobd.ui.navigation.AppRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class MainActivityRecreationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun vehiclesTabShowsActiveVehicleAndActions() {
        composeRule.onNodeWithText("Vehículos").performClick()
        composeRule.onNodeWithText("Test Vehicle").assertIsDisplayed()
        composeRule.onNodeWithText("Activo").assertIsDisplayed()
        composeRule.onNodeWithText("Editar").assertIsDisplayed()
        composeRule.onNodeWithText("Eliminar").assertIsDisplayed()
    }

    @Test
    fun queuedTabExitWaitsForSaveAndKeepsDraftOnFailure() {
        val fixture = (composeRule.activity.application as TestBitacoraApplication).fixture
        composeRule.onNodeWithText("Vehículos").performClick()
        composeRule.onNodeWithText("Agregar vehículo").performClick()

        fixture.vehicles.saveGate = kotlinx.coroutines.CompletableDeferred()
        fixture.vehicles.failSave = true
        composeRule.onNodeWithText("Guardar").performClick()
        composeRule.waitUntil(5_000) { fixture.vehicles.saveStarted.isCompleted }
        composeRule.onNodeWithText("Mantenimiento").performClick()
        assertNull(fixture.session.state.value.authorizedExit)
        composeRule.onNodeWithText("Agregar vehículo").assertIsDisplayed()

        fixture.vehicles.saveGate?.complete(Unit)
        composeRule.waitUntil(5_000) {
            fixture.session.state.value.vehicleActionResult?.error != null &&
                !fixture.session.state.value.sensitiveOperationRunning
        }
        composeRule.onNodeWithText("Se perderán los cambios del vehículo.").assertIsDisplayed()
        composeRule.onNodeWithText("Seguir editando").performClick()
        composeRule.onNodeWithText("Agregar vehículo").assertIsDisplayed()

        fixture.vehicles.saveStarted = kotlinx.coroutines.CompletableDeferred()
        fixture.vehicles.saveGate = kotlinx.coroutines.CompletableDeferred()
        fixture.vehicles.failSave = false
        composeRule.onNodeWithText("Guardar").performClick()
        composeRule.waitUntil(5_000) { fixture.vehicles.saveStarted.isCompleted }
        composeRule.onNodeWithText("Mantenimiento").performClick()
        fixture.vehicles.saveGate?.complete(Unit)
        composeRule.onNodeWithText("Maintenance").assertIsDisplayed()
    }

    @Test
    fun rootBackFinishesAndAllowsTheNextExitRequest() {
        val session = (composeRule.activity.application as BitacoraApplication).appSession
        assertNull(session.state.value.pendingDiagnostic)
        assertNull(session.state.value.pendingDecision)
        assertNull(session.state.value.authorizedExit)
        composeRule.runOnUiThread { session.requestExit(ExitAction.Back) }
        composeRule.waitUntil(5_000) {
            composeRule.activity.isFinishing || session.state.value.authorizedExit != null
        }
        assertTrue(
            "root Back did not finish: state=${session.state.value}; connection=${session.connectionState.value}; route=Home",
            composeRule.activity.isFinishing
        )
        composeRule.waitUntil(5_000) { session.state.value.authorizedExit == null }
        assertTrue(composeRule.activity.isFinishing)
        assertNull(session.state.value.authorizedExit)
        composeRule.waitForIdle()
    }

    @Test
    fun pendingCaptureAndSaveDecisionSurviveActivityRecreation() {
        val app = composeRule.activity.application as TestBitacoraApplication
        val fixture = app.fixture
        val session = fixture.session

        composeRule.runOnUiThread { session.scanDiagnostic() }
        composeRule.waitUntil(5_000) { session.state.value.pendingDiagnostic != null }
        val capture = session.state.value.pendingDiagnostic
        session.requestExit(ExitAction.ToRoute(AppRoute.history(1L)))
        composeRule.waitUntil(5_000) { session.state.value.pendingDecision != null }

        composeRule.activityRule.scenario.recreate()
        assertEquals(capture, session.state.value.pendingDiagnostic)
        assertTrue(session.state.value.pendingDecision != null)
        session.resolvePending(PendingChoice.Stay)
        composeRule.waitUntil(5_000) { session.state.value.pendingDecision == null }
        composeRule.waitForIdle()
        assertEquals(capture, session.state.value.pendingDiagnostic)
        assertNull(session.state.value.authorizedExit)

        session.requestExit(ExitAction.ToRoute(AppRoute.history(1L)))
        composeRule.waitUntil(5_000) { session.state.value.pendingDecision != null }
        session.resolvePending(PendingChoice.Save)
        composeRule.waitUntil(5_000) { fixture.diagnostics.saveStarted.isCompleted }
        assertEquals(1, fixture.diagnostics.saveCalls)
        assertEquals(capture, session.state.value.pendingDiagnostic)
        assertNull(session.state.value.authorizedExit)

        composeRule.activityRule.scenario.recreate()
        assertEquals(capture, session.state.value.pendingDiagnostic)
        fixture.diagnostics.allowSave.complete(Unit)
        composeRule.onNodeWithText("History").assertIsDisplayed()
        assertNull(session.state.value.pendingDiagnostic)
        assertNull(session.state.value.authorizedExit)

        composeRule.runOnUiThread { session.scanDiagnostic() }
        composeRule.waitUntil(5_000) { session.state.value.pendingDiagnostic != null }
        val failedCapture = session.state.value.pendingDiagnostic
        fixture.diagnostics.failSave = true
        session.requestExit(ExitAction.ToRoute(AppRoute.SETTINGS))
        composeRule.waitUntil(5_000) { session.state.value.pendingDecision != null }
        session.resolvePending(PendingChoice.Save)
        composeRule.waitUntil(5_000) {
            session.state.value.error != null && session.state.value.pendingDecision == null
        }
        assertEquals(failedCapture, session.state.value.pendingDiagnostic)
        assertNull(session.state.value.authorizedExit)
        composeRule.onNodeWithText("History").assertIsDisplayed()
    }

    @Test
    fun invalidRouteShowsErrorAndAValidRouteCanFollow() {
        val session = (composeRule.activity.application as TestBitacoraApplication).appSession

        composeRule.runOnUiThread {
            session.requestExit(ExitAction.ToRoute("history/0"))
        }
        composeRule.waitUntil(5_000) {
            session.state.value.error == "Destino de navegación no válido"
        }
        assertNull(session.state.value.authorizedExit)

        composeRule.runOnUiThread {
            session.requestExit(ExitAction.ToRoute(AppRoute.SETTINGS))
        }
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        assertNull(session.state.value.authorizedExit)
        assertNull(session.state.value.error)
    }

    @Test
    fun authorizedExitIsConsumedOnceAndConnectionFlowReturnsToDiagnosticAfterRecreation() {
        val session = (composeRule.activity.application as BitacoraApplication).appSession

        composeRule.runOnUiThread {
            session.requestExit(ExitAction.ToRoute(AppRoute.CONNECTION))
        }
        composeRule.onNodeWithText("Connection").assertIsDisplayed()
        composeRule.runOnUiThread {
            session.requestExit(ExitAction.ToRoute(AppRoute.HOME))
        }
        composeRule.onNodeWithText("Diagnostic").assertIsDisplayed()
        assertNull(session.state.value.authorizedExit)

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("Diagnostic").assertIsDisplayed()
        assertSame(session, (composeRule.activity.application as BitacoraApplication).appSession)
        assertNull(session.state.value.authorizedExit)
    }
}
