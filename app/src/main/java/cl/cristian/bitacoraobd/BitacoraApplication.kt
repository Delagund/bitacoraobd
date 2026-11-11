package cl.cristian.bitacoraobd

import android.app.Application
import cl.cristian.bitacoraobd.ui.AppSession

open class BitacoraApplication : Application() {
    private val container by lazy { AppContainer(this) }
    open val appSession: AppSession by lazy {
        AppSession(
            vehicles = container.vehicles,
            preferences = container.preferences,
            gateway = container.gateway,
            scanDiagnostic = container.scanDiagnostic,
            saveDiagnostic = container.saveDiagnostic,
            deleteVehicleUseCase = container.deleteVehicle,
            scope = container.sessionScope
        )
    }

    internal val appContainer: AppContainer get() = container
}
