package cl.cristian.bitacoraobd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.model.MaintenanceType
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.usecase.AnalyzeDiagnosticHistoryUseCase
import cl.cristian.bitacoraobd.domain.usecase.GetDashboardDataUseCase
import cl.cristian.bitacoraobd.domain.usecase.GetMaintenanceStatusUseCase
import cl.cristian.bitacoraobd.domain.usecase.SaveMaintenanceUseCase
import cl.cristian.bitacoraobd.ui.HomeViewModel
import cl.cristian.bitacoraobd.ui.connection.ConnectionViewModel
import cl.cristian.bitacoraobd.ui.history.HistoryViewModel
import cl.cristian.bitacoraobd.ui.maintenance.MaintenanceViewModel
import cl.cristian.bitacoraobd.ui.navigation.AppNavigation
import cl.cristian.bitacoraobd.ui.theme.BitacoraOBDTheme
import cl.cristian.bitacoraobd.ui.vehicles.VehiclesViewModel
import cl.cristian.bitacoraobd.ui.settings.SettingsViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class HomeTabReactivationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun returningToHomeWhileResumedRecalculatesMaintenanceForCurrentDate() {
        val fixture = TestSessionFixture()
        val clock = MutableClock(Instant.parse("2026-11-10T12:00:00Z"))
        val maintenance = TestHomeMaintenance(
            MaintenanceRecord(
                id = 1L,
                vehicleId = 1L,
                serviceType = MaintenanceType.OIL_ONLY,
                performedDate = LocalDate.parse("2026-10-01"),
                mileageAtServiceKm = 100,
                nextDueDate = LocalDate.parse("2026-11-11")
            )
        )
        val status = GetMaintenanceStatusUseCase(fixture.vehicles, maintenance, clock)
        val dashboard = GetDashboardDataUseCase(
            fixture.vehicles, fixture.preferences, fixture.diagnostics, maintenance, fixture.gateway,
            status, AnalyzeDiagnosticHistoryUseCase(fixture.diagnostics)
        )
        val home = HomeViewModel(dashboard)
        val vehicles = VehiclesViewModel(fixture.session, fixture.preferences)
        val connection = ConnectionViewModel(fixture.session, fixture.preferences)
        val history = HistoryViewModel(fixture.diagnostics, AnalyzeDiagnosticHistoryUseCase(fixture.diagnostics))
        val maintenanceViewModel = MaintenanceViewModel(
            maintenance, status, SaveMaintenanceUseCase(fixture.vehicles, maintenance), fixture.preferences
        )
        val settings = SettingsViewModel(fixture.preferences)

        composeRule.setContent {
            BitacoraOBDTheme {
                AppNavigation(
                    rememberNavController(), fixture.session, vehicles, connection, history,
                    maintenanceViewModel, home, settings, fixture.diagnostics, fixture.preferences
                ) {}
            }
        }

        composeRule.onNodeWithText("OIL_ONLY").assertIsDisplayed()
        composeRule.onNodeWithText("Próximo").assertIsDisplayed()
        clock.current = Instant.parse("2026-11-11T12:00:00Z")
        composeRule.onNodeWithText("OIL_ONLY").assertIsDisplayed()
        composeRule.onNodeWithText("Próximo").assertIsDisplayed()

        composeRule.onNodeWithText("Vehículos").performClick()
        composeRule.onNodeWithText("Inicio").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Vencido").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("OIL_ONLY").assertIsDisplayed()
        composeRule.onNodeWithText("Vencido").assertIsDisplayed()
    }
}

private class TestHomeMaintenance(private val record: MaintenanceRecord) : MaintenanceRepository {
    override fun observeMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> =
        MutableStateFlow(listOf(record).filter { it.vehicleId == vehicleId })
    override suspend fun getMaintenanceById(recordId: Long): MaintenanceRecord? = record.takeIf { it.id == recordId }
    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord): Long = record.id
    override suspend fun getLatestRecordsPerAlertableType(vehicleId: Long): List<MaintenanceRecord> =
        listOf(record).filter { it.vehicleId == vehicleId }
}

private class MutableClock(var current: Instant) : Clock() {
    override fun getZone(): ZoneId = ZoneId.of("UTC")
    override fun withZone(zone: ZoneId): Clock = MutableClock(current)
    override fun instant(): Instant = current
}
