package cl.cristian.bitacoraobd.domain.model

data class DiagnosedDtc(
    val code: String,
    val guidance: DtcGuidance,
    val isRecurrent: Boolean
)
