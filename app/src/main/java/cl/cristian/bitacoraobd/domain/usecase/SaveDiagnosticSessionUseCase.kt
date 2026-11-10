package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.model.DiagnosticScanResult
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository

class SaveDiagnosticSessionUseCase(private val diagnostics: DiagnosticRepository) {
    suspend operator fun invoke(result: DiagnosticScanResult): Long? = when (result) {
        is DiagnosticScanResult.Failure -> null
        is DiagnosticScanResult.Success -> diagnostics.saveDiagnosticSessionAndAdvanceMileage(
            cl.cristian.bitacoraobd.domain.model.DiagnosticSession(
                vehicleId = result.vehicleId,
                timestampMillis = result.timestampMillis,
                mileageAtScanKm = result.mileageAtScanKm,
                codes = result.codes,
                captureId = result.captureId
            )
        )
    }
}
