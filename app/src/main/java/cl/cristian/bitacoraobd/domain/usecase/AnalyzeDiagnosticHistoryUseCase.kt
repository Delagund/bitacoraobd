package cl.cristian.bitacoraobd.domain.usecase

import cl.cristian.bitacoraobd.domain.model.DiagnosedDtc
import cl.cristian.bitacoraobd.domain.model.DiagnosticSession
import cl.cristian.bitacoraobd.domain.repository.DiagnosticRepository

class AnalyzeDiagnosticHistoryUseCase(private val diagnostics: DiagnosticRepository) {
    suspend operator fun invoke(session: DiagnosticSession): List<DiagnosedDtc> {
        val previous = diagnostics.getSuccessfulCodesBefore(
            session.vehicleId, session.timestampMillis, session.id
        )
        return session.codes.distinct().map { code ->
            DiagnosedDtc(code, diagnostics.getDtcGuidance(code), code in previous)
        }
    }
}
