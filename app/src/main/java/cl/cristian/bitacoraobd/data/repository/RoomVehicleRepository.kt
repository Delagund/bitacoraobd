package cl.cristian.bitacoraobd.data.repository

import cl.cristian.bitacoraobd.data.local.dao.VehicleDao
import cl.cristian.bitacoraobd.data.local.entity.toDomain
import cl.cristian.bitacoraobd.data.local.entity.toEntity
import cl.cristian.bitacoraobd.domain.model.Vehicle
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomVehicleRepository(private val dao: VehicleDao) : VehicleRepository {
    override fun observeVehicles(): Flow<List<Vehicle>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }
    override fun observeVehicleById(vehicleId: Long): Flow<Vehicle?> = dao.observeById(vehicleId).map { it?.toDomain() }
    override suspend fun getVehicleById(vehicleId: Long): Vehicle? = dao.getById(vehicleId)?.toDomain()

    override suspend fun saveVehicle(vehicle: Vehicle): Long {
        if (vehicle.id == 0L) return dao.insert(vehicle.toEntity())
        check(dao.getById(vehicle.id) != null) { "Vehicle does not exist" }
        dao.update(vehicle.toEntity())
        return vehicle.id
    }

    override suspend fun deleteVehicle(vehicleId: Long) = dao.deleteById(vehicleId)
}