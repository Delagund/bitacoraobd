package cl.cristian.bitacoraobd.data.local.entity

import cl.cristian.bitacoraobd.data.local.dao.DiagnosticSessionWithCodes
import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.model.MaintenanceRecord
import cl.cristian.bitacoraobd.domain.model.Vehicle
import java.time.LocalDate

fun VehicleEntity.toDomain() = Vehicle(id, brand, model, year, fuelType, engineDescription, currentMileageKm)
fun Vehicle.toEntity() = VehicleEntity(id, brand, model, year, fuelType, engineDescription, currentMileageKm)
fun DiagnosticSessionWithCodes.toDomain() =
    DiagnosticSession(session.id, session.vehicleId, session.timestampMillis, session.mileageAtScanKm, codes.map { it.code })
fun DiagnosticSession.toEntity() = DiagnosticSessionEntity(id, vehicleId, timestampMillis, mileageAtScanKm)
fun MaintenanceRecordEntity.toDomain() =
    MaintenanceRecord(id, vehicleId, serviceType, LocalDate.ofEpochDay(performedDateEpochDay), mileageAtServiceKm, notes, nextDueDateEpochDay?.let(LocalDate::ofEpochDay), nextDueMileageKm)
fun MaintenanceRecord.toEntity() =
    MaintenanceRecordEntity(id, vehicleId, serviceType, performedDate.toEpochDay(), mileageAtServiceKm, notes, nextDueDate?.toEpochDay(), nextDueMileageKm)
