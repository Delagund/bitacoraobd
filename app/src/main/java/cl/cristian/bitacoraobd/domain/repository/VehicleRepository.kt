package cl.cristian.bitacoraobd.domain.repository

import cl.cristian.bitacoraobd.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    fun observeVehicleById(vehicleId: Long): Flow<Vehicle?>
    suspend fun getVehicleById(vehicleId: Long): Vehicle?
    suspend fun saveVehicle(vehicle: Vehicle): Long
    suspend fun deleteVehicle(vehicleId: Long)
}
