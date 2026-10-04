package com.vraidev.acousticdriver

import com.vraidev.acousticdriver.core.air.*
import com.vraidev.acousticdriver.core.parser.JsonAirParser
import org.junit.Assert.*
import org.junit.Test

class JsonAirParserTest {

    private val jsonParser = JsonAirParser()

    @Test
    fun testSerializeAndDeserializeAir() {
        val original = AcousticIntermediateRepresentation(
            metadata = DriverMetadata(
                id = "json-test-1",
                name = "Cyber Acoustic",
                author = "Vrai-Dev",
                sourceDevice = "Universal",
                version = "1.0",
                description = "Test description",
                tags = listOf("Cyber", "Test")
            ),
            preampDb = -1.0f,
            masterGainDb = 2.0f,
            graphicEq = listOf(EqBand(60, 3.5f), EqBand(1000, 0f), EqBand(14000, 2.5f)),
            bassBoost = BassConfig(enabled = true, strength = 600),
            compressor = CompressorConfig(enabled = true, thresholdDb = -10f, ratio = 2f)
        )

        val jsonStr = jsonParser.serialize(original)
        assertTrue(jsonParser.canParse(jsonStr))

        val parsed = jsonParser.parse(jsonStr)
        assertEquals(original.metadata.id, parsed.metadata.id)
        assertEquals(original.metadata.name, parsed.metadata.name)
        assertEquals(original.preampDb, parsed.preampDb, 0.01f)
        assertEquals(original.graphicEq.size, parsed.graphicEq.size)
        assertEquals(original.bassBoost?.strength, parsed.bassBoost?.strength)
    }
}
