package cl.cristian.bitacoraobd.ui.vehicles

import cl.cristian.bitacoraobd.domain.model.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleMileageTest {
    @Test fun milesConvertToNearestKilometer() {
        assertEquals(16, mileageToKilometers("10,0", DistanceUnit.MILES))
    }

    @Test fun kilometersMustBeWholeNumbers() {
        assertNull(mileageToKilometers("10.5", DistanceUnit.KILOMETERS))
    }

    @Test fun keepsStoredKilometersWhenDisplayValueWasNotEdited() {
        assertEquals(17, mileageForSave("10.6", DistanceUnit.MILES, 17, edited = false))
    }

    @Test fun milesAllowOneLocalDecimalSeparator() {
        assertEquals(16, mileageToKilometers("10.0", DistanceUnit.MILES))
        assertNull(mileageToKilometers("10.00", DistanceUnit.MILES))
        assertEquals(16, mileageForSave("10,0", DistanceUnit.MILES, 17, edited = true))
    }

    @Test fun rejectsAmbiguousOrOutOfRangeMileage() {
        assertNull(mileageToKilometers("1,234.5", DistanceUnit.MILES))
        assertNull(mileageToKilometers("-1", DistanceUnit.KILOMETERS))
        assertNull(mileageToKilometers("999999999999", DistanceUnit.MILES))
    }
}
