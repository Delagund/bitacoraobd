package cl.cristian.bitacoraobd.ui

import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import java.math.BigDecimal
import java.math.RoundingMode

fun formatDistance(kilometers: Int, unit: DistanceUnit): String =
    if (unit == DistanceUnit.KILOMETERS) "$kilometers km"
    else "${BigDecimal(kilometers).divide(BigDecimal("1.609344"), 1, RoundingMode.HALF_UP).toPlainString()} mi"
