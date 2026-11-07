package cl.cristian.bitacoraobd.data.obd.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObdPidInterpreterTest {
    private val interpreter = ObdPidInterpreter()

    @Test
    fun interpretsRpmAndZero() {
        assertEquals(1303, interpreter.rpm(listOf(0x14, 0x5F)))
        assertEquals(0, interpreter.rpm(listOf(0, 0)))
    }

    @Test
    fun interpretsCoolantTemperatureBoundaries() {
        assertEquals(-40, interpreter.coolantTempCelsius(listOf(0x00)))
        assertEquals(215, interpreter.coolantTempCelsius(listOf(0xFF)))
    }

    @Test
    fun interpretsSpeedBoundaries() {
        assertEquals(0, interpreter.speedKmh(listOf(0x00)))
        assertEquals(255, interpreter.speedKmh(listOf(0xFF)))
    }

    @Test
    fun rejectsMissingTruncatedExtraAndOutOfRangePayloads() {
        assertNull(interpreter.rpm(null))
        assertNull(interpreter.rpm(listOf(0x14)))
        assertNull(interpreter.rpm(listOf(0x14, 0x5F, 0x00)))
        assertNull(interpreter.rpm(listOf(0x100, 0x00)))

        assertNull(interpreter.coolantTempCelsius(null))
        assertNull(interpreter.coolantTempCelsius(emptyList()))
        assertNull(interpreter.coolantTempCelsius(listOf(0x00, 0x01)))
        assertNull(interpreter.coolantTempCelsius(listOf(-1)))

        assertNull(interpreter.speedKmh(null))
        assertNull(interpreter.speedKmh(emptyList()))
        assertNull(interpreter.speedKmh(listOf(0x00, 0x01)))
        assertNull(interpreter.speedKmh(listOf(0x100)))
    }
}
