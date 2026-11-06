package cl.cristian.bitacoraobd.domain.model

import java.time.LocalDate

sealed interface VehicleAlert {
    val conditionKey: String

    data class RecurrentDtcAlert(
        val code: String,
        val description: String,
        val occurrencesCount: Int
    ) : VehicleAlert {
        override val conditionKey = "DTC_RECURRENT_$code"
    }

    data class MaintenanceAlert(
        val serviceType: MaintenanceType,
        val status: MaintenanceStatus,
        val dueDate: LocalDate?,
        val dueMileageKm: Int?
    ) : VehicleAlert {
        override val conditionKey = "MAINT_${serviceType.name}"
    }
}
