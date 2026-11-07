package cl.cristian.bitacoraobd.data.local.entity

import cl.cristian.bitacoraobd.data.local.dao.DiagnosticSessionWithCodes
import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.model.MaintenanceType
import cl.cristian.bitacoraobd.domain.model.Vehicle
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class EntityMappersTest {
    @Test
    fun vehicleMapsInBothDirections() {
        val vehicle = Vehicle(7L, "Toyota", "Corolla", 2022, "Gasoline", "1.8", 45_000)
        val entity = VehicleEntity(7L, "Toyota", "Corolla", 2022, "Gasoline", "1.8", 45_000)

        assertEquals(entity, vehicle.toEntity())
        assertEquals(vehicle, entity.toDomain())
    }

    @Test
    fun diagnosticSessionMapsInBothDirectionsAndKeepsCodeCollections() {
        val session = DiagnosticSession(3L, 7L, 1234L, 45_000, listOf("P0123", "P0456"))
        val entity = DiagnosticSessionEntity(3L, 7L, 1234L, 45_000)
        val withCodes = DiagnosticSessionWithCodes(
            entity,
            listOf(DiagnosticCodeEntity(3L, "P0123"), DiagnosticCodeEntity(3L, "P0456"))
        )

        assertEquals(entity, session.toEntity())
        assertEquals(session, withCodes.toDomain())
        assertEquals(
            DiagnosticSession(3L, 7L, 1234L, 45_000, emptyList()),
            withCodes.copy(codes = emptyList()).toDomain()
        )
    }

    @Test
    fun maintenanceMapsCalendarDatesAndNullableFieldsInBothDirections() {
        val performedDate = LocalDate.of(2024, 2, 29)
        val nextDueDate = LocalDate.of(2025, 2, 28)
        val scheduled = MaintenanceRecord(
            id = 9L, vehicleId = 7L, serviceType = MaintenanceType.OIL_AND_FILTER,
            performedDate = performedDate, mileageAtServiceKm = 45_000, notes = "full service",
            nextDueDate = nextDueDate, nextDueMileageKm = 55_000
        )
        val scheduledEntity = MaintenanceRecordEntity(
            id = 9L, vehicleId = 7L, serviceType = MaintenanceType.OIL_AND_FILTER,
            performedDateEpochDay = performedDate.toEpochDay(), mileageAtServiceKm = 45_000,
            notes = "full service", nextDueDateEpochDay = nextDueDate.toEpochDay(),
            nextDueMileageKm = 55_000
        )
        val unscheduled = scheduled.copy(nextDueDate = null, nextDueMileageKm = null)
        val unscheduledEntity = scheduledEntity.copy(nextDueDateEpochDay = null, nextDueMileageKm = null)

        assertEquals(scheduledEntity, scheduled.toEntity())
        assertEquals(scheduled, scheduledEntity.toDomain())
        assertEquals(unscheduledEntity, unscheduled.toEntity())
        assertEquals(unscheduled, unscheduledEntity.toDomain())
    }
}
