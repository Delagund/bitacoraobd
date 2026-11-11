package cl.cristian.bitacoraobd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import cl.cristian.bitacoraobd.ui.AppViewModel
import cl.cristian.bitacoraobd.ui.HomeViewModel
import cl.cristian.bitacoraobd.ui.vehicles.VehiclesViewModel
import cl.cristian.bitacoraobd.ui.connection.ConnectionViewModel
import cl.cristian.bitacoraobd.ui.history.HistoryViewModel
import cl.cristian.bitacoraobd.ui.maintenance.MaintenanceViewModel
import cl.cristian.bitacoraobd.ui.settings.SettingsViewModel
import cl.cristian.bitacoraobd.ui.navigation.AppNavigation
import cl.cristian.bitacoraobd.ui.theme.BitacoraOBDTheme

class MainActivity : ComponentActivity() {
    private lateinit var appViewModel: AppViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var vehiclesViewModel: VehiclesViewModel
    private lateinit var connectionViewModel: ConnectionViewModel
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var maintenanceViewModel: MaintenanceViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as BitacoraApplication
        appViewModel = ViewModelProvider(this, AppViewModel.Factory(application.appSession))[AppViewModel::class.java]
        homeViewModel = ViewModelProvider(
            this, HomeViewModel.Factory(application.appContainer.getDashboard)
        )[HomeViewModel::class.java]
        vehiclesViewModel = ViewModelProvider(
            this, VehiclesViewModel.Factory(application.appSession, application.appContainer.preferences)
        )[VehiclesViewModel::class.java]
        connectionViewModel = ViewModelProvider(
            this, ConnectionViewModel.Factory(application.appSession, application.appContainer.preferences)
        )[ConnectionViewModel::class.java]
        historyViewModel = ViewModelProvider(
            this, HistoryViewModel.Factory(application.appContainer.diagnostics, application.appContainer.analyzeHistory)
        )[HistoryViewModel::class.java]
        maintenanceViewModel = ViewModelProvider(
            this, MaintenanceViewModel.Factory(application.appContainer.maintenance,
                application.appContainer.maintenanceStatus, application.appContainer.saveMaintenance,
                application.appContainer.preferences)
        )[MaintenanceViewModel::class.java]
        settingsViewModel = ViewModelProvider(
            this, SettingsViewModel.Factory(application.appContainer.preferences)
        )[SettingsViewModel::class.java]
        setContent {
            BitacoraOBDTheme {
                val navController = rememberNavController()
                AppNavigation(navController, appViewModel.session, vehiclesViewModel,
                    connectionViewModel, historyViewModel, maintenanceViewModel, homeViewModel, settingsViewModel,
                    application.appContainer.diagnostics, application.appContainer.preferences,
                    onBackAtRoot = ::finish)
            }
        }
    }

}
