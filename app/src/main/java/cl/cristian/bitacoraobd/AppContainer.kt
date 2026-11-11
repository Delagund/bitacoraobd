package cl.cristian.bitacoraobd

import android.content.Context
import cl.cristian.bitacoraobd.data.local.database.AppDatabase
import cl.cristian.bitacoraobd.data.local.database.buildAppDatabase
import cl.cristian.bitacoraobd.data.local.datastore.DataStoreAppPreferencesRepository
import cl.cristian.bitacoraobd.data.local.datastore.appPreferencesDataStore
import cl.cristian.bitacoraobd.data.obd.Elm327ObdGateway
import cl.cristian.bitacoraobd.data.repository.RoomDiagnosticRepository
import cl.cristian.bitacoraobd.data.repository.RoomMaintenanceRepository
import cl.cristian.bitacoraobd.data.repository.RoomVehicleRepository
import cl.cristian.bitacoraobd.domain.usecase.AnalyzeDiagnosticHistoryUseCase
import cl.cristian.bitacoraobd.domain.usecase.DeleteVehicleUseCase
import cl.cristian.bitacoraobd.domain.usecase.GetDashboardDataUseCase
import cl.cristian.bitacoraobd.domain.usecase.GetMaintenanceStatusUseCase
import cl.cristian.bitacoraobd.domain.usecase.SaveDiagnosticSessionUseCase
import cl.cristian.bitacoraobd.domain.usecase.ScanDiagnosticCodesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: AppDatabase by lazy { buildAppDatabase(appContext, DATABASE_NAME) }
    val preferences by lazy { DataStoreAppPreferencesRepository(appContext.appPreferencesDataStore) }
    val vehicles by lazy { RoomVehicleRepository(database.vehicleDao()) }
    val diagnostics by lazy { RoomDiagnosticRepository(database) }
    val maintenance by lazy { RoomMaintenanceRepository(database) }
    val gateway by lazy { Elm327ObdGateway() }

    val analyzeHistory by lazy { AnalyzeDiagnosticHistoryUseCase(diagnostics) }
    val maintenanceStatus by lazy { GetMaintenanceStatusUseCase(vehicles, maintenance) }
    val saveMaintenance by lazy { cl.cristian.bitacoraobd.domain.usecase.SaveMaintenanceUseCase(vehicles, maintenance) }
    val scanDiagnostic by lazy { ScanDiagnosticCodesUseCase(gateway, vehicles) }
    val saveDiagnostic by lazy { SaveDiagnosticSessionUseCase(diagnostics) }
    val deleteVehicle by lazy { DeleteVehicleUseCase(vehicles, preferences, gateway) }
    val getDashboard by lazy {
        GetDashboardDataUseCase(
            vehicles, preferences, diagnostics, maintenance, gateway, maintenanceStatus, analyzeHistory
        )
    }

    internal val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    companion object {
        const val DATABASE_NAME = "bitacora_obd.db"
    }
}
