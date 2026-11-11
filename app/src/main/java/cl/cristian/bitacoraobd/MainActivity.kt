package cl.cristian.bitacoraobd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import cl.cristian.bitacoraobd.ui.AppViewModel
import cl.cristian.bitacoraobd.ui.HomeViewModel
import cl.cristian.bitacoraobd.ui.navigation.AppNavigation
import cl.cristian.bitacoraobd.ui.theme.BitacoraOBDTheme

class MainActivity : ComponentActivity() {
    private lateinit var appViewModel: AppViewModel
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as BitacoraApplication
        appViewModel = ViewModelProvider(this, AppViewModel.Factory(application.appSession))[AppViewModel::class.java]
        homeViewModel = ViewModelProvider(
            this, HomeViewModel.Factory(application.appContainer.getDashboard)
        )[HomeViewModel::class.java]
        setContent {
            BitacoraOBDTheme {
                val navController = rememberNavController()
                AppNavigation(navController, appViewModel.session, onBackAtRoot = ::finish)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.setActive(true)
    }

    override fun onPause() {
        homeViewModel.setActive(false)
        super.onPause()
    }
}
