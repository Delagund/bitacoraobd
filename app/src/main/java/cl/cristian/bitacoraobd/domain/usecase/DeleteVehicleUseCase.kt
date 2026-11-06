package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdGateway
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.first

sealed interface DeleteVehicleResult {
    data object Deleted : DeleteVehicleResult
    data object Connected : DeleteVehicleResult
    data object NotFound : DeleteVehicleResult
}

class DeleteVehicleUseCase(
    private val vehicles: VehicleRepository,
    private val preferences: AppPreferencesRepository,
    private val gateway: ObdGateway
) {
    suspend operator fun invoke(vehicleId: Long): DeleteVehicleResult {
        if (gateway.connectionState.value is ObdConnectionState.Connected) {
            return DeleteVehicleResult.Connected
        }
        if (vehicles.getVehicleById(vehicleId) == null) return DeleteVehicleResult.NotFound
        val wasActive = preferences.activeVehicleId.first() == vehicleId
        vehicles.deleteVehicle(vehicleId)
        if (wasActive) {
            preferences.setActiveVehicleId(vehicles.observeVehicles().first().firstOrNull()?.id)
        }
        return DeleteVehicleResult.Deleted
    }
}
