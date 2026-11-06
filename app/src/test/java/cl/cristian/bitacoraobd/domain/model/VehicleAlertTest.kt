package cl.cristian.bitacoraobd.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleAlertTest {
    @Test fun keysUseCodeAndMaintenanceType() {
        assertEquals("DTC_RECURRENT_P0300",
            VehicleAlert.RecurrentDtcAlert("P0300", "", 2).conditionKey)
        assertEquals("MAINT_OIL_ONLY",
            VehicleAlert.MaintenanceAlert(MaintenanceType.OIL_ONLY,
                MaintenanceStatus.UPCOMING, null, null).conditionKey)
        assertEquals("MAINT_OIL_ONLY",
            VehicleAlert.MaintenanceAlert(MaintenanceType.OIL_ONLY,
                MaintenanceStatus.OVERDUE, null, null).conditionKey)
        assertEquals("MAINT_FILTER_ONLY",
            VehicleAlert.MaintenanceAlert(MaintenanceType.FILTER_ONLY,
                MaintenanceStatus.OVERDUE, null, null).conditionKey)
    }
}
