package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.obd.ObdConnectionState
import cl.cristian.bitacoraobd.domain.obd.ObdGateway
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellationException

sealed interface DeleteVehicleResult {
    data object Deleted : DeleteVehicleResult
    data object Connected : DeleteVehicleResult
    data object NotFound : DeleteVehicleResult
    data object Failed : DeleteVehicleResult
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
        return try {
            if (vehicles.getVehicleById(vehicleId) == null) return DeleteVehicleResult.NotFound
            val wasActive = preferences.activeVehicleId.first() == vehicleId
            if (wasActive) {
                val nextActiveId = vehicles.observeVehicles().first().firstOrNull { it.id != vehicleId }?.id
                preferences.setActiveVehicleId(nextActiveId)
            }
            vehicles.deleteVehicle(vehicleId)
            DeleteVehicleResult.Deleted
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            DeleteVehicleResult.Failed
        }
    }
}
