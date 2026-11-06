package cl.cristian.bitacoraobd.data.repository

import cl.cristian.bitacoraobd.domain.model.DtcGuidance

object DtcCatalog {
    private val entries = mapOf(
        "P0300" to DtcGuidance("P0300", "Se detectaron fallos de combustión aleatorios o en varios cilindros.", "Motor", listOf("Encendido", "Combustible", "Admisión"), listOf("Solicita una revisión si el código persiste."), true),
        "P0420" to DtcGuidance("P0420", "La eficiencia del catalizador está bajo el umbral esperado.", "Emisiones", listOf("Escape", "Sensores de oxígeno", "Catalizador"), listOf("Solicita una revisión antes de reemplazar componentes."), true)
    )

    fun lookup(code: String): DtcGuidance = entries[code] ?: DtcGuidance(
        code, "No hay información local para este código.", "Sistema no identificado",
        emptyList(), listOf("Consulta a un técnico calificado."), false
    )
}