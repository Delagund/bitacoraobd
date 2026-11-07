package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.model.DiagnosticScanResult
import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdGateway
import cl.cristian.bitacoraobd.domain.obd.ObdResult
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import java.time.Clock

class ScanDiagnosticCodesUseCase(
    private val gateway: ObdGateway,
    private val vehicles: VehicleRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    suspend operator fun invoke(vehicleId: Long): DiagnosticScanResult {
        val vehicle = vehicles.getVehicleById(vehicleId)
            ?: return DiagnosticScanResult.Failure("Vehicle does not exist")
        if (gateway.connectionState.value !is ObdConnectionState.Connected) {
            return DiagnosticScanResult.Failure("OBD adapter is not connected")
        }
        return when (val result = gateway.readDiagnosticCodes()) {
            is ObdResult.Success -> DiagnosticScanResult.Success(
                vehicleId, clock.millis(), vehicle.currentMileageKm, result.value.distinct()
            )
            is ObdResult.Failure -> DiagnosticScanResult.Failure(result.message)
        }
    }
}
