package cl.cristian.bitacoraobd.data.obd.parser

import cl.cristian.bitacoraobd.domain.obd.ObdResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObdResponseParserTest {
    private val parser = ObdResponseParser()

    @Test
    fun parsesStoredDtcsAndDeduplicatesInOrder() {
        assertEquals(
            ObdResult.Success(listOf("P0143", "P0196", "P0234")),
            parser.parseDiagnosticCodes("43 01 43 01 96 02 34")
        )
        assertEquals(
            ObdResult.Success(listOf("P0143", "P0196", "P0234")),
            parser.parseDiagnosticCodes("03\rSEARCHING...\r43 01 43 01 96 02 34\r>")
        )
        assertEquals(
            ObdResult.Success(listOf("P0143")),
            parser.parseDiagnosticCodes("43 01 43 00 00 01 43")
        )
    }

    @Test
    fun explicitZeroPairMeansCleanScan() {
        assertEquals(ObdResult.Success(emptyList<String>()), parser.parseDiagnosticCodes("43 00 00\r>"))
        assertFailure(parser.parseDiagnosticCodes("43"))
    }

    @Test
    fun rejectsNoDataAndAdapterErrors() {
        listOf("NO DATA", "?", "ERROR", "UNABLE TO CONNECT").forEach { response ->
            assertFailure(parser.parseDiagnosticCodes(response))
        }
    }

    @Test
    fun rejectsInvalidIncompleteAndUnexpectedFrames() {
        listOf("", "43 GG 01", "43 01", "43 01 43 02", "47 03 00", "41 0C 14 5F")
            .forEach { response -> assertFailure(parser.parseDiagnosticCodes(response)) }
    }

    @Test
    fun removesOnlyExactCommandEchoAndParsesPidBytes() {
        assertEquals(
            ObdResult.Success(listOf(0x14, 0x5F)),
            parser.parsePidResponse("010C\rSEARCHING...\r41 0C 14 5F\r>", 0x0C)
        )
        assertEquals(
            ObdResult.Success(listOf(0x14, 0x5F)),
            parser.parsePidResponse("410C145F>", 0x0C)
        )
    }

    @Test
    fun rejectsWrongPidAndTruncatedPidFrames() {
        assertFailure(parser.parsePidResponse("41 0D 0A", 0x0C))
        assertFailure(parser.parsePidResponse("41 0C 14", 0x0C))
        assertFailure(parser.parsePidResponse("NO DATA", 0x0C))
    }

    private fun assertFailure(result: ObdResult<*>) {
        assertTrue("Expected failure but was $result", result is ObdResult.Failure)
    }
}
