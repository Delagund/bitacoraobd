package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.model.DashboardState
import cl.cristian.bitacoraobd.domain.model.DashboardSummary
import cl.cristian.bitacoraobd.domain.model.MaintenanceStatus
import cl.cristian.bitacoraobd.domain.model.VehicleAlert
import cl.cristian.bitacoraobd.domain.obd.ObdGateway
import cl.cristian.bitacoraobd.domain.repository.AppPreferencesRepository
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository
import cl.cristian.bitacoraobd.domain.repository.MaintenanceRepository
import cl.cristian.bitacoraobd.domain.repository.VehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class GetDashboardDataUseCase(
    private val vehicles: VehicleRepository,
    private val preferences: AppPreferencesRepository,
    private val diagnostics: DiagnosticRepository,
    private val maintenance: MaintenanceRepository,
    private val gateway: ObdGateway,
    private val maintenanceStatus: GetMaintenanceStatusUseCase,
    private val analyzeHistory: AnalyzeDiagnosticHistoryUseCase
) {
    operator fun invoke(): Flow<DashboardState> =
        combine(vehicles.observeVehicles(), preferences.activeVehicleId) { all, activeId ->
            all to (all.firstOrNull { it.id == activeId } ?: all.firstOrNull())
        }.flatMapLatest { (all, vehicle) ->
            if (all.isEmpty() || vehicle == null) return@flatMapLatest flowOf(DashboardState.NoVehicles)
            combine(
                diagnostics.observeSessionsByVehicle(vehicle.id),
                maintenance.observeMaintenanceByVehicle(vehicle.id),
                gateway.connectionState
            ) { sessions, _, connection ->
                Triple(sessions, connection, vehicle)
            }.mapLatest { (sessions, connection, activeVehicle) ->
                val latest = sessions.firstOrNull()
                val analyzed = latest?.let { analyzeHistory(it) }.orEmpty()
                val recurrent = analyzed.filter { it.isRecurrent }
                val statuses = maintenanceStatus(activeVehicle.id)
                val counts = diagnostics.getSuccessfulCodeOccurrenceCounts(activeVehicle.id)
                val alerts = buildList {
                    recurrent.forEach { dtc ->
                        add(VehicleAlert.RecurrentDtcAlert(dtc.code, dtc.guidance.description, counts[dtc.code] ?: 0))
                    }
                    statuses.filter { it.status != MaintenanceStatus.UNSCHEDULED }.forEach {
                        add(VehicleAlert.MaintenanceAlert(it.record.serviceType, it.status, it.record.nextDueDate, it.record.nextDueMileageKm))
                    }
                }.distinctBy { it.conditionKey }
                DashboardState.Ready(
                    DashboardSummary(activeVehicle, connection, latest, analyzed, statuses, alerts)
                )
            }
        }
}
