package cl.cristian.bitacoraobd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import cl.cristian.bitacoraobd.domain.model.AppSettings
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.usecase.AnalyzeDiagnosticHistoryUseCase
import cl.cristian.bitacoraobd.domain.usecase.GetDashboardDataUseCase
import cl.cristian.bitacoraobd.domain.usecase.GetMaintenanceStatusUseCase
import cl.cristian.bitacoraobd.ui.HomeViewModel
import cl.cristian.bitacoraobd.ui.home.HomeScreen
import cl.cristian.bitacoraobd.ui.theme.BitacoraOBDTheme
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeMetricsVisibilityTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun hidesMetricsCardWhenEveryMetricIsDisabled() {
        val fixture = TestSessionFixture()
        val home = HomeViewModel(dashboardFor(fixture))
        val settings = AppSettings(showRpm = false, showCoolant = false, showSpeed = false)

        composeRule.setContent {
            BitacoraOBDTheme {
                HomeScreen(
                    home, fixture.session, settings,
                    onVehicles = {}, onMaintenance = {}, onDiagnostic = {},
                    onDetail = { _, _ -> }, onHistory = {}, onSettings = {}, onConnection = {}
                )
            }
        }
        composeRule.runOnIdle { home.setActive(true) }
        composeRule.waitUntil(5_000) {
            !home.state.value.loading && home.state.value.dashboard != null
        }

        assertTrue(composeRule.onAllNodesWithText("Métricas OBD").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun showsMetricsCardWhenAnyMetricIsEnabled() {
        val fixture = TestSessionFixture()
        val home = HomeViewModel(dashboardFor(fixture))
        val settings = AppSettings(showRpm = true, showCoolant = false, showSpeed = false)

        composeRule.setContent {
            BitacoraOBDTheme {
                HomeScreen(
                    home, fixture.session, settings,
                    onVehicles = {}, onMaintenance = {}, onDiagnostic = {},
                    onDetail = { _, _ -> }, onHistory = {}, onSettings = {}, onConnection = {}
                )
            }
        }
        composeRule.runOnIdle { home.setActive(true) }
        composeRule.waitUntil(5_000) {
            !home.state.value.loading && home.state.value.dashboard != null
        }

        composeRule.onNodeWithText("Métricas OBD").assertIsDisplayed()
    }
}

private fun dashboardFor(fixture: TestSessionFixture): GetDashboardDataUseCase {
    val maintenance = EmptyHomeMaintenance()
    val status = GetMaintenanceStatusUseCase(fixture.vehicles, maintenance, Clock.systemDefaultZone())
    return GetDashboardDataUseCase(
        fixture.vehicles, fixture.preferences, fixture.diagnostics, maintenance, fixture.gateway,
        status, AnalyzeDiagnosticHistoryUseCase(fixture.diagnostics)
    )
}

private class EmptyHomeMaintenance : MaintenanceRepository {
    override fun observeMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> =
        MutableStateFlow(emptyList())
    override suspend fun getMaintenanceById(recordId: Long): MaintenanceRecord? = null
    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord): Long = 0L
    override suspend fun getLatestRecordsPerAlertableType(vehicleId: Long): List<MaintenanceRecord> = emptyList()
}
