package cl.cristian.bitacoraobd.domain.model

data class DtcGuidance(
    val code: String,
    val description: String,
    val relatedSystem: String,
    val possibleCauses: List<String>,
    val orientativeActions: List<String>,
    val isKnownInCatalog: Boolean
)
