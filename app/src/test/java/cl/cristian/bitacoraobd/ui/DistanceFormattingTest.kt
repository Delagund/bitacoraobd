package cl.cristian.bitacoraobd.ui

import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceFormattingTest {
    @Test fun formatsStoredKilometersInSelectedUnitWithoutChangingSourceValue() {
        assertEquals("17 km", formatDistance(17, DistanceUnit.KILOMETERS))
        assertEquals("10.6 mi", formatDistance(17, DistanceUnit.MILES))
    }
}
