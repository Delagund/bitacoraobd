package cl.cristian.bitacoraobd.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DomainModelsTest {
    @Test fun cleanScanIsValidAndKeepsCapture() {
        val result = DiagnosticScanResult.Success(1L, 123L, 500, emptyList())
        assertTrue(result.codes.isEmpty())
        assertEquals(1L, result.vehicleId)
        assertEquals(123L, result.timestampMillis)
        assertEquals(500, result.mileageAtScanKm)
    }

    @Test(expected = IllegalArgumentException::class)
    fun vehicleRejectsNegativeMileage() {
        Vehicle(brand = "A", model = "B", year = 2020, fuelType = "gas",
            engineDescription = "", currentMileageKm = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun sessionRejectsNegativeMileage() {
        DiagnosticSession(vehicleId = 1, timestampMillis = 0, mileageAtScanKm = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun scanRejectsNegativeMileage() {
        DiagnosticScanResult.Success(1, 0, -1, emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun maintenanceRejectsNegativeMileage() {
        MaintenanceRecord(vehicleId = 1, serviceType = MaintenanceType.OTHER,
            performedDate = LocalDate.of(2026, 1, 2), mileageAtServiceKm = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun maintenanceRejectsNegativeDueMileage() {
        MaintenanceRecord(vehicleId = 1, serviceType = MaintenanceType.OTHER,
            performedDate = LocalDate.of(2026, 1, 2), mileageAtServiceKm = 0,
            nextDueMileageKm = -1)
    }

    @Test fun calendarDateAndAlertPolicyArePreserved() {
        val date = LocalDate.of(2026, 11, 6)
        val record = MaintenanceRecord(vehicleId = 1, serviceType = MaintenanceType.OIL_ONLY,
            performedDate = date, mileageAtServiceKm = 0)
        assertEquals(date, record.performedDate)
        assertEquals(null, record.nextDueDate)
        assertTrue(MaintenanceType.entries.filter { it != MaintenanceType.OTHER }
            .all { it.supportsAutoAlerts })
        assertEquals(false, MaintenanceType.OTHER.supportsAutoAlerts)
    }

    @Test fun dashboardStateIsExhaustive() {
        assertEquals("empty", describe(DashboardState.NoVehicles))
    }

    private fun describe(state: DashboardState): String = when (state) {
        DashboardState.NoVehicles -> "empty"
        is DashboardState.Ready -> "ready"
    }

    @Test fun diagnosticResultIsExhaustive() {
        assertEquals("failure", describe(DiagnosticScanResult.Failure("error")))
    }

    private fun describe(result: DiagnosticScanResult): String = when (result) {
        is DiagnosticScanResult.Success -> "success"
        is DiagnosticScanResult.Failure -> "failure"
    }
}
